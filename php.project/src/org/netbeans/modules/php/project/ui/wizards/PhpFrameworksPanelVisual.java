/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * List of frameworks is "copied" from web project.
 * @author Tomas Mysik
 */
public class PhpFrameworksPanelVisual extends JPanel implements HelpCtx.Provider, TableModelListener, ListSelectionListener, ChangeListener {
    private static final int STEP_INDEX = 2;
    private static final long serialVersionUID = 158602680330133653L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final FrameworksTableModel model;
    private final Map<PhpFrameworkProvider, PhpModuleExtender> extenders;

    private PhpModuleExtender actualExtender;

    public PhpFrameworksPanelVisual(PhpFrameworksPanel wizardPanel, Map<PhpFrameworkProvider, PhpModuleExtender> extenders) {
        assert extenders != null;
        this.extenders = extenders;

        // Provide a name in the title bar.
        setName(wizardPanel.getSteps()[STEP_INDEX]);
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, STEP_INDEX);
        // Step name (actually the whole list for reference).
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardPanel.getSteps());

        initComponents();

        // frameworks
        model = new FrameworksTableModel();
        frameworksTable.setModel(model);
        createFrameworksList();

        FrameworksTableCellRenderer renderer = new FrameworksTableCellRenderer(model);
        renderer.setBooleanRenderer(frameworksTable.getDefaultRenderer(Boolean.class));
        frameworksTable.setDefaultRenderer(PhpFrameworkProvider.class, renderer);
        frameworksTable.setDefaultRenderer(Boolean.class, renderer);
        initTableVisualProperties();

        changeDescriptionAndPanel();
    }

    public void addPhpFrameworksListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removePhpFrameworksListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public Map<PhpFrameworkProvider, PhpModuleExtender> getSelectedExtenders() {
        Map<PhpFrameworkProvider, PhpModuleExtender> selectedExtenders = new LinkedHashMap<PhpFrameworkProvider, PhpModuleExtender>();
        for (int i = 0; i < model.getRowCount(); ++i) {
            FrameworkModelItem item = model.getItem(i);
            if (item.isSelected()) {
                PhpFrameworkProvider framework = item.getFramework();
                assert framework != null;
                PhpModuleExtender extender = extenders.get(framework);
                selectedExtenders.put(framework, extender);
            }
        }

        return selectedExtenders;
    }

    public PhpModuleExtender getSelectedVisibleExtender() {
        int selectedRow = frameworksTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        FrameworkModelItem item = model.getItem(selectedRow);
        assert item != null;
        if (item.isSelected()) {
            return extenders.get(item.getFramework());
        }
        return null;
    }

    public void markInvalidFrameworks(Set<PhpFrameworkProvider> invalidFrameworks) {
        for (int i = 0; i < model.getRowCount(); ++i) {
            FrameworkModelItem item = model.getItem(i);
            item.setValid(!invalidFrameworks.contains(item.getFramework()));
        }
    }

    public HelpCtx getHelpCtx() {
        for (Component component : configPanel.getComponents()) {
            if (component instanceof HelpCtx.Provider) {
                HelpCtx helpCtx = ((HelpCtx.Provider) component).getHelpCtx();
                if (helpCtx != null) {
                    return helpCtx;
                }
            }
        }
        return null;
    }

    public void tableChanged(TableModelEvent e) {
        changeDescriptionAndPanel();
    }

    public void valueChanged(ListSelectionEvent e) {
        changeDescriptionAndPanel();
    }

    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    private void createFrameworksList() {
        for (PhpFrameworkProvider provider : extenders.keySet()) {
            model.addItem(new FrameworkModelItem(provider));
        }
    }

    private void initTableVisualProperties() {
        frameworksTable.getModel().addTableModelListener(this);
        frameworksTable.getSelectionModel().addListSelectionListener(this);

        frameworksTable.setRowHeight(frameworksTable.getRowHeight() + 4);
        frameworksTable.setIntercellSpacing(new Dimension(0, 0));
        // set the color of the table's JViewport
        frameworksTable.getParent().setBackground(frameworksTable.getBackground());
        frameworksTable.getColumnModel().getColumn(0).setMaxWidth(30);
    }

    private void changeDescriptionAndPanel() {
        if (actualExtender != null) {
            actualExtender.removeChangeListener(this);
        }
        if (frameworksTable.getSelectedRow() == -1) {
            descriptionLabel.setText(" "); // NOI18N
            configPanel.removeAll();
            configPanel.repaint();
            configPanel.revalidate();
        } else {
            FrameworkModelItem item = model.getItem(frameworksTable.getSelectedRow());
            descriptionLabel.setText(item.getFramework().getDescription());
            descriptionLabel.setEnabled(item.isSelected());

            configPanel.removeAll();
            actualExtender = item.getFramework().createPhpModuleExtender(null);
            actualExtender.addChangeListener(this);
            JComponent component = actualExtender.getComponent();
            if (component != null) {
                configPanel.add(component, BorderLayout.NORTH);
                enableComponents(component, item.isSelected());
            }
            configPanel.revalidate();
            configPanel.repaint();
        }
        fireChange();
    }

    private void enableComponents(Container root, boolean enabled) {
        root.setEnabled(enabled);
        for (Component child : root.getComponents()) {
            if (child instanceof Container) {
                enableComponents((Container) child, enabled);
            } else {
                child.setEnabled(enabled);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameworksScrollPane = new JScrollPane();
        frameworksTable = new JTable();
        descriptionLabel = new JLabel();
        separator = new JSeparator();
        configPanel = new JPanel();

        frameworksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        frameworksTable.setShowHorizontalLines(false);
        frameworksTable.setShowVerticalLines(false);
        frameworksTable.setTableHeader(null);
        frameworksScrollPane.setViewportView(frameworksTable);

        descriptionLabel.setText("DUMMY"); // NOI18N

        configPanel.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(separator, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(descriptionLabel)
                .addContainerGap())
            .add(configPanel, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .add(frameworksScrollPane, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(frameworksScrollPane, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(separator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(descriptionLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(configPanel, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel configPanel;
    private JLabel descriptionLabel;
    private JScrollPane frameworksScrollPane;
    private JTable frameworksTable;
    private JSeparator separator;
    // End of variables declaration//GEN-END:variables

    private static final class FrameworksTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 22495101047716943L;
        private static final Color ERROR_COLOR = UIManager.getColor("nb.errorForeground"); // NOI18N
        private static final Color NORMAL_COLOR = new JLabel().getForeground();

        private final FrameworksTableModel model;

        private TableCellRenderer booleanRenderer;

        private FrameworksTableCellRenderer(FrameworksTableModel model) {
            this.model = model;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof PhpFrameworkProvider) {
                FrameworkModelItem item = model.getItem(row);

                Component defaultRenderer = super.getTableCellRendererComponent(table, item.getFramework().getName(), isSelected, false, row, column);
                if (item.isValid()) {
                    defaultRenderer.setForeground(NORMAL_COLOR);
                } else {
                    defaultRenderer.setForeground(ERROR_COLOR);
                }
                return defaultRenderer;
            } else if (value instanceof Boolean && booleanRenderer != null) {
                return booleanRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        }

        public void setBooleanRenderer(TableCellRenderer booleanRenderer) {
            this.booleanRenderer = booleanRenderer;
        }
    }

    private static final class FrameworksTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 8082636013224696L;

        private final DefaultListModel model;

        public FrameworksTableModel() {
            model = new DefaultListModel();
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return model.size();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 1:
                    return PhpFrameworkProvider.class;
                default:
                    assert false : "Unknown column index: " + columnIndex;
                    break;
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        public Object getValueAt(int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0:
                    return item.isSelected();
                case 1:
                    return item.getFramework();
                default:
                    assert false : "Unknown column index: " + column;
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0:
                    item.setSelected((Boolean) value);
                    break;
                case 1:
                    item.setFramework((PhpFrameworkProvider) value);
                    break;
                default:
                    assert false : "Unknown column index: " + column;
                    break;
            }
            fireTableCellUpdated(row, column);
        }

        FrameworkModelItem getItem(int index) {
            return (FrameworkModelItem) model.get(index);
        }

        void addItem(FrameworkModelItem item) {
            model.addElement(item);
        }
    }

    private static final class FrameworkModelItem {
        private PhpFrameworkProvider framework;
        private Boolean selected;
        private boolean valid = true;

        public FrameworkModelItem(PhpFrameworkProvider framework) {
            setFramework(framework);
            setSelected(Boolean.FALSE);
        }

        public PhpFrameworkProvider getFramework() {
            return framework;
        }

        public void setFramework(PhpFrameworkProvider framework) {
            this.framework = framework;
        }

        public Boolean isSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }
}
