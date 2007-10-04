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

package org.netbeans.modules.web.jsf.editor.jspel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.completion.ELExpression;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JSFELExpression extends ELExpression{
    
    public static final int EL_JSF_BEAN = 100;
    public static final int EL_JSF_RESOURCE_BUNDLE = 101;
    
    private WebModule webModule;
    
    protected String bundle;
    
    public JSFELExpression(WebModule wm, JspSyntaxSupport sup){
        super(sup);
        this.webModule = wm;
    }
    
    @Override
    protected int findContext(String expr) {
        int dotIndex = expr.indexOf('.');
        int value = EL_UNKNOWN;
        
        if (dotIndex > -1){
            String first = expr.substring(0, dotIndex);
            
            // look through all registered managed beans
            List <ManagedBean> beans = JSFBeanCache.getBeans(webModule);
            for (int i = 0; i < beans.size(); i++) {
                if (beans.get(i).getManagedBeanName().equals(first)){
                    value = EL_JSF_BEAN;
                    break;
                }
            }
            
            // look trhough all registered resource bundles
            List <ResourceBundle> bundles = getJSFResourceBundles(webModule);
            for (int i = 0; i < bundles.size(); i++) {
                if (first.equals(bundles.get(i).getVar())) {
                    value = EL_JSF_RESOURCE_BUNDLE;
                    bundle = bundles.get(i).getBaseName();
                    break;
                }
            }
        } else if (dotIndex == -1) {
            value = EL_START;
        }
        return value;
    }
    
    @Override 
    public String getObjectClass(){
        String beanName = extractBeanName();
  
        List <ManagedBean>beans = JSFBeanCache.getBeans(webModule);
        
        for (ManagedBean bean : beans){
            if (beanName.equals(bean.getManagedBeanName())){
                return bean.getManagedBeanClass();
            }
        }
        
        return null;
    }
    
    /**
     * Finds list of all ResourceBundles, which are registered in all
     * JSF configuration files in a web module.
     * @param webModule
     * @return
     */
    public List <ResourceBundle> getJSFResourceBundles(WebModule webModule){
        FileObject[] files = ConfigurationUtils.getFacesConfigFiles(webModule);
        ArrayList <ResourceBundle> bundles = new ArrayList<ResourceBundle>();
        
        for (int i = 0; i < files.length; i++) {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(files[i], true).getRootComponent();
            for (int j = 0; j < facesConfig.getApplications().size(); j++) {
                Collection<ResourceBundle> resourceBundles = facesConfig.getApplications().get(j).getResourceBundles();
                for (Iterator<ResourceBundle> it = resourceBundles.iterator(); it.hasNext();) {
                    bundles.add(it.next());   
                }
            }
        }
        return bundles;
    }
    
    public  List<CompletionItem> getPropertyKeys(String propertyFile, String prefix) {
        ArrayList<CompletionItem> items = new ArrayList<CompletionItem>();
        java.util.ResourceBundle labels = null;
        ClassPath classPath;
        ClassLoader classLoader;
        
        try { // try to find on the source classpath
            classPath = ClassPath.getClassPath(sup.getFileObject(), ClassPath.SOURCE);
            classLoader = classPath.getClassLoader(false);
            labels = java.util.ResourceBundle.getBundle(propertyFile, Locale.getDefault(), classLoader);
        }  catch (MissingResourceException exception) {
            // There is not the property on source classpath - try compile
            try {
                classLoader = ClassPath.getClassPath(sup.getFileObject(), ClassPath.COMPILE).getClassLoader(false);
                labels = java.util.ResourceBundle.getBundle(propertyFile, Locale.getDefault(), classLoader);
            } catch (MissingResourceException exception2) {
                // the propertyr file wasn't find on the compile classpath as well
            }
        }
        
        if (labels != null) {  
            // the property file was found
            Enumeration<String> keys = labels.getKeys();
            String key;
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                if (key.startsWith(prefix)) {
                    StringBuffer helpText = new StringBuffer();
                    helpText.append(key).append("=<font color='#ce7b00'>"); //NOI18N
                    helpText.append(labels.getString(key)).append("</font>"); //NOI18N
                    items.add(new JSFResultItem.JSFResourceItem(key, helpText.toString()));
                }
            }
        }
        
        return items;
    }
}
