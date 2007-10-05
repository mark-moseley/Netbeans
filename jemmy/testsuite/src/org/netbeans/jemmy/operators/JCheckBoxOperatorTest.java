/* * $Id$ * * --------------------------------------------------------------------------- * * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. * * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved. * * The contents of this file are subject to the terms of either the GNU * General Public License Version 2 only ("GPL") or the Common * Development and Distribution License("CDDL") (collectively, the * "License"). You may not use this file except in compliance with the * License. You can obtain a copy of the License at * http://www.netbeans.org/cddl-gplv2.html * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the * specific language governing permissions and limitations under the * License.  When distributing the software, include this License Header * Notice in each file and include the License file at * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this * particular file as subject to the "Classpath" exception as provided * by Sun in the GPL Version 2 section of the License file that * accompanied this code. If applicable, add the following below the * License Header, with the fields enclosed by brackets [] replaced by * your own identifying information: * "Portions Copyrighted [year] [name of copyright owner]" * * Contributor(s): Manfred Riem (mriem@netbeans.org). * * The Original Software is the Jemmy library. The Initial Developer of the * Original Software is Alexandre Iline. All Rights Reserved. * * If you wish your version of this file to be governed by only the CDDL * or only the GPL Version 2, indicate your decision by adding * "[Contributor] elects to include this software in this distribution * under the [CDDL or GPL Version 2] license." If you do not indicate a * single choice of license, a recipient has the option to distribute * your version of this file under either the CDDL, the GPL Version 2 or * to extend the choice of license to its licensees as provided above. * However, if you add GPL Version 2 code and therefore, elected the GPL * Version 2 license, then the option applies only if the new code is * made subject to such option by the copyright holder. * * --------------------------------------------------------------------------- * */package org.netbeans.jemmy.operators;import javax.swing.JCheckBox;import javax.swing.JFrame;import junit.framework.Test;import junit.framework.TestCase;import junit.framework.TestSuite;import org.netbeans.jemmy.util.NameComponentChooser;/** * A JUnit test for JCheckBoxOperatorTest. * * @author Manfred Riem (mriem@netbeans.org) * @version $Revision$ */public class JCheckBoxOperatorTest extends TestCase {    /**     * Stores the frame we use for testing.     */    private JFrame frame;        /**     * Stores the checkBox we use for testing.     */    private JCheckBox checkBox;        /**     * Constructor.     *      * @param testName the name of the test.     */    public JCheckBoxOperatorTest(String testName) {        super(testName);    }    /**     * Setup for testing.     */    protected void setUp() throws Exception {        frame = new JFrame();        checkBox = new JCheckBox("JCheckBoxOperatorTest");        checkBox.setName("JCheckBoxOperatorTest");        frame.getContentPane().add(checkBox);        frame.pack();        frame.setLocationRelativeTo(null);    }    /**     * Cleanup after testing.     */    protected void tearDown() throws Exception {        frame.setVisible(false);        frame.dispose();        frame = null;    }    /**     * Suite method.     */    public static Test suite() {        TestSuite suite = new TestSuite(JCheckBoxOperatorTest.class);                return suite;    }    /**     * Test constructor.     */    public void testConstructor() {        frame.setVisible(true);                JFrameOperator operator1 = new JFrameOperator();        assertNotNull(operator1);                JCheckBoxOperator operator2 = new JCheckBoxOperator(operator1);        assertNotNull(operator2);                JCheckBoxOperator operator3 = new JCheckBoxOperator(operator1, new NameComponentChooser("JCheckBoxOperatorTest"));        assertNotNull(operator3);        JCheckBoxOperator operator4 = new JCheckBoxOperator(operator1, "JCheckBoxOperatorTest");        assertNotNull(operator4);    }            /**     * Test findJCheckBox method.     */    public void testFindJCheckBox() {        frame.setVisible(true);        JCheckBox checkBox1 = JCheckBoxOperator.findJCheckBox(frame, new NameComponentChooser("JCheckBoxOperatorTest"));        assertNotNull(checkBox1);        JCheckBox checkBox2 = JCheckBoxOperator.findJCheckBox(frame, "JCheckBoxOperatorTest", false, false);        assertNotNull(checkBox2);    }    /**     * Test waitJCheckBox method.     */    public void testWaitJCheckBox() {        frame.setVisible(true);        JCheckBox checkBox1 = JCheckBoxOperator.waitJCheckBox(frame, new NameComponentChooser("JCheckBoxOperatorTest"));        assertNotNull(checkBox1);        JCheckBox checkBox2 = JCheckBoxOperator.waitJCheckBox(frame, "JCheckBoxOperatorTest", false, false);        assertNotNull(checkBox2);    }}