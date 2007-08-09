/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebFrameworkSupport;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.WizardDescriptor;

public class CustomizerFrameworks extends javax.swing.JPanel implements HelpCtx.Provider, ListSelectionListener {
    
    private final ProjectCustomizer.Category category;
    private WebProject project;
    private WebProjectProperties uiProperties;
    private List usedFrameworks = new LinkedList();
    private List newFrameworks = new LinkedList();
    
    /** Creates new form CustomizerFrameworks */
    public CustomizerFrameworks(ProjectCustomizer.Category category, WebProjectProperties uiProperties) {
        this.category = category;
        this.uiProperties = uiProperties;
        initComponents();
        
        project = uiProperties.getProject();
        initFrameworksList(project.getAPIWebModule());        
    }
    
    private void initFrameworksList(WebModule webModule) {
        jListFrameworks.setModel(new DefaultListModel());
        List frameworks = WebFrameworkSupport.getFrameworkProviders();
        for (int i = 0; i < frameworks.size(); i++) {
            WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
            if (framework.isInWebModule(webModule)) {
                usedFrameworks.add(framework);
                ((DefaultListModel) jListFrameworks.getModel()).addElement(framework.getName());
            }                
        }
        jListFrameworks.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListFrameworks.addListSelectionListener(this);
        if (usedFrameworks.size() > 0)
            jListFrameworks.setSelectedIndex(0);
        
        if (frameworks.size() == jListFrameworks.getModel().getSize())
            jButtonAdd.setEnabled(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelFrameworks = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListFrameworks = new javax.swing.JList();
        jButtonAdd = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanelConfig = new javax.swing.JPanel();
        jLabelConfig = new javax.swing.JLabel();

        jLabelFrameworks.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_CustomizerFrameworks_ListMnemonic").charAt(0));
        jLabelFrameworks.setLabelFor(jListFrameworks);
        jLabelFrameworks.setText(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_UsedFrameworks")); // NOI18N

        jScrollPane1.setViewportView(jListFrameworks);
        jListFrameworks.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "ACS_Frameworks_FrameworksList_A11YDesc")); // NOI18N

        jButtonAdd.setMnemonic(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_CustomizerFrameworks_AddButton_LabelMnemonic").charAt(0));
        jButtonAdd.setText(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_AddFramework")); // NOI18N
        jButtonAdd.setActionCommand("Add...");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jPanelConfig.setLayout(new java.awt.GridBagLayout());

        jLabelConfig.setLabelFor(jPanelConfig);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonAdd))
            .add(layout.createSequentialGroup()
                .add(jLabelFrameworks)
                .addContainerGap())
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(jLabelConfig)
                .addContainerGap())
            .add(jPanelConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelFrameworks)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jButtonAdd)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 253, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(9, 9, 9)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelConfig)
                .add(18, 18, 18)
                .add(jPanelConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 171, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jButtonAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "ACS_Frameworks_AddButton_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        AddFrameworkPanel panel = new AddFrameworkPanel(usedFrameworks);
        javax.swing.JPanel inner = new javax.swing.JPanel();
        inner.setLayout(new java.awt.GridBagLayout());
        inner.getAccessibleContext().setAccessibleDescription(panel.getAccessibleContext().getAccessibleDescription());
        inner.getAccessibleContext().setAccessibleName(panel.getAccessibleContext().getAccessibleName());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        inner.add(panel, gridBagConstraints);
 
        DialogDescriptor desc = new DialogDescriptor(inner, NbBundle.getMessage(CustomizerFrameworks.class, "LBL_SelectWebExtension_DialogTitle")); //NOI18N
        Object res = DialogDisplayer.getDefault().notify(desc);
        if (res.equals(NotifyDescriptor.YES_OPTION)) {
            newFrameworks.addAll(panel.getSelectedFrameworks());
            if (newFrameworks != null) {
                uiProperties.setNewFrameworks(newFrameworks);
                for(int i = 0; i < newFrameworks.size(); i++) {
                    WebFrameworkProvider framework = (WebFrameworkProvider) newFrameworks.get(i);
		    if (!((DefaultListModel) jListFrameworks.getModel()).contains(framework.getName()))
			((DefaultListModel) jListFrameworks.getModel()).addElement(framework.getName());
		    
		    if (usedFrameworks.size() == 0)
			usedFrameworks.add(framework);
		    else
			for (int j = 0; j < usedFrameworks.size(); j++)
			    if (!((WebFrameworkProvider) usedFrameworks.get(j)).getName().equals(framework.getName())) {
				usedFrameworks.add(framework);
				break;
			    }
		    
                    jListFrameworks.setSelectedValue(framework.getName(), true);
                }
            }
        }
        
        if (WebFrameworkSupport.getFrameworkProviders().size() == jListFrameworks.getModel().getSize())
            jButtonAdd.setEnabled(false);
    }//GEN-LAST:event_jButtonAddActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JLabel jLabelFrameworks;
    private javax.swing.JList jListFrameworks;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerFrameworks.class);
    }

    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        String frameworkName = (String) jListFrameworks.getSelectedValue();
	int selectedIndex = jListFrameworks.getSelectedIndex();
	if (selectedIndex != -1) {	
	    WebFrameworkProvider framework = (WebFrameworkProvider) usedFrameworks.get(selectedIndex);
	    WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
	    if (framework.getName().equals(frameworkName))
		if (framework.getConfigurationPanel(wm) != null) {
		    String message = MessageFormat.format(NbBundle.getMessage(CustomizerFrameworks.class, "LBL_FrameworkConfiguration"), new Object[] {frameworkName}); //NOI18N
		    jLabelConfig.setText(message);
		    jPanelConfig.removeAll();

		    java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		    gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
		    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		    gridBagConstraints.weightx = 1.0;
		    gridBagConstraints.weighty = 1.0;

                    FrameworkConfigurationPanel frameworkConfigurationPanel = framework.getConfigurationPanel(wm);
                    frameworkConfigurationPanel.addChangeListener(new FrameworkConfigurationPanelListener(frameworkConfigurationPanel));
		    jPanelConfig.add(frameworkConfigurationPanel.getComponent(), gridBagConstraints);
		    jPanelConfig.revalidate();
		} else {
		    hideConfigPanel();
		}
	} else
	    hideConfigPanel();
    }
    
    // #109426
    private final class FrameworkConfigurationPanelListener implements ChangeListener {
        private final FrameworkConfigurationPanel frameworkConfigurationPanel;
        private final WizardDescriptor wizardDescriptor;

        public FrameworkConfigurationPanelListener(FrameworkConfigurationPanel frameworkConfigurationPanel) {
            this.frameworkConfigurationPanel = frameworkConfigurationPanel;
            
            // ugly, i know...
            WizardDescriptor.Panel<Void>[] wizardPanels = null;
            wizardDescriptor = new WizardDescriptor(wizardPanels, null);
            String j2eeVersion = (String)uiProperties.get(WebProjectProperties.J2EE_PLATFORM);
            String serverInstanceID = (String)uiProperties.get(WebProjectProperties.J2EE_SERVER_INSTANCE);
            wizardDescriptor.putProperty("j2eeLevel", j2eeVersion);
            wizardDescriptor.putProperty("serverInstanceID", serverInstanceID);
            frameworkConfigurationPanel.readSettings(wizardDescriptor);
        }

        public void stateChanged(ChangeEvent e) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", null); // NOI18N
            if (frameworkConfigurationPanel.isValid()) {
                if (!category.isValid()) {
                    category.setValid(true);
                    category.setErrorMessage(null);
                }
            } else {
                String errorMessage = (String) wizardDescriptor.getProperty("WizardPanel_errorMessage"); // NOI18N
                category.setValid(false);
                category.setErrorMessage(errorMessage);
            }
        }
    }
    
    private void hideConfigPanel() {
	jLabelConfig.setText(""); //NOI18N
	jPanelConfig.removeAll();
	jPanelConfig.repaint();
	jPanelConfig.revalidate();
    }
}
