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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.RemoteConfiguration;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.environment.PhpEnvironment.DocumentRoot;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.SourcesFolderNameProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class RunConfigurationPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    static final String VALID = "valid"; // NOI18N // used in the previous step while validating sources - copy-folder
    static final String RUN_AS = PhpProjectProperties.RUN_AS; // this property is used in RunAsPanel... yeah, ugly
    static final String URL = "url"; // NOI18N
    static final String COPY_SRC_FILES = "copySrcFiles"; // NOI18N
    static final String COPY_SRC_TARGET = "copySrcTarget"; // NOI18N
    static final String COPY_SRC_TARGETS = "copySrcTargets"; // NOI18N
    static final String REMOTE_CONNECTION = "remoteConnection"; // NOI18N
    static final String REMOTE_DIRECTORY = "remoteDirectory"; // NOI18N
    static final String REMOTE_UPLOAD = "remoteUpload"; // NOI18N

    static final String[] CFG_PROPS = new String[] {
        RUN_AS,
        URL,
        REMOTE_CONNECTION,
        REMOTE_DIRECTORY,
        REMOTE_UPLOAD,
    };

    private final String[] steps;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final SourcesFolderNameProvider sourcesFolderNameProvider;
    private WizardDescriptor descriptor = null;

    private ConfigManager.ConfigProvider configProvider;
    private ConfigManager configManager;

    private RunConfigurationPanelVisual runConfigurationPanelVisual = null;
    private RunAsLocalWeb runAsLocalWeb = null;
    private RunAsRemoteWeb runAsRemoteWeb = null;
    private RunAsScript runAsScript = null;
    private String defaultLocalUrl = null;
    private boolean copyToFolderValid = false;

    public RunConfigurationPanel(String[] steps, SourcesFolderNameProvider sourcesFolderNameProvider) {
        this.sourcesFolderNameProvider = sourcesFolderNameProvider;
        this.steps = steps;
    }

    String[] getSteps() {
        return steps;
    }

    public Component getComponent() {
        if (runConfigurationPanelVisual == null) {
            configProvider = new WizardConfigProvider();
            configManager = new ConfigManager(configProvider);

            runAsLocalWeb = new RunAsLocalWeb(configManager, sourcesFolderNameProvider);
            runAsRemoteWeb = new RunAsRemoteWeb(configManager);
            runAsScript = new RunAsScript(configManager);
            RunAsPanel.InsidePanel[] insidePanels = new RunAsPanel.InsidePanel[] {
                runAsLocalWeb,
                runAsRemoteWeb,
                runAsScript,
            };
            runConfigurationPanelVisual = new RunConfigurationPanelVisual(this, sourcesFolderNameProvider, configManager, insidePanels);

            // listen to the changes in php interpreter
            PhpOptions.getInstance().addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (PhpOptions.PROP_PHP_INTERPRETER.equals(evt.getPropertyName())) {
                        runAsScript.loadPhpInterpreter();
                    }
                }
            });
        }
        return runConfigurationPanelVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(RunConfigurationPanel.class.getName());
    }

    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        // we don't want to get events now
        removeListeners();

        adjustUrl();
        runAsLocalWeb.setLocalServerModel(getLocalServerModel());
        runAsLocalWeb.setCopyFiles(getCopyFiles());

        // register back to receive events
        addListeners();
        fireChangeEvent();
    }

    public void storeSettings(WizardDescriptor settings) {
        getComponent();
        // first remove all the properties
        for (String s : CFG_PROPS) {
            settings.putProperty(s, null);
        }
        // and put only the valid ones
        RunAsType runAs = getRunAsType();
        settings.putProperty(RUN_AS, runAs);
        settings.putProperty(COPY_SRC_FILES, runAsLocalWeb.isCopyFiles());
        settings.putProperty(COPY_SRC_TARGET, runAsLocalWeb.getLocalServer());
        settings.putProperty(COPY_SRC_TARGETS, runAsLocalWeb.getLocalServerModel());

        switch (runAs) {
            case LOCAL:
                storeRunAsLocalWeb(settings);
                break;
            case REMOTE:
                storeRunAsRemoteWeb(settings);
                break;
            case SCRIPT:
                // nothing to store
                break;
            default:
                assert false : "Unhandled RunAsType type: " + runAs;
                break;
        }
    }

    private MutableComboBoxModel getLocalServerModel() {
        MutableComboBoxModel model = (MutableComboBoxModel) descriptor.getProperty(COPY_SRC_TARGETS);
        if (model != null) {
            return model;
        }

        List<DocumentRoot> copyToFolderRoots = PhpEnvironment.get().getDocumentRoots();
        int size = copyToFolderRoots.size();
        List<LocalServer> localServers = new ArrayList<LocalServer>(size);
        for (DocumentRoot root : copyToFolderRoots) {
            String srcRoot = new File(root.getDocumentRoot(), sourcesFolderNameProvider.getSourcesFolderName()).getAbsolutePath();
            LocalServer ls = new LocalServer(null, root.getUrl(), root.getDocumentRoot(), srcRoot, true);
            localServers.add(ls);
        }

        return new LocalServer.ComboBoxModel(localServers.toArray(new LocalServer[size]));
    }

    private boolean getCopyFiles() {
        Boolean copyFiles = (Boolean) descriptor.getProperty(COPY_SRC_FILES);
        if (copyFiles != null) {
            return copyFiles;
        }
        return false;
    }

    private void storeRunAsLocalWeb(WizardDescriptor settings) {
        settings.putProperty(URL, runAsLocalWeb.getUrl());
    }

    private void storeRunAsRemoteWeb(WizardDescriptor settings) {
        settings.putProperty(URL, runAsRemoteWeb.getUrl());
        settings.putProperty(REMOTE_CONNECTION, runAsRemoteWeb.getRemoteConfiguration());
        settings.putProperty(REMOTE_DIRECTORY, runAsRemoteWeb.getUploadDirectory());
        settings.putProperty(REMOTE_UPLOAD, runAsRemoteWeb.getUploadFiles());
    }

    public boolean isValid() {
        getComponent();
        descriptor.putProperty("WizardPanel_errorMessage", " "); // NOI18N
        String error = null;
        switch (getRunAsType()) {
            case LOCAL:
                error = validateRunAsLocalWeb();
                break;
            case REMOTE:
                error = validateRunAsRemoteWeb();
                break;
            case SCRIPT:
                error = validateRunAsScript();
                break;
            default:
                assert false : "Unhandled RunAsType type: " + getRunAsType();
                break;
        }
        if (error != null) {
            descriptor.putProperty("WizardPanel_errorMessage", error); // NOI18N
            descriptor.putProperty(VALID, false);
            return false;
        }
        descriptor.putProperty(VALID, true);
        return true;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public boolean isFinishPanel() {
        return false;
    }

    private void adjustUrl() {
        if (getRunAsType() != RunAsType.LOCAL) {
            // only local url is adjusted
            return;
        }
        String currentUrl = runAsLocalWeb.getUrl();
        if (defaultLocalUrl == null) {
            defaultLocalUrl = currentUrl;
        }
        if (!defaultLocalUrl.equals(currentUrl)) {
            return;
        }
        LocalServer sources = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        assert sources != null;
        String url = null;
        if (runAsLocalWeb.isCopyFiles()) {
            if (!copyToFolderValid) {
                // error exactly in the copy-to-folder field => do nothing
                return;
            }
            LocalServer ls = runAsLocalWeb.getLocalServer();
            String documentRoot = ls.getDocumentRoot();
            assert documentRoot != null;
            String srcRoot = ls.getSrcRoot();
            String urlSuffix = getUrlSuffix(documentRoot, srcRoot);
            if (urlSuffix == null) {
                // user changed path to a different place => use the name of the directory
                urlSuffix = new File(srcRoot).getName();
            }
            String urlPrefix = ls.getUrl() != null ? ls.getUrl() : "http://localhost/"; // NOI18N
            url = urlPrefix + urlSuffix;
        } else if (ConfigureProjectPanel.isProjectFolder(sources)) {
            // project/web => check project name and url
            String correctUrl = getDefaultUrl();
            if (!defaultLocalUrl.equals(correctUrl)) {
                url = correctUrl;
            }
        } else {
            // /var/www or similar => check source folder name and url
            String srcRoot = sources.getSrcRoot();
            @SuppressWarnings("unchecked")
            List<DocumentRoot> srcRoots = (List<DocumentRoot>) descriptor.getProperty(ConfigureProjectPanel.ROOTS);
            assert srcRoots != null;
            for (DocumentRoot root : srcRoots) {
                String urlSuffix = getUrlSuffix(root.getDocumentRoot(), srcRoot);
                if (urlSuffix != null) {
                    url = root.getUrl() + urlSuffix;
                    break;
                }
            }
            if (url == null) {
                // not found => get the name of the sources
                url = "http://localhost/" + new File(sources.getSrcRoot()).getName(); // NOI18N
            }
        }
        // we have to do it here because we need correct url BEFORE the following comparison [!defaultLocalUrl.equals(url)]
        if (url != null && !url.endsWith("/")) { // NOI18N
            url += "/"; // NOI18N
        }
        if (url != null && !defaultLocalUrl.equals(url)) {
            defaultLocalUrl = url;
            runAsLocalWeb.setUrl(url);
        }
    }

    private String getUrlSuffix(String documentRoot, String srcRoot) {
        if (!documentRoot.endsWith(File.separator)) {
            documentRoot += File.separator;
        }
        if (!srcRoot.startsWith(documentRoot)) {
            return null;
        }
        // handle situations like: /var/www///// or c:\\apache\htdocs\aaa\bbb
        srcRoot = srcRoot.replaceAll(Pattern.quote(File.separator) + "+", "/");
        return srcRoot.substring(documentRoot.length());
    }

    private String getDefaultUrl() {
        return "http://localhost/" + sourcesFolderNameProvider.getSourcesFolderName() // NOI18N
                + "/" + ConfigureProjectPanel.DEFAULT_SOURCES_FOLDER + "/"; // NOI18N
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
        adjustUrl();
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    private void addListeners() {
        runAsLocalWeb.addRunAsLocalWebListener(this);
        runAsRemoteWeb.addRunAsRemoteWebListener(this);
        runAsScript.addRunAsScriptListener(this);
    }

    private void removeListeners() {
        runAsLocalWeb.removeRunAsLocalWebListener(this);
        runAsRemoteWeb.removeRunAsRemoteWebListener(this);
        runAsScript.removeRunAsScriptListener(this);
    }

    private PhpProjectProperties.RunAsType getRunAsType() {
        String activeConfig = configProvider.getActiveConfig();
        String runAs = configManager.configurationFor(activeConfig).getValue(RUN_AS);
        if (runAs == null) {
            return PhpProjectProperties.RunAsType.LOCAL;
        }
        return PhpProjectProperties.RunAsType.valueOf(runAs);
    }

    private String validateRunAsLocalWeb() {
        String error = RunAsValidator.validateWebFields(runAsLocalWeb.getUrl(), null, null);
        if (error != null) {
            return error;
        }
        error = validateServerLocation();
        if (error != null) {
            return error;
        }
        return null;
    }

    private String validateRunAsRemoteWeb() {
        String error = RunAsValidator.validateWebFields(runAsRemoteWeb.getUrl(), null, null);
        if (error != null) {
            return error;
        }

        RemoteConfiguration selected = runAsRemoteWeb.getRemoteConfiguration();
        assert selected != null;
        if (selected == RunAsRemoteWeb.NO_REMOTE_CONFIGURATION) {
            return NbBundle.getMessage(RunAsRemoteWeb.class, "MSG_NoConfigurationSelected");
        }
        return null;
    }

    private String validateRunAsScript() {
        return RunAsValidator.validateScriptFields(runAsScript.getPhpInterpreter(), null, null);
    }

    private String validateServerLocation() {
        copyToFolderValid = false;
        if (!runAsLocalWeb.isCopyFiles()) {
            return null;
        }

        LocalServer copyTarget = runAsLocalWeb.getLocalServer();
        String sourcesLocation = copyTarget.getSrcRoot();
        File sources = FileUtil.normalizeFile(new File(sourcesLocation));
        if (sourcesLocation == null
                || !Utils.isValidFileName(sources)) {
            return NbBundle.getMessage(RunConfigurationPanel.class, "MSG_IllegalFolderName");
        }

        String err = Utils.validateProjectDirectory(sourcesLocation, "Folder", false, true); // NOI18N
        if (err != null) {
            return err;
        }
        err = validateSourcesAndCopyTarget();
        if (err != null) {
            return err;
        }
        // warn about visibility of source folder
        String url = runAsLocalWeb.getUrl();
        String warning = NbBundle.getMessage(RunConfigurationPanel.class, "MSG_TargetFolderVisible", url);
        descriptor.putProperty("WizardPanel_errorMessage", warning); // NOI18N
        copyToFolderValid = true;
        return null;
    }

    // #131023
    private String validateSourcesAndCopyTarget() {
        LocalServer sources = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        assert sources != null;
        String sourcesSrcRoot = sources.getSrcRoot();
        if (ConfigureProjectPanel.isProjectFolder(sources)) {
            File projectFolder = (File) descriptor.getProperty(ConfigureProjectPanel.PROJECT_DIR);
            String projectName = (String) descriptor.getProperty(ConfigureProjectPanel.PROJECT_NAME);
            assert projectFolder != null;
            assert projectName != null;
            File project = new File(projectFolder, projectName);
            File src = FileUtil.normalizeFile(new File(project, ConfigureProjectPanel.DEFAULT_SOURCES_FOLDER));
            sourcesSrcRoot = src.getAbsolutePath();
        }
        File normalized = FileUtil.normalizeFile(new File(runAsLocalWeb.getLocalServer().getSrcRoot()));
        String copyTarget = normalized.getAbsolutePath();
        return Utils.validateSourcesAndCopyTarget(sourcesSrcRoot, copyTarget);
    }

    private class WizardConfigProvider implements ConfigManager.ConfigProvider {
        final Map<String, Map<String, String>> configs;

        public WizardConfigProvider() {
            configs = ConfigManager.createEmptyConfigs();
            // we will be using the default configuration (=> no bold labels)
            configs.put(null, new HashMap<String, String>());
        }

        public String[] getConfigProperties() {
            return CFG_PROPS;
        }

        public Map<String, Map<String, String>> getConfigs() {
            return configs;
        }

        public String getActiveConfig() {
            return null;
        }

        public void setActiveConfig(String configName) {
        }
    }
}
