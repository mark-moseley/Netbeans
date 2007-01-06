/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.execution;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.util.Utilities;

/** Encapsulates start information for a process. It allows the user to
* specify the process name to execute and arguments to provide. The progammer
* then uses method exec to start the process and can pass additional format that
* will be applied to arguments. 
* <P>
* This allows to define arguments in format -user {USER_NAME} -do {ACTION} and then
* use MapFormat with defined values for USER_NAME and ACTION that will be substitued
* by into the arguments.
*
* @author  Ian Formanek, Jaroslav Tulach
*/
public final class NbProcessDescriptor extends Object implements Serializable {

    private static final long serialVersionUID = -4535211234565221486L;
    
    /** Logger for logging execs */
    private static Logger execLog;

    /** The name of the executable to run */
    private String processName;
    /** argument format */
    private String arguments;
    /** info about format of the arguments */
    private String info;

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param processName     the name of the executable to run
    * @param arguments string for formating of arguments (may be {@link Utilities#parseParameters quoted})
    */
    public NbProcessDescriptor(String processName, String arguments) {
        this (processName, arguments, null);
    }

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param processName     the name of the executable to run
    * @param arguments string for formating of arguments (may be {@link Utilities#parseParameters quoted})
    * @param info info how to format the arguments (human-readable string)
    */
    public NbProcessDescriptor(String processName, String arguments, String info) {
        this.processName = processName;
        this.arguments = arguments;
        this.info = info;
    }


    /** Get the name of the executable to run.
    * @return the name
    */
    public String getProcessName () {
        return processName;
    }

    /** Getter the execution arguments of the process.
    * @return the switch that the executable uses for passing the classpath as its command-line parameter 
    */
    public String getArguments () {
        return arguments;
    }

    /** Getter for the human readable info about the arguments.
    * @return the info string or null
    */
    public String getInfo () {
        return info;
    }

    /* JST: Commented out, should not be needed.
    *
    *  Get the command string and arguments from the supplied process name.
    * Normally the process name will be the actual name of the process executable,
    * in which case this method will just return that name by itself.
    * However, {@link org.openide.util.Utilities#parseParameters} is used
    * to break apart the string into tokens, so that users may:
    * <<moved to Utilities.parseParameters Javadoc>>
    * @return a list of the command name itself and any arguments, unescaped
    * @see Runtime#exec(String[])
    *
    public String[] getProcessArgs() {
      if (processArguments == null) {
        processArguments = parseArguments(processName);
      }
      return (String[]) processArguments.clone();
    }
    */

    /** Executes the process with arguments formatted by the provided
    * format. Also the envp properties are passed to the executed process,
    * and a working directory may be supplied.
    *
    * @param format format to be applied to arguments, process and envp supplied by user. It can be <code>null</code> if no formatting should be done.
    * @param envp list of properties to be applied to the process, or <code>null</code> to leave unspecified
    * @param cwd the working directory to use, or <code>null</code> if this should not be specified
    * @return handle to executed process.
    * @exception IOException if the start of the process fails, or if setting the working directory is not supported
    */
    public Process exec (Format format, String[] envp, File cwd) throws IOException {
        return exec (format, envp, false, cwd);
    }
    
    /** Executes the process with arguments, processName and envp formatted by the provided
    * format. Also the envp properties are passed to the executed process,
    * and a working directory may be supplied. Optionally the environment variables of the NetBeans JVM may
    * be appended to (replaced when there is overlap) instead of specifying
    * all of the environment variables from scratch. This requires the NetBeans core
    * to translate environment variables to system properties prefixed
    * by <samp>Env-</samp> in order to work correctly.
    *
    * @param format format to be applied to arguments, process and envp supplied by user. It can be <code>null</code> if no formatting should be done.
    * @param envp list of properties to be applied to the process, or <code>null</code> to leave unspecified
    * @param appendEnv if true and <code>envp</code> is not <code>null</code>, append or replace JVM's environment
    * @param cwd the working directory to use, or <code>null</code> if this should not be specified
    * @return handle to executed process.
    * @exception IOException if the start of the process fails, or if setting the working directory is not supported
    * @since 1.15
    */
    public Process exec (Format format, String[] envp, boolean appendEnv, File cwd) throws IOException {
        String stringArgs = format == null ? arguments : format.format (arguments);
        String[] args = parseArguments (stringArgs);
        String[] call = null;
        
        envp = substituteEnv(format, envp);
       
        // copy the call string
        call = new String[args.length + 1];
        call[0] = format == null ? processName : format.format(processName);
        System.arraycopy (args, 0, call, 1, args.length); 

        logArgs(call);
        
        ProcessBuilder pb = new ProcessBuilder(call);
        
        if (envp != null) {
            Map<String,String> e = pb.environment();
            if (!appendEnv) e.clear();
            for (int i = 0; i < envp.length; i++) {
                String nameval = envp[i];
                int idx = nameval.indexOf ('='); // NOI18N
                // [PENDING] add localized annotation...
                if (idx == -1) throw new IOException ("No equal sign in name=value: " + nameval); // NOI18N
                e.put (nameval.substring (0, idx), nameval.substring (idx + 1));
            }
        }

        if (cwd != null) pb.directory(cwd);
        return pb.start();
    }
    
    private static void logArgs(String[] args) {
        getExecLog().fine("Running: " + Arrays.asList(args)); // NOI18N
    }

    /** Executes the process with arguments and processNme formatted by the provided
    * format. Also the envp properties are passed to the executed process.
    *
    * @param format format to be aplied to arguments, process and envp suplied by user. It can be <code>null</code> if no formatting should be done.
    * @param envp list of properties to be applied to the process, or <code>null</code> to leave unspecified
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec (Format format, String[] envp) throws IOException {
        return exec (format, envp, null);
    }

    /** Executes the process with arguments and processName formatted by the provided
    * format. 
    *
    * @param format format to be aplied to arguments and process. It can be <code>null</code> if no formatting should be done.
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec (Format format) throws IOException {
        return exec (format, null);
    }

    /** Executes the process with arguments provided in constructor.
    *
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec () throws IOException {
        return exec (null, null);
    }

    /* hashCode */
    public int hashCode() {
        return processName.hashCode() + arguments.hashCode ();
    }

    /* equals */
    public boolean equals(Object o) {
        if (! (o instanceof NbProcessDescriptor)) return false;
        NbProcessDescriptor him = (NbProcessDescriptor) o;
        return processName.equals(him.processName) && arguments.equals(him.arguments);
    }

    /** Parses given string to an array of arguments.
    * @param sargs is tokenized by spaces unless a space is part of "" token
    * @return tokenized string
    */
    private static String[] parseArguments(String sargs) {
        return Utilities.parseParameters(sargs);
    }
    
    /** Getter for the execLog */
    private static Logger getExecLog() {
        if (execLog == null) {
            execLog = Logger.getLogger(NbProcessDescriptor.class.getName());
        }
        return execLog;
    }
    
    /** Iterates through envp and applies format.format() on values
     * @param format for formatting, i.e. substitute {filesystems} to /home/phil/dev/classes/pack1:/home/phil/dev/classes/pack2:...
     * @param envp an String array
     *
     * @return substitutet array
     */
    private static String[] substituteEnv(Format format, String[] envp) {
        if (envp == null || envp.length == 0 || format == null) {
            return envp;
        }
        
        String[] ret = new String[envp.length];
        StringBuffer adder = new StringBuffer();
        for (int i = 0; i < envp.length; i++) {
            ret[i] = envp[i];
            if (ret[i] == null) {
                continue;
            }
            
            int idx = ret[i].indexOf('=');
            if (idx < 0) {
                continue;
            }
            
            String val = ret[i].substring(idx + 1);
            String key = ret[i].substring(0, idx);
            adder.append(key).append('=').append(format.format(val));
            ret[i] = adder.toString();
            adder.setLength(0);
        }
        
        return ret;
    }
}
