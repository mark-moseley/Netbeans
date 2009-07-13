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
package org.netbeans.performance.scanning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import junit.framework.Test;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;

/**
 * 
 * @author Pavel Flaska
 */
public class ScanProjectPerfTest extends NbTestCase {

    private final List<PerformanceData> data;
    
    public ScanProjectPerfTest(String name) {
        super(name);
        data = new ArrayList<PerformanceData>();
    }
    
    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
    }

    public void testScanJEdit() throws IOException, ExecutionException, InterruptedException {
        String zipPath = Utilities.projectOpen("http://spbweb.russia.sun.com/~ok153203/jEdit41.zip", "jEdit41.zip");
        File zipFile = FileUtil.normalizeFile(new File(zipPath));
        Utilities.unzip(zipFile, getWorkDirPath());
        FileObject projectDir = Utilities.openProject("jEdit41", getWorkDir());
        scanProject(projectDir);
    }

    public void testScanNigelsFreeForm() throws IOException, ExecutionException, InterruptedException {
        String zipPath = Utilities.projectOpen("http://kim-sun.czech.sun.com/~pf124312/nigel.zip", "Clean.zip");
        File zipFile = FileUtil.normalizeFile(new File(zipPath));
        Utilities.unzip(zipFile, getWorkDirPath());
        FileObject projectDir = Utilities.openProject("clean/projects/ST", getWorkDir());
        scanProject(projectDir);
    }

    @org.junit.Test(timeout=Long.MAX_VALUE)
    public void testPhpProject() throws IOException, ExecutionException, InterruptedException {
        String zipPath = System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "mediawiki.zip";
        File f = new File(zipPath);
        if (!f.exists()) {
            zipPath = Utilities.projectOpen("http://wiki.netbeans.org/attach/FitnessViaSamples/mediawiki-1.14.0-nbproject.zip", "mediawiki.zip");
        }
        File zipFile = FileUtil.normalizeFile(new File(zipPath));
        Utilities.unzip(zipFile, getWorkDirPath());
        FileObject projectDir = Utilities.openProject("mediawiki-1.14.0", getWorkDir());
        scanProject(projectDir);
    }
    
    public void testOpenJdk7() throws IOException, ExecutionException, InterruptedException {
        String zipPath = System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "openjdk.zip";
        File f = new File(zipPath);
        if (!f.exists()) {
             zipPath = Utilities.projectOpen("http://kim-sun.czech.sun.com/~pf124312/openjdk-7-ea-src-b63-02_jul_2009.zip", "openjdk.zip");
        }
        File zipFile = FileUtil.normalizeFile(new File(zipPath));
        Utilities.unzip(zipFile, getWorkDirPath());
        FileObject projectDir = Utilities.openProject("openjdk/jdk/make/netbeans/world", getWorkDir());
        scanProject(projectDir);
    }

    private void scanProject(final FileObject projectDir) throws IOException, ExecutionException, InterruptedException{
        final String projectName = projectDir.getName();
        
        Logger repositoryUpdater = Logger.getLogger(RepositoryUpdater.class.getName());
        repositoryUpdater.setLevel(Level.INFO);
        repositoryUpdater.addHandler(new ScanningHandler(projectName));
        JavaSource src = JavaSource.create(ClasspathInfo.create(projectDir));

        src.runWhenScanFinished(new Task<CompilationController>() {
            
            @Override()
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
            }
        }, false).get();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        for (PerformanceData rec : getPerformanceData()) {
            Utilities.processUnitTestsResults(ScanProjectPerfTest.class.getCanonicalName(), rec);
        }
        data.clear();
    }

    
    public static Test suite() throws InterruptedException {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().
                addTest(ScanProjectPerfTest.class).
                clusters(".*").gui(false).
                enableModules("php.*", ".*"));
    }

    public PerformanceData[] getPerformanceData() {
        return data.toArray(new PerformanceData[0]);
    }

    private class ScanningHandler extends Handler {

        private final String projectName;

        public ScanningHandler(String projectName) {
            this.projectName = projectName;
        }
        
        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            if (message != null && message.startsWith("Complete indexing")) {
                if (message.contains("source roots")) {
                    PerformanceData res = new PerformanceData();
                    StringTokenizer tokenizer = new StringTokenizer(message, " ");
                    int count = tokenizer.countTokens();
                    res.name = projectName + " source scan";
                    for (int i = 0; i < count-2; i++) {
                        tokenizer.nextToken();
                    }
                    String token = tokenizer.nextToken();
                    res.value = Long.parseLong(token);
                    res.unit = "ms";
                    res.runOrder = 0;
                    data.add(res);
                } else if (message.contains("binary roots")) {
                    PerformanceData res = new PerformanceData();
                    StringTokenizer tokenizer = new StringTokenizer(message, " ");
                    int count = tokenizer.countTokens();
                    res.name = projectName + " binary scan";
                    for (int i = 0; i < count-2; i++) {
                        tokenizer.nextToken();
                    }
                    String token = tokenizer.nextToken();
                    res.value = Long.parseLong(token);
                    res.unit = "ms";
                    res.runOrder = 0;
                    data.add(res);
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
