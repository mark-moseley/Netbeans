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

import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL Atomic Formula
 * Example Form: a.x = b.y
 */
public final class Predicate implements Expression {

    // Fields

    // ToDo: Generalize this, to allows arbitrary forms on both sides

    Value _val1, _val2;
//    ColumnNode _col1, _col2;
    String _op;


    // Constructors

    public Predicate() {
    }

    public Predicate(Value val1, Value val2) {
        this(val1, val2, "=");  // NOI18N
    }

    public Predicate (Value val1, Value val2, String op) {
        _val1 = val1;
        _val2 = val2;
        _op = op;
        }

    // Special ctor used when we have an FK
    public Predicate (String[] rel) {
        _val1 = new ColumnNode(rel[0], rel[1]);
        _val2 = new ColumnNode(rel[2], rel[3]);
        _op = "=";
    }

    // Methods

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        if ((_val1 instanceof ColumnNode) && (_val2 instanceof ColumnNode)) {
            ColumnNode col1 = (ColumnNode) _val1;
            ColumnNode col2 = (ColumnNode) _val2;
            if ((col1.matches(table1, column1) && col2.matches(table2, column2)) ||
                (col2.matches(table1, column1) && col1.matches(table2, column2))) {
                return this;
            }
        }
        return null;
    }

    // get the columns specified in the condition if any
    public void getReferencedColumns(Collection columns) {
        if (_val1 instanceof ColumnItem) {
            columns.add (((ColumnItem)_val1).getReferencedColumn());
        }

        if (_val2 instanceof ColumnItem) {
            columns.add(((ColumnItem)_val2).getReferencedColumn());
        }
    }

    // Return the Where clause as a SQL string
    public String genText(SQLIdentifiers.Quoter quoter) {
        return _val1.genText(quoter) + " " + _op + " " + _val2.genText(quoter); // NOI18N
    }

    public Value getVal1() {
        return _val1;
    }

    public void setVal1(Value val1 ) {
        _val1 = val1;
    }

    public Value getVal2() {
        return _val2;
    }

    public void setVal2(Value val2) {
        _val2 = val2;
    }

    public String getOp() {
        return _op;
    }

    public void setFields(String tableName1, String columnName1, String tableName2, String columnName2)
    {
        _val1 = new ColumnNode(tableName1, columnName1);
        _val2 = new ColumnNode(tableName2, columnName2);
    }

    /**
     * Return true if the Predicate is a criterion, rather than a relationship
     */
    public boolean isCriterion () {

        // If both sides of the predicate are columns, we have a relationship
        return ( ! ((getVal1() instanceof ColumnNode) && (getVal2() instanceof ColumnNode)));
    }

    /** Rename any column specs that use the old table spec
     */
    public void renameTableSpec(String oldTableSpec, String corrName) {

        if (_val1 instanceof ColumnNode)
            ((ColumnNode)_val1).renameTableSpec(oldTableSpec, corrName);
        if (_val2 instanceof ColumnNode)
            ((ColumnNode)_val2).renameTableSpec(oldTableSpec, corrName);
    }

    public boolean isParameterized() {
        return (_val1.isParameterized() || _val2.isParameterized());
    }

}
