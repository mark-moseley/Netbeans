/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder.model;

import org.netbeans.modules.jdbcwizard.builder.model.DBQueryModel;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBColumn;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Venkat P
 */
public class DerbyQueryGenerator implements DBQueryModel {
	
    private String mtabName = null;

    private List mcolumns = null;

    private List mInsertColumns = null;

    private List mUpdateColumns = null;

    private List mChooseColumns = null;

    private List mPollColumns = null;

    private String mSchemaName = null;

    private static final String INSERT_QUERY = "insertQuery";

    private static final String UPDATE_QUERY = "updateQuery";

    private static final Logger mLog = Logger.getLogger(DerbyQueryGenerator.class.getName());
    
    
    private DerbyQueryGenerator(){
    	
    }
    
    private static final DerbyQueryGenerator instance = new DerbyQueryGenerator();
    
    public static final DerbyQueryGenerator getInstance(){
    	if(instance != null)
    		return instance;
    	else
    		return new DerbyQueryGenerator();
    }
    /*
     * (non-Javadoc)
     * 
     * @see com.sun.jbi.ui.devtool.jdbc.builder.model.DBQueryModel#init(com.sun.jbi.ui.devtool.jdbc.builder.dbmodel.DBTable)
     */
    public void init(final DBTable souTable) {
        this.mtabName = souTable.getName();
        this.mcolumns = souTable.getColumnList();
        this.mInsertColumns = new ArrayList();
        this.mUpdateColumns = new ArrayList();
        this.mChooseColumns = new ArrayList();
        this.mPollColumns = new ArrayList();

        for (int cnt = 0; cnt < this.mcolumns.size(); cnt++) {
            final DBColumn temp = (DBColumn) this.mcolumns.get(cnt);
            if (temp.isInsertSelected()) {
                this.mInsertColumns.add(temp);
            }
            if (temp.isUpdateSelected()) {
                this.mUpdateColumns.add(temp);
            }
            if (temp.isChooseSelected()) {
                this.mChooseColumns.add(temp);
            }
            if (temp.isPollSelected()) {
                this.mPollColumns.add(temp);
            }
        }
        this.mSchemaName = souTable.getSchema();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createInsertQuery() throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("insert into");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");
        sb.append(" ");
        sb.append("(");
        for (int i = 0; i < this.mInsertColumns.size(); i++) {
            sb.append("\"" + ((DBColumn) this.mInsertColumns.get(i)).getName() + "\"");
            if (i == this.mInsertColumns.size() - 1) {
                sb.append(")");
            } else {
                sb.append(",");
            }
        }
        sb.append(" ");
        sb.append("values (");
        for (int i = 0; i < this.mInsertColumns.size(); i++) {
            sb.append("?");
            if (i == this.mInsertColumns.size() - 1) {
                sb.append(")");
            } else {
                sb.append(",");
            }
        }
        DerbyQueryGenerator.mLog.log(Level.INFO, "Generated Insert Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createUpdateQuery() throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("update");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");
        sb.append(" ");
        sb.append("set ");
        for (int i = 0; i < this.mUpdateColumns.size(); i++) {
            sb.append("\"" + this.mtabName + "\"");
            sb.append(".");
            sb.append("\"" + ((DBColumn) this.mUpdateColumns.get(i)).getName() + "\"");
            sb.append("  ");
            sb.append("=");
            sb.append(" ?");
            if (i != this.mUpdateColumns.size() - 1) {
                sb.append(",");
            }
        }
        DerbyQueryGenerator.mLog.log(Level.INFO, "Generated Update Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createDeleteQuery() throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("delete");
        sb.append(" ");
        sb.append("from");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");
        DerbyQueryGenerator.mLog.log(Level.INFO, "Generated Delete Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createFindQuery() throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("select");
        sb.append(" ");

        for (int i = 0; i < this.mChooseColumns.size(); i++) {
            sb.append("\"" + ((DBColumn) this.mChooseColumns.get(i)).getName() + "\"");
            if (i == this.mChooseColumns.size() - 1) {
                sb.append(" ");
            } else {
                sb.append(",");
            }
        }
        // selected columns

        sb.append("from");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");

        DerbyQueryGenerator.mLog.log(Level.INFO, "Generated Find Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createPoolQuery() throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("select");
        sb.append(" ");
        for (int i = 0; i < this.mPollColumns.size(); i++) {
            sb.append("\"" + ((DBColumn) this.mPollColumns.get(i)).getName() + "\"");
            if (i == this.mPollColumns.size() - 1) {
                sb.append(" ");
            } else {
                sb.append(",");
            }
        }
        // selected columns
        sb.append("from");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");
        DerbyQueryGenerator.mLog.log(Level.INFO, "Generated Pool Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String getParamOrder(final String queryType) throws Exception {
        final StringBuffer sb = new StringBuffer();
        List tempList = null;
        if (DerbyQueryGenerator.INSERT_QUERY.equals(queryType)) {
            tempList = this.mInsertColumns;
        }
        if (DerbyQueryGenerator.UPDATE_QUERY.equals(queryType)) {
            tempList = this.mUpdateColumns;
        }
        for (int i = 0; i < tempList.size(); i++) {
            sb.append(((DBColumn) tempList.get(i)).getName());
            if (i == tempList.size() - 1) {
                sb.append("");
            } else {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String getPrimaryKey() throws Exception {
        String pKey = null;
        final java.util.Iterator pkIter = this.mcolumns.iterator();
        while (pkIter.hasNext()) {
            final DBColumn tc = (DBColumn) pkIter.next();
            if (tc.isPrimaryKey()) {
                pKey = tc.getName();
                break;
            }
        }
        return pKey;
    }
}
