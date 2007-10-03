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
 * MDEjbPanel.java        October 27, 2003, 3:59 PM
 *
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.beans.PropertyVetoException;
import javax.swing.JPanel;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.MDEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class MDEjbPanel extends JPanel {

    private MDEjbCustomizer masterPanel;


    /** Creates new form MDEjbPanel */
    public MDEjbPanel(MDEjbCustomizer src) {
        this.masterPanel = src;
        
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jmsDurableSubscriptionNameLabel = new javax.swing.JLabel();
        jmsDurableSubscriptionNameTextField = new javax.swing.JTextField();
        jmsMaxMessagesLoadLabel = new javax.swing.JLabel();
        jmsMaxMessagesLoadTextField = new javax.swing.JTextField();
        resourceAdapterMidLabel = new javax.swing.JLabel();
        resourceAdapterMidTextField = new javax.swing.JTextField();
        activationConfigDescriptionLabel = new javax.swing.JLabel();
        activationConfigDescriptionTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jmsDurableSubscriptionNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Jms_Durable_Subscription_Name").charAt(0));
        jmsDurableSubscriptionNameLabel.setLabelFor(jmsDurableSubscriptionNameTextField);
        jmsDurableSubscriptionNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Jms_Durable_Subscription_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jmsDurableSubscriptionNameLabel, gridBagConstraints);
        jmsDurableSubscriptionNameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Durable_Subscription_Name_Acsbl_Name"));
        jmsDurableSubscriptionNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Durable_Subscription_Name_Acsbl_Desc"));

        jmsDurableSubscriptionNameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Durable_Subscription_Name_Tool_Tip"));
        jmsDurableSubscriptionNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jmsDurableSubscriptionNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jmsDurableSubscriptionNameTextField, gridBagConstraints);
        jmsDurableSubscriptionNameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Durable_Subscription_Name_Acsbl_Name"));
        jmsDurableSubscriptionNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Durable_Subscription_Name_Acsbl_Desc"));

        jmsMaxMessagesLoadLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Jms_Max_Messages_Load").charAt(0));
        jmsMaxMessagesLoadLabel.setLabelFor(jmsMaxMessagesLoadTextField);
        jmsMaxMessagesLoadLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Jms_Max_Messages_Load_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        add(jmsMaxMessagesLoadLabel, gridBagConstraints);
        jmsMaxMessagesLoadLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Max_Messages_Load_Acsbl_Name"));
        jmsMaxMessagesLoadLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Max_Messages_Load_Acsbl_Desc"));

        jmsMaxMessagesLoadTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Max_Messages_Load_Tool_Tip"));
        jmsMaxMessagesLoadTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jmsMaxMessagesLoadKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jmsMaxMessagesLoadTextField, gridBagConstraints);
        jmsMaxMessagesLoadTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Max_Messages_Load_Acsbl_Name"));
        jmsMaxMessagesLoadTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jms_Max_Messages_Load_Acsbl_Desc"));

        resourceAdapterMidLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Resource_Adapter_Mid").charAt(0));
        resourceAdapterMidLabel.setLabelFor(resourceAdapterMidTextField);
        resourceAdapterMidLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Resource_Adapter_Mid_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(resourceAdapterMidLabel, gridBagConstraints);
        resourceAdapterMidLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resource_Adapter_Mid_Acsbl_Name"));
        resourceAdapterMidLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resource_Adapter_Mid_Acsbl_Desc"));

        resourceAdapterMidTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resource_Adapter_Mid_Tool_Tip"));
        resourceAdapterMidTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resourceAdapterMidKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 5);
        add(resourceAdapterMidTextField, gridBagConstraints);
        resourceAdapterMidTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resource_Adapter_Mid_Acsbl_Name"));
        resourceAdapterMidTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resource_Adapter_Mid_Acsbl_Desc"));

        activationConfigDescriptionLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Activation_Config_Description").charAt(0));
        activationConfigDescriptionLabel.setLabelFor(activationConfigDescriptionTextField);
        activationConfigDescriptionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Activation_Config_Description_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(activationConfigDescriptionLabel, gridBagConstraints);
        activationConfigDescriptionLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Activation_Config_Description_Acsbl_Name"));
        activationConfigDescriptionLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Activation_Config_Description_Acsbl_Desc"));

        activationConfigDescriptionTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Activation_Config_Description_Tool_Tip"));
        activationConfigDescriptionTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                activationConfigDescriptionKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(activationConfigDescriptionTextField, gridBagConstraints);
        activationConfigDescriptionTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Activation_Config_Description_Acsbl_Name"));
        activationConfigDescriptionTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Activation_Config_Description_Acsbl_Desc"));

    }// </editor-fold>//GEN-END:initComponents

    private void activationConfigDescriptionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_activationConfigDescriptionKeyReleased
        MDEjb theBean = masterPanel.getMDBean();
        if(theBean != null) {
            MdbResourceAdapter mra = theBean.getMdbResourceAdapter();
            ActivationConfig ac = mra.getActivationConfig();
            String newDescription = activationConfigDescriptionTextField.getText();
            String oldDescription = (ac != null) ? ac.getDescription() : null;

            if(!Utils.strEquivalent(oldDescription, newDescription)) {
                if(Utils.notEmpty(newDescription)) {
                    if(ac == null) {
                        ac = mra.newActivationConfig();
                        mra.setActivationConfig(ac);
                    }
                    ac.setDescription(newDescription);
                } else if(ac != null) {
                    if(ac.sizeActivationConfigProperty() == 0) {
                        mra.setActivationConfig(null);
                    } else {
                        ac.setDescription(null);
                    }
                }

                theBean.firePropertyChange("mdActCfgDescription", oldDescription, newDescription); // NOI18N
                masterPanel.validateField(MDEjb.FIELD_MD_ADAPTER);
            }
        }
    }//GEN-LAST:event_activationConfigDescriptionKeyReleased

    private void resourceAdapterMidKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resourceAdapterMidKeyReleased
        MDEjb theBean = masterPanel.getMDBean();
        if(theBean != null) {
            MdbResourceAdapter mra = theBean.getMdbResourceAdapter();
            String newResourceAdapterMid = resourceAdapterMidTextField.getText();
            String oldResourceAdapterMid = mra.getResourceAdapterMid();

            if(!Utils.strEquivalent(oldResourceAdapterMid, newResourceAdapterMid)) {
                if(Utils.notEmpty(newResourceAdapterMid)) {
                    mra.setResourceAdapterMid(newResourceAdapterMid);
                } else {
                    mra.setResourceAdapterMid(null);
                }

                theBean.firePropertyChange("mdResourceAdapterMid", oldResourceAdapterMid, newResourceAdapterMid); // NOI18N
                masterPanel.validateField(MDEjb.FIELD_MD_ADAPTER);
            }
        }
    }//GEN-LAST:event_resourceAdapterMidKeyReleased

    private void jmsMaxMessagesLoadKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jmsMaxMessagesLoadKeyReleased
        MDEjb theBean = masterPanel.getMDBean();
        if(theBean != null) {
            String newMaxMessages = jmsMaxMessagesLoadTextField.getText();
            String oldMaxMessages = theBean.getMaxMessageLoad();

            try {
               if(!Utils.strEquivalent(oldMaxMessages, newMaxMessages)) {
                    if(Utils.notEmpty(newMaxMessages)) {
                        theBean.setMaxMessageLoad(newMaxMessages);
                    } else {
                        theBean.setMaxMessageLoad(null);
                    }

//                    theBean.firePropertyChange("mdJmsSubscriptionName", oldMaxMessages, newMaxMessages); // NOI18N
                    masterPanel.validateField(MDEjb.FIELD_MD_MAXMESSAGES);
                }
            } catch (PropertyVetoException ex) {
                jmsDurableSubscriptionNameTextField.setText(oldMaxMessages);
            }
        }
    }//GEN-LAST:event_jmsMaxMessagesLoadKeyReleased

    private void jmsDurableSubscriptionNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jmsDurableSubscriptionNameKeyReleased
        MDEjb theBean = masterPanel.getMDBean();
        if(theBean != null) {
            String newSubscriptionName = jmsDurableSubscriptionNameTextField.getText();
            String oldSubscriptionName = theBean.getSubscriptionName();

            try {
               if(!Utils.strEquivalent(oldSubscriptionName, newSubscriptionName)) {
                    if(Utils.notEmpty(newSubscriptionName)) {
                        theBean.setSubscriptionName(newSubscriptionName);
                    } else {
                        theBean.setSubscriptionName(null);
                    }

//                    theBean.firePropertyChange("mdJmsSubscriptionName", oldSubscriptionName, newSubscriptionName); // NOI18N
                    masterPanel.validateField(MDEjb.FIELD_MD_SUBSCRIPTION);
                }
            } catch (PropertyVetoException ex) {
                jmsDurableSubscriptionNameTextField.setText(oldSubscriptionName);
            }
        }
    }//GEN-LAST:event_jmsDurableSubscriptionNameKeyReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activationConfigDescriptionLabel;
    private javax.swing.JTextField activationConfigDescriptionTextField;
    private javax.swing.JLabel jmsDurableSubscriptionNameLabel;
    private javax.swing.JTextField jmsDurableSubscriptionNameTextField;
    private javax.swing.JLabel jmsMaxMessagesLoadLabel;
    private javax.swing.JTextField jmsMaxMessagesLoadTextField;
    private javax.swing.JLabel resourceAdapterMidLabel;
    private javax.swing.JTextField resourceAdapterMidTextField;
    // End of variables declaration//GEN-END:variables
 
    public void initFields(MDEjb theBean) {
        String subscriptionName = null;
        String maxMessageLoad = null;
        String resourceAdaptorMid = null;
        String acDescription = null;
        
        if(theBean != null) {
            subscriptionName = theBean.getSubscriptionName();
            maxMessageLoad = theBean.getMaxMessageLoad();
            
            MdbResourceAdapter mra = theBean.getMdbResourceAdapter();
            if(mra != null) {
                resourceAdaptorMid = mra.getResourceAdapterMid();
                ActivationConfig ac = mra.getActivationConfig();
                if(ac != null) {
                    acDescription = ac.getDescription();
                }
            }
        }
        
        jmsDurableSubscriptionNameTextField.setText(subscriptionName);
        jmsMaxMessagesLoadTextField.setText(maxMessageLoad);
        resourceAdapterMidTextField.setText(resourceAdaptorMid);
        activationConfigDescriptionTextField.setText(acDescription);
    }
}
