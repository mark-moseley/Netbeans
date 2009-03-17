/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.highlight.error;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test for IdentifierErrorProvider.
 *
 * @author Alexey Vladykin
 */
public class UnresolvedIdentifierTest extends ErrorHighlightingBaseTestCase {

    static {
        //System.setProperty("cnd.identifier.error.provider", "true");
    }

    public UnresolvedIdentifierTest(String testName) {
        super(testName);
    }

    public void testIZ145280() throws Exception {
        // IZ#145280: IDE highlights code with '__attribute__((unused))' as wrong
        performStaticTest("iz145280.cc");
    }
    
    public void testSimple() throws Exception {
        performStaticTest("simple.cpp");
    }

    public void testTemplateParameterTypes() throws Exception {
        performStaticTest("templates.cpp");
    }

    public void testMacros() throws Exception {
        performStaticTest("macros.cpp");
    }

    public void testAttributes() throws Exception {
        performStaticTest("attributes.cpp");
    }

    public void testTypedefTemplate() throws Exception {
        performStaticTest("typedef_templ.cpp");
    }

    public void testSkipSomeInstructionsBlock() throws Exception {
        performStaticTest("skipBlocks.cpp");
    }

    public void testIZ144537() throws Exception {
        performStaticTest("iz144537.cpp");
    }

    public void testForwardClassDecl() throws Exception {
        performStaticTest("forward_class_decl.cpp");
    }
    
    public void testTemplateParameterAncestor() throws Exception {
        performStaticTest("template_parameter_ancestor.cpp");
    }
    
    public void testIZ144873() throws Exception {
        performStaticTest("iz_144873.cpp");
    }

    public void testIZ145118() throws Exception {
        performStaticTest("iz_145118.cpp");
    }

    public void testIZ155112() throws Exception {
        performStaticTest("iz155112.cpp");
    }

    public void testIZ158216() throws Exception {
        // IZ#158216 : Unresolved ids in compiler extensions
        performStaticTest("iz158216.cpp");
    }

    public void testIZ158730() throws Exception {
        // IZ#158730 : False positive error highlighting on nested types in templates
        performStaticTest("iz158730.cpp");
    }

    public void testIZ158831() throws Exception {
        // IZ#158831 : False positive error highlighting errors on typedefs in local methods of templates
        performStaticTest("iz158831.cpp");
    }

    public void testIZ158873() throws Exception {
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        // IZ#158873 : recursion in Instantiation.Type.isInstantiation()
        performStaticTest("iz158873.cpp");
    }

    public void testIZ159615() throws Exception {
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        // IZ#159615 : recursion in CsmCompletionQuery.getClassifier()
        performStaticTest("iz159615.cpp");
    }

    public void testIZ143044() throws Exception {
        // IZ#143044 : Wrong overloaded method is not highlighted as error
        performStaticTest("iz143044.cpp");
    }

    public void testIZ151909() throws Exception {
        // IZ#151909 : Template friend classes (parser problem)
        performStaticTest("iz151909.cpp");
    }

    public void testIZ148236() throws Exception {
        // IZ#148236 : IDE highlights some operator's definitions as wrong code
        performStaticTest("iz148236.cpp");
    }

    /////////////////////////////////////////////////////////////////////
    // FAILS

    public static class Failed extends ErrorHighlightingBaseTestCase {

        public Failed(String testName) {
            super(testName);
        }

        @Override
        protected Class getTestCaseDataClass() {
            return UnresolvedIdentifierTest.class;
        }

        public void testIZ158280() throws Exception {
            // IZ#158280 : False positive error highlighting on templates in case of macro usage
            performStaticTest("iz158280.cpp");
        }
    }
}
