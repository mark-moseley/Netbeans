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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;

/**
 * A common base class for RemoteSyncWorker implementations
 * @author Vladimir Kvashin
 */
/*package-local*/ abstract class BaseSyncWorker implements RemoteSyncWorker {

    protected final File localDir;
    protected final ExecutionEnvironment executionEnvironment;
    protected final PrintWriter out;
    protected final PrintWriter err;
    protected final Logger logger = Logger.getLogger("cnd.remote.logger"); // NOI18N

    public BaseSyncWorker(File localDir, ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err) {
        this.localDir = localDir;
        this.executionEnvironment = executionEnvironment;
        this.out = out;
        this.err = err;
    }

    /**
     * Performs synchronization.
     * It is supposed that maping is found and the path exists;
     * it is also supposed that target direcotory isn't the same as source
     * (isn't just mapped)
     */
    protected abstract void synchronizeImpl(String remoteDir) throws InterruptedException, ExecutionException, IOException;

    protected String getRemoteSyncRoot() {
        String root;
        root = System.getProperty("cnd.remote.sync.root." + executionEnvironment.getHost()); //NOI18N
        if (root != null) {
            return root;
        }
        root = System.getProperty("cnd.remote.sync.root"); //NOI18N
        if (root != null) {
            return root;
        }
        String home = RemoteUtil.getHomeDirectory(executionEnvironment);
        return (home == null) ? null : home + "/.netbeans/remote"; // NOI18N
    }

    public boolean synchronize() {
        // Later we'll allow user to specify where to copy project files to
        String remoteParent = getRemoteSyncRoot();
        if (remoteParent == null) {
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Cant_find_sync_root", ServerList.get(executionEnvironment).toString()));
            }
            return false; // TODO:
        }
        String remoteDir = remoteParent + '/' + localDir.getName(); //NOI18N

        boolean success = false;
        try {
            boolean same;
            try {
                same = RemotePathMap.isTheSame(executionEnvironment, remoteDir, localDir);
            } catch (InterruptedException e) {
                return false;
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(executionEnvironment.getHost() + ":" + remoteDir + " and " + localDir.getAbsolutePath() + //NOI18N
                        (same ? " are same - skipping" : " arent same - copying")); //NOI18N
            }
            if (!same) {
                if (out != null) {
                    out.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Copying",
                            remoteDir, ServerList.get(executionEnvironment).toString()));
                }
                synchronizeImpl(remoteDir);
                RemotePathMap mapper = RemotePathMap.getRemotePathMapInstance(executionEnvironment);
                mapper.addMapping(localDir.getParentFile().getAbsolutePath(), remoteParent);
            }
            success = true;
        } catch (InterruptedException ex) {
            // reporting does not make sense, just return false
            logger.finest(ex.getMessage());
        } catch (InterruptedIOException ex) {
            // reporting does not make sense, just return false
            logger.finest(ex.getMessage());
        } catch (ExecutionException ex) {
            logger.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        } catch (IOException ex) {
            logger.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s asdasd \n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        }
        return success;
    }

}
