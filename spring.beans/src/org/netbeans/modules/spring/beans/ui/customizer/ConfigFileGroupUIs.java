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

package org.netbeans.modules.spring.beans.ui.customizer;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class ConfigFileGroupUIs {

    private ConfigFileGroupUIs() {}

    public static void setupGroupsList(JList list) {
        list.setCellRenderer(new ConfigFileGroupRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static void setupGroupFilesList(JList list, FileDisplayName displayName) {
        list.setCellRenderer(new ConfigFileRenderer(displayName));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static void connect(List<ConfigFileGroup> groups, JList list) {
        list.setModel(new ConfigFileGroupListModel(groups));
    }

    public static void connect(ConfigFileGroup group, JList list) {
        list.setModel(new ConfigFileListModel(group));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static String getGroupName(ConfigFileGroup group) {
        String name = group.getName();
        if (name == null || name.length() == 0) {
            name = NbBundle.getMessage(ConfigFileGroupUIs.class, "LBL_Unnamed");
        }
        return name;
    }

    public static void disconnect(JList list) {
        list.setModel(new DefaultListModel());
    }

    public static void setupConfigFileSelectionTable(JTable table, FileDisplayName displayName) {
        table.setDefaultRenderer(File.class, new ConfigFileSelectionFileRenderer(displayName));
        table.setDefaultRenderer(Boolean.class, new ConfigFileSelectionBooleanRenderer(table.getDefaultRenderer(Boolean.class)));
    }

    public static void connect(List<File> availableFiles, Set<File> alreadySelectedFiles, JTable table) {
        table.setModel(new ConfigFileSelectionTableModel(availableFiles, alreadySelectedFiles));
    }

    public static List<File> getSelectedFiles(JTable table) {
        return ((ConfigFileSelectionTableModel)table.getModel()).getSelectedFiles();
    }

    private static final class ConfigFileGroupListModel implements ListModel {

        private final List<ConfigFileGroup> groups;

        public ConfigFileGroupListModel(List<ConfigFileGroup> groups) {
            this.groups = groups;
        }

        public void addListDataListener(ListDataListener l) {
        }

        public ConfigFileGroup getElementAt(int index) {
            return groups.get(index);
        }

        public int getSize() {
            return groups.size();
        }

        public void removeListDataListener(ListDataListener l) {
        }
    }

    private static final class ConfigFileGroupRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ConfigFileGroup group = (ConfigFileGroup)value;
            component.setText(getGroupName(group));
            return component;
        }
    }

    private static final class ConfigFileListModel implements ListModel {

        private ConfigFileGroup group;

        public ConfigFileListModel(ConfigFileGroup group) {
            this.group = group;
        }

        public void addListDataListener(ListDataListener l) {
        }

        public Object getElementAt(int index) {
            return group.getFiles().get(index);
        }

        public int getSize() {
            return group.getFiles().size();
        }

        public void removeListDataListener(ListDataListener l) {
        }
    }

    private static final class ConfigFileRenderer extends DefaultListCellRenderer {

        private final FileDisplayName displayName;

        public ConfigFileRenderer(FileDisplayName displayName) {
            this.displayName = displayName;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            File file = (File)value;
            component.setText(displayName.getDisplayName(file));
            return component;
        }
    }

    private static final class ConfigFileSelectionTableModel implements TableModel {

        private final List<File> availableFiles;
        private final Set<File> alreadySelectedFiles;
        private boolean[] selected;

        public ConfigFileSelectionTableModel(List<File> availableFiles, Set<File> alreadySelectedFiles) {
            this.availableFiles = availableFiles;
            this.alreadySelectedFiles = alreadySelectedFiles;
            selected = new boolean[availableFiles.size()];
        }

        public void addTableModelListener(TableModelListener l) {
        }

        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 0) ? Boolean.class : File.class;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int columnIndex) {
            return (columnIndex == 0) ? "" : "File Name";
        }

        public int getRowCount() {
            return availableFiles.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return (columnIndex == 0) ? selected[rowIndex] : availableFiles.get(rowIndex);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        public void removeTableModelListener(TableModelListener l) {
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (isEnabled(rowIndex)) {
                selected[rowIndex] = (Boolean)aValue;
            }
        }

        public boolean isEnabled(int rowIndex) {
            return !alreadySelectedFiles.contains(availableFiles.get(rowIndex));
        }

        public List<File> getSelectedFiles() {
            List<File> result = new ArrayList<File>(availableFiles.size());
            for (int i = 0; i < availableFiles.size(); i++) {
                if (selected[i]) {
                    result.add(availableFiles.get(i));
                }
            }
            return result;
        }
    }

    private static final class ConfigFileSelectionFileRenderer extends DefaultTableCellRenderer {

        private final FileDisplayName displayName;

        public ConfigFileSelectionFileRenderer(FileDisplayName displayName) {
            this.displayName = displayName;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel component = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            File file = (File)value;
            component.setText(displayName.getDisplayName(file));
            if (!(table.getModel() instanceof ConfigFileSelectionTableModel)) {
                return component;
            }
            ConfigFileSelectionTableModel model = (ConfigFileSelectionTableModel)table.getModel();
            String toolTipText = null;
            if (!model.isEnabled(row)) {
                toolTipText = NbBundle.getMessage(ConfigFileGroupUIs.class, "LBL_FileAlreadyAdded");
            }
            component.setToolTipText(toolTipText);
            component.setEnabled(model.isEnabled(row));
            return component;
        }
    }

    private static final class ConfigFileSelectionBooleanRenderer implements TableCellRenderer {

        private final TableCellRenderer delegate;

        public ConfigFileSelectionBooleanRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = delegate.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            if (!(table.getModel() instanceof ConfigFileSelectionTableModel)) {
                return component;
            }
            ConfigFileSelectionTableModel model = (ConfigFileSelectionTableModel)table.getModel();
            component.setEnabled(model.isEnabled(row));
            String toolTipText = null;
            if (!model.isEnabled(row)) {
                toolTipText = NbBundle.getMessage(ConfigFileGroupUIs.class, "LBL_FileAlreadyAdded");
            }
            if (component instanceof JComponent) {
                ((JComponent)component).setToolTipText(toolTipText);
            }
            return component;
        }
    }

    public interface FileDisplayName {

        String getDisplayName(File file);
    }
}
