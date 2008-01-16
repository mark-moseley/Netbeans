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
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.swing.AbstractAction;

/**
 * Update action for mercurial: 
 * hg update - update or merge working directory
 * 
 * @author John Rice
 */
public class UpdateAction extends AbstractAction {
    
    private final VCSContext context;

    public UpdateAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        if(!Mercurial.getInstance().isGoodVersionAndNotify()) return;
        update(context, null);
    }
    
    public static void update(final VCSContext ctx, final String revision){
        final File root = HgUtils.getRootFile(ctx);
        if (root == null) return;
        String repository = root.getAbsolutePath();
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                boolean bNoUpdates = true;
                try {
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(UpdateAction.class,
                            "MSG_UPDATE_TITLE")); // NOI18N
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(UpdateAction.class,
                            "MSG_UPDATE_TITLE_SEP")); // NOI18N
                    List<String> list = HgCommand.doUpdateAll(root, false, revision);
                    
                    if (list != null && !list.isEmpty()){
                        bNoUpdates = HgCommand.isNoUpdates(list.get(0));
                        //HgUtils.clearOutputMercurialTab();
                        HgUtils.outputMercurialTab(list);
                        HgUtils.outputMercurialTab(""); // NOI18N
                    }  
                    // refresh filesystem to take account of changes
                    FileObject rootObj = FileUtil.toFileObject(root);
                    try {
                        rootObj.getFileSystem().refresh(true);
                    } catch (Exception ex) {
                    }

                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
                
                // Force Status Refresh from this dir and below
                if(!bNoUpdates)
                    HgUtils.forceStatusRefreshProject(ctx);

                HgUtils.outputMercurialTabInRed(
                        NbBundle.getMessage(UpdateAction.class,
                        "MSG_UPDATE_DONE")); // NOI18N
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
    }
    
    public boolean isEnabled() {
        // If it's a mercurial managed repository enable Update action
        File root = HgUtils.getRootFile(context);
        if (root == null)
            return false;
        else
            return true;
    }     
}
