/*
 * ServiceConfiguretionPanel.java
 *
 * Created on January 22, 2008, 3:10 PM
 */

package org.netbeans.modules.websvc.axis2.actions;

import java.awt.event.ItemEvent;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.config.model.GenerateWsdl;
import org.netbeans.modules.websvc.axis2.config.model.Service;



/**
 *
 * @author  mkuchtiak
 */
public class ServiceConfigurationPanel extends javax.swing.JPanel implements java.awt.event.ItemListener {
    
    private String defaultNs, defaultSchemaNs;
    
    /** Creates new form ServiceConfiguretionPanel */
    public ServiceConfigurationPanel(Service service) {
        initComponents();
        jTextField1.setText(service.getNameAttr());
        String serviceClass = service.getServiceClass();
        jTextField2.setText(serviceClass);
        defaultNs = AxisUtils.getNamespaceFromClassName(serviceClass);
        jTextField3.setText(defaultNs);
        defaultSchemaNs = defaultNs+"xsd"; //NOI18N
        jTextField4.setText(defaultSchemaNs);
        if (service.getGenerateWsdl() != null) {
            cbDefault1.setEnabled(true);
            cbDefault2.setEnabled(true);
            GenerateWsdl genWsdl = service.getGenerateWsdl();
            cbGenerateWsdl.setSelected(true);
            String ns = genWsdl.getTargetNamespaceAttr();
            if (!defaultNs.equals(ns)) {
                cbDefault1.setSelected(false);
                jTextField3.setEditable(true);
                if (ns != null) jTextField3.setText(ns);
            }
            jTextField3.setText(ns == null?defaultNs:ns);
            String schemaNs = genWsdl.getSchemaNamespaceAttr();
            if (!defaultSchemaNs.equals(schemaNs)) {
                cbDefault2.setSelected(false);
                jTextField4.setEditable(true);
                if (schemaNs != null) jTextField4.setText(schemaNs);
            }
        }
        cbGenerateWsdl.addItemListener(this);
        cbDefault1.addItemListener(this);
        cbDefault2.addItemListener(this);
        
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        wsNameLabel = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        wsClassLabel = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        cbGenerateWsdl = new javax.swing.JCheckBox();
        nsLabel = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        cbDefault1 = new javax.swing.JCheckBox();
        schemaNsLabel = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        cbDefault2 = new javax.swing.JCheckBox();
        generateWsdlDesc = new javax.swing.JLabel();

        wsNameLabel.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(wsNameLabel, org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.wsNameLabel.text")); // NOI18N

        wsClassLabel.setLabelFor(jTextField2);
        org.openide.awt.Mnemonics.setLocalizedText(wsClassLabel, org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.wsClassLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.browseButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbGenerateWsdl, org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.cbGenerateWsdl.text")); // NOI18N
        cbGenerateWsdl.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbGenerateWsdlStateChanged(evt);
            }
        });

        nsLabel.setLabelFor(jTextField3);
        org.openide.awt.Mnemonics.setLocalizedText(nsLabel, org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.nsLabel.text")); // NOI18N

        jTextField3.setEditable(false);

        cbDefault1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbDefault1, org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.cbDefault1.text")); // NOI18N
        cbDefault1.setEnabled(false);

        schemaNsLabel.setLabelFor(jTextField4);
        org.openide.awt.Mnemonics.setLocalizedText(schemaNsLabel, org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.schemaNsLabel.text")); // NOI18N

        jTextField4.setEditable(false);

        cbDefault2.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbDefault2, org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.cbDefault2.text")); // NOI18N
        cbDefault2.setEnabled(false);

        generateWsdlDesc.setText(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.generateWsdlDesc.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, browseButton)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(wsClassLabel)
                                    .add(wsNameLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE))))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbGenerateWsdl)
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(generateWsdlDesc)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(nsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(schemaNsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 344, Short.MAX_VALUE)
                                                .add(cbDefault2))
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, cbDefault1)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))))))
                        .add(16, 16, 16))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wsNameLabel)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wsClassLabel)
                    .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton)
                .add(18, 18, 18)
                .add(cbGenerateWsdl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(generateWsdlDesc)
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(nsLabel))
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(cbDefault1)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(schemaNsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbDefault2)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jTextField1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.jTextField1.AccessibleContext.accessibleDescription")); // NOI18N
        jTextField2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.jTextField2.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        cbGenerateWsdl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.cbGenerateWsdl.AccessibleContext.accessibleDescription")); // NOI18N
        jTextField3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.jTextField3.AccessibleContext.accessibleDescription")); // NOI18N
        cbDefault1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.cbDefault1.AccessibleContext.accessibleDescription")); // NOI18N
        jTextField4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.jTextField4.AccessibleContext.accessibleDescription")); // NOI18N
        cbDefault2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.cbDefault2.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceConfigurationPanel.class, "ServiceConfigurationPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbGenerateWsdlStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbGenerateWsdlStateChanged
        // TODO add your handling code here:
        if (cbGenerateWsdl.isSelected()) {
            
        }
    }//GEN-LAST:event_cbGenerateWsdlStateChanged

    public void itemStateChanged(ItemEvent e) {
        Object o = e.getSource();
        if (o == cbGenerateWsdl) {
            if (cbGenerateWsdl.isSelected()) {
                cbDefault1.setEnabled(true);
                cbDefault2.setEnabled(true);
                if (!cbDefault1.isSelected()) jTextField3.setEditable(true);
                if (!cbDefault2.isSelected()) jTextField4.setEditable(true);
            } else {
                cbDefault1.setEnabled(false);
                cbDefault2.setEnabled(false);
                jTextField3.setEditable(false);
                jTextField4.setEditable(false);            
            }
        } else if (o == cbDefault1) {
            if (cbDefault1.isSelected()) {
                jTextField3.setEditable(false);
            } else {
                jTextField3.setEditable(true);
            }
        } else if (o == cbDefault2) {
            if (cbDefault2.isSelected()) {
                jTextField4.setEditable(false);
            } else {
                jTextField4.setEditable(true);
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox cbDefault1;
    private javax.swing.JCheckBox cbDefault2;
    private javax.swing.JCheckBox cbGenerateWsdl;
    private javax.swing.JLabel generateWsdlDesc;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JLabel nsLabel;
    private javax.swing.JLabel schemaNsLabel;
    private javax.swing.JLabel wsClassLabel;
    private javax.swing.JLabel wsNameLabel;
    // End of variables declaration//GEN-END:variables
    
    
    public String getTargetNamespace() {
        String ns = jTextField3.getText().trim();
        if (cbDefault1.isSelected() || ns.length() == 0) return defaultNs;
        else return ns;
    }
    
    public String getSchemaNamespace() {
        String ns = jTextField4.getText().trim();
        if (cbDefault2.isSelected() || ns.length() == 0) return defaultSchemaNs;
        else return ns;
    }
    
    public String getServiceName() {
        return jTextField1.getText().trim();
    }
    
    public String getServiceClass() {
        return jTextField2.getText().trim();
    }
    
    public boolean generateWsdl() {
        return cbGenerateWsdl.isSelected();
    }
}
