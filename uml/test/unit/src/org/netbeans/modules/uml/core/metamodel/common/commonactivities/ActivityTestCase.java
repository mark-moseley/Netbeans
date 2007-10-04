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
 * Created on Sep 29, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class ActivityTestCase extends AbstractUMLTestCase
{
	private IActivity activity = null; 

	protected void setUp()
	{
		activity = factory.createActivity(null); 
        project.addElement(activity);
	}
	
	public void testAddEdge()
	{
		IActivityEdge edge = (IActivityEdge)FactoryRetriever.instance().createType("ControlFlow", null);
		//edge.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(edge);
		
		activity.addEdge(edge);
		ETList<IActivityEdge> edges = activity.getEdges();
		assertNotNull(edges);
				
		Iterator iter = edges.iterator();
		while (iter.hasNext())
		{
			IActivityEdge edgeGot = (IActivityEdge)iter.next();
			assertEquals(edge.getXMIID(), edgeGot.getXMIID());							
		}
		
		//Remove Input
		activity.removeEdge(edge);
		edges = activity.getEdges();
		if (edges != null)
		{
			assertEquals(0,edges.size());
		}
	}
	
	public void testAddGroup()
	{
		IActivityGroup group = (IActivityGroup)FactoryRetriever.instance().createType("ActivityPartition", null);
		//group.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(group);
		
		activity.addGroup(group);
		ETList<IActivityGroup> groups = activity.getGroups();
		assertNotNull(groups);
				
		Iterator iter = groups.iterator();
		while (iter.hasNext())
		{
			IActivityGroup groupGot = (IActivityGroup)iter.next();
			assertEquals(group.getXMIID(), groupGot.getXMIID());							
		}
		
		//Remove Input
		activity.removeGroup(group);
		groups = activity.getGroups();
		if (groups != null)
		{
			assertEquals(0,groups.size());
		}
	}
	
	public void testAddNode()
	{
		IActivityNode node = (IActivityNode)FactoryRetriever.instance().createType("InvocationNode", null);
		//node.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(node);
    
		activity.addNode(node);
		ETList<IActivityNode> nodes = activity.getNodes();
		assertNotNull(nodes);
				
		Iterator iter = nodes.iterator();
		while (iter.hasNext())
		{
			IActivityNode nodeGot = (IActivityNode)iter.next();
			assertEquals(node.getXMIID(), nodeGot.getXMIID());							
		}
		
		//Remove Input
		activity.removeNode(node);
		nodes = activity.getNodes();
		if (nodes != null)
		{
			assertEquals(0,nodes.size());
		}
	}
	
	public void testAddPartition()
	{
		IActivityPartition partition = (IActivityPartition)FactoryRetriever.instance().createType("ActivityPartition", null);
		//partition.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(partition);
    
		activity.addPartition(partition);
		ETList<IActivityPartition> partitions = activity.getPartitions();
		assertNotNull(partitions);
				
		Iterator iter = partitions.iterator();
		while (iter.hasNext())
		{
			IActivityPartition partitionGot = (IActivityPartition)iter.next();
			assertEquals(partition.getXMIID(), partitionGot.getXMIID());							
		}
		
		//Remove Input
		activity.removePartition(partition);
		partitions = activity.getPartitions();
		if (partitions != null)
		{
			assertEquals(0,partitions.size());
		}
	}
	
	public void testSetIsSingleCopy()
	{
		activity.setIsSingleCopy(true);
		assertTrue(activity.getIsSingleCopy());
	}
	
	public void testSetKind()
	{
		activity.setKind(1);
		assertEquals(1,activity.getKind());
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(ActivityTestCase.class);
	}
	
	
}



