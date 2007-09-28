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


package org.netbeans.modules.uml.core.metamodel.structure;

import org.dom4j.Document;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.StructuredClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Node extends StructuredClassifier implements INode
{

	/**
	 * 
	 */
	public Node() 
	{
		super();		
	}
	
	public ETList<IDeployment> getDeployments()
	{
		ElementCollector<IDeployment> collector = new ElementCollector<IDeployment>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"deployment", IDeployment.class);
	}
	
	public void removeDeployment( IDeployment deployment )
	{
		final IDeployment dep = deployment;
		new ElementConnector<INode>().removeByID(this,dep,"deployment",
			new IBackPointer<INode>() 
			{
				public void execute(INode obj) 
				{
					dep.setLocation(obj);
				}
			}
		);		
	}
	
	public void addDeployment( IDeployment deploy )
	{
		final IDeployment deployment = deploy;		
		new ElementConnector<INode>().addChildAndConnect(
										this, true, "deployment", 
										"deployment", deployment,
										 new IBackPointer<INode>() 
										 {
											 public void execute(INode obj) 
											 {
												deployment.setLocation(obj);
											 }
										 }
										 );
	}
	
	public void removeDeployedElement( INamedElement element )
	{
		removeElementByID(element,"deployedElement");
	}
	
	public void addDeployedElement( INamedElement element )
	{
		addElementByID(element,"deployedElement");
	}
	
	public ETList<INamedElement> getDeployedElements()
	{
		ElementCollector<INamedElement> collector = new ElementCollector<INamedElement>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"deployedElement", INamedElement.class);
	}
	
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(org.dom4j.Document doc, org.dom4j.Node parent)
	{
		buildNodePresence("UML:Node",doc,parent);
	}	
}


