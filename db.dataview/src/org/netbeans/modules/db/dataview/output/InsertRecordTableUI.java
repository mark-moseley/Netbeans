/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.output;

import org.netbeans.modules.db.dataview.table.ResultSetJXTable;
import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.sql.Types;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXTable;
import org.netbeans.modules.db.dataview.meta.DBColumn;

/**
 * @author Shankari
 */
class InsertRecordTableUI extends ResultSetJXTable {

    boolean isRowSelectionAllowed = rowSelectionAllowed;

    public InsertRecordTableUI(DataView dataView) {
        super(dataView);
        if (getRSColumnCount() < 7) {
            setAutoResizeMode(JXTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        }
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        AWTEvent awtEvent = EventQueue.getCurrentEvent();
        if (awtEvent instanceof KeyEvent) {
            KeyEvent keyEvt = (KeyEvent) awtEvent;
            if (keyEvt.getSource() != this) {
                return;
            }
            if (rowIndex == 0 && columnIndex == 0 && KeyStroke.getKeyStrokeForEvent(keyEvt).equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))) {
                DefaultTableModel model = (DefaultTableModel) getModel();
                model.addRow(createNewRow());
                rowIndex = getRowCount() - 1; //Otherwise the selection switches to the first row
                editCellAt(rowIndex, 0);
            } else if (KeyStroke.getKeyStrokeForEvent(keyEvt).equals(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT + KeyEvent.VK_TAB, 0))) {
                editCellAt(rowIndex, columnIndex);
            } else {
                editCellAt(rowIndex, columnIndex);
            }
        }
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
    }

    protected Object[] createNewRow() {
        Object[] row = new Object[getRSColumnCount()];
        for (int i = 0, I = getRSColumnCount(); i < I; i++) {
            DBColumn col = getDBColumn(i);
            if (col.isGenerated()) {
                row[i] = "<GENERATED>";
            } else if (col.hasDefault()) {
                row[i] = "<DEFAULT>";
            } else if (col.getJdbcType() == Types.TIMESTAMP) {
                row[i] = "<CURRENT_TIMESTAMP>";
            } else if (col.getJdbcType() == Types.DATE) {
                row[i] = "<CURRENT_DATE>";
            } else if (col.getJdbcType() == Types.TIME) {
                row[i] = "<CURRENT_TIME>";
            }
        }
        return row;
    }

    protected void removeRows() {
        if (isEditing()) {
            getCellEditor().cancelCellEditing();
        }
        int[] rows = getSelectedRows();
        DefaultTableModel model = (DefaultTableModel) getModel();
        for (int ii = 0; ii < rows.length; ii++) {
            if ((rows[ii] >= 0) && (getRowCount() >= 1)) {
                model.removeRow(rows[ii]);
            }
            for (int j = 0; j < rows.length; j++) {
                rows[j]--;
            }
        }
        if (getRowCount() == 0) {
            model.addRow(createNewRow());
        }
    }

}
