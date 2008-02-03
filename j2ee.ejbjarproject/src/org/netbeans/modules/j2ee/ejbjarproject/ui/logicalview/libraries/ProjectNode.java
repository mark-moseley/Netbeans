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

package org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.libraries;

import java.awt.Image;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;



/**
 * ProjectNode represents a dependent project under the Libraries Node.
 * It is a leaf node with the following actions: {@link OpenProjectAction},
 * {@link ShowJavadocAction} and {@link RemoveClassPathRootAction}
 * @author Tomas Zezula
 */
class ProjectNode extends AbstractNode {

    private static final String PROJECT_ICON = "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/projectDependencies.gif";    //NOI18N

    private final AntArtifact antArtifact;
    private final URI artifactLocation;
    private Image cachedIcon;

    ProjectNode (AntArtifact antArtifact, URI artifactLocation, UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper, String classPathId, String entryId, String includedLibrariesElement) {
        super (Children.LEAF, createLookup(antArtifact, artifactLocation, helper, eval, refHelper, classPathId, entryId, includedLibrariesElement));
        this.antArtifact = antArtifact;
        this.artifactLocation = artifactLocation;
    }

    @Override
    public String getDisplayName () {        
        ProjectInformation info = getProjectInformation();        
        if (info != null) {
            return MessageFormat.format(NbBundle.getMessage(ProjectNode.class,"TXT_ProjectArtifactFormat"),
                    new Object[] {info.getDisplayName(), artifactLocation.toString()});
        }
        else {
            return NbBundle.getMessage (ProjectNode.class,"TXT_UnknownProjectName");
        }
    }

    @Override
    public String getName () {
        return this.getDisplayName();
    }

    @Override
    public Image getIcon(int type) {
        if (cachedIcon == null) {
            ProjectInformation info = getProjectInformation();
            if (info != null) {
                Icon icon = info.getIcon();
                cachedIcon = Utilities.icon2Image(icon);
            }
            else {
                cachedIcon = Utilities.loadImage(PROJECT_ICON);
            }
        }
        return cachedIcon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return this.getIcon(type);
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get (OpenProjectAction.class),
            SystemAction.get (ShowJavadocAction.class),
            SystemAction.get (RemoveClassPathRootAction.class),
        };
    }

    @Override
    public Action getPreferredAction () {
        return getActions(false)[0];
    }
    
    private ProjectInformation getProjectInformation () {
        Project p = this.antArtifact.getProject();
        if (p != null) {
            return ProjectUtils.getInformation(p);
        }
        return null;
    }
    
    private static Lookup createLookup (AntArtifact antArtifact, URI artifactLocation, 
            UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper, 
            String classPathId, String entryId, String includedLibrariesElement) {
        Project p = antArtifact.getProject();
        Object[] content;
        if (p == null) {
            content = new Object[1];
        }
        else {
            content = new Object[3];
            content[1] = new JavadocProvider(antArtifact, artifactLocation);
            content[2] = p;
        }
        content[0] = new Removable(helper, eval, refHelper, classPathId, entryId, includedLibrariesElement);        
        Lookup lkp = Lookups.fixed(content);
        return lkp;
    }    

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {

        private final AntArtifact antArtifact;
        private final URI artifactLocation;

        JavadocProvider (AntArtifact antArtifact, URI artifactLocation) {
            this.antArtifact = antArtifact;
            this.artifactLocation = artifactLocation;
        }


        public boolean hasJavadoc() {
            return findJavadoc().size() > 0;
        }

        public void showJavadoc() {
            Set<URL> us = findJavadoc();
            URL[] urls = us.toArray(new URL[us.size()]);
            URL pageURL = ShowJavadocAction.findJavadoc("overview-summary.html",urls);
            if (pageURL == null) {
                pageURL = ShowJavadocAction.findJavadoc("index.html",urls);
            }
            ProjectInformation info = null;
            Project p = this.antArtifact.getProject ();
            if (p != null) {
                info = ProjectUtils.getInformation(p);
            }
            ShowJavadocAction.showJavaDoc (pageURL, info == null ?
                NbBundle.getMessage (ProjectNode.class,"TXT_UnknownProjectName") : info.getDisplayName());
        }
        
        private Set<URL> findJavadoc() {
            File scriptLocation = this.antArtifact.getScriptLocation();            
            Set<URL> urls = new HashSet<URL>();
            try {
                URL artifactURL = scriptLocation.toURI().resolve(this.artifactLocation).normalize().toURL();
                if (FileUtil.isArchiveFile(artifactURL)) {
                    artifactURL = FileUtil.getArchiveRoot(artifactURL);
                }
                urls.addAll(Arrays.asList(JavadocForBinaryQuery.findJavadoc(artifactURL).getRoots()));                
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);                
            }                                    
            return urls;
        }

    }

    private static class OpenProjectAction extends NodeAction {
        private static final long serialVersionUID = 87316177258248119L;

        protected void performAction(Node[] activatedNodes) {
            Project[] projects = new Project[activatedNodes.length];
            for (int i=0; i<projects.length;i++) {
                final Project p = getProject(activatedNodes[i]);
                if (p == null) {
                    //Should not happen, only for case when project is deleted after enabled called
                    return;
                }
                projects[i] = p;
            }
            OpenProjects.getDefault().open(projects, false);
        }

        protected boolean enable(Node[] activatedNodes) {
            final Collection<Project> openedProjects =Arrays.asList(OpenProjects.getDefault().getOpenProjects());
            for (int i=0; i<activatedNodes.length; i++) {
                final Project p = getProject (activatedNodes[i]);
                if (p == null) {
                    return false;
                }
                if (openedProjects.contains(p)) {
                    return false;
                }
            }
            return true;
        }
        
        private static Project getProject (final Node node) {
            assert node != null;
            final Project p = node.getLookup().lookup(Project.class);
            if (p != null) {
                final FileObject projectRoot = p.getProjectDirectory();
                if (projectRoot == null || !projectRoot.isValid()) {
                    return null;
                }
            }
            return p;
        }
        
        public String getName() {
            return NbBundle.getMessage (ProjectNode.class,"CTL_OpenProject");
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx (OpenProjectAction.class);
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }
    }

    private static class Removable implements RemoveClassPathRootAction.Removable {

        private final UpdateHelper helper;
//        private final PropertyEvaluator eval;
        private final ReferenceHelper refHelper;
        private final String classPathId;
        private final String entryId;
        private final String includedLibrariesElement;
        private final ClassPathSupport cs;

        Removable (UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper, String classPathId, String entryId, String includedLibrariesElement) {
            this.helper = helper;
//            this.eval = eval;
            this.refHelper = refHelper;
            this.classPathId = classPathId;
            this.entryId = entryId;
            this.includedLibrariesElement = includedLibrariesElement;
            
            this.cs = new ClassPathSupport( eval, refHelper, helper.getAntProjectHelper(), 
                                            EjbJarProjectProperties.WELL_KNOWN_PATHS, 
                                            EjbJarProjectProperties.LIBRARY_PREFIX, 
                                            EjbJarProjectProperties.LIBRARY_SUFFIX, 
                                            EjbJarProjectProperties.ANT_ARTIFACT_PREFIX );        
        }

        public boolean canRemove () {
            //Allow to remove only entries from PROJECT_PROPERTIES, same behaviour as the project customizer
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return props.getProperty (classPathId) != null;
        }

        public Project remove() {
            // The caller has write access to ProjectManager
            // and ensures the project will be saved.
            
            boolean removed = false;
            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String raw = props.getProperty (classPathId);
            List<ClassPathSupport.Item> resources = cs.itemsList(raw, includedLibrariesElement);
            for (Iterator i = resources.iterator(); i.hasNext();) {
                ClassPathSupport.Item item = (ClassPathSupport.Item)i.next();
                if (entryId.equals(EjbJarProjectProperties.getAntPropertyName(item.getReference()))) {
                    i.remove();
                    removed = true;
                }
            }
            if (removed) {
                String[] itemRefs = cs.encodeToStrings(resources.iterator(), includedLibrariesElement);
                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                props.setProperty(classPathId, itemRefs);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);

                String ref = "${" + entryId + "}"; //NOI18N
                if (!RemoveClassPathRootAction.isReferenced (new EditableProperties[] {
                        props,
                        helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH)}, ref)) {
                    refHelper.destroyReference (ref);
                }

                return FileOwnerQuery.getOwner(helper.getAntProjectHelper().getProjectDirectory());
            }
            return null;
        }
    }
}


