/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public final class SelectObjectFilesPanel extends JPanel {
    private SelectObjectFilesWizard wizard;
    private boolean ignoreEvent = false;
    private int chooserMode = 0;
    private String selectorID;
    
    /** Creates new form DiscoveryVisualPanel1 */
    public SelectObjectFilesPanel(SelectObjectFilesWizard wizard) {
        this.wizard = wizard;
        initComponents();
        addListeners();
    }

    private void addListeners(){
        DocumentListener documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }
            
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }
            
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        rootFolder.getDocument().addDocumentListener(documentListener);
    }
    
    private void initFields(String path) {
        // Set default values
        if (path == null) {
            rootFolder.setText(""); // NOI18N
        } else {
            if (Utilities.isWindows()) {
                path = path.replace('/', File.separatorChar);
            }
            rootFolder.setText(path);
        }
    }

    void read(DiscoveryDescriptor wizardDescriptor) {
        String oldSelectorID = selectorID;
        DiscoveryProvider provider = wizardDescriptor.getProvider();
        for(String key : provider.getPropertyKeys()){
            ProviderProperty property = provider.getProperty(key);
            LabelForRoot.setText(property.getName());
            instructionsTextArea.setText(property.getDescription());
            selectorID = key;
            switch (property.getKind()){
                case BinaryFile:
                    chooserMode = JFileChooser.FILES_ONLY;
                    break;
                case Folder:
                    chooserMode = JFileChooser.DIRECTORIES_ONLY;
                    break;
                default:
                    // unsuported UI
                    continue;
            }
            break;
        }
        if (!selectorID.equals(oldSelectorID)) {
            initFields(wizardDescriptor.getRootFolder());
            wizard.stateChanged(null);
        }
    }
    
    void store(DiscoveryDescriptor wizardDescriptor) {
        wizardDescriptor.getProvider().getProperty(selectorID).setValue(rootFolder.getText());
        wizardDescriptor.setInvokeProvider(true);
    }
    
    boolean valid() {
        String path = rootFolder.getText();
        if ( path.length() == 0 || selectorID == null) {
            return false;
        }
        if (chooserMode == JFileChooser.FILES_ONLY){
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                return true;
            }
        } else if (chooserMode == JFileChooser.DIRECTORIES_ONLY){
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    private void update(DocumentEvent e) {
        if (ignoreEvent) {
            // side-effect of changes done in this handler
            return;
        }
        // start ignoring events
        ignoreEvent = true;
        // stop ignoring events
        ignoreEvent = false;
        wizard.stateChanged(null);
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rootFolder = new javax.swing.JTextField();
        rootFolderButton = new javax.swing.JButton();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();
        LabelForRoot = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setMaximumSize(new java.awt.Dimension(100, 100));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        add(rootFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(rootFolderButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
        rootFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rootFolderButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        add(rootFolderButton, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setBackground(instructionPanel.getBackground());
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("BuildActionsInstructions"));
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        LabelForRoot.setLabelFor(rootFolder);
        org.openide.awt.Mnemonics.setLocalizedText(LabelForRoot, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("SpecifyRootFolder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(LabelForRoot, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void rootFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rootFolderButtonActionPerformed
        String seed = null;
        if (rootFolder.getText().length() > 0) {
            seed = rootFolder.getText();
        } else if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        
        JFileChooser fileChooser = new FileChooser(
                getString("ROOT_DIR_CHOOSER_TITLE_TXT"), // NOI18N
                getString("ROOT_DIR_BUTTON_TXT"), // NOI18N
                chooserMode, false, 
                null,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION)
            return;
        String path = fileChooser.getSelectedFile().getPath();
        //path = FilePathAdaptor.normalize(path);
        rootFolder.setText(path);
    }//GEN-LAST:event_rootFolderButtonActionPerformed

    private String getString(String key) {
        return NbBundle.getBundle(SelectObjectFilesPanel.class).getString(key);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelForRoot;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JTextField rootFolder;
    private javax.swing.JButton rootFolderButton;
    // End of variables declaration//GEN-END:variables
    
}

