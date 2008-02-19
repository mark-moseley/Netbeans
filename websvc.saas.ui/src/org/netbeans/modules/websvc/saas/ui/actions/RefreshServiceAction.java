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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.saas.ui.actions;

import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Action that refreshes a web service from its original wsdl location.
 * 
 * @author quynguyen
 */
public class RefreshServiceAction extends NodeAction {

    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        WsdlSaas saas = nodes[0].getLookup().lookup(WsdlSaas.class);
        return saas != null && saas.getWsdlData() != null && saas.getState() != Saas.State.INITIALIZING;
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/websvc/saas/ui/resources/ActionIcon.gif"; // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(RefreshServiceAction.class, "REFRESH");
    }

    protected void performAction(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return;
        }
        
        final WsdlSaas saas = nodes[0].getLookup().lookup(WsdlSaas.class);
        if (saas == null || saas.getUrl() == null) {
            throw new IllegalArgumentException("Node has no aasociated good Saas");
        }
        if (saas.getState() == Saas.State.INITIALIZING) {
            throw new IllegalStateException("Saas is initializing");
        }

        String msg = NbBundle.getMessage(RefreshServiceAction.class, "WS_REFRESH");
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
        Object response = DialogDisplayer.getDefault().notify(d);
        if (null != response && response.equals(NotifyDescriptor.YES_OPTION)) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    saas.refresh();
                }
            });
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
