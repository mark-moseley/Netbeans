/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.wizards;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class PanelConfigureProjectVisual extends JPanel {
    
    private PanelConfigureProject panel;
        
    private PanelProjectLocationVisual projectLocationPanel;
    private PanelOptionsVisual optionsPanel;
    
    /** Creates new form PanelInitProject */
    public PanelConfigureProjectVisual(PanelConfigureProject panel) {
        this.panel = panel;
        initComponents();
                
        projectLocationPanel = new PanelProjectLocationVisual(panel);
        locationContainer.add(projectLocationPanel, java.awt.BorderLayout.NORTH);
                
        optionsPanel = new PanelOptionsVisual(panel);
        optionsContainer.add(optionsPanel, java.awt.BorderLayout.NORTH);
        
        DocumentListener dl = new DocumentListener() {
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

        
        // Provide a name in the title bar.
        setName(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("LBL_NWP1_ProjectTitleName")); //NOI18N
        putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelConfigureProjectVisual.class, "TXT_NewWebApp")); //NOI18N
    }
    
    boolean valid() {
        return projectLocationPanel.valid() && optionsPanel.valid();
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
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        locationContainer = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        optionsContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(550, 350));
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
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        gridBagConstraints.weightx = 1.0;
        add(jSeparator1, gridBagConstraints);

        optionsContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(optionsContainer, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel locationContainer;
    private javax.swing.JPanel optionsContainer;
    // End of variables declaration//GEN-END:variables
    
}
