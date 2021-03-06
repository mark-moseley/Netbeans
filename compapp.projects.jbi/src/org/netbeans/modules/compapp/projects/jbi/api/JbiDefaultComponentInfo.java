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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.util.ArrayList;
import org.netbeans.modules.compapp.javaee.util.ProjectUtil;
import org.netbeans.api.project.Project;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import java.util.Enumeration;
import java.util.List;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.openide.filesystems.FileSystem;


/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class JbiDefaultComponentInfo {
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_ID = "id"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_NAME = "name"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_DESC = "description"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_TYPE = "type"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_NAMESPACE = "namespace"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_ICON = "icon"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    private static final String OLD_NAME_PREFIX = "com.sun."; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    private static final String NEW_NAME_PREFIX = "sun-"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String SB_COMP_RESOURCE_NAME = "SeeBeyondJbiComponents"; // NOI18N
    public static final String COMP_RESOURCE_NAME = "JbiComponents"; // NOI18N
    public static final String WSDLEDITOR_NAME = "WSDLEditor"; // NOI18N
    public static final String WSDL_ICON_NAME = "SystemFileSystem.icon"; // NOI18N

    private static JbiDefaultComponentInfo singleton = null;
    
    // a list of SE and BCs known at design time
    private List<JBIComponentStatus> componentList = new ArrayList<JBIComponentStatus>();
    
    // mapping SE/BC's name to the component
    private Map<String, JBIComponentStatus> componentMap = new HashMap<String, JBIComponentStatus>();
    
    // mapping BC name to icon
    private Map<String, URL> bcIconMap = new HashMap<String, URL>();
    
    // mapping BC name to binding info
    private Map<String, JbiBindingInfo> bindingInfoHash = new HashMap<String, JbiBindingInfo>();
    private List<JbiBindingInfo> bindingInfoList = new ArrayList<JbiBindingInfo>();

    private JbiDefaultComponentInfo() {
    }
    
    /**
     * Factory method for the default component list object
     *
     * @return the default component list object
     */
    public static JbiDefaultComponentInfo getJbiDefaultComponentInfo() {
        if (singleton == null) {
            try {
                singleton = new JbiDefaultComponentInfo();
                
                FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();
               
                // load new container first
                FileObject fo = fileSystem.findResource(WSDLEDITOR_NAME);
                loadJbiDefaultComponentInfoForSDLEditor(fo);

                // load new container first
                fo = fileSystem.findResource(COMP_RESOURCE_NAME);
                loadJbiDefaultComponentInfoFromFileObject(fo);
                
                // backward compatibility
                fo = fileSystem.findResource(SB_COMP_RESOURCE_NAME);
                loadJbiDefaultComponentInfoFromFileObject(fo);

            } catch (Exception ex) {
                // failed... return withopt changing the selector content.
                ex.printStackTrace();
            }
        }
        
        return singleton;
    }

    private static void loadJbiDefaultComponentInfoForSDLEditor(FileObject fo) { // Register Binding Component Icon: WSDLEditor/Binding/{FileBinding, ...}
        if (fo != null) {
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] bs = df.getChildren();

            for (int i = 0; i < bs.length; i++) {
                String name = bs[i].getName();
                if (name.equalsIgnoreCase("binding")) {
                    DataObject[] ms = DataFolder.findFolder(bs[i].getPrimaryFile()).getChildren();
                    for (int j = 0; j < ms.length; j++) {
                        String bname = ms[j].getName().toLowerCase(); // e.x., filebinding
                        FileObject msfo = ms[i].getPrimaryFile(); 
                        URL icon = (URL) msfo.getAttribute(WSDL_ICON_NAME);
                        if (icon != null) {
                            singleton.bcIconMap.put(bname, icon);
                            // System.out.println("Add ICON: "+bname+", "+ ((URL) icon).toString());
                        }
                    }
                }
            }
        }
    }

    private static void loadJbiDefaultComponentInfoFromFileObject(FileObject fo) { // JbiComponents or SeeBeyondJbiComponents
        if (fo != null) {
            DataFolder df = DataFolder.findFolder(fo);            
            for (DataObject compDO : df.getChildren()) {
                String name = compDO.getName();
                String id = ""; // NOI18N
                String desc = ""; // NOI18N
                String type = ""; // NOI18N
                String state = "Installed"; // NOI18N
                List<String> nsList = new ArrayList<String>();
                
                FileObject compFO = compDO.getPrimaryFile();  // e.x., SeeBeyondJbiComponents/sun-file-binding
                for (Enumeration<String> e = compFO.getAttributes(); e.hasMoreElements();) {
                    String attrName = e.nextElement();
                    String attrValue = (String) compFO.getAttribute(attrName);
                    
                    if (attrName.equals(COMP_ID)) {
                        id = attrValue;
                    } else if (attrName.equals(COMP_DESC)) {
                        desc = attrValue;
                    } else if (attrName.equals(COMP_TYPE)) {
                        type = attrValue;
                    } else if (attrName.equals(COMP_NAMESPACE)) {
                        nsList.add(attrValue);
                    }
                }
                        
                if (JBIComponentStatus.BINDING.equals(type) && compDO instanceof DataFolder) {
                    for (DataObject bindingTypeDO : ((DataFolder)compDO).getChildren()) { 
                        FileObject bindingTypeFO = bindingTypeDO.getPrimaryFile(); // e.x., SeeBeyondJbiComponents/sun-file-binding/file.binding-1.0
                        
                        String bindingType = bindingTypeFO.getName(); // e.x., file.binding-1    // for http soap, there are two files: http.binding-1 and soap.binding-1
                        int idx = bindingType.indexOf('.');
                        if (idx > 0) {
                            bindingType = bindingType.substring(0,idx).toLowerCase();
                        }
                        
                        String ns = (String) bindingTypeFO.getAttribute(COMP_NAMESPACE);
                        if (ns != null) {
                            nsList.add(ns);
                            addBindingInfo(id, bindingType, desc, ns);
                        }
                    }
                }
                
                // check for duplicates first..
                if (id.length() > 0 && !singleton.componentMap.containsKey(id)) {
                    JBIComponentStatus jcs = 
                            new JBIComponentStatus(id, desc, type, state, nsList);
                    singleton.componentList.add(jcs);
                    singleton.componentMap.put(id, jcs);
                }
            }
        }
    }
    
    /** 
     * @param id    binding component identifier, e.x., "sun-http-binding"
     * @param bindingType   binding type, e.x., "http", or "soap"
     * @param desc  binding component description 
     * @param ns    namespace for the binding type
     */
    private static void addBindingInfo(String id, String bindingType, String desc, String ns) {
        URL icon = null;
        
        for (String name : singleton.bcIconMap.keySet()) {
            if (name.startsWith(bindingType)) { // e.x., name: filebinding; bid: file
                icon = singleton.bcIconMap.get(name);
                break;
            }
        }
        
        if (icon != null) {
            JbiBindingInfo biinfo = new JbiBindingInfo(id, bindingType, icon, desc, ns);
            singleton.bindingInfoHash.put(id, biinfo);
            singleton.bindingInfoList.add(biinfo);
        }
    }

    /**
     * getter for the default component list
     *
     * @return the default componet list
     */
    public List<JBIComponentStatus> getComponentList() {
        return componentList;
    }
    
    /**
     * Getter for the default component list hashtable
     *
     * @return the default component list hashtable
     */
    public Map<String, JBIComponentStatus> getComponentHash() {
        return componentMap;
    }

    /**
     * Getter for the default binding info list
     *
     * @return the default binding info list
     */
    public List<JbiBindingInfo> getBindingInfoList() {
        return bindingInfoList;
    }

    /**
     * Getter for the specific binding info
     *
     * @param  id  binding component identifier 
     * @return the specific binding info
     */
    public JbiBindingInfo getBindingInfo(String id) {
        return bindingInfoHash.get(id);
    }

    /**
     * Parse the compoent name for a short display name. Only handle
     * two formats: com.sun.xxx-1.2-3 and sun-xxx-engine
     *
     * @param id component identifier
     * @return display name
     */
    public static String getDisplayName(String id) {
        int idx;
        if (id.startsWith(OLD_NAME_PREFIX)) {
            idx = id.indexOf('-');
            if (idx > 0) {
                return id.substring(OLD_NAME_PREFIX.length(), idx);
            }

        } else if (id.startsWith(NEW_NAME_PREFIX)) {
            idx = id.lastIndexOf('-');
            if (idx > 0) {
                return id.substring(NEW_NAME_PREFIX.length(), idx);
            }
        }
        return id;
    }

    /**
     * Is a given project a JavaEE project
     *
     * @param proj given project
     * @return true if it is a JavaEE project
     */
    public static boolean isJavaEEProject(Project proj){
        return ProjectUtil.isJavaEEProject(proj);
    }
        
    /**
     * Utility method to get the JBI binding info for the given WSDL Port.
     * 
     * @param port given WSDL Port
     * @return the corresponding JBI binding info
     */
    public static JbiBindingInfo getBindingInfo(final Port port) {
        JbiDefaultComponentInfo bcinfo = getJbiDefaultComponentInfo();
        if (bcinfo == null) {
            return null;
        }

        List<ExtensibilityElement> xts = port.getExtensibilityElements();
        if (xts.size() > 0) {
            ExtensibilityElement ex = xts.get(0);
            String qns = ex.getQName().getNamespaceURI();
            if (qns != null) {
                for (JbiBindingInfo bi : bcinfo.getBindingInfoList()) {
                    String ns = bi.getNameSpace();
                    if (qns.equalsIgnoreCase(ns)) {
                        return bi;
                    }
                }
            }
        }
        return null;
    }
}
