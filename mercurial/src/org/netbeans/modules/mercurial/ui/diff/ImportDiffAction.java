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
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;

/**
 * ImportDiff action for mercurial: 
 * hg export
 * 
 * @author Padraig O'Briain
 */
public class ImportDiffAction extends AbstractAction {
    
    private final VCSContext context;

    public ImportDiffAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        importDiff(context);
    }
    
    public boolean isEnabled() {
        if(!Mercurial.getInstance().isGoodVersion()) return false;
        return HgRepositoryContextCache.hasHistory(context);
    } 

    private static void importDiff(VCSContext ctx) {
        final File root = HgUtils.getRootFile(ctx);
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(ImportDiffAction.class, "ACSD_ImportBrowseFolder"), null);   // NO I18N
        fileChooser.setDialogTitle(NbBundle.getMessage(ImportDiffAction.class, "ImportBrowse_title"));                                            // NO I18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.showDialog(null, NbBundle.getMessage(ImportDiffAction.class,
"OK_Button"));                                            // NO I18N
        final File patchFile = fileChooser.getSelectedFile();

        if (patchFile != null) {
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());
            HgProgressSupport support = new HgProgressSupport() {
                public void perform() {
                    performImport(root, patchFile);
                }
            };
            support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(ImportDiffAction.class, "LBL_ImportDiff_Progress")); // NOI18N
        }
    }

    private static void performImport(File repository, File patchFile) {
    try {
        HgUtils.outputMercurialTabInRed(
                NbBundle.getMessage(ImportDiffAction.class,
                "MSG_IMPORT_TITLE")); // NOI18N
        HgUtils.outputMercurialTabInRed(
                NbBundle.getMessage(ImportDiffAction.class,
                "MSG_IMPORT_TITLE_SEP")); // NOI18N

        List<String> list = HgCommand.doImport(repository, patchFile);
        Mercurial.getInstance().changesetChanged(repository);
        HgUtils.outputMercurialTab(list); // NOI18N

        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(ImportDiffAction.class, "MSG_IMPORT_DONE")); // NOI18N
            HgUtils.outputMercurialTab(""); // NOI18N
        }
    }
}
