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

package org.netbeans.modules.j2ee.ejbjarproject.jaxws;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.spi.ProjectJAXWSSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 *
 * @author mkuchtiak
 */
public class EjbProjectJAXWSSupport extends ProjectJAXWSSupport /*implements JAXWSSupportImpl*/ {
    private EjbJarProject project;
    private AntProjectHelper antProjectHelper;
    
    /** Creates a new instance of JAXWSSupport */
    public EjbProjectJAXWSSupport(EjbJarProject project, AntProjectHelper antProjectHelper) {
        super(project,antProjectHelper);
        this.project = project;
        this.antProjectHelper = antProjectHelper;
    }

    public FileObject getWsdlFolder(boolean create) throws java.io.IOException {
        EjbJar ejbModule = EjbJar.getEjbJar(project.getProjectDirectory());
        if (ejbModule!=null) {
            FileObject metaInfFo = ejbModule.getMetaInf();
            if (metaInfFo!=null) {
                FileObject wsdlFo = metaInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) {
                    return wsdlFo;
                }
                else if (create) {
                    return metaInfFo.createFolder("wsdl"); //NOI18N
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
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            if (service!=null) {
                String localWsdlFile = service.getLocalWsdlFile();
                if (localWsdlFile!=null) {
                    localWsdl = localWsdlFile;
                }
            }
        }
        String prefix = "META-INF/wsdl/"; //NOI18N
        return prefix+serviceName+"/"+localWsdl; //NOI18N
    }

    public FileObject getDeploymentDescriptorFolder() {
        EjbJar ejbModule = EjbJar.getEjbJar(project.getProjectDirectory());
        if (ejbModule!=null) {
            return ejbModule.getMetaInf();
        }   
        return null;
    }

    protected void addJaxwsArtifacts(Project project, String wsName, String serviceImpl) throws Exception {
    }
    
    /** return root folder for xml artifacts
     */
    @Override
    protected FileObject getXmlArtifactsRoot() {
        return project.getAPIEjbJar().getMetaInf();
    }

    public void removeNonJsr109Entries(String serviceName) throws IOException {
        //noop since nonJSR 109 web services are not supported in the EJB module
    }

    @Override
    public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName, String portName, String packageName, boolean isJsr109) {
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.addService(name, serviceImpl, wsdlUrl, serviceName, portName, packageName, isJsr109);
    }

    @Override
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
        return project.getEjbModule().getWebservicesMetadataModel();
    }
    
}
