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

package com.netbeans.enterprise.modules.db.explorer.nodes;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Map;
import java.util.Iterator;
import com.netbeans.ide.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.MapFormat;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.actions.*;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;

public class DatabaseNode extends AbstractNode implements Node.Cookie
{
	private DatabaseNodeInfo info;
	private boolean writable = false;
	
	public static final String ROOT = "root";
	public static final String DRIVER_LIST = "driverlist";
	public static final String DRIVER = "driver";
	public static final String CONNECTION = "connection";
	public static final String CATALOG = "catalog";
	public static final String TABLE = "table";
	public static final String VIEW = "view";
	public static final String COLUMN = "column";
	public static final String PRIMARY_KEY = "icolumn";
	public static final String FOREIGN_KEY = "fcolumn";
	public static final String EXPORTED_KEY = "ekey";
	public static final String PROCEDURE = "procedure";
	public static final String PROCEDURE_COLUMN = "procedurecolumn";
	
	public DatabaseNode()
	{
		super(new DatabaseNodeChildren());
	}

	public DatabaseNode(Children child)
	{
		super(child);
	}

	public DatabaseNodeInfo getInfo()
	{
		return info;
	}

	public void setInfo(DatabaseNodeInfo nodeinfo)
	{
		info = (DatabaseNodeInfo)nodeinfo.clone();
		super.setName(info.getName());
		setIconBase(info.getIconBase());
		
		String fmt = info.getDisplayname();
		if (fmt != null) {
			String dname = MapFormat.format(fmt, info);
			if (dname != null) setDisplayName(dname);
		}
		
		try {
    		Vector prop = (Vector)info.get(DatabaseNodeInfo.PROPERTIES);
			Enumeration prop_i = prop.elements();
			while (prop_i.hasMoreElements()) {
				Map propmap = (Map)prop_i.nextElement();
    			if (((String)propmap.get(DatabaseNodeInfo.CODE)).equals(DatabaseNodeInfo.NAME)) {
    				writable = ((String)propmap.get(DatabaseNodeInfo.WRITABLE)).toUpperCase().equals("YES");
    			}
    		}
		} catch (Exception e) {}
	}

	public void setName(String newname)
	{
		super.setName(newname);
		info.setName(newname);
	}

	public boolean canRename()
	{
		return writable;
	}

	public Node.Cookie getCookie(Class cls) 
	{
		if (cls.isInstance(info)) return info;
    	return super.getCookie(cls);
  	}

	protected SystemAction[] createActions() 
	{
		Vector actions = info.getActions();
		SystemAction[] myacts = (SystemAction[])actions.toArray(new SystemAction[actions.size()]);
		SystemAction[] sysacts = NodeOp.getDefaultActions();
		SystemAction[] retacts = new SystemAction[sysacts.length+myacts.length];
		System.arraycopy((Object)myacts,0,(Object)retacts,0,myacts.length);
		System.arraycopy((Object)sysacts,0,(Object)retacts,myacts.length,sysacts.length);

		return retacts;
  	}

	protected Map createProperty(String name)
	{
		return null;
	}
	
	/** Sheet for this node.
	*/
	protected Sheet createSheet() 
	{
    	Sheet sheet = Sheet.createDefault();
    	Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
    	Vector prop = (Vector)info.get(DatabaseNodeInfo.PROPERTIES);
		Enumeration prop_i = prop.elements();
		while (prop_i.hasMoreElements()) {
			boolean canWrite = writable;
			Map propmap = (Map)prop_i.nextElement();
			String key = (String)propmap.get(DatabaseNodeInfo.CODE);
			
			try {

				PropertySupport psitem = null;
				String pname = null, pclass = null, pdesc = null;
				if (propmap == null) {
					propmap = createProperty(key);
					if (propmap != null) info.put(key, propmap);
				} 

				if (key.equals("name")) psitem = new PropertySupport.Name(this);
				else {
					Class pc = null;
					pname = (String)propmap.get(DatabaseNodeInfo.NAME);
					pdesc = (String)propmap.get(DatabaseNodeInfo.DESCRIPTION);
					pclass = (String)propmap.get(DatabaseNodeInfo.CLASS);
					String wflag = (String)propmap.get(DatabaseNodeInfo.WRITABLE);
					if (wflag != null) canWrite = wflag.toUpperCase().equals("YES");
					if (pclass.equals("java.lang.Boolean")) pc = Boolean.TYPE;
					else if (pclass.equals("java.lang.Integer")) pc = Integer.TYPE;
					else pc = Class.forName(pclass);
					psitem = new DatabasePropertySupport(key, pc, pname, pdesc, info, canWrite);				
				}
			
				if (psitem != null) ps.put(psitem);
				else throw new DatabaseException("no property for "+pname+" "+pclass);

			} catch (Exception e) {
				System.out.println("unable to create property "+key+" ("+e+")");
			}
		}
    	
    	return sheet;
  	}  	

	/** Deletes subnode.
	* Called by deleteNode.
	* @param node Node to delete.
	*/
	protected void deleteNode(DatabaseNode node)
	throws DatabaseException
	{
		try {
			DatabaseNodeInfo ninfo = node.getInfo();
			DatabaseNodeChildren children = (DatabaseNodeChildren)getChildren();
			info.getChildren().removeElement(ninfo);
			children.remove(new Node[] {node});
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}			
	}
	
	/** Deletes node and subnodes.
	* Called by delete actions
	*/
	public void deleteNode()
	throws DatabaseException
	{
		try {
			DatabaseNode parent = (DatabaseNode)getParentNode().getCookie(null);
			if (parent != null) parent.deleteNode(this);
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}