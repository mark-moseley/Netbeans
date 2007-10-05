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
 * BeanCachePanel.java        October 22, 2003, 1:07 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ResourceBundle;
import javax.swing.JPanel;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.ValidationError;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class BeanCachePanel extends JPanel {

    private EjbCustomizer masterPanel;
    
    private final ResourceBundle bundle = ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N

    /** Creates new form BeanCachePanel */
    public BeanCachePanel(EjbCustomizer src) {
        this.masterPanel = src;
        
        initComponents();
        initUserComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        maxCacheSizeLabel = new javax.swing.JLabel();
        maxCacheSizeTextField = new javax.swing.JTextField();
        resizeQuantityLabel = new javax.swing.JLabel();
        resizeQuantityTextField = new javax.swing.JTextField();
        isCacheOverflowAllowedLabel = new javax.swing.JLabel();
        isCacheOverflowAllowedComboBox = new javax.swing.JComboBox();
        cacheIdleTimeoutInSecondsLabel = new javax.swing.JLabel();
        cacheIdleTimeoutInSecondsTextField = new javax.swing.JTextField();
        removalTimeoutInSecondsLabel = new javax.swing.JLabel();
        removalTimeoutInSecondsTextField = new javax.swing.JTextField();
        victimSelectionPolicyLabel = new javax.swing.JLabel();
        victimSelectionPolicyComboBox = new javax.swing.JComboBox();
        fillerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        maxCacheSizeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Max_Cache_Size").charAt(0));
        maxCacheSizeLabel.setLabelFor(maxCacheSizeTextField);
        maxCacheSizeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Max_Cache_Size_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(maxCacheSizeLabel, gridBagConstraints);
        maxCacheSizeLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Acsbl_Name"));
        maxCacheSizeLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Acsbl_Desc"));

        maxCacheSizeTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Tool_Tip"));
        maxCacheSizeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxCacheSizeKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(maxCacheSizeTextField, gridBagConstraints);
        maxCacheSizeTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Acsbl_Name"));
        maxCacheSizeTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Acsbl_Desc"));

        resizeQuantityLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Resize_Quantity").charAt(0));
        resizeQuantityLabel.setLabelFor(resizeQuantityTextField);
        resizeQuantityLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Resize_Quantity_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(resizeQuantityLabel, gridBagConstraints);
        resizeQuantityLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resize_Quantity_Acsbl_Name"));
        resizeQuantityLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Cache_Resize_Quantity_Acsbl_Desc"));

        resizeQuantityTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Cache_Resize_Quantity_Tool_Tip"));
        resizeQuantityTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resizeQuantityKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(resizeQuantityTextField, gridBagConstraints);
        resizeQuantityTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resize_Quantity_Acsbl_Name"));
        resizeQuantityTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Cache_Resize_Quantity_Acsbl_Desc"));

        isCacheOverflowAllowedLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Is_Cache_Overflow_Allowed").charAt(0));
        isCacheOverflowAllowedLabel.setLabelFor(isCacheOverflowAllowedComboBox);
        isCacheOverflowAllowedLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Is_Cache_Overflow_Allowed_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(isCacheOverflowAllowedLabel, gridBagConstraints);
        isCacheOverflowAllowedLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Acsbl_Name"));
        isCacheOverflowAllowedLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Acsbl_Desc"));

        isCacheOverflowAllowedComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        isCacheOverflowAllowedComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Tool_Tip"));
        isCacheOverflowAllowedComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isCacheOverflowAllowedComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(isCacheOverflowAllowedComboBox, gridBagConstraints);
        isCacheOverflowAllowedComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Acsbl_Name"));
        isCacheOverflowAllowedComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Acsbl_Desc"));

        cacheIdleTimeoutInSecondsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Cache_Idle_Timeout_In_Seconds").charAt(0));
        cacheIdleTimeoutInSecondsLabel.setLabelFor(cacheIdleTimeoutInSecondsTextField);
        cacheIdleTimeoutInSecondsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Cache_Idle_Timeout_In_Seconds_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(cacheIdleTimeoutInSecondsLabel, gridBagConstraints);
        cacheIdleTimeoutInSecondsLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Acsbl_Name"));
        cacheIdleTimeoutInSecondsLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Acsbl_Desc"));

        cacheIdleTimeoutInSecondsTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Tool_Tip"));
        cacheIdleTimeoutInSecondsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cacheIdleTimeoutInSecondsKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(cacheIdleTimeoutInSecondsTextField, gridBagConstraints);
        cacheIdleTimeoutInSecondsTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Acsbl_Name"));
        cacheIdleTimeoutInSecondsTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Acsbl_Desc"));

        removalTimeoutInSecondsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Removal_Timeout_In_Seconds").charAt(0));
        removalTimeoutInSecondsLabel.setLabelFor(removalTimeoutInSecondsTextField);
        removalTimeoutInSecondsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Removal_Timeout_In_Seconds_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(removalTimeoutInSecondsLabel, gridBagConstraints);
        removalTimeoutInSecondsLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Acsbl_Name"));
        removalTimeoutInSecondsLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Acsbl_Desc"));

        removalTimeoutInSecondsTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Tool_Tip"));
        removalTimeoutInSecondsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                removalTimeoutInSecondsKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(removalTimeoutInSecondsTextField, gridBagConstraints);
        removalTimeoutInSecondsTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Acsbl_Name"));
        removalTimeoutInSecondsTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Acsbl_Desc"));

        victimSelectionPolicyLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Victim_Selection_Policy").charAt(0));
        victimSelectionPolicyLabel.setLabelFor(victimSelectionPolicyComboBox);
        victimSelectionPolicyLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Victim_Selection_Policy_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(victimSelectionPolicyLabel, gridBagConstraints);
        victimSelectionPolicyLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Acsbl_Name"));
        victimSelectionPolicyLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Acsbl_Desc"));

        victimSelectionPolicyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "FIFO", "LRU", "NRU" }));
        victimSelectionPolicyComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Tool_Tip"));
        victimSelectionPolicyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                victimSelectionPolicyComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(victimSelectionPolicyComboBox, gridBagConstraints);
        victimSelectionPolicyComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Acsbl_Name"));
        victimSelectionPolicyComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillerPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void victimSelectionPolicyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_victimSelectionPolicyComboBoxActionPerformed
        BaseEjb theBean = masterPanel.getBean();
        if(theBean != null) {
            BeanCache beanCache = theBean.getBeanCache();
            String newVictimPolicy = (String) victimSelectionPolicyComboBox.getSelectedItem();
            String oldVictimPolicy = beanCache.getVictimSelectionPolicy();

            if(!Utils.strEquivalent(oldVictimPolicy, newVictimPolicy)) {
                if(Utils.notEmpty(newVictimPolicy)) {
                    beanCache.setVictimSelectionPolicy(newVictimPolicy);
                } else {
                    beanCache.setVictimSelectionPolicy(null);
                }

                theBean.firePropertyChange("beanCacheVictimPolicy", oldVictimPolicy, newVictimPolicy); // NOI18N
                masterPanel.validateField(BaseEjb.FIELD_BEANCACHE_VICTIMPOLICY);
            }
        }
    }//GEN-LAST:event_victimSelectionPolicyComboBoxActionPerformed

    private void isCacheOverflowAllowedComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isCacheOverflowAllowedComboBoxActionPerformed
        BaseEjb theBean = masterPanel.getBean();
        if(theBean != null) {
            BeanCache beanCache = theBean.getBeanCache();
            String newOverflowAllowed = (String) isCacheOverflowAllowedComboBox.getSelectedItem();
            String oldOverflowAllowed = beanCache.getIsCacheOverflowAllowed();

            if(!Utils.strEquivalent(oldOverflowAllowed, newOverflowAllowed)) {
                if(Utils.notEmpty(newOverflowAllowed)) {
                    beanCache.setIsCacheOverflowAllowed(newOverflowAllowed);
                } else {
                    beanCache.setIsCacheOverflowAllowed(null);
                }

                theBean.firePropertyChange("beanCacheOverflowAllowed", oldOverflowAllowed, newOverflowAllowed); // NOI18N
                masterPanel.validateField(BaseEjb.FIELD_BEANCACHE_OVERFLOWALLOWED);
            }
        }
    }//GEN-LAST:event_isCacheOverflowAllowedComboBoxActionPerformed

    private void removalTimeoutInSecondsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_removalTimeoutInSecondsKeyReleased
        BaseEjb theBean = masterPanel.getBean();
        if(theBean != null) {
            BeanCache beanCache = theBean.getBeanCache();
            String newRemovalTimeout = removalTimeoutInSecondsTextField.getText();
            String oldRemovalTimeout = beanCache.getRemovalTimeoutInSeconds();

            if(!Utils.strEquivalent(oldRemovalTimeout, newRemovalTimeout)) {
                if(Utils.notEmpty(newRemovalTimeout)) {
                    beanCache.setRemovalTimeoutInSeconds(newRemovalTimeout);
                } else {
                    beanCache.setRemovalTimeoutInSeconds(null);
                }

                theBean.firePropertyChange("beanCacheRemovalTimeout", oldRemovalTimeout, newRemovalTimeout); // NOI18N
                masterPanel.validateField(BaseEjb.FIELD_BEANCACHE_REMOVALTIMEOUT);
            }
        }
    }//GEN-LAST:event_removalTimeoutInSecondsKeyReleased

    private void cacheIdleTimeoutInSecondsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cacheIdleTimeoutInSecondsKeyReleased
        BaseEjb theBean = masterPanel.getBean();
        if(theBean != null) {
            BeanCache beanCache = theBean.getBeanCache();
            String newCacheIdleTimeout = cacheIdleTimeoutInSecondsTextField.getText();
            String oldCacheIdleTimeout = beanCache.getCacheIdleTimeoutInSeconds();

            if(!Utils.strEquivalent(oldCacheIdleTimeout, newCacheIdleTimeout)) {
                if(Utils.notEmpty(newCacheIdleTimeout)) {
                    beanCache.setCacheIdleTimeoutInSeconds(newCacheIdleTimeout);
                } else {
                    beanCache.setCacheIdleTimeoutInSeconds(null);
                }

                theBean.firePropertyChange("beanCacheIdleTimeout", oldCacheIdleTimeout, newCacheIdleTimeout); // NOI18N
                masterPanel.validateField(BaseEjb.FIELD_BEANCACHE_IDLETIMEOUT);
            }
        }
    }//GEN-LAST:event_cacheIdleTimeoutInSecondsKeyReleased

    private void resizeQuantityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resizeQuantityKeyReleased
        BaseEjb theBean = masterPanel.getBean();
        if(theBean != null) {
            BeanCache beanCache = theBean.getBeanCache();
            String newResizeQuantity = resizeQuantityTextField.getText();
            String oldResizeQuantity = beanCache.getResizeQuantity();

            if(!Utils.strEquivalent(oldResizeQuantity, newResizeQuantity)) {
                if(Utils.notEmpty(newResizeQuantity)) {
                    beanCache.setResizeQuantity(newResizeQuantity);
                } else {
                    beanCache.setResizeQuantity(null);
                }

                theBean.firePropertyChange("beanCacheResizeQuantity", oldResizeQuantity, newResizeQuantity); // NOI18N
                masterPanel.validateField(BaseEjb.FIELD_BEANCACHE_RESIZEQUANTITY);
            }
        }
    }//GEN-LAST:event_resizeQuantityKeyReleased

    private void maxCacheSizeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxCacheSizeKeyReleased
        BaseEjb theBean = masterPanel.getBean();
        if(theBean != null) {
            BeanCache beanCache = theBean.getBeanCache();
            String newMaxCacheSize = maxCacheSizeTextField.getText();
            String oldMaxCacheSize = beanCache.getMaxCacheSize();

            if(!Utils.strEquivalent(oldMaxCacheSize, newMaxCacheSize)) {
                if(Utils.notEmpty(newMaxCacheSize)) {
                    beanCache.setMaxCacheSize(newMaxCacheSize);
                } else {
                    beanCache.setMaxCacheSize(null);
                }

                theBean.firePropertyChange("beanCacheMaxCacheSize", oldMaxCacheSize, newMaxCacheSize); // NOI18N
                masterPanel.validateField(BaseEjb.FIELD_BEANCACHE_MAXSIZE);
            }
        }
    }//GEN-LAST:event_maxCacheSizeKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cacheIdleTimeoutInSecondsLabel;
    private javax.swing.JTextField cacheIdleTimeoutInSecondsTextField;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JComboBox isCacheOverflowAllowedComboBox;
    private javax.swing.JLabel isCacheOverflowAllowedLabel;
    private javax.swing.JLabel maxCacheSizeLabel;
    private javax.swing.JTextField maxCacheSizeTextField;
    private javax.swing.JLabel removalTimeoutInSecondsLabel;
    private javax.swing.JTextField removalTimeoutInSecondsTextField;
    private javax.swing.JLabel resizeQuantityLabel;
    private javax.swing.JTextField resizeQuantityTextField;
    private javax.swing.JComboBox victimSelectionPolicyComboBox;
    private javax.swing.JLabel victimSelectionPolicyLabel;
    // End of variables declaration//GEN-END:variables

    private void initUserComponents() {
        putClientProperty(EjbCustomizer.PARTITION_KEY, ValidationError.PARTITION_EJB_BEANCACHE);
    }
    
    public void initFields(BeanCache beanCache) {
        if(beanCache != null) {
            maxCacheSizeTextField.setText(beanCache.getMaxCacheSize());
            resizeQuantityTextField.setText(beanCache.getResizeQuantity());
            isCacheOverflowAllowedComboBox.setSelectedItem(beanCache.getIsCacheOverflowAllowed());
            cacheIdleTimeoutInSecondsTextField.setText(beanCache.getCacheIdleTimeoutInSeconds());
            removalTimeoutInSecondsTextField.setText(beanCache.getRemovalTimeoutInSeconds());
            victimSelectionPolicyComboBox.setSelectedItem(beanCache.getVictimSelectionPolicy());
        }
    }
}
