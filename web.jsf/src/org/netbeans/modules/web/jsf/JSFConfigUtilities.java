/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.config.model.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;

/**
 *
 * @author petr
 */
public class JSFConfigUtilities {
    
    public static List getAllNavigationRules(JSFConfigDataObject data){
        ArrayList list = new ArrayList();
        try{
            FacesConfig config = data.getFacesConfig();
            NavigationRule [] rules = config.getNavigationRule();
            for (int i = 0; i < rules.length; i++)
                list.add(rules[i]);
        } catch (java.io.IOException e){
            ErrorManager.getDefault().notify(e);
        }
        return list;
    }
    
    public static List getAllManagedBeans(JSFConfigDataObject data){
        ArrayList list = new ArrayList();
        try{
            FacesConfig config = data.getFacesConfig();
            ManagedBean [] beans = config.getManagedBean();
            for (int i = 0; i < beans.length; i++)
                list.add(beans[i]);
        } catch (java.io.IOException e){
            ErrorManager.getDefault().notify(e);
        }
        return list;
    }
    
    public static NavigationRule findNavigationRule(JSFConfigDataObject data, String fromView){
        try {
            return findNavigationRule(data.getFacesConfig(), fromView);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }
    
    /** Returns the navigation rule, where the FromViewID is the parameter. If the rule doesn't exist
     * then returns null.
     */
    public static NavigationRule findNavigationRule(FacesConfig config, String fromView){
        NavigationRule [] rules = config.getNavigationRule();
        for (int i = 0; i < rules.length; i++)
            if (rules[i].getFromViewId().equals(fromView))
                return rules[i];
        return null;
    }
    
    /** Returns WebPages for the project, where the fo is located.
     */
    public static SourceGroup[] getDocBaseGroups(FileObject fo) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
    }
    
    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator, boolean withExt) {
        for (int i=0;i<groups.length;i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root,fo)) {
                String relativePath = FileUtil.getRelativePath(root,fo);
                if (relativePath!=null) {
                    if (separator!='/') relativePath = relativePath.replace('/',separator);
                    if (!withExt) {
                        int index = relativePath.lastIndexOf((int)'.');
                        if (index>0) relativePath = relativePath.substring(0,index);
                    }
                    return relativePath;
                } else {
                    return "";
                }
            }
        }
        return "";
    }
    
    public static Servlet getActionServlet(FileObject dd){
        // PENDING - must be more declarative.
        try{
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            if (webApp == null) {
                return null;
            }
            return (Servlet)webApp.findBeanByName("Servlet","ServletName","Faces Servlet"); //NOI18N;
        } catch (java.io.IOException e) {
            return null;
        }
    }
    
    /** Returns the mapping for the Struts Action Servlet.
     */
    public static String getActionServletMapping(FileObject dd){
        Servlet servlet = getActionServlet(dd);
        if (servlet != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                ServletMapping[] mappings = webApp.getServletMapping();
                for (int i = 0; i < mappings.length; i++){
                    if (mappings[i].getServletName().equals(servlet.getServletName()))
                        return mappings[i].getUrlPattern();
                }
            } catch (java.io.IOException e) {
                
            }
        }
        return null;
    }
    
    public static boolean validateXML(FileObject dd){
        try{
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            InitParam param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.validateXml"); //NOI18N
            return  "true".equals(param.getParamValue().trim());
        } catch (java.io.IOException e) {
            
        }
        return false;
    }
    
    public static boolean verifyObjects(FileObject dd){
        try{
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            InitParam param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.verifyObjects"); //NOI18N
            return  "true".equals(param.getParamValue().trim());
        } catch (java.io.IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return false;
    }
    
    /** Returns relative path for all jsf configuration files in the web module. If there is no
     *  configuration file, then returns String array with lenght = 0. 
     */
    public static String[] getConfigFiles(FileObject dd){
        InitParam param = null;
        try{
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "javax.faces.CONFIG_FILES"); //NOI18N
        } catch (java.io.IOException e) {
          ErrorManager.getDefault().notify(e);  
        }
        
        if (param != null){
            // the configuration files are defined
            String value = param.getParamValue().trim();
            if (value != null){
                String[] files = value.split(","); 
                for (int i = 0; i < files.length; i++)
                    files[i] = files[i].trim();
                return  files;
            }
        }
        else{
            // the configguration files are not defined -> looking for WEB-INF/faces-config.xml
            WebModule wm = WebModule.getWebModule(dd);
            FileObject baseDir = wm.getDocumentBase();
            FileObject fo = baseDir.getFileObject("WEB-INF/faces-config.xml");
            if (fo != null)
                return new String[]{"WEB-INF/faces-config.xml"};
        }
        return new String[]{};
    }
    
    public static FileObject[] getConfiFilesFO(FileObject dd){
        String[] sFiles = getConfigFiles(dd);
        if (sFiles.length > 0){
            WebModule wm = WebModule.getWebModule(dd);
            FileObject documentBase = wm.getDocumentBase();
            FileObject config;
            ArrayList files = new ArrayList();
            FileObject file;
            for (int i = 0; i < sFiles.length; i++){
                file = documentBase.getFileObject(sFiles[i]);
                if (file != null)
                    files.add(file);
            }
            
            return (FileObject[])files.toArray(new FileObject[files.size()]);
        }
        return new FileObject [0];
    }
    
    public static String getActionAsResource(String mapping, String action){
        String resource = "";
        if (mapping != null && mapping.length()>0){
            if (mapping.startsWith("*."))
                resource = action + mapping.substring(1);
            else
                if (mapping.endsWith("/*"))
                    resource = mapping.substring(0,mapping.length()-2) + action;
        }
        return resource;
    }
}
