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

package org.netbeans.modules.mercurial.ui.update;

import java.io.File;
import java.util.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.HgException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.swing.AbstractAction;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;

/**
 * Reverts local changes.
 *
 * @author Padraig O'Briain
 */
public class RevertModificationsAction extends AbstractAction {
    
    private final VCSContext context;
 
    public RevertModificationsAction(String name, VCSContext context) {        
        this.context =  context;
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent e) {
        revert(context);
    }

    public static void revert(final VCSContext ctx) {
        final File[] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        final File repository  = HgUtils.getRootFile(ctx);
        if (repository == null) return;
        String rev = null;

        final RevertModifications revertModifications = new RevertModifications(repository, files);
        if (!revertModifications.showDialog()) {
            return;
        }
        rev = revertModifications.getSelectionRevision();
        final String revStr = rev;

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                performRevert(repository, revStr, files);
            }
        };
        support.start(rp, repository.getAbsolutePath(), org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Revert_Progress")); // NOI18N

        return;
    }

    public static void performRevert(File repository, String revStr, File file) {
        File[] files = new File[1];
        files[0] = file;

        performRevert(repository, revStr, files);
    }

    public static void performRevert(File repository, String revStr, File[] files) {
        try {
            List<File> revertFiles = new ArrayList<File>();
            for (File file : files) {
                revertFiles.add(file);
            }
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_TITLE_SEP")); // NOI18N
            HgUtils.outputMercurialTab(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_REVISION_STR", revStr)); // NOI18N
            for (File file : files) {
                HgUtils.outputMercurialTab(file.getAbsolutePath());
            }
            HgUtils.outputMercurialTab(""); // NOI18N
 
            HgCommand.doRevert(repository, revertFiles, revStr);
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
            File[] conflictFiles = cache.listFiles(files, FileInformation.STATUS_VERSIONED_CONFLICT);
            if(conflictFiles.length != 0){
                ConflictResolvedAction.conflictResolved(repository, conflictFiles);
            }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }

        // refresh filesystem to take account of changes
        FileObject rootObj = FileUtil.toFileObject(repository);
        try {
            rootObj.getFileSystem().refresh(true);
        } catch (java.lang.Exception exc) {
        }
        HgUtils.outputMercurialTabInRed(
                NbBundle.getMessage(RevertModificationsAction.class,
                "MSG_REVERT_DONE")); // NOI18N
 
    }

    public boolean isEnabled() {
        return HgRepositoryContextCache.hasHistory(context);
    }
}
