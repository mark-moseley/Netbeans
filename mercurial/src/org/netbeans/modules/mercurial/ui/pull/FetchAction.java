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
package org.netbeans.modules.mercurial.ui.pull;

import org.netbeans.modules.mercurial.ui.view.*;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

import java.io.File;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.openide.util.NbBundle;

/**
 * Fetch action for mercurial: 
 * hg fetch - launch hg view to view the dependency tree for the repository
 * Pull changes from a remote repository, merge new changes if needed.
 * This finds all changes from the repository at the specified path
 * or URL and adds them to the local repository.
 * 
 * If the pulled changes add a new head, the head is automatically
 * merged, and the result of the merge is committed.  Otherwise, the
 * working directory is updated.
 * 
 * @author John Rice
 */
public class FetchAction extends AbstractAction {
    
    private final VCSContext context;

    public FetchAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        if(!Mercurial.getInstance().isGoodVersionAndNotify()) return;
        final File root = HgUtils.getRootFile(context);
        if (root == null) return;
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() { performFetch(root); } };

        support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(FetchAction.class, "MSG_FETCH_PROGRESS")); // NOI18N
    }

    static void performFetch(File root) {
        try {
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_TITLE_SEP")); // NOI18N

            boolean bFetchPropExists = HgConfigFiles.getInstance().containsProperty(
                            HgConfigFiles.HG_EXTENSIONS, HgConfigFiles.HG_EXTENSIONS_FETCH);
            
            if(!bFetchPropExists){
                boolean bConfirmSetFetchProp = false;
                bConfirmSetFetchProp = HgUtils.confirmDialog(
                        FetchAction.class, "MSG_FETCH_SETFETCH_PROP_CONFIRM_TITLE", // NOI18N
                        "MSG_FETCH_SETFETCH_PROP_CONFIRM_QUERY"); // NOI18N                
                if (bConfirmSetFetchProp) {
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(FetchAction.class, "MSG_FETCH_SETHGK_PROP_DO_INFO")); // NOI18N
                    HgConfigFiles.getInstance().setProperty(HgConfigFiles.HG_EXTENSIONS_FETCH, ""); // NOI18N
                }else{
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(FetchAction.class, "MSG_FETCH_NOTSETHGK_PROP_INFO")); // NOI18N
                    HgUtils.outputMercurialTab(""); // NOI18N
                    return;
                }
            }
            
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(FetchAction.class, 
                    "MSG_FETCH_LAUNCH_INFO", root.getAbsolutePath())); // NOI18N
            
            List<String> list;
            list = HgCommand.doFetch(root);
            
            if (list != null && !list.isEmpty()) {
                HgUtils.outputMercurialTab(list);
            }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }finally{
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_DONE")); // NOI18N
            HgUtils.outputMercurialTab(""); // NOI18N
        }
    }

    public boolean isEnabled() {
        File root = HgUtils.getRootFile(context);
        if (root == null)
            return false;
        else
            return true;
    } 
}
