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

package org.netbeans.modules.web.jsf.palette.items;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.TypeElementFinder;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(empty, bundle.getString("LBL_Empty_Form")); // NOI18N
        empty.setMargin(new java.awt.Insets(0, 0, 0, 0));
        empty.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                emptyItemStateChanged(evt);
            }
        });

        populate.add(fromBean);
        org.openide.awt.Mnemonics.setLocalizedText(fromBean, bundle.getString("LBL_Form_From_Entity")); // NOI18N
        fromBean.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fromBean.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                emptyItemStateChanged(evt);
            }
        });

        jLabel1.setLabelFor(classTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_GetProperty_Bean")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, bundle.getString("LBL_Browse")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        viewType.add(detail);
        detail.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(detail, bundle.getString("LBL_View_Detail")); // NOI18N
        detail.setMargin(new java.awt.Insets(0, 0, 0, 0));

        viewType.add(edit);
        org.openide.awt.Mnemonics.setLocalizedText(edit, bundle.getString("LBL_View_Edit")); // NOI18N
        edit.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("LBL_From_Fields")); // NOI18N

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
                    .add(classTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE))
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

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSL_EntytyClass")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSD_EntytyClass")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSL_Browse")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSD_Browse")); // NOI18N
        detail.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "RB_ReadOnly")); // NOI18N
        detail.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSD_ReadOnly")); // NOI18N
        edit.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSN_Editable")); // NOI18N
        edit.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSD_Editable")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(errorField, bundle.getString("MSG_No_Managed_Beans")); // NOI18N

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
                .addContainerGap(445, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(errorField)
                .addContainerGap(58, Short.MAX_VALUE))
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

        empty.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSN_EmtryForm")); // NOI18N
        empty.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSD_Emtry_form")); // NOI18N
        fromBean.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSN_FormGenerated")); // NOI18N
        fromBean.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfFormCustomizer.class, "ACSD_FromGenerated")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JsfForm")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ClasspathInfo cpInfo = ClasspathInfo.create(targetFileObject);
        final ElementHandle<TypeElement> handle = TypeElementFinder.find(cpInfo, new TypeElementFinder.Customizer() {
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {                                            
                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }

            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                return true;
            }
        });
        if (handle != null) {
            classTextField.setText(handle.getQualifiedName());
        }
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
            if (javaSource == null) {
                return result[0];
            }

            javaSource.runUserActionTask(new Task<CompilationController>() {
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
