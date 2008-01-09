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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import org.openide.util.NbBundle;
import javax.swing.table.DefaultTableModel;

class QueryBuilderInputTableModel extends DefaultTableModel {

    // Variables

    final String[] columnNames = {
        // "Column",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "COLUMN"),       // NOI18N
        // "Alias",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "ALIAS"),         // NOI18N
        // "Table",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "TABLE"),        // NOI18N
        // "Output",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OUTPUT"),       // NOI18N
        // "Sort Type",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "SORT_TYPE"),        // NOI18N
        // "Sort Order",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "SORT_ORDER"),       // NOI18N
        // "Criteria",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "CRITERIA"),         // NOI18N
        // "Criteria Order"
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "CRITERIA_ORDER"),       // NOI18N
        // "Or...",
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
        // "Or...",
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
        // "Or..."
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
    };

    Object[][] data = {
        { "", "", "", "", Boolean.FALSE, "", "", "" /*, "", "", "" */ }       // NOI18N
    };


    // Constructor

    public QueryBuilderInputTableModel ()
    {
        super(0, 10);
        setColumnIdentifiers ( columnNames );
    }


    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        if ( getRowCount() == 0 ) return String.class;       // NOI18N
        if ( getValueAt(0,c) == null ) return String.class;      // NOI18N
        return getValueAt(0, c).getClass();
    }


    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if ((col==QueryBuilderInputTable.Column_COLUMN) ||
            (col==QueryBuilderInputTable.Table_COLUMN)) {
            return false;
        }
        else if ( col==QueryBuilderInputTable.Criteria_COLUMN &&
                  getValueAt(row, col).equals (
                      QueryBuilderInputTable.Criteria_Uneditable_String) ) {
            return false;
        }
        else if ( col==QueryBuilderInputTable.CriteriaOrder_COLUMN &&
                  getValueAt(row, col).equals (
                    QueryBuilderInputTable.CriteriaOrder_Uneditable_String ) ) {
            return false;
        }
        else {
            return true;
        }
    }
}

