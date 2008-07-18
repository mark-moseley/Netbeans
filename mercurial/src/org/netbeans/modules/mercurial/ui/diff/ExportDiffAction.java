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
package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;

/**
 * ExportDiff action for mercurial:
 * hg export
 *
 * @author Padraig O'Briain
 */
public class ExportDiffAction extends ContextAction {

    private final VCSContext context;

    public ExportDiffAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public void performAction(ActionEvent e) {
        exportDiff(context);
    }

    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    }

    private static void exportDiff(VCSContext ctx) {
        final File root = HgUtils.getRootFile(ctx);
        File[] files = ctx != null? ctx.getFiles().toArray(new File[0]): null;
        ExportDiff ed = new ExportDiff(root, files);
        if (!ed.showDialog()) {
            return;
        }
        final String revStr = ed.getSelectionRevision();
        final String outputFileName = ed.getOutputFileName();
        File destinationFile = new File(outputFileName);
        if (destinationFile.exists()) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportDiffAction.class, "BK3005", destinationFile.getAbsolutePath()));
            nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(nd);
            if (nd.getValue().equals(NotifyDescriptor.OK_OPTION) == false) {
                return;
            }
        }

        HgModuleConfig.getDefault().setExportFolder(destinationFile.getParent());
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                OutputLogger logger = getLogger();
                performExport(root, revStr, outputFileName, logger);
            }
        };
        support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")); // NOI18N
    }

    public static void exportDiffFileRevision(final RepositoryRevision.Event drev) {
        if(drev == null) return;
        final File fileToDiff = drev.getFile();
        RepositoryRevision repoRev = drev.getLogInfoHeader();
        if(repoRev.getRepositoryRootUrl() == null || repoRev.getRepositoryRootUrl().equals(""))
            return;
        final File root = new File(repoRev.getRepositoryRootUrl());
        ExportDiff ed = new ExportDiff(root, repoRev, null, fileToDiff);
        final String revStr = repoRev.getLog().getRevision();
        if (!ed.showDialog()) {
            return;
        }
        final String outputFileName = ed.getOutputFileName();
        File destinationFile = new File(outputFileName);
        if (destinationFile.exists()) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportDiffAction.class, "BK3005", destinationFile.getAbsolutePath()));
            nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(nd);
            if (nd.getValue().equals(NotifyDescriptor.OK_OPTION) == false) {
                return;
            }
        }

        HgModuleConfig.getDefault().setExportFolder(destinationFile.getParent());
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());
        HgProgressSupport support = new HgProgressSupport() {

            public void perform() {
                OutputLogger logger = getLogger();
                performExportFile(root, revStr, fileToDiff, outputFileName, logger);
            }
        };
        support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")); // NOI18N
    }

    public static void exportDiffRevision(final RepositoryRevision repoRev, final File[] roots) {
        if(repoRev == null || repoRev.getRepositoryRootUrl() == null || repoRev.getRepositoryRootUrl().equals(""))
            return;
        final File root = new File(repoRev.getRepositoryRootUrl());
        ExportDiff ed = new ExportDiff(root, repoRev, roots);
        final String revStr = repoRev.getLog().getRevision();
        if (!ed.showDialog()) {
            return;
        }
        final String outputFileName = ed.getOutputFileName();
        File destinationFile = new File(outputFileName);
        if (destinationFile.exists()) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportDiffAction.class, "BK3005", destinationFile.getAbsolutePath()));
            nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(nd);
            if (nd.getValue().equals(NotifyDescriptor.OK_OPTION) == false) {
                return;
            }
        }

        HgModuleConfig.getDefault().setExportFolder(destinationFile.getParent());
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());
        HgProgressSupport support = new HgProgressSupport() {

            public void perform() {
                OutputLogger logger = getLogger();
                performExport(root, revStr, outputFileName, logger);
            }
        };
        support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")); // NOI18N
    }

    private static void performExport(File repository, String revStr, String outputFileName, OutputLogger logger) {
        try {
            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_TITLE_SEP")); // NOI18N

            if (revStr != null && NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_Revision_Default").startsWith(revStr)) {
                logger.output(
                        NbBundle.getMessage(ExportDiffAction.class,
                        "MSG_EXPORT_NOTHING")); // NOI18N
            } else {
                List<String> list = HgCommand.doExport(repository, revStr, outputFileName, logger);
                logger.output(list); // NOI18N
                if (!list.isEmpty() && list.size() > 1) {
                    File outFile = new File(list.get(1));
                    if (outFile != null && outFile.canRead()) {
                        org.netbeans.modules.versioning.util.Utils.openFile(FileUtil.normalizeFile(outFile));
                    }
                }
            }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            logger.outputInRed(NbBundle.getMessage(ExportDiffAction.class, "MSG_EXPORT_DONE")); // NOI18N
            logger.output(""); // NOI18N
        }
    }
    private static void performExportFile(File repository, String revStr, File fileToDiff, String outputFileName, OutputLogger logger) {
    try {
        logger.outputInRed(
                NbBundle.getMessage(ExportDiffAction.class,
                "MSG_EXPORT_FILE_TITLE")); // NOI18N
        logger.outputInRed(
                NbBundle.getMessage(ExportDiffAction.class,
                "MSG_EXPORT_FILE_TITLE_SEP")); // NOI18N

        if (NbBundle.getMessage(ExportDiffAction.class,
                "MSG_Revision_Default").startsWith(revStr)) {
            logger.output(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_NOTHING")); // NOI18N
        } else {
            List<String> list = HgCommand.doExportFileDiff(repository, fileToDiff, revStr, outputFileName, logger);
            String repoPath = repository.getAbsolutePath();
            String fileToDiffPath = fileToDiff.getAbsolutePath();
            fileToDiffPath = fileToDiffPath.substring(repoPath.length()+1);

            logger.output(NbBundle.getMessage(ExportDiffAction.class, "MSG_EXPORT_FILE", fileToDiffPath, revStr, outputFileName)); // NOI18N
            if (!list.isEmpty() && list.size() > 1) {
                File outFile = new File(outputFileName);
                if (outFile != null && outFile.canRead()) {
                    org.netbeans.modules.versioning.util.Utils.openFile(FileUtil.normalizeFile(outFile));
                }
            }
        }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            logger.outputInRed(NbBundle.getMessage(ExportDiffAction.class, "MSG_EXPORT_FILE_DONE")); // NOI18N
            logger.output(""); // NOI18N
        }
    }
}
