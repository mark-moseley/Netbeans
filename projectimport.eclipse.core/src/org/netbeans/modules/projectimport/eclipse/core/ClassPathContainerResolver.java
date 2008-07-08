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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 * For now all well-known containers (based on Europa/Ganymede) are hardcoded here.
 * Can be refactored into SPI if needed.
 * 
 */
public class ClassPathContainerResolver {

    public static final String JUNIT_CONTAINER = "org.eclipse.jdt.junit.JUNIT_CONTAINER/";
    public static final String USER_LIBRARY_CONTAINER = "org.eclipse.jdt.USER_LIBRARY/";
    public static final String WEB_CONTAINER = "org.eclipse.jst.j2ee.internal.web.container";
    public static final String J2EE_MODULE_CONTAINER = "org.eclipse.jst.j2ee.internal.module.container";
    public static final String JSF_CONTAINER = "org.eclipse.jst.jsf.core.internal.jsflibrarycontainer/";
    public static final String J2EE_SERVER_CONTAINER = "org.eclipse.jst.server.core.container/";
    
    /**
     * Converts eclipse CONTAINER claspath entry to something what can be put
     * directly to Ant based project classpath. 
     * 
     * Eg. for "org.eclipse.jdt.junit.JUNIT_CONTAINER/3.8.1" it would be "libs.junit.classpath"
     * 
     * This method is called during project import just before eclipse project is converted to NetBeans
     * and it is possible to create for example NetBeans IDE library here etc.
     */
    public static boolean resolve(Workspace workspace, DotClassPathEntry entry, List<String> importProblems) throws IOException {
        assert entry.getKind() == DotClassPathEntry.Kind.CONTAINER : entry;
        
        String container = entry.getRawPath();
        
        if (container.startsWith(JUNIT_CONTAINER)) {
            String library = "libs.junit.classpath";
            if (container.substring(JUNIT_CONTAINER.length()).startsWith("4")) {
                library = "libs.junit_4.classpath";
            }
            entry.setContainerMapping(library);
            return true;
        }
        
        if (container.startsWith(USER_LIBRARY_CONTAINER)) {
            createLibrary(workspace, container, importProblems);
            entry.setContainerMapping("libs."+getNetBeansLibraryName(container)+".classpath");
            return true;
        }
        
        if (container.startsWith(JSF_CONTAINER)) {
            createLibrary(workspace, container, importProblems);
            entry.setContainerMapping("libs."+getNetBeansLibraryName(container)+".classpath");
            return true;
        }
        
        if (container.startsWith(J2EE_MODULE_CONTAINER) ||
            container.startsWith(J2EE_SERVER_CONTAINER)) {
            // TODO: resolve these containers as empty for now.
            //       most of these are not needed anyway as they are 
            //       handled differntly directly by web project
            entry.setContainerMapping("");
            return true;
        }
        
        importProblems.add("unsupported classpath container found. It will be ignored and " +
                "you may need to update NetBeans project classpath by hand. Internal name of this container is: '"+
                container+"'");
        
        return false;
    }

    public static List<DotClassPathEntry> replaceContainerEntry(EclipseProject project, Workspace workspace, DotClassPathEntry entry, List<String> importProblems) {
        assert entry.getKind() == DotClassPathEntry.Kind.CONTAINER : entry;
        String container = entry.getRawPath();
        if (container.startsWith(WEB_CONTAINER)) {
            String projectName = null;
            if (container.length() > WEB_CONTAINER.length()) {
                projectName = container.substring(WEB_CONTAINER.length()+1);
            }
            return createClassPathForWebContainer(project, workspace, projectName);
        }
        
        return null;
    }
    
    private static List<DotClassPathEntry> createClassPathForWebContainer(EclipseProject project, Workspace w, String name) {
        List<DotClassPathEntry> newEntries = new ArrayList<DotClassPathEntry>();
        EclipseProject p = name != null ? w.getProjectByName(name) : project;
        if (p == null) {
            return newEntries;
        }
        if (!p.isImportSupported()) {
            return newEntries;
        }
        File location = p.getProjectFileLocation(ProjectTypeFactory.FILE_LOCATION_TOKEN_WEBINF);
        if (location == null) {
            return newEntries;
        }
        File lib = new File(location, "lib"); // NOI18N
        if (lib.exists()) {
            for (File f : lib.listFiles()) {
                if (f.isFile()) {
                    newEntries.add(createFileDotClassPathEntry(f));
                }
            }
        }
        File classes = new File(location, "classes"); // NOI18N
        if (classes.exists() && classes.isDirectory()) {
            newEntries.add(createFileDotClassPathEntry(classes));
        }
        return newEntries;
    }
    
    private static DotClassPathEntry createFileDotClassPathEntry(File f) {
        Map<String, String> props = new HashMap<String, String>();
        props.put(DotClassPathEntry.ATTRIBUTE_KIND, "lib"); // NOI18N
        props.put(DotClassPathEntry.ATTRIBUTE_PATH, f.getPath());
        DotClassPathEntry d = new DotClassPathEntry(props, null);
        d.setAbsolutePath(f.getPath());
        return d;
    }
        
    private static String getNetBeansLibraryName(String container) {
        String prefix = container.startsWith(USER_LIBRARY_CONTAINER) ? USER_LIBRARY_CONTAINER : JSF_CONTAINER;
        return PropertyUtils.getUsablePropertyName(container.substring(prefix.length()));
    }

    private static String getEclipseLibraryName(String container) {
        String prefix = container.startsWith(USER_LIBRARY_CONTAINER) ? USER_LIBRARY_CONTAINER : JSF_CONTAINER;
        return container.substring(prefix.length());
    }

    private static void createLibrary(Workspace workspace, String container, List<String> importProblems) throws IOException {
        // create eclipse user libraries in NetBeans:
        assert container.startsWith(USER_LIBRARY_CONTAINER) ||
                container.startsWith(JSF_CONTAINER) : container;
        String library = getNetBeansLibraryName(container);
        LibraryManager lm = LibraryManager.getDefault();
        if (lm.getLibrary(library) != null) {
            return;
        }
        Map<String,List<URL>> content = new HashMap<String,List<URL>>();
        if (workspace == null) {
            importProblems.add("User library '"+library+"' cannot be created because project is being imported without Eclipse workspace.");
            return;
        }
        content.put("classpath", workspace.getJarsForUserLibrary(getEclipseLibraryName(container)));
        lm.createLibrary("j2se", library, content);
    }
    
    public static boolean isJUnit(DotClassPathEntry entry) {
        if (entry.getKind() == DotClassPathEntry.Kind.CONTAINER && 
                entry.getRawPath().startsWith(JUNIT_CONTAINER)) {
            return true;
        }
        if (entry.getKind() == DotClassPathEntry.Kind.LIBRARY ||  
            entry.getKind() == DotClassPathEntry.Kind.VARIABLE) {
            int i = entry.getRawPath().replace('\\', '/').lastIndexOf('/'); // NOI18N
            if (i != -1) {
                String s = entry.getRawPath().substring(i+1);
                return s.startsWith("junit") && s.endsWith(".jar"); // NOI18N
            }
        }
        return false;
    }
        
}
