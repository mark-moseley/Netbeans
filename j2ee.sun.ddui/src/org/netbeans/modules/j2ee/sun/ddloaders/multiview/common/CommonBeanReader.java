/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


/**
 *
 * @author Peter Williams
 */
public abstract class CommonBeanReader
{
    protected String propertyName;
    
    protected void addMapString(Map<String, Object> map, String property, String value) {
        if(Utils.notEmpty(value)) {
            map.put(property, value);
        }
    }
    
    public CommonBeanReader(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public Map<String, Object> readDescriptor(CommonDDBean commonDD) {
        Map<String, Object> result = null;
        
        try {
            commonDD = normalizeParent(commonDD);
            // Need to call getValues() here, but it's not exposed by ddapi :(
//            Object value = (commonDD != null) ? commonDD.getValues(propertyName) : null;
            Object value = (commonDD != null) ? getChild(commonDD, propertyName) : null;
            if(value != null && value.getClass().isArray() && value instanceof CommonDDBean []) {
                result = genProperties((CommonDDBean []) value);
            }
        } catch(Exception ex) {  // e.g. schema2beans missing property exception.
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return result;
    }
    
    public Map<String, Object> readAnnotations(DataObject dObj) {
        Map<String, Object> result = null;
        try {
            File key = FileUtil.toFile(dObj.getPrimaryFile());
            SunONEDeploymentConfiguration dc = SunONEDeploymentConfiguration.getConfiguration(key);
            if(dc != null) {
                J2eeModule module = dc.getJ2eeModule();
                if(module != null) {
                    if(J2eeModule.WAR == module.getModuleType()) {
                        readWebAppMetadata(module.getMetadataModel(WebAppMetadata.class));
                    } else if(J2eeModule.EJB == module.getModuleType()) {
                        result = readEjbJarMetadata(module.getMetadataModel(EjbJarMetadata.class));
                    } else if(J2eeModule.CLIENT == module.getModuleType()) {
                        result = readAppClientMetadata(module.getMetadataModel(AppClientMetadata.class));
                    }
                }
            }
        } catch(MetadataModelException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return result;
    }
    
    /** Maps interesting fields from ejb-jar descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    protected abstract Map<String, Object> genProperties(CommonDDBean [] beans);

    /** For normalizing data structures within /ejb-jar graph, e.g. 
     *    assembly-descriptor/security-role
     *    enterprise-beans/message-destination
     *    enterprise-beans/ejb
     */
    protected CommonDDBean normalizeParent(CommonDDBean oldParent) {
        return oldParent;
    }
    
    /** Used by derived classes to locate a parent ejb by it's name, if one
     *  exists and we're reading /ejb-jar.
     */ 
    protected CommonDDBean findEjbByName(EjbJar ejbJar, String ejbName) {
        CommonDDBean match = null;
        EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
        if(eb != null) {
            match = findEjbByName(eb.getSession(), ejbName);
            if(match == null) {
                match = findEjbByName(eb.getMessageDriven(), ejbName);
            }
            if(match == null) {
                match = findEjbByName(eb.getEntity(), ejbName);
            }
        }
        return match;
    }
    
    protected CommonDDBean findEjbByName(Ejb [] ejbs, String ejbName) {
        CommonDDBean match = null;
        if(ejbs != null) {
            for(Ejb ejb: ejbs) {
                if(ejbName.equals(ejb.getEjbName())) {
                    match = ejb;
                    break;
                }
            }
        }
        return match;
    }
    
    /** Entry points to generate map from annotation metadata
     */
    public Map<String, Object> readWebAppMetadata(MetadataModel<WebAppMetadata> model) 
            throws MetadataModelException, IOException {
        return model.runReadAction(new WebAppCommonReader());
    }
    
    public Map<String, Object> readAppClientMetadata(MetadataModel<AppClientMetadata> model) 
            throws MetadataModelException, IOException {
        return model.runReadAction(new AppClientCommonReader());
    }
    
    public Map<String, Object> readEjbJarMetadata(MetadataModel<EjbJarMetadata> model) 
            throws MetadataModelException, IOException {
        return model.runReadAction(new EjbJarCommonReader());
    }
    
    public Map<String, Object> readWebservicesMetadata(MetadataModel<WebservicesMetadata> model) 
            throws MetadataModelException, IOException {
        return model.runReadAction(new WebservicesCommonReader());
    }
    
    // Metadata model run methods
    public class WebAppCommonReader extends CommonReader 
            implements MetadataModelAction<WebAppMetadata, Map<String, Object>> {

        public Map<String, Object> run(WebAppMetadata metadata) throws Exception {
            return genCommonProperties(metadata.getRoot());
        }
        
    }
    
    public class AppClientCommonReader extends CommonReader 
            implements MetadataModelAction<AppClientMetadata, Map<String, Object>> {

        public Map<String, Object> run(AppClientMetadata metadata) throws Exception {
            CommonDDBean newParent = normalizeParent(metadata.getRoot());
            return genCommonProperties(newParent);
        }
        
    }
    
    public class EjbJarCommonReader extends CommonReader 
            implements MetadataModelAction<EjbJarMetadata, Map<String, Object>> {

        public Map<String, Object> run(EjbJarMetadata metadata) throws Exception {
            CommonDDBean newParent = normalizeParent(metadata.getRoot());
            return genCommonProperties(newParent);
        }
        
    }
    
    public class WebservicesCommonReader extends CommonReader 
            implements MetadataModelAction<WebservicesMetadata, Map<String, Object>> {

        public Map<String, Object> run(WebservicesMetadata metadata) throws Exception {
            CommonDDBean newParent = normalizeParent(metadata.getRoot());
            return genCommonProperties(newParent);
        }
        
    }
    
    public class CommonReader {
        
        public Map<String, Object> genCommonProperties(CommonDDBean parentDD) {
            Map<String, Object> result = null;
            if(parentDD != null) {
                Object value = getChild(parentDD, propertyName);
                if(value != null && value.getClass().isArray() && value instanceof CommonDDBean []) {
                    result = genProperties((CommonDDBean []) value);
                }
            }
            return result;
        }
        
    }

    // Introspection to call appropriate get[Property] method
    private static WeakHashMap<String, Method> methodMap = new WeakHashMap<String, Method>();

    private static Object getChild(CommonDDBean bean, String propertyName) {
        // equivalent to bean.getValue(propertyName), but via instrospection.
        Object result = null;
        try {
            String getterName = "get" + propertyName;
            Class beanClass = bean.getClass();
            String key = beanClass.getName() + getterName;
            Method getter = methodMap.get(key);
            if(getter == null) {
                getter = beanClass.getMethod(getterName);
                methodMap.put(key, getter);
//            } else {
//                System.out.println("Using cached method " + getter.getName() + " on " + beanClass.getName());
            }
            result = getter.invoke(bean);
        } catch(InvocationTargetException ex) {
            if(ex.getCause() instanceof UnsupportedOperationException) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "!!!" + bean.getClass().getName() + ".get" + propertyName + " is not supported by metamodel yet.");
            } else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return result;
    }    

}
