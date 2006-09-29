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

package org.netbeans.modules.mobility.project.deployment;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.openide.DialogDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Adam Sotona
 */
public class NewInstanceDialog extends JPanel implements DocumentListener, ActionListener {
    
    private final MobilityDeploymentProperties props;
    private DialogDescriptor dd;
    private Collection<String> invalidNames = Collections.EMPTY_SET;
    
    /** Creates new form NewInstanceDialog */
    public NewInstanceDialog(MobilityDeploymentProperties props, DeploymentPlugin selected) {
        this.props = props;
        initComponents();
        final ListCellRenderer r = jComboBoxType.getRenderer();
        jComboBoxType.setRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return r.getListCellRendererComponent(list, value instanceof DeploymentPlugin ? ((DeploymentPlugin)value).getDeploymentMethodDisplayName() : value, index, isSelected, cellHasFocus);
            }
        });
        Vector<DeploymentPlugin> v = new Vector();
        for (DeploymentPlugin d : Lookup.getDefault().lookupAll(DeploymentPlugin.class)) {
            if (d.getGlobalPropertyDefaultValues().size() > 0) v.add(d);
        }
        jComboBoxType.setModel(new DefaultComboBoxModel(v));
        if (selected != null) jComboBoxType.setSelectedItem(selected);
        jComboBoxType.addActionListener(this);
        jTextFieldName.getDocument().addDocumentListener(this);
    }
    
    public void setDialogDescriptor(DialogDescriptor dd) {
        this.dd = dd;
        actionPerformed(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabelType = new javax.swing.JLabel();
        jComboBoxType = new javax.swing.JComboBox();
        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabelError = new javax.swing.JLabel();

        jLabelType.setLabelFor(jComboBoxType);
        jLabelType.setText(NbBundle.getMessage(NewInstanceDialog.class, "NewInstanceDialog.jLabelType.text")); // NOI18N

        jLabelName.setLabelFor(jTextFieldName);
        jLabelName.setText(NbBundle.getMessage(NewInstanceDialog.class, "NewInstanceDialog.jLabelName.text")); // NOI18N

        jLabelError.setForeground(new java.awt.Color(89, 79, 191));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabelError, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jLabelType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabelName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTextFieldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                            .add(jComboBoxType, 0, 256, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelType))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelName)
                    .add(jTextFieldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelError)
                .addContainerGap(25, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void actionPerformed(ActionEvent e) {
        DeploymentPlugin dp = getDeploymentPlugin();
        invalidNames = dp == null ? Collections.EMPTY_SET : props.getInstanceList(dp.getDeploymentMethodName());
        changedUpdate(null);
    }

    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        String name = getInstanceName();
        if (invalidNames.contains(name)) {
            jLabelError.setText(NbBundle.getMessage(NewInstanceDialog.class, "ERR_InstanceExists")); // NOI18N
            jLabelError.setVisible(true);
            if (dd != null) dd.setValid(false);
        } else if (!Utilities.isJavaIdentifier(name)) {
            jLabelError.setText(NbBundle.getMessage(NewInstanceDialog.class, "ERR_InvalidName")); // NOI18N
            jLabelError.setVisible(true);
            if (dd != null) dd.setValid(false);
        } else {
            jLabelError.setVisible(false);
            if (dd != null) dd.setValid(true);
        }
    }

    public DeploymentPlugin getDeploymentPlugin() {
        return (DeploymentPlugin)jComboBoxType.getSelectedItem();
    }
    
    public String getInstanceName() {
        return jTextFieldName.getText();
    }

    public void addNotify() {
        super.addNotify();
        jTextFieldName.requestFocusInWindow();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxType;
    private javax.swing.JLabel jLabelError;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelType;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
}
