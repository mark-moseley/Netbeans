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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.apache.tools.ant.module.bridge.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.Redirector;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputWriter;

/**
 * Replacement for Ant's java task which directly sends I/O to the output without line buffering.
 * Idea from ide/projectimport/bluej/antsrc/org/netbeans/bluej/ant/task/BlueJava.java.
 * See issue #56341.
 */
public class ForkedJavaOverride extends Java {

    private static final RequestProcessor PROCESSOR = new RequestProcessor(ForkedJavaOverride.class.getName(), Integer.MAX_VALUE);

    // should be consistent with java.project.JavaAntLogger.STACK_TRACE
    private static final Pattern STACK_TRACE = Pattern.compile(
    "(?:\t|\\[catch\\] )at ((?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)*)[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$<][a-zA-Z0-9_$>]*\\(([a-zA-Z_$][a-zA-Z0-9_$]*\\.java):([0-9]+)\\)"); // NOI18N
    
    public ForkedJavaOverride() {
        redirector = new NbRedirector(this);
        super.setFork(true);
    }

    @Override
    public void setFork(boolean fork) {
        // #47465: ignore! Does not work to be set to false.
    }

    private class NbRedirector extends Redirector {

        private String outEncoding = System.getProperty("file.encoding"); // NOI18N
        private String errEncoding = System.getProperty("file.encoding"); // NOI18N

        public NbRedirector(Task task) {
            super(task);
        }

        public @Override ExecuteStreamHandler createHandler() throws BuildException {
            createStreams();
            return new NbOutputStreamHandler();
        }

        public @Override synchronized void setOutputEncoding(String outputEncoding) {
            outEncoding = outputEncoding;
            super.setOutputEncoding(outputEncoding);
        }

        public @Override synchronized void setErrorEncoding(String errorEncoding) {
            errEncoding = errorEncoding;
            super.setErrorEncoding(errorEncoding);
        }

        private class NbOutputStreamHandler implements ExecuteStreamHandler {

            private RequestProcessor.Task outTask;
            private RequestProcessor.Task errTask;
            //private RequestProcessor.Task inTask;

            //long init = System.currentTimeMillis();
            NbOutputStreamHandler() {}

            public void start() throws IOException {}

            public void stop() {
                /* XXX causes process to hang at end
                if (inTask != null) {
                    inTask.waitFinished();
                }
                */
                if (errTask != null) {
                    errTask.waitFinished();
                }
                if (outTask != null) {
                    outTask.waitFinished();
                }
            }

            public void setProcessOutputStream(InputStream inputStream) throws IOException {
                OutputStream os = getOutputStream();
                Integer logLevel = null;
                if (os == null || os instanceof LogOutputStream) {
                    os = AntBridge.delegateOutputStream(false);
                    logLevel = Project.MSG_INFO;
                }
                outTask = PROCESSOR.post(new Copier(inputStream, os, logLevel, outEncoding/*, init*/));
            }

            public void setProcessErrorStream(InputStream inputStream) throws IOException {
                OutputStream os = getErrorStream();
                Integer logLevel = null;
                if (os == null || os instanceof LogOutputStream) {
                    os = AntBridge.delegateOutputStream(true);
                    logLevel = Project.MSG_WARN;
                }
                errTask = PROCESSOR.post(new Copier(inputStream, os, logLevel, errEncoding/*, init*/));
            }

            public void setProcessInputStream(OutputStream outputStream) throws IOException {
                InputStream is = getInputStream();
                if (is == null) {
                    is = AntBridge.delegateInputStream();
                }
                /*inTask = */PROCESSOR.post(new Copier(is, outputStream, null, null/*, init*/));
            }

        }

    }

    private class Copier implements Runnable {

        private final InputStream in;
        private final OutputStream out;
        //final long init;
        private final Integer logLevel;
        private final String encoding;
        private final RequestProcessor.Task flusher;
        private final ByteArrayOutputStream currentLine;
        private OutputWriter ow = null;

        public Copier(InputStream in, OutputStream out, Integer logLevel, String encoding/*, long init*/) {
            this.in = in;
            this.out = out;
            this.logLevel = logLevel;
            this.encoding = encoding;
            //this.init = init;
            if (logLevel != null) {
                flusher = PROCESSOR.create(new Runnable() {
                    public void run() {
                        maybeFlush();
                    }
                });
                currentLine = new ByteArrayOutputStream();
            } else {
                flusher = null;
                currentLine = null;
            }
        }

        public void run() {
            /*
            StringBuilder content = new StringBuilder();
            long tick = System.currentTimeMillis();
            content.append(String.format("[init: %1.1fsec]", (tick - init) / 1000.0));
             */
            
            if (ow == null && logLevel != null) {
                Vector v = getProject().getBuildListeners();
                for (Object o : v) {
                    if (o instanceof NbBuildLogger) {
                        NbBuildLogger l = (NbBuildLogger) o;
                        ow = logLevel == Project.MSG_INFO ? l.out : l.err;
                        break;
                    }
                }
            }
            try {
                try {
                    int c;
                    while ((c = in.read()) != -1) {
                        if (logLevel == null) {
                            // Input gets sent immediately.
                            out.write(c);
                            out.flush();
                        } else {
                            synchronized (this) {
                                if (c == '\n') {
                                    String str = currentLine.toString(encoding);
                                    int len = str.length();
                                    if (len > 0 && str.charAt(len - 1) == '\r') {
                                        str = str.substring(0, len - 1);
                                    }
                                    // skip stack traces (hyperlinks are created by JavaAntLogger), everything else write directly
                                    if (!STACK_TRACE.matcher(str).matches()) {
                                        ow.println(str);
                                    }
                                    log(str, logLevel);
                                    currentLine.reset();
                                } else {
                                    currentLine.write(c);
                                    flusher.schedule(250);
                                }
                            }
                        }
                    }
                } finally {
                    if (logLevel != null) {
                        maybeFlush();
                    }
                }
            } catch (IOException x) {
                // ignore IOException: Broken pipe from FileOutputStream.writeBytes in BufferedOutputStream.flush
            } catch (ThreadDeath d) {
                // OK, build just stopped.
                return;
            }
            //System.err.println("copied " + in + " to " + out + "; content='" + content + "'");
        }

        private synchronized void maybeFlush() {
            try {
                currentLine.writeTo(out);
                out.flush();
                String str = currentLine.toString(encoding);
                ow.write(str);
            } catch (IOException x) {
                // probably safe to ignore
            } catch (ThreadDeath d) {
                // OK, build just stopped.
            }
            currentLine.reset();
        }

    }

}
