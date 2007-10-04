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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.serviceapi.InterfaceDescription;
import org.netbeans.modules.serviceapi.ServiceInterface;
import org.netbeans.modules.serviceapi.ServiceLink;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import org.netbeans.modules.serviceapi.ServiceComponent;
import org.netbeans.modules.serviceapi.ServiceModule;
import org.netbeans.modules.serviceapi.wsdl.WSDL11Description;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NotImplementedException;

/**
 *
 * @author Nam Nguyen
 */
public class ServiceModuleImpl extends ServiceModule {
    private WebProject project;
    
    /** Creates a new instance of ServiceModuleImpl */
    public ServiceModuleImpl(WebProject webProject) {
        this.project = webProject;
    }
    
    private WebServicesSupportImpl getWebServiceSupport() {
        return project.getLookup().lookup(WebServicesSupportImpl.class);
    }
    
    /**
     * @return name of the service module.
     */
    public String getName() {
        return project.getName();
    }
    
    /**
     * Returns service components contained in this module.
     */
    public Collection<ServiceComponent> getServiceComponents() {
        ArrayList<ServiceComponent> ret = new ArrayList<ServiceComponent>();
        Webservices wss = getWebservices();
        
        if (wss != null) {
            WebserviceDescription[] descriptions = wss.getWebserviceDescription();
            if (descriptions == null) return ret;
            for (WebserviceDescription wsd : descriptions) {
                PortComponent[] portComponents = wsd.getPortComponent();
                if (portComponents == null) continue;
                String wsdlPath = wsd.getWsdlFile();
                assert wsdlPath != null;
                for (PortComponent pc : portComponents) {
                    ret.add(new Component(wsdlPath, pc));
                }
            }
            
            for (Servlet s : getWebApp().getServlet()) {
                s.getServletClass();
            }
        }
        return ret;
    }
    
    public static QName convertSchema2BeansQName(org.netbeans.modules.schema2beans.QName q) {
        return new QName(q.getNamespaceURI(), q.getLocalPart());
    }
    
    private class Component extends ServiceComponent {
        private PortComponent portComponent;
        private String wsdlFilePath;
        
        private Component(String wsdlFilePath) {
            //absolute path only for now, 
            //PENDING: maybe also support project reference?
            this.wsdlFilePath = wsdlFilePath;
        }
        
        private Component(String wsdlFilePath, PortComponent pc) {
            this.wsdlFilePath = wsdlFilePath;
            portComponent = pc;
        }
        
        public String getWsdlFilePath() {
            return wsdlFilePath;
        }
        
        public List<ServiceInterface> getServiceProviders() {
            if (portComponent == null || portComponent.getWsdlPort() == null) {
                return Collections.emptyList();
            }
            
            QName portQName =  convertSchema2BeansQName(portComponent.getWsdlPort());
            Description def = new Description(getWsdlFilePath(), portQName);
            Interface i = new Interface(this, def, true);
            List<ServiceInterface> ret = new ArrayList<ServiceInterface>();
            ret.add(i);
            return ret;
        }
        
        public List<ServiceInterface> getServiceConsumers() {
            //TODO how do I discovery interfaces consume by this component.
            return Collections.emptyList();
        }
        
        public String getImplClassName() {
            String link = portComponent.getServiceImplBean().getServletLink();
            return getWebServiceSupport().getImplementationBean(link);
        }
        
        public Collection<ServiceLink> getServiceLinks() {
            // Web project module does not support service links
            return Collections.emptyList();
        }
        
        public Node getNode() {
            FileObject fo = getSourcesClassPath().findResource(getImplClassName());
            assert fo != null;
            try {
                DataObject dobj =  DataObject.find(fo);
                assert dobj != null;
                return dobj.getNodeDelegate();
            } catch(DataObjectNotFoundException ex) {
                assert false : "DataObjectNotFoundException: " + ex.getMessage();
                return null;
            }
        }
        
        public ServiceInterface createServiceInterface(InterfaceDescription description,
                boolean provider) {
            if (provider) return null;
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public ServiceInterface createServiceInterface(ServiceInterface other) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeServiceInterface(ServiceInterface serviceInterface) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        public int hashCode() {
            return getImplClassName().hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof Component)) return false;
            Component other = (Component) obj;
            return this.getImplClassName().equals(other.getImplClassName());
        }
    }
    
    private class Interface implements ServiceInterface {
        Component component;
        Description description;
        boolean isProvider = true;
        
        Interface(Component parent, Description description, boolean isProvider) {
            this(parent, description);
            this.isProvider = isProvider;
        }
        
        private Interface(Component parent, Description description) {
            this.component = parent;
            this.description = description;
        }
        
        public Description getInterfaceDescription() {
            return description;
        }
        
        public ServiceComponent getServiceComponent() {
            return component;
        }
        
        public boolean canConnect(ServiceInterface other) {
            if (! (other.getInterfaceDescription() instanceof WSDL11Description)) {
                return false;
            }
            WSDL11Description otherDescription = (WSDL11Description) other.getInterfaceDescription();
            return other.isProvider() != isProvider() &&
                    otherDescription.getInterfaceQName().equals(getQName());
        }
        
        public boolean isProvider() {
            return isProvider;
        }
        
        public Node getNode() {
            //TODO probably portType node from WSLD UI or provide more functionality here.
            return new AbstractNode(Children.LEAF) {
                public String getName() {
                    return description.getDisplayName();
                }
            };
        }
        
        public QName getQName() {
            return getInterfaceDescription().getInterfaceQName();
        }
    }
    
    private class Description extends WSDL11Description {
        private String pathToWSDL;
        private QName portTypeQName;
        
        Description(String pathToWSDL, QName portTypeQName) {
            this.pathToWSDL = pathToWSDL;
            this.portTypeQName = portTypeQName;
        }
        
        @Override
        public QName getInterfaceQName() {
            return portTypeQName;
        }
        
        public String getDisplayName() {
            return portTypeQName.getLocalPart();
        }
        
        public PortType getInterface() {
            File sourceFile = new File(pathToWSDL);
            FileObject sourceFO = FileUtil.toFileObject(sourceFile);
            if (sourceFO != null) {
                try {
                    ModelSource ms = Utilities.createModelSource(sourceFO, true);
                    WSDLModel model = WSDLModelFactory.getDefault().getModel(ms);
                    if (model.getState().equals(WSDLModel.State.VALID)) {
                        Collection<PortType> portTypes = model.getDefinitions().getPortTypes();
                        if (portTypes.size() > 0) {
                            return portTypes.iterator().next();
                        }
                    }
                } catch(Exception ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }
            }
            return null;
        }
    }
    
    private Webservices getWebservices() {
        throw new NotImplementedException();
        // TODO MetadataModel:
//        RootInterface rootInterface = project.getWebModule().getDeploymentDescriptor(J2eeModule.WEBSERVICES_XML);
//        if (rootInterface instanceof Webservices) {
//            return (Webservices) rootInterface;
//        }
//        return null;
    }
    
    private WebApp getWebApp() {
        throw new NotImplementedException();
        // TODO MetadataModel:
//        RootInterface rootInterface = project.getWebModule().getDeploymentDescriptor(J2eeModule.WEB_XML);
//        if (rootInterface instanceof WebApp) {
//            return (WebApp) rootInterface;
//        }
//        assert false : "Failed to get WebApp";
//        return null;
    }
    
    /**
     * Add service component.
     */
    
    public void addServiceComponent(ServiceComponent component) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Remove service component.
     */
    public void removeServiceComponent(ServiceComponent component) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * @return the project if applicable (not applicable for service modules from appserver).
     */
    public Project getProject() {
        return project;
    }
    
    private ClassPath getSourcesClassPath() {
        ClassPathProviderImpl cpProvider = project.getClassPathProvider();
        return cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE);
    }
}
