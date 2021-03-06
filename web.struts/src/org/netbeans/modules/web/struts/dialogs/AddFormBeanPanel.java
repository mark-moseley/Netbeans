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

package org.netbeans.modules.web.struts.dialogs;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.modules.web.struts.StrutsConfigDataObject;
import org.netbeans.modules.web.struts.config.model.FormBean;
import org.openide.util.NbBundle;

/**
 *
 * @author  Milan Kuchtiak
 */
public class AddFormBeanPanel extends javax.swing.JPanel implements ValidatingPanel {

    private StrutsConfigDataObject config;
    private Hashtable beanNames;
    /** Creates new form AddFormBeanPanel */
    public AddFormBeanPanel(StrutsConfigDataObject config) {
        initComponents();
        this.config = config;
        beanNames = null;
    }

    public String validatePanel() {
        //config.getStrutsConfig().getFormBeans().sizeFormBean()
        if (getFormName().length()==0)
            return NbBundle.getMessage(AddFormBeanPanel.class,"MSG_EmptyFormName");
        if (beanNames == null){
            System.out.println("vytvarim cashe of jmen");
            beanNames = new Hashtable();
            try {
                FormBean[] beans = config.getStrutsConfig().getFormBeans().getFormBean();
                for (int i = 0; i < beans.length; i++){
                    beanNames.put(beans[i].getAttributeValue("name"), "");
                }
            } catch (IOException ex) {
                // don't cashe
            }
        }
        if (beanNames.get(getFormName()) != null)
            return NbBundle.getMessage(AddFormBeanPanel.class,"MSG_BeanNameDefined");
        if (jRadioButton1.isSelected() && TFBeanClass.getText().trim().length()==0)
            return NbBundle.getMessage(AddFormBeanPanel.class,"MSG_EmptyFormBeanClass");
        return null;
    }

    public javax.swing.AbstractButton[] getStateChangeComponents() {
        return new javax.swing.AbstractButton[]{ jRadioButton1 };
    }

    public javax.swing.text.JTextComponent[] getDocumentChangeComponents() {
        return new javax.swing.text.JTextComponent[]{TFBeanClass, TFFormName};
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabelFormName = new javax.swing.JLabel();
        CBDynamic = new javax.swing.JComboBox();
        TFBeanClass = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        TFFormName = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());

        jLabelFormName.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddFormBeanPanel.class, "LBL_FormName_mnem").charAt(0));
        jLabelFormName.setLabelFor(TFFormName);
        jLabelFormName.setText(org.openide.util.NbBundle.getMessage(AddFormBeanPanel.class, "LBL_FormName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelFormName, gridBagConstraints);

        CBDynamic.setEditable(true);
        CBDynamic.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "org.apache.struts.action.DynaActionForm", "org.apache.struts.validator.DynaValidatorForm", "org.apache.struts.validator.DynaValidatorActionForm" }));
        CBDynamic.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(CBDynamic, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/struts/dialogs/Bundle"); // NOI18N
        CBDynamic.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CVBDynamic")); // NOI18N

        TFBeanClass.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(TFBeanClass, gridBagConstraints);
        TFBeanClass.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_TFBeanClass")); // NOI18N
        TFBeanClass.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TFBeanClass")); // NOI18N

        jButtonBrowse.setMnemonic(org.openide.util.NbBundle.getMessage(AddFormBeanPanel.class, "LBL_Browse_mnem").charAt(0));
        jButtonBrowse.setText(org.openide.util.NbBundle.getMessage(AddFormBeanPanel.class, "LBL_BrowseButton")); // NOI18N
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });
        jButtonBrowse.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jButtonBrowseComponentHidden(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jButtonBrowse, gridBagConstraints);
        jButtonBrowse.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jButtonBrowseClass")); // NOI18N

        TFFormName.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(TFFormName, gridBagConstraints);
        TFFormName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TFFormName")); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setMnemonic(org.openide.util.NbBundle.getMessage(AddFormBeanPanel.class, "LBL_FormBeanClass_mnem").charAt(0));
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(org.openide.util.NbBundle.getMessage(AddFormBeanPanel.class, "LBL_FormBeanClass")); // NOI18N
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton1ItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jRadioButton1, gridBagConstraints);
        jRadioButton1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jRadioButton1")); // NOI18N

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setMnemonic(org.openide.util.NbBundle.getMessage(AddFormBeanPanel.class, "LBL_DYNAMIC_mnem").charAt(0));
        jRadioButton2.setText(org.openide.util.NbBundle.getMessage(AddFormBeanPanel.class, "LBL_DYNAMIC")); // NOI18N
        jRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jRadioButton2, gridBagConstraints);
        jRadioButton2.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jRadioButton2")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_AddFormBeanPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonBrowseComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jButtonBrowseComponentHidden
    }//GEN-LAST:event_jButtonBrowseComponentHidden

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        ClasspathInfo cpInfo = ClasspathInfo.create(config.getPrimaryFile());
        final ElementHandle<TypeElement> handle = TypeElementFinder.find(cpInfo, new TypeElementFinder.Customizer() {
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {                                            
                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }

            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                return true;
            }
        });
        if (handle != null) {
            TFBeanClass.setText(handle.getQualifiedName());
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    private void jRadioButton1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton1ItemStateChanged
// TODO add your handling code here:
        boolean selected = jRadioButton1.isSelected();
        TFBeanClass.setEditable(selected);
        CBDynamic.setEnabled(!selected);
    }//GEN-LAST:event_jRadioButton1ItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox CBDynamic;
    private javax.swing.JTextField TFBeanClass;
    private javax.swing.JTextField TFFormName;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JLabel jLabelFormName;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    // End of variables declaration//GEN-END:variables
    
    public String getFormBeanClass() {
        return jRadioButton1.isSelected()?TFBeanClass.getText().trim():(String)CBDynamic.getSelectedItem();
    }

    public String getFormName() {
        return (String)TFFormName.getText().trim();
    }
    
}
