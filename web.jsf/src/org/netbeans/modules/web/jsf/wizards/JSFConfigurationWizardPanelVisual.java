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

package org.netbeans.modules.web.jsf.wizards;


import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public class JSFConfigurationWizardPanelVisual extends JPanel implements HelpCtx.Provider, ChangeListener {
    private WebModuleExtender wme;
    private ExtenderController controller;
    private JSFConfigurationWizardPanel panel;
    
    /** Creates new form PanelInitProject
     * @param project the web project; if it is null, all available web extensions will be shown
     * @param filter one of the options <code>ALL_FRAMEWORKS</code>, <code>USED_FRAMEWORKS</code>, <code>UNUSED_FRAMEWORKS</code>
     * @param ignoredFrameworks the list of frameworks to be ignored when creating list; null is allowed
     */
    JSFConfigurationWizardPanelVisual(JSFConfigurationWizardPanel panel, ExtenderController controller, WebModuleExtender wme) {
        this.panel = panel;
        this.controller = controller;
        this.wme = wme;

        initComponents();
        setConfigPanel();

        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JSFConfigurationWizardPanelVisual.class, "ACS_JSF_Config_CRUD_Panel_A11YDesc"));  // NOI18N        

        // Provide a name in the title bar.
        setName(NbBundle.getMessage(JSFConfigurationWizardPanelVisual.class, "LBL_JSF_Config_CRUD")); //NOI18N
        putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(JSFConfigurationWizardPanelVisual.class, "LBL_JSF_Config_CRUD")); //NOI18N
    }


    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelConfig = new javax.swing.JLabel();
        jPanelConfig = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(500, 340));
        setRequestFocusEnabled(false);

        jLabelConfig.setLabelFor(jPanelConfig);
        jLabelConfig.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationWizardPanelVisual.class, "LBL_JSF_Config_CRUD_instruction")); // NOI18N

        jPanelConfig.setLayout(new java.awt.GridBagLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .add(jPanelConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanelConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                .add(137, 137, 137))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JPanel jPanelConfig;
    // End of variables declaration//GEN-END:variables

    boolean valid(WizardDescriptor wizardDescriptor) {
        setErrorMessage(wizardDescriptor, null);
        if (wme != null && !wme.isValid()) {
            setErrorMessage(wizardDescriptor, controller.getErrorMessage());
            return false;
        }

        return true;
    }
    
    private void setErrorMessage(WizardDescriptor wizardDescriptor, String errorMessage) {
        if (errorMessage == null || errorMessage.length() == 0) {
            errorMessage = " "; // NOI18N
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N
    }
    
    void read(WizardDescriptor settings) {
        if (wme != null) {
            wme.update();
        }
    }

    void store(WizardDescriptor settings) {
    }

    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        if (jPanelConfig.getComponentCount()>0){
            for (int i = 0; i < jPanelConfig.getComponentCount(); i++)
                if (jPanelConfig.getComponent(i) instanceof  HelpCtx.Provider)
                    return ((HelpCtx.Provider)jPanelConfig.getComponent(i)).getHelpCtx();
        }
        return null;
    }
    
    
    private void setConfigPanel() {
        if (wme != null) {
            jPanelConfig.removeAll();

            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;

            JComponent panelComponent = wme.getComponent();
            jPanelConfig.add(panelComponent, gridBagConstraints);
            
//            jLabelConfig.setEnabled(item.isSelected().booleanValue());
//            enableComponents(panelComponent, item.isSelected().booleanValue());
            
            //invoke JSFConfigurationPanelVisual.initLibraries now (during
            //construction of the wizard) instead of when this JavaServer Faces Configuration
            //panel is reached by the user, so as to ensure that the settings
            //set by wme.update() take precedence rather than those set by 
            //JSFConfigurationPanelVisual.initLibraries.
            if (panelComponent instanceof JSFConfigurationPanelVisual) {
                ((JSFConfigurationPanelVisual)panelComponent).initLibraries();
            }
            
            wme.update();
            jPanelConfig.revalidate();
            jPanelConfig.repaint();
        } else {
            jLabelConfig.setText(""); //NOI18N
            jPanelConfig.removeAll();
            jPanelConfig.repaint();
            jPanelConfig.revalidate();
        }
        
        if (panel != null)
            panel.fireChangeEvent();
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        if (panel != null)
            panel.fireChangeEvent();
    }
}
