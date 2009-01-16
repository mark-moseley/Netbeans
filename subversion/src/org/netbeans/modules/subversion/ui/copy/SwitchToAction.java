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

package org.netbeans.modules.subversion.ui.copy;

import java.awt.EventQueue;
import java.io.File;
import java.util.List;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Petr Kuzel
 */
public class SwitchToAction extends ContextAction {
    
    /**
     * Creates a new instance of SwitchToAction
     */
    public SwitchToAction() {        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Switch";    // NOI18N        
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

    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1 &&  getContext(nodes).getRoots().size() > 0;
    }        
    
    protected void performContextAction(final Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = getContext(nodes);        

        File[] roots = SvnUtils.getActionRoots(ctx);
        if(roots == null || roots.length == 0) return;

        File interestingFile;
        if(roots.length == 1) {
            interestingFile = roots[0];
        } else {
            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
        }

        SVNUrl rootUrl;
        try {            
            rootUrl = SvnUtils.getRepositoryRootUrl(interestingFile);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }                
        final RepositoryFile repositoryRoot = new RepositoryFile(rootUrl, rootUrl, SVNRevision.HEAD);
        File[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        boolean hasChanges = files.length > 0;

        final RequestProcessor rp = createRequestProcessor(nodes);

        final SwitchTo switchTo = new SwitchTo(repositoryRoot, interestingFile, hasChanges);
                
        if(switchTo.showDialog()) {
            performSwitch(switchTo, rp, nodes, roots);
        }
    }

    /**
     * Switches all files from the roots array to their respective urls given by the SwitchTo controller
     * @param switchTo
     * @param rp
     * @param nodes
     * @param roots
     */
    private void performSwitch(final SwitchTo switchTo, final RequestProcessor rp, final Node[] nodes, final File[] roots) {
        rp.post(new Runnable() {
            public void run() {
                if(!validateInput(roots[0], switchTo.getRepositoryFile())) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            performSwitch(switchTo, rp, nodes, roots);
                        }
                    });
                } else {
                    ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(SwitchToAction.this, nodes) {
                        public void perform() {
                            for (File root : roots) {
                                RepositoryFile toRepositoryFile = switchTo.getRepositoryFile().replaceLastSegment(root.getName(), 0);
                                performSwitch(toRepositoryFile, root, this);
                            }
                        }
                    };
                    support.start(rp);                            
                }
            }
        });
    }
    
    private boolean validateInput(File root, RepositoryFile toRepositoryFile) {
        boolean ret = false;
        SvnClient client;
        try {                   
            client = Subversion.getInstance().getClient(toRepositoryFile.getRepositoryUrl());
            ISVNInfo info = client.getInfo(toRepositoryFile.getFileUrl());
            if(info.getNodeKind() == SVNNodeKind.DIR && root.isFile()) {
                SvnClientExceptionHandler.annotate(NbBundle.getMessage(SwitchToAction.class, "LBL_SwitchFileToFolderError"));
                ret = false;
            } else if(info.getNodeKind() == SVNNodeKind.FILE && root.isDirectory()) {
                SvnClientExceptionHandler.annotate(NbBundle.getMessage(SwitchToAction.class, "LBL_SwitchFolderToFileError"));
                ret = false;
            } else {
                ret = true;
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return ret;
        }                            
        return ret;
    }        

    static void performSwitch(RepositoryFile toRepositoryFile, File root, SvnProgressSupport support) {
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
                client = Subversion.getInstance().getClient(toRepositoryFile.getRepositoryUrl());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }            
            // ... and switch
            client.switchToUrl(root, toRepositoryFile.getFileUrl(), toRepositoryFile.getRevision(), recursive);
            // the client doesn't notify as there is no output rom the cli. Lets emulate onNotify as from the client
            List<File> switchedFiles = SvnUtils.listRecursively(root);
            File[] fileArray = switchedFiles.toArray(new File[switchedFiles.size()]);
            Subversion.getInstance().getRefreshHandler().refreshImediately(fileArray);                        
            // the cache fires status change events to trigger the annotation refresh
            // unfortunatelly - we have to call the refresh explicitly for each file also 
            // from this place as the branch label was changed evern if the files status didn't
            Subversion.getInstance().refreshAnnotations(fileArray);
            // refresh the inline diff
            Subversion.getInstance().versionedFilesChanged();
        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }
}
