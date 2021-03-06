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
package org.netbeans.modules.mercurial.ui.create;

import java.io.File;
import java.util.Calendar;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;

/**
 * Create action for mercurial: 
 * hg init - create a new repository in the given directory
 * 
 * @author John Rice
 */
public class CreateAction extends AbstractAction {
    
    private final VCSContext context;
    Map<File, FileInformation> repositoryFiles = new HashMap<File, FileInformation>();

    public CreateAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public boolean isEnabled() {
        // If it is not a mercurial managed repository enable action
        File root = HgUtils.getRootFile(context);
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if ( files == null || files.length == 0) 
            return false;
        
        if (root == null)
            return true;
        else
            return false;
    } 

    private File getCommonAncestor(File firstFile, File secondFile) {
        if (firstFile.equals(secondFile)) return firstFile;

        File tempFirstFile = firstFile;
        while (tempFirstFile != null) {
            File tempSecondFile = secondFile;
            while (tempSecondFile != null) {
                if (tempFirstFile.equals(tempSecondFile))
                    return tempSecondFile;
                tempSecondFile = tempSecondFile.getParentFile();
            }
            tempFirstFile = tempFirstFile.getParentFile();
        }
        return null;
    }

    private File getCommonAncestor(File[] files) {
        File f1 = files[0];

        for (int i = 1; i < files.length; i++) {
            f1 = getCommonAncestor(f1, files[i]);
            if (f1 == null) {
                Mercurial.LOG.log(Level.SEVERE, "Unable to get common parent of {0} and {1} ", // NOI18N
                        new Object[] {f1.getAbsolutePath(), files[i].getAbsolutePath()});
             }

        }
        return f1;
    }

    public void actionPerformed(ActionEvent e) {
        final Mercurial hg = Mercurial.getInstance();
        if(!hg.isGoodVersionAndNotify()) return;

        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if(files == null || files.length == 0) return;
        
        // If there is a .hg directory in an ancestor of any of the files in 
        // the context we fail.
        
        for (File file : files) {
            if(!file.isDirectory()) file = file.getParentFile();
            if (hg.getTopmostManagedParent(file) != null) {
                Mercurial.LOG.log(Level.SEVERE, "Found .hg directory in ancestor of {0} ", // NOI18N
                        file);
                return;
            }
        }

        final Project proj = HgUtils.getProject(context);
        File projFile = HgUtils.getProjectFile(proj);
        
        if (projFile == null) {
            HgUtils.outputMercurialTabInRed( NbBundle.getMessage(CreateAction.class,"MSG_CREATE_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed( NbBundle.getMessage(CreateAction.class,"MSG_CREATE_TITLE_SEP")); // NOI18N
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(CreateAction.class, "MSG_CREATE_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            HgUtils.outputMercurialTab(""); // NOI18N
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(CreateAction.class, "MSG_CREATE_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(CreateAction.class, "MSG_CREATE_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String projName = HgProjectUtils.getProjectName(projFile);
        File root = null;
 
        root = getCommonAncestor(files);
        root = getCommonAncestor(root, projFile);
        if (root == null) return;
        
        HgUtils.outputMercurialTabInRed(
                NbBundle.getMessage(CreateAction.class,
                "MSG_CREATE_TITLE")); // NOI18N
        HgUtils.outputMercurialTabInRed(
                NbBundle.getMessage(CreateAction.class,
                "MSG_CREATE_TITLE_SEP")); // NOI18N
        
        final File rootToManage = root;
        final String prjName = projName;

        RequestProcessor rp = hg.getRequestProcessor(rootToManage.getAbsolutePath());
        
        HgProgressSupport supportCreate = new HgProgressSupport() {
            public void perform() {
                
                try {
                    HgUtils.outputMercurialTab(
                            NbBundle.getMessage(CreateAction.class,
                            "MSG_CREATE_INIT", prjName, rootToManage)); // NOI18N
                    HgCommand.doCreate(rootToManage);
                    hg.versionedFilesChanged();
                    hg.refreshAllAnnotations();      
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }             
            }
        };
        supportCreate.start(rp, rootToManage.getAbsolutePath(), 
                org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Progress")); // NOI18N

        
        HgProgressSupport supportAdd = new HgProgressSupport() {
            public void perform() {
                try {
                    File[] files = HgUtils.getProjectRootFiles(proj);
                    FileStatusCache cache = hg.getFileStatusCache();
                    FileInformation fi = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, null, false);

                    for (int j = 0; j < files.length; j++) {
                        File rootFile = files[j];
                        Calendar start = Calendar.getInstance();
                        repositoryFiles = HgCommand.getUnknownStatus(rootToManage, rootFile);
                        Calendar end = Calendar.getInstance();
                        Mercurial.LOG.log(Level.FINE, "getUnknownStatus took {0} millisecs", end.getTimeInMillis() - start.getTimeInMillis()); // NOI18N
                        HgUtils.outputMercurialTab(
                                NbBundle.getMessage(CreateAction.class,
                                "MSG_CREATE_ADD", repositoryFiles.keySet().size(), rootFile.getAbsolutePath())); // NOI18N
                        start = Calendar.getInstance(); cache.addToCache(repositoryFiles.keySet());
                        end = Calendar.getInstance();
                        Mercurial.LOG.log(Level.FINE, "addUnknownsToCache took {0} millisecs", end.getTimeInMillis() - start.getTimeInMillis()); // NOI18N
                        if (repositoryFiles.keySet().size() < HgUtils.MAX_LINES_TO_PRINT) {
                            for(File f: repositoryFiles.keySet()){
                                HgUtils.outputMercurialTab("\t" + f.getAbsolutePath()); // NOI18N
                            }
                        }
                    }
                    HgUtils.createIgnored(rootToManage);
                    HgUtils.outputMercurialTab(""); // NOI18N
                    HgUtils.outputMercurialTabInRed(NbBundle.getMessage(CreateAction.class, "MSG_CREATE_DONE_WARNING")); // NOI18N
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                } finally {
                    HgUtils.outputMercurialTabInRed(NbBundle.getMessage(CreateAction.class, "MSG_CREATE_DONE")); // NOI18N
                    HgUtils.outputMercurialTab(""); // NOI18N
                }
            }
        };
        supportAdd.start(rp, rootToManage.getAbsolutePath(), 
                org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Add_Progress")); // NOI18N
    }
}
