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

package org.netbeans.modules.php.project.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.util.PhpProjectGenerator;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class NewPhpProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    public static enum WizardType {
        NEW,
        EXISTING,
    }

    private final WizardType wizardType;
    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private int index;

    public NewPhpProjectWizardIterator() {
        this(WizardType.NEW);
    }

    private NewPhpProjectWizardIterator(WizardType wizardType) {
        this.wizardType = wizardType;
    }

    public static NewPhpProjectWizardIterator existing() {
        return new NewPhpProjectWizardIterator(WizardType.EXISTING);
    }

    public void initialize(WizardDescriptor wizard) {
        descriptor = wizard;
        index = 0;
        panels = createPanels();
        // normally we would do it in uninitialize but we have listener on ide options (=> NPE)
        initDescriptor(wizard);
    }

    public void uninitialize(WizardDescriptor wizard) {
        Panel<WizardDescriptor> current = current();
        // #158483
        if (current instanceof CancelablePanel) {
            ((CancelablePanel) current).cancel();
        }
        panels = null;
        descriptor = null;
    }

    public Set<FileObject> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();

        PhpProjectGenerator.ProjectProperties projectProperties = new PhpProjectGenerator.ProjectProperties(
                getProjectDirectory(),
                getSources(),
                (String) descriptor.getProperty(ConfigureProjectPanel.PROJECT_NAME),
                getRunAsType(),
                (Charset) descriptor.getProperty(ConfigureProjectPanel.ENCODING),
                getUrl(),
                getIndexFile(),
                descriptor,
                isCopyFiles(),
                getCopySrcTarget(),
                (RemoteConfiguration) descriptor.getProperty(RunConfigurationPanel.REMOTE_CONNECTION),
                (String) descriptor.getProperty(RunConfigurationPanel.REMOTE_DIRECTORY),
                (UploadFiles) descriptor.getProperty(RunConfigurationPanel.REMOTE_UPLOAD));
        AntProjectHelper helper = PhpProjectGenerator.createProject(projectProperties, new ProgressMonitor(handle));
        resultSet.add(helper.getProjectDirectory());

        FileObject sources = FileUtil.toFileObject(projectProperties.getSourcesDirectory());
        resultSet.add(sources);

        FileObject indexFile = sources.getFileObject(projectProperties.getIndexFile());
        if (indexFile != null && indexFile.isValid()) {
            resultSet.add(indexFile);
        }

        return resultSet;
    }

    public String name() {
        return NbBundle.getMessage(NewPhpProjectWizardIterator.class, "LBL_IteratorName", index + 1, panels.length);
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel<WizardDescriptor> current() {
        // #158483
        if (descriptor != null) {
            // wizard title
            String title = NbBundle.getMessage(NewPhpProjectWizardIterator.class, wizardType == WizardType.NEW ? "TXT_PhpProject" : "TXT_ExistingPhpProject");
            descriptor.putProperty("NewProjectWizard_Title", title); // NOI18N
        }
        return panels[index];
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        String[] steps = new String[] {
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_ProjectNameLocation"),
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_RunConfiguration"),
        };

        ConfigureProjectPanel configureProjectPanel = new ConfigureProjectPanel(steps, wizardType);
        @SuppressWarnings("unchecked")
        WizardDescriptor.Panel<WizardDescriptor>[] pnls = new WizardDescriptor.Panel[] {
            configureProjectPanel,
            new RunConfigurationPanel(steps, configureProjectPanel, wizardType),
        };
        return pnls;
    }

    // prevent incorrect default values (empty project => back => existing project)
    private void initDescriptor(WizardDescriptor settings) {
        settings.putProperty(ConfigureProjectPanel.IS_PROJECT_DIR_USED, null);
        settings.putProperty(ConfigureProjectPanel.PROJECT_DIR, null);
        settings.putProperty(ConfigureProjectPanel.PROJECT_NAME, null);
        settings.putProperty(ConfigureProjectPanel.SOURCES_FOLDER, null);
        settings.putProperty(ConfigureProjectPanel.LOCAL_SERVERS, null);
        settings.putProperty(ConfigureProjectPanel.ENCODING, null);
        settings.putProperty(RunConfigurationPanel.RUN_AS, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_FILES, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_TARGET, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_TARGETS, null);
        settings.putProperty(RunConfigurationPanel.URL, null);
        settings.putProperty(RunConfigurationPanel.INDEX_FILE, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_CONNECTION, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_DIRECTORY, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_UPLOAD, null);
    }

    private File getProjectDirectory() {
        if ((Boolean) descriptor.getProperty(ConfigureProjectPanel.IS_PROJECT_DIR_USED)) {
            return (File) descriptor.getProperty(ConfigureProjectPanel.PROJECT_DIR);
        }
        return null;
    }

    private File getSources() {
        LocalServer localServer = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        return new File(localServer.getSrcRoot());
    }

    private RunAsType getRunAsType() {
        return (RunAsType) descriptor.getProperty(RunConfigurationPanel.RUN_AS);
    }

    private String getUrl() {
        String url = (String) descriptor.getProperty(RunConfigurationPanel.URL);
        if (url == null) {
            // #146882
            url = RunConfigurationPanel.getUrlForSources(wizardType, descriptor);
        }
        return url;
    }

    private String getIndexFile() {
        String indexName = (String) descriptor.getProperty(RunConfigurationPanel.INDEX_FILE);
        if (indexName == null) {
            // run configuration panel not shown at all
            indexName = RunConfigurationPanel.DEFAULT_INDEX_FILE;
        }
        return indexName;
    }

    private Boolean isCopyFiles() {
        PhpProjectProperties.RunAsType runAs = getRunAsType();
        if (runAs == null) {
            return null;
        }
        boolean copyFiles = false;
        switch (runAs) {
            case LOCAL:
                Boolean tmp = (Boolean) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_FILES);
                if (tmp != null && tmp) {
                    copyFiles = true;
                }
                break;
            default:
                // noop
                break;
        }
        return copyFiles;
    }

    private File getCopySrcTarget() {
        if (getRunAsType() == null) {
            return null;
        }
        LocalServer localServer = (LocalServer) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_TARGET);
        if (PhpProjectUtils.hasText(localServer.getSrcRoot())) {
            return new File(localServer.getSrcRoot());
        }
        return null;
    }

    private static final class ProgressMonitor implements PhpProjectGenerator.Monitor {
        private final ProgressHandle handle;

        public ProgressMonitor(ProgressHandle handle) {
            assert handle != null;
            this.handle = handle;
        }

        public void starting() {
            handle.start(5);

            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingProject");
            handle.progress(msg, 2);
        }

        public void creatingIndexFile() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingIndexFile");
            handle.progress(msg, 4);
        }

        public void finishing() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_PreparingToOpen");
            handle.progress(msg, 5);
        }
    }
}
