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
 * NavigateNode.java
 *
 *
 * Created: Fri May 19 17:02:05 2000
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.*;
import org.openide.util.NbBundle;

public class NavigateNode extends AbstractNode {

    public NavigateNode(Children ch) {
	super(ch);
	setIconBase("org/netbeans/modules/web/monitor/client/icons/folder"); //NOI18N
	setName(NbBundle.getBundle(NavigateNode.class).getString("MON_All_transactions_2"));
	//initialize();
    }

    /* Getter for set of actions that should be present in the
     * popup menu of this node. This set is used in construction of
     * menu returned from getContextMenu and specially when a menu for
     * more nodes is constructed.
     *
     * @return array of system actions that should be in popup menu
     */

    protected SystemAction[] createActions () {
	return new SystemAction[] {
            SystemAction.get(ReloadAction.class),
	    SystemAction.get(DeleteAllAction.class),
            null,
            SystemAction.get(SortAction.class),
            null,
            SystemAction.get(ShowTimestampAction.class),
	};
    }
    
} // NavigateNode
