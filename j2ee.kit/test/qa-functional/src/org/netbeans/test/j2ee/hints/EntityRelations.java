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
 * Software is Sun Micro//Systems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//Systems, Inc. All Rights Reserved.
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
package org.netbeans.test.j2ee.hints;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.editor.hints.AnnotationHolder;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jindrich Sedek
 */
public class EntityRelations extends J2eeTestCase {

    private String goldenFilePath;
    private Writer goldenWriter;
    private List<Fix> fixes;
    private List<ErrorDescription> problems;
    private File secondFile = null;
    private static boolean projectsOpened = false;
    private static final Logger LOG = Logger.getLogger(EntityRelations.class.getName());
    private boolean isEmty = false;

    /** Creates a new instance of EntityRelations */
    public EntityRelations(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(EntityRelations.class);
        addServerTests(Server.GLASSFISH, conf);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }


    @Override
    public void setUp() throws IOException {
        if (!projectsOpened && isRegistered(Server.ANY)) {
            for (File file : getProjectsDirs()) {
                JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 180000);
                openProjects(file.getAbsolutePath());
                resolveServer(file.getName());
            }
            projectsOpened = true;
        }
        System.out.println("########  " + getName() + "  #######");
    }

    private boolean generateGoldenFiles() {
        return false;
    }

    @Override
    protected void tearDown() throws IOException {
        if (isEmty){
            return;
        }
        if (generateGoldenFiles()) {
            if (goldenWriter != null) {
                goldenWriter.close();
            }
            fail("GENERATING GOLDEN FILES: " + goldenFilePath);
        } else {
            compareReferenceFiles();
        }
        EditorOperator.closeDiscardAll();
    }

    private File[] getProjectsDirs() {
        return new File[]{
            new File(getDataDir(), "projects/EntityHintsApp"),
            new File(getDataDir(), "projects/EntityHintsEJB")                    
        };
    }

    private EditorOperator openFile(String fileName) throws Exception {
        secondFile = new File(getDataDir(), fileName);
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(secondFile));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        return new EditorOperator(secondFile.getName()); // wait for opening
    }

    private void testEntityHintsBidirectional(int fixOrder) throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/B.java");
        openFile("projects/EntityHintsEJB/src/java/hints/A.java");
        hintTest(f, fixOrder, "Create", 12);
    }

    private void testEntityHintsUnidirectional(int fixOrder) throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/B.java");
        openFile("projects/EntityHintsEJB/src/java/hints/A.java");
        hintTest(f, fixOrder, null, 12);
    }

    public void testManyToManyBidirectional() throws Exception {
        testEntityHintsBidirectional(3);
    }

    public void testManyToManyBidirectional2() throws Exception {
        testEntityHintsBidirectional(4);
    }

    public void testManyToOneBidirectional() throws Exception {
        testEntityHintsBidirectional(5);
    }

    public void testManyToOneBidirectional2() throws Exception {
        testEntityHintsBidirectional(6);
    }

    public void testOneToManyBidirectional() throws Exception {
        testEntityHintsBidirectional(7);
    }

    public void testOneToOneBidirectional() throws Exception {
        testEntityHintsBidirectional(8);
    }

    public void testManyToOneUnidirectional() throws Exception {
        testEntityHintsUnidirectional(9);
    }

    public void testManyToOneUnidirectional2() throws Exception {
        testEntityHintsUnidirectional(10);
    }

    public void testOneToOneUnidirectional() throws Exception {
        testEntityHintsUnidirectional(11);
    }

    public void testAARelation() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 3, "Create", 6);
    }

    public void testAARelation2() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 4, "Create", 6);
    }

    public void testAARelation3() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 5, null, 6);
    }

    public void testCreateID() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/CreateID.java");
        hintTest(f, 0, "Create", 2);
    }

    public void testMakePublic() throws Exception {
        hintTest(new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/MakePublic.java"), 1, null, 2);
    }

    public void testDefaultConstructor() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/DefaultConstructor.java");
        hintTest(f, 0, null, 1);
    }

    @Override
    public void testEmpty() {
        isEmty = true;
    }

    ///@param size size is the expected size of fixes list length
    private void hintTest(File testedFile, int fixOrder, String captionDirToClose, int size) throws Exception {
        String result = null;
        try {
            LOG.fine("starting hint test");
            FileObject fileToTest = FileUtil.toFileObject(testedFile);
            DataObject dataToTest;
            dataToTest = DataObject.find(fileToTest);
            EditorCookie editorCookie = dataToTest.getCookie(EditorCookie.class);
            editorCookie.open();
            EditorOperator operator = new EditorOperator(testedFile.getName());
            assertNotNull(operator);
            String text = operator.getText();
            assertNotNull(text);
            assertFalse(text.length() == 0);
            waitHintsShown(fileToTest, size);
            for (ErrorDescription errorDescription : problems) {
                write(errorDescription.toString());
            }
            for (Fix fix : fixes) {
                write(fix.getText());
            }
            final Fix fix = fixes.get(fixOrder);
            if (fix == null) {
                System.out.println(fixOrder);
                System.out.println(fixes.size());
                assert (false);
            }
            RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    try {
                        fix.implement();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        fail("IMPLEMENT" + ex.toString());
                    }
                }
            });
            if (captionDirToClose != null){
                new NbDialogOperator(captionDirToClose).ok();
            }
            task.waitFinished(1000);
            int count = 0;
            while (!editorCookie.isModified()) {
                LOG.fine("wait for modifications :" + count);
                Thread.sleep(1000);
                if (++count == 10) {
                    throw new AssertionError("NO CODE EDITED");
                }
            }
            write("---------------------");
            result = operator.getText();
            assertFalse(text.equals(result));
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        } finally {
            write(result);
            if (secondFile != null) {
                write("----SECOND FILE-----");
                write(new EditorOperator(secondFile.getName()).getText());
            }
            EditorOperator.closeDiscardAll();
            Thread.sleep(1000);
        }
    }

    private void write(String str) {
        ref(str);
        if (generateGoldenFiles()) {
            try {
                if (goldenWriter == null) {
                    goldenFilePath = getGoldenFile().getPath().replace("work/sys", "qa-functional");
                    File gFile = new File(goldenFilePath);
                    gFile.createNewFile();
                    goldenWriter = new FileWriter(gFile);
                }
                goldenWriter.append(str + "\n");
                goldenWriter.flush();
            } catch (java.io.IOException exc) {
                exc.printStackTrace();
                fail("IMPOSSIBLE TO GENERATE GOLDENFILES");
            }
        }
    }

    private List<ErrorDescription> getProblems(FileObject fileToTest) {
        problems = AnnotationHolder.getInstance(fileToTest).getErrors();
        Collections.sort(problems, new Comparator<ErrorDescription>() {

            public int compare(ErrorDescription o1, ErrorDescription o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return problems;
    }

    private static class HintsHandler extends Handler {

        RequestProcessor.Task task;
        int delay;

        public HintsHandler(int delay, RequestProcessor.Task task) {
            this.task = task;
            this.delay = delay;
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("updateAnnotations")) {
                LOG.fine("rescheduling");
                task.schedule(delay);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    private void waitHintsShown(FileObject fileToTest, int size) {
        final int delay = 1000;
        int repeat = 20;
        final Object lock = new Object();
        Runnable posted = new Runnable() {

            public void run() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };
        final RequestProcessor.Task task = RequestProcessor.getDefault().create(posted);
        HintsHandler handl = new HintsHandler(delay, task);
        Logger logger = Logger.getLogger(AnnotationHolder.class.getName());
        logger.setLevel(Level.FINE);
        try {
            do {
                synchronized (lock) {
                    task.schedule(delay);
                    logger.addHandler(handl);
                    lock.wait(repeat * delay);
                }
            } while ((repeat-- > 0) && (getFixes(fileToTest).size() < size));
        } catch (InterruptedException intExc) {
            throw new JemmyException("REFRESH DID NOT FINISHED IN " + repeat * delay + " SECONDS", intExc);
        } finally {
            logger.removeHandler(handl);
        }
    }

    private List<Fix> getFixes(FileObject fileToTest) {
        fixes = new ArrayList<Fix>();
        for (ErrorDescription errorDescription : getProblems(fileToTest)) {
            fixes.addAll(errorDescription.getFixes().getFixes());
        }
        Collections.sort(fixes, new Comparator<Fix>() {

            public int compare(Fix o1, Fix o2) {
                return o1.getText().compareTo(o2.getText());
            }
        });
        return fixes;
    }
}
