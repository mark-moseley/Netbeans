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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;

/**
 * The first panel in the <em>New Action Wizard</em>.
 *
 * @author Martin Krauskopf
 */
final class ActionTypePanel extends BasicWizardIterator.Panel {
    
    private DataModel data;
    
    public ActionTypePanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        // XXX temporary hardcoded few ones - where to get all these values?
        coockieClass.setModel(new DefaultComboBoxModel(new String[] {
            "DataObject",
            "EditCookie",
            "EditorCookie",
            "OpenCookie",
            "Project"
        }));
    }
    
    protected String getPanelName() {
        return getMessage("LBL_ActionType_Title"); // NOI18N
    }
    
    protected void storeToDataModel() {
        data.setAlwaysEnabled(alwaysEnabled.isSelected());
        data.setCookieClasses(new String[] { (String) coockieClass.getSelectedItem() });
    }
    
    protected void readFromDataModel() {
        condionallyEnabledActionPerformed(null);
        setValid(Boolean.TRUE);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        whenEnabledGroup = new javax.swing.ButtonGroup();
        alwaysEnabled = new javax.swing.JRadioButton();
        condionallyEnabled = new javax.swing.JRadioButton();
        coockieClassTxt = new javax.swing.JLabel();
        coockieClass = new javax.swing.JComboBox();
        multiSelection = new javax.swing.JCheckBox();
        filler = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        whenEnabledGroup.add(alwaysEnabled);
        alwaysEnabled.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(alwaysEnabled, org.openide.util.NbBundle.getMessage(ActionTypePanel.class, "CTL_AlwaysEnabled"));
        alwaysEnabled.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        alwaysEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        alwaysEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                condionallyEnabledActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(alwaysEnabled, gridBagConstraints);

        whenEnabledGroup.add(condionallyEnabled);
        org.openide.awt.Mnemonics.setLocalizedText(condionallyEnabled, org.openide.util.NbBundle.getMessage(ActionTypePanel.class, "CTL_ConditionallyEnabled"));
        condionallyEnabled.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        condionallyEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        condionallyEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                condionallyEnabledActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(condionallyEnabled, gridBagConstraints);

        coockieClassTxt.setLabelFor(coockieClass);
        org.openide.awt.Mnemonics.setLocalizedText(coockieClassTxt, org.openide.util.NbBundle.getMessage(ActionTypePanel.class, "LBL_CookieClass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 6, 0);
        add(coockieClassTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(coockieClass, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(multiSelection, org.openide.util.NbBundle.getMessage(ActionTypePanel.class, "CTL_AllowMultipleSelections"));
        multiSelection.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        multiSelection.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        add(multiSelection, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void condionallyEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_condionallyEnabledActionPerformed
        boolean enabled = condionallyEnabled.isSelected();
        coockieClass.setEnabled(enabled);
        coockieClassTxt.setEnabled(enabled);
        multiSelection.setEnabled(enabled);
    }//GEN-LAST:event_condionallyEnabledActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton alwaysEnabled;
    private javax.swing.JRadioButton condionallyEnabled;
    private javax.swing.JComboBox coockieClass;
    private javax.swing.JLabel coockieClassTxt;
    private javax.swing.JLabel filler;
    private javax.swing.JCheckBox multiSelection;
    private javax.swing.ButtonGroup whenEnabledGroup;
    // End of variables declaration//GEN-END:variables
    
}
