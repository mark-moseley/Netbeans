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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.saas.codegen.java;

import java.awt.Component;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.websvc.saas.codegen.java.Constants;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.java.support.Inflector;
import org.openide.util.NbBundle;

/**
 *
 * @author  nam
 * 
 * TODO: Need to check to make sure the UriTemplate value is unique after
 * the user modifies it.
 * 
 */
public class CustomCodeSetupPanel extends javax.swing.JPanel {

    private ParamTableModel tableModel;
    private List<ParameterInfo> inputParams;
    private boolean methodNameModified = false;

    /** Creates new form InputValuesJPanel */
    public CustomCodeSetupPanel(String uriTemplate, String resourceName, List<ParameterInfo> inputParams, 
			boolean showResourceInfo, boolean showParams) {
        initComponents();

        uriTemplateTF.setText(uriTemplate);
        updateMethodName();
        resourceNameTF.setText(resourceName);

        this.inputParams = inputParams;
        tableModel = new ParamTableModel();
        paramTable.setModel(tableModel);

        if (!showResourceInfo) {
            methodNameLabel.setVisible(false);
            methodNameTF.setVisible(false);
            resourceNameLabel.setVisible(false);
            resourceNameTF.setVisible(false);
            subresourceLabel.setVisible(false);
            subresourceLocatorLabel.setVisible(false);
            uriTemplateLabel.setVisible(false);
            uriTemplateTF.setVisible(false);
        }
        
        if (showParams && !inputParams.isEmpty()) {
            messageLabel.setVisible(false);     
        } else {
            paramLabel.setVisible(false);
            paramScrollPane.setVisible(false);
            messageLabel.setVisible(true);
            if(inputParams.isEmpty())
                messageLabel.setText(NbBundle.getMessage(JaxRsCodeSetupPanel.class, "MSG_EmptyParams"));
        }
    }

    public String getUriTemplate() {
        return uriTemplateTF.getText().trim();
    }

    public String getMethodName() {
        return methodNameTF.getText().trim();
    }

    public String getResourceName() {
        return resourceNameTF.getText().trim();
    }

    private class ParamTable extends JTable {

        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            String paramName = (String) tableModel.getValueAt(row, 0);
            Class type = (column == 2) ? (Class) tableModel.getValueAt(row, 1) : Boolean.class;

            if (Enum.class.isAssignableFrom(type)) {
                JComboBox combo = new JComboBox(type.getEnumConstants());
                return new DefaultCellEditor(combo);
            } else if (type == Boolean.class || type == Boolean.TYPE) {
                JCheckBox cb = new JCheckBox();
                cb.setHorizontalAlignment(JLabel.CENTER);
                cb.setBorderPainted(true);
                return new DefaultCellEditor(cb);
            } else if (paramName.toLowerCase().contains(Constants.PASSWORD)) {
                return new DefaultCellEditor(new JPasswordField());
            }

            return super.getCellEditor(row, column);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (column != 0) {
                return new ParamCellRenderer();
            }
            return super.getCellRenderer(row, column);
        }
    }

    private class ParamCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            Component ret = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String paramName = (String) tableModel.getValueAt(row, 0);

            if (value == null) {
                return new JLabel(NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_NotSet"));
            } else if (value instanceof Class) {
                return new JLabel(((Class) value).getName());
            } else if (value instanceof Boolean) {
                JCheckBox cb = new JCheckBox();
                cb.setHorizontalAlignment(JLabel.CENTER);
                cb.setBorderPainted(true);
                cb.setSelected((Boolean) value);
                return cb;
            } else if (paramName.contains(Constants.PASSWORD)) {
                return new JPasswordField((String) value);
            } 
            return ret;
        }
    }

    private class ParamTableModel extends AbstractTableModel {

        public ParamTableModel() {
        }
        String[] columnNames = new String[]{NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_Name"), NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_Type"), NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_DefaultValue"), NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_MapToQueryParam")};
        Class[] types = new Class[]{String.class, Class.class, Object.class, Boolean.class};
        boolean[] canEdit = new boolean[]{false, false, true, true};

        public String getColumnName(int index) {
            return columnNames[index];
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return inputParams.size();
        }

        public Object getValueAt(int row, int column) {
            ParameterInfo info = inputParams.get(row);

            switch (column) {
                case 0:
                    return info.getName();
                case 1:
                    return info.getType();
                case 2:
                    return info.getDefaultValue();
                case 3:
                    return info.getStyle() == ParamStyle.QUERY;
            }

            return null;
        }

        public void setValueAt(Object value, int row, int column) {
            ParameterInfo info = inputParams.get(row);

            if (column == 2) {
                info.setDefaultValue(value);
            } else if (column == 3) {
                if(((Boolean) value))
                    info.setStyle(ParamStyle.QUERY);
                else
                    info.setStyle(ParamStyle.UNKNOWN);
            }
        }

        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit[columnIndex];
        }
    }

    private void updateMethodName() {
        if (!methodNameModified) {
            methodNameTF.setText(computeMethodName());
        }
    }

    private String computeMethodName() {
        String uriTemplate = getUriTemplate();
        
        if (uriTemplate.endsWith("/")) {    //NOI18N
            uriTemplate = uriTemplate.substring(0, uriTemplate.length()-1);
        }
    
        return "get" + Inflector.getInstance().camelize(uriTemplate);

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        paramLabel = new javax.swing.JLabel();
        paramScrollPane = new javax.swing.JScrollPane();
        paramTable = new ParamTable();
        uriTemplateLabel = new javax.swing.JLabel();
        uriTemplateTF = new javax.swing.JTextField();
        methodNameLabel = new javax.swing.JLabel();
        methodNameTF = new javax.swing.JTextField();
        subresourceLocatorLabel = new javax.swing.JLabel();
        subresourceLabel = new javax.swing.JLabel();
        resourceNameLabel = new javax.swing.JLabel();
        resourceNameTF = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();

        paramLabel.setLabelFor(paramTable);
        paramLabel.setText(org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_Parameters")); // NOI18N

        paramTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        paramScrollPane.setViewportView(paramTable);

        uriTemplateLabel.setLabelFor(uriTemplateTF);
        org.openide.awt.Mnemonics.setLocalizedText(uriTemplateLabel, org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_UriTemplate")); // NOI18N

        uriTemplateTF.setText(org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "JaxRsCodeSetupPanel.uriTemplateTF.text")); // NOI18N
        uriTemplateTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                uriTemplateTFKeyReleased(evt);
            }
        });

        methodNameLabel.setLabelFor(methodNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(methodNameLabel, org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_MethodName")); // NOI18N

        methodNameTF.setText(org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "JaxRsCodeSetupPanel.methodNameTF.text")); // NOI18N
        methodNameTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                methodNameTFKeyReleased(evt);
            }
        });

        subresourceLocatorLabel.setText(org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_SubresourceLocator")); // NOI18N

        subresourceLabel.setText(org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_Subresource")); // NOI18N

        resourceNameLabel.setLabelFor(resourceNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(resourceNameLabel, org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "LBL_ResourceName")); // NOI18N

        resourceNameTF.setText(org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "JaxRsCodeSetupPanel.resourceNameTF.text")); // NOI18N
        resourceNameTF.setEnabled(false);

        messageLabel.setText(org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "MSG_AlreadyGenerated")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(subresourceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                    .add(subresourceLocatorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(methodNameLabel)
                            .add(uriTemplateLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(uriTemplateTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                            .add(methodNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(paramLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(resourceNameLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(resourceNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE))
                            .add(paramScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                            .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(subresourceLocatorLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(uriTemplateLabel)
                    .add(uriTemplateTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(methodNameLabel)
                    .add(methodNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(subresourceLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resourceNameLabel)
                    .add(resourceNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(paramLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(paramScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .addContainerGap())
        );

        paramLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomCodeSetupPanel.class, "MSG_SetConstantValues")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void uriTemplateTFKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_uriTemplateTFKeyReleased
        // TODO add your handling code here:
        updateMethodName();
    }//GEN-LAST:event_uriTemplateTFKeyReleased

    private void methodNameTFKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_methodNameTFKeyReleased
        methodNameModified = true;
    }//GEN-LAST:event_methodNameTFKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel methodNameLabel;
    private javax.swing.JTextField methodNameTF;
    private javax.swing.JLabel paramLabel;
    private javax.swing.JScrollPane paramScrollPane;
    private javax.swing.JTable paramTable;
    private javax.swing.JLabel resourceNameLabel;
    private javax.swing.JTextField resourceNameTF;
    private javax.swing.JLabel subresourceLabel;
    private javax.swing.JLabel subresourceLocatorLabel;
    private javax.swing.JLabel uriTemplateLabel;
    private javax.swing.JTextField uriTemplateTF;
    // End of variables declaration//GEN-END:variables
}