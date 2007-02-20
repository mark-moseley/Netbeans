/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.web.freeform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.HelpIDFragmentProvider;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class LookupProviderImpl implements LookupProvider {

    private static final String HELP_ID_FRAGMENT = "web"; // NOI18N
    
    /** Creates a new instance of LookupProviderImpl */
    public LookupProviderImpl() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project prj = (Project)baseContext.lookup(Project.class);
        ProjectAccessor acc = (ProjectAccessor)baseContext.lookup(ProjectAccessor.class);
        AuxiliaryConfiguration aux = (AuxiliaryConfiguration)baseContext.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        assert prj != null;
        assert acc != null;
        return new ProjectLookup(prj, acc.getHelper(), acc.getEvaluator(), aux);
    }

   private static Lookup initLookup(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        WebClasspath webcp = new WebClasspath(projectHelper, projectEvaluator, aux, project);
        return Lookups.fixed(new Object[] {
            new ProjectOpenedHookImpl(webcp),       // register webroots as source classpath
            new PrivilegedTemplatesImpl(),          // List of templates in New action popup
            new WebModules(project, projectHelper, projectEvaluator), // WebModuleProvider, ClassPathProvider
            new WebFreeFormActionProvider(project, projectHelper, aux),   //ActionProvider
            new HelpIDFragmentProviderImpl(),
        });
    }
   
    public static boolean isMyProject(AuxiliaryConfiguration aux) {
        return aux.getConfigurationFragment("web-data", WebProjectNature.NS_WEB, true) != null; // NOI18N
    }   
    
    private static final class HelpIDFragmentProviderImpl implements HelpIDFragmentProvider {
        public String getHelpIDFragment() {
            return HELP_ID_FRAGMENT;
        }
    }
    
    private static class ProjectOpenedHookImpl extends ProjectOpenedHook {
        private final WebClasspath webcp;
        public ProjectOpenedHookImpl(WebClasspath wcp) {
            this.webcp = wcp;
        }
        protected void projectOpened() {
            webcp.prjOpened();
        }
        protected void projectClosed() {
            webcp.prjClosed();
        }
    }
    
    public static final class WebClasspath implements AntProjectListener, PropertyChangeListener {
        
        private ClassPath registeredCP[] = new ClassPath[0];
        private List/*<FileObject>*/ registeredRoots = Collections.EMPTY_LIST;
        
        private PropertyEvaluator evaluator;
        private AuxiliaryConfiguration aux;
        private Project project;
        
        private PropertyChangeSupport pcs;
        
        private boolean prjClosed = false;
        
        public WebClasspath(AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux, Project proj) {
            this.evaluator = evaluator;
            this.aux = aux;
            this.project = proj;
            helper.addAntProjectListener(this);
            evaluator.addPropertyChangeListener(this);
            pcs = new PropertyChangeSupport(this);
        }
        
        public void prjOpened() {
            registeredRoots = getWebRoots(aux, project, evaluator);
            FileObject fos[] = new FileObject[registeredRoots.size()];
            ClassPath cp = ClassPathSupport.createClassPath((FileObject[]) registeredRoots.toArray(fos));
            registeredCP = new ClassPath[] { cp };
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, registeredCP);
            prjClosed = false;
        }
        
        public void prjClosed() {
            if (!registeredRoots.isEmpty()) {
                GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, registeredCP);
            }
            registeredRoots = Collections.EMPTY_LIST;
            prjClosed = true;
        }
        
        public void configurationXmlChanged(AntProjectEvent ev) {
            updateClasspath();
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            // ignore
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            updateClasspath();
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
        private synchronized void updateClasspath() {
            if (!prjClosed) {
                List newRoots = getWebRoots(aux, project, evaluator);
                if (!newRoots.equals(registeredRoots)) {
                    FileObject fos[] = new FileObject[newRoots.size()];
                    ClassPath cp = ClassPathSupport.createClassPath((FileObject[]) newRoots.toArray(fos));
                    GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, registeredCP);
                    registeredCP = new ClassPath[] { cp };
                    registeredRoots = newRoots;
                    GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, registeredCP);
                    pcs.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
                }
            }
        }
        
        private List/*<FileObject>*/ getWebRoots(AuxiliaryConfiguration aux, Project proj, PropertyEvaluator evaluator) {
            Element web = aux.getConfigurationFragment("web-data", WebProjectNature.NS_WEB, true); // NOI18N
            if (web == null) {
                return null;
            }
            List webModules = Util.findSubElements(web);
            Iterator it = webModules.iterator();
            List/*<FileObject>*/ roots = new ArrayList();
            while (it.hasNext()) {
                Element webModulesEl = (Element) it.next();
                assert webModulesEl.getLocalName().equals("web-module") : webModulesEl; // NOI18N
                roots.add(FileUtil.toFileObject(getFile(webModulesEl, "doc-root", proj, evaluator))); // NOI18N
            }
            return roots;
        }
        
        private File getFile(Element parent, String fileElName, Project proj, PropertyEvaluator evaluator) {
            Element el = Util.findElement(parent, fileElName, WebProjectNature.NS_WEB);
            return Util.resolveFile(evaluator, FileUtil.toFile(proj.getProjectDirectory()), Util.findText(el));
        }
        
    }
    
    private static final class ProjectLookup extends ProxyLookup implements AntProjectListener {

        private AntProjectHelper helper;
        private PropertyEvaluator evaluator;
        private Project project;
        private AuxiliaryConfiguration aux;
        private boolean isMyProject;
        
        public ProjectLookup(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
            super(new Lookup[0]);
            this.project = project;
            this.helper = helper;
            this.evaluator = evaluator;
            this.aux = aux;
            this.isMyProject = isMyProject(aux);
            updateLookup();
            helper.addAntProjectListener(this);
        }
        
        private void updateLookup() {
            Lookup l = Lookup.EMPTY;
            if (isMyProject) {
                l = initLookup(project, helper, evaluator, aux);
            }
            setLookups(new Lookup[]{l});
        }
        
        public void configurationXmlChanged(AntProjectEvent ev) {
            if (isMyProject(aux) != isMyProject) {
                isMyProject = !isMyProject;
                updateLookup();
            }
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            // ignore
        }
        
    }
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates, RecommendedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/JSP_Servlet/JSP.jsp",
            "Templates/JSP_Servlet/Html.html",
            "Templates/JSP_Servlet/Servlet.java",
            "Templates/Classes/Class.java",
        };
        
        private static final String[] RECOMENDED_TYPES = new String[] {         
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "wsdl",                 // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "servlet-types",        // NOI18N
            "web-types",            // NOI18N
            "j2ee-types",           // NOI18N
            "junit",                // NOI18N
            "simple-files"          // NOI18N
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

        public String[] getRecommendedTypes() {
            return RECOMENDED_TYPES;
        }
    }    
}
