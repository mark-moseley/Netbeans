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
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;

/**
 * Performs the CVS 'tag -b' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class BranchAction extends AbstractSystemAction {

    private static final int enabledForStatus = FileInformation.STATUS_VERSIONED_MERGE
                    | FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY 
                    | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY 
                    | FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY
                    | FileInformation.STATUS_VERSIONED_UPTODATE;
    
    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_Branch";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    protected boolean asynchronous() {
        return false;
    }
    
    public void performCvsAction(Node[] nodes) {
        Context context = getContext(nodes);

        String title = MessageFormat.format(NbBundle.getBundle(BranchAction.class).getString("CTL_BranchDialog_Title"),
                                     new Object[] { getContextDisplayName(nodes) });

        JButton branch = new JButton(NbBundle.getMessage(BranchAction.class, "CTL_BranchDialog_Action_Branch"));
        branch.setToolTipText(NbBundle.getMessage(BranchAction.class,  "TT_BranchDialog_Action_Branch"));
        JButton cancel = new JButton(NbBundle.getMessage(BranchAction.class, "CTL_BranchDialog_Action_Cancel"));
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BranchAction.class,  "ACSD_BranchDialog_Action_Cancel"));
        BranchSettings settings = new BranchSettings(context.getFiles());
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { branch, cancel },
                branch,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(BranchAction.class),
                null);
        descriptor.setClosingOptions(null);

        settings.putClientProperty("org.openide.DialogDescriptor", descriptor);  // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BranchAction.class,  "ACSD_BranchDialog"));
        dialog.setVisible(true);
        if (descriptor.getValue() != branch) return;

        settings.saveSettings();

        RequestProcessor.getDefault().post(new BranchExecutor(context, settings, getRunningName(nodes)));
    }
    
    private static class BranchExecutor implements Runnable {

        private final Context context;
        private final BranchSettings settings;
        private final String name;

        public BranchExecutor(Context context, BranchSettings settings, String name) {
            this.context = context;
            this.settings = settings;
            this.name = name;
        }

        public void run() {
            ExecutorGroup group = new ExecutorGroup(name);
            if (settings.isTaggingBase()) {
                group.addExecutors(tag(context.getFiles(), settings.getBaseTagName()));
                group.addBarrier(null);
            }
            group.addExecutors(branch(context.getFiles(), settings.getBranchName()));
            group.addBarrier(null);
            if (settings.isCheckingOutBranch()) {
                group.addExecutors(update(context, settings.getBranchName()));
            }
            group.execute();
        }

        private UpdateExecutor[] update(Context context, String revision) {
            UpdateCommand cmd = new UpdateCommand();

            GlobalOptions options = CvsVersioningSystem.createGlobalOptions();
            if (context.getExclusions().size() > 0) {
                options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
            }
            cmd.setUpdateByRevision(revision);
            cmd.setFiles(context.getRootFiles());
        
            return UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options);
        }

        private ExecutorSupport[] branch(File[] roots, String branchName) {
            TagCommand cmd = new TagCommand();

            cmd.setMakeBranchTag(true);
            cmd.setFiles(roots);
            cmd.setTag(branchName);
        
            return TagExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null);
        }

        private ExecutorSupport[] tag(File[] roots, String tagName) {
            TagCommand cmd = new TagCommand();
        
            cmd.setFiles(roots);
            cmd.setTag(tagName);
        
            return TagExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null);
        }
    }
}
