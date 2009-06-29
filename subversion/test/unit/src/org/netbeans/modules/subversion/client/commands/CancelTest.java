/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.commands;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.client.AbstractCommandTest;
import org.netbeans.modules.subversion.client.SvnClient;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author tomas
 */
public class CancelTest extends AbstractCommandTest {

    public CancelTest(String testName) throws Exception {
        super(testName);
    }
            
    public void testCancel() throws Exception {                                                
        final SvnClient c = getNbClient();

        abstract class TimeFormatter {
            public String getCurrTime() {
                return Long.toString(System.currentTimeMillis());
            }
        }
        class CommandRunner extends TimeFormatter implements Runnable {
            public void run() {
                try {
                    System.out.println("CommandRunner.run() - started at    " + getCurrTime());
                    c.getInfo(getRepoUrl(), null, null);
                } catch (SVNClientException ex) {
                    System.out.println("CommandRunner.run() - exception thrown: " + ex.getClass() + ": " + ex.getMessage());
                } finally {
                    System.out.println("CommandRunner.run() - finished at   " + getCurrTime());
                }
            }
        } 
        class TaskInterrupter extends TimeFormatter implements Runnable {
            Boolean cancellationSuccess;
            TaskInterrupter() { }
            public void run() {
                try {
                    System.out.println("TaskInterrupter.run() - started at  " + getCurrTime());
                    c.cancel();
                    cancellationSuccess = Boolean.TRUE;
                } catch (Exception ex) {
                    System.out.println("TaskInterrupter.run() - exception thrown " + ex.getClass() + ": " + ex.getMessage());
                    Exceptions.printStackTrace(ex);
                    cancellationSuccess = Boolean.FALSE;
                } finally {
                    System.out.println("TaskInterrupter.run() - finished at " + getCurrTime());
                }
            }
        }

        Blocker blocker;

        final CommandRunner cmdRunner = new CommandRunner();
        final TaskInterrupter interrupter = new TaskInterrupter();

        Logger.getLogger("").addHandler(blocker = new Blocker("cli: process created"));

        RequestProcessor rp = new RequestProcessor("clitest", 2);
        rp.post(cmdRunner);
        boolean interrupted = rp.post(interrupter).waitFinished(60000);

        assertTrue(interrupted);
        assertTrue(interrupter.cancellationSuccess.booleanValue());
        assertTrue(blocker.destroyed);
    }            
    
    private class Blocker extends Handler {
        private final String msg;
        private boolean destroyed = false;
        public Blocker(String msg) {
            this.msg = msg;
        }
        @Override
        public void publish(LogRecord record) {
            if(record.getMessage().indexOf(msg) > -1) {
                while(true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            } else if(record.getMessage().indexOf("cli: Process destroyed") > -1) {
                destroyed = true;
            }
        }
        @Override
        public void flush() { }
        @Override
        public void close() throws SecurityException { }
    }
    
}
