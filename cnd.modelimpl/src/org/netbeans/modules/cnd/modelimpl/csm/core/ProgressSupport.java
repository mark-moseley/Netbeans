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

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.util.WeakList;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 * @author Alexander Simon
 */
public class ProgressSupport {
    private static ProgressSupport instance = new ProgressSupport();
    private WeakList<CsmProgressListener> progressListeners = new WeakList<CsmProgressListener>();
    
    /** Creates a new instance of ProgressSupport */
    private ProgressSupport() {
    }
    
    /*package-local*/ static ProgressSupport instance() {
        return instance;
    }
    
    /*package-local*/ void addProgressListener(CsmProgressListener listener) {
        progressListeners.add(listener);
    }
    
    /*package-local*/ void removeProgressListener(CsmProgressListener listener) {
        progressListeners.remove(listener);
    }
    
    /*package-local*/ Iterator<CsmProgressListener> getProgressListeners() {
        return progressListeners.iterator();
    }
   
    
    /*package-local*/ void fireFileInvalidated(FileImpl file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileInvalidated " + file.getAbsolutePath());
        for( CsmProgressListener listener : progressListeners ) {
	    try { // have to do this to not allow a listener to crush code model threads
		listener.fileInvalidated(file);
	    }
	    catch(Exception e) {
		e.printStackTrace(System.err);
	    }
        }
    }
    
    /*package-local*/ void fireFileParsingStarted(FileImpl file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileParsingStarted " + file.getAbsolutePath());
        for( CsmProgressListener listener : progressListeners ) {
	    try { // have to do this to not allow a listener to crush code model threads
		listener.fileParsingStarted(file);
	    } catch(Exception e) {
		e.printStackTrace(System.err);
	    }
        }
    }
    
    
    /*package-local*/ void fireFileParsingFinished(FileImpl file, APTPreprocHandler preprocHandler) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileParsingFinished " + file.getAbsolutePath());
        for( CsmProgressListener listener : progressListeners ) {
	    try { // have to do this to not allow a listener to crush code model threads
		listener.fileParsingFinished(file);
	    } catch(Exception e) {
		e.printStackTrace(System.err);
	    }
        }
    }
    
    /*package-local*/ void fireProjectParsingStarted(ProjectBase project) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireProjectParsingStarted " + project.getName());
        for( CsmProgressListener listener : progressListeners ) {
	    try { // have to do this to not allow a listener to crush code model threads
		listener.projectParsingStarted(project);
	    } catch(Exception e) {
		e.printStackTrace(System.err);
	    }
        }
    }
    
    /*package-local*/ void fireProjectParsingFinished(ProjectBase project) {
        for( CsmProgressListener listener : progressListeners ) {
	    try { // have to do this to not allow a listener to crush code model threads
		listener.projectParsingFinished(project);
	    } catch(Exception e) {
		e.printStackTrace(System.err);
	    }
        }
    }
    
    /*package-local*/ void fireProjectLoaded(ProjectBase project) {
        for( CsmProgressListener listener : progressListeners ) {
	    try { // have to do this to not allow a listener to crush code model threads
		listener.projectLoaded(project);
	    } catch(Exception e) {
		e.printStackTrace(System.err);
	    }
        }
    }
    
   /*package-local*/ void fireIdle() {
        for( CsmProgressListener listener : progressListeners ) {
	    try { // have to do this to not allow a listener to crush code model threads
		listener.parserIdle();
	    } catch(Exception e) {
		e.printStackTrace(System.err);
	    }
        }
    }
    
    /*package-local*/ void fireProjectFilesCounted(ProjectBase project, int cnt){
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireProjectFilesCounted " + project.getName() + ' ' + cnt);
        for( CsmProgressListener listener : progressListeners ) {
	    try { // have to do this to not allow a listener to crush code model threads
		listener.projectFilesCounted(project, cnt);
	    } catch(Exception e) {
		e.printStackTrace(System.err);
	    }
        }
    }
}
