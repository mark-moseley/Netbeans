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

package org.netbeans.modules.java.editor.options;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;

/**
 *
 * @author  Petr Hrebejk
 */
public class MarkOccurencesPanel extends javax.swing.JPanel {
   
    private static final boolean DEFAULT_VALUE = true; // May need to be splited if the defaunts ar not all on
    
    private List<JCheckBox> boxes;
    private MarkOccurencesOptionsPanelController controller;
    
    /** Creates new form MarkOccurencesPanel */
    public MarkOccurencesPanel( MarkOccurencesOptionsPanelController controller ) {
        initComponents();
        fillBoxes();
        addListeners();
        load( controller );
    }
    
    public void load( MarkOccurencesOptionsPanelController controller ) {
        this.controller = controller;
        
        Preferences node = controller.getCurrentNode();
        
        for (JCheckBox box : boxes) {
            box.setSelected(node.getBoolean(box.getActionCommand(), DEFAULT_VALUE));
        }
        
        componentsSetEnabled();
        
    }
    
    public void store( ) {
        Preferences node = controller.getCurrentNode();

        for (javax.swing.JCheckBox box : boxes) {
            boolean value = box.isSelected();
            boolean original = node.getBoolean(box.getActionCommand(),
                                               DEFAULT_VALUE);

            if (value != original) {
                node.putBoolean(box.getActionCommand(), value);
            }
        }
        try {
            node.flush();
        }
        catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
}
    
    public boolean changed() {
        
        Preferences node = controller.getCurrentNode();
        
        for (JCheckBox box : boxes) {
            boolean value = box.isSelected();
            boolean original = node.getBoolean(box.getActionCommand(), DEFAULT_VALUE);
            if ( value != original ) {
                return true;
            }
        }

        return false;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        onOffCheckBox = new javax.swing.JCheckBox();
        typesCheckBox = new javax.swing.JCheckBox();
        methodsCheckBox = new javax.swing.JCheckBox();
        constantsCheckBox = new javax.swing.JCheckBox();
        fieldsCheckBox = new javax.swing.JCheckBox();
        localVariablesCheckBox = new javax.swing.JCheckBox();
        exceptionsCheckBox = new javax.swing.JCheckBox();
        exitCheckBox = new javax.swing.JCheckBox();
        implementsCheckBox = new javax.swing.JCheckBox();
        overridesCheckBox = new javax.swing.JCheckBox();
        breakContinueCheckBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(onOffCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_OnOff_CheckBox")); // NOI18N
        onOffCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        onOffCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(onOffCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(typesCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_Types_CheckBox")); // NOI18N
        typesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        typesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(typesCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(methodsCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_Methods_CheckBox")); // NOI18N
        methodsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(methodsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(constantsCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_Constants_CheckBox")); // NOI18N
        constantsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        constantsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(constantsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fieldsCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_Fields_CheckBox")); // NOI18N
        fieldsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fieldsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(fieldsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(localVariablesCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_LocalVariables_CheckBox")); // NOI18N
        localVariablesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        localVariablesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(localVariablesCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(exceptionsCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_Exceptions_CheckBox")); // NOI18N
        exceptionsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exceptionsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(exceptionsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(exitCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_Exit_CheckBox")); // NOI18N
        exitCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exitCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(exitCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(implementsCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_Implements_CheckBox")); // NOI18N
        implementsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        implementsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(implementsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(overridesCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_Overrides_CheckBox")); // NOI18N
        overridesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        overridesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(overridesCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(breakContinueCheckBox, org.openide.util.NbBundle.getMessage(MarkOccurencesPanel.class, "CTL_BreakContinue_CheckBox")); // NOI18N
        breakContinueCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        breakContinueCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 0);
        add(breakContinueCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox breakContinueCheckBox;
    private javax.swing.JCheckBox constantsCheckBox;
    private javax.swing.JCheckBox exceptionsCheckBox;
    private javax.swing.JCheckBox exitCheckBox;
    private javax.swing.JCheckBox fieldsCheckBox;
    private javax.swing.JCheckBox implementsCheckBox;
    private javax.swing.JCheckBox localVariablesCheckBox;
    private javax.swing.JCheckBox methodsCheckBox;
    private javax.swing.JCheckBox onOffCheckBox;
    private javax.swing.JCheckBox overridesCheckBox;
    private javax.swing.JCheckBox typesCheckBox;
    // End of variables declaration//GEN-END:variables
    // End of variables declaration

    
    private void fillBoxes() {
        boxes = new ArrayList<JCheckBox>();
        boxes.add( onOffCheckBox );
        boxes.add( typesCheckBox );
        boxes.add( methodsCheckBox );
        boxes.add( constantsCheckBox );
        boxes.add( fieldsCheckBox );
        boxes.add( localVariablesCheckBox );
        boxes.add( exceptionsCheckBox );
        boxes.add( exitCheckBox );
        boxes.add( implementsCheckBox );
        boxes.add( overridesCheckBox );
        boxes.add( breakContinueCheckBox );
        
        onOffCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.ON_OFF);
        typesCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.TYPES);
        methodsCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.METHODS);
        constantsCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.CONSTANTS);
        fieldsCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.FIELDS);
        localVariablesCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.LOCAL_VARIABLES);
        exceptionsCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.EXCEPTIONS);
        exitCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.EXIT);
        implementsCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.IMPLEMENTS);
        overridesCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.OVERRIDES);
        breakContinueCheckBox.setActionCommand(MarkOccurencesOptionsPanelController.BREAK_CONTINUE);
    }
    
    
    private void addListeners() {
        ChangeListener cl = new CheckChangeListener();
        
        for( JCheckBox box : boxes ) {
            box.addChangeListener(cl);
        }
        
    }
    
    private void componentsSetEnabled() {
        for( int i = 1; i < boxes.size(); i++ ) {
            boxes.get(i).setEnabled(onOffCheckBox.isSelected()); // Switch off the other boxes
        }
    }
    
    
    private class CheckChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent evt) {
            
            if ( evt.getSource() == onOffCheckBox ) {
                componentsSetEnabled();
            }
            
            controller.changed();            
        }
        
    }
    
    
    
}
