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

/**
 * Module installer class that persist the
 * @author Winston Prakash
 */
package org.netbeans.modules.websvc.registry.netbeans;

import java.io.*;

import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;


/** class WebServiceModuleInstaller
 *
 *  ModuleInstall for the web service registry module.  Handles reading
 *  the registry on module startup and saving any changes on module shutdown.
 */
public class WebServiceModuleInstaller extends ModuleInstall implements InstanceListener {
    
    private static ExtensionClassLoader specialLoader = null;
    private static boolean registryInstalled = false;
        
    public void restored() {
            restoreds();
           Deployment.getDefault().addInstanceListener(this);
        
      /*  if(registryInstalled) {
            try {
                PersistenceManagerInterface persistenceManager = (PersistenceManagerInterface)
                specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance(); //NOI18N
                persistenceManager.load(specialLoader);
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }*/
    }
    
    public void close() {
        if(registryInstalled) {
            try {
                PersistenceManagerInterface persistenceManager =(PersistenceManagerInterface)
                specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance(); //NOI18N
                persistenceManager.save(specialLoader);
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            } finally {
            }
        }
    }
    
    public void uninstalled() {
        close();
    }
    
    public  void restoreds() {
        if(specialLoader == null) {
            try {
                specialLoader = new ExtensionClassLoader(new Empty().getClass().getClassLoader());
                updatesSecialLoader(specialLoader);
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
    }
    
    public static ClassLoader getExtensionClassLoader() {
        return specialLoader;
    }
    
    public  void updatesSecialLoader(ExtensionClassLoader loader) throws Exception {
        try {
            String serverInstanceIDs[] = Deployment.getDefault().getServerInstanceIDs();
            J2eePlatform platform = null;
            for (int i = 0; i < serverInstanceIDs.length; i++) {
                J2eePlatform p = Deployment.getDefault().getJ2eePlatform(serverInstanceIDs [i]);
                if (p!= null && p.isToolSupported("wscompile")) {
                    platform = p;
                    break;
                }
            }
             File f1 = platform == null ? null : platform.getPlatformRoots() [0];
            if(f1 != null && f1.exists()) {
                String installRoot = f1.getAbsolutePath();
                InstalledFileLocator locator = InstalledFileLocator.getDefault();
                
                File f = locator.locate("modules/ext/websvcregistry.jar", null, true); // NOI18N
                if(f != null) {
                    registryInstalled = true;
                    loader.addURL(f);
                    loadLocaleSpecificJars(f, loader);
                } else {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot locate file modules/ext/websvcregistry.jar");
                }
                
                // Add correct jars from the installed application server.
                SJSASVersion appServerVersion = SJSASVersion.getSJSAppServerVersion();
                String [] registryRuntimeJars = appServerVersion.getRegistryRuntimeLibraries();
                
                for(int i = 0; i < registryRuntimeJars.length; i++) {
                    loader.addURL(new File(installRoot + registryRuntimeJars[i]));
                }
            }
        } catch(Exception ex) {
            throw new Exception(ex.getLocalizedMessage(), ex);
        }
    }
    private static void loadLocaleSpecificJars(File file, ExtensionClassLoader loader) {
        File parentDir = file.getParentFile();
        //System.out.println("parentDir: " + parentDir);
        File localeDir = new File(parentDir, "locale"); //NOI18N
        if(localeDir.exists()){
            File[] localeFiles = localeDir.listFiles();
            File localeFile = null;
            String localeFileName = null;
            String fileName = file.getName();
            fileName = getFileNameWithoutExt(fileName);
            //System.out.println("fineName: " + fileName);
            assert(fileName.length() > 0);
            for(int i=0; i<localeFiles.length; i++){
                localeFile = localeFiles[i];
                localeFileName = localeFile.getName();
                //System.out.println("localeFileName: " + localeFileName);
                assert(localeFileName.length() > 0);
                if(localeFileName.startsWith(fileName)){
                    try{
                        loader.addURL(localeFile);
                    }catch (Exception ex2) {
                        System.out.println(ex2.getLocalizedMessage());
                    }
                }
            }
        }
    }
    
    private static String getFileNameWithoutExt(String fileName){
        int index = fileName.lastIndexOf("."); //NOI18N
        if(index != -1){
            fileName = fileName.substring(0, index);
        }
        return fileName;
    }    

    public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
    }
    
    public void instanceAdded(String serverInstanceID) {
        if (registryInstalled==false){
            specialLoader = null;
            restoreds();
            try {
                PersistenceManagerInterface persistenceManager = (PersistenceManagerInterface)
                specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance(); //NOI18N
                persistenceManager.load(specialLoader);
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
            
            firePropertyChange("specialLoader", null,"specialLoader");//NOI18N
        }
    }

    public void instanceRemoved(String serverInstanceID) {
    }
        /*
         * Used to get the netbeans classloader of this class.
         *
         */
    static class Empty {
    }
}
