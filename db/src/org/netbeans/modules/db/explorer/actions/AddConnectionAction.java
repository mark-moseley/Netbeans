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

import java.io.*;
import java.beans.*;
import java.util.*;
import java.sql.*;
import com.netbeans.ide.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ddl.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.dlg.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;

public class AddConnectionAction extends DatabaseAction
{
	public void performAction (Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		try {
			DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			ConnectionOwnerOperations nfo = (ConnectionOwnerOperations)info.getParent(nodename);

			Vector drvs = RootNode.getOption().getAvailableDrivers();
			DatabaseConnection cinfo = new DatabaseConnection();
			
			cinfo.setDatabase("jdbc:sybase:Tds:localhost:2638");
			cinfo.setUser("dba");
			
			NewConnectionDialog cdlg = new NewConnectionDialog(drvs, cinfo);
			if (cdlg.run()) nfo.addConnection((DBConnection)cinfo);
		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to perform action, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}