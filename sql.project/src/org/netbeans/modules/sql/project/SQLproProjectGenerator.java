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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sql.project;

import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;

/**
 * Create a fresh EjbProject from scratch or by importing and exisitng web module 
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class SQLproProjectGenerator {
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_BPELASA_FOLDER = "bpelasa"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    
    private static final String DEFAULT_NBPROJECT_DIR = "nbproject"; //NOI18N
    
    private SQLproProjectGenerator() {}

    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String j2eeLevel) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null /*UNC*/&& rootF.getParentFile().exists()) {
            rootF = rootF.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject (rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject (dir);

        // vlv # 113228
        if (fo == null) {
          throw new IOException("Can't create " + dir.getName());
        }
        assert fo.isFolder() : "Not really a dir: " + dir;
        assert fo.getChildren().length == 0 : "Dir must have been empty: " + dir;
        AntProjectHelper h = setupProject (fo, name, j2eeLevel);
        FileObject srcRoot = fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N
        FileObject bpelasaRoot = srcRoot;
        FileObject sqlmapFile = FileUtil.copyFile(FileUtil.getConfigFile("org-netbeans-modules-sql-project/connectivityInfo.xml"), bpelasaRoot, "connectivityInfo"); //NOI18N

        FileObject nbProjectRoot = FileUtil.toFileObject(new File(dir, DEFAULT_NBPROJECT_DIR)); // NOI18N
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put (IcanproProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(IcanproProjectProperties.META_INF, "${"+IcanproProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
        ep.setProperty(IcanproProjectProperties.SRC_DIR, "${"+IcanproProjectProperties.SOURCE_ROOT+"}"); //NOI18N
        ep.setProperty(IcanproProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory ());
        ProjectManager.getDefault().saveProject(p);

        return h;
    }
    
    private static AntProjectHelper setupProject (FileObject dirFO, String name, String j2eeLevel) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, SQLproProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(SQLproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(SQLproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // ep.setProperty(IcanproProjectProperties.JAVAC_CLASSPATH, "${libs.j2ee14.classpath}");
        ep.setProperty(IcanproProjectProperties.DIST_DIR, "dist");
        ep.setProperty(IcanproProjectProperties.DIST_JAR, "${"+IcanproProjectProperties.DIST_DIR+"}/" + name + ".zip");
        ep.setProperty(IcanproProjectProperties.J2EE_PLATFORM, j2eeLevel);
        ep.setProperty(IcanproProjectProperties.JAR_NAME, name + ".jar");
        ep.setProperty(IcanproProjectProperties.JAR_COMPRESS, "false");
//        ep.setProperty(IcanproProjectProperties.JAR_CONTENT_ADDITIONAL, "");
        
        Deployment deployment = Deployment.getDefault ();
        String serverInstanceID = deployment.getDefaultServerInstanceID ();
        if(serverInstanceID != null) {
            ep.setProperty(IcanproProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID (serverInstanceID));
        }
        ep.setProperty(IcanproProjectProperties.JAVAC_SOURCE, "1.4");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEBUG, "true");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEPRECATION, "false");
        
        ep.setProperty(IcanproProjectProperties.JAVAC_TARGET, "1.4");
        
        ep.setProperty(IcanproProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(IcanproProjectProperties.BUILD_GENERATED_DIR, "${"+IcanproProjectProperties.BUILD_DIR+"}/generated");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_DIR, "${"+IcanproProjectProperties.BUILD_DIR+"}/jar");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(IcanproProjectProperties.DIST_JAVADOC_DIR, "${"+IcanproProjectProperties.DIST_DIR+"}/javadoc");
        ep.setProperty(IcanproProjectProperties.JAVA_PLATFORM, "default_platform");
        ep.setProperty(IcanproProjectProperties.DEBUG_CLASSPATH, "${"+IcanproProjectProperties.JAVAC_CLASSPATH+"}:${"+IcanproProjectProperties.BUILD_CLASSES_DIR+"}");
		Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(IcanproProjectProperties.SOURCE_ENCODING, enc.name());

        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_SETYPE_PREFIX, "sun-sql-engine"); // NOI18N
        ep.setProperty(IcanproProjectProperties.ASSEMBLY_UNIT_ALIAS, "This Assembly Unit"); // NOI18N
        ep.setProperty(IcanproProjectProperties.ASSEMBLY_UNIT_DESCRIPTION, "Represents this Assembly Unit"); // NOI18N
        ep.setProperty(IcanproProjectProperties.APPLICATION_SUB_ASSEMBLY_ALIAS, "This Application Sub-Assembly"); // NOI18N
        ep.setProperty(IcanproProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION, NbBundle.getMessage (SQLproProjectGenerator.class, "APP_SUB_ASSEMBLY")); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_COMPONENT_CONF_ROOT, "nbproject/private"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_DEPLOYMENT_CONF_ROOT, "nbproject/deployment"); // NOI18N

        ep.setProperty(IcanproProjectProperties.BC_DEPLOYMENT_JAR, "${"+IcanproProjectProperties.BUILD_DIR+"}/" + "BCDeployment.jar");
        ep.setProperty(IcanproProjectProperties.SE_DEPLOYMENT_JAR, "${"+IcanproProjectProperties.BUILD_DIR+"}/" + "SEDeployment.jar");
        //============= End of IcanPro========================================//

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        if(serverInstanceID != null) {
            ep.setProperty(IcanproProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        }
        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_COMPONENT_CONF_FILE, "ComponentInformation.xml"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_DEPLOYMENT_CONF_FILE, "default.xml"); // NOI18N
        //============= End of IcanPro========================================//

        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }

}
