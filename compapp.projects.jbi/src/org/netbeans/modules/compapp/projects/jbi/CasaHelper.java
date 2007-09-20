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

package org.netbeans.modules.compapp.projects.jbi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.netbeans.modules.compapp.projects.jbi.CasaConstants.*;

/**
 * A helper class to handle CASA related functions.
 * 
 * @author jqian
 */
public class CasaHelper {
    public static String CASA_DIR_NAME = "/src/conf/";  // NOI18N 
    public static String CASA_EXT = ".casa";  // NOI18N 
        
    /**
     * Gets the name of the CASA file in the given project.
     * 
     * @param project   a JBI project
     * 
     * @return  CASA file name
     */
    public static String getCasaFileName(Project project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);    
        String projName = projInfo.getName();
                            
        File pf = FileUtil.toFile(project.getProjectDirectory());
        return pf.getPath() + CASA_DIR_NAME + projName + CASA_EXT;
    }
    
    /**
     * Gets the CASA file object in the given JBI project.
     * 
     * @param project   a JBI project
     * @param create    if <code>true</code> and the CASA file doesn't exist in
     *                  the project, then an empty CASA file will be created
     * 
     * @return  CASA file object
     */
    public static FileObject getCasaFileObject(JbiProject project, boolean create) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        assert projInfo != null;
        
        String projName = projInfo.getName();
        
        FileObject confFO = project.getProjectDirectory().getFileObject(CASA_DIR_NAME);
        if (confFO == null) {
            // This could happen during compapp rename with directory name change.
            return null;
        }
        
        FileObject casaFO = confFO.getFileObject(projName + CASA_EXT);   
        
        if (casaFO == null && create) {
            casaFO = createDefaultCasaFileObject(project);
            updateCasaWithJBIModules(project);
        }
        
        return casaFO;
    }    
    
    /**
     * Creates the default CASA file object in the JBI project.
     * 
     * @param project   a JBI project
     * 
     * @return  the newly created CASA file object
     */
    public static FileObject createDefaultCasaFileObject(JbiProject project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        assert projInfo != null;        
        String projName = projInfo.getName();        
        FileObject confFO = project.getProjectDirectory().getFileObject(CASA_DIR_NAME); 
        FileObject casaFO = null;
        try {        
            casaFO = FileUtil.copyFile(
                    Repository.getDefault().getDefaultFileSystem().findResource(
                    "org-netbeans-modules-compapp-projects-jbi/project.casa" // NOI18N
                    ), confFO, projName
                    );        
//            registerCasaFileListener(project);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return casaFO;
    }
    
    public static FileObject getCompAppWSDLFileObject(Project project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        String projName = projInfo.getName();
        FileObject srcDirFO = ((JbiProject)project).getSourceDirectory();
        return srcDirFO == null ? null : srcDirFO.getFileObject(projName + ".wsdl"); // NOI18N
    }
    
    /**
     * Checks whether the CASA file contains any non-deleted defined WSDL Port.
     */
    public static boolean containsWSDLPort(JbiProject project) {
        FileObject casaFO = getCasaFileObject(project, false);
        if (casaFO != null) {
            InputStream is = null;
            try {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                
                is = casaFO.getInputStream();
                Document doc = builder.parse(is);
                
                NodeList endpointNodeList = doc.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
                NodeList portNodeList = doc.getElementsByTagName(CASA_PORT_ELEM_NAME); 
                for (int i = 0; i < portNodeList.getLength(); i++) {
                    Element port = (Element) portNodeList.item(i);
                    String state = port.getAttribute(CASA_STATE_ATTR_NAME);
                    if (!state.equals(CASA_DELETED_ATTR_VALUE)) { 
                        Element consumes = (Element) port.getElementsByTagName(CASA_CONSUMES_ELEM_NAME).item(0);
                        String endpointName = consumes.getAttribute(CASA_ENDPOINT_ATTR_NAME);
                        
                        for (int j = 0; j < endpointNodeList.getLength(); j++) {
                            Element endpoint = (Element) endpointNodeList.item(j);
                            if (endpoint.getAttribute(CASA_NAME_ATTR_NAME).equals(endpointName)) {
                                String interfaceName = endpoint.getAttribute(CASA_INTERFACE_NAME_ATTR_NAME);
                                if (interfaceName.endsWith(":" + CASA_DUMMY_PORTTYPE)) { // NOI18N
                                    break;
                                } else {
                                    return true;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing CASA file: " + e); // NOI18N
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return false;
    }
    
//    public static void registerCasaFileListener(JbiProject project) {        
//        FileObject casaFO = CasaHelper.getCasaFileObject(project, false);
//        if (casaFO != null) {
//            FileChangeListener listener =
//                    project.getLookup().lookup(FileChangeListener.class);
//            if (listener != null) {
//                casaFO.removeFileChangeListener(listener);
//                casaFO.addFileChangeListener(listener);
//            }
//        }
//    }
        
    public static void saveCasa(JbiProject project) {
        FileObject casaFO = getCasaFileObject(project, false);
        if (casaFO != null) {
            try {
                DataObject casaDO = DataObject.find(casaFO);
                
                SaveCookie saveCookie = casaDO.getCookie(SaveCookie.class);
                if (saveCookie != null) {
                    try {
                        saveCookie.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Updates CASA FileObject with service engine service unit info
     * defined in the project properties.
     *
     * @param project   a JBI project
     *
     * @return      the updated CASA file object
     */
    public static FileObject updateCasaWithJBIModules(JbiProject project) { 
        JbiProjectProperties properties = project.getProjectProperties();
        return updateCasaWithJBIModules(project, properties);
    }

    /**
     * Updates CASA FileObject with service engine service unit info
     * defined in the project properties.
     *
     * @param project       a JBI project
     * @param properties    project properties (may not been persisted yet)
     *
     * @return      the updated CASA file object
     */   
    public static FileObject updateCasaWithJBIModules(JbiProject project, 
            JbiProjectProperties properties) { 
         
        FileObject casaFO = CasaHelper.getCasaFileObject(project, true);
        if (casaFO == null) {
            return null;
        }
           
        File casaFile = FileUtil.toFile(casaFO);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document casaDocument = builder.parse(casaFile);

            Element sus = (Element) casaDocument.getElementsByTagName(
                    CasaConstants.CASA_SERVICE_UNITS_ELEM_NAME).item(0);
            NodeList seSUs = sus.getElementsByTagName(
                    CasaConstants.CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
            
            List<VisualClassPathItem> newContentList = 
                    (List<VisualClassPathItem>) properties.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
            List<String> newTargetIDs = 
                    (List<String>) properties.get(JbiProjectProperties.JBI_CONTENT_COMPONENT);

            List<String> newArtifactsList = new ArrayList<String>();
            for (VisualClassPathItem newContent : newContentList) {
                newArtifactsList.add(newContent.toString());
            }

            // Remove deleted service units from casa
            for (int i = 0; i < seSUs.getLength(); i++) {
                Element seSU = (Element) seSUs.item(i);
                String zipName = seSU.getAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME);
                if (!newArtifactsList.contains(zipName)) {
                    sus.removeChild(seSU);
                }
            }

            // Add new service units to casa
            for (String artifactName: newArtifactsList) {
                boolean found = false;
                for (int i = 0; i < seSUs.getLength(); i++) {
                    Element seSU = (Element) seSUs.item(i);
                    if (seSU.getAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME).
                            equals(artifactName)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    String targetCompID = "unknown"; // NOI18N
                    for (int j = 0; j < newContentList.size(); j++) {
                        if (newContentList.get(j).toString().equals(artifactName)) {
                            targetCompID = newTargetIDs.get(j);
                            break;
                        }
                    }
                    Element seSU = casaDocument.createElement(
                            CasaConstants.CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
                    String compProjName = artifactName.substring(0, artifactName.length() - 4);
                    seSU.setAttribute(CasaConstants.CASA_X_ATTR_NAME, "-1"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_Y_ATTR_NAME, "-1"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_INTERNAL_ATTR_NAME, "true"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_DEFINED_ATTR_NAME, "false"); // NOI18N 
                    seSU.setAttribute(CasaConstants.CASA_UNKNOWN_ATTR_NAME, "false"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_NAME_ATTR_NAME, compProjName); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_UNIT_NAME_ATTR_NAME, compProjName); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_COMPONENT_NAME_ATTR_NAME, targetCompID); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_DESCRIPTION_ATTR_NAME, "some description"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME, artifactName);

                    sus.appendChild(seSU);
                }
            }
                        
            XmlUtil.writeToFile(casaFile.getPath(), casaDocument);
            
            casaFO = FileUtil.toFileObject(casaFile);
            casaFO.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return casaFO;
    }
}
