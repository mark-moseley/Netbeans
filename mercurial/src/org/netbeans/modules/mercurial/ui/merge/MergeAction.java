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
package org.netbeans.modules.mercurial.ui.merge;

import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.openide.util.Utilities;
import org.openide.windows.OutputWriter;

/**
 * Merge action for mercurial:
 * hg merge - attempts to merge changes when the repository has 2 heads
 *
 * @author John Rice
 */
public class MergeAction extends AbstractAction {

    private final VCSContext context;
    private String revStr;
    private final static int MULTIPLE_AUTOMERGE_HEAD_LIMIT = 2;
    
    public MergeAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public boolean isEnabled() {
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(HgUtils.getRootFile(context) == null || ctxFiles == null || ctxFiles.size() == 0) 
            return false;
        return true; // #121293: Speed up menu display, warn user if nothing to merge when Merge selected
    }

    public void actionPerformed(ActionEvent ev) {
        if(!Mercurial.getInstance().isGoodVersionAndNotify()) return;
        final File root = HgUtils.getRootFile(context);
        if (root == null) {
            HgUtils.outputMercurialTabInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE_SEP")); // NOI18N
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            HgUtils.outputMercurialTab(""); // NOI18N
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if(root != null && !HgCommand.isMergeRequired(root)){
            HgUtils.outputMercurialTabInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE_SEP")); // NOI18N
            HgUtils.outputMercurialTab( NbBundle.getMessage(MergeAction.class,"MSG_NOTHING_TO_MERGE")); // NOI18N
            HgUtils.outputMercurialTabInRed( NbBundle.getMessage(MergeAction.class, "MSG_MERGE_DONE")); // NOI18N
            HgUtils.outputMercurialTab(""); // NOI18N
            JOptionPane.showMessageDialog(null,
                NbBundle.getMessage(MergeAction.class,"MSG_NOTHING_TO_MERGE"),// NOI18N
                NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE"),// NOI18N
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String repository = root.getAbsolutePath();
        try{
            List<String> headList = HgCommand.getHeadRevisions(root);
            revStr = null;
            if (headList.size() > MULTIPLE_AUTOMERGE_HEAD_LIMIT){
                final MergeRevisions mergeDlg = new MergeRevisions(root);
                if (!mergeDlg.showDialog()) {
                    return;
                }
                revStr = mergeDlg.getSelectionRevision();               
            }
        } catch(HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
        }
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                try {
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE")); // NOI18N
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE_SEP")); // NOI18N
                    doMergeAction(root, revStr);
                    HgUtils.forceStatusRefreshProject(context);
                    HgUtils.outputMercurialTab(""); // NOI18N
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
            }
        };
        support.start(rp, repository, NbBundle.getMessage(MergeAction.class, "MSG_MERGE_PROGRESS")); // NOI18N
    }

    public static boolean doMergeAction(File root, String revStr) throws HgException {
        List<String> listMerge = HgCommand.doMerge(root, revStr);
        Boolean bConflicts = false;
        Boolean bMergeFailed = false;
        
        if (listMerge != null && !listMerge.isEmpty()) {
            HgUtils.outputMercurialTab(listMerge);          
            for (String line : listMerge) {
                if (HgCommand.isMergeAbortUncommittedMsg(line)){ 
                    bMergeFailed = true;
                    HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class,
                            "MSG_MERGE_FAILED")); // NOI18N
                    JOptionPane.showMessageDialog(null,
                        NbBundle.getMessage(MergeAction.class,"MSG_MERGE_UNCOMMITTED"), // NOI18N
                        NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE"), // NOI18N
                        JOptionPane.WARNING_MESSAGE);
                    break;
                }            

                if (HgCommand.isMergeAbortMultipleHeadsMsg(line)){ 
                    bMergeFailed = true;
                    HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class,
                            "MSG_MERGE_FAILED")); // NOI18N
                    break;
                }
                if (HgCommand.isMergeConflictMsg(line)) {
                    bConflicts = true;
                    String filepath = null;
                    if(Utilities.isWindows()){
                        filepath = line.substring(
                            HgCommand.HG_MERGE_CONFLICT_WIN1_ERR.length(),
                            line.length() - HgCommand.HG_MERGE_CONFLICT_WIN2_ERR.length()
                            ).trim().replace("/", "\\"); // NOI18N
                        filepath = root.getAbsolutePath() + File.separator + filepath;
                    }else{
                        filepath = line.substring(HgCommand.HG_MERGE_CONFLICT_ERR.length());
                    }
                    HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_CONFLICT", filepath)); // NOI18N
                    HgCommand.createConflictFile(filepath);
                }
                
                if (HgCommand.isMergeUnavailableMsg(line)){ 
                        JOptionPane.showMessageDialog(null, 
                                NbBundle.getMessage(MergeAction.class, "MSG_MERGE_UNAVAILABLE"), // NOI18N
                                NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE"), // NOI18N
                                JOptionPane.WARNING_MESSAGE);
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(MergeAction.class, "MSG_MERGE_INFO"));// NOI18N            
                        HgUtils.outputMercurialTabLink(
                                NbBundle.getMessage(MergeAction.class, "MSG_MERGE_INFO_URL")); // NOI18N 
                }            
            }
                  
            if (bConflicts) {
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                        "MSG_MERGE_DONE_CONFLICTS")); // NOI18N
            }
            if (!bMergeFailed && !bConflicts) {
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                        "MSG_MERGE_DONE")); // NOI18N
            }
        }
        return true;
    }
    
    public static void printMergeWarning(OutputWriter outRed, List<String> list){
        if(list == null || list.isEmpty() || list.size() <= 1) return;
        
        if (list.size() == 2) {
            outRed.println(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_NEEDED", list)); // NOI18N
            outRed.println(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DO_NEEDED")); // NOI18N
        } else {
            outRed.println(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_MULTIPLE_HEADS", list.size(), list)); // NOI18N
            outRed.println(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DONE_MULTIPLE_HEADS")); // NOI18N
        }
    }
    
    public static void printMergeWarning(List<String> list){
        if(list == null || list.isEmpty() || list.size() <= 1) return;
        
        if (list.size() == 2) {
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_NEEDED", list)); // NOI18N
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DO_NEEDED")); // NOI18N
        } else {
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_MULTIPLE_HEADS", list.size(), list)); // NOI18N
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DONE_MULTIPLE_HEADS")); // NOI18N
        }
    }

}
