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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author  Pavel Buzek
 * @author  Petr Slechta
 */
public class JsfTableCustomizer extends javax.swing.JPanel implements DocumentListener {

    private static final ResourceBundle bundle = NbBundle.getBundle(JsfTableCustomizer.class);

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    private boolean dialogOK = false;

    private final boolean hasModuleJsf;
    JsfTable jsfTable;
    JTextComponent target;
    private final FileObject targetFileObject;
    
            
    public JsfTableCustomizer(JsfTable jsfTable, JTextComponent target) {
        this.jsfTable = jsfTable;
        this.target = target;
        this.targetFileObject = JsfForm.getFO(target);
        
        initComponents();
        hasModuleJsf = JsfForm.hasModuleJsf(target);
        classTextField.getDocument().addDocumentListener(this);
    }
    
    public boolean showDialog() {
        dialogOK = false;
        String displayName = bundle.getString("NAME_jsp-JsfTable"); // NOI18N
        descriptor = new DialogDescriptor
                (this, bundle.getString("LBL_Customizer_InsertPrefix") + " " + displayName, true,
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
                     }
                 }
        );
        statusLine = descriptor.createNotificationLineSupport();
        
        checkStatus();
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void evaluateInput() {
        String entityClass = classTextField.getText();
        jsfTable.setBean(entityClass);
        
        jsfTable.setVariable("arrayOrCollectionOf" + entityClass);
        int formType = empty.isSelected() ? 0 : 1;
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
        jLabel1 = new javax.swing.JLabel();
        classTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        empty = new javax.swing.JRadioButton();
        fromBean = new javax.swing.JRadioButton();

        jFileChooser1.setCurrentDirectory(null);

        jLabel1.setLabelFor(classTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_GetProperty_Bean")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, bundle.getString("LBL_Browse")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        populate.add(empty);
        empty.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(empty, bundle.getString("LBL_Empty_Table")); // NOI18N
        empty.setMargin(new java.awt.Insets(0, 0, 0, 0));
        empty.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                emptyItemStateChanged(evt);
            }
        });

        populate.add(fromBean);
        org.openide.awt.Mnemonics.setLocalizedText(fromBean, bundle.getString("LBL_Table_From_Entity")); // NOI18N
        fromBean.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fromBean.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                emptyItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(empty)
                    .add(fromBean)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(classTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(empty)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fromBean)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(classTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfTableCustomizer.class, "ACSN_EntytyClass")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfTableCustomizer.class, "ACSD_EntytyClass")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfTableCustomizer.class, "ACSL_Browse")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfTableCustomizer.class, "ACSD_Browse")); // NOI18N
        empty.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfTableCustomizer.class, "ACSN_EmptyTable")); // NOI18N
        empty.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfTableCustomizer.class, "ACSD_EmptyTable")); // NOI18N
        fromBean.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JsfTableCustomizer.class, "ACSN_GeneratedTable")); // NOI18N
        fromBean.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JsfTableCustomizer.class, "ACSD_GeneratedTable")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JsfTable")); // NOI18N
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
        } else {
            classTextField.setEnabled(true);
            jButton1.setEnabled(true);
        }
        boolean validClassName = false;
        try {
            validClassName = empty.isSelected() || JsfFormCustomizer.classExists(targetFileObject, classTextField.getText());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        descriptor.setValid(hasModuleJsf && validClassName);

        statusLine.clearMessages();
        if (!validClassName) {
            if (classTextField.getText().length() < 1)
                statusLine.setInformationMessage(bundle.getString("MSG_EmptyClassName"));  //NOI18N
            else
                statusLine.setErrorMessage(bundle.getString("MSG_InvalidClassName"));  //NOI18N
        }
        if (!hasModuleJsf) {
            statusLine.setErrorMessage(bundle.getString("MSG_NoJSF"));  //NOI18N
        }
    }

    public void insertUpdate(DocumentEvent event) {
        checkStatus();
    }

    public void removeUpdate(DocumentEvent event) {
        checkStatus();
    }

    public void changedUpdate(DocumentEvent event) {
        checkStatus();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classTextField;
    private javax.swing.JRadioButton empty;
    private javax.swing.JRadioButton fromBean;
    private javax.swing.JButton jButton1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.ButtonGroup populate;
    // End of variables declaration//GEN-END:variables
    
    public static class ManagedBeanRenderer extends JLabel implements ListCellRenderer {
        
        public ManagedBeanRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String text = "" + value;
            if (value instanceof ManagedBean) {
                ManagedBean bean = (ManagedBean) value;
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                text = bean.getManagedBeanName() + "(" + bean.getManagedBeanClass() + ")"; //NOI18N
            }
            setFont(list.getFont());
            setText(text);
            return this;
        }
    }
}
