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

package org.netbeans.modules.cnd.makeproject.api.actions;

import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.wizards.SourceFilesPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class AddExistingFolderItemsAction extends NodeAction {
    
    protected boolean enable(Node[] activatedNodes)  {
        if (activatedNodes.length != 1)
            return false;
        Folder folder = (Folder)activatedNodes[0].getValue("Folder"); // NOI18N
        if (folder == null)
            return false;
        if (!folder.isProjectFiles())
            return false;
        return true;
    }
    
    public String getName() {
        return getString("CTL_AddExistingFolderItemsAction"); // NOI18N
    }
    
    public void performAction(Node[] activatedNodes) {
        boolean notifySources = false;
        Node n = activatedNodes[0];
        Project project = (Project)n.getValue("Project"); // NOI18N
        assert project != null;
        Folder folder = (Folder)n.getValue("Folder"); // NOI18N
        assert folder != null;
        
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        MakeConfigurationDescriptor makeConfigurationDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        
        String seed = null;
        if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        }
        if (seed == null) {
            seed = makeConfigurationDescriptor.getBaseDir();
        }
        
        JButton addButton = new JButton("Add");
        Object[] options = new Object[] {
            addButton,
            DialogDescriptor.CANCEL_OPTION,
        };
        SourceFilesPanel sourceFilesPanel = new SourceFilesPanel();
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        panel.add(sourceFilesPanel, gridBagConstraints);
        
        JTextArea instructionsTextArea = new JTextArea();
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(getString("AddExistingFolderItemsTxt")); // NOI8N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setBackground(panel.getBackground());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        panel.add(instructionsTextArea, gridBagConstraints);
        
        sourceFilesPanel.setSeed(makeConfigurationDescriptor.getBaseDir(), null);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                "Add Files", 
                true,
                options,
                addButton,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);
        Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (ret == addButton) {
            makeConfigurationDescriptor.addSourceFilesFromFolders(folder, sourceFilesPanel.getListData());
        }
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(BatchBuildAction.class);
        }
        return bundle.getString(s);
    }
    private static String getString(String s, String arg) {
        return NbBundle.getMessage(BatchBuildAction.class, s, arg);
    }
}
