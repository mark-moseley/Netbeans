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
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for ObjectNode.
 */
public class ObjectNodeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ObjectNodeTestCase.class);
    }

    private IObjectNode node;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        node = (IObjectNode)FactoryRetriever.instance().createType("SignalNode", null);
        //node.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(node);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        node.delete();
    }

    
    public void testAddInState()
    {
        IState state = factory.createState(null);
        project.addElement(state);
        node.addInState(state);
        assertEquals(1, node.getInStates().size());
        assertEquals(state.getXMIID(), node.getInStates().get(0).getXMIID());
    }

    public void testRemoveInState()
    {
        testAddInState();
        node.removeInState(node.getInStates().get(0));
        assertEquals(0, node.getInStates().size());
    }
    
    public void testGetInStates()
    {
        // Tested by testAddInState.
    }
    

    public void testSetOrdering()
    {
        node.setOrdering(BaseElement.OOK_FIFO);
        assertEquals(BaseElement.OOK_FIFO, node.getOrdering());
        node.setOrdering(BaseElement.OOK_LIFO);
        assertEquals(BaseElement.OOK_LIFO, node.getOrdering());
        node.setOrdering(BaseElement.OOK_ORDERED);
        assertEquals(BaseElement.OOK_ORDERED, node.getOrdering());
        node.setOrdering(BaseElement.OOK_UNORDERED);
        assertEquals(BaseElement.OOK_UNORDERED, node.getOrdering());
    }

    public void testGetOrdering()
    {
        // Tested by testSetOrdering.
    }

    public void testSetSelection()
    {
        IBehavior b = factory.createActivity(null);
        project.addElement(b);
        node.setSelection(b);
        assertEquals(b.getXMIID(), node.getSelection().getXMIID());
    }

    public void testGetSelection()
    {
        // Tested by testSetSelection.
    }

    public void testSetUpperBound()
    {
        IValueSpecification spec = factory.createExpression(null);
        project.addElement(spec);
        node.setUpperBound(spec);
        assertEquals(spec.getXMIID(), node.getUpperBound().getXMIID());
    }

    public void testGetUpperBound()
    {
        // Tested by testSetUpperBound.
    }
}