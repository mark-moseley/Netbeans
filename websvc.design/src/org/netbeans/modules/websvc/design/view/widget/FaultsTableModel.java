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

package org.netbeans.modules.websvc.design.view.widget;

import org.netbeans.modules.websvc.design.javamodel.MethodModel;

/**
 *
 * @author Ajit
 */
public class FaultsTableModel implements TableModel{
    
    private transient MethodModel method;
    
    
    /**
     *
     * @param method
     */
    public FaultsTableModel(MethodModel method) {
        this.method = method;
        
    }
    
    public int getRowCount() {
        return method.getFaults().size();
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
        case 0:
            return "Parameter Name";
        case 1:
            return "Parameter Type";
        default:
            throw new IllegalArgumentException("");
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch(columnIndex) {
        case 0:
            return false;
        case 1:
            return false;
        default:
            return false;
        }
    }
    
    public String getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            switch(columnIndex) {
            case 0:
                return method.getFaults().get(rowIndex).getName();
            case 1:
                return method.getFaults().get(rowIndex).getFaultType();
            default:
                throw new IllegalArgumentException("");
            }
        }
        return null;
    }
    
    public void setValueAt(String aValue, int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            switch(columnIndex) {
            case 0:
                throw new IllegalArgumentException("");
            case 1:
                throw new IllegalArgumentException("");
            default:
                throw new IllegalArgumentException("");
            }
        }
    }
    
}
