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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 * @author vk155633
 */
public class ParserThread implements Runnable {
    private boolean stopped = false;

    public void stop() {
        this.stopped = true;
    }
    
    public void run() {
	if( TraceFlags.TRACE_PARSER_QUEUE ) trace("started"); // NOI18N
        ParserQueue queue = ParserQueue.instance();
        while( !stopped ) {
            if( TraceFlags.TRACE_PARSER_QUEUE ) trace("polling queue"); // NOI18N
            try {
                ParserQueue.Entry entry = queue.poll();
                if( entry == null ) {
                    if( TraceFlags.TRACE_PARSER_QUEUE ) trace("waiting"); // NOI18N
                    queue.waitReady();
                }
                else {
                    FileImpl file = entry.getFile();
                    if( TraceFlags.TRACE_PARSER_QUEUE ) {
                        trace("parsing started: " + entry.toString(TraceFlags.TRACE_PARSER_QUEUE_DETAILS)); // NOI18N
                    }
                    Diagnostic.StopWatch stw = (TraceFlags.TIMING_PARSE_PER_FILE_FLAT && ! file.isParsed()) ? new Diagnostic.StopWatch() : null;
                    APTPreprocHandler preprocHandler = null;
                    try {
                            APTPreprocHandler.State state = entry.getPreprocState();
                            if (state != null) {
                                // init from entry
                                preprocHandler = file.getProjectImpl().createEmptyPreprocHandler(file.getBuffer().getFile());
                                if( TraceFlags.TRACE_PARSER_QUEUE ) {
                                    System.err.println("before ensureParse on " + file.getAbsolutePath() + 
                                            ParserQueue.tracePreprocState(state)); 
                                }
                                preprocHandler.setState(state);
                            }
                            file.ensureParsed(preprocHandler);
                    }
                    catch( Throwable thr ) {
                        thr.printStackTrace(System.err);
                    }
                    finally {
                        if( stw != null ) stw.stopAndReport("parsing " + file.getAbsolutePath()); // NOI18N
			try {
                            queue.onFileParsingFinished(file, preprocHandler);
                            if( TraceFlags.TRACE_PARSER_QUEUE ) trace("parsing done: " + file.getAbsolutePath()); // NOI18N
			    Notificator.instance().flush();
			    if( TraceFlags.TRACE_PARSER_QUEUE ) trace("model event flushed"); // NOI18N
			} catch( Throwable thr ) {
			    thr.printStackTrace(System.err);
			}
                    }
                }
            } catch (InterruptedException ex) {
                if( TraceFlags.TRACE_PARSER_QUEUE ) trace("interrupted"); // NOI18N
                break;
            }
        }
	if( TraceFlags.TRACE_PARSER_QUEUE ) trace(stopped ? "stopped" : "finished"); // NOI18N
    }
    
    private void trace(String text) {
        System.err.println(Thread.currentThread().getName() + ": " + text);
    }
    
}
