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
 * SortableDDTableModel.java -- synopsis.
 *
 */
package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.util.*;

import javax.swing.table.*;

/**
 * Table model using the composite pattern allowing DDTableModels to be sorted.
 * The model allows any column containing a java.util.Comparable type to be
 * sorted. The sorting model is index based and therefore only sorts the view
 * into the underlying model and not the model itself.
 *
 * @author Chris Webster
 */
//
// 29-may-2001
//	Change for bug 4457984 - pass the new methods of DDTableModel onto
//      the delegate and added parameter to addRowAt. (joecorto)
//
public class SortableDDTableModel extends AbstractTableModel 
implements DDTableModel {
    private DDTableModel modelDelegate;
    private boolean needsSorting;
    private List modelIndex;
    private Comparator comp;
    private int sortColumn;

    public SortableDDTableModel(DDTableModel model) {
        modelDelegate = model;
	modelIndex = new ArrayList(getRowCount());
	for (int i =0; i < getRowCount(); i++) {
	    modelIndex.add(new DelegateReference(i));
	}

	comp = new Comparator() {
	    public boolean equals(Object other) {
	        return this == other;	
	    }

	    public int compare(Object o1, Object o2) {
                if (!(o1 instanceof DelegateReference) ||
		    !(o2 instanceof DelegateReference))
                {
		    throw new ClassCastException();
		}
		DelegateReference d1 = (DelegateReference) o1;
		DelegateReference d2 = (DelegateReference) o2;
		Comparable compo1 = 
                    (Comparable) modelDelegate.getValueAt(d1.ref,
                                                          getSortColumn());
	        Object comp2 = modelDelegate.getValueAt(d2.ref, 
							  getSortColumn());

		  
		if (compo1 == null && comp2 == null) {
		    return 0;
		}
		      
		if (compo1 == null) {
		    return -1;	  
		}

		if (comp2 == null) {
		    return 1;
		}
			    
		return compo1.compareTo(comp2);
            }
        };
	setSortColumn(-1);
    }

    public int getSortColumn() {
        return sortColumn;
    }

    public boolean isSortable() {
	return Comparable.class.isAssignableFrom(
	     getColumnClass(getSortColumn()));
    }

    private static class DelegateReference {
        public DelegateReference(int ref) {
	    this.ref = ref;
	}
	public int ref;
    }

    public void setSortColumn(int col) {
        if (col < 0 || col >= getColumnCount()) {
	    sortColumn = col;
	    return;
        }

        if (sortColumn != col || needsSorting) {
	    sortColumn = col;
	    sort(); 
	    needsSorting = false;
	}
    }

    private void sort() {
        Collections.sort(modelIndex,comp);
        fireTableDataChanged();
    } 

    private int getInd(int row) {
        return ((DelegateReference) modelIndex.get(row)).ref;
    }

    public int getColumnCount() {
        return modelDelegate.getColumnCount();
    }

    public String getColumnName(int col) {
	return modelDelegate.getColumnName(col);
    }

    public DDTableModelEditor getEditor() {
        return modelDelegate.getEditor();
    }
    
    public boolean isEditValid (Object value, int row) {
	return modelDelegate.isEditValid (value, row);
    }

    public List canRemoveRow (int row) {
	return modelDelegate.canRemoveRow (row);
    }

    public List isValueValid(Object value, int fromRow) {
        return modelDelegate.isValueValid(value, 
					  fromRow==-1?-1:getInd(fromRow));
    }
    
    public Object getValueAt(int row, int col) {
	return modelDelegate.getValueAt(getInd(row), col);
    }

    public String getModelName() {
	return modelDelegate.getModelName();
    }
        
    public Object [] getValue () {
	Object[] rv = modelDelegate.getValue();
	/* XXXX Ask Chris about this.
	for (int i = 0; i < rv.length; i++) {
	    rv[i] = getValueAt(i);
	}*/
	return rv;
    }
   
    public int getRowCount () {
        return modelDelegate.getRowCount();
    }

    public Class getColumnClass (int col) {
	return modelDelegate.getColumnClass(col);
    }

    public boolean isCellEditable(int row, int col) {
	return modelDelegate.isCellEditable(getInd(row),col);
    }

    public Object getValueAt (int row) {
	return modelDelegate.getValueAt(getInd(row));    
    }

    public void setValueAt (int row, Object value) {
        modelDelegate.setValueAt(getInd(row), value);
	needsSorting = true;
	setSortColumn(getSortColumn());
    }

    public void setValueAt (Object value, int row, int col) {
	modelDelegate.setValueAt(value, getInd(row), col);
	needsSorting = true;
	setSortColumn(getSortColumn());
    }

    public Object makeNewElement() {
	return modelDelegate.makeNewElement();
    }

    public void newElementCancelled(Object obj) {
	modelDelegate.newElementCancelled(obj);
    }

    public void editsCancelled() {
	modelDelegate.editsCancelled();
    }
       
    public void addRowAt(int row, Object newVal, Object editVal) {
        if (row == -1) {
	    row = getRowCount();
	} else {
	    row++;
	}

	modelIndex.add(row, new DelegateReference(getRowCount()));
	modelDelegate.addRowAt(-1, newVal, editVal);
	fireTableRowsInserted(row,row);
	needsSorting = true;
	setSortColumn(getSortColumn());
    }

    public void removeRowAt(int row) {
	int delegateRow = getInd(row);
	modelDelegate.removeRowAt(delegateRow);
	modelIndex.remove(row);
	Iterator it = modelIndex.iterator();
	while (it.hasNext()) {
	    DelegateReference del = (DelegateReference) it.next();
		if (del.ref >= delegateRow) {
		    del.ref--;
		}
	    fireTableRowsDeleted(row, row);
        }
    }
}
