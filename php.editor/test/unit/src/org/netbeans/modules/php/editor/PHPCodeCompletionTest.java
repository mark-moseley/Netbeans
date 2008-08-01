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

package org.netbeans.modules.php.editor;

/**
 *
 * @author tomslot
 */
public class PHPCodeCompletionTest extends PHPTestBase {
    
    public PHPCodeCompletionTest(String testName) {
        super(testName);
    }

    public void testPhpContextWithPrefix() throws Exception {
        checkCompletion("testfiles/completion/lib/tst.php", "^GL", false);
    }

    public void testPhpContext2() throws Exception {
        checkCompletion("testfiles/completion/lib/tst.php", "$GL^", false);
    }
    
    public void testHTML() throws Exception {
        checkCompletion("testfiles/completion/lib/nowdoc02.php", "<title>^</title>", false);
    }
    
    public void test136744_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136744.php", "print $test1^", false);
    }
    
    public void test136744_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136744.php", "print $test2^", false);
    }
    
    public void test136744_3() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136744.php", "print $test3^", false);
    }
    
    public void test136744_4() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136744.php", "print $test4^", false);
    }
    // #142024 Code completion + references
    public void test142024() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142024.php", "$t->^", false);
    }    
    // #142051 CC doesn't work when an object is a refence
    public void test142051() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142051.php", "echo \"Name1: \".$user1->^", false);
    }

    public void test142051_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142051.php", "echo \"Name2: \".$user2->^", false);
    }

    // #136092 Code completion doesn't show reference parameters
    public void test136092_withoutReference() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136092.php", "$source1 = $reques^", false);
    }
    public void test136092_withReference() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136092.php", "$source2 = $reques^", false);
    }
    // #132294 [cc] cc for variables in strings not working if there are non-ws chars preceding the variablle
    public void test132294() throws Exception {
        checkCompletion("testfiles/completion/lib/issue132294.php", "echo \"Hello $ts^", false);
    }
    public void test132294_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue132294.php", "echo \"Hello$ts^", false);
    }
    // #142234 $t->| shouldn't propose __construct()
    public void test142234() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142234.php", "$t->^", false);
    }
    public void test142234_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142234.php", "parent::^", false);
    }
    // #135618 [CC] Missing static members from parent classes after "self::"
    public void test135618() throws Exception {
        checkCompletion("testfiles/completion/lib/issue135618.php", "self::^", false);
    }
    public void test135618_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue135618.php", "B135618::^", false);
    }
    public void test135618_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue135618.php", "A135618::^", false);
    }
    public void test142091() throws Exception {
        checkCompletion("testfiles/completion/lib/issue142091.php", "function f142091 (^", false);
    }
    // #136188 [cc] issues related to class name case-sensitivity
    public void test136188() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v1->^", false);
    }
    public void test136188_1() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v2->^", false);
    }
    public void test136188_2() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v3->^", false);
    }
    public void test136188_3() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v1 = new Cls136188^", false);
    }
    public void test136188_4() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v2 = new cls136188^", false);
    }
    public void test136188_5() throws Exception {
        checkCompletion("testfiles/completion/lib/issue136188.php", "$v3 = new CLS136188^", false);
    }
    // tests for class declaration until '{' like "class name extends MyClass  " 
    public void testClsDeclaration() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class^", false);
    }
    public void testClsDeclaration_1() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ^", false);
    }
    public void testClsDeclaration_2() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTes^", false);
    }
    public void testClsDeclaration_3() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest ^", false);
    }
    public void testClsDeclaration_4() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest e^", false);
    }
    public void testClsDeclaration_5() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest extends^", false);
    }
    public void testClsDeclaration_6() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest extends Cls2DeclarationTes^", false);
    }
    public void testClsDeclaration_7() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest extends Cls2DeclarationTest^", false);
    }
    public void testClsDeclaration_8() throws Exception {
        checkCompletion("testfiles/completion/lib/clsDeclaration.php", "class ClsDeclarationTest extends Cls2DeclarationTest ^", false);
    }
    public void testInsideClass() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "cons^", false);
    }
    public void testInsideClass_1() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "priv^", false);
    }
    public void testInsideClass_2() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "function ^", false);
    }
    public void testInsideClass_3() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "$this->setFl^", false);
    }
    public void testInsideClass_4() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "function __cons^", false);
    }
    public void testInsideClass_5() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "function __dest^", false);
    }
    public void testInsideClass_6() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "$v = new InsideCl^", false);
    }
    public void testInsideClass_7() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "public stat^", false);
    }
    public void testInsideClass_8() throws Exception {
        checkCompletion("testfiles/completion/lib/insideClass.php", "InsideClass::^", false);
    }
}