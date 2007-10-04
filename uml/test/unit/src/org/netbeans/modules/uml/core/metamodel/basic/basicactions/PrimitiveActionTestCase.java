/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */



/*
 * Created on Sep 25, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class PrimitiveActionTestCase extends AbstractUMLTestCase
{
	private IValueSpecification argument = null;
	private IPrimitiveAction action = null; 
	public PrimitiveActionTestCase()
	{
		super();		
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(PrimitiveActionTestCase.class);
	}

	protected void setUp() throws Exception
	{
		action = (IPrimitiveAction)FactoryRetriever.instance().createType("PrimitiveAction", null);
//		 {			
//			public void establishNodePresence(Document doc, Node node)
//			{
//				super.buildNodePresence("UML:PrimitiveAction", doc, node);
//			}
//		};
//		action.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (action != null)
		{		
			project.addElement(action);
		}
		argument = factory.createExpression(null);
		project.addElement(argument);	
	}
	public void testAddArgument()
	{
		if (action == null)
		{
			return;	
		}
		//Add & get Arguments
		action.addArgument(argument);
		ETList<IValueSpecification> arguments = action.getArguments();		
		assertNotNull(arguments);
						
		Iterator iter = arguments.iterator();
		while (iter.hasNext())
		{
			IValueSpecification argumentGot = (IValueSpecification)iter.next();
			assertEquals(argument.getXMIID(), argumentGot.getXMIID());							
		}
				
		//Remove Argument
		action.removeArgument(argument);
		arguments = action.getArguments();
		if (arguments != null)
		{
			assertEquals(0,arguments.size());
		}			
	}
	
	public void testSetTarget()
	{
		if (action == null)
		{
			return;	
		}
		action.setTarget(argument);
		IValueSpecification argumentGot = action.getTarget();
		assertNotNull(argumentGot);
		assertEquals(argument.getXMIID(), argumentGot.getXMIID());
	}
}



