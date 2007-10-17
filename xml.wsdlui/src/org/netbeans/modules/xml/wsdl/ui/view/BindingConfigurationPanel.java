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

/*
 * BindingAndServiceConfigurationPanel.java
 *
 * Created on August 25, 2006, 2:51 PM
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.ComboBoxModel;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;

import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;


/**
 *
 * @author  radval
 */
public class BindingConfigurationPanel extends javax.swing.JPanel {
    
    public static final String PROP_BINDING_TYPE = "PROP_BINDING_TYPE";
    public static final String PROP_BINDING_SUBTYPE = "PROP_BINDING_SUBTYPE";
            
    private ExtensibilityElementTemplateFactory factory;
    private Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
    private LocalizedTemplateGroup defaultSelection; //Select SOAP as default
    
    /** Creates new form BindingAndServiceConfigurationPanel */
    public BindingConfigurationPanel() {
        factory = new ExtensibilityElementTemplateFactory();
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        protocols = new Vector<LocalizedTemplateGroup>();
        
        SortedSet<LocalizedTemplateGroup> set = new TreeSet<LocalizedTemplateGroup>();
        for (TemplateGroup group : groups) {
            LocalizedTemplateGroup ltg = factory.getLocalizedTemplateGroup(group);
            if (ltg.getNamespace().equals(SOAPQName.SOAP_NS_URI)) {
                defaultSelection = ltg;
            }
            set.add(ltg);
        }
        
        protocols.addAll(set);
        
        initComponents();
        initGUI();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        bindingNameTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        bindingTypeComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        serviceNameTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        servicePortTextField = new javax.swing.JTextField();

        jLabel1.setLabelFor(bindingNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BindingConfigurationPanel.class, "BindingConfigurationPanel.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(bindingTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BindingConfigurationPanel.class, "BindingConfigurationPanel.jLabel2.text")); // NOI18N

        DefaultComboBoxModel model = new DefaultComboBoxModel(protocols);
        model.setSelectedItem(defaultSelection);
        bindingTypeComboBox.setModel(model);
        bindingTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bindingTypeComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setLabelFor(jPanel1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(BindingConfigurationPanel.class, "BindingConfigurationPanel.jLabel3.text")); // NOI18N

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));

        jLabel4.setLabelFor(serviceNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(BindingConfigurationPanel.class, "BindingConfigurationPanel.jLabel4.text")); // NOI18N

        jLabel5.setLabelFor(servicePortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(BindingConfigurationPanel.class, "BindingConfigurationPanel.jLabel5.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bindingTypeComboBox, 0, 288, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bindingNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .add(serviceNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .add(servicePortTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bindingNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bindingTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel3)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serviceNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(servicePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .add(108, 108, 108))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void bindingTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bindingTypeComboBoxActionPerformed
        setBindingSubType(getBindingType());
        this.firePropertyChange(PROP_BINDING_TYPE, null, getBindingType());
    }//GEN-LAST:event_bindingTypeComboBoxActionPerformed
    
    public String getBindingName() {
        return this.bindingNameTextField.getText();
    }
    
    public void setBindingName(String bindingName) {
        this.bindingNameTextField.setText(bindingName);
    }
    
    public LocalizedTemplateGroup getBindingType() {
        return (LocalizedTemplateGroup) bindingTypeComboBox.getSelectedItem();
    }
    
    public void setBindingType(String bindingSubType) {
        this.bindingTypeComboBox.setSelectedItem(bindingSubType);
    }
    
    public LocalizedTemplate getBindingSubType() {
        return subTypePanel.getBindingSubType();
    }
    
    private void setBindingSubType(LocalizedTemplateGroup bindingType) {
        subTypePanel.reset(bindingType);
    }
    
    public String getServiceName() {
        return serviceNameTextField.getText();
    }
    
    public void setServiceName(String serviceName) {
        this.serviceNameTextField.setText(serviceName);
    }
    
    public String getServicePortName() {
        return servicePortTextField.getText();
    }
    
    public void setServicePortName(String servicePortName) {
        this.servicePortTextField.setText(servicePortName);
    }
    
    public JTextField getBindingNameTextField() {
        return this.bindingNameTextField;
    }
    
    public JTextField getServiceNameTextField() {
        return this.serviceNameTextField;
    }
    
    public JTextField getServicePortTextField() {
        return this.servicePortTextField;
    }
    
    
    private void initGUI() {
        if (protocols.size() > 0) {
            subTypePanel = new BindingSubTypePanel(defaultSelection,  new BindingSubTypeActionListener());
            jPanel1.add(subTypePanel);
        }
    }
    
    class BindingSubTypeActionListener implements ActionListener {
    	
    	public void actionPerformed(ActionEvent e) {
    		firePropertyChange(PROP_BINDING_SUBTYPE, null, getBindingSubType());
    	}
    	
    }
    
    private BindingSubTypePanel subTypePanel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bindingNameTextField;
    private javax.swing.JComboBox bindingTypeComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField serviceNameTextField;
    private javax.swing.JTextField servicePortTextField;
    // End of variables declaration//GEN-END:variables
    
}
