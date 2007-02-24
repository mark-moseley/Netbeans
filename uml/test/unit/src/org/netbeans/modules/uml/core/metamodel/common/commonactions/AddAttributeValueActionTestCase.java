package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
/**
 * Test cases for AddAttributeValueAction.
 */
public class AddAttributeValueActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AddAttributeValueActionTestCase.class);
    }

    private IAddAttributeValueAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        act = (IAddAttributeValueAction)FactoryRetriever.instance().createType("AddAttributeValueAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        act.delete();
    }
    
    public void testSetInsertAt()
    {
        IInputPin pin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        act.setInsertAt(pin);
        assertEquals(pin.getXMIID(), act.getInsertAt().getXMIID());
    }

    public void testGetInsertAt()
    {
        // Tested by testSetInsertAt.
    }

    public void testSetIsReplaceAll()
    {
        act.setIsReplaceAll(true);
        assertTrue(act.getIsReplaceAll());
        act.setIsReplaceAll(false);
        assertFalse(act.getIsReplaceAll());
    }

    public void testGetIsReplaceAll()
    {
        // Tested by testSetIsReplaceAll.
    }
}