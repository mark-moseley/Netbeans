/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.Connector.Argument;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


/**
 * Launches a new JVM in debug mode and returns VirtualMachine for it.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerInfo di = DebuggerInfo.create (
 *        "My First Launching Debugger Info", 
 *        new Object [] {
 *            LaunchingDICookie.create (
 *                "examples.texteditor.Ted",
 *                new String [] {},
 *                "c:\\nb\\settings\\sampledir",
 *                true
 *            )
 *        }
 *    );
 *    DebuggerManager.getDebuggerManager ().startDebugging (di);</pre>
 *
 * @author Jan Jancura
 */
public final class LaunchingDICookie extends AbstractDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-jpda-LaunchingDICookie";

    private LaunchingConnector  launchingConnector;
    private Map                 args;

    private String              mainClassName;
    private boolean             suspend;


    private LaunchingDICookie (
        LaunchingConnector launchingConnector,
        Map args,
        String mainClassName,
        boolean suspend
    ) {
        this.launchingConnector = launchingConnector;
        this.args = args;
        this.mainClassName = mainClassName;
        this.suspend = suspend;
    }

    /**
     * Creates a new instance of LaunchingDICookie for given parameters.
     *
     * @param mainClassName a name or main class
     * @param commandLine command line of debugged JVM
     * @param address a address to listen on
     * @param suspend if true session will be suspended
     * @return a new instance of LaunchingDICookie for given parameters
     */
    public static LaunchingDICookie create (
        String          mainClassName,
        String          commandLine,
        String          address,
        boolean         suspend
    ) {
        return new LaunchingDICookie (
            findLaunchingConnector (),
            getArgs (commandLine, address),
            mainClassName,
            suspend
        );
    }

    /**
     * Creates a new instance of LaunchingDICookie for given parameters.
     *
     * @param mainClassName a name or main class
     * @param args command line arguments
     * @param classPath a classPath
     * @param suspend if true session will be suspended
     * @return a new instance of LaunchingDICookie for given parameters
     */
    public static LaunchingDICookie create (
        String          mainClassName,
        String[]        args,
        String          classPath,
        boolean         suspend
    ) {
        String argss = "";
        int i, k = args.length;
        for (i = 0; i < k; i++) {
            argss += " \"" + args [i] + "\"";
        }
        String commandLine = System.getProperty ("java.home") + 
            "\\bin\\java -Xdebug -Xnoagent -Xrunjdwp:transport=" + 
            getTransportName () + 
            ",address=name,suspend=" + 
            (suspend ? "y" : "n") +
            " -Djava.compiler=NONE -classpath \"" + 
            classPath + 
            "\" " +
            mainClassName + 
            argss;
        String address = "name";
        return new LaunchingDICookie (
            findLaunchingConnector (),
            getArgs (commandLine, address),
            mainClassName,
            suspend
        );
    }
    
    /**
     * Returns type of transport to be used.
     *
     * @return type of transport to be used
     */
    public static String getTransportName () {
        return findLaunchingConnector ().transport ().name ();
    }


    // main methods ............................................................

    /**
     * Returns main class name.
     *
     * @return main class name
     */
    public String getClassName () {
        return mainClassName;
    }

    /**
     * Returns suspended state.
     *
     * @return suspended state
     */
    public boolean getSuspend () {
        return suspend;
    }

    /**
     * Returns command line to be used.
     *
     * @return command line to be used
     */
    public String getCommandLine () {
        Argument a = (Argument) args.get ("command");
        if (a == null) return null;
        return a.value ();
    }
    
    /**
     * Creates a new instance of VirtualMachine for this DebuggerInfo Cookie.
     *
     * @return a new instance of VirtualMachine for this DebuggerInfo Cookie
     */
    public VirtualMachine getVirtualMachine () throws IOException,
    IllegalConnectorArgumentsException, VMStartException {
        return launchingConnector.launch (args);
    }
    
    
    // private helper methods ..................................................

    private static Map getArgs (
        String commandLine,
        String address
    ) {
        Map args = findLaunchingConnector ().defaultArguments ();
        ((Argument) args.get ("command")).setValue (commandLine);
        ((Argument) args.get ("address")).setValue (address);
        return args;
    }
    
    private static LaunchingConnector findLaunchingConnector () {
        Iterator iter = Bootstrap.virtualMachineManager ().
            launchingConnectors ().iterator ();
        while (iter.hasNext ()) {
            LaunchingConnector lc = (LaunchingConnector) iter.next ();
            if (lc.name ().indexOf ("RawCommandLineLaunch") > -1)
                return lc;
        }
        return null;
    }
}
