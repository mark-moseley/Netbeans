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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * The first panel in the <em>New Action Wizard</em>.
 *
 * @author Martin Krauskopf
 */
final class ActionTypePanel extends BasicWizardIterator.Panel {
    
    private static final Map/*<String, String>*/ NAME_TO_FQCN;
    private static final DefaultComboBoxModel COOKIE_CLASS_MODEL;
    
    static {
        COOKIE_CLASS_MODEL = new DefaultComboBoxModel();
        Map map = new HashMap(DataModel.PREDEFINED_COOKIE_CLASSES.length);
        for (int i = 0; i < DataModel.PREDEFINED_COOKIE_CLASSES.length; i++) {
            String fqcn = DataModel.PREDEFINED_COOKIE_CLASSES[i];
            String name = DataModel.parseClassName(fqcn);
            map.put(name, fqcn);
            COOKIE_CLASS_MODEL.addElement(name);
        }
        NAME_TO_FQCN = Collections.unmodifiableMap(map);
    }
    
    private DataModel data;
    
    public ActionTypePanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        coockieClass.setModel(COOKIE_CLASS_MODEL);
    }
    
    protected String getPanelName() {
        return getMessage("LBL_ActionType_Title"); // NOI18N
    }
    
    protected void storeToDataModel() {
        data.setAlwaysEnabled(alwaysEnabled.isSelected());
        data.setCookieClasses(getCookieClasses());
        data.setMultiSelection(isMultiSelection());
    }
    
    private String[] getCookieClasses() {
        StringTokenizer classesST  = new StringTokenizer(
                coockieClass.getEditor().getItem().toString(), ","); // NOI18N
        Collection classes = new ArrayList();
        while (classesST.hasMoreTokens()) {
            String clazz = ((String) classesST.nextToken()).trim();
            String fqcn = (String) NAME_TO_FQCN.get(clazz);
            classes.add(fqcn == null ? clazz : fqcn);
        }
        String[] s = new String[classes.size()];
        return (String[]) classes.toArray(s);
    }
    
    protected void readFromDataModel() {
        condionallyEnabledActionPerformed(null);
        setValid(Boolean.TRUE);
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(ActionTypePanel.class);
    }
    
    private boolean isMultiSelection() {
        return modeMultiple.isSelected();
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
        modeGroup = new javax.swing.ButtonGroup();
        alwaysEnabled = new javax.swing.JRadioButton();
        condionallyEnabled = new javax.swing.JRadioButton();
        coockieClassTxt = new javax.swing.JLabel();
        coockieClass = new javax.swing.JComboBox();
        filler = new javax.swing.JLabel();
        modeOne = new javax.swing.JRadioButton();
        modeMultiple = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        whenEnabledGroup.add(alwaysEnabled);
        alwaysEnabled.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(alwaysEnabled, org.openide.util.NbBundle.getMessage(ActionTypePanel.class, "CTL_AlwaysEnabled"));
        alwaysEnabled.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
        condionallyEnabled.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(condionallyEnabled, gridBagConstraints);

        coockieClassTxt.setLabelFor(coockieClass);
        org.openide.awt.Mnemonics.setLocalizedText(coockieClassTxt, org.openide.util.NbBundle.getMessage(ActionTypePanel.class, "LBL_CookieClass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 6, 0);
        add(coockieClassTxt, gridBagConstraints);

        coockieClass.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(coockieClass, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        modeGroup.add(modeOne);
        modeOne.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(modeOne, org.openide.util.NbBundle.getMessage(ActionTypePanel.class, "CTL_ModeOneNode"));
        modeOne.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modeOne.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 6, 0);
        add(modeOne, gridBagConstraints);

        modeGroup.add(modeMultiple);
        org.openide.awt.Mnemonics.setLocalizedText(modeMultiple, org.openide.util.NbBundle.getMessage(ActionTypePanel.class, "CTL_ModeMultipleNode"));
        modeMultiple.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modeMultiple.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        add(modeMultiple, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void condionallyEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_condionallyEnabledActionPerformed
        boolean enabled = condionallyEnabled.isSelected();
        coockieClass.setEnabled(enabled);
        coockieClassTxt.setEnabled(enabled);
        modeOne.setEnabled(enabled);
        modeMultiple.setEnabled(enabled);
    }//GEN-LAST:event_condionallyEnabledActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton alwaysEnabled;
    private javax.swing.JRadioButton condionallyEnabled;
    private javax.swing.JComboBox coockieClass;
    private javax.swing.JLabel coockieClassTxt;
    private javax.swing.JLabel filler;
    private javax.swing.ButtonGroup modeGroup;
    private javax.swing.JRadioButton modeMultiple;
    private javax.swing.JRadioButton modeOne;
    private javax.swing.ButtonGroup whenEnabledGroup;
    // End of variables declaration//GEN-END:variables
    
}

