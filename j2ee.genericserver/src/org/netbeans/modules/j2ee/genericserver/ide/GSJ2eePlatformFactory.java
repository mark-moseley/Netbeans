/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.genericserver.ide;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public class GSJ2eePlatformFactory extends J2eePlatformFactory {
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        return new J2eePlatformImplImpl();
    }
    
    private class J2eePlatformImplImpl extends J2eePlatformImpl {
        
        public boolean isToolSupported(String toolName) {
            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            return new File[0];
        }
        
        public Set getSupportedSpecVersions() {
            Set result = new HashSet();
            result.add(J2eeModule.J2EE_14);
            return result;
        }
        
        public java.util.Set getSupportedModuleTypes() {
            Set result = new HashSet();
//            result.add(J2eeModule.EAR);
//            result.add(J2eeModule.WAR);
            result.add(J2eeModule.EJB);
//            result.add(J2eeModule.CONN);
//            result.add(J2eeModule.CLIENT);
            return result;
        }
        
        public Set/*<String>*/ getSupportedJavaPlatformVersions() {
            Set versions = new HashSet();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            versions.add("1.6"); // NOI18N
            return versions;
        }
        
        public JavaPlatform getJavaPlatform() {
            return null;
        }
        
        public java.io.File[] getPlatformRoots() {
            return new File[0];
        }
        
        public LibraryImplementation[] getLibraries() {
            return new LibraryImplementation[0];
        }
        
        public java.awt.Image getIcon() {
            return Utilities.loadImage("org/netbeans/modules/j2ee/genericserver/resources/GSInstanceIcon.gif"); // NOI18N
        }
        
        public String getDisplayName() {
            return "Generic Server Platform";
        }
        
    }
    
}
