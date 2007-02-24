
/*
 * Created on Sep 26, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class TransitionTestCase extends AbstractUMLTestCase
{
	private ITransition transition = null;
	
	public TransitionTestCase()
	{
		super();		
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(TransitionTestCase.class);
	}
	
	protected void setUp()
	{
		transition = factory.createTransition(null);
		project.addElement(transition);
	}
	
	public void testAddReferredOperation()
	{
		IClassifier cl = createClass("Trellis");
		IOperation  op = cl.createOperation("int", "almond");
		cl.addOperation(op);
		
		//add and get
		transition.addReferredOperation(op);
		ETList<IOperation> operations = transition.getReferredOperations();
		assertNotNull(operations);
				
		Iterator iter = operations.iterator();
		while (iter.hasNext())
		{
			IOperation opGot = (IOperation)iter.next();
			assertEquals(op.getXMIID(), opGot.getXMIID());							
		}
		
		//Remove Input
		transition.removeReferredOperation(op);
		operations = transition.getReferredOperations();
		if (operations != null)
		{
			assertEquals(0,operations.size());
		}
	}
	
	public void testSetContainer()
	{
		IRegion region = (IRegion)FactoryRetriever.instance().createType("Region", null);
		//region.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(region);
		 
		transition.setContainer(region);
		IRegion regionGot = transition.getContainer();
		assertEquals(region.getXMIID(), regionGot.getXMIID());
	}
	
	public void testSetEffect()
	{
		IProcedure procedure = (IProcedure)FactoryRetriever.instance().createType("Procedure", null);
		//procedure.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(procedure);
		
		transition.setEffect(procedure);
		IProcedure procedureGot = transition.getEffect();
		assertEquals(procedure.getXMIID(), procedureGot.getXMIID());
	}
	
	public void testSetGuard()
	{
		IConstraint constraint = factory.createConstraint(null);
		project.addElement(constraint);
		
		transition.setGuard(constraint);
		IConstraint constraintGot = transition.getGuard();
		assertEquals(constraint.getXMIID(), constraintGot.getXMIID());
	}
	
	public void testSetInternal()
	{
		transition.setIsInternal(true);
		assertTrue(transition.getIsInternal());		
	}
	
	public void testSetPostCondition()
	{
		IConstraint constraint = factory.createConstraint(null);
		project.addElement(constraint);
		
		transition.setPostCondition(constraint);
		IConstraint constraintGot = transition.getPostCondition();
		assertEquals(constraint.getXMIID(), constraintGot.getXMIID());		
	}
	
	public void testSetPreCondition()
	{
		IConstraint constraint = factory.createConstraint(null);
		project.addElement(constraint);
		
		transition.setPreCondition(constraint);
		IConstraint constraintGot = transition.getPreCondition();
		assertEquals(constraint.getXMIID(), constraintGot.getXMIID());		
	}
	
	public void testSetSource()
	{
		IStateVertex stateVertex = (IStateVertex)FactoryRetriever.instance().createType("StateVertex", null);
		//stateVertex.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(stateVertex); 
		
		transition.setSource(stateVertex);
		IStateVertex stateVertexGot = transition.getSource();
		assertEquals(stateVertex.getXMIID(), stateVertexGot.getXMIID());	
	}
	
	
	public void testSetTarget()
	{
		IStateVertex stateVertex = (IStateVertex)FactoryRetriever.instance().createType("StateVertex", null);
		//stateVertex.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(stateVertex); 
		
		transition.setTarget(stateVertex);
		IStateVertex stateVertexGot = transition.getTarget();
		assertEquals(stateVertex.getXMIID(), stateVertexGot.getXMIID());	
	}
	
	public void testSetTrigger()
	{
		IEvent event = (IEvent)FactoryRetriever.instance().createType("Event", null);
//		{			
//			public void establishNodePresence(Document doc, Node node)
//			{
//				super.buildNodePresence("UML:Event", doc, node);
//			}
//		};
//		event.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (event == null)
		{
			return;
		}
		project.addElement(event);
		
		transition.setTrigger(event);
		IEvent eventGot = transition.getTrigger();
		assertEquals(event.getXMIID(), eventGot.getXMIID());
	}	
}



