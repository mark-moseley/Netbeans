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
package org.netbeans.modules.mercurial.ui.view;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

import java.io.File;
import java.util.List;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * View action for mercurial: 
 * hg view - launch hg view to view the dependency tree for the repository
 * 
 * @author John Rice
 */
public class ViewAction extends AbstractAction {
    
    private final VCSContext context;

    public ViewAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        final File root = HgUtils.getRootFile(context);
        if (root == null) return;
        String repository = root.getAbsolutePath();
        RequestProcessor rp = RequestProcessor.getDefault();
        rp.post(new Runnable() {
            public void run() {
                performView(root);
            }
        });
    }

    static void performView(File root) {
        try {
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(ViewAction.class, "MSG_VIEW_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(ViewAction.class, "MSG_VIEW_TITLE_SEP")); // NOI18N

            String hgkCommand = HgCommand.HGK_COMMAND;
            if(Utilities.isWindows()){ 
                hgkCommand = hgkCommand + HgCommand.HG_WINDOWS_CMD;
            }
            boolean bHgkFound = false;
            if(HgUtils.isInUserPath(hgkCommand)){
                    bHgkFound = true;                
            } else if(HgUtils.isSolaris()){
                File f = new File(HgCommand.HG_HGK_PATH_SOLARIS10, hgkCommand);
                if(f.exists() && f.isFile()) 
                    bHgkFound = true;
            }
            boolean bHgkPropExists = HgConfigFiles.getInstance().containsProperty(
                            HgConfigFiles.HG_EXTENSIONS, HgConfigFiles.HG_EXTENSIONS_HGK);
            
            if(!bHgkFound){
                HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(ViewAction.class, "MSG_VIEW_HGK_NOT_FOUND_INFO")); // NOI18N
                HgUtils.outputMercurialTab(""); // NOI18N
                JOptionPane.showMessageDialog(null,
                        NbBundle.getMessage(ViewAction.class, "MSG_VIEW_HGK_NOT_FOUND"),// NOI18N
                        NbBundle.getMessage(ViewAction.class, "MSG_VIEW_HGK_NOT_FOUND_TITLE"),// NOI18N
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if(!bHgkPropExists){
                boolean bConfirmSetHgkProp = false;
                bConfirmSetHgkProp = HgUtils.confirmDialog(
                        ViewAction.class, "MSG_VIEW_SETHGK_PROP_CONFIRM_TITLE", // NOI18N
                        "MSG_VIEW_SETHGK_PROP_CONFIRM_QUERY"); // NOI18N                
                if (bConfirmSetHgkProp) {
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(ViewAction.class, "MSG_VIEW_SETHGK_PROP_DO_INFO")); // NOI18N
                    HgConfigFiles.getInstance().setProperty(HgConfigFiles.HG_EXTENSIONS_HGK, ""); // NOI18N
                }else{
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(ViewAction.class, "MSG_VIEW_NOTSETHGK_PROP_INFO")); // NOI18N
                    HgUtils.outputMercurialTab(""); // NOI18N
                    return;
                }
            }
            
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(ViewAction.class, 
                    "MSG_VIEW_LAUNCH_INFO", root.getAbsolutePath())); // NOI18N
            HgUtils.outputMercurialTab(""); // NOI18N
            HgCommand.doView(root);
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
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
