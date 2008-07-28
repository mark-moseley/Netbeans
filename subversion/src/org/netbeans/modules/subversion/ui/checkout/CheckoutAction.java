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
package org.netbeans.modules.subversion.ui.checkout;

import java.io.File;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.wizards.*;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public final class CheckoutAction extends CallableSystemAction {
           
    public void performAction() {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Utils.logVCSActionEvent("SVN");

        CheckoutWizard wizard = new CheckoutWizard();
        if (!wizard.show()) return;
        
        final SVNUrl repository = wizard.getRepositoryRoot();
        final RepositoryFile[] repositoryFiles = wizard.getRepositoryFiles();
        final File file = wizard.getWorkdir();        
        final boolean atWorkingDirLevel = wizard.isAtWorkingDirLevel();
        
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
                final SvnClient client;
                try {
                    client = Subversion.getInstance().getClient(repository);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, true, true); // should not happen
                    return;
                }
        
                try {
                    setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));
                    checkout(client, repository, repositoryFiles, file, atWorkingDirLevel, this);
                } catch (SVNClientException ex) {
                    annotate(ex);
                    return;
                }
                if(isCanceled()) {
                    return;
                }
                
                setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_ScanFolders_Progress"));
                if (SvnModuleConfig.getDefault().getShowCheckoutCompleted()) {
                    String[] folders;
                    if(atWorkingDirLevel) {
                        folders = new String[1];
                        folders[0] = "."; // NOI18N
                    } else {
                        folders = new String[repositoryFiles.length];
                        for (int i = 0; i < repositoryFiles.length; i++) {
                            if(isCanceled()) {
                                return;
                            }
                            if(repositoryFiles[i].isRepositoryRoot()) {
                                folders[i] = "."; // NOI18N
                            } else {
                                folders[i] = repositoryFiles[i].getFileUrl().getLastPathSegment();
                            }
                        }
                    }                    
                    CheckoutCompleted cc = new CheckoutCompleted(file, folders, true);
                    if(isCanceled()) {
                        return;
                    }
                    cc.scanForProjects(this);
                }
            }
        };
        support.start(Subversion.getInstance().getRequestProcessor(repository), repository, java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));

    }
    
    public String getName() {
        return NbBundle.getMessage(CheckoutAction.class, "CTL_CheckoutAction"); // NOI18N
    }
    
    protected void initialize() {
        super.initialize();        
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public static void checkout(final SvnClient client,
                                final SVNUrl repository,
                                final RepositoryFile[] repositoryFiles,
                                final File workingDir,
                                final boolean atWorkingDirLevel,
                                final SvnProgressSupport support)
    throws SVNClientException
    {
        for (int i = 0; i < repositoryFiles.length; i++) {
            File destination;
            if(!atWorkingDirLevel) {
                destination = new File(workingDir.getAbsolutePath() +
                                       "/" +  // NOI18N
                                       repositoryFiles[i].getName()); // XXX what if the whole repository is seletcted
                destination = FileUtil.normalizeFile(destination);
                destination.mkdir();
            } else {
                destination = workingDir;
            }
            if(support!=null && support.isCanceled()) { 
                return;
            }
            client.checkout(repositoryFiles[i].getFileUrl(), destination, repositoryFiles[i].getRevision(), true);
            if(support!=null && support.isCanceled()) {
                return;                
            }            
        }
    }

}
