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
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class CreateDomainVisualDirectoryPanel extends JPanel {
    
    
    /**
     * Creates new form AddInstanceVisualDirectoryPanel
     */
    public CreateDomainVisualDirectoryPanel() {
        initComponents();
        
        
        parentDirectory.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                locationChanged();
            }
            public void insertUpdate(DocumentEvent e) {
                locationChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                locationChanged();
            }
        });
    }
    
    public String getName() {
        return NbBundle.getMessage(CreateDomainVisualDirectoryPanel.class,
                "StepName_EnterDomainDirectory");                                // NOI18N
    }
    
    
    
    String getParentDirectory() {
        return parentDirectory.getText();
    }
    
    // Event handling
    //
    private final Set/*<ChangeListener>*/ listeners = new HashSet/*<ChangeListener>*/(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    void locationChanged() {
        fireChangeEvent();
    }
    
    
    private String browseDomainLocation(){
        String insLocation = null;
        JFileChooser chooser = null;
        
        chooser = new JFileChooser();
        
        Util.decorateChooser(chooser,parentDirectory.getText(),
                NbBundle.getMessage(CreateDomainVisualDirectoryPanel.class,
                "LBL_Choose_Domain"));                                          //NOI18N
        int returnValue = chooser.showDialog(this,
                NbBundle.getMessage(CreateDomainVisualDirectoryPanel.class,
                "LBL_Choose_Button"));                                          //NOI18N
        
        if(returnValue == JFileChooser.APPROVE_OPTION){
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        description = new javax.swing.JLabel();
        parentDirectoryLabel = new javax.swing.JLabel();
        parentDirectory = new javax.swing.JTextField();
        openInstanceDirectorySelector = new javax.swing.JButton();
        domainNameLabel = new javax.swing.JLabel();
        domainNameField = new javax.swing.JTextField();
        spaceHack = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        description.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("TXT_instanceDirectoryDescription2"));
        description.setEnabled(false);
        description.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(description, gridBagConstraints);

        parentDirectoryLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CreateDomainVisualDirectoryPanel.class, "MNM_instanceDirectoryLabel").charAt(0));
        parentDirectoryLabel.setLabelFor(parentDirectory);
        parentDirectoryLabel.setText("Domain Parent Folder:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 6, 6);
        add(parentDirectoryLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 6);
        add(parentDirectory, gridBagConstraints);
        parentDirectory.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("DSC_instanceDirectory"));

        openInstanceDirectorySelector.setMnemonic(org.openide.util.NbBundle.getMessage(CreateDomainVisualDirectoryPanel.class, "MNM_openInstanceDirectorySelector").charAt(0));
        openInstanceDirectorySelector.setText(org.openide.util.NbBundle.getMessage(CreateDomainVisualDirectoryPanel.class, "LBL_openInstanceDirectorySelector"));
        openInstanceDirectorySelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInstanceDirectorySelectorActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 0);
        add(openInstanceDirectorySelector, gridBagConstraints);
        openInstanceDirectorySelector.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("DSC_openInstanceDirectorySelector"));

        domainNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("MNM_adminPortLabel").charAt(0));
        domainNameLabel.setLabelFor(domainNameField);
        domainNameLabel.setText(org.openide.util.NbBundle.getMessage(CreateDomainVisualDirectoryPanel.class, "LBL_domainNameLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        add(domainNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 6);
        add(domainNameField, gridBagConstraints);

        spaceHack.setEnabled(false);
        spaceHack.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        add(spaceHack, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void openInstanceDirectorySelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInstanceDirectorySelectorActionPerformed
        String val = browseDomainLocation();
        if (null != val && val.length() >=1)
            parentDirectory.setText(val);
    }//GEN-LAST:event_openInstanceDirectorySelectorActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel description;
    private javax.swing.JTextField domainNameField;
    private javax.swing.JLabel domainNameLabel;
    private javax.swing.JButton openInstanceDirectorySelector;
    private javax.swing.JTextField parentDirectory;
    private javax.swing.JLabel parentDirectoryLabel;
    private javax.swing.JLabel spaceHack;
    // End of variables declaration//GEN-END:variables
    
}

