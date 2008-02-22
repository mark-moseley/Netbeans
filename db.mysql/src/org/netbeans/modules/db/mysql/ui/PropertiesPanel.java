/*
 * PropertiesPanel.java
 *
 * Created on February 15, 2008, 12:59 PM
 */

package org.netbeans.modules.db.mysql.ui;

import java.awt.Color;
import java.awt.Dialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.MySQLOptions;
import org.netbeans.modules.db.mysql.ServerInstance;
import org.netbeans.modules.db.mysql.ServerNodeProvider;
import org.netbeans.modules.db.mysql.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Van Couvering
 */
public class PropertiesPanel extends javax.swing.JPanel {
    MySQLOptions options = MySQLOptions.getDefault();
    DialogDescriptor descriptor;
    private Color nbErrorForeground;

    
    private DocumentListener docListener = new DocumentListener() {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

    };


    private void validatePanel() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        
        if ( getHost() == null || getHost().equals("")) {
            error = NbBundle.getMessage(PropertiesPanel.class,
                        "PropertiesPanel.MSG_SpecifyHost");
        }
        if ( getUser() == null || getUser().equals("")) {
            error = NbBundle.getMessage(PropertiesPanel.class,
                        "PropertiesPanel.MSG_SpecifyUser");
        }
        
        if (error != null) {
            messageLabel.setText(error);
            descriptor.setValid(false);
        } else {
            messageLabel.setText(" "); // NOI18N
            descriptor.setValid(true);
        }
    }
    public static boolean showMySQLProperties(ServerInstance server) {
        assert SwingUtilities.isEventDispatchThread();
        
        PropertiesPanel panel = new PropertiesPanel(server);
        String title = NbBundle.getMessage(PropertiesPanel.class, 
                "PropertiesPanel.LBL_MySQLPropertiesTitle");

        DialogDescriptor desc = new DialogDescriptor(panel, title);
        panel.setDialogDescriptor(desc);

        for (;;) {                    
            Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
            String acsd = NbBundle.getMessage(PropertiesPanel.class, 
                    "PropertiesPanel.ACSD_PropertiesPanel");
            dialog.getAccessibleContext().setAccessibleDescription(acsd);
            dialog.setVisible(true);
            dialog.dispose();

            if (!DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                return false;
            }

            server.setHost(panel.getHost());
            server.setPort(panel.getPort());
            server.setUser(panel.getUser());
            server.setPassword(panel.getPassword());
            server.setSavePassword(panel.getSavePassword());

            // Register the node provider in case it isn't currently registered
            ServerNodeProvider.getDefault().setRegistered(true);
            
            try {
                server.connect();
            } catch ( DatabaseException e ) {
                Utils.displayError(
                        NbBundle.getMessage(PropertiesPanel.class, 
                            "PropertiesPanel.MSG_UnableToConnect"), 
                        e);
            }
           
            return true;
        }
    }

    /** Creates new form PropertiesPanel */
    public PropertiesPanel(ServerInstance server) {
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        this.setBackground(getBackground());
        messageLabel.setBackground(getBackground());
        
        txtUser.getDocument().addDocumentListener(docListener);
        txtHost.getDocument().addDocumentListener(docListener);
        
        String user = server.getUser();
        if ( user == null || user.equals("") ) {
            user = MySQLOptions.getDefaultAdminUser();
        }
        txtUser.setText(user);
        
        String host = server.getHost();
        if ( host == null || host.equals("")) {
            host = MySQLOptions.getDefaultHost();
        }
        txtHost.setText(host);
        
        String port = server.getPort();
        if ( port == null || port.equals("")) {
            port = MySQLOptions.getDefaultPort();
        }
        txtPort.setText(port);
        
        txtPassword.setText(server.getPassword());        
        chkSavePassword.setSelected(server.isSavePassword());
    }

    private String getHost() {
        return txtHost.getText().trim();
    }

    private String getPassword() {
        return new String(txtPassword.getPassword()).trim();
    }

    private String getPort() {
        return txtPort.getText().trim();
    }

    private String getUser() {
        return txtUser.getText().trim();
    }

    private boolean getSavePassword() {
        return chkSavePassword.isSelected();
    }
    private void setDialogDescriptor(DialogDescriptor desc) {
        this.descriptor = desc;
        validatePanel();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        chkSavePassword = new javax.swing.JCheckBox();
        txtHost = new javax.swing.JTextField();
        txtPort = new javax.swing.JTextField();
        txtUser = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        messageLabel = new javax.swing.JLabel();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.jLabel4.text")); // NOI18N

        chkSavePassword.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.chkSavePassword.text")); // NOI18N

        txtHost.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.txtHost.text")); // NOI18N
        txtHost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHostActionPerformed(evt);
            }
        });

        txtPort.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.txtPort.text")); // NOI18N

        txtUser.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.txtUser.text")); // NOI18N

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        messageLabel.setText(org.openide.util.NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.messageLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2)
                            .add(jLabel3)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(txtHost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                                .add(txtUser)
                                .add(txtPort))))
                    .add(layout.createSequentialGroup()
                        .add(52, 52, 52)
                        .add(chkSavePassword))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(messageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 432, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(36, 36, 36)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2)
                        .add(11, 11, 11)
                        .add(jLabel3))
                    .add(layout.createSequentialGroup()
                        .add(txtHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(txtPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(chkSavePassword)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 38, Short.MAX_VALUE)
                .add(messageLabel)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel3, jLabel4, txtHost, txtPort, txtUser}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents


private void txtHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHostActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtHostActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkSavePassword;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField txtHost;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables

}
