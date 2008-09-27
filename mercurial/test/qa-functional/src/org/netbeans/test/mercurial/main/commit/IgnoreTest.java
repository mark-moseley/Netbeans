/*
 * IgnoreTest.java
 *
 * Created on June 8, 2006, 9:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.main.commit;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.MessageHandler;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author peter pis
 */
public class IgnoreTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaAppIgnUnign";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    static Logger log;
    
    /** Creates a new instance of IgnoreTest */
    public IgnoreTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        System.out.println("### "+getName()+" ###");
        if (log == null) {
            log = Logger.getLogger(TestKit.LOGGER_NAME);
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
    }
    
   
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(IgnoreTest.class).addTest("testIgnoreUnignoreFile" /*, "testFinalRemove" */).enableModules(".*").clusters(".*"));
    }
        
    public void testIgnoreUnignoreFile() throws Exception {
        try {
            MessageHandler mh = new MessageHandler("Ignoring");
            log.addHandler(mh);
            
            TestKit.showStatusLabels();
            TestKit.prepareProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, PROJECT_NAME);
            ProjectSupport.waitScanFinished();
            Thread.sleep(1000);
            new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME).performPopupActionNoBlock("Versioning|Initialize Mercurial Project");
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));

            TestKit.createNewElement(PROJECT_NAME, "javaappignunign", "NewClassIgnUnign");

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClassIgnUnign");
            node.select();
            node.performPopupAction("Mercurial|Toggle Ignore");
            String outputTabName=TestKit.getProjectAbsolutePath(PROJECT_NAME);

            TestKit.waitText(mh);

            Thread.sleep(1000);
            
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            Thread.sleep(1000);
            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be ignored!!!", TestKit.IGNORED_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be ignored!!!", TestKit.IGNORED_STATUS, status);
            
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();

            //unignore file
            mh = new MessageHandler("Ignoring");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            node.performPopupAction("Mercurial|Toggle Ignore");
            TestKit.waitText(mh);
            Thread.sleep(1000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            Thread.sleep(1000);
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.NEW_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.NEW_STATUS, status);
            
            //verify content of Versioning view
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");

            mh = new MessageHandler("Refreshing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node.select();
            node.performPopupAction("Mercurial|Status");
            TestKit.waitText(mh);
            Thread.sleep(1000);
            VersioningOperator vo = VersioningOperator.invoke();
            TableModel model = vo.tabFiles().getModel();
            assertEquals("Versioning view should be empty", 1, model.getRowCount());
            assertEquals("File should be listed in Versioning view", "NewClassIgnUnign.java", model.getValueAt(0, 0).toString());
            
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
    
    public void testFinalRemove() throws Exception {
        TestKit.finalRemove();
    }
}
