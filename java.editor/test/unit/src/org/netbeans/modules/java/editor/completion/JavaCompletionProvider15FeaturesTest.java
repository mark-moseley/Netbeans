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

package org.netbeans.modules.java.editor.completion;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionProvider15FeaturesTest extends CompletionTestBase {

    public JavaCompletionProvider15FeaturesTest(String testName) {
        super(testName);
    }

    // Java 1.5 generics tests -------------------------------------------------------

    public void testEmptyFileBeforeTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<", "empty.pass");
    }

    public void testBeforeTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<", "empty.pass");
    }
    
    public void testBeforeFirstTypeParam() throws Exception {
        performTest("Generics", 33, null, "empty.pass");
    }
    
    public void testEmptyFileTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X", "empty.pass");
    }
    
    public void testTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X", "empty.pass");
    }
    
    public void testOnFirstTypeParam() throws Exception {
        performTest("Generics", 34, null, "empty.pass");
    }

    public void testEmptyFileAfterTypingFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsStart", 32, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterTypingFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterFirstTypeParamAndSpace() throws Exception {
        performTest("Generics", 35, null, "extendsKeyword.pass");
    }

    public void testEmptyFileTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X e", "extendsKeyword.pass");
    }
    
    public void testTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X e", "extendsKeyword.pass");
    }
    
    public void testOnExtendsInFirstTypeParam() throws Exception {
        performTest("Generics", 36, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterExtendsInFirstTypeParam() throws Exception {
        performTest("Generics", 42, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends ", "javaLangContent.pass");
    }
    
    public void testAfterTypingExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends ", "javaLangContent.pass");
    }
    
    public void testAfterExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("Generics", 43, null, "javaLangContent.pass");
    }

    public void testEmptyFileAfterTypingBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterTypingBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("Generics", 49, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterFirstTypeParam() throws Exception {
        performTest("Generics", 50, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingTypeParams() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsKeywords.pass");
    }
    
    public void testAfterTypingTypeParams() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsKeywords.pass");
    }
    
    public void testAfterTypeParams() throws Exception {
        performTest("Generics", 78, null, "extendsAndImplementsKeywords.pass");
    }

}
