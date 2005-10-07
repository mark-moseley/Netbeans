/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ServerLocationManager
 *
 */

package org.netbeans.modules.j2ee.sun.api;


import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import org.openide.modules.InstalledFileLocator;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;


public class ServerLocationManager  {
    
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"; //NOI18N
    
    private static Map ServerLocationAndClassLoaderMap = Collections.synchronizedMap((Map)new HashMap(2,1));
    
    private static void updatePluginLoader(File platformLocation, ExtendedClassLoader loader) throws Exception{
	try {
	    java.io.File f = platformLocation;
	    if (null == f || !f.exists())
		return;
	    String installRoot = f.getAbsolutePath();
            //if we are only 8.1 set the necessary property there:
            if(!isGlassFish(f)){
                System.setProperty(INSTALL_ROOT_PROP_NAME, installRoot);
            }
	    InstalledFileLocator fff= InstalledFileLocator.getDefault();
	    f = fff.locate("modules/ext/appsrvbridge.jar", null, true);
	    if (f!=null){
		loader.addURL(f);
		loadLocaleSpecificJars(f, loader);
	    } else
		System.out.println("cannot locate file modules/ext/appsrvbridge.jar");
	    
	    
	    
	    f = new File(installRoot+"/lib/appserv-admin.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/appserv-ext.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/appserv-rt.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/appserv-cmp.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/commons-logging.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/admin-cli.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/common-laucher.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/j2ee.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/install/applications/jmsra/imqjmsra.jar");
	    loader.addURL(f);
	    
	    //for AS 8.1: no more endorsed dir!!!
	    f = new File(installRoot+"/lib/xercesImpl.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/dom.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/xalan.jar");
	    loader.addURL(f);
	    //for AS 8.1:
	    f = new File(installRoot+"/lib/jaxrpc-api.jar");
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/jaxrpc-impl.jar");
	    loader.addURL(f);
	    
	    
	} catch (Exception ex2) {
	    throw new Exception(ex2.getLocalizedMessage());
	}
    }
    
    
    static public File[] getPlatformsLocation(){
	return null;
    }
    
    /* return the latest available platform, i.e if 9 and 8.1 are regsitered,
     ** this will return the location of the AS 9 server
     ** this way we can access the latest DTDs, etc that cover also the 8.1 server
     ** because of the backward compatibility requirement.
     * may return null if no platform is available...
     **/
    
    static public File getLatestPlatformLocation(){
        Iterator i = ServerLocationAndClassLoaderMap.entrySet().iterator();
        File ret =null;
        while (i.hasNext()){
            Map.Entry e = (Map.Entry)i.next();
            String loc = (String)e.getKey();
            File possibleOne = new File(loc);
            if (ret==null){
                ret =possibleOne;
            }
            if (isGlassFish(possibleOne)){
                ret =possibleOne;
            }
//            System.out.println("Location is getLatestPlatformLocation "+ret);
        }
	return ret;
	
    }
    
    
    
    
    /*
     *used to get the netbeans classload of this class.
     *
     **/
    static class Empty{
	
    }
    
    
    public static ClassLoader getServerOnlyClassLoader(File platformLocation){
	CacheData data =(CacheData) ServerLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
	if (data==null){// try to initialize it
	    getNetBeansAndServerClassLoader(platformLocation);
	    data =(CacheData) ServerLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
            if (data==null){
                return null;
            }
	}
        	return data.serverOnlyClassLoader;

    }
    public synchronized static DeploymentFactory getDeploymentFactory(File platformLocation) {
	CacheData data =(CacheData) ServerLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
	if (data==null){// try to initialize it
	    getNetBeansAndServerClassLoader(platformLocation);
	    data =(CacheData) ServerLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
            if (data==null){
                return null;
            }
	}
	return data.deploymentFactory;
    
    }
    
    public synchronized static ClassLoader getNetBeansAndServerClassLoader(File platformLocation) {
	CacheData data =(CacheData) ServerLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
	if (data==null){
	    if (!isGoodAppServerLocation(platformLocation))
		return null;
            data = new CacheData();
	    ServerLocationAndClassLoaderMap.put(platformLocation.getAbsolutePath(), data);
	    
	}
	if(data.cachedClassLoader==null){
	    if (!isGoodAppServerLocation(platformLocation))
		return null;
	    try {
		data.cachedClassLoader =new ExtendedClassLoader( new Empty().getClass().getClassLoader());
		updatePluginLoader( platformLocation, data.cachedClassLoader);
		data.deploymentFactory =  (DeploymentFactory) data.cachedClassLoader.loadClass("com.sun.enterprise.deployapi.SunDeploymentFactory").newInstance();//NOI18N
                data.serverOnlyClassLoader = new ExtendedClassLoader();
                updatePluginLoader(platformLocation, data.serverOnlyClassLoader);
	    } catch (Exception ex2) {
		org.openide.ErrorManager.getDefault().notify(ex2);
		System.out.println(ex2);
	    }}
	
	return data.cachedClassLoader;
    }
    
    
    private static Collection fileColl = new java.util.ArrayList();
    
    static {
	fileColl.add("bin");
	fileColl.add("lib");
	fileColl.add("config");
    }
    
    public static boolean isGlassFish(File candidate){
	//now test for AS 9 (J2EE 5.0) which should work for this plugin
	File as9 = new File(candidate.getAbsolutePath()+"/lib/dtds/sun-web-app_2_5-0.dtd");
	return as9.exists();
    }
    
    public static boolean isGoodAppServerLocation(File candidate){
	if (null == candidate || !candidate.exists() || !candidate.canRead() ||
		!candidate.isDirectory()  || !hasRequiredChildren(candidate, fileColl)) {
	    
	    return false;
	}
	//now test for AS 9 (J2EE 5.0) which should work for this plugin
	if(isGlassFish(candidate)){
	    return true;//we are as9
	}
	
	//one extra test to detect 8.0 versus 8.1: dom.jar has to be in lib not endorsed anymore:
	File f = new File(candidate.getAbsolutePath()+"/lib/dom.jar");
	return f.exists();
	
    }
    
    
    
    
    
    private static void loadLocaleSpecificJars(File file, ExtendedClassLoader loader) {
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

    private static   boolean hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate)
            return false;
        String[] children = candidate.list();
        if (null == children)
            return false;
        if (null == requiredChildren)
            return true;
        java.util.List kidsList = java.util.Arrays.asList(children);
        return kidsList.containsAll(requiredChildren);
    }
    
    static class CacheData{
	public CacheData(){
	    
	}
	public ExtendedClassLoader cachedClassLoader;
	public ExtendedClassLoader serverOnlyClassLoader;
        
	public DeploymentFactory deploymentFactory;
	
    }
}
