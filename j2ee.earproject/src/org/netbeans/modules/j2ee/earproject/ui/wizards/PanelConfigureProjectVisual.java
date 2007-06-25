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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import org.openide.WizardDescriptor;

public class PanelConfigureProjectVisual extends JPanel {
    private static final long serialVersionUID = 1L;

    private final SettingsPanel projectLocationPanel;
    private final PanelOptionsVisual optionsPanel;

    /** Creates new form PanelInitProject */
    public PanelConfigureProjectVisual(PanelConfigureProject panel, String namePropIndex, ResourceBundle customBundle, boolean importStyle) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(customBundle.getString("ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N
        
        if (!importStyle) {
            projectLocationPanel = new PanelProjectLocationVisual(panel, namePropIndex, customBundle,importStyle);
        } else {
            projectLocationPanel = new PanelProjectImportVisual(panel, namePropIndex, customBundle,importStyle);
        }
        locationContainer.add(projectLocationPanel, BorderLayout.CENTER);

        optionsPanel = new PanelOptionsVisual(panel,importStyle);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        optionsContainer.add(optionsPanel, gridBagConstraints);
        
        if (!importStyle) {
            projectLocationPanel.addPropertyChangeListener(optionsPanel);
        }
        
        // Provide a name in the title bar.
        setName(customBundle.getString("LBL_NWP1_ProjectTitleName")); //NOI18N
        if (!importStyle) {
            putClientProperty("NewProjectWizard_Title", customBundle.getString("TXT_NewProject")); //NOI18N
        } else {
            putClientProperty("NewProjectWizard_Title", customBundle.getString("TXT_ImportProject")); //NOI18N
        }
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        if (!projectLocationPanel.valid(wizardDescriptor)) {
            return false;
        }
        boolean optionsValid = true;
        if (optionsPanel != null) {
            optionsValid = optionsPanel.valid(wizardDescriptor);
        }
        return optionsValid;
    }

    void read (WizardDescriptor d) {
        projectLocationPanel.read(d);
        if (null != optionsPanel) {
            optionsPanel.read(d);
        }
    }

    void store(WizardDescriptor d) {
        projectLocationPanel.store(d);
        if (null != optionsPanel) {
            optionsPanel.store(d);
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

        locationContainer = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        optionsContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setRequestFocusEnabled(false);
        locationContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(locationContainer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jSeparator1, gridBagConstraints);

        optionsContainer.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(optionsContainer, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel locationContainer;
    private javax.swing.JPanel optionsContainer;
    // End of variables declaration//GEN-END:variables

}
