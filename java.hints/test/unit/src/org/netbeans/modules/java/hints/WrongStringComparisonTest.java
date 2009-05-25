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

package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jan Lahoda
 */
public class WrongStringComparisonTest extends TreeRuleTestBase {

    private WrongStringComparison wsc;

    public WrongStringComparisonTest(String name) {
        super(name);
        wsc = new WrongStringComparison();
    }

    public void testSimple() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        String t = null;" +
                            "        if (s =|= t);" +
                            "    }" +
                            "}",
                            "0:114-0:120:verifier:Comparing Strings using == or !=");
    }
    
    public void testDisableWhenCorrectlyCheckedAsIn111441() throws Exception {
        String code = "package test;" +
                      "public class Test {" +
                      "    private String s;" +
                      "    private void test() {" +
                      "        Test t = null;" +
                      "        boolean b = this.s !";
        
        String codeAfter = "= t.s && (this.s == null || !this.s.equals(t.s));" +
                           "    }" +
                           "}";
        performAnalysisTest("test/Test.java", code + codeAfter, code.length());
    }

    public void testFixWithTernaryNullCheck() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        String t = null;" +
                            "        if (s =|= t);" +
                            "    }" +
                            "}",
                            "0:114-0:120:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals() with null check (ternary)]",
                            "package test;public class Test { private String s; private void test() { String t = null; if (s == null ? t == null : s.equals(t)); }}");
    }

    public void testFixWithoutNullCheck() throws Exception {
        WrongStringComparison.setTernaryNullCheck(wsc.getPreferences(HintsSettings.getCurrentProfileId()), false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        String t = null;" +
                            "        if (s =|= t);" +
                            "    }" +
                            "}",
                            "0:114-0:120:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals() without null check]",
                            "package test;public class Test { private String s; private void test() { String t = null; if (s.equals(t)); }}");
    }

    public void testFixWithNullCheck() throws Exception {
        WrongStringComparison.setTernaryNullCheck(wsc.getPreferences(HintsSettings.getCurrentProfileId()), false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        String t = null;" +
                            "        if (s =|= t);" +
                            "    }" +
                            "}",
                            "0:114-0:120:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals() with null check]",
                            "package test;public class Test { private String s; private void test() { String t = null; if ((s == null && t == null) || (s != null && s.equals(t))); }}");
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        return wsc.run(info, path);
    }

}