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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface INamedElementEventsSink
{
	/**
	 * Fired whenever the name of the passed in element is about to change.
	*/
	public void onPreNameModified( INamedElement element, String proposedName, IResultCell cell );

	/**
	 * Fired whenever the element's name has changed.
	*/
	public void onNameModified( INamedElement element, IResultCell cell );

	/**
	 * Fired whenever the visibility value of the passed in element is about to change.
	*/
	public void onPreVisibilityModified( INamedElement element, /* VisibilityKind */ int proposedValue, IResultCell cell );

	/**
	 * Fired whenever the visibility value of the passed in element has changed.
	*/
	public void onVisibilityModified( INamedElement element, IResultCell cell );

	/**
	 * Fired whenever the alias name of the passed in element is about to change.
	*/
	public void onPreAliasNameModified( INamedElement element, String proposedName, IResultCell cell );

	/**
	 * Fired whenever the element's alias name has changed.
	*/
	public void onAliasNameModified( INamedElement element, IResultCell cell );

	/**
	 * Fired whenever the name of element is about to change to the name of an existing element.
	*/
	public void onPreNameCollision( INamedElement element, String proposedName, ETList<INamedElement> collidingElements, IResultCell cell );

	/**
	 * Fired whenever the name of element has changed to the name of an existing element.
	*/
	public void onNameCollision( INamedElement element, ETList<INamedElement> collidingElements, IResultCell cell );

}
