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

package org.netbeans.modules.gsfpath.platform;

import java.io.IOException;
import java.lang.ref.*;
import java.util.*;
import org.netbeans.spi.gsfpath.platform.CustomPlatformInstall;
import org.netbeans.spi.gsfpath.platform.GeneralPlatformInstall;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.netbeans.spi.gsfpath.platform.PlatformInstall;
import org.openide.util.NbCollections;

/**
 * Simple helper class, which keeps track of registered PlatformInstallers.
 * It caches its [singleton] instance for a while.
 *
 * @author Svata Dedic
 */
public class InstallerRegistry {
    static final String INSTALLER_REGISTRY_FOLDER = "org-netbeans-api-gsfpath/platform/installers"; // NOI18N
    
    static Reference<InstallerRegistry> defaultInstance = new WeakReference<InstallerRegistry>(null);
    
    private Provider provider;
    private List<GeneralPlatformInstall> platformInstalls;      //Used by unit test
    
    InstallerRegistry(FileObject registryResource) {
        assert registryResource != null;
        this.provider = new Provider (registryResource);
    }
    
    /**
     * Used only by unit tests
     */
    InstallerRegistry (GeneralPlatformInstall[] platformInstalls) {
        assert platformInstalls != null;
        this.platformInstalls = Arrays.asList(platformInstalls);
    }
    
    /**
     * Returns all registered Java platform installers, in the order as
     * they are specified by the module layer(s).
     */
    public List<PlatformInstall> getInstallers () {
        return filter(getAllInstallers(),PlatformInstall.class);
    }
    
    public List<CustomPlatformInstall> getCustomInstallers () {
        return filter(getAllInstallers(),CustomPlatformInstall.class);
    }
    
    public List<GeneralPlatformInstall> getAllInstallers () {
        if (this.platformInstalls != null) {
            //In the unit test
            return platformInstalls;
        }
        else {
            List<GeneralPlatformInstall> list = Collections.emptyList();
            try {
                assert this.provider != null;
                list = NbCollections.checkedListByCopy((List) provider.instanceCreate(), GeneralPlatformInstall.class, true);
            } catch (IOException ex) {
            } catch (ClassNotFoundException ex) {
            }
            return list;
        }
    }
    
    

    /**
     * Creates/acquires an instance of InstallerRegistry
     */
    public static InstallerRegistry getDefault() {
        InstallerRegistry regs = defaultInstance.get();
        if (regs != null)
            return regs;
        regs = new InstallerRegistry(Repository.getDefault().getDefaultFileSystem().findResource(
            INSTALLER_REGISTRY_FOLDER));
        defaultInstance = new WeakReference<InstallerRegistry>(regs);
        return regs;
    }
    
    
    /**
     * Used only by Unit tests.
     * Sets the {@link InstallerRegistry#defaultInstance} to the new InstallerRegistry instance which 
     * always returns the given GeneralPlatformInstalls
     * @return an instance of InstallerRegistry which has to be hold by strong reference during the test
     */
    static InstallerRegistry prepareForUnitTest (GeneralPlatformInstall[] platformInstalls) {
        InstallerRegistry regs = new InstallerRegistry (platformInstalls);
        defaultInstance = new WeakReference<InstallerRegistry>(regs);
        return regs;
    }
        
    
    private static <T> List<T> filter(List<?> list, Class<T> clazz) {
        List<T> result = new ArrayList<T>(list.size());
        for (Object item : list) {
            if (clazz.isInstance(item)) {
                result.add(clazz.cast(item));
            }
        }
        return result;
    }
    
    private static class Provider extends FolderInstance {
        
        Provider (FileObject registryResource) {            
            super(DataFolder.findFolder(registryResource));
        }
        
        
        protected Object createInstance(InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
            List<Object> installers = new ArrayList<Object>(cookies.length);
            for (int i = 0; i < cookies.length; i++) {
                InstanceCookie cake = cookies[i];
                Object o = null;
                try {
                    if (cake instanceof InstanceCookie.Of &&
                        !((((InstanceCookie.Of)cake).instanceOf(PlatformInstall.class))  ||
                        (((InstanceCookie.Of)cake).instanceOf(CustomPlatformInstall.class))))
                        continue;
                    o = cake.instanceCreate();
                } catch (IOException ex) {
                } catch (ClassNotFoundException ex) {
                }
                if (o != null)
                    installers.add(o);
            }
            return installers;
        }        
    }
}
