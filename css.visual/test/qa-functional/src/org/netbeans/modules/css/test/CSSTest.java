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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.css.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JList;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.junit.NbTestSuite;


/**
 *
 * @author Jindrich Sedek
 */
public class CSSTest extends JellyTestCase {
    protected static final String newFileName = "newFileName";
    protected static final int rootRuleLineNumber = 15;
    protected static final String projectName = "CSSTestProject";
    
    /** Creates new CSS Test */
    public CSSTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws Exception{
        super.setUp();
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        System.out.println("running" + this.getName());
    }
    
    protected void waitUpdate(){
        try{
            Thread.sleep(5000);
        }catch (InterruptedException exc){
            throw new JemmyException("INTERUPTION", exc);
        }
    }
    
    protected EditorOperator openFile(String fileName){
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode,"Web Pages|css|"+fileName);
        node.select();
        node.performPopupAction("Open");
        return new EditorOperator(fileName);
    }
    
    protected String getRootRuleText(){
        String content = new EditorOperator(newFileName).getText();
        String root = content.substring(content.indexOf("root"));
        String rule = root.substring(root.indexOf('{'), root.indexOf('}'));
        return rule;
    }
    
    protected List<String> getItems(JComboBoxOperator boxOperator){
        JComboBox box = (JComboBox) boxOperator.getSource();
        int boxSize = box.getItemCount();
        List<String> result = new ArrayList<String>(boxSize);
        for(int i = 0;i < boxSize; i++){
            result.add(box.getModel().getElementAt(i).toString());
        }
        return result;
    }
    
    protected List<String> getItems(JListOperator listOperator){
        JList jList = (JList) listOperator.getSource();
        int listOperatorSize = getSize(listOperator);
        List<String> result = new ArrayList<String>(listOperatorSize);
        for (int i=0; i <listOperatorSize ;i++){
            result.add(jList.getModel().getElementAt(i).toString());
        }
        return result;
    }
    
    protected int getSize(JListOperator listOperator){
        return ((JList)listOperator.getSource()).getModel().getSize();
    }
    
    protected int getSize(JComboBoxOperator listOperator){
        return ((JComboBox)listOperator.getSource()).getModel().getSize();
    }
    
    protected void checkAtrribute(String attributeName, JComboBoxOperator operator) {
        checkAtrribute(attributeName,operator, false);
    }
    
    protected void checkAtrribute(String attributeName, JComboBoxOperator operator, boolean ignoreLastItem) {
        int size = getSize(operator);
        assertFalse("SOME ITEMS", size == 0);
        //--------INSERT ONCE--------//
        if (ignoreLastItem) --size;
        int order = new Random().nextInt(size-1)+1;
        operator.selectItem(order);
        waitUpdate();
        String selected = getItems(operator).get(order);
        //String selected = operator.getSelectedItem().toString();
        assertTrue("INSERTING", getRootRuleText().contains(attributeName + ": " + selected));
        //--------  UPDATE   --------//
        order = new Random().nextInt(size-1)+1;
        operator.selectItem(order);
        waitUpdate();
        selected = getItems(operator).get(order);
        //selected = operator.getSelectedItem().toString();
        assertTrue("UPDATING", getRootRuleText().contains(attributeName + ": "+selected));
        //-------- REMOVE -----------//
        operator.selectItem(0);//<NOT SET>
        waitUpdate();
        assertFalse("REMOVING", getRootRuleText().contains(attributeName));
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite("TestCSS");
        suite.addTest(new TestBasic("testNewCSS"));
        suite.addTest(new TestBasic("testAddRule"));
        suite.addTest(new TestBasic("testNavigator"));
        suite.addTest(new TestFontSettings("testSetFontFamily"));
        suite.addTest(new TestFontSettings("testChangeFontFamily"));
        suite.addTest(new TestFontSettings("testChangeFontSize"));
        suite.addTest(new TestFontSettings("testChangeFontWeight"));
        suite.addTest(new TestFontSettings("testChangeFontStyle"));
        suite.addTest(new TestFontSettings("testChangeFontVariant"));
        suite.addTest(new TestFontSettings("testDecoration"));
        suite.addTest(new TestFontSettings("testChangeFontColor"));
        suite.addTest(new TestIssues("test105562"));
        suite.addTest(new TestIssues("test105568"));
        suite.addTest(new TestBackgroundSettings("testTile"));
        suite.addTest(new TestBackgroundSettings("testScroll"));
        suite.addTest(new TestBackgroundSettings("testHPosition"));
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        TestRunner.run(new TestFontSettings("testChangeFontSize"));
        //TestRunner.run(suite());
    }
    
}