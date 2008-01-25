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


package org.netbeans.test.uml.activitydiagram;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public class ActivityDiagramPlaceNameElementReloadDiagram extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "ActivityDiagramProjectRL";
    private static String project = prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private static String defaultNewElementName=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultOperationVisibility;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private String lastTestCase=null;
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("xtest.workdir");
    //--
     private static String activityDiagramName1 = "acD1";
    private static String workPkg1 = "pkg1";
    private static String element1="Invocation";
    private static String elementName1="";
    private static ElementTypes elementType1=ElementTypes.INVOCATION;
    //--
    private static String activityDiagramName2 = "acD2";
    private static String workPkg2 = "pkg2";
    private static String element2="Activity Group";
    private static String elementName2=defaultNewElementName;
    private static ElementTypes elementType2=ElementTypes.ACTIVITY_GROUP;
    //--
    private static String activityDiagramName3 = "acD3";
    private static String workPkg3 = "pkg3";
    private static String element3="Initial Node";
    private static String elementName3="";
    private static ElementTypes elementType3=ElementTypes.INITIAL_NODE;
    //--
    private static String activityDiagramName4 = "acD4";
    private static String workPkg4 = "pkg4";
    private static String element4="Activity Final Node";
    private static String elementName4="";
    private static ElementTypes elementType4=ElementTypes.ACTIVITY_FINAL_NODE;
    //--
    private static String activityDiagramName5 = "acD5";
    private static String workPkg5 = "pkg5";
    private static String element5="Flow Final";
    private static String elementName5="";
    private static ElementTypes elementType5=ElementTypes.FLOW_FINAL;
    //--
    private static String activityDiagramName6 = "acD6";
    private static String workPkg6 = "pkg6";
    private static String element6="Decision";
    private static String elementName6="";
    private static ElementTypes elementType6=ElementTypes.DECISION;
    //--
    private static String activityDiagramName7 = "acD7";
    private static String workPkg7 = "pkg7";
    private static String element7="Vertical Fork";
    private static String elementName7="";
    private static ElementTypes elementType7=ElementTypes.VERTICAL_FORK;
    //--
    private static String activityDiagramName8 = "acD8";
    private static String workPkg8 = "pkg8";
    private static String element8="Horizontal Fork";
    private static String elementName8="";
    private static ElementTypes elementType8=ElementTypes.HORIZONTAL_FORK;
    //--
    private static String activityDiagramName9 = "acD9";
    private static String workPkg9 = "pkg9";
    private static String element9="Parameter Usage";
    private static String elementName9="";
    private static ElementTypes elementType9=ElementTypes.PARAMETER_USAGE;
    //--
    private static String activityDiagramName10 = "acD10";
    private static String workPkg10 = "pkg10";
    private static String element10="Data Store";
    private static String elementName10="";
    private static ElementTypes elementType10=ElementTypes.DATA_STORE;
    //--
    private static String activityDiagramName11 = "acD11";
    private static String workPkg11 = "pkg11";
    private static String element11="Signal";
    private static String elementName11="";
    private static ElementTypes elementType11=ElementTypes.SIGNAL;
    //--
    private static String activityDiagramName12 = "acD12";
    private static String workPkg12 = "pkg12";
    private static String element12="Partition";
    private static String elementName12=defaultNewElementName;
    private static ElementTypes elementType12=ElementTypes.PARTITION;
    //--
    private static String activityDiagramName13 = "acD13";
    private static String workPkg13 = "pkg13";
    private static String element13="Comment";
    private static String elementName13="";
    private static ElementTypes elementType13=ElementTypes.COMMENT;
    //--
    private static String activityDiagramName14 = "acD14";
    private static String workPkg14 = "pkg14";
    private static String element14="Link Comment";
    private static String elementName14="";
    private static ElementTypes elementType14=ElementTypes.LINK_COMMENT;


    
    /** Need to be defined because of JUnit */
    public ActivityDiagramPlaceNameElementReloadDiagram(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.activitydiagram.ActivityDiagramPlaceNameElementReloadDiagram.class);
        return suite;
    }
    
    private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagram) {
        pto = ProjectsTabOperator.invoke();
        prTree=new JTreeOperator(pto);
        Node root=new Node(prTree,project);
        //
        if(root.isChildPresent(workPkg))
        {
            if(new Node(root,workPkg).isChildPresent(diagram))
            {
                new Node(root,workPkg+"|"+diagram).callPopup().pushMenu("Open");
                 DiagramOperator ret=null;
                 ret=new DiagramOperator(diagram);
                 ret.waitComponentShowing(true);
                 lastDiagramNode = new Node(root,workPkg+"|"+diagram);
                 return ret;
            }
        }
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagram,NewDiagramWizardOperator.ACTIVITY_DIAGRAM);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
    

    public void testCreateAndCheckAfterReopen1() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg1,activityDiagramName1,element1,elementName1,elementType1);
    }
    public void testCreateAndCheckAfterReopen2() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg2,activityDiagramName2,element2,elementName2,elementType2);
    }
    public void testCreateAndCheckAfterReopen3() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg3,activityDiagramName3,element3,elementName3,elementType3);
    }
    public void testCreateAndCheckAfterReopen4() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg4,activityDiagramName4,element4,elementName4,elementType4);
    }
    public void testCreateAndCheckAfterReopen5() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg5,activityDiagramName5,element5,elementName5,elementType5);
    }
    public void testCreateAndCheckAfterReopen6() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg6,activityDiagramName6,element6,elementName6,elementType6);
    }
    public void testCreateAndCheckAfterReopen7() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg7,activityDiagramName7,element7,elementName7,elementType7);
    }
    public void testCreateAndCheckAfterReopen8() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg8,activityDiagramName8,element8,elementName8,elementType8);
    }
    public void testCreateAndCheckAfterReopen9() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg9,activityDiagramName9,element9,elementName9,elementType9);
    }
    public void testCreateAndCheckAfterReopen10() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg10,activityDiagramName10,element10,elementName10,elementType10);
    }
   public void testCreateAndCheckAfterReopen11() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg11,activityDiagramName11,element11,elementName11,elementType11);
    }
   public void testCreateAndCheckAfterReopen12() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg12,activityDiagramName12,element12,elementName12,elementType12);
    }
   public void testCreateAndCheckAfterReopen13() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg13,activityDiagramName13,element13,elementName13,elementType13);
    }
   public void testCreateAndCheckAfterReopen14() {
        lastTestCase=getCurrentTestMethodName();
        testPlaceNameReopen(workPkg14,activityDiagramName14,element14,elementName14,elementType14);
    }

 
      
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        pto = ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            org.netbeans.test.uml.activitydiagram.utils.Utils.commonActivityDiagramSetup(workdir, prName);
            //
            codeSync=true;
        }
    }
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(1000);
        //
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.saveAll();
        if(lastDiagramNode!=null)
        {
            lastDiagramNode.collapse();
            new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath()).collapse();
        }
        try{
            DiagramOperator d=new DiagramOperator("acD");
            d.closeAllDocuments();
            d.waitClosed();
            new EventTool().waitNoEvent(1000);
        }catch(Exception ex){};
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }

    
   private void testPlaceNameReopen(String workPkg,String activityDiagramName,String element,String elementName,ElementTypes elementType,String newName)
   {
        DiagramOperator d = createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        int numChild=lastDiagramNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        //
        try {
            pl.selectTool(element);
        } catch(NotFoundException ex) {
            fail("BLOCKING: Can't find '"+element+"' in paletter");
        }
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //
        try
        {
            DiagramElementOperator dEl=new DiagramElementOperator(d,elementName,elementType,0);
        }
        catch(Exception ex)
        {
            try
            {
                fail(element+" wasn't added to diagram, but object with type:"+new DiagramElementOperator(d,elementName).getType()+": and element type :"+new DiagramElementOperator(d,elementName).getElementType()+": was added whyle type should be :"+elementType+": was added");
            }
            catch(Exception ex2)
            {
                
            }
            fail(element+" wasn't added to iagram.");
        }
       //
        new EventTool().waitNoEvent(500);
        PropertySheetOperator ps=new PropertySheetOperator();
        if(elementName.equals(""))
        {
            assertTrue("Property sheet isn't for "+elementType+", but for "+ps.getName(),(elementType+" - Properties").equals(ps.getName()));
        }
        else
        {
            assertTrue("Property sheet isn't for "+elementType+" with "+elementName+" name, but for "+ps.getName(),(elementName+" - Properties").equals(ps.getName()));
        }
        //
        int numChild2=lastDiagramNode.getChildren().length;
        
        org.netbeans.test.uml.activitydiagram.utils.Utils.setTextProperty("Name",newName);
        //
        new EventTool().waitNoEvent(500);
        //
        try
        {
            DiagramElementOperator dEl=new DiagramElementOperator(d,newName,elementType,0);
        }
        catch(Exception ex)
        {
             fail("Can't find "+element+" with name \""+newName+"\" on diagram.");
        }
        //
        assertTrue("No name propagated to node within diagram (in project tree)",lastDiagramNode.isChildPresent(newName));
        //
          new Thread(new Runnable() {
                public void run() {
                    new JButtonOperator(new JDialogOperator("Save"),"Save").push();
                }
            }).start();
        d.closeWindow();
        //
        d = createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
       assertTrue("There is no elements on diagram after close-save-open the diagram",d.getDiagramElements().size()>0);
        //
        try
        {
            DiagramElementOperator dEl=new DiagramElementOperator(d,newName,elementType,0);
        }
        catch(Exception ex)
        {
             fail( "Can't find "+element+" with name \""+newName+"\" on diagram after close/open.");
        }
   }
   private void testPlaceNameReopen(String workPkg,String activityDiagramName,String element,String elementName,ElementTypes elementType)
   {
       testPlaceNameReopen(workPkg,activityDiagramName,element,elementName,elementType,"ElName");
   }

}
