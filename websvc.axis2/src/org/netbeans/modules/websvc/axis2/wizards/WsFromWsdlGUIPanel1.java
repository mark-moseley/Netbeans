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

package org.netbeans.modules.websvc.axis2.wizards;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.StringTokenizer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.axis2.WSDLUtils;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;

/**
 *
 * @author  mkuchtiak
 */
public class WsFromWsdlGUIPanel1 extends javax.swing.JPanel {
    WsFromWsdlPanel1 wizardPanel;
    Collection<Service> services;
    /** Creates new form WsFromWsdlGUIPanel1 */
    
    /** Creates new form WsFromJavaGUIPanel1 */
    public WsFromWsdlGUIPanel1(WsFromWsdlPanel1 wizardPanel) {
        this.wizardPanel = wizardPanel;
        initComponents();
        setName("Code Generator Options");
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(WizardProperties.DATA_BINDING));
        ItemListener cbListener = new CBListener();
        jComboBox1.addItemListener(cbListener);
        jComboBox2.addItemListener(cbListener);
        jComboBox3.addItemListener(cbListener);
        jTextArea2.setText("-s"); //NOI18N
        tfPackageName.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    changeOption("-p", doc.getText(0, doc.getLength()));
                } catch (BadLocationException ex){}
            }

            public void removeUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    changeOption("-p", doc.getText(0, doc.getLength()));
                } catch (BadLocationException ex){}
            }

            public void changedUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    changeOption("-p", doc.getText(0, doc.getLength()));
                } catch (BadLocationException ex){}
            }
            
        });
        jCheckBox1.addItemListener(cbListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        tfPackageName = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        moreOptionsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        optionsLabel = new javax.swing.JLabel();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(WsFromWsdlGUIPanel1.class, "WsFromWsdlGUIPanel1.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(WsFromWsdlGUIPanel1.class, "WsFromWsdlGUIPanel1.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(WsFromWsdlGUIPanel1.class, "WsFromWsdlGUIPanel1.jLabel3.text")); // NOI18N

        jCheckBox1.setSelected(true);
        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(WsFromWsdlGUIPanel1.class, "WsFromWsdlGUIPanel1.jCheckBox1.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(WsFromWsdlGUIPanel1.class, "WsFromWsdlGUIPanel1.jLabel4.text")); // NOI18N

        jTextArea2.setColumns(10);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        moreOptionsLabel.setText(org.openide.util.NbBundle.getMessage(WsFromWsdlGUIPanel1.class, "WsFromWsdlGUIPanel1.moreOptionsLabel.text")); // NOI18N

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextArea1.setColumns(5);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        optionsLabel.setText(org.openide.util.NbBundle.getMessage(WsFromWsdlGUIPanel1.class, "WsFromWsdlGUIPanel1.optionsLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                    .add(jCheckBox1)
                    .add(optionsLabel)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                    .add(moreOptionsLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2)
                            .add(jLabel4)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jComboBox3, 0, 309, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jComboBox1, 0, 309, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jComboBox2, 0, 309, Short.MAX_VALUE)
                            .add(tfPackageName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(tfPackageName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jComboBox3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(optionsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(moreOptionsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JLabel moreOptionsLabel;
    private javax.swing.JLabel optionsLabel;
    private javax.swing.JTextField tfPackageName;
    // End of variables declaration//GEN-END:variables
    boolean isFinishable() {
        return true;
    }
    
    boolean dataIsValid() {
        return true;
    }
    
    void setServices(Collection<Service> services) {
        this.services = services;
        jComboBox1.removeAllItems();
        for (Service service:services) {
            jComboBox1.addItem(service.getName());
        }
    }
    
    void setPorts(Collection<Port> ports) {
        jComboBox2.removeAllItems();
        for (Port port:ports) {
            jComboBox2.addItem(port.getName());
        }
    }
    
    void setPackageName(String packageName) {
        tfPackageName.setText(packageName);
    }

    String getServiceName() {
        return (String)jComboBox1.getSelectedItem();
    }
    
    String getPortName() {
        return (String)jComboBox2.getSelectedItem();
    }

    String getPackageName() {
        return tfPackageName.getText().trim();
    }
    
    String getDatabindingName() {
        String databindingName = (String)jComboBox3.getSelectedItem();
        if (WizardProperties.BINDING_ADB.equals(databindingName)) return "adb"; //NOI18N
        else if (WizardProperties.BINDING_XML_BEANS.equals(databindingName)) return "xmlbeans"; //NOI18N
        else if (WizardProperties.BINDING_JIBX.equals(databindingName)) return "jibx"; //NOI18N
        return "adb"; //NOI18N
    }
    
    boolean isSEI() {
        return jCheckBox1.isSelected();
    }
    
    void setW2JOptions(String options) {
        jTextArea1.setText(options);
    }

    void setW2JMoreOptions(String options) {
        jTextArea2.setText(options);
    }
    
    String getW2JMoreOptions() {
        String text = jTextArea2.getText();
        StringBuffer buf = new StringBuffer();
        StringTokenizer tokens = new StringTokenizer(text);
        boolean first=true;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            buf.append(first ? token : " "+token);
            if (first) first = false;
        }
        return buf.toString();
    }
    
    private class CBListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            Object source = e.getSource();
            if (jComboBox1 == source) {
                String serviceName = (String)jComboBox1.getSelectedItem();
                if (serviceName != null) {
                    Collection<Port> ports = WSDLUtils.getPortsForService(services, serviceName);
                    setPorts(ports);
                }
                changeOption("-sn", (String)serviceName);
            } else if (jComboBox2 == source) {
                String portName = (String)jComboBox2.getSelectedItem();
                changeOption("-pn", portName);
            } else if (jComboBox3 == source) {
                String databindingName = (String)jComboBox3.getSelectedItem();
                String db = getDatabindingName();
                changeOption("-d", db);
            } else if (jCheckBox1 == source) {
                boolean selected = jCheckBox1.isSelected();
                changeOption("-ssi", Boolean.valueOf(selected));
                
            }
        }
        
    }
    
    private void changeOption(String option, Object value) {
        StringBuffer buf = new StringBuffer();
        String text = jTextArea1.getText();
        if (!(value instanceof Boolean) && text.indexOf(option) == -1) return;
        StringTokenizer tokens = new StringTokenizer(text);
        boolean foundOption = false;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if ("-p".equals(token)) buf.append("\n");
            else buf.append(" ");
            if (option.equals(token)) {
                // string options
                if (value instanceof String) {
                    buf.append(token);
                    // skip next token
                    token = tokens.nextToken();
                    buf.append(" "+(String)value);
                }
                // boolean option
                else if (value instanceof Boolean) {
                    if (((Boolean)value).booleanValue()) {
                        buf.append(token);
                    }
                    foundOption = true;
                }
            } else {
                buf.append(token);
            }
        }
        if ((value instanceof Boolean) && !foundOption) {
            if (((Boolean)value).booleanValue()) {
                buf.append(" "+option);
            }
        }
        jTextArea1.setText(buf.toString());
    }
    
}
