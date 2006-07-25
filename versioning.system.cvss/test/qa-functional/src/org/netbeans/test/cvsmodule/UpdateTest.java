/*
 * UpdateTest.java
 *
 * Created on July 12, 2006, 12:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.cvsmodule;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.CommitOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.ProxyConfigurationOperator;
import org.netbeans.jellytools.modules.javacvs.VersioningOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author pvcs
 */
public class UpdateTest extends JellyTestCase {
    
    static final String PROJECT1 = "Project1";
    static final String PROJECT2 = "Project2";
    static final String cvsRoot1 = ":pserver:test@qa-linux-s6:/usr/local/CVSrepo";
    static final String cvsRoot2 = ":pserver:pvcs@peterp.czech.sun.com:/usr/cvsrepo";
    static final String[] nodes1 = new String[] {"aa|NewClass1.java", "aa|NewClass2.java", "aa|NewClass3.java", "aa|NewClass4.java", "aa|NewClass5.java",
            "bb|NewClass1.java", "bb|NewClass2.java", "bb|NewClass3.java", "bb|NewClass4.java", "bb|NewClass5.java",
            "cc|NewClass1.java", "cc|NewClass2.java", "cc|NewClass3.java", "cc|NewClass4.java", "cc|NewClass5.java"};
            
    String os_name;
    static String sessionCVSroot;
    boolean unix = false;
    final String projectName = "CVS Client Library";
    
    /** Creates a new instance of UpdateTest */
    public UpdateTest(String name) {
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
        //suite.addTest(new UpdateTest("testOpen"));
        suite.addTest(new UpdateTest("testBrokenCommit"));
        return suite;
    }
    
    public void testOpen() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        File loc = new File("/tmp/work/w1153322002833");
        //closeProject(PROJECT1);
        //closeProject(PROJECT2);
        openProject(loc, PROJECT1);
    }
    
    public void testUpdate() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        String cvsRoot = ":pserver:anoncvs@cvsnetbeansorg.sfbay.sun.com:/cvs";
        Node node;
        org.openide.nodes.Node nodeIDE;
        String color;
                
        String[] nodes = new String[] {
            "org.netbeans.lib.cvsclient|Bundle.properties",
            "org.netbeans.lib.cvsclient|CVSRoot.java",
            "org.netbeans.lib.cvsclient|Client.java",
            "org.netbeans.lib.cvsclient|ClientServices.java",
            "org.netbeans.lib.cvsclient.admin|AdminHandler.java",
            "org.netbeans.lib.cvsclient.admin|DateComparator.java",
            "org.netbeans.lib.cvsclient.admin|Entry.java",
            "org.netbeans.lib.cvsclient.admin|StandardAdminHandler.java",
            "org.netbeans.lib.cvsclient.command|BasicCommand.java",
            "org.netbeans.lib.cvsclient.command|BinaryBuilder.java",
            "org.netbeans.lib.cvsclient.command|BuildableCommand.java",
            "org.netbeans.lib.cvsclient.command|Builder.java",
            "org.netbeans.lib.cvsclient.command|Bundle.properties",
            "org.netbeans.lib.cvsclient.command|Command.java",
            "org.netbeans.lib.cvsclient.command|CommandAbortedException.java",
            "org.netbeans.lib.cvsclient.command|CommandException.java",
            "org.netbeans.lib.cvsclient.command|CommandUtils.java",
            "org.netbeans.lib.cvsclient.command|DefaultFileInfoContainer.java",
            "org.netbeans.lib.cvsclient.command|FileInfoContainer.java",
            "org.netbeans.lib.cvsclient.command|GlobalOptions.java",
            "org.netbeans.lib.cvsclient.command|KeywordSubstitutionOptions.java",
            "org.netbeans.lib.cvsclient.command|PipedFileInformation.java",
            "org.netbeans.lib.cvsclient.command|PipedFilesBuilder.java",
            "org.netbeans.lib.cvsclient.command|RepositoryCommand.java",
            "org.netbeans.lib.cvsclient.command|TemporaryFileCreator.java",
            "org.netbeans.lib.cvsclient.command|Watch.java",
            "org.netbeans.lib.cvsclient.command|WrapperUtils.java"
        };
        VersioningOperator vo = VersioningOperator.invoke();
        OutputOperator oo = OutputOperator.invoke();
        OutputTabOperator oto = new OutputTabOperator(cvsRoot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
        node = new Node(new ProjectsTabOperator().tree(), projectName);
        node.performPopupAction("CVS|Show Changes");
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        
        assertEquals("Wrong files counts in Versioning view", nodes.length, vo.tabFiles().getRowCount());
        String[] actual = new String[vo.tabFiles().getRowCount()];
        String[] expected = new String[nodes.length];
        for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
            actual[i] = vo.tabFiles().getModel().getValueAt(i, 0).toString();
        }
        for (int i = 0; i < nodes.length; i++) {
            expected[i] = getObjectName(nodes[i]);
        }
        
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Some files disappear!!!", expected.length, result);
        
        for (int j = 0; j < 100; j ++) {
            oto = new OutputTabOperator(cvsRoot);
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            node = new Node(new ProjectsTabOperator().tree(), projectName);
            node.performPopupAction("CVS|Update");
            oto.waitText("Updating \"CVS Client Library\" finished");
            Thread.sleep(1000);
            for (int i = 0; i < nodes.length; i++) {
                node = new Node(new SourcePackagesNode(projectName), nodes[i]);
                nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
                color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
                assertEquals("Wrong color of <" + nodes[i] + ">", TestKit.MODIFIED_COLOR, color);
            }
            vo = VersioningOperator.invoke();
            actual = new String[vo.tabFiles().getRowCount()];
            for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
                actual[i] = vo.tabFiles().getModel().getValueAt(i, 0).toString();
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Some files disappear!!!", expected.length, result);
        }    
    }
    
    String getObjectName(String value) {
        int pos = value.lastIndexOf('|');
        return value.substring(pos + 1);
    }
    
    public void testBrokenCommit() throws Exception {
        int j = 0;
        long iter;
        File location1;
        File location2;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        closeProject(PROJECT2);
        //TestKit.deleteRecursively(work);
        
        Node node1;
        Node node2;
        org.openide.nodes.Node nodeIDE1;
        org.openide.nodes.Node nodeIDE2;
        
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        location2 = checkOutProject(cvsRoot2, "pvcspvcs", PROJECT2);
        
        for (int i = 0; i < 100; i++) {
            iter = System.currentTimeMillis();
            editFilesForMerge(PROJECT1, iter);
            editFilesForMerge(PROJECT2, iter);
            
            closeProject(PROJECT1);
            closeProject(PROJECT2);
            
            checkOutProject(cvsRoot1, "test", PROJECT1);
            checkOutProject(cvsRoot2, "pvcspvcs", PROJECT2);
        
            editFiles(PROJECT1, iter);
            editFiles(PROJECT2, iter);
            
            node1 = new Node(new SourcePackagesNode(PROJECT1), "");
            node2 = new Node(new SourcePackagesNode(PROJECT2), "");
            CommitOperator co = CommitOperator.invoke(new Node[] {node1, node2});
            assertEquals("Wrong count of files to commit", 30, co.tabFiles().getRowCount());

            OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
            OutputTabOperator oto2 = new OutputTabOperator(cvsRoot2);
            oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto1.clear();
            oto2.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto2.clear();
            co.commit();
            oto1.waitText("Committing");
            oto1.waitText("finished");
            oto2.waitText("Committing");
            oto2.waitText("finished");
            //delete all
            closeProject(PROJECT1);
            closeProject(PROJECT2);
            //TestKit.deleteRecursively(work);
            
            openProject(location1, PROJECT1);
            openProject(location2, PROJECT2);
            
            updateProject(PROJECT1, cvsRoot1);
            updateProject(PROJECT2, cvsRoot2);
            
            //Commit
            node1 = new Node(new SourcePackagesNode(PROJECT1), "");
            node2 = new Node(new SourcePackagesNode(PROJECT2), "");
            co = CommitOperator.invoke(new Node[] {node1, node2});
            assertEquals("Wrong count of files to commit", 30, co.tabFiles().getRowCount());

            oto1 = new OutputTabOperator(cvsRoot1);
            oto2 = new OutputTabOperator(cvsRoot2);
            oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto1.clear();
            oto2.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto2.clear();
            co.commit();
            oto1.waitText("Committing");
            oto1.waitText("finished");
            oto2.waitText("Committing");
            oto2.waitText("finished");
            //delete all
            closeProject(PROJECT1);
            closeProject(PROJECT2);
            //TestKit.deleteRecursively(work);
            
            //check out again
            location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
            location2 = checkOutProject(cvsRoot2, "pvcspvcs", PROJECT2);
            
            //validate data
            validateCheckout(PROJECT1, iter, new int[] {1, 6});
            validateCheckout(PROJECT2, iter, new int[] {1, 6});
           
        }
    }
    
    public static void closeProject(String project_name) {
        try {
            Node rootNode = new ProjectsTabOperator().getProjectRootNode(project_name);
            rootNode.performPopupAction("Close Project");
        }    
        catch (Exception e) {
            
        }    
        //TestKit.deleteRecursively(file);
    }
    
    public void updateProject(String project, String cvsRoot) throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        OutputTabOperator oto = new OutputTabOperator(cvsRoot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        Node node = new Node(new ProjectsTabOperator().tree(), project);
        node.performPopupAction("CVS|Update");
        oto.waitText("Updating");
        oto.waitText("finished");        
    }
    
    public File checkOutProject(String cvsRoot, String passwd, String project) throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        File work = new File("/tmp/work/w" + System.currentTimeMillis());
        work.mkdir();
        OutputOperator oo = OutputOperator.invoke();
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        CVSRootStepOperator crso = new CVSRootStepOperator();
        //JComboBoxOperator combo = new JComboBoxOperator(crso, 0);
        crso.setCVSRoot(cvsRoot);
        //crso.setPassword("");
        ProxyConfigurationOperator pco = crso.proxyConfiguration();
        pco.noProxyDirectConnection();
        pco.ok();
        crso.setPassword(passwd);
        crso.next();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        moduleCheck.setModule(project);
        moduleCheck.setLocalFolder(work.getCanonicalPath());
        moduleCheck.finish();
        OutputTabOperator oto = new OutputTabOperator(cvsRoot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Checking out finished");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        return work;
    }
    
    public void editFiles(String project, long iter) {
        Node node = new Node(new ProjectsTabOperator().tree(), project);
        node.performPopupAction("CVS|Show Changes");
        for (int i = 0; i < nodes1.length; i++) {
            node = new Node(new SourcePackagesNode(project), nodes1[i]);
            node.performPopupAction("Open");
            EditorOperator eo = new EditorOperator(getObjectName(nodes1[i]));
            eo.insert("//" + nodes1[i] + " >iter< " + iter + "\n", 1, 1);
            eo.save();
        }
    }
    
    public void editFilesForMerge(String project, long iter) {
        Node node = new Node(new ProjectsTabOperator().tree(), project);
        node.performPopupAction("CVS|Show Changes");
        for (int i = 0; i < nodes1.length; i++) {
            node = new Node(new SourcePackagesNode(project), nodes1[i]);
            node.performPopupAction("Open");
            EditorOperator eo = new EditorOperator(getObjectName(nodes1[i]));
            eo.insert("//" + nodes1[i] + " >iter< " + iter + "\n", 5, 1);
            eo.save();
        }
    }
    
    public void validateCheckout(String project, long iter, int[] indexes) throws Exception {
        Node node;
        EditorOperator eo;
        for (int i = 0; i < nodes1.length; i++) {
            node = new Node(new SourcePackagesNode(project), nodes1[i]);
            node.performPopupAction("Open");
            eo = new EditorOperator(getObjectName(nodes1[i]));
            for (int j = 0; j < indexes.length; j++) {
                String line = eo.getText(indexes[j]);
                System.out.println("line: " + line);
                assertEquals("Data was not committed!!!", "//" + nodes1[i] + " >iter< " + iter + "\n", line);
                
            }
            if (i == nodes1.length - 1) 
                eo.closeDiscardAll();
        }
        
    }
    
    public void openProject(File location, String project) throws Exception {
        new ActionNoBlock("File|Open Project", null).perform();
        NbDialogOperator nb = new NbDialogOperator("Open Project");
        JFileChooserOperator fco = new JFileChooserOperator(nb);
        System.out.println(location.getCanonicalPath());
        fco.setCurrentDirectory(new File(location, project));
        fco.approve();
        ProjectSupport.waitScanFinished();
    }
}
