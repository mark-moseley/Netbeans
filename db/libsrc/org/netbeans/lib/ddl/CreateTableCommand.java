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

package com.netbeans.ddl;

/** 
* Interface of create table action command. 
*
* @author Slavek Psenicka
*/
public interface CreateTableCommand extends DDLCommand, OwnedObjectCommand {
	
	/** Returns column specification array 
	* @param type Type of command.
	* @param name Name of command.
	*/
	public TableColumnDescriptor createColumn(String type, String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException;
}

/*
* <<Log>>
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
