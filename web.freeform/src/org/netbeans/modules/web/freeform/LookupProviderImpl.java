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
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
            new WebModules(project, projectHelper, projectEvaluator, aux), // WebModuleProvider, ClassPathProvider
            new WebFreeFormActionProvider(project, projectHelper, aux),   //ActionProvider
            new HelpIDFragmentProviderImpl(),
        });
    }
   
    public static boolean isMyProject(AuxiliaryConfiguration aux) {
        return aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true) != null // NOI18N
                || aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true) != null; // NOI18N
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
    
    /**
     * Transparently handles /1 -> /2 schema upgrade (on read only, not write!).
     * See Java FreeForm for more info.
     */
    static final class UpgradingAuxiliaryConfiguration implements AuxiliaryConfiguration {
        
        private final AuxiliaryConfiguration delegate;
        
        public UpgradingAuxiliaryConfiguration(AuxiliaryConfiguration delegate) {
            this.delegate = delegate;
        }

        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            if (elementName.equals(WebProjectNature.EL_WEB) && namespace.equals(WebProjectNature.NS_WEB_2) && shared) {
                Element nue = delegate.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true);
                if (nue == null) {
                    Element old = delegate.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true);
                    if (old != null) {
                        nue = upgradeSchema(old);
                    }
                }
                return nue;
            } else {
                return delegate.getConfigurationFragment(elementName, namespace, shared);
            }
        }

        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            delegate.putConfigurationFragment(fragment, shared);
        }
        
        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            return delegate.removeConfigurationFragment(elementName, namespace, shared);
        }
    }
    
    static Element upgradeSchema(Element old) {
        Document doc = old.getOwnerDocument();
        Element nue = doc.createElementNS(WebProjectNature.NS_WEB_2, WebProjectNature.EL_WEB);
        copyXMLTree(doc, old, nue, WebProjectNature.NS_WEB_2);
        return nue;
    }
    
    // copied from org.netbeans.modules.java.j2seproject.UpdateHelper with changes; could be an API eventually:
    private static void copyXMLTree(Document doc, Element from, Element to, String newNamespace) {
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            org.w3c.dom.Node node = nl.item(i);
            org.w3c.dom.Node newNode;
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.ELEMENT_NODE:
                    Element oldElement = (Element) node;
                    newNode = doc.createElementNS(newNamespace, oldElement.getTagName());
                    NamedNodeMap attrs = oldElement.getAttributes();
                    int alength = attrs.getLength();
                    for (int j = 0; j < alength; j++) {
                        org.w3c.dom.Attr oldAttr = (org.w3c.dom.Attr) attrs.item(j);
                        ((Element)newNode).setAttributeNS(oldAttr.getNamespaceURI(), oldAttr.getName(), oldAttr.getValue());
                    }
                    copyXMLTree(doc, oldElement, (Element) newNode, newNamespace);
                    break;
                case org.w3c.dom.Node.TEXT_NODE:
                    newNode = doc.createTextNode(((Text) node).getData());
                    break;
                case org.w3c.dom.Node.COMMENT_NODE:
                    newNode = doc.createComment(((Comment) node).getData());
                    break;
                default:
                    // Other types (e.g. CDATA) not yet handled.
                    throw new AssertionError(node);
            }
            to.appendChild(newNode);
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
                if (newRoots != null
                        &&  !newRoots.equals(registeredRoots)) {
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
            Element web = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true); // NOI18N
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
            Element el = Util.findElement(parent, fileElName, WebProjectNature.NS_WEB_2);
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
            this.aux = new UpgradingAuxiliaryConfiguration(aux);
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
