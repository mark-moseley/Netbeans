/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.core.roundtripframework.codegeneration;

import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;

public interface ICodeGeneration
{
	/**
	 * Get/Set the RoundTripController that this CodeGeneration object listens to
	*/
	public IRoundTripController getRoundTripController();

	/**
	 * Get/Set the RoundTripController that this CodeGeneration object listens to
	*/
	public void setRoundTripController( IRoundTripController value );

	/**
	 * Gets / Sets DataFormatter
	*/
	public IDataFormatter getDataFormatter();

	/**
	 * Gets / Sets DataFormatter
	*/
	public void setDataFormatter( IDataFormatter value );

	/**
	 * Adds a ChangeNotificationFilter
	*/
	public int addChangeNotificationFilter( ICodeGenerationChangeNotificationFilter pFilter );

	/**
	 * Removes a CodeGenerationChangeRequest filter
	*/
	public long removeChangeNotificationFilter( int Cookie );

	/**
	 * Determines if an ICGChangeNotification should be filtered
	*/
	public boolean shouldFilterNotification( ICGChangeNotification pCGChangeNotification );

}
