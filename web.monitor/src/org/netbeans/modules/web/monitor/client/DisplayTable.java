/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * DisplayTable.java
 *
 *
 * Created: Mon Jan 29 16:43:09 2001
 *
 * @author Ana von Klopp
 * @version
 */

// PENDING - tidy up this one, the editable variable is dodgy...

package org.netbeans.modules.web.monitor.client;

import javax.swing.*;     // widgets
import javax.swing.border.Border;     // widgets
import javax.swing.table.*;     // widgets
import javax.swing.event.TableModelEvent;     // widgets
import javax.swing.event.TableModelListener;     // widgets
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.util.*;

import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;

public class DisplayTable extends JTable {

    private static final boolean debug = false;
     
    public static final int UNEDITABLE = 0;
    public static final int REQUEST = 1;
    public static final int SERVER = 2;
    public static final int HEADERS = 3;
    public static final int PARAMS = 4;
    public static final int COOKIES = 5;

    private static final ResourceBundle msgs =
       NbBundle.getBundle(TransactionView.class);  

    private int numRows = 0;
    private int numCols = 3;

    private Object[][] data = null;

    private TableCellEditor[][] cellEditors = null;

    /** determines whether the names of a table are editable or not.
     */
    private boolean editableNames = false;
    private int editable = UNEDITABLE;
    private boolean sortAscending = true;

    public DisplayTable(String[] categories) {
	this(categories, null, UNEDITABLE);
    }

    public DisplayTable(String[] categories, int editable) {
	this(categories, null, editable);
    }

    public DisplayTable(String[] names, String[] values) {
	this(names, values, UNEDITABLE);
    }

    public DisplayTable(String[] names, String[] values, int editable) {
	super();

	numRows = names.length;
	editableNames = false;
	this.editable = editable;
	
	data = new Object[numRows][numCols];
	cellEditors = new TableCellEditor[numRows][numCols];
	for(int i=0; i<numRows; ++i) {
	    data[i][0] = names[i];
	    if (values == null) {
		data[i][1] = new String(""); // NOI18N
	    } else {
		data[i][1] = values[i];
	    }
	    data[i][2] = msgs.getString("MON_Edit_dots"); // NOI18N
	    cellEditors[i][2] =
		NameValueCellEditor.createCellEditor((JTable)this, data,
						     false, i, editable);
	}
	setMyModel(data, editable > UNEDITABLE);
	setup();
    }

    public DisplayTable(Param[] params) {
	this(params, UNEDITABLE);
    }
    
    public DisplayTable(Param[] params, int editable) {

	super();

	numRows = params.length;
	if(editable < 3) 
	    editableNames = false; 
	else
	    editableNames = true; 

	this.editable = editable; 

	data = new Object[numRows][numCols];
	cellEditors = new TableCellEditor[numRows][numCols];
	for(int i=0; i<numRows; ++i) {
	    data[i][0] = params[i].getAttributeValue("name");   // NOI18N
	    data[i][1] = params[i].getAttributeValue("value");  // NOI18N
	    data[i][2] = msgs.getString("MON_Edit_dots"); // NOI18N
	    cellEditors[i][2] =
		NameValueCellEditor.createCellEditor((JTable)this, data,
						     true, i, editable);
	}
	setMyModel(data, editable > UNEDITABLE); 
	setup();
    }

    private void setup() {
	setBorderAndColorScheme();
	Dimension margins = new Dimension(6, 4);
	setIntercellSpacing(margins);
    }

    /**
     * Set the border and colors for the table.
     * Depends on whether the table is ediable or not.
     */
    public void setBorderAndColorScheme() {
	setBorderAndColorScheme(editable != UNEDITABLE); 
    }

    public void setBorderAndColorScheme(boolean editable) {
	Color bg;
	this.setBorder(BorderFactory.createLoweredBevelBorder());
	if (!editable) { 
	    //bg = SystemColor.control;
	    bg = this.getBackground().darker();
	} else {
	    bg = Color.white;
	}
	this.setTableHeader(null);
	this.setBackground(bg);
    }
    
    /**
     * Set the choices used for a cell.
     * This makes the cell use a combo box for its editor.
     * If editable is true, then the combobox is set to editable.
     *
     * This method has been superceded. Remove from RequestPanel. 
     *
     * @return the combobox that is used as the editor.
     */
    public JComboBox setChoices(int row, int col, String[] choices, boolean editable) {
	JComboBox box = new JComboBox(choices);
	box.setEditable(editable);
	TableCellEditor ed = new DefaultCellEditor(box);
	cellEditors[row][col] = ed;

	// if the table is editable, we should turn off the [...] editor
	// when there's a choice on the row.
	data[row][2]=msgs.getString("MON_Editing");
	cellEditors[row][2] = null;
	
	return box;
    }

    /**
     * Override the getter for the cell editors, so that customized
     * cell editors will show up.
     */
    public TableCellEditor getCellEditor(int row, int col) {
	TableCellEditor ed = cellEditors[row][col];
	if (ed == null) {
	    return super.getCellEditor(row, col);
	}
	return ed;
    }

    public void setSortAscending(boolean ascending) {
	this.sortAscending = ascending;
    }

    public void sortByNameAscending() {
	((SortingModel)getModel()).sort(true); 
    }

    public void sortByNameDescending() {
	((SortingModel)getModel()).sort(false); 
    }

    public void sortByName() {
	((SortingModel)getModel()).sort(sortAscending); 
    }

    public void sortByName(boolean asc) {
	((SortingModel)getModel()).sort(asc); 
    }
    
    public void noSorting() {
	if(debug) System.out.println("Neutral sorting selected"); //NOI18N
	//setMyModel(data, EDITABLE > UNEDITABLE);
    }

    private void setMyModel(Object[][] data, boolean canEdit) {
	 
		
	SortingModel sorter = new SortingModel(data, canEdit, 
					       editable > 2); 
	
	super.setModel(sorter);
	
	// PENDING - I can't get the column size to shrink the way I'd 
	// like it. This works in the AttValTable but not here for
	// some reason.
	TableColumnModel tcm = getColumnModel();
	if (tcm.getColumnCount() > 0) {
	    TableColumn column = tcm.getColumn(0);     
	    column.setPreferredWidth(10);
	    tcm.getColumn(2).setMaxWidth(5);
	}
    }

    /**
     * Check if the cell is being edited and return that value instead.
     */
    public Object getPossiblyEditingValueAt(int row, int col) {
	if (isEditing()) {
	    int editR = getEditingRow();
	    int editC = getEditingColumn();

	    if (row == editR && col == editC) {
		TableCellEditor tce = getCellEditor(editR, editC);
		tce.stopCellEditing();
	    }
	}
	return getValueAt(row, col);
    }
	
    public void addTableModelListener(TableModelListener tml) {
	TableModel tableModel = getModel();
	if (tableModel != null) {
	    tableModel.addTableModelListener(tml);
	}
    }
    public void removeTableModelListener(TableModelListener tml) {
	TableModel tableModel = getModel();
	if (tableModel != null) {
	    tableModel.removeTableModelListener(tml);
	}
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void reset() {
	for(int i=0; i<numRows; ++i) setValueAt("", i, 1); //NOI18N
    }

    public Object[][] getData() {
	return data;
    }
} // DisplayTable
