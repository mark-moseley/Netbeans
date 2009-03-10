/*
 * EditPathMapDialog.java
 *
 * Created on 14 Июль 2008 г., 16:11
 */
package org.netbeans.modules.cnd.remote.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Sergey Grinev
 */
public class EditPathMapDialog extends JPanel implements ActionListener {

    public static boolean showMe(String hkey, String[] hostsList) {
        return showMe(hkey, null, hostsList);
    }

    public static boolean showMe(String hkey, String pathToValidate) {
        return showMe(hkey, pathToValidate, RemoteServerList.getInstance().getServerNames());
    }

    private static boolean showMe(String hkey, String pathToValidate, String[] hostsList) {
        JButton btnOK = new JButton(NbBundle.getMessage(EditPathMapDialog.class, "BTN_OK"));
        EditPathMapDialog dlg = new EditPathMapDialog(hkey, pathToValidate, hostsList, btnOK);

        DialogDescriptor dd = new DialogDescriptor(dlg,
                NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialogTitle"),
                true, new Object[] { btnOK, DialogDescriptor.CANCEL_OPTION}, btnOK, DialogDescriptor.DEFAULT_ALIGN, null, dlg);
        dd.setClosingOptions(new Object[]{DialogDescriptor.CANCEL_OPTION});
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dlg.presenter = dialog;
        dialog.setVisible(true);
        if (dd.getValue() == btnOK) {
            dlg.applyChanges();
            return true;
        }
        return false;
    }

    private final JButton btnOK;
    private Dialog presenter;
    private String currentHkey;
    private DefaultComboBoxModel serverListModel;
    private final String pathToValidate;
    private final Map<String, DefaultTableModel> cache = new HashMap<String, DefaultTableModel>();
    private ProgressHandle phandle;

    /** Creates new form EditPathMapDialog */
    protected EditPathMapDialog(String defaultHost, String pathToValidate, String[] hostsList, JButton btnOK) {
        this.btnOK = btnOK;
        this.pathToValidate = pathToValidate;
        currentHkey = defaultHost;
        serverListModel = new DefaultComboBoxModel();

        for (String hkey : hostsList) {
            if (!CompilerSetManager.LOCALHOST.equals(hkey)) {
                serverListModel.addElement(hkey);
            }
        }

        initComponents();

        tblPathMappings.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        tblPathMappings.getTableHeader().setPreferredSize(new Dimension(0, 20));

        cbHostsList.setSelectedItem(currentHkey);

        String explanationText;
        if (pathToValidate != null) {
            explanationText = NbBundle.getMessage(EditPathMapDialog.class, "EPMD_ExplanationWithPath", pathToValidate);
        } else {
            explanationText = NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Explanation");
        }
        txtExplanation.setText(explanationText);

        // bg color jdk bug fixup
        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            jScrollPane1.setOpaque(false);
            jScrollPane3.setOpaque(false);
        }

        initTableModel(currentHkey);
    }

    private static RemotePathMap getRemotePathMap(String hkey) {
        return RemotePathMap.getRemotePathMapInstance(ExecutionEnvironmentFactory.getExecutionEnvironment(hkey));
    }

    private synchronized void initTableModel(final String hkey) {
        DefaultTableModel tableModel = cache.get(hkey);
        if (tableModel == null) {
            if (RemotePathMap.isReady(hkey)) {
                tableModel = prepareTableModel(hkey);
            } else {
                handleProgress(true);
                tableModel = new DefaultTableModel(0, 2);
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        final DefaultTableModel tm = prepareTableModel(hkey);
                        cache.put(hkey, tm);
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                if (tblPathMappings != null) {
                                    handleProgress(false);
                                    updatePathMappingsTable(tm);
                                    enableControls(true, "");
                                }
                            }
                        });
                    }
                });
                enableControls(false, NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Loading"));

            }
            cache.put(hkey, tableModel);
        }

        updatePathMappingsTable(tableModel);
    }

    private void enableControls(boolean value, String message) {
        btnOK.setEnabled(value);
        tblPathMappings.setEnabled(value);
        cbHostsList.setEnabled(value);
        txtError.setText(message);
    }

    private void updatePathMappingsTable(DefaultTableModel tableModel) {
        tblPathMappings.setModel(tableModel);
        setColumnNames();
    }

    private DefaultTableModel prepareTableModel(String hkey) {
        Map<String, String> pm = getRemotePathMap(hkey).getMap();
        DefaultTableModel tableModel = new DefaultTableModel(0, 2);
        for (Map.Entry<String, String> entry : pm.entrySet()) {
            tableModel.addRow(new String[]{entry.getKey(), entry.getValue()});
        }
        if (tableModel.getRowCount() < 4) { // TODO: switch from JTable to a normal TableView
            for (int i = 4; i > tableModel.getRowCount(); i--) {
                tableModel.addRow(new String[]{null, null});
            }
        } else {
            tableModel.addRow(new String[]{null, null});
        }
        return tableModel;
    }

    private void setColumnNames() {
        tblPathMappings.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(EditPathMapDialog.class, "LocalPathColumnName")); // NOI18N
        tblPathMappings.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(EditPathMapDialog.class, "RemotePathColumnName")); // NOI18N
    }

    /* package */ void applyChanges() {
        for (String hkey : cache.keySet()) {
            Map<String, String> map = new HashMap<String, String>();
            DefaultTableModel model = cache.get(hkey);
            for (int i = 0; i < model.getRowCount(); i++) {
                String local = (String) model.getValueAt(i, 0);
                String remote = (String) model.getValueAt(i, 1);
                if (local != null && remote != null) {
                    local = local.trim();
                    remote = remote.trim();
                    if (local.length() > 0 && remote.length() > 0) {
                        map.put(local, remote);
                    }
                }
            }
            getRemotePathMap(hkey).updatePathMap(map);
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

        lblHostName = new javax.swing.JLabel();
        cbHostsList = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPathMappings = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtExplanation = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtError = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();

        lblHostName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("EPMD_Hostname").charAt(0));
        lblHostName.setLabelFor(cbHostsList);
        lblHostName.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.lblHostName.text")); // NOI18N
        lblHostName.setFocusable(false);

        cbHostsList.setModel(serverListModel);
        cbHostsList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbHostsListItemStateChanged(evt);
            }
        });

        tblPathMappings.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblPathMappings.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tblPathMappings);
        tblPathMappings.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_MappingsTable_AN")); // NOI18N
        tblPathMappings.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_MappingsTable_AD")); // NOI18N

        jScrollPane2.setBorder(null);

        txtExplanation.setBackground(getBackground());
        txtExplanation.setColumns(20);
        txtExplanation.setLineWrap(true);
        txtExplanation.setRows(4);
        txtExplanation.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.txtExplanation.text")); // NOI18N
        txtExplanation.setWrapStyleWord(true);
        txtExplanation.setAutoscrolls(false);
        txtExplanation.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        txtExplanation.setFocusable(false);
        txtExplanation.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jScrollPane2.setViewportView(txtExplanation);

        jScrollPane3.setBorder(null);

        txtError.setBackground(getBackground());
        txtError.setColumns(20);
        txtError.setForeground(new java.awt.Color(255, 0, 0));
        txtError.setLineWrap(true);
        txtError.setRows(4);
        txtError.setWrapStyleWord(true);
        txtError.setFocusable(false);
        jScrollPane3.setViewportView(txtError);

        jPanel1.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblHostName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cbHostsList, 0, 391, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblHostName)
                    .add(cbHostsList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 118, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        lblHostName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Hostname")); // NOI18N
        lblHostName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Host_AD")); // NOI18N
        cbHostsList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Hostname")); // NOI18N
        cbHostsList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Host_AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cbHostsListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbHostsListItemStateChanged
    currentHkey = (String) cbHostsList.getSelectedItem();
    initTableModel(currentHkey);
}//GEN-LAST:event_cbHostsListItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbHostsList;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblHostName;
    private javax.swing.JTable tblPathMappings;
    private javax.swing.JTextArea txtError;
    private javax.swing.JTextArea txtExplanation;
    // End of variables declaration//GEN-END:variables
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOK) {
            if (cache.get(currentHkey).getRowCount() == 0) {
                // fast handle vacuous case
                presenter.setVisible(false);
                return;
            }
            handleProgress(true);
            enableControls(false, NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Validating"));
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    final String errors = validateMaps();
                    Runnable action = errors.length() == 0
                            ? new Runnable() {
                                public void run() {
                                    try {
                                        //this is done to don't scare user with red note if validateMaps() was fast
                                        Thread.sleep(500);
                                    } catch (InterruptedException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                    presenter.setVisible(false);
                                }
                            }
                            : new Runnable() {
                                public void run() {
                                    handleProgress(false);
                                    enableControls(true, errors);
                                }
                            };
                    SwingUtilities.invokeLater(action);
                }
            });
        }
    }

    private void handleProgress(boolean start) {
        if (start) {
            phandle = ProgressHandleFactory.createHandle("");
            jPanel1.add(ProgressHandleFactory.createProgressComponent(phandle), BorderLayout.NORTH);
            jPanel1.setVisible(true);
            phandle.start();
        } else {
            phandle.finish();
            jPanel1.setVisible(false);
            jPanel1.removeAll();
        }
    }

    private String validateMaps() {
        DefaultTableModel model = cache.get(currentHkey);
        StringBuilder sb = new StringBuilder();
        boolean pathIsValidated = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            String local = (String) model.getValueAt(i, 0);
            String remote = (String) model.getValueAt(i, 1);
            if (local != null) {
                local = local.trim();
                if (local.length() > 0) {
                    if (!HostInfoProvider.getDefault().fileExists(ExecutionEnvironmentFactory.getLocalExecutionEnvironment(), local)) {
                        sb.append(NbBundle.getMessage(EditPathMapDialog.class, "EPMD_BadLocalPath", local));
                    }
                    if (pathToValidate != null && !pathIsValidated) {
                        if (remote != null && RemotePathMap.isSubPath(local, pathToValidate)) {
                            pathIsValidated = true;
                            //TODO: real path mapping validation (create file, check from both sides, etc)
                        }
                    }
                }
            }
            if (remote != null) {
                remote = remote.trim();
                if (remote.length() > 0) {
                    if (!HostInfoProvider.getDefault().fileExists(
                            ExecutionEnvironmentFactory.getExecutionEnvironment(currentHkey), remote)) {
                        sb.append(NbBundle.getMessage(EditPathMapDialog.class, "EPMD_BadRemotePath", remote));
                    }
                }
            }
        }
        if (pathToValidate != null && !pathIsValidated) {
            sb.append(NbBundle.getMessage(EditPathMapDialog.class, "EPMD_PathNotResolved", pathToValidate));
        }
        return sb.toString();
    }
}
