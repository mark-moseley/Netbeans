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

package org.apache.tools.ant.module.run;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import java.util.regex.Pattern;

import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;

/**
 * Standard logger for producing Ant output messages.
 * @author Jesse Glick
 */
public final class StandardLogger extends AntLogger {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(StandardLogger.class.getName());
    private static final boolean LOGGABLE = ERR.isLoggable(ErrorManager.INFORMATIONAL);
    
    /**
     * Regexp matching an output line that is a column marker from a compiler or similar.
     * Captured groups:
     * <ol>
     * <li>spaces preceding caret; length indicates column number
     * </ol>
     * @see "#37358"
     */
    private static final Pattern CARET_SHOWING_COLUMN = Pattern.compile("^( *)\\^$"); // NOI18N
    
    /**
     * Data stored in the session.
     */
    private static final class SessionData {
        /** Time build was started. */
        public long startTime;
        /** Last-created hyperlink, in case we need to adjust the column number. */
        public Hyperlink lastHyperlink;
        public SessionData() {}
    }
    
    /** Default constructor for lookup. */
    public StandardLogger() {}
    
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }
    
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    public String[] interestedInTasks(AntSession session) {
        return AntLogger.ALL_TASKS;
    }
    
    public int[] interestedInLogLevels(AntSession session) {
        int verb = session.getVerbosity();
        assert verb >= AntEvent.LOG_ERR && verb <= AntEvent.LOG_DEBUG : verb;
        int[] levels = new int[verb + 1];
        for (int i = 0; i <= verb; i++) {
            levels[i] = i;
        }
        return levels;
    }
    
    private SessionData getSessionData(AntSession session) {
        SessionData data = (SessionData) session.getCustomData(this);
        if (data == null) {
            data = new SessionData();
            session.putCustomData(this, data);
        }
        return data;
    }
    
    public void buildInitializationFailed(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        // Write errors to the output window, since
        // a lot of errors could be annoying as dialogs
        Throwable t = event.getException();
        if (event.getSession().getVerbosity() >= AntEvent.LOG_VERBOSE) {
            deliverStackTrace(t, event);
        } else {
            event.getSession().println(t.toString(), true, null);
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_target_failed_status", event.getSession().getDisplayName()));
        event.consume();
    }

    private static void deliverBlockOfTextAsErrors(String lines, AntEvent originalEvent) {
        StringTokenizer tok = new StringTokenizer(lines, "\n"); // NOI18N
        while (tok.hasMoreTokens()) {
            String line = tok.nextToken();
            originalEvent.getSession().deliverMessageLogged(originalEvent, line, AntEvent.LOG_ERR);
        }
    }
    
    private static void deliverStackTrace(Throwable t, AntEvent originalEvent) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        deliverBlockOfTextAsErrors(sw.toString(), originalEvent);
    }
    
    public void buildStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        getSessionData(event.getSession()).startTime = System.currentTimeMillis();
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_running_ant", event.getSession().getDisplayName()));
        // no messages printed for now
        event.consume();
    }
    
    public void buildFinished(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        AntSession session = event.getSession();
        Throwable t = event.getException();
        long time = System.currentTimeMillis() - getSessionData(session).startTime; // #10305
        if (t == null) {
            session.println(formatMessageWithTime("FMT_finished_target_printed", time), false, null);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_finished_target_status", session.getDisplayName()));
        } else {
            if (!session.isExceptionConsumed(t)) {
                session.consumeException(t);
                if (t.getClass().getName().equals("org.apache.tools.ant.BuildException") && session.getVerbosity() < AntEvent.LOG_VERBOSE) { // NOI18N
                    // Stack trace probably not required.
                    // Check for hyperlink to handle e.g. <fail>
                    // which produces a BE whose toString is the location + message.
                    // But send to other loggers since they may wish to suppress such an error.
                    String msg = t.toString();
                    deliverBlockOfTextAsErrors(msg, event);
                } else if (!(t instanceof ThreadDeath) || event.getSession().getVerbosity() >= AntEvent.LOG_VERBOSE) {
                    // ThreadDeath can be thrown when killing an Ant process, so don't print it normally
                    deliverStackTrace(t, event);
                }
            }
            event.getSession().println(formatMessageWithTime("FMT_target_failed_printed", time), true, null); // #10305
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_target_failed_status", event.getSession().getDisplayName()));
        }
        event.consume();
    }
    
    /** Formats the millis in a human readable String.
     * Total time: {0} minutes
     *             {1} seconds
     */
    private static String formatMessageWithTime(String key, long millis) {
        int secs = (int) (millis / 1000);
        int minutes = secs / 60;
        int seconds = secs % 60;
        return NbBundle.getMessage(StandardLogger.class, key, new Integer(minutes), new Integer(seconds));
    }
    
    public void targetStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        // XXX this could start indenting messages, perhaps
        String name = event.getTargetName();
        if (name != null) {
            // Avoid printing internal targets normally:
            int minlevel = (name.length() > 0 && name.charAt(0) == '-') ? AntEvent.LOG_VERBOSE : AntEvent.LOG_INFO;
            if (event.getSession().getVerbosity() >= minlevel) {
                event.getSession().println(NbBundle.getMessage(StandardLogger.class, "MSG_target_started_printed", name), false, null);
            }
        }
        event.consume();
    }
    
    public void messageLogged(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        AntSession session = event.getSession();
        String line = event.getMessage();
        if (LOGGABLE) ERR.log("Received message: " + line);
        Matcher m = CARET_SHOWING_COLUMN.matcher(line);
        if (m.matches()) {
            // #37358: adjust the column number of the last hyperlink accordingly.
            if (LOGGABLE) ERR.log("Looks like a special caret line");
            SessionData data = getSessionData(session);
            if (data.lastHyperlink != null) {
                // For "  ^", infer a column number of 3.
                data.lastHyperlink.setColumn1(m.group(1).length() + 1);
                data.lastHyperlink = null;
                // Don't print the actual caret line, just noise.
                event.consume();
                return;
            }
        }
        OutputListener hyperlink = findHyperlink(session, line);
        if (hyperlink instanceof Hyperlink) {
            getSessionData(session).lastHyperlink = (Hyperlink) hyperlink;
        }
        // XXX should translate tabs to spaces here as a safety measure
        event.getSession().println(line, event.getLogLevel() <= AntEvent.LOG_WARN, hyperlink);
        event.consume();
    }
    
    public void taskFinished(AntEvent event) {
        // Do not consider hyperlinks from previous tasks.
        getSessionData(event.getSession()).lastHyperlink = null;
    }

    /**
     * Possibly hyperlink a message logged event.
     */
    private static OutputListener findHyperlink(AntSession session, String line) {
        // #29246: handle new (Ant 1.5.1) URLifications:
        // [PENDING] Under JDK 1.4, could use new File(URI)... if Ant uses URI too (Jakarta BZ #8031)
        // XXX so tweak that for Ant 1.6 support!
        // XXX would be much easier to use a regexp here
        if (line.startsWith("file:///")) { // NOI18N
            line = line.substring(7);
            if (LOGGABLE) ERR.log("removing file:///");
        } else if (line.startsWith("file:")) { // NOI18N
            line = line.substring(5);
            if (LOGGABLE) ERR.log("removing file:");
        } else if (line.length() > 0 && line.charAt(0) == '/') {
            if (LOGGABLE) ERR.log("result: looks like Unix file");
        } else if (line.length() > 2 && line.charAt(1) == ':' && line.charAt(2) == '\\') {
            if (LOGGABLE) ERR.log("result: looks like Windows file");
        } else {
            // not a file -> nothing to parse
            if (LOGGABLE) ERR.log("result: not a file");
            return null;
        }
        
        int colon1 = line.indexOf(':');
        if (colon1 == -1) {
            if (LOGGABLE) ERR.log("result: no colon found");
            return null;
        }
        String fileName = line.substring (0, colon1); //.replace(File.separatorChar, '/');
        File file = FileUtil.normalizeFile(new File(fileName));
        if (!file.exists()) {
            if (LOGGABLE) ERR.log("result: no FO for " + fileName);
            // maybe we are on Windows and filename is "c:\temp\file.java:25"
            // try to do the same for the second colon
            colon1 = line.indexOf (':', colon1+1);
            if (colon1 == -1) {
                if (LOGGABLE) ERR.log("result: no second colon found");
                return null;
            }
            fileName = line.substring (0, colon1);
            file = FileUtil.normalizeFile(new File(fileName));
            if (!file.exists()) {
                if (LOGGABLE) ERR.log("result: no FO for " + fileName);
                return null;
            }
        }

        int line1 = -1, col1 = -1, line2 = -1, col2 = -1;
        int start = colon1 + 1; // start of message
        int colon2 = line.indexOf (':', colon1 + 1);
        if (colon2 != -1) {
            try {
                line1 = Integer.parseInt (line.substring (colon1 + 1, colon2).trim ());
                start = colon2 + 1;
                int colon3 = line.indexOf (':', colon2 + 1);
                if (colon3 != -1) {
                    col1 = Integer.parseInt (line.substring (colon2 + 1, colon3).trim ());
                    start = colon3 + 1;
                    int colon4 = line.indexOf (':', colon3 + 1);
                    if (colon4 != -1) {
                        line2 = Integer.parseInt (line.substring (colon3 + 1, colon4).trim ());
                        start = colon4 + 1;
                        int colon5 = line.indexOf (':', colon4 + 1);
                        if (colon5 != -1) {
                            col2 = Integer.parseInt (line.substring (colon4 + 1, colon5).trim ());
                            if (col2 == col1)
                                col2 = -1;
                            start = colon5 + 1;
                        }
                    }
                }
            } catch (NumberFormatException nfe) {
                // Fine, rest is part of the message.
            }
        }
        String message = line.substring (start).trim ();
        if (message.length () == 0) {
            message = null;
        }
        if (LOGGABLE) ERR.log("Hyperlink: [" + file + "," + line1 + "," + col1 + "," + line2 + "," + col2 + "," + message + "]");

        try {
            return session.createStandardHyperlink(file.toURI().toURL(), message, line1, col1, line2, col2);
        } catch (MalformedURLException e) {
            assert false : e;
            return null;
        }
    }

}
