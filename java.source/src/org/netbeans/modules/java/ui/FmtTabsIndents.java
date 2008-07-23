/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.ui;

import org.netbeans.api.java.source.CodeStyle.WrapStyle;
import static org.netbeans.modules.java.ui.FmtOptions.*;
import static org.netbeans.modules.java.ui.FmtOptions.CategorySupport.OPTION_ID;
import org.netbeans.modules.java.ui.FmtOptions.CategorySupport;
import org.netbeans.spi.options.OptionsPanelController;

/**
 *
 * @author  phrebejk
 */
public class FmtTabsIndents extends javax.swing.JPanel {
   
    /** Creates new form FmtTabsIndents */
    public FmtTabsIndents() {
        initComponents();
        
        expandTabCheckBox.putClientProperty(OPTION_ID, expandTabToSpaces);
        tabSizeField.putClientProperty(OPTION_ID, tabSize);
        indentSizeField.putClientProperty(OPTION_ID, new String [] { indentSize, spacesPerTab });
        continuationIndentSizeField.putClientProperty(OPTION_ID, continuationIndentSize);
        labelIndentField.putClientProperty(OPTION_ID, labelIndent);
        absoluteLabelIndentCheckBox.putClientProperty(OPTION_ID, absoluteLabelIndent);
        indentTopLevelClassMembersCheckBox.putClientProperty(OPTION_ID, indentTopLevelClassMembers);
        indentCasesFromSwitchCheckBox.putClientProperty(OPTION_ID, indentCasesFromSwitch);
        addLeadingStarInCommentCheckBox.putClientProperty(OPTION_ID, addLeadingStarInComment);
        rightMarginField.putClientProperty(OPTION_ID, rightMargin);
    }
    
    public static OptionsPanelController getController() {
        return new CategorySupport(new FmtTabsIndents(),
                org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "SAMPLE_TabsIndents"), // NOI18N
                new String[] { FmtOptions.rightMargin, "30" }, //NOI18N
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
                new String[] { FmtOptions.wrapWhileStatement, WrapStyle.WRAP_ALWAYS.name() },
                new String[] { FmtOptions.alignMultilineArrayInit, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineAssignment, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineBinaryOp, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineCallArgs, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineFor, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineImplements, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineMethodParams, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineParenthesized, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineTernaryOp, Boolean.FALSE.toString() },
                new String[] { FmtOptions.alignMultilineThrows, Boolean.FALSE.toString() }
                );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        expandTabCheckBox = new javax.swing.JCheckBox();
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
        addLeadingStarInCommentCheckBox = new javax.swing.JCheckBox();
        rightMarginLabel = new javax.swing.JLabel();
        rightMarginField = new javax.swing.JTextField();

        setName(org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_TabsAndIndents")); // NOI18N
        setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(expandTabCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_ExpandTabToSpaces")); // NOI18N
        expandTabCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        tabSizeLabel.setLabelFor(tabSizeField);
        org.openide.awt.Mnemonics.setLocalizedText(tabSizeLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_TabSize")); // NOI18N

        indentSizeLabel.setLabelFor(indentSizeField);
        org.openide.awt.Mnemonics.setLocalizedText(indentSizeLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_IndentSize")); // NOI18N

        continuationIndentSizeLabel.setLabelFor(continuationIndentSizeField);
        org.openide.awt.Mnemonics.setLocalizedText(continuationIndentSizeLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_ContinuationIndentSize")); // NOI18N

        labelIndentLabel.setLabelFor(labelIndentField);
        org.openide.awt.Mnemonics.setLocalizedText(labelIndentLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_LabelIndent")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(absoluteLabelIndentCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_AbsoluteLabelIndent")); // NOI18N
        absoluteLabelIndentCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(indentTopLevelClassMembersCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_IndentTopLevelClassMemberts")); // NOI18N
        indentTopLevelClassMembersCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(indentCasesFromSwitchCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_IndentCasesFromSwitch")); // NOI18N
        indentCasesFromSwitchCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(addLeadingStarInCommentCheckBox, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_AddLeadingStarInComment")); // NOI18N
        addLeadingStarInCommentCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        rightMarginLabel.setLabelFor(rightMarginField);
        org.openide.awt.Mnemonics.setLocalizedText(rightMarginLabel, org.openide.util.NbBundle.getMessage(FmtTabsIndents.class, "LBL_RightMargin")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addLeadingStarInCommentCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(rightMarginLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(rightMarginField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(indentCasesFromSwitchCheckBox)
                                .add(indentTopLevelClassMembersCheckBox)
                                .add(expandTabCheckBox)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(indentSizeLabel)
                                        .add(continuationIndentSizeLabel)
                                        .add(labelIndentLabel)
                                        .add(tabSizeLabel))
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, labelIndentField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, continuationIndentSizeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, indentSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, tabSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(absoluteLabelIndentCheckBox)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {continuationIndentSizeField, labelIndentField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(expandTabCheckBox)
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tabSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tabSizeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(indentSizeLabel)
                    .add(indentSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(continuationIndentSizeLabel)
                    .add(continuationIndentSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelIndentLabel)
                    .add(labelIndentField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(absoluteLabelIndentCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(indentTopLevelClassMembersCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(indentCasesFromSwitchCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addLeadingStarInCommentCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rightMarginLabel)
                    .add(rightMarginField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox absoluteLabelIndentCheckBox;
    private javax.swing.JCheckBox addLeadingStarInCommentCheckBox;
    private javax.swing.JTextField continuationIndentSizeField;
    private javax.swing.JLabel continuationIndentSizeLabel;
    private javax.swing.JCheckBox expandTabCheckBox;
    private javax.swing.JCheckBox indentCasesFromSwitchCheckBox;
    private javax.swing.JTextField indentSizeField;
    private javax.swing.JLabel indentSizeLabel;
    private javax.swing.JCheckBox indentTopLevelClassMembersCheckBox;
    private javax.swing.JTextField labelIndentField;
    private javax.swing.JLabel labelIndentLabel;
    private javax.swing.JTextField rightMarginField;
    private javax.swing.JLabel rightMarginLabel;
    private javax.swing.JTextField tabSizeField;
    private javax.swing.JLabel tabSizeLabel;
    // End of variables declaration//GEN-END:variables
    
}
