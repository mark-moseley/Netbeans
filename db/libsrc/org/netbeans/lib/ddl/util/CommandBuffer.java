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

package org.netbeans.lib.ddl.util;

import java.util.*;

import org.openide.*;

import org.netbeans.lib.ddl.*;

/**
* Command buffer used to execute a bunch of commands. Main advantages of using
* buffer is:
* - Optimized connection handling. Buffer opens JDBC connection before executing
* of first command and closes it after a last one. It's safely then manually
* handling connection and better then leaving commands open and close connection
* for each comand separately.
* - Exception handler. You can assign an exception handler to buffer. When any 
* error occures during the execution, this handler catches it and lets user to 
* decide if continue or not (when you're dropping nonexisting table, you probably 
* would like to continue).
* - Debgging. You can set up debug mode and buffer will print each command to 
* System.out before execution.
*
* @author   Slavek Psenicka
*/
public class CommandBuffer
{
    /** Buffered items */
    Vector commands;

    /** Exception handler */
    CommandBufferExceptionHandler handler;

    /** Debug mode */
    boolean debugmode;
    
    /** Execution command with some exception */
    boolean executionWithException;

    /** Adds command to buffer
    * @param cmd Command to add.
    */
    public void add(DDLCommand cmd)
    {
        if (commands == null) commands = new Vector();
        commands.add(cmd);
    }

    /** Sets exception handler.
    * This handler will catch and alows user to solve all exception throwed during
    * the executing of buffered commands.
    */
    public void setExceptionHandler(CommandBufferExceptionHandler hand)
    {
        handler = hand;
    }

    /** Returns true if debugging mode is on.
    * You can set up debug mode and buffer will print each command to 
    * System.out before execution.
    */
    public boolean isDebugMode()
    {
        return debugmode;
    }

    /** Sets debug mode on/off.
    * You can set up debug mode and buffer will print each command to 
    * System.out before execution.
    * @param flag true = debugging enabled
    */
    public void setDebugMode(boolean flag)
    {
        debugmode = flag;
    }

    /** Returns a string with string representation of all commands in buffer
    */
    public String getCommands()
    throws DDLException
    {
        String cmds = "";
        Enumeration cmd_e = commands.elements();
        while (cmd_e.hasMoreElements()) {
            DDLCommand e_cmd = (DDLCommand)cmd_e.nextElement();
            cmds = cmds + e_cmd.getCommand() + "\n";
        }

        return cmds;
    }

    /** Executes commnds in buffer.
    * Buffer opens JDBC connection before executing (if isn't already open)
    * of first command and closes it after a last one. It's safely then manually 
    * handling connection and better then leaving commands open and close connection 
    * for each comand separately. You can also assign an exception handler to buffer. 
    * When any error occures during the execution, this handler catches it and lets user to 
    * decide if continue or not (when you're dropping nonexisting table, you probably 
    * would like to continue).	
    */
    public void execute()
    throws DDLException
    {
        boolean opencon = false;
        executionWithException = false;
        DatabaseSpecification spec = null;
        Enumeration cmd_e = commands.elements();
        while (cmd_e.hasMoreElements()) {
            DDLCommand e_cmd = (DDLCommand)cmd_e.nextElement();
            try {
                if (spec == null) {
                    spec = e_cmd.getSpecification();
                    if (spec.getJDBCConnection() == null) {
                        opencon = true;
                        spec.openJDBCConnection();
                    }
                }
                if (debugmode) System.out.println(e_cmd);
                e_cmd.execute();
                executionWithException = e_cmd.wasException();
            } catch (Exception e) {
                //				e.printStackTrace();
                executionWithException = true;
                boolean exres = false;
                if (handler != null)
                    exres = handler.shouldContinueAfterException(e);
                if (!exres)
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
            }
        }

        if (opencon) spec.closeJDBCConnection();
    }
    
    /** information about appearance some exception in the last execute a bunch of commands */
    public boolean wasException() {
        return executionWithException;
    }
}
