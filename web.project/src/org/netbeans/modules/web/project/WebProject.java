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

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.web.project.api.WebPropertyEvaluator;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSClientSupport;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.ui.WebLogicalViewProvider;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.project.ArtifactCopyOnSaveSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathExtender;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifierSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.j2ee.common.project.ui.DeployOnSaveUtils;
import org.netbeans.modules.j2ee.common.project.ui.J2EEProjectProperties;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Capabilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.DeployOnSaveSupport;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.web.project.classpath.WebProjectLibrariesModifierImpl;
import org.netbeans.modules.web.project.spi.BrokenLibraryRefFilter;
import org.netbeans.modules.web.project.spi.BrokenLibraryRefFilterProvider;
import org.netbeans.modules.web.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.web.spi.webmodule.WebPrivilegedTemplates;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Represents one plain Web project.
 * @author Jesse Glick, et al., Pavel Buzek
 * @author kaktus
 */
public final class WebProject implements Project, AntProjectListener {
    
    private static final Logger LOGGER = Logger.getLogger(WebProject.class.getName());
    
    private static final Icon WEB_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif", false); // NOI18
    
    private static final Pattern TLD_PATTERN = Pattern.compile("(META-INF/.*\\.tld)|(META-INF/tlds/.*\\.tld)");
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private Lookup lookup;
    private final ProjectWebModule webModule;
    private final CopyOnSaveSupport css;
    private final ArtifactCopyOnSaveSupport artifactSupport;
    private final DeployOnSaveSupport deployOnSaveSupport;
    private final EjbJarProvider webEjbJarProvider;
    private final EjbJar apiEjbJar;
    private WebModule apiWebModule;
    private WebServicesSupport apiWebServicesSupport;
    private JAXWSSupport apiJaxwsSupport;
    private WebServicesClientSupport apiWebServicesClientSupport;
    private JAXWSClientSupport apiJAXWSClientSupport;
    private WebContainerImpl enterpriseResourceSupport;
    private FileWatch webPagesFileWatch;
    private FileWatch webInfFileWatch;
    private PropertyChangeListener j2eePlatformListener;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private final UpdateHelper updateHelper;
    private final UpdateProjectImpl updateProject;
    private final AuxiliaryConfiguration aux;
    private final ClassPathExtender classPathExtender;
    private final ClassPathModifier cpMod;
    private final WebProjectLibrariesModifierImpl libMod;
    private final ClassPathProviderImpl cpProvider;
    private ClassPathUiSupport.Callback classPathUiSupportCallback;
    
    private AntBuildExtender buildExtender;
            
    private class FileWatch implements AntProjectListener, FileChangeListener {

        private String propertyName;

        private FileObject fileObject = null;
        private boolean watchRename = false;

        public FileWatch(String property) {
            this.propertyName = property;
        }

        public void init() {
            helper.addAntProjectListener(this);
            updateFileChangeListener();
        }

        public void reset() {
            helper.removeAntProjectListener(this);
            setFileObject(null);
        }

        public void updateFileChangeListener() {
            File resolvedFile;
            FileObject fo = null;
            String propertyValue = helper.getStandardPropertyEvaluator().getProperty(propertyName);
            if (propertyValue != null) {
                String resolvedPath = helper.resolvePath(propertyValue);
                resolvedFile = new File(resolvedPath).getAbsoluteFile();
                if (resolvedFile != null) {
                    File f = resolvedFile;
                    while (f != null && (fo = FileUtil.toFileObject(f)) == null) {
                        f = f.getParentFile();
                    }
                    watchRename = f == resolvedFile;
                } else {
                    watchRename = false;
                }
            } else {
                resolvedFile = null;
                watchRename = false;
            }
            setFileObject(fo);
        }

        private void setFileObject(FileObject fo) {
            if (!isEqual(fo, fileObject)) {
                if (fileObject != null) {
                    fileObject.removeFileChangeListener(this);
                }
                fileObject = fo;
                if (fileObject != null) {
                    fileObject.addFileChangeListener(this);
                }
            }
        }

        private boolean isEqual(Object object1, Object object2) {
            if (object1 == object2) {
                return true;
            }
            if(object1 == null) {
                return false;
            }
            return object1.equals(object2);
        }

        // AntProjectListener

        public void configurationXmlChanged(AntProjectEvent ev) {
            updateFileChangeListener();
        }

        public void propertiesChanged(AntProjectEvent ev) {
            updateFileChangeListener();
        }

        // FileChangeListener

        public void fileFolderCreated(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileDataCreated(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileChanged(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileDeleted(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileRenamed(final FileRenameEvent fe) {
            if(watchRename && fileObject.isValid()) {
                final File f = new File(helper.getStandardPropertyEvaluator().getProperty(propertyName));
                if(f.getName().equals(fe.getName())) {
                    ProjectManager.mutex().postWriteRequest(new Runnable() {
                        public void run() {
                            EditableProperties properties = new EditableProperties(true);
                            properties.setProperty(propertyName, new File(f.getParentFile(), fe.getFile().getName()).getPath());
                            Utils.updateProperties(helper, AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);
                            try {
                                ProjectManager.getDefault().saveProject(WebProject.this);
                                updateFileChangeListener();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IllegalArgumentException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
            }
            updateFileChangeListener();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    };
    
    WebProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        buildExtender = AntBuildExtenderFactory.createAntExtender(new WebExtenderImplementation(), refHelper);
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        updateProject = new UpdateProjectImpl(this, this.helper, aux);
        this.updateHelper = new UpdateHelper(updateProject, helper);
        updateProject.setUpdateHelper(updateHelper);
        this.cpProvider = new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots());
        webModule = new ProjectWebModule (this, updateHelper, cpProvider);
        apiWebModule = WebModuleFactory.createWebModule (webModule);
        webEjbJarProvider = new EjbJarProvider(webModule, cpProvider);
        apiEjbJar = EjbJarFactory.createEjbJar(webEjbJarProvider);
        WebProjectWebServicesSupport webProjectWebServicesSupport = new WebProjectWebServicesSupport(this, helper, refHelper);
        WebProjectJAXWSSupport jaxwsSupport = new WebProjectJAXWSSupport(this, helper);
        WebProjectJAXWSClientSupport jaxWsClientSupport = new WebProjectJAXWSClientSupport(this);
        WebProjectWebServicesClientSupport webProjectWebServicesClientSupport = new WebProjectWebServicesClientSupport(this, helper, refHelper);
        apiWebServicesSupport = WebServicesSupportFactory.createWebServicesSupport (webProjectWebServicesSupport);
        apiJaxwsSupport = JAXWSSupportFactory.createJAXWSSupport(jaxwsSupport);
        apiWebServicesClientSupport = WebServicesClientSupportFactory.createWebServicesClientSupport (webProjectWebServicesClientSupport);
        apiJAXWSClientSupport = JAXWSClientSupportFactory.createJAXWSClientSupport(jaxWsClientSupport);
        enterpriseResourceSupport = new WebContainerImpl(this, refHelper, helper);
        cpMod = new ClassPathModifier(this, this.updateHelper, eval, refHelper,
            new ClassPathSupportCallbackImpl(helper), createClassPathModifierCallback(), getClassPathUiSupportCallback());
        libMod = new WebProjectLibrariesModifierImpl(this, this.updateHelper, eval, refHelper);
        classPathExtender = new ClassPathExtender(cpMod, ProjectProperties.JAVAC_CLASSPATH, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES);
        lookup = createLookup(aux, cpProvider);
        helper.addAntProjectListener(this);
        css = new CopyOnSaveSupport();
        artifactSupport = new ArtifactCopySupport();
        deployOnSaveSupport = new DeployOnSaveSupportProxy();
        webPagesFileWatch = new FileWatch(WebProjectProperties.WEB_DOCBASE_DIR);
        webInfFileWatch = new FileWatch(WebProjectProperties.WEBINF_DIR);
    }

    public ClassPathModifier getClassPathModifier() {
        return cpMod;
    }

    public WebProjectLibrariesModifierImpl getLibrariesModifier() {
        return libMod;
    }
    
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return deployOnSaveSupport;
    }
    
    private ClassPathModifier.Callback createClassPathModifierCallback() {
        return new ClassPathModifier.Callback() {
            public String getClassPathProperty(SourceGroup sg, String type) {
                assert sg != null : "SourceGroup cannot be null";  //NOI18N
                assert type != null : "Type cannot be null";  //NOI18N
                final String[] classPathProperty = getClassPathProvider().getPropertyName (sg, type);
                if (classPathProperty == null || classPathProperty.length == 0) {
                    throw new UnsupportedOperationException ("Modification of [" + sg.getRootFolder().getPath() +", " + type + "] is not supported"); //NOI18N
                }
                assert !classPathProperty[0].equals(WebProjectProperties.J2EE_PLATFORM_CLASSPATH);
                return classPathProperty[0];
            }

            public String getElementName(String classpathProperty) {
                if (ProjectProperties.JAVAC_CLASSPATH.equals(classpathProperty)) {
                    return ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES;
                }
                return null;
            }
        };        
    }
    
    public synchronized ClassPathUiSupport.Callback getClassPathUiSupportCallback() {
        if (classPathUiSupportCallback == null) {
            classPathUiSupportCallback = new ClassPathUiSupport.Callback() {
                public void initItem(ClassPathSupport.Item item) {
                    switch (item.getType()) {
                        case ClassPathSupport.Item.TYPE_JAR:
                            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, 
                                    item.getResolvedFile().isDirectory() ? 
                                        ClassPathSupportCallbackImpl.PATH_IN_WAR_DIR : 
                                        ClassPathSupportCallbackImpl.PATH_IN_WAR_LIB);
                            break;
                        case ClassPathSupport.Item.TYPE_LIBRARY:
                            if (item.getLibrary().getType().equals(J2eePlatform.LIBRARY_TYPE)) {
                                break;
                            }
                        default:
                            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, 
                                    ClassPathSupportCallbackImpl.PATH_IN_WAR_LIB);
                    }
                }
            };
            
        }
        return classPathUiSupportCallback;
    }

    public UpdateProjectImpl getUpdateImplementation() {
        return updateProject;
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }
    
    public String toString() {
        return "WebProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        return helper.getStandardPropertyEvaluator();
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    public ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux, ClassPathProviderImpl cpProvider) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        WebSources webSources = new WebSources(this, helper, evaluator(), getSourceRoots(), getTestSourceRoots());
        FileEncodingQueryImplementation encodingQuery = QuerySupport.createFileEncodingQuery(evaluator(), WebProjectProperties.SOURCE_ENCODING);
        
        Lookup base = Lookups.fixed(new Object[] {            
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            helper.createAuxiliaryProperties(),
            spp,
            new ProjectWebModuleProvider (),
            new WebProjectEjbJarProvider(this), 
            new ProjectWebServicesSupportProvider(),
            webModule, //implements J2eeModuleProvider
            enterpriseResourceSupport,
            new WebActionProvider( this, this.updateHelper, this.eval ),
            new WebLogicalViewProvider(this, this.updateHelper, evaluator (), refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper),        
            LookupMergerSupport.createClassPathProviderMerger(cpProvider),
            QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots(), 
                    new String[]{"build.classes.dir", "dist.war"}, new String[]{"build.test.classes.dir"}),
            QuerySupport.createJavadocForBinaryQuery(helper, evaluator(), new String[]{"build.classes.dir", "dist.war"}),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
            QuerySupport.createUnitTestForSourceQuery(getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createSourceLevelQuery(evaluator()),
            webSources,
            QuerySupport.createSharabilityQuery(helper, evaluator(), getSourceRoots(), 
                getTestSourceRoots(), WebProjectProperties.WEB_DOCBASE_DIR),
            new RecommendedTemplatesImpl(this),
            new CoSAwareFileBuiltQueryImpl(QuerySupport.createFileBuiltQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()), this),
            classPathExtender,
            buildExtender,
            cpMod,
            new WebProjectOperations(this),
            new WebPersistenceProvider(this, evaluator(), cpProvider),
            new WebPersistenceProviderSupplier(this),
            new WebEMGenStrategyResolver(),
            new WebJPADataSourceSupport(this), 
            Util.createServerStatusProvider(getWebModule()),
            new WebJPAModuleInfo(this),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            new WebPropertyEvaluatorImpl(evaluator()),
            WebProject.this, // never cast an externally obtained Project to WebProject - use lookup instead
            libMod,
            encodingQuery,
            QuerySupport.createTemplateAttributesProvider(helper, encodingQuery),
            ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, eval),
            LookupMergerSupport.createSFBLookupMerger(),
            ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, eval),
            LookupMergerSupport.createJFBLookupMerger(),
            QuerySupport.createBinaryForSourceQueryImplementation(sourceRoots, testRoots, helper, eval),
        });
        lookup = base;
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-web-project/Lookup"); //NOI18N
    }
    
    public ClassPathProviderImpl getClassPathProvider () {
        return this.cpProvider;
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored (probably better to listen to evaluator() if you need to)
    }
    
    String getBuildXmlName () {
        String storedName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    // Package private methods -------------------------------------------------
    
    /**
     * Returns the source roots of this project
     * @return project's source roots
     */    
    public synchronized SourceRoots getSourceRoots() {        
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(), WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(this.updateHelper, evaluator(), getReferenceHelper(), WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }

    File getTestClassesDirectory() {
        String testClassesDir = evaluator().getProperty(ProjectProperties.BUILD_TEST_CLASSES_DIR);
        if (testClassesDir == null) {
            return null;
        }
        return helper.resolveFile(testClassesDir);
    }
    
    public ProjectWebModule getWebModule () {
        return webModule;
    }

    public WebModule getAPIWebModule () {
        return apiWebModule;
    }

    public EjbJar getAPIEjbJar() {
        return apiEjbJar;
    }
    
    WebServicesSupport getAPIWebServicesSupport () {
            return apiWebServicesSupport;
    }
    
    JAXWSSupport getAPIJAXWSSupport () {
            return apiJaxwsSupport;
    }
    
    WebServicesClientSupport getAPIWebServicesClientSupport () {
            return apiWebServicesClientSupport;
    }
    
    JAXWSClientSupport getAPIJAXWSClientSupport () {
            return apiJAXWSClientSupport;
    }
    
    /** Return configured project name. */
    public String getName() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    public void registerJ2eePlatformListener(final J2eePlatform platform) {
        // listen to classpath changes
        j2eePlatformListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(J2eePlatform.PROP_CLASSPATH)) {
                    ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                        public Void run() {
                            EditableProperties ep = helper.getProperties(
                                    AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            EditableProperties projectProps = helper.getProperties(
                                    AntProjectHelper.PROJECT_PROPERTIES_PATH);

                            if (!J2EEProjectProperties.isUsingServerLibrary(projectProps,
                                    WebProjectProperties.J2EE_PLATFORM_CLASSPATH)) {
                                String classpath = Utils.toClasspathString(platform.getClasspathEntries());
                                ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            }
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(WebProject.this);
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            }
                            return null;
                        }
                    });
                }
            }
        };
        platform.addPropertyChangeListener(j2eePlatformListener);
    }
    
    public void unregisterJ2eePlatformListener(J2eePlatform platform) {
        if (j2eePlatformListener != null) {
            platform.removePropertyChangeListener(j2eePlatformListener);
        }
    }
    // Private innerclasses ----------------------------------------------------
    
    //when #110886 gets implemented, this class is obsolete
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private WeakReference<String> cachedName = null;
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
            synchronized (pcs) {
                cachedName = null;
            }
        }
        
        public String getName() {
            return WebProject.this.getName();
        }
        
        public String getDisplayName() {
            synchronized (pcs) {
                if (cachedName != null) {
                    String dn = cachedName.get();
                    if (dn != null) {
                        return dn;
                    }
                }
            }
            String dn = ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
                public String run() {
                    Element data = updateHelper.getPrimaryConfigurationData(true);
                    // XXX replace by XMLUtil when that has findElement, findText, etc.
                    NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    if (nl.getLength() == 1) {
                        nl = nl.item(0).getChildNodes();
                        if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            return ((Text) nl.item(0)).getNodeValue();
                        }
                    }
                    return "???"; // NOI18N
                }
            });
            synchronized (pcs) {
                cachedName = new WeakReference<String>(dn);
            }
            return dn;
        }

        
        public Icon getIcon() {
            return WEB_PROJECT_ICON;
        }
        
        public Project getProject() {
            return WebProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
            int flags = genFilesHelper.getBuildScriptState(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                WebProject.class.getResource("resources/build-impl.xsl"));
            if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        JButton updateOption = new JButton(NbBundle.getMessage(WebProject.class, "CTL_Regenerate"));
                        if (DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor(NbBundle.getMessage(WebProject.class, "TXT_BuildImplRegenerate"),
                                NbBundle.getMessage(WebProject.class,"TXT_BuildImplRegenerateTitle"),
                                NotifyDescriptor.DEFAULT_OPTION,
                                NotifyDescriptor.WARNING_MESSAGE,
                                new Object[] {
                            updateOption,
                            NotifyDescriptor.CANCEL_OPTION
                        },
                                updateOption)) == updateOption) {
                            try {
                                genFilesHelper.generateBuildScriptFromStylesheet(
                                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                        WebProject.class.getResource("resources/build-impl.xsl"));
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            } catch (IllegalStateException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    }
                });
            } else {
                genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                                  org.netbeans.modules.web.project.WebProject.class.getResource("resources/build-impl.xsl"),
                                                  false);
            }
            genFilesHelper.refreshBuildScript(
                getBuildXmlName (),
                WebProject.class.getResource("resources/build.xsl"),false);
        }
        
    }
    
    final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                getProjectDirectory().getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() throws IOException {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            public void run()  {
                                updateProject();
                            }
                        });
                    }
                });
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
            try {
                //DDDataObject initialization to be ready to listen on changes (#45771)

                // web.xml
                try {
                    FileObject ddFO = webModule.getDeploymentDescriptor();
                    if (ddFO != null) {
                        DataObject dobj = DataObject.find(ddFO);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    //PENDING
                }

                // ejb-jar.xml
                try {
                    FileObject ejbDdFO = webEjbJarProvider.getDeploymentDescriptor();
                    if (ejbDdFO != null) {
                        DataObject ejbdobj = DataObject.find(ejbDdFO);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    //PENDING`
                }
                
                // Register copy on save support
                css.initialize();
                
                // Check up on build scripts.
                if (updateHelper.isCurrent()) {
                    int flags = genFilesHelper.getBuildScriptState(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        WebProject.class.getResource("resources/build-impl.xsl"));
                    if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0
                        && (flags & GeneratedFilesHelper.FLAG_OLD_PROJECT_XML) != 0) {
                        JButton updateOption = new JButton (NbBundle.getMessage(WebProject.class, "CTL_Regenerate"));
                        if (DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor (NbBundle.getMessage(WebProject.class,"TXT_BuildImplRegenerate"),
                                NbBundle.getMessage(WebProject.class,"TXT_BuildImplRegenerateTitle"),
                                NotifyDescriptor.DEFAULT_OPTION,
                                NotifyDescriptor.WARNING_MESSAGE,
                                new Object[] {
                                    updateOption,
                                    NotifyDescriptor.CANCEL_OPTION
                                },
                                updateOption)) == updateOption) {
                            genFilesHelper.generateBuildScriptFromStylesheet(
                                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                WebProject.class.getResource("resources/build-impl.xsl"));
                        }
                    } else {
                        genFilesHelper.refreshBuildScript(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            WebProject.class.getResource("resources/build-impl.xsl"), true);
                    }
                    genFilesHelper.refreshBuildScript(
                        getBuildXmlName(),
                        WebProject.class.getResource("resources/build.xsl"), true);

                    String servInstID = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
                    String serverType = null;
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                    if (platform != null) {
                        // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                        WebProjectProperties.setServerInstance(WebProject.this, WebProject.this.updateHelper, servInstID);
                    } else {
                        // if there is some server instance of the type which was used
                        // previously do not ask and use it
                        serverType = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_TYPE);
                        if (serverType != null) {
                            String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                            if (servInstIDs.length > 0) {
                                WebProjectProperties.setServerInstance(WebProject.this, WebProject.this.updateHelper, servInstIDs[0]);
                                platform = Deployment.getDefault().getJ2eePlatform(servInstIDs[0]);
                            }
                        }
                        if (platform == null) {
                            BrokenServerSupport.showAlert();
                        }
                    }
                    // UI Logging
                    Utils.logUI(NbBundle.getBundle(WebProject.class), "UI_WEB_PROJECT_OPENED", // NOI18N
                            new Object[] {(serverType != null ? serverType : Deployment.getDefault().getServerID(servInstID)), servInstID});
                    // Usage Logging
                    String serverName = ""; // NOI18N
                    try {
                        if (servInstID != null) {
                            serverName = Deployment.getDefault().getServerInstance(servInstID).getServerDisplayName();
                        }
                    }
                    catch (InstanceRemovedException ire) {
                        // do nothing
                    }
                    Utils.logUsage(WebProject.class, "USG_PROJECT_OPEN_WEB", new Object[] { serverName }); // NOI18N
                }
                
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
            // register project's classpaths to GlobalPathRegistry
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
                                
            // initialize the server configuration
            // it MUST BE called AFTER classpaths are registered to GlobalPathRegistry!
            // DDProvider (used here) needs classpath set correctly when resolving Java Extents for annotations
            webModule.getConfigSupport().ensureConfigurationReady();
            
            //check the config context path
            String ctxRoot = webModule.getContextPath ();
            if (ctxRoot == null) {
                String sysName = getProjectDirectory ().getName (); //NOI18N
                sysName = Utils.createDefaultContext(sysName); //NOI18N
                webModule.setContextPath (sysName);
            }

            // TODO: dongmei Anything for EJBs???????
            
            if (Boolean.parseBoolean(evaluator().getProperty(
                    WebProjectProperties.J2EE_DEPLOY_ON_SAVE))) {
                Deployment.getDefault().enableCompileOnSaveSupport(webModule);
                // TODO: dongmei Anything for EJBs??????
            }
            artifactSupport.enableArtifactSynchronization(true);
            
            WebLogicalViewProvider logicalViewProvider = (WebLogicalViewProvider) WebProject.this.getLookup().lookup (WebLogicalViewProvider.class);
            if (logicalViewProvider != null &&  logicalViewProvider.hasBrokenLinks()) {   
                BrokenReferencesSupport.showAlert();
            }
            if(apiWebServicesSupport.isBroken(WebProject.this)) {
                apiWebServicesSupport.showBrokenAlert(WebProject.this);
            }
            else if(apiWebServicesClientSupport.isBroken(WebProject.this)) {
                apiWebServicesClientSupport.showBrokenAlert(WebProject.this);
            }
            webPagesFileWatch.init();
            webInfFileWatch.init();
        }
        
        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
            ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N

            // set jaxws.endorsed.dir property (for endorsed mechanism to be used with wsimport, wsgen)
            WSUtils.setJaxWsEndorsedDirProperty(ep);
            
            filterBrokenLibraryRefs();

            EditableProperties props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them

            // #134642 - use Ant task from copylibs library
            SharabilityUtility.makeSureProjectHasCopyLibsLibrary(helper, refHelper);

            J2EEProjectProperties.removeObsoleteLibraryLocations(ep);
            J2EEProjectProperties.removeObsoleteLibraryLocations(props);

            //add webinf.dir required by 6.0 projects
            if (props.getProperty(WebProjectProperties.WEBINF_DIR) == null) {
                //we can do this because in previous versions WEB-INF was expected under docbase
                String web = props.get(WebProjectProperties.WEB_DOCBASE_DIR);
                props.setProperty(WebProjectProperties.WEBINF_DIR, web + "/WEB-INF"); //NOI18N
            }
            
            //add persistence.xml.dir introduced in 6.5 - see issue 143884 and 142164
            if (props.getProperty(WebProjectProperties.PERSISTENCE_XML_DIR) == null) {
                props.setProperty(WebProjectProperties.PERSISTENCE_XML_DIR, "${" + WebProjectProperties.CONF_DIR + "}");
            }

            updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

            // update a dual build directory project to use a single directory
            if (updateHelper.isCurrent()) { // #113297, #118187
                // this operation should be safe in future as well - of course if properties with the same name weren't re-introduced
                props.remove("build.ear.web.dir");      // used to be WebProjectProperties.BUILD_EAR_WEB_DIR    // NOI18N
                props.remove("build.ear.classes.dir");  // used to be WebProjectProperties.BUILD_EAR_CLASSES_DIR    // NOI18N
            }
            // check debug.classpath - can be done every time, whenever
            String debugClassPath = props.getProperty(WebProjectProperties.DEBUG_CLASSPATH);
            props.setProperty(WebProjectProperties.DEBUG_CLASSPATH, Utils.correctDebugClassPath(debugClassPath));

            if (!props.containsKey(ProjectProperties.INCLUDES)) {
                props.setProperty(ProjectProperties.INCLUDES, "**"); // NOI18N
            }
            if (!props.containsKey(ProjectProperties.EXCLUDES)) {
                props.setProperty(ProjectProperties.EXCLUDES, ""); // NOI18N
            }
            if (!props.containsKey("build.generated.sources.dir")) { // NOI18N
                props.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
            }

            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);

            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        /**
         * Filters the broken library references (see issue 110040).
         */
        private void filterBrokenLibraryRefs() {
            // filter the compilation CP
            EditableProperties props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            List<ClassPathSupport.Item> toRemove = filterBrokenLibraryItems(cpMod.getClassPathSupport().itemsList(props.getProperty(ProjectProperties.JAVAC_CLASSPATH), WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
            if (!toRemove.isEmpty()) {
                LOGGER.log(Level.FINE, "Will remove broken classpath library references: " + toRemove);
                try {
                    ClassPathModifierSupport.handleLibraryClassPathItems(WebProject.this, getAntProjectHelper(), cpMod.getClassPathSupport(),  
                            toRemove, ProjectProperties.JAVAC_CLASSPATH, WebProjectProperties.TAG_WEB_MODULE_LIBRARIES, ClassPathModifier.REMOVE, false);
                } catch (IOException e) {
                    // should only occur when passing true as the saveProject parameter which we are not doing here
                    Exceptions.printStackTrace(e);
                }
            }
            // filter the additional (packaged) items
            // need to re-read the properites as the handleLibraryClassPathItems() might have changed them
            props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            toRemove = filterBrokenLibraryItems(libMod.getClassPathSupport().itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL), WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
            if (!toRemove.isEmpty()) {
                LOGGER.log(Level.FINE, "Will remove broken additional library references: " + toRemove);
                try {
                    ClassPathModifierSupport.handleLibraryClassPathItems(WebProject.this, getAntProjectHelper(), cpMod.getClassPathSupport(),  
                            toRemove, WebProjectProperties.WAR_CONTENT_ADDITIONAL, WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES, ClassPathModifier.REMOVE, false);
                } catch (IOException e) {
                    // should only occur when passing true as the saveProject parameter which we are not doing here
                    Exceptions.printStackTrace(e);
                }
            }
        }
        
        private List<ClassPathSupport.Item> filterBrokenLibraryItems(List<ClassPathSupport.Item> items) {
            List<ClassPathSupport.Item> toRemove = new LinkedList<ClassPathSupport.Item>();
            Collection<? extends BrokenLibraryRefFilter> filters = null;
            for (ClassPathSupport.Item item : items) {
                if (!item.isBroken() || item.getType() != ClassPathSupport.Item.TYPE_LIBRARY) {
                    continue;
                }
                String libraryName = ClassPathSupport.getLibraryNameFromReference(item.getReference());
                LOGGER.log(Level.FINE, "Broken reference to library: " + libraryName);
                if (filters == null) {
                    // initializing the filters lazily because usually they will not be needed anyway
                    // (most projects have no broken references)
                    filters = createFilters(WebProject.this);
                }
                for (BrokenLibraryRefFilter filter : filters) {
                    if (filter.removeLibraryReference(libraryName)) {
                        LOGGER.log(Level.FINE, "Will remove broken reference to library " + libraryName + " because of filter " + filter.getClass().getName());
                        toRemove.add(item);
                        break;
                    }
                }
            }
            return toRemove;
        }
        
        private List<BrokenLibraryRefFilter> createFilters(Project project) {
            List<BrokenLibraryRefFilter> filters = new LinkedList<BrokenLibraryRefFilter>();
            for (BrokenLibraryRefFilterProvider provider : Lookups.forPath("Projects/org-netbeans-modules-web-project/BrokenLibraryRefFilterProviders").lookupAll(BrokenLibraryRefFilterProvider.class)) { // NOI18N
                BrokenLibraryRefFilter filter = provider.createFilter(project);
                if (filter != null) {
                    filters.add(filter);
                }
            }
            return filters;
        }
        
        protected void projectClosed() {
            webPagesFileWatch.reset();
            webInfFileWatch.reset();

            // listen to j2ee platform classpath changes
            String servInstID = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
            if (servInstID != null) {
                try {
                    J2eePlatform platform = Deployment.getDefault().getServerInstance(servInstID).getJ2eePlatform();
                    if (platform != null) {
                        unregisterJ2eePlatformListener(platform);
                    }
                } catch (InstanceRemovedException ex) {
                    // ignore in this case
                }
            }
            
            // remove ServiceListener from jaxWsModel            
            //if (jaxWsModel!=null) jaxWsModel.removeServiceListener(jaxWsServiceListener);

            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            // Unregister copy on save support
            try {
                css.cleanup();
            } 
            catch (FileStateInvalidException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
            artifactSupport.enableArtifactSynchronization(false);
            Deployment.getDefault().disableCompileOnSaveSupport(webModule);
            // TODO: dongmei: anything for EJBs???????
            
            // unregister project's classpaths to GlobalPathRegistry
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
                }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR, "dist.war", evaluator(), "dist", "clean", WebProjectProperties.BUILD_FILE), // NOI18N
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE, "dist.ear.war", evaluator(), "dist-ear", "clean-ear", WebProjectProperties.BUILD_FILE) // NOI18N
            };
        }

    }
    
    // List of primarily supported templates

    private static final String[] TYPES = new String[] { 
        "java-classes",         // NOI18N
        "java-main-class",      // NOI18N
        "java-forms",           // NOI18N
        "java-beans",           // NOI18N
        "persistence",          // NOI18N
        "oasis-XML-catalogs",   // NOI18N
        "XML",                  // NOI18N
        "ant-script",           // NOI18N
        "ant-task",             // NOI18N
        "servlet-types",        // NOI18N
        "web-types",            // NOI18N
        "web-types-server",     // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "wsdl",                 // NOI18N
        "junit",                // NOI18N
        "simple-files"          // NOI18N
    };

    private static final String[] JAVAEE6_TYPES = new String[] {
        "java-classes",         // NOI18N
        "java-main-class",      // NOI18N
        "java-forms",           // NOI18N
        "java-beans",           // NOI18N
        "persistence",          // NOI18N
        "oasis-XML-catalogs",   // NOI18N
        "XML",                  // NOI18N
        "ant-script",           // NOI18N
        "ant-task",             // NOI18N
        "servlet-types",        // NOI18N
        "web-types",            // NOI18N
        "web-types-server",     // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "wsdl",                 // NOI18N
        "junit",                // NOI18N
        "simple-files",         // NOI18N
        
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "ejb-types_3_0"         // NOI18N
    };

    private static final String[] TYPES_ARCHIVE = new String[] { 
        "deployment-descriptor",          // NOI18N
        "XML",                            // NOI18N
    };
    
    private static final String[] PRIVILEGED_NAMES = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/WebServices/WebService.java",    // NOI18N
        "Templates/WebServices/WebServiceClient",   // NOI18N                    
        "Templates/Other/Folder"                    // NOI18N
    };
    
    private static final String[] PRIVILEGED_NAMES_EE5 = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/Persistence/Entity.java", // NOI18N
        "Templates/Persistence/RelatedCMP", // NOI18N                    
        "Templates/Persistence/JsfFromDB", // NOI18N                    
        "Templates/WebServices/WebService.java",    // NOI18N
        "Templates/WebServices/WebServiceFromWSDL.java",    // NOI18N
        "Templates/WebServices/WebServiceClient",   // NOI18N  
        "Templates/WebServices/RestServicesFromEntities", // NOI18N
        "Templates/WebServices/RestServicesFromPatterns",  //NOI18N
        "Templates/Other/Folder",                   // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES_ARCHIVE = new String[] {
        "Templates/JSP_Servlet/webXml",     // NOI18N  --- 
    };
    
    // guarded by this, #115809
    private String[] privilegedTemplatesEE5 = null;
    private String[] privilegedTemplates = null;

    // Path where instances of privileged templates are registered
    private static final String WEBTEMPLATE_PATH = "j2ee/webtier/templates"; //NOI18N
    
    synchronized String[] getPrivilegedTemplates() {
        ensureTemplatesInitialized();
        return privilegedTemplates;
    }

    synchronized String[] getPrivilegedTemplatesEE5() {
        ensureTemplatesInitialized();
        return privilegedTemplatesEE5;
    }
    
    public synchronized void resetTemplates() {
        privilegedTemplates = null;
        privilegedTemplatesEE5 = null;
    }
    
    private void ensureTemplatesInitialized() {
        assert Thread.holdsLock(this);
        if (privilegedTemplates != null
                && privilegedTemplatesEE5 != null) {
            return;
        }
        
        ArrayList<String>templatesEE5 = new ArrayList<String>(PRIVILEGED_NAMES_EE5.length + 1);
        ArrayList<String>templates = new ArrayList<String>(PRIVILEGED_NAMES.length + 1);

        // how many templates are added
        int countTemplate = 0;
        
        for (WebPrivilegedTemplates webPrivililegedTemplates : Lookups.forPath(WEBTEMPLATE_PATH).lookupAll(WebPrivilegedTemplates.class)) {
            String[] addedTemplates = webPrivililegedTemplates.getPrivilegedTemplates(apiWebModule);
            if (addedTemplates != null && addedTemplates.length > 0){
                countTemplate = countTemplate + addedTemplates.length;
                List<String> addedList = Arrays.asList(addedTemplates);
                templatesEE5.addAll(addedList);
                templates.addAll(addedList);
            }
        }

        if(countTemplate > 0){
            templatesEE5.addAll(Arrays.asList(PRIVILEGED_NAMES_EE5));
            privilegedTemplatesEE5 = templatesEE5.toArray(new String[templatesEE5.size()]);
            templates.addAll(Arrays.asList(PRIVILEGED_NAMES));
            privilegedTemplates = templates.toArray(new String[templates.size()]);
        }
        else {
            privilegedTemplatesEE5 = PRIVILEGED_NAMES_EE5;
            privilegedTemplates = PRIVILEGED_NAMES;
        }
    }
    
    private final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        private WebProject project;
        
        RecommendedTemplatesImpl (WebProject project) {
            this.project = project;
        }
        
        private boolean isEE5 = false;
        private boolean checked = false;
        private boolean isArchive = false;
        private boolean isEE6 = false;

        public String[] getRecommendedTypes() {
            String[] retVal = null;
            checkEnvironment();
            if (isArchive) {
                retVal = TYPES_ARCHIVE;
            } else if (isEE6){
                retVal = JAVAEE6_TYPES;
            }else{
                retVal = TYPES;
            }
           
            return retVal;
        }
        
        public String[] getPrivilegedTemplates() {
            String[] retVal = null;
            checkEnvironment();
            if (isArchive) {
                retVal = PRIVILEGED_NAMES_ARCHIVE;
            } else {
                if (isEE5) {
                    retVal = getPrivilegedTemplatesEE5();
                } else {
                    retVal = WebProject.this.getPrivilegedTemplates();
                }
            }
            return retVal;
        }
        
        private void checkEnvironment() {
            if (!checked) {
                final Object srcType = helper.getStandardPropertyEvaluator().
                        getProperty(WebProjectProperties.JAVA_SOURCE_BASED);
                if ("false".equals(srcType)) {
                    isArchive = true;
                }
                isEE5 = J2eeModule.JAVA_EE_5.equals(getAPIWebModule().getJ2eePlatformVersion());
                isEE6 = Capabilities.forProject(project).isEJB31Supported();
                checked = true;
            }
        }

    }

    private class DeployOnSaveSupportProxy implements DeployOnSaveSupport {

        public synchronized void addArtifactListener(ArtifactListener listener) {
            css.addArtifactListener(listener);
            artifactSupport.addArtifactListener(listener);
        }

        public synchronized void removeArtifactListener(ArtifactListener listener) {
            css.removeArtifactListener(listener);
            artifactSupport.removeArtifactListener(listener);
        }

        public boolean containsIdeArtifacts() {
            return DeployOnSaveUtils.containsIdeArtifacts(eval, updateHelper, "build.classes.dir");
        }
        
    }

    /**
     * This class handle copying of web resources to appropriate place in build
     * dir. User is not forced to perform redeploy on JSP change. This
     * class is also used in true Deploy On Save.
     *
     * Class should not request project lock from FS listener methods
     * (deadlock prone).
     */
    private class CopyOnSaveSupport extends FileChangeAdapter implements PropertyChangeListener {

        private FileObject docBase = null;

        private String docBaseValue = null;

        private FileObject webInf = null;

        private String webInfValue = null;

        private String buildWeb = null;

        private final List<ArtifactListener> listeners = new CopyOnWriteArrayList<ArtifactListener>();

        /** Creates a new instance of CopyOnSaveSupport */
        public CopyOnSaveSupport() {
            super();
        }

        public void addArtifactListener(ArtifactListener listener) {
            listeners.add(listener);
        }

        public void removeArtifactListener(ArtifactListener listener) {
            listeners.remove(listener);
        }

        public void initialize() throws FileStateInvalidException {
            docBase = getWebModule().getDocumentBase();
            docBaseValue = evaluator().getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
            webInf = getWebModule().getWebInf();
            webInfValue = evaluator().getProperty(WebProjectProperties.WEBINF_DIR);
            buildWeb = evaluator().getProperty(WebProjectProperties.BUILD_WEB_DIR);

            FileSystem docBaseFileSystem = null;
            if (docBase != null) {
                docBaseFileSystem = docBase.getFileSystem();
                docBaseFileSystem.addFileChangeListener(this);
            }

            if (webInf != null) {
                if (!webInf.getFileSystem().equals(docBaseFileSystem)) {
                    webInf.getFileSystem().addFileChangeListener(this);
                }
            }

            LOGGER.log(Level.FINE, "Web directory is {0}", docBaseValue);
            LOGGER.log(Level.FINE, "WEB-INF directory is {0}", webInfValue);

            WebProject.this.evaluator().addPropertyChangeListener(this);
        }

        public void cleanup() throws FileStateInvalidException {
            FileSystem docBaseFileSystem = null;
            if (docBase != null) {
                docBaseFileSystem = docBase.getFileSystem();
                docBaseFileSystem.removeFileChangeListener(this);
            }
            if (webInf != null) {
                if (!webInf.getFileSystem().equals(docBaseFileSystem)) {
                    webInf.getFileSystem().removeFileChangeListener(this);
                }
            }
            WebProject.this.evaluator().removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (WebProjectProperties.WEB_DOCBASE_DIR.equals(evt.getPropertyName())
                    || WebProjectProperties.WEBINF_DIR.equals(evt.getPropertyName())) {
                try {
                    cleanup();
                    initialize();
                } catch (org.openide.filesystems.FileStateInvalidException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            } else if (WebProjectProperties.BUILD_WEB_DIR.equals(evt.getPropertyName())) {
                // TODO copy all files ?
                Object value = evt.getNewValue();
                buildWeb = value == null ? null : value.toString();
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            try {
                FileObject fo = fe.getFile();
                handleCopyFileToDestDir(fo);

                FileObject webInf = getWebModule().resolveWebInf(docBaseValue, webInfValue, false, true);
                FileObject docBase = getWebModule().resolveDocumentBase(docBaseValue, false);

                if (webInf != null && FileUtil.isParentOf(webInf, fo)
                        && !(webInf.getParent() != null && webInf.getParent().equals(docBase))) {
                    // inside webinf
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(webInf, parent)) {
                        path = FileUtil.getRelativePath(webInf, fo.getParent())
                                + "/" + fe.getName() + "." + fe.getExt(); // NOI18N
                    } else {
                        path = fe.getName() + "." + fe.getExt(); // NOI18N
                    }
                    path = "WEB-INF/" + path;

                    if (!isSynchronizationAppropriate(path))  {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }

                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(docBase, parent)) {
                        path = FileUtil.getRelativePath(docBase, fo.getParent())
                                + "/" + fe.getName() + "." + fe.getExt(); // NOI18N
                    } else {
                        path = fe.getName() + "." + fe.getExt(); // NOI18N
                    }
                    if (!isSynchronizationAppropriate(path))  {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            try {
                FileObject fo = fe.getFile();

                FileObject webInf = getWebModule().resolveWebInf(docBaseValue, webInfValue, false, true);
                FileObject docBase = getWebModule().resolveDocumentBase(docBaseValue, false);

                if (webInf != null && FileUtil.isParentOf(webInf, fo)
                        && !(webInf.getParent() != null && webInf.getParent().equals(docBase))) {
                    // inside webInf
                    String path = "WEB-INF/" + FileUtil.getRelativePath(webInf, fo); // NOI18N
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }
                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    String path = FileUtil.getRelativePath(docBase, fo);
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        private boolean isSynchronizationAppropriate(String filePath) {
            if (filePath.startsWith("WEB-INF/classes")) { // NOI18N
                return false;
            }
            if (filePath.startsWith("WEB-INF/src")) { // NOI18N
                return false;
            }
            if (filePath.startsWith("WEB-INF/lib")) { // NOI18N
                return false;
            }
            return true;
        }

        private void fireArtifactChange(Iterable<ArtifactListener.Artifact> artifacts) {
            for (ArtifactListener listener : listeners) {
                listener.artifactsUpdated(artifacts);
            }
        }

        private void handleDeleteFileInDestDir(String resourcePath) throws IOException {
            File deleted = null;
            FileObject webBuildBase = buildWeb == null ? null : helper.resolveFileObject(buildWeb);
            if (webBuildBase != null) {
                // project was built
                FileObject toDelete = webBuildBase.getFileObject(resourcePath);
                if (toDelete != null) {
                    deleted = FileUtil.toFile(toDelete);
                    toDelete.delete();
                }
                if (deleted != null) {
                    fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(deleted)));
                }
            }
        }

        private void handleCopyFileToDestDir(FileObject fo) throws IOException {
            if (fo.isVirtual()) {
                return;
            }

            FileObject webInf = getWebModule().resolveWebInf(docBaseValue, webInfValue, false, true);
            FileObject docBase = getWebModule().resolveDocumentBase(docBaseValue, false);

            if (webInf != null && FileUtil.isParentOf(webInf, fo)
                    && !(webInf.getParent() != null && webInf.getParent().equals(docBase))) {
                handleCopyFileToDestDir("WEB-INF", webInf, fo); // NOI18N
            }
            if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                handleCopyFileToDestDir(null, docBase, fo);
            }
        }

        private void handleCopyFileToDestDir(String prefix, FileObject baseDir, FileObject fo) throws IOException {
            if (fo.isVirtual()) {
                return;
            }

            if (baseDir != null && FileUtil.isParentOf(baseDir, fo)) {
                // inside docbase
                String path = FileUtil.getRelativePath(baseDir, fo);
                if (prefix != null) {
                    path = prefix + "/" + path;
                }
                if (!isSynchronizationAppropriate(path)) {
                    return;
                }

                FileObject webBuildBase = buildWeb == null ? null : helper.resolveFileObject(buildWeb);
                if (webBuildBase != null) {
                    // project was built
                    if (FileUtil.isParentOf(baseDir, webBuildBase) || FileUtil.isParentOf(webBuildBase, baseDir)) {
                        //cannot copy into self
                        return;
                    }
                    FileObject destFile = ensureDestinationFileExists(webBuildBase, path, fo.isFolder());
                    assert destFile != null : "webBuildBase: " + webBuildBase + ", path: " + path + ", isFolder: " + fo.isFolder();
                    if (!fo.isFolder()) {
                        InputStream is = null;
                        OutputStream os = null;
                        FileLock fl = null;
                        try {
                            is = fo.getInputStream();
                            fl = destFile.lock();
                            os = destFile.getOutputStream(fl);
                            FileUtil.copy(is, os);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                            if (os != null) {
                                os.close();
                            }
                            if (fl != null) {
                                fl.releaseLock();
                            }
                            File file = FileUtil.toFile(destFile);
                            if (file != null) {
                                fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(file)));
                            }
                        }
                    }
                }
            }
        }

        /**
         * Returns the destination (parent) directory needed to create file
         * with relative path path under webBuilBase
         */
        private FileObject ensureDestinationFileExists(FileObject webBuildBase, String path, boolean isFolder) throws IOException {
            FileObject current = webBuildBase;
            StringTokenizer st = new StringTokenizer(path, "/");
            while (st.hasMoreTokens()) {
                String pathItem = st.nextToken();
                FileObject newCurrent = current.getFileObject(pathItem);
                if (newCurrent == null) {
                    // need to create it
                    if (isFolder || st.hasMoreTokens()) {
                        // create a folder
                        newCurrent = FileUtil.createFolder(current, pathItem);
                        assert newCurrent != null : "webBuildBase: " + webBuildBase + ", path: " + path + ", isFolder: " + isFolder;
                    } else {
                        newCurrent = FileUtil.createData(current, pathItem);
                        assert newCurrent != null : "webBuildBase: " + webBuildBase + ", path: " + path + ", isFolder: " + isFolder;
                    }
                }
                assert newCurrent != null : "webBuildBase: " + webBuildBase + ", path: " + path + ", isFolder: " + isFolder;
                current = newCurrent;
            }
            assert current != null : "webBuildBase: " + webBuildBase + ", path: " + path + ", isFolder: " + isFolder;
            return current;
        }
    }

    private class ArtifactCopySupport extends ArtifactCopyOnSaveSupport {

        public ArtifactCopySupport() {
            super(WebProjectProperties.BUILD_WEB_DIR, evaluator(), getAntProjectHelper());
        }

        @Override
        public Map<Item, String> getArtifacts() {
            final AntProjectHelper helper = getAntProjectHelper();

            ClassPathSupport cs = new ClassPathSupport(evaluator(), getReferenceHelper(),
                    helper, getUpdateHelper(), new ClassPathSupportCallbackImpl(helper));

            Map<Item, String> result = new HashMap<Item, String>();
            for (ClassPathSupport.Item item : cs.itemsList(
                    helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ProjectProperties.JAVAC_CLASSPATH),
                    WebProjectProperties.TAG_WEB_MODULE_LIBRARIES)) {

                if (!item.isBroken() && item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
                    String path = item.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT);
                    if (path != null) {
                        result.put(item, path);
                    }
                }
            }
            return result;
        }

        @Override
        protected Artifact filterArtifact(Artifact artifact) {
            if (containsTLD(artifact.getFile())) {
                return artifact;
            }

            return artifact.relocatable();
        }

        private boolean containsTLD(File f) {
            if (f.exists() && f.isFile() && f.canRead()) {
                ZipFile zip = null;
                try {
                    zip = new ZipFile(f);
                    for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
                        String zipEntryName = ((ZipEntry) entries.nextElement()).getName();
                        if (TLD_PATTERN.matcher(zipEntryName).matches()) {
                            return true;
                        }
                    }
                    return false;
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } finally {
                    if (zip != null) {
                        try {
                            zip.close();
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        }
                    }
                }
            }

            return false;
        }
    }

    public boolean isJavaEE5(Project project) {
        return J2eeModule.JAVA_EE_5.equals(getAPIWebModule().getJ2eePlatformVersion());
    }

    private static final class WebPropertyEvaluatorImpl implements WebPropertyEvaluator {
        private PropertyEvaluator evaluator;
        public WebPropertyEvaluatorImpl (PropertyEvaluator eval) {
            evaluator = eval;
        }
        public PropertyEvaluator evaluator() {
            return evaluator;
        }
    }
    
    private class WebExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "-do-init", "-init-check", "-post-clean", "jar", "-pre-pre-compile","-do-ws-compile","-do-compile","-do-compile-single", "-post-compile", "-pre-dist", //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return WebProject.this;
        }

    }

    private static final class CoSAwareFileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener
    {

        private final FileBuiltQueryImplementation delegate;
        private final WebProject project;
        private final AtomicBoolean cosEnabled = new AtomicBoolean();
        private final Map<FileObject, Reference<StatusImpl>> file2Status = new WeakHashMap<FileObject, Reference<StatusImpl>>();

        public CoSAwareFileBuiltQueryImpl(FileBuiltQueryImplementation delegate, WebProject project)
        {

            this.delegate = delegate;
            this.project = project;
            project.evaluator().addPropertyChangeListener(this);
            setCoSEnabledAndXor();

        }

        private synchronized StatusImpl readFromCache(FileObject file)
        {
            Reference<StatusImpl> r = file2Status.get(file);
            return r != null ? r.get() : null;

        }

        public Status getStatus(FileObject file)
        {
            StatusImpl result = readFromCache(file);
            if (result != null)
            {
                return result;
            }


            Status status = delegate.getStatus(file);
            if (status == null)
            {
                return null;
            }

            synchronized (this)
            {
                StatusImpl foisted = readFromCache(file);
                if (foisted != null)
                {
                    return foisted;
                }

                file2Status.put(file, new WeakReference<StatusImpl>(result = new StatusImpl(cosEnabled, status)));
            }

            return result;

        }

        boolean setCoSEnabledAndXor()
        {
            boolean nue = Boolean.parseBoolean(project.evaluator().getProperty(
                                     WebProjectProperties.J2EE_DEPLOY_ON_SAVE));
            boolean old = cosEnabled.getAndSet(nue);

            return old != nue;

        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if (!setCoSEnabledAndXor())
            {
                return;
            }

            Collection<Reference<StatusImpl>> toRefresh;

            synchronized (this)
            {
                toRefresh = new LinkedList<Reference<StatusImpl>>(file2Status.values());
            }

            for (Reference<StatusImpl> r : toRefresh)
            {
                StatusImpl s = r.get();

                if (s != null)
                {
                    s.stateChanged(null);
                }
            }
        }

        private static final class StatusImpl implements Status, ChangeListener
        {

            private final ChangeSupport cs = new ChangeSupport(this);
            private final AtomicBoolean cosEnabled;
            private final Status delegate;

            public StatusImpl(AtomicBoolean cosEnabled, Status delegate)
            {
                this.cosEnabled = cosEnabled;
                this.delegate = delegate;
            }

            public boolean isBuilt()
            {
                return cosEnabled.get() || delegate.isBuilt();
            }

            public void addChangeListener(ChangeListener l)
            {
                cs.addChangeListener(l);
            }

            public void removeChangeListener(ChangeListener l)
            {
                cs.removeChangeListener(l);
            }

            public void stateChanged(ChangeEvent e)
            {

                cs.fireChange();

            }
        }
    }
}
