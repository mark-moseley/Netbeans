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
package org.netbeans.modules.db.dataview.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds FK of a given database table
 * 
 * @author Ahimanikya Satapathy
 */
public final class DBForeignKey extends DBObject<DBTable> {

    private static final String RS_PK_NAME = "PK_NAME"; // NOI18N
    private static final String RS_PKCATALOG_NAME = "PKTABLE_CAT"; // NOI18N
    private static final String RS_PKSCHEMA_NAME = "PKTABLE_SCHEM"; // NOI18N
    private static final String RS_PKTABLE_NAME = "PKTABLE_NAME"; // NOI18N
    private static final String RS_PKCOLUMN_NAME = "PKCOLUMN_NAME"; // NOI18N
    private static final String RS_FK_NAME = "FK_NAME"; // NOI18N
    private static final String RS_FKCOLUMN_NAME = "FKCOLUMN_NAME"; // NOI18N
    private static final String RS_UPDATE_RULE = "UPDATE_RULE"; // NOI18N
    private static final String RS_DELETE_RULE = "DELETE_RULE"; // NOI18N
    private static final String RS_DEFERRABILITY = "DEFERRABILITY"; // NOI18N
    private int deferrability;
    private int deleteRule;
    private String fkName;
    private String pkCatalog;
    private String pkName;
    private String pkSchema;
    private String pkTable;
    private int updateRule;
    private List<String> fkColumnNames = new ArrayList<String>();
    private List<String> pkColumnNames = new ArrayList<String>();

    /**
     * Creates a List of ForeignKeyColumn instances from the given ResultSet.
     */
    public static Map<String, DBForeignKey> createForeignKeyColumnMap(
            DBTable table, ResultSet rs) throws SQLException, DBException {
        assert rs != null;

        Map<String, DBForeignKey> fkColumns = new HashMap<String, DBForeignKey>();
        while (rs.next()) {
            DBForeignKey fk = (DBForeignKey) fkColumns.get(rs.getString(RS_FK_NAME));
            if (fk != null) {
                fk.addColumnNames(rs);
            } else {
                fk = new DBForeignKey(rs);
                fk.setParentObject(table);
                fkColumns.put(fk.getName(), fk);
            }
        }
        return fkColumns;
    }

    private DBForeignKey(ResultSet rs) throws SQLException {
        assert rs !=null;

        fkName = rs.getString(RS_FK_NAME);
        pkName = rs.getString(RS_PK_NAME);

        pkTable = rs.getString(RS_PKTABLE_NAME);
        pkSchema = rs.getString(RS_PKSCHEMA_NAME);

        pkCatalog = rs.getString(RS_PKCATALOG_NAME);
        addColumnNames(rs);

        //rs.getShort(RS_SEQUENCE_NUM)
        updateRule = rs.getShort(RS_UPDATE_RULE);
        deleteRule = rs.getShort(RS_DELETE_RULE);
        deferrability = rs.getShort(RS_DEFERRABILITY);
    }

    private void addColumnNames(ResultSet rs) throws SQLException {
        String pkColName = rs.getString(RS_PKCOLUMN_NAME);
        if (!isNullString(pkColName)) {
            pkColumnNames.add(pkColName);
        }

        String fkColName = rs.getString(RS_FKCOLUMN_NAME);
        if (!isNullString(pkColName)) {
            fkColumnNames.add(fkColName);
        }
    }

    public boolean contains(DBColumn fkCol) {
        return contains(fkCol.getName());
    }

    public boolean contains(String fkColumnName) {
        return fkColumnNames.contains(fkColumnName);
    }

    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof DBForeignKey)) {
            return false;
        }

        DBForeignKey ref = (DBForeignKey) refObj;

        boolean result = (fkName != null) ? fkName.equals(ref.fkName) : (ref.fkName == null);
        result &= (pkName != null) ? pkName.equals(ref.pkName) : (ref.pkName == null);
        result &= (pkTable != null) ? pkTable.equals(ref.pkTable) : (ref.pkTable == null);
        result &= (pkSchema != null) ? pkSchema.equals(ref.pkSchema) : (ref.pkSchema == null);
        result &= (pkCatalog != null) ? pkCatalog.equals(ref.pkCatalog) : (ref.pkCatalog == null);
        result &= (updateRule == ref.updateRule) && (deleteRule == ref.deleteRule) && (deferrability == ref.deferrability);
        result &= (pkColumnNames != null) ? pkColumnNames.equals(ref.pkColumnNames) : (ref.pkColumnNames != null);
        result &= (fkColumnNames != null) ? fkColumnNames.equals(ref.fkColumnNames) : (ref.fkColumnNames != null);

        return result;
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(fkColumnNames);
    }

    public String getName() {
        return fkName;
    }

    public String getPKCatalog() {
        return pkCatalog;
    }

    public List<String> getPKColumnNames() {
        return Collections.unmodifiableList(pkColumnNames);
    }

    public String getPKName() {
        return pkName;
    }

    public String getPKSchema() {
        return pkSchema;
    }

    public String getPKTable() {
        return pkTable;
    }

    @Override
    public int hashCode() {
        int myHash = (fkName != null) ? fkName.hashCode() : 0;

        myHash += (pkName != null) ? pkName.hashCode() : 0;
        myHash += (pkTable != null) ? pkTable.hashCode() : 0;
        myHash += (pkSchema != null) ? pkSchema.hashCode() : 0;
        myHash += (pkCatalog != null) ? pkCatalog.hashCode() : 0;
        myHash += updateRule + deleteRule + deferrability;
        myHash += (fkColumnNames != null) ? fkColumnNames.hashCode() : 0;
        myHash += (pkColumnNames != null) ? pkColumnNames.hashCode() : 0;

        return myHash;
    }

    public boolean references(DBPrimaryKey pk) {
        if (pk == null) {
            return false;
        }

        List<String> targetColNames = pk.getColumnNames();
        DBTable targetTable = pk.getParentObject();

        return references(targetTable) && targetColNames.containsAll(pkColumnNames) && pkColumnNames.containsAll(targetColNames);
    }

    private boolean references(DBTable aTable) {
        return (aTable != null) ? references(aTable.getName(), aTable.getSchema(), aTable.getCatalog()) : false;
    }

    private boolean references(String pkTableName, String pkSchemaName, String pkCatalogName) {
        if (pkCatalogName.equals("")) {
            pkCatalogName = null;
        }
        if (pkSchemaName.equals("")) {
            pkSchemaName = null;
        }
        if (pkTableName.equals("")) {
            pkTableName = null;
        }

        boolean tableMatches = (pkTableName != null) ? pkTableName.equals(pkTable) : (pkTable == null);
        boolean schemaMatches = (pkSchemaName != null) ? pkSchemaName.equals(pkSchema) : (pkSchema == null);
        boolean catalogMatches = (pkCatalogName != null) ? pkCatalogName.equals(pkCatalog) : (pkCatalog == null);
        return tableMatches && schemaMatches && catalogMatches;
    }
}

