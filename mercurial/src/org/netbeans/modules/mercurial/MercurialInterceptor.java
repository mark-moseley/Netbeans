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
package org.netbeans.modules.mercurial;

import javax.swing.SwingUtilities;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.mercurial.HgException;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.Calendar;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.api.queries.SharabilityQuery;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Listens on file system changes and reacts appropriately, mainly refreshing affected files' status.
 * 
 * @author Maros Sandor
 */
public class MercurialInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private ConcurrentHashMap<File, File> dirsToDelete = new ConcurrentHashMap<File,File>(); 

    private ConcurrentLinkedQueue<File> filesToRefresh = new ConcurrentLinkedQueue<File>();

    private RequestProcessor.Task refreshTask;

    private static final RequestProcessor rp = new RequestProcessor("MercurialRefresh", 1, true);

    public MercurialInterceptor() {
        cache = Mercurial.getInstance().getFileStatusCache();
        refreshTask = rp.create(new RefreshTask());
    }

    public boolean beforeDelete(File file) {
        Mercurial.LOG.fine("beforeDelete " + file);
        if (file == null) return true;
        if (HgUtils.isPartOfMercurialMetadata(file)) return false;
        
        // We track the deletion of top level directories
        if (file.isDirectory()) {
            for (File dir : dirsToDelete.keySet()) {
                if (file.equals(dir.getParentFile())) {
                    dirsToDelete.remove(dir);
                }
            }
            if (SharabilityQuery.getSharability(file) != SharabilityQuery.NOT_SHARABLE) {
                dirsToDelete.put(file, file);
            }
        }
        return true;
    }

    public void doDelete(File file) throws IOException {
        Mercurial.LOG.fine("doDelete " + file);
        return;
    }

    public void afterDelete(final File file) {
        Mercurial.LOG.fine("afterDelete " + file);
        Utils.post(new Runnable() {
            public void run() {
                fileDeletedImpl(file);
            }
        });
    }
    
    private void fileDeletedImpl(final File file) {
        if (file == null) return;
        Mercurial hg = Mercurial.getInstance();
        final File root = hg.getTopmostManagedParent(file);
        RequestProcessor rp = null;
        if (root != null) {
            rp = hg.getRequestProcessor(root.getAbsolutePath());
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                file.delete();
                if (!dirsToDelete.remove(file, file)) return;
                if (root == null) return;
                HgProgressSupport support = new HgProgressSupport() {
                    public void perform() {
                        try {
                            HgCommand.doRemove(root, file, this.getLogger());
                            // We need to cache the status of all deleted files
                            Map<File, FileInformation> interestingFiles = HgCommand.getInterestingStatus(root, file);
                            if (!interestingFiles.isEmpty()){
                                Collection<File> files = interestingFiles.keySet();

                                Map<File, Map<File,FileInformation>> interestingDirs =
                                        HgUtils.getInterestingDirs(interestingFiles, files);

                                Calendar start = Calendar.getInstance();
                                for (File tmpFile : files) {
                                    if(this.isCanceled()) {
                                        return;
                                    }
                                    FileInformation fi = interestingFiles.get(tmpFile);

                                    cache.refreshFileStatus(tmpFile, fi,
                                    interestingDirs.get(tmpFile.isDirectory()? tmpFile: tmpFile.getParentFile()), true);
                                }
                                Calendar end = Calendar.getInstance();
                            }
                        } catch (HgException ex) {
                            Mercurial.LOG.log(Level.FINE, "fileDeletedImpl(): File: {0} {1}", new Object[] {file.getAbsolutePath(), ex.toString()}); // NOI18N
                        }             
                    }
                };

                support.start(rp, root.getAbsolutePath(), 
                        org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Remove_Progress")); // NOI18N
            } else {
                Boolean isIgnored  = HgUtils.isIgnored(file, false);
                Mercurial.LOG.log(Level.FINE, "fileDeletedImpl(): File: {0} isIgnored {1}", new Object[] {file.getAbsolutePath(), isIgnored}); // NOI18N
                file.delete();
                if (root == null || isIgnored) return;
                // If we are deleting a parent directory of this file
                // skip the call to hg remove as we will do it for the directory
                for (File dir : dirsToDelete.keySet()) {
                    File tmpFile = file.getParentFile();
                    while (tmpFile != null) {
                        if (tmpFile.equals(dir)) return;
                        tmpFile = tmpFile.getParentFile();
                    }
                }
                HgProgressSupport support = new HgProgressSupport() {
                    public void perform() {
                        try {
                            HgCommand.doRemove(root, file, this.getLogger());
                            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        } catch (HgException ex) {
                            Mercurial.LOG.log(Level.FINE, "fileDeletedImpl(): File: {0} {1}", new Object[] {file.getAbsolutePath(), ex.toString()}); // NOI18N
                        }             
                    }
                };
                support.start(rp, root.getAbsolutePath(), 
                        org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Remove_Progress")); // NOI18N
            }
        }
    }

    public boolean beforeMove(File from, File to) {
        Mercurial.LOG.fine("beforeMove " + from + "->" + to);
        if (from == null || to == null || to.exists()) return true;
        
        Mercurial hg = Mercurial.getInstance();
        if (hg.isManaged(from)) {
            return hg.isManaged(to);
        }
        return super.beforeMove(from, to);
    }

    public void doMove(final File from, final File to) throws IOException {
        Mercurial.LOG.fine("doMove " + from + "->" + to);
        if (from == null || to == null || to.exists()) return;
        
        if (SwingUtilities.isEventDispatchThread()) {

            Mercurial.LOG.log(Level.INFO, "Warning: launching external process in AWT", new Exception().fillInStackTrace()); // NOI18N
            final Throwable innerT[] = new Throwable[1];
            Runnable outOfAwt = new Runnable() {
                public void run() {
                    try {
                        hgMoveImplementation(from, to);
                    } catch (Throwable t) {
                        innerT[0] = t;
                    }
                }
            };

            Mercurial.getInstance().getRequestProcessor().post(outOfAwt).waitFinished();
            if (innerT[0] != null) {
                if (innerT[0] instanceof IOException) {
                    throw (IOException) innerT[0];
                } else if (innerT[0] instanceof RuntimeException) {
                    throw (RuntimeException) innerT[0];
                } else if (innerT[0] instanceof Error) {
                    throw (Error) innerT[0];
                } else {
                    throw new IllegalStateException("Unexpected exception class: " + innerT[0]);  // NOI18N
                }
            }

            // end of hack

        } else {
            hgMoveImplementation(from, to);
        }
    }

    private void hgMoveImplementation(final File srcFile, final File dstFile) throws IOException {
        final Mercurial hg = Mercurial.getInstance();
        final File root = hg.getTopmostManagedParent(srcFile);
        final File dstRoot = hg.getTopmostManagedParent(dstFile);
        if (root == null) return;

        RequestProcessor rp = hg.getRequestProcessor(root.getAbsolutePath());

        Mercurial.LOG.log(Level.FINE, "hgMoveImplementation(): File: {0} {1}", new Object[] {srcFile, dstFile}); // NOI18N

        srcFile.renameTo(dstFile);
        Runnable moveImpl = new Runnable() {
            public void run() {
                OutputLogger logger = OutputLogger.getLogger(root.getAbsolutePath());
                try {
                    if (root.equals(dstRoot)) {
                        HgCommand.doRenameAfter(root, srcFile, dstFile, logger);
                    }
                } catch (HgException e) {
                    Mercurial.LOG.log(Level.FINE, "Mercurial failed to rename: File: {0} {1}", new Object[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()}); // NOI18N
                } finally {
                    logger.closeLog();
                }
            }
        };

        rp.post(moveImpl);
    }

    public void afterMove(final File from, final File to) {
        Mercurial.LOG.fine("afterMove " + from + "->" + to);
        Utils.post(new Runnable() {
            public void run() {
                fileMovedImpl(from, to);
            }
        });
    }

    private void fileMovedImpl(final File from, final File to) {
        if (from == null || to == null || !to.exists()) return;
        if (to.isDirectory()) return;
        
        reScheduleRefresh(100, from.getParentFile());
    }
    
    public boolean beforeCreate(final File file, boolean isDirectory) {
        Mercurial.LOG.fine("beforeCreate " + file + " " + isDirectory);
        if (HgUtils.isPartOfMercurialMetadata(file)) return false;
        if (!isDirectory && !file.exists()) {
            FileInformation info = cache.getCachedStatus(file, false);
            if (info != null && info.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                Mercurial.LOG.log(Level.FINE, "beforeCreate(): LocallyDeleted: {0}", file); // NOI18N
                Mercurial hg = Mercurial.getInstance();
                final File root = hg.getTopmostManagedParent(file);
                if (root == null) return false;
                final OutputLogger logger = Mercurial.getInstance().getLogger(root.getAbsolutePath());
                final Throwable innerT[] = new Throwable[1];
                Runnable outOfAwt = new Runnable() {
                    public void run() {
                        try {
                            List<File> revertFiles = new ArrayList<File>();
                            revertFiles.add(file);
                            HgCommand.doRevert(root, revertFiles, null, false, logger);
                        } catch (Throwable t) {
                            innerT[0] = t;
                        }
                    }
                };

                Mercurial.getInstance().getRequestProcessor().post(outOfAwt).waitFinished();
                if (innerT[0] != null) {
                    Mercurial.LOG.log(Level.FINE, "beforeCreate(): File: {0} {1}", new Object[] {file.getAbsolutePath(), innerT[0].toString()}); // NOI18N
                }
                Mercurial.LOG.log(Level.FINE, "beforeCreate(): afterWaitFinished: {0}", file); // NOI18N
                logger.closeLog();
                file.delete();
            }
        }
        return false;
    }

    public void doCreate(File file, boolean isDirectory) throws IOException {
        Mercurial.LOG.fine("doCreate " + file + " " + isDirectory);
        super.doCreate(file, isDirectory);
    }

    public void afterCreate(final File file) {
        Mercurial.LOG.fine("afterCreate " + file);
        Utils.post(new Runnable() {
            public void run() {
                fileCreatedImpl(file);
            }
        });
    }

    private void fileCreatedImpl(final File file) {
        if (file.isDirectory()) return;
        Mercurial.LOG.log(Level.FINE, "fileCreatedImpl {0}", file);

       // There is no point in refreshing the cache for ignored files.
       if (!HgUtils.isIgnored(file, false)) {
          reScheduleRefresh(100, file.getParentFile());
       }
    }
    
    public void afterChange(final File file) {
        Utils.post(new Runnable() {
            public void run() {
                fileChangedImpl(file);
            }
        });
    }

    private void fileChangedImpl(final File file) {
        if (file.isDirectory()) return;
        Mercurial.LOG.log(Level.FINE, "fileChangedImpl(): File: {0}", file); // NOI18N
        // There is no point in refreshing the cache for ignored files.
        if (!HgUtils.isIgnored(file, false)) {
            reScheduleRefresh(100, file.getParentFile());
        }
    }

    public Boolean isRefreshScheduled(File file) {
        return filesToRefresh.contains(file);
    }

    private void reScheduleRefresh(int delayMillis, File fileToRefresh) {
        if (!filesToRefresh.contains(fileToRefresh)) {
            if (!filesToRefresh.offer(fileToRefresh)) {
                Mercurial.LOG.log(Level.FINE, "reScheduleRefresh failed to add to filesToRefresh queue {0}", fileToRefresh);
            }
        }
        refreshTask.schedule(delayMillis);
    }

    private class RefreshTask implements Runnable {
        public void run() {
            Thread.interrupted();
            File fileToRefresh = filesToRefresh.poll();
            if (fileToRefresh != null) {
                Mercurial.LOG.log(Level.FINE, "RefreshTask called refreshAll {0}", fileToRefresh);
                cache.refreshAll(fileToRefresh);
                fileToRefresh = filesToRefresh.peek();
                if (fileToRefresh != null) {
                    refreshTask.schedule(0);
                }
            }
        }
    }
}
