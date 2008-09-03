/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.api.sql.execute;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.sql.execute.SQLExecuteHelper;
import org.netbeans.modules.db.sql.execute.SQLExecutionLogger;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.netbeans.modules.db.sql.execute.SQLExecutionResults;

/**
 * Support execution of SQL scripts without bringing up the editor
 *
 * @author David Van Couvering
 */
public class SQLExecutor {
    private static Logger LOGGER = Logger.getLogger(SQLExecutor.class.getName());

    /**
     * Execute SQL and log summary information about the results and any errors
     * are logged to the error log file. 
     *
     * This method should not be called on the AWT event thread; to do so will
     * cause an exception to be thrown.
     *
     * @param dbconn the database connection to use when executing the SQL
     * @param sql the SQL which contains one or more valid SQL statements
     *
     * @throws IllegalStateException if this is executed on the AWT event thread
     * @throws DatabaseException if the database connection is not connected
     */
    public static SQLExecutionInfo execute(DatabaseConnection dbconn, String sql)
            throws DatabaseException {
        return execute(dbconn, sql, new LogFileLogger());
    }

    /**
     * Execute SQL.
     *
     * Note this is a very basic implementation.  Subsequent extensions could
     * include getting results back, passing in a logger, etc., but trying to
     * keep things simple for now.
     *
     * This method should not be called on the AWT event thread; to do so will
     * cause an exception to be thrown.
     *
     * @param dbconn the database connection to use when executing the SQL
     * @param sql the SQL which contains one or more valid SQL statements
     * @param logger this object is notified with execution information when execution
     *    of each statement completes and when the entire execution completes or is cancelled.
     *
     * @throws IllegalStateException if this is executed on the AWT event thread
     * @throws DatabaseException if the database connection is not connected
     */
    public static SQLExecutionInfo execute(DatabaseConnection dbconn, String sql, SQLExecuteLogger logger)
            throws DatabaseException {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("You can not run this method on the event dispatching thread."); // NOI18N
        }

        if (logger == null) {
            throw new NullPointerException();
        }

        try {
            if (dbconn.getJDBCConnection() == null || dbconn.getJDBCConnection().isClosed()) {
                throw new DatabaseException("The connection is not open"); // NOI18N
            }
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }


        SQLExecutionResults results = SQLExecuteHelper.execute(sql, 0, sql.length(),
                dbconn, new LoggerProxy(logger));

        return new SQLExecutionInfoImpl(results);
    }

    private static class SQLExecutionInfoImpl implements SQLExecutionInfo {
        private final boolean hasExceptions;
        private final List<Throwable> exceptions;
        private final List<StatementExecutionInfo> infos;

        SQLExecutionInfoImpl(SQLExecutionResults results) {
            hasExceptions = results.hasExceptions();

            exceptions = new ArrayList<Throwable>();
            infos = new ArrayList<StatementExecutionInfo>();

            for (SQLExecutionResult result : results.getResults()) {
                infos.add(new StatementExecutionInfoImpl(result));
                if (result.hasExceptions()) {
                    exceptions.addAll(result.getExceptions());
                }
            }

        }

        public boolean hasExceptions() {
            return hasExceptions;
        }

        public List<? extends Throwable> getExceptions() {
            return exceptions;
        }

        public List<StatementExecutionInfo> getStatementInfos() {
            return infos;
        }

    }

    private static class StatementExecutionInfoImpl implements StatementExecutionInfo {
        private SQLExecutionResult result;
        public StatementExecutionInfoImpl(SQLExecutionResult result) {
            this.result = result;
        }

        public String getSQL() {
            return result.getStatementInfo().getSQL();
        }

        public boolean hasExceptions() {
            return result.hasExceptions();
        }

        public Collection<Throwable> getExceptions() {
            return result.getExceptions();
        }

        public long getExecutionTime() {
            return result.getExecutionTime();
        }

    }


    private static class LoggerProxy implements SQLExecutionLogger {
        private final SQLExecuteLogger delegate;

        public LoggerProxy(SQLExecuteLogger delegate) {
            this.delegate = delegate;
        }

        public void log(SQLExecutionResult result) {
            delegate.log(new StatementExecutionInfoImpl(result));
        }

        public void finish(long executionTime) {
            delegate.finish(executionTime);
        }

        public void cancel() {
            delegate.cancel();
        }

    }

}
