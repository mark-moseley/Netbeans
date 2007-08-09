/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.dlg;

import org.netbeans.modules.db.util.DDLTestBase;

/**
 *
 * @author David
 */
public class AddTableColumnDDLTest extends DDLTestBase {

    public AddTableColumnDDLTest(String name) {
        super(name);
    }

    /**
     * Basic test, nothing fancy
     */
    public void testAddTableColumn() throws Exception {
        String tablename = "testAddColumn";
        String colname = "testColumn";
        String pkeyName = "id";
        
        super.createBasicTable(tablename, pkeyName);
        
        addColumn(tablename, colname);
        
        // Now verify the column exists
        assertTrue(columnExists(quote(tablename), quote(colname)));
    }

    
    private void addColumn(String tablename, String colname) throws Exception {
        AddTableColumnDDL ddl = new AddTableColumnDDL(
                spec, drvSpec, SCHEMA, tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, colname);
        TypeElement type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        ddl.setColumn(colname, col);
        
        ddl.execute();
    }

    public void testAddColumnToIndex() throws Exception {
        String tablename = "testAddColumn";
        String firstColname = "firstColumn";
        String secondColname = "secondColumn";
        String pkeyName = "id";
        String indexName = "idx";

        super.createBasicTable(tablename, pkeyName);     
        addColumn(tablename, firstColname);
        super.createSimpleIndex(tablename, indexName, firstColname);        

        AddTableColumnDDL ddl = new AddTableColumnDDL(
                spec, drvSpec, SCHEMA, tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, secondColname);
        TypeElement type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        col.setProperty(ColumnItem.INDEX, new Boolean(true));
        ddl.setColumn(secondColname, col);
        ddl.setIndexName(indexName);
        
        ddl.execute();
        
        // Now verify the column exists and is part of the index
        assertTrue(columnInIndex(quote(tablename), quote(secondColname), 
            quote(indexName)));        
        
    }    
}
