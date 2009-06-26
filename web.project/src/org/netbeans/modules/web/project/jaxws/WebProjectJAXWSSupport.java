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

package org.netbeans.modules.web.project.jaxws;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.WebProjectWebServicesSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.ProjectJAXWSSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class WebProjectJAXWSSupport extends ProjectJAXWSSupport /*implements JAXWSSupportImpl*/ {
    private WebProject project;
    //servlet name and servlet listener for non-JSR 109 web services
    private final static String servletClassName = "com.sun.xml.ws.transport.http.servlet.WSServlet"; //NOI18N
    private final static String servletListener = "com.sun.xml.ws.transport.http.servlet.WSServletContextListener"; //NOI18N
    
    /** Creates a new instance of JAXWSSupport */
    public WebProjectJAXWSSupport(WebProject project, AntProjectHelper antProjectHelper) {
        super(project,antProjectHelper);
        this.project = project;
    }
    
    public FileObject getWsdlFolder(boolean create) throws java.io.IOException {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule!=null) {
            FileObject webInfFo = webModule.getWebInf();
            if (webInfFo!=null) {
                FileObject wsdlFo = webInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) return wsdlFo;
                else if (create) {
                    return webInfFo.createFolder("wsdl"); //NOI18N
                }
            }
        }
        return null;
    }
    
    /** Get wsdlLocation information
     * Useful for web service from wsdl
     * @param name service "display" name
     */
    public String getWsdlLocation(String serviceName) {
        String localWsdl = serviceName+".wsdl"; //NOI18N
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            if (service!=null) {
                String localWsdlFile = service.getLocalWsdlFile();
                if (localWsdlFile!=null) localWsdl=localWsdlFile;
            }
        }
        String prefix = "WEB-INF/wsdl/"; //NOI18N
        return prefix+serviceName+"/"+localWsdl; //NOI18N
    }
    
    /**
     * This is to support non-JSR 109 containers. In this case, a regular jaxws web service
     * is created and the deployment descriptor is updated with the jaxws-ri servlet and
     * listener.
     */
    private void addServiceEntriesToDD(String serviceName) {
        //add servlet entry to web.xml
        String servletName = serviceName;
        
        WebApp webApp = getWebApp();
        if(webApp != null){
            Servlet servlet = null;
            Listener listener = null;
            try{
                servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},
                        new Object[]{servletName,servletClassName}, "ServletName");
                servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                ServletMapping servletMapping = (ServletMapping)
                webApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"},
                        new Object[]{servletName, "/" + serviceName}, "ServletName");
                
                if(!webAppHasListener(webApp, servletListener)){
                    listener = (Listener)webApp.addBean("Listener", new String[]{"ListenerClass"},
                            new Object[]{servletListener}, "ListenerClass");
                }
                // This also saves server specific configuration, if necessary.
                webApp.write(getDeploymentDescriptor());
            } catch (ClassNotFoundException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            } catch (NameAlreadyUsedException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            } catch (IOException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            }
        }
    }
    
    protected void addServletElement(Project project, String wsName, String serviceImpl) throws IOException {
        WebApp webApp = getWebApp();
        if(webApp != null) {
            boolean addServletEntry = false;
            JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
            if (wss != null) {
                Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
                String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
                if (serverInstance != null) {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                    if (j2eePlatform != null) {
                        if (j2eePlatform.isToolSupported("JaxWs-in-j2ee14-supported")) addServletEntry = true;
                    }
                }
            }
            if (addServletEntry) {
                try{
                    Servlet servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},
                            new Object[]{wsName,serviceImpl}, "ServletName");
                    servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                    ServletMapping servletMapping = (ServletMapping)
                    webApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"},
                            new Object[]{wsName, "/" + wsName}, "UrlPattern");
                    // This also saves server specific configuration, if necessary.
                    webApp.write(getDeploymentDescriptor());
                } catch (ClassNotFoundException exc) {
                    Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
                } catch (NameAlreadyUsedException exc) {
                    Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
                }
            }
        }
    }    
    /**
     * Remove the web.xml entries for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private void removeServiceEntriesFromDD(String serviceName) {
        WebApp webApp = getWebApp();
        if (webApp != null) {
            boolean changed = removeNonJsr109ServletsFromDD(webApp, serviceName);

            //determine if there are other web services in the project
            //if none, remove the listener
            boolean hasMoreWebServices = false;
            Servlet[] remainingServlets = webApp.getServlet();
            for(int i = 0; i < remainingServlets.length; i++){
                if(remainingServlets[i].getServletClass().equals(servletClassName)){
                    hasMoreWebServices = true;
                    break;
                }
            }
            if(!hasMoreWebServices){
                Listener[] listeners = webApp.getListener();
                for(int i = 0; i < listeners.length; i++){
                    Listener listener = listeners[i];
                    if(listener.getListenerClass().equals(servletListener)){
                        webApp.removeListener(listener);
                        changed = true;
                        break;
                    }
                }
            }
            if(changed){
                try{
                    webApp.write(getDeploymentDescriptor());
                } catch(IOException e){
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }
    
    /**
     * Remove the web.xml servlets for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private boolean removeNonJsr109ServletsFromDD(WebApp webApp, String serviceName) {
        boolean changed = false;
        //first remove the servlet
        Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++){
            Servlet servlet = servlets[i];
            if(servlet.getServletName().equals(serviceName)){
                webApp.removeServlet(servlet);
                changed = true;
                break;
            }
        }
        //remove the servlet mapping
        ServletMapping[] mappings = webApp.getServletMapping();
        for(int i = 0; i < mappings.length; i++){
            ServletMapping mapping = mappings[i];
            if(mapping.getServletName().equals(serviceName)){
                webApp.removeServletMapping(mapping);
                changed = true;
                break;
            }
        }
        return changed;
    }
    
    private boolean webAppHasListener(WebApp webApp, String listenerClass){
        Listener[] listeners = webApp.getListener();
        for(int i = 0; i < listeners.length; i++){
            Listener listener = listeners[i];
            if(listenerClass.equals(listener.getListenerClass())){
                return true;
            }
        }
        return false;
    }
    /**
     * Returns the directory that contains the deployment descriptor in the project
     */
    public FileObject getDeploymentDescriptorFolder() {
        return this.getWebInf();
    }
    
    private WebApp getWebApp() {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor();
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, e.getLocalizedMessage());
        }
        return null;
    }
    
    private boolean isProjectOpened() {
        // XXX workaround: OpenProjects.getDefault() can be null
        // when called from ProjectOpenedHook.projectOpened() upon IDE startup
        if (OpenProjects.getDefault() == null)
            return true;
        
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(project))
                return true;
        }
        return false;
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject webInfFo = getWebInf();
        if (webInfFo==null) {
            if (isProjectOpened()) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_WebInfCorrupted"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE));
            }
            return null;
        }
        return getWebInf().getFileObject(ProjectWebModule.FILE_DD);
    }
    
    public FileObject getWebInf() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if(webModule != null){
            return webModule.getWebInf();
        }
        return null;
    }
    
    protected void addJaxwsArtifacts(Project project, String wsName, String serviceImpl) throws Exception {
        
        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = SourceGroups.getJavaSourceGroups(project);
        if (sgs.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
            FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N

            if (wsimportFO == null) {
                //Add the Metro library to the project to be packed with the archive
                Library metroLib = LibraryManager.getDefault().getLibrary("metro"); //NOI18N
                if (metroLib != null) {
                    try {
                        ProjectClassPathModifier.addLibraries(
                                new Library[] {metroLib},
                                sgs[0].getRootFolder(),
                                ClassPath.COMPILE);
                    }catch(IOException e){
                        throw new Exception("Unable to add Metro library", e);
                    }
                } else {
                    throw new Exception("Unable to add Metro Library"); //NOI18N
                }
            }
        }
        
        //Add the servlet and listener entry in web.xml
        addServiceEntriesToDD(wsName);
        
        //create the sun-jaxws deployment descriptor
        FileObject ddFolder = getDeploymentDescriptorFolder();
        if(ddFolder != null){
            FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");
            if(sunjaxwsFile == null){
                WSUtils.generateSunJaxwsFile(ddFolder);
            }
            
            sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
            Endpoint endpoint = endpoints.newEndpoint();
            endpoint.setEndpointName(wsName);
            endpoint.setImplementation(serviceImpl);
            endpoint.setUrlPattern("/" + wsName);
            endpoints.addEnpoint(endpoint);
            FileLock lock = null;
            OutputStream os = null;
            synchronized (this) {
                try{
                    lock = sunjaxwsFile.lock();
                    os = sunjaxwsFile.getOutputStream(lock);
                    endpoints.write(os);
                }finally{
                    if(lock != null)
                        lock.releaseLock();

                    if(os != null)
                        os.close();
                }
            }
        }else{
            String mes = NbBundle.getMessage(WebProjectJAXWSSupport.class, "MSG_CannotFindWEB-INF"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
    }
    
    /** return root folder for xml artifacts
     */
    protected FileObject getXmlArtifactsRoot() {
        return project.getWebModule().getConfDir();
    }
    
    /**
     * Notification when Service (created from java) is removed from jax-ws.xml
     * (JAXWSSupport needs to react when @WebService annotation is removed
     * or when impl.class is removed (manually from project)
     * Default implementation does nothing.
     */
    public void serviceFromJavaRemoved(String serviceName) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        Boolean isJsr109 = jaxWsModel.getJsr109();
        if(isJsr109!=null && !isJsr109.booleanValue()){
            try{
                removeNonJsr109Entries(serviceName);
            }catch(IOException e){
                Exceptions.printStackTrace(e);
            }
        } else {
            try{
                removeJsr109Entries(serviceName);
            }catch(IOException e){
                Exceptions.printStackTrace(e);
            }            
        }
        
    }
    
    /**
     * Removes the servlet entry from web.xml and
     * the endpoint entry from the sun-jaxws.xml file
     */
    public void removeNonJsr109Entries(String serviceName) throws IOException {
        //delete web.xml entry
        removeServiceEntriesFromDD(serviceName);
        //delete entry in sun-jaxws.xml file.
        FileObject ddFolder = getDeploymentDescriptorFolder();
        if(ddFolder != null){
            FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");
            if(sunjaxwsFile != null){
                FileLock lock = null;
                //if there are no more services, delete the file
                JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
                if(jaxWsModel.getServices().length == 0) {
                    synchronized(this) {
                        try{
                            lock = sunjaxwsFile.lock();
                            sunjaxwsFile.delete(lock);
                        } finally{
                            if(lock != null){
                                lock.releaseLock();
                            }
                        }
                    }
                } else{
                    //remove the entry from the sunjaxwsFile
                    Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
                    Endpoint endpoint = endpoints.findEndpointByName(serviceName);
                    if(endpoint != null){
                        endpoints.removeEndpoint(endpoint);
                        OutputStream os = null;
                        synchronized(this) {
                            try{
                                lock = sunjaxwsFile.lock();
                                os = sunjaxwsFile.getOutputStream(lock);
                                endpoints.write(os);
                            }finally{
                                if(lock != null){
                                    lock.releaseLock();
                                }
                                if(os != null){
                                    os.close();
                                }
                            }
                        }
                    }
                }
            }
        }else{
            String mes = NbBundle.getMessage(WebProjectJAXWSSupport.class, "MSG_CannotFindDDDirectory"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
        
        //TODO if no more web services, remove the jaxws21 library
    }
    
    /**
     * Removes the servlet entry from web.xml and
     * the endpoint entry from the sun-jaxws.xml file
     */
    public void removeJsr109Entries(String serviceName) throws IOException {
        WebApp webApp = getWebApp();       
        if (webApp != null) {
            JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                Service service = jaxWsModel.findServiceByName(serviceName);
                if (service != null) {
                    boolean changed = removeJsr109ServletsFromDD(webApp, service);
                    if(changed){
                        try{
                            webApp.write(getDeploymentDescriptor());
                        } catch(IOException e){
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            }
        }
    }
 
    /**
     * Remove the web.xml servlets for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private boolean removeJsr109ServletsFromDD(WebApp webApp, Service service) {
        boolean changed = false;
        //first remove the servlet
        String implClass = service.getImplementationClass();
        if (implClass != null) {
            String servletName = null;
            for(Servlet servlet:webApp.getServlet()){
                if(implClass.equals(servlet.getServletClass())) {
                    webApp.removeServlet(servlet);
                    servletName = servlet.getServletName();
                    changed = true;
                    break;
                }
            }
            //remove the servlet mapping
            if (servletName != null) {
                for(ServletMapping mapping:webApp.getServletMapping()) {
                    if(mapping.getServletName().equals(servletName)){
                        webApp.removeServletMapping(mapping);
                        changed = true;
                        break;
                    }
                }
            }
        }
        return changed;
    }
 
    public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName, 
            String portName, String packageName, boolean isJsr109, boolean useProvider) {
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.addService(name, serviceImpl, wsdlUrl, serviceName, portName, packageName, isJsr109, useProvider);
    }

    public void addService(String serviceName, String serviceImpl, boolean isJsr109) {
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        super.addService(serviceName, serviceImpl, isJsr109);
    }

    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        return project.getWebModule().getWebservicesMetadataModel();
    }
    
}
