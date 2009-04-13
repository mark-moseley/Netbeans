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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ak119685
 */
public class ConcurrentTasksSupport {
    final private static Logger log = Logger.getLogger(ConcurrentTasksSupport.class.getName());
    final int concurrentTasks;
    final ArrayList<TaskFactory> factories = new ArrayList<TaskFactory>();
    final CyclicBarrier startSignal;
    final CountDownLatch doneSignal;

    public ConcurrentTasksSupport(int concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
        startSignal = new CyclicBarrier(concurrentTasks + 1);
        doneSignal = new CountDownLatch(concurrentTasks);
    }

    void addFactory(TaskFactory taskFactory) {
        factories.add(taskFactory);
    }

    void init() {
        Thread[] threads = new Thread[concurrentTasks];
        final AtomicInteger counter = new AtomicInteger(0);
        final Random r = new Random();

        for (int i = 0; i < concurrentTasks; i++) {
            threads[i] = new Thread(new Runnable() {

                final int id = counter.incrementAndGet();

                public void run() {
                    try {
                        startSignal.await();
                    } catch (BrokenBarrierException ex) {
                        log.log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        log.info("InterruptedException while waiting startSignal");
                    }

                    TaskFactory factoryToUse = factories.get(r.nextInt(factories.size()));

                    try {
                        factoryToUse.newTask().run();
                    } catch (Throwable th) {
                        log.info("Exception in task " + th.toString());
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });

            threads[i].start();
        }
    }

    void start() {
        try {
            startSignal.await();
        } catch (InterruptedException ex) {
        } catch (BrokenBarrierException ex) {
        }

    }

    void waitCompletion() {
        try {
            doneSignal.await();
        } catch (InterruptedException ex) {
            log.info("InterruptedException while waiting doneSignal");
        }
    }

    public static interface TaskFactory {

        public Runnable newTask();
    }

    public static class Counters {
        private final ConcurrentMap<String, AtomicInteger> counters =
                new ConcurrentHashMap<String, AtomicInteger>();

        public AtomicInteger getCounter(String id) {
            AtomicInteger newCounter = new AtomicInteger(0);
            AtomicInteger existentCounter = counters.putIfAbsent(id, newCounter);

            if (existentCounter != null) {
                return existentCounter;
            } else {
                return newCounter;
            }
        }

        public void dump(PrintStream stream) {
            TreeMap<String, AtomicInteger> map = new TreeMap<String, AtomicInteger>(counters);

            for (String id : map.keySet()) {
                stream.println(id + ": " + map.get(id));
            }
        }
    }
}
