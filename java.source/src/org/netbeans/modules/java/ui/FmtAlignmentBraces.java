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

import org.netbeans.api.java.source.CodeStyle.WrapStyle;
import static org.netbeans.modules.java.ui.FmtOptions.*;
import static org.netbeans.modules.java.ui.FmtOptions.CategorySupport.OPTION_ID;
import org.netbeans.modules.java.ui.FmtOptions.CategorySupport;
import org.openide.util.NbBundle;


/**
 *
 * @author  phrebejk
 */
public class FmtAlignmentBraces extends javax.swing.JPanel {
    
    /** Creates new form FmtAlignmentBraces */
    public FmtAlignmentBraces() {
        initComponents();
        
        classDeclCombo.putClientProperty(OPTION_ID, classDeclBracePlacement);
        methodDeclCombo.putClientProperty(OPTION_ID, methodDeclBracePlacement);
        otherCombo.putClientProperty(OPTION_ID, otherBracePlacement);
        specialElseIfCheckBox.putClientProperty(OPTION_ID, specialElseIf);
        ifBracesCombo.putClientProperty(OPTION_ID, redundantIfBraces);
        forBracesCombo.putClientProperty(OPTION_ID, redundantForBraces);
        whileBracesCombo.putClientProperty(OPTION_ID, redundantWhileBraces);
        doWhileBracesCombo.putClientProperty(OPTION_ID, redundantDoWhileBraces);
        nlElseCheckBox.putClientProperty(OPTION_ID, placeElseOnNewLine);
        nlWhileCheckBox.putClientProperty(OPTION_ID, placeWhileOnNewLine);
        nlCatchCheckBox.putClientProperty(OPTION_ID, placeCatchOnNewLine);
        nlFinallyCheckBox.putClientProperty(OPTION_ID, placeFinallyOnNewLine);
        nlModifiersCheckBox.putClientProperty(OPTION_ID, placeNewLineAfterModifiers);
        amMethodParamsCheckBox.putClientProperty(OPTION_ID, alignMultilineMethodParams);
        amCallArgsCheckBox.putClientProperty(OPTION_ID, alignMultilineCallArgs);
        amImplementsCheckBox.putClientProperty(OPTION_ID, alignMultilineImplements);
        amThrowsCheckBox.putClientProperty(OPTION_ID, alignMultilineThrows);
        amParenthesizedCheckBox.putClientProperty(OPTION_ID, alignMultilineParenthesized);
        amBinaryOpCheckBox.putClientProperty(OPTION_ID, alignMultilineBinaryOp);
        amTernaryOpCheckBox.putClientProperty(OPTION_ID, alignMultilineTernaryOp);
        amAssignCheckBox.putClientProperty(OPTION_ID, alignMultilineAssignment);
        amForCheckBox.putClientProperty(OPTION_ID, alignMultilineFor);
        amArrayInitCheckBox.putClientProperty(OPTION_ID, alignMultilineArrayInit);
    }
    
    public static FormatingOptionsPanel.Category getController() {
        return new CategorySupport(
                "LBL_AlignmentAndBraces", // NOI18N
                new FmtAlignmentBraces(), // NOI18N
                NbBundle.getMessage(FmtAlignmentBraces.class, "SAMPLE_AlignBraces"),
                new String[] { FmtOptions.wrapAnnotations, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapArrayInit, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapAssert, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapAssignOps, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapBinaryOps, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapChainedMethodCalls, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapDoWhileStatement, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapEnumConstants, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapExtendsImplementsKeyword, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapExtendsImplementsList, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapFor, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapForStatement, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapIfStatement, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapMethodCallArgs, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapMethodParams, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapTernaryOps, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapThrowsKeyword, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapThrowsList, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.wrapWhileStatement, WrapStyle.WRAP_ALWAYS.name() } ); 
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bracesPlacementPanel = new javax.swing.JPanel();
        classDeclLabel = new javax.swing.JLabel();
        classDeclCombo = new javax.swing.JComboBox();
        methodDeclLabel = new javax.swing.JLabel();
        methodDeclCombo = new javax.swing.JComboBox();
        otherLabel = new javax.swing.JLabel();
        otherCombo = new javax.swing.JComboBox();
        specialElseIfCheckBox = new javax.swing.JCheckBox();
        bracesGenerationPanel = new javax.swing.JPanel();
        ifBracesLabel = new javax.swing.JLabel();
        ifBracesCombo = new javax.swing.JComboBox();
        forBracesLabel = new javax.swing.JLabel();
        forBracesCombo = new javax.swing.JComboBox();
        whileBracesLabel = new javax.swing.JLabel();
        whileBracesCombo = new javax.swing.JComboBox();
        doWhileBracesLabel = new javax.swing.JLabel();
        doWhileBracesCombo = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        newLinesPanel = new javax.swing.JPanel();
        nlElseCheckBox = new javax.swing.JCheckBox();
        nlWhileCheckBox = new javax.swing.JCheckBox();
        nlCatchCheckBox = new javax.swing.JCheckBox();
        nlFinallyCheckBox = new javax.swing.JCheckBox();
        nlModifiersCheckBox = new javax.swing.JCheckBox();
        multilineAlignmentPanel = new javax.swing.JPanel();
        amMethodParamsCheckBox = new javax.swing.JCheckBox();
        amCallArgsCheckBox = new javax.swing.JCheckBox();
        amImplementsCheckBox = new javax.swing.JCheckBox();
        amThrowsCheckBox = new javax.swing.JCheckBox();
        amBinaryOpCheckBox = new javax.swing.JCheckBox();
        amTernaryOpCheckBox = new javax.swing.JCheckBox();
        amAssignCheckBox = new javax.swing.JCheckBox();
        amForCheckBox = new javax.swing.JCheckBox();
        amArrayInitCheckBox = new javax.swing.JCheckBox();
        amParenthesizedCheckBox = new javax.swing.JCheckBox();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        bracesPlacementPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_ab_placementBorder")), javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8))); // NOI18N
        bracesPlacementPanel.setOpaque(false);
        bracesPlacementPanel.setLayout(new java.awt.GridBagLayout());

        classDeclLabel.setLabelFor(classDeclCombo);
        org.openide.awt.Mnemonics.setLocalizedText(classDeclLabel, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_bp_ClassDecl")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        bracesPlacementPanel.add(classDeclLabel, gridBagConstraints);

        classDeclCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 0);
        bracesPlacementPanel.add(classDeclCombo, gridBagConstraints);

        methodDeclLabel.setLabelFor(methodDeclCombo);
        org.openide.awt.Mnemonics.setLocalizedText(methodDeclLabel, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_bp_MethodDecl")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        bracesPlacementPanel.add(methodDeclLabel, gridBagConstraints);

        methodDeclCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        methodDeclCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                methodDeclComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 0);
        bracesPlacementPanel.add(methodDeclCombo, gridBagConstraints);

        otherLabel.setLabelFor(otherCombo);
        org.openide.awt.Mnemonics.setLocalizedText(otherLabel, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_bp_Other")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        bracesPlacementPanel.add(otherLabel, gridBagConstraints);

        otherCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 0);
        bracesPlacementPanel.add(otherCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(specialElseIfCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_bp_SpecialElseIf")); // NOI18N
        specialElseIfCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        specialElseIfCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        specialElseIfCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        bracesPlacementPanel.add(specialElseIfCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(bracesPlacementPanel, gridBagConstraints);

        bracesGenerationPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_ab_generationBorder")), javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8))); // NOI18N
        bracesGenerationPanel.setOpaque(false);
        bracesGenerationPanel.setLayout(new java.awt.GridBagLayout());

        ifBracesLabel.setLabelFor(ifBracesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(ifBracesLabel, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_bg_If")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        bracesGenerationPanel.add(ifBracesLabel, gridBagConstraints);

        ifBracesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 0);
        bracesGenerationPanel.add(ifBracesCombo, gridBagConstraints);

        forBracesLabel.setLabelFor(forBracesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(forBracesLabel, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_bg_For")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        bracesGenerationPanel.add(forBracesLabel, gridBagConstraints);

        forBracesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 0);
        bracesGenerationPanel.add(forBracesCombo, gridBagConstraints);

        whileBracesLabel.setLabelFor(whileBracesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(whileBracesLabel, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_bg_While")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        bracesGenerationPanel.add(whileBracesLabel, gridBagConstraints);

        whileBracesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 0);
        bracesGenerationPanel.add(whileBracesCombo, gridBagConstraints);

        doWhileBracesLabel.setLabelFor(doWhileBracesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(doWhileBracesLabel, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_bg_DoWhile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        bracesGenerationPanel.add(doWhileBracesLabel, gridBagConstraints);

        doWhileBracesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        bracesGenerationPanel.add(doWhileBracesCombo, gridBagConstraints);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        bracesGenerationPanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(bracesGenerationPanel, gridBagConstraints);

        newLinesPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_ab_newLinesBorder")), javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8))); // NOI18N
        newLinesPanel.setOpaque(false);
        newLinesPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(nlElseCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_nl_Else")); // NOI18N
        nlElseCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        nlElseCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nlElseCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        newLinesPanel.add(nlElseCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(nlWhileCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_nl_While")); // NOI18N
        nlWhileCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        nlWhileCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nlWhileCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        newLinesPanel.add(nlWhileCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(nlCatchCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_nl_Catch")); // NOI18N
        nlCatchCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        nlCatchCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nlCatchCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        newLinesPanel.add(nlCatchCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(nlFinallyCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_nl_Finally")); // NOI18N
        nlFinallyCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        nlFinallyCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nlFinallyCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        newLinesPanel.add(nlFinallyCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(nlModifiersCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_nl_Modifiers")); // NOI18N
        nlModifiersCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        nlModifiersCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nlModifiersCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        newLinesPanel.add(nlModifiersCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(newLinesPanel, gridBagConstraints);

        multilineAlignmentPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_ab_multilineAlignmentBodrer")), javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8))); // NOI18N
        multilineAlignmentPanel.setOpaque(false);
        multilineAlignmentPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(amMethodParamsCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_MethodParams")); // NOI18N
        amMethodParamsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amMethodParamsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amMethodParamsCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        multilineAlignmentPanel.add(amMethodParamsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amCallArgsCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_CallArgs")); // NOI18N
        amCallArgsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amCallArgsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amCallArgsCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 6, 0);
        multilineAlignmentPanel.add(amCallArgsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amImplementsCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_an_Implements")); // NOI18N
        amImplementsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amImplementsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amImplementsCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        multilineAlignmentPanel.add(amImplementsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amThrowsCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_Throws")); // NOI18N
        amThrowsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amThrowsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amThrowsCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 6, 0);
        multilineAlignmentPanel.add(amThrowsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amBinaryOpCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_BinaryOp")); // NOI18N
        amBinaryOpCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amBinaryOpCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amBinaryOpCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        multilineAlignmentPanel.add(amBinaryOpCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amTernaryOpCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_TernaryOp")); // NOI18N
        amTernaryOpCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amTernaryOpCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amTernaryOpCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 6, 8);
        multilineAlignmentPanel.add(amTernaryOpCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amAssignCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_Assign")); // NOI18N
        amAssignCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amAssignCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amAssignCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        multilineAlignmentPanel.add(amAssignCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amForCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_For")); // NOI18N
        amForCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amForCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amForCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 6, 0);
        multilineAlignmentPanel.add(amForCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amArrayInitCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_ArrayInit")); // NOI18N
        amArrayInitCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amArrayInitCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amArrayInitCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        multilineAlignmentPanel.add(amArrayInitCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(amParenthesizedCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignmentBraces.class, "LBL_am_Paren")); // NOI18N
        amParenthesizedCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        amParenthesizedCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        amParenthesizedCheckBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        multilineAlignmentPanel.add(amParenthesizedCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(multilineAlignmentPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void methodDeclComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodDeclComboActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_methodDeclComboActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox amArrayInitCheckBox;
    private javax.swing.JCheckBox amAssignCheckBox;
    private javax.swing.JCheckBox amBinaryOpCheckBox;
    private javax.swing.JCheckBox amCallArgsCheckBox;
    private javax.swing.JCheckBox amForCheckBox;
    private javax.swing.JCheckBox amImplementsCheckBox;
    private javax.swing.JCheckBox amMethodParamsCheckBox;
    private javax.swing.JCheckBox amParenthesizedCheckBox;
    private javax.swing.JCheckBox amTernaryOpCheckBox;
    private javax.swing.JCheckBox amThrowsCheckBox;
    private javax.swing.JPanel bracesGenerationPanel;
    private javax.swing.JPanel bracesPlacementPanel;
    private javax.swing.JComboBox classDeclCombo;
    private javax.swing.JLabel classDeclLabel;
    private javax.swing.JComboBox doWhileBracesCombo;
    private javax.swing.JLabel doWhileBracesLabel;
    private javax.swing.JComboBox forBracesCombo;
    private javax.swing.JLabel forBracesLabel;
    private javax.swing.JComboBox ifBracesCombo;
    private javax.swing.JLabel ifBracesLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox methodDeclCombo;
    private javax.swing.JLabel methodDeclLabel;
    private javax.swing.JPanel multilineAlignmentPanel;
    private javax.swing.JPanel newLinesPanel;
    private javax.swing.JCheckBox nlCatchCheckBox;
    private javax.swing.JCheckBox nlElseCheckBox;
    private javax.swing.JCheckBox nlFinallyCheckBox;
    private javax.swing.JCheckBox nlModifiersCheckBox;
    private javax.swing.JCheckBox nlWhileCheckBox;
    private javax.swing.JComboBox otherCombo;
    private javax.swing.JLabel otherLabel;
    private javax.swing.JCheckBox specialElseIfCheckBox;
    private javax.swing.JComboBox whileBracesCombo;
    private javax.swing.JLabel whileBracesLabel;
    // End of variables declaration//GEN-END:variables
    
}
