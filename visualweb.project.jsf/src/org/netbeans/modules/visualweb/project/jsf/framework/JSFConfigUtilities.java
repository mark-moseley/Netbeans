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

package org.netbeans.modules.visualweb.project.jsf.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Po-Ting Wu
 */
public class JSFConfigUtilities {
    
    private static String CONFIG_FILES_PARAM_NAME = "javax.faces.CONFIG_FILES"; //NOI18N
    private static String DEFAULT_FACES_CONFIG_PATH = "WEB-INF/faces-config.xml"; //NOI18N
    
    
    /* <RAVE> Not needed and comment out because of No access to JSFConfigDataObject
    public static NavigationRule findNavigationRule(JSFConfigDataObject data, String fromView){
        NavigationRule navigationRule = null;
        FacesConfig config = ConfigurationUtils.getConfigModel(data.getPrimaryFile(), true).getRootComponent();
        Collection<NavigationRule> rules = config.getNavigationRules();
        for (Iterator<NavigationRule> it = rules.iterator(); it.hasNext();) {
            NavigationRule nRule = it.next();
            if ((fromView != null && fromView.equals(nRule.getFromViewId()))
                    || (fromView == null && (nRule.getFromViewId() == null || nRule.getFromViewId().trim().length()==0))){
                navigationRule = nRule;
                continue;
            }
        }
        return navigationRule;
    }
    </RAVE> */
    
    /** Returns the navigation rule, where the FromViewID is the parameter. If the rule doesn't exist
     * then returns null.
     */
    //    public static NavigationRule findNavigationRule(FacesConfig config, String fromView){
    //        if (fromView != null){
    //            FacesConfig config = getConfigModel(data.getPrimaryFile(), true).getRootComponent();
    //            NavigationRule [] rules = config.getNavigationRule();
    //            for (int i = 0; i < rules.length; i++)
    //                if (fromView.equals(rules[i].getFromViewId()))
    //                    return rules[i];
    //        }
    //        return null;
    //    }
    
    /** Returns WebPages for the project, where the fo is located.
     */
    public static SourceGroup[] getDocBaseGroups(FileObject fileObject) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(fileObject);
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
    }
    
    public static String getResourcePath(SourceGroup[] groups,FileObject fileObject, char separator, boolean withExt) {
        for (int i=0;i<groups.length;i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root,fileObject)) {
                String relativePath = FileUtil.getRelativePath(root,fileObject);
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
    
    
    
    
    
    public static boolean validateXML(FileObject deploymentDesc){
        boolean value = false;  // the default value of the com.sun.faces.validateXml
        if (deploymentDesc != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.validateXml"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return value;
    }
    
    public static boolean verifyObjects(FileObject deploymentDesc){
        boolean value = false; // the default value of the com.sun.faces.verifyObjects
        if (deploymentDesc != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.verifyObjects"); //NOI18N
                if (param != null)
                    value = "true".equals(param.getParamValue().trim());
            } catch (java.io.IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return value;
    }
    
    /** Returns relative path for all jsf configuration files in the web module. If there is no
     *  configuration file, then returns String array with lenght = 0.
     */
    public static String[] getConfigFiles(FileObject deploymentDesc){
        ArrayList<String> files = new ArrayList();
        String[]  filesURI;
        // looking for WEB-INF/faces-config.xml
        WebModule webModule = null;
        if (deploymentDesc != null) {
            webModule = WebModule.getWebModule(deploymentDesc);
        }
        if (webModule != null) {
            FileObject baseDir = webModule.getDocumentBase();
            FileObject fileObject = baseDir.getFileObject(DEFAULT_FACES_CONFIG_PATH);
            if (fileObject != null)
                files.add(DEFAULT_FACES_CONFIG_PATH);
            if (deploymentDesc != null) {
                InitParam param = null;
                try{
                    WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
                    if (webApp != null) {
                        // Fix for #117845. Cannot be used the method webApp.findBeanByName,
                        // because this method doesn't trim the names of attribute.
                        InitParam[] params = webApp.getContextParam();
                        for (int i = 0; i < params.length; i++) {
                            InitParam initParam = params[i];
                            if (CONFIG_FILES_PARAM_NAME.equals(initParam.getParamName().trim())) {
                                param = initParam;
                                break;
                            }
                        }
                    }
                } catch (java.io.IOException e) {
                    Exceptions.printStackTrace(e);
                }

                if (param != null){
                    // the configuration files are defined
                    String value = param.getParamValue().trim();
                    if (value != null){
                        filesURI = value.split(",");
                        for (int i = 0; i < filesURI.length; i++) {
                            String file = filesURI[i].trim();
                            // prevent to be default faces config twice listed twice
                            if (!DEFAULT_FACES_CONFIG_PATH.equals(file) // need to check absolute and relative path
                                    && !("/" + DEFAULT_FACES_CONFIG_PATH).equals(file)) { //NOI18N
                                files.add(file);
                            }
                        }
                    }
                }
            }
        }
        filesURI = new String[files.size()];
        return files.toArray(filesURI);
    }
}
