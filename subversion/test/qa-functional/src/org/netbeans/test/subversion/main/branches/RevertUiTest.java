/*
 * RevertUiTest.java
 *
 * Created on 18 May 2006, 17:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.branches;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.RevertModificationsOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class RevertUiTest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    
    String os_name;
    
    /** Creates a new instance of RevertUiTest */
    public RevertUiTest(String name) {
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
        suite.addTest(new RevertUiTest("testInvokeCloseRevert"));
        return suite;
    }
    
    public void testInvokeCloseRevert() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        try {
            TestKit.closeProject(PROJECT_NAME);

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            ImportWizardOperator iwo = ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME));
            RepositoryStepOperator rso = new RepositoryStepOperator();
            //rso.verify();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            rso.next();
            Thread.sleep(1000);

            FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
            ftiso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            ftiso.setImportMessage("initial import");
            ftiso.next();
            Thread.sleep(1000);
            CommitStepOperator cso = new CommitStepOperator();
            cso.finish();

            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            //Node projNode = new Node(new ProjectsTabOperator().tree(), "AnagramGame");
            RevertModificationsOperator rmo = RevertModificationsOperator.invoke(projNode);
            rmo.verify();
            TimeoutExpiredException tee = null;
            try {
                rmo.setStartRevision("1");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);
            tee = null;
            try {
                rmo.setEndRevision("1");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);
            tee = null;
            try {
                rmo.setEndRevision("1");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);

            rmo.rbPreviousCommits().push();
            rmo.setStartRevision("1");
            rmo.setEndRevision("2");

            tee = null;
            try {
                rmo.setRevision("3");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);

            rmo.rbSingleCommit().push();
            rmo.setRevision("3");

            tee = null;
            try {
                rmo.setStartRevision("1");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);

            tee = null;
            try {
                rmo.setEndRevision("2");
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("Components shouldn't be accessed", tee);

            rmo.cancel(); 
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        } 
        
    }
}
