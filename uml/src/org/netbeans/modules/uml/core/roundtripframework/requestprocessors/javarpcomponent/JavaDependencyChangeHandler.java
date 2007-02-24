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
 * Created on Nov 13, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;

/**
 * @author aztec
 *
 */
public class JavaDependencyChangeHandler extends JavaChangeHandler
									     implements IJavaDependencyChangeHandler
{
	public JavaDependencyChangeHandler()
	{
		super();
	}
	
	public JavaDependencyChangeHandler(IJavaChangeHandler copy)
	{
		super(copy);
	}

	public void handleRequest(RequestValidator requestValidator)
	{
		//C++ method is empty
	}

	public IChangeRequest createRequest(IClassifier pDependent, IClassifier pIndependent)
	{
		//C++ method is empty
		return null;
	}
					
}



