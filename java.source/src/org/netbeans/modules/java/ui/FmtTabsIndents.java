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

package org.netbeans.modules.java.ui;

import static org.netbeans.modules.java.ui.FmtOptions.*;
import static org.netbeans.modules.java.ui.FmtOptions.CategorySupport.OPTION_ID;
import org.netbeans.modules.java.ui.FmtOptions.CategorySupport;

/**
 *
 * @author  phrebejk
 */
public class FmtTabsIndents extends javax.swing.JPanel {
    
    /** Creates new form FmtTabsIndents */
    public FmtTabsIndents() {
        initComponents();
        
        expandTabsToSpacesCheckBox.putClientProperty(OPTION_ID, expandTabToSpaces);
        tabSizeField.putClientProperty(OPTION_ID, tabSize);
        indentSizeField.putClientProperty(OPTION_ID, indentSize);
        continuationIndentSizeField.putClientProperty(OPTION_ID, continuationIndentSize);
        labelIndentField.putClientProperty(OPTION_ID, labelIndent);
        absoluteLabelIndentCheckBox.putClientProperty(OPTION_ID, absoluteLabelIndent);
        indentTopLevelClassMembersCheckBox.putClientProperty(OPTION_ID, indentTopLevelClassMembers);
        indentCasesFromSwitchCheckBox.putClientProperty(OPTION_ID, indentCasesFromSwitch);        
        rightMarginField.putClientProperty(OPTION_ID, rightMargin);
    }
    
    public static FormatingOptionsPanel.Category getController() {
        return new CategorySupport("LBL_TabsAndIndents", new FmtTabsIndents(), null); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField3 = new javax.swing.JTextField();
        jCheckBox3 = new javax.swing.JCheckBox();
        expandTabsToSpacesCheckBox = new javax.swing.JCheckBox();
        tabSizeLabel = new javax.swing.JLabel();
        tabSizeField = new javax.swing.JTextField();
        indentSizeLabel = new javax.swing.JLabel();
        indentSizeField = new javax.swing.JTextField();
        continuationIndentSizeLabel = new javax.swing.JLabel();
        continuationIndentSizeField = new javax.swing.JTextField();
        labelIndentLabel = new javax.swing.JLabel();
        labelIndentField = new javax.swing.JTextField();
        absoluteLabelIndentCheckBox = new javax.swing.JCheckBox();
        indentTopLevelClassMembersCheckBox = new javax.swing.JCheckBox();
        indentCasesFromSwitchCheckBox = new javax.swing.JCheckBox();
        rightMarginLabel = new javax.swing.JLabel();
        rightMarginField = new javax.swing.JTextField();

        jTextField3.setText("jTextField3");

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox3, "jCheckBox3");
        jCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(expandTabsToSpacesCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_TabToSpaces")); // NOI18N
        expandTabsToSpacesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        expandTabsToSpacesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(tabSizeLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_TabSize")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(indentSizeLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_IndentSize")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(continuationIndentSizeLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_ContinuationIndentSize")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelIndentLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_LabelIndent")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(absoluteLabelIndentCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_AbsoluteLabelIndent")); // NOI18N
        absoluteLabelIndentCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        absoluteLabelIndentCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(indentTopLevelClassMembersCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_IndentTopLevelClassMemberts")); // NOI18N
        indentTopLevelClassMembersCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        indentTopLevelClassMembersCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(indentCasesFromSwitchCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_IndentCasesFromSwitch")); // NOI18N
        indentCasesFromSwitchCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        indentCasesFromSwitchCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rightMarginLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_RightMargin")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(expandTabsToSpacesCheckBox)
                    .add(absoluteLabelIndentCheckBox)
                    .add(indentTopLevelClassMembersCheckBox)
                    .add(indentCasesFromSwitchCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelIndentLabel)
                            .add(continuationIndentSizeLabel)
                            .add(indentSizeLabel)
                            .add(tabSizeLabel)
                            .add(rightMarginLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(continuationIndentSizeField, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(labelIndentField)
                                .add(indentSizeField, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(tabSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(rightMarginField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(261, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {continuationIndentSizeField, indentSizeField, labelIndentField, rightMarginField, tabSizeField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(expandTabsToSpacesCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tabSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tabSizeLabel))
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(indentSizeLabel)
                    .add(indentSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(continuationIndentSizeLabel)
                    .add(labelIndentField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelIndentLabel)
                    .add(continuationIndentSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(absoluteLabelIndentCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(indentTopLevelClassMembersCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(indentCasesFromSwitchCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rightMarginLabel)
                    .add(rightMarginField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(101, 101, 101))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox absoluteLabelIndentCheckBox;
    private javax.swing.JTextField continuationIndentSizeField;
    private javax.swing.JLabel continuationIndentSizeLabel;
    private javax.swing.JCheckBox expandTabsToSpacesCheckBox;
    private javax.swing.JCheckBox indentCasesFromSwitchCheckBox;
    private javax.swing.JTextField indentSizeField;
    private javax.swing.JLabel indentSizeLabel;
    private javax.swing.JCheckBox indentTopLevelClassMembersCheckBox;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField labelIndentField;
    private javax.swing.JLabel labelIndentLabel;
    private javax.swing.JTextField rightMarginField;
    private javax.swing.JLabel rightMarginLabel;
    private javax.swing.JTextField tabSizeField;
    private javax.swing.JLabel tabSizeLabel;
    // End of variables declaration//GEN-END:variables
    
}
