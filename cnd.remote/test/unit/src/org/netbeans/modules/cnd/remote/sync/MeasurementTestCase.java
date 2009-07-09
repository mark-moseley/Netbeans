/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.support.RemoteTestBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;

/**
 * Test for ScpSyncWorker
 * @author Vladimir Kvashin
 */
public class MeasurementTestCase extends RemoteTestBase {

    static {
        System.setProperty("cnd.remote.sync.zip", "true");
    }

    private final File srcDir;
    private final String displayNameBase;
    private static final boolean DEBUG = true;

    public MeasurementTestCase(String testName, ExecutionEnvironment execEnv, File testDirectory, String displayNameBase) {
        super(testName, execEnv);
        if (DEBUG)  {
            Logger.getLogger("cnd.remote.logger").setLevel(Level.FINEST);
            Logger.getLogger("nativeexecution.support.logger.level").setLevel(Level.FINEST);
        }
        this.srcDir = testDirectory;
        this.displayNameBase = displayNameBase;
    }

   
    private void do_test() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        String dst = getDestDir(execEnv);
        PrintWriter out = new PrintWriter(System.out);
        PrintWriter err = new PrintWriter(System.err);
        System.out.printf("testUploadFile: %s to %s:%s\n", srcDir.getAbsolutePath(), execEnv.getDisplayName(), dst);
        ZipSyncWorker worker = new ZipSyncWorker(srcDir, execEnv, out, err);
        worker.synchronizeImpl(dst);
        CommonTasksSupport.rmDir(execEnv, dst, true, err).get();
    }

    public void test_upload_first() throws Exception {
        do_test();
    }

    public void test_upload_changed() throws Exception {
        change();
        do_test();
    }

    private static final int MAX_SIZE = 1024 * 1024; // max size of files to be changed
    private static final int latch = 10; // we change each X-th file that is less or equal than MAX_SIZE bytes
    private int totalCount;
    private int changedCount;

    private void change() throws IOException {
        totalCount = 0;
        changedCount = 0;
        change(srcDir);
        if (DEBUG) {
            System.out.printf("%d of %d diles changed\n", changedCount, totalCount);
        }
    }

    private void change(File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                change(child);
            }
        } else {
            if (file.length() <= MAX_SIZE) {
                if ((totalCount++) % latch == 0) {
                    changedCount++;
                    if (DEBUG) { System.out.printf("Changing %s\n", file); }
                    char[] buffer = new char[(int) file.length()];
                    Reader reader = new FileReader(file);
                    int size = reader.read(buffer);
                    reader.close();
                    for (int i = 0; i < buffer.length; i++) {
                        if (Character.isLowerCase(buffer[i])) {
                            buffer[i] = Character.toUpperCase(buffer[i]);
                        } else if(Character.isUpperCase(buffer[i])) {
                            buffer[i] = Character.toLowerCase(buffer[i]);
                        }
                    }
                    Writer writer = new FileWriter(file);
                    writer.write(buffer, 0, size);
                    writer.close();
                }
            }
        }
    }

    private String getDestDir(ExecutionEnvironment execEnv) {
        return  "/tmp/" + execEnv.getUser() + "/sync-worker-measurements-test/" + Math.random() + "/";
    }

    @Override
    public String getName() {
        String name = displayNameBase + ' ' + srcDir.getName();
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env == null) {
            return name;
        } else {
            return String.format("%s [%s]", name, env);
        }
    }

    public static Test suite() throws IOException, FormatException {
        Collection<Test> tests = new ArrayList<Test>();
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        Collection<String> dirs = rcFile.getKeys("remote.zip.testdirs");
        Collection<String> mspecs = rcFile.getKeys(RemoteDevelopmentTest.PLATFORMS_SECTION);
        for (String mspec : mspecs) {
            ExecutionEnvironment env = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec);
            for (String dir : dirs) {
                File file = new File(dir);
                if (env == null) {
                    tests.add(TestSuite.warning("Can't get execution environment for " + mspec));
                } else if (!file.exists() || !file.canRead() || !file.isDirectory()) {
                    tests.add(TestSuite.warning("Can't read directory  " + file.getAbsolutePath()));
                } else {
                    tests.add(new MeasurementTestCase("test_upload_first", env, file, "First"));
                    tests.add(new MeasurementTestCase("test_upload_first", env, file, "First"));
                    tests.add(new MeasurementTestCase("test_upload_changed", env, file, "Changed"));
                }
            }
        }
        RemoteDevelopmentTest suite = new RemoteDevelopmentTest(MeasurementTestCase.class.getName(), tests);
        return suite;
    }
}

