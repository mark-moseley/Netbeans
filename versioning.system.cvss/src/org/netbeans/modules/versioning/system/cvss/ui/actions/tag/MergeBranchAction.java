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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.tag.RtagCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;

/**
 * Performs the CVS 'update -j' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class MergeBranchAction extends AbstractSystemAction {

    private static final int enabledForStatus = FileInformation.STATUS_VERSIONED_MERGE
                    | FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY 
                    | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY 
                    | FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY
                    | FileInformation.STATUS_VERSIONED_UPTODATE;
    
    protected String getBaseName() {
        return "CTL_MenuItem_MergeBranch";
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    public void performCvsAction(ActionEvent ev) {
        Context context = getContext();

        String title = MessageFormat.format(NbBundle.getBundle(MergeBranchAction.class).getString("CTL_MergeBranchDialog_Title"), 
                                         new Object[] { getContextDisplayName() });
        
        MergeBranchPanel settings = new MergeBranchPanel(context.getFiles());

        JButton merge = new JButton(NbBundle.getMessage(MergeBranchAction.class, "CTL_MergeBranchDialog_Action_Merge"));
        JButton cancel = new JButton(NbBundle.getMessage(MergeBranchAction.class, "CTL_MergeBranchDialog_Action_Cancel"));
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { merge, cancel },
                merge,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(MergeBranchAction.class),
                null);
        descriptor.setClosingOptions(null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != merge) return;

        settings.saveSettings();
       
        RequestProcessor.getDefault().post(new MergeBranchExecutor(context, settings));
    }
 
    /**
     * Runnable that executes actions specified in the MergeBranch settings panel.
     */ 
    private static class MergeBranchExecutor implements Runnable {

        private final Context context;
        private final MergeBranchPanel settings;
        private String temporaryTag;

        public MergeBranchExecutor(Context context, MergeBranchPanel settings) {
            this.context = context;
            this.settings = settings;
        }

        public void run() {
            if (settings.isTaggingAfterMerge()) {
                temporaryTag = settings.getAfterMergeTagName() + "_tempheadmarker";
                if (!tagHeadTemporary()) return;
                try {
                    if (!update()) return;
                    tag();
                } finally {
                    removeTagHeadTemporary();
                }
            } else {
                update();
            }
        }

        /**
         * If user requests the merge to start at a specific tag: 
         *     cvs update -j merge_tag -j my_branch
         * If user requests to merge everything from the common ancestor revision: 
         *     cvs update -j my_branch
         * Branch_tag may be HEAD if we merge from trunk.
         * 
         * @return true if update succeeds, false otherwise
         */ 
        private boolean update() {
            UpdateCommand cmd = new UpdateCommand();

            String branchName = settings.isMergingFromTrunk() ? "HEAD" : settings.getBranchName();
            String headTag = temporaryTag != null ? temporaryTag : branchName; 

            GlobalOptions options = new GlobalOptions();
            if (context.getExclusions().size() > 0) {
                options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
            }
            
            if (settings.isUsingMergeTag()) {
                cmd.setMergeRevision1(settings.getMergeTagName());
                cmd.setMergeRevision2(headTag);
            } else {
                cmd.setMergeRevision1(headTag);
            }
            cmd.setFiles(context.getRootFiles());
        
            UpdateExecutor [] executors = UpdateExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), options);
            return ExecutorSupport.wait(executors);
        }

        /**
         * Tags the head of branch we merged from:
         *     cvs rtag -F -r my_branch merge_tag module1 module2 ...
         * 
         * @return true if tagging succeeds, false otherwsie 
         */ 
        private boolean tag() {
            RtagCommand cmd = new RtagCommand();
        
            cmd.setOverrideExistingTag(true);
            cmd.setTagByRevision(temporaryTag);
            cmd.setTag(settings.getAfterMergeTagName());
        
            RTagExecutor [] executors = RTagExecutor.executeCommand(cmd, context.getFiles(), null);
            return ExecutorSupport.wait(executors);
        }

        /**
         * Places or removes a temporary tag at the head of the branch we merge from:
         *     cvs rtag -F -r my_branch temporary_tag module1 module2 ...
         * 
         * @return true if tagging succeeds, false otherwsie 
         */ 
        private boolean tagHeadTemporary() {
            RtagCommand cmd = new RtagCommand();
        
            cmd.setOverrideExistingTag(true);
            cmd.setTagByRevision(settings.isMergingFromTrunk() ? "HEAD" : settings.getBranchName());
            cmd.setTag(temporaryTag);

            RTagExecutor [] executors = RTagExecutor.executeCommand(cmd, context.getFiles(), null);
            return ExecutorSupport.wait(executors);
        }

        private boolean removeTagHeadTemporary() {
            RtagCommand cmd = new RtagCommand();
                    
            cmd.setDeleteTag(true);
            cmd.setTag(temporaryTag);
        
            RTagExecutor [] executors = RTagExecutor.executeCommand(cmd, context.getFiles(), null);
            return ExecutorSupport.wait(executors);
        }
    }
}
