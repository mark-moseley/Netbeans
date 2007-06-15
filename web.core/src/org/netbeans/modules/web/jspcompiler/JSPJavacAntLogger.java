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

package org.netbeans.modules.web.jspcompiler;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.OutputListener;

/**
 * Ant logger which handles compilation of JSPs, both the JSP -> Java and 
 * the Java -> class compilation stages.
 * Specifically, handles hyperlinking of errors from JspC and from Javac run on 
 * classes generated from JSPs.
 * @author Petr Jiricka, Jesse Glick
 * @see "#42525"
 */
public final class JSPJavacAntLogger extends AntLogger {
    
//    private static PrintWriter debugwriter = null;
//    private static void debug(String s) {
//        if (debugwriter == null) {
//            try {
//                debugwriter = new PrintWriter(new java.io.FileWriter("/local/repo/trunk/nb_all/nbbuild/AntOutputParser.log")); // NOI18N
//            } catch (java.io.IOException ioe) {
//                return;
//            }
//        }
//        debugwriter.println(s);
//        debugwriter.flush();
//    }
    
    private static final Logger ERR = Logger.getLogger(JSPJavacAntLogger.class.getName());
    private static final boolean LOGGABLE = ERR.isLoggable(Level.FINE);
    
    /**
     * Regexp matching the compilation error from JspC. Sample message could look like this:
     * org.apache.jasper.JasperException: file:C:/project/AntParseTestProject2/build/web/index.jsp(6,0) Include action: Mandatory attribute page missing
     */
    private static final Pattern JSP_COMPILER_ERROR = Pattern.compile(
        "(.*)(org.apache.jasper.JasperException: file:)([^\\(]*)\\(([0-9]+),([0-9]+)\\)(.*)"); // NOI18N


    private static final String[] TASKS_OF_INTEREST = AntLogger.ALL_TASKS;
    
    private static final int[] LEVELS_OF_INTEREST = {
        //AntEvent.LOG_DEBUG, // XXX is this needed?
        //AntEvent.LOG_VERBOSE, // XXX is this needed?
        AntEvent.LOG_INFO, // XXX is this needed?
        AntEvent.LOG_WARN, // XXX is this needed?
        AntEvent.LOG_ERR, // XXX is this needed?
    };
    
    
    /** Default constructor for lookup. */
    public JSPJavacAntLogger() {}
    
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }
    
    public boolean interestedInScript(File script, AntSession session) {
        return true;
    }
    
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    public String[] interestedInTasks(AntSession session) {
        return TASKS_OF_INTEREST;
    }
    
    public int[] interestedInLogLevels(AntSession session) {
        // XXX could exclude those in [INFO..ERR] greater than session.verbosity
        return LEVELS_OF_INTEREST;
    }

    
    private static FileObject guessWebModuleOutputRoot(WebModule wm, FileObject fo) {
        /*
        File outputF = wm.getWebOutputRoot();
        if (outputF != null) {
            return FileUtil.toFileObject(outputF);
        }
        */
        FileObject potentialRoot = fo.getParent();
        while (potentialRoot != null) {
            if (potentialRoot.getFileObject("WEB-INF") != null) {
                return potentialRoot;
            }
            potentialRoot = potentialRoot.getParent();
        }
        return null;
    }
    
    public void messageLogged(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        AntSession session = event.getSession();
        String line = event.getMessage();
        OutputListener hyper = findHyperlink(session, line);
        // XXX should translate tabs to spaces here as a safety measure
        if (hyper != null) {
            event.getSession().println(line, event.getLogLevel() <= AntEvent.LOG_WARN, hyper);
            event.consume();
        }
    }

    /**
     * Possibly hyperlink a message logged event.
     */
    private static OutputListener findHyperlink(AntSession session, String line) {
        if (LOGGABLE) ERR.log(Level.FINE, "line: " + line);
        // #29246: handle new (Ant 1.5.1) URLifications:
        // [PENDING] Under JDK 1.4, could use new File(URI)... if Ant uses URI too (Jakarta BZ #8031)
        // XXX so tweak that for Ant 1.6 support!
        // XXX would be much easier to use a regexp here
        if (line.startsWith("file:///")) { // NOI18N
            line = line.substring(7);
            if (LOGGABLE) ERR.log(Level.FINE, "removing file:///");
        } else if (line.startsWith("file:")) { // NOI18N
            line = line.substring(5);
            if (LOGGABLE) ERR.log(Level.FINE, "removing file:");
        } else if (line.length() > 0 && line.charAt(0) == '/') {
            if (LOGGABLE) ERR.log(Level.FINE, "result: looks like Unix file");
        } else if (line.length() > 2 && line.charAt(1) == ':' && line.charAt(2) == '\\') {
            if (LOGGABLE) ERR.log(Level.FINE, "result: looks like Windows file");
        } else {
            // not a file -> nothing to parse
            if (LOGGABLE) ERR.log(Level.FINE, "result: not a file");
            return null;
        }
        
        int colon1 = line.indexOf(':');
        if (colon1 == -1) {
            if (LOGGABLE) ERR.log(Level.FINE, "result: no colon found");
            return null;
        }
        String fileName = line.substring (0, colon1); //.replace(File.separatorChar, '/');
        File file = FileUtil.normalizeFile(new File(fileName));
        if (!file.exists()) {
            if (LOGGABLE) ERR.log(Level.FINE, "result: no FO for " + fileName);
            // maybe we are on Windows and filename is "c:\temp\file.java:25"
            // try to do the same for the second colon
            colon1 = line.indexOf (':', colon1+1);
            if (colon1 == -1) {
                if (LOGGABLE) ERR.log(Level.FINE, "result: no second colon found");
                return null;
            }
            fileName = line.substring (0, colon1);
            file = FileUtil.normalizeFile(new File(fileName));
            if (!file.exists()) {
                if (LOGGABLE) ERR.log(Level.FINE, "result: no FO for " + fileName);
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
        if (LOGGABLE) ERR.log(Level.FINE, "Hyperlink: [" + file + "," + line1 + "," + col1 + "," + line2 + "," + col2 + "," + message + "]");

        File smapFile = getSMAPFileForFile(file);
        if (LOGGABLE) ERR.log(Level.FINE, "smapfile: [" + smapFile + "]");
        if ((smapFile != null) && (smapFile.exists())) {
            try {
                SmapResolver resolver = new SmapResolver(new SmapFileReader(smapFile));
                String jspName = resolver.getJspFileName(line1, col1);
                if (jspName == null) {
                    return null;
                }
                if (LOGGABLE) ERR.log(Level.FINE, "translate: [" + line1 + ", " + col1 + "]");
                int newRow = resolver.unmangle(line1, col1);
//debug ("translated to '" + jspName + ":" + newRow + "'");
                // some mappings may not exist, so try next or previous lines, too
                if (newRow == -1) {
                    newRow = resolver.unmangle(line1-1, col1);
                    jspName = resolver.getJspFileName(line1-1, col1);
                }
                if (newRow == -1) {
                    newRow = resolver.unmangle(line1+1, col1);
                    jspName = resolver.getJspFileName(line1+1, col1);
                }
                try {
                    WebModule wm = WebModule.getWebModule(FileUtil.toFileObject(file));
                    if (wm == null) {
                        return null;
                    }
                    FileObject jspFO = wm.getDocumentBase().getFileObject(jspName);
//debug ("jsp '" + jspFO + "'");
                    if (jspFO != null) {
                        return session.createStandardHyperlink(FileUtil.toFile(jspFO).toURI().toURL(), message, newRow, -1, -1, -1);
                    }
                    return null;
                } catch (MalformedURLException e) {
                    assert false : e;
                    return null;
                }
            }
            catch (IOException e) {
                ERR.log(Level.INFO, null, e);
                // PENDING
                return null;
            }
            catch (Exception e) {
                // PENDING - this catch clause should not be here, it's here only to
                // hide bugs in the SmapResolver library
                ERR.log(Level.INFO, null, e);
                return null;
            }
        } else {
            return null;
        }
        
    }
    
    private static final String JAVA_SUFFIX = ".java"; // NOI18N
    private static final String SMAP_SUFFIX = ".class.smap"; // NOI18N
    /** Returns a SMAP file corresponding to the given file, if exists.
     */
    public static File getSMAPFileForFile(File javaFile) {
        File f = FileUtil.normalizeFile(javaFile);
        File dir = f.getAbsoluteFile().getParentFile();
        String name = f.getName();
        if (!name.endsWith(JAVA_SUFFIX)) {
            return null;
        }
        name = name.substring(0, name.length() - JAVA_SUFFIX.length());
        File newFile = new File(dir, name + SMAP_SUFFIX);
        return newFile;
    }
    
}
