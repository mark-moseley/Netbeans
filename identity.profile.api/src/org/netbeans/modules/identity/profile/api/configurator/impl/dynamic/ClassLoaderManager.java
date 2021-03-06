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
package org.netbeans.modules.identity.profile.api.configurator.impl.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.openide.util.NbBundle;

/**
 * This class manages classloaders for each instance of the AM client 
 * sdk. This is used to support local and remote AM server instances.
 * 
 * @author ptliu
 */
class ClassLoaderManager {
    
    private static final String AM_SYSTEM_PROPERTIES_CLASS = "com.iplanet.am.util.SystemProperties";  //NOI18N
    
    private static final String AM_INITIALIZE_PROPERTIES_METHOD = "initializeProperties";     //NOI18N
    
    private static String[] dependentJars = {
        "/addons/accessmanager/amclientsdk.jar",      //NOI18N
        "/lib/javaee.jar",                       //NOI18N
        "/lib/webservices-rt.jar"                    //NOI18N
    };
    
    private static ClassLoaderManager instance;
    
    private Map<String, AMClientSDKClassLoader> classLoaderCache =
            new HashMap<String, AMClientSDKClassLoader>();
    
    private ClassLoaderManager() {
        
    }
    
    public static ClassLoaderManager getDefault() {
        if (instance == null) {
            instance = new ClassLoaderManager();
        }
        
        return instance;
    }
    
    /**
     * TODO:  Need to pass in configuration information for the amclientsdk.
     *
     */
    public ClassLoader getClassLoader(ServerProperties properties) {
        String key = properties.getProperty(ServerProperties.PROP_ID);
        AMClientSDKClassLoader classLoader = classLoaderCache.get(key);
        
        // First check to see if the server properties have changed.
        // If so, we need to recreate the class loader.
        if (classLoader != null) {
            if (!properties.equals(classLoader.getProperties())) {
                classLoader = null;
            }
        }
        
        if (classLoader == null) {
            classLoader = createClassLoader(properties);
            classLoaderCache.put(key, classLoader);
        }
        
        return classLoader;
    }
    
    public void removeClassLoader(ServerProperties properties) {
        classLoaderCache.remove(properties.getProperty(ServerProperties.PROP_ID));
    }
    
    private AMClientSDKClassLoader createClassLoader(ServerProperties properties) {
        String asRoot = properties.getProperty(ServerProperties.PROP_AS_ROOT);
        
        //System.out.println("asRoot = " + asRoot);
        
        if (asRoot != null) {
            URL[] urls = new URL[dependentJars.length];
            
            for (int i = 0; i < dependentJars.length; i++) {
                File jarFile = new File(asRoot + dependentJars[i]);
                
                try {
                    URL url = jarFile.toURL();
                    urls[i] = url;
                } catch (MalformedURLException ex) {
                    // ignore
                }
            }
            
            return new AMClientSDKClassLoader(urls, properties);
        } else {
            return new AMClientSDKClassLoader(new URL[] {},
                    ClassLoaderManager.class.getClassLoader(), properties);
        }
    }
    
    private static class AMClientSDKClassLoader extends URLClassLoader {
        private ServerProperties properties;
        
        public AMClientSDKClassLoader(URL[] urls, ServerProperties properties) {
            super(urls);
            
            init(properties);
        }
        
        public AMClientSDKClassLoader(URL[] urls, ClassLoader parent,
                ServerProperties properties) {
            super(urls, parent);
            
            init(properties);
        }
        
        private void init(ServerProperties properties) {
            this.properties = (ServerProperties) properties.clone();
            
            try {
                Class systemPropClazz = loadClass(AM_SYSTEM_PROPERTIES_CLASS);
                Method method = systemPropClazz.getMethod(AM_INITIALIZE_PROPERTIES_METHOD,
                        Properties.class);
                method.invoke(null, properties);
            } catch (ClassNotFoundException ex) {
                throw new ConfiguratorException(NbBundle.getMessage(ClassLoaderManager.class,
                        "TXT_FailedToLoadAMClientSDK"));
            } catch (Exception ex) {
                throw ConfiguratorException.create(ex);
            }
        }
        
        public ServerProperties getProperties() {
            return properties;
        }
    }
}
