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
package org.netbeans.modules.ws.qaf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.web.NewJspFileNameStepOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Handler;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandler;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChain;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChains;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *  Basic validation suite for web services support in the IDE
 *
 * @author lukas.jungmann@sun.com
 */
public class WsValidation extends WebServicesTestBase {

    protected static final String WEB_SERVICES_NODE_NAME = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.nodes.Bundle", "LBL_WebServices");
    private static final String WEB_SERVICE_CLIENTS_NODE_NAME = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.nodes.Bundle", "LBL_ServiceReferences");
    private static int foId = 0;

    protected enum HandlerType {

        LOGICAL,
        MESSAGE;

        public String getFileTypeLabel() {
            switch (this) {
                case LOGICAL:
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/LogicalHandler.java");
                case MESSAGE:
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/SOAPMessageHandler.java");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public String getMessageType() {
            switch (this) {
                case LOGICAL:
                    return "LogicalMessage"; //NOI18N
                case MESSAGE:
                    return "SOAPMessage"; //NOI18N
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    /** Creates a new instance of WsValidation */
    public WsValidation(String name) {
        super(name);
    }

    public String getProjectName() {
        return getName().indexOf("Client") > -1 //NOI18N
                ? getWsClientProjectName()
                : getWsProjectName();
    }

    protected String getWsProjectName() {
        return "WsInWeb"; //NOI18N
    }

    protected String getWsClientProjectName() {
        return "WsClientInWeb"; //NOI18N
    }

    protected String getWsName() {
        return "MyWebWs"; //NOI18N
    }

    protected String getWsPackage() {
        return "o.n.m.ws.qaf.ws"; //NOI18N
    }

    protected String getWsClientPackage() {
        return getWsPackage(); //NOI18N
    }

    /**
     * Creates a new web service in a web project and checks whether web service
     * node has been created in the project view and web service implementation
     * class has been opened in the editor
     * @throws java.io.IOException
     */
    public void testCreateNewWs() throws IOException {
        // Web Service
        String webServiceLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/WebService.java");
        createNewWSFile(getProject(), webServiceLabel);
        NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
        op.setObjectName(getWsName());
        op.setPackage(getWsPackage());
        op.finish();
        // needed for slower machines
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); //NOI18N
        //TODO: following nodes should be expanded by default - this test should check it as well
        Node wsRootNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME);
        wsRootNode.expand();
        new Node(wsRootNode, getWsName());
        new EditorOperator(getWsName());
        checkNonJSR109Service();
    }

    /**
     * Tests adding operation to webservice using
     * - add operation action from editor's popup menu
     * - add operation action from ws node's context menu
     */
    public void testAddOperation() {
        final EditorOperator eo = new EditorOperator(getWsName());
        //Web Service
        String actionGroupName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "LBL_WebServiceActionGroup");
        //Add Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "LBL_OperationAction");
        //invoke action from editor's context menu
        new ActionNoBlock(null, actionGroupName + "|" + actionName).performPopup(eo);
        addWsOperation(eo, "myStringMethod", "String"); //NOI18N
        //invoke action from ws node's context menu
        Node wsRootNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME);
        wsRootNode.expand();
        Node wsImplNode = new Node(wsRootNode, getWsName());
        wsImplNode.callPopup().pushMenuNoBlock(actionName);
        addWsOperation(eo, "myIntMethod", "int[]"); //NOI18N
    //      wsImplNode.expand();
    //      assertTrue(wsImplNode.isChildPresent("myMethod")); //NOI18N
    //      assertTrue(wsImplNode.isChildPresent("myMethod2")); //NOI18N
    }

    public void testDeployWsProject() throws IOException {
        deployProject(getProjectName());
    }

    public void testDeployWsClientProject() throws IOException {
        deployProject(getProjectName());
    }

    /**
     * Creates a new web service client in a web project and checks whether web
     * service client node has been created in the project view
     * @throws java.io.IOException
     */
    public void testCreateWsClient() throws IOException {
        //Web Service Client
        String wsClientLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.client.wizard.Bundle", "Templates/WebServices/WebServiceClient");
        createNewWSFile(getProject(), wsClientLabel);
        NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
        new JButtonOperator(op, 3).push();
        //Browse Web Services
        String browseWsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.client.wizard.Bundle", "TTL_SelectService");
        NbDialogOperator ndo = new NbDialogOperator(browseWsLabel);
        JTreeOperator jto = new JTreeOperator(ndo);
        jto.selectPath(jto.findPath(getWsProjectName() + "|" + getWsName())); //NOI18N
        ndo.ok();
        op.finish();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        //expand ws client node
        // needed for slower machines
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 60000); //NOI18N
        Node wsClientRootNode = new Node(getProjectRootNode(), WEB_SERVICE_CLIENTS_NODE_NAME);
        wsClientRootNode.expand();
        Node wsClientNode = new Node(wsClientRootNode, getWsName()); //NOI18N
        wsClientNode.expand();
        waitForWsImport("wsimport-client-compile)"); //NOI18N
        Node wsClientServiceNode = new Node(wsClientNode, getWsName()); //NOI18N
        wsClientServiceNode.expand();
        Node wsClientPortNode = new Node(wsClientServiceNode, getWsName()); //NOI18N
        wsClientPortNode.expand();
        assertTrue(wsClientPortNode.isChildPresent("myStringMethod")); //NOI18N
        assertTrue(wsClientPortNode.isChildPresent("myIntMethod")); //NOI18N
        assertEquals(wsClientPortNode.getChildren().length, 2);
    }

    /**
     * Tests Call Web Service Operation action in a servlet
     */
    public void testCallWsOperationInServlet() {
        //create a servlet
        //Web
        String webLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        //Servlet
        String servletLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet/Servlet.java");
        createNewFile(getWsClientProject(), webLabel, servletLabel);
        NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
        JComboBoxOperator jcbo = new JComboBoxOperator(op, 1);
        jcbo.typeText("org.mycompany.servlets"); //NOI18N
        op.finish();
        //edit code in the servlet
        EditorOperator eo = new EditorOperator("NewServlet"); //NOI18N
        // delete comments
        eo.replace("/* TODO output your page here", ""); //NOI18N
        eo.replace("            */", ""); //NOI18N
        // add new line and select it
        eo.setCaretPosition("\"</h1>\");", false); //NOI18N
        eo.insert("\n//xxx"); //NOI18N
        eo.select("//xxx"); //NOI18N
        callWsOperation(eo, "myIntMethod", eo.getLineNumber()); //NOI18N
        assertTrue("@WebServiceRef has not been found", eo.contains("@WebServiceRef")); //NOI18N
        assertFalse("Lookup present", eo.contains(getWsClientLookupCall())); //NOI18N
        eo.close(true);
    }

    /**
     * Test Call Web Service Operation action in a JSP
     */
    public void testCallWsOperationInJSP() {
        //create new JSP
        //Web
        String webLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        //JSP
        String servletLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet/JSP.jsp");
        createNewFile(getWsClientProject(), webLabel, servletLabel);
        NewJspFileNameStepOperator op = new NewJspFileNameStepOperator();
        op.setJSPFileName("index1"); //NOI18N
        op.finish();
        //edit code in JSP
        EditorOperator eo = new EditorOperator("index1"); //NOI18N
        eo.setCaretPosition("</h2>", false); //NOI18N
        eo.insert("\n<!-- xxx -->"); //NOI18N
        eo.select("<!-- xxx -->"); //NOI18N
        callWsOperation(eo, "myStringMethod", eo.getLineNumber()); //NOI18N
        assertTrue("Lookup class has not been found", eo.contains(getWsClientLookupCall())); //NOI18N
        assertFalse("@WebServiceRef present", eo.contains("@WebServiceRef")); //NOI18N
        eo.close(true);
    }

    /**
     * Test Call Web Service Operation action in a regular java file
     */
    public void testCallWsOperationInJavaClass() {
        //Create new Java class
        //Java
        String javaAppLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "Templates/Classes"); //NOI18N
        //Java Class
        String javaFileLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Class.java"); //NOI18N
        createNewFile(getWsClientProject(), javaAppLabel, javaFileLabel);
        NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
        op.setPackage("org.mycompany.classes"); //NOI18N
        op.finish();
        final EditorOperator eo = new EditorOperator("NewClass"); //NOI18N
        eo.select(13); //NOI18N
        eo.insert("    public void callMethod() {\n\t//xxx\n    }\n"); //NOI18N
        eo.select("//xxx"); //NOI18N
        callWsOperation(eo, "myIntMethod", eo.getLineNumber()); //NOI18N
        assertTrue("Lookup class has not been found", eo.contains(getWsClientLookupCall())); //NOI18N
        assertFalse("@WebServiceRef present", eo.contains("@WebServiceRef")); //NOI18N
        eo.close(true);
    }

    public void testWsHandlers() throws IOException {
        createHandler(getHandlersPackage(), "WsMsgHandler1", HandlerType.MESSAGE); //NOI18N
        createHandler(getHandlersPackage(), "WsMsgHandler2", HandlerType.MESSAGE); //NOI18N
        createHandler(getHandlersPackage(), "WsLogHandler1", HandlerType.LOGICAL); //NOI18N
        createHandler(getHandlersPackage(), "WsLogHandler2", HandlerType.LOGICAL); //NOI18N
        FileObject fo = getProject().getProjectDirectory().getFileObject(
                "src/java/" + getWsPackage().replace('.', '/')); //NOI18N
        File handlerCfg = new File(FileUtil.toFile(fo), getWsName() + "_handler.xml"); //NOI18N
        Node serviceNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME + "|" + getWsName()); //NOI18N
        configureHandlers(serviceNode, handlerCfg, true);
    }

    public void testWsClientHandlers() throws IOException {
        createHandler(getHandlersPackage(), "WsMsgHandler1", HandlerType.MESSAGE); //NOI18N
        createHandler(getHandlersPackage(), "WsMsgHandler2", HandlerType.MESSAGE); //NOI18N
        createHandler(getHandlersPackage(), "WsLogHandler1", HandlerType.LOGICAL); //NOI18N
        createHandler(getHandlersPackage(), "WsLogHandler2", HandlerType.LOGICAL); //NOI18N
        String path = "xml-resources/web-service-references/" + getWsName() + "Service/bindings/"; //NOI18N
        FileObject fo = getProject().getProjectDirectory().getFileObject("src/conf/"); //NOI18N
        if (fo == null) {
            fo = getProject().getProjectDirectory();
        }
        File handlerCfg = new File(FileUtil.toFile(fo), path + getWsName() + "Service_handler.xml"); //NOI18N
        Node clientNode = new Node(getProjectRootNode(), WEB_SERVICE_CLIENTS_NODE_NAME + "|" + getWsName()); //NOI18N
        configureHandlers(clientNode, handlerCfg, false);
    }

    /**
     * Cleanup method - undeploys projects deployed by this suite
     */
    public void testUndeployProjects() throws IOException {
        undeployProject(getWsProjectName());
        undeployProject(getWsClientProjectName());
    }

    /**
     * Test for Refresh Service action of Web Services node (from WSDL) 
     */
    public void testRefreshService() {
        refreshWSDL("service","",false);
    }

    /**
     * Test for Refresh Client action of Web Services References node 
     */
    public void testRefreshClient() {
        refreshWSDL("client","",false);
    }
    
    /**
     * Test for Refresh Service action of Web Services node (from WSDL)
     * including WSDL regeneration 
     */
    public void testRefreshServiceAndReplaceWSDL() {
        refreshWSDL("service","",true);
    }

    /**
     * Test for Refresh Client action of Web Services References node 
     * including WSDL regeneration
     */
    public void testRefreshClientAndReplaceWSDL() {
        refreshWSDL("client","",true);
    }

    public static TestSuite suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new WsValidation("testCreateNewWs")); //NOI18N
        suite.addTest(new WsValidation("testAddOperation")); //NOI18N
        suite.addTest(new WsValidation("testStartServer")); //NOI18N
        suite.addTest(new WsValidation("testWsHandlers")); //NOI18N
        suite.addTest(new WsValidation("testDeployWsProject")); //NOI18N
        suite.addTest(new WsValidation("testCreateWsClient")); //NOI18N
        suite.addTest(new WsValidation("testCallWsOperationInServlet")); //NOI18N
        suite.addTest(new WsValidation("testCallWsOperationInJSP")); //NOI18N
        suite.addTest(new WsValidation("testCallWsOperationInJavaClass")); //NOI18N
        suite.addTest(new WsValidation("testRefreshClient")); //NOI18N
        suite.addTest(new WsValidation("testWsClientHandlers")); //NOI18N
        suite.addTest(new WsValidation("testDeployWsClientProject")); //NOI18N
        suite.addTest(new WsValidation("testUndeployProjects")); //NOI18N
        suite.addTest(new WsValidation("testStopServer")); //NOI18N
        return suite;
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    protected void addWsOperation(EditorOperator eo, String opName, String opRetVal) {
        //Add Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "LBL_OperationAction");
        addMethod(eo, actionName, opName, opRetVal);
    }

    protected void addMethod(final EditorOperator eo, String dlgTitle, String opName, String opRetVal) {
        NbDialogOperator dialog = new NbDialogOperator(dlgTitle);
        new JTextFieldOperator(dialog, 2).setText(opName);
        new JTextFieldOperator(dialog, 1).setText(opRetVal);
        dialog.ok();
        eo.save();
        waitForTextInEditor(eo, opName);
    }

    protected void callWsOperation(final EditorOperator eo, String opName, int line) {
        //Web Service Client Resources
        String actionGroupName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "LBL_WebServiceClientActionGroup");
        //Call Web Service Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "LBL_CallWebServiceOperation");
        try {
            new ActionNoBlock(null, actionGroupName + "|" + actionName).performPopup(eo); //NOI18N
        } catch (TimeoutExpiredException tee) {
            eo.select(line);
            new ActionNoBlock(null, actionGroupName + "|" + actionName).performPopup(eo); //NOI18N
        }
        //Select Operation to Invoke
        String dlgTitle = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "TTL_SelectOperation");
        NbDialogOperator ndo = new NbDialogOperator(dlgTitle);
        JTreeOperator jto = new JTreeOperator(ndo);
        jto.selectPath(jto.findPath(
                getWsClientProjectName() + "|" + getWsName()//NOI18N
                + "|" + getWsName() + "Service|" //NOI18N
                + getWsName() + "Port|" + opName)); //NOI18N
        eo.select(line);
        ndo.ok();
        waitForTextInEditor(eo, "port." + opName); //NOI18N
    }

    protected String getWsClientLookupCall() {
        return getWsClientPackage() + "." + getWsName() + "Service " +
                "service = new " +
                getWsClientPackage() + "." + getWsName() + "Service();";
    }

    protected void createHandler(String pkg, String name, HandlerType type) {
        createNewWSFile(getProject(), type.getFileTypeLabel());
        NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
        op.txtObjectName().clearText();
        op.txtObjectName().typeText(name);
        op.cboPackage().clearText();
        op.cboPackage().typeText(pkg);
        op.finish();
        EditorOperator eo = new EditorOperator(name);
        assertTrue(eo.contains(type.getMessageType()));
        eo.close();
    }

    private void configureHandlers(Node n, File handlerCfg, boolean isService) throws IOException {
        assertFalse(handlerCfg.exists());
        //Configure Handlers...
        String handlersLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.Bundle", "LBL_ConfigureHandlerAction");
        n.performPopupActionNoBlock(handlersLabel);
        //Configure Message Handlers
        String handlersDlgLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.nodes.Bundle", "TTL_MessageHandlerPanel");
        NbDialogOperator ndo = new NbDialogOperator(handlersDlgLabel);

        //add 2 handlers
        String[] handlers = {"WsMsgHandler1", "WsLogHandler1"}; //NOI18N
        addHandlers(ndo, handlers);
        ndo.ok();
        EditorOperator eo = null;
        if (isService) {
            eo = new EditorOperator(getWsName());
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"" + getWsName() + "_handler.xml\")")); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        FileObject fo = FileUtil.toFileObject(handlerCfg);
        checkHandlers(new String[]{
                    "WsLogHandler1", "WsMsgHandler1" //NOI18N
                }, fo, isService);

        //remove one handler
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        removeHandlers(ndo, new String[]{"WsLogHandler1"}); //NOI18N
        ndo.ok();
        if (isService) {
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"MyWebWs_handler.xml\")")); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        checkHandlers(new String[]{"WsMsgHandler1"}, fo, isService); //NOI18N

        //add remaining handlers
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        addHandlers(ndo, new String[]{"WsLogHandler1", "WsLogHandler2", "WsMsgHandler2"}); //NOI18N
        ndo.ok();
        if (isService) {
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"MyWebWs_handler.xml\")")); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        checkHandlers(new String[]{
                    "WsLogHandler1", "WsLogHandler2", //NOI18N
                    "WsMsgHandler1", "WsMsgHandler2" //NOI18N
                }, fo, isService);

        //move up one handler
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        moveUpHandler(ndo, "WsLogHandler2"); //NOI18N
        ndo.ok();
        if (isService) {
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"MyWebWs_handler.xml\")")); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        checkHandlers(new String[]{
                    "WsLogHandler2", "WsLogHandler1", //NOI18N
                    "WsMsgHandler1", "WsMsgHandler2" //NOI18N
                }, fo, isService);

        //move down another one
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        moveDownHandler(ndo, "WsMsgHandler1"); //NOI18N
        ndo.ok();
        if (isService) {
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"MyWebWs_handler.xml\")")); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        checkHandlers(new String[]{
                    "WsLogHandler2", "WsLogHandler1", //NOI18N
                    "WsMsgHandler2", "WsMsgHandler1" //NOI18N
                }, fo, isService);

        //finally remove all handlers
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        removeHandlers(ndo, new String[]{
                    "WsMsgHandler2", "WsLogHandler2", //NOI18N
                    "WsLogHandler1", "WsMsgHandler1"
                }); //NOI18N
        ndo.ok();

        if (isService) {
            assertFalse("offending @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"MyWebWs_handler.xml\")")); //NOI18N
            assertFalse(handlerCfg.exists());
        }
    }

    /**
     * Check non-JSR-109 service (web service DD, application DD)
     *
     * @throws java.io.IOException
     */
    protected void checkNonJSR109Service() throws IOException {
        if (ServerType.TOMCAT.equals(REGISTERED_SERVER)) {
            FileObject projectHome = getProject().getProjectDirectory();
            FileObject webInfFO = projectHome.getFileObject("web/WEB-INF"); //NOI18N
            //check sun-jaxws.xml
            FileObject sunJaxWsFO = webInfFO.getFileObject("sun-jaxws.xml"); //NOI18N
            assertNotNull("sun-jaxws.xml present", sunJaxWsFO); //NOI18N
            assertTrue("sun-jaxws.xml present", FileUtil.toFile(sunJaxWsFO).exists()); //NOI18N
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunJaxWsFO);
            assertEquals("Should have one endpoint", 1, endpoints.getEndpoints().length); //NOI18N
            Endpoint endpoint = endpoints.findEndpointByName(getWsName());
            assertNotNull(getWsName() + " is missing in sun-jaxws.xml", endpoint); //NOI18N
            //check web.xml
            FileObject webXmlFO = webInfFO.getFileObject("web.xml"); //NOI18N
            WebApp webDD = DDProvider.getDefault().getDDRoot(webXmlFO);
            Listener[] listeners = webDD.getListener();
            assertEquals("1 listener present", 1, listeners.length); //NOI18N
            assertEquals("Invalid listener class", //NOI18N
                    "com.sun.xml.ws.transport.http.servlet.WSServletContextListener", //NOI18N
                    listeners[0].getListenerClass());
            Servlet[] servlets = webDD.getServlet();
            assertEquals("1 servlet present", 1, servlets.length); //NOI18N
            assertEquals("Invalid servlet name", getWsName(), servlets[0].getServletName()); //NOI18N
            assertEquals("Invalid servlet class", //NOI18N
                    "com.sun.xml.ws.transport.http.servlet.WSServlet", //NOI18N
                    servlets[0].getServletClass());
            ServletMapping[] mappings = webDD.getServletMapping();
            assertEquals("1 servlet mapping present", 1, mappings.length); //NOI18N
            assertEquals("Invalid servlet mapping name", getWsName(), mappings[0].getServletName()); //NOI18N
            assertEquals("Invalid url pattern", "/" + getWsName(), //NOI18N
                    mappings[0].getUrlPattern());
        }
    }

    protected void waitForWsImport(String targetName) throws IOException {
        OutputTabOperator oto = new OutputTabOperator(targetName); //NOI18N
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 300000); //NOI18N
        oto.waitText("(total time: "); //NOI18N
        dumpOutput();
        assertTrue(oto.getText().indexOf("BUILD SUCCESSFUL") > -1); //NOI18N
    }

    protected void waitForTextInEditor(final EditorOperator eo, final String text) {
        try {
            new Waiter(new Waitable() {

                public Object actionProduced(Object obj) {
                    return eo.contains(text) ? Boolean.TRUE : null; //NOI18N
                }

                public String getDescription() {
                    return ("Editor contains " + text); //NOI18N
                }
            }).waitAction(null);
        } catch (InterruptedException ie) {
            throw new JemmyException("Interrupted.", ie); //NOI18N
        }
    }

    protected Project getWsClientProject() {
        ProjectRootNode node = new ProjectsTabOperator().getProjectRootNode(getWsClientProjectName());
        Project p = ((org.openide.nodes.Node) node.getOpenideNode()).getLookup().lookup(Project.class);
        assertNotNull("Project can't be null", p); //NOI18N
        return p;
    }

    protected String getHandlersPackage() {
        return "o.n.m.ws.qaf.handlers"; //NOI18N
    }

    private void addHandlers(NbDialogOperator ndo, String[] handlers) {
        //Add...
        JButtonOperator jbo = new JButtonOperator(ndo, 0);
        //Add Message Handler Class
        String addHandlerDlg = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.nodes.Bundle", "TTL_SelectHandler");
        //Source Packages
        String srcPkgLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir");
        for (int i = 0; i < handlers.length; i++) {
            jbo.pushNoBlock();
            NbDialogOperator ndo2 = new NbDialogOperator(addHandlerDlg);
            JTreeOperator jto2 = new JTreeOperator(ndo2);
            Node spn = new Node(jto2, srcPkgLabel);
            Node pkg = new Node(spn, getHandlersPackage());
            Node handler = new Node(pkg, handlers[i]);
            handler.select();
            ndo2.ok();
        }
    }

    private void removeHandlers(NbDialogOperator ndo, String[] handlers) {
        //Confirm Handler Configuration Change
        String changeTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.ui.panels.Bundle", "TTL_CONFIRM_DELETE");
        JTableOperator jto = new JTableOperator(ndo);
        for (int i = 0; i < handlers.length; i++) {
            jto.selectCell(jto.findCellRow(handlers[i]), jto.findCellColumn(handlers[i]));
            //Remove
            new JButtonOperator(ndo, 2).pushNoBlock();
            new NbDialogOperator(changeTitle).yes();
        }
    }

    private void moveUpHandler(NbDialogOperator ndo, String handler) {
        JTableOperator jto = new JTableOperator(ndo);
        jto.selectCell(jto.findCellRow(handler), jto.findCellColumn(handler));
        //Move Up
        new JButtonOperator(ndo, 1).pushNoBlock();
    }

    private void moveDownHandler(NbDialogOperator ndo, String handler) {
        JTableOperator jto = new JTableOperator(ndo);
        jto.selectCell(jto.findCellRow(handler), jto.findCellColumn(handler));
        //Move Down
        new JButtonOperator(ndo, 3).pushNoBlock();
    }

    /**
     * According to parameter this method invokes Refresh action on proper node in
     * Projects tab
     * @param type
     */
    public void refreshWSDL(String type,java.lang.String wsname,boolean includeSources) {
        ProjectsTabOperator prj = new ProjectsTabOperator();
        JTreeOperator prjtree = new JTreeOperator(prj);
        ProjectRootNode prjnd;
        Node actual;
        NbDialogOperator ccr;
        if (type.equalsIgnoreCase("service")) {
            prjnd = new ProjectRootNode(prjtree, getWsProjectName());
            if(!wsname.equalsIgnoreCase("")){
                actual = new Node(prjnd, "Web Services|" + wsname); //NOI18N
            }
            else {
                actual = new Node(prjnd, "Web Services|" + getWsName()); //NOI18N  
            }
            actual.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_RefreshServiceAction")); //NOI18N
            ccr = new NbDialogOperator("Confirm Service Refresh"); //NOI18N
            new EventTool().waitNoEvent(2000);
            if(includeSources) {
                new JCheckBoxOperator(ccr,0).push();
                new EventTool().waitNoEvent(10000);
            }
            ccr.yes();
        } else {
            prjnd = new ProjectRootNode(prjtree, getWsClientProjectName());
            if(!getWsName().contains("Web")){
               actual = new Node(prjnd, "Web Service References|" + getWsName()); //NOI18N 
            }
            else {
               actual = new Node(prjnd, "Web Service References|" + getWsName() + "Service"); //NOI18N  
            }
            actual.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_RefreshClientAction")); //NOI18N
            ccr = new NbDialogOperator("Confirm Client Refresh"); //NOI18N
            new EventTool().waitNoEvent(2000);
            if(includeSources) {
                new JCheckBoxOperator(ccr,0).push();
                new EventTool().waitNoEvent(10000);
            }
            ccr.yes();
        }
    }

    private void checkHandlers(String[] handlerClasses, FileObject handlerConfigFO, boolean isService) throws IOException {
        //Let's keep the config file to resolve possible issues
        handlerConfigFO.copy(FileUtil.toFileObject(getWorkDir()), handlerConfigFO.getName() + foId++, "xml"); //NOI18N
        if (isService) {
            HandlerChains hChains = HandlerChainsProvider.getDefault().getHandlerChains(handlerConfigFO);
            HandlerChain[] chains = hChains.getHandlerChains();
            assertEquals(1, chains.length);
            Handler[] handlers = chains[0].getHandlers();
            assertEquals("Some handler is missing?", handlerClasses.length, handlers.length); //NOI18N
            for (int i = 0; i < handlerClasses.length; i++) {
                Handler h = handlers[i];
                assertEquals(getHandlersPackage() + "." + handlerClasses[i], h.getHandlerName());
                assertEquals(getHandlersPackage() + "." + handlerClasses[i], h.getHandlerClass());
            }
        } else {
            ModelSource ms = Utilities.getModelSource(handlerConfigFO, false);
            BindingsModel bindingsModel = BindingsModelFactory.getDefault().getModel(ms);
            BindingsHandlerChains bChains = bindingsModel.getGlobalBindings().getDefinitionsBindings().getHandlerChains();
            Collection<BindingsHandlerChain> bHChains = bChains.getHandlerChains();
            assertEquals(1, bHChains.size());
            Collection<BindingsHandler> bHandlers = bHChains.iterator().next().getHandlers();
            assertEquals(handlerClasses.length, bHandlers.size());
            int i = 0;
            for (BindingsHandler h : bHandlers) {
                assertEquals(getHandlersPackage() + "." + handlerClasses[i],
                        h.getHandlerClass().getClassName());
                i++;
            }
        }
    }
}
