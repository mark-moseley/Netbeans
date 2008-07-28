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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.ErrorManager;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Runnable that actually performs a command and stores
 * an exception it may throw and its isFinished state.
 *
 * @author Maros Sandor
 */
class CommandRunnable implements Runnable, Cancellable {

    private final Client        client;
    private final GlobalOptions options;
    private final Command       cmd;
    private Throwable           failure;
    
    private boolean             aborted;
    private ExecutorSupport     support;

    private static boolean      testRetry = Boolean.getBoolean("netbeans.debug.cvs.io.retry");  // NOI18N
    private static boolean      metricsAlreadyLogged;

    public CommandRunnable(Client client, GlobalOptions options, Command cmd, ExecutorSupport support) {
        this.client = client;
        this.options = options;
        this.cmd = cmd;
        this.support = support;
    }

    public void run() {
        synchronized(this) {
            if (isAborted()) {
                return;
            }
            support.commandStarted(this);
        }
        CounterRunnable counterUpdater = new CounterRunnable();
        RequestProcessor.Task counterTask = RequestProcessor.getDefault().create(counterUpdater);
        counterUpdater.initTask(counterTask);
        try {
            counterTask.schedule(500);
            if (testRetry && support.t9yRetryFlag == false) {
                support.t9yRetryFlag = true;
                String msg = "Testing retry logic. Retry attempt will be OK. (-Dnetbeans.debug.cvs.io.retry=true)"; // NOI18N
                throw new AuthenticationException(msg, msg);
            }
            if(!metricsAlreadyLogged) {
                Utils.logVCSClientEvent("CVS", "JAVALIB");
                metricsAlreadyLogged = true;
            }
            client.executeCommand(cmd, options);
        } catch (Throwable e) {
            failure = e;
        } finally {
            counterTask.cancel();
            try {
                client.getConnection().close();
            } catch (Throwable e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }

    public Throwable getFailure() {
        return failure;
    }
    
    /**
     * Cancelled?
     */
    public synchronized boolean isAborted() {
        return aborted;
    }

    public synchronized boolean cancel() {
        if (aborted) {
            return false;
        }
        aborted = true;
        client.abort();
        return true;
    }

    public String toString() {
        return "CommandRunnable command=" + cmd.getCVSCommand();  // NOI18N
    }

    /** Periodic task updating transmitted/received data counter. */
    private class CounterRunnable implements Runnable {

        private RequestProcessor.Task task;

        private long counter;

        public void run() {
            long current = client.getCounter();
            long delta = current - counter;
            counter = current;
            support.increaseDataCounter(delta);
            task.schedule(500);
        }

        void initTask(RequestProcessor.Task task) {
            this.task = task;
        }
    }
}
