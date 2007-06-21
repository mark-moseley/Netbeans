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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.project.jsf.framework;

import org.netbeans.modules.visualweb.project.jsf.JsfProjectTemplateJakarta;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.ProjectTemplate;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.Repository;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.loaders.DataObject;
import org.openide.cookies.OpenCookie;

/**
 *
 * @author Po-Ting Wu
 */
public class JSFFrameworkProvider extends WebFrameworkProvider {
    private static final String FACES_STATE_SAVING_METHOD = "javax.faces.STATE_SAVING_METHOD"; // NOI18N
    private static final String FACES_VALIDATE_XML = "com.sun.faces.validateXml"; // NOI18N
    private static final String FACES_VERIFY_OBJECTS = "com.sun.faces.verifyObjects"; // NOI18N
    
    private JSFConfigurationPanel panel;
    /** Creates a new instance of JSFFrameworkProvider */
    public JSFFrameworkProvider() {
        super(
                NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Name"),               // NOI18N
                NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Description"));       //NOI18N
    }
    
    public Set extend(final WebModule webModule) {
        final FileObject fileObject = webModule.getDocumentBase();
        final Project project = FileOwnerQuery.getOwner(fileObject);
        final ProjectTemplate template = new JsfProjectTemplateJakarta();
        Set result = new HashSet();
              
        // Set Bean Package and Start Page
        String presetPackage = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE);
        if (presetPackage == null || presetPackage.length() == 0) {
            presetPackage = panel.getBeanPackage();
            JsfProjectUtils.createProjectProperty(project, JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE, presetPackage);
        }
        template.setBeanPackage(presetPackage);

        String presetName = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_START_PAGE);
        if (presetName == null || presetName.length() == 0) {
            presetName = "Page1.jsp"; // NOI18N
        }
        final String pageName = presetName;
        JsfProjectUtils.createProjectProperty(project, JsfProjectConstants.PROP_START_PAGE, pageName);
        JsfProjectUtils.setProjectVersion(project, "4.0"); // NOI18N

        updateNode(project);

        // Create Visual Web files
        ProjectManager.mutex().postReadRequest(new Runnable() {
            public void run() {
                try{
                    project.getProjectDirectory().setAttribute("NewProject", Boolean.TRUE); // NOI18N
                    template.create(project, webModule.getJ2eePlatformVersion(), pageName);
                } catch (IOException ioe){
                    Exceptions.printStackTrace(ioe);
                }
           }
        }); 

        // <RAVE> Add the VWP libraries to the project
        try {
            ClassPath cp = ClassPath.getClassPath(fileObject, ClassPath.COMPILE);
            boolean isMyFaces = cp.findResource("org/apache/myfaces/webapp/StartupServletContextListener.class") != null; //NOI18N
            if (!isMyFaces && (cp.findResource("javax/faces/FacesException.class") == null)) { //NOI18N
                Library jsfLib = null;
                if (JsfProjectUtils.isJavaEE5Project(project)) {
                    jsfLib = LibraryManager.getDefault().getLibrary("jsf12");
                } else {
                    JSFConfigurationPanel.LibraryType libraryType = panel.getLibraryType();
                    if (libraryType != null && libraryType != JSFConfigurationPanel.LibraryType.NONE) {
                        if (libraryType == JSFConfigurationPanel.LibraryType.USED) {
                            jsfLib = panel.getLibrary();
                        } else if (libraryType == JSFConfigurationPanel.LibraryType.NEW) {
                            String libraryVersion = panel.getNewLibraryVersion();
                            File installFolder = panel.getInstallFolder();
                            if (installFolder != null && libraryVersion != null) {
                                try {
                                    JSFUtils.createJSFUserLibrary(installFolder, libraryVersion);
                                    jsfLib = JSFUtils.getJSFLibrary(libraryVersion);
                                } catch (IOException ioe) {
                                    Exceptions.printStackTrace(ioe);
                                }
                            }
                        }
                    }
                }
                
                if (jsfLib != null) {
                    try {
                        JsfProjectUtils.addLibraryReferences(project, new Library[] {
                            jsfLib,
                            LibraryManager.getDefault().getLibrary("jstl11"),
                        });
                    } catch (IOException ioe) {
                        // Exceptions.printStackTrace(ioe);
                    }
                }
            }
            template.addLibrary(project);

            FileSystem fileSystem = webModule.getWebInf().getFileSystem();
            fileSystem.runAtomicAction(new CreateFacesConfig(webModule, isMyFaces, pageName));

            FileObject pagejsp = fileObject.getFileObject(pageName);
            if (pagejsp != null) {
                result.add(pagejsp);
            } else {
                // Page is not created yet, open later.
                ProjectManager.mutex().postReadRequest(new Runnable() {
                    public void run() {
                        try {
                            FileObject pagejsp = fileObject.getFileObject(pageName);
                            if (pagejsp != null) {
                                DataObject obj = DataObject.find(pagejsp);
                                OpenCookie open = (OpenCookie) obj.getCookie(OpenCookie.class);
                                if (open != null) {
                                    open.open();
                                }
                            }
                        } catch (IOException ioe){
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                }); 
            }
        } catch (FileNotFoundException exc) {
            Exceptions.printStackTrace(exc);
        } catch (IOException exc) {
            Exceptions.printStackTrace(exc);
        }
        return result;
    }
    
    public static String readResource(InputStream is, String encoding) throws IOException {
        // read the config from resource first
        StringBuffer sbuffer = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line = br.readLine();
        while (line != null) {
            sbuffer.append(line);
            sbuffer.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sbuffer.toString();
    }
    
    public java.io.File[] getConfigurationFiles(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        // The JavaEE 5 introduce web modules without deployment descriptor. In such wm can not be jsf used.
        FileObject dd = wm.getDeploymentDescriptor();
        if (dd != null){
            FileObject[] filesFO = JSFConfigUtilities.getConfiFilesFO(wm.getDeploymentDescriptor());
            File[] files = new File[filesFO.length];
            for (int i = 0; i < filesFO.length; i++)
                files[i] = FileUtil.toFile(filesFO[i]);
            if (files.length > 0)
                return files;
        }
        return null;
    }
    
    public FrameworkConfigurationPanel getConfigurationPanel(WebModule webModule) {
        boolean defaultValue = (webModule == null || !isInWebModule(webModule));
        panel = new JSFConfigurationPanel(!defaultValue);

        // Default Bean Package
        if (webModule != null) {
            Project project = FileOwnerQuery.getOwner(webModule.getDeploymentDescriptor());
            panel.setBeanPackage(project.getProjectDirectory().getName());
        }

        if (!defaultValue){
            // get configuration panel with values from the wm
            Servlet servlet = JSFConfigUtilities.getActionServlet(webModule.getDeploymentDescriptor());
            panel.setServletName(servlet.getServletName());
            panel.setURLPattern(JSFConfigUtilities.getActionServletMapping(webModule.getDeploymentDescriptor()));
            panel.setValidateXML(JSFConfigUtilities.validateXML(webModule.getDeploymentDescriptor()));
            panel.setVerifyObjects(JSFConfigUtilities.verifyObjects(webModule.getDeploymentDescriptor()));
        }
        
        return panel;
    }
    
    public boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule webModule) {
        FileObject documentBase = webModule.getDocumentBase();
        Project project = FileOwnerQuery.getOwner(documentBase);
        return JsfProjectUtils.isJsfProject(project);
    }
    
    public static void createFile(FileObject target, String content, String encoding) throws IOException{
        FileLock lock = target.lock();
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), encoding));
            bw.write(content);
            bw.close();
            
        } finally {
            lock.releaseLock();
        }
    }
    
    private class  CreateFacesConfig implements FileSystem.AtomicAction{
        WebModule webModule;
        boolean isMyFaces;
        String pageName;
        
        public CreateFacesConfig(WebModule webModule, boolean isMyFaces, String pageName){
            this.webModule = webModule;
            this.isMyFaces = isMyFaces;
            this.pageName = pageName;
        }
        
        public void run() throws IOException {
            // Enter servlet into the deployment descriptor
            FileObject dd = webModule.getDeploymentDescriptor();
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            String j2eeLevel = webModule.getJ2eePlatformVersion();
            if (ddRoot != null){
                try{
                    // Set the context parameter
                    InitParam facesSaving = null;
                    InitParam facesValidate = null;
                    InitParam facesVerify = null;
                    InitParam[] params = ddRoot.getContextParam();
                    for (int i = 0; i < params.length; i++) {
                        InitParam ip = params[i];
                        String name = ip.getParamName();
                        if (FACES_STATE_SAVING_METHOD.equals(name)) {
                            facesSaving = ip;
                        } else if (FACES_VALIDATE_XML.equals(name)) {
                            facesValidate = ip;
                        } else if (FACES_VERIFY_OBJECTS.equals(name)) {
                            facesVerify = ip;
                        }
                    }

                    if (facesSaving == null) {
                        facesSaving = (InitParam)ddRoot.createBean("InitParam"); // NOI18N
                        facesSaving.setParamName(FACES_STATE_SAVING_METHOD);
                        facesSaving.setParamValue("client"); // NOI18N
                        ddRoot.addContextParam(facesSaving);
                    }
                    
                    String value = (panel == null || panel.validateXML()) ? "true" : "false"; // NOI18N
                    if (facesValidate == null) {
                        facesValidate = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                        facesValidate.setParamName(FACES_VALIDATE_XML);
                        facesValidate.setParamValue(value);
                        ddRoot.addContextParam(facesValidate);
                    } else {
                        facesValidate.setParamValue(value);
                    }
                    
                    value = (panel == null || panel.verifyObjects()) ? "true" : "false"; // NOI18N
                    if (facesVerify == null) {
                        facesVerify = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                        facesVerify.setParamName(FACES_VERIFY_OBJECTS);
                        facesVerify.setParamValue(value);
                        ddRoot.addContextParam(facesVerify);
                    } else {
                        facesVerify.setParamValue(value);
                    }
                    
                    // The UpLoad Filter
                    Filter filter;
                    InitParam contextParam;
                    boolean hasUploadFilter = false;
                    Filter[] filters = ddRoot.getFilter();
                    for (int i = 0; i < filters.length; i++) {
                        filter = filters[i];
                        if ("UploadFilter".equals(filter.getFilterName())) {
                            hasUploadFilter = true;
                            break;
                        }
                    }

                    if (!hasUploadFilter) {
                        filter = (Filter)ddRoot.createBean("Filter"); // NOI18N
                        filter.setFilterName("UploadFilter"); // NOI18N
                        if (J2eeModule.JAVA_EE_5.equals(j2eeLevel))
                            filter.setFilterClass("com.sun.webui.jsf.util.UploadFilter"); // NOI18N
                        else
                            filter.setFilterClass("com.sun.rave.web.ui.util.UploadFilter"); // NOI18N
                    
                        contextParam = (InitParam)filter.createBean("InitParam"); // NOI18N
                        contextParam.setDescription("The maximum allowed upload size in bytes.  If this is set " +
                                "to a negative value, there is no maximum.  The default " +
                                "value is 1000000."); // NOI18N
                        contextParam.setParamName("maxSize"); // NOI18N
                        contextParam.setParamValue("1000000"); // NOI18N
                        filter.addInitParam(contextParam);
                        
                        contextParam = (InitParam)filter.createBean("InitParam"); // NOI18N
                        contextParam.setDescription("The size (in bytes) of an uploaded file which, if it is " +
                                "exceeded, will cause the file to be written directly to " +
                                "disk instead of stored in memory.  Files smaller than or " +
                                "equal to this size will be stored in memory.  The default " +
                                "value is 4096."); // NOI18N
                        contextParam.setParamName("sizeThreshold"); // NOI18N
                        contextParam.setParamValue("4096"); // NOI18N
                        filter.addInitParam(contextParam);
                        ddRoot.addFilter(filter);
                        
                        FilterMapping filterMapping = (FilterMapping)ddRoot.createBean("FilterMapping"); // NOI18N
                        filterMapping.setFilterName("UploadFilter"); // NOI18N
                        filterMapping.setServletName(panel.getServletName());
                        ddRoot.addFilterMapping(filterMapping);
                    }
                    
                    // The Servlets
                    Servlet servlet;
                    boolean hasFacesServlet = false;
                    boolean hasExceptionServlet = false;
                    boolean hasThemeServlet = false;
                    Servlet[] servlets = ddRoot.getServlet();
                    for (int i = 0; i < servlets.length; i++) {
                        servlet = servlets[i];
                        String name = servlet.getServletName();
                        if (panel.getServletName().equals(name)) {
                            hasFacesServlet = true;
                        } else if ("ExceptionHandlerServlet".equals(name)) {
                            hasExceptionServlet = true;
                        } else if ("ThemeServlet".equals(name)) {
                            hasThemeServlet = true;
                        }
                    }

                    if (!hasFacesServlet) {
                        servlet = (Servlet)ddRoot.createBean("Servlet"); // NOI18N
                        servlet.setServletName(panel.getServletName());
                        servlet.setServletClass("javax.faces.webapp.FacesServlet"); // NOI18N    
                        if (J2eeModule.JAVA_EE_5.equals(j2eeLevel)) {
                            contextParam = (InitParam)servlet.createBean("InitParam"); // NOI18N
                            contextParam.setParamName("javax.faces.LIFECYCLE_ID"); // NOI18N
                            contextParam.setParamValue("com.sun.faces.lifecycle.PARTIAL"); // NOI18N
                            servlet.addInitParam(contextParam);
                        }
                        servlet.setLoadOnStartup(new BigInteger("1"));// NOI18N
                        ddRoot.addServlet(servlet);
                    }

                    if (!hasExceptionServlet) {
                        servlet = (Servlet)ddRoot.createBean("Servlet"); // NOI18N
                        servlet.setServletName("ExceptionHandlerServlet");
                        servlet.setServletClass("com.sun.errorhandler.ExceptionHandler"); // NOI18N    

                        contextParam = (InitParam)servlet.createBean("InitParam"); // NOI18N
                        contextParam.setParamName("errorHost"); // NOI18N
                        contextParam.setParamValue("localhost"); // NOI18N
                        servlet.addInitParam(contextParam);

                        contextParam = (InitParam)servlet.createBean("InitParam"); // NOI18N
                        contextParam.setParamName("errorPort"); // NOI18N
                        contextParam.setParamValue("24444"); // NOI18N
                        servlet.addInitParam(contextParam);

                        ddRoot.addServlet(servlet);
                    }

                    if (!hasThemeServlet) {
                        servlet = (Servlet)ddRoot.createBean("Servlet"); // NOI18N
                        servlet.setServletName("ThemeServlet"); // NOI18N

                        if (J2eeModule.JAVA_EE_5.equals(j2eeLevel))
                            servlet.setServletClass("com.sun.webui.theme.ThemeServlet"); // NOI18N
                        else
                            servlet.setServletClass("com.sun.rave.web.ui.theme.ThemeServlet"); // NOI18N

                        ddRoot.addServlet(servlet);
                    }
                    
                    // The Servlet Mappings
                    ServletMapping mapping;
                    boolean hasFacesPattern = false;
                    ServletMapping[] maps = ddRoot.getServletMapping();
                    for (int i = 0; i < maps.length; i++) {
                        mapping = maps[i];
                        if (panel.getServletName().equals(mapping.getServletName()) &&
                            panel.getURLPattern().equals(mapping.getUrlPattern())) {
                            hasFacesPattern = true;
                        }
                    }

                    if (!hasFacesPattern) {
                        mapping = (ServletMapping)ddRoot.createBean("ServletMapping"); // NOI18N
                        mapping.setServletName(panel.getServletName());
                        mapping.setUrlPattern(panel.getURLPattern());
                        ddRoot.addServletMapping(mapping);
                    }

                    mapping = (ServletMapping)ddRoot.createBean("ServletMapping"); // NOI18N
                    mapping.setServletName("ExceptionHandlerServlet");
                    mapping.setUrlPattern("/error/ExceptionHandler");
                    ddRoot.addServletMapping(mapping);

                    mapping = (ServletMapping)ddRoot.createBean("ServletMapping"); // NOI18N
                    mapping.setServletName("ThemeServlet"); // NOI18N
                    mapping.setUrlPattern("/theme/*"); // NOI18N
                    ddRoot.addServletMapping(mapping);

                    // Adjust the path to the startpage based on JSF parameters
                    String welcomeFile = JsfProjectUtils.getWelcomeFile(panel.getURLPattern(), pageName);
                    WelcomeFileList wfl = ddRoot.getSingleWelcomeFileList();
                    if (wfl == null) {
                        wfl = (WelcomeFileList) ddRoot.createBean("WelcomeFileList");
                        ddRoot.setWelcomeFileList(wfl);
                    }
                    wfl.setWelcomeFile(new String[] { welcomeFile });

                    // Catch ServletException
                    ErrorPage errorPage = (ErrorPage)ddRoot.createBean("ErrorPage");
                    errorPage.setExceptionType("javax.servlet.ServletException");
                    errorPage.setLocation("/error/ExceptionHandler");
                    ddRoot.addErrorPage(errorPage);

                    // Catch IOException
                    errorPage = (ErrorPage)ddRoot.createBean("ErrorPage");
                    errorPage.setExceptionType("java.io.IOException");
                    errorPage.setLocation("/error/ExceptionHandler");
                    ddRoot.addErrorPage(errorPage);

                    // Catch FacesException
                    errorPage = (ErrorPage)ddRoot.createBean("ErrorPage");
                    errorPage.setExceptionType("javax.faces.FacesException");
                    errorPage.setLocation("/error/ExceptionHandler");
                    ddRoot.addErrorPage(errorPage);

                    // Catch ApplicationException
                    errorPage = (ErrorPage)ddRoot.createBean("ErrorPage");
                    errorPage.setExceptionType("com.sun.rave.web.ui.appbase.ApplicationException");
                    errorPage.setLocation("/error/ExceptionHandler");
                    ddRoot.addErrorPage(errorPage);

                    // The JSP Configuration
                    if (!J2eeModule.J2EE_13.equals(j2eeLevel)) {
                        try {
                            JspConfig jspConfig = ddRoot.getSingleJspConfig();
                            if (jspConfig == null) {
                                jspConfig = (JspConfig)ddRoot.createBean("JspConfig"); // NOI18N
                                ddRoot.addJspConfig(jspConfig);
                            }

                            JspPropertyGroup jspGroup = (JspPropertyGroup)jspConfig.createBean("JspPropertyGroup"); // NOI18N
                            jspGroup.addUrlPattern("*.jspf");
                            jspGroup.setIsXml(true);
                            jspConfig.addJspPropertyGroup(jspGroup);
                        } catch (VersionNotSupportedException e) {
                            // already exclude J2EE 1.3 project here
                        }
                    }
                    
                    if (isMyFaces) {
                        Listener facesListener = (Listener) ddRoot.createBean("Listener");
                        facesListener.setListenerClass("org.apache.myfaces.webapp.StartupServletContextListener");
                        ddRoot.addListener(facesListener);
                    }
                    ddRoot.write(dd);
                    
                    
                } catch (ClassNotFoundException cnfe){
                    Exceptions.printStackTrace(cnfe);
                }
            }
            
            // copy faces-config.xml
            File fileConfig = new File(FileUtil.toFile(webModule.getWebInf()), "faces-config.xml"); // NOI18N
            if (!fileConfig.exists()) {
                String facesConfigTemplate = "faces-config.xml"; //NOI18N
                if (ddRoot != null) {
                    if (WebApp.VERSION_2_5.equals(ddRoot.getVersion())) {
                        facesConfigTemplate = "faces-config_1_2.xml"; //NOI18N
                    }
                }
                String content = readResource(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-jsf/" + facesConfigTemplate).getInputStream(), "UTF-8"); //NOI18N
                FileObject target = FileUtil.createData(webModule.getWebInf(), "faces-config.xml");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
            }

            // update welcome page
            FileObject documentBase = webModule.getDocumentBase();
            FileObject indexjsp = documentBase.getFileObject("index.jsp"); //NOI18N
            if (indexjsp != null){
                changeIndexJSP(indexjsp, pageName);
            }
        }
        
        /** Changes the index.jsp file. Only when there is <h1>JSP Page</h1> string.
         */
        private void changeIndexJSP(FileObject indexjsp, String pageName) throws IOException {
            
            String content = readResource(indexjsp.getInputStream(), "UTF-8"); //NO18N
            
            // what find
            String find = "<h1>JSP Page</h1>"; // NOI18N
            String endLine = System.getProperty("line.separator"); //NOI18N
            if ( content.indexOf(find) > 0){
                StringBuffer replace = new StringBuffer();
                replace.append(find);
                replace.append(endLine);
                replace.append("    <br/>");                        //NOI18N
                replace.append(endLine);
                replace.append("    <a href=\".");                  //NOI18N
                replace.append(translateURI(panel == null ? "/faces/*" : panel.getURLPattern(),"/"+pageName)); //NOI18N
                replace.append("\">");                              //NOI18N
                replace.append(NbBundle.getMessage(JSFFrameworkProvider.class,"LBL_JSF_WELCOME_PAGE"));
                replace.append("</a>");                             //NOI18N
                content = content.replaceFirst(find, new String(replace.toString().getBytes("UTF8"), "UTF-8")); //NOI18N
                createFile(indexjsp, content, "UTF-8"); //NOI18N
            }
        }
    
        /**
         * Translates an URI to be executed with faces serlvet with the given mapping.
         * For example, the servlet has mapping <i>*.jsf</i> then uri <i>/hello.jps</i> will be
         * translated to <i>/hello.jsf</i>. In the case where the mapping is <i>/faces/*</i>
         * will be translated to <i>/faces/hello.jsp<i>.
         *
         * @param mapping The servlet mapping
         * @param uri The original URI
         * @return The translated URI
         */
        public String translateURI(String mapping, String uri){
            String resource = "";
            if (mapping != null && mapping.length()>0){
                if (mapping.startsWith("*.")){
                    if (uri.indexOf('.') > 0)
                        resource = uri.substring(0, uri.lastIndexOf('.'))+mapping.substring(1);
                    else
                        resource = uri + mapping.substring(1);
                } else
                    if (mapping.endsWith("/*"))
                        resource = mapping.substring(0,mapping.length()-2) + uri;
            }
            return resource;
        }
    }

    private HashMap propertyListeners = new HashMap();

    private void updateNode(Project project) {
        PropertyChangeEvent event = new PropertyChangeEvent(project, null, null, null);

        PropertyChangeListener[] listeners;
        synchronized (propertyListeners) {
            ArrayList projectListeners = (ArrayList) propertyListeners.get(project);
            if (projectListeners == null) {
                return;
            }
            listeners = (PropertyChangeListener[])projectListeners.toArray(new PropertyChangeListener[propertyListeners.size()]);
        }
        for (int i = 0; i < listeners.length; i++) {
            PropertyChangeListener listener = listeners[i];
            if (listener != null) {
                listener.propertyChange(event);
            }
        }
    }
    
    public void addPropertyChangeListener(Project project, PropertyChangeListener listener) {
        synchronized (propertyListeners) {
            ArrayList projectListeners = (ArrayList) propertyListeners.get(project);
            if (projectListeners == null) {
                projectListeners = new ArrayList();
                propertyListeners.put(project, projectListeners);
            }
            projectListeners.add(listener);
        }
    }
    
    public void removePropertyChangeListener(Project project, PropertyChangeListener listener) {
        synchronized (propertyListeners) {
            ArrayList projectListeners = (ArrayList) propertyListeners.get(project);
            if (projectListeners != null) {
                projectListeners.remove(listener);
            }
        }
    }
}
