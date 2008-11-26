/*
 * SearchRevisionsTest.java
 *
 * Created on July 3, 2006, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.archeology;

import java.io.File;
import java.io.PrintStream;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SearchRevisionsOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class SearchRevisionsTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator; 
    
    /** Creates a new instance of SearchRevisionsTest */
    public SearchRevisionsTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        System.out.println("### "+getName()+" ###");
        
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(SearchRevisionsTest.class).addTest(
                    "testSearchRevisionsTest"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testSearchRevisionsTest() throws Exception {
        try {
            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            CheckoutWizardOperator.invoke();
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();        

            //create repository... 
            File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
            new File(TMP_PATH).mkdirs();
            Thread.sleep(1000);
            work.mkdirs();
            Thread.sleep(1000);
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            Thread.sleep(500);
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
            Thread.sleep(500);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            Thread.sleep(500);
            
            rso.next();

            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);

            SearchRevisionsOperator sro = wdso.search();
            sro.setFrom("");
            sro.list();
            sro.verify();
            sro.selectListItem(0);
            sro.ok();
            assertEquals("Requested repository revision is not propagated!!!", "5", wdso.getRevisionNumber());
            wdso.cancel();

            stream.flush();
            stream.close();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            try {
                TestKit.closeProject(PROJECT_NAME);
            } catch (TimeoutExpiredException e) {/* OK */}
        }   
    }
}
