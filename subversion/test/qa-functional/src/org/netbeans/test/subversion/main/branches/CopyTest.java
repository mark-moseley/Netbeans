/*
 * CopyTest.java
 *
 * Created on June 6, 2006, 3:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.branches;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CopyToOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SwitchOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter pis
 */
public class CopyTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    public PrintStream stream;
    
    String os_name;
    
    /** Creates a new instance of CopyTest */
    public CopyTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        //suite.addTest(new CopyTest("testCreateNewCopySwitch"));
        suite.addTest(new CopyTest("testCreateNewCopy"));
        return suite;
    }
    
    public void testCreateNewCopySwitch() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator rso = new RepositoryStepOperator();       
        OutputTabOperator oto = new OutputTabOperator("SVN Output");
        oto.clear();
        
        //create repository... 
        new File(TMP_PATH).mkdirs();
        new File(TMP_PATH + File.separator + WORK_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/JavaApp");
        wdso.setLocalFolder(TMP_PATH + File.separator + WORK_PATH);
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project
        oto.waitText("Checking out... finished.");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        
        oto = new OutputTabOperator("SVN Output");
        oto.clear();
        Node projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
        CopyToOperator cto = CopyToOperator.invoke(projNode);
        cto.setRepositoryFolder("branches/release01/JavaApp");
        cto.setCopyPurpose("New branch for project.");
        cto.checkSwitchToCopy(true);
        cto.copy();
        oto.waitText("Copy");
        oto.waitText("finished.");
        
        Node nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp" + "|Main.java");
        org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node!!!", "[release01]", status);
        
        nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node!!!", "[release01]", status);
        //to do 
        TestKit.removeAllData("JavaApp");
        stream.flush();
        stream.close();
    }
    
    public void testCreateNewCopy() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator rso = new RepositoryStepOperator();       
        OutputTabOperator oto = new OutputTabOperator("SVN Output");
        oto.clear();
        
        //create repository... 
        new File(TMP_PATH).mkdirs();
        new File(TMP_PATH + File.separator + WORK_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/JavaApp");
        wdso.setLocalFolder(TMP_PATH + File.separator + WORK_PATH);
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project
        oto.waitText("Checking out... finished.");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        
        oto = new OutputTabOperator("SVN Output");
        oto.clear();
        Node projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
        CopyToOperator cto = CopyToOperator.invoke(projNode);
        cto.setRepositoryFolder("branches/release01/JavaApp");
        cto.setCopyPurpose("New branch for project.");
        cto.checkSwitchToCopy(false);
        cto.copy();
        oto.waitText("Copy");
        oto.waitText("finished.");
        
        Node nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp" + "|Main.java");
        org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);
        
        nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);
        //to do 
        
        //switch to branch
        oto = new OutputTabOperator("SVN Output");
        oto.clear();
        projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
        SwitchOperator so = SwitchOperator.invoke(projNode);
        so.setRepositoryFolder("branches/release01/JavaApp");
        so.switchBt();
        oto.waitText("Switch");
        oto.waitText("finished.");
        
        nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp" + "|Main.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node!!!", "[release01]", status);
        
        nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node!!!", "[release01]", status);
        
        oto = new OutputTabOperator("SVN Output");
        oto.clear();
        projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
        so = SwitchOperator.invoke(projNode);
        so.setRepositoryFolder("trunk/JavaApp");
        so.switchBt();
        oto.waitText("Switch");
        oto.waitText("finished.");
        Thread.sleep(2000);
        
        nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp" + "|Main.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);
        
        nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);
        
        TestKit.removeAllData("JavaApp");
        stream.flush();
        stream.close();
    }
    
    
}
