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

package org.apache.tools.ant.module.bridge.impl;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.*;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/** NetBeans-sensitive build logger.
 */
final class NbBuildLogger implements BuildLogger {
    
    private boolean updateStatusLine;
    /** see "#19929" */
    private String displayName;
    
    public NbBuildLogger(boolean updateStatusLine, String displayName) {
        this.updateStatusLine = updateStatusLine;
        this.displayName = displayName;
    }
    
    // [PENDING] more coherent selection of messages based on verbosity
    
    private int level = Project.MSG_INFO;
    private PrintStream out, err;
    
    /** Time when build started. */
    private long startTime = System.currentTimeMillis();
    
    public void setEmacsMode(boolean ignore) {
        // Gack! Why is this part of an *interface*?!
    }
    
    public void setMessageOutputLevel(int l) {
        level = l;
    }
    
    public void setErrorPrintStream(PrintStream ps) {
        err = ps;
    }
    
    public void setOutputPrintStream(PrintStream ps) {
        out = ps;
    }
    
    private static final Pattern classpathPattern = Pattern.compile("\n'-classpath'\n'(.*)'\n"); // NOI18N
    
    public void messageLogged(BuildEvent ev) {
        if (ev.getPriority() <= level) {
            // XXX should translate tabs to spaces here as a safety measure
            if (ev.getPriority() <= Project.MSG_WARN) {
                err.println(ev.getMessage());
            } else {
                out.println(ev.getMessage());
            }
        }
        // Hack to find the classpath an Ant task is using.
        // Cf. Commandline.describeArguments, issue #28190.
        if (ev.getPriority() == Project.MSG_VERBOSE) {
            Matcher m = classpathPattern.matcher(ev.getMessage());
            if (m.find()) {
                String cp = m.group(1);
                err.println("***CLASSPATH***=" + cp); // NOI18N
            }
        }
        // XXX should also probably clear classpath when taskFinished called
    }
    
    public void buildStarted(BuildEvent ev) {
        startTime = System.currentTimeMillis();
        
        if (updateStatusLine) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "FMT_running_ant", displayName));
        }
        // no messages printed for now
    }
    
    public void buildFinished(BuildEvent ev) {
        Throwable t = ev.getException();
        // result message
        long time = System.currentTimeMillis() - startTime; // #10305
        if (t == null) {
            out.println(formatMessageWithTime("FMT_finished_target_printed", time));
            if (updateStatusLine) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "FMT_finished_target_status", displayName));
            }
        } else {
            if ((t instanceof BuildException) && level < Project.MSG_VERBOSE) {
                // Stack trace probably not required.
                err.println(t);
            } else if (!(t instanceof ThreadDeath) || level >= Project.MSG_VERBOSE) {
                // ThreadDeath can be thrown when killing an Ant process, so don't print it normally
                t.printStackTrace(err);
            }
            err.println(formatMessageWithTime("FMT_target_failed_printed", time)); // #10305
            if (updateStatusLine) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "FMT_target_failed_status", displayName));
            }
        }
    }
    
    public void targetStarted(BuildEvent ev) {
        // XXX this could start indenting messages, perhaps
        String name = ev.getTarget().getName();
        // Avoid printing internal targets normally:
        int minlevel = (name.length() > 0 && name.charAt(0) == '-') ? Project.MSG_VERBOSE : Project.MSG_INFO;
        if (level >= minlevel) {
            out.println(NbBundle.getMessage(NbBuildLogger.class, "MSG_target_started_printed", name));
        }
    }
    
    public void targetFinished(BuildEvent ev) {
        // ignore for now
    }
    
    public void taskStarted(BuildEvent ev) {
        // ignore for now
    }
    
    public void taskFinished(BuildEvent ev) {
        // ignore for now
    }
    
    /** Formats the millis in a human readable String.
     * Total time: {0} minutes
     *             {1} seconds
     */
    private static String formatMessageWithTime(String key, long millis) {
        int secs = (int) (millis / 1000);
        int minutes = secs / 60;
        int seconds = secs % 60;
        return NbBundle.getMessage(NbBuildLogger.class, key, new Integer(minutes), new Integer(seconds));
    }
}
