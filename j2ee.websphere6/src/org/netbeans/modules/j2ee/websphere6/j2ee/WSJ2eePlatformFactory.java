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
package org.netbeans.modules.j2ee.websphere6.j2ee;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.netbeans.spi.project.libraries.*;
import org.netbeans.modules.j2ee.deployment.common.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.websphere6.*;

/**
 * A sub-class of the J2eePlatformFactory that is set up to return the
 * plugin-specific J2eePlatform.
 *
 * @author Kirill Sorokin
 */
public class WSJ2eePlatformFactory extends J2eePlatformFactory {
    
    /**
     * Factory method for WSJ2eePlatformImpl. This method is used for
     * constructing the plugin-specific J2eePlatform object.
     *
     * @param dm the server specific deployment manager that can be used as an
     * additional source of information
     */
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        return new J2eePlatformImplImpl(dm);
    }
    
    /**
     * The plugin implementation of the J2eePlatform interface. It is used to
     * provide all kinds of information about the environment that the deployed
     * application will run against, such as the set of .jsr files representing
     * the j2ee implementation, which kinds of application the server may
     * contain, which j2ee specification version the server supports, etc.
     */
    private static class J2eePlatformImplImpl extends J2eePlatformImpl {
        
        /**
         * The platform icon's URL
         */
        private static final String ICON = "org/netbeans/modules/" +   // NOI18N
                "j2ee/websphere6/resources/16x16.gif";                 // NOI18N
        
        /**
         * The server's deployment manager, to be exact the plugin's wrapper for
         * it
         */
        WSDeploymentManager dm;
        
        /**
         * Creates a new instance of J2eePlatformImplImpl.
         *
         * @param dm the server's deployment manager
         */
        public J2eePlatformImplImpl(DeploymentManager dm) {
            // save the prarmeters
            this.dm = (WSDeploymentManager) dm;
        }
        
        /**
         * Defines whether the platform supports the named tool. Since it's
         * unclear what actually a 'tool' is, currently it returns false.
         *
         * @param toolName tool name
         *
         * @return false
         */
        public boolean isToolSupported(String toolName) {
            return false;
        }
        
        /**
         * Gets the classpath entries for the named tool. Since it's
         * unclear what actually a 'tool' is, currently it returns an empty
         * array.
         *
         * @param toolName tool name
         *
         * @return an empty array of File
         */
        public File[] getToolClasspathEntries(String toolName) {
            return new File[0];
        }
        
        /**
         * Specifies which versions of j2ee the server supports.
         *
         * @return a Set with the supported versions
         */
        public Set getSupportedSpecVersions() {
            // init the set
            Set result = new HashSet();
            
            // add j2ee 1.4
            result.add(J2eeModule.J2EE_14);
            
            // return
            return result;
        }
        
        
        public Set getSupportedJavaPlatformVersions() {
            Set versions = new HashSet();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            return versions;
            
        }
        
        /**
         * Specifies which module types the server supports.
         *
         * @return a Set the the supported module types
         */
        public Set getSupportedModuleTypes() {
            // init the set
            Set result = new HashSet();
            
            // add supported modules
            result.add(J2eeModule.EAR);
            result.add(J2eeModule.WAR);
            result.add(J2eeModule.EJB);
            result.add(J2eeModule.CONN);
            result.add(J2eeModule.CLIENT);
            
            // return
            return result;
        }
        
        /**
         * Specifies the platform root directories. It's unclear where and why
         * it is used, for now returning the server home directory.
         *
         * @return an array of files with a single entry - the server home
         *      directory
         */
        public java.io.File[] getPlatformRoots() {
            return new File[] {
                new File(dm.getInstanceProperties().getProperty(
                        WSDeploymentFactory.SERVER_ROOT_ATTR))
            };
        }
        
        /**
         * Gets the libraries that will be attached to the project for
         * compilation. A library includes a set of jar files, sources and
         * javadocs. As there may be multiple jars per library we create only
         * one.
         *
         * @return an array of libraries
         */
        public LibraryImplementation[] getLibraries() {
            // init the resulting array
            LibraryImplementation[] libraries = new LibraryImplementation[1];
            
            // create a new library
            LibraryImplementation library = new J2eeLibraryTypeProvider().
                    createLibrary();
            
            // set its name
            library.setName(NbBundle.getMessage(WSJ2eePlatformFactory.class,
                    "TXT_libraryName"));                               // NOI18N
            
            // add the required jars to the library
            try {
                ArrayList list = new ArrayList();
                list.add(fileToUrl(new File(dm.getInstanceProperties().
                        getProperty(WSDeploymentFactory.SERVER_ROOT_ATTR),
                        "/lib/j2ee.jar")));                            // NOI18N
                
                library.setContent(J2eeLibraryTypeProvider.
                        VOLUME_TYPE_CLASSPATH, list);
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
            
            // add the created library to the array
            libraries[0] = library;
            
            // return
            return libraries;
        }
        
        /**
         * Gets the platform icon. A platform icon is the one that appears near
         * the libraries attached to j2ee project.
         *
         * @return the platform icon
         */
        public Image getIcon() {
            return Utilities.loadImage(ICON);
        }
        
        /**
         * Gets the platform display name. This one appears exactly to the
         * right of the platform icon ;)
         *
         * @return the platform's display name
         */
        public String getDisplayName() {
            return NbBundle.getMessage(WSJ2eePlatformFactory.class,
                    "TXT_platformName");                               // NOI18N
        }
        
        /**
         * Converts a file to the URI in system resources.
         * Copied from the plugin for Sun Appserver 8
         *
         * @param file a file to be converted
         *
         * @return the resulting URI
         */
        private URL fileToUrl(File file) throws MalformedURLException {
            // get the file's absolute URL
            URL url = file.toURI().toURL();
            
            // strip the jar's path and remain with the system resources URI
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            
            // return
            return url;
        }
        
        
        /**
         * Implements J2eePlatformImpl
         *
         */
        
        public JavaPlatform getJavaPlatform() {
            /* TO DO
            String currentJvm = ip.getProperty(PROP_JAVA_PLATFORM);
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            JavaPlatform[] installedPlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
            for (int i = 0; i < installedPlatforms.length; i++) {
                String platformName = (String)installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
                if (platformName != null && platformName.equals(currentJvm)) {
                    return installedPlatforms[i];
                }
            }
            // return default platform if none was set
            return jpm.getDefaultPlatform();
             */
            return null;
        }
    }
}