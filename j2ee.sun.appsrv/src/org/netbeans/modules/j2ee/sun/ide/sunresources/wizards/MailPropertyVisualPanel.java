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
/*
 * MailPropertyVisualPanel.java
 *
 * Created on December 17, 2002, 1:19 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.ListSelectionModel;
import org.openide.util.NbBundle;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper;

/**
 *
 * @author  Jennifer Chou
 */
public class MailPropertyVisualPanel extends javax.swing.JPanel implements WizardConstants, TableModelListener {
    
    private final MailPropertyPanel panel;
    private FieldGroup generalGroup;
    private FieldGroup propertiesGroup;
    private PropertiesTableModel tableModel;
    private ResourceConfigHelper helper;
    private javax.swing.table.TableColumn propNameColumn;
    private javax.swing.table.TableColumn propValueColumn;
        
    /** Creates new form MailPropertyVisualPanel */
    public MailPropertyVisualPanel(MailPropertyPanel panel) {
        this.panel = panel;
        this.helper = panel.getHelper();
        this.generalGroup = panel.getFieldGroup(__General);  
        this.propertiesGroup = panel.getFieldGroup(__Properties);  

        tableModel = new PropertiesTableModel(this.helper.getData());
        initComponents();
        setPropTableCellEditor();
        tableModel.addTableModelListener(this);
        propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propertyTable.setRowSelectionAllowed(true);
        
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(MailPropertyVisualPanel.class, "TITLE_MailPropertyPanel"));  //NOI18N
      
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        descriptionTextArea = new javax.swing.JTextArea();
        propertyInfo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        propertyTable = new PropertiesTable();
        buttonsPane = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(627, 305));
        setPreferredSize(new java.awt.Dimension(627, 305));
        setLayout(new java.awt.GridBagLayout());

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setText(NbBundle.getMessage(MailPropertyVisualPanel.class, "MailPropertyPanel_Description", this.helper.getData().getString(__JndiName)));
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setMinimumSize(new java.awt.Dimension(500, 17));
        descriptionTextArea.setOpaque(false);
        descriptionTextArea.setPreferredSize(new java.awt.Dimension(500, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 11);
        add(descriptionTextArea, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/sunresources/wizards/Bundle"); // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleName(bundle.getString("MailPropertyPanel_Description")); // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleDescription(bundle.getString("MailPropertyPanel_Description")); // NOI18N

        propertyInfo.setLabelFor(propertyTable);
        org.openide.awt.Mnemonics.setLocalizedText(propertyInfo, bundle.getString("LBL_properties")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        add(propertyInfo, gridBagConstraints);
        propertyInfo.getAccessibleContext().setAccessibleName(bundle.getString("LBL_properties")); // NOI18N
        propertyInfo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_propTableMail_A11yDesc")); // NOI18N

        jScrollPane1.setPreferredSize(new java.awt.Dimension(453, 17));

        propertyTable.setModel(tableModel);
        jScrollPane1.setViewportView(propertyTable);
        propertyTable.getAccessibleContext().setAccessibleName(bundle.getString("LBL_AddProperty")); // NOI18N
        propertyTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_propTableMail_A11yDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 10, 11);
        add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleName(bundle.getString("LBL_properties")); // NOI18N
        jScrollPane1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_propTableMail_A11yDesc")); // NOI18N

        buttonsPane.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        buttonsPane.add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddButtonA11yDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString("LBL_Remove")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        buttonsPane.add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_RemoveButtonA11yDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipady = 150;
        add(buttonsPane, gridBagConstraints);
        buttonsPane.getAccessibleContext().setAccessibleName(bundle.getString("LBL_properties")); // NOI18N
        buttonsPane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_propTableMail_A11yDesc")); // NOI18N

        getAccessibleContext().setAccessibleName(bundle.getString("TITLE_MailPropertyPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("MailPropertyPanel_Description")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add your handling code here:
        //Fix for bug#4958730 - value overwrites into next row
        propertyTable.editingStopped(new ChangeEvent (this));
        ResourceConfigData data = this.helper.getData();
        data.addProperty(new NameValuePair());
        tableModel.fireTableDataChanged();
    }//GEN-LAST:event_addButtonActionPerformed
    
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Add your handling code here:
        int selectedRow = propertyTable.getSelectedRow();
        if (selectedRow != -1) {
            //Fix for bug#4958730 - value overwrites into next row
            propertyTable.editingStopped(new ChangeEvent (this));
            this.helper.getData().removeProperty(selectedRow);
            tableModel.fireTableDataChanged();
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    
    public void tableChanged(TableModelEvent evt) {
        setPropTableCellEditor();
        this.panel.fireChange(evt.getSource());
    }

    public void setPropTableCellEditor() {
        javax.swing.JComboBox propNameComboBox = new javax.swing.JComboBox();
        String[] remainingProperties = FieldHelper.getRemainingFieldNames(propertiesGroup, this.helper.getData().getPropertyNames());

        for (int i = 0; i < remainingProperties.length; i++) 
            propNameComboBox.addItem(remainingProperties[i]);
        propNameComboBox.setEditable(true);
        this.propNameColumn = propertyTable.getColumnModel().getColumn(0);
        propNameColumn.setCellEditor(new javax.swing.DefaultCellEditor(propNameComboBox));
        this.propValueColumn = propertyTable.getColumnModel().getColumn(1);
        
        javax.swing.DefaultCellEditor editor = new javax.swing.DefaultCellEditor(new javax.swing.JTextField());
        editor.setClickCountToStart(1);
        propValueColumn.setCellEditor(editor);
    }    
   
    public void refreshFields() {
        ResourceConfigData data = this.helper.getData();
        ((PropertiesTableModel)propertyTable.getModel()).setData(this.helper.getData());
        descriptionTextArea.setText(NbBundle.getMessage(MailPropertyVisualPanel.class, "MailPropertyPanel_Description", data.getString(__JndiName))); //NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPane;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel propertyInfo;
    private javax.swing.JTable propertyTable;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
     public void setInitialFocus(){
         new setFocus(addButton);
     }  
}
