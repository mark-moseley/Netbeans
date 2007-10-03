/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.mercurial.ui.clone;

import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.ui.clone.Clone;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Clone action for mercurial: 
 * hg clone - Create a copy of an existing repository in a new directory.
 * 
 * @author John Rice
 */
public class CloneAction extends AbstractAction {
    private final VCSContext context;

    public CloneAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent ev){
        final File root = HgUtils.getRootFile(context);
        if (root == null) return;
        
        // Get unused Clone Folder name
        File tmp = root.getParentFile();
        File projFile = HgUtils.getProjectFile(context);
        String folderName = root.getName();
        Boolean projIsRepos = true;
        if (!root.equals(projFile))  {
            // Mercurial Repository is not the same as project root
            projIsRepos = false;
        }
        for(int i = 0; i < 10000; i++){
            if (!new File(tmp,folderName+"_clone"+i).exists()){ // NOI18N
                tmp = new File(tmp, folderName +"_clone"+i); // NOI18N
                break;
            }
        }
        Clone clone = new Clone(root, tmp);
        if (!clone.showDialog()) {
            return;
        }

        performClone(root.getAbsolutePath(), clone.getOutputFileName(), projIsRepos, projFile);
    }

    public static void performClone(final String source, final String target, boolean projIsRepos, File projFile) {
        final Mercurial hg = Mercurial.getInstance();
        final ProjectManager projectManager = ProjectManager.getDefault();
        final File prjFile = projFile;
        final Boolean prjIsRepos = projIsRepos;
        final File cloneFolder = new File (target);
        final File normalizedCloneFolder = FileUtil.normalizeFile(cloneFolder);
        String projName = null;
        if (projFile != null) projName = HgProjectUtils.getProjectName(projFile);
        final String prjName = projName;
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(source);
        HgProgressSupport support = new HgProgressSupport() {
            Runnable doOpenProject = new Runnable () {
                public void run()  { 
                    // Open and set focus on the cloned project
                    File cloneProjFile;
                    if (!prjIsRepos) {
                        String name = prjFile.getAbsolutePath().substring(source.length() + 1);
                        cloneProjFile = new File (normalizedCloneFolder, name);
                    } else {
                        cloneProjFile = normalizedCloneFolder;
                    }

                    FileObject cloneProj = FileUtil.toFileObject(cloneProjFile);
                    if (projectManager.isProject(cloneProj)){
                        try {
                            org.netbeans.api.project.Project prj = projectManager.findProject(cloneProj);
                            HgProjectUtils.openProject(prj, this);
                            // TODO: figure out how to rename the cloned project
                            // Following brings up Rename Project Dialog but not with correct settings 
                            // - thought the ctx was ok but must not be
                            // HgProjectUtils.renameProject(prj);
                
                            hg.versionedFilesChanged();
                            hg.refreshAllAnnotations();
                            //HgUtils.forceStatusRefresh(cloneFolder);
                
                        } catch (java.lang.Exception ex) {
                            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(new HgException(ex.toString()));
                            DialogDisplayer.getDefault().notifyLater(e);
                        } 
                    } else {
                        JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(CloneAction.class,"MSG_NO_PROJECT")); // NOI18N
                    }
                }
            };
            public void perform() {
                try {
                    // TODO: We need to annotate the cloned project 
                    // See http://qa.netbeans.org/issues/show_bug.cgi?id=112870
                    List<String> list = HgCommand.doClone(source, target);
                    if(list != null && !list.isEmpty()){
                        HgUtils.createIgnored(cloneFolder);
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(CloneAction.class,
                                "MSG_CLONE_TITLE")); // NOI18N
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(CloneAction.class,
                                "MSG_CLONE_TITLE_SEP")); // NOI18N
                        HgUtils.outputMercurialTab(list);
               
                        if (prjName != null) {
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_FROM", prjName, source)); // NOI18N
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_TO", prjName, target)); // NOI18N
                        } else {
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_FROM", source)); // NOI18N
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_TO", target)); // NOI18N

                        }
                        HgUtils.outputMercurialTab(""); // NOI18N

                        SwingUtilities.invokeLater(doOpenProject);
                    }

                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
                HgUtils.outputMercurialTabInRed(
                        NbBundle.getMessage(CloneAction.class,
                        "MSG_CLONE_DONE")); // NOI18N
                HgUtils.outputMercurialTab(""); // NOI18N
            }
        };
        support.start(rp, source, org.openide.util.NbBundle.getMessage(CloneAction.class, "LBL_Clone_Progress")); // NOI18N
    }

    public boolean isEnabled() {
        // If it's a mercurial managed repository with history
        // enable clone of this repository
        return HgRepositoryContextCache.hasHistory(context);
    }
}
