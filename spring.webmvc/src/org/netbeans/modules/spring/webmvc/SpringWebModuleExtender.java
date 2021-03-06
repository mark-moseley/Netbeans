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
 *
 * Portions Copyrighted 2008 Craig MacKay.
 */

package org.netbeans.modules.spring.webmvc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.spring.api.SpringUtilities;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.ConfigFileManager;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.webmvc.utils.SpringWebFrameworkUtils;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * The WebModuleExtender implementation for Spring Web MVC.
 *
 * @author Craig MacKay
 */
public class SpringWebModuleExtender extends WebModuleExtender implements ChangeListener {      
    private static final Logger LOGGER = Logger.getLogger(SpringWebModuleExtender.class.getName());
    
    private final SpringWebFrameworkProvider framework;
    private final ExtenderController controller;
    private final boolean customizer;
    private SpringConfigPanelVisual component;
    private String dispatcherName = "dispatcher"; // NOI18N
    private String dispatcherMapping = "*.htm"; // NOI18N
    private boolean includeJstl = true;
    private ChangeSupport changeSupport = new ChangeSupport(this); 
    
    /**
     * Creates a new instance of SpringWebModuleExtender 
     * @param framework
     * @param controller an instance of org.netbeans.modules.web.api.webmodule.ExtenderController 
     * @param customizer     
     */
    public SpringWebModuleExtender(SpringWebFrameworkProvider framework, ExtenderController controller, boolean customizer) {
        this.framework = framework;
        this.controller = controller;
        this.customizer = customizer;       
    }
    
    public ExtenderController getController() {
        return controller;
    }

    public String getDispatcherName() {
        return dispatcherName;
    }

    public String getDispatcherMapping() {
        return dispatcherMapping;
    }

    public boolean getIncludeJstl() {
        return includeJstl;
    }

    public SpringConfigPanelVisual getComponent() {
        if (component == null) {
            component = new SpringConfigPanelVisual(this);
            component.setEnabled(!customizer);
        }
        return component;
    }

    public boolean isValid() {
        if (dispatcherName == null || dispatcherName.trim().length() == 0){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_DispatcherNameIsEmpty")); // NOI18N
            return false;
        }
               
        if (!SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid(dispatcherName)){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_DispatcherServletConfigFilenameIsNotValid")); 
            return false;
        }                

        if (dispatcherMapping == null || dispatcherMapping.trim().length() == 0) {
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_DispatcherMappingPatternIsEmpty")); // NOI18N
            return false;
        }
        if (!SpringWebFrameworkUtils.isDispatcherMappingPatternValid(dispatcherMapping)){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_DispatcherMappingPatternIsNotValid")); // NOI18N
            return false;
        }        
        controller.setErrorMessage(null);
        return true;    
    }
        
    public HelpCtx getHelp() {
        return new HelpCtx(SpringWebModuleExtender.class);
    }

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {       
        changeSupport.removeChangeListener(l);
    }
        
    public void stateChanged(ChangeEvent e) {
        dispatcherName = getComponent().getDispatcherName();
        dispatcherMapping = getComponent().getDispatcherMapping();
        includeJstl = getComponent().getIncludeJstl();
        changeSupport.fireChange();
    }

    @Override
    public void update() {
    // not used yet
    }

    @Override
    public Set<FileObject> extend(WebModule webModule) {
        CreateSpringConfig createSpringConfig = new CreateSpringConfig(webModule);
        FileObject webInf = webModule.getWebInf();
        if (webInf != null) {
            try {
                FileSystem fs = webInf.getFileSystem();
                fs.runAtomicAction(createSpringConfig);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }
        }
        return createSpringConfig.getFilesToOpen();
    }

    private class CreateSpringConfig implements FileSystem.AtomicAction {

        public static final String CONTEXT_LOADER = "org.springframework.web.context.ContextLoaderListener"; // NOI18N
        public static final String DISPATCHER_SERVLET = "org.springframework.web.servlet.DispatcherServlet"; // NOI18N        
        public static final String ENCODING = "UTF-8"; // NOI18N
        private Set<FileObject> filesToOpen = new LinkedHashSet<FileObject>();
        private WebModule webModule;

        public CreateSpringConfig(WebModule webModule) {
            this.webModule = webModule;
        }

        public void run() throws IOException {
            // MODIFY WEB.XML
            FileObject dd = webModule.getDeploymentDescriptor();
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            addContextParam(ddRoot, "contextConfigLocation", "/WEB-INF/applicationContext.xml"); // NOI18N
            addListener(ddRoot, CONTEXT_LOADER);
            addServlet(ddRoot, getComponent().getDispatcherName(), DISPATCHER_SERVLET, getComponent().getDispatcherMapping(), "2"); // NOI18N
            WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
            if (welcomeFiles == null) {
                try {
                    welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList"); // NOI18N
                    ddRoot.setWelcomeFileList(welcomeFiles);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (welcomeFiles.sizeWelcomeFile() == 0) {
                welcomeFiles.addWelcomeFile("redirect.jsp"); // NOI18N
            }
            ddRoot.write(dd);

            // ADD JSTL LIBRARY IF ENABLED AND SPRING LIBRARY
            List<Library> libraries = new ArrayList<Library>(3);
            Library webMVCLibrary = SpringUtilities.findSpringWebMVCLibrary();
            Library springLibrary = null;
            if (webMVCLibrary != null) {
                libraries.add(webMVCLibrary);
                if (SpringUtilities.isSpringLibrary(webMVCLibrary)) {
                    // In case this is an user library with a monolithic Spring.
                    springLibrary = webMVCLibrary;
                }
            } else {
                LOGGER.log(Level.WARNING, null, new Error("No Spring Web MVC library found."));
            }
            if (springLibrary == null) {
                springLibrary = SpringUtilities.findSpringLibrary();
                if (springLibrary != null){
                    libraries.add(springLibrary);
                } else {
                    LOGGER.log(Level.WARNING, null, new Error("No Spring Framework library found."));
                }
            }
            if (includeJstl) {
                Library jstlLibrary = SpringUtilities.findJSTLibrary();
                if (jstlLibrary != null) {
                    libraries.add(jstlLibrary);
                } else {
                    LOGGER.log(Level.WARNING, null, new Error("No JSTL library found."));
                }
            }
            if (!libraries.isEmpty()) {
                addLibrariesToWebModule(libraries, webModule);
            }
            
            // CREATE WEB-INF/JSP FOLDER
            FileObject webInf = webModule.getWebInf();
            FileObject jsp = webInf.createFolder("jsp");

            // COPY TEMPLATE SPRING RESOURCES (JSP, XML, PROPERTIES)
            copyResource("index.jsp", FileUtil.createData(jsp, "index.jsp")); // NOI18N
            copyResource("jdbc.properties", FileUtil.createData(webInf, "jdbc.properties")); // NOI18N
            final List<File> newFiles = new ArrayList<File>(2);
            FileObject configFile;
            configFile = copyResource("applicationContext.xml", FileUtil.createData(webInf, "applicationContext.xml")); // NOI18N
            addFileToOpen(configFile);
            newFiles.add(FileUtil.toFile(configFile));
            configFile = copyResource("dispatcher-servlet.xml", FileUtil.createData(webInf, getComponent().getDispatcherName() + "-servlet.xml")); // NOI18N
            addFileToOpen(configFile);
            newFiles.add(FileUtil.toFile(configFile));

            SpringScope scope = SpringScope.getSpringScope(configFile);
            if (scope != null) {
                final ConfigFileManager manager = scope.getConfigFileManager();
                try {
                    manager.mutex().writeAccess(new ExceptionAction<Void>() {
                        public Void run() throws IOException {
                            List<File> files = manager.getConfigFiles();
                            files.addAll(newFiles);
                            List<ConfigFileGroup> groups = manager.getConfigFileGroups();
                            String groupName = NbBundle.getMessage(SpringWebModuleExtender.class, "LBL_DefaultGroup");
                            ConfigFileGroup newGroup = ConfigFileGroup.create(groupName, newFiles);
                            groups.add(newGroup);
                            manager.putConfigFilesAndGroups(files, groups);
                            manager.save();
                            return null;
                        }
                    });
                } catch (MutexException e) {
                    throw (IOException)e.getException();
                }
            } else {
                LOGGER.log(Level.WARNING, "Could not find a SpringScope for file {0}", configFile);
            }

            // MODIFY EXISTING REDIRECT.JSP
            FileObject documentBase = webModule.getDocumentBase();
            FileObject redirectJsp = documentBase.getFileObject("redirect.jsp"); // NOI18N
            if (redirectJsp == null) {
                redirectJsp = FileUtil.createData(documentBase, "redirect.jsp"); // NOI18N
            }
            addFileToOpen(copyResource("redirect.jsp", redirectJsp)); // NOI18N
        }
               
        public void addFileToOpen(FileObject file) {
            filesToOpen.add(file);
        }

        public Set<FileObject> getFilesToOpen() {
            return filesToOpen;
        }

        protected FileObject copyResource(String resourceName, FileObject target) throws UnsupportedEncodingException, IOException {
            InputStream in = getClass().getResourceAsStream("resources/templates/" + resourceName); // NOI18N
            String lineSeparator = System.getProperty("line.separator"); // NOI18N
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, ENCODING));
            try {
                String line = reader.readLine();               
                while (line != null) {
                    // If an extension mapping is entered by the user, then update filename extensions in the Spring bean config file and index.jsp
                    if ((resourceName.contains("-servlet.xml") || ((resourceName.equals("redirect.jsp"))))) { // NOI18N
                        line = SpringWebFrameworkUtils.replaceExtensionInTemplates(line, dispatcherMapping);
                    }
                    if (resourceName.equals("redirect.jsp")) { // NOI18N
                        line = SpringWebFrameworkUtils.reviseRedirectJsp(line, dispatcherMapping);
                    }
                    if (resourceName.equals("index.jsp")) { // NOI18N
                        line = SpringWebFrameworkUtils.getWelcomePageText();
                    }
                    buffer.append(line);
                    buffer.append(lineSeparator);
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
            FileLock lock = target.lock();
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), ENCODING));
                try {
                    writer.write(buffer.toString());
                } finally {
                    writer.close();
                }
            } finally {
                lock.releaseLock();
            }
            return target;
        }

        protected boolean addLibrariesToWebModule(List<Library> libraries, WebModule webModule) throws IOException, UnsupportedOperationException {
            FileObject fileObject = webModule.getDocumentBase();
            Project project = FileOwnerQuery.getOwner(fileObject);
            if (project == null) {
                return false;
            }
            boolean addLibraryResult = false;
            try {
                SourceGroup[] groups = SourceGroups.getJavaSourceGroups(project);
                if (groups.length == 0) {
                    return false;
                }
                addLibraryResult = ProjectClassPathModifier.addLibraries(libraries.toArray(new Library[libraries.size()]), groups[0].getRootFolder(), ClassPath.COMPILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Libraries required for the Spring MVC project not added", e); // NOI18N
            } catch (UnsupportedOperationException uoe) {
                LOGGER.log(Level.WARNING, "This project does not support adding these types of libraries to the classpath", uoe); // NOI18N
            }
            return addLibraryResult;
        }

        protected Listener addListener(WebApp webApp, String classname) throws IOException {
            Listener listener = (Listener) createBean(webApp, "Listener"); // NOI18N
            listener.setListenerClass(classname);
            webApp.addListener(listener);
            return listener;
        }

        protected Servlet addServlet(WebApp webApp, String name, String classname, String pattern, String loadOnStartup) throws IOException {
            Servlet servlet = (Servlet) createBean(webApp, "Servlet"); // NOI18N
            servlet.setServletName(name);
            servlet.setServletClass(classname);
            if (loadOnStartup != null) {
                servlet.setLoadOnStartup(new BigInteger(loadOnStartup));
            }
            webApp.addServlet(servlet);
            if (pattern != null) {
                addServletMapping(webApp, name, pattern);
            }
            return servlet;
        }

        protected ServletMapping addServletMapping(WebApp webApp, String name, String pattern) throws IOException {
            ServletMapping mapping = (ServletMapping) createBean(webApp, "ServletMapping"); // NOI18N
            mapping.setServletName(name);
            mapping.setUrlPattern(pattern);
            webApp.addServletMapping(mapping);
            return mapping;
        }

        protected InitParam addContextParam(WebApp webApp, String name, String value) throws IOException {
            InitParam initParam = (InitParam) createBean(webApp, "InitParam"); // NOI18N
            initParam.setParamName(name);
            initParam.setParamValue(value);
            webApp.addContextParam(initParam);
            return initParam;
        }

        protected CommonDDBean createBean(CreateCapability creator, String beanName) throws IOException {
            CommonDDBean bean = null;
            try {
                bean = creator.createBean(beanName);
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                throw new IOException("Error creating bean with name:" + beanName); // NOI18N
            }
            return bean;
        }
    }
}
