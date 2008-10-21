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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.designer;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ws.qaf.WebServicesTestBase;

/**
 *
 * @author lukas
 */
public class WebServiceDesignerTest extends WebServicesTestBase {

    public WebServiceDesignerTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return "60_webapp"; //NOI18N
    }

    public void testAddOperation() {
        addOperation("EmptyWs", 0, false); //NOI18N
    }

    public void testRemoveOperation() {
        removeOperation("EmptyWs", 1, false); //NOI18N
    }

    public void testAddOperation2() {
        addOperation("SampleWs", 2, false); //NOI18N
    }

    public void testRemoveOperation2() {
        removeOperation("SampleWs", 3, false); //NOI18N
    }

    public void testAddOperationToIntf() {
        addOperation("WsImpl", 1, true); //NOI18N
    }

    public void testRemoveOperationFromIntf() {
        removeOperation("WsImpl", 2, true); //NOI18N
    }

    public void testGoToSource() {
        String wsName = "EmptyWs"; //NOI18N
        String opName = "test1"; //NOI18N
        openFileInEditor(wsName);
        WsDesignerUtilities.invokeGoToSource(wsName, opName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        EditorOperator eo = new EditorOperator(wsName);
        assertEquals(24, eo.getLineNumber());
//      see: http://www.netbeans.org/issues/show_bug.cgi?id=150923
//        wsName = "WsImpl"; //NOI18N
//        openFileInEditor(wsName);
//        WsDesignerUtilities.invokeGoToSource(wsName, opName);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ex) {
//            //ignore
//        }
//        eo = new EditorOperator(wsName);
//        assertEquals(18, eo.getLineNumber());
        wsName = "SampleWs"; //NOI18N
        opName = "sayHi"; //NOI18N
        openFileInEditor(wsName);
        WsDesignerUtilities.invokeGoToSource(wsName, opName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        eo = new EditorOperator(wsName);
        assertEquals(33, eo.getLineNumber());
    }

    //only sanity test (see if there's no exception)
    //some checks can be added later
    public void testOperationButtons() {
        String wsName = "SampleWs"; //NOI18N
        String opName = "voidOperation"; //NOI18N
        WsDesignerUtilities.clickOnButton(wsName, opName, 0);
        WsDesignerUtilities.clickOnButton(wsName, opName, 2);
        WsDesignerUtilities.clickOnButton(wsName, opName, 1);
        opName = "sayHi"; //NOI18N
        WsDesignerUtilities.clickOnButton(wsName, opName, 1);
        WsDesignerUtilities.clickOnButton(wsName, opName, 0);
        WsDesignerUtilities.clickOnButton(wsName, opName, 0);
        WsDesignerUtilities.clickOnButton(wsName, opName, 2);
        WsDesignerUtilities.clickOnButton(wsName, opName, 0);
    }

    private void addOperation(String wsName, int opCount, boolean hasInterface) {
        openFileInEditor(wsName);
        assertEquals(opCount, WsDesignerUtilities.operationsCount(wsName));
        WsDesignerUtilities.invokeAddOperation(wsName);
        //Add Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "LBL_OperationAction");
        NbDialogOperator dialog = new NbDialogOperator(actionName);
        new JTextFieldOperator(dialog, 2).setText("test1"); //NOI18N
        new JTextFieldOperator(dialog, 1).setText("String"); //NOI18N
        dialog.ok();
        try {
            //slow down a bit
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        new SaveAllAction().performAPI();
        WsDesignerUtilities.source(wsName);
        EditorOperator eo = new EditorOperator(wsName);
        assertNotNull(eo);
        if (hasInterface) {
            assertFalse(eo.contains("import javax.jws.WebMethod;")); //NOI18N
            assertFalse(eo.contains("@WebMethod(operationName = \"test1\")")); //NOI18N
//            see http://www.netbeans.org/issues/show_bug.cgi?id=150896
//            assertEquals(opCount + 1, WsDesignerUtilities.operationsCount(wsName));
        } else {
            assertTrue(eo.contains("import javax.jws.WebMethod;")); //NOI18N
            assertTrue(eo.contains("@WebMethod(operationName = \"test1\")")); //NOI18N
            assertEquals(opCount + 1, WsDesignerUtilities.operationsCount(wsName));
        }
        assertTrue(eo.contains("public String test1() {")); //NOI18N
        //check ws endpoint interface
        if (hasInterface) {
            //XXX-rather should find interface from the source
            String iName = "EndpointI"; //NOI18N
            openFileInEditor(iName);
            EditorOperator eo2 = new EditorOperator(iName);
            assertTrue(eo2.contains("public String test1();")); //NOI18N
            eo2.close();
        }
    }

    private void removeOperation(String wsName, int opCount, boolean hasInterface) {
        openFileInEditor(wsName);
        WsDesignerUtilities.invokeRemoveOperation(wsName, "test1", opCount % 2 == 0); //NOI18N
        NbDialogOperator ndo = new NbDialogOperator("Question"); //NOI18N
        ndo.yes();
        //see: http://www.netbeans.org/issues/show_bug.cgi?id=150896
        if (!hasInterface) {
            assertEquals(opCount - 1, WsDesignerUtilities.operationsCount(wsName));
        }
        new SaveAllAction().performAPI();
        WsDesignerUtilities.source(wsName);
        EditorOperator eo = new EditorOperator(wsName);
        assertNotNull(eo);
        assertFalse(eo.contains("@WebMethod(operationName = \"test1\")")); //NOI18N
        if (hasInterface) {
            assertTrue(eo.contains("public String test1() {")); //NOI18N
        } else {
            assertFalse(eo.contains("public String test1() {")); //NOI18N
        }
        //check ws endpoint interface
        if (hasInterface) {
            //XXX-rather should find interface from the source
            String iName = "EndpointI"; //NOI18N
            openFileInEditor(iName);
            EditorOperator eo2 = new EditorOperator(iName);
            assertNotNull(eo2);
            assertFalse(eo2.contains("public String test1();")); //NOI18N
            eo2.close();
        }
    }

    private void openFileInEditor(String fileName) {
        //XXX:
        //there's some weird bug:
        //if project with webservices is checked out from VCS (cvs)
        //and its class is opened in the editor then there's no
        //web service designer or it is not initialized correctly :(
        Node wsNode = new Node(getProjectRootNode(), "Web Services");
        if (wsNode.isCollapsed()) {
            wsNode.expand();
        }
        //end
        SourcePackagesNode spn = new SourcePackagesNode(getProjectRootNode());
        Node n = new Node(spn, "samples|" + fileName); //NOI18N
        new OpenAction().perform(n);
    }

    public static Test suite() {
        return NbModuleSuite.create(addServerTests(
                NbModuleSuite.createConfiguration(WebServiceDesignerTest.class),
                "testAddOperation", //NOI18N
                "testAddOperation2", //NOI18N
                "testAddOperationToIntf", //NOI18N
                "testOperationButtons", //NOI18N
                "testGoToSource", //NOI18N
                "testRemoveOperation", //NOI18N
                "testRemoveOperation2", //NOI18N
                "testRemoveOperationFromIntf" //NOI18N
                ).enableModules(".*").clusters(".*")); //NOI18N
    }
}
