/*
 * BasePropertiesPanel.java
 *
 * Created on February 15, 2008, 12:59 PM
 */

package org.netbeans.modules.db.mysql.ui;

import java.awt.Color;
import java.util.ResourceBundle;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.impl.MySQLOptions;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Van Couvering
 */
public class BasePropertiesPanel extends javax.swing.JPanel {
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


    public void validatePanel() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        
        if ( getHost() == null || getHost().equals("")) {
            error = NbBundle.getMessage(BasePropertiesPanel.class,
                        "BasePropertiesPanel.MSG_SpecifyHost");
        }
        if ( getUser() == null || getUser().equals("")) {
            error = NbBundle.getMessage(BasePropertiesPanel.class,
                        "BasePropertiesPanel.MSG_SpecifyUser");
        }
        
        if (error != null) {
            messageLabel.setText(error);
            descriptor.setValid(false);
        } else {
            messageLabel.setText(" "); // NOI18N
            descriptor.setValid(true);
        }
    }
    
    /** Creates new form BasePropertiesPanel */
    public BasePropertiesPanel(DatabaseServer server) {
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
        
        if (server.isSavePassword())
        {
            txtPassword.setText(server.getPassword());        
        }
        
        chkSavePassword.setSelected(server.isSavePassword());
    }

    String getHost() {
        return txtHost.getText().trim();
    }

    String getPassword() {
        return new String(txtPassword.getPassword()).trim();
    }

    String getPort() {
        return txtPort.getText().trim();
    }

    String getUser() {
        return txtUser.getText().trim();
    }

    boolean getSavePassword() {
        return chkSavePassword.isSelected();
    }
    void setDialogDescriptor(DialogDescriptor desc) {
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

        chkSavePassword = new javax.swing.JCheckBox();
        messageLabel = new javax.swing.JLabel();
        txtHost = new javax.swing.JTextField();
        labelHost = new javax.swing.JLabel();
        labelPort = new javax.swing.JLabel();
        txtPort = new javax.swing.JTextField();
        labelUser = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        labelPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();

        setAutoscrolls(true);

        org.openide.awt.Mnemonics.setLocalizedText(chkSavePassword, org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.chkSavePassword.text")); // NOI18N
        chkSavePassword.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.chkSavePassword.AccessibleContext.accessibleDescription")); // NOI18N
        chkSavePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSavePasswordActionPerformed(evt);
            }
        });

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getBundle(BasePropertiesPanel.class).getString("BasePropertiesPanel.messageLabel.text")); // NOI18N

        txtHost.setText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtHost.text")); // NOI18N
        txtHost.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtHost.AccessibleContext.accessibleDescription")); // NOI18N

        labelHost.setLabelFor(txtHost);
        org.openide.awt.Mnemonics.setLocalizedText(labelHost, org.openide.util.NbBundle.getBundle(BasePropertiesPanel.class).getString("BasePropertiesPanel.labelHost.text")); // NOI18N

        labelPort.setLabelFor(txtPort);
        org.openide.awt.Mnemonics.setLocalizedText(labelPort, org.openide.util.NbBundle.getBundle(BasePropertiesPanel.class).getString("BasePropertiesPanel.labelPort.text")); // NOI18N

        txtPort.setText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtPort.text")); // NOI18N
        txtPort.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtPort.AccessibleContext.accessibleDescription")); // NOI18N

        labelUser.setLabelFor(txtUser);
        org.openide.awt.Mnemonics.setLocalizedText(labelUser, org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.labelUser.text")); // NOI18N

        txtUser.setText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtUser.text")); // NOI18N
        txtUser.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtUser.AccessibleContext.accessibleDescription")); // NOI18N

        labelPassword.setLabelFor(txtPassword);
        org.openide.awt.Mnemonics.setLocalizedText(labelPassword, org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.labelPassword.text")); // NOI18N

        txtPassword.setText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtPassword.text")); // NOI18N
        txtPassword.setToolTipText(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.txtPassword.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(labelHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 181, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .add(labelPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 181, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                    .addContainerGap()
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, labelPassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, labelUser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(txtHost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txtPassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txtPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txtUser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(50, 50, 50)
                        .add(chkSavePassword))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelHost)
                    .add(txtHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelPort)
                    .add(txtPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelUser)
                    .add(txtUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelPassword)
                    .add(txtPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chkSavePassword)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(messageLabel)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        chkSavePassword.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasePropertiesPanel.class, "BasePropertiesPanel.chkSavePassword.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


private void chkSavePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSavePasswordActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_chkSavePasswordActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkSavePassword;
    private javax.swing.JLabel labelHost;
    private javax.swing.JLabel labelPassword;
    private javax.swing.JLabel labelPort;
    private javax.swing.JLabel labelUser;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField txtHost;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables

}
