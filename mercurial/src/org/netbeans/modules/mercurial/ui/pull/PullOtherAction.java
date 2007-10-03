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

import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.io.File;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.repository.Repository;
import org.netbeans.modules.mercurial.ui.wizards.CloneRepositoryWizardPanel;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.NotifyDescriptor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Pull Other action for mercurial: 
 * hg pull - pull changes from the specified source
 * 
 * @author John Rice
 */
public class PullOtherAction extends AbstractAction implements PropertyChangeListener {
    
    private final VCSContext context;
    private Repository repository = null;
    private JButton pullButton = null;

    public PullOtherAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent e) {
        final File root = HgUtils.getRootFile(context);
        if (root == null) return;

        if (repository == null) {
            int repositoryModeMask = Repository.FLAG_URL_EDITABLE | Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
            String title = org.openide.util.NbBundle.getMessage(CloneRepositoryWizardPanel.class, "CTL_Repository_Location");       // NOI18N
            repository = new Repository(repositoryModeMask, title);
            repository.addPropertyChangeListener(this);
        }

        DialogDescriptor dd = new DialogDescriptor(repository.getPanel(), org.openide.util.NbBundle.getMessage(PullOtherAction.class, "CTL_PullDialog_Title")); //NOI18N
        dd.setModal(true);
        pullButton = new JButton(org.openide.util.NbBundle.getMessage(PullOtherAction.class, "CTL_Pull_Action_Pull")); // NOI18N
        pullButton.setEnabled(false);
        dd.setOptions(new Object[] {pullButton, org.openide.util.NbBundle.getMessage(PullOtherAction.class, "CTL_Pull_Action_Cancel")}); // NOI18N
        dd.setHelpCtx(new HelpCtx(PullOtherAction.class));

        repository.getPanel().putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        dialog.addWindowListener(new DialogBoundsPreserver(HgModuleConfig.getDefault().getPreferences(), "hg.pull.dialog")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);
        if (dd.getValue() == pullButton) {
            final String pullPath = repository.getSelectedRC().getUrl();
            pull(context, root, pullPath);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Repository.PROP_VALID)) {
            pullButton.setEnabled(repository.isValid());
        }
    }

    public static void pull(final VCSContext ctx, final File root, final String pullPath) {
        if (root == null || pullPath == null) return;
        String repository = root.getAbsolutePath();
        final String fromPrjName = NbBundle.getMessage(PullAction.class, "MSG_EXTERNAL_REPOSITORY"); // NOI18N
        final String toPrjName = HgProjectUtils.getProjectName(root);
         
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
                        public void perform() { 
                            PullAction.performPull(PullAction.PullType.OTHER, ctx, root, pullPath, fromPrjName, toPrjName); } };

        support.start(rp, repository, 
                org.openide.util.NbBundle.getMessage(PullAction.class, "MSG_PULL_PROGRESS")); // NOI18N
    }
    
    public boolean isEnabled() {
        File root = HgUtils.getRootFile(context);
        if(root == null)
            return false;
        else
            return true;
    }
}
