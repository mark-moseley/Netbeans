/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.update;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.file.FileUtils;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Revert modifications action.
 * 
 * @author Maros Sandor
 */
public class GetCleanAction extends AbstractSystemAction {

    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_GetClean";  // NOI18N
    }

    protected boolean enable(Node[] nodes) {
        return CvsVersioningSystem.getInstance().getFileTableModel(Utils.getCurrentContext(nodes), FileInformation.STATUS_LOCAL_CHANGE).getNodes().length > 0;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public void performCvsAction(final Node[] nodes) {
        if (!confirmed(null, null)) return;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                revertModifications(nodes);
            }
        });
    }

    private static boolean confirmed(File file, String revision) {
        String message;
        if (file == null || revision == null) {
            message = NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Prompt");
        } else {
            message = NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Prompt2", file.getName(), revision);
        }
        NotifyDescriptor descriptor = new NotifyDescriptor(
                message,
                NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Title"),
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                null,
                null
        );
        Object option = DialogDisplayer.getDefault().notify(descriptor);
        return option == NotifyDescriptor.YES_OPTION;
    }
    
    private void revertModifications(Node[] nodes) {
        ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Progress"));
        try {
            group.progress(NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_ProgressPrepare"));
            FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
            File [] files = cache.listFiles(getContext(nodes), FileInformation.STATUS_LOCAL_CHANGE & FileInformation.STATUS_IN_REPOSITORY);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                rollback(file, VersionsCache.REVISION_BASE, group);
            }
            for (int i = 0; i < files.length; i++) {
                refresh(files[i]);
            }
        } finally {
            group.executed();
        }

    }

    /**
     * Overwrites given file with its specified revision. Revision number and sticky information in Entries is NOT modified, 
     * only the content is overwritten.
     * 
     * @param file the file to overwrite
     * @param revision revision to get
     */ 
    public static void rollback(File file, String revision) {
        if (!confirmed(file, revision)) return;
        rollback(file, revision, null);
        refresh(file);
    }
    
    private static void refresh(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            fo.refresh();
        }
    }

    private static void rollback(File file, String revision, ExecutorGroup group) {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        AdminHandler ah = CvsVersioningSystem.getInstance().getAdminHandler();
        Entry entry = null;
        try {
            entry = ah.getEntry(file);
        } catch (IOException e) {
            // non-fatal, we have no entry for this file
        }
        if (entry == null) {
            // handling 'move away file.txt, it is in the way'
            file.delete();
            UpdateCommand cmd = new UpdateCommand();
            cmd.setFiles(new File [] { file });
            UpdateExecutor executor = UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null)[0];
            group.addExecutor(executor);
            executor.execute();
            return;
        }
        try {
            File cleanFile = VersionsCache.getInstance().getRemoteFile(file, revision, group);
            if (cleanFile != null) {
                // 'atomic' action  >>>
                backup(file);
                try {
                    CvsVersioningSystem.ignoreFilesystemEvents(true);
                    FileObject target;
                    if (file.exists() == false) {
                        File dir = file.getParentFile();
                        mkfolders(dir);
                        FileObject folder = FileUtil.toFileObject(dir);
                        target = folder.createData(file.getName());
                    } else {
                        target = FileUtil.toFileObject(file);
                    }
                    InputStream in = null;
                    OutputStream out = null;
                    FileLock lock = null;
                    try {
                        in = new FileInputStream(cleanFile);
                        lock = target.lock();
                        out = target.getOutputStream(lock);
                        copyStream(in, out);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException alreadyClosed) {
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException alreadyClosed) {
                            }
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }

                } finally {
                    CvsVersioningSystem.ignoreFilesystemEvents(false);
                }
                if (entry != null && entry.isUserFileToBeRemoved()) {
                    entry.setRevision(entry.getRevision().substring(1));
                    ah.setEntry(file, entry);
                }
                cache.refresh(file, revision == VersionsCache.REVISION_BASE ? FileStatusCache.REPOSITORY_STATUS_UPTODATE : FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                // 'atomic' action  <<<
            } else {
                // locally delete? NOt yet there seems to be bug in checkout -p
                ErrorManager.getDefault().log(ErrorManager.WARNING, "Unable to checkout " + file.getName()); // NOI18N
                cleanFile.getName(); // raise compatability NPE
            }
        } catch (Exception e) {
            if (e.getCause() instanceof InterruptedException) {
                // command aborted
            } else {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    /** Like mkdirs but but using openide filesystems (firing events) */
    private static void mkfolders(File file) throws IOException {
        if (file.isDirectory()) return;

        File parent = file.getParentFile();
        ArrayList dirs = new ArrayList();
        
        dirs.add(file);
        String path = file.getName();
        while (parent.isDirectory() == false) {
            dirs.add(0, parent);
            path = parent.getName() + "/" + path;  // NOI18N
            parent = parent.getParentFile();
        }

        FileObject fo = FileUtil.toFileObject(parent);
        FileUtil.createFolder(fo, path);

        Iterator it = dirs.iterator();
        while (it.hasNext()) {
            File next = (File) it.next();
            CvsVersioningSystem.getInstance().getStatusCache().refresh(next, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte [] buffer = new byte[4096];
        for (;;) {
            int n = in.read(buffer, 0, 4096);
            if (n < 0) return;
            out.write(buffer, 0, n);
        }
    }

    private static void backup(File file) {
        Entry entry = null;
        try {
            entry = CvsVersioningSystem.getInstance().getAdminHandler().getEntry(file);
            if (entry != null) {
                File backup = new File(file.getParentFile(), ".#" + file.getName() + "." + entry.getRevision());
                FileUtils.copyFile(file, backup);
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
