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

/*
 * DerivationTypeForm.java
 *
 * Created on December 18, 2005, 9:21 PM
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Derivation;

/**
 *
 * @author  Ajit Bhate
 */
public class DerivationTypeForm extends javax.swing.JPanel implements ActionListener {

    static final long serialVersionUID = 1L;
    private String property = "final";
    private Set<Derivation.Type> currentSelection;
    private Set<Derivation.Type> supportedTypes;
    private boolean isValid = true;
    
    /**
     * Creates new form DerivationTypeForm
     */
    
    public DerivationTypeForm(String property, 
            Set<Derivation.Type> initialSelection, 
            Set<Derivation.Type> supportedTypes) {
        this.currentSelection = initialSelection==null?
            new HashSet<Derivation.Type>():initialSelection;
        this.property = property;
        this.supportedTypes = supportedTypes;
        initComponents();
        initialize();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        radioButtonGroup = new javax.swing.ButtonGroup();
        useDefaultRadioButton = new javax.swing.JRadioButton();
        useDefaultLabel = new javax.swing.JLabel();
        allowAllRadioButton = new javax.swing.JRadioButton();
        preventTypeRadioButton = new javax.swing.JRadioButton();
        extensionCheckBox = new javax.swing.JCheckBox();
        restrictionCheckBox = new javax.swing.JCheckBox();
        preventAllRadioButton = new javax.swing.JRadioButton();
        listCheckBox = new javax.swing.JCheckBox();
        unionCheckBox = new javax.swing.JCheckBox();
        substitutionCheckBox = new javax.swing.JCheckBox();
        dummyLabel1 = new javax.swing.JLabel();
        dummyLabel2 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultRadioButton, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Use_Schema_Default_"+property));
        useDefaultRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Use_Schema_Default_"+property));
        useDefaultRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useDefaultRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useDefaultRadioButton.addActionListener(this);
        radioButtonGroup.add(useDefaultRadioButton);

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultLabel, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Default_Value_"+property));
        useDefaultLabel.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Default_Value_"+property));

        org.openide.awt.Mnemonics.setLocalizedText(allowAllRadioButton, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Allow_All_"+property));
        allowAllRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Allow_All_"+property));
        allowAllRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        allowAllRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        allowAllRadioButton.addActionListener(this);
        radioButtonGroup.add(allowAllRadioButton);

        org.openide.awt.Mnemonics.setLocalizedText(preventTypeRadioButton, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Prevent_Derivations_"+property));
        preventTypeRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Prevent_Derivations_"+property));
        preventTypeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        preventTypeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        preventTypeRadioButton.addActionListener(this);
        radioButtonGroup.add(preventTypeRadioButton);

        org.openide.awt.Mnemonics.setLocalizedText(extensionCheckBox, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Extension"));
        extensionCheckBox.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Extension"));
        extensionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        extensionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        extensionCheckBox.addActionListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(restrictionCheckBox, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Restriction"));
        restrictionCheckBox.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Restriction"));
        restrictionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        restrictionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        restrictionCheckBox.addActionListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(preventAllRadioButton, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Prevent_All_"+property));
        preventAllRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Prevent_All_"+property));
        preventAllRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        preventAllRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        preventAllRadioButton.addActionListener(this);
        radioButtonGroup.add(preventAllRadioButton);

        org.openide.awt.Mnemonics.setLocalizedText(listCheckBox, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_List"));
        listCheckBox.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_List"));
        listCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        listCheckBox.addActionListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(unionCheckBox, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Union"));
        unionCheckBox.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Union"));
        unionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        unionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        unionCheckBox.addActionListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(substitutionCheckBox, org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("LBL_Substitution"));
        substitutionCheckBox.setToolTipText(org.openide.util.NbBundle.getBundle(DerivationTypeForm.class).getString("HINT_Substitution"));
        substitutionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        substitutionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        substitutionCheckBox.addActionListener(this);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(useDefaultLabel))
                    .add(useDefaultRadioButton)
                    .add(allowAllRadioButton)
                    .add(preventAllRadioButton)
                    .add(preventTypeRadioButton)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(extensionCheckBox)
                            .add(restrictionCheckBox)
                            .add(substitutionCheckBox)
                            .add(listCheckBox)
                            .add(unionCheckBox)
                            .add(dummyLabel2)
                            .add(dummyLabel1))))
                .addContainerGap(227, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(useDefaultRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(useDefaultLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(allowAllRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(preventAllRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(preventTypeRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dummyLabel1)
                    .add(layout.createSequentialGroup()
                        .add(extensionCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(restrictionCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(substitutionCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dummyLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(unionCheckBox)))
                .addContainerGap(102, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void initialize() {
        if(!supportedTypes.contains(Derivation.Type.EXTENSION)) {
            extensionCheckBox.setVisible(false);
        }
        if(!supportedTypes.contains(Derivation.Type.RESTRICTION)) {
            restrictionCheckBox.setVisible(false);
        }
        if(!supportedTypes.contains(Derivation.Type.LIST)) {
            listCheckBox.setVisible(false);
        }
        if(!supportedTypes.contains(Derivation.Type.UNION)) {
            unionCheckBox.setVisible(false);
        }
        if (!supportedTypes.contains(Derivation.Type.SUBSTITUTION)) {
            substitutionCheckBox.setVisible(false);
        }

        if (currentSelection.isEmpty()) {
            useDefaultRadioButton.setSelected(true);
        } else if (currentSelection.contains(Derivation.Type.EMPTY)){
            allowAllRadioButton.setSelected(true);
        } else if (currentSelection.contains(Derivation.Type.ALL)){
            preventAllRadioButton.setSelected(true);
        } else {
            for (Derivation.Type type:currentSelection) {
                if(type == Derivation.Type.EXTENSION) {
                    extensionCheckBox.setSelected(true);
                } else if(type == Derivation.Type.RESTRICTION) {
                    restrictionCheckBox.setSelected(true);
                } else if(type == Derivation.Type.LIST) {
                    listCheckBox.setSelected(true);
                } else if(type == Derivation.Type.UNION) {
                    unionCheckBox.setSelected(true);
                } else if(type == Derivation.Type.SUBSTITUTION) {
                    substitutionCheckBox.setSelected(true);
                }
            }
            if(checkBoxSelected()) preventTypeRadioButton.setSelected(true);
        }
    }

    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if(source instanceof javax.swing.JRadioButton) {
            if (source.equals(useDefaultRadioButton)) {
                currentSelection.clear();
                setCheckBoxesSelected(false);
            } else if (source.equals(allowAllRadioButton)) {
                currentSelection.clear();
                currentSelection.add(Derivation.Type.EMPTY);
                setCheckBoxesSelected(false);
            } else if (source.equals(preventAllRadioButton)) {
                currentSelection.clear();
                currentSelection.add(Derivation.Type.ALL);
                setCheckBoxesSelected(false);
            } else if (source.equals(preventTypeRadioButton)) {
                if (!checkBoxSelected()) currentSelection.clear();
            }
        } else if(source instanceof javax.swing.JCheckBox) {
            if(((javax.swing.JCheckBox)source).isSelected() &&
                    !preventTypeRadioButton.isSelected()) {
                preventTypeRadioButton.setSelected(true);
                currentSelection.clear();
            }
            if (source.equals(extensionCheckBox)) {
                if(extensionCheckBox.isSelected()) {
                    currentSelection.add(Derivation.Type.EXTENSION);
                } else {
                    currentSelection.remove(Derivation.Type.EXTENSION);
                }
            } else if (source.equals(restrictionCheckBox)) {
                if(restrictionCheckBox.isSelected()) {
                    currentSelection.add(Derivation.Type.RESTRICTION);
                } else {
                    currentSelection.remove(Derivation.Type.RESTRICTION);
                }
            } else if (source.equals(listCheckBox)) {
                if(listCheckBox.isSelected()) {
                    currentSelection.add(Derivation.Type.LIST);
                } else {
                    currentSelection.remove(Derivation.Type.LIST);
                }
            } else if (source.equals(unionCheckBox)) {
                if(unionCheckBox.isSelected()) {
                    currentSelection.add(Derivation.Type.UNION);
                } else {
                    currentSelection.remove(Derivation.Type.UNION);
                }
            } else if (source.equals(substitutionCheckBox)) {
                if(substitutionCheckBox.isSelected()) {
                    currentSelection.add(Derivation.Type.SUBSTITUTION);
                } else {
                    currentSelection.remove(Derivation.Type.SUBSTITUTION);
                }
            }
        }
        // notify dialog to disable ok option if needed
        firePropertyChange("valid", isValid, !currentSelection.isEmpty() || 
                !preventTypeRadioButton.isSelected());
        isValid = !currentSelection.isEmpty() || 
                !preventTypeRadioButton.isSelected();
    }
    
    public Set<Derivation.Type> getCurrentSelection() {
        return currentSelection;
    }

    private boolean checkBoxSelected() {
        return (extensionCheckBox.isSelected() ||
                restrictionCheckBox.isSelected() ||
                listCheckBox.isSelected() ||
                unionCheckBox.isSelected() ||
                substitutionCheckBox.isSelected());
    }
    
    private void setCheckBoxesSelected(boolean flag) {
        extensionCheckBox.setSelected(flag);
        restrictionCheckBox.setSelected(flag);
        listCheckBox.setSelected(flag);
        unionCheckBox.setSelected(flag);
        substitutionCheckBox.setSelected(flag);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allowAllRadioButton;
    private javax.swing.JLabel dummyLabel1;
    private javax.swing.JLabel dummyLabel2;
    private javax.swing.JCheckBox extensionCheckBox;
    private javax.swing.JCheckBox listCheckBox;
    private javax.swing.JRadioButton preventAllRadioButton;
    private javax.swing.JRadioButton preventTypeRadioButton;
    private javax.swing.ButtonGroup radioButtonGroup;
    private javax.swing.JCheckBox restrictionCheckBox;
    private javax.swing.JCheckBox substitutionCheckBox;
    private javax.swing.JCheckBox unionCheckBox;
    private javax.swing.JLabel useDefaultLabel;
    private javax.swing.JRadioButton useDefaultRadioButton;
    // End of variables declaration//GEN-END:variables

}
