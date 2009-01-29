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
package org.netbeans.modules.nativeexecution;

import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class NativeTaskTest {

    public NativeTaskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    static int count = 0;

    /**
     * Test of run method, of class NativeTask.
     */
    @Test
    public void testRun() {
        System.out.println("run");
//
//        final NativeTaskListener l = new NativeTaskListener() {
//
//            public void taskStarted(NativeTask task) {
//                System.out.println(task.getPID() + " - started");
//            }
//
//            public void taskFinished(NativeTask task, Integer result) {
//                System.out.println(task.getPID() + " - finished");
//            }
//
//            public void taskCancelled(NativeTask task, CancellationException cex) {
//                System.out.println(task.getPID() + " - cancelled");
//            }
//
//            public void taskError(NativeTask task, Throwable t) {
//                System.out.println(task.getPID() + " - error");
//            }
//        };
//
//        StringBuffer outBuffer = new StringBuffer();
//        final ExecutionEnvironment ee = new ExecutionEnvironment(null, null);
//
//        NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
//        nt.redirectOutTo(new StringBufferWriter(outBuffer));
//        nt.addListener(l);
//        nt.submit();
//        try {
//            nt.get();
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ExecutionException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        System.out.println(outBuffer.toString());
//
//
//        Thread[] ts = new Thread[10];
//        for (int i = 0; i < 10; i++) {
//            ts[i] = new Thread(new Runnable() {
//                public void run() {
//                    StringBuffer outBuffer = new StringBuffer();
////                    NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
//                    NativeTask nt = new NativeTask("/tmp/qq");
//                    nt.redirectOutTo(new StringBufferWriter(outBuffer));
//                    nt.addListener(l);
//                    nt.submit();
//                    try {
//                        nt.get();
//                    } catch (InterruptedException ex) {
//                        Exceptions.printStackTrace(ex);
//                    } catch (ExecutionException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//
//                    System.out.println("" + (++count) + ") " + nt.getPID() + " - " + outBuffer.toString());
//                }
//            });
//        }
//
//        for (int i = 0; i < 10; i++) {
//            ts[i].start();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            try {
//                ts[i].join();
//            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }

//        NativeTask instance = new NativeTask("");
//        instance.run();
        // TODO review the generated test code and remove the default call to fail.


        int tcount = 1;
        Thread[] threads = new Thread[tcount];
        for (int i = 0; i < tcount; i++) {
            threads[i] = new Thread(new Runnable() {

                public void run() {
                    final NativeTask task = new NativeTask("/bin/ls");
                    task.submit(true, false);
                    System.out.println("PID is " + task.getPID());

                    try {
                        System.out.println("Result: " + task.get());
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }

        for (int i = 0; i < tcount; i++) {
            threads[i].start();
        }

        for (int i = 0; i < tcount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }


//        fail("The test case is a prototype.");
    }
}