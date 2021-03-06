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
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 *
 * @author mkuchtiak
 */
public class EjbProjectJAXWSClientSupport extends ProjectJAXWSClientSupport/* implements JAXWSClientSupportImpl*/ {
    private EjbJarProject project;
    
    /** Creates a new instance of WebProjectJAXWSClientSupport */
    public EjbProjectJAXWSClientSupport(EjbJarProject project) {
        super(project);
        this.project=project;    
    }
    
    public FileObject getWsdlFolder(boolean create) throws IOException {
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

    protected void addJaxWs20Library() throws Exception {
    }
    
    /** return root folder for xml artifacts
     */
    @Override
    protected FileObject getXmlArtifactsRoot() {
        return project.getAPIEjbJar().getMetaInf();
    }

    @Override
    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {

        String finalClientName = super.addServiceClient(clientName, wsdlUrl, packageName, isJsr109);
        
        // copy resources to META-INF/wsdl/client/${clientName}
        // this will be done only for local wsdl files
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        Client client = jaxWsModel.findClientByName(finalClientName);
        if (client!=null && client.getWsdlUrl().startsWith("file:")) { //NOI18N
            try {
                FileObject wsdlFolder = getWsdlFolderForClient(finalClientName);
                FileObject xmlResorcesFo = getLocalWsdlFolderForClient(finalClientName, false);
                if (xmlResorcesFo != null) {
                    WSUtils.copyFiles(xmlResorcesFo, wsdlFolder);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return finalClientName;
    }

}
