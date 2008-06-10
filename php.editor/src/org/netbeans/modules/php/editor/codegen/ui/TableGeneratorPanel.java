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

package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.Dialog;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.editor.codegen.DatabaseURL;
import org.netbeans.modules.php.editor.codegen.DatabaseURL.Server;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Andrei Badea
 */
public class TableGeneratorPanel extends javax.swing.JPanel {

    private DialogDescriptor descriptor;
    private DatabaseConnection dbconn;
    private Connection conn;
    private DatabaseMetaData dmd;
    private String table;
    private String lastErrorMessage;

    public static TableAndColumns selectTableAndColumns(String connVariable) {
        TableGeneratorPanel panel = new TableGeneratorPanel();
        DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(TableGeneratorPanel.class, "MSG_SelectTableAndColumns"));
        panel.initialize(desc, connVariable);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.setVisible(true);
        dialog.dispose();
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            return new TableAndColumns(panel.table, panel.getAllColumns(), panel.getSelectedColumns(), panel.getConnVariable()); // NOI18N
        }
        return null;
    }

    private TableGeneratorPanel() {
        initComponents();
        columnList.setCellRenderer(new CheckRenderer());
        CheckListener checkListener = new CheckListener();
        columnList.addKeyListener(checkListener);
        columnList.addMouseListener(checkListener);
        connVariableTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateErrorState();
            }
            public void removeUpdate(DocumentEvent e) {
                updateErrorState();
            }
            public void changedUpdate(DocumentEvent e) {
                updateErrorState();
            }
        });
        errorLabel.setForeground(UIUtils.getErrorForeground());
    }

    private void initialize(DialogDescriptor descriptor, String connVariable) {
        this.descriptor = descriptor;
        DatabaseExplorerUIs.connect(dbconnComboBox, ConnectionManager.getDefault());
        connVariableTextField.setText(connVariable);
        updateErrorState();
    }

    private String changeDatabaseConnection(DatabaseConnection newDBConn) {
        dbconn = null;
        conn = null;
        dmd = null;
        tableComboBox.setModel(new DefaultComboBoxModel());
        columnList.setModel(new DefaultListModel());
        if (newDBConn == null) {
            return null;
        }
        DatabaseURL url = DatabaseURL.detect(newDBConn.getDatabaseURL());
        if (url == null || url.getServer() != Server.MYSQL) {
            return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_UnknownServer");
        }
        Connection newConn = newDBConn.getJDBCConnection();
        if (newConn == null) {
            ConnectionManager.getDefault().showConnectionDialog(newDBConn);
            newConn = newDBConn.getJDBCConnection();
        }
        String password = newDBConn.getPassword();
        if (password == null || newConn == null) {
            if (password == null) {
                return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_NoPassword");
            } else {
                return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_CouldNotConnect");
            }
        }
        String catalog;
        DatabaseMetaData newDmd;
        try {
            catalog = newConn.getCatalog();
            newDmd = newConn.getMetaData();
        } catch (SQLException e) {
            Exceptions.printStackTrace(e);
            return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_DatabaseMetadata");
        }
        List<String> tables = new ArrayList<String>();
        String errorMessage = extractTables(tables, newDmd, catalog, newDBConn.getSchema());
        if (errorMessage != null) {
            return errorMessage;
        }
        Collections.<String>sort(tables);
        dbconn = newDBConn;
        conn = newConn;
        dmd = newDmd;
        DefaultComboBoxModel tableModel = new DefaultComboBoxModel();
        for (String table : tables) {
            tableModel.addElement(table);
        }
        tableComboBox.setModel(tableModel);
        return null;
    }

    private String extractTables(final List<? super String> tables, final DatabaseMetaData dmd, final String catalog, final String schema) {
        return doWithProgress(NbBundle.getMessage(TableGeneratorPanel.class, "MSG_ExtractingTables"), new Callable<String>() {
            public String call() throws Exception {
                try {
                    ResultSet rs = dmd.getTables(catalog, schema, "%", new String[] { "TABLE" }); // NOI18N
                    try {
                        while (rs.next()) {
                            tables.add(rs.getString("TABLE_NAME")); // NOI18N
                        }
                    } finally {
                        rs.close();
                    }
                    return null;
                } catch (SQLException e) {
                    Exceptions.printStackTrace(e);
                    return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_DatabaseMetadata");
                }
            }
        });
    }

    private String changeTable(final String newTable) {
        List<String> columns = new ArrayList<String>();
        String errorMessage = extractColumns(newTable, columns);
        if (errorMessage != null) {
            return errorMessage;
        }
        table = newTable;
        ColumnModel model = new ColumnModel(columns);
        columnList.setModel(model);
        int selectedIndex = model.getSize() > 0 ? 0 : -1;
        columnList.setSelectedIndex(selectedIndex);
        return null;
    }

    private String extractColumns(final String table, final List<? super String> columns) {
        return doWithProgress(NbBundle.getMessage(TableGeneratorPanel.class, "MSG_ExtractingColumns"), new Callable<String>() {
            public String call() {
                try {
                    ResultSet rs = dmd.getColumns(conn.getCatalog(), dbconn.getSchema(), table, "%"); // NOI18N
                    try {
                        while (rs.next()) {
                            columns.add(rs.getString("COLUMN_NAME")); // NOI18N
                        }
                    } finally {
                        rs.close();
                    }
                    // Do not sort the columns, we need them in the order they
                    // are defined in the database.
                    return null;
                } catch (SQLException e) {
                    Exceptions.printStackTrace(e);
                    return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_DatabaseMetadata");
                }
            }
        });
    }

    private void tableComboBoxSelectionChanged() {
        String table = (String) tableComboBox.getSelectedItem();
        lastErrorMessage = changeTable(table);
        updateErrorState();
    }

    private List<String> getSelectedColumns() {
        List<String> result = new ArrayList<String>();
        ListModel model = columnList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object element = model.getElementAt(i);
            if (!(element instanceof Selectable)) {
                continue;
            }
            Selectable columnEl = (Selectable) element;
            if (!columnEl.isSelected()) {
                continue;
            }
            result.add(columnEl.getDisplayName());
        }
        return result;
    }

    private List<String> getAllColumns() {
        List<String> result = new ArrayList<String>();
        ListModel model = columnList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object element = model.getElementAt(i);
            if (!(element instanceof Selectable)) {
                continue;
            }
            Selectable columnEl = (Selectable) element;
            result.add(columnEl.getDisplayName());
        }
        return result;
    }

    private String getConnVariable() {
        return connVariableTextField.getText().trim();
    }

    private void updateErrorState() {
        tableComboBox.setEnabled(dbconn != null);
        columnList.setEnabled(tableComboBox.getSelectedItem() != null);
        if (lastErrorMessage != null) {
            setErrorMessage(lastErrorMessage);
            return;
        }
        if (dbconn == null) {
            setErrorMessage(NbBundle.getMessage(TableGeneratorPanel.class, "ERR_SelectConnection"));
            return;
        }
        if (tableComboBox.getSelectedItem() == null) {
            setErrorMessage(NbBundle.getMessage(TableGeneratorPanel.class, "ERR_SelectTable"));
            return;
        }
        if (getConnVariable().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(TableGeneratorPanel.class, "ERR_EnterConnVariable"));
            return;
        }
        setErrorMessage(null);
    }

    private void setErrorMessage(String message) {
        errorLabel.setText(message != null ? message : " "); // NOI18N
         descriptor.setValid(message == null);
    }

    private static <T> T doWithProgress(String message, final Callable<? extends T> run) {
        final ProgressPanel panel = new ProgressPanel();
        panel.setCancelVisible(false);
        panel.setText(message);
        ProgressHandle handle = ProgressHandleFactory.createHandle(null);
        JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
        handle.start();
        final List<T> result = new ArrayList<T>(1);
        try {
            Task task = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        try {
                            result.add(run.call());
                        } catch (Exception e) {
                            result.add(null);
                            Exceptions.printStackTrace(e);
                        } finally {
                            SwingUtilities.invokeLater(this);
                        }
                    } else {
                        panel.close();
                    }
                }
            });
            panel.open(progress);
            task.waitFinished();
        } finally {
            handle.finish();
        }
        return result.get(0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dbconnLabel = new javax.swing.JLabel();
        dbconnComboBox = new javax.swing.JComboBox();
        tableLabel = new javax.swing.JLabel();
        tableComboBox = new javax.swing.JComboBox();
        columnLabel = new javax.swing.JLabel();
        columnScrollPane = new javax.swing.JScrollPane();
        columnList = new javax.swing.JList();
        connVariableLabel = new javax.swing.JLabel();
        connVariableTextField = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();

        dbconnLabel.setLabelFor(dbconnComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(dbconnLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "ConnectionGeneratorPanel.dbconnLabel.text")); // NOI18N

        dbconnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbconnComboBoxActionPerformed(evt);
            }
        });

        tableLabel.setLabelFor(tableComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(tableLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.tableLabel.text")); // NOI18N

        tableComboBox.setEnabled(false);
        tableComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableComboBoxActionPerformed(evt);
            }
        });

        columnLabel.setLabelFor(columnList);
        org.openide.awt.Mnemonics.setLocalizedText(columnLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnLabel.text")); // NOI18N

        columnList.setEnabled(false);
        columnScrollPane.setViewportView(columnList);
        columnList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnList.AccessibleContext.accessibleName")); // NOI18N
        columnList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnList.AccessibleContext.accessibleDescription")); // NOI18N

        connVariableLabel.setLabelFor(connVariableLabel);
        org.openide.awt.Mnemonics.setLocalizedText(connVariableLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.connVariableLabel.text")); // NOI18N

        connVariableTextField.setColumns(16);

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.errorLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, columnScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, dbconnLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, dbconnComboBox, 0, 301, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, errorLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tableLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tableComboBox, 0, 301, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, columnLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, connVariableTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, connVariableLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(dbconnLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dbconnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tableLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tableComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(columnLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(columnScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connVariableLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connVariableTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(errorLabel)
                .addContainerGap())
        );

        dbconnComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.dbconnComboBox.AccessibleContext.accessibleName")); // NOI18N
        dbconnComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.dbconnComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        tableComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.tableComboBox.AccessibleContext.accessibleName")); // NOI18N
        tableComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.tableComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        connVariableTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.connVariableTextField.AccessibleContext.accessibleName")); // NOI18N
        connVariableTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.connVariableTextField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void dbconnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbconnComboBoxActionPerformed
        DatabaseConnection dbconn = null;
        Object selected = dbconnComboBox.getSelectedItem();
        if (selected instanceof DatabaseConnection) {
            dbconn = (DatabaseConnection) selected;
        }
        lastErrorMessage = changeDatabaseConnection(dbconn);
        if (lastErrorMessage == null) {
            tableComboBoxSelectionChanged();
        }
        updateErrorState();
}//GEN-LAST:event_dbconnComboBoxActionPerformed

private void tableComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableComboBoxActionPerformed
        tableComboBoxSelectionChanged();
}//GEN-LAST:event_tableComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel columnLabel;
    private javax.swing.JList columnList;
    private javax.swing.JScrollPane columnScrollPane;
    private javax.swing.JLabel connVariableLabel;
    private javax.swing.JTextField connVariableTextField;
    private javax.swing.JComboBox dbconnComboBox;
    private javax.swing.JLabel dbconnLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JComboBox tableComboBox;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables

    private static final class ColumnModel extends AbstractListModel implements ChangeListener {

        private final List<Selectable> elements;

        public ColumnModel(List<String> columns) {
            elements = new ArrayList<Selectable>(columns.size());
            for (String table : columns) {
                Selectable element = new Selectable(table);
                element.addChangeListener(this);
                elements.add(element);
            }
        }

        public int getSize() {
            return elements.size();
        }

        public Selectable getElementAt(int index) {
            return elements.get(index);
        }

        public void stateChanged(ChangeEvent e) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i) == e.getSource()) {
                    fireContentsChanged(this, i, i);
                    break;
                }
            }
        }
    }

    public static final class TableAndColumns {

        private final String table;
        private final List<String> allColumns;
        private final List<String> selectedColumns;
        private final String connVariable;

        private TableAndColumns(String table, List<String> allColumns, List<String> selectedColumns, String connVariable) {
            this.table = table;
            this.allColumns = allColumns;
            this.selectedColumns = selectedColumns;
            this.connVariable = connVariable;
        }

        public String getTable() {
            return table;
        }

        public List<String> getAllColumns() {
            return allColumns;
        }

        public List<String> getSelectedColumns() {
            return selectedColumns;
        }

        public String getConnVariable() {
            return connVariable;
        }
    }
}
