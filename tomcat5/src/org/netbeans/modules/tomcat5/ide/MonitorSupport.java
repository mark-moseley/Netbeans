/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.ide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import org.netbeans.api.web.dd.DDProvider;
import org.netbeans.api.web.dd.Filter;
import org.netbeans.api.web.dd.FilterMapping;
import org.netbeans.api.web.dd.InitParam;
import org.netbeans.api.web.dd.WebApp;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatManager;

import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.modules.ModuleInfo;
import org.openide.util.*;

import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;


/** Monitor enabling/disabling utilities for Tomcat 5.
 *
 * @author Milan.Kuchtiak@sun.com, Petr Jiricka
 */
public class MonitorSupport {

    // monitor module enable status data
    public static final String MONITOR_ENABLED_PROPERTY_NAME = "monitor_enabled"; // NOI18N
    private static final String MONITOR_MODULE_NAME="org.netbeans.modules.web.monitor"; //NOI18N    
    private static ModuleInfo httpMonitorInfo;
    private static ModuleSpy monitorSpy;
    private static Lookup.Result res;
    private static MonitorInfoListener monitorInfoListener;
    private static MonitorLookupListener monitorLookupListener;
    
    // data for declaration in web.xml
    private static final String MONITOR_FILTER_NAME  = "HTTPMonitorFilter"; //NOI18N
    private static final String MONITOR_FILTER_CLASS = "org.netbeans.modules.web.monitor.server.MonitorFilter"; //NOI18N
    private static final String MONITOR_FILTER_PATTERN = "/*"; //NOI18N
    private static final String MONITOR_INTERNALPORT_PARAM_NAME = "netbeans.monitor.ide"; //NOI18N
    
    public static void setMonitorFlag(String managerURL, boolean enable) {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(managerURL);
        ip.setProperty(MONITOR_ENABLED_PROPERTY_NAME, Boolean.toString(enable));
    }
    
    public static boolean getMonitorFlag(String managerURL) {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(managerURL);
        String prop = ip.getProperty(MONITOR_ENABLED_PROPERTY_NAME);
        return (prop == null) ? true : Boolean.valueOf(prop).booleanValue();
    }
    
    public static void synchronizeMonitorWithFlag(TomcatManager tm, boolean alsoSetPort, boolean alsoCopyJars) throws IOException, SAXException {
        String url = TomcatFactory.tomcatUriPrefix + tm.getUri();
        boolean monitorFlag = getMonitorFlag(url);
        boolean monitorModuleAvailable = isMonitorEnabled();
        boolean shouldInstall = monitorModuleAvailable && monitorFlag;
        
        // find the web.xml file
        File webXML = getDefaultWebXML(tm);
        if (webXML == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(url));
            return;
        }
        WebApp webApp = DDProvider.getDefault().getDDRoot(webXML);
        if (webApp == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(url));
            return;
        }
        boolean needsSave = false;
        boolean result;
        if (shouldInstall) {
            result = changeFilterMonitor(webApp, true);
            needsSave = needsSave || result;
            if (alsoSetPort) {                  
                result = specifyFilterPortParameter(webApp);
                needsSave = needsSave || result;
            }
            if (alsoCopyJars) {
                addMonitorJars(tm);
            }
        }
        else {                               
            result = changeFilterMonitor(webApp, false);
            needsSave = needsSave || result; 
        }
        if (needsSave) {
            OutputStream os = new FileOutputStream(webXML);
            try {
                webApp.write(os);
            }
            finally {
                os.close();
            }
        }
    }
    
    private static File getDefaultWebXML(TomcatManager tm) {
        File cb = tm.getCatalinaBaseDir();
        if (cb == null)
            cb = tm.getCatalinaHomeDir();
        File webXML = new File(cb, "conf" + File.separator + "web.xml");
        if (webXML.exists())
            return webXML;
        return null;
    }
    
    private static void addMonitorJars(TomcatManager tm) throws IOException {
        // getting Tomcat4.0 Directory
        File instDir = tm.getCatalinaHomeDir();
        if (instDir==null) return;
        
        copyFromIDEInstToDir("modules/ext/httpmonitor.jar"  , instDir, "common/lib/httpmonitor.jar");  // NOI18N
        copyFromIDEInstToDir("modules/schema2beans.jar"     , instDir, "common/lib/schema2beans.jar"); // NOI18N
        
        //copyFromIDEInstToDir("modules/ext/monitor-valve.jar", instDir, "server/lib/monitor-valve.jar"); // NOI18N
    }
    
    private static boolean changeFilterMonitor(WebApp webApp,boolean full) {
        boolean filterWasChanged=false;
        if (full) { // adding monitor filter/filter-mapping element
            //if (tomcat.isMonitorEnabled()) {
            boolean isFilter=false;
            Filter[] filters = webApp.getFilter();
            for(int i=0;i<filters.length;i++) {
                if (filters[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    isFilter=true;
                    break;
                }
            }
            if (!isFilter) {
                try {
                    Filter filter = (Filter)webApp.createBean("Filter"); //NOI18N
                    filter.setFilterName(MONITOR_FILTER_NAME);
                    filter.setFilterClass(MONITOR_FILTER_CLASS);
/*                    InitParam initParam = (InitParam)filter.createBean("InitParam"); //NOI18N
                    initParam.setParamName(MONITOR_INIT_PARAM_NAME);
                    initParam.setParamValue(MONITOR_INIT_PARAM_VALUE);
                    filter.addInitParam(initParam);*/
                    webApp.addFilter(filter);
                    filterWasChanged=true;
                }catch (ClassNotFoundException ex) {}
            }
            
            boolean isMapping=false;
            FilterMapping[] maps = webApp.getFilterMapping();
            for(int i=0;i<maps.length;i++) {
                if (maps[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    isMapping=true;
                    break;
                }
            }
            if (!isMapping) {
                try {
                    FilterMapping filterMapping = (FilterMapping)webApp.createBean("FilterMapping"); //NOI18N
                    filterMapping.setFilterName(MONITOR_FILTER_NAME);
                    filterMapping.setUrlPattern(MONITOR_FILTER_PATTERN);
                    webApp.addFilterMapping(filterMapping);
                    filterWasChanged=true;
                } catch (ClassNotFoundException ex) {}
            }
            //}
        } else { // removing monitor filter/filter-mapping element
            FilterMapping[] maps = webApp.getFilterMapping();
            for(int i=0;i<maps.length;i++) {
                
                if (maps[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    webApp.removeFilterMapping(maps[i]);
                    filterWasChanged=true;
                    break;
                }
            }
            Filter[] filters = webApp.getFilter();
            for(int i=0;i<filters.length;i++) {
                if (filters[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    webApp.removeFilter(filters[i]);
                    filterWasChanged=true;
                    break;
                }
            }
        }
        return filterWasChanged;
    }
    
    /** Finds a file inside the IDE installation, given a slash-separated
     * path relative to the IDE installation. Takes into account the fact that
     * modules may have been installed by Autoupdate, and reside in the user
     * home directory.
     * @param instRelPath file path relative to the inst dir, delimited by '/'
     * @return file containing the file, or null if it does not exist.
     */
    private static File findInstallationFile(String instRelPath) {
        return InstalledFileLocator.getDefault().locate(instRelPath, null, false);
    }
    
    private static void copyFromIDEInstToDir(String sourceRelPath, File copyTo, String targetRelPath) throws IOException {
        File targetFile = findFileUnderBase(copyTo, targetRelPath);
        if (!targetFile.exists()) {
            File sourceFile = findInstallationFile(sourceRelPath);
            if ((sourceFile != null) && sourceFile.exists()) {
                copy(sourceFile,targetFile);
            }
        }
    }
    
    private static void copy(File file1, File file2) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file1));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file2));
        int b;
        while((b=bis.read())!=-1)bos.write(b);
        bis.close();
        bos.close();
    }
    
    private static File findFileUnderBase(File base, String fileRelPath) {
        if (fileRelPath.startsWith("/")) { // NOI18N
            fileRelPath = fileRelPath.substring(1);
        }
        fileRelPath = fileRelPath.replace('/', File.separatorChar);
        return new File(base, fileRelPath);
    }
    
    /** Inserts or and updates in the Monitor Filter element the parameter
     *  which tells the Monitor the number of the internal port,
     *  depending on whether the integration mode is full or minimal
     *  @param webApp deployment descriptor in which to do the changes
     *  @return true if the default deployment descriptor was modified
     */
    private static boolean specifyFilterPortParameter(WebApp webApp) {
        Filter[] filters = webApp.getFilter();
        Filter myFilter = null;
        for(int i=0; i<filters.length; i++) {
            if (MONITOR_FILTER_NAME.equals(filters[i].getFilterName())) {
                myFilter = filters[i];
                break;
            }
        }
        // see if we found it
        if (myFilter == null)
            return false;
        
        // look for the parameter
        InitParam[] params = myFilter.getInitParam();
        InitParam myParam = null;
        for(int i=0; i<params.length; i++) {
            if (MONITOR_INTERNALPORT_PARAM_NAME.equals(params[i].getParamName())) {
                myParam = params[i];
                break;
            }
        }
        
        // host name acts as the parameter name
        String correctParamValue = getLocalHost() + ":" + getInternalServerPort(); // NOI18N
        
        // insert or correct the param
        if (myParam == null) {
            // add it
            try {
                InitParam init = (InitParam)myFilter.createBean("InitParam"); //NOI18N
                init.setParamName(MONITOR_INTERNALPORT_PARAM_NAME);
                init.setParamValue(correctParamValue);
                myFilter.addInitParam(init);
            } catch (ClassNotFoundException ex){}
            return true;
        }
        else {
            // check whether the value is correct
            if (correctParamValue.equals(myParam.getParamValue())) {
                // no need to change
                return false;
            }
            else {
                // change
                myParam.setParamValue(correctParamValue);
                return true;
            }
        }
        
    } // end of specifyFilterPortParameter
    
    public static String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "127.0.0.1"; // NOI18N
        }
    }
    
    private static String getInternalServerPort() {
        try {
            URL u = HttpServer.getRepositoryRoot();
            return "" + u.getPort();
        }
        catch (MalformedURLException e) {
            // no need to report the exception, as it will be reported by Tomcat40Installation
            return "8082"; // NOI18N
        }
        catch (UnknownHostException e) {
            // no need to report the exception, as it will be reported by Tomcat40Installation
            return "8082"; // NOI18N
        }
    }
    
/*    private void manageConfiguration(){
        Tomcat40DataObject tdo = getTomcatDataObject();
        boolean monitorEnabled = Tomcat40WebServer.getServer().isMonitorEnabled();
        boolean ideMode = tdo.isIdeMode();
        FileObject fo = tdo.getPrimaryFile();
        Boolean monitorFilter = (Boolean)fo.getAttribute(Tomcat40DataObject.MONITOR_FILTER_ATTRIBUTE);
        if (ideMode) {
            
            // add compilation stuff
            tdo.addCompilationJar();
            
            if (monitorEnabled) {
                if (!Boolean.TRUE.equals(monitorFilter)) {
                    // monitor filter need to be added
                    DDDataObject dd = tdo.getDD();
                    if (dd==null) return;
                    WebApp webApp = dd.getWebApp();
                    if (webApp==null) return;
                    if (tdo.changeFilterMonitor(webApp, true)) dd.setNodeDirty(true);
                }
                // also need to add the monitor jars
                // all this code should be redesigned, so the following line should only be consodered temporary
                tdo.addMonitorJars();
            }
            if (!monitorEnabled && !Boolean.FALSE.equals(monitorFilter)) {
                // monitor filter need to be deleted
                DDDataObject dd = tdo.getDD();
                if (dd==null) return;
                WebApp webApp = dd.getWebApp();
                if (webApp==null) return;
                if (tdo.changeFilterMonitor(webApp, false)) dd.setNodeDirty(true);
                tdo.removeAllMonitorValves();
            }
        }
        
        // pointbase stuff
        tdo.addPointbaseJar();
        
        DDDataObject dd = tdo.getDD();
        if (dd==null) return;
        WebApp webApp = dd.getWebApp();
        if (webApp==null) return;
        if (tdo.specifyIDEServletParameter(webApp)) {
            dd.setNodeDirty(true);
        }
        if (tdo.specifyFilterPortParameter(webApp)) {
            dd.setNodeDirty(true);
        }
    }
*/    
    
    private static void startModuleSpy (final ModuleSpy spy) {
        // trying to hang a listener on monitor module 
        res = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class));
        java.util.Iterator it = res.allInstances ().iterator ();
        final String moduleId = spy.getModuleId();        
        boolean found = false;
        while (it.hasNext ()) {
            org.openide.modules.ModuleInfo mi = (ModuleInfo)it.next ();
            if (mi.getCodeName ().startsWith(moduleId)) {
                httpMonitorInfo=mi;
                spy.setEnabled(mi.isEnabled());
                monitorInfoListener = new MonitorInfoListener(spy);
                httpMonitorInfo.addPropertyChangeListener(monitorInfoListener);
                found=true;
                break;
            }
        }
        // hanging a listener to the lookup result
        monitorLookupListener = new MonitorLookupListener(spy,httpMonitorInfo);
        res.addLookupListener(monitorLookupListener); 
    }
    
    private static class ModuleSpy {
        private boolean enabled;
        private String moduleId;
        
        public ModuleSpy (String moduleId) {
            this.moduleId=moduleId;
        }
        public void setModuleId(String moduleId){
            this.moduleId=moduleId;
        }
        public void setEnabled(boolean enabled){
            this.enabled=enabled;
        }
        public boolean isEnabled(){
            return enabled;
        }
        public String getModuleId(){
            return moduleId;
        }
    }
    
    static synchronized boolean isMonitorEnabled(){
        if (monitorSpy==null) {
            monitorSpy = new ModuleSpy(MONITOR_MODULE_NAME);
            startModuleSpy(monitorSpy);
        }
        return monitorSpy.isEnabled();
    }
    
    // PENDING - who should call this?
    void removeListeners() {
        if (httpMonitorInfo!=null) {
            httpMonitorInfo.removePropertyChangeListener(monitorInfoListener);
        }
        if (res!=null) {
            res.removeLookupListener(monitorLookupListener);
        }
    }
    
    private static class MonitorInfoListener implements java.beans.PropertyChangeListener {
        ModuleSpy spy;
        MonitorInfoListener(ModuleSpy spy) {
            this.spy=spy;
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("enabled")){ // NOI18N
                spy.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
            }            
        }  
    }
    
    private static class MonitorLookupListener implements LookupListener {
        
        ModuleSpy spy;
        ModuleInfo httpMonitorInfo;
        MonitorLookupListener(ModuleSpy spy, ModuleInfo httpMonitorInfo) {
            this.spy=spy;
            this.httpMonitorInfo=httpMonitorInfo;
        }
        
        public void resultChanged(LookupEvent lookupEvent) {
            java.util.Iterator it = res.allInstances ().iterator ();
            boolean moduleFound=false;
            while (it.hasNext ()) {
                ModuleInfo mi = (ModuleInfo)it.next ();
                if (mi.getCodeName ().startsWith(spy.getModuleId())) {
                    spy.setEnabled(mi.isEnabled());
                    if (httpMonitorInfo==null) {
                        httpMonitorInfo=mi;                        
                        monitorInfoListener = new MonitorInfoListener(spy);
                        httpMonitorInfo.addPropertyChangeListener(monitorInfoListener);
                    }
                    moduleFound=true;
                    break;
                }
            }
            if (!moduleFound) {
                if (httpMonitorInfo!=null) {
                    httpMonitorInfo.removePropertyChangeListener(monitorInfoListener);
                    httpMonitorInfo=null;
                    spy.setEnabled(false);
                }
            }            
        }
        
    }
}
