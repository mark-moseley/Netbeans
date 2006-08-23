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

package org.netbeans.modules.subversion.ui.ignore;

import java.util.*;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.util.*;
import org.openide.*;
import org.openide.nodes.Node;

import java.io.File;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Adds/removes files to svn:ignore property.
 * It does not support patterns.
 *
 * @author Maros Sandor
 */
public class IgnoreAction extends ContextAction {
    
    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
        switch (actionStatus) {
        case UNDEFINED:
        case IGNORING:
            return "CTL_MenuItem_Ignore";  // NOI18N
        case UNIGNORING:
            return "CTL_MenuItem_Unignore"; // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }
    
    public int getActionStatus(Node [] nodes) {
        return getActionStatus(SvnUtils.getCurrentContext(nodes).getFiles());
    }

    public int getActionStatus(File [] files) {
        int actionStatus = -1;
        if (files.length == 0) return UNDEFINED; 
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(".svn") || files[i].getName().equals("_svn")) { // NOI18N
                actionStatus = UNDEFINED;
                break;
            }
            FileInformation info = cache.getStatus(files[i]);
            if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                actionStatus = (actionStatus == -1 || actionStatus == IGNORING) ?
                        IGNORING : UNDEFINED;
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                actionStatus = ((actionStatus == -1 || actionStatus == UNIGNORING)
                        && canBeUnignored(files[i])) ?
                        UNIGNORING : UNDEFINED;
            } else {
                actionStatus = UNDEFINED;
                break;
            }
        }
        return actionStatus == -1 ? UNDEFINED : actionStatus;
    }
    
    private boolean canBeUnignored(File file) {
        File parent = file.getParentFile();
        try {
            SvnClient client = Subversion.getInstance().getClient(false);
            List patterns = client.getIgnoredPatterns(parent);
            return patterns.contains(file.getName());
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
    }

    protected boolean enable(Node[] nodes) {
        return getActionStatus(nodes) != UNDEFINED;
    }

    public void performContextAction(final Node[] nodes) {

        final File files[] = SvnUtils.getCurrentContext(nodes).getFiles();
        final int actionStatus = getActionStatus(nodes);
        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {

                SvnClient client = Subversion.getInstance().getClient(true);               
                for (int i = 0; i<files.length; i++) {
                    if(isCanceled()) {
                        return;
                    }
                    File file = files[i];
                    File parent = file.getParentFile();
                    if (actionStatus == IGNORING) {
                        try {
                            ensureVersioned(parent);
                            client.addToIgnoredPatterns(parent, file.getName());
                            // it's not catched by cache's onNotify(), refresh explicitly
                            Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        } catch (SVNClientException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    } else if (actionStatus == UNIGNORING) {
                        try {
                            List patterns = Subversion.getInstance().getClient(true).getIgnoredPatterns(parent);
                            patterns.remove(file.getName());
                            client.setIgnoredPatterns(parent, patterns);
                            Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        } catch (SVNClientException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    } else {
                        throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
                    }
                }
            }
        };            
        support.start(createRequestProcessor(nodes));
    }

    /**
     * Adds this file and all its parent folders to repository if they are not yet added. 
     * 
     * @param file file to add
     * @throws SVNClientException if something goes wrong in subversion
     */ 
    private void ensureVersioned(File file) throws SVNClientException {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) != 0) return;
        ensureVersioned(file.getParentFile());
        add(file);
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    /**
     * Adds the file to repository with 'svn add', non-recursively.
     * 
     * @param file file to add
     */ 
    private void add(File file) throws SVNClientException {
        SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(file);
        SvnClient client = Subversion.getInstance().getClient(repositoryUrl, true);               
        client.addFile(file);
    }

    protected boolean asynchronous() {
        return false;
    }

}
