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


package org.netbeans.modules.uml.ui.swing.propertyeditor;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INameCollisionHandler;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IPropertyEditorCollisionHandler extends INameCollisionHandler
{
	/**
	 * The parent property editor.
	*/
	public IPropertyEditor getPropertyEditor();

	/**
	 * The parent property editor.
	*/
	public void setPropertyEditor( IPropertyEditor value );

	public long onPreNameCollision(INamedElement element, String proposedName, 
																			  ETList<INamedElement> collidingElements, 
																			  IResultCell cell);

	public long onNameCollision(INamedElement element, ETList<INamedElement> collidingElements, IResultCell cell);

	public long listenerDisabled();

	/**
	 * Fired whenever the alias name of the passed in element is about to change.
	*/
	public long onPreAliasNameModified( INamedElement element, String proposedName, IResultCell cell );

	/**
	 * Fired whenever the element's alias name has changed.
	*/
	public long onAliasNameModified( INamedElement element, IResultCell cell );

}
