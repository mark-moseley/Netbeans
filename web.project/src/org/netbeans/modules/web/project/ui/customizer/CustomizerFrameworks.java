/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;
import java.awt.Component;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebFrameworkSupport;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.wizards.PanelSupportedFrameworksVisual;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;

public class CustomizerFrameworks extends javax.swing.JPanel implements HelpCtx.Provider, ListSelectionListener {
    
    List usedFrameworks = new LinkedList();
    private WebProject project;
    private WebProjectProperties uiProperties;
    
    /** Creates new form CustomizerFrameworks */
    public CustomizerFrameworks(WebProjectProperties uiProperties) {
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
        jLabelConfig = new javax.swing.JLabel();
        jPanelConfig = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabelFrameworks.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_CustomizerFrameworks_ListMnemonic").charAt(0));
        jLabelFrameworks.setLabelFor(jListFrameworks);
        jLabelFrameworks.setText(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_UsedFrameworks"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelFrameworks, gridBagConstraints);

        jScrollPane1.setMaximumSize(new java.awt.Dimension(32767, 70));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(22, 70));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(259, 70));
        jScrollPane1.setViewportView(jListFrameworks);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(jScrollPane1, gridBagConstraints);

        jButtonAdd.setMnemonic(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_CustomizerFrameworks_AddButton_LabelMnemonic").charAt(0));
        jButtonAdd.setText(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_AddFramework"));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAdd, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jSeparator1, gridBagConstraints);

        jLabelConfig.setLabelFor(jPanelConfig);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelConfig, gridBagConstraints);

        jPanelConfig.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanelConfig, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        PanelSupportedFrameworksVisual panel = new PanelSupportedFrameworksVisual(null, project, PanelSupportedFrameworksVisual.UNUSED_FRAMEWORKS, usedFrameworks);
        Component[] components = panel.getConfigComponents();
        for (int i = 0; i < components.length; i++)
            components[i].setVisible(false);
        javax.swing.JPanel inner = new javax.swing.JPanel();
        inner.setLayout(new java.awt.GridBagLayout());
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
            List newFrameworks = panel.getSelectedFrameworks();
            if (newFrameworks != null) {
                uiProperties.setNewFrameworks(newFrameworks);
                for(int i = 0; i < newFrameworks.size(); i++) {
                    WebFrameworkProvider framework = (WebFrameworkProvider) newFrameworks.get(i);
                    ((DefaultListModel) jListFrameworks.getModel()).addElement(framework.getName());
                    usedFrameworks.add(framework);
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
        WebFrameworkProvider framework = (WebFrameworkProvider) usedFrameworks.get(jListFrameworks.getSelectedIndex());
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
                
                jPanelConfig.add(framework.getConfigurationPanel(wm).getComponent(), gridBagConstraints);
                jPanelConfig.revalidate();
            } else {
                jLabelConfig.setText(""); //NOI18N
                jPanelConfig.removeAll();
                jPanelConfig.repaint();
                jPanelConfig.revalidate();
            }
    }

}
