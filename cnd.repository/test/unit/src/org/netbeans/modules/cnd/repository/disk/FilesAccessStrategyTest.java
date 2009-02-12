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
package org.netbeans.modules.cnd.repository.disk;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.test.TestObject;
import org.netbeans.modules.cnd.repository.test.TestObjectCreator;

/**
 * Test for FilesAccessStrategyImpl
 * @author Vladimir Kvashin
 */
public class FilesAccessStrategyTest extends ModelImplBaseTestCase {

    private static final boolean TRACE = false;

    static {
        //System.setProperty("cnd.repository.files.cache", "4");
        //System.setProperty("cnd.repository.mf.stat", "true");
        //System.setProperty("access.strategy.laps", "5");
        //System.setProperty("access.strategy.threads", "12");
        //System.setProperty("cnd.repository.trace.conflicts", "true");
    }
    private final FilesAccessStrategyImpl strategy = (FilesAccessStrategyImpl) FilesAccessStrategyImpl.getInstance();

    public FilesAccessStrategyTest(String testName) {
        super(testName);
    }

    public void testSignleThread() throws Exception {

        String dataPath = getDataDir().getAbsolutePath().replaceAll("repository", "modelimpl"); //NOI18N

        String unit = "FilesAccessStrategyTestUnit";

        Collection<TestObject> objects = new TestObjectCreator(unit, Key.Behavior.LargeAndMutable).createTestObjects(dataPath);

        Collection<TestObject> slice;
        slice = slice(objects, 10);
        write(slice);
        write(slice);
        readAndCompare(slice);
        strategy.printStatistics();
        if (strategy.getCacheSize() >= 10) {
            assertTrue("Write hit percentage should be ", strategy.getWriteHitPercentage() > 40);
            assertTrue("", strategy.getReadHitPercentage() > 90);
        }
        strategy.closeUnit(unit);
        assertNoExceptions();
    }
    private boolean proceed;
    private volatile int filled;
    private final Object barrier = new Object();

    private void waitBarrier() {
        synchronized (barrier) {
            try {
                barrier.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    private void notifyBarrier() {
        synchronized (barrier) {
            barrier.notifyAll();
        }
    }

    public void testMultyThread() throws Exception {

        String dataPath = getDataDir().getAbsolutePath().replaceAll("repository", "modelimpl"); //NOI18N

        String[] units = new String[]{
            "FilesAccessStrategyTestUnit1", "FilesAccessStrategyTestUnit2",
            "FilesAccessStrategyTestUnit3", "FilesAccessStrategyTestUnit4",
            "FilesAccessStrategyTestUnit5"};

        Collection<TestObject> objectsCollection = new ArrayList<TestObject>(2000);
        for (int i = 0; i < units.length; i++) {
            objectsCollection.addAll(new TestObjectCreator(units[i], Key.Behavior.LargeAndMutable).createTestObjects(dataPath));
        }

        final TestObject[] objects = objectsCollection.toArray(new TestObject[objectsCollection.size()]);

        int lapsCount = Integer.getInteger("access.strategy.laps", 20);
        int readingThreadCount = Integer.getInteger("access.strategy.threads", 6);

        if (TRACE) {
            System.out.printf("\n\ntestMultyThread: %d objects, %d laps, %d reading threads\n\n", objects.length, lapsCount, readingThreadCount);
        }

        Runnable r = new Runnable() {

            public void run() {
                if (TRACE) {
                    System.out.printf("%s waiting on barrier\n", Thread.currentThread().getName());
                }
                waitBarrier();
                if (TRACE) {
                    System.out.printf("%s working...\n", Thread.currentThread().getName());
                }
                while (proceed) {
                    try {
                        int i = random(0, filled);
                        Persistent read = strategy.read(objects[i].getKey());
                        assertEquals(objects[i], read);
                    } catch (Exception e) {
                        DiagnosticExceptoins.register(e);
                        break;
                    }
                }
                if (TRACE) {
                    System.out.printf("%s finished\n", Thread.currentThread().getName());
                }
            }
        };

        // starting reader threads
        proceed = true;
        for (int i = 0; i < readingThreadCount; i++) {
            Thread thread = new Thread(r);
            thread.setName("Reader thread " + i);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
        sleep(1000); // wait threads to start

        notifyBarrier();

        for (int lap = 0; lap < lapsCount; lap++) {
            if (TRACE) {
                System.out.printf("Writing objects: lap %d\n", lap);
            }
            for (int i = 0; i < objects.length; i++) {
                strategy.write(objects[i].getKey(), objects[i]);
                if (lap == 0) {
                    filled = i;
                    assertNotNull("Read shouldn't return null for an object that has been just written", strategy.read(objects[i].getKey()));
                }
            }
        }
        proceed = false;

        strategy.printStatistics();
        for (int i = 0; i < units.length; i++) {
            strategy.closeUnit(units[i]);
        }
        assertNoExceptions();
    }

    private int random(int from, int to) {
        return from + (int) ((double) (to - from - 1) * Math.random());
    }

    private void write(TestObject object) throws Exception {
        strategy.write(object.getKey(), object);
    }

    private void write(Collection<TestObject> objects) throws Exception {
        for (TestObject object : objects) {
            strategy.write(object.getKey(), object);
        }
    }

    private void readAndCompare(Collection<TestObject> objects) throws Exception {
        for (TestObject object : objects) {
            Persistent read = strategy.read(object.getKey());
            assertEquals(object, read);
        }
    }

    private Collection<TestObject> slice(Collection<TestObject> objects, int size) {
        Collection<TestObject> result = new ArrayList<TestObject>(size);
        int cnt = 0;
        for (TestObject object : objects) {
            result.add(object);
            if (cnt++ >= size) {
                break;
            }
        }
        return result;
    }
}
