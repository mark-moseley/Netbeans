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

package org.netbeans.modules.web.jsf.dialogs;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.util.NbBundle;

/**
 *
 * @author  radko
 */
public class AddManagedBeanDialog extends javax.swing.JPanel implements ValidatingPanel {
    private JSFConfigDataObject config;
    private Hashtable existingBeans = null;
    /** Creates new form AddManagedBeanDialog */
    public AddManagedBeanDialog(JSFConfigDataObject config) {
        initComponents();
        this.config = config;
    }

    public javax.swing.text.JTextComponent[] getDocumentChangeComponents() {
        return new javax.swing.text.JTextComponent[]{jTextFieldName, jTextFieldClass};
    }

    public javax.swing.AbstractButton[] getStateChangeComponents() {
        return new javax.swing.AbstractButton[]{  };
    }

    public String validatePanel() {
        if (getName().length()==0)
            return NbBundle.getMessage(AddManagedBeanDialog.class,"MSG_AddManagedBean_EmptyName");
        if (existingBeans == null){
            existingBeans = new Hashtable();
            ManagedBean bean;
            //Iterator iter = JSFConfigUtilities.getAllManagedBeans(config).iterator();
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config.getPrimaryFile(), false).getRootComponent();
            Collection<ManagedBean> beans = facesConfig.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                existingBeans.put(managedBean.getManagedBeanName(), "");
            }
        }
        if (existingBeans.get(getName()) != null)
            return NbBundle.getMessage(AddManagedBeanDialog.class,"MSG_AddManagedBean_BeanExist");
        if (getBeanClass().length()==0)
            return NbBundle.getMessage(AddManagedBeanDialog.class,"MSG_AddManagedBean_EmptyClass");
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelClass = new javax.swing.JLabel();
        jTextFieldClass = new javax.swing.JTextField();
        jButtonClass = new javax.swing.JButton();
        jLabelScope = new javax.swing.JLabel();
        jComboBoxScope = new javax.swing.JComboBox();
        jLabelDesc = new javax.swing.JLabel();
        jScrollPaneDesc = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();
        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLabelClass.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddManagedBeanDialog.class, "MNE_ManagedBeanClass").charAt(0));
        jLabelClass.setLabelFor(jTextFieldClass);
        jLabelClass.setText(org.openide.util.NbBundle.getMessage(AddManagedBeanDialog.class, "LBL_ManagedBeanClass")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(jLabelClass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jTextFieldClass, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/dialogs/Bundle"); // NOI18N
        jTextFieldClass.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_BeanClass")); // NOI18N
        jTextFieldClass.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BeanClass")); // NOI18N

        jButtonClass.setMnemonic(org.openide.util.NbBundle.getMessage(AddManagedBeanDialog.class, "MNE_Browse").charAt(0));
        jButtonClass.setText(org.openide.util.NbBundle.getMessage(AddManagedBeanDialog.class, "LBL_Browse")); // NOI18N
        jButtonClass.setEnabled(false);
        jButtonClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 11);
        add(jButtonClass, gridBagConstraints);
        jButtonClass.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Browse")); // NOI18N

        jLabelScope.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddManagedBeanDialog.class, "MNE_Scope").charAt(0));
        jLabelScope.setLabelFor(jComboBoxScope);
        jLabelScope.setText(org.openide.util.NbBundle.getMessage(AddManagedBeanDialog.class, "LBL_Scope")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(jLabelScope, gridBagConstraints);

        jComboBoxScope.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "request", "session", "application", "none" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboBoxScope, gridBagConstraints);
        jComboBoxScope.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ManagedBeanScope")); // NOI18N
        jComboBoxScope.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Scope")); // NOI18N

        jLabelDesc.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddManagedBeanDialog.class, "MNE_BeanDescription").charAt(0));
        jLabelDesc.setLabelFor(jTextAreaDesc);
        jLabelDesc.setText(org.openide.util.NbBundle.getMessage(AddManagedBeanDialog.class, "LBL_BeanDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jLabelDesc, gridBagConstraints);

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setRows(5);
        jScrollPaneDesc.setViewportView(jTextAreaDesc);
        jTextAreaDesc.getAccessibleContext().setAccessibleName(bundle.getString("ACN_ManagedBeanDescription")); // NOI18N
        jTextAreaDesc.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BeanDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jScrollPaneDesc, gridBagConstraints);

        jLabelName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/dialogs/Bundle").getString("MNE_ManagedBeanName").charAt(0));
        jLabelName.setLabelFor(jTextFieldName);
        jLabelName.setText(bundle.getString("LBL_Bean_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 0);
        add(jLabelName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 5, 0);
        add(jTextFieldName, gridBagConstraints);
        jTextFieldName.getAccessibleContext().setAccessibleName(bundle.getString("ACS_BeanName")); // NOI18N
        jTextFieldName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BeanName")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_AddManagedBeanDialog")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClassActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
//TODO: RETOUCHE FQN search
//                FQNSearch.showFastOpen(jTextFieldClass);
            }
        });
    }//GEN-LAST:event_jButtonClassActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClass;
    private javax.swing.JComboBox jComboBoxScope;
    private javax.swing.JLabel jLabelClass;
    private javax.swing.JLabel jLabelDesc;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelScope;
    private javax.swing.JScrollPane jScrollPaneDesc;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextFieldClass;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
    public String getBeanClass(){
        return jTextFieldClass.getText();
    }
    
    public String getScope(){
        return (String)jComboBoxScope.getSelectedItem();
    }
    
    public String getDescription(){
        return jTextAreaDesc.getText();
    }
    
    public String getName(){
        return jTextFieldName.getText();
    }
}
