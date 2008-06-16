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

package org.netbeans.jellytools;

import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
/**
 * Test of org.netbeans.jellytools.QuestionDialogOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class QuestionDialogOperatorTest extends NbDialogOperatorTest {

    /** "Question" */
    private static final String TEST_DIALOG_TITLE =
            Bundle.getString("org.openide.text.Bundle", "LBL_SaveFile_Title");

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public QuestionDialogOperatorTest(String testName) {
        super(testName);
    }
    
    public static final String[] tests = new String[] {
        "testConstructorWithParameter", "testLblQuestion"};

    public static junit.framework.Test suite() {
         return NbModuleSuite.create(QuestionDialogOperatorTest.class, ".*", ".*", tests);
    }
    
    /** Shows dialog to test. */
    protected void setUp() {
        showTestDialog(TEST_DIALOG_TITLE);
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test constructor with text parameter. */
    public void testConstructorWithParameter() {
        new QuestionDialogOperator(TEST_DIALOG_LABEL).ok();
    }

    /** Test lblQuestion() method. */
    public void testLblQuestion() {
        QuestionDialogOperator qdo = new QuestionDialogOperator();
        String label = qdo.lblQuestion().getText();
        qdo.ok();
        assertEquals("Wrong label found.", TEST_DIALOG_LABEL, label);
    }
    
}
