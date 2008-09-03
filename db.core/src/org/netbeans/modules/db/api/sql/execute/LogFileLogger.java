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
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.sql.loader.SQLEditorSupport;
import org.openide.util.NbBundle;

/**
 * This logger writes everything to the log file.
 *
 * @author David Van Couvering
 */
public class LogFileLogger implements SQLExecuteLogger {
    private static Logger LOGGER = Logger.getLogger(LogFileLogger.class.getName());
    
    private int errorCount;

    public void log(StatementExecutionInfo info) {
        if (info.hasExceptions()) {
            logException(info);
        }
    }

    public void finish(long executionTime) {
        LOGGER.log(Level.INFO, (NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionFinished",
                String.valueOf(millisecondsToSeconds(executionTime)),
                String.valueOf(errorCount))));
    }

    public void cancel() {
        LOGGER.log(Level.INFO, NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionCancelled"));
    }

    private void logException(StatementExecutionInfo info) {
        errorCount++;

        for(Throwable e: info.getExceptions()) {
            if (e instanceof SQLException) {
                logSQLException((SQLException)e, info);
            } else {
                LOGGER.log(Level.INFO, NbBundle.getMessage(SQLExecutor.class, "MSG_SQLExecutionException", info.getSQL()), e);
            }
        }
    }

    private void logSQLException(SQLException e, StatementExecutionInfo info) {
        while (e != null) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(SQLExecutor.class, "MSG_SQLExecutionException", info.getSQL()), e);
            e = e.getNextException();
        }
    }

    private String millisecondsToSeconds(long ms) {
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(3);
        return fmt.format(ms / 1000.0);
    }
}
