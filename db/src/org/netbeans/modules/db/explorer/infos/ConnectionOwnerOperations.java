/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

/**
* Interface of driver-related nodes.
* @author Slavek Psenicka
*/
public interface ConnectionOwnerOperations
{
    /** Add connection operation
    * @param drv Driver to add (null allowed)
    */
    public void addConnection(DBConnection con)
    throws DatabaseException;
}

/*
* <<Log>>
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
*  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
* $
*/
