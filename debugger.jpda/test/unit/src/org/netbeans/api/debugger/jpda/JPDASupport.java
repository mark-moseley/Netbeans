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

package org.netbeans.api.debugger.jpda;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.*;

import java.io.*;
import java.util.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.sun.jdi.connect.*;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.Bootstrap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 * Contains support functionality for unit tests.
 *
 * @author Maros Sandor
 */
public class JPDASupport implements DebuggerManagerListener {

    private static final boolean    verbose = false;
    private static final DateFormat df = new SimpleDateFormat("kk:mm:ss.SSS");
    private static DebuggerManager  dm = DebuggerManager.getDebuggerManager ();

    private JPDADebugger            jpdaDebugger;
    private DebuggerEngine          debuggerEngine;
    

    private Object [] debuggerStartLock = new Object[1];
    private Object [] stepLock = new Object[1];

    private Object              STATE_LOCK = new Object ();
    
    
    private JPDASupport (JPDADebugger jpdaDebugger) {
        this.jpdaDebugger = jpdaDebugger;
        jpdaDebugger.addPropertyChangeListener (this);
        DebuggerEngine[] de = dm.getDebuggerEngines ();
        int i, k = de.length;
        for (i = 0; i < k; i++)
            if (de [i].lookupFirst (null, JPDADebugger.class) == jpdaDebugger) {
                debuggerEngine = de [i];
                break;
            }
    }

    
    // starting methods ........................................................

//    public static JPDASupport listen (String mainClass) 
//    throws IOException, IllegalConnectorArgumentsException, 
//    DebuggerStartException {
//        VirtualMachineManager vmm = Bootstrap.virtualMachineManager ();
//        List lconnectors = vmm.listeningConnectors ();
//        ListeningConnector connector = null;
//        for (Iterator i = lconnectors.iterator (); i.hasNext ();) {
//            ListeningConnector lc = (ListeningConnector) i.next ();
//            Transport t = lc.transport ();
//            if (t != null && t.name ().equals ("dt_socket")) {
//                connector = lc;
//                break;
//            }
//        }
//        if (connector == null) 
//            throw new RuntimeException 
//                ("No listening socket connector available");
//
//        Map args = connector.defaultArguments ();
//        String address = connector.startListening (args);
//        String localhostAddres;
//        try
//        {
//            int port = Integer.parseInt 
//                (address.substring (address.indexOf (':') + 1));
//            localhostAddres = "localhost:" + port;
//            Connector.IntegerArgument portArg = 
//                (Connector.IntegerArgument) args.get("port");
//            portArg.setValue(port);
//        } catch (Exception e) {
//            // this address format is not known, use default
//            localhostAddres = address;
//        }
//
//        JPDADebugger jpdaDebugger = JPDADebugger.listen 
//            (connector, args, createServices ());
//        if (jpdaDebugger == null) 
//            throw new DebuggerStartException ("JPDA jpdaDebugger was not started");
//        Process process = launchVM (mainClass, localhostAddres, false);
//        ProcessIO pio = new ProcessIO (process);
//        pio.go ();
//        return new JPDASupport (jpdaDebugger);
//    }

    public static JPDASupport attach (String mainClass) throws IOException, 
    DebuggerStartException {
        Process process = launchVM (mainClass, "", true);
        String line = readLine (process.getInputStream ());
        int port = Integer.parseInt (line.substring (line.lastIndexOf (':') + 1).trim ());
        ProcessIO pio = new ProcessIO (process);
        pio.go ();

        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List aconnectors = vmm.attachingConnectors();
        AttachingConnector connector = null;
        for (Iterator i = aconnectors.iterator(); i.hasNext();) {
            AttachingConnector ac = (AttachingConnector) i.next();
            Transport t = ac.transport ();
            if (t != null && t.name().equals("dt_socket")) {
                connector = ac;
                break;
            }
        }
        if (connector == null) 
            throw new RuntimeException
                ("No attaching socket connector available");

        JPDADebugger jpdaDebugger = JPDADebugger.attach (
            "localhost", 
            port, 
            createServices ()
        );
        return new JPDASupport (jpdaDebugger);
    }

    
    // public interface ........................................................
    
    public void doContinue () {
        if (jpdaDebugger.getState () != JPDADebugger.STATE_STOPPED) 
            throw new IllegalStateException ();
        debuggerEngine.getActionsManager ().doAction 
            (ActionsManager.ACTION_CONTINUE);
    }

    public void stepOver () {
        step (ActionsManager.ACTION_STEP_OVER);
    }

    public void stepInto () {
        step (ActionsManager.ACTION_STEP_INTO);
    }

    public void stepOut () {
        step (ActionsManager.ACTION_STEP_OUT);
    }

    public void step (Object action) {
        if (jpdaDebugger.getState () != JPDADebugger.STATE_STOPPED)
            throw new IllegalStateException ();
        debuggerEngine.getActionsManager ().doAction (action);
        waitState (JPDADebugger.STATE_STOPPED);
    }

    public void doFinish () {
        if (jpdaDebugger == null) return;
        debuggerEngine.getActionsManager ().
            doAction (ActionsManager.ACTION_KILL);
        waitState (JPDADebugger.STATE_DISCONNECTED);
    }

    public void waitState (int state) {
        while ( jpdaDebugger.getState () != state &&
                jpdaDebugger.getState () != JPDADebugger.STATE_DISCONNECTED
        ) {
            synchronized (STATE_LOCK) {
                try {
                    STATE_LOCK.wait ();
                } catch (InterruptedException ex) {
                    ex.printStackTrace ();
                }
            }
        }
    }

    public JPDADebugger getDebugger() {
        return jpdaDebugger;
    }
    
    
    // other methods ...........................................................
    
    private static Object[] createServices () {
        try {
            Map map = new HashMap ();
            ClassLoader cl = JPDASupport.class.getClassLoader ();
            String file = "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.class";
            URL url = cl.getResource (file);
            String surl = url.toString ();
            url = new URL (surl.substring (0, surl.length () - file.length ()));
            ClassPath cp = ClassPathSupport.createClassPath (new URL[] {
                url
            });
            map.put ("sourcepath", cp);
            return new Object[] {
                map
            };
        } catch (MalformedURLException ex) {
            return new Object[] {};
        }
    }

    private static String readLine (InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (;;) {
            int c = in.read();
            if (c == -1) throw new EOFException();
            if (c == 0x0D) {
                c = in.read();
                if (c != 0x0A) sb.append((char)0x0D);
            }
            if (c == 0x0A) return sb.toString();
            sb.append((char)c);
        }
    }
    
    private static Process launchVM (
        String mainClass, 
        String connectorAddress, 
        boolean server
    ) throws IOException {

        URLClassLoader ucl = (URLClassLoader) JPDASupport.class.
            getClassLoader ();
        URL [] urls = ucl.getURLs ();

        StringBuffer cp = new StringBuffer (200);
        for (int i = 0; i < urls.length; i++) {
            URL url = urls [i];
            cp.append (url.getPath ());
            cp.append (File.pathSeparatorChar);
        }

        String [] cmdArray = new String [] {
            System.getProperty ("java.home") + File.separatorChar + 
                "bin" + File.separatorChar + "java",
            "-Xdebug",
            "-Xnoagent",
            "-Xrunjdwp:transport=" + "dt_socket" + ",address=" + 
                connectorAddress + ",suspend=y,server=" + 
                (server ? "y" : "n"),
            "-Djava.compiler=NONE",
            "-classpath",
            cp.substring(0, cp.length() -1),
            mainClass
        };

        return Runtime.getRuntime ().exec (cmdArray);
    }
    
    
    // DebuggerListener ........................................................

    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[0];
    }

    public void breakpointAdded(Breakpoint breakpoint) {
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
    }

    public void initWatches() {
    }

    public void watchAdded(Watch watch) {
    }

    public void watchRemoved(Watch watch) {
    }

    public void sessionAdded(Session session) {
    }

    public void sessionRemoved(Session session) {
    }

    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof JPDADebugger) {
            JPDADebugger dbg = (JPDADebugger) evt.getSource();

            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                synchronized (STATE_LOCK) {
                    STATE_LOCK.notifyAll ();
                }
                if (jpdaDebugger.getState () == JPDADebugger.STATE_DISCONNECTED)
                    jpdaDebugger.removePropertyChangeListener (this);
            }
        }
    }

    // TODO: Include check of these call in the test suite
    public void engineAdded (DebuggerEngine debuggerEngine) {
    }

    // TODO: Include check of these call in the test suite
    public void engineRemoved (DebuggerEngine debuggerEngine) {
    }

    
    // innerclasses ............................................................
    
    private static class ProcessIO {

        private Process p;

        public ProcessIO(Process p) {
            this.p = p;
        }

        public void go() {
            InputStream out = p.getInputStream();
            InputStream err = p.getErrorStream();

            new SimplePipe(System.out, out).start();
            new SimplePipe(System.out, err).start();
        }
    }

    private static class SimplePipe extends Thread {
        private OutputStream out;
        private InputStream in;

        public SimplePipe(OutputStream out, InputStream in) {
            this.out = out;
            this.in = in;
            setDaemon(true);
        }

        public void run() {
            byte [] buffer = new byte[1024];
            int n;
            try {
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            } catch (IOException e) {
            } finally {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                }
            }
            System.out.println("PIO QUIT");
        }
    }
}
