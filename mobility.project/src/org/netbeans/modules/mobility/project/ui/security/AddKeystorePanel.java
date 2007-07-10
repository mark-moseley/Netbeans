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

/*
 * CreateKeystorePanel.java
 *
 * Created on May 31, 2004
 */
package org.netbeans.modules.mobility.project.ui.security;

import org.netbeans.modules.mobility.project.ui.wizard.Utils;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;

/**
 *
 * @author  Adam Sotona
 */
public class AddKeystorePanel extends javax.swing.JPanel implements ActionListener, DocumentListener {
    
    static String location = System.getProperty("user.home", ""); // NOI18N
    
    private DialogDescriptor dd;
    private static final Dimension PREFERRED_SIZE = new Dimension(500, 300);
    
    /** Creates new form CreateKeystorePanel */
    public AddKeystorePanel() {
        initComponents();
        initAccessibility();
        rNew.addActionListener(this);
        rExisting.addActionListener(this);
        tName.getDocument().addDocumentListener(this);
        tLocation.getDocument().addDocumentListener(this);
        tPassword.getDocument().addDocumentListener(this);
        tPasswordConfirm.getDocument().addDocumentListener(this);
        tFile.getDocument().addDocumentListener(this);
        
        rNew.setSelected(true);
        tName.setText("keystore.ks"); // NOI18N
        tLocation.setText(location);
        tFile.setText(location);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        rNew = new javax.swing.JRadioButton();
        lName = new javax.swing.JLabel();
        tName = new javax.swing.JTextField();
        lLocation = new javax.swing.JLabel();
        tLocation = new javax.swing.JTextField();
        bBrowseLocation = new javax.swing.JButton();
        lPassword = new javax.swing.JLabel();
        tPassword = new javax.swing.JPasswordField();
        lPasswordConfirm = new javax.swing.JLabel();
        tPasswordConfirm = new javax.swing.JPasswordField();
        rExisting = new javax.swing.JRadioButton();
        lFile = new javax.swing.JLabel();
        tFile = new javax.swing.JTextField();
        bBrowseFile = new javax.swing.JButton();
        pError = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        setMinimumSize(new java.awt.Dimension(350, 200));
        setPreferredSize(new java.awt.Dimension(450, 200));
        buttonGroup.add(rNew);
        rNew.setMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_CreateNew").charAt(0));
        rNew.setSelected(true);
        rNew.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_CreateNew"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(rNew, gridBagConstraints);
        rNew.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_New"));

        lName.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_Name").charAt(0));
        lName.setLabelFor(tName);
        lName.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 12, 5);
        add(lName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(tName, gridBagConstraints);
        tName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_Name"));

        lLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_Folder").charAt(0));
        lLocation.setLabelFor(tLocation);
        lLocation.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_Folder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 5);
        add(lLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(tLocation, gridBagConstraints);
        tLocation.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_Folder"));

        bBrowseLocation.setMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_BrowseFolder").charAt(0));
        bBrowseLocation.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_BrowseFolder"));
        bBrowseLocation.addActionListener(new java.awt.event.ActionListener() {
            @SuppressWarnings("synthetic-access")
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBrowseLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(bBrowseLocation, gridBagConstraints);
        bBrowseLocation.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_Browse1"));

        lPassword.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_Password").charAt(0));
        lPassword.setLabelFor(tPassword);
        lPassword.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_Password"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 16, 12, 5);
        add(lPassword, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 12, 0);
        add(tPassword, gridBagConstraints);
        tPassword.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_Password"));

        lPasswordConfirm.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_ConfirmPassword").charAt(0));
        lPasswordConfirm.setLabelFor(tPasswordConfirm);
        lPasswordConfirm.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_ConfirmPassword"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 5);
        add(lPasswordConfirm, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(tPasswordConfirm, gridBagConstraints);
        tPasswordConfirm.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_Password2"));

        buttonGroup.add(rExisting);
        rExisting.setMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_AddExisting").charAt(0));
        rExisting.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_AddExisting"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 12, 0);
        add(rExisting, gridBagConstraints);
        rExisting.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_Existing"));

        lFile.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_File").charAt(0));
        lFile.setLabelFor(tFile);
        lFile.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_File"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 5);
        add(lFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(tFile, gridBagConstraints);
        tFile.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_Keystore"));

        bBrowseFile.setMnemonic(org.openide.util.NbBundle.getBundle(AddKeystorePanel.class).getString("MNM_AddKeystore_BrowseFile").charAt(0));
        bBrowseFile.setText(org.openide.util.NbBundle.getMessage(AddKeystorePanel.class, "LBL_AddKeystore_BrowseFile"));
        bBrowseFile.addActionListener(new java.awt.event.ActionListener() {
            @SuppressWarnings("synthetic-access")
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBrowseFileActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(bBrowseFile, gridBagConstraints);
        bBrowseFile.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore_Browse2"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pError, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddKeystorePanel.class, "ACSN_AddKeystore"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddKeystorePanel.class, "ACSD_AddKeystore"));
    }
    
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }
    
    protected void setDialogDescriptor(final DialogDescriptor desc) {
        this.dd = desc;
        dd.setHelpCtx(new HelpCtx(AddKeystorePanel.class));
        actionPerformed(null);
    }
    
    public String getErrorMessage() {
        File file;
        if (rNew.isSelected()) {
            final String text = tName.getText();
            if (text == null  ||  "".equals(text)) // NOI18N
                return "ERR_EmptyKSFileName"; // NOI18N
            file = new File(tLocation.getText());
            if (! file.exists()  ||  ! file.isDirectory()  ||  ! file.canWrite())
                return "ERR_KSFolderNotExists"; // NOI18N
            file = new File(file, text);
            if (file.exists())
                return "ERR_KSFileExists"; // NOI18N
            if (KeyStoreRepository.getDefault().getKeyStore(file.getAbsolutePath(), false) != null)
                return "ERR_KSFileAlreadyAdded"; // NOI18N
            if (tPassword.getPassword().length < 6)
                return "ERR_PasswordSmall"; // NOI18N
            if (! new String(tPassword.getPassword()).equals(new String(tPasswordConfirm.getPassword())))
                return "ERR_PasswordsNotEqual"; // NOI18N
        } else {
            file = new File(tFile.getText());
            if (! file.exists()  ||  ! file.isFile())
                return "ERR_KSFileNotExists"; // NOI18N
        }
        return null;
    }
    
    public void checkErrors() {
        final String errorMessage = getErrorMessage();
        pError.setErrorMessage(errorMessage != null ? NbBundle.getMessage(AddKeystorePanel.class, errorMessage) : null);
        final boolean valid = errorMessage == null;
        if (dd != null  &&  valid != dd.isValid())
            dd.setValid(valid);
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent e) {
        final boolean selected = rNew.isSelected();
        lName.setEnabled(selected);
        tName.setEnabled(selected);
        lLocation.setEnabled(selected);
        tLocation.setEnabled(selected);
        bBrowseLocation.setEnabled(selected);
        lPassword.setEnabled(selected);
        tPassword.setEnabled(selected);
        lPasswordConfirm.setEnabled(selected);
        tPasswordConfirm.setEnabled(selected);
        lFile.setEnabled(! selected);
        tFile.setEnabled(! selected);
        bBrowseFile.setEnabled(! selected);
        
        checkErrors();
    }
    
    public void changedUpdate(@SuppressWarnings("unused")
	final DocumentEvent e) {
        checkErrors();
    }
    
    public void insertUpdate(@SuppressWarnings("unused")
	final DocumentEvent e) {
        checkErrors();
    }
    
    public void removeUpdate(@SuppressWarnings("unused")
	final DocumentEvent e) {
        checkErrors();
    }
    
    private void bBrowseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseFileActionPerformed
        final String file = Utils.browseFilter(this, tFile.getText(), NbBundle.getMessage(AddKeystorePanel.class, "TITLE_SelectKeystore"), JFileChooser.FILES_ONLY, new FileFilter() { // NOI18N
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                if (! f.isFile())
                    return false;
                String name = f.getName();
                int i = name.lastIndexOf('.');
                if (i < 0)
                    return false;
                name = name.substring(i).toLowerCase();
                return ".ks".equals(name)  ||  ".keystore".equals(name)  ||  ".p12".equals(name)  ||  ".pkcs12".equals(name) || ".jks".equals(name); // NOI18N
            }
            
            public String getDescription() {
                return NbBundle.getMessage(AddKeystorePanel.class, "LBL_KeystoreFileFilter"); // NOI18N
            }
            
        });
        if (file == null)
            return;
        File f = new File(file);
        f = f.getParentFile();
        if (f != null)
            location = f.getAbsolutePath();
        else
            location = file;
        tFile.setText(file);
    }//GEN-LAST:event_bBrowseFileActionPerformed
    
    private void bBrowseLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseLocationActionPerformed
        final String folder = Utils.browseFolder(this, tLocation.getText(), NbBundle.getMessage(AddKeystorePanel.class, "TITLE_SelectKeystoresFolder")); // NOI18N
        if (folder == null)
            return;
        tLocation.setText(location = folder);
    }//GEN-LAST:event_bBrowseLocationActionPerformed
    
    public static KeyStoreRepository.KeyStoreBean showAddKeystorePanel() {
        final AddKeystorePanel add = new AddKeystorePanel();
        final DialogDescriptor dd = new DialogDescriptor(add, NbBundle.getMessage(AddKeystorePanel.class, "TITLE_AddKeystore"), true, null); // NOI18N
        add.setDialogDescriptor(dd);
        add.checkErrors();
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        if (dd.getValue() != NotifyDescriptor.OK_OPTION)
            return null;
        KeyStoreRepository.KeyStoreBean bean;
        if (add.rNew.isSelected()) {
            String file = add.tLocation.getText() + File.separator + add.tName.getText();
            if (! file.endsWith(".ks")  &&  ! file.endsWith(".keystore")) // NOI18N
                file += ".ks"; // NOI18N
            bean = KeyStoreRepository.getDefault().getKeyStore(file, false);
            if (bean == null) {
                bean = KeyStoreRepository.KeyStoreBean.create(file, new String(add.tPassword.getPassword()));
                bean.openKeyStore(true);
                KeyStoreRepository.getDefault().addKeyStore(bean);
            }
        } else {
            String file = add.tFile.getText();
            bean = KeyStoreRepository.getDefault().getKeyStore(file, true);
        }
        return bean;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowseFile;
    private javax.swing.JButton bBrowseLocation;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel lFile;
    private javax.swing.JLabel lLocation;
    private javax.swing.JLabel lName;
    private javax.swing.JLabel lPassword;
    private javax.swing.JLabel lPasswordConfirm;
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel pError;
    private javax.swing.JRadioButton rExisting;
    private javax.swing.JRadioButton rNew;
    private javax.swing.JTextField tFile;
    private javax.swing.JTextField tLocation;
    private javax.swing.JTextField tName;
    private javax.swing.JPasswordField tPassword;
    private javax.swing.JPasswordField tPasswordConfirm;
    // End of variables declaration//GEN-END:variables
    
}
