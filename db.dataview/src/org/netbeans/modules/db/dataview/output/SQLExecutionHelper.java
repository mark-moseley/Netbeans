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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.db.dataview.output;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.util.RequestProcessor;

/**
 * This class assumes there will be only one connection which can't be closed.
 * 
 * @author Ahimanikya Satapathy
 */
class SQLExecutionHelper {

    private final DataView dataView;
    private final DatabaseConnection dbConn;
    private static Logger mLogger = Logger.getLogger(SQLExecutionHelper.class.getName());
    // the RequestProcessor used for executing statements.
    private final RequestProcessor rp = new RequestProcessor("SQLStatementExecution", 1, true); // NOI18N


    SQLExecutionHelper(DataView dataView, DatabaseConnection dbConn) {
        this.dataView = dataView;
        this.dbConn = dbConn;
    }

    static void initialDataLoad(DataView dv, DatabaseConnection dbConn, SQLExecutionHelper execHelper) throws DBException, SQLException {
        Statement stmt = null;
        try {
            Connection conn = DBConnectionFactory.getInstance().getConnection(dbConn);
            DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);

            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(dv.getDataViewPageContext().getPageSize());

            ResultSet resultSet = null;
            try {
                resultSet = stmt.executeQuery(dv.getQueryString());
                Collection<DBTable> tables = dbMeta.generateDBTables(resultSet);

                dv.tblMeta = new DataViewDBTable(tables);
                if (!tables.isEmpty()) {
                    for (DBTable tbl : tables) {
                        tbl.setEditable(tables.size() == 1 && !tbl.getName().equals(""));
                    }
                }
                execHelper.loadDataFrom(resultSet);
            } finally {
                DataViewUtils.closeResources(resultSet);
            }

            ResultSet cntResultSet = null;
            try {
                cntResultSet = stmt.executeQuery(SQLStatementGenerator.getCountSQLQuery(dv.getQueryString()));
                execHelper.setTotalCount(cntResultSet);
            } finally {
                DataViewUtils.closeResources(cntResultSet);
            }
        } finally {
            DataViewUtils.closeResources(stmt);
        }
    }

    void executeInsert(final String[] insertSQL, final Object[] insertedRow) {

        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, "Executing Insert", "") {

            @Override
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                PreparedStatement pstmt = conn.prepareStatement(insertSQL[0]);
                try {
                    int pos = 1;
                    for (int i = 0; i < insertedRow.length; i++) {
                        if (insertedRow[i] != null) {
                            DBReadWriteHelper.setAttributeValue(pstmt, pos++, dataView.getDataViewDBTable().getColumnType(i), insertedRow[i]);
                        }
                    }

                    int rows = pstmt.executeUpdate();
                    if (rows != 1) {
                        error = true;
                        errorMsg = "No rows inserted ";
                    }
                } finally {
                    DataViewUtils.closeResources(pstmt);
                }
            }

            @Override
            public void finished() {
                dataView.setEditable(true);
                commitOrRollback("Insert command");
                if (dataView.getDataViewPageContext().getTotalRows() <= 0) {
                    dataView.getDataViewPageContext().setTotalRows(1);
                }
            }

            @Override
            protected void executeOnSucess() {
                SQLExecutionHelper.this.executeQuery();
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void executeDeleteRow(final DataViewTableUI rsTable) {

        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, "Executing Delete", "") {

            @Override
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                int[] rows = rsTable.getSelectedRows();
                for (int j = 0; j < rows.length; j++) {
                    deleteARow(rows[j], rsTable.getModel());
                }
            }

            private void deleteARow(int rowNum, TableModel tblModel) throws SQLException, DBException {
                final List<Object> values = new ArrayList<Object>();
                final List<Integer> types = new ArrayList<Integer>();

                SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
                final String[] deleteStmt = generator.generateDeleteStatement(types, values, rowNum, tblModel);
                final String rawDeleteStmt = deleteStmt[1];
                mLogger.info("Statement: " + rawDeleteStmt);
                dataView.setInfoStatusText("Statement: " + rawDeleteStmt);

                PreparedStatement pstmt = conn.prepareStatement(deleteStmt[0]);
                try {
                    int pos = 1;
                    for (Object val : values) {
                        DBReadWriteHelper.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                        pos++;
                    }
                    int rows = pstmt.executeUpdate();
                    if (rows == 0) {
                        error = true;
                        errorMsg = errorMsg + "No matching row(s) to delete.\n";
                    } else if (rows > 1) {
                        error = true;
                        errorMsg = errorMsg + "No unique row for the matching condition.\n";
                    }
                } finally {
                    DataViewUtils.closeResources(pstmt);
                }
            }

            @Override
            public void finished() {
                dataView.setEditable(true);
                commitOrRollback("Delete command");
            }

            @Override
            protected void executeOnSucess() {
                SQLExecutionHelper.this.executeQuery();
            }
        };

        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void executeUpdate() {

        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, "Executing Update", "") {

            private PreparedStatement pstmt;

            @Override
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                UpdatedRowContext tblContext = dataView.getUpdatedRowContext();
                for (String key : tblContext.getUpdateKeys()) {
                    updateARow(key);
                }
            }

            private void updateARow(String key) throws SQLException, DBException {
                UpdatedRowContext tblContext = dataView.getUpdatedRowContext();
                final String updateStmt = tblContext.getUpdateStmt(key);
                final String rawUpdateStmt = tblContext.getRawUpdateStmt((key));
                final List<Object> values = tblContext.getValueList(key);
                final List<Integer> types = tblContext.getTypeList(key);

                mLogger.info("Statement: " + rawUpdateStmt);
                dataView.setInfoStatusText("Statement: " + rawUpdateStmt);

                pstmt = conn.prepareStatement(updateStmt);
                int pos = 1;
                for (Object val : values) {
                    DBReadWriteHelper.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                    pos++;
                }

                try {
                    int rows = pstmt.executeUpdate();
                    if (rows == 0) {
                        error = true;
                        errorMsg = errorMsg + "No matching row(s) to update.\n";
                    } else if (rows > 1) {
                        error = true;
                        errorMsg = errorMsg + "No unique row for the matching condition.\n";
                    }
                } finally {
                    DataViewUtils.closeResources(pstmt);
                }
            }

            @Override
            public void finished() {
                dataView.setEditable(true);
                commitOrRollback("Update command");
            }

            @Override
            protected void executeOnSucess() {
                SQLExecutionHelper.this.executeQuery();
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    // Truncate is allowed only when there is single table used in the query.
    void executeTruncate() {
        String msg = "Truncating Table from database, please wait...";
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, "Executing Truncate", msg) {

            private Statement stmt = null;

            @Override
            public void execute() throws SQLException, DBException {
                stmt = conn.createStatement();

                DBTable dbTable = dataView.getDataViewDBTable().geTable(0);
                String truncateSql = "Truncate table " + dbTable.getFullyQualifiedName();
                try {
                    mLogger.info("Trncating Table Using: " + truncateSql);
                    stmt.executeUpdate(truncateSql);
                } catch (SQLException sqe) {
                    mLogger.info("TRUNCATE Not supported...will try DELETE * \n");
                    truncateSql = "Delete from " + dbTable.getFullyQualifiedName();
                    mLogger.info("Trncating Table Using: " + truncateSql);
                    stmt.executeUpdate(truncateSql);
                } finally {
                    DataViewUtils.closeResources(stmt);
                }
            }

            @Override
            public void finished() {
                commitOrRollback("Truncate command");
            }

            @Override
            protected void executeOnSucess() {
                SQLExecutionHelper.this.executeQuery();
            }
        };

        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    // Once Data View is created the it assumes the query never changes.
    void executeQuery() {
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, "Executing Query", dataView.getQueryString()) {

            private ResultSet rs = null;
            private ResultSet crs = null;
            private Statement stmt = null;
            boolean lastEditState = dataView.isEditable();

            // Execute the Select statement
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(dataView.getDataViewPageContext().getPageSize());

                // Execute the query
                try {
                    rs = stmt.executeQuery(dataView.getQueryString());
                    loadDataFrom(rs);
                } finally {
                    DataViewUtils.closeResources(rs);
                }

                // Get total row count
                try {
                    crs = stmt.executeQuery(SQLStatementGenerator.getCountSQLQuery(dataView.getQueryString()));
                    setTotalCount(crs);
                } finally {
                    DataViewUtils.closeResources(crs);
                }
            }

            public void finished() {
                DataViewUtils.closeResources(stmt);
                dataView.setEditable(lastEditState);
                synchronized (dataView) {
                    if (error) {
                        dataView.setErrorStatusText(ex.getMessage());
                    }
                    dataView.resetToolbar(error);
                    dataView.setRowsInTableModel();
                }
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void loadDataFrom(ResultSet rs) throws SQLException {
        if (rs == null) {
            return;
        }

        int pageSize = dataView.getDataViewPageContext().getPageSize();
        int startFrom = dataView.getDataViewPageContext().getCurrentPos() - 1;
        DataViewDBTable tblMeta = dataView.getDataViewDBTable();

        List<Object[]> rows = new ArrayList<Object[]>();
        int colCnt = tblMeta.getColumnCount();
        try {
            rs.setFetchSize(pageSize);

            // Skip till current position
            boolean lastRowPicked = rs.next();
            while (lastRowPicked && rs.getRow() < (startFrom + 1)) {
                lastRowPicked = rs.next();
            }

            // Get next page
            int rowCnt = 0;
            while (((pageSize == -1) || (pageSize > rowCnt)) && (lastRowPicked || rs.next())) {
                Object[] row = new Object[colCnt];
                for (int i = 0; i < colCnt; i++) {
                    int type = tblMeta.getColumn(i).getJdbcType();
                    row[i] = DBReadWriteHelper.readResultSet(rs, type, i + 1);
                }
                rows.add(row);
                rowCnt++;
                if (lastRowPicked) {
                    lastRowPicked = false;
                }
            }
        } catch (SQLException e) {
            mLogger.info(" Failed to set up table model " + DBException.getMessage(e));
            throw e;
        } finally {
            dataView.getDataViewPageContext().setCurrentRows(rows);
        }
    }

    void setTotalCount(ResultSet countresultSet) {
        try {
            if (countresultSet == null) {
                dataView.getDataViewPageContext().setTotalRows(0);
            } else {
                if (countresultSet.next()) {
                    int count = countresultSet.getInt(1);
                    dataView.getDataViewPageContext().setTotalRows(count);
                }
            }
        } catch (SQLException ex) {
            mLogger.info("Could not get total row count " + ex);
        }
    }
}
