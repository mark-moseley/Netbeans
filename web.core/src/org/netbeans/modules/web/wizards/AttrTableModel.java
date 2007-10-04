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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.web.wizards;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;

import org.openide.util.NbBundle;

/**
 *
 * @author  mk115033
 */
public class AttrTableModel extends AbstractTableModel {

    public AttrTableModel() {
    }

    private boolean debug;
    private String[] colheaders = null;
    private Object[][] data = null;
    private int numCols;
    private int numRows=0;

    /** Creates a new instance of AttrTableModel */
    AttrTableModel(String[] headers) { 
        this.colheaders = headers; 
        numCols = colheaders.length;
        
    }
    
    AttrTableModel(String[] headers, Object[][] data) { 
        this.colheaders = headers; 
        numCols = colheaders.length;
        this.data=data;
        numRows = data.length;
    }
    
    public String getColumnName(int col) { 
        String key = "LBL_".concat(colheaders[col]); //NOI18N
        return NbBundle.getMessage(AttrTableModel.class, key); 
    }

    public int getRowCount() { return numRows; }
    public int getColumnCount() { return numCols; }

    public Object getValueAt(int row, int col) { 
        return data[row][col];
    }
    public int addRow(String name, String type, boolean required, boolean rtexpr) { 

        Object[][] data2 = new Object[numRows+1][numCols]; 
        int i=0, j=0; 

        if(numRows > 0) { 
            for(j=0; j<numRows; ++j) 
                data2[j] = data[j]; 
        }

        data2[j][0] = name;
        data2[j][1] = type;
        data2[j][2] = Boolean.valueOf(required);
        data2[j][3] = Boolean.valueOf(rtexpr);
        data = data2; 
        numRows++;
        return j; 
    }

    public void removeRow(int row) { 

        if(debug) { 
            log("::removeRow()"); //NOI18N
            log("row is " + row); //NOI18N
            log("numRows is " + numRows); //NOI18N
        }

        Object[][] data2 = new Object[numRows-1][numCols]; 
        int newRowIndex = 0; 
        for(int i=0; i<numRows; ++i) { 
            if(debug) log("\tExamining row " + i); //NOI18N 
            if(i==row) continue; 
            if(debug) log("\tKeep this row"); //NOI18N
            data2[newRowIndex]=data[i]; 
            newRowIndex++;
            if(debug) log("\tnewRowIndex is " + newRowIndex); //NOI18N
        }
        data = data2; 
        numRows = --numRows; 
    }

    public void setData(String name, String value, boolean required, boolean rtexpr, int row) { 
        data[row][0] = name;
        data[row][1] = value;
        data[row][2] = Boolean.valueOf(required);
        data[row][3] = Boolean.valueOf(rtexpr);
        fireTableChanged(new TableModelEvent(this, row)); 
    } 

    public void setValueAt(Object value, int row, int col) {

        if(debug) 
            log("::setValueAt(): value = " + value + //NOI18N
                " at " + row + ", " + col); //NOI18N

        data[row][col] = value;

        if(debug) { 
            for(int i=0; i<data.length; ++i) { 
                for(int j=0; j<numCols; ++j) { 
                    log("\t" + String.valueOf(i) + "," + //NOI18N
                        String.valueOf(j) + ": " + data[i][j]); //NOI18N
                }
            }
        } 
        // Commenting this out since the value is set twice. 
        fireTableCellUpdated(row, col);
    }

    private void log(String s) { 
        System.out.println("AttrTableModel:"+s); //NOI18N
    }
    
    public Object[][] getAttributes() {
        if (data==null) return new Object[][]{};
        else return data;
    }
}
