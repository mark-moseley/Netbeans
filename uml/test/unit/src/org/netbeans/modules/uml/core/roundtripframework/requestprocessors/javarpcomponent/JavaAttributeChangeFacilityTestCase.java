package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IParameterChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class JavaAttributeChangeFacilityTestCase extends AbstractUMLTestCase
    implements IRoundTripOperationEventsSink
{
    private IAttributeChangeFacility facility;
    private String createdMethod;
    private String changedMethod;
    private String changedType;
    private String changedVis;
    private String changedAbs;
    private String deletedMethod;
    private String createdParamType;
    private String createdParam;
    private String changedParam;
    private String changedParamType;
    private String deletedParam;
    private IOperation beforeOp;
    private IOperation afterOp;
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(JavaAttributeChangeFacilityTestCase.class);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.registerForRoundTripOperationEvents(this, "Java");
        
        IFacilityManager facMan = product.getFacilityManager();
        facility = (IAttributeChangeFacility) facMan.retrieveFacility(
            "RoundTrip.JavaAttributeChangeFacility");
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
 * AttributeCreateTestCase
 */
 
    public void testCreateAttribute()
    {
// TODO: conover - temporary until fixed            
//        IClass c = createClass("Archangel");
//        
//        facility.addAttribute2("peace", "char", c, true, false);
//        assertEquals(1, c.getOperationsByName("getPeace").size());
//        assertEquals(1, c.getOperationsByName("setPeace").size());
        
//        System.out.println();
//        System.out.println("about to create attribute abc");
//        System.out.println();
//
//        IAttribute attr = facility.addAttribute2("abc", "float", c, true, false);
//        
//        IOperation oper = c.getOperationsByName("getAbc").get(0);
//        ETList<IDependency> clientDeps = oper.getClientDependencies();
//        
//        for (IDependency dep: clientDeps)
//        {
//            INamedElement sup = dep.getSupplier();
//            INamedElement cli = dep.getClient();
//            System.out.println("supplier: " + sup.getName());
//            System.out.println("client:   " + cli.getName());
//        }
//
//        System.out.println();
//        
//        oper = null;
//        clientDeps = null;
//        
//        oper = c.getOperationsByName("setAbc").get(0);
//        clientDeps = oper.getClientDependencies();
//        
//        for (IDependency dep: clientDeps)
//        {
//            INamedElement sup = dep.getSupplier();
//            INamedElement cli = dep.getClient();
//            System.out.println("supplier: " + sup.getName());
//            System.out.println("client:   " + cli.getName());
//        }

        
//        assertEquals(1, c.getOperationsByName("getAbc").size());
//        assertEquals(1, c.getOperationsByName("setAbc").size());
//        
//        assertEquals(1, c.getOperationsByName("getAbc").get(0).getClientDependencies().size());
//        assertEquals(1, c.getOperationsByName("setAbc").get(0).getSupplierDependencies().size());
//        
//        beforeOp = afterOp = null;
//        attr.getMultiplicity().setRangeThroughString("0..*");
//        IOperation op = c.getOperationsByName("getAbc").get(0);
//        
//        //assertEquals("float->float[]", changedParamType);
//        assertNotNull(beforeOp);
//        assertNotNull(afterOp);
//        
//        assertEquals(1, op.getReturnType().getMultiplicity().getRangeCount());
//        assertEquals(1, op.getReturnType().getMultiplicity().getRanges().size());
//        assertEquals("0..*", op.getReturnType().getMultiplicity().getRangeAsString());
//        
//        op = c.getOperationsByName("setAbc").get(0);
//        assertEquals(1, op.getFormalParameters().get(0).getMultiplicity().getRangeCount());
//        assertEquals(1, op.getFormalParameters().get(0).getMultiplicity().getRanges().size());
//        assertEquals("0..*", op.getFormalParameters().get(0).getMultiplicity().getRangeAsString());
    }


/**
 * CreateNavigableAssociationTestCase
 */

    public void testCreateNavigableAssociation()
    {
// TODO: conover - temporary until fixed            
//        IClass c = createClass("Scapegoat");
//        createClass("DirtyWork");
//        
//        facility.addAttribute2("chaos", "DirtyWork", c, true, false);
//        assertEquals(1, c.getOperationsByName("getChaos").size());
//        assertEquals(1, c.getOperationsByName("setChaos").size());
//        facility.addAttribute2("firts", "java::awt::Color", c, true, false);
//        IPackage element = (IPackage)project.getOwnedElementsByName("java").get(0);
//        element = (IPackage)element.getOwnedElementsByName("awt").get(0);
//        assertEquals(1,element.getOwnedElementsByName("Color").size());
//        //assertEquals(0,project.getOwnedElementsByName("Color").size());
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
            IParameterChangeRequest parreq = (IParameterChangeRequest) req;
            IParameter before = (IParameter) req.getBefore(),
                after  = (IParameter) req.getAfter();
            beforeOp = parreq.getBeforeOperation();
            afterOp = parreq.getAfterOperation();
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
                    changedParamType   = getType(before) + "->" +
                        getType(after);
                    break;
                }
                case ChangeKind.CT_DELETE:
                    deletedParam = before.getTypeName() + " " + before.getName();
                    break;
            }
        }
    }
    
    /**
     * @param before
     * @return
     */
    private String getType(IParameter par)
    {
        String type = par.getTypeName();
        long num = par.getMultiplicity().getRangeCount();
        
        for (int i = 0; i < num; i++)
        {
            type += "[]";
        }
        
        return type;
    }
}