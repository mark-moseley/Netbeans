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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.ruby.railsprojects.server;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionException;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Support for the builtin Ruby on Rails web server: WEBrick, Mongrel, Lighttpd
 *
 * This is really primitive at this point; I should talk to the people who
 * write Java web server plugins and take some pointers. Perhaps it can
 * even implement some of their APIs such that logging, runtime nodes etc.
 * all begin to work.
 * 
 * @todo When launching under JRuby, also pass in -Djruby.thread.pooling=true to the VM
 * @todo Rewrite the WEBrick error message which says to press Ctrl-C to cancel the process;
 *   tell the user to use the Stop button in the margin instead (somebody on nbusers asked about this)
 * @todo Normalize & merge RubyServer and RubyInstance interfaces and their
 *   various implementations (V3, WEBrick, Mongrel).
 * 
 * @author Tor Norbye, Pavel Buzek, Erno Mononen, Peter Williams
 */
public final class RailsServerManager {
     /**
      * A hidden flag to turn off automatic browser display on server startup.
      * Should probably be exposed as a user visible option somewhere.
      */
    private static boolean NO_BROWSER = Boolean.getBoolean("rails.nobrowser");

   
    enum ServerStatus { NOT_STARTED, STARTING, RUNNING; }

    private static final Logger LOGGER = Logger.getLogger(RailsServerManager.class.getName());
    
    /** Set of currently active - in use; ports. */
    private static final Set<Integer> IN_USE_PORTS = new HashSet<Integer>();;

    /**
     * The timeout in seconds for waiting a server to start.
     */
    private static final int SERVER_STARTUP_TIMEOUT = 120; 
    
    private ServerStatus status = ServerStatus.NOT_STARTED;
    private RubyServer server;
    private RubyInstance instance;
    
    /** True if server failed to start due to port conflict. */
    private boolean portConflict;
    
    /** User chosen port */
    private int originalPort;
    
    /** Actual port in use (trying other ports for ones not in use) */
    private int port = -1;
    
    private RailsProject project;
    private RubyExecution execution;
    private File dir;
    private String projectName;
    private boolean debug;
    private boolean clientDebug;
    private boolean switchToDebugMode;
    private Semaphore debugSemaphore;
    
    public RailsServerManager(RailsProject project) {
        this.project = project;
        dir = FileUtil.toFile(project.getProjectDirectory());
    }
    
    public synchronized void setDebug(boolean debug) {
        if (status == ServerStatus.RUNNING && !this.debug && debug) {
            switchToDebugMode = true;
        }
        this.debug = debug;
    }
    
    public void setClientDebug(boolean clientDebug) {
        this.clientDebug = clientDebug;
    }

    /**
     * @return true if server is ready and application can be run immediately,
     *   otherwise return false indicating server is becoming ready asynchonously.
     */
    private boolean ensureRunning() {
        synchronized (RailsServerManager.this) {
            if(projectName == null) {
                projectName = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
            }
            if (status == ServerStatus.STARTING) {
                return false;
            } else if (status == ServerStatus.RUNNING) {
                if (switchToDebugMode) {
                    if (!isPluginServer(instance)) {
                        assert debugSemaphore == null : "startSemaphor supposed to be null";
                        debugSemaphore = new Semaphore(0);
                    } else {
//                        instance.stopServer();
                    }
                    switchToDebugMode = false;
                } else if (isPortInUse(port)) {
                    if (!debug && isPluginServer(instance)) {
                        if(port == instance.getRailsPort()) {
                            status = ServerStatus.STARTING;
                            final Future<RubyInstance.OperationState> result =
                                    instance.deploy(projectName, dir);

                            final RubyInstance serverInstance = instance;
                            RequestProcessor.getDefault().post(new Runnable() {
                                public void run() {
                                    try {
                                        RubyInstance.OperationState state = result.get(120, TimeUnit.SECONDS);
                                        if(state == RubyInstance.OperationState.COMPLETED) {
                                            synchronized(RailsServerManager.this) {
                                                port = serverInstance.getRailsPort();
                                                status = ServerStatus.RUNNING;
                                                if (LOGGER.isLoggable(Level.FINEST)) {
                                                    LOGGER.finest("RSM.ensureRunning(): (V3) status = " +
                                                            status + ", port = " + port);
                                                }
                                            }
                                        } else {
                                            synchronized(RailsServerManager.this) {
                                                status = ServerStatus.NOT_STARTED;
                                            }
                                            instance.stopServer();
                                        }
                                    } catch (Exception ex) {
                                        LOGGER.log(Level.INFO, ex.getMessage(), ex);

                                        // Ensure status value is reset on exceptions too...
                                        synchronized(RailsServerManager.this) {
                                            status = ServerStatus.NOT_STARTED;
                                        }
                                        instance.stopServer();
                                    }
                                }
                            });
                            return false;
                        } else {
//                            if(execution != null) {
//                                assert debugSemaphore == null : "startSemaphor supposed to be null";
//                                debugSemaphore = new Semaphore(0);
//                            }
                        }
                    } else {
                        // Simply assume it is still the same server running
                        return true;
                    }
                }
            }
        }
        if (debugSemaphore != null) {
            try {
                if(execution != null) {
                    execution.kill();
                }
                debugSemaphore.acquire();
                debugSemaphore = null;
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // Start the server
        synchronized (RailsServerManager.this) {
            status = ServerStatus.STARTING;
        }

        projectName = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
        String classPath = project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH);
        String serverId = project.evaluator().getProperty(RailsProjectProperties.RAILS_SERVERTYPE);
        RubyPlatform platform = RubyPlatform.platformFor(project);
        RubyInstance candidateInstance = ServerRegistry.getDefault().getServer(serverId, platform);
        if (candidateInstance == null) {
            // TODO: need to inform the user somehow
            // fall back to the first available server
            List<? extends RubyInstance> availableServers = ServerRegistry.getDefault().getServers();
            for (RubyInstance each : availableServers) {
                if (each.isPlatformSupported(platform)) {
                    candidateInstance = each;
                    break;
                }
            }
            assert candidateInstance != null : "No servers found for " + platform;
        }
        
        instance = candidateInstance;

        if (isPluginServer(instance)) {
            if(!debug) {
                final Future<RubyInstance.OperationState> result = 
                        instance.runApplication(platform, projectName, dir);

                final RubyInstance serverInstance = instance;
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            RubyInstance.OperationState state = result.get(120, TimeUnit.SECONDS);
                            if(state == RubyInstance.OperationState.COMPLETED) {
                                synchronized(RailsServerManager.this) {
                                    port = serverInstance.getRailsPort();
                                    status = ServerStatus.RUNNING;
                                }
                            } else {
                                synchronized(RailsServerManager.this) {
                                    status = ServerStatus.NOT_STARTED;
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.INFO, ex.getMessage(), ex);

                            // Ensure status value is reset on exceptions too...
                            synchronized(RailsServerManager.this) {
                                status = ServerStatus.NOT_STARTED;
                            }
                        }
                    }
                });

                return false;
            } else {
                ensurePortAvailable();
                String displayName = NbBundle.getMessage(RailsServerManager.class,
                        "LBL_ServerTab" , instance.getDisplayName(), projectName, Integer.toString(port)); // NOI18N
                ExecutionDescriptor desc = new ExecutionDescriptor(platform, displayName, dir, "unknown"); // NOI18N
                desc.cmd(getJavaExecutable());
                desc.useInterpreter(false);
                desc.initialArgs(instance.getServerCommand(platform, classPath, dir, port, debug));
                desc.postBuild(getFinishAction());
                desc.addStandardRecognizers();
                desc.addOutputRecognizer(new GrizzlyServerRecognizer(instance));
                desc.frontWindow(false);
                desc.debug(debug);
                desc.fastDebugRequired(debug);
                desc.fileLocator(new DirectoryFileLocator(FileUtil.toFileObject(dir)));
                desc.showSuspended(true);
                
                String charsetName = project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING);
                IN_USE_PORTS.add(port);
                execution = new RubyExecution(desc, charsetName);
                execution.run();
                return false;
            }
        }

        // check whether the user has modified script/server to use another server
        RubyInstance explicitlySpecified = ServerResolver.getExplicitlySpecifiedServer(project);
        if (explicitlySpecified instanceof RubyServer) {
            server = (RubyServer) explicitlySpecified;
            instance = explicitlySpecified;
        } else {
            server = (RubyServer) instance;
        }

        ensurePortAvailable();
        String displayName = getServerTabName(server, projectName, port);
        String serverPath = server.getServerPath();
        ExecutionDescriptor desc = new ExecutionDescriptor(platform, displayName, dir, serverPath);
// can place debug flags here to allow attaching NB debugger to jruby process
// running server that is started in debug-commons.
//        if(debug && "true".equals(System.getProperty("rdebug.enable.debug"))) {
//            desc.initialArgs("-J-Xdebug -J-Xrunjdwp:transport=dt_socket,address=3105,server=y,suspend=y");
//        }
        // Paths required for GlassFish gem.  Not used or required for WEBrick or Mongrel.
        String gemPath = server.getLocation();
        if(gemPath != null) {
            desc.initialArgs("-I \"" + gemPath + File.separatorChar + "bin\" " +
                    "-I \"" + gemPath + File.separatorChar + "lib\"");
        }
        desc.scriptPrefix(server.getScriptPrefix());
        desc.additionalArgs(buildStartupArgs());
        desc.postBuild(getFinishAction());
        desc.classPath(classPath);
        desc.addStandardRecognizers();
        desc.addOutputRecognizer(new RailsServerRecognizer(server));
        desc.frontWindow(false);
        desc.debug(debug);
        desc.fastDebugRequired(debug);
        desc.fileLocator(new DirectoryFileLocator(FileUtil.toFileObject(dir)));
        //desc.showProgress(false); // http://ruby.netbeans.org/issues/show_bug.cgi?id=109261
        desc.showSuspended(true);
        String charsetName = project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING);
        IN_USE_PORTS.add(port);
        execution = new RubyExecution(desc, charsetName);
        execution.run();
        return false;
    }
    
    private String[] buildStartupArgs() {
        List<String> result = new  ArrayList<String>();
        if (server.getStartupParam() != null) {
            result.add(server.getStartupParam());
        } 
        String railsEnv = project.evaluator().getProperty(RailsProjectProperties.RAILS_ENV);
        if (railsEnv != null && !"".equals(railsEnv.trim())) {
            result.add("-e");
            result.add(railsEnv);
        }
        if(server instanceof GlassFishGem) {
            result.add(dir.getAbsolutePath());
        } else {
            result.add("--port");
            result.add(Integer.toString(port));
        }
        return result.toArray(new String[result.size()]);
    }
    
    private void ensurePortAvailable() {
        portConflict = false;
        String portString = project.evaluator().getProperty(RailsProjectProperties.RAILS_PORT);
        port = 0;
        if (portString != null) {
            port = Integer.parseInt(portString);
        }
        if (port == 0) {
            port = 3000;
        }
        originalPort = port;

        while(isPortInUse(port)) {
            port++;
        }
    }
    
    private Runnable getFinishAction() {
        return new Runnable() {
            public void run() {
                synchronized (RailsServerManager.this) {
                    status = ServerStatus.NOT_STARTED;
                    if (server != null) {
                        server.removeApplication(port);
                    }
                    IN_USE_PORTS.remove(port);
                    if (portConflict) {
                        // Failed to start due to port conflict - notify user.
                        notifyPortConflict();
                    }
                    if (debugSemaphore != null) {
                        debugSemaphore.release();
                    } else {
                        debug = false;
                    }
                }
            }
        };
    }
    
    private File getJavaExecutable() {
        String javaPath = System.getProperty("java.home") + File.separatorChar +
                "bin" + File.separatorChar + (Utilities.isWindows() ? "java.exe" : "java");
        File javaExe = new File(javaPath);
        if(!javaExe.exists()) {
            LOGGER.log(Level.SEVERE, "Unable to locate java executable: " + javaPath);
        }
        return javaExe;
    }

    /**
     * Hack to determine if the selected server instance is a managed server
     * provided a by a plugin (e.g. GlassFish V3) or one of the Ruby Servers
     * loaded from the gem repository.
     *
     * This should be removed when we merge/normalize RubyServer and RubyInstance
     * interfaces.
     */
    private static boolean isPluginServer(RubyInstance instance) {
        return instance != null && !(instance instanceof RubyServer);
    }
    
    private static String getServerTabName(RubyServer server, String projectName, int port) {
        return NbBundle.getMessage(RailsServerManager.class, 
                "LBL_ServerTab" , server.getDisplayName(), projectName, String.valueOf(port));
    }
    
    private void notifyPortConflict() {
        String message = NbBundle.getMessage(RailsServerManager.class, "Conflict", Integer.toString(originalPort));
        NotifyDescriptor nd =
            new NotifyDescriptor.Message(message, 
            NotifyDescriptor.Message.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }
    
    private String getContextRoot() {
        if(!debug && instance != null) {
            return instance.getContextRoot(projectName);
        }
        return "";
    }

    /**
     * Starts the server if not running and shows url.
     * @param relativeUrl the resulting url will be for example: http://localhost:{port}/{relativeUrl}
     */
    public void showUrl(final String relativeUrl) {
        if (ensureRunning()) {
            RailsServerManager.showURL(getContextRoot(), relativeUrl, port, clientDebug, project);
        } else {
            String displayName = NbBundle.getMessage(RailsServerManager.class, "ServerStartup");
            final ProgressHandle handle =
                ProgressHandleFactory.createHandle(displayName,new Cancellable() {
                        public boolean cancel() {
                            return true;
                        }
                    },
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            // XXX ?
                        }
                    });

            handle.start();
            handle.switchToIndeterminate();

            final boolean runClientDebug = clientDebug;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        // Try connecting repeatedly, up to time specified 
                        // by SERVER_STARTUP_TIMEOUT, then bail
                        int i = 0;
                        for (; i <= SERVER_STARTUP_TIMEOUT; i++) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                // Don't worry about it
                            }

                            synchronized (RailsServerManager.this) {
                                if (status == ServerStatus.RUNNING) {
                                    LOGGER.fine("Server " + ((server != null) ? server : instance) +
                                            " started in " + i + " seconds.");
                                    RailsServerManager.showURL(getContextRoot(), relativeUrl, port, runClientDebug, project);
                                    return;
                                }

                                if (status == ServerStatus.NOT_STARTED) {
                                    LOGGER.fine("Server starup failed, server type is: " +
                                           ((server != null) ? server : instance));
                                    // Server startup somehow failed...
                                    break;
                                }
                            }
                        }

                        LOGGER.fine("Could not start " + server + " in " + i +
                                " seconds, current server status is " + status);

                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(RailsServerManager.class,
                                    "NoServerFound", "http://localhost:" + port + "/" + relativeUrl));
                    } finally {
                        handle.finish();
                    }
                }
            });
        }
    }

    /** Return true if there is an HTTP response from the port on localhost.
     * Based on tomcatint\tomcat5\src\org.netbeans.modules.tomcat5.util.Utils.java.
     */
    private static boolean useHttpValidation = Boolean.parseBoolean(
            System.getProperty("rails.server.http.validation"));

    public static boolean isPortInUse(int port) {
        if (IN_USE_PORTS.contains(port)) {
            return true;
        }
        int timeout = 3000;
        Socket socket = new Socket();
        try {
            try {
                socket.connect(new InetSocketAddress("localhost", port), timeout); // NOI18N
                if(useHttpValidation) {
                    socket.setSoTimeout(timeout);
                    OutputStream out = socket.getOutputStream();
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        try {
                            // request -- mongrel requires \r\n instead of just \n
                            out.write("GET / HTTP/1.0\r\n\r\n".getBytes("UTF8")); // NOI18N
                            out.flush();

                            // response
                            String text = in.readLine();
                            if (text != null && text.startsWith("HTTP")) { // NOI18N
                                return true; // http response.
                            }
                            return false;
                        } finally {
                            in.close();
                        }
                    } finally {
                        out.close();
                    }
                } else {
                    return true;
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            return false;
        }
    }

    private static void showURL(String contextRoot, String relativeUrl, int port, boolean runClientDebugger, RailsProject project) {
        if (NO_BROWSER) {
            return;
        }

        LOGGER.fine("Opening URL: " + "http://localhost:" + port + "/" + relativeUrl);
        try {
            URL url = new URL("http://localhost:" + port + contextRoot + "/" + relativeUrl); // NOI18N
            
            if (!runClientDebugger) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            } else {
                // launch browser with clientside debugger
                FileObject projectDocBase = project.getRakeProjectHelper().resolveFileObject("public"); // NOI18N
                String hostPrefix = "http://localhost:" + port + "/"; // NOI18N
                
                HtmlBrowser.Factory browser = null;
                if (WebClientToolsProjectUtils.isInternetExplorer(project)) {
                    browser = WebClientToolsProjectUtils.getInternetExplorerBrowser();
                } else {
                    browser = WebClientToolsProjectUtils.getFirefoxBrowser();
                }
                
                if (browser == null) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                    return;
                }
                                 
                LocationMappersFactory mapperFactory = Lookup.getDefault().lookup(LocationMappersFactory.class);

                Lookup debuggerLookup = null;
                if (mapperFactory != null) {
                    URI appContext = new URI(hostPrefix);

                    // If the public/index.html file exists assume that it is the welcome file.
                    Map<String, Object> extendedInfo = null;
                    FileObject welcomeFile = projectDocBase.getFileObject("index.html");  //NOI18N
                    if (welcomeFile != null) {
                        extendedInfo = new HashMap<String, Object>();
                        extendedInfo.put("welcome-file", "index.html"); //NOI18N
                    }
                    
                    JSToNbJSLocationMapper forwardMapper =
                            mapperFactory.getJSToNbJSLocationMapper(projectDocBase, appContext, extendedInfo);
                    NbJSToJSLocationMapper reverseMapper =
                            mapperFactory.getNbJSToJSLocationMapper(projectDocBase, appContext, extendedInfo);
                    debuggerLookup = Lookups.fixed(forwardMapper, reverseMapper, project);
                } else {
                    debuggerLookup = Lookups.fixed(project);
                }
                
                WebClientToolsSessionStarterService.startSession(url.toURI(), browser, debuggerLookup);
            }
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (URISyntaxException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (WebClientToolsSessionException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /**
     * @param outputLine the output line to check.
     * @return true if the given <code>outputLine</code> represented 'address in use'
     * message.
     */
    static boolean isAddressInUseMsg(String outputLine){
        return outputLine.matches(".*in.*: Address.+in use.+(Errno::EADDRINUSE).*"); //NOI18N
    }
    
    private class RailsServerRecognizer extends OutputRecognizer {

        private final RubyServer server;
        
        RailsServerRecognizer(RubyServer server) {
            this.server = server;
        }

        @Override
        public ActionText processLine(String outputLine) {
            
            if (LOGGER.isLoggable(Level.FINEST)){
                LOGGER.log(Level.FINEST, "Processing output line: " + outputLine);
            }

            String line = outputLine;
            
            // This is ugly, but my attempts to use URLConnection on the URL repeatedly
            // and check for connection.getResponseCode()==HttpURLConnection.HTTP_OK didn't
            // work - try that again later
            if (server.isStartupMsg(outputLine)) {
                synchronized (RailsServerManager.this) {
                    LOGGER.fine("Identified " + server + " as running");
                    status = ServerStatus.RUNNING;
                    String projectName = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
                    server.addApplication(new RailsApplication(projectName, port, execution));
                }
            } else if (isAddressInUseMsg(outputLine)) {
                LOGGER.fine("Detected port conflict: " + outputLine);
                portConflict = true;
            }

            if (!line.equals(outputLine)) {
                return new ActionText(new String[] { line }, null, null, null);
            }

            return null;
        }
    }

    private class GrizzlyServerRecognizer extends OutputRecognizer {

        private RubyInstance server;

        GrizzlyServerRecognizer(RubyInstance server) {
            this.server = server;
        }

        @Override
        public ActionText processLine(String outputLine) {
            
            if (LOGGER.isLoggable(Level.FINEST)){
                LOGGER.log(Level.FINEST, "Processing output line: " + outputLine);
            }

            String line = outputLine;
            
            if (isStartupMsg(outputLine)) {
                synchronized (RailsServerManager.this) {
                    LOGGER.fine("Identified " + server + " as running");
                    status = ServerStatus.RUNNING;
//                    String projectName = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
//                    server.addApplication(new RailsApplication(projectName, port, execution));
                }
            } else if (isAddressInUseMsg(outputLine)) {
                LOGGER.fine("Detected port conflict: " + outputLine);
                portConflict = true;
            }

            if (!line.equals(outputLine)) {
                return new ActionText(new String[] { line }, null, null, null);
            }

            return null;
        }

        private boolean isStartupMsg(String line) {
            return line.contains("Grizzly configuration for port");
        }

        private boolean isAddressInUseMsg(String line) {
            return line.contains("BindException");
        }
    }

    public static JComboBox getServerComboBox(RubyPlatform platform) {
        JComboBox result = new JComboBox();
        if (platform != null) {
            result.setModel(new ServerListModel(platform));
        }
        result.setRenderer(new ServerListCellRendered());
        return result;
    }

    public static class ServerListModel extends AbstractListModel implements ComboBoxModel {

        private final List<? extends RubyInstance> servers;
        private Object selected;

        public ServerListModel(RubyPlatform platform) {
            this.servers = ServerRegistry.getDefault().getServers(platform);
            this.selected = servers.get(0);
        }

        public int getSize() {
            return servers.size();
        }

        public Object getElementAt(int index) {
            return servers.get(index);
        }

        public void setSelectedItem(Object server) {
            if (selected != server) {
                this.selected = server;
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            return selected;
        }
        
    }

    private static class ServerListCellRendered extends JLabel implements ListCellRenderer {

        public ServerListCellRendered() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            RubyInstance server = (RubyInstance) value;
            if (server != null) {
                setText(server.getDisplayName());
                setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            }
            return this;
        }
    }

}
