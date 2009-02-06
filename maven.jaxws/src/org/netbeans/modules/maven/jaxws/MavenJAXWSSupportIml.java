/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.jaxws;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class MavenJAXWSSupportIml implements JAXWSLightSupportImpl {
    private Project prj;
    private List<JaxWsService> services = new LinkedList<JaxWsService>();
    /** Path for catalog file. */
    public static final String CATALOG_PATH = "src/jax-ws-catalog.xml"; //NOI18N

    private final static String SERVLET_CLASS_NAME =
            "com.sun.xml.ws.transport.http.servlet.WSServlet"; //NOI18N
    private final static String SERVLET_LISTENER =
            "com.sun.xml.ws.transport.http.servlet.WSServletContextListener"; //NOI18N
    private  Boolean generateNonJsr109Stuff;
    /** Constructor.
     *
     * @param prj project
     */
    MavenJAXWSSupportIml(Project prj) {
        this.prj = prj;
    }

    public void addService(JaxWsService service) {
        services.add(service);

        if (!WSUtils.isJsr109Supported(prj)) {

            if (generateNonJsr109Stuff == null) {
                FileObject ddFolder = getDeploymentDescriptorFolder();
                if (ddFolder == null || ddFolder.getFileObject("sun-jaxws.xml") == null) {
                    // ask user if non jsr109 stuff should be generated
                    generateNonJsr109Stuff = Boolean.valueOf(WSUtils.generateNonJsr109Artifacts(prj));
                } else {
                    generateNonJsr109Stuff = Boolean.TRUE;
                }
            }
            if (generateNonJsr109Stuff) {
                // modify web.xml file
                try {
                    WSUtils.addServiceEntriesToDD(prj, service);
                } catch (IOException ex) {
                    Logger.getLogger(MavenJAXWSSupportIml.class.getName()).log(Level.WARNING,
                            "Cannot add service elements to web.xml file", ex); //NOI18N
                }
                // modify sun-jaxws.xml file
                try {
                    addSunJaxWsEntries(service);
                } catch (IOException ex) {
                    Logger.getLogger(MavenJAXWSSupportIml.class.getName()).log(Level.WARNING,
                            "Cannot modify sun-jaxws.xml file", ex); //NOI18N
                }
            }
        }
    }

    private void addSunJaxWsEntries(JaxWsService service)
        throws IOException {

        FileObject ddFolder = getDeploymentDescriptorFolder();
        if (ddFolder != null) {
            WSUtils.addSunJaxWsEntries(ddFolder, service);
        } else{
            String mes = NbBundle.getMessage(MavenJAXWSSupportIml.class, "MSG_CannotFindWEB-INF"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
    }

    public List<JaxWsService> getServices() {
        return services;
    }

    public void removeService(JaxWsService service) {
        String localWsdl = service.getLocalWsdl();
        if (localWsdl != null) {
            // remove auxiliary wsdl url property
            FileObject wsdlFolder = getWsdlFolder(false);
            if (wsdlFolder != null) {
                FileObject wsdlFo = wsdlFolder.getFileObject(localWsdl);
                if (wsdlFo != null) {
                    Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class, true);
                    if (prefs != null) {
                        prefs.remove(MavenWebService.CLIENT_PREFIX+wsdlFo.getName());
                        prefs.remove(MavenWebService.SERVICE_PREFIX+wsdlFo.getName());
                    }
                }
            }
        }
        if (!WSUtils.isJsr109Supported(prj)) {

            // modify web.xml file
            try {
                WSUtils.removeServiceEntriesFromDD(prj, service);
            } catch (IOException ex) {
                Logger.getLogger(MavenJAXWSSupportIml.class.getName()).log(Level.WARNING,
                        "Cannot remove services from web.xml", ex); //NOI18N
            }

            // modify sun-jaxws.xml file
            try {
                removeSunJaxWsEntries(service);
            } catch (IOException ex) {
                Logger.getLogger(MavenJAXWSSupportIml.class.getName()).log(Level.WARNING,
                        "Cannot modify sun-jaxws.xml file", ex); //NOI18N
                }
        }
        services.remove(service);
    }

    private void removeSunJaxWsEntries(JaxWsService service) throws IOException {
        FileObject ddFolder = getDeploymentDescriptorFolder();
        if (ddFolder != null) {
            WSUtils.removeSunJaxWsEntries(ddFolder, service);
        } else{
            String mes = NbBundle.getMessage(MavenJAXWSSupportIml.class, "MSG_CannotFindWEB-INF"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
    }

    public FileObject getDeploymentDescriptorFolder() {
        File wsdlDir = FileUtilities.resolveFilePath(
            FileUtil.toFile(prj.getProjectDirectory()), "src/main/webapp/WEB-INF"); //NOI18N
        if (wsdlDir.exists()) {
            return FileUtil.toFileObject(wsdlDir);
        }
        return null;
    }

    public FileObject getWsdlFolder(boolean createFolder) {
        File wsdlDir = FileUtilities.resolveFilePath(
                FileUtil.toFile(prj.getProjectDirectory()), "src/wsdl"); //NOI18N
        if (wsdlDir.exists()) {
            return FileUtil.toFileObject(wsdlDir);
        } else if (createFolder) {
            boolean created = wsdlDir.mkdirs();
            if (created) {
                return FileUtil.toFileObject(wsdlDir);
            }
        }
        return null;
    }

    public FileObject getBindingsFolder(boolean createFolder) {
        File bindingsDir = FileUtilities.resolveFilePath(
                FileUtil.toFile(prj.getProjectDirectory()), "src/jaxws-bindings"); //NOI18N
        if (bindingsDir .exists()) {
            return FileUtil.toFileObject(bindingsDir);
        } else if (createFolder) {
            boolean created = bindingsDir.mkdirs();
            if (created) {
                return FileUtil.toFileObject(bindingsDir);
            }
        }
        return null;
    }

    public URL getCatalog() {
        File catalogFile = FileUtilities.resolveFilePath(FileUtil.toFile(prj.getProjectDirectory()), CATALOG_PATH);
        try {
            return catalogFile.toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        if (webservicesMetadataModel == null) {
            J2eeModuleProvider j2eeModuleProvider = prj.getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null) {
                webservicesMetadataModel =
                        j2eeModuleProvider.getJ2eeModule().getMetadataModel(WebservicesMetadata.class);
            }
        }
        return webservicesMetadataModel;
    }

    private MetadataModel < WebservicesMetadata > webservicesMetadataModel;
}
