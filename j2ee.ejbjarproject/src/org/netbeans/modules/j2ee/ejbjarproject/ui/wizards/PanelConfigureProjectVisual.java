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

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import javax.swing.JPanel;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class PanelConfigureProjectVisual extends JPanel {
    private static final long serialVersionUID = 178059227255000355L;

    private PanelConfigureProject panel;

    private PanelProjectLocationVisual projectLocationPanel;
    private PanelOptionsVisual optionsPanel;

    /** Creates new form PanelInitProject */
    public PanelConfigureProjectVisual(PanelConfigureProject panel) {
        this.panel = panel;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelConfigureProjectVisual.class, "ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N
        
        projectLocationPanel = new PanelProjectLocationVisual(panel);
        locationContainer.add(projectLocationPanel, java.awt.BorderLayout.NORTH);

        optionsPanel = new PanelOptionsVisual(panel);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        optionsContainer.add(optionsPanel, gridBagConstraints);

///
/*        DocumentListener dl = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                setContextPath(e);
            }

            public void insertUpdate(DocumentEvent e) {
                setContextPath(e);
            }

            public void removeUpdate(DocumentEvent e) {
                setContextPath(e);
            }

            private void setContextPath(DocumentEvent e) {
                if (!optionsPanel.isContextModified())
                    optionsPanel.jTextFieldContextPath.setText("/" + projectLocationPanel.projectNameTextField.getText().trim().replace(' ', '_'));
            }
        };
        projectLocationPanel.projectNameTextField.getDocument().addDocumentListener(dl);
*/
///


        // Provide a name in the title bar.
        setName(NbBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_NWP1_ProjectTitleName")); //NOI18N
        putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelConfigureProjectVisual.class, "TXT_NewWebApp")); //NOI18N
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        return projectLocationPanel.valid(wizardDescriptor) && optionsPanel.valid(wizardDescriptor);
    }

    void read (WizardDescriptor d) {
        projectLocationPanel.read(d);
        optionsPanel.read(d);
    }

    void store(WizardDescriptor d) {
        projectLocationPanel.store(d);
        optionsPanel.store(d);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        locationContainer = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        optionsContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setRequestFocusEnabled(false);
        locationContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(locationContainer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jSeparator1, gridBagConstraints);

        optionsContainer.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(optionsContainer, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel locationContainer;
    private javax.swing.JPanel optionsContainer;
    // End of variables declaration//GEN-END:variables

}
