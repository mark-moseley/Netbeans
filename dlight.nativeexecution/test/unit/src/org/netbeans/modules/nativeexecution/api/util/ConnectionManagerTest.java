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

package org.netbeans.modules.nativeexecution.api.util;

import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class ConnectionManagerTest {

    public ConnectionManagerTest() {
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


//    @Test
    public void testGetConnectToAction() {
        System.out.println("getConnectToAction");

        ExecutionEnvironment execEnv = new ExecutionEnvironment("ak119685", "129.159.127.94", 22);
        
        try {
            ConnectionManager.getInstance().connectTo(execEnv);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println(ConnectionManager.getInstance().isConnectedTo(execEnv));
    }

    @Test
    public void concurrentAccess() {
        System.out.println("Concurrent access");

        int threadsNum = 10;

        final CountDownLatch startFlag = new CountDownLatch(1);
        final CountDownLatch doneFlag = new CountDownLatch(threadsNum);
        Runnable onConnect = new Runnable() {

            public void run() {
                System.out.println("Perform on connect action!");
            }
        };

        ExecutionEnvironment execEnv = new ExecutionEnvironment("ak119685", "129.159.127.252", 22);

        for (int i = 0; i < threadsNum; i++) {
            final AsynchronousAction action = ConnectionManager.getInstance().getConnectToAction(execEnv, onConnect);
            new Thread(new Runnable() {

                public void run() {
                    try {
                        startFlag.await();
                        System.out.println("trying to connect..."); // NOI18N
                        try {
                            action.invoke();
                        } catch (Throwable ex) {
                            System.err.println("XXX: " + ex.toString());
                        }
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        doneFlag.countDown();
                    }
                }
            }).start();
        }

        startFlag.countDown();
        
        try {
            doneFlag.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }


    }

}