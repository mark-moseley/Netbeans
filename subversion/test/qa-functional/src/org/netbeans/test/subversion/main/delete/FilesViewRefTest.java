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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.subversion.main.delete;

import java.io.File;
import java.io.PrintStream;
import junit.framework.Test;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CommitOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author novakm
 */
public class FilesViewRefTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;

    /** Creates a new instance of FilesViewRefTest */
    public FilesViewRefTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        System.out.println("### " + getName() + " ###");
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
                 NbModuleSuite.createConfiguration(FilesViewRefTest.class).addTest(
                    "testFilesViewRefactoring"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }

    public void testFilesViewRefactoring() throws Exception {
        try {
            OutputOperator.invoke();
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            VersioningOperator vo = VersioningOperator.invoke();
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            CheckoutWizardOperator.invoke();
            Operator.setDefaultStringComparator(oldOperator);
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
            //open project
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            TestKit.waitForScanFinishedAndQueueEmpty();

            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            TestKit.createNewPackage(PROJECT_NAME, "a.b.c");
            TestKit.createNewElement(PROJECT_NAME, "a", "AClass");
            TestKit.createNewElement(PROJECT_NAME, "a.b", "BClass");
            TestKit.createNewElement(PROJECT_NAME, "a.b.c", "CClass");
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "");
            node = new Node(new FilesTabOperator().tree(), PROJECT_NAME);
            node.performPopupActionNoBlock("Subversion|Show Changes");
            CommitOperator cmo = CommitOperator.invoke(node);
            cmo.commit();
            node = new Node(new FilesTabOperator().tree(), PROJECT_NAME + "|src|a");
            node.performPopupActionNoBlock("Cut");
            node = new Node(new FilesTabOperator().tree(), PROJECT_NAME + "|src|javaapp");
            node.performPopupAction("Paste|Refactor Move");

            nbdialog = new NbDialogOperator("Move Classes");
            JButtonOperator refBut = new JButtonOperator(nbdialog, "Refactor");
            refBut.push();
            nbdialog.waitClosed();
            Thread.sleep(1000);
            node = new Node(new FilesTabOperator().tree(), PROJECT_NAME);
            node.performPopupActionNoBlock("Subversion|Show Changes");
            vo = VersioningOperator.invoke();
            Thread.sleep(2000);
            String[] expected = new String[]{"AClass.java", "BClass.java", "CClass.java", "a", "AClass.java", "b", "BClass.java", "c", "CClass.java"};
            String[] actual = new String[vo.tabFiles().getRowCount()];
            for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
                actual[i] = vo.tabFiles().getValueAt(i, 0).toString().trim();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong files in Versioning View", expected.length, result);
            expected = new String[]{"Locally Deleted", "Locally Deleted", "Locally Deleted", "Locally New", "Locally Copied", "Locally New", "Locally Copied", "Locally New", "Locally Copied"};
            actual = new String[vo.tabFiles().getRowCount()];
            for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
                actual[i] = vo.tabFiles().getValueAt(i, 1).toString().trim();
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong status in Versioning View", expected.length, result);
            Exception e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "a|b|BClass.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Unexpected behavior - File shouldn't be in explorer!!!", e);
            e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp.a.b|BClass.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Unexpected behavior - File should be in explorer!!!", e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}