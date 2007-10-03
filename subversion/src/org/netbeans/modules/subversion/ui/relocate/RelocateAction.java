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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.subversion.ui.relocate;

import java.awt.Dialog;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author  Peter Pis
 */
public class RelocateAction extends ContextAction {
         
    protected boolean enable(Node[] nodes) {
        final Context ctx = getContext(nodes);
        File[] roots = ctx.getRootFiles();
        if(roots == null || roots.length != 1) {
            return false;
        }
        File file = roots[0];
        return file != null && file.isDirectory();
    }
    
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    private String getCurrentURL(File root) {       
        try {
            SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
            return repositoryUrl.toString();
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);            
        }                            
        return "";
    }        
    
    private String getWorkingCopy(File root) {
        final String working = root.getAbsolutePath();
        return working;
    }
    
    public void validate(RelocatePanel panel, JButton btnOk) {
        try {
            new SVNUrl(panel.getNewURL().getText());
            btnOk.setEnabled(true);
        } catch (MalformedURLException e) {
            btnOk.setEnabled(false);
        }
        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_Relocate_Title";
    }

    protected void performContextAction(Node[] nodes) {
        ResourceBundle loc = NbBundle.getBundle(RelocateAction.class);
        
        final Context ctx = getContext(nodes);
        File[] roots = ctx.getRootFiles();
        if (roots == null) {
            return;
        }
        
        final RelocatePanel panel = new RelocatePanel();
        panel.getCurrentURL().setText(getCurrentURL(roots[0]));
        panel.getWorkingCopy().setText(getWorkingCopy(roots[0]));
        
        final JButton btnRelocate = new JButton(loc.getString("CTL_Relocate_Action_Name"));
        btnRelocate.setEnabled(false);
        btnRelocate.setToolTipText(loc.getString("TT_Relocate_Action"));
        
        panel.getNewURL().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                validate(panel, btnRelocate);
            }

            public void removeUpdate(DocumentEvent event) {
                validate(panel, btnRelocate);
            }

            public void changedUpdate(DocumentEvent event) {
                validate(panel, btnRelocate);
            }          
        });
        JButton btnCancel = new JButton(loc.getString("CTL_Relocate_Action_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RelocateAction.class, "ACSD_Relocate_Action_Cancel"));  // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(panel,  loc.getString("CTL_Relocate_Dialog_Title"),  true, new Object [] {btnRelocate, btnCancel}, btnRelocate, DialogDescriptor.BOTTOM_ALIGN, null, null);
        descriptor.setClosingOptions(null);
        descriptor.setHelpCtx(new HelpCtx(RelocateAction.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(loc.getString("ACSD_Relocate"));
        
        dialog.setVisible(true);
        if (descriptor.getValue() != btnRelocate) 
            return;
        
        final String newUrl = panel.getNewURL().getText();
        
        final SVNUrl repositoryUrl;
        try {            
            repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }                  
        final String wc = roots[0].getAbsolutePath();
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport() {
            SvnClient client = null;
            protected void perform() {                    
                try {
                    client = Subversion.getInstance().getClient(repositoryUrl);
                    client.relocate(repositoryUrl.toString(), newUrl, wc, true);
                } catch (SVNClientException ex) {
                    annotate(ex);
                } 
            }
        };
        support.start(rp, repositoryUrl, loc.getString("LBL_Relocate_Progress"));
    }
}