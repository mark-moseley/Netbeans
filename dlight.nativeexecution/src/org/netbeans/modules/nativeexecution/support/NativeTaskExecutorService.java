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
package org.netbeans.modules.nativeexecution.support;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.openide.util.RequestProcessor;

/**
 * Default implementation of tasks executor service.
 * Currently it just uses Executors.newCachedThreadPool() as a thread
 * pool for tasks execution threads.
 */
public class NativeTaskExecutorService {

    private static final String PREFIX = "NATIVEEXECUTOR: "; // NOI18N
    private static final Boolean NAMED_THREADS =
            Boolean.getBoolean("nativeexecutor.namedthreads"); // NOI18N
    private final static ExecutorService executorService = Executors.newCachedThreadPool();


    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                executorService.shutdown();
            }
        });
    }

    public static <T> Future<T> submit(final Callable<T> task, String name) {
        Future<T> result = null;

        if (NAMED_THREADS) {
            final RequestProcessor processor = new RequestProcessor(PREFIX + name, 1);
            final FutureTask<T> ftask = new FutureTask<T>(task);
            processor.post(ftask);
            result = ftask;
        } else {
            result = executorService.submit(task);
        }

        return result;
    }

    public static void submit(final Runnable task, String name) {
        if (NAMED_THREADS) {
            final RequestProcessor processor = new RequestProcessor(PREFIX + name, 1);
            processor.post(task);
        } else {
            executorService.submit(task);
        }
    }
}
