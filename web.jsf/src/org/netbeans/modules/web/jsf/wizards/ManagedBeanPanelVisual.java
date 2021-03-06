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
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
//import org.netbeans.modules.web.struts.StrutsConfigUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public class ManagedBeanPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, ListDataListener {
    
    private final DefaultComboBoxModel scopeModel = new DefaultComboBoxModel();
    
    /**
     * Creates new form PropertiesPanelVisual
     */
    public ManagedBeanPanelVisual(Project proj) {
        initComponents();
        
        WebModule wm = WebModule.getWebModule(proj.getProjectDirectory());
        if (wm != null){
            String[] configFiles = JSFConfigUtilities.getConfigFiles(wm.getDeploymentDescriptor());
            jComboBoxConfigFile.setModel(new javax.swing.DefaultComboBoxModel(configFiles));
        }
        ManagedBean.Scope[] scopes = ManagedBean.Scope.values();
        for (int i = 0; i < scopes.length; i++){
            scopeModel.addElement(scopes[i]);
        }
        
//        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FormBeanNewPanelVisual.class, "ACS_BeanFormProperties"));  // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelConfigFile = new javax.swing.JLabel();
        jComboBoxConfigFile = new javax.swing.JComboBox();
        jLabelScope = new javax.swing.JLabel();
        jComboBoxScope = new javax.swing.JComboBox();
        jLabelDesc = new javax.swing.JLabel();
        jScrollPaneDesc = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        jLabelConfigFile.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_ConfigFile").charAt(0));
        jLabelConfigFile.setLabelFor(jComboBoxConfigFile);
        jLabelConfigFile.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_ConfigFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(jLabelConfigFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboBoxConfigFile, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        jComboBoxConfigFile.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ConfigurationFile")); // NOI18N

        jLabelScope.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_Scope").charAt(0));
        jLabelScope.setLabelFor(jComboBoxScope);
        jLabelScope.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_Scope")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(jLabelScope, gridBagConstraints);

        jComboBoxScope.setModel(scopeModel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboBoxScope, gridBagConstraints);
        jComboBoxScope.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ManagedBeanScope")); // NOI18N

        jLabelDesc.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_BeanDescription").charAt(0));
        jLabelDesc.setLabelFor(jTextAreaDesc);
        jLabelDesc.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_BeanDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabelDesc, gridBagConstraints);

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setRows(5);
        jScrollPaneDesc.setViewportView(jTextAreaDesc);
        jTextAreaDesc.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BeanDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPaneDesc, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxConfigFile;
    private javax.swing.JComboBox jComboBoxScope;
    private javax.swing.JLabel jLabelConfigFile;
    private javax.swing.JLabel jLabelDesc;
    private javax.swing.JLabel jLabelScope;
    private javax.swing.JScrollPane jScrollPaneDesc;
    private javax.swing.JTextArea jTextAreaDesc;
    // End of variables declaration//GEN-END:variables
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        String configFile = (String) jComboBoxConfigFile.getSelectedItem();
        boolean result = (configFile != null && !configFile.trim().equals("")); 
        if (!result){
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_NoConfFileSelected"));
        }
        return result;
    }
    
    void read(WizardDescriptor settings) {
    }
    
    void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.CONFIG_FILE, jComboBoxConfigFile.getSelectedItem());
        settings.putProperty(WizardProperties.SCOPE, jComboBoxScope.getSelectedItem());
        settings.putProperty(WizardProperties.DESCRIPTION, jTextAreaDesc.getText());
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ManagedBeanPanelVisual.class);
    }
    
    public void intervalRemoved(ListDataEvent e) {
    }
    
    public void intervalAdded(ListDataEvent e) {
    }
    
    public void contentsChanged(ListDataEvent e) {

    }
    
}
