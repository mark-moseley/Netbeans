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

package org.netbeans.modules.php.project;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.util.PhpInterpreter;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.api.PhpLanguageOptions;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.ui.BrowseTestSources;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;

/**
 * Helper class for getting <b>all</b> the properties of a PHP project.
 * <p>
 * <b>This class is the preferred way to get PHP project properties.</b>
 * </p>
 * <p>
 * Method {@link #isActiveConfigValid(org.netbeans.modules.php.project.PhpProject, boolean) isActiveConfigValid()}
 * could be called before getting any Run Configuration property. It's possible to show the project properties
 * dialog if the configuration is invalid.
 * @author Tomas Mysik
 */
public final class ProjectPropertiesSupport {

    private ProjectPropertiesSupport() {
    }

    /**
     * <b>This method should not be used, use other methods in this class.</b>
     * <p>
     * Use this method only if you don't want to show customizer automatically
     * or if you understand what you are doing ;)
     * @see #addWeakPropertyEvaluatorListener(org.netbeans.modules.php.project.PhpProject, java.beans.PropertyChangeListener)
     */
    public static PropertyEvaluator getPropertyEvaluator(PhpProject project) {
        return project.getEvaluator();
    }

    public static void addWeakPropertyEvaluatorListener(PhpProject project, PropertyChangeListener listener) {
        project.addWeakPropertyEvaluatorListener(listener);
    }

    public static FileObject getProjectDirectory(PhpProject project) {
        return project.getProjectDirectory();
    }

    public static FileObject getSourcesDirectory(PhpProject project) {
        return project.getSourcesDirectory();
    }

    /**
     * @return test sources directory or <code>null</code> (if not set up yet e.g.)
     */
    public static FileObject getTestDirectory(PhpProject project, boolean showFileChooser) {
        FileObject testsDirectory = project.getTestsDirectory();
        if (testsDirectory != null) {
            return testsDirectory;
        }
        if (showFileChooser) {
            BrowseTestSources panel = new BrowseTestSources(project);
            if (panel.open()) {
                File tests = new File(panel.getTestSources());
                assert tests.isDirectory();
                testsDirectory = FileUtil.toFileObject(tests);
                assert testsDirectory != null && testsDirectory.isValid();
                saveTestSources(project, tests);
            }
        }
        return testsDirectory;
    }

    public static FileObject getWebRootDirectory(PhpProject project) {
        return getSourceSubdirectory(project, project.getEvaluator().getProperty(PhpProjectProperties.WEB_ROOT));
    }

    public static FileObject getSourceSubdirectory(PhpProject project, String subdirectoryPath) {
        FileObject subdirectory = project.getSourcesDirectory();
        if (subdirectoryPath != null && subdirectoryPath.trim().length() > 0 && !subdirectoryPath.equals(".")) { // NOI18N
            subdirectory = subdirectory.getFileObject(subdirectoryPath);
        }
        assert subdirectory != null : "Subdirectory " + subdirectoryPath + " must be found";
        return subdirectory;
    }

    public static PhpInterpreter getPhpInterpreter(PhpProject project) {
        String interpreter = project.getEvaluator().getProperty(PhpProjectProperties.INTERPRETER);
        if (interpreter != null && interpreter.length() > 0) {
            return new PhpInterpreter(interpreter);
        }
        return new PhpInterpreter(PhpOptions.getInstance().getPhpInterpreter());
    }

    public static boolean isCopySourcesEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.COPY_SRC_FILES, false);
    }

    /**
     * @return file or <code>null</code>.
     */
    public static File getCopySourcesTarget(PhpProject project) {
        String targetString = project.getEvaluator().getProperty(PhpProjectProperties.COPY_SRC_TARGET);
        if (targetString != null && targetString.trim().length() > 0) {
            return FileUtil.normalizeFile(new File(targetString));
        }
        return null;
    }

    public static String getEncoding(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.SOURCE_ENCODING);
    }

    public static boolean areShortTagsEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.SHORT_TAGS, PhpLanguageOptions.SHORT_TAGS_ENABLED);
    }

    public static boolean areAspTagsEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.ASP_TAGS, PhpLanguageOptions.ASP_TAGS_ENABLED);
    }

    /** validates the active config and return <code>true</code> if it's OK */
    public static boolean isActiveConfigValid(PhpProject project, boolean indexFileNeeded, boolean showCustomizer) {
        boolean valid = validateActiveConfig(project, indexFileNeeded);
        if (!valid && showCustomizer) {
            project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(CompositePanelProviderImpl.RUN);
        }
        return valid;
    }

    private static boolean validateActiveConfig(PhpProject project, boolean indexFileNeeded) {
        String indexFile = getIndexFile(project);
        if (indexFileNeeded && (indexFile == null || indexFile.trim().length() == 0)) {
            return false;
        }
        RunAsType runAs = getRunAs(project);
        switch (runAs) {
            case LOCAL:
                if (RunAsValidator.validateWebFields(getUrl(project), FileUtil.toFile(getWebRootDirectory(project)),
                        null, getArguments(project)) != null) {
                    return false;
                }
                break;
            case REMOTE:
                String remoteConnection = getRemoteConnection(project);
                if (RunAsValidator.validateWebFields(getUrl(project), FileUtil.toFile(getWebRootDirectory(project)),
                        null, getArguments(project)) != null) {
                    return false;
                } else if (remoteConnection == null || RemoteConnections.get().remoteConfigurationForName(remoteConnection) == null) {
                    return false;
                } else if (RunAsValidator.validateUploadDirectory(getRemoteDirectory(project), true) != null) {
                    return false;
                }
                break;
            case SCRIPT:
                if (RunAsValidator.validateScriptFields(getPhpInterpreter(project).getInterpreter(),
                        FileUtil.toFile(getSourcesDirectory(project)), null, getArguments(project)) != null) {
                    return false;
                }
                break;
            default:
                assert false : "Unknown run configuration type: " + runAs;
        }
        return true;
    }

    /**
     * @return run as type, {@link RunAsType#LOCAL} is the default.
     */
    public static PhpProjectProperties.RunAsType getRunAs(PhpProject project) {
        PhpProjectProperties.RunAsType runAsType = null;
        String runAs = project.getEvaluator().getProperty(PhpProjectProperties.RUN_AS);
        if (runAs != null) {
            try {
                runAsType = PhpProjectProperties.RunAsType.valueOf(runAs);
            } catch (IllegalArgumentException iae) {
                // ignored
            }
        }
        return runAsType != null ? runAsType : PhpProjectProperties.RunAsType.LOCAL;
    }

    /**
     * @return url or <code>null</code>.
     */
    public static String getUrl(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.URL);
    }

    /**
     * @return index file or <code>null</code>.
     */
    public static String getIndexFile(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.INDEX_FILE);
    }

    /**
     * @return arguments or <code>null</code>.
     */
    public static String getArguments(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.ARGS);
    }

    /**
     * @return remote connection (configuration) name or <code>null</code>.
     */
    public static String getRemoteConnection(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_CONNECTION);
    }

    /**
     * @return remote (upload) directory or <code>null</code>.
     */
    public static String getRemoteDirectory(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_DIRECTORY);
    }

    /**
     * @return <code>true</code> if permissions should be preserved; default is <code>false</code>.
     */
    public static boolean areRemotePermissionsPreserved(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.REMOTE_PERMISSIONS, false);
    }

    /**
     * @return remote upload or <code>null</code>.
     */
    public static PhpProjectProperties.UploadFiles getRemoteUpload(PhpProject project) {
        PhpProjectProperties.UploadFiles uploadFiles = null;
        String remoteUpload = project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_UPLOAD);
        assert remoteUpload != null;
        try {
            uploadFiles = PhpProjectProperties.UploadFiles.valueOf(remoteUpload);
        } catch (IllegalArgumentException iae) {
            // ignored
        }
        return uploadFiles;
    }

    private static boolean getBoolean(PhpProject project, String property, boolean defaultValue) {
        String boolValue = project.getEvaluator().getProperty(property);
        if (boolValue != null && boolValue.trim().length() > 0) {
            return Boolean.parseBoolean(boolValue);
        }
        return defaultValue;
    }

    private static void saveTestSources(final PhpProject project, final File testDir) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    // store properties
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        public Void run() throws IOException {
                            AntProjectHelper helper = project.getHelper();

                            // relativize path
                            File projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
                            String testPath = PropertyUtils.relativizeFile(projectDirectory, testDir);
                            if (testPath == null) {
                                // path cannot be relativized => use absolute path (any VCS can be hardly use, of course)
                                testPath = testDir.getAbsolutePath();
                            }

                            EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            projectProperties.put(PhpProjectProperties.TEST_SRC_DIR, testPath);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
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
        });
    }
}
