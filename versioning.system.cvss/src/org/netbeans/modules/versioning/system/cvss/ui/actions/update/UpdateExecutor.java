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

import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.event.*;
import org.openide.filesystems.*;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Executes a given 'update' command and refreshes file statuses.
 * 
 * @author Maros Sandor
 */
public class UpdateExecutor extends ExecutorSupport {
    
    /**
     * Contains all files that should NOT be set as up-to-date after Update finishes. 
     */ 
    private Set     refreshedFiles = Collections.synchronizedSet(new HashSet());
    private boolean rwUpdate;
    
    /**
     * Files modified afterwards will not be cosidered up-to-date even if server says so.
     */ 
    private long updateStartTimestamp;

    /**
     * Splits the original command into more commands if the original
     * command would execute on incompatible files.
     * See {@link #prepareBasicCommand(org.netbeans.lib.cvsclient.command.BasicCommand)}
     * for more information.
     *
     * @param cmd command to execute
     * @param cvs CVS engine to use
     * @param options global option for the command
     * @return array of executors that will execute the command (or array of splitted commands)
     */
    public static UpdateExecutor [] splitCommand(UpdateCommand cmd, CvsVersioningSystem cvs, GlobalOptions options) {
        Command [] cmds = new org.netbeans.lib.cvsclient.command.Command[0];
        if (cmd.getDisplayName() == null) cmd.setDisplayName(NbBundle.getMessage(UpdateExecutor.class, "MSG_UpdateExecutor_CmdDisplayName"));
        try {
            cmds = prepareBasicCommand(cmd);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        UpdateExecutor [] executors = new UpdateExecutor[cmds.length];
        for (int i = 0; i < cmds.length; i++) {
            Command command = cmds[i];
            executors[i] = new UpdateExecutor(cvs, (UpdateCommand) command, options);
        }
        return executors;
    }

    private UpdateExecutor(CvsVersioningSystem cvs, UpdateCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
        rwUpdate = options == null || !options.isDoNoChanges();
    }

    protected void setup() {
        super.setup();
        updateStartTimestamp = System.currentTimeMillis(); 
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        super.fileInfoGenerated(e);
    }
    
    /**
     * Refreshes statuse of relevant files after this command terminates.
     */ 
    protected void commandFinished(ClientRuntime.Result result) {
        
        UpdateCommand ucmd = (UpdateCommand) cmd;
        
        cvs.setParameter(CvsVersioningSystem.PARAM_BATCH_REFRESH_RUNNING, Boolean.TRUE);
        
        File [] files = ucmd.getFiles();
        
        for (int i = 0; i < files.length; i++) {
            cache.clearVirtualDirectoryContents(files[i], ucmd.isRecursive(), ucmd.getGlobalOptions().getExclusions());
        }
        
        Set filesystems = new HashSet(2);
        boolean hasConflict = false;
        for (Iterator i = toRefresh.iterator(); i.hasNext();) {
            DefaultFileInfoContainer info = (DefaultFileInfoContainer) i.next();
            File file = info.getFile();
            if (refreshedFiles.contains(file)) continue;
            int c = info.getType().charAt(0);
            if (c == 'P') c = 'U';                
            if (rwUpdate) {
                if (c == 'U') c = FileStatusCache.REPOSITORY_STATUS_UPTODATE;
                if (c == 'G') c = FileStatusCache.REPOSITORY_STATUS_MODIFIED;
                if (c == 'C') hasConflict = true;
            }
            cache.refreshCached(file, c);
            refreshedFiles.add(file);
        }
                
        // refresh all command roots
        // assuming that command roots and updated files all belong to the same filesystem
        for (int i = 0; i < files.length; i++) {
            if (ucmd.isRecursive()) {
                refreshRecursively(files[i]);
            } else {
                refreshFlat(files[i]);
            }
            addFileSystem(filesystems, files[i]);
            if (files[i].isFile()) {
                cache.refreshCached(files[i].getParentFile(), FileStatusCache.REPOSITORY_STATUS_UNKNOWN);                
            }
        }
        
        cvs.setParameter(CvsVersioningSystem.PARAM_BATCH_REFRESH_RUNNING, null);
        if (hasConflict) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(UpdateExecutor.class, "MSG_UpdateGeneratedConflicts_Prompt"), 
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        
        for (Iterator i = filesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = (FileSystem) i.next();
            try {
                CvsVersioningSystem.ignoreFilesystemEvents(true);
                fileSystem.refresh(true); // fires fileChanged
            } finally {
                CvsVersioningSystem.ignoreFilesystemEvents(false);
            }
        }
        
        if (ucmd.getUpdateByRevision() != null || ucmd.isResetStickyOnes()) {
            FileStatusProvider.getInstance().refreshAllAnnotations(false, true);
        }
    }

    private void addFileSystem(Set filesystems, File file) {
        FileObject fo;
        for (;;) {
            fo = FileUtil.toFileObject(file);
            if (fo != null) break;
            file = file.getParentFile();
            if (file == null) return;
        }
        try {
            filesystems.add(fo.getFileSystem());
        } catch (FileStateInvalidException e) {
            // ignore invalid filesystems
        }
    }

    private void refreshRecursively(File file) {
        try {
            if (cvs.isIgnoredFilename(file)) return;
            if (cmd.getGlobalOptions().isExcluded(file)) return;
            if (file.isDirectory()) {
                if (cache.getStatus(file).getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) return;
                File [] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    refreshRecursively(files[i]);
                }
                if (!refreshedFiles.contains(file)) cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            } else {
                if (!refreshedFiles.contains(file)) refreshFile(file);
            }
        } catch (Throwable e) {
            // we catch exceptions here because we want to refresh statuses of all files regardless of any errors below 
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void refreshFlat(File file) {
        if (cvs.isIgnoredFilename(file)) return;
        if (cmd.getGlobalOptions().isExcluded(file)) return;
        if (refreshedFiles.contains(file)) return;
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (cvs.isIgnoredFilename(files[i])) return;
                if (refreshedFiles.contains(files[i])) return;
                if (files[i].isDirectory()) continue;
                refreshFile(files[i]);
            }
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        } else {
            refreshFile(file);
        }
    }

    private void refreshFile(File file) {
        long lastModified = file.lastModified();
        if (cache.getStatus(file.getParentFile()).getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE &&
                lastModified > 0 && lastModified < updateStartTimestamp) {
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UPTODATE);
        } else {
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);                
        }
    }
}
