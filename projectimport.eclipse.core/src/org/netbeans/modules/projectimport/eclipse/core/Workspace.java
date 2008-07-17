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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Provides access to an eclipse workspace.
 *
 * @author mkrauskopf
 */
public final class Workspace {

    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(Workspace.class.getName());
    
    
    /** Represents variable in Eclipse project's classpath. */
    public static class Variable {
        private String name;
        private String location;
        private boolean fileVar;
        private String file;
        
        public Variable(String name, String location) {
            this.name = name;
            this.location = location;
            File f = new File(location);
            fileVar = f.exists() && f.isFile();
            if (fileVar) {
                file = f.getName();
                this.location = f.getParentFile().getAbsolutePath();
            }
        }
        
        String getName() {
            return name;
        }
        
        String getLocation() {
            return location;
        }

        public String getFileName() {
            return file;
        }
        
        @Override
        public String toString() {
            return name + " = " + location;
        }
        
        public boolean isFileVariable() {
            return fileVar;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Variable)) return false;
            final Variable var = (Variable) obj;
            if (name != null ? !name.equals(var.name) :var.name != null)
                return false;
            if (location != null ? !location.equals(var.location) : var.location != null)
                return false;
            return true;
        }
        
        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + System.identityHashCode(name);
            result = 37 * result + System.identityHashCode(location);
            return result;
        }
    }
    
    private static final String RUNTIME_SETTINGS =
            ".metadata/.plugins/org.eclipse.core.runtime/.settings/"; //NOI18N
    static final String CORE_PREFERENCE =
            RUNTIME_SETTINGS + "org.eclipse.jdt.core.prefs"; //NOI18N
    static final String RESOURCES_PREFERENCE =
            RUNTIME_SETTINGS + "org.eclipse.core.resources.prefs"; //NOI18N
    static final String LAUNCHING_PREFERENCES =
            RUNTIME_SETTINGS + "org.eclipse.jdt.launching.prefs"; //NOI18N
    
    static final String RESOURCE_PROJECTS_DIR =
            ".metadata/.plugins/org.eclipse.core.resources/.projects"; //NOI18N
    
    static final String DEFAULT_JRE_CONTAINER =
            "org.eclipse.jdt.launching.JRE_CONTAINER"; //NOI18N
    
    static final String USER_JSF_LIBRARIES =
            ".metadata/.plugins/org.eclipse.jst.jsf.core/JSFLibraryRegistryV2.xml"; //NOI18N
    
    private File corePrefFile;
    private File resourcesPrefFile;
    private File launchingPrefsFile;
    private File resourceProjectsDir;
    private File workspaceDir;
    private File userJSFLibraries;
    
    private Set<Variable> variables = new HashSet<Variable>();
    private Set<Variable> resourcesVariables = new HashSet<Variable>();
    private Set<EclipseProject> projects = new HashSet<EclipseProject>();
    private Map<String,String> jreContainers;
    private Map<String, List<String>> userLibraries;
    
    private boolean myEclipseLibrariesLoaded;
    
    /**
     * Returns <code>Workspace</code> instance representing Eclipse Workspace
     * found in the given <code>workspaceDir</code>. If a workspace is not found
     * in the specified directory, <code>null</code> is returned.
     *
     * @return either a <code>Workspace</code> instance or null if a given
     *      <code>workspaceDir</code> doesn't contain valid Eclipse workspace.
     */
    static Workspace createWorkspace(File workspaceDir) {
        if (!EclipseUtils.isRegularWorkSpace(workspaceDir)) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                    "There is not a regular workspace in " + workspaceDir); //NOI18N
            return null;
        }
        Workspace workspace = new Workspace(workspaceDir);
        return workspace;
    }
    
    /** Sets up a workspace directory. */
    Workspace(File workspaceDir) {
        this.workspaceDir = workspaceDir;
        corePrefFile = new File(workspaceDir, CORE_PREFERENCE);
        resourcesPrefFile = new File(workspaceDir, RESOURCES_PREFERENCE);
        launchingPrefsFile = new File(workspaceDir, LAUNCHING_PREFERENCES);
        resourceProjectsDir = new File(workspaceDir, RESOURCE_PROJECTS_DIR);
        userJSFLibraries = new File(workspaceDir, USER_JSF_LIBRARIES);
    }

    File getUserJSFLibraries() {
        return userJSFLibraries;
    }
    
    public File getDirectory() {
        return workspaceDir;
    }
    
    File getCorePreferenceFile() {
        return corePrefFile;
    }
    
    File getResourcesPreferenceFile() {
        return resourcesPrefFile;
    }
    
    File getLaunchingPrefsFile() {
        return launchingPrefsFile;
    }
    
    File getResourceProjectsDir() {
        return resourceProjectsDir;
    }
    
    void addVariable(Variable var) {
        variables.add(var);
    }
    
    void addResourcesVariable(Variable var) {
        resourcesVariables.add(var);
    }
    
    /**
     * @return set of variables; never null; can be empty
     */
    Set<Variable> getVariables() {
        return variables;
    }
    
    /**
     * @return set of variables; never null; can be empty
     */
    Set<Variable> getResourcesVariables() {
        return resourcesVariables;
    }

    void loadMyEclipseLibraries(List<String> importProblems) {
        if (!myEclipseLibrariesLoaded) {
            myEclipseLibrariesLoaded = true;
            Variable v = getVariable("ECLIPSE_HOME"); //NOI18N
            if (v == null) {
                importProblems.add(NbBundle.getMessage(Workspace.class, "MSG_CannotReadMyEclipseLibs"));
                return;
            }
            File f = new File(new File(v.getLocation()), "plugins"); //NOI18N
            if (!f.exists()) {
                importProblems.add(NbBundle.getMessage(Workspace.class, "MSG_CannotReadMyEclipseLibs"));
                return;
            }
            scanForLibraries(f);
        }
    }
    
    private void scanForLibraries(File dir) {
        assert dir.isDirectory() : dir;
        File[] kids = dir.listFiles();
        for (File kid : kids) {
            if (kid.isDirectory()) {
                File f = new File(kid, "preferences.ini"); //NOI18N
                if (f.exists()) {
                    analyzePreferencesIniFile(f);
                }
            }
        }
    }
    
    private void analyzePreferencesIniFile(File f) {
        Properties p = new Properties();
        EclipseUtils.tryLoad(p, f);
        for (Map.Entry e : p.entrySet()) {
            String key = (String)e.getKey();
            String value = (String)e.getValue();
            if (key.startsWith("melibrary.com.genuitec.eclipse.") && key.endsWith(".classpath")) { //NOI18N
                List<String> jars = parseLibDefinition(value);
                int end = key.indexOf(".classpath");
                int index = key.indexOf(".MYECLIPSE_"); // NOI18N
                if (index == -1) {
                    index = key.substring(0, end).lastIndexOf("."); // NOI18N
                    if (index !=-1) {
                        index +=1;
                    }
                } else {
                    index += 11;
                }
                assert index != -1 : key;
                String libName = key.substring(index, end); //NOI18N
                addUserLibrary(libName, jars);
            }
        }
    }
    
    private List<String> parseLibDefinition(String s) {
        List<String> res = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(s, ";"); //NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int index = token.indexOf("("); //NOI18N
            if (index != -1) {
                token = token.substring(0, index);
            }
            String ss[] = EclipseUtils.splitVariable(token);
            Variable var = getVariable(ss[0]);
            if (var != null) {
                token = var.getLocation() + ss[1];
            }
            res.add(token);
        }
        return res;
    }
    
    public Workspace.Variable getVariable(String rawPath) {
        for (Workspace.Variable variable : getVariables()) {
            if (variable.getName().equals(rawPath)) {
                return variable;
            }
        }
        return null;
    }
    
    void setJREContainers(Map<String,String> jreContainers) {
        this.jreContainers = jreContainers;
    }
    
    void addProject(EclipseProject project) {
        projects.add(project);
    }
    
    void addUserLibrary(String libName, List<String> jars) {
        if (userLibraries == null) {
            userLibraries = new HashMap<String, List<String>>();
        }
        userLibraries.put(libName, jars);
    }

    Map<String, List<String>> getUserLibraries() {
        return userLibraries;
    }
    
    List<URL> getJarsForUserLibrary(String libRawPath) {
        if (userLibraries != null && userLibraries.get(libRawPath) != null) {
            List<String> jars = userLibraries.get(libRawPath);
            List<URL> urls = new ArrayList<URL>(jars.size());
            for (String jar : jars) {
                try {
                    File f = new File(jar);
                    URL url = f.toURI().toURL();
                    if (f.isFile()) {
                        url = FileUtil.getArchiveRoot(url);
                    }
                    urls.add(url);
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return urls;
        } else {
            return Collections.<URL>emptyList();
        }
    }
    
    /**
     * Tries to find an <code>EclipseProject</code> in the workspace and either
     * returns its instance or null in the case it's not found.
     */
    EclipseProject getProjectByRawPath(String rawPath) {
        EclipseProject project = null;
        for (EclipseProject prj : projects) {
            // rawpath = /name
            if (prj.getName().equals(rawPath.substring(1))) {
                project = prj;
            }
        }
        if (project == null) {
            logger.info("Project with raw path \"" + rawPath + "\" cannot" + // NOI18N
                    " be found in project list: " + projects); // NOI18N
        }
        return project;
    }
    
    public Set<EclipseProject> getProjects() {
        return projects;
    }
    
    String getProjectAbsolutePath(String projectName) {
        for (EclipseProject project : projects) {
            if (project.getName().equals(projectName)) {
                return project.getDirectory().getAbsolutePath();
            }
        }
        return null;
    }
    
    EclipseProject getProjectByName(String projectName) {
        for (EclipseProject project : projects) {
            if (project.getName().equals(projectName)) {
                return project;
            }
        }
        return null;
    }
    
    /**
     * Returns JDK used for compilation of project with the specified
     * projectDirName.
     */
    String getJDKDirectory(String jreContainer) {
        if (jreContainer != null) {
            if (!DEFAULT_JRE_CONTAINER.equals(jreContainer)) {
                // JRE name seems to be after the last slash
                jreContainer = jreContainer.substring(jreContainer.lastIndexOf('/') + 1);
            }
            for (Map.Entry<String,String> entry : jreContainers.entrySet()) {
                if (entry.getKey().equals(jreContainer)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}
