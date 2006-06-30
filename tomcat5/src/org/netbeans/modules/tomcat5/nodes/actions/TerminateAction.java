/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes.actions;

import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.nodes.TomcatInstanceNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Terminate Tomcat server action. If the Tomcat has been started from withing the
 * IDE, this action will terminate the running process.
 *
 * @author sherold
 */
public class TerminateAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie != null) {
                final TomcatManager tm = cookie.getTomcatManager();
                String name = tm.getTomcatProperties().getDisplayName();
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(TerminateAction.class, "MSG_terminate", name),
                        NotifyDescriptor.YES_NO_OPTION);
                Object retValue = DialogDisplayer.getDefault().notify(nd);
                if (retValue == DialogDescriptor.YES_OPTION) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            tm.terminate();
                            // wait a sec before refreshing the state
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ie) {}
                            tm.getInstanceProperties().refreshServerInstance();
                        }
                    });
                }
            }
        }
    }
    
    protected boolean enable(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie == null) {
                return false;
            }
            TomcatManager tm = cookie.getTomcatManager();
            if (tm == null || !tm.isRunning(false)) {
                return false;
            }
        }
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(TerminateAction.class, "LBL_TerminateAction");
    }
    
    protected boolean asynchronous() { return false; }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
