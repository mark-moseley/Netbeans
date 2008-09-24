/*
 * DiffTest.java
 *
 * Created on July 3, 2006, 3:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.test.subversion.main.diff;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;
import org.netbeans.test.subversion.operators.DiffOperator;
import org.netbeans.test.subversion.utils.MessageHandler;

/**
 *
 * @author pvcs
 */
public class DiffTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    static Logger log;

    /** Creates a new instance of DiffTest */
    public DiffTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        if (log == null) {
            log = Logger.getLogger(TestKit.LOGGER_NAME);
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
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
                NbModuleSuite.createConfiguration(DiffTest.class).addTest(
                "testDiffFile").enableModules(".*").clusters(".*"));
    }

    public void testDiffFile() throws Exception {
        try {
            MessageHandler mh = new MessageHandler("Checking out");
            log.addHandler(mh);

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            VersioningOperator vo = VersioningOperator.invoke();
            TestKit.showStatusLabels();
            CheckoutWizardOperator.invoke();
            RepositoryStepOperator rso = new RepositoryStepOperator();

            //create repository...
            File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
            new File(TMP_PATH).mkdirs();
            work.mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

            rso.next();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);
            wdso.finish();

            TestKit.waitText(mh);

            //open project
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();

            TestKit.waitForScanFinishedAndQueueEmpty();

            //modify, save file and invoke Diff
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Open");
            EditorOperator eo = new EditorOperator("Main.java");
            eo.deleteLine(2);
            eo.insert(" insert", 5, 1);
            eo.insert("\tSystem.out.println(\"\");\n", 19, 1);
            eo.save();

            mh = new MessageHandler("Refreshing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node.performPopupAction("Subversion|Show Changes");
            TestKit.waitText(mh);

            Thread.sleep(1000);
            vo = VersioningOperator.invoke();
            //Save action should change the file annotations
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.MODIFIED_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.MODIFIED_STATUS, status);
            assertEquals("Wrong number of records in Versioning view!!!", 1, vo.tabFiles().getRowCount());

            mh = new MessageHandler("Diffing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);
            node.performPopupAction("Subversion|Diff");

//            TestKit.waitText(mh);

            System.out.println("DIFF action doesn't print information into OUTPUT!!!");
            Thread.sleep(2000);

            DiffOperator diffOp = new DiffOperator("Main.java");
            //
            try {
                TimeoutExpiredException afee = null;
                diffOp.next();
                diffOp.next();
                try {
                    diffOp.next();
                } catch (TimeoutExpiredException e) {
                    afee = e;
                }
                assertNotNull("TimeoutExpiredException was expected.", afee);

                //verify previous button
                afee = null;
                diffOp.previous();
                diffOp.previous();
                try {
                    diffOp.previous();
                } catch (TimeoutExpiredException e) {
                    afee = e;
                }
                assertNotNull("TimeoutExpiredException was expected.", afee);
            } catch (Exception e) {
                System.out.println("Problem with buttons of differences");
            }
            stream.flush();
            stream.close();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
