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

package com.netbeans.enterprise.modules.db.explorer.infos;

import java.sql.*;
import java.util.*;
import java.io.IOException;
import com.netbeans.ddl.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.ddl.impl.*;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class IndexNodeInfo extends TableNodeInfo
{
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
//			DatabaseMetaData dmd = getConnection().getMetaData();
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String table = (String)get(DatabaseNode.TABLE);
//			ResultSet rs = dmd.getIndexInfo(catalog,getUser(),table, true, false);

//je to BARBARSTVI, po beta 6 rozumne prepsat
ResultSet rs;
if (dmd.getDatabaseProductName().trim().equals("ACCESS"))
	rs = dmd.getIndexInfo(catalog, null, table, true, false);
else
	rs = dmd.getIndexInfo(catalog, dmd.getUserName(), table, true, false);
			
			Hashtable ixmap = new Hashtable();
			while (rs.next()) {
//				System.out.println("index column "+rs.getString("INDEX_NAME"));
				String ixname = (String)get("index");
				DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEXCOLUMN, rs);
				String newixname = (String)info.get("ixname");
				if (ixname != null && newixname != null && newixname.equals(ixname)) {
					String way = (String)info.get("ord");
					if (way == null) way = "A";
					info.put(DatabaseNodeInfo.ICONBASE, info.get(DatabaseNodeInfo.ICONBASE+way));
					if (info != null) children.add(info);
					else throw new Exception("unable to create node information for index");
				}
			}
			rs.close();
 		} catch (Exception e) {
 			e.printStackTrace();
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void refreshChildren() throws DatabaseException
	{
		Vector charr = new Vector();
		DatabaseNodeChildren chil = (DatabaseNodeChildren)getNode().getChildren();

		put(DatabaseNodeInfo.CHILDREN, charr);
		chil.remove(chil.getNodes());		
		initChildren(charr);
		Enumeration en = charr.elements();
		while(en.hasMoreElements()) {
			DatabaseNode subnode = chil.createNode((DatabaseNodeInfo)en.nextElement());
			chil.add(new Node[] {subnode});
		}
	}

	public void delete()
	throws IOException
	{
		try {
			String code = getCode();
			String table = (String)get(DatabaseNode.TABLE);
			Specification spec = (Specification)getSpecification();
			AbstractCommand cmd = spec.createCommandDropIndex(getName());
			cmd.execute();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}	
}

/*
 * <<Log>>
 *  11   Gandalf   1.10        11/15/99 Radko Najman    MS ACCESS
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/12/99 Radko Najman    debug messages removed
 *  8    Gandalf   1.7         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  7    Gandalf   1.6         9/13/99  Slavek Psenicka 
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka adaptor changes
 *  5    Gandalf   1.4         8/19/99  Slavek Psenicka English
 *  4    Gandalf   1.3         6/15/99  Slavek Psenicka debug prints
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
