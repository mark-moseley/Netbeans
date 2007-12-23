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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.project.wizards;

import javax.swing.JPanel;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

class PanelConfigureProjectVisual
    extends JPanel
    implements org.netbeans.modules.bpel.project.IcanproConstants {

    private PanelConfigureProject panel;

    private PanelProjectLocationVisual projectLocationPanel;
    private PanelOptionsVisual optionsPanel;

    /** Creates new form PanelInitProject */
    public PanelConfigureProjectVisual(PanelConfigureProject panel) {
        this.panel = panel;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(WIZARD_BUNDLE).getString("ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N
        
        projectLocationPanel = new PanelProjectLocationVisual(panel);
        locationContainer.add(projectLocationPanel, java.awt.BorderLayout.NORTH);

        optionsPanel = new PanelOptionsVisual(panel);
        optionsContainer.add(optionsPanel, java.awt.BorderLayout.NORTH);

///
/*        DocumentListener dl = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                setContextPath(e);
            }

            public void insertUpdate(DocumentEvent e) {
                setContextPath(e);
            }

            public void removeUpdate(DocumentEvent e) {
                setContextPath(e);
            }

            private void setContextPath(DocumentEvent e) {
                if (!optionsPanel.isContextModified())
                    optionsPanel.jTextFieldContextPath.setText("/" + projectLocationPanel.projectNameTextField.getText().trim().replace(' ', '_'));
            }
        };
        projectLocationPanel.projectNameTextField.getDocument().addDocumentListener(dl);
*/
///
        projectLocationPanel.addPropertyChangeListener(optionsPanel);

        // Provide a name in the title bar.
        setName(NbBundle.getBundle(WIZARD_BUNDLE).getString("LBL_NWP1_ProjectTitleName")); //NOI18N
        putClientProperty ("NewProjectWizard_Title", NbBundle.getBundle(WIZARD_BUNDLE).getString("TXT_NewWebApp")); //NOI18N
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        return projectLocationPanel.valid(wizardDescriptor) && optionsPanel.valid(wizardDescriptor);
    }

    void read (WizardDescriptor d) {
        projectLocationPanel.read(d);
        optionsPanel.read(d);
    }

    void store(WizardDescriptor d) {
        projectLocationPanel.store(d);
        optionsPanel.store(d);
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

        optionsContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
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
