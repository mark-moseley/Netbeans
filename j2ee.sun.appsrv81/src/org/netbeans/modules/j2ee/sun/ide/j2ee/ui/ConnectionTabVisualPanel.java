/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * AddUserDefLocalServerVisualPanel.java
 *
 * Created on January 7, 2004
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;

import java.io.File;
import java.util.Collection;

import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/** A single panel for a wizard - the GUI portion.
 *
 * @author nityad
 */
public class ConnectionTabVisualPanel extends javax.swing.JPanel {
    
    /** The wizard panel descriptor associated with this GUI panel.
     * If you need to fire state changes or something similar, you can
     * use this handle to do so.
     */
    private final AddUserDefLocalServerPanel panel;
    private final DeploymentManagerProperties targetData;
    
    /** Create the wizard panel and set up some basic properties. */
    public ConnectionTabVisualPanel(AddUserDefLocalServerPanel panel, DeploymentManagerProperties data) {
        this.panel = panel;
        this.targetData = data;
        initComponents();
        InstanceProperties ips = data.getInstanceProperties();
        String url = (String) ips.getProperty("url"); // NOI18N
        int dex = url.indexOf("::");
        if (dex > -1)
            url = url.substring(dex+2);
        socketField.setText(url);
    userNameField.setText(data.getUserName());
    passwordField.setText(data.getPassword());
    domainField.setText(data.getDomainName());
    domainLocField.setText(data.getLocation());
    enableHttpMonitor.setSelected(Boolean.valueOf(data.getHttpMonitorOn()).booleanValue());
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(ConnectionTabVisualPanel.class, "TITLE_AddUserDefinedLocalServerPanel"));
//        msgLabel.setText(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "Msg_ValidPort"));
        /*
        // Optional: provide a special description for this pane.
        // You must have turned on WizardDescriptor.WizardPanel_helpDisplayed
        // (see descriptor in standard iterator template for an example of this).
        try {
            putClientProperty("WizardPanel_helpURL", // NOI18N
                new URL("nbresloc:/org/netbeans/modules/j2ee/sun/ide/j2ee/AddUserDefLocalServerVisualHelp.html")); // NOI18N
        } catch (MalformedURLException mfue) {
            throw new IllegalStateException(mfue.toString());
        }
         */
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        socketField = new javax.swing.JTextField();
        userNameField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        domainField = new javax.swing.JTextField();
        domainLocField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        userPasswordLabel = new javax.swing.JLabel();
        domainLabel = new javax.swing.JLabel();
        domainLocLabel = new javax.swing.JLabel();
        msgLabel = new javax.swing.JLabel();
        enableHttpMonitor = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("Step_ChooseUserDefinedLocalServer"));
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("AddUserDefinedLocalServerPanel_Desc"));
        socketField.setEditable(false);
        socketField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                socketFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 3);
        add(socketField, gridBagConstraints);
        socketField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_AdminPort"));
        socketField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_AdminPort"));

        userNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                userNameFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 3);
        add(userNameField, gridBagConstraints);
        userNameField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Username"));
        userNameField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_Username"));

        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwordFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 3);
        add(passwordField, gridBagConstraints);
        passwordField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Pw"));
        passwordField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_Pw"));

        domainField.setEditable(false);
        domainField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                domainFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 3);
        add(domainField, gridBagConstraints);
        domainField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Domain"));
        domainField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_Domain"));

        domainLocField.setEditable(false);
        domainLocField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                domainLocFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 3);
        add(domainLocField, gridBagConstraints);
        domainLocField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_InstallRoot"));
        domainLocField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_InstallRoot"));

        portLabel.setLabelFor(socketField);
        portLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_AdminSocket"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 3, 5, 0);
        add(portLabel, gridBagConstraints);
        portLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_AdminPort_Mnemonic"));
        portLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_AdminPort"));

        userNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Username_Mnemonic").charAt(0));
        userNameLabel.setLabelFor(userNameField);
        userNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Username"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 0);
        add(userNameLabel, gridBagConstraints);
        userNameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Username"));
        userNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_Username"));

        userPasswordLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Pw_Mnemonic").charAt(0));
        userPasswordLabel.setLabelFor(passwordField);
        userPasswordLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Pw"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 5, 0);
        add(userPasswordLabel, gridBagConstraints);
        userPasswordLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Pw"));
        userPasswordLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_Pw"));

        domainLabel.setLabelFor(domainField);
        domainLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Domain"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 6, 0);
        add(domainLabel, gridBagConstraints);
        domainLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_Domain"));
        domainLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_Domain"));

        domainLocLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_InstallRoot_Mnemonic").charAt(0));
        domainLocLabel.setLabelFor(domainLocField);
        domainLocLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_DomainRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 0);
        add(domainLocLabel, gridBagConstraints);
        domainLocLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("LBL_InstallRoot"));
        domainLocLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("ACSD_InstallRoot"));

        msgLabel.setForeground(new java.awt.Color(89, 79, 191));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 0);
        add(msgLabel, gridBagConstraints);

        enableHttpMonitor.setMnemonic(org.openide.util.NbBundle.getBundle(ConnectionTabVisualPanel.class).getString("MNE_Connection").charAt(0));
        enableHttpMonitor.setText(org.openide.util.NbBundle.getBundle(ConnectionTabVisualPanel.class).getString("LBL_EnableHttpMonitor"));
        enableHttpMonitor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableHttpMonitorActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 0);
        add(enableHttpMonitor, gridBagConstraints);

    }//GEN-END:initComponents

    private void enableHttpMonitorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableHttpMonitorActionPerformed
        targetData.setHttpMonitorOn(""+enableHttpMonitor.isSelected());
        msgLabel.setText(NbBundle.getMessage(ConnectionTabVisualPanel.class, "Msg_httpMonitorStatusChangedAtRestart"));
        if (null != panel)
            panel.fireChangeEvent();
    }//GEN-LAST:event_enableHttpMonitorActionPerformed

    private void domainLocFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_domainLocFieldKeyReleased
        String installLoc = domainLocField.getText();
        //targetData.setInstallLocation(installLoc);
        //panel.fireChangeEvent();
    }//GEN-LAST:event_domainLocFieldKeyReleased

    private void domainFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_domainFieldKeyReleased
        String domainName = domainField.getText();
        //targetData.setDomain(domainName);
       
        //panel.fireChangeEvent();
    }//GEN-LAST:event_domainFieldKeyReleased

    private void passwordFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordFieldKeyReleased
        char[] passWd = passwordField.getPassword();
        String adminPassword = new String(passWd);
        targetData.setPassword(adminPassword);
        if (null != panel)
            panel.fireChangeEvent();
    }//GEN-LAST:event_passwordFieldKeyReleased

    private void userNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_userNameFieldKeyReleased
        String userName = userNameField.getText();
        targetData.setUserName(userName);
        if (null != panel)
            panel.fireChangeEvent();
    }//GEN-LAST:event_userNameFieldKeyReleased

    private void socketFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_socketFieldKeyReleased
        //String portNo = portField.getText();
        //targetData.setPort(portNo);
        //panel.fireChangeEvent();
    }//GEN-LAST:event_socketFieldKeyReleased
    
    public boolean isValid(){
//        if((portField.getText() == null) || (portField.getText().trim().equals(""))) {//NOI18N
//            msgLabel.setText(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "Msg_ValidPort"));
//            return false;
//        }
        if((userNameField.getText() == null) || (userNameField.getText().trim().equals(""))) {//NOI18N
            msgLabel.setText(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "Msg_ValidUser"));
            return false;
        }
        
        char[] passWd = passwordField.getPassword();
        String userPassword = new String(passWd);
     /*   if((userPassword == null) || (userPassword.trim().equals(""))) { //NOI18N
            msgLabel.setText(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "Msg_ValidPassword"));
            return false;
        }*/
        
//        if((domainField.getText() == null) || (domainField.getText().trim().equals(""))) { //NOI18N
//            msgLabel.setText(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "Msg_ValidDomain"));
//            return false;
//        }
//        if((domainLocField.getText() == null) || (domainLocField.getText().trim().equals(""))) {  //NOI18N
//            msgLabel.setText(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "Msg_ValidDomainDir"));
//            return false;
//        }
//        if((domainLocField.getText() != null) || (!domainLocField.getText().trim().equals(""))) { //NOI18N
//            File f = new File(domainLocField.getText()+domainField.getText());
//            if(!f.exists() || !f.canRead() || !f.isDirectory()  || !hasRequiredChildren(f, fileColl)) {
//                msgLabel.setText(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "Msg_InValidDomainDir",
//                        f.getAbsolutePath()));
//                return false;
//            }
//        }
//        
//        msgLabel.setText(""); //NOI18N
        return true;
    }
    
    private String getInstallLocation(){
        String insLocation = null;
        JFileChooser chooser = getJFileChooser();
        int returnValue = chooser.showDialog(this, NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "LBL_Choose_Button")); //NOI18N
        
        if(returnValue == JFileChooser.APPROVE_OPTION){
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }
    
    private JFileChooser getJFileChooser(){
        JFileChooser chooser = new JFileChooser();
        
        chooser.setDialogTitle(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "LBL_Chooser_Name")); //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "Choose_Button_Mnemonic").charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(new dirFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "LBL_Chooser_Name")); //NOI18N
        
        chooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "LBL_Chooser_Name")); //NOI18N
        chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "LBL_Chooser_Name")); //NOI18N
        
        return chooser;
    }
    
    static boolean hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate)
            return false;
        String[] children = candidate.list();
        if (null == children)
            return false;
        if (null == requiredChildren)
            return true;
        java.util.List kidsList = java.util.Arrays.asList(children);
        return kidsList.containsAll(requiredChildren);
    }
    
    private static Collection fileColl = new java.util.ArrayList();
    
    static {
        fileColl.add("bin"); //NOI18N
        fileColl.add("lib"); //NOI18N 
//        fileColl.add("appserv_uninstall.class"); //NOI18N
        fileColl.add("applications"); //NOI18N
        fileColl.add("config"); //NOI18N
        fileColl.add("docroot"); //NOI18N
        fileColl.add("generated"); //NOI18N
    }
    
    private static class dirFilter extends javax.swing.filechooser.FileFilter {
        
        public boolean accept(File f) {
            if(!f.exists() || !f.canRead() || !f.isDirectory() ) {
                return false;
            }else{
                return true;
            }
        }
        
        public String getDescription() {
            return NbBundle.getMessage(AddUserDefLocalServerVisualPanel.class, "LBL_DirType");
        }
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField domainField;
    private javax.swing.JLabel domainLabel;
    private javax.swing.JTextField domainLocField;
    private javax.swing.JLabel domainLocLabel;
    private javax.swing.JCheckBox enableHttpMonitor;
    private javax.swing.JLabel msgLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField socketField;
    private javax.swing.JTextField userNameField;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JLabel userPasswordLabel;
    // End of variables declaration//GEN-END:variables
        
}
