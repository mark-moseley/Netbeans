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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * SheetTableModel.java
 *
 * Created on December 13, 2002, 6:14 PM
 */
package org.openide.explorer.propertysheet;

import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


/** Column model for the property sheet table.  This class primarily exists because
 *  dragging to move the center line of the table is much faster without a
 *  DefaultTableColumnModel firing spurious change events while dragging.
 *
 * @author  Tim Boudreau
 */
final class SheetColumnModel implements TableColumnModel {
    static final Object NAMES_IDENTIFIER = "names"; //NOI18N
    static final Object VALUES_IDENTIFIER = "values"; //NOI18N
    TableColumn namesColumn;
    TableColumn valuesColumn;
    ListSelectionModel lsm = new DefaultListSelectionModel();

    /** Creates a new instance of SheetTableModel */
    public SheetColumnModel() {
        namesColumn = new TableColumn(0);
        namesColumn.setIdentifier(NAMES_IDENTIFIER);
        valuesColumn = new TableColumn(1);
        valuesColumn.setIdentifier(VALUES_IDENTIFIER);
        namesColumn.setMinWidth(60);
        valuesColumn.setMinWidth(30);
    }

    public void addColumn(TableColumn aColumn) {
        throw new UnsupportedOperationException("Adding columns not supported"); //NOI18N
    }

    public void addColumnModelListener(TableColumnModelListener x) {
        //do nothing - no events wil happen
    }

    public TableColumn getColumn(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return namesColumn;

        case 1:
            return valuesColumn;
        }

        throw new IllegalArgumentException("Property sheet only has 2 columns - " + Integer.toString(columnIndex)); //NOI18N
    }

    public int getColumnCount() {
        return 2;
    }

    public int getColumnIndex(Object columnIdentifier) {
        if (columnIdentifier instanceof String) {
            if (columnIdentifier.equals(NAMES_IDENTIFIER)) {
                return 0;
            }

            if (columnIdentifier.equals(VALUES_IDENTIFIER)) {
                return 1;
            }
        }

        throw new IllegalArgumentException("Illegal value: " + columnIdentifier);
    }

    public int getColumnIndexAtX(int xPosition) {
        int width0 = namesColumn.getWidth();

        if (xPosition < width0) {
            return 0;
        }

        if (xPosition < (width0 + valuesColumn.getWidth())) {
            return 1;
        }

        return -1;
    }

    public int getColumnMargin() {
        return 1; //XXX fix
    }

    public boolean getColumnSelectionAllowed() {
        return false;
    }

    public Enumeration<TableColumn> getColumns() {
        return new Enumeration<TableColumn>() {
                private boolean done = false;
                private boolean doneOne = false;

                public boolean hasMoreElements() {
                    return !done;
                }

                public TableColumn nextElement() {
                    if (done) {
                        return null;
                    }

                    if (doneOne) {
                        done = true;

                        return valuesColumn;
                    }

                    doneOne = true;

                    return namesColumn;
                }
            };
    }

    public int getSelectedColumnCount() {
        return 0;
    }

    public int[] getSelectedColumns() {
        return new int[] {  };
    }

    public ListSelectionModel getSelectionModel() {
        return lsm;
    }

    public int getTotalColumnWidth() {
        return namesColumn.getWidth() + valuesColumn.getWidth();
    }

    public void moveColumn(int columnIndex, int newIndex) {
        //do nothing
    }

    public void removeColumn(TableColumn column) {
        throw new UnsupportedOperationException("Deleting columns not supported"); //NOI18N
    }

    public void removeColumnModelListener(TableColumnModelListener x) {
        //do nothing, columns will not change
    }

    public void setColumnMargin(int newMargin) {
        //do nothing, unsupported
    }

    public void setColumnSelectionAllowed(boolean flag) {
        //do nothing, unsupported
    }

    public void setSelectionModel(ListSelectionModel newModel) {
        //do nothing, unsupported
    }
}
