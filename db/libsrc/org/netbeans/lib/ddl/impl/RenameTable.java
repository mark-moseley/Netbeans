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

package com.netbeans.ddl.impl;

import java.util.*;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;

/** 
* Rename table command. Encapsulates name and new name of table.
*
* @author Slavek Psenicka
*/

public class RenameTable extends AbstractCommand 
{
	/** New name */
	private String newname;

	/** Command name */
	public static final String NEW_NAME = "object.newname";
	
	static final long serialVersionUID =-4410972392441335153L;

	/** Returns new name */
	public String getNewName()
	{
		return newname;
	}
	
	/** Sets new name */
	public void setNewName(String name)
	{
		newname = name;
	}

	/** Returns properties of command:
	* object.newname	New name of table
	*/
	public Map getCommandProperties()
	throws DDLException
	{
		Map args = super.getCommandProperties();
		args.put(NEW_NAME, newname);			
		return args;	
	}
}

/*
* <<Log>>
*  4    Gandalf   1.3         10/1/99  Radko Najman    NEW_NAME
*  3    Gandalf   1.2         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
