/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory.ProjectHudsonJobCreator;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;
import org.netbeans.modules.hudson.ui.wizard.InstanceDialog;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Visual configuration of {@link CreateJob}.
 */
public class CreateJobPanel extends JPanel implements ChangeListener {

    private Set<String> takenNames;
    private NotificationLineSupport notifications;
    private NotifyDescriptor descriptor;
    private Set<Project> manuallyAddedProjects = new HashSet<Project>();
    ProjectHudsonJobCreator creator;
    HudsonInstance instance;

    CreateJobPanel() {}

    void init(NotifyDescriptor descriptor, HudsonInstance instance) {
        this.descriptor = descriptor;
        this.notifications = descriptor.createNotificationLineSupport();
        initComponents();
        updateServerModel();
        this.instance = instance;
        server.setSelectedItem(instance);
        server.setRenderer(new ServerRenderer());
        updateProjectModel();
        project.setSelectedItem(project.getItemCount() > 0 ? project.getItemAt(0) : null);
        project.setRenderer(new ProjectRenderer());
        name.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                check();
            }
            public void removeUpdate(DocumentEvent e) {
                check();
            }
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    public @Override void addNotify() {
        super.addNotify();
        project.requestFocusInWindow();
        check();
    }

    private void check() {
        descriptor.setValid(false);
        notifications.clearMessages();
        if (instance == null) {
            notifications.setInformationMessage("Pick a Hudson server to create a job on."); // XXX I18N
            return;
        }
        Project p = selectedProject();
        if (p == null) {
            notifications.setInformationMessage("Pick a project to create a job for."); // XXX I18N
            return;
        }
        if (creator == null) {
            notifications.setErrorMessage("The IDE does not know how to set up a job for this project."); // XXX I18N
            return;
        }
        if (takenNames.contains(name())) {
            notifications.setErrorMessage("This name is taken. Pick another."); // XXX I18N
            return;
        }
        String problem = creator.error();
        if (problem != null) {
            notifications.setErrorMessage(problem);
            return;
        }
        if (ProjectHudsonProvider.getDefault().findAssociation(p) != null) {
            notifications.setWarningMessage("This project already seems to be associated with a Hudson job."); // XXX I18N
        }
        descriptor.setValid(true);
    }

    String name() {
        return name.getText();
    }

    Project selectedProject() {
        return (Project) project.getSelectedItem();
    }

    private void updateServerModel() {
        server.setModel(new DefaultComboBoxModel(HudsonManagerImpl.getDefault().getInstances().toArray()));
    }

    private void computeTakenNames() {
        takenNames = new HashSet<String>();
        if (instance != null) {
            for (HudsonJob job : instance.getJobs()) {
                takenNames.add(job.getName());
            }
        }
    }

    private void updateProjectModel() {
        SortedSet<Project> projects = new TreeSet<Project>(new Comparator<Project>() {
            Collator COLL = Collator.getInstance();
            public int compare(Project o1, Project o2) {
                return COLL.compare(ProjectUtils.getInformation(o1).getDisplayName(),
                                    ProjectUtils.getInformation(o2).getDisplayName());
            }
        });
        projects.addAll(Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        projects.addAll(manuallyAddedProjects);
        project.setModel(new DefaultComboBoxModel(projects.toArray(new Project[projects.size()])));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverLabel = new javax.swing.JLabel();
        server = new javax.swing.JComboBox();
        addServer = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        project = new javax.swing.JComboBox();
        browse = new javax.swing.JButton();
        custom = new javax.swing.JPanel();
        explanationLabel = new javax.swing.JLabel();

        serverLabel.setLabelFor(server);
        org.openide.awt.Mnemonics.setLocalizedText(serverLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.serverLabel.text")); // NOI18N

        server.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addServer, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.addServer.text")); // NOI18N
        addServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerActionPerformed(evt);
            }
        });

        nameLabel.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.nameLabel.text")); // NOI18N

        projectLabel.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.projectLabel.text")); // NOI18N

        project.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.browse.text")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        custom.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(explanationLabel, org.openide.util.NbBundle.getMessage(CreateJobPanel.class, "CreateJobPanel.explanationLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(explanationLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(serverLabel)
                            .add(nameLabel)
                            .add(projectLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, project, 0, 278, Short.MAX_VALUE)
                            .add(server, 0, 278, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(addServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(browse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, custom, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverLabel)
                    .add(server, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addServer))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLabel)
                    .add(project, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browse))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(custom, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 241, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(explanationLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.showOpenDialog(this);
        File dir = chooser.getSelectedFile();
        if (dir != null) {
            FileObject d = FileUtil.toFileObject(dir);
            if (d != null) {
                try {
                    Project p = ProjectManager.getDefault().findProject(d);
                    if (p != null) {
                        manuallyAddedProjects.add(p);
                        updateProjectModel();
                        project.setSelectedItem(p);
                    }
                } catch (IOException x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }
    }//GEN-LAST:event_browseActionPerformed

    private void projectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectActionPerformed
        if (creator != null) {
            creator.removeChangeListener(this);
        }
        creator = null;
        Project p = selectedProject();
        if (p == null) {
            check();
            return;
        }
        if (p.getClass().getName().equals("org.netbeans.modules.project.ui.LazyProject")) { // NOI18N
            // XXX ugly but not obvious how better to handle this...
            updateProjectModel();
            project.setSelectedItem(null);
            return;
        }
        for (ProjectHudsonJobCreatorFactory factory : Lookup.getDefault().lookupAll(ProjectHudsonJobCreatorFactory.class)) {
            creator = factory.forProject(p);
            if (creator != null) {
                break;
            }
        }
        if (creator == null) {
            check();
            return;
        }
        name.setText(creator.jobName());
        custom.removeAll();
        custom.add(creator.customizer());
        creator.addChangeListener(this);
        check();
    }//GEN-LAST:event_projectActionPerformed

    private void serverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverActionPerformed
        instance = (HudsonInstance) server.getSelectedItem();
        computeTakenNames();
        check();
    }//GEN-LAST:event_serverActionPerformed

    private void addServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerActionPerformed
        HudsonInstance created = new InstanceDialog().show();
        if (created != null) {
            updateServerModel();
            instance = created;
            server.setSelectedItem(instance);
            check();
        }
    }//GEN-LAST:event_addServerActionPerformed

    public void stateChanged(ChangeEvent event) {
        check();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServer;
    private javax.swing.JButton browse;
    private javax.swing.JPanel custom;
    private javax.swing.JLabel explanationLabel;
    private javax.swing.JTextField name;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox project;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JComboBox server;
    private javax.swing.JLabel serverLabel;
    // End of variables declaration//GEN-END:variables

    private static class ProjectRenderer extends DefaultListCellRenderer {
        public @Override Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            }
            ProjectInformation info = ProjectUtils.getInformation((Project) value);
            JLabel label = (JLabel) super.getListCellRendererComponent(list, info.getDisplayName(), index, isSelected, cellHasFocus);
            label.setIcon(info.getIcon());
            return label;
        }
    }

    private static class ServerRenderer extends DefaultListCellRenderer {
        public @Override Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            }
            return super.getListCellRendererComponent(list, ((HudsonInstance) value).getName(), index, isSelected, cellHasFocus);
        }
    }

}
