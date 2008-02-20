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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.Properties;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.web.NewJspFileNameStepOperator;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.ide.ProjectSupport;

/** End-to-end scenario test based on
 * http://qa.netbeans.org/modules/webapps/promo-f/frameworks/struts-user-scenario.html.
 *
 * @author Jiri Skrivanek
 */
public class EndToEndTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "StrutsWebApplication";
    
    /** Constructor required by JUnit */
    public EndToEndTest(String name) {
        super(name);
    }
    
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new EndToEndTest("testSetupStrutsProject"));
        suite.addTest(new EndToEndTest("testCreateLoginPage"));
        suite.addTest(new EndToEndTest("testCreateLoginBean"));
        suite.addTest(new EndToEndTest("testCreateLoginAction"));
        suite.addTest(new EndToEndTest("testCreateSecurityManager"));
        suite.addTest(new EndToEndTest("testCreateForward"));
        suite.addTest(new EndToEndTest("testCreateShopPage"));
        suite.addTest(new EndToEndTest("testCreateLogoutPage"));
        suite.addTest(new EndToEndTest("testCreateForwardInclude"));
        suite.addTest(new EndToEndTest("testRunApplication"));
        return suite;
    }
    
    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new EndToEndTest("test1"));
    }
    
    /** Called before every test case. */
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Called after every test case. */
    @Override
    public void tearDown() {
    }
    
    /** Create web application with struts support and check correctness. */
    public void testSetupStrutsProject() throws IOException {
        // "Web"
        String web = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "OpenIDE-Module-Display-Category");
        // "Web Application"
        String webApplication = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        NewProjectWizardOperator nop = NewProjectWizardOperator.invoke();
        nop.selectCategory(web);
        nop.selectProject(webApplication);
        nop.next();
        NewWebProjectNameLocationStepOperator lop = new NewWebProjectNameLocationStepOperator();
        lop.setProjectName(PROJECT_NAME);
        lop.setProjectLocation(getDataDir().getCanonicalPath());
        lop.next();
        lop.next();
        NewProjectWizardOperator frameworkStep = new NewProjectWizardOperator();
        // select Struts
        JTableOperator tableOper = new JTableOperator(frameworkStep);
        for(int i=0; i<tableOper.getRowCount(); i++) {
            if(tableOper.getValueAt(i, 1).toString().startsWith("org.netbeans.modules.web.struts.StrutsFrameworkProvider")) { // NOI18N
                tableOper.selectCell(i, 0);
                break;
            }
        }
        // set ApplicationResource location
        new JTextFieldOperator(
                (JTextField)new JLabelOperator(frameworkStep, "Application Resource:").getLabelFor()
                ).setText("com.mycompany.eshop.struts.ApplicationResource");
        frameworkStep.btFinish().pushNoBlock();
        frameworkStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        frameworkStep.waitClosed();
        // Opening Projects
        String openingProjectsTitle = Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_Opening_Projects_Progress");
        try {
            // wait at most 60 second until progress dialog dismiss
            NbDialogOperator openingOper = new NbDialogOperator(openingProjectsTitle);
            frameworkStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
            openingOper.waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
        ProjectSupport.waitScanFinished();
        // Check project contains all needed files.
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        new Node(webPages, "welcomeStruts.jsp");
        Node strutsConfig = new Node(webPages, "WEB-INF|struts-config.xml");
        new OpenAction().performAPI(strutsConfig);
        webPages.setComparator(new DefaultStringComparator(true, true));
        Node webXML = new Node(webPages, "WEB-INF|web.xml");
        new EditAction().performAPI(webXML);
        EditorOperator webXMLEditor = new EditorOperator("web.xml");
        String expected = "<servlet-class>org.apache.struts.action.ActionServlet</servlet-class>";
        assertTrue("ActionServlet should be created in web.xml.", webXMLEditor.getText().indexOf(expected) > -1);
        webXMLEditor.replace("index.jsp", "login.jsp");
        webXMLEditor.save();
    }
    
    /** Create login.jsp and insert prepared source code to it. */
    public void testCreateLoginPage() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Web");
        newWizardOper.selectFileType("JSP");
        newWizardOper.next();
        NewJspFileNameStepOperator jspStepOper = new NewJspFileNameStepOperator();
        jspStepOper.setJSPFileName("login");
        jspStepOper.setFolder("");
        jspStepOper.finish();
        // verify
        EditorOperator loginEditorOper = new EditorOperator("login.jsp");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("Bundle.properties"));
        String sourceCode = properties.getProperty("login");
        // wait for text to be displayed
        loginEditorOper.txtEditorPane().waitText("JSP Page", -1);
        loginEditorOper.replace(loginEditorOper.getText(), sourceCode);
        loginEditorOper.save();
    }
    
    /** Create bean which handles login form. */
    public void testCreateLoginBean() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Struts");
        newWizardOper.selectFileType("Struts ActionForm Bean");
        newWizardOper.next();
        NewFileNameLocationStepOperator nameStepOper = new NewFileNameLocationStepOperator();
        nameStepOper.setObjectName("LoginForm");
        nameStepOper.setPackage("com.mycompany.eshop.struts.forms");
        nameStepOper.finish();
        EditorOperator loginEditorOper = new EditorOperator("LoginForm.java");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("Bundle.properties"));
        String sourceCode = properties.getProperty("LoginForm");
        loginEditorOper.replace(loginEditorOper.getText(), sourceCode);
        loginEditorOper.save();
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        String expected = "<form-bean name=\"LoginForm\" type=\"com.mycompany.eshop.struts.forms.LoginForm\"/>";
        assertTrue("form-bean record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
    }
    
    /** Create struts action which verify input fields in login form. */
    public void testCreateLoginAction() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Struts");
        newWizardOper.selectFileType("Struts Action");
        newWizardOper.next();
        NewFileNameLocationStepOperator nameStepOper = new NewFileNameLocationStepOperator();
        nameStepOper.setObjectName("LoginVerifyAction");
        nameStepOper.setPackage("com.mycompany.eshop.struts.actions");
        JTextFieldOperator txtActionPath = new JTextFieldOperator(
                (JTextField)new JLabelOperator(nameStepOper, "Action Path:").getLabelFor());
        txtActionPath.setText("/Login/Verify");
        nameStepOper.next();
        // "ActionForm Bean, Parameter" page
        NewFileWizardOperator actionBeanStepOper = new NewFileWizardOperator();
        // set Input Resource
        new JTextFieldOperator(actionBeanStepOper, "/").setText("/login.jsp");
        new JRadioButtonOperator(actionBeanStepOper, "Request").push();
        actionBeanStepOper.finish();
        EditorOperator loginEditorOper = new EditorOperator("LoginVerifyAction.java");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("Bundle.properties"));
        String sourceCode = properties.getProperty("LoginVerifyAction");
        loginEditorOper.replace("return mapping.findForward(SUCCESS);", sourceCode);
        loginEditorOper.save();
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        String expected = "<action input=\"/login.jsp\" name=\"LoginForm\" path=\"/Login/Verify\" scope=\"request\" type=\"com.mycompany.eshop.struts.actions.LoginVerifyAction\"/>";
        assertTrue("action record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
    }
    
    /** Create SecurityManager class.  */
    public void testCreateSecurityManager() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        // need to distinguish Java and Java Server Faces
        newWizardOper.treeCategories().setComparator(new DefaultStringComparator(true, true));
        newWizardOper.selectCategory("Java");
        newWizardOper.selectFileType("Empty Java File");
        newWizardOper.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.setObjectName("SecurityManager");
        nfnlso.setPackage("com.mycompany.eshop.security");
        nfnlso.finish();
        EditorOperator editorOper = new EditorOperator("SecurityManager.java");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("Bundle.properties"));
        String sourceCode = properties.getProperty("SecurityManager");
        editorOper.replace(editorOper.getText(), sourceCode);
        editorOper.save();
    }
    
    /** Call "Add Forward" action in struts-config.xml and fill in the dialog values. */
    public void testCreateForward() {
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        ActionNoBlock addForwardAction = new ActionNoBlock(null, "Struts|Add Forward");
        addForwardAction.setComparator(new DefaultStringComparator(true, true));
        addForwardAction.perform(strutsConfigEditor);
        NbDialogOperator addForwardOper = new NbDialogOperator("Add Forward");
        JTextFieldOperator txtForwardName = new JTextFieldOperator(
                (JTextField)new JLabelOperator(addForwardOper, "Forward Name:").getLabelFor());
        txtForwardName.setText("success");
        new JTextFieldOperator(addForwardOper, "/").setText("/shop.jsp");
        // set Redirect check box
        new JCheckBoxOperator(addForwardOper).push();
        // select Action as Location
        new JRadioButtonOperator(addForwardOper, "Action:", 1).push();
        new JButtonOperator(addForwardOper, "Add").push();
        String expected = "<forward name=\"success\" path=\"/shop.jsp\" redirect=\"true\"/>";
        assertTrue("forward record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
        strutsConfigEditor.save();
    }
    
    /** Create shop.jsp and insert prepared source code to it. */
    public void testCreateShopPage() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Web");
        newWizardOper.selectFileType("JSP");
        newWizardOper.next();
        NewJspFileNameStepOperator jspStepOper = new NewJspFileNameStepOperator();
        jspStepOper.setJSPFileName("shop");
        jspStepOper.setFolder("");
        jspStepOper.finish();
        // verify
        EditorOperator editorOper = new EditorOperator("shop.jsp");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("Bundle.properties"));
        String sourceCode = properties.getProperty("shop");
        // wait for text to be displayed
        editorOper.txtEditorPane().waitText("JSP Page", -1);
        editorOper.replace(editorOper.getText(), sourceCode);
        editorOper.save();
    }
    
    /** Create logout.jsp and insert prepared source code to it. */
    public void testCreateLogoutPage() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Web");
        newWizardOper.selectFileType("JSP");
        newWizardOper.next();
        NewJspFileNameStepOperator jspStepOper = new NewJspFileNameStepOperator();
        jspStepOper.setJSPFileName("logout");
        jspStepOper.setFolder("");
        jspStepOper.finish();
        // verify
        EditorOperator editorOper = new EditorOperator("logout.jsp");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("Bundle.properties"));
        String sourceCode = properties.getProperty("logout");
        // wait for text to be displayed
        editorOper.txtEditorPane().waitText("JSP Page", -1);
        editorOper.replace(editorOper.getText(), sourceCode);
        editorOper.save();
    }
    
    /** Call "Add Forward/Include" action in struts-config.xml and fill in the dialog values. */
    public void testCreateForwardInclude() {
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        ActionNoBlock addForwardAction = new ActionNoBlock(null, "Struts|Add Forward/Include");
        addForwardAction.perform(strutsConfigEditor);
        NbDialogOperator addForwardOper = new NbDialogOperator("Add Forward/Include Action");
        // set Action Path
        new JTextFieldOperator(addForwardOper, "/").setText("/Logout");
        new JButtonOperator(addForwardOper, "Browse").pushNoBlock();
        NbDialogOperator browseOper = new NbDialogOperator("Browse Files");
        new Node(new JTreeOperator(browseOper), "Web Pages|logout.jsp").select();
        new JButtonOperator(browseOper, "Select File").push();
        new JButtonOperator(addForwardOper, "Add").push();
        String expected = "<action forward=\"/logout.jsp\" path=\"/Logout\"/>";
        assertTrue("forward record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
        strutsConfigEditor.save();
    }

    /** Run created application. */
    public void testRunApplication() {
        // not display browser on run
        // open project properties
        ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME).properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Run" category
        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
        String displayBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
        new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
        // confirm properties dialog
        propertiesDialogOper.ok();

        try {
            // "Run Project"
            String runProjectItem = Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_RunAction_Name");
            new Action(null, runProjectItem).perform(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME));
            waitText(PROJECT_NAME, 240000, "Login page");
        } finally {
            // log messages from output
            getLog("RunOutput").print(new OutputTabOperator(PROJECT_NAME).getText());
            getLog("ServerLog").print(new OutputTabOperator("GlassFish").getText());
            // stop server
            try {
                J2eeServerNode serverNode = new J2eeServerNode("GlassFish");
                serverNode.stop();
            } catch (JemmyException e) {
                // ignore it
            }
        }
    }
    
    /** Opens URL connection and waits for given text. It thows TimeoutExpiredException
     * if timeout expires.
     * @param urlSuffix suffix added to server URL
     * @param timeout time to wait
     * @param text text to be found
     */
    public static void waitText(final String urlSuffix, final long timeout, final String text) {
        Waitable waitable = new Waitable() {
            public Object actionProduced(Object obj) {
                InputStream is = null;
                try {
                    URLConnection connection = new URI("http://localhost:8080/"+urlSuffix).toURL().openConnection();
                    connection.setReadTimeout(Long.valueOf(timeout).intValue());
                    is = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = br.readLine();
                    while(line != null) {
                        if(line.indexOf(text) > -1) {
                            return Boolean.TRUE;
                        }
                        line = br.readLine();
                    }
                    is.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                    return null;
                } finally {
                    if(is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
                return null;
            }
            public String getDescription() {
                return("Text \""+text+"\" at http://localhost:8080/"+urlSuffix);
            }
        };
        Waiter waiter = new Waiter(waitable);
        waiter.getTimeouts().setTimeout("Waiter.WaitingTime", timeout);
        try {
            waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Exception while waiting for connection.", e);
        }
    }
}
