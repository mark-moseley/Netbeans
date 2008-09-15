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
import org.netbeans.modules.php.project.util.PhpInterpreter;
import java.io.File;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Helper class for getting <b>all</b> the properties of a PHP project.
 * <p>
 * <b>This class is the preferred way to get PHP project properties.</b>
 * <p>
 * The most common method {@link #getRunAs(org.netbeans.modules.php.project.PhpProject) getRunAs(project)} will show the customizer
 * if the property is unknown (this behaviour can be suppressed,
 * see {@link #getRunAs(org.netbeans.modules.php.project.PhpProject, boolean) getRunAs(project, boolean)}).
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

    public static FileObject getWebRootDirectory(PhpProject project) {
        String webRootPath = project.getEvaluator().getProperty(PhpProjectProperties.WEB_ROOT);
        FileObject webRoot = project.getSourcesDirectory();
        if (webRootPath != null && webRootPath.trim().length() > 0 && !webRootPath.equals(".")) { // NOI18N
            webRoot = project.getSourcesDirectory().getFileObject(webRootPath);
        }
        assert webRoot != null : "WebRoot must be found";
        return webRoot;
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
        return getBoolean(project, PhpProjectProperties.SHORT_TAGS, org.netbeans.modules.php.project.api.PhpOptions.SHORT_TAGS_ENABLED);
    }

    public static boolean areAspTagsEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.ASP_TAGS, org.netbeans.modules.php.project.api.PhpOptions.ASP_TAGS_ENABLED);
    }

    /**
     * Will show the customizer if the property is unknown.
     * @return run as type or <code>null</code>.
     */
    public static PhpProjectProperties.RunAsType getRunAs(PhpProject project) {
        return getRunAs(project, true);
    }

    /**
     * Will show the customizer if the property is unknown and <code>showCustomizer</code> equals <code>true</code>.
     * @return run as type or <code>null</code>.
     */
    public static PhpProjectProperties.RunAsType getRunAs(PhpProject project, boolean showCustomizer) {
        PhpProjectProperties.RunAsType runAsType = null;
        String runAs = project.getEvaluator().getProperty(PhpProjectProperties.RUN_AS);
        if (runAs == null) {
            // show customizer?
            if (showCustomizer) {
                project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(CompositePanelProviderImpl.RUN);
            }
            return null;
        }
        try {
            runAsType = PhpProjectProperties.RunAsType.valueOf(runAs);
        } catch (IllegalArgumentException iae) {
            // ignored
        }
        return runAsType;
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
        boolean retval = defaultValue;
        String boolValue = project.getEvaluator().getProperty(property);
        if (boolValue != null && boolValue.trim().length() > 0) {
            retval = Boolean.parseBoolean(boolValue);
        }
        return retval;
    }
}
