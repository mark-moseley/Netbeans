package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 */
public class JavaMethodChangeHandlerTestCase extends AbstractUMLTestCase
    implements IRoundTripOperationEventsSink
{
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(JavaMethodChangeHandlerTestCase.class);
    }
    
    
    private IClass c;
    public static IOperation  oper;
    private String createdMethod;
    private String changedMethod;
    private String deletedMethod;
    private String changedType;
    private String changedVis;
    private String createdParam, createdParamType, deletedParam;
    private String changedParam;
    private String changedParamType;
    private String changedAbs;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.registerForRoundTripOperationEvents(this, "Java");
        
        c = createClass("Test");
        oper = c.createOperation("int", "");
        
        IClassifier type = createClass("Whale");
        IPackage p = createType("Package");
        p.setName("xyz");
        p.addOwnedElement(type);
        
        oper.setReturnType2("int");
        oper.setVisibility(IVisibilityKind.VK_PACKAGE);
        c.addOperation(oper);
        oper.setName("a");
        
        createdMethod = changedMethod = deletedMethod = null;
        createdParam  = createdParamType = null;
        changedType   = null;
        changedVis    = null;
        changedAbs    = null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AbstractUMLTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.revokeRoundTripOperationSink(this);
    }
  

 /**
 * OperationTypeChangedTestCase
 */
 
    public void testTypeChanged()
    {
        oper.setReturnType2("String");
        assertEquals("int->String", changedParamType);
    }
 
/**
 * OperationCreateTestCase
 */
 
    public void testCreate()
    {
        IOperation op = c.createOperation("int", "");
        c.addOperation(op);
        op.setName("sss");
        assertEquals("sss", createdMethod);
    }
    
 /**
 * OperationDeleteTestCase
 */
 
	public void testDelete()
    {
        oper.delete();
        assertEquals("a", deletedMethod);
    }
    
 /**
 * OperationNameChangeTestCase
 */
 
	public void testNameChanged()
    {
        oper.setName("b");
        assertEquals("a->b", changedMethod);
    }
    
 /**
 * OperationVisibilityChangeTestCase
 */
 
	public void testVisibilityChanged()
    {
        oper.setVisibility(IVisibilityKind.VK_PRIVATE);
        assertEquals(IVisibilityKind.VK_PACKAGE + "->" +
            IVisibilityKind.VK_PRIVATE, changedVis);
    }
    
 
/**
 * OperationModifiersChangeTestCase
 */
 	
	public void testModifiersChanged()
    {
        oper.setIsAbstract(true);
        assertEquals("false->true", changedAbs);
    }
    
 /**
 * OperationMovedTestCase
 */
 
	public void testMoved()
    {
        IClass targ = createClass("Target");
        oper.moveToClassifier(targ);
        assertEquals(1, targ.getOperationsByName("a").size());
        assertEquals(0, c.getOperationsByName("a").size());
        
        IParameter pm;
        IOperation op;
        IClass src = createClass("Source");
        IClass des = createClass("Destination");
        src.addOperation(op = src.createOperation("int", "delhi"));
        op.addParameter(pm = op.createParameter("int","i"));
        op.moveToClassifier(des);
        assertEquals(1, des.getOperationsByName("delhi").size());
        IOperation nOp = des.getOperationsByName("delhi").get(0);
        
        assertEquals(2, nOp.getParameters().size());
        IParameter parm = nOp.getParameters().get(1);
        
        assertEquals("i", parm.getName().trim());
    }
    
 /**
 * OperationCopiedTestCase
 */
 
	public void testCopied()
    {
        IClass targ = createClass("Target");
        oper.duplicateToClassifier(targ);
        assertEquals(1, targ.getOperationsByName("a").size());
        assertEquals(1, c.getOperationsByName("a").size());
        
        IParameter pm;
        IOperation op;
        IClass src = createClass("Source");
        IClass des = createClass("Destination");
        src.addOperation(op = src.createOperation("int", "delhi"));
        op.addParameter(pm = op.createParameter("int","i"));
        op.duplicateToClassifier(des);
        assertEquals(1, des.getOperationsByName("delhi").size());
        assertEquals(1, src.getOperationsByName("delhi").size());
        assertEquals(2, op.getParameters().size());
        IOperation nOp = des.getOperationsByName("delhi").get(0);
        assertEquals(2, nOp.getParameters().size());
        IParameter parm = nOp.getParameters().get(1);
        
        assertEquals("i", parm.getName().trim());
    }
    
 /**
 * OperationParameterAddedTestCase
 */
 
	public void testParameterAdded()
    {
        IParameter p = oper.createParameter("double", "zigzag");
        // TODO: Reintroduce these checks once the transition element problems
        //       are fixed.
//        assertNull(createdParam);
//        assertNull(createdParamType);
        
        oper.addParameter(p);
        assertEquals("double", createdParamType);
        assertEquals("zigzag", createdParam);
    }
    
 /**
 * OperationParameterRemovedTestCase
 */
 
	public void testParameterRemoved()
    {
        IParameter p = oper.createParameter("double", "zigzag");
        oper.addParameter(p);
        
        p.delete();
        assertEquals("double zigzag", deletedParam);
    }
    
 /**
 * OperationParameterNameChangeTestCase
 */
 
	public void testParameterNameChanged()
    {
        IParameter p = oper.createParameter("double", "zigzag");
        oper.addParameter(p);
        
        p.setName("zagzig");
        assertEquals("zigzag->zagzig", changedParam);
    }
    
 /**
 * OperationParameterTypeChangeTestCase
 */
 
	public void testParameterTypeChanged()
    {
        IParameter p = oper.createParameter("double", "zigzag");
        oper.addParameter(p);
        
        p.setType2("float");
        assertEquals("double->float", changedParamType);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink#onPreOperationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreOperationChangeRequest(IChangeRequest newVal, IResultCell cell)
    {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink#onOperationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onOperationChangeRequest(IChangeRequest req, IResultCell cell)
    {
        int chgk = req.getState();
        int rdt  = req.getRequestDetailType();
        
        IElement eBefore = req.getBefore(),
            eAfter  = req.getAfter();
        
        try
        {
            IOperation before = (IOperation) req.getBefore(),
                after  = (IOperation) req.getAfter();
            
            switch (chgk)
            {
                case ChangeKind.CT_CREATE:
                    createdMethod = after.getName();
                    break;
                    
                case ChangeKind.CT_MODIFY:
                {
                    changedMethod = before.getName() + "->"
                        + after.getName();
                    changedType   = before.getReturnType2() + "->" +
                        after.getReturnType2();
                    changedVis    = before.getVisibility() + "->" +
                        after.getVisibility();
                    changedAbs    = before.getIsAbstract() + "->" +
                        after.getIsAbstract();
                    break;
                }
                case ChangeKind.CT_DELETE:
                    deletedMethod = before.getName();
                    break;
            }
        }
        catch (ClassCastException e)
        {
            IParameter before = (IParameter) req.getBefore(),
                after  = (IParameter) req.getAfter();
            switch (chgk)
            {
                case ChangeKind.CT_CREATE:
                    createdParamType = after.getTypeName();
                    createdParam = after.getName();
                    break;
                    
                case ChangeKind.CT_MODIFY:
                {
                    changedParam = before.getName() + "->"
                        + after.getName();
                    changedParamType   = before.getTypeName() + "->" +
                        after.getTypeName();
                    break;
                }
                case ChangeKind.CT_DELETE:
                    deletedParam = before.getTypeName() + " " + before.getName();
                    break;
            }
        }
    }
}