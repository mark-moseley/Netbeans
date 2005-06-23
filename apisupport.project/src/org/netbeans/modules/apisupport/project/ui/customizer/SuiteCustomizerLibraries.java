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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Represents <em>Libraries</em> panel in Suite customizer.
 *
 * @author Martin Krauskopf
 */
public class SuiteCustomizerLibraries extends JPanel
        implements ComponentFactory.StoragePanel {
    
    private SuiteProperties suiteProps;
    
    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerLibraries(final SuiteProperties suiteProps) {
        this.suiteProps = suiteProps;
        initComponents();
        platformValue.setSelectedItem(suiteProps.getActivePlatform());
        moduleList.setModel(suiteProps.getModulesListModel());
        moduleList.setCellRenderer(ComponentFactory.getModuleCellRenderer());
        moduleList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateEnabled();
                }
            }
        });
        jLabel1.setForeground(UIManager.getColor("nb.errorForeground"));
    }
    
    private void updateEnabled() {
        boolean enabled = moduleList.getSelectedIndex() != -1;
        removeModuleButton.setEnabled(enabled);
    }
    
    public void store() {
        suiteProps.setActivePlatform((NbPlatform) platformValue.getSelectedItem());
    }
    
    private ComponentFactory.SuiteSubModulesListModel getModuleListModel() {
        return (ComponentFactory.SuiteSubModulesListModel) moduleList.getModel();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        platformPanel = new javax.swing.JPanel();
        platformValue = org.netbeans.modules.apisupport.project.ui.platform.ComponentFactory.getNbPlatformsComboxBox();
        platform = new javax.swing.JLabel();
        managePlafsButton = new javax.swing.JButton();
        moduleLabel = new javax.swing.JLabel();
        modulesSP = new javax.swing.JScrollPane();
        moduleList = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        addModuleButton = new javax.swing.JButton();
        removeModuleButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        platformPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformPanel.add(platformValue, gridBagConstraints);

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformPanel.add(platform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(managePlafsButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "CTL_ManagePlatform"));
        managePlafsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatforms(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        platformPanel.add(managePlafsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(platformPanel, gridBagConstraints);

        moduleLabel.setLabelFor(moduleList);
        org.openide.awt.Mnemonics.setLocalizedText(moduleLabel, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_Modules"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 2, 0);
        add(moduleLabel, gridBagConstraints);

        modulesSP.setViewportView(moduleList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(modulesSP, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridLayout(2, 1, 0, 6));

        org.openide.awt.Mnemonics.setLocalizedText(addModuleButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "CTL_AddButton"));
        addModuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModule(evt);
            }
        });

        buttonPanel.add(addModuleButton);

        org.openide.awt.Mnemonics.setLocalizedText(removeModuleButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "CTL_RemoveButton"));
        removeModuleButton.setEnabled(false);
        removeModuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeModule(evt);
            }
        });

        buttonPanel.add(removeModuleButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(buttonPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "<html>Please be aware that Modules Manipulation is under a heavy development<br> and it is not really in a stable state yet. So use only at your own risk. Thanks.</html> ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jLabel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void removeModule(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModule
        getModuleListModel().removeModules(Arrays.asList(moduleList.getSelectedValues()));
        if (moduleList.getModel().getSize() > 0) {
            moduleList.setSelectedIndex(0);
        }
        moduleList.requestFocus();
    }//GEN-LAST:event_removeModule
    
    private void addModule(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModule
        JFileChooser chooser = ProjectChooser.projectChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File projectDir = chooser.getSelectedFile();
            UIUtil.setProjectChooserDirParent(projectDir);
            Project project;
            try {
                project = ProjectManager.getDefault().findProject(
                        FileUtil.toFileObject(projectDir));
                if (project == null) {
                    return;
                }
                if (getModuleListModel().contains(project)) {
                    moduleList.setSelectedValue(project, true);
                    return;
                }
                NbModuleTypeProvider nmtp = (NbModuleTypeProvider) project.
                        getLookup().lookup(NbModuleTypeProvider.class);
                if (nmtp == null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(SuiteCustomizerLibraries.class,
                            "MSG_TryingToAddNonNBModule", // NOI18N
                            ProjectUtils.getInformation(project).getDisplayName())));
                } else if (nmtp.getModuleType() == NbModuleTypeProvider.SUITE_COMPONENT) {
                    Object[] params = new Object[] {
                        ProjectUtils.getInformation(project).getDisplayName(),
                        getSuiteProjectName(project),
                        getSuiteProjectDirectory(project),
                        suiteProps.getProjectDisplayName(),
                    };
                    NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(SuiteCustomizerLibraries.class,
                            "MSG_MoveFromSuiteToSuite", params), // NOI18N
                            NotifyDescriptor.OK_CANCEL_OPTION);
                    DialogDisplayer.getDefault().notify(confirmation);
                    if (confirmation.getValue() == NotifyDescriptor.OK_OPTION) {
                        getModuleListModel().addModule(project);
                    }
                } else if (nmtp.getModuleType() == NbModuleTypeProvider.STANDALONE) {
                    getModuleListModel().addModule(project);
                } else if (nmtp.getModuleType() == NbModuleTypeProvider.NETBEANS_ORG) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(SuiteCustomizerLibraries.class,
                            "MSG_TryingToAddNBORGModule", // NOI18N
                            ProjectUtils.getInformation(project).getDisplayName())));
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }//GEN-LAST:event_addModule
    
    private File getSuiteDirectory(Project suiteComp) {
        SuiteProvider sp = (SuiteProvider) suiteComp.
                getLookup().lookup(SuiteProvider.class);
        assert sp != null;
        return sp.getSuiteDirectory();
    }
    
    private String getSuiteProjectDirectory(Project suiteComp) {
        return getSuiteDirectory(suiteComp).getAbsolutePath();
    }
    
    private String getSuiteProjectName(Project suiteComp) {
        return Util.getDisplayName(FileUtil.toFileObject(getSuiteDirectory(suiteComp)));
    }
    
    private void managePlatforms(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatforms
        NbPlatformCustomizer.showCustomizer();
        platformValue.setModel(new org.netbeans.modules.apisupport.project.ui.platform.ComponentFactory.NbPlatformListModel()); // refresh
        platformValue.setSelectedItem(suiteProps.getActivePlatform());
        platformValue.requestFocus();
    }//GEN-LAST:event_managePlatforms
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addModuleButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton managePlafsButton;
    private javax.swing.JLabel moduleLabel;
    private javax.swing.JList moduleList;
    private javax.swing.JScrollPane modulesSP;
    private javax.swing.JLabel platform;
    private javax.swing.JPanel platformPanel;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JButton removeModuleButton;
    // End of variables declaration//GEN-END:variables
    
}
