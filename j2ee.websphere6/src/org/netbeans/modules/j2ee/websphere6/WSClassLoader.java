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
package org.netbeans.modules.j2ee.websphere6;

import java.io.*;
import java.net.*;
import java.util.*;

import org.netbeans.modules.j2ee.websphere6.util.WSDebug;

/**
 * The singleton classloader that is used for loading the WebSphere classes.
 * This loader has exactly one instance per server domain and is set as the
 * thread's context classloader before any operation on WS classes is called.
 *
 * @author Kirill Sorokin
 */
public class WSClassLoader extends URLClassLoader {

    /**
     * A <code>HashMap</code> used to store all registered instances of the
     * loader
     */
    private static Map instances = new HashMap();
    
    /**
     * A factory method.
     * It is responsible for maintaining a single instance of
     * <code>WSClassLoader</code> for each profile root.
     *
     * @param serverRoot path to the server installation directory
     * @param domainRoot path to the profile root directory
     */
    public static WSClassLoader getInstance(String serverRoot,
            String domainRoot) {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(WSClassLoader.class, "getInstance(" +       // NOI18N
                    serverRoot + ", " + domainRoot + ")");             // NOI18N
        
        // check whether such instance is already registered
        WSClassLoader instance = (WSClassLoader) instances.get(domainRoot);
        
        // if it's not, create a new one and register
        if (instance == null) {
            instance = new WSClassLoader(serverRoot, domainRoot);
            instances.put(domainRoot, instance);
        }
        
        // return
        return instance;
    }
    
    /**
     * Path to the server installation directory
     */
    private String serverRoot;
    
    /**
     * Path to the profile root directory
     */
    private String domainRoot;
    
    /**
     * Constructs an instance of the <code>WSClassLoader</code> with the
     * specified server installation directory and the profile root directory.
     *
     * @param serverRoot path to the server installation directory
     * @param domainRoot path to the profile root directory
     */
    private WSClassLoader(String serverRoot, String domainRoot) {
        // we have to isolate the loader from the netbeans main loader in order
        // to avoid conflicts with SOAP classes implementations
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        
        // save the instance variables
        this.serverRoot = serverRoot;
        this.domainRoot = domainRoot;
        
        // add the required directories to the class path
        File[] directories = new File[] {
            new File(serverRoot + "/lib/"),                            // NOI18N
            new File(serverRoot + "/java/jre/lib/"),                   // NOI18N
            new File(serverRoot + "/java/jre/lib/ext/"),                // NOI18N
            new File(serverRoot + "/lib/WMQ/java/lib/"),
            new File(serverRoot + "/cloudscape/lib/"),
            new File(serverRoot + "/cloudscape/lib/locales/"),
            new File(serverRoot + "/cloudscape/lib/otherjars/"),
            new File(serverRoot + "/deploytool/itp/"),
            new File(serverRoot + "/deploytool/itp/plugins/"),
            new File(serverRoot + "/installedChannels/"),
            new File(serverRoot + "/etc/"),
            new File(serverRoot + "/optionalLibraries/Apache/Struts/1.1/")
        };
        
        // for each directory add all the .jar files to the class path
        // and finally add the directory itself
        for (int i = 0; i < directories.length; i++) {
            File directory = directories[i];
            if (directory.exists() && directory.isDirectory()) {
                File[] children = directory.listFiles(new JarFileFilter());
                for (int j = 0; j < children.length; j++) {
                    try {
                        addURL(children[j].toURL());
                    } catch (MalformedURLException e) {
                        // do nothing just skip this jar file
                    }
                }
            }
            try {
                addURL(directory.toURL());
            } catch (MalformedURLException e) {
                // do nothing just skip this directory
            }
        }
    }
    
    /**
     * Handle for the clasloader that was the context loader for the current 
     * thread before update
     */
    private ClassLoader oldLoader;
    
    /**
     * Updates the context classloader of the current thread. 
     * The old loader is saved so that a restore operation is possible.
     */
    public void updateLoader() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), "updateLoader()");              // NOI18N
        
        // set the system properties that are required for correct functioning
        // of WebSphere
        System.setProperty("websphere.home", serverRoot);              // NOI18N
        System.setProperty("was.install.root", serverRoot);            // NOI18N
        System.setProperty("was.repository.root", domainRoot +         // NOI18N
                File.separator + "config");                            // NOI18N
        
        // if debugging is enabled set the system property pointing to the WS
        // debug properties file
        if (WSDebug.isEnabled())
            System.setProperty("traceSettingsFile",                    // NOI18N
                    "TraceSettings.properties");                       // NOI18N
        
        // save the current context loader and update the thread if we are not
        // already the context loader
        if (!Thread.currentThread().getContextClassLoader().equals(this)) {
            oldLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this);
        }
    }
    
    /**
     * Restores the thread's context classloader.
     * Set the current thread's context loader to the classloader stored in
     * <code>oldLoader</code> variable.
     */
    public void restoreLoader() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), "restoreLoader()");             // NOI18N
        
        // restore the loader if it's not null
        if (oldLoader != null) {
            Thread.currentThread().setContextClassLoader(oldLoader);
            oldLoader = null;
        }
    }
    
    /**
     * File filter that accepts only .jar files.
     * 
     * @author Kirill Sorokin
     */
    private static class JarFileFilter implements FileFilter {
        /**
         * Checks whether the supplied file complies with the filter 
         * requirements.
         * 
         * @return whether the file complies with the requirements
         */
        public boolean accept(File file) {
            // check the file's extension, if it's '.jar' then the file is ok
            if (file.getName().endsWith(".jar")) {                     // NOI18N
                return true;
            } else {
                return false;
            }
        }
    }
}
