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

package org.netbeans.modules.cnd.repository.access;

import java.io.File;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.trace.*;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Test for accessing project while it is being closed
 * (iz #115491, http://www.netbeans.org/issues/show_bug.cgi?id=115491)
 * @author Vladimir Kvashin
 */
public class FindFileWhileClosingProject  extends RepositoryAccessTestBase {

    private final static boolean verbose;
    static {
        verbose = true; // Boolean.getBoolean("test.get.file.while.closing.project.verbose");
        if( verbose ) {
            System.setProperty("cnd.modelimpl.timing", "true");
            System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
            System.setProperty("cnd.repository.listener.trace", "true");
            System.setProperty("cnd.trace.close.project", "true");
	    System.setProperty("cnd.repository.workaround.nulldata", "true");
        }
    }

    public FindFileWhileClosingProject(String testName) {
	super(testName);
    }

    public void testGetFileWhileClosingProject() throws Exception {

	File projectRoot = getDataFile("quote_nosyshdr");
	
	int count = Integer.getInteger("test.get.file.while.closing.project.laps", 500);
	
	final TraceModelBase traceModel = new  TraceModelBase();
	traceModel.processArguments(projectRoot.getAbsolutePath());
	ModelImpl model = traceModel.getModel();
	
	List<String> files = new ArrayList<String>(traceModel.getFiles().size());
	for( File file : traceModel.getFiles() ) {
	    files.add(file.getAbsolutePath());
	}
	
	for (int i = 0; i < count; i++) {
	    
	    System.err.printf("%s: processing project %s. Pass %d \n", getBriefClassName(), projectRoot.getAbsolutePath(), i);
	    
	    final CsmProject project = traceModel.getProject();
	    project.waitParse();
	    Cancellable task = model.enqueueModelTask(new Runnable() {
		public void run() {
                    TraceModelBase.closeProject(project);
		}
	    }, "Closing Project " + i); //NOI18N
	    for( String path : files ) {
                try {
                    CsmFile csmFile = project.findFile(path);
                    if( verbose ) System.err.printf("\tfind %s -> %s \n", path, csmFile);
                }
                catch( Throwable e ) {
                    registerException(e);
                }
		assertNoExceptions();
	    }
	    if( task instanceof RequestProcessor.Task ) {
		if( verbose ) System.err.printf("Waiting util close task finishes...\n");
		((RequestProcessor.Task) task).waitFinished();
		if( verbose ) System.err.printf("\tClose task has finished.\n");
	    }
	    assertNoExceptions();
	    traceModel.resetProject();
	}
	assertNoExceptions();
    }
    
}
