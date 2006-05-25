/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.update;

import java.io.File;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Reverts local changes.
 *
 * @author Petr Kuzel
 */
public class RevertModificationsAction extends ContextAction {
    
    /** Creates a new instance of RevertModificationsAction */
    public RevertModificationsAction() {        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Revert"; // NOI18N
    }

    protected void performContextAction(final Node[] nodes) {
        final Context ctx = getContext(nodes);
        final File root = ctx.getRootFiles()[0];
        final SVNUrl url = SvnUtils.getRepositoryRootUrl(root);
        final RepositoryFile repositoryFile = new RepositoryFile(url, url, SVNRevision.HEAD);
        
        final RevertModifications revertModifications = new RevertModifications(repositoryFile);
        if(!revertModifications.showDialog()) {
            return;
        }

        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {
                performRevert(ctx, revertModifications, this);
            }
        };            
        support.start(createRequestProcessor(nodes));
    }
        
    /** Recursive revert */
    public static void performRevert(Context ctx, RevertModifications revertModifications, SvnProgressSupport support) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();

        File files[] = ctx.getFiles();
        File[][] split = SvnUtils.splitFlatOthers(files);
        for (int c = 0; c<split.length; c++) {
            if(support.isCanceled()) {
                return;
            }
            files = split[c];
            boolean recursive = c == 1;
            if (recursive == false) {
                files = SvnUtils.flatten(files, FileInformation.STATUS_REVERTIBLE_CHANGE);
            }

            try {
                RevertModifications.RevisionInterval revisions = revertModifications.getRevisionInterval();
                if(revisions != null) {
                    for (int i= 0; i<files.length; i++) {
                        if(support.isCanceled()) {
                            return;
                        }
                        SVNUrl url = SvnUtils.getRepositoryUrl(files[i]);
                        client.merge(url, revisions.endRevision, url, revisions.startRevision, files[i], false, recursive);
                    }
                } else {
                    for (int i= 0; i<files.length; i++) {
                        if(support.isCanceled()) {
                            return;
                        }
                        client.revert(files[i], recursive);
                    }
                }
            } catch (SVNClientException ex) {
                ExceptionHandler eh = new ExceptionHandler (ex);
                eh.annotate();
            }
        }
    }
}
