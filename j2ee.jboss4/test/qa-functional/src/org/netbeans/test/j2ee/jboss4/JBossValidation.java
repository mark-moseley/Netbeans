/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.test.j2ee.jboss4;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.test.j2ee.lib.Util;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jemmy.operators.*;
import java.util.*;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 *
 * @author jpospisil
 */
public class JBossValidation extends JellyTestCase {
    
    public static final String PROJECT_NAME = "TestDeployDebugWebApp";
    public static final String EJB_PROJECT_NAME = "ejb";
    public static final String EJB_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + EJB_PROJECT_NAME;
    public static final String JBOSS_PATH = "E:\\Work\\Appservers\\jboss-4.0.1sp1\\jboss-4.0.1sp1";
    public static final String WEB_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + PROJECT_NAME;
    
    
    
    int modifiers;
    
    public static String openSourceAction = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    
    /** Need to be defined because of JUnit */
    public JBossValidation(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new JBossValidation("addDefaultJBoss"));
        suite.addTest(new JBossValidation("redeployWebModule"));
        suite.addTest(new JBossValidation("stopJBoss"));
        suite.addTest(new JBossValidation("runDebugWebModule"));
        suite.addTest(new JBossValidation("stopJBoss"));
        suite.addTest(new JBossValidation("deployEJBModule"));
        suite.addTest(new JBossValidation("stopJBoss"));
        return suite;
    }
  
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        if (System.getProperty("os.name").startsWith("Mac OS X")) {
            modifiers = KeyEvent.META_DOWN_MASK;
        } else {
            modifiers = KeyEvent.CTRL_DOWN_MASK;
        }
    }
    
   public void addJBoss(String domain){  
       String path = System.getProperty("jboss.server.path");
       if (path == null) {
            throw new RuntimeException("Cannot setup jboss, property jboss.server.path is not set.");
        }
        Node node = new Node(new RuntimeTabOperator().getRootNode(),Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE"));
        node.performPopupActionNoBlock(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"));
        NbDialogOperator dialog = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"));
        new JComboBoxOperator(dialog).selectItem("JBoss Application Server 4.0");
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT")).push();
        new JTextFieldOperator(dialog).setText("");
        new JTextFieldOperator(dialog).typeText(path);
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT")).push();
        new JComboBoxOperator(dialog).selectItem(domain);
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH")).push();
        new ProjectsTabOperator();
    }
    
    
    // Adds JBoss server instance in default domain - working
    public void addDefaultJBoss(){  
        addJBoss("default");
    }
    
   
    // Adds JBoss server instance in minimal domain - working
    public void addMinimalJBoss(){
       addJBoss("minimal");
    }
    
    // Adds JBoss server instance in all domain - working
    public void addAllJBoss(){
        addJBoss("all");
    }
    
    // Starts JBoss server in default mode - working
    public void startJBoss(){
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node serverNode = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
                                   +"|JBoss");
                
        serverNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_StartStopServer"));
        JFrameOperator status = new JFrameOperator("Server Status");
        JButtonOperator startStopButton;
        startStopButton = new JButtonOperator(status,Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "LBL_StartServer"));
        startStopButton.getTimeouts().setTimeout("ComponentOperator.WaitComponentEnabledTimeout", 300000);
        try { startStopButton.waitComponentEnabled(); }
        catch (InterruptedException e) { }
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        startStopButton.push();
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();        

        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
    }
    
    // Starts JBoss server id debug mode - working
    public void startJBossDebug(){
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node serverNode = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
                                   +"|JBoss");
                
        serverNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_StartStopServer"));
        JFrameOperator status = new JFrameOperator("Server Status");
        JButtonOperator startStopButton;
        startStopButton = new JButtonOperator(status,Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "LBL_StartDebugServer"));
        startStopButton.getTimeouts().setTimeout("ComponentOperator.WaitComponentEnabledTimeout", 300000);
        try { startStopButton.waitComponentEnabled(); }
        catch (InterruptedException e) { }
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        startStopButton.push();
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();        

        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
    }
    
    // Stops running JBoss server - working,test of working server recommended to add
    public void stopJBoss(){
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node serverNode = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
                                   +"|JBoss");        
        serverNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_StartStopServer"));
        JFrameOperator status = new JFrameOperator("Server Status");
        JButtonOperator startStopButton;
        startStopButton = new JButtonOperator(status,Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "LBL_StopServer"));
        startStopButton.getTimeouts().setTimeout("ComponentOperator.WaitComponentEnabledTimeout", 300000);
        try { startStopButton.waitComponentEnabled(); }
        catch (InterruptedException e) { }
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        startStopButton.push();
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();        

        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
    }
    
     public void refreshJBoss(){  
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node serverNode = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
                                   +"|JBoss");     
        serverNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Refresh"));
    }
    
     
    // Removes JBoss server from Runtime tab - doesn't work/problem with pressing the OK button in Remove dialog
    public void removeJBoss(){
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node serverNode = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
                                   +"|JBoss");     
        serverNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Remove"));
        // The moment test is failing - now is on display dialog with Remove Server Instance in title
        JDialogOperator rsi = new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "MSG_RemoveInstanceTitle"));
        rsi.activate();
        //JButtonOperator cancelButton = new JButtonOperator(rsi,Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "LBL_Cancel"));
        rsi.move(0,0);
        //rsi.waitClosed();
        //new org.netbeans.jemmy.EventTool().waitNoEvent(1000);

        //JDialogOperator remove = new JDialogOperator("Remove Server Instance");
        //JDialogOperator remove = new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "MSG_RemoveInstanceTitle"));
        //remove.activate();
        //remove.moveMouse(10,10);
        //JFrameOperator status = new JFrameOperator("Remove Server Instance");
        //JButtonOperator yesButton = new JButtonOperator(remove,Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "LBL_Cancel"));
        //yesButton.waitComponentEnabled();
        /*JButtonOperator yesButton;
        yesButton = new JButtonOperator(status,Bundle.getStringTrimmed("org.netbeans.web.wizards.Bundle", "LBL_OK"));
        //yesButton.getTimeouts().setTimeout("ComponentOperator.WaitComponentEnabledTimeout", 300000);
        try { yesButton.waitComponentEnabled(); }
        catch (InterruptedException e) { }
        //new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        yesButton.push();*/
        //JButtonOperator yesButton = new JButtonOperator(status,Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "LBL_Cancel"));
        //yesButton.push();
        //new ProjectsTabOperator();
    }
     
    // Deploys web module on JBoss - working
    public void deployWebModule() {
        System.err.println(WEB_PROJECT_PATH);
        Util.openProject(WEB_PROJECT_PATH);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        ProjectsTabOperator projectTab=ProjectsTabOperator.invoke();
        ProjectRootNode prn = projectTab.getProjectRootNode(PROJECT_NAME);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        prn.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.Bundle", "LBL_RedeployAction_Name"));
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        JFrameOperator status = new JFrameOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle","LBL_Deploy_Progress_Monitor"));
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();
    }  
   
   // Deploys web module on JBoss twice - working 
    public void redeployWebModule() {
        System.err.println(WEB_PROJECT_PATH);
        Util.openProject(WEB_PROJECT_PATH);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        ProjectsTabOperator projectTab=ProjectsTabOperator.invoke();
        ProjectRootNode prn = projectTab.getProjectRootNode(PROJECT_NAME);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        prn.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.Bundle", "LBL_RedeployAction_Name"));
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        JFrameOperator status = new JFrameOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle","LBL_Deploy_Progress_Monitor"));
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        prn.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.Bundle", "LBL_RedeployAction_Name"));
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();
    }  
    // Debugs web module on JBoss - working
    public void debugWebModule() {
        System.err.println(WEB_PROJECT_PATH);
        Util.openProject(WEB_PROJECT_PATH);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        ProjectsTabOperator projectTab=ProjectsTabOperator.invoke();
        ProjectRootNode prn = projectTab.getProjectRootNode(PROJECT_NAME);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        prn.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.Bundle", "LBL_DebugAction_Name"));
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        JFrameOperator status = new JFrameOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle","LBL_Deploy_Progress_Monitor"));
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();
    }  
    
    // Deploys web module on JBoss and then debugs it - working partially
    public void runDebugWebModule() {
        System.err.println(WEB_PROJECT_PATH);
        Util.openProject(WEB_PROJECT_PATH);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        ProjectsTabOperator projectTab=ProjectsTabOperator.invoke();
        ProjectRootNode prn = projectTab.getProjectRootNode(PROJECT_NAME);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        prn.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.Bundle", "LBL_RedeployAction_Name"));
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        JFrameOperator status = new JFrameOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle","LBL_Deploy_Progress_Monitor"));
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        prn.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.Bundle", "LBL_DebugAction_Name"));
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        JFrameOperator status2 = new JFrameOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle","LBL_Deploy_Progress_Monitor"));
        status2.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status2.waitClosed();
        
    }  
    
    public void deployEJBModule() {
        System.err.println(EJB_PROJECT_PATH);
        Util.openProject(EJB_PROJECT_PATH);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        ProjectsTabOperator projectTab=ProjectsTabOperator.invoke();
        ProjectRootNode prn = projectTab.getProjectRootNode(EJB_PROJECT_NAME);
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        prn.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.Bundle", "LBL_RedeployAction_Name"));
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        JFrameOperator status = new JFrameOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle","LBL_Deploy_Progress_Monitor"));
        status.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        status.waitClosed();
    }
    
    
    
   
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }
    
}
