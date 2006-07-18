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
package org.netbeans.modules.subversion.ui.checkout;

import java.io.File;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.ui.wizards.*;
import org.openide.ErrorManager;
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
                    ErrorManager.getDefault().notify(ex); // should not happen
                    return;
                }
        
                try {
                    setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));
                    checkout(client, repository, repositoryFiles, file, atWorkingDirLevel, this);
                } catch (SVNClientException ex) {
                    ExceptionHandler eh = new ExceptionHandler(ex);
                    eh.annotate();
                    //ErrorManager.getDefault().notify(ex);
                    return;
                }
                if(isCanceled()) {
                    return;
                }

                HistorySettings.getDefault().setCheckoutDirectory(file.getAbsolutePath());
                
                setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_ScanFolders_Progress"));
                if (HistorySettings.getFlag(HistorySettings.PROP_SHOW_CHECKOUT_COMPLETED, -1) != 0) {
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
