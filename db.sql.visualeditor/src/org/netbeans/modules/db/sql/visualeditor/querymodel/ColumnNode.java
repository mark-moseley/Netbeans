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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderMetaData;

import java.util.Collection;

/**
 * Represents a column in a SELECT clause
 */
public class ColumnNode extends ColumnItem implements Column {

    // Fields

    private TableNode   _table;

    private Identifier  _columnName;

    private Identifier	_derivedColName;      // Column alias


    // Constructors

    // Called from a number of places in the editor
    // First arg is a tableSpec, which may need to be split
    public ColumnNode(String tableSpec, String columnName) {

        String tableName=null, schemaName=null;

        // See if we've got a schema specified with the table
        String[] table = tableSpec.split("\\.");
        if (table.length>1) {
            schemaName=table[0];
            tableName=table[1];
        } else
            tableName=tableSpec;

        // Note that this will take care of delimiters if necessary
        _table = new TableNode(tableName, null, schemaName);
        _columnName=new Identifier(columnName);
     }

    // Called only from QueryModel.replaceStar
    public ColumnNode(String tableName, String columnName, String corrName, String schemaName) {
        _table=new TableNode(tableName, corrName, schemaName);
        _columnName = new Identifier(columnName);

    }

//     public ColumnNode(String tableName, String columnName, String corrName) {
//         this(tableName, columnName, corrName, null);
//     }


//     // Used mainly for "*", "?"
//     public ColumnNode(String columnName) {
//         this(null, columnName, null, null);
//     }


    // Special case where we already have the table object
    public ColumnNode(Table table, String columnName) {
        _table = (TableNode)table;
        _columnName = new Identifier(columnName);
    }

    // Ctor used by the make method
    private ColumnNode() {
    }

    // Pseudo-constructor
    // These constructors take Strings, but can't be overloaded with 'Identifier'
    // because of compiler ambiguity
    public static ColumnNode make (Identifier tableName, Identifier columnName, Identifier schemaName,
        Identifier derivedColName)
    {
        ColumnNode c = new ColumnNode();
        c._columnName = columnName;
        c._derivedColName = derivedColName;
        c._table= (tableName!=null) ? TableNode.make(tableName, null, schemaName) : null;
        return c;
    }


    // Methods

    Column getReferencedColumn() {
        return this;
    }

    public void getReferencedColumns(Collection columns) {}
    public void getQueryItems(Collection items) {}

    public boolean matches(String table, String column) {
        return (table.equals(getTableSpec()) && column.equals(getColumnName()));
    }

    public boolean matches(String table) {
        return table.equals(getTableSpec());
    }

    public boolean equals(Column column) {
        return column.matches(getTableSpec(), getColumnName());
    }

    public String genText(QueryBuilderMetaData qbMetaData, boolean select) {
        return
            // Table Spec, if any
            ( ((_table!=null) && (_table.getTableSpec()!=null)) ?
              _table.genText(qbMetaData, false)+ "."                        :       // NOI18N
              "")       			                     +  // NOI18N

            // Column Name
            _columnName.genText(qbMetaData)                                    +

            // Derived Column Name, if there is one and we're in a SELECT
            ( ((select) && (_derivedColName!=null))  ?
              " AS " + _derivedColName.genText(qbMetaData)     :                 // NOI18N
              "");                                                      // NOI18N
    }


    public String genText(QueryBuilderMetaData qbMetaData) {
        return genText(qbMetaData, false);
    }


    /**
     * Rename the table part of the column spec
     */
    public void renameTableSpec(String oldTableSpec, String corrName) {
        _table.renameTableSpec(oldTableSpec, corrName);
    }


    /**
     * set table name 
     */
    public void setTableSpec (String oldTableSpec, String newTableSpec) {
        if ( _table == null ) {
            String tableName=null, schemaName=null;

            // See if we've got a schema specified with the table
            String[] table = newTableSpec.split("\\.");
            if (table.length>1) {
                schemaName=table[0];
                tableName=table[1];
            } else
                tableName=newTableSpec;

            // Note that this will take care of delimiters if necessary
            _table = new TableNode(tableName, null, schemaName);
        }

        _table.setTableSpec ( oldTableSpec, newTableSpec );
    }


    // Accessors/Mutators

    public String getColumnName() {
        return _columnName.getName();
    }

    public String getTableSpec() {
        return (_table==null) ? null : _table.getTableSpec();
    }

    public String getFullTableName() {
        return (_table==null) ? null : _table.getFullTableName();
    }

    public String getDerivedColName() {
        return
            (_derivedColName==null) ? null : _derivedColName.getName();
    }

    public void setDerivedColName(String derivedColName) {
        _derivedColName =
            (derivedColName==null)  ? null : new Identifier(derivedColName);
    }


    /**
     * set column name 
     */
    public void setColumnName (String oldColumnName, String newColumnName) {
        if ( _columnName.getName().equals(oldColumnName) ) {
            _columnName = new Identifier(newColumnName);
        }
    }

    public void setColumnTableName (String tableName ) {
        if ( _table == null ) {
            // this should never happen.
            _table = new TableNode();
        }
        _table.setTableName (tableName);
    }

    public void setColumnCorrName (String corrName ) {
        if ( _table == null ) {
            // this should never happen.
            _table = new TableNode();
        }
        _table.setCorrName (corrName);
    }

    public boolean isParameterized() {
        return false;
    }
    
    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return null;
    }
}


