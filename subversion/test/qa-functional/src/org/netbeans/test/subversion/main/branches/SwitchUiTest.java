/*
 * SwitchUiTest.java
 *
 * Created on 17 May 2006, 22:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.branches;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SwitchOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class SwitchUiTest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    
    String os_name;
    
    /** Creates a new instance of SwitchUiTest */
    public SwitchUiTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(SwitchUiTest.class).addTest(
                    "testInvokeCloseSwitch"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testInvokeCloseSwitch() throws Exception {
        try {
            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);
            
            ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME));
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
            
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.waitText("Committed revision 7");
            
            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            SwitchOperator so = SwitchOperator.invoke(projNode);
            //only required nodes are expended - want to see all in browser
            so.setRepositoryFolder("");
            RepositoryBrowserOperator rbo = so.browseRepositoryFolder();
            so.verify();
            rbo.selectFolder("tags");
            rbo.selectFolder("trunk");
            rbo.selectFolder("branches");
            rbo.ok();
            assertEquals("Folder wasn't created", "branches", so.getRepositoryFolder());
            so.cancel();
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
}
