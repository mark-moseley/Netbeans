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
package org.netbeans.qa.form.actions;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
//import org.netbeans.jemmy.Test;
import org.netbeans.jellytools.properties.DimensionProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;

/**
 * This test should cover all actions of the Form module
 *
 * List of tested actions is included in the javadoc of each test case
 * 
 * @author Pavel Pribyl
 * @version 0.9 (not finished)
 */
public class actionsTest extends JellyTestCase {

    public String DATA_PROJECT_NAME = "SampleDesktopApplication";
    public String PACKAGE_NAME = "data";
    public String PROJECT_NAME = "Java";
    private String FILE_NAME = "clear_JFrame";
    public String FRAME_ROOT = "[JFrame]";
    public String workdirpath;
    public Node formnode;
    private ProjectsTabOperator pto;
    private ComponentInspectorOperator inspector;
    private PropertySheetOperator properties;
    ProjectRootNode prn;

    /** Constructor required by JUnit */
    public actionsTest(String name) {
        super(name);
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(actionsTest.class).addTest(
                "testDummy",
                "testDuplicate",
                "testEditContainer",
                "testResizing",
                "testBeans").gui(true).enableModules(".*").clusters(".*"));
    }

    /** Called before every test case. */
    @Override
    public void setUp() throws IOException {
        openDataProjects(DATA_PROJECT_NAME);
        workdirpath = getWorkDir().getParentFile().getAbsolutePath();
        System.out.println("########  " + getName() + "  #######");

        pto = new ProjectsTabOperator();
        prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();


        formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + FILE_NAME);
    //formnode.select();


    }

    /** Called after every test case. */
    @Override
    public void tearDown() {
    }

    /**
     * Just a helper test to avoid failing wit "Menu pushing ..." in following tests
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    public void testDummy() throws InterruptedException, IOException {
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
    }
    ;

    /** Test case 1.
     * This test case verifies following Form Editor actions:<br />
     * <ul>
     * <li>org.netbeans.modules.form.actions.DuplicateAction</li>
     * <li>org.netbeans.modules.form.actions.SelectLayoutAction</li>
     * <li>org.netbeans.modules.form.actions.InspectorActions</li>
     * </ul>
     * THIS TEST CASE MUST BE RUN<br />
     * It place several components into the form, they are used in following tests too
     *
     */
    public void testDuplicate() throws InterruptedException, IOException {
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);

        Thread.sleep(1000);

        inspector = new ComponentInspectorOperator();

        Node inspectorRootNode = new Node(inspector.treeComponents(), FRAME_ROOT);
        inspectorRootNode.select();
        inspectorRootNode.expand();

        Node gridBagNode = new Node(inspectorRootNode, "GridBagLayout");
        gridBagNode.select();
        new ActionNoBlock(null, "Customize").perform(gridBagNode);
        NbDialogOperator nbo = new NbDialogOperator("GridBagLayout Customizer");
        nbo.btClose().push();

        new Action(null, "Add From Palette|Swing Containers|Panel").perform(inspectorRootNode);

        Node panelNode = new Node(inspectorRootNode, "jPanel1 [JPanel]");
        panelNode.select();

        Thread.sleep(1000);

        new Action(null, "Add From Palette|Swing Controls|Button").performPopup(panelNode);
        Action freedesignAction = new Action(null, "Set Layout|Free Design");

        freedesignAction.performPopup(panelNode);

        Node buttonNode = new Node(panelNode, "jButton1 [JButton]");
        buttonNode.select();
        new Action(null, "Duplicate").performPopup(buttonNode);

        inspectorRootNode.select();
        new Action(null, "Customiz");

        freedesignAction.performPopup(inspectorRootNode);

        panelNode.select();
        new Action(null, "Duplicate").performPopup(panelNode);

        Thread.sleep(1000);

        ArrayList lines = new ArrayList<String>();

        lines.add("jButton1");
        lines.add("jButton2");
        lines.add("jButton3");
        lines.add("jButton4");
        lines.add("jPanel1");
        lines.add("jPanel2");

        findInCode(lines, new FormDesignerOperator(FILE_NAME));
    }

    /** Test case 2.
     * This test case verifies following Form Editor actions:<br />
     * <ul>
     * <li>org.netbeans.modules.form.actions.EditContainerAction</li>
     * <li>org.netbeans.modules.form.actions.DesignParentAction</li>
     * <li>org.netbeans.modules.form.actions.PropertyAction (?)</li>
     * <li>org.netbeans.modules.form.actions.EditFormAction</li>
     * </ul>
     */
    public void testEditContainer() throws InterruptedException {
        formnode.select();
        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);

        inspector = new ComponentInspectorOperator();

        Node inspectorRootNode = new Node(inspector.treeComponents(), FRAME_ROOT);
        Node panelNode = new Node(inspectorRootNode, "jPanel1 [JPanel]");
        panelNode.select();

        new Action(null, "Design This Container").performPopup(panelNode);

        Thread.sleep(2000);

        Node buttonNode = new Node(panelNode, "jButton1 [JButton]");
        buttonNode.select();
        new Action(null, "Design Parent|[Top Parent]").performPopup(buttonNode);

        Thread.sleep(2000);
        buttonNode.select();
        new Action(null, "Enclose In|Scroll Pane").performPopup(buttonNode);

        Thread.sleep(1000);

        buttonNode = new Node(panelNode, "jButton2 [JButton]");
        buttonNode.setComparator(new Operator.DefaultStringComparator(true, false));
        buttonNode.select();

//        This part is still failing on my PC, need to verify on another
        new ActionNoBlock(null, "Space Around Component...").perform(buttonNode);
        //Thread.sleep(2000);

        NbDialogOperator jdo = new NbDialogOperator("Space Around Component");

        JComboBoxOperator jcbSize = new JComboBoxOperator(jdo, 0);
        jcbSize.enterText("100");
        jdo.btOK().push();

        Thread.sleep(1000);

    //     buttonNode.select();
    //     inspector.pressKey(KeyEvent.VK_ENTER);  //enters edit mode
    }

    /** This test case verifies following Form Editor actions:<br />
     * <ul>
     * <li>org.netbeans.modules.form.actions.ChooseSameSizeAction</li>
     * <ul>
     */
    public void testResizing() throws InterruptedException {
        createForm("JFrame Form", "MyJFrame");

        inspector = new ComponentInspectorOperator();

        Node inspectorRootNode = new Node(inspector.treeComponents(), FRAME_ROOT);
        inspectorRootNode.select();
        //inspectorRootNode.expand();
        Thread.sleep(1000);

        new Action(null, "Add From Palette|Swing Containers|Panel").performPopup(new Node(inspector.treeComponents(), "[JFrame]"));

        Node panelNode = new Node(inspectorRootNode, "jPanel1 [JPanel]");
        panelNode.select();

        new Action(null, "Add From Palette|Swing Controls|Button").performPopup(panelNode);
        Thread.sleep(1000);
        new Action(null, "Add From Palette|Swing Controls|Button").performPopup(panelNode);

        Node btn1Node = new Node(panelNode, "jButton1 [JButton]");
        btn1Node.select();

        properties = new PropertySheetOperator();

        new DimensionProperty(properties, "preferredSize").setDimensionValue("100", "50");

        Node btn2Node = new Node(panelNode, "jButton2 [JButton]");
        btn1Node.select();
        btn2Node.addSelectionPath();

        Node[] nodes = {btn1Node, btn2Node};
        new Action(null, "Same Size|Same Width").performPopup(nodes);

        Thread.sleep(2000);

        //verify, that source code contains the "grouping" of two JButtons
        String line = "new java.awt.Component[] {jButton1, jButton2}";
        FormDesignerOperator opDesigner = new FormDesignerOperator("MyJFrame");

        findInCode(line, opDesigner);

    }

    /** Test case 5
     * org.netbeans.modules.form.actions.InstallBeanAction 
     * org.netbeans.modules.form.actions.InstallToPaletteAction 
     */
    public void testBeans() throws InterruptedException {
        String beanName = "MyBean";

        createForm("Bean Form", beanName);
        inspector = new ComponentInspectorOperator();

        new ActionNoBlock("Tools|Palette|Swing/AWT Components", null).performMenu();
        NbDialogOperator nbo = new NbDialogOperator("Palette Manager");
        nbo.btClose().push();
        Thread.sleep(3000);

        Node beanNode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + beanName);
        beanNode.select();
        new ActionNoBlock(null, "Tools|Add to Palette...").perform(beanNode);

        Thread.sleep(2000);
        NbDialogOperator jdo = new NbDialogOperator("Select Palette Category");
        JListOperator jlo = new JListOperator(jdo);
        jlo.selectItem("Beans");
        jdo.btOK().push();
        Thread.sleep(3000);



    }

    private void createForm(String formType, String name) throws InterruptedException {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(DATA_PROJECT_NAME);
        Thread.sleep(3000);
        nfwo.selectCategory("Swing GUI Forms");
        nfwo.selectFileType(formType);
        nfwo.next();
        JTextFieldOperator form_name = new JTextFieldOperator(nfwo, 0);
        form_name.setText(name);
        JComboBoxOperator jcb_package = new JComboBoxOperator(nfwo, 1);
        jcb_package.selectItem("data");
        Thread.sleep(3000);

        if (formType.equals("Bean Form")) {
            nfwo.next();
            JTextFieldOperator class_name = new JTextFieldOperator(nfwo);
            class_name.setText("javax.swing.JButton");
            nfwo.finish();
            log(formType + " is created correctly");
        } else {
            nfwo.finish();
            log(formType + " is created correctly");
            Thread.sleep(3000);
        }

    }

    /*
     * select tab in PropertySheet
     */
    private void selectPropertiesTab(PropertySheetOperator pso) {
        selectTab(pso, 0);
    }

    private void selectBindTab(PropertySheetOperator pso) {
        selectTab(pso, 1);
    }

    private void selectEventsTab(PropertySheetOperator pso) {
        selectTab(pso, 2);
    }

    private void selectCodeTab(PropertySheetOperator pso) {
        selectTab(pso, 3);
    }

    //select tab in PropertySheet
    private void selectTab(PropertySheetOperator pso, int index) {
        JToggleButtonOperator tbo = null;
        if (tbo == null) {
            tbo = new JToggleButtonOperator(pso, " ", index);
        }
        tbo.push();
    }

    /**
     * Find a string in a code
     * @param lines array list of strings to find
     * @param designer operator "with text"
     */
    private void findInCode(String stringToFind, FormDesignerOperator designer) {
        EditorOperator oPeditor = designer.editor();
        findStringInCode(stringToFind, oPeditor.getText());
        designer.design();
    }

    /**
     * Find a substring in a string
     * Test fail() method is called, when code string doesnt contain stringToFind.
     * @param stringToFind string to find
     * @param string to search
     */
    private void findStringInCode(String stringToFind, String code) {
        if (!code.contains(stringToFind)) {
            fail("Missing string \"" + stringToFind + "\" in code."); // NOI18N
        }
    }

    /**
     * Find a strings in a code
     * @param lines array list of strings to find
     * @param designer operator "with text"
     */
    public void findInCode(ArrayList<String> lines, FormDesignerOperator designer) {
        EditorOperator editor = designer.editor();
        String code = editor.getText();

        for (String line : lines) {
            findStringInCode(line, code);
        }
        designer.design();
    }
}
