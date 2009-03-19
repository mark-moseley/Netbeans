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

package org.netbeans.modules.cnd.remote.support;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author gordonp
 */
public abstract class RemoteConnectionSupport {

    protected final ExecutionEnvironment executionEnvironment;
    private int exit_status;
    private boolean cancelled = false;
    private boolean failed = false;
    private String failureReason;
    protected static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N

    public RemoteConnectionSupport(ExecutionEnvironment env) {
        this.executionEnvironment = env;
        exit_status = -1; // this is what JSch initializes it to...
        failureReason = "";
        log.finest("RCS<Init>: Starting " + getClass().getName() + " on " + executionEnvironment);

        if (!ConnectionManager.getInstance().isConnectedTo(executionEnvironment)) {
            RemoteUserInfo ui = RemoteUserInfo.getUserInfo(executionEnvironment, false);
            boolean retry = false;
            do {
                try {
                    ConnectionManager.getInstance().connectTo(env, ui.getPassword().toCharArray(), false);
                } catch (IOException ex) {
                    log.warning("RCS<Init>: Got JSchException [" + ex.getMessage() + "]");
                    String msg = ex.getMessage();
                    //TODO (execution): error processinf
                    if (msg.equals("Auth fail")) { // NOI18N
                        JButton btRetry = new JButton(NbBundle.getMessage(RemoteConnectionSupport.class, "BTN_Retry"));
                        NotifyDescriptor d = new NotifyDescriptor(
                                NbBundle.getMessage(RemoteConnectionSupport.class, "MSG_AuthFailedRetry"),
                                NbBundle.getMessage(RemoteConnectionSupport.class, "TITLE_AuthFailedRetryDialog"),
                                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                                new Object[] { btRetry, NotifyDescriptor.CANCEL_OPTION}, btRetry);
                        if (DialogDisplayer.getDefault().notify(d) == btRetry) {
                             retry = true;
                        } else {
                            failed = true;
                            failureReason = msg;
                        }
                    } else {
                        failed = true;
                        failureReason = msg;
                    }

                } catch (CancellationException ex) {
                    cancelled = true;
                }
            } while (retry);
            if (!ConnectionManager.getInstance().isConnectedTo(executionEnvironment)) {
                log.fine("RCS<Init>: Connection failed on " + executionEnvironment);
            }
        }
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    //TODO (execution): ???
    public int getExitStatus() {
//        return !cancelled && channel != null ? channel.getExitStatus() : -1; // JSch initializes exit status to -1
        return exit_status;
    }
    
    //TODO (execution): ???
    public boolean isCancelled() {
        return cancelled;
    }
    
    //TODO (execution): IMPLEMENT
    public String getFailureReason() {
        return failureReason;
    }
    
    //TODO (execution): IMPLEMENT
    public boolean isFailed() {
        return failed;
    }

    //TODO (execution): ???
    public boolean isFailedOrCancelled() {
        return failed || cancelled;
    }

    //TODO (execution): ???
    public void setFailed(String reason) {
        failed = true;
        failureReason = reason;
    }
    
    protected void setExitStatus(int exit_status) {
        this.exit_status = exit_status;
    }
    

    public String getUser() {
        return executionEnvironment.getUser();
    }

    public String getHost() {
        return executionEnvironment.getHost();
    }
}
