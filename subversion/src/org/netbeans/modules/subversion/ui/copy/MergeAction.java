/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.copy;

import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class MergeAction extends ContextAction {

    public MergeAction() {        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Merge";    // NOI18N        
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    protected void performContextAction(final Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = getContext(nodes);        
        final File root = ctx.getRootFiles()[0];
        SVNUrl rootUrl;
        SVNUrl url;
        try {            
            rootUrl = SvnUtils.getRepositoryRootUrl(root);
            url = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }           
        final RepositoryFile repositoryRoot = new RepositoryFile(rootUrl, url, SVNRevision.HEAD);
     
        final Merge merge = new Merge(repositoryRoot, root);           
        if(merge.showDialog()) {
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
                public void perform() {
                    performMerge(merge, repositoryRoot, root, this);
                }
            };
            support.start(createRequestProcessor(nodes));
        }        
    }

    private void performMerge(Merge merge,  RepositoryFile repositoryRoot, File root, SvnProgressSupport support) {
        File[][] split = Utils.splitFlatOthers(new File[] {root} );
        boolean recursive;
        // there can be only 1 root file
        if(split[0].length > 0) {
            recursive = false;
        } else {
            recursive = true;
        }                

        try {
            SvnClient client;
            try {
                client = Subversion.getInstance().getClient(repositoryRoot.getRepositoryUrl());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }

            if(support.isCanceled()) {
                return;
            }
            
            SVNUrl endUrl = merge.getMergeEndUrl();
            SVNRevision endRevision = merge.getMergeEndRevision();
                        
            SVNUrl startUrl = merge.getMergeStartUrl();
            SVNRevision startRevision;
            if(startUrl != null) {                
                startRevision = merge.getMergeStartRevision();
            } else {
                // XXX is this the only way we can do it?
                startUrl = endUrl;
                ISVNLogMessage[] log = client.getLogMessages(startUrl, new SVNRevision.Number(0), new SVNRevision.Number(0), SVNRevision.HEAD, true, false, 0L);
                startRevision = log[0].getRevision();
            }                        
            if(support.isCanceled()) {
                return;
            }
            
            client.merge(startUrl,
                         startRevision,
                         endUrl,
                         endRevision,
                         root,
                         false,
                         recursive);                              

        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }
}
