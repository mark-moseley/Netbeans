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

import java.text.NumberFormat;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMMSModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.xml.wsdl.model.Binding;

import javax.swing.*;

/**
 *
 * @author Martin Grebac
 */
public class AdvancedRMPanel extends JPanel {

    private Binding binding;
    private boolean inSync = false;

    private DefaultFormatterFactory inactff = null;
    private DefaultFormatterFactory maxBufff = null;
    
    public AdvancedRMPanel(Binding binding) {
        this.binding = binding;
        
        inactff = new DefaultFormatterFactory();
        NumberFormat inactivityFormat = NumberFormat.getIntegerInstance();
        inactivityFormat.setGroupingUsed(false);
        NumberFormatter inactivityFormatter = new NumberFormatter(inactivityFormat);
        inactivityFormat.setMaximumIntegerDigits(8);
        inactivityFormatter.setCommitsOnValidEdit(true);
        inactivityFormatter.setMinimum(0);
        inactivityFormatter.setMaximum(99999999);
        inactff.setDefaultFormatter(inactivityFormatter);
                
        maxBufff = new DefaultFormatterFactory();
        NumberFormat maxBufFormat = NumberFormat.getIntegerInstance();
        maxBufFormat.setGroupingUsed(false);
        NumberFormatter maxBufFormatter = new NumberFormatter(maxBufFormat);
        maxBufFormat.setMaximumIntegerDigits(8);
        maxBufFormatter.setCommitsOnValidEdit(true);
        maxBufFormatter.setMinimum(0);
        maxBufFormatter.setMaximum(99999999);
        maxBufff.setDefaultFormatter(maxBufFormatter);

        initComponents();
        
        sync();
    }

    private void sync() {
        inSync = true;
        
        String inactivityTimeout = RMModelHelper.getInactivityTimeout(binding);
        if (inactivityTimeout == null) { // no setup exists yet - set the default
            setInactivityTimeout(RMModelHelper.DEFAULT_TIMEOUT);
        } else {
            setInactivityTimeout(inactivityTimeout);
        } 

        String maxRcvBufferSize = RMMSModelHelper.getMaxReceiveBufferSize(binding);
        if (maxRcvBufferSize == null) { // no setup exists yet - set the default
            setMaxRcvBufferSize(RMModelHelper.DEFAULT_MAXRCVBUFFERSIZE);
        } else {
            setMaxRcvBufferSize(maxRcvBufferSize);
        } 

        setFlowControl(RMMSModelHelper.isFlowControlEnabled(binding));

        enableDisable();
        inSync = false;
    }

    // max receive buffer size
    private Number getMaxRcvBufferSize() {
        return (Number) this.maxBufTextField.getValue();
    }
    
    private void setMaxRcvBufferSize(String value) {
        this.maxBufTextField.setText(value);
    }

    // inactivity timeout
    private Number getInactivityTimeout() {
        return (Number) this.inactivityTimeoutTextfield.getValue();
    }
    
    private void setInactivityTimeout(String value) {
        this.inactivityTimeoutTextfield.setText(value);
    }

    // flow control
    private void setFlowControl(Boolean enable) {
        if (enable == null) {
            this.flowControlChBox.setSelected(false);
        } else {
            this.flowControlChBox.setSelected(enable);
        }
    }

    public Boolean getFlowControl() {
        if (flowControlChBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
        
    public void storeState() {

        if (flowControlChBox.isSelected()) {
            if (!(RMMSModelHelper.isFlowControlEnabled(binding))) {
                RMMSModelHelper.enableFlowControl(binding);
            }
        } else {
            if (RMMSModelHelper.isFlowControlEnabled(binding)) {
                RMMSModelHelper.disableFlowControl(binding);
            }
        }
        
        Number timeout = getInactivityTimeout();
        if ((timeout == null) || (RMModelHelper.DEFAULT_TIMEOUT.equals(timeout.toString()))) {
            RMModelHelper.setInactivityTimeout(binding, null);
        } else {
            RMModelHelper.setInactivityTimeout(binding, timeout.toString());
        }

        Number bufSize = getMaxRcvBufferSize();
        if ((bufSize == null) || (RMModelHelper.DEFAULT_MAXRCVBUFFERSIZE.equals(bufSize.toString()))) {
            RMMSModelHelper.setMaxReceiveBufferSize(binding, null);
        } else {
            RMMSModelHelper.setMaxReceiveBufferSize(binding, bufSize.toString());
        }

    }
    
    private void enableDisable() {
        boolean flowSelected = flowControlChBox.isSelected();
        maxBufLabel.setEnabled(flowSelected);
        maxBufTextField.setEnabled(flowSelected);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        flowControlChBox = new javax.swing.JCheckBox();
        maxBufLabel = new javax.swing.JLabel();
        inactivityTimeoutLabel = new javax.swing.JLabel();
        inactivityTimeoutTextfield = new javax.swing.JFormattedTextField();
        maxBufTextField = new javax.swing.JFormattedTextField();

        flowControlChBox.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_FlowControlChBox")); // NOI18N
        flowControlChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        flowControlChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flowControlChBoxActionPerformed(evt);
            }
        });

        maxBufLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_maxBufLabel")); // NOI18N

        inactivityTimeoutLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_InactivityTimeoutLabel")); // NOI18N

        inactivityTimeoutTextfield.setFormatterFactory(inactff);

        maxBufTextField.setColumns(8);
        maxBufTextField.setFormatterFactory(maxBufff);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(maxBufLabel)
                            .add(inactivityTimeoutLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(inactivityTimeoutTextfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                            .add(maxBufTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)))
                    .add(flowControlChBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(17, 17, 17)
                .add(flowControlChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxBufLabel)
                    .add(maxBufTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inactivityTimeoutLabel)
                    .add(inactivityTimeoutTextfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        flowControlChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_FlowControl_ACSD")); // NOI18N
        maxBufLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_MaxFlowBufSize_ACSD")); // NOI18N
        inactivityTimeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_InactTimeout_ACSD")); // NOI18N
        inactivityTimeoutTextfield.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_InactTimeout_ACSN")); // NOI18N
        inactivityTimeoutTextfield.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_InactTimeout_ACSD")); // NOI18N
        maxBufTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_MaxBuf_ACSN")); // NOI18N
        maxBufTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_MaxBuf_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void flowControlChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flowControlChBoxActionPerformed
        enableDisable();
    }//GEN-LAST:event_flowControlChBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox flowControlChBox;
    private javax.swing.JLabel inactivityTimeoutLabel;
    private javax.swing.JFormattedTextField inactivityTimeoutTextfield;
    private javax.swing.JLabel maxBufLabel;
    private javax.swing.JFormattedTextField maxBufTextField;
    // End of variables declaration//GEN-END:variables
    
}
