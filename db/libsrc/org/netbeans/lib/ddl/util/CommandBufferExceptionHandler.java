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

package com.netbeans.ddl.util;

import java.sql.*;
import com.netbeans.ddl.*;

/** 
* Interface of command buffer handler. When you use CommandBuffer to execute
* a bunch of command and any error occures during the execution, this handler
* catches it and lets user to decide if continue or not (when you're dropping
* nonexisting table, you probably would like to continue).
*
* @author Slavek Psenicka
*/
public interface CommandBufferExceptionHandler {

	/** Solves exception situation 
	* Returns true if catched exception does not affect data consistency.
	* @param ex Thrown exception
	*/
	public boolean shouldContinueAfterException(Exception ex);
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
