package org.netbeans.modules.cnd.modelimpl.csm.core;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;
import org.netbeans.modules.cnd.repository.access.RepositoryAccessTestBase;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/**
 *
 * @author Vladimir Kvashin
 */
public class RepositoryFilesCacheStress extends RepositoryAccessTestBase {

    static {
	//System.setProperty("cnd.repository.listener.trace", "true");
    }
    
    public RepositoryFilesCacheStress(String testName) {
        super(testName);
    }

    private boolean runOtherThread;
    private int closeCnt;
    
    public void testRun() throws Exception {
	
	File projectRoot1 = getDataFile("quote_nosyshdr");
	File projectRoot2 = getDataFile("../org");
	
	int count = Integer.getInteger("caches.stress.laps", 1000);
	
	final TraceModelBase traceModel = new  TraceModelBase();
	traceModel.setUseSysPredefined(true);
	traceModel.processArguments(projectRoot1.getAbsolutePath(), projectRoot2.getAbsolutePath());
	//ModelImpl model = traceModel.getModel();
        
        final File tmpSrcFile = File.createTempFile("TempFile", ".cpp", getWorkDir());
        writeFile(tmpSrcFile, "int foo();");
        runOtherThread = true;
        closeCnt = 0;
        
        Runnable r = new Runnable() {
            public void run() {
                while( runOtherThread ) {
                    createAndCloseAxtraProject(traceModel, tmpSrcFile);
                    closeCnt++;
                    sleep(100);
                }
            }
        };
        new Thread(r).start();
	
        
	for (int i = 0; i < count; i++) {
	    System.err.printf("%s: processing project %s. Pass %d \n", getBriefClassName(), projectRoot1.getAbsolutePath(), i);
	    final CsmProject project = traceModel.getProject();
	    project.waitParse();
	    invalidateProjectFiles(project);
	    //traceModel.resetProject(i < count/2);
	    assertNoExceptions();
	}
        runOtherThread = false;
	assertNoExceptions();
        System.err.printf("\n\nDone. Main project was parsed %d times. Extra project was closed %d times\n", count, closeCnt);
    }
    
    private void invalidateProjectFiles(CsmProject project) {
	for(CsmFile file : project.getAllFiles() ) {
	    FileImpl impl = (FileImpl) file;
	    impl.stateChanged(false);
	    try {
		file.scheduleParsing(false);
		//sleep(500);
	    } catch ( InterruptedException e ) {}
	}
    }

    private void writeFile(File file, String text) throws IOException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(file));
        writer.append(text);
        writer.close();
    }
    
    private void createAndCloseAxtraProject(TraceModelBase traceModel, File tmpSrcFile) {
        ProjectBase project = createExtraProject(traceModel, Collections.singletonList(tmpSrcFile), "DummyProject2");
        project.waitParse();
        traceModel.getModel().disposeProject(project);
    }
            
    private ProjectBase createExtraProject(TraceModelBase traceModel, List<File> files, String name) {
	NativeProject nativeProject = NativeProjectProvider.createProject(name, files, 
		Collections.<String>emptyList(), Collections.<String>emptyList(), 
                Collections.<String>emptyList(), Collections.<String>emptyList(), true);
	ProjectBase result = traceModel.getModel().addProject(nativeProject, "DummyProject", true); // NOI18N
	return result;
    }   
}
