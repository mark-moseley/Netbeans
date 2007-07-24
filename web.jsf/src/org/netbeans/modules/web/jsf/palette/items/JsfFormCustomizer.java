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

package org.netbeans.modules.web.jsf.palette.items;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author  Pavel Buzek
 */
public final class JsfFormCustomizer extends javax.swing.JPanel implements DocumentListener {
    
    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;

    private final boolean hasModuleJsf;
    private final FileObject targetFileObject;
    JsfForm jsfTable;
    JTextComponent target;
            
    public JsfFormCustomizer(JsfForm jsfTable, JTextComponent target) {
        this.jsfTable = jsfTable;
        this.target = target;
        this.targetFileObject = JsfForm.getFO(target);
        initComponents();
        hasModuleJsf = JsfForm.hasModuleJsf(target);
        errorField.setForeground(UIManager.getColor("nb.errorForeground")); //NOI18N
        
        classTextField.getDocument().addDocumentListener(this);
    }
    
    public boolean showDialog() {
        
        dialogOK = false;
        
        String displayName = NbBundle.getMessage(JsfFormCustomizer.class, "NAME_jsp-JsfForm");
        
        descriptor = new DialogDescriptor
                (this, NbBundle.getMessage(JsfFormCustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName, true,
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent actionEvent) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
		     }
		 } 
                );
        checkStatus();
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void evaluateInput() {
        
        String entityClass = classTextField.getText();
        jsfTable.setBean(entityClass);
        
        jsfTable.setVariable("anInstanceOf" + entityClass);
        
        int formType = empty.isSelected() ? 0 : detail.isSelected() ? 1 : 2;
        jsfTable.setFormType(formType);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jFileChooser1 = new javax.swing.JFileChooser();
        populate = new javax.swing.ButtonGroup();
        viewType = new javax.swing.ButtonGroup();
        empty = new javax.swing.JRadioButton();
        fromBean = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        classTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        detail = new javax.swing.JRadioButton();
        edit = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        errorField = new javax.swing.JLabel();

        jFileChooser1.setCurrentDirectory(null);

        populate.add(empty);
        empty.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(empty, java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("LBL_Empty_Form"));
        empty.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        empty.setMargin(new java.awt.Insets(0, 0, 0, 0));
        empty.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                emptyItemStateChanged(evt);
            }
        });

        populate.add(fromBean);
        org.openide.awt.Mnemonics.setLocalizedText(fromBean, java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("LBL_Form_From_Entity"));
        fromBean.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fromBean.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fromBean.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                emptyItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("LBL_GetProperty_Bean"));

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("LBL_Browse"));
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        viewType.add(detail);
        detail.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(detail, java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("LBL_View_Detail"));
        detail.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        detail.setMargin(new java.awt.Insets(0, 0, 0, 0));

        viewType.add(edit);
        org.openide.awt.Mnemonics.setLocalizedText(edit, java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("LBL_View_Edit"));
        edit.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        edit.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("LBL_From_Fields"));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(edit)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(detail))
                    .add(classTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jButton1)
                    .add(classTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(edit)
                    .add(detail)))
        );

        org.openide.awt.Mnemonics.setLocalizedText(errorField, java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("MSG_No_Managed_Beans"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(empty)
                .add(289, 289, 289))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(fromBean)
                .addContainerGap(430, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(errorField)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(empty)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fromBean)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorField)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //TODO: RETOUCHE FQN search
//        FQNSearch.showFastOpen(classTextField);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void emptyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_emptyItemStateChanged
        checkStatus();
    }//GEN-LAST:event_emptyItemStateChanged

    private void checkStatus() {
        if (empty.isSelected()) {
            classTextField.setEnabled(false);
            jButton1.setEnabled(false);
            detail.setEnabled(false);
            edit.setEnabled(false);
        }
        if (fromBean.isSelected()) {
            classTextField.setEnabled(true);
            jButton1.setEnabled(true);
            detail.setEnabled(true);
            edit.setEnabled(true);
        }
        boolean validClassName = false;
        try {
            validClassName = empty.isSelected() || classExists(targetFileObject, classTextField.getText());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        descriptor.setValid(hasModuleJsf && validClassName);
        errorField.setText(hasModuleJsf ? 
                (validClassName ? "" : java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("MSG_InvalidClassName")) :
                java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle").getString("MSG_NoJSF"));
    }

    public void insertUpdate(DocumentEvent documentEvent) {
        checkStatus();
    }

    public void removeUpdate(DocumentEvent documentEvent) {
        checkStatus();
    }

    public void changedUpdate(DocumentEvent documentEvent) {
        checkStatus();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classTextField;
    private javax.swing.JRadioButton detail;
    private javax.swing.JRadioButton edit;
    private javax.swing.JRadioButton empty;
    private javax.swing.JLabel errorField;
    private javax.swing.JRadioButton fromBean;
    private javax.swing.JButton jButton1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.ButtonGroup populate;
    private javax.swing.ButtonGroup viewType;
    // End of variables declaration//GEN-END:variables
    
    protected static boolean classExists(FileObject referenceFO, final String className) throws IOException {
        final boolean[] result = new boolean[] { false };
        if (referenceFO != null) {
            JavaSource javaSource = JavaSource.forFileObject(referenceFO);
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = controller.getElements().getTypeElement(className);
                    result[0] = typeElement != null;
                }
            }, true);
        }
        return result[0];
    }
    
}
