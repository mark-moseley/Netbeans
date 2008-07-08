// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
//</editor-fold>

package org.netbeans.modules.glassfish.eecommon.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;

import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.xml.sax.SAXException;

import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;

public class HttpMonitorHelper {

    // monitor module enable status data
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
    
    public static boolean synchronizeMonitor(String domainLoc, String domainName, boolean monitorFlag) throws IOException, SAXException {
        boolean monitorModuleAvailable = isMonitorEnabled();
        boolean shouldInstall = monitorModuleAvailable && monitorFlag;
        // find the web.xml file
        File webXML = getDefaultWebXML(domainLoc, domainName);
        if (webXML == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(""));
            return false;
        }
        WebApp webApp = DDProvider.getDefault().getDDRoot(webXML);
        if (webApp == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(""));
            return false;
        }
        boolean needsSave = false;
        boolean result;
        try {
            if (shouldInstall) {
                addMonitorJars(domainLoc,domainName);
                result = changeFilterMonitor(webApp, true);
                needsSave = needsSave || result;
                result = specifyFilterPortParameter(webApp);
                needsSave = needsSave || result;
                
            } else {
                result = changeFilterMonitor(webApp, false);
                needsSave = needsSave || result;
            }
        } catch (ClassNotFoundException cnfe) {
            needsSave = false;
            ErrorManager.getDefault().notify(ErrorManager.ERROR, cnfe);
        }
        if (needsSave) {
            OutputStream os = new FileOutputStream(webXML);
            try {
                webApp.write(os);
            } finally {
                os.close();
            }
        }
        return needsSave;
    }
    
    private static File getDefaultWebXML(String domainLoc, String domainName) {
        String loc = domainLoc+"/"+domainName+"/config/default-web.xml"; // NOI18N
        File webXML = new File(loc);
        if (webXML.exists()) {
            return webXML;
        }
        return null;
    }
    
    private static void addMonitorJars(String domainLoc, String domainName) throws IOException {
        String loc = domainLoc+"/"+domainName;
        File instDir = new File(loc);
        copyFromIDEInstToDir("modules/ext/org-netbeans-modules-web-httpmonitor.jar"  , instDir, "lib/org-netbeans-modules-web-httpmonitor.jar");  // NOI18N        
    }
    
    private static boolean changeFilterMonitor(WebApp webApp,boolean full) throws ClassNotFoundException {
        boolean filterWasChanged=false;
        if (full) { // adding monitor filter/filter-mapping element
            boolean isFilter=false;
            Filter[] filters = webApp.getFilter();
            for(int i=0;i<filters.length;i++) {
                if (filters[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    isFilter=true;
                    break;
                }
            }
            if (!isFilter) {
                    Filter filter = (Filter)webApp.createBean("Filter"); //NOI18N
                    filter.setFilterName(MONITOR_FILTER_NAME);
                    filter.setFilterClass(MONITOR_FILTER_CLASS);
                    webApp.addFilter(filter);
                    filterWasChanged=true;
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
                    FilterMapping filterMapping = (FilterMapping)webApp.createBean("FilterMapping"); //NOI18N
                    
                    // setting the dispatcher values even for Servlet2.3 web.xml
                    String[] dispatcher = new String[] {"REQUEST","FORWARD","INCLUDE","ERROR"}; //NOI18N
                    try {
                        filterMapping.setDispatcher(dispatcher);
                    } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
                        Logger.getLogger("glassfish-eecommon").log(Level.FINER,"ignorable and ignoring",ex);
                        ((BaseBean)filterMapping).createProperty("dispatcher", // NOI18N
                            "Dispatcher", // NOI18N
                            Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
                            java.lang.String.class);
                        ((BaseBean)filterMapping).setValue("Dispatcher",dispatcher); // NOI18N
                    }
                    
                    filterMapping.setFilterName(MONITOR_FILTER_NAME);
                    filterMapping.setUrlPattern(MONITOR_FILTER_PATTERN);
                    webApp.addFilterMapping(filterMapping);
                    filterWasChanged=true;
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
        File sourceFile = findInstallationFile(sourceRelPath);
        if (sourceFile != null && sourceFile.exists()) {
            if (!targetFile.exists() 
                || sourceFile.length() != targetFile.length()) {
                copy(sourceFile,targetFile);
            }
        }
    }
    
    private static void copy(File file1, File file2) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
             bis = new BufferedInputStream(new FileInputStream(file1));
             bos = new BufferedOutputStream(new FileOutputStream(file2));
            int b;
            while((b=bis.read())!=-1)bos.write(b);
        } finally {
            if (null != bis) {
                try { 
                    bis.close(); 
                } catch (IOException ioe) {
                    Logger.getLogger("glassfish-eecommon").log(Level.FINEST,"bis", ioe);
                }
            }
            if (null != bos) {
                try { 
                    bos.close(); 
                } catch (IOException ioe) {
                    Logger.getLogger("glassfish-eecommon").log(Level.FINEST,"bos", ioe);
                }
            }
        }
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
    private static boolean specifyFilterPortParameter(WebApp webApp) throws ClassNotFoundException {
        Filter[] filters = webApp.getFilter();
        Filter myFilter = null;
        for(int i=0; i<filters.length; i++) {
            if (MONITOR_FILTER_NAME.equals(filters[i].getFilterName())) {
                myFilter = filters[i];
                break;
            }
        }
        // see if we found it
        if (myFilter == null) {
            return false;
        }
        
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
                InitParam init = (InitParam)myFilter.createBean("InitParam"); //NOI18N
                init.setParamName(MONITOR_INTERNALPORT_PARAM_NAME);
                init.setParamValue(correctParamValue);
                myFilter.addInitParam(init);
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
        // just return 127.0.0.1, other values don't seem to work reliably
        return "127.0.0.1"; // NOI18N
        /**
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "127.0.0.1"; // NOI18N
        }
        */
    }
    
    private static URL getSampleHTTPServerURL() {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
	    FileObject fo = fs.findResource("HTTPServer_DUMMY");
	    if (fo == null) {
	        return null;
	    }
	    URL u = URLMapper.findURL(fo, URLMapper.NETWORK);
	    return u;
    }

    private static String getInternalServerPort() {
        //URL u = HttpServer.getRepositoryRoot();
        URL u = getSampleHTTPServerURL();
        if (u != null) {
            return Integer.toString(u.getPort()); // NOI18N
        }
        else {
            return "8082"; // NOI18N
        }
    }
    
    
    private static void startModuleSpy (final ModuleSpy spy) {
        // trying to hang a listener on monitor module 
        res = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class));
        java.util.Iterator it = res.allInstances ().iterator ();
        final String moduleId = spy.getModuleId();        
       // boolean found = false;
        while (it.hasNext ()) {
            org.openide.modules.ModuleInfo mi = (ModuleInfo)it.next ();
            if (mi.getCodeName ().startsWith(moduleId)) {
                httpMonitorInfo=mi;
                spy.setEnabled(mi.isEnabled());
                monitorInfoListener = new MonitorInfoListener(spy);
                httpMonitorInfo.addPropertyChangeListener(monitorInfoListener);
               // found=true;
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
