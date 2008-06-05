/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.db.sql.execute;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.db.core.SQLOptions;
import org.netbeans.modules.db.sql.history.SQLHistoryManager;

/**
 * Support class for executing SQL statements.
 *
 * @author Andrei Badea
 */
public final class SQLExecuteHelper {

    private static final Logger LOGGER = Logger.getLogger(SQLExecuteHelper.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    /**
     * Executes a SQL string, possibly containing multiple statements. Returns the execution
     * result, but only if the string contained a single statement.
     *
     * @param sqlScript the SQL script to execute. If it contains multiple lines
     * they have to be delimited by '\n' characters.
     */
    public static SQLExecutionResults execute(String sqlScript, int startOffset, int endOffset, Connection conn, ProgressHandle progressHandle, SQLExecutionLogger executionLogger) {
        
        boolean cancelled = false;
        
        List<StatementInfo> statements = getStatements(sqlScript, startOffset, endOffset);
        boolean computeResults = statements.size() == 1;
        
        List<SQLExecutionResult> resultList = new ArrayList<SQLExecutionResult>();
        long totalExecutionTime = 0;
        
        for (Iterator i = statements.iterator(); i.hasNext();) {
            
            cancelled = Thread.currentThread().isInterrupted();
            if (cancelled) {
                break;
            }
            
            StatementInfo info = (StatementInfo)i.next();
            String sql = info.getSQL();

            if (LOG) {
                LOGGER.log(Level.FINE, "Executing: " + sql);
            }
            
            SQLExecutionResult result = null;
            Statement stmt = null;

            try {
                if (sql.startsWith("{")) { // NOI18N
                    stmt = conn.prepareCall(sql);
                } else {
                    stmt = conn.createStatement();
                }
                
                // Issue 133814 - Set max rows to a reasonably large size,
                // to avoid getting out-of-memory errors if the driver
                // (e.g. MySQL) tries to load all the rows of a Very Big
                // Table before returning
                stmt.setMaxRows(SQLOptions.getDefault().getMaxRows());
                
                boolean isResultSet = false;
                long startTime = System.currentTimeMillis();
                if (stmt instanceof PreparedStatement) {
                    isResultSet = ((PreparedStatement)stmt).execute();
                } else {
                    isResultSet = stmt.execute(sql);
                }
                long executionTime = System.currentTimeMillis() - startTime;
                totalExecutionTime += executionTime;

                // Save SQL statements executed for the SQLHistoryManager
                SQLHistoryManager.getInstance().saveSQL(new SQLHistory(sql, conn.getMetaData().getURL(), new Date(startTime)));

                if (isResultSet) {
                    result = new SQLExecutionResult(info, stmt, stmt.getResultSet(), executionTime);
                } else {
                    result = new SQLExecutionResult(info, stmt, stmt.getUpdateCount(), executionTime);
                }
            } catch (SQLException e) {
                result = new SQLExecutionResult(info, stmt, e);
            }
            assert result != null;
            
            executionLogger.log(result);
            
            if (LOG) {
                LOGGER.log(Level.FINE, "Result: " + result);
            }
            
            if (computeResults || result.getException() != null) {
                resultList.add(result);
            } else {
                try {
                    result.close();
                } catch (SQLException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }
        }
        
        if (!cancelled) {
            executionLogger.finish(totalExecutionTime);
        } else {
            if (LOG) {
                LOGGER.log(Level.FINE, "Execution cancelled"); // NOI18N
            }
            executionLogger.cancel();
        }
        
        SQLExecutionResults results = new SQLExecutionResults(resultList);
        if (!cancelled) {
            return results;
        } else {
            results.close();
            return null;
        }
    }

    private static int[] getSupportedResultSetTypeConcurrency(Connection conn) throws SQLException {
        // XXX some drivers don't implement the DMD.supportsResultSetConcurrency() method
        // for example the MSSQL WebLogic driver 4v70rel510 always throws AbstractMethodError
        
        DatabaseMetaData dmd = conn.getMetaData();
        
        int type = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int concurrency = ResultSet.CONCUR_UPDATABLE;
        if (!dmd.supportsResultSetConcurrency(type, concurrency)) {
            concurrency = ResultSet.CONCUR_READ_ONLY;
            if (!dmd.supportsResultSetConcurrency(type, concurrency)) {
                type = ResultSet.TYPE_FORWARD_ONLY;
            }
        }
        return new int[] { type, concurrency };
    }
    
    private static List<StatementInfo> getStatements(String script, int startOffset, int endOffset) {
        List<StatementInfo> allStatements = split(script);
        if (startOffset == 0 && endOffset == script.length()) {
            return allStatements;
        }
        List<StatementInfo> statements = new ArrayList<StatementInfo>();
        for (Iterator i = allStatements.iterator(); i.hasNext();) {
            StatementInfo stmt = (StatementInfo)i.next();
            if (startOffset == endOffset) {
                // only find the statement at offset startOffset
                if (stmt.getRawStartOffset() <= startOffset && stmt.getRawEndOffset() >= endOffset) {
                    statements.add(stmt);
                }
            } else {
                // find the statements between startOffset and endOffset
                if (stmt.getStartOffset() >= startOffset && stmt.getEndOffset() <= endOffset) {
                    statements.add(stmt);
                }
            }
        }
        return Collections.unmodifiableList(statements);
    }
        
    static List<StatementInfo> split(String script) {
        return new SQLSplitter(script).getStatements();
    }
    
    private static final class SQLSplitter {
        
        private static final int STATE_MEANINGFUL_TEXT = 0;
        private static final int STATE_MAYBE_LINE_COMMENT = 1;
        private static final int STATE_LINE_COMMENT = 2;
        private static final int STATE_MAYBE_BLOCK_COMMENT = 3;
        private static final int STATE_BLOCK_COMMENT = 4;
        private static final int STATE_MAYBE_END_BLOCK_COMMENT = 5;
        private static final int STATE_STRING = 6;
        
        private String sql;
        private int sqlLength;
        
        private StringBuffer statement = new StringBuffer();
        private List<StatementInfo> statements = new ArrayList<StatementInfo>();
        
        private int pos = 0;
        private int line = -1;
        private int column;
        private boolean wasEOL = true;
        
        private int rawStartOffset;
        private int startOffset;
        private int startLine;
        private int startColumn;
        private int endOffset;
        private int rawEndOffset;
        
        private int state = STATE_MEANINGFUL_TEXT;
        
        private String delimiter = ";"; // NOI18N
        private static final String DELIMITER_TOKEN = "delimiter"; // NOI18N
                
        /**
         * @param sql the SQL string to parse. If it contains multiple lines
         * they have to be delimited by '\n' characters.
         */
        public SQLSplitter(String sql) {
            assert sql != null;
            this.sql = sql;
            sqlLength = sql.length();
            parse();
        }
        
        private void parse() {
            checkDelimiterStatement();
            while (pos < sqlLength) {
                char ch = sql.charAt(pos);
                
                if (ch == '\r') { // NOI18N
                    // the string should not contain these
                    if (LOG) {
                        LOGGER.log(Level.FINE, "The SQL string contained non-supported \r characters."); // NOI18N
                    }
                    continue;
                }
                
                nextColumn();
                
                switch (state) {
                    case STATE_MEANINGFUL_TEXT:
                        if (ch == '-') {
                            state = STATE_MAYBE_LINE_COMMENT;
                        } else if (ch == '/') {
                            state = STATE_MAYBE_BLOCK_COMMENT;
                        } else if (ch == '\'') {
                            state = STATE_STRING;
                        }
                        break;
                        
                    case STATE_MAYBE_LINE_COMMENT:
                        if (ch == '-') {
                            state = STATE_LINE_COMMENT;
                        } else {
                            state = STATE_MEANINGFUL_TEXT;
                            statement.append('-'); // previous char
                            endOffset = pos;
                        }
                        break;
                        
                    case STATE_LINE_COMMENT:
                        if (ch == '\n') {
                            state = STATE_MEANINGFUL_TEXT;
                            // avoid appending the final \n to the result
                            pos++;
                            continue;
                        } 
                        break;
                        
                    case STATE_MAYBE_BLOCK_COMMENT:
                        if (ch == '*') {
                            state = STATE_BLOCK_COMMENT;
                        } else {
                            statement.append('/'); // previous char
                            endOffset = pos;
                            if (ch != '/') {
                                state = STATE_MEANINGFUL_TEXT;
                            }
                        }
                        break;
                        
                    case STATE_BLOCK_COMMENT:
                        if (ch == '*') {
                            state = STATE_MAYBE_END_BLOCK_COMMENT;
                        }
                        break;
                        
                    case STATE_MAYBE_END_BLOCK_COMMENT:
                        if (ch == '/') {
                            state = STATE_MEANINGFUL_TEXT;
                            // avoid writing the final / to the result
                            pos++;
                            continue;
                        } else if (ch != '*') {
                            state = STATE_BLOCK_COMMENT;
                        }
                        break;
                        
                    case STATE_STRING:
                        if (ch == '\n' || ch == '\'') {
                            state = STATE_MEANINGFUL_TEXT;
                        }
                        break;
                        
                    default:
                        assert false;
                }
                
                if (state == STATE_MEANINGFUL_TEXT && isDelimiter()) {
                    rawEndOffset = pos;
                    addStatement();
                    statement.setLength(0);
                    rawStartOffset = pos + delimiter.length(); // skip the delimiter
                    pos += delimiter.length();
                    
                    // See if the next statement changes the delimiter
                    // Note how we skip over a 'delimiter' statement - it's not
                    // something we send to the server.
                    checkDelimiterStatement();
                } else if (state == STATE_MEANINGFUL_TEXT || state == STATE_STRING) {
                    // don't append leading whitespace
                    if (statement.length() > 0 || !Character.isWhitespace(ch)) {
                        // remember the position of the first appended char
                        if (statement.length() == 0) {
                            startOffset = pos;
                            endOffset = pos;
                            startLine = line;
                            startColumn = column;
                        }
                        statement.append(ch);
                        // the end offset is the character after the last non-whitespace character
                        if (state == STATE_STRING || !Character.isWhitespace(ch)) {
                            endOffset = pos + 1;
                        }
                    }
                    pos++;
                } else {
                    pos++;
                }
            }
            
            rawEndOffset = pos;
            addStatement();
        }
        
        /**
         * See if the user wants to use a different delimiter for splitting
         * up statements.  This is useful if, for example, their SQL contains
         * stored procedures or triggers or other blocks that contain multiple
         * statements but should be executed as a single unit. 
         * 
         * If we see the delimiter token, we read in what the new delimiter 
         * should be, and then return the new character position past the
         * delimiter statement, as this shouldn't be passed on to the 
         * database.
         */
        private void checkDelimiterStatement() {
            skipWhitespace();
                        
            if ( pos == sqlLength) {
                return;
            }
            
            if ( ! isToken(DELIMITER_TOKEN)) {
                return;
            }
            
            // Skip past the delimiter token
            int tokenLength = DELIMITER_TOKEN.length();
            pos += tokenLength;
            
            skipWhitespace();
            
            int endPos = pos;
            while ( endPos < sqlLength &&
                    ! Character.isWhitespace(sql.charAt(endPos))) {
                endPos++;
            }
            
            if ( pos == endPos ) {
                return;
            }
            
            delimiter = sql.substring(pos, endPos);
            
            pos = endPos;
            statement.setLength(0);
            rawStartOffset = pos;
        }
        
        private void skipWhitespace() {
            while ( pos < sqlLength && Character.isWhitespace(sql.charAt(pos)) ) {
                nextColumn();
                pos++;
            }            
        }
        
        private boolean isDelimiter() {
            int length = delimiter.length();
            
            if ( pos + length > sqlLength) {
                return false;
            }
            
            for ( int i = 0 ; i < length ; i++ ) {
                if (delimiter.charAt(i) != sql.charAt(pos + i)) {
                    return false;
                }
                i++;
            }
            
            return true;
        }
        
        private void nextColumn() {
            if (wasEOL) {
                line++;
                column = 0;
                wasEOL = false;
            } else {
                column++;
            }
                            
            if (sql.charAt(pos) == '\n') {
                wasEOL = true;
            }
        }
        
        
        /** 
         * See if the SQL text starting at the given position is a given token 
         * 
         * @param sql - the full SQL text
         * @param ch - the character at the current position
         * @param pos - the current position index for the SQL text
         * @param token - the token we are looking for
         * 
         * @return true if the token is found at the current position
         */
        private boolean isToken(String token) {
            char ch = sql.charAt(pos);
            
            // Simple check to see if there's potential.  In most cases this
            // will return false and we don't have to waste our time doing
            // any other processing.  Move along, move along...
            if ( Character.toUpperCase(ch) != 
                    Character.toUpperCase(token.charAt(0)) ) {
                return false;
            }

            // Don't want to recognize larger strings that contain the token
            if ( pos > 0 &&  !Character.isWhitespace(sql.charAt(pos - 1)) ) {
                return false;
            }
            
            if ( sql.length() > pos + token.length() &&
                    Character.isLetterOrDigit(sql.charAt(pos + token.length())) ) {
                return false;
            }
        

            // Create a substring that contains just the potential token
            // This way we don't have to uppercase the entire SQL string.
            String substr;
            try {
                substr = sql.substring(pos, pos + token.length()); // NOI18N
            } catch ( IndexOutOfBoundsException e ) {
                return false;
            }
            
            if ( substr.toUpperCase().equals(token.toUpperCase())) { // NOI8N
                return true;
            }
            
            return false;            
        }
        
        private void addStatement() {
            // PENDING since startOffset is the first non-whitespace char and
            // endOffset is the offset after the last non-whitespace char,
            // the trim() call could be replaced with statement.substring(startOffset, endOffset)
            String sql = statement.toString().trim();
            if (sql.length() <= 0) {
                return;
            }
            
            StatementInfo info = new StatementInfo(sql, rawStartOffset, startOffset, startLine, startColumn, endOffset, rawEndOffset);
            statements.add(info);
        }
        
        public List<StatementInfo> getStatements() {
            return Collections.unmodifiableList(statements);
        }
    }
}
