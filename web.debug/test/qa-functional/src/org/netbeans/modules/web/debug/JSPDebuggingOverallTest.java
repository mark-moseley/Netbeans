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
package org.netbeans.modules.web.debug;

import java.io.File;
import javax.swing.JDialog;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.AttachDialogOperator;
import org.netbeans.jellytools.modules.debugger.SessionsOperator;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.actions.RefreshAction;
import org.netbeans.jellytools.modules.j2ee.actions.RestartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StartDebugAction;
import org.netbeans.jellytools.modules.j2ee.actions.StopAction;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/** Test of web application debugging. Manual test specification is here:
 * http://qa.netbeans.org/modules/webapps/promo-f/jspdebug/jspdebug-testspec.html
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class JSPDebuggingOverallTest extends J2eeTestCase {
    // status bar tracer used to wait for state
    private MainWindowOperator.StatusTextTracer stt;
    
    public JSPDebuggingOverallTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        if(Utils.DEFAULT_SERVER.equals(Utils.TOMCAT)) {
            return NbModuleSuite.create(addServerTests(NbModuleSuite.createConfiguration(JSPDebuggingOverallTest.class),
                    "testOpenProjects",
                    "testSetTomcatPort", /// <---
                    "testRunProject",
                    "testSetBreakpoint",
                    "testDebugProject",
                    "testDebugReload",
                    "testAttachDebugger",
                    "testDebugAfterBreakpoint",
                    "testDebugAndStopServer",
                    "testStartAnotherSession",
                    "testJavaSession",
                    "testStopServer"
                    ).enableModules(".*").clusters(".*"));
        } else {
            return NbModuleSuite.create(addServerTests(NbModuleSuite.createConfiguration(JSPDebuggingOverallTest.class),
                    "testOpenProjects",
                    "testRunProject",
                    "testSetBreakpoint",
                    "testDebugProject",
                    "testDebugReload",
                    "testAttachDebugger",
                    "testDebugAfterBreakpoint",
                    "testDebugAndStopServer",
                    "testStartAnotherSession",
                    "testJavaSession",
                    "testStopServer"
                    ).enableModules(".*").clusters(".*"));
        }
    }
    
    /** Print test name and initialize status bar tracer. */
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        stt = MainWindowOperator.getDefault().getStatusTextTracer();
        // start to track Main Window status bar
        stt.start();
        // increase timeout to 60 seconds when waiting for status bar text
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
    }
    
    /** Stops status bar tracer. */
    @Override
    public void tearDown() {
        stt.stop();
    }
    
    // name of sample web application project
    private static final String SAMPLE_WEB_PROJECT_NAME = "MainTestApplication";  //NOI18N
    
    
    /** Opens test projects. */
    public void testOpenProjects() {
        String[] projects = {"MainTestApplication", "TestFreeformLibrary", "TestLibrary", "TestTagLibrary"}; //NOI18N
        for(int i=0;i<projects.length;i++) {
            ProjectSupport.openProject(new File(getDataDir(), projects[i]));
        }
        // check missing target server dialog is shown    
        // "Open Project"
        String openProjectTitle = Bundle.getString("org.netbeans.modules.j2ee.common.ui.Bundle", "MSG_Broken_Server_Title");
        boolean needToSetServer = false;
        if(JDialogOperator.findJDialog(openProjectTitle, true, true) != null) {
            new NbDialogOperator(openProjectTitle).close();
            needToSetServer = true;
        }
        // Set as Main Project
        String setAsMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetAsMainProjectAction_Name");
        new Action(null, setAsMainProjectItem).perform(new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME));
        // not display browser on run
        // open project properties
        ProjectsTabOperator.invoke().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME).properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Run" category
        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
        String displayBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
        new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
        if(needToSetServer) {
            // set default server
            new JComboBoxOperator(propertiesDialogOper).setSelectedIndex(0);
        }
        // confirm properties dialog
        propertiesDialogOper.ok();
        // if setting default server, it scans server jars; otherwise it continues immediatelly
        ProjectSupport.waitScanFinished();
        // start thread to close any information dialog
        closeInformationDialog();
    }
    
    /** Set a random port for Tomcat server and socket debugger transport. */
    public void testSetTomcatPort() throws Exception {
        Utils.setTomcatProperties();
    }
    
    /** Run project. */
    public void testRunProject() {
        String runProjectItem = Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_RunAction_Name");
        new Action(null, runProjectItem).perform(new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME));
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "run");
    }

    /** Set breakpoint.
     * - open index.jsp
     * - select <h1> in editor
     * - toggle breakpoint at selected line
     */
    public void testSetBreakpoint() throws Exception {
        new OpenAction().performAPI(new Node(new WebPagesNode(SAMPLE_WEB_PROJECT_NAME), "index.jsp")); // NOI18N
        // find sample file in Editor
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        Utils.setBreakpoint(eo, "<h1>"); // NOI18N
    }

    /** Debug project.
     * - on project node call Debug Project popup
     * - wait until debug task is finished
     */
    public void testDebugProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        new DebugProjectAction().perform(rootNode);
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
    }

    /** Reload browser while debugging.
     * - reload page
     * - wait until debugger stops at previously set breakpoint
     * - continue debugging
     * - finish debugger
     */
    public void testDebugReload() {
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check breakpoint reached
        // wait status text "Thread main stopped at SampleClass1.java:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:"+line);
        new ContinueAction().perform();
        Utils.finishDebugger();
    }
    
    /** Attach debugger.
     * - call Run|Attach Debugger... main menu item
     * - in Attach dialog set socket attach, port and click OK
     * - wait User program running appears in status bar
     * - reload page in browser
     * - check breakpoint reached
     * - finish debugger
     */
    public void testAttachDebugger() {
        // assuming server is running in debug mode
        AttachDialogOperator ado = AttachDialogOperator.invoke();
        // "JPDA Debugger"
        String jpdaDebuggerLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Connector_name");
        ado.selectDebugger(jpdaDebuggerLabel);
        ado.selectConnector(AttachDialogOperator.ITEM_SOCKET_ATTACH);
        ado.setPort(Utils.getSocketPort());
        ado.ok();
        // "User program running"
        String runningLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_running");
        stt.waitText(runningLabel);
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check breakpoint reached
        // wait status text "Thread main stopped at SampleClass1.java:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:"+line);
        Utils.finishDebugger();
    }
    
    /** Restart debugger after breakpoint reached.
     * - start to debug main project from main menu
     * - wait until debugger stops at previously set breakpoint
     * - finish debugger
     * - start debugger again
     * - wait until debugger stops at breakpoint
     * - finish debugger
     */
    public void testDebugAfterBreakpoint() {
        // start debugging
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check the first breakpoint reached
        // wait status text "Thread main stopped at index.jsp:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:"+line);
        stt.clear();
        Utils.finishDebugger();
        // start debugger again
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        stt.waitText("index.jsp:"+line); // NOI18N
        Utils.finishDebugger();
    }
    
    /** Restart debugger after server stopped.
     * - start to debug main project from main menu
     * - wait until debugger stops at previously set breakpoint
     * - check it is not possible to stop server
     * - finish debugger
     * - stop server
     * - start debugger again
     * - wait until debugger stops at breakpoint
     * - finish debugger
     */
    public void testDebugAndStopServer() {
        // start debugging
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check the first breakpoint reached
        // wait status text "Thread main stopped at index.jsp:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:"+line);
        stt.clear();
        
        // check it is not possible to stop server
        J2eeServerNode serverNode = new J2eeServerNode(Utils.DEFAULT_SERVER);
        assertFalse("Start action on server node should be disabled when stopped at breakpoint.", new StartAction().isEnabled(serverNode));
        assertFalse("Stop action on server node should be disabled when stopped at breakpoint.", new StopAction().isEnabled(serverNode));
        assertFalse("Restart action on server node should be disabled when stopped at breakpoint.", new RestartAction().isEnabled(serverNode));
        assertFalse("Start in Debug Mode action on server node should be disabled when stopped at breakpoint.", new StartDebugAction().isEnabled(serverNode));
        assertTrue("Refresh action on server node should be enabled when stopped at breakpoint.", new RefreshAction().isEnabled(serverNode));
        
        Utils.finishDebugger();
        serverNode.stop();
        // start debugger again
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        stt.waitText("index.jsp:"+line);
        Utils.finishDebugger();
    }
    
    /** Start another session.
     * - start to debug main project from main menu
     * - wait until debugger stops at previously set breakpoint
     * - try to start debugger again
     * - wait until message informing that server is in suspended state appears
     * - try to run project
     * - wait until message informing that server is in suspended state appears
     * - finish debugger
     * - wait for page in browser and close it
     */
    public void testStartAnotherSession() {
        // start debugging
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check the first breakpoint reached
        // wait status text "Thread main stopped at index.jsp:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:"+line);
        
        new DebugProjectAction().perform();
        OutputTabOperator outputOper = new OutputTabOperator(SAMPLE_WEB_PROJECT_NAME);
        // "Cannot perform required operation, since the server is currently in suspended state and thus cannot handle any requests."
        String suspendedMessage = Bundle.getString("org.netbeans.modules.j2ee.deployment.impl.Bundle", "MSG_ServerSuspended");
        outputOper.waitText(suspendedMessage);
        outputOper.close();
        
        String runProjectItem = Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_RunAction_Name");
        Action runProjectAction = new Action(null, runProjectItem);
        runProjectAction.perform(new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME));
        outputOper = new OutputTabOperator(SAMPLE_WEB_PROJECT_NAME);
        outputOper.waitText(suspendedMessage);
        outputOper.close();
        Utils.finishDebugger();
    }
    
    /** Test concurrent java and jsp debugging sessions. Also test debugging
     * of jsp in sub folder.
     * - open main class MyBean.java
     * - set breakpoint in it
     * - start debugger from popup on MyBean node
     * - wait until debugger stops at breakpoint
     * - open page simpleInclude.jsp in incl folder
     * - set breakpoint in it
     * - start debugger from popup on simpleInclude.jsp node
     * - wait until debugger stops at breakpoint
     * - open Window|Debugging|Session view
     * - call Make Current popup on MyBean debugging session
     * - wait until pointer is on breakpoint in MyBean.java
     * - call Make Current popup on simpleInclude.jsp debugging session
     * - wait until pointer is on breakpoint in simpleInclude.jsp
     * - call Finish All popup in Sessions view
     * - wait until debugger is finished
     * - close Sessions view
     */
    public void testJavaSession() throws Exception {
        Node beanNode = new Node(new SourcePackagesNode(SAMPLE_WEB_PROJECT_NAME), "org.netbeans.test|MyBean.java"); //NOI18N
        new OpenAction().performAPI(beanNode); // NOI18N
        EditorOperator eoBean = new EditorOperator("MyBean.java"); // NOI18N
        final int lineJavaSource = Utils.setBreakpoint(eoBean, "System.out.println"); // NOI18N
        new DebugAction().perform(beanNode);
        stt.waitText("MyBean.java:"+lineJavaSource); //NOI18N
        
        Node pageNode = new Node(new WebPagesNode(SAMPLE_WEB_PROJECT_NAME), "incl|simpleInclude.jsp"); //NOI18N
        new OpenAction().performAPI(pageNode);
        EditorOperator eoPage = new EditorOperator("simpleInclude.jsp"); // NOI18N
        final int lineJSP = Utils.setBreakpoint(eoPage, "incl/simpleInclude.jsp"); // NOI18N
        // "Debug File"
        new Action(null, new DebugAction().getPopupPath()).perform(pageNode);
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME+"/incl/simpleInclude.jsp");
        stt.waitText("simpleInclude.jsp:"+lineJSP); //NOI18N
        
        SessionsOperator so = SessionsOperator.invoke();
        so.makeCurrent("MyBean"); //NOI18N
        // wait pointer in editor (two annotations there)
        new Waiter(new Waitable() {
            public Object actionProduced(Object editorOper) {
                return ((EditorOperator)editorOper).getAnnotations(lineJavaSource).length == 2 ? Boolean.TRUE : null;
            }
            public String getDescription() {
                return("Wait 2 annotations in editor."); // NOI18N
            }
        }).waitAction(eoBean);
        // when issue 52506 fixed use proper name
        so.makeCurrent("name");
        // wait pointer in editor (two annotations there)
        new Waiter(new Waitable() {
            public Object actionProduced(Object editorOper) {
                return ((EditorOperator)editorOper).getAnnotations(lineJSP).length == 2 ? Boolean.TRUE : null;
            }
            public String getDescription() {
                return("Wait 2 annotations in editor."); // NOI18N
            }
        }).waitAction(eoPage);
        ContainerOperator debugToolbarOper = Utils.getDebugToolbar();
        so.finishAll();
        // wait until Debug toolbar dismiss
        debugToolbarOper.waitComponentVisible(false);
        so.close();
    }
    
    /** Stop server just for clean-up.
     * - stop server and wait until it finishes
     */
    public void testStopServer() {
        J2eeServerNode serverNode = new J2eeServerNode(Utils.DEFAULT_SERVER);
        serverNode.stop();
    }
    
    /** Sometimes is opened an information dialog with the following message:
     * "The Admin Server is stopped at a Break Point. No calls can be executed.
     * Please, complete your debugging session (continue) in order to be able
     * to manage this server instance."
     * It happens only on qa-perf-sol4 and I am not able to reproduce it
     * manually. That's why we start a new thread which is watching for this
     * dialog and close it if it appears.
     */
    private void closeInformationDialog() {
        new Thread(new Runnable() {
            public void run() {
                // "Information"
                String informationTitle = Bundle.getString("org.openide.Bundle", "NTF_InformationTitle");
                while (true) {
                    JDialog dialog = JDialogOperator.findJDialog(informationTitle, false, false);
                    if (dialog != null) {
                        new JDialogOperator(dialog).setVisible(false);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, "JSPDebuggingOverallTest - Wait for info dialog").start();
    }
}
