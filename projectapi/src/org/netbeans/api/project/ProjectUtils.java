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

package org.netbeans.api.project;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.projectapi.AuxiliaryConfigBasedPreferencesProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Utility methods to get information about {@link Project}s.
 * @author Jesse Glick
 */
public class ProjectUtils {

    private ProjectUtils() {}
    
    /**
     * Get basic information about a project.
     * If the project has a {@link ProjectInformation} instance in its lookup,
     * that is used. Otherwise, a basic dummy implementation is returned.
     * @param p a project
     * @return some information about it
     * @see Project#getLookup
     */
    public static ProjectInformation getInformation(Project p) {
        ProjectInformation pi = p.getLookup().lookup(ProjectInformation.class);
        if (pi != null) {
            return pi;
        } else {
            return new BasicInformation(p);
        }
    }
    
    /**
     * Get a list of sources for a project.
     * If the project has a {@link Sources} instance in its lookup,
     * that is used. Otherwise, a basic implementation is returned
     * using {@link GenericSources#genericOnly}.
     * @param p a project
     * @return a list of sources for it
     * @see Project#getLookup
     */
    public static Sources getSources(Project p) {
        Sources s = p.getLookup().lookup(Sources.class);
        if (s != null) {
            return s;
        } else {
            return GenericSources.genericOnly(p);
        }
    }
    
    /**
     * Check whether a project has, or might have, cycles in its subproject graph.
     * <p>
     * If the candidate parameter is null, this simply checks whether the master
     * project's current directed graph of (transitive) subprojects contains any
     * cycles. If the candidate is also passed, this checks whether the master
     * project's subproject graph would contain cycles if the candidate were added
     * as a (direct) subproject of the master project.
     * </p>
     * <p>
     * All cycles are reported even if they do not contain the master project.
     * </p>
     * <p>
     * If the master project already contains the candidate as a (direct) subproject,
     * the effect is as if the candidate were null.
     * </p>
     * <p>
     * Projects with no {@link SubprojectProvider} are considered to have no
     * subprojects, just as if the provider returned an empty set.
     * </p>
     * <p>
     * Acquires read access.
     * </p>
     * <p class="nonnormative">
     * Project types which let the user somehow configure subprojects in the GUI
     * (perhaps indirectly, e.g. via a classpath) should use this call to check
     * for possible cycles before adding new subprojects.
     * </p>
     * @param master a project to root the subproject graph from
     * @param candidate a potential direct subproject of the master project, or null
     * @return true if the master project currently has a cycle somewhere in its
     *         subproject graph, regardless of the candidate parameter, or if the
     *         candidate is not null and the master project does not currently have
     *         a cycle but would have one if the candidate were added as a subproject
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=43845">Issue #43845</a>
     */
    public static boolean hasSubprojectCycles(final Project master, final Project candidate) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                return visit(new HashSet<Project>(), new HashMap<Project,Set<? extends Project>>(), master, master, candidate);
            }
        });
    }
    
    /**
     * Return {@link Preferences} for the given project and given module.
     * 
     * <p class="nonnormative">
     * The preferences are stored in the project using either {@link AuxiliaryConfiguration}
     * or {@link AuxiliaryProperties}.
     * </p>
     * 
     * @param project project for which preferences should be returned
     * @param clazz module specification as in {@link org.openide.util.NbPreferences#forModule(java.lang.Class)}
     * @param shared whether the returned settings should be shared
     * @return {@link Preferences} for the given project
     * @since 1.16
     */
    public static Preferences getPreferences(Project project, Class clazz, boolean shared) {
        Parameters.notNull("project", project);
        Parameters.notNull("clazz", clazz);
        
        return AuxiliaryConfigBasedPreferencesProvider.getPreferences(project, clazz, shared);
    }
    
    /**
     * Do a DFS traversal checking for cycles.
     * @param encountered projects already encountered in the DFS (added and removed as you go)
     * @param curr current node to visit
     * @param master the original master project (for use with candidate param)
     * @param candidate a candidate added subproject for master, or null
     */
    private static boolean visit(Set<Project> encountered, Map<Project,Set<? extends Project>> cache, Project curr, Project master, Project candidate) {
        if (!encountered.add(curr)) {
            return true;
        }
        Set<? extends Project> subprojects = cache.get(curr);
        if (subprojects == null) {
            SubprojectProvider spp = curr.getLookup().lookup(SubprojectProvider.class);
            subprojects = spp != null ? spp.getSubprojects() : Collections.<Project>emptySet();
            cache.put(curr, subprojects);
        }
        for (Project child : subprojects) {
            if (candidate == child) {
                candidate = null;
            }
            if (visit(encountered, cache, child, master, candidate)) {
                return true;
            }
        }
        if (candidate != null && curr == master) {
            if (visit(encountered, cache, candidate, master, candidate)) {
                return true;
            }
        }
        assert encountered.contains(curr);
        encountered.remove(curr);
        return false;
    }
    
    private static final class BasicInformation implements ProjectInformation {
        
        private final Project p;
        
        public BasicInformation(Project p) {
            this.p = p;
        }
        
        public String getName() {
            try {
                return p.getProjectDirectory().getURL().toExternalForm();
            } catch (FileStateInvalidException e) {
                return e.toString();
            }
        }
        
        public String getDisplayName() {
            return p.getProjectDirectory().getNameExt();
        }
        
        public Icon getIcon() {
            return new ImageIcon(Utilities.loadImage("org/netbeans/modules/projectapi/resources/empty.gif")); // NOI18N
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }
        
        public Project getProject() {
            return p;
        }
        
    }
    
}
