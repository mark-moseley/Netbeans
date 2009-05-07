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
package org.netbeans.modules.nativeexecution.sps.impl;

import java.security.acl.NotOwnerException;
import java.util.List;
import java.util.concurrent.CancellationException;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport;
import org.netbeans.modules.nativeexecution.sps.impl.RequestPrivilegesTask.RequestPrivilegesTaskParams;
import org.netbeans.modules.nativeexecution.support.ObservableActionListener;
import org.netbeans.modules.nativeexecution.support.TasksCachedProcessor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public abstract class SPSCommonImpl implements SolarisPrivilegesSupport {

    private final static TasksCachedProcessor<ExecutionEnvironment, List<String>> cachedPrivilegesFetcher =
            new TasksCachedProcessor<ExecutionEnvironment, List<String>>(new FetchPrivilegesTask(), false);
    private final static TasksCachedProcessor<RequestPrivilegesTaskParams, Boolean> cachedPrivilegesRequestor =
            new TasksCachedProcessor<RequestPrivilegesTaskParams, Boolean>(new RequestPrivilegesTask(), true);
    private final ExecutionEnvironment execEnv;
    private volatile boolean cancelled = false;

    protected SPSCommonImpl(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    ExecutionEnvironment getExecEnv() {
        return execEnv;
    }

    abstract String getPID();

    public abstract void requestPrivileges(
            List<String> requestedPrivileges,
            String root, char[] passwd) throws NotOwnerException;

    public void requestPrivileges(
            final List<String> requestedPrivileges,
            boolean askForPassword) throws NotOwnerException {

        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("requestExecutionPrivileges " + // NOI18N
                    "should never be called in AWT thread"); // NOI18N
        }

        if (askForPassword && cancelled) {
            return;
        }

        if (hasPrivileges(requestedPrivileges)) {
            return;
        }

        try {
            if (cachedPrivilegesRequestor.compute(
                    new RequestPrivilegesTaskParams(this, requestedPrivileges, askForPassword)).booleanValue() == true) {
                invalidateCache();
            } else {
                throw new NotOwnerException();
            }
        } catch (CancellationException ex) {
            //cancelled = true;
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    // No synchronization, because cancelled is volatile
    public boolean isCanceled() {
        return cancelled;
    }

    public boolean hasPrivileges(
            final List<String> privs) {

        List<String> real_privs = getExecutionPrivileges();
        if (real_privs == null) {
            return false;
        }

        boolean status = true;
        for (String priv : privs) {
            if (!status) {
                break;
            }
            status &= real_privs.contains(priv);
        }

        return status;
    }

    public List<String> getExecutionPrivileges() {
        List<String> result = null;

        try {
            result = cachedPrivilegesFetcher.compute(execEnv);
        } catch (InterruptedException ex) {
        }

        return result;
    }

    /**
     * Provides action to request privileges.
     * The SAME action is returned for requesting same privileges.
     * @param requestedPrivileges
     * @param onPrivilegesGranted
     * @return
     */
    public AsynchronousAction getRequestPrivilegesAction(
            final List<String> requestedPrivileges,
            final Runnable onPrivilegesGranted) {
        final RequestPrivilegesAction action =
                RequestPrivilegesAction.getInstance(this, requestedPrivileges);

        if (onPrivilegesGranted != null) {
            action.addObservableActionListener(new ObservableActionListener<Boolean>() {

                public void actionStarted(Action source) {
                }

                public void actionCompleted(Action source, Boolean result) {
                    if (result != null && result.booleanValue() == true) {
                        onPrivilegesGranted.run();
                    }
                }
            });
        }

        return action;
    }

    private void invalidateCache() {
        cachedPrivilegesFetcher.remove(execEnv);
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(SPSCommonImpl.class, key, params);
    }
}
