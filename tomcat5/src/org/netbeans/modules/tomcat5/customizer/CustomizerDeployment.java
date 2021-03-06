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

package org.netbeans.modules.tomcat5.customizer;

import javax.swing.JSpinner.NumberEditor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Customizer deployment tab.
 *
 * @author  sherold
 */
public class CustomizerDeployment extends javax.swing.JPanel {
    
    private final CustomizerDataSupport custData;
    
    /** Creates new form CustomizerDeployment */
    public CustomizerDeployment(CustomizerDataSupport custData) {
        this.custData = custData;
        initComponents();
        ((NumberEditor) deploymentTimeoutSpinner.getEditor()).getTextField().setColumns(5);
        // working around the issue #111094
        Mnemonics.setLocalizedText(driverDeploymentjCheckBox, NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.driverDeploymentjCheckBox.text"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        driverDeploymentjCheckBox = new javax.swing.JCheckBox();
        deploymentTimeoutSpinner = new javax.swing.JSpinner();
        deplolymentTimeoutLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(driverDeploymentjCheckBox, org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.driverDeploymentjCheckBox.text")); // NOI18N
        driverDeploymentjCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.driverDeploymentjCheckBox.toolTipText")); // NOI18N
        driverDeploymentjCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        driverDeploymentjCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        driverDeploymentjCheckBox.setModel(custData.getDriverDeploymentModel());

        deploymentTimeoutSpinner.setModel(custData.getDeploymentTimeoutModel());
        deploymentTimeoutSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.deplolymentTimeoutSpinner.tooltip")); // NOI18N
        deploymentTimeoutSpinner.setEditor(new NumberEditor(deploymentTimeoutSpinner, "#"));

        deplolymentTimeoutLabel.setLabelFor(deploymentTimeoutSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(deplolymentTimeoutLabel, org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.deplolymentTimeoutLabel.text")); // NOI18N
        deplolymentTimeoutLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.deplolymentTimeoutLabel.tooltip")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(deplolymentTimeoutLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deploymentTimeoutSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(driverDeploymentjCheckBox))
                .addContainerGap(176, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deplolymentTimeoutLabel)
                    .add(deploymentTimeoutSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(driverDeploymentjCheckBox)
                .addContainerGap(241, Short.MAX_VALUE))
        );

        driverDeploymentjCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.driverDeploymentjCheckBox.accessible.name")); // NOI18N
        driverDeploymentjCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.driverDeploymentjCheckBox.accessible.description")); // NOI18N
        deploymentTimeoutSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.deplolymentTimeoutSpinner.accessible.name")); // NOI18N
        deploymentTimeoutSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.deplolymentTimeoutSpinner.accessible.description")); // NOI18N
        deplolymentTimeoutLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.deplolymentTimeoutLabel.accessible.name")); // NOI18N
        deplolymentTimeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerDeployment.class, "CustomizerDeployment.deplolymentTimeoutLabel.accessible.description")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel deplolymentTimeoutLabel;
    private javax.swing.JSpinner deploymentTimeoutSpinner;
    private javax.swing.JCheckBox driverDeploymentjCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
