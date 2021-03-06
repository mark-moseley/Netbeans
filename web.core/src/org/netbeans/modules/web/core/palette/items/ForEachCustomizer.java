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

package org.netbeans.modules.web.core.palette.items;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.JTextComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;





/**
 *
 * @author  Libor Kotouc
 */
public class ForEachCustomizer extends javax.swing.JPanel {

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;

    ForEach forEach;
    JTextComponent target;
            
    public ForEachCustomizer(ForEach forEach, JTextComponent target) {
        this.forEach = forEach;
        this.target = target;
        
        initComponents();

    }
    
    public boolean showDialog() {
        
        dialogOK = false;
        
        String displayName = "";
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-ForEach"); // NOI18N
        }
        catch (Exception e) {}
        
        descriptor = new DialogDescriptor
                (this, NbBundle.getMessage(ForEachCustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName, true,
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
		     }
		 } 
                );
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void evaluateInput() {
        
        
        String collection = jTextField1.getText();
        forEach.setCollection(collection);
        
        String variable = jTextField2.getText();
        forEach.setVariable(variable);
        
        forEach.setFixed(jCheckBox1.isSelected());
        
        String begin = jTextField3.getText();
        forEach.setBegin(begin);
        
        String end = jTextField4.getText();
        forEach.setEnd(end);
        
        String step = jTextField5.getText();
        forEach.setStep(step);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser1 = new javax.swing.JFileChooser();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();

        jFileChooser1.setCurrentDirectory(null);

        setLayout(new java.awt.GridBagLayout());

        jTextField1.setColumns(35);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jTextField1, gridBagConstraints);

        jLabel1.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "LBL_ForEach_Collection"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSN_ForEach_Collection"));
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSD_ForEach_Collection"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jTextField2, gridBagConstraints);

        jLabel5.setLabelFor(jTextField2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "LBL_ForEach_VariableHelp"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSN_ForEach_VariableHelp"));
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSD_ForEach_VariableHelp"));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "LBL_ForEach_Fixed"));
        jCheckBox1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jCheckBox1, gridBagConstraints);
        jCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSN_ForEach_Fixed"));
        jCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSD_ForEach_Fixed"));

        jLabel3.setLabelFor(jTextField3);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "LBL_ForEach_Begin"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 24, 0, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSN_ForEach_Begin"));
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSD_ForEach_Begin"));

        jLabel6.setLabelFor(jTextField4);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "LBL_ForEach_End"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 24, 0, 0);
        add(jLabel6, gridBagConstraints);
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSN_ForEach_End"));
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSD_ForEach_End"));

        jLabel7.setLabelFor(jTextField5);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "LBL_ForEach_Step"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 24, 12, 0);
        add(jLabel7, gridBagConstraints);
        jLabel7.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSN_ForEach_Step"));
        jLabel7.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ForEachCustomizer.class, "ACSD_ForEach_Step"));

        jTextField3.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jTextField3, gridBagConstraints);

        jTextField4.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jTextField4, gridBagConstraints);

        jTextField5.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 12, 12);
        add(jTextField5, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        
        if (jCheckBox1.isSelected()) {
            jTextField3.setEditable(true);
            jTextField4.setEditable(true);
            jTextField5.setEditable(true);
        }
        else {
            jTextField3.setEditable(false);
            jTextField4.setEditable(false);
            jTextField5.setEditable(false);
        }
    }//GEN-LAST:event_jCheckBox1ItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
    
}
