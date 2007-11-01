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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.modules.web.struts.StrutsConfigDataObject;
import org.netbeans.modules.web.struts.StrutsConfigUtilities;
import org.netbeans.modules.web.struts.config.model.FormBean;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkuchtiak
 */
public class AddFormPropertyPanel extends javax.swing.JPanel implements ValidatingPanel {
    StrutsConfigDataObject config;
    /** Creates new form AddForwardDialog */
    public AddFormPropertyPanel(StrutsConfigDataObject config, String targetFormName) {
        this.config=config;
        initComponents();
        List beans = StrutsConfigUtilities.getAllFormBeansInModule(config);
        DefaultComboBoxModel model = (DefaultComboBoxModel)jComboBoxFormName.getModel();
        Iterator iter = beans.iterator();
        while (iter.hasNext()) {
            String name=((FormBean)iter.next()).getAttributeValue("name"); //NOI18N
            model.addElement(name);
        }
        if (targetFormName != null) {
            jComboBoxFormName.setSelectedItem(targetFormName);
        }
    }
    
    public AddFormPropertyPanel(StrutsConfigDataObject config) {
        this(config,null);
    }

    public String validatePanel() {
        if (getPropertyName()==null)
            return NbBundle.getMessage(AddFormPropertyPanel.class,"MSG_EmptyPropertyName");
        if (getFormName()==null)
            return NbBundle.getMessage(AddFormPropertyPanel.class,"MSG_EmptyFormName");
        if (getPropertyType()==null)
            return NbBundle.getMessage(AddFormPropertyPanel.class,"MSG_EmptyPropertyType");
        if (jRadioButtonArray.isSelected() && getArraySize()==null) {
                return NbBundle.getMessage(AddFormPropertyPanel.class,"MSG_IncorrectSize");
        }
        return null;
    }

    public AbstractButton[] getStateChangeComponents() {
        return new AbstractButton[] {jRadioButtonSingle};
    }

    public JTextComponent[] getDocumentChangeComponents() {
        return new JTextComponent[]{jTextFieldPropertyName, jTextFieldSize, (JTextComponent)jComboBoxPropertyType.getEditor().getEditorComponent()};
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
        jLabelPropertyName = new javax.swing.JLabel();
        jTextFieldPropertyName = new javax.swing.JTextField();
        jRadioButtonSingle = new javax.swing.JRadioButton();
        jTextFieldSize = new javax.swing.JTextField();
        jComboBoxFormName = new javax.swing.JComboBox();
        jLabelFormName = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxPropertyType = new javax.swing.JComboBox();
        jLabelInitValue = new javax.swing.JLabel();
        jLabelSize = new javax.swing.JLabel();
        jRadioButtonArray = new javax.swing.JRadioButton();
        jTextFieldInitValue = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());

        jLabelPropertyName.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_PropertyName_mnem").charAt(0));
        jLabelPropertyName.setLabelFor(jTextFieldPropertyName);
        jLabelPropertyName.setText(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_PropertyName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelPropertyName, gridBagConstraints);

        jTextFieldPropertyName.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jTextFieldPropertyName, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/struts/dialogs/Bundle"); // NOI18N
        jTextFieldPropertyName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jTextFieldPropertyName")); // NOI18N

        buttonGroup1.add(jRadioButtonSingle);
        jRadioButtonSingle.setMnemonic(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_SingleType_mnem").charAt(0));
        jRadioButtonSingle.setSelected(true);
        jRadioButtonSingle.setText(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_SingleType")); // NOI18N
        jRadioButtonSingle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonSingle.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButtonSingle.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonSingleItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jRadioButtonSingle, gridBagConstraints);
        jRadioButtonSingle.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jRadioButtonSingle")); // NOI18N

        jTextFieldSize.setColumns(5);
        jTextFieldSize.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jTextFieldSize, gridBagConstraints);
        jTextFieldSize.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jTextFieldSize")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jComboBoxFormName, gridBagConstraints);
        jComboBoxFormName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jComboBoxFormName")); // NOI18N

        jLabelFormName.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_FormName_mnem").charAt(0));
        jLabelFormName.setLabelFor(jComboBoxFormName);
        jLabelFormName.setText(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_FormName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelFormName, gridBagConstraints);

        jLabel2.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_PropertyType_mnem").charAt(0));
        jLabel2.setLabelFor(jComboBoxPropertyType);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_PropertyType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel2, gridBagConstraints);

        jComboBoxPropertyType.setEditable(true);
        jComboBoxPropertyType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "java.lang.String", "int", "byte", "long", "float", "double", "boolean", "char", "java.lang.Integer", "java.lang.Byte", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Boolean", "java.lang.Char" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jComboBoxPropertyType, gridBagConstraints);
        jComboBoxPropertyType.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jComboBoxPropertyType")); // NOI18N

        jLabelInitValue.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_InitValue_mnem").charAt(0));
        jLabelInitValue.setLabelFor(jTextFieldInitValue);
        jLabelInitValue.setText(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_InitValue")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 12, 0);
        add(jLabelInitValue, gridBagConstraints);

        jLabelSize.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_Size_mnem").charAt(0));
        jLabelSize.setLabelFor(jTextFieldSize);
        jLabelSize.setText(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_Size")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabelSize, gridBagConstraints);

        buttonGroup1.add(jRadioButtonArray);
        jRadioButtonArray.setMnemonic(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_ArrayType_mnem").charAt(0));
        jRadioButtonArray.setText(org.openide.util.NbBundle.getMessage(AddFormPropertyPanel.class, "LBL_ArrayType")); // NOI18N
        jRadioButtonArray.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonArray.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jRadioButtonArray, gridBagConstraints);
        jRadioButtonArray.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jRadioButtonArray")); // NOI18N

        jTextFieldInitValue.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jTextFieldInitValue, gridBagConstraints);
        jTextFieldInitValue.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jTextFieldInitValue")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        add(jPanel1, gridBagConstraints);

        jButton1.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/struts/dialogs/Bundle").getString("ACSM_BrowseClasses").charAt(0));
        jButton1.setText(bundle.getString("B_BROWSE")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jbBrowseClass")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_AddFormPropertyPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
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
            jComboBoxPropertyType.setSelectedItem(handle.getQualifiedName());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jRadioButtonSingleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonSingleItemStateChanged
// TODO add your handling code here:
        jTextFieldSize.setEditable(!jRadioButtonSingle.isSelected());
    }//GEN-LAST:event_jRadioButtonSingleItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBoxFormName;
    private javax.swing.JComboBox jComboBoxPropertyType;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelFormName;
    private javax.swing.JLabel jLabelInitValue;
    private javax.swing.JLabel jLabelPropertyName;
    private javax.swing.JLabel jLabelSize;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButtonArray;
    private javax.swing.JRadioButton jRadioButtonSingle;
    private javax.swing.JTextField jTextFieldInitValue;
    private javax.swing.JTextField jTextFieldPropertyName;
    private javax.swing.JTextField jTextFieldSize;
    // End of variables declaration//GEN-END:variables
    
    public String getFormName() {
        return (String)jComboBoxFormName.getSelectedItem();
    }
    
    public String getPropertyName() {
        String name = jTextFieldPropertyName.getText().trim();
        return name.length()==0?null:name;
    }
    
    public String getPropertyType() {
        javax.swing.text.Document doc = ((JTextComponent)jComboBoxPropertyType.getEditor().getEditorComponent()).getDocument();
        try {
            String propType = doc.getText(0,doc.getLength());
            return propType==null?null:(isArray()?propType+"[]":propType); //NOi18N
        } catch (javax.swing.text.BadLocationException ex) {
            return null;
        }
    }
    
    public boolean isArray() {
        return jRadioButtonArray.isSelected();
    }
    
    public String getInitValue() {
        return jTextFieldInitValue.getText().trim();
    }
     
    public String getArraySize() {
        String text = jTextFieldSize.getText().trim();
        try {
            Integer size = new Integer(text);
            return text;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
