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

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.LoggerFactory;
import org.netbeans.modules.projectimport.ProjectImporterException;

/**
 * Able to load and fill up an <code>EclipseProject</code> from Eclipse project
 * directory using a .project and .classpath file and eventually passed
 * workspace. It is also able to load the basic information from workspace.
 *
 * @author mkrauskopf
 */
public final class ProjectFactory {
    
    /** Logger for this class. */
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(ProjectFactory.class);
    
    /** singleton */
    private static ProjectFactory instance = new ProjectFactory();
    
    private ProjectFactory() {/*empty constructor*/}
    
    /** Returns ProjectFactory instance. */
    public static ProjectFactory getInstance() {
        return instance;
    }
    
    /**
     * Loads a project contained in the given <code>projectDir</code> and tries
     * if there is workspace in the parent directory (which works only for
     * eclipse internal projects)
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    public EclipseProject load(File projectDir) throws
            ProjectImporterException {
        Workspace workspace = Workspace.createWorkspace(projectDir.getParentFile());
        if (workspace != null) {
            WorkspaceParser parser = new WorkspaceParser(workspace);
            parser.parse();
        }
        return load(projectDir, workspace);
    }
    
    /**
     * Loads a project contained in the given <code>projectDir</code>.
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    EclipseProject load(File projectDir, Workspace workspace) throws
            ProjectImporterException {
        
        EclipseProject project = EclipseProject.createProject(projectDir);
        if (project != null) {
            project.setWorkspace(workspace);
            load(project);
        }
        return project;
    }
    
    /**
     * Fullfill given <code>project</code> with all needed information.
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    void load(EclipseProject project) throws ProjectImporterException {
        logger.finest("Loading project: " + project.getDirectory().getAbsolutePath()); // NOI18N
        ProjectParser.parse(project);
        File cpFile = project.getClassPathFile();
        // non-java project doesn't need to have a classpath file
        if (cpFile != null && cpFile.exists()) {
            project.setClassPath(ClassPathParser.parse(cpFile));
            for (Iterator it = project.getClassPath().getEntries().iterator(); it.hasNext(); ) {
                project.setAbsolutePathForEntry((ClassPathEntry) it.next());
            }
        } else {
            logger.finer("Project " + project.getName() + // NOI18N
                    " doesn't have java nature."); // NOI18N
            project.setJavaNature(false);
        }
    }
}


