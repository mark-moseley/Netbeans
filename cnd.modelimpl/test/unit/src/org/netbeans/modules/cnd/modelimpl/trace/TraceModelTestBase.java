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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.PrintStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public class TraceModelTestBase extends ModelImplBaseTestCase {

    private TestModelHelper helper;

    public TraceModelTestBase(String testName) {
        super(testName);
    }

    protected TraceModel getTraceModel() {
        assert helper != null;
        return helper.getTraceModel();
    }

    protected void performTest(String source) throws Exception {
        performTest(source, source + ".dat", source + ".err"); // NOI18N
    }

    protected final ProjectBase getProject() {
        return helper.getProject();
    }

    protected final CsmProject getCsmProject() {
        return helper.getProject();
    }

    protected final void resetProject() {
        helper.resetProject();
    }

    protected final CsmModel getModel() {
        return helper.getModel();
    }

    protected void preSetUp() throws Exception {
        // init flags needed for file model tests before creating TraceModel
    }

    protected void postSetUp() throws Exception {
        // init flags needed for file model tests
    }

    protected final void initParsedProject() throws Exception {
        File projectDir = getTestCaseDataDir();
        helper.initParsedProject(projectDir.getAbsolutePath());
    }

    protected final FileImpl getFileImpl(File file) {
        return helper.getProject().getFile(file);
    }

    protected final void reparseFile(CsmFile file) {
        if (file instanceof FileImpl) {
            ((FileImpl) file).stateChanged(true);
            try {
                ((FileImpl) file).scheduleParsing(true);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        preSetUp();
        super.setUp();
        super.clearWorkDir();
        helper = new TestModelHelper();
        assertNotNull("Model must be valid", getTraceModel().getModel()); // NOI18N
        postSetUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        helper.shutdown();
    }

    protected final void performModelTest(File testFile, PrintStream streamOut, PrintStream streamErr) throws Exception {
        performModelTest(new String[]{testFile.getAbsolutePath()}, streamOut, streamErr);
    }

    protected final void performModelTest(String[] args, PrintStream streamOut, PrintStream streamErr) throws Exception {
        getTraceModel().test(args, streamOut, streamErr);
    }

    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try {
            // redirect output and err
            System.setOut(streamOut);
            System.setErr(streamErr);
            performModelTest(args, streamOut, streamErr);
            postTest(args);
        } finally {
            // restore err and out
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }
    
    protected void postTest(String[] args) {
        
    }
            

    protected void performTest(String source, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        File testFile = getDataFile(source);
        performTest(new String[]{testFile.getAbsolutePath()}, goldenDataFileName, goldenErrFileName, params);
    }

    protected void performTest(String[] args, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        File workDir = getWorkDir();

        File output = new File(workDir, goldenDataFileName);
        PrintStream streamOut = new PrintStream(output);
        File error = goldenErrFileName == null ? null : new File(workDir, goldenErrFileName);
        PrintStream streamErr = goldenErrFileName == null ? null : new PrintStream(error);
        try {
            doTest(args, streamOut, streamErr, params);
        } finally {
            // restore err and out
            streamOut.close();
            if (streamErr != null) {
                streamErr.close();
            }
        }
        //System.out.println("finished testing " + testFile);
        boolean errTheSame = true;
        File goldenErrFile = null;
        File goldenErrFileCopy = null;
        // first of all check err, because if not failed (often) => dat diff will be created
        if (goldenErrFileName != null) {
            goldenErrFile = getGoldenFile(goldenErrFileName);
            if (CndCoreTestUtils.diff(error, goldenErrFile, null)) {
                errTheSame = false;
                // copy golden
                goldenErrFileCopy = new File(workDir, goldenErrFileName + ".golden");
                CndCoreTestUtils.copyToWorkDir(goldenErrFile, goldenErrFileCopy); // NOI18N
            }
        }

        boolean outTheSame = true;
        File goldenDataFile = getGoldenFile(goldenDataFileName);
        File goldenDataFileCopy = null;
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            outTheSame = false;
            // copy golden
            goldenDataFileCopy = new File(workDir, goldenDataFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenDataFileCopy); // NOI18N
        }
        if (outTheSame) {
            assertTrue("ERR Difference - check: diff " + error + " " + goldenErrFileCopy, errTheSame); // NOI18N
        } else if (errTheSame) {
            assertTrue("OUTPUT Difference - check: diff " + output + " " + goldenDataFileCopy, outTheSame); // NOI18N
        } else {
            assertTrue("OUTPUT and ERR are different, see content of folder " + workDir, false); // NOI18N
        }
    }
}