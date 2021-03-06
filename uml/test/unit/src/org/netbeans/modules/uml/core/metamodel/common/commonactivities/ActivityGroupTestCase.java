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


package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for ActivityGroup.
 */
public class ActivityGroupTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActivityGroupTestCase.class);
    }

    private IActivityGroup activityGroup;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
  
		activityGroup = (IActivityGroup)FactoryRetriever.instance().createType("ActivityPartition", null);
		//activityGroup.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(activityGroup);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        //activityGroup.delete();
    }

    
    public void testSetActivity()
    {
		IActivity activity = factory.createActivity(null);
		project.addElement(activity);
		
		activityGroup.setActivity(activity);
		IActivity activityGot = activityGroup.getActivity();
		assertNotNull(activityGot);
		assertEquals(activity.getXMIID(), activityGot.getXMIID()); 
    }

    public void testAddEdgeContent()
    {
		IActivityEdge edge = (IActivityEdge)FactoryRetriever.instance().createType("ControlFlow", null);
		//edge.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(edge);
		
		activityGroup.addEdgeContent(edge);
		ETList<IActivityEdge> edges = activityGroup.getEdgeContents();
		assertNotNull(edges);
				
		Iterator iter = edges.iterator();
		while (iter.hasNext())
		{
			IActivityEdge edgeGot = (IActivityEdge)iter.next();
			assertEquals(edge.getXMIID(), edgeGot.getXMIID());							
		}
		
		//Remove Input
		activityGroup.removeEdgeContent(edge);
		edges = activityGroup.getEdgeContents();
		if (edges != null)
		{
			assertEquals(0,edges.size());
		}
    }

    public void testAddNodeContent()
    {
		IActivityNode node = (IActivityNode)FactoryRetriever.instance().createType("InvocationNode", null);
		//node.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(node);
		
		activityGroup.addNodeContent(node);
		ETList<IActivityNode> nodes = activityGroup.getNodeContents();
		assertNotNull(nodes);
				
		Iterator iter = nodes.iterator();
		while (iter.hasNext())
		{
			IActivityNode nodeGot = (IActivityNode)iter.next();
			assertEquals(node.getXMIID(), nodeGot.getXMIID());							
		}
		
		//Remove
		activityGroup.removeNodeContent(node);
		nodes = activityGroup.getNodeContents();
		if (nodes != null)
		{
			assertEquals(0,nodes.size());
		}
		
		
    }

    public void testAddSubGroup()
    {
		IActivityGroup group = (IActivityGroup)FactoryRetriever.instance().createType("ActivityPartition", null);
		//group.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(group);
		
		activityGroup.addSubGroup(group);
		ETList<IActivityGroup> groups = activityGroup.getSubGroups();
		assertNotNull(groups);
				
		Iterator iter = groups.iterator();
		while (iter.hasNext())
		{
			IActivityGroup groupGot = (IActivityGroup)iter.next();
			assertEquals(group.getXMIID(), groupGot.getXMIID());							
		}
		
		//Remove
		activityGroup.removeSubGroup(group);
		groups = activityGroup.getSubGroups();
		if (groups != null)
		{
			assertEquals(0,groups.size());
		}
		
    }
}
