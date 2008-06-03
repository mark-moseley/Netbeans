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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.*;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.Vector;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  Martin Grebac
 */
public class TargetsPanel extends javax.swing.JPanel {
    
    private WSDLComponent comp;

    private Binding binding;
    private Vector<Vector> targetsModel;
    private MessagePartsModel targetsTableDataModel;
    private Vector<String> columnNames = new Vector<String>();

    private AddHeaderPanel addHeaderPanel;
    
    boolean inSync = false;
    
    /**
     * Creates new form TargetsPanel
     */
    public TargetsPanel(WSDLComponent c) {
        super();
        this.comp = c;
        
        WSDLComponent b = c;
        while (!(b instanceof Binding) && (b != null)) {
            b = b.getParent();
        }
        if ((b != null) && (b instanceof Binding)) {
            binding = (Binding) b;
        }
        
        initComponents();

        columnNames.add(NbBundle.getMessage(TargetsPanel.class, "LBL_Targets_MessagePart"));    //NOI18N
        columnNames.add(NbBundle.getMessage(TargetsPanel.class, "LBL_Targets_Sign"));           //NOI18N
        columnNames.add(NbBundle.getMessage(TargetsPanel.class, "LBL_Targets_Encrypt"));        //NOI18N
        columnNames.add(NbBundle.getMessage(TargetsPanel.class, "LBL_Targets_Require"));        //NOI18N
        
        sync();
    }
    
    private void sync() {
        inSync = true;
        
        targetsModel = SecurityPolicyModelHelper.getTargets(comp);
        targetsTableDataModel = new MessagePartsModel(getTargetsModel(), columnNames);
        jTable1.setModel(targetsTableDataModel);
        jTable1.doLayout();
        jTable1.setDefaultEditor(MessageElement.class, new XPathTableCellEditor());
        jTable1.getColumnModel().getColumn(TargetElement.DATA).setCellEditor(new XPathTableCellEditor());
        
        enableDisable();
        
        inSync = false;
    }
    
    private AddHeaderPanel getAddHeaderPanel() {
        return new AddHeaderPanel(binding);
    }
    
    private void saveTargetsModel() {
        if (!inSync) {
            SecurityPolicyModelHelper.getInstance(
                    PolicyModelHelper.getConfigVersion(comp)).setTargets(comp, getTargetsModel());
            jTable1.setModel(new MessagePartsModel(getTargetsModel(), columnNames));
        }
    }

    private void enableDisable() {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addHeaderButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addPartButton = new javax.swing.JButton();
        addBodyButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        addAttachButton = new javax.swing.JButton();

        addHeaderButton.setText(org.openide.util.NbBundle.getMessage(TargetsPanel.class, "LBL_AddHeader")); // NOI18N
        addHeaderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addHeaderButtonActionPerformed(evt);
            }
        });

        removeButton.setText(org.openide.util.NbBundle.getMessage(TargetsPanel.class, "LBL_Remove")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        addPartButton.setText(org.openide.util.NbBundle.getMessage(TargetsPanel.class, "LBL_AddXPath")); // NOI18N
        addPartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPartButtonActionPerformed(evt);
            }
        });

        addBodyButton.setText(org.openide.util.NbBundle.getMessage(TargetsPanel.class, "LBL_AddBody")); // NOI18N
        addBodyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBodyButtonActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setShowVerticalLines(false);
        jTable1.setVerifyInputWhenFocusTarget(false);
        jScrollPane2.setViewportView(jTable1);

        addAttachButton.setText(org.openide.util.NbBundle.getMessage(TargetsPanel.class, "LBL_AddAttachment")); // NOI18N
        addAttachButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAttachButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(addBodyButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(addPartButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(addHeaderButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(addAttachButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addAttachButton, addBodyButton, addHeaderButton, addPartButton, removeButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addBodyButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addHeaderButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addPartButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addAttachButton)
                        .add(7, 7, 7)
                        .add(removeButton))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings("unchecked")
    private void addBodyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBodyButtonActionPerformed
        MessageBody body = new MessageBody();
        if (!(SecurityPolicyModelHelper.targetExists(getTargetsModel(), body) != null)) {
            Vector row = new Vector();
            row.add(TargetElement.DATA, body);
            row.add(TargetElement.SIGN, Boolean.TRUE);
            row.add(TargetElement.ENCRYPT, Boolean.FALSE);
            row.add(TargetElement.REQUIRE, Boolean.FALSE);
            getTargetsModel().add(row);
            saveTargetsModel();
        }
    }//GEN-LAST:event_addBodyButtonActionPerformed

    @SuppressWarnings("unchecked")
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int[] rows = jTable1.getSelectedRows();
        ArrayList rowsToRemove = new ArrayList();
        for (int i=0; i<rows.length; i++) {            
            rowsToRemove.add(getTargetsModel().get(rows[i]));
        }
        for (Object o : rowsToRemove) {
            getTargetsModel().remove(o);
        }
        saveTargetsModel();
    }//GEN-LAST:event_removeButtonActionPerformed
    
    @SuppressWarnings("unchecked")
    private void addPartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPartButtonActionPerformed
        
        Object button = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(TargetsPanel.class, "WARNING_DISABLE_OPTIMSECURITY")));
        if (NotifyDescriptor.OK_OPTION.equals(button)) {
            MessageElement e = new MessageElement(NbBundle.getMessage(TargetsPanel.class, "TXT_EditHere")); //NOI18N
            Vector row = new Vector();
            row.add(TargetElement.DATA, e);
            row.add(TargetElement.SIGN, Boolean.FALSE);
            row.add(TargetElement.ENCRYPT, Boolean.FALSE);
            row.add(TargetElement.REQUIRE, Boolean.TRUE);
            getTargetsModel().add(row);
            jTable1.updateUI();
        }
    }//GEN-LAST:event_addPartButtonActionPerformed

    @SuppressWarnings("unchecked")
    private void addHeaderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addHeaderButtonActionPerformed
        addHeaderPanel = getAddHeaderPanel();
        DialogDescriptor dd = new DialogDescriptor(
                addHeaderPanel, 
                NbBundle.getMessage(TargetsPanel.class, "LBL_SignEncryptChooser_AddHeaderTitle"),  //NOI18N
                true, 
                DialogDescriptor.OK_CANCEL_OPTION, 
                DialogDescriptor.CANCEL_OPTION, 
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(TargetsPanel.class),
                null);
        if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.OK_OPTION)) {
            if (addHeaderPanel != null) {
                if (addHeaderPanel.isAllHeaders()) {
                    for (String s : MessageHeader.ADDRESSING_HEADERS) {
                        MessageHeader h = new MessageHeader(s);
                        if (!(SecurityPolicyModelHelper.targetExists(getTargetsModel(), h) != null)) {
                            Vector row = new Vector();
                            row.add(TargetElement.DATA, h);
                            row.add(TargetElement.SIGN, Boolean.TRUE);
                            row.add(TargetElement.ENCRYPT, Boolean.FALSE);
                            row.add(TargetElement.REQUIRE, Boolean.FALSE);
                            getTargetsModel().add(row);
                        }
                    }
//                    boolean rm = true;
//                    if (binding != null) {
//                        rm = RMModelHelper.isRMEnabled(binding);
//                    }
//                    if (rm) {
                        for (String s : MessageHeader.RM_HEADERS) {
                            MessageHeader h = new MessageHeader(s);
                            if (!(SecurityPolicyModelHelper.targetExists(getTargetsModel(), h) != null)) {
                                Vector row = new Vector();
                                row.add(TargetElement.DATA, h);
                                row.add(TargetElement.SIGN, Boolean.TRUE);
                                row.add(TargetElement.ENCRYPT, Boolean.FALSE);
                                row.add(TargetElement.REQUIRE, Boolean.FALSE);
                                getTargetsModel().add(row);
                            }
//                        }
                    }
                } else {
                    String header = addHeaderPanel.getHeader();
                    MessageHeader h = new MessageHeader(header);
                    if (!(SecurityPolicyModelHelper.targetExists(getTargetsModel(), h) != null)) {
                        Vector row = new Vector();
                        row.add(TargetElement.DATA, h);
                        row.add(TargetElement.SIGN, Boolean.TRUE);
                        row.add(TargetElement.ENCRYPT, Boolean.FALSE);
                        row.add(TargetElement.REQUIRE, Boolean.FALSE);
                        getTargetsModel().add(row);
                    }
                }
                saveTargetsModel();
            }
        }
    }//GEN-LAST:event_addHeaderButtonActionPerformed

private void addAttachButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAttachButtonActionPerformed

        MessageAttachments attch = new MessageAttachments();
        if (!(SecurityPolicyModelHelper.targetExists(getTargetsModel(), attch) != null)) {
            Vector row = new Vector();
            row.add(TargetElement.DATA, attch);
            row.add(TargetElement.SIGN, Boolean.TRUE);
            row.add(TargetElement.ENCRYPT, Boolean.FALSE);
            row.add(TargetElement.REQUIRE, Boolean.FALSE);
            getTargetsModel().add(row);
            saveTargetsModel();
        }

}//GEN-LAST:event_addAttachButtonActionPerformed

    public static class XPathTableCellEditor extends AbstractCellEditor implements TableCellEditor {
        // This is the component that will handle the editing of the cell value
        JTextField component = new JTextField();
    
        // This method is called when a cell value is edited by the user.
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int rowIndex, int vColIndex) {
    
            if (isSelected) {
                    // cell (and perhaps other cells) are selected

                // Configure the component with the specified value
                component.setText(((MessageElement)value).getElement());

                // Return the configured component
                return component;
               
            }
            return null;
        }
    
        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        public Object getCellEditorValue() {
//            component.setVisible(false);
            return new MessageElement(component.getText());
        }

    }

    private class MessagePartsModel extends DefaultTableModel {

        public MessagePartsModel (Vector<Vector> data, Vector columnNames) {
            super (data, columnNames);//NOI18N
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            Vector rowVector = getTargetsModel().get(row);
            if (column == 0) { // heading - allow editing only of xpath values
                return (rowVector.get(TargetElement.DATA) instanceof MessageElement);
            }
            if ((column == 1) || (column ==2)) {
                return true;
            }
            if (column == 3) {
                return (rowVector.get(TargetElement.DATA) instanceof MessageElement);
            }
            return false;
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return TargetElement.class;
            }
            return Boolean.class;
        }
    }

    public Vector<Vector> getTargetsModel() {
        return targetsModel;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAttachButton;
    private javax.swing.JButton addBodyButton;
    private javax.swing.JButton addHeaderButton;
    private javax.swing.JButton addPartButton;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
}

