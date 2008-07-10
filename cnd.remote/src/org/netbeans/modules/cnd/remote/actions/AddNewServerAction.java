/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.actions;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.ui.AddServerDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author gordonp
 */
public class AddNewServerAction extends NodeAction implements PropertyChangeListener {
    
    protected JButton ok;
    
    public String getName() {
        return NbBundle.getMessage(AddNewServerAction.class, "LBL_AddNewServer");
    }

    public void performAction(Node[] nodes) {
        AddServerDialog dlg = new AddServerDialog();
        dlg.addPropertyChangeListener(AddServerDialog.PROP_VALID, this);
        ok = new JButton(NbBundle.getMessage(AddNewServerAction.class, "BTN_OK"));
        ok.setEnabled(dlg.isOkValid());
        DialogDescriptor dd = new DialogDescriptor((Object) dlg, NbBundle.getMessage(AddNewServerAction.class, "TITLE_AddNewServer"), true, 
                    new Object[] { ok, DialogDescriptor.CANCEL_OPTION},
                    DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == ok) {
            String entry = dlg.getLoginName() + '@' + dlg.getServerName();
            RemoteServerList registry = RemoteServerList.getInstance();
            if (!registry.contains(entry)) {
                registry.add(entry);
                if (dlg.isDefault()) {
                    registry.setDefaultIndex(registry.size() - 1);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AddServerDialog.PROP_VALID)) {
            AddServerDialog dlg = (AddServerDialog) evt.getSource();
            ok.setEnabled(dlg.isOkValid());
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public boolean asynchronous() {
        return false;
    }
}
