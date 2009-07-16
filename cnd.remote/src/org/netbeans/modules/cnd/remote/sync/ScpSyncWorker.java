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
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class ScpSyncWorker extends BaseSyncWorker implements RemoteSyncWorker {

    
    protected final FileFilter sharabilityFilter;

    private int plainFilesCount;
    private int dirCount;
    private long totalSize;

    public ScpSyncWorker(File localDir, ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, File privProjectStorageDir) {
        super(localDir, executionEnvironment, out, err, privProjectStorageDir);
        sharabilityFilter = new SharabilityFilter();
    }

    @Override
    protected void synchronizeImpl(String remoteDir) throws InterruptedException, ExecutionException, IOException {

        plainFilesCount = dirCount = 0;
        totalSize = 0;
        long time = 0;
        
        if (logger.isLoggable(Level.FINE)) {
            System.out.printf("Uploading %s to %s ...\n", localDir.getAbsolutePath(), executionEnvironment); // NOI18N
            time = System.currentTimeMillis();
        }

        boolean success = false;
        try {
            CommonTasksSupport.mkDir(executionEnvironment, remoteDir, err);
            dirCount++;
            File[] files = localDir.listFiles(sharabilityFilter);
            if (files == null) {
                throw new IOException("Failed to get children of " + localDir); // NOI18N
            } else {
                for (File file : files) {
                    synchronizeImpl(file, remoteDir);
                }
            }
            success = true;
        } finally {
            
        }

        if (logger.isLoggable(Level.FINE)) {
            time = System.currentTimeMillis() - time;
            long bps = totalSize * 1000L / time;
            String speed = (bps < 1024*8) ? (bps + " b/s") : ((bps/1024) + " Kb/s"); // NOI18N
            String size = (totalSize < 1024 ? (totalSize + " bytes") : ((totalSize/1024) + " K")); // NOI18N
            System.out.printf("Uploading %s in %d files in %d directories to %s took %d ms. %s. Avg. speed: %s\n", // NOI18N
                    size, plainFilesCount, dirCount, executionEnvironment, time, success ? "OK" : "FAILURE", speed); // NOI18N
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
                throw new IOException("creating directory " + executionEnvironment + ':' + remoteDir + // NOI18N
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
            logger.finest("SCP: uploading " + localFile + " to " + executionEnvironment + ':' + remoteFile + " rc=" + rc); //NOI18N
            if (rc != 0) {
                throw new IOException("uploading " + localFile + " to " + executionEnvironment + ':' + remoteFile + // NOI18N
                        " finished with error code " + rc); // NOI18N
            }
        }
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
