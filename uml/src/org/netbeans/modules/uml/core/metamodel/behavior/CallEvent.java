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

/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Event;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;

/**
 * @author aztec
 *
 */
public class CallEvent extends Event implements ICallEvent
{
	public CallEvent()
	{
	}
	/**
	 * Gets the operation whose invocation raised the call event.
	 *
	 * @return IOperation
	 */
	public IOperation getOperation()
	{
		return new ElementCollector< IOperation >().
						retrieveSingleElementWithAttrID(this,"operation", IOperation.class);
	}

	/**
	 * Sets the operation whose invocation raised the call event
	 *
	 * @param oper[in]
	 */
	public void setOperation( IOperation oper )
	{
		setElement( oper, "operation" );
	}
	
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:CallEvent",doc,parent);
	}
}



