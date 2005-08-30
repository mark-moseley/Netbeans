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
package org.netbeans.modules.apisupport.project.ui.wizard.project;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Wizard for creating new project templates - selection of project to become template.
 *
 * @author Milos Kleint
 */
final class SelectProjectPanel extends BasicWizardIterator.Panel {
    
    private NewProjectIterator.DataModel data;
    private final ComboWrapper EMPTY = new ComboWrapper(NbBundle.getMessage(getClass(),"MSG_No_Projects"));
    
    /** Creates new form SelectProjectPanel */
    public SelectProjectPanel(WizardDescriptor setting, NewProjectIterator.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        putClientProperty("NewFileWizard_Title", getMessage("LBL_ProjectWizardTitle"));
        loadComboBox();
        comProject.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkValidity();
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblProject = new javax.swing.JLabel();
        comProject = new javax.swing.JComboBox();
        btnProject = new javax.swing.JButton();
        pnlHeightAdjuster = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        lblProject.setLabelFor(comProject);
        org.openide.awt.Mnemonics.setLocalizedText(lblProject, org.openide.util.NbBundle.getMessage(SelectProjectPanel.class, "LBL_Project_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(lblProject, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(comProject, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnProject, org.openide.util.NbBundle.getMessage(SelectProjectPanel.class, "LBL_Browse"));
        btnProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProjectActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(btnProject, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.1;
        add(pnlHeightAdjuster, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void btnProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProjectActionPerformed
        JFileChooser chooser = ProjectChooser.projectChooser();
        int res = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
        if (res == JFileChooser.APPROVE_OPTION) {
            File fil = chooser.getSelectedFile();
            FileObject fo = FileUtil.toFileObject(fil);
            if (fo != null) {
                try {
                    Project p = ProjectManager.getDefault().findProject(fo);
                    DefaultComboBoxModel model = (DefaultComboBoxModel)comProject.getModel();
                    ComboWrapper wrapper = new ComboWrapper(p);
                    model.addElement(wrapper);
                    model.setSelectedItem(wrapper);
                    if (EMPTY == model.getElementAt(0)) {
                        model.removeElement(EMPTY);
                    }
                } catch (IOException exc) {
                    ErrorManager.getDefault().notify(exc);
                }
            }
        }
    }//GEN-LAST:event_btnProjectActionPerformed
    protected void storeToDataModel() {
        ComboWrapper wrapper = (ComboWrapper)comProject.getSelectedItem();
        data.setTemplate(wrapper.getProject());
    }
    
    protected void readFromDataModel() {
        checkValidity();
    }
    
    private void loadComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Project[] prjs = OpenProjects.getDefault().getOpenProjects();
        if (prjs.length > 0) {
            for (int i = 0; i < prjs.length; i++) {
                if (prjs[i] != data.getProject()) {
                    // ignore the currently active project..
                    model.addElement(new ComboWrapper(prjs[i]));
                }
            }
        }
        if (model.getSize() == 0) {
            model.addElement(EMPTY);
        }
        comProject.setModel(model);
    }
    
    
    private void checkValidity() {
        ComboWrapper sel = (ComboWrapper)comProject.getModel().getSelectedItem();
        if (sel == EMPTY) {
            setErrorMessage(getMessage("MSG_NoProjectSelected"));
            return;
        }
        Sources srcs = (Sources)sel.getProject().getLookup().lookup(Sources.class);
        if (srcs.getSourceGroups(Sources.TYPE_GENERIC).length > 1) {
            setErrorMessage(getMessage("MSG_NoExternalRoots")); 
            return;
        }
        setErrorMessage(null);
    }
    
    protected String getPanelName() {
        return getMessage("LBL_ProjectSelection_Title");
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(SelectProjectPanel.class);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnProject;
    private javax.swing.JComboBox comProject;
    private javax.swing.JLabel lblProject;
    private javax.swing.JPanel pnlHeightAdjuster;
    // End of variables declaration//GEN-END:variables
    
    private class ComboWrapper {
        private Project proj;
        private String text;
        ComboWrapper(String text) {
            this.text = text;
        }
         
        ComboWrapper(Project project) {
            proj = project;
        }
        
        public Project getProject() {
            return proj;
        }
        
        public String toString() {
            if (text != null) {
                return text;
            }
            ProjectInformation inf = (ProjectInformation)proj.getLookup().lookup(ProjectInformation.class);
            return inf.getDisplayName();
        }
    }
    
}
