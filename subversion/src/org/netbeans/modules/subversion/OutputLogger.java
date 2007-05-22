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

package org.netbeans.modules.subversion;

import java.io.File;
import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class OutputLogger implements ISVNNotifyListener {

    private InputOutput log;
    private boolean ignoreCommand = false;
    private String repositoryRootString;
    private static final RequestProcessor rp = new RequestProcessor("SubversionOutput", 1);
    
    public static OutputLogger getLogger(SVNUrl repositoryRoot) {
        if (repositoryRoot != null) {
            return new OutputLogger(repositoryRoot);
        } else {
            return new NullLogger();
        }
    }
    
    private OutputLogger(SVNUrl repositoryRoot) {
        repositoryRootString = repositoryRoot.toString();
        log = IOProvider.getDefault().getIO(repositoryRootString, false);
    }

    private OutputLogger() {
    }
    
    public void logCommandLine(final String commandLine) {
        rp.post(new Runnable() {
            public void run() {                        
                logln(commandLine, false);
                log.getOut().flush();
            }
        });        
    }
    
    public void logCompleted(final String message) {
        rp.post(new Runnable() {
            public void run() {                
                logln(message, ignoreCommand);
                log.getOut().flush();
            }
        });        
    }
    
    public void logError(final String message) {
        rp.post(new Runnable() {
            public void run() {                
                logln(message, false);
                log.getOut().flush();
            }
        });            
    }
    
    public void logMessage(final String message) {
        rp.post(new Runnable() {
            public void run() {                
                logln(message, ignoreCommand);
                log.getOut().flush();
            }
        });
    }
    
    public void logRevision(long revision, String path) {
       // logln(" revision " + revision + ", path = '" + path + "'");
    }
    
    public void onNotify(File path, SVNNodeKind kind) {
        //logln(" file " + path + ", kind " + kind);
    }
    
    public void setCommand(final int command) {
        rp.post(new Runnable() {
            public void run() {        
                ignoreCommand = command == ISVNNotifyListener.Command.INFO ||
                                command == ISVNNotifyListener.Command.STATUS ||
                                command == ISVNNotifyListener.Command.ANNOTATE ||
                                command == ISVNNotifyListener.Command.LOG ||
                                command == ISVNNotifyListener.Command.LS;
            }
        });
    }
         
    public void closeLog() {
        rp.post(new Runnable() {
            public void run() {
                log.getOut().flush();
                log.getOut().close();        
            }
        });
    }

    public void flushLog() {
        rp.post(new Runnable() {
            public void run() {        
                log.getOut().flush();
            }
        });        
    }
    
    private void logln(String message, boolean ignore) {
        log(message + "\n", null, ignore); // NOI18N
    }
    
    private void log(String message, OutputListener hyperlinkListener, boolean ignore) {                
        if(ignore) {
            return;
        }
        if (log.isClosed()) {
            log = IOProvider.getDefault().getIO(repositoryRootString, false);
            try {
                // HACK (mystic logic) workaround, otherwise it writes to nowhere 
                log.getOut().reset();
            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
            //log.select();
        }
        if (hyperlinkListener != null) {
            try {
                log.getOut().println(message, hyperlinkListener);
            } catch (IOException e) {
                log.getOut().write(message);
            }
        } else {
            log.getOut().write(message);
        }        
    }      
    
    private static class NullLogger extends OutputLogger {

        public void logCommandLine(String commandLine) {
        }

        public void logCompleted(String message) {
        }

        public void logError(String message) {
        }

        public void logMessage(String message) {
        }

        public void logRevision(long revision, String path) {
        }

        public void onNotify(File path, SVNNodeKind kind) {
        }

        public void setCommand(int command) {
        }

        public void closeLog() {
        }

        public void flushLog() {
        }
    }

}
