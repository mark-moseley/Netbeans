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

package org.netbeans.modules.j2ee.common.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Displays a list of data sources for which database connections are not registered and helps the user to register
 * database connections needed by the project's data source to resolve this problem.
 *
 * @author Pavel Buzek, John Baker
 */
public final class MissingDatabaseConnectionWarning extends JPanel {
    public static final String OK_ENABLED = "ok_enabled"; //NOI18N
    public static final String CANCEL_ENABLED = "cancel_enabled"; //NOI18N
    private final Border scrollPaneBorder;
    private Project project;
    private static RequestProcessor.Task task = null;
    
    private MissingDatabaseConnectionWarning(Project project) {
        initComponents();
        this.project = project;
        datasourceList.setModel(new DatasourceListModel());
        scrollPaneBorder = jScrollPane2.getBorder();
        initDatasourceList();
    }
    
    private void initDatasourceList() {
        datasourceList.setCellRenderer(new DatasourceRenderer());
        datasourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        datasourceList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!datasourceList.isSelectionEmpty()) { // something has been selected
                    jButtonAddConnection.setEnabled(true);
                } else {
                    firePropertyChange(OK_ENABLED, false, true);
                    jButtonAddConnection.setEnabled(false);                    
                }
            }
        }
        );                
    }
    
    /**
     * Show the "Resolve Data Sources" dialog and let the user choose a datasource from
     * the list.
     *
     * @param title dialog title
     * @param description dialog accessible description
     * @param project
     * 
     *
     */
    public static void selectDatasources(String title, String description, Project project) {
        MissingDatabaseConnectionWarning panel = new MissingDatabaseConnectionWarning(project);
        Object[] options = new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        };
        final DialogDescriptor desc = new DialogDescriptor(panel, title, true, options,
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        desc.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.getAccessibleContext().setAccessibleDescription(description);
            panel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(MissingDatabaseConnectionWarning.OK_ENABLED)) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            desc.setValid(((Boolean)newvalue).booleanValue());
                        }
                    }
                }
            }
            );
            desc.setValid(panel.getSelectedDatasource() != null);
            panel.setSize(panel.getPreferredSize());
            dlg.pack();
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }

    /** Returns the selected datasource or null if no datasource was selected.
     *
     * @return datasource or null if no datasource was selected
     */
    public String getSelectedDatasource() {
        if ((datasourceList.getSelectedIndex() == -1) || (datasourceList.getFirstVisibleIndex() == -1)) {
            return null;
        } else {
            return ((Datasource)datasourceList.getSelectedValue()).getJndiName();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane2 = new javax.swing.JScrollPane();
        datasourceList = new javax.swing.JList();
        jTextArea1 = new javax.swing.JTextArea();
        jTextArea2 = new javax.swing.JTextArea();
        jButtonAddConnection = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane2.setMinimumSize(new java.awt.Dimension(200, 100));

        datasourceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        datasourceList.setPreferredSize(null);
        datasourceList.setVerifyInputWhenFocusTarget(false);
        datasourceList.setVisibleRowCount(4);
        jScrollPane2.setViewportView(datasourceList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        add(jScrollPane2, gridBagConstraints);

        jTextArea1.setColumns(25);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setText(NbBundle.getMessage(MissingDatabaseConnectionWarning.class, "LBL_SelectDatasource")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setFocusable(false);
        jTextArea1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(jTextArea1, gridBagConstraints);
        jTextArea1.getAccessibleContext().setAccessibleName("Resolve Datasource");

        jTextArea2.setEditable(false);
        jTextArea2.setLineWrap(true);
        jTextArea2.setText(org.openide.util.NbBundle.getMessage(MissingDatabaseConnectionWarning.class, "LBL_MissingDatabaseConnectionWarning")); // NOI18N
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setFocusable(false);
        jTextArea2.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(jTextArea2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddConnection, org.openide.util.NbBundle.getMessage(MissingDatabaseConnectionWarning.class, "LBL_AddDatabaseConnection")); // NOI18N
        jButtonAddConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddConnectionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jButtonAddConnection, gridBagConstraints);
        jButtonAddConnection.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MissingDatabaseConnectionWarning.class, "ACSN_AddDatabaseConnection")); // NOI18N
        jButtonAddConnection.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MissingDatabaseConnectionWarning.class, "ACSD_AddDatabaseConnection")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButtonAddConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddConnectionActionPerformed
        Datasource brokenDatasource = (Datasource)datasourceList.getSelectedValue();        
        addDatabaseConnection(brokenDatasource);        
        
        // enable the scrollbar 
        if (datasourceList.getModel().getSize() > 0) {            
            jScrollPane2.setBorder(scrollPaneBorder);
            jTextArea2.setText(org.openide.util.NbBundle.getMessage(MissingDatabaseConnectionWarning.class, "LBL_SelectDatasource"));
        }
        
        // refresh the jList
        datasourceList.setCellRenderer(new DatasourceRenderer());                
}//GEN-LAST:event_jButtonAddConnectionActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList datasourceList;
    private javax.swing.JButton jButtonAddConnection;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables
    
    
    private final class DatasourceListModel extends AbstractListModel implements Runnable, ConnectionListener {
        private final RequestProcessor BROKEN_DATASOURCE_RP = new RequestProcessor("WebLogicalViewProvider.BROKEN_DATASOURCE_RP"); //NOI18N
        private Set<Datasource> datasources;
        
        public DatasourceListModel() {          
            datasources = BrokenDatasourceSupport.getBrokenDatasources(project);
            addConnectionListener();
        }
        
        public synchronized int getSize() {
            return datasources.size();
        }
        
        /*
         *
         * Return the datasource corresponding to the selected position in datasourceList
         */
        public synchronized Object getElementAt(int index) {
            Iterator it = datasources.iterator();
            int i = 0;
            while (it.hasNext()) {
                if (index >= 0 && index <= datasources.size()) {
                    if (i == index) {
                        return it.next();
                    }
                    
                    // get the next datasource from the set of datasources
                    it.next();
                    i++;
                }
            }
            
            return null;
        }
        
        /*
         * Refresh model to be done in RP thread for checking database connections
         * checking connections might be done by another thread at the same time.
         */
        public synchronized void refreshModel() {              
            if (task == null) {
                task = BROKEN_DATASOURCE_RP.create(this);
            }
            task.schedule(50);                        
        }
        
        public synchronized void run() {
            datasources = BrokenDatasourceSupport.getBrokenDatasources(project);
            if (datasources.isEmpty()) {
                removeConnectionListener();
            }
        }
        
        // Listen for any connections changed in DB Explorer and refresh model accordingly
        public void connectionsChanged() {
            initDatasourceList();
            refreshModel();
        }
        
        private void addConnectionListener() {
            ConnectionManager.getDefault().addConnectionListener(this);
        }
        
        private void removeConnectionListener() {
            ConnectionManager.getDefault().removeConnectionListener(this);
        }
    }
    
    private static final class DatasourceRenderer extends JLabel implements ListCellRenderer {
        
        DatasourceRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Datasource) {
                String jndiName = ((Datasource)value).getJndiName();
                setText(jndiName.substring(jndiName.indexOf("/")+1));
            }
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
    /**
     * Post the Add Database Connection dialog passing the data source information for populating the dialgo
     * @param brokenDatasource data source that requires a registered database connection
     */
    private static void addDatabaseConnection(Datasource brokenDatasource) {
        // next check to see if the driver has been added
        String driverName = brokenDatasource.getDriverClassName();
        final JDBCDriver matchingDriver = findMatchingDriver(driverName);
        final Datasource edtDS = brokenDatasource;
        
        // if matchingDriver is null, user can add a driver in the Add Connection dialog
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ConnectionManager.getDefault().showAddConnectionDialogFromEventThread(matchingDriver, edtDS.getUrl(), edtDS.getUsername(), edtDS.getPassword());
            }
        });
    }
    
    /*
     * Find a matching driver registered with the IDE
     * public: to-be-used outside this class
     */
    private static JDBCDriver findMatchingDriver(String driverClass) {
        int i = 0;
        JDBCDriver[] newDrivers;
        newDrivers = JDBCDriverManager.getDefault().getDrivers();
        
        for (i = 0; i <newDrivers.length; i++) {
            if (newDrivers[i].getClassName().equals(driverClass)) {
                return newDrivers[i];
            }
        }
        
        return null;
    }        
}
