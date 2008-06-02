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

package org.netbeans.modules.tomcat5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tomcat5.config.gen.Server;
import org.openide.filesystems.*;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.tomcat5.ide.StartTomcat;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.modules.tomcat5.progress.MultiProgressObjectWrapper;
import org.netbeans.modules.tomcat5.util.*;
import org.openide.util.NbBundle;


/** DeploymentManager that can deploy to 
 * Tomcat 5 using manager application.
 *
 * @author  Radim Kubacki
 */
public class TomcatManager implements DeploymentManager {
    
    public enum TomcatVersion {TOMCAT_50, TOMCAT_55, TOMCAT_60};
    
    private static final Logger LOGGER = Logger.getLogger(TomcatManager.class.getName());

    /** Enum value for get*Modules methods. */
    static final int ENUM_AVAILABLE = 0;
    
    /** Enum value for get*Modules methods. */
    static final int ENUM_RUNNING = 1;
    
    /** Enum value for get*Modules methods. */
    static final int ENUM_NONRUNNING = 2;
    
    public static final String PROP_BUNDLED_TOMCAT = "is_it_bundled_tomcat";       // NOI18N
    
    /** Manager state. */
    private boolean connected;
    
    /** uri of this DeploymentManager. */
    private String uri;
    
    private StartTomcat startTomcat;
    
    /** System process of the started Tomcat */
    private Process process;
    
    /** Easier access to some server.xml settings. */
    private TomcatManagerConfig tomcatManagerConfig;
    
    /** LogManager manages all context and shared context logs for this TomcatManager. */
    private LogManager logManager = new LogManager(this);
    
    private TomcatPlatformImpl tomcatPlatform;
    
    private TomcatProperties tp;
    
    private TomcatVersion tomcatVersion;
    
    private InstanceProperties ip;
    
    private boolean needsRestart;

    /** Creates an instance of connected TomcatManager
     * @param conn <CODE>true</CODE> to create connected manager
     * @param uri URI for DeploymentManager
     * @param uname username
     * @param passwd password
     */
    public TomcatManager(boolean conn, String uri, TomcatVersion tomcatVersion) 
    throws IllegalArgumentException {
        LOGGER.log(Level.FINE, "Creating connected TomcatManager uri=" + uri); //NOI18N
        this.connected = conn;
        this.tomcatVersion = tomcatVersion;
        this.uri = uri;
        ip = InstanceProperties.getInstanceProperties(getUri());
        if (ip != null) {
            tp = new TomcatProperties(this);
        }
    }

    public InstanceProperties getInstanceProperties() {
        return ip;
    }
    
    public boolean isBundledTomcat() {
        if (ip == null) {
            return false;
        }
        String val = ip.getProperty(PROP_BUNDLED_TOMCAT);
        return val != null ? Boolean.valueOf(val).booleanValue()
                           : false;
    }
    
    public TomcatProperties getTomcatProperties() {
        return tp;
    }
    
    public synchronized void setNeedsRestart(boolean needsRestart) {
        this.needsRestart = needsRestart;
    }
    
    public synchronized boolean getNeedsRestart() {
        return needsRestart;
    }

    /**
     * Returns true if the server is running.
     *
     * @param checkResponse should be checked whether is the server responding - is really up?
     * @return <code>true</code> if the server is running.
     */
    public boolean isRunning(boolean checkResponse) {
        return isRunning(tp.getRunningCheckTimeout(), checkResponse);
    }
    
    /**
     * Returns true if the server is running.
     * 
     * @param timeout for how long should we keep trying to detect the running state.
     * @param checkResponse should be checked whether is the server responding - is really up?
     * @return <code>true</code> if the server is running.
     */
    public boolean isRunning(int timeout, boolean checkResponse) {
        Process proc = getTomcatProcess();
        if (proc != null) {
            try {
                // process is stopped
                proc.exitValue();
                return false;
            } catch (IllegalThreadStateException e) {
                // process is running
                if (!checkResponse) {
                    return true;
                }
            }
        }
        if (checkResponse) {
            return Utils.pingTomcat(getServerPort(), timeout); // is tomcat responding?
        } else {
            return false; // cannot resolve the state
        }
    }
    
    /** Returns identifier of TomcatManager. This is not a real URI!
     * @return URI including home and base specification
     */
    public String getUri () {
        switch (tomcatVersion) {
            case TOMCAT_60: 
                return TomcatFactory.TOMCAT_URI_PREFIX_60 + uri;
            case TOMCAT_55: 
                return TomcatFactory.TOMCAT_URI_PREFIX_55 + uri;
            case TOMCAT_50: 
            default:
                return TomcatFactory.TOMCAT_URI_PREFIX_50 + uri;
        }
    }
    
    /** Returns URI of TomcatManager (manager application).
     * @return URI without home and base specification
     */
    public String getPlainUri () {
        return "http://" + tp.getHost() + ":" + getCurrentServerPort() + "/manager/"; //NOI18N
    }
    
    /** Returns URI of TomcatManager.
     * @return URI without home and base specification
     */
    public String getServerUri () {
        return "http://" + tp.getHost() + ":" + getCurrentServerPort(); //NOI18N
    }

    /**
     * Return path to catalina work directory, which is used to store generated 
     * sources and classes from JSPs.
     *
     * @return path to catalina work directory.
     */
    public String getCatalinaWork() {
        TomcatManagerConfig tmConfig = getTomcatManagerConfig();
        String engineName = tmConfig.getEngineElement().getAttributeValue("name"); //NOI18N
        String hostName = tmConfig.getHostElement().getAttributeValue("name"); //NOI18N
        StringBuffer catWork = new StringBuffer(tp.getCatalinaDir().toString());
        catWork.append("/work/").append(engineName).append("/").append(hostName); //NOI18N
        return catWork.toString(); 
    }
    
    /** Ensure that the catalina base folder is ready, generate it if empty. */
    public void ensureCatalinaBaseReady() {
        File baseDir = tp.getCatalinaBase();
        if (baseDir != null) {
            String[] files = baseDir.list();
            // if empty, copy all the needed files from the catalina home folder
            if (files == null || files.length == 0) {
                // TODO: display a progress dialog
                createBaseDir(baseDir, tp.getCatalinaHome());
                // check whether filesystem sees it
                if (FileUtil.toFileObject(baseDir) == null) {
                    // try to refresh parent file object
                    File parentDir = baseDir.getParentFile();
                    if (parentDir != null) {
                        FileObject parentFileObject = FileUtil.toFileObject(parentDir);
                        if (parentFileObject != null) {
                            parentFileObject.refresh();
                        }
                    }
                }
            }
        }
    }
    
    public StartTomcat getStartTomcat(){
        return startTomcat;
    }
    
    public void setStartTomcat (StartTomcat st){
        startTomcat = st;
    }
    
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it.
     * Doesn't matter whether the thread are suspended or not.
     */
    public boolean isDebugged() {
        
        ServerDebugInfo sdi = null;

        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();

        sdi = getStartTomcat().getDebugInfo(null);
        if (sdi == null) {
            LOGGER.log(Level.INFO, "DebuggerInfo cannot be found for: " + this.toString());
        }

        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
        
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it 
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public boolean isSuspended() {

        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        ServerDebugInfo sdi = getStartTomcat().getDebugInfo(null);
        if (sdi == null) {
            LOGGER.log(Level.INFO, "DebuggerInfo cannot be found for: " + this.toString());
        }

        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        String shmem = attCookie.getSharedMemoryName();
                        if (shmem == null) continue;
                        if (shmem.equalsIgnoreCase(sdi.getShmemName())) {
                            Object d = s.lookupFirst(null, JPDADebugger.class);
                            if (d != null) {
                                JPDADebugger jpda = (JPDADebugger)d;
                                if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        String host = attCookie.getHostName();
                        if (host == null) continue;
                        if (host.equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                Object d = s.lookupFirst(null, JPDADebugger.class);
                                if (d != null) {
                                    JPDADebugger jpda = (JPDADebugger)d;
                                    if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
    
    public boolean isTomcat60() {
        return tomcatVersion == TomcatVersion.TOMCAT_60;
    }
    
    public boolean isTomcat55() {
        return tomcatVersion == TomcatVersion.TOMCAT_55;
    }
    
    public boolean isTomcat50() {
        return tomcatVersion == TomcatVersion.TOMCAT_50;
    }
    
    /** Returns Tomcat lib folder: "lib" for  Tomcat 6.0 and "common/lib" for Tomcat 5.x */
    public String libFolder() {
        // Tomcat 5.x and 6.0 uses different lib folder
        return isTomcat60() ?  "lib" : "common/lib"; // NOI18N
    }
    
    public TomcatVersion getTomcatVersion() {
        return tomcatVersion;
    }

// --- DeploymentManager interface implementation ----------------------
    
    public DeploymentConfiguration createConfiguration (DeployableObject deplObj) 
    throws InvalidModuleException {
        throw new RuntimeException("This should never be called"); // NOI18N
    }
    
    public Locale getCurrentLocale () {
        return Locale.getDefault ();
    }
    
    public Locale getDefaultLocale () {
        return Locale.getDefault ();
    }
    
    public Locale[] getSupportedLocales () {
        return Locale.getAvailableLocales ();
    }
    
    public boolean isLocaleSupported (Locale locale) {
        if (locale == null) {
            return false;
        }
        
        Locale [] supLocales = getSupportedLocales ();
        for (int i =0; i<supLocales.length; i++) {
            if (locale.equals (supLocales[i])) {
                return true;
            }
        }
        return false;
    }
    
    public TargetModuleID[] getAvailableModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_AVAILABLE, moduleType, targetList);
    }
    
    public TargetModuleID[] getNonRunningModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_NONRUNNING, moduleType, targetList);
    }
    
    public TargetModuleID[] getRunningModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_RUNNING, moduleType, targetList);
    }    
    
    public Target[] getTargets () throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.getTargets called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return new TomcatTarget [] { 
            new TomcatTarget (uri, "Tomcat at "+uri, getServerUri ())
        };
    }
    
    public DConfigBeanVersionType getDConfigBeanVersion () {
        // PENDING 
        return null;
    }
    
    public void setDConfigBeanVersion (DConfigBeanVersionType version) 
    throws DConfigBeanVersionUnsupportedException {
        if (!DConfigBeanVersionType.V1_3_1.equals (version)) {
            throw new DConfigBeanVersionUnsupportedException ("unsupported version");
        }
    }
    
    public boolean isDConfigBeanVersionSupported (DConfigBeanVersionType version) {
        return DConfigBeanVersionType.V1_3_1.equals (version);
    }
    
    public boolean isRedeploySupported () {
        // XXX what this really means
        return false;
    }
    
    public ProgressObject redeploy (TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) 
    throws UnsupportedOperationException, IllegalStateException {
        // PENDING
        throw new UnsupportedOperationException ("TomcatManager.redeploy not supported yet.");
    }
    
    public ProgressObject redeploy (TargetModuleID[] tmID, File file, File file2) 
    throws UnsupportedOperationException, IllegalStateException {
        // PENDING
        throw new UnsupportedOperationException ("TomcatManager.redeploy not supported yet.");
    }
    
    public void release () {
    }
    
    public void setLocale (Locale locale) throws UnsupportedOperationException {
    }
    
    public ProgressObject start (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.start called on disconnected instance");   // NOI18N
        }
        if (tmID.length != 1 || !(tmID[0] instanceof TomcatModule)) {
            throw new IllegalStateException ("TomcatManager.start invalid TargetModuleID passed");   // NOI18N
        }
        
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.start ((TomcatModule)tmID[0]);
        return impl;
    }
    
    public ProgressObject stop (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.stop called on disconnected instance");   // NOI18N
        }
        if (tmID.length != 1 || !(tmID[0] instanceof TomcatModule)) {
            throw new IllegalStateException ("TomcatManager.stop invalid TargetModuleID passed");   // NOI18N
        }
        
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.stop ((TomcatModule)tmID[0]);
        return impl;
    }
    
    public ProgressObject undeploy (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.undeploy called on disconnected instance");   // NOI18N
        }
        
        if (tmID == null) {
            throw new NullPointerException("TomcatManager.undeploy the tmID argument must not be null."); // NOI18N
        }
        
        if (tmID.length == 0) {
            throw new IllegalArgumentException("TomcatManager.undeploy at least one TargetModuleID object must be passed."); // NOI18N
        }
        
        for (int i = 0; i < tmID.length; i++) {
            if (!(tmID[i] instanceof TomcatModule)) {
                throw new IllegalStateException ("TomcatManager.undeploy invalid TargetModuleID passed: " + tmID[i].getClass().getName());   // NOI18N
            }
        }
        
        TomcatManagerImpl[] tmImpls = new TomcatManagerImpl[tmID.length];
        for (int i = 0; i < tmID.length; i++) {
            tmImpls[i] = new TomcatManagerImpl (this);
        }
        // wrap all the progress objects into a single one
        ProgressObject po = new MultiProgressObjectWrapper(tmImpls);
        
        for (int i = 0; i < tmID.length; i++) {
            TomcatModule tm = (TomcatModule) tmID[i];
            // it should not be allowed to undeploy the /manager application
            if ("/manager".equals(tm.getPath())) { // NOI18N
                String msg = NbBundle.getMessage(TomcatModule.class, "MSG_CannotUndeployManager");
                throw new IllegalStateException(msg);
            }
            tmImpls[i].remove(tm);
        }
        return po;
    }
    
    /** Deploys web module using deploy command
     * @param targets Array containg one web module
     * @param is Web application stream
     * @param deplPlan Server specific data
     * @throws IllegalStateException when TomcatManager is disconnected
     * @return Object that reports about deployment progress
     */ 
    public ProgressObject distribute (Target[] targets, InputStream is, InputStream deplPlan) 
    throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.distribute called on disconnected instance");   // NOI18N
        }
        LOGGER.log(Level.FINE, "TomcatManager.distribute streams");
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.deploy (targets[0], is, deplPlan);
        return impl;
    }
    
    /** Deploys web module using install command
     * @param targets Array containg one web module
     * @param moduleArchive directory with web module or WAR file
     * @param deplPlan Server specific data
     * @throws IllegalStateException when TomcatManager is disconnected
     * @return Object that reports about deployment progress
     */    
    public ProgressObject distribute (Target[] targets, File moduleArchive, File deplPlan) 
    throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.distribute called on disconnected instance");   // NOI18N
        }
        LOGGER.log(Level.FINE, "TomcatManager.distribute archive=" + moduleArchive.getPath() + ", plan=" + deplPlan.getPath()); // NOI18N
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.install (targets[0], moduleArchive, deplPlan);
        return impl;
    }
    
    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }
    
// --- End of DeploymentManager interface implementation ----------------------
        
    /** Utility method that retrieve the list of J2EE application modules 
     * distributed to the identified targets.
     * @param state     One of available, running, non-running constants.
     * @param moduleType    Predefined designator for a J2EE module type.
     * @param targetList    A list of deployment Target designators.
     */
    private TargetModuleID[] modules (int state, ModuleType moduleType, Target[] targetList)
    throws TargetException, IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.modules called on disconnected instance");   // NOI18N
        }
        if (targetList.length != 1) {
            throw new TargetException ("TomcatManager.modules supports only one target");   // NOI18N
        }
        
        if (!ModuleType.WAR.equals (moduleType)) {
            return new TargetModuleID[0];
        }
        
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        return impl.list (targetList[0], state);
    }
    
    /** Connected / disconnected status.
     * @return <CODE>true</CODE> when connected.
     */
    public boolean isConnected () {
        return connected;
    }
    
    public String toString () {
        return "Tomcat manager ["+uri+", home "+tp.getCatalinaHome()+", base "+tp.getCatalinaBase()+(connected?"conneceted":"disconnected")+"]";    // NOI18N
    }
    
    public void setServerPort(int port) {
        ensureCatalinaBaseReady(); // generated the catalina base folder if empty
        if (TomcatInstallUtil.setServerPort(port, tp.getServerXml())) {
            tp.setServerPort(port);
        }
    }
    
    public void setShutdownPort(int port) {
        ensureCatalinaBaseReady(); // generated the catalina base folder if empty
        if (TomcatInstallUtil.setShutdownPort(port, tp.getServerXml())) {
            tp.setShutdownPort(port);
        }
    }
    
    /** If Tomcat is running, return the port it was started with. Please note that 
     * the value in the server.xml (returned by getServerPort()) may differ. */
    public int getCurrentServerPort() {
        if (startTomcat != null && isRunning(false)) {
            return startTomcat.getCurrentServerPort();
        } else {
            return getServerPort();
        }
    }

    /** Return server port defined in the server.xml file, this value may differ
     * from the actual port Tomcat is currently running on @see #getCurrentServerPort(). */
    public int getServerPort() {
        ensurePortsUptodate();
        return tp.getServerPort();
    }
    
    public int getShutdownPort() {
        ensurePortsUptodate();
        return tp.getShutdownPort();
    }
    
    private void ensurePortsUptodate() {
        File serverXml = tp.getServerXml();
        long timestamp = -1;
        if (serverXml.exists()) {
            timestamp = serverXml.lastModified();
            if (timestamp > tp.getTimestamp()) {
                try {
                    // for the bundled tomcat we cannot simply use the server.xml 
                    // file from the home folder, since we change the port numbers
                    // during base folder generation
                    if (isBundledTomcat() && !new File(tp.getCatalinaBase(), "conf/server.xml").exists()) { // NOI18N
                        tp.setTimestamp(timestamp);
                        tp.setServerPort(TomcatProperties.DEF_VALUE_BUNDLED_SERVER_PORT);
                        tp.setShutdownPort(TomcatProperties.DEF_VALUE_BUNDLED_SHUTDOWN_PORT);
                        return;
                    }
                    Server server = Server.createGraph(serverXml);
                    tp.setTimestamp(timestamp);
                    tp.setServerPort(Integer.parseInt(TomcatInstallUtil.getPort(server)));
                    tp.setShutdownPort(Integer.parseInt(TomcatInstallUtil.getShutdownPort(server)));
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, null, ioe);
                } catch (NumberFormatException nfe) {
                    LOGGER.log(Level.INFO, null, nfe);
                } catch (RuntimeException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        }
    }
    
    public Server getRoot() {        
        try {
            return Server.createGraph(tp.getServerXml());
        } catch (IOException e) {
            LOGGER.log(Level.FINE, null, e);
            return null;
        } catch (RuntimeException e) {
            LOGGER.log(Level.INFO, null, e);
            return null;
        }
    }
    
    /** Initializes base dir for use with Tomcat 5.0.x. 
     *  @param baseDir directory for base dir.
     *  @param homeDir directory to copy config files from.
     *  @return File with absolute path for created dir or <CODE>null</CODE> when ther is an error.
     */    
    public File createBaseDir(File baseDir, File homeDir) {
        File targetFolder;
        if (!baseDir.isAbsolute ()) {
            baseDir = new File(System.getProperty("netbeans.user")+System.getProperty("file.separator")+baseDir);
            targetFolder = new File(System.getProperty("netbeans.user"));

        } else {
            targetFolder = baseDir.getParentFile ();
        }
        
        try {
            
            if (targetFolder == null) {
                LOGGER.log(Level.INFO, "Cannot find parent folder for base dir " + baseDir.getPath());
                return null;
            }
            File baseDirFO = new File (targetFolder, baseDir.getName ());
            baseDirFO.mkdir ();
                        
            // create directories
            String [] subdirs = new String [] { 
                "conf",   // NOI18N
                "conf/Catalina",   // NOI18N
                "conf/Catalina/localhost",   // NOI18N
                "logs",   // NOI18N
                "work",   // NOI18N
                "temp",   // NOI18N
                "webapps" // NOI18N
            };
            for (int i = 0; i<subdirs.length; i++) {
                File dest = new File (baseDirFO, subdirs [i]);
                dest.mkdirs ();
            }
            // copy config files
            final String ADMIN_XML = "conf/Catalina/localhost/admin.xml";
            String [] files = new String [] { 
                "conf/catalina.policy",   // NOI18N
                "conf/catalina.properties",   // NOI18N
                "conf/logging.properties", // NOI18N
                "conf/server.xml",   // NOI18N
                "conf/tomcat-users.xml",   // NOI18N
                "conf/web.xml",   // NOI18N
                ADMIN_XML,   // NOI18N For bundled tomcat 5.0.x 
                "conf/Catalina/localhost/manager.xml",   // NOI18N
            };
            String [] patternFrom = new String [] { 
                null, 
                null, 
                null,
                null,
                "</tomcat-users>",   // NOI18N
                null,
                "docBase=\"../server/webapps/admin\"",    // NOI18N For bundled tomcat 5.0.x 
                isTomcat50() || isTomcat55() ? "docBase=\"../server/webapps/manager\"" : null,    // NOI18N
            };
            String passwd = null;
            if (isBundledTomcat()) {
                passwd = tp.getPassword();
                if ("ide_manager".equals(passwd)) { // NOI18N
                    // change the default password that comes from the bundled Tomcat module
                    passwd = TomcatInstallUtil.generatePassword(8);
                    tp.setPassword(passwd);
                }
            }
            String [] patternTo = new String [] { 
                null, 
                null, 
                null,
                null,
                passwd != null ? "<user username=\"ide\" password=\"" + passwd + "\" roles=\"manager,admin\"/>\n</tomcat-users>" : null,   // NOI18N
                null, 
                "docBase=\"${catalina.home}/server/webapps/admin\"",   // NOI18N For bundled tomcat 5.0.x
                isTomcat50() || isTomcat55() ? "docBase=\"${catalina.home}/server/webapps/manager\"" : null,   // NOI18N 
            };
            for (int i = 0; i < files.length; i++) {
                // get folder from, to, name and ext
                int slash = files[i].lastIndexOf ('/');
                String sfolder = files[i].substring (0, slash);
                File fromDir = new File (homeDir, sfolder); // NOI18N
                File toDir = new File (baseDir, sfolder); // NOI18N

                if (patternTo[i] == null) {
                    File fileToCopy = new File(homeDir, files[i]);
                    if (!fileToCopy.exists()) {
                        LOGGER.log(Level.INFO, "Cannot copy file " + fileToCopy.getAbsolutePath() + " to the Tomcat base dir, since it does not exist.");
                        continue;
                    }
                    FileInputStream is = new FileInputStream(fileToCopy);
                    FileOutputStream os = new FileOutputStream (new File (toDir, files[i].substring (slash+1)));
                    try {
                        final byte[] BUFFER = new byte[4096];
                        int len;

                        for (;;) {
                            len = is.read (BUFFER);
                            if (len == -1) break;
                            os.write (BUFFER, 0, len);
                        }
                    }
                    catch (IOException ioe) {
                        LOGGER.log(Level.INFO, null, ioe);
                    } finally {
                        try {
                            if (os != null)
                                os.close();
                        }
                        catch (IOException ioe) { } // ignored
                        try {
                            if (is != null)
                                is.close();
                        } catch (IOException ioe) { } // ignored
                    }
                } else {
                    // use patched version
                    if (!copyAndPatch (
                        new File (fromDir, files[i].substring (slash+1)), 
                        new File (toDir, files[i].substring (slash+1)), 
                        patternFrom[i],
                        patternTo[i]
                        )) {
                        if (!(ADMIN_XML.equals(files[i]) && !(new File (fromDir, files[i].substring (slash+1))).exists()) ){
                            LOGGER.log(Level.INFO, "Cannot create config file "+files[i]);
                        }
                    }
                }
            }
            // deploy the ROOT context, if exists
            if (new File(homeDir, "webapps/ROOT").exists()) { // NOI18N
                writeToFile(new File(baseDir, "conf/Catalina/localhost/ROOT.xml"), // NOI18N
                    "<Context path=\"\" docBase=\"${catalina.home}/webapps/ROOT\"/>\n"); // NOI18N
            }
            // since tomcat 6.0 the manager app lives in the webapps folder
            if (!isTomcat50() && !isTomcat55() && new File(homeDir, "webapps/manager").exists()) { // NOI18N
                writeToFile(new File(baseDir, "conf/Catalina/localhost/manager.xml"), // NOI18N
                     "<Context docBase=\"${catalina.home}/webapps/manager\" antiResourceLocking=\"false\" privileged=\"true\"/>\n"); // NOI18N
            }

            if (isBundledTomcat()) {
                // create a special directory for the HTTP Monitor libs under the CATALINA_BASE
                // directory, since the user might not have write access to CATALINA_HOME
                TomcatInstallUtil.patchCatalinaProperties(new File(baseDir, "conf/catalina.properties")); // NOI18N
                TomcatInstallUtil.createNBLibDirectory(baseDir);
            }
        } catch (java.io.IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
            return null;
        }
        if (isBundledTomcat()) {
            TomcatInstallUtil.patchBundledServerXml(new File(baseDir, "conf/server.xml")); // NOI18N
        }
        return baseDir;
    }
    
    /**
     * Create a file and fill it with the data.
     */
    private void writeToFile(File file, String data) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(data);
        } finally {
            if (bw != null) bw.close();
        }
    }
    
    /** Copies server.xml file and patches appBase="webapps" to
     * appBase="$CATALINA_HOME/webapps" during the copy.
     * @return success status.
     */
    private boolean copyAndPatch (File src, File dst, String from, String to) {
        java.io.Reader r = null;
        java.io.Writer out = null;
        if (!src.exists())
            return false;
        try {
            r = new BufferedReader (new InputStreamReader (new FileInputStream (src), "utf-8")); // NOI18N
            StringBuffer sb = new StringBuffer ();
            final char[] BUFFER = new char[4096];
            int len;

            for (;;) {
                len = r.read (BUFFER);
                if (len == -1) break;
                sb.append (BUFFER, 0, len);
            }
            int idx = sb.toString ().indexOf (from);
            if (idx >= 0) {
                sb.replace (idx, idx+from.length (), to);  // NOI18N
            }
            else {
                // Something unexpected
                LOGGER.log(Level.INFO, "Pattern " + from + " not found in " + src.getPath()); // NOI18N
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream (dst), "utf-8")); // NOI18N
            out.write (sb.toString ());
            
        } catch (java.io.IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
            return false;
        } finally {
            try { if (out != null) out.close (); } catch (java.io.IOException ioe) { // ignore this
            }
            try { if (r != null) r.close (); } catch (java.io.IOException ioe) { // ignore this 
            }
        }
        return true;
    }
    
    /**
     * Open a context log for the specified module, if specified module does not
     * have its own logger defined, open shared context log instead.
     *
     * @param module module its context log should be opened
     */
    public void openLog(TargetModuleID module) {
        TomcatModule tomcatModule = null;
        if (module instanceof TomcatModule) {
            tomcatModule = (TomcatModule)module;
        } else {
            try {
                TargetModuleID[] tomMod = getRunningModules(ModuleType.WAR, new Target[]{module.getTarget()});
                for (int i = 0; i < tomMod.length; i++) {
                    if (module.getModuleID().equals(tomMod[i].getModuleID())) {
                        tomcatModule = (TomcatModule)tomMod[i];
                        break;
                    }
                }
            } catch (TargetException te) {
                LOGGER.log(Level.INFO, null, te);
            }
        }
        if (tomcatModule != null && logManager.hasContextLogger(tomcatModule)) {
            logManager.openContextLog(tomcatModule);
        } else {
            logManager.openSharedContextLog();
        }
    }
    
    /**
     * Return <code>TomcatManagerConfig</code> for easier access to some server.xml
     * settings.
     *
     * @return <code>TomcatManagerConfig</code> for easier access to some server.xml
     *         settings.
     */
    public synchronized TomcatManagerConfig getTomcatManagerConfig() {
        if (tomcatManagerConfig == null) {
            tomcatManagerConfig = new TomcatManagerConfig(tp.getServerXml());
        }
        return tomcatManagerConfig;
    }
    
    /**
     * Return <code>LogManager</code> which manages all context and shared context
     * logs for this <code>TomcatManager</code>.
     *
     * @return <code>LogManager</code> which manages all context and shared context
     *         logs for this <code>TomcatManager</code>.
     */
    public LogManager logManager() {
        return logManager;
    }
    
    /**
     * Set the <code>Process</code> of the started Tomcat.
     *
     * @param <code>Process</code> of the started Tomcat.
     */
    public synchronized void setTomcatProcess(Process p) {
        process = p;
    }

    /**
     * Return <code>Process</code> of the started Tomcat.
     *
     * @return <code>Process</code> of the started Tomcat, <code>null</code> if
     *         Tomcat wasn't started by IDE.
     */
    public synchronized Process getTomcatProcess() {
        return process;
    }
    
    /** Terminates the running Tomcat process. */
    public void terminate() {
        Process proc = getTomcatProcess();
        if (proc != null) {
            proc.destroy();
        }
    }
    
    public synchronized TomcatPlatformImpl getTomcatPlatform() {
        if (tomcatPlatform == null) {
            tomcatPlatform = new TomcatPlatformImpl(this);
        }
        return tomcatPlatform;
    }

}
