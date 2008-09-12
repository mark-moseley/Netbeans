/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.clearcase.options;

import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.clearcase.ClearcaseAnnotator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Maros Sandor
 */
class ClearcaseOptionsPanel extends javax.swing.JPanel {

    /** Creates new form ClearcaseOptionsPanel */
    public ClearcaseOptionsPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        taExecutable = new javax.swing.JTextField();
        bBrowse = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        rbDisabled = new javax.swing.JRadioButton();
        rbPrompt = new javax.swing.JRadioButton();
        rbHijack = new javax.swing.JRadioButton();
        rbUnreserved = new javax.swing.JRadioButton();
        rbReserved = new javax.swing.JRadioButton();
        cbFallback = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        taLabelFormat = new javax.swing.JTextField();
        bAddVariable = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        cbCheckinViewPrivate = new javax.swing.JCheckBox();
        cbHijackAfterUnreserved = new javax.swing.JCheckBox();
        cbHijackAfterReserved = new javax.swing.JCheckBox();

        jLabel1.setLabelFor(taExecutable);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.jLabel1.text")); // NOI18N

        taExecutable.setText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.taExecutable.text")); // NOI18N
        taExecutable.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.taExecutable.toolTipText")); // NOI18N
        taExecutable.setMinimumSize(new java.awt.Dimension(0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(bBrowse, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.bBrowse.text")); // NOI18N
        bBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.bBrowse.toolTipText")); // NOI18N
        bBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.jLabel2.text")); // NOI18N

        buttonGroup1.add(rbDisabled);
        org.openide.awt.Mnemonics.setLocalizedText(rbDisabled, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbDisabled.text")); // NOI18N
        rbDisabled.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbDisabled.toolTipText")); // NOI18N
        rbDisabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbDisabledActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbPrompt);
        org.openide.awt.Mnemonics.setLocalizedText(rbPrompt, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbPrompt.text")); // NOI18N
        rbPrompt.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbPrompt.toolTipText")); // NOI18N
        rbPrompt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbPromptActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbHijack);
        org.openide.awt.Mnemonics.setLocalizedText(rbHijack, NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbHijack.text")); // NOI18N
        rbHijack.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbHijack.toolTipText")); // NOI18N
        rbHijack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbHijackActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbUnreserved);
        org.openide.awt.Mnemonics.setLocalizedText(rbUnreserved, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbUnreserved.text")); // NOI18N
        rbUnreserved.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbUnreserved.toolTipText")); // NOI18N
        rbUnreserved.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbUnreservedActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbReserved);
        org.openide.awt.Mnemonics.setLocalizedText(rbReserved, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbReserved.text")); // NOI18N
        rbReserved.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.rbReserved.toolTipText")); // NOI18N
        rbReserved.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbReservedActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbFallback, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.cbFallback.text")); // NOI18N
        cbFallback.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.cbFallback.toolTipText")); // NOI18N
        cbFallback.setEnabled(false);
        cbFallback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFallbackActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.jLabel3.text")); // NOI18N

        jLabel4.setLabelFor(taLabelFormat);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.jLabel4.text")); // NOI18N

        taLabelFormat.setText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.taLabelFormat.text")); // NOI18N
        taLabelFormat.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.taLabelFormat.toolTipText")); // NOI18N
        taLabelFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                taLabelFormatActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bAddVariable, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.bAddVariable.text")); // NOI18N
        bAddVariable.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.bAddVariable.toolTipText")); // NOI18N
        bAddVariable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddVariableActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbCheckinViewPrivate, org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.cbCheckinViewPrivate.text")); // NOI18N
        cbCheckinViewPrivate.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.cbCheckinViewPrivate.toolTipText")); // NOI18N
        cbCheckinViewPrivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCheckinViewPrivateActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbHijackAfterUnreserved, NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.cbHijackAfterUnreserved.text")); // NOI18N
        cbHijackAfterUnreserved.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.cbHijackAfterUnreserved.toolTipText")); // NOI18N
        cbHijackAfterUnreserved.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(cbHijackAfterReserved, NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.cbHijackAfterReserved.text")); // NOI18N
        cbHijackAfterReserved.setToolTipText(org.openide.util.NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearcaseOptionsPanel.cbHijackAfterReserved.toolTipText")); // NOI18N
        cbHijackAfterReserved.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(cbCheckinViewPrivate))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rbHijack)
                            .add(rbUnreserved)
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(cbHijackAfterUnreserved))
                            .add(rbReserved)
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(cbHijackAfterReserved)
                                    .add(cbFallback)))))
                    .add(layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(taLabelFormat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bAddVariable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rbPrompt)
                            .add(rbDisabled)))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))))
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(taExecutable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bBrowse))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(bBrowse)
                    .add(taExecutable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(13, 13, 13)
                        .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbDisabled)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbPrompt)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbHijack)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbUnreserved)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbHijackAfterUnreserved)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbReserved)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbFallback)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbHijackAfterReserved)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel3))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(bAddVariable)
                    .add(taLabelFormat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel5)
                        .add(2, 2, 2))
                    .add(layout.createSequentialGroup()
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)))
                .add(cbCheckinViewPrivate)
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseActionPerformed
        String execPath = taExecutable.getText();
        File oldFile = FileUtil.normalizeFile(new File(execPath));
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(ClearcaseOptionsPanel.class, "ACSD_ClearToolBrowse"), oldFile);   // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearToolBrowse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showDialog(this, NbBundle.getMessage(ClearcaseOptionsPanel.class, "ClearToolBrowse_OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            taExecutable.setText(f.getAbsolutePath());
        }
    }//GEN-LAST:event_bBrowseActionPerformed
    
    private void rbDisabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbDisabledActionPerformed
        setCheckBoxes();
    }//GEN-LAST:event_rbDisabledActionPerformed
    
    private void rbReservedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbReservedActionPerformed
        setCheckBoxes();
    }//GEN-LAST:event_rbReservedActionPerformed
    
    private void cbFallbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFallbackActionPerformed
    // TODO add your handling code here:
    }//GEN-LAST:event_cbFallbackActionPerformed
    
    private void bAddVariableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddVariableActionPerformed
        LabelsPanel labelsPanel = new LabelsPanel();
        List<LabelVariable> variables = new ArrayList<LabelVariable>(ClearcaseAnnotator.LABELS.length);
        for (int i = 0; i < ClearcaseAnnotator.LABELS.length; i++) {   
            LabelVariable variable = new LabelVariable(
                    ClearcaseAnnotator.LABELS[i], 
                    "{" + ClearcaseAnnotator.LABELS[i] + "} - " + NbBundle.getMessage(ClearcaseOptionsPanel.class, "Variable." + ClearcaseAnnotator.LABELS[i])
            );
            variables.add(variable);   
        }       
        labelsPanel.labelsList.setListData(variables.toArray(new LabelVariable[variables.size()]));                
                
        String title = NbBundle.getMessage(ClearcaseOptionsPanel.class, "Variables.title");
        String acsd = NbBundle.getMessage(ClearcaseOptionsPanel.class, "Variables.acsd");

        DialogDescriptor dialogDescriptor = new DialogDescriptor(labelsPanel, title);
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(true);
        
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(acsd);
        
        labelsPanel.labelsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    dialog.setVisible(false);
                }
            }        
        });                 
        
        dialog.setVisible(true);
        
        if(DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue())) {
            
            Object[] selection = labelsPanel.labelsList.getSelectedValues();
            
            String variable = "";
            for (int i = 0; i < selection.length; i++) {
                variable += "{" + ((LabelVariable)selection[i]).getVariable() + "}";
            }

            String annotation = taLabelFormat.getText();

            int pos = taLabelFormat.getCaretPosition();
            if(pos < 0) pos = annotation.length();

            StringBuffer sb = new StringBuffer(annotation.length() + variable.length());
            sb.append(annotation.substring(0, pos));
            sb.append(variable);
            if(pos < annotation.length()) {
                sb.append(annotation.substring(pos, annotation.length()));
            }
            taLabelFormat.setText(sb.toString());
            taLabelFormat.requestFocus();
            taLabelFormat.setCaretPosition(pos + variable.length());            
            
        }        
    }//GEN-LAST:event_bAddVariableActionPerformed
    
    private void taLabelFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taLabelFormatActionPerformed
    // TODO add your handling code here:
    }//GEN-LAST:event_taLabelFormatActionPerformed
    
    private void cbCheckinViewPrivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCheckinViewPrivateActionPerformed
    // TODO add your handling code here:
    }//GEN-LAST:event_cbCheckinViewPrivateActionPerformed

private void rbPromptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbPromptActionPerformed
    setCheckBoxes();
}//GEN-LAST:event_rbPromptActionPerformed

private void rbUnreservedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbUnreservedActionPerformed
    setCheckBoxes();
}//GEN-LAST:event_rbUnreservedActionPerformed

private void rbHijackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbHijackActionPerformed
    setCheckBoxes();
}//GEN-LAST:event_rbHijackActionPerformed

    private void setCheckBoxes() {
        cbHijackAfterReserved.setEnabled(rbReserved.isSelected());
        cbFallback.setEnabled(rbReserved.isSelected());
        cbHijackAfterUnreserved.setEnabled(rbUnreserved.isSelected());
    }

private class LabelVariable {
        private String description;
        private String variable;
         
        public LabelVariable(String variable, String description) {
            this.description = description;
            this.variable = variable;
        }
         
        @Override
        public String toString() {
            return description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getVariable() {
            return variable;
        }
    }    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton bAddVariable;
    javax.swing.JButton bBrowse;
    javax.swing.ButtonGroup buttonGroup1;
    javax.swing.JCheckBox cbCheckinViewPrivate;
    javax.swing.JCheckBox cbFallback;
    javax.swing.JCheckBox cbHijackAfterReserved;
    javax.swing.JCheckBox cbHijackAfterUnreserved;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel5;
    javax.swing.JSeparator jSeparator1;
    javax.swing.JSeparator jSeparator2;
    javax.swing.JSeparator jSeparator3;
    javax.swing.JRadioButton rbDisabled;
    javax.swing.JRadioButton rbHijack;
    javax.swing.JRadioButton rbPrompt;
    javax.swing.JRadioButton rbReserved;
    javax.swing.JRadioButton rbUnreserved;
    javax.swing.JTextField taExecutable;
    javax.swing.JTextField taLabelFormat;
    // End of variables declaration//GEN-END:variables

}
