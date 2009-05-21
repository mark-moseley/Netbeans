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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class ScpSyncWorker extends BaseSyncWorker implements RemoteSyncWorker {

    private Logger logger = Logger.getLogger("cnd.remote.logger"); // NOI18N
    private FileFilter sharabilityFilter;

    private int plainFilesCount;
    private int dirCount;
    private long totalSize;

    public ScpSyncWorker(File localDir, ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err) {
        super(localDir, executionEnvironment, out, err);
        sharabilityFilter = new SharabilityFilter();
    }

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

        // determine the remote directory
        RemotePathMap mapper = RemotePathMap.getRemotePathMapInstance(executionEnvironment);

        // probably mapper already knows it?
        String remoteDir = mapper.getRemotePath(this.localDir.getAbsolutePath(), false);
        if (remoteDir == null) {
            // mapper does not know dir; let's check its parent
            String localParent = this.localDir.getParentFile().getAbsolutePath();
            String remoteParent = mapper.getRemotePath(localParent, false);
            boolean addMapping = false;
            if (remoteParent == null) {
                // we can't map parent path either
                addMapping = true;
                remoteParent = getRemoteSyncRoot();
                if (remoteParent == null) {
                    if (mapper.checkRemotePath(localDir.getAbsolutePath(), true)) {
                        remoteDir = mapper.getRemotePath(this.localDir.getAbsolutePath(), false);
                        addMapping = false;
                    } else {
                        return false;
                    }
                }
            }
            if (remoteDir == null) {
                remoteDir = remoteParent + '/' + localDir.getName(); //NOI18N
            }
            if (addMapping) {
                mapper.addMapping(localParent, remoteParent);
            }
        }
        plainFilesCount = dirCount = 0;
        totalSize = 0;
        long time = System.currentTimeMillis();
        boolean success = false;
        try {
            synchronizeImpl(remoteDir);
            success = true;
        } catch (InterruptedException ex) {
            // reporting does not make sense, just return false
            logger.log(Level.FINEST, null, ex);
        } catch (InterruptedIOException ex) {
            // reporting does not make sense, just return false
            logger.log(Level.FINEST, null, ex);
        } catch (ExecutionException ex) {
            logger.log(Level.FINE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.FINE, null, ex);
        }
        time = System.currentTimeMillis() - time;
        logger.fine("Uploading " + plainFilesCount + " files in " + dirCount + " directories to "  //NOI18N
                + executionEnvironment + " took " + time + " ms. " + //NOI18N
                " Total size: " + (totalSize/1024) + "K. Success: " + success); //NOI18N
        return success;
    }

    /*package-local (for testing purposes, otherwise would be private) */
    void synchronizeImpl(String remoteDir) throws InterruptedException, ExecutionException, IOException {
        CommonTasksSupport.mkDir(executionEnvironment, remoteDir, err);
        dirCount++;
        File[] files = localDir.listFiles(sharabilityFilter);
        if (files == null) {
            throw new IOException("Failed to get children of " + localDir);
        } else {
            for (File file : files) {
                synchronizeImpl(file, remoteDir);
            }
        }
    }

    private void synchronizeImpl(File file, String remoteDir) throws InterruptedException, ExecutionException, IOException {        
        if (file.isDirectory()) {
            remoteDir += "/"  + file.getName(); // NOI18N
            // NOI18N
            Future<Integer> mkDir = CommonTasksSupport.mkDir(executionEnvironment, remoteDir, err);
            dirCount++;
            int rc = mkDir.get();
            if (rc != 0) {
                throw new IOException("creating directory " + remoteDir + " on " + executionEnvironment + // NOI18N
                        " finished with error code " + rc); // NOI18N
            }
            for (File child : file.listFiles(sharabilityFilter)) {
                synchronizeImpl(child, remoteDir);
            }
        } else {
            if (file.length() == 0) {
                // FIXUP for #164786 CommonTasksSupport.uploadFile fail to copy empty files
                return;
            }
            String localFile = file.getAbsolutePath();
            String remoteFile = remoteDir + '/' + file.getName(); //NOI18N
            Future<Integer> upload = CommonTasksSupport.uploadFile(localFile, executionEnvironment, remoteFile, 0777, err);
            int rc = upload.get();
            plainFilesCount++;
            totalSize += file.length();
            logger.finest("SCP: uploading " + localFile + " to " + remoteFile + " rc=" + rc); //NOI18N
            if (rc != 0) {
                throw new IOException("uploading " + localFile + " to " + remoteFile + // NOI18N
                        " finished with error code " + rc); // NOI18N
            }
        }
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
