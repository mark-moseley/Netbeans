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

package org.netbeans.modules.spring.beans.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.beans.ui.customizer.ConfigFileGroupUIs.FileDisplayName;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Andrei Badea
 */
public class ConfigFileDetectPanel extends javax.swing.JPanel {

    private final RequestProcessor rp = new RequestProcessor("Spring config file detection thread", 1, true); // NOI18N
    private final Set<File> alreadySelectedFiles;
    private final Project project;

    private List<File> availableFiles;
    private DialogDescriptor descriptor;
    private Task detectTask;

    /**
     * Creates a new instance of the panel for a project and a set of already selected
     * files. The panel will run a background task to detect any config files in the given project.
     */
    public static ConfigFileDetectPanel create(Project project, Set<File> alreadySelectedFiles, FileDisplayName fileDisplayName) {
        return new ConfigFileDetectPanel(project, alreadySelectedFiles, fileDisplayName);
    }

    /**
     * Creates a new instance of the panel for a set of available and already selected
     * files. Since the available files are known, no config files detection
     * task will be run.
     */
    public static ConfigFileDetectPanel create(List<File> availableFiles, Set<File> alreadySelectedFiles, FileDisplayName fileDisplayName) {
        return new ConfigFileDetectPanel(availableFiles, alreadySelectedFiles, fileDisplayName);
    }

    private ConfigFileDetectPanel(List<File> availableFiles, Set<File> alreadySelectedFiles, FileDisplayName fileDisplayName) {
        this.alreadySelectedFiles = alreadySelectedFiles;
        this.availableFiles = availableFiles;
        this.project = null;
        initComponents(fileDisplayName);
    }

    private ConfigFileDetectPanel(Project project, Set<File> alreadySelectedFiles, FileDisplayName fileDisplayName) {
        this.project = project;
        this.alreadySelectedFiles = alreadySelectedFiles;
        initComponents(fileDisplayName);
    }

    private void initComponents(FileDisplayName fileDisplayName) {
        initComponents();
        ConfigFileGroupUIs.setupConfigFileSelectionTable(configFileTable, fileDisplayName);
        configFileTable.getParent().setBackground(configFileTable.getBackground());
        configFileTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public boolean open() {
        String title = NbBundle.getMessage(ConfigFileDetectPanel.class, "LBL_ConfigFiles");
        descriptor = new DialogDescriptor(this, title, true, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelDetection();
            }
        });
        if (availableFiles == null) {
            // No available files, will run the detection task.
            descriptor.setValid(false);
            configFileTable.setEnabled(true);
            progressBar.setIndeterminate(true);
            detectTask = rp.create(new FileDetector());
            rp.post(detectTask);
        } else {
            updateAvailableFiles(availableFiles);
        }
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == DialogDescriptor.OK_OPTION;
    }

    public List<File> getAvailableFiles() {
        return availableFiles;
    }

    public List<File> getSelectedFiles() {
        return ConfigFileGroupUIs.getSelectedFiles(configFileTable);
    }

    private void cancelDetection() {
        if (detectTask != null) {
            detectTask.cancel();
        }
    }

    private void updateAvailableFiles(List<File> availableFiles) {
        this.availableFiles = availableFiles;
        configFileTable.setEnabled(true);
        ConfigFileGroupUIs.connect(availableFiles, alreadySelectedFiles, configFileTable);
        configFileTable.getColumnModel().getColumn(0).setMaxWidth(0);
        // In an attempt to hide the progress bar and label, but force
        // the occupy the same space.
        String message = (availableFiles.size() == 0) ? NbBundle.getMessage(ConfigFileDetectPanel.class, "LBL_NoFilesFound") : " "; // NOI18N
        messageLabel.setText(message); // NOI18N
        progressBar.setIndeterminate(false);
        progressBar.setBorderPainted(false);
        progressBar.setBackground(getBackground());
        descriptor.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        detectedFilesLabel = new javax.swing.JLabel();
        configFileScrollPane = new javax.swing.JScrollPane();
        configFileTable = new javax.swing.JTable();
        progressBar = new javax.swing.JProgressBar();
        messageLabel = new javax.swing.JLabel();

        detectedFilesLabel.setText(org.openide.util.NbBundle.getMessage(ConfigFileDetectPanel.class, "LBL_DetectedFiles")); // NOI18N

        configFileTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        configFileTable.setShowHorizontalLines(false);
        configFileTable.setShowVerticalLines(false);
        configFileTable.setTableHeader(null);
        configFileScrollPane.setViewportView(configFileTable);

        progressBar.setString(" "); // NOI18N
        progressBar.setStringPainted(true);

        messageLabel.setText(org.openide.util.NbBundle.getMessage(ConfigFileDetectPanel.class, "LBL_PleaseWait")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, configFileScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, detectedFilesLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, messageLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(detectedFilesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configFileScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane configFileScrollPane;
    private javax.swing.JTable configFileTable;
    private javax.swing.JLabel detectedFilesLabel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

    private final class FileDetector implements Runnable {

        public void run() {
            final Set<File> result = new HashSet<File>();
            // Search in the source groups of the projects.
            for (SourceGroup group : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                for (FileObject fo : group.getRootFolder().getChildren()) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    if (!SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                        continue;
                    }
                    File file = FileUtil.toFile(fo);
                    if (file == null) {
                        continue;
                    }
                    result.add(file);
                }
            }
            // Search any providers of Spring config files registered in the project lookup.
            for (SpringConfigFileProvider provider : project.getLookup().lookupAll(SpringConfigFileProvider.class)) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                result.addAll(provider.getConfigFiles());
            }
            final List<File> sorted = new ArrayList<File>(result.size());
            sorted.addAll(result);
            Collections.sort(sorted);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateAvailableFiles(sorted);
                }
            });
        }
    }
}
