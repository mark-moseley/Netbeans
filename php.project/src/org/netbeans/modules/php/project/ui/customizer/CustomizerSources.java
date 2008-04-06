/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.CopyFilesVisual;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.LocalServerController;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.Utils.EncodingModel;
import org.netbeans.modules.php.project.ui.Utils.EncodingRenderer;
import org.netbeans.modules.php.project.ui.WebFolderNameProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CustomizerSources extends JPanel implements WebFolderNameProvider {
    private static final long serialVersionUID = -5803489817914071L;

    final PhpProjectProperties properties;
    final PropertyEvaluator evaluator;
    private final LocalServerController localServerController;
    private final CopyFilesVisual copyFilesVisual;

    public CustomizerSources(final Category category, final PhpProjectProperties properties) {
        initComponents();

        this.properties = properties;
        evaluator = properties.getProject().getEvaluator();

        initEncoding();
        LocalServer sources = initSources();
        boolean copyFiles = initCopyFiles();
        LocalServer copyTarget = initCopyTarget();
        initUrl();

        localServerController = LocalServerController.create(localServerComboBox, localServerButton,
                NbBundle.getMessage(CustomizerSources.class, "LBL_SelectSourceFolderTitle"), sources);
        localServerController.selectLocalServer(sources);

        copyFilesVisual = new CopyFilesVisual(this, copyTarget);
        copyFilesVisual.setCopyFiles(copyFiles);
        copyFilesPanel.add(BorderLayout.NORTH, copyFilesVisual);

        encodingComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Charset enc = (Charset) encodingComboBox.getSelectedItem();
                String encName;
                if (enc == null) {
                    return;
                }
                encName = enc.name();
                properties.setEncoding(encName);
            }
        });
        localServerController.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                validateFields(category);
            }
        });
        copyFilesVisual.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                validateFields(category);
            }
        });
        urlTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateFields(category);
            }
            public void removeUpdate(DocumentEvent e) {
                validateFields(category);
            }
            public void changedUpdate(DocumentEvent e) {
                validateFields(category);
            }
        });

        // check init values
        validateFields(category);
    }

    private void initEncoding() {
        encodingComboBox.setRenderer(new EncodingRenderer());
        encodingComboBox.setModel(new EncodingModel(evaluator.evaluate(properties.getEncoding())));
    }

    private LocalServer initSources() {
        PhpProject project = properties.getProject();

        // load project path
        FileObject projectFolder = project.getProjectDirectory();
        String projectPath = FileUtil.getFileDisplayName(projectFolder);
        projectFolderTextField.setText(projectPath);

        // sources
        String src = evaluator.evaluate(properties.getSrcDir());
        File resolvedFile = PropertyUtils.resolveFile(FileUtil.toFile(projectFolder), src);
        FileObject resolvedFO = FileUtil.toFileObject(resolvedFile);
        if (resolvedFO == null) {
            // src directory doesn't exist?!
            return new LocalServer(resolvedFile.getAbsolutePath());
        }
        return new LocalServer(FileUtil.getFileDisplayName(resolvedFO));
    }

    private boolean initCopyFiles() {
        return Boolean.valueOf(evaluator.evaluate(properties.getCopySrcFiles()));
    }

    private LocalServer initCopyTarget() {
        // copy target, if any
        String copyTarget = evaluator.evaluate(properties.getCopySrcTarget());
        if (copyTarget == null || copyTarget.length() == 0) {
            return new LocalServer(""); // NOI18N
        }
        File resolvedFile = FileUtil.normalizeFile(new File(copyTarget));
        FileObject resolvedFO = FileUtil.toFileObject(resolvedFile);
        if (resolvedFO == null) {
            // target directory doesn't exist?!
            return new LocalServer(resolvedFile.getAbsolutePath());
        }
        return new LocalServer(FileUtil.getFileDisplayName(resolvedFO));
    }

    private void initUrl() {
        urlTextField.setText(properties.getUrl());
    }

    public String getWebFolderName() {
        return new File(projectFolderTextField.getText()).getName();
    }

    void validateFields(Category category) {
        category.setErrorMessage(null);
        category.setValid(true);

        // sources
        String err = LocalServerController.validateLocalServer(localServerController.getLocalServer(), "Sources", true); // NOI18N
        if (err != null) {
            category.setErrorMessage(err);
            category.setValid(false);
            return;
        }

        // copy files
        File srcDir = getSrcDir();
        File copyTargetDir = getCopyTargetDir();
        boolean isCopyFiles = copyFilesVisual.isCopyFiles();
        if (isCopyFiles) {
            err = LocalServerController.validateLocalServer(copyFilesVisual.getLocalServer(), "Folder", true); // NOI18N
            if (err != null) {
                category.setErrorMessage(err);
                category.setValid(false);
                return;
            }
            // #131023
            err = Utils.validateSourcesAndCopyTarget(srcDir.getAbsolutePath(), copyTargetDir.getAbsolutePath());
            if (err != null) {
                category.setErrorMessage(err);
                category.setValid(false);
                return;
            }
        }

        // url
        String url = urlTextField.getText();
        if (!Utils.isValidUrl(url)) {
            err = NbBundle.getMessage(CustomizerSources.class, "MSG_InvalidUrl");
            category.setErrorMessage(err);
            category.setValid(false);
            return;
        }

        // everything ok
        File projectDirectory = FileUtil.toFile(properties.getProject().getProjectDirectory());
        String srcPath = PropertyUtils.relativizeFile(projectDirectory, srcDir);
        if (srcPath.startsWith("../")) { // NOI18N
            // relative path, change to absolute
            srcPath = srcDir.getAbsolutePath();
        }
        properties.setSrcDir(srcPath);
        properties.setCopySrcFiles(String.valueOf(isCopyFiles));
        properties.setCopySrcTarget(copyTargetDir.getAbsolutePath());
        properties.setUrl(url);
    }

    private File getSrcDir() {
        LocalServer localServer = localServerController.getLocalServer();
        return FileUtil.normalizeFile(new File(localServer.getSrcRoot()));
    }

    private File getCopyTargetDir() {
        LocalServer localServer = copyFilesVisual.getLocalServer();
        return FileUtil.normalizeFile(new File(localServer.getSrcRoot()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectFolderLabel = new javax.swing.JLabel();
        projectFolderTextField = new javax.swing.JTextField();
        sourceFolderLabel = new javax.swing.JLabel();
        encodingLabel = new javax.swing.JLabel();
        encodingComboBox = new javax.swing.JComboBox();
        copyFilesPanel = new javax.swing.JPanel();
        urlLabel = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();
        localServerComboBox = new javax.swing.JComboBox();
        localServerButton = new javax.swing.JButton();

        projectFolderLabel.setLabelFor(projectFolderTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, bundle.getString("LBL_ProjectFolder")); // NOI18N

        projectFolderTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(sourceFolderLabel, bundle.getString("LBL_SourceFolder")); // NOI18N

        encodingLabel.setLabelFor(encodingComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Encoding")); // NOI18N

        copyFilesPanel.setLayout(new java.awt.BorderLayout());

        urlLabel.setLabelFor(urlTextField);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_ProjectUrl")); // NOI18N

        localServerComboBox.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(localServerButton, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Browse")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(sourceFolderLabel)
                            .add(urlLabel)
                            .add(encodingLabel))
                        .add(13, 13, 13)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(urlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                            .add(encodingComboBox, 0, 261, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(localServerComboBox, 0, 166, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(localServerButton))))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(copyFilesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(projectFolderLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(projectFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectFolderLabel)
                    .add(projectFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourceFolderLabel)
                    .add(localServerButton)
                    .add(localServerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(copyFilesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(urlLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(encodingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(encodingLabel))
                .addContainerGap(24, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel copyFilesPanel;
    private javax.swing.JComboBox encodingComboBox;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JButton localServerButton;
    private javax.swing.JComboBox localServerComboBox;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JTextField projectFolderTextField;
    private javax.swing.JLabel sourceFolderLabel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
