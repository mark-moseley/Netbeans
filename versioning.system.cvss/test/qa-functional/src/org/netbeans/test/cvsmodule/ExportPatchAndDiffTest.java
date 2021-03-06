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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * ExportPatchAndDiffTest.java
 *
 * Created on 15. prosinec 2006, 10:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.cvsmodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.CommitOperator;
import org.netbeans.jellytools.modules.javacvs.DiffOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author novakm
 */
public class ExportPatchAndDiffTest extends JellyTestCase {
    
    String os_name;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    static File cacheFolder;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of ExportPatchAndDiffTest */
    public ExportPatchAndDiffTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### " + getName() + " ###");
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ExportPatchAndDiffTest("testCheckOutProject"));
        suite.addTest(new ExportPatchAndDiffTest("testExportDiffPatch"));
        suite.addTest(new ExportPatchAndDiffTest("testDiffFile"));
        suite.addTest(new ExportPatchAndDiffTest("removeAllData"));
        return suite;
    }
    
    public void testCheckOutProject() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 18000);
        TestKit.closeProject(projectName);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        String CVSroot = cvss.getCvsRoot();
        sessionCVSroot = CVSroot;
        crso.setCVSRoot(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        crso.next();
        
        //2nd step of CheckOutWizard
        File tmp = new File("/tmp"); // NOI18N
        File work = new File(tmp, "" + File.separator + System.currentTimeMillis());
        cacheFolder = new File(work, projectName + File.separator + "src" + File.separator + "forimport" + File.separator + "CVS" + File.separator + "RevisionCache");
        tmp.mkdirs();
        work.mkdirs();
        tmp.deleteOnExit();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        moduleCheck.setModule("ForImport");
        moduleCheck.setLocalFolder(work.getAbsolutePath()); // NOI18N
        //Pseudo CVS server for finishing check out wizard
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "checkout_finish_2.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        cwo.finish();
        OutputOperator oo = OutputOperator.invoke();
        OutputTabOperator oto = oo.getOutputTab(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Checking out finished");
        cvss.stop();
        in.close();
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        ProjectSupport.waitScanFinished();
        TestKit.waitForQueueEmpty();
        ProjectSupport.waitScanFinished();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testExportDiffPatch() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 18000);
        PseudoCvsServer cvss;
        InputStream in;
        OutputTabOperator oto;
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        new ProjectsTabOperator().tree().clearSelection();
        Node nodeClass = new Node(new SourcePackagesNode(projectName), pathToMain);
    
        nodeClass.performPopupAction("Open");
        EditorOperator eo = new EditorOperator("Main.java");
        eo.insert("// EXPORT PATCH", 5, 1);
        eo.save();
        
        nodeClass = new Node(new SourcePackagesNode(projectName), pathToMain);
        //nodeClass.select();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        nodeClass.performMenuActionNoBlock("Versioning|Export Diff Patch...");
        Operator.setDefaultStringComparator(oldOperator);
        
        //Operator.setDefaultStringComparator(oldOperator);
        NbDialogOperator dialog = new NbDialogOperator("Export");

        JTextFieldOperator tf = new JTextFieldOperator(dialog, 0);
        String patchFile = "/tmp/patch" + System.currentTimeMillis() + ".patch";
        File file = new File(patchFile);
        tf.setText(file.getCanonicalFile().toString());
        JButtonOperator btnExport = new JButtonOperator(dialog, "export");
        oto = new OutputTabOperator(sessionCVSroot);
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/export_diff.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        btnExport.push();
        //        no output in output tab now
        //        oto.waitText("Diff Patch finished");
        Thread.sleep(3000);
        cvss.stop();
        Thread.sleep(2000);
        //test file existence
        assertTrue("Diff Patch file wasn't created!", file.isFile());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        boolean generated = false;
        if (line != null) {
            generated = line.indexOf("# This patch file was generated by NetBeans IDE") != -1 ? true : false;
        }
        
        br.close();
        assertTrue("Diff Patch file is empty!", generated);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");

    }
    public void testDiffFile() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        PseudoCvsServer cvss;
        InputStream in;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color;
        
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        
        Node node = new Node(new SourcePackagesNode(projectName), pathToMain);
        node.performPopupAction("Open");
        EditorOperator eo = new EditorOperator("Main.java");
        eo.deleteLine(2);
        eo.insert(" insert", 5, 1);
        eo.insert("\tSystem.out.println(\"\");\n", 19, 1);
        eo.save();
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff_class.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        node.performPopupAction("CVS|Diff");
        
        //        oto.waitText("Diffing \"Main.java\" finished");
        Thread.sleep(1000);
        cvss.stop();
        
        Node nodeClass = new Node(new SourcePackagesNode(projectName), pathToMain);
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for modified file", TestKit.MODIFIED_COLOR, color);
        
        //verify next button
        DiffOperator diffOp = new DiffOperator("Main.java");
        //
        try {
            TimeoutExpiredException afee = null;
            //find the last difference            
            try {
                diffOp.next();
                diffOp.next();
                diffOp.next();
            } catch (TimeoutExpiredException e) {
                //want to be sure that the last difference was found
                afee = e;
            }
            assertNotNull("TimeoutExpiredException was expected - diff should have pointed to the last difference.", afee);
            
            //verify previous button
            afee = null;
            diffOp.previous();
            diffOp.previous();
            try {
                diffOp.previous();
            } catch (TimeoutExpiredException e) {
                afee = e;
            }
            assertNotNull("TimeoutExpiredException was expected - previous diff should have been disabled.", afee);
            
            //verify next button
            afee = null;
            diffOp.next();
            diffOp.next();
            try {
                diffOp.next();
            } catch (TimeoutExpiredException e) {
                afee = e;
            }
            assertNotNull("TimeoutExpiredException was expected - next diff should have been disabled.", afee);
            
        } catch (Exception e) {
            System.out.println("Problem with buttons of differences");
            e.printStackTrace();
        }
        
        //refresh button
        oto = new OutputTabOperator(sessionCVSroot);
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/refresh.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        diffOp.refresh();
        oto.waitText("Refreshing CVS Status finished");
        cvss.stop();
        Thread.sleep(1000);
        
        //update button
        oto = new OutputTabOperator(sessionCVSroot);
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/refresh.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        diffOp.update();
        oto.waitText("Updating Sources finished");
        cvss.stop();
        Thread.sleep(1000);
        
        //commit button
        CommitOperator co = diffOp.commit();
        JTableOperator table = co.tabFiles();
        assertEquals("There should be only one file!", 1, table.getRowCount());
        assertEquals("There should be Main.java file only!", "Main.java", table.getValueAt(0, 0));
        co.cancel();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        //        eo.closeWindow();
        eo.closeAllDocuments();
        TestKit.deleteRecursively(cacheFolder);
    }
    
    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}
