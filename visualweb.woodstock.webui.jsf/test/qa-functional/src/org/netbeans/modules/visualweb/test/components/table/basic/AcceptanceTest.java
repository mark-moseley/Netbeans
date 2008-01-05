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

package org.netbeans.modules.visualweb.test.components.table.basic;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

/**
 * @author Chary Kanaparthi (Chary.Kanaparthi@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    public static String BUNDLE = "org.netbeans.modules.visualweb.test.components.output.Bundle";
    public String _projectName = "BasicTableAcceptance";
    
    //Project variables
    public String _sharedBundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _projectServer = Bundle.getStringTrimmed(_sharedBundle,"projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_sharedBundle,"logFile");
    public String _logFile = System.getProperty("xtest.sketchpad") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_sharedBundle,"Exception");
    public String _close = Bundle.getStringTrimmed(_sharedBundle,"close");
    public String _run = Bundle.getStringTrimmed(_sharedBundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_sharedBundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_sharedBundle,"true");
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_sharedBundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_sharedBundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_sharedBundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_sharedBundle, "deploymentPathGlassfish");
    public String _separator = Bundle.getStringTrimmed(_sharedBundle, "separator");
    
    public static DesignerPaneOperator designer;
    public static SheetTableOperator sheet;
    public static ServerNavigatorOperator explorer;
    
    //Point at which table is dropped
    public static int xTable=25;
    public static int yTable=50;
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new NbTestSuite();
        suite.addTest(new AcceptanceTest("createProject"));
        suite.addTest(new AcceptanceTest("addTable"));
        suite.addTest(new AcceptanceTest("testTableDataBind"));
        //suite.addTest(new AcceptanceTest("testTableAddColumn"));
        //suite.addTest(new AcceptanceTest("testTableAddRowGroup"));
        suite.addTest(new AcceptanceTest("testDeploy"));
        suite.addTest(new AcceptanceTest("closeProject"));
        suite.addTest(new AcceptanceTest("testUndeploy"));
        suite.addTest(new AcceptanceTest("testCheckIDELog"));
        
        return suite;
    }
    
    /** method called before each testcase
     */
    protected void setUp() {
        System.out.println("Running Test  " + getName()+ "  ..................");
    }
    
    /** method called after each testcase
     */
    protected void tearDown() {
        System.out.println(" Finished Running Test    "+ getName()+ "..................");
    }
    
    
    public void createProject() {
        startTest();
        log("**Creating Project");
        //Create Project
        try {
            ComponentUtils.createNewProject(_projectName);
        } catch(Exception e) {
            log(">> Project Creation Failed");
            e.printStackTrace();
            log(e.toString());
            fail();
        }
        log("**Done");
        endTest();
    }
    
    public void addTable() {
        
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator("Basic");
        palette.dndPaletteComponent("Table", designer, new Point(xTable, yTable));
        try { Thread.sleep(2000); } catch(Exception e) {}
        
//        log("make sure property sheet is visible");
//        Util.getMainMenu().pushMenu("Window|Properties","|");
//        try { Thread.sleep(2000); } catch(Exception e) {}
        
        sheet = new SheetTableOperator();
        sheet.setTextValue("id", "pTable");
//commented because it tries to set augmentTitle	
//        sheet.setTextValue("title", "Traveller Details");
//        sheet.setTextValue("width", "100%");
        Util.wait(2000);
        endTest();
        
    }
    /* Test Databinding of table
    */
     public void testTableDataBind() {
        startTest();
        //String _dataProvider = "customerDataProvider";
        String _dbName=Bundle.getStringTrimmed(_sharedBundle, "Databses_SampleDBNode");
        String _passwd="app";
        String _tableName=Bundle.getStringTrimmed(_sharedBundle, "Databases_CustomerTableNode");         
        log("Connect sample database");
        ComponentUtils.connectDB(_dbName, _passwd); 
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        log("Bind Sample DB table Customer to Table Component");
        ComponentUtils.bindToDataProvider(xTable, yTable, _tableName);
        Util.wait(2000);
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
   
     /* Test Adding column to table
      */
    public void testTableAddColumn() {
          startTest();
          log("Adding new Column to table");
          //Drop Table Column on table to add a new column to table
          designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
          PaletteContainerOperator palette = new PaletteContainerOperator("Basic");
          //Column has to be dropped on the existing columns on the table
          //therefore adjusting point coordinates 
          palette.dndPaletteComponent("Table Column", designer, new Point(xTable+25, yTable+50));
          try { Thread.sleep(5000); } catch(Exception e) {}
          
          sheet = new SheetTableOperator();
          log("Changing Id of new Column through property sheet");
          sheet.setTextValue("id", "newColumn");
          designer.select("ptable");
          sheet = new SheetTableOperator();
          log("Changing HeaderText of new Column through property Sheet");
          sheet.setTextValue("headerText", "Test Column");
          endTest();
    }
     /* Test Adding new Row Group to table
      * 
      */
    public void testTableAddRowGroup() {
          startTest();
          //Drop Row Group on table to add a new row group
          log("Adding new Row Group to table");
          designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
          PaletteContainerOperator palette = new PaletteContainerOperator("Basic");
          palette.dndPaletteComponent("Table Row Group", designer, new Point(xTable, yTable));
          Util.wait(5000);
          //try { Thread.sleep(5000); } catch(Exception e) {}
          sheet = new SheetTableOperator();
          log("Changing Id of new Row Group through property sheet");
          sheet.setTextValue("id", "newRowGroup");
          Util.wait(2000);
          endTest();
    }

    public void testTableLayout() {
        startTest();
        endTest();
    }
        

    /*
     * Deploy application
     */
    public void testDeploy() {
        startTest();
        //need to wait responce
        Waiter deploymentWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object output) {
                String text = ((OutputOperator)output).getText();
                if (text.indexOf(_buildSuccess)!=-1)
                    return _true;
                return null;
                
            }
            public String getDescription() {
                return("Waiting Project Deployed");
            }
        });
        log("Deploy from menu");
        ProjectNavigatorOperator.pressPopupItemOnNode(_projectName, _run);
        TestUtils.wait(2000);
        OutputOperator outputWindow = new OutputOperator();
        deploymentWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 240000);
        log("wait until " + _buildSuccess);
        try {
            deploymentWaiter.waitAction(outputWindow);
        } catch(InterruptedException e) {
            log(outputWindow.getText());
            e.printStackTrace();
            fail("Deployment error: "+e);
        }
        log("Deployment complete");
        endTest();
    }
    
    public void closeProject() {
        startTest();
        Util.saveAllAPICall();
        new ProjectNavigatorOperator().pressPopupItemOnNode(_projectName,  "close");
        TestUtils.wait(5000);
        endTest();
    }
    
    /* Need to undeploy project to finish tests correctly */
    public void testUndeploy() {
        startTest();
        log("Initialize");
        explorer = ServerNavigatorOperator.showNavigatorOperator();
        String serverPath = _serverPath + _projectServer;  //Current deployment server
        String deploymentPath = serverPath + _deploymentPath; //glassfish specific
        String applicationPath = deploymentPath + _separator + _projectName; //project name
        
        // Select the Server Navigator and set the JTreeOperator
        log("get explorer");
        new QueueTool().waitEmpty(100); //??
        explorer.requestFocus();
        JTreeOperator tree = explorer.getTree();
        try { Thread.sleep(4000); } catch(Exception e) {} // Sleep 4 secs to make sure Server Navigator is in focus
        
        // Need to refresh J2EE AppServer node
        log("refresh");
        explorer.pushPopup(tree, serverPath, _refresh);
        TestUtils.wait(1000);
        
        log("refresh deployment path: " + deploymentPath);
        TestUtils.wait(1000);
        explorer.selectPath(deploymentPath);
        explorer.getTree().expandPath(explorer.getTree().findPath(deploymentPath));
        explorer.pushPopup(tree, deploymentPath, _refresh);
        TestUtils.wait(1000);
        
        log("undeploy Path: " + applicationPath);
        explorer.selectPath(applicationPath);
        TestUtils.wait(1000);
        
        log("Push Menu Undeploy...");
        explorer.pushPopup(explorer.getTree(), applicationPath, _undeploy);
        TestUtils.wait(5000);
        endTest();
    }
    
    public void testCheckIDELog() {
        startTest();
        try {
            String err = ComponentUtils.hasUnexpectedException();
            String str = "";
            if (!(err.equals(""))) {
                assertTrue("Unexpected  exceptions found in message.log: " + err, str.equals(""));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Failed to open message.log : " + ioe);
        }
        endTest();
    }    
    
}
