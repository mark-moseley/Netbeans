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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;

/**
 *
 * @author  cwebster
 * @author Martin Adamek
 */
public class SessionEJBWizardPanel extends javax.swing.JPanel {

    private final ChangeListener listener;
    private final Project project;

    /** Creates new form SingleEJBWizardPanel */
    public SessionEJBWizardPanel(Project project, ChangeListener changeListener) {
        this.listener = changeListener;
        this.project = project;
        initComponents();

        J2eeProjectCapabilities projectCap = J2eeProjectCapabilities.forProject(project);
        if (projectCap.isEjb31LiteSupported()){
            singletonButton.setVisible(false);
            singletonButton.setEnabled(false);
            localCheckBox.setSelected(true);
            if (!projectCap.isEjb31Supported()){
                remoteCheckBox.setVisible(false); // EJB 3.1 Lite supports only local and no interface views
                remoteCheckBox.setEnabled(false);
            }
        }

        localCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                listener.stateChanged(null);
            }
        });

        remoteCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                listener.stateChanged(null);
            }
        });

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sessionStateButtons = new javax.swing.ButtonGroup();
        sessionTypeLabel = new javax.swing.JLabel();
        statelessButton = new javax.swing.JRadioButton();
        statefulButton = new javax.swing.JRadioButton();
        interfaceLabel = new javax.swing.JLabel();
        remoteCheckBox = new javax.swing.JCheckBox();
        localCheckBox = new javax.swing.JCheckBox();
        singletonButton = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(sessionTypeLabel, org.openide.util.NbBundle.getMessage(SessionEJBWizardPanel.class, "LBL_SessionType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(sessionTypeLabel, gridBagConstraints);

        sessionStateButtons.add(statelessButton);
        statelessButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ejb/wizard/session/Bundle").getString("MN_Stateless").charAt(0));
        statelessButton.setSelected(true);
        statelessButton.setText(org.openide.util.NbBundle.getMessage(SessionEJBWizardPanel.class, "LBL_Stateless")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(statelessButton, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ejb/wizard/session/Bundle"); // NOI18N
        statelessButton.getAccessibleContext().setAccessibleName(bundle.getString("LBL_Stateless")); // NOI18N
        statelessButton.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_Stateless")); // NOI18N

        sessionStateButtons.add(statefulButton);
        statefulButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ejb/wizard/session/Bundle").getString("MN_Stateful").charAt(0));
        statefulButton.setText(org.openide.util.NbBundle.getMessage(SessionEJBWizardPanel.class, "LBL_Stateful")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(statefulButton, gridBagConstraints);
        statefulButton.getAccessibleContext().setAccessibleName(bundle.getString("LBL_Stateful")); // NOI18N
        statefulButton.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_Stateful")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(interfaceLabel, org.openide.util.NbBundle.getMessage(SessionEJBWizardPanel.class, "LBL_Interface")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        add(interfaceLabel, gridBagConstraints);

        remoteCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ejb/wizard/session/Bundle").getString("MN_Remote").charAt(0));
        remoteCheckBox.setText(org.openide.util.NbBundle.getMessage(SessionEJBWizardPanel.class, "LBL_Remote")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(remoteCheckBox, gridBagConstraints);
        remoteCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("LBL_Remote")); // NOI18N
        remoteCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_Remote")); // NOI18N

        localCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ejb/wizard/session/Bundle").getString("MN_Local").charAt(0));
        localCheckBox.setText(org.openide.util.NbBundle.getMessage(SessionEJBWizardPanel.class, "LBL_Local")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(localCheckBox, gridBagConstraints);
        localCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("LBL_Local")); // NOI18N
        localCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_Local")); // NOI18N

        sessionStateButtons.add(singletonButton);
        singletonButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ejb/wizard/session/Bundle").getString("MN_Singleton").charAt(0));
        singletonButton.setText(org.openide.util.NbBundle.getMessage(SessionEJBWizardPanel.class, "LBL_Singleton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(singletonButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
                        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel interfaceLabel;
    private javax.swing.JCheckBox localCheckBox;
    private javax.swing.JCheckBox remoteCheckBox;
    private javax.swing.ButtonGroup sessionStateButtons;
    private javax.swing.JLabel sessionTypeLabel;
    private javax.swing.JRadioButton singletonButton;
    private javax.swing.JRadioButton statefulButton;
    private javax.swing.JRadioButton statelessButton;
    // End of variables declaration//GEN-END:variables

    public String getSessionType() {
        if (statelessButton.isSelected()){
            return Session.SESSION_TYPE_STATELESS;
        }else if (statefulButton.isSelected()){
            return Session.SESSION_TYPE_STATEFUL;
        }else if (singletonButton.isSelected()){
            return Session.SESSION_TYPE_SINGLETON;
        }

        return "";
    }
    
    public boolean isRemote() {
        return remoteCheckBox.isSelected();
    }
    
    public boolean isLocal() {
        return localCheckBox.isSelected();
    }
}
