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

package org.netbeans.modules.tomcat5;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.tomcat5.config.Context;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/** Implemtation of management task that provides info about progress
 *
 * @author  Radim Kubacki
 */
public class TomcatManagerImpl implements ProgressObject, Runnable {
    
    /** RequestProcessor processor that serializes management tasks. */
    private static RequestProcessor rp;
    
    /** Returns shared RequestProcessor. */
    private static synchronized RequestProcessor rp () {
        if (rp == null) {
            rp = new RequestProcessor ("Tomcat management", 1); // NOI18N
        }
        return rp;
    }

    /** Support for progress notifications. */
    private ProgressEventSupport pes;
    
    /** Command that is executed on running server. */
    private String command;
    
    /** Output of executed command (parsed for list commands). */
    private String output;
    
    /** Command type used for events. */
    private CommandType cmdType;
    
    /** InputStream of application data. */
    private InputStream istream;
    
    private TomcatManager tm;
    
    /** TargetModuleID of module that is managed. */
    private TomcatModule tmId;

    public TomcatManagerImpl (TomcatManager tm) {
        this.tm = tm;
        pes = new ProgressEventSupport (this);
    }

    public void deploy (Target t, InputStream is, InputStream deplPlan) {
        Context ctx = Context.createGraph (deplPlan);
        String ctxPath = ctx.getAttributeValue ("path");   // NOI18N
        tmId = new TomcatModule (t, ctxPath);
        
        command = "deploy?path="+ctxPath; // NOI18N
        cmdType = CommandType.DISTRIBUTE;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, "", StateType.RUNNING));
        istream = is;
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Deploys WAR file or directory to Tomcat using deplPlan as source 
     * of conetx configuration data.
     */
    public void install (Target t, File wmfile, File deplPlan) {
        // WAR file
        String docBase = wmfile.toURI ().toASCIIString ();
        if (docBase.endsWith ("/")) { // NOI18N
            docBase = docBase.substring (0, docBase.length ()-1);
        }
        if (wmfile.isFile ()) {
            // WAR file
            docBase = "jar:"+docBase+"!/"; // NOI18N
        }
        // config or path
        String ctxPath = null;
        try {
            if (!deplPlan.exists ()) {
                if (wmfile.isDirectory ()) {
                    ctxPath = "/"+wmfile.getName ();    // NOI18N
                }
                else {
                    ctxPath = "/"+wmfile.getName ().substring (0, wmfile.getName ().lastIndexOf ('.'));    // NOI18N
                }
                tmId = new TomcatModule (t, ctxPath); // NOI18N
                command = "deploy?update=true&path="+ctxPath+"&war="+docBase; // NOI18N
            }
            else {
                FileInputStream in = new FileInputStream (deplPlan);
                Context ctx = Context.createGraph (in);
                //PENDING #37763
//                tmId = new TomcatModule (t, ctx.getAttributeValue ("path")); // NOI18N
//                command = "install?update=true&config="+deplPlan.toURI ()+ // NOI18N
//                    "&war="+docBase; // NOI18N
                if (wmfile.isDirectory ()) {
                    ctxPath = "/"+wmfile.getName ();    // NOI18N
                }
                else {
                    ctxPath = "/"+wmfile.getName ().substring (0, wmfile.getName ().lastIndexOf ('.'));    // NOI18N
                }
                ctxPath = ctx.getAttributeValue ("path");
                tmId = new TomcatModule (t, ctxPath); // NOI18N
                command = "deploy?update=true&path="+ctxPath+"&war="+docBase; // NOI18N
            }
            
            // call the command
            cmdType = CommandType.DISTRIBUTE;
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, "", StateType.RUNNING));
            
            rp ().post (this, 0, Thread.NORM_PRIORITY);
        }
        catch (java.io.FileNotFoundException fnfe) {
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, fnfe.getLocalizedMessage (), StateType.FAILED));
        }
        
    }
    
    public void initialDeploy (Target t, File contextXml, File dir) {
        try {
            FileInputStream in = new FileInputStream (contextXml);
            Context ctx = Context.createGraph (in);
            String docBaseURI = dir.getAbsoluteFile().toURI().toASCIIString();
            String docBase = dir.getAbsolutePath ();
            this.tmId = new TomcatModule (t, ctx.getAttributeValue ("path"), docBase); //NOI18N
            if (!docBase.equals (ctx.getAttributeValue ("docBase"))) { //NOI18N
                ctx.setAttributeValue ("docBase", docBase); //NOI18N
                FileOutputStream fos = new FileOutputStream (contextXml);
                ctx.write (fos);
                fos.close ();
            }
            command = "deploy?config=" + contextXml.toURI ().toASCIIString () + "&war=" + docBaseURI; // NOI18N
            cmdType = CommandType.DISTRIBUTE;
        } catch (java.io.IOException ioex) {
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, ioex.getLocalizedMessage (), StateType.FAILED));
        }
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, "", StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }

    public void remove (TomcatModule tmId) {
        this.tmId = tmId;
        command = "undeploy?path="+tmId.getPath (); // NOI18N
        cmdType = CommandType.UNDEPLOY;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, "", StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Starts web module. */
    public void start (TomcatModule tmId) {
        this.tmId = tmId;
        command = "start?path="+tmId.getPath (); // NOI18N
        cmdType = CommandType.START;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, "", StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Stops web module. */
    public void stop (TomcatModule tmId) {
        this.tmId = tmId;
        command = "stop?path="+tmId.getPath (); // NOI18N
        cmdType = CommandType.STOP;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, "", StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Reloads web module. */
    public void reload (TomcatModule tmId) {
        this.tmId = tmId;
        command = "reload?path="+tmId.getPath (); // NOI18N
        cmdType = CommandType.REDEPLOY;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, "", StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    public void incrementalRedeploy (TomcatModule tmId) {
        try {
            this.tmId = tmId;
            String docBase = tmId.getDocRoot ();
            assert docBase != null;
            String docBaseURI = new File (docBase).toURI().toASCIIString();
            File contextXml = new File (docBase + "/META-INF/context.xml");
            FileInputStream in = new FileInputStream (contextXml);
            Context ctx = Context.createGraph (in);
            if (!docBase.equals (ctx.getAttributeValue ("docBase"))) { //NOI18N
                ctx.setAttributeValue ("docBase", docBase); //NOI18N
                FileOutputStream fos = new FileOutputStream (contextXml);
                ctx.write (fos);
                fos.close ();
            }
            command = "deploy?config=" + contextXml.toURI ().toASCIIString () + "&war=" + docBaseURI; // NOI18N
            cmdType = CommandType.DISTRIBUTE;
        } catch (java.io.IOException ioex) {
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, ioex.getLocalizedMessage (), StateType.FAILED));
        }
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, "", StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Lists web modules.
     * This method runs synchronously.
     * @param target server target
     * @param state one of ENUM_ constants.
     */
    TargetModuleID[] list (Target t, int state) {
        this.tmId = tmId;
        command = "list"; // NOI18N
        run ();
        // PENDING : error check
        java.util.List modules = new java.util.ArrayList ();
        boolean first = true;
        StringTokenizer stok = new StringTokenizer (output, "\r\n");    // NOI18N
        while (stok.hasMoreTokens ()) {
            String line = stok.nextToken ();
            if (first) {
                first = false;
            }
            else {
                StringTokenizer ltok = new StringTokenizer (line, ":"); // NOI18N
                try {
                    String ctx = ltok.nextToken ();
                    String s = ltok.nextToken ();
                    String tag = ltok.nextToken ();
                    //take the rest of line as path (it can contain ':')
                    String path = line.substring (ctx.length () + s.length () + tag.length () + 3);
                    if ("running".equals (s)
                    &&  (state == TomcatManager.ENUM_AVAILABLE || state == TomcatManager.ENUM_RUNNING)) {
                        modules.add (new TomcatModule (t, ctx, path));
                    }
                    if ("stopped".equals (s)
                    &&  (state == TomcatManager.ENUM_AVAILABLE || state == TomcatManager.ENUM_NONRUNNING)) {
                        modules.add (new TomcatModule (t, ctx, path));
                    }
                }
                catch (java.util.NoSuchElementException e) {
                    // invalid value
                    e.printStackTrace ();
                }
            }
        }
        return (TargetModuleID []) modules.toArray (new TargetModuleID[modules.size ()]);
    }
    
    /** Queries Tomcat server to get JMX beans containing management information
     * @param param encoded parameter(s) for query
     * @return server output
     */
    public String jmxProxy (String query) {
        command = "jmxproxy/"+query; // NOI18N
        run ();
        // PENDING : error check
        return output;
    }
    
    /** JSR88 method. */
    public ClientConfiguration getClientConfiguration (TargetModuleID targetModuleID) {
        return null; // PENDING
    }
    
    /** JSR88 method. */
    public DeploymentStatus getDeploymentStatus () {
        return pes.getDeploymentStatus ();
    }
    
    /** JSR88 method. */
    public TargetModuleID[] getResultTargetModuleIDs () {
        return new TargetModuleID [] { tmId };
    }
    
    /** JSR88 method. */
    public boolean isCancelSupported () {
        return false;
    }
    
    /** JSR88 method. */
    public void cancel () 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("cancel not supported in Tomcat deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    public boolean isStopSupported () {
        return false;
    }
    
    /** JSR88 method. */
    public void stop () throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("stop not supported in Tomcat deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    public void addProgressListener (ProgressListener l) {
        pes.addProgressListener (l);
    }
    
    /** JSR88 method. */
    public void removeProgressListener (ProgressListener l) {
        pes.removeProgressListener (l);
    }
    
    /** Executes one management task. */
    public synchronized void run () {
        TomcatFactory.getEM ().log(ErrorManager.INFORMATIONAL, command);
        pes.fireHandleProgressEvent (tmId, new Status (ActionType.EXECUTE, cmdType, command /* message */, StateType.RUNNING));
        
        output = ""; 
        
        int retries = 4;
        
        // similar to Tomcat's Ant task
        URLConnection conn = null;
        InputStreamReader reader = null;
        
        URL urlToConnectTo = null;

        String msg = null;
        while (retries >= 0) {
            retries = retries - 1;
            try {

                // Create a connection for this command
                String uri = tm.getPlainUri ();
                String withoutSpaces = (uri + command).replaceAll(" ", "%20");  //NOI18N
                urlToConnectTo = new URL(withoutSpaces);
                
                if (Boolean.getBoolean("org.netbeans.modules.tomcat5.LogManagerCommands")) { // NOI18N
                    String message = "Tomcat 5 sending manager command: " + urlToConnectTo;
                    System.out.println(message);
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(message));
                }

                URLWait.waitForStartup(tm, 30000);

                conn = urlToConnectTo.openConnection();
                HttpURLConnection hconn = (HttpURLConnection) conn;

                // Set up standard connection characteristics
                hconn.setAllowUserInteraction(false);
                hconn.setDoInput(true);
                hconn.setUseCaches(false);
                if (istream != null) {
                    hconn.setDoOutput(true);
                    hconn.setRequestMethod("PUT");   // NOI18N
                    hconn.setRequestProperty("Content-Type", "application/octet-stream");   // NOI18N
                } else {
                    hconn.setDoOutput(false);
                    hconn.setRequestMethod("GET"); // NOI18N
                }
                hconn.setRequestProperty("User-Agent", // NOI18N
                                         "NetBeansIDE-Tomcat-Manager/1.0"); // NOI18N
                // Set up an authorization header with our credentials
                String input = tm.getUsername () + ":" + tm.getPassword ();
                String auth = new String(Base64.encode(input.getBytes()));
                hconn.setRequestProperty("Authorization", // NOI18N
                                         "Basic " + auth); // NOI18N

                // Establish the connection with the server
                hconn.connect();
                if (Boolean.getBoolean("org.netbeans.modules.tomcat5.LogManagerCommands")) { // NOI18N
                    int code = hconn.getResponseCode();
                    String message = "Tomcat 5 receiving response, code: " + code;
                    System.out.println(message);
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(message));
                }
                // Send the request data (if any)
                if (istream != null) {
                    BufferedOutputStream ostream =
                        new BufferedOutputStream(hconn.getOutputStream(), 1024);
                    byte buffer[] = new byte[1024];
                    while (true) {
                        int n = istream.read(buffer);
                        if (n < 0) {
                            break;
                        }
                        ostream.write(buffer, 0, n);
                    }
                    ostream.flush();
                    ostream.close();
                    istream.close();
                }

                // Process the response message
                reader = new InputStreamReader(hconn.getInputStream(),"UTF-8"); //NOI18N
                retries = -1;
                StringBuffer buff = new StringBuffer();
                String error = null;
                msg = null;
                boolean first = !command.startsWith ("jmxproxy");   // NOI18N
                while (true) {
                    int ch = reader.read();
                    if (ch < 0) {
                        output += buff.toString ()+"\n";    // NOI18N
                        break;
                    } else if ((ch == '\r') || (ch == '\n')) {
                        String line = buff.toString();
                        buff.setLength(0);
                        TomcatFactory.getEM ().log(ErrorManager.INFORMATIONAL, line);
                        if (first) {
                            // hard fix to accept the japanese localization of manager app
                            String japaneseOK="\u6210\u529f"; //NOI18N
                            msg = line;
                            if (!(line.startsWith("OK -") || line.startsWith(japaneseOK))) { // NOI18N
                                error = line;
                            }
                            first = false;
                        }
                        pes.fireHandleProgressEvent (
                            tmId, 
                            new Status (ActionType.EXECUTE, cmdType, line, StateType.RUNNING)
                        );
                        output += line+"\n";    // NOI18N
                    } else {
                        buff.append((char) ch);
                    }
                }
                if (buff.length() > 0) {
                    TomcatFactory.getEM ().log(ErrorManager.INFORMATIONAL, buff.toString());
                }
                if (error != null) {
                    TomcatFactory.getEM().log("TomcatManagerImpl connecting to: " + urlToConnectTo); // NOI18N
                    TomcatFactory.getEM ().log (error);
                    pes.fireHandleProgressEvent (tmId, new Status (ActionType.EXECUTE, cmdType, error, StateType.FAILED));
                }

            } catch (Exception e) {
                if (retries < 0) {
                    TomcatFactory.getEM().log("TomcatManagerImpl connecting to: " + urlToConnectTo); // NOI18N
                    TomcatFactory.getEM ().notify (ErrorManager.INFORMATIONAL, e);
                    pes.fireHandleProgressEvent (tmId, new Status (ActionType.EXECUTE, cmdType, e.getLocalizedMessage (), StateType.FAILED));
                }
                // throw t;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (java.io.IOException ioe) { // ignore this
                    }
                    reader = null;
                }
                if (istream != null) {
                    try {
                        istream.close();
                    } catch (java.io.IOException ioe) { // ignore this
                    }
                    istream = null;
                }
            }
            if (retries >=0) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {}
            }
        } // while
        pes.fireHandleProgressEvent (tmId, new Status (ActionType.EXECUTE, cmdType, msg, StateType.COMPLETED));

    }

}
