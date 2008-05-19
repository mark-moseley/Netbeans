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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.php.project.ui.customizer;

import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.ui.IncludePathUiSupport;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.classpath.IncludePathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik, Radek Matous
 */
public class PhpProjectProperties implements ConfigManager.ConfigProvider {

    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String COMMAND_PATH = "command.path"; // NOI18N
    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N
    public static final String COPY_SRC_FILES = "copy.src.files"; // NOI18N
    public static final String COPY_SRC_TARGET = "copy.src.target"; // NOI18N
    public static final String URL = "url"; // NOI18N
    public static final String INDEX_FILE = "index.file"; // NOI18N
    public static final String INCLUDE_PATH = "include.path"; // NOI18N
    public static final String GLOBAL_INCLUDE_PATH = "php.global.include.path"; // NOI18N
    public static final String ARGS = "script.arguments"; // NOI18N
    public static final String RUN_AS = "run.as"; // NOI18N
    public static final String REMOTE_CONNECTION = "remote.connection"; // NOI18N
    public static final String REMOTE_DIRECTORY = "remote.directory"; // NOI18N
    public static final String REMOTE_UPLOAD = "remote.upload"; // NOI18N

    public static final String[] CFG_PROPS = new String[] {
        URL,
        INDEX_FILE,
        ARGS,
        RUN_AS,
        REMOTE_CONNECTION,
        REMOTE_DIRECTORY,
        REMOTE_UPLOAD
    };

    public static enum RunAsType {
        LOCAL,
        SCRIPT,
        REMOTE
    }

    public static enum UploadFiles {
        MANUALLY ("LBL_UploadFilesManually", "TXT_UploadFilesManually"), // NOI18N
        ON_RUN ("LBL_UploadFilesOnRun", "TXT_UploadFilesOnRun"), // NOI18N
        ON_SAVE ("LBL_UploadFilesOnSave", "TXT_UploadFilesOnSave"); // NOI18N

        private final String label;
        private final String description;

        UploadFiles(String labelKey, String descriptionKey) {
            label = NbBundle.getMessage(PhpProjectProperties.class, labelKey);
            description = NbBundle.getMessage(PhpProjectProperties.class, descriptionKey);
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
          return description;
        }
    }

    static final String CONFIG_PRIVATE_PROPERTIES_PATH = "nbproject/private/config.properties"; // NOI18N

    private final PhpProject project;
    private final IncludePathSupport includePathSupport;

    // all these fields don't have to be volatile - this ensures request processor
    // CustomizerSources
    private String srcDir;
    private String copySrcFiles;
    private String copySrcTarget;
    private String url;
    private String indexFile;
    private String encoding;

    // CustomizerRun
    Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> runConfigs;
    String activeConfig;

    // CustomizerPhpIncludePath
    private DefaultListModel includePathListModel = null;
    private ListCellRenderer includePathListRenderer = null;

    public PhpProjectProperties(PhpProject project, IncludePathSupport includePathSupport) {
        assert project != null;
        assert includePathSupport != null;

        this.project = project;
        this.includePathSupport = includePathSupport;
        runConfigs = readRunConfigs();
        activeConfig = project.getEvaluator().getProperty("config"); // NOI18N
    }

    public String[] getConfigProperties() {
        return CFG_PROPS;
    }

    public Map<String, Map<String, String>> getConfigs() {
        return runConfigs;
    }

    public String getActiveConfig() {
        return activeConfig;
    }

    public void setActiveConfig(String configName) {
        activeConfig = configName;
    }

    public String getCopySrcFiles() {
        if (copySrcFiles == null) {
            copySrcFiles = project.getEvaluator().getProperty(COPY_SRC_FILES);
        }
        return copySrcFiles;
    }

    public void setCopySrcFiles(String copySrcFiles) {
        this.copySrcFiles = copySrcFiles;
    }

    public String getCopySrcTarget() {
        if (copySrcTarget == null) {
            copySrcTarget = project.getEvaluator().getProperty(COPY_SRC_TARGET);
        }
        return copySrcTarget;
    }

    public void setCopySrcTarget(String copySrcTarget) {
        this.copySrcTarget = copySrcTarget;
    }

    public String getEncoding() {
        if (encoding == null) {
            encoding = project.getEvaluator().getProperty(SOURCE_ENCODING);
        }
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getSrcDir() {
        if (srcDir == null) {
            srcDir = project.getEvaluator().getProperty(SRC_DIR);
        }
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    public String getUrl() {
        if (url == null) {
            url = project.getEvaluator().getProperty(URL);
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIndexFile() {
        if (indexFile == null) {
            indexFile = project.getEvaluator().getProperty(INDEX_FILE);
        }
        return indexFile;
    }

    public void setIndexFile(String indexFile) {
        this.indexFile = indexFile;
    }

    public DefaultListModel getIncludePathListModel() {
        if (includePathListModel == null) {
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            includePathListModel = IncludePathUiSupport.createListModel(includePathSupport.itemsIterator(
                    properties.getProperty(INCLUDE_PATH)));
        }
        return includePathListModel;
    }

    public ListCellRenderer getIncludePathListRenderer() {
        if (includePathListRenderer == null) {
            includePathListRenderer = new IncludePathUiSupport.ClassPathListCellRenderer(project.getEvaluator(),
                project.getProjectDirectory());
        }
        return includePathListRenderer;
    }

    public void save() {
        try {
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    saveProperties();
                    return null;
                }
            });
            ProjectManager.getDefault().saveProject(project);
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void saveProperties() throws IOException {
        AntProjectHelper helper = project.getHelper();

        // encode include path
        String[] includePath = null;
        if (includePathListModel != null) {
            includePath = includePathSupport.encodeToStrings(IncludePathUiSupport.getIterator(includePathListModel));
        }

        // get properties
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privateProperties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

        // sources
        if (srcDir != null) {
            projectProperties.setProperty(SRC_DIR, srcDir);
        }
        if (copySrcFiles != null) {
            projectProperties.setProperty(COPY_SRC_FILES, copySrcFiles);
        }
        if (copySrcTarget != null) {
            projectProperties.setProperty(COPY_SRC_TARGET, copySrcTarget);
        }
        if (encoding != null) {
            projectProperties.setProperty(SOURCE_ENCODING, encoding);
        }

        // php include path
        if (includePath != null) {
            projectProperties.setProperty(INCLUDE_PATH, includePath);
        }

        // configs
        storeRunConfigs(runConfigs, projectProperties, privateProperties);
        EditableProperties ep = helper.getProperties(CONFIG_PRIVATE_PROPERTIES_PATH);
        if (activeConfig == null) {
            ep.remove("config"); // NOI18N
        } else {
            ep.setProperty("config", activeConfig); // NOI18N
        }

        // store all the properties
        helper.putProperties(CONFIG_PRIVATE_PROPERTIES_PATH, ep);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

        // additional changes
        // encoding
        if (encoding != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(encoding));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }

        // check whether src directory exists - if not, create it (can happen using customizer)
        FileObject srcDirectory = null;
        File srcFolder = helper.resolveFile(srcDir);
        if (srcDir != null) {
            if (!srcFolder.exists()) {
                srcDirectory = FileUtil.createFolder(srcFolder);
            }
        }

        // UI log
        if (srcDirectory == null) {
            srcDirectory = FileUtil.toFileObject(srcFolder);
        }
        logUI(helper.getProjectDirectory(), srcDirectory, Boolean.valueOf(getCopySrcFiles()));
    }

    // http://wiki.netbeans.org/UILoggingInPHP
    private void logUI(FileObject projectDir, FileObject sourceDir, Boolean copyFiles) {
        LogRecord logRecord = new LogRecord(Level.INFO, "UI_PHP_PROJECT_CUSTOMIZED"); //NOI18N
        logRecord.setLoggerName(PhpProject.UI_LOGGER_NAME);
        logRecord.setResourceBundle(NbBundle.getBundle(PhpProjectProperties.class));
        logRecord.setParameters(new Object[] {
            FileUtil.isParentOf(projectDir, sourceDir),
            copyFiles != null && copyFiles
        });
        Logger.getLogger(PhpProject.UI_LOGGER_NAME).log(logRecord);
    }

    public PhpProject getProject() {
        return project;
    }

    /**
     * A mess.
     */
    Map<String/*|null*/, Map<String, String>> readRunConfigs() {
        Map<String, Map<String, String>> m = new TreeMap<String, Map<String, String>>(new Comparator<String>() {

            public int compare(String s1, String s2) {
                return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
            }
        });
        Map<String, String> def = new TreeMap<String, String>();
        EditableProperties privateProperties = getProject().getHelper().getProperties(
                AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        EditableProperties projectProperties = getProject().getHelper().getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        for (String prop : CFG_PROPS) {
            String v = privateProperties.getProperty(prop);
            if (v == null) {
                v = projectProperties.getProperty(prop);
            }
            if (v != null) {
                def.put(prop, v);
            }
        }
        m.put(null, def);
        FileObject configs = project.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                String path = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                m.put(kid.getName(), new TreeMap<String, String>(getProject().getHelper().getProperties(path)));
            }
        }
        configs = project.getProjectDirectory().getFileObject("nbproject/private/configs"); // NOI18N
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                Map<String, String> c = m.get(kid.getName());
                if (c == null) {
                    continue;
                }
                String path = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                c.putAll(new HashMap<String, String>(getProject().getHelper().getProperties(path)));
            }
        }
        //System.err.println("readRunConfigs: " + m);
        return m;
    }

    /**
     * A royal mess.
     */
    void storeRunConfigs(Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> configs,
            EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        //System.err.println("storeRunConfigs: " + configs);
        Map<String, String> def = configs.get(null);
        for (String prop : CFG_PROPS) {
            String v = def.get(prop);
            EditableProperties ep = prop.equals(ARGS) ? privateProperties : projectProperties;
            if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                if (v != null && v.length() > 0) {
                    ep.setProperty(prop, v);
                } else {
                    ep.remove(prop);
                }
            }
        }
        for (Map.Entry<String, Map<String, String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config == null) {
                continue;
            }
            String sharedPath = "nbproject/configs/" + config + ".properties"; // NOI18N

            String privatePath = "nbproject/private/configs/" + config + ".properties"; // NOI18N

            Map<String, String> c = entry.getValue();
            if (c == null) {
                getProject().getHelper().putProperties(sharedPath, null);
                getProject().getHelper().putProperties(privatePath, null);
                continue;
            }
            for (Map.Entry<String, String> entry2 : c.entrySet()) {
                String prop = entry2.getKey();
                String v = entry2.getValue();
                String path = (prop.equals(ARGS)) ? privatePath : sharedPath;
                EditableProperties ep = getProject().getHelper().getProperties(path);
                if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                    if (v != null && (v.length() > 0 || (def.get(prop) != null && def.get(prop).length() > 0))) {
                        ep.setProperty(prop, v);
                    } else {
                        ep.remove(prop);
                    }
                    getProject().getHelper().putProperties(path, ep);
                }
            }
            // Make sure the definition file is always created, even if it is empty.
            getProject().getHelper().putProperties(sharedPath, getProject().getHelper().getProperties(sharedPath));
        }
    }
}
