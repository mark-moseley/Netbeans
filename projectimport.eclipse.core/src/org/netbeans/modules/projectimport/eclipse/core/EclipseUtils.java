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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.openide.filesystems.FileUtil;

/**
 * Serveral helpers for parsing, managing, loading Eclipse projects and
 * workspace metadata.
 *
 * @author mkrauskopf
 */
public class EclipseUtils {

    /**
     * Returns whether there is a valid project in the given
     * <code>projectDir</code>.
     */
    public static boolean isRegularProject(String projectDir) {
        return projectDir != null &&
                isRegularProject(new File(projectDir.trim()));
    }
    
    /**
     * Returns whether there is a valid project in the given
     * <code>projectDir</code>.
     */
    public static boolean isRegularProject(File projectDir) {
        return projectDir != null
                && FileUtil.toFileObject(FileUtil.normalizeFile(projectDir)) != null
                && projectDir.isDirectory()
                && new File(projectDir, EclipseProject.PROJECT_FILE).isFile();
    }
    
    /**
     * Returns whether there is a valid workspace in the given
     * <code>workspaceDir</code>.
     */
    public static boolean isRegularWorkSpace(File workspaceDir) {
        assert workspaceDir == null || workspaceDir.equals(FileUtil.normalizeFile(workspaceDir)) : "#137407 problem: " + workspaceDir + " vs. " + FileUtil.normalizeFile(workspaceDir);
        return workspaceDir != null
                && FileUtil.toFileObject(workspaceDir) != null
                && workspaceDir.isDirectory()
                && new File(workspaceDir, Workspace.CORE_PREFERENCE).isFile()
                && new File(workspaceDir, Workspace.LAUNCHING_PREFERENCES).isFile()
                && new File(workspaceDir, Workspace.RESOURCE_PROJECTS_DIR).isDirectory();
    }
    
    private static final String TMP_NAME =
            "NB___TMP___ENOUGH___UNIQUE___CONSTANT___"; // NOI18N
    
    public static boolean isWritable(String projectDestination) {
        File tmpDir = new File(projectDestination.trim(),
                (TMP_NAME + System.currentTimeMillis()));
        if (tmpDir.mkdirs()) {
            tmpDir.delete();
            return true;
        }
        return false;
    }
    
    /**
     * Load properties from a given <code>file</code>.
     * <p>
     * <strong>Note: package private for unit tests only.</strong>
     * 
     * @throws IOException when reading file failed
     */
    static Properties loadProperties(File file) throws IOException {
        InputStream propsIS = new BufferedInputStream(new FileInputStream(file));
        Properties properties = new Properties();
        try {
            properties.load(propsIS);
        } finally {
            propsIS.close();
        }
        return properties;
    }
    
    /**
     * Splits Eclipse variable into variable and path, eg. MAVEN/commons/1.jar
     * is split into MAVEN and /commons/1.jar.
     */
    public static String[] splitVariable(String v) {
        int i = v.replace('\\', '/').indexOf('/');
        if (i == -1) {
            i = v.length();
        }
        return new String[]{v.substring(0, i), v.substring(i)};
    }        

    /**
     * Splits Eclipse internal jar reference into project name and path wihtin project, 
     * eg. /some-project/commons/1.jar is split into some-project and /commons/1.jar.
     */
    public static String[] splitProject(String v) {
        assert v.startsWith("/") : v;
        int i = v.replace('\\', '/').indexOf('/', 1);
        if (i == -1) {
            i = v.length();
        }
        return new String[]{v.substring(1, i), v.substring(i)};
    }        

}
