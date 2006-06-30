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

/**
 * SaveAction
 *
 *
 * Created: Wed Mar  1 16:59:48 2000
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class SaveAction extends NodeAction {

    public SaveAction() {}

    /**
     * Sets the name of the action
     */
    public String getName() {
	return NbBundle.getBundle(SaveAction.class).getString("MON_Save_20");
    }

    /**
     * Not implemented
     */
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }

    public void performAction() { 
	Node[] nodes = getActivatedNodes();
	TransactionView.getInstance().saveTransaction(nodes);
    }

    public void performAction(Node[] nodes) { 
	TransactionView.getInstance().saveTransaction(nodes);
    }

    public boolean enable(Node[] nodes) {
	return true;
    }

    public boolean asynchronous() { 
	return false; 
    } 
} // SaveAction
