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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.DisabledReason;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class TableUISupport {

    private TableUISupport() {
    }

    public static JList createTableList() {
        return new TableJList();
    }

    public static void connectAvailable(JList availableTablesList, TableClosure tableClosure) {
        availableTablesList.setModel(new AvailableTablesModel(tableClosure));

        if (!(availableTablesList.getCellRenderer() instanceof AvailableTableRenderer)) {
            availableTablesList.setCellRenderer(new AvailableTableRenderer());
        }
    }

    public static void connectSelected(JList selectedTablesList, TableClosure tableClosure) {
        selectedTablesList.setModel(new SelectedTablesModel(tableClosure));

        if (!(selectedTablesList.getCellRenderer() instanceof SelectedTableRenderer)) {
            selectedTablesList.setCellRenderer(new SelectedTableRenderer());
        }
    }

    public static Set<Table> getSelectedTables(JList list) {
        Set<Table> result = new HashSet<Table>();

        Object[] selectedValues = list.getSelectedValues();
        for (int i = 0; i < selectedValues.length; i++) {
            result.add((Table)selectedValues[i]);
        }

        return result;
    }

    public static void connectClassNames(JTable table, SelectedTables selectedTables) {
        table.setModel(new TableClassNamesModel(selectedTables));
        setRenderer(table.getColumnModel().getColumn(0));
        setRenderer(table.getColumnModel().getColumn(1));
    }

    private static void setRenderer(TableColumn column) {
        if (!(column.getCellRenderer() instanceof TableClassNameRenderer)) {
            column.setCellRenderer(new TableClassNameRenderer());
        }
    }

    private static abstract class TableModel extends AbstractListModel {

        public abstract Table getElementAt(int index);
    }

    private static final class AvailableTablesModel extends TableModel implements ChangeListener {

        private final TableClosure tableClosure;

        private List<Table> displayTables;

        public AvailableTablesModel(TableClosure tableClosure) {
            this.tableClosure = tableClosure;
            tableClosure.addChangeListener(this);
            refresh();
        }

        public Table getElementAt(int index) {
            return displayTables.get(index);
        }

        public int getSize() {
            return displayTables != null ? displayTables.size() : 0;
        }

        public void stateChanged(ChangeEvent event) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            displayTables = new ArrayList<Table>(tableClosure.getAvailableTables());
            Collections.sort(displayTables);
            fireIntervalRemoved(this, 0, oldSize);
            fireIntervalAdded(this, 0, getSize());
        }
    }

    private static final class SelectedTablesModel extends TableModel implements ChangeListener {

        private final TableClosure tableClosure;

        private List<Table> displayTables;

        public SelectedTablesModel(TableClosure tableClosure) {
            this.tableClosure = tableClosure;
            tableClosure.addChangeListener(this);
            refresh();
        }

        public Table getElementAt(int index) {
            return displayTables.get(index);
        }

        public int getSize() {
            return displayTables != null ? displayTables.size() : 0;
        }

        public void stateChanged(ChangeEvent event) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            displayTables = new ArrayList<Table>(tableClosure.getSelectedTables());
            Collections.sort(displayTables);
            fireIntervalRemoved(this, 0, oldSize);
            fireIntervalAdded(this, 0, getSize());
        }

        public TableClosure getTableClosure() {
            return tableClosure;
        }
    }

    private static final class AvailableTableRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            DisabledReason disabledReason = null;
            Object displayName = null;

            if (value instanceof Table) {
                Table tableItem = (Table)value;
                disabledReason = tableItem.getDisabledReason();
                if (disabledReason!= null) {
                    displayName = NbBundle.getMessage(TableUISupport.class, "LBL_TableNameWithDisabledReason", tableItem.getName(), disabledReason.getDisplayName());
                } else {
                    if(tableItem.isTable())
                        displayName = tableItem.getName();
                    else
                        displayName = tableItem.getName() + NbBundle.getMessage(TableUISupport.class, "LBL_DB_VIEW");
                }
            }

            JLabel component = (JLabel)super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            component.setEnabled(disabledReason == null);
            component.setToolTipText(disabledReason != null ? disabledReason.getDescription() : null);

            return component;
        }

    }

    private static final class SelectedTableRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Table table = null;
            Object displayName = null;
            boolean referenced = false;
            TableClosure tableClosure = null;

            if (value instanceof Table) {
                table = (Table)value;
                if(((Table)value).isTable())
                    displayName = table.getName();
                else
                    displayName = table.getName() + NbBundle.getMessage(TableUISupport.class, "LBL_DB_VIEW");

                if (list.getModel() instanceof SelectedTablesModel) {
                    SelectedTablesModel model = (SelectedTablesModel)list.getModel();
                    tableClosure = model.getTableClosure();
                    referenced = tableClosure.getReferencedTables().contains(table);
                }
            } else {
                displayName = value;
            }

            JLabel component = (JLabel)super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            component.setEnabled(!referenced);
            component.setToolTipText(referenced ? getTableTooltip(table, tableClosure) : null); // NOI18N

            return component;
        }

        private static String getTableTooltip(Table table, TableClosure tableClosure) {
            List<Table> tables = new ArrayList<Table>();
            Set<Table> relatedTables;
            String bundleKey;

            if (table.isJoin()) {
                relatedTables = table.getReferencedTables();
                bundleKey = "LBL_RelatedTableJoin"; // NOI18N
            } else {
                relatedTables = table.getReferencedByTables();
                bundleKey = "LBL_RelatedTableRefBy"; // NOI18N
            }
            for (Iterator<Table> i = relatedTables.iterator(); i.hasNext();) {
                Table refTable = i.next();
                if (tableClosure.getSelectedTables().contains(refTable)) {
                    tables.add(refTable);
                }
            }
            return NbBundle.getMessage(TableUISupport.class, bundleKey, createTableList(tables));
        }

        private static String createTableList(List<Table> tables) {
            assert tables.size() > 0;

            if (tables.size() == 1) {
                return tables.iterator().next().getName();
            }

            Collections.sort(tables);

            String separator = NbBundle.getMessage(TableUISupport.class, "LBL_TableListSep");
            Iterator<Table> i = tables.iterator();
            StringBuilder builder = new StringBuilder(i.next().getName());
            String lastTable = i.next().getName();
            while (i.hasNext()) {
                builder.append(separator);
                builder.append(lastTable);
                lastTable = i.next().getName();
            }
            return NbBundle.getMessage(TableUISupport.class, "LBL_TableList", builder.toString(), lastTable);
        }
    }

    private static final class TableClassNamesModel extends AbstractTableModel {

        private SelectedTables selectedTables;
        private final List<Table> tables;

        public TableClassNamesModel(SelectedTables selectedTables) {
            this.selectedTables = selectedTables;
            this.tables = selectedTables.getTables();
        }

        public Table getTableAt(int rowIndex) {
            return tables.get(rowIndex);
        }

        public boolean isValidClass(Table table) {
            return !selectedTables.hasProblem(table);
        }

        public String getProblemDisplayName(Table table) {
            return selectedTables.getProblemDisplayNameForTable(table);
        }

        public int getRowCount() {
            return tables.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return tables.get(rowIndex).getName();

                case 1:
                    Table table = tables.get(rowIndex);
                    return selectedTables.getClassName(table);

                default:
                    assert false;
            }

            return null;
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex != 1) {
                return;
            }

            Table table = tables.get(rowIndex);
            selectedTables.setClassName(table, (String)value);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            Table table = tables.get(rowIndex);
            return !table.isJoin() && columnIndex == 1;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return NbBundle.getMessage(TableUISupport.class, "LBL_DatabaseTable");

                case 1:
                    return NbBundle.getMessage(TableUISupport.class, "LBL_ClassName");

                default:
                    assert false;
            }

            return null;
        }
    }

    private static final class TableClassNameRenderer extends DefaultTableCellRenderer {
        private static Color errorForeground;
        private static Color nonErrorForeground;

        static {
            errorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
            if (errorForeground == null) {
                errorForeground = Color.RED;
            }
            nonErrorForeground = UIManager.getColor("Label.foreground"); // NOI18N
        }

        public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean joinTable = false;
            boolean validClass = true;
            String problemDisplayName = null;

            if (jTable.getModel() instanceof TableClassNamesModel) {
                TableClassNamesModel model = (TableClassNamesModel)jTable.getModel();
                Table table = model.getTableAt(row);
                joinTable = table.isJoin();
                if (column == 1) {
                    validClass = model.isValidClass(table);
                    if (!validClass) {
                        problemDisplayName = model.getProblemDisplayName(table);
                    }
                }
            }

            Object realValue = null;
            if (joinTable && column == 1) {
                realValue = NbBundle.getMessage(TableUISupport.class, "LBL_JoinTable");
            } else {
                realValue = value;
            }
            JComponent component = (JComponent)super.getTableCellRendererComponent(jTable, realValue, isSelected, hasFocus, row, column);
            component.setEnabled(!joinTable);
            component.setToolTipText(joinTable ? NbBundle.getMessage(TableUISupport.class, "LBL_JoinTableDescription") : problemDisplayName);
            component.setForeground((validClass) ? nonErrorForeground : errorForeground);
           
            return component;
        }
    }

    private static final class TableJList extends JList {

        @Override
        public int getNextMatch(String prefix, int startIndex, Bias bias) {
            ListModel model = getModel();
            if (!(model instanceof TableModel)) {
                return super.getNextMatch(prefix, startIndex, bias);
            }
            TableModel tablesModel = (TableModel)model;
            int max = tablesModel.getSize();
            int increment = (bias == Bias.Forward) ? 1 : -1;
            int index = startIndex;
            prefix = prefix.toUpperCase();
            do {
                Table table = tablesModel.getElementAt(index);
                String tableName = table.getName().toUpperCase();
                if (tableName.startsWith(prefix)) {
                    return index;
                }
                index = (index + increment + max) % max;
            } while (index != startIndex);
            return -1;
        }
    }
}
