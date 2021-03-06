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

package org.netbeans.modules.websvc.registry.actions;

import java.awt.Dialog;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;

import org.netbeans.modules.websvc.registry.nodes.*;
import org.netbeans.modules.websvc.registry.nodes.WebServiceGroupNode;
import org.openide.nodes.Node;

/** Add a webservice group node to the root node
 * @author  Winston Prakash
 */
public class AddWebServiceGroupAction extends NodeAction {
    
    protected boolean enable(org.openide.nodes.Node[] node) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(AddWebServiceGroupAction.class, "ADD_GROUP");
    }
    
    protected void performAction(Node[] nodes) {
        WebServiceListModel wsNodeModel = WebServiceListModel.getInstance();
        AddWSGroupPanel innerPanel = new AddWSGroupPanel();
        DialogDescriptor dialogDesc = new DialogDescriptor(innerPanel,
                NbBundle.getMessage(AddWebServiceGroupAction.class, "TTL_AddWSGroup"));
        MyDocListener dl = new MyDocListener(dialogDesc,wsNodeModel);
        innerPanel.getTFDocument().addDocumentListener(dl);      
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialogDesc.setValid(false);
        dialog.setVisible(true);
        if (NotifyDescriptor.OK_OPTION.equals(dialogDesc.getValue())) {
            WebServiceGroup wsGroup =  new WebServiceGroup();
            String groupName = innerPanel.getGroupName();
            wsGroup.setName(groupName);
            wsNodeModel.addWebServiceGroup(wsGroup); 
        }
        innerPanel.getTFDocument().removeDocumentListener(dl);
        dialog.dispose();
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
    /** Listener that checks if group name is not empty or duplicite
     */
    private class MyDocListener implements DocumentListener {
        
        private DialogDescriptor dd;
        private WebServiceListModel wsNodeModel;
        
        MyDocListener(DialogDescriptor dd, WebServiceListModel wsNodeModel) {
            this.dd=dd;
            this.wsNodeModel=wsNodeModel;
        }
              
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }
        
        public void update(javax.swing.event.DocumentEvent e) {
            Document doc = e.getDocument();
            try {
                String text = doc.getText(0,doc.getLength()).trim();
                if (text.length()==0 || wsNodeModel.findWebServiceGroup(text) !=null) {
                    dd.setValid(false);
                    return;
                }
            } catch (BadLocationException ex){}
            dd.setValid(true);
        }
        
    }
    
}
