/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package com.netbeans.enterprise.modules.db.explorer.actions;

import java.sql.*;
import com.netbeans.ide.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;

public class DisableDebugAction extends DatabaseAction
{
	protected boolean enable(Node[] activatedNodes)
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return false;
		DatabaseNodeInfo nfo = findInfo((DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class));
		return nfo.isDebugMode();
	}

	public void performAction (Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		DatabaseNodeInfo nfo = findInfo((DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class));
		nfo.setDebugMode(false);
		DriverManager.setLogStream(null);
	}
}