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
package org.netbeans.core;

import org.netbeans.core.modules.ManifestSection;

/** Implements necessary callbacks from module system.
 *
 * @author Jaroslav Tulach
 */
public final class CoreBridgeImpl extends org.netbeans.core.modules.CoreBridge {
    protected void attachToCategory (Object category) {
        ModuleActions.attachTo(category);
    }
    protected org.netbeans.core.modules.ModuleSystem getModuleSystem () {
        return NbTopManager.get().getModuleSystem();
    }
    protected void systemClassLoaderChanged (ClassLoader l) {
        NbTopManager.Lkp.systemClassLoaderChanged(l);
    }
    
    protected void moduleLookupReady (org.openide.util.Lookup l) {
        NbTopManager.Lkp.moduleLookupReady(l);
    }
    
    protected void moduleClassLoadersUp () {
        NbTopManager.Lkp.moduleClassLoadersUp();
    }
    
    protected void modulesClassPathInitialized () {
        NbTopManager.Lkp.modulesClassPathInitialized();
    }
    
    protected void loadDefaultSection (
        org.netbeans.core.modules.ManifestSection s, 
        org.openide.util.lookup.InstanceContent.Convertor convertor, 
        boolean load
    ) {
        if (load) {
            if (convertor != null) {
                NbTopManager.get().register(s, convertor);
            } else {
                NbTopManager.get().register(s);
            }
        } else {
            if (convertor != null) {
                NbTopManager.get().unregister(s, convertor);
            } else {
                NbTopManager.get().unregister(s);
            }
        }
    }
    
    protected void loadActionSection(ManifestSection.ActionSection s, boolean load) throws Exception {
        if (load) {
            ModuleActions.add(s);
        } else {
            ModuleActions.remove(s);
        }
    }
    
    protected void loadLoaderSection(ManifestSection.LoaderSection s, boolean load) throws Exception {
        if (load) {
            LoaderPoolNode.add(s);
        } else {
            LoaderPoolNode.remove((org.openide.loaders.DataLoader)s.getInstance());
        }
    }
    
    protected void loaderPoolTransaction (boolean begin) {
        if (begin) {
            LoaderPoolNode.beginUpdates();
        } else {
            LoaderPoolNode.endUpdates();
        }
    }

    protected void addToSplashMaxSteps (int cnt) {
        Main.addToSplashMaxSteps (cnt);
    }
    protected void incrementSplashProgressBar () {
        Main.incrementSplashProgressBar ();
    }
    
    protected Layer getUserModuleLayer () {
        return org.netbeans.core.projects.ModuleLayeredFileSystem.getUserModuleLayer();
    }
    protected Layer getInstallationModuleLayer () {
        return org.netbeans.core.projects.ModuleLayeredFileSystem.getInstallationModuleLayer();
    }
}
