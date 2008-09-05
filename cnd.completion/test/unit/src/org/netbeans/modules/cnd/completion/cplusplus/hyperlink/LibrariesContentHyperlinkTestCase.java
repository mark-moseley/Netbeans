/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Test case for hyperlink to library content
 *
 * @author Nick Krasilnikov
 */
public class LibrariesContentHyperlinkTestCase extends HyperlinkBaseTestCase {

    public LibrariesContentHyperlinkTestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected File changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        // we have following structure for this test
        // test-folder
        //  --src\
        //        main.cc
        //  --sys_include1\
        //        include1.h
        //  --sys_include2\
        //        include2.h
        //
        // so, adjust used folders

        File srcDir = new File(projectDir, "src");
        File incl1 = new File(projectDir, "sys_include");
        File incl2 = new File(projectDir, "sys_include2");
        checkDir(srcDir);
        checkDir(incl1);
        checkDir(incl2);
        List<String> sysIncludes = Arrays.asList(incl1.getAbsolutePath(), incl2.getAbsolutePath());
        super.setSysIncludes(sysIncludes);
        return srcDir;
    }

    private void checkDir(File srcDir) {
        assertTrue("Not existing directory" + srcDir, srcDir.exists());
        assertTrue("Not directory" + srcDir, srcDir.isDirectory());
    }

    public void testDuplicationConstructions_0() throws Exception {
        // IZ#145982: context of code changes unexpectedly
        performTest("src/testDup1.cc", 5, 15, "src/dup1.h", 12, 5); // duplicationFoo
        performTest("src/testDup1.cc", 7, 15, "src/dup1.h", 5, 5); // classElementDup
//        performTest("src/testDup1.cc", 4, 10, "sys_include/sys1dup.h", 10, 1); // Duplication
//        performTest("src/testDup1.cc", 6, 10, "sys_include2/sys2dup.h", 3, 1); // ElementDup
    }

    public void testDuplicationConstructions_1() throws Exception {
        // IZ#145982: context of code changes unexpectedly
        performTest("src/testSys1Dup.cc", 5, 15, "sys_include/sys1dup.h", 4, 5); // duplicationSys1
        performTest("src/testSys1Dup.cc", 7, 15, "sys_include/sys1dup.h", 11, 5); // structMethod
//        performTest("src/testSysDup1.cc", 4, 10, "sys_include/sys1dup.h", 2, 1); // Duplication
//        performTest("src/testSysDup1.cc", 6, 10, "sys_include/sys1dup.h", 10, 1); // ElementDup
    }

    public void testDuplicationConstructions_2() throws Exception {
        // IZ#145982: context of code changes unexpectedly
        performTest("src/testSys2Dup.cc", 5, 15, "sys_include2/sys2dup.h", 4, 5); // duplicationSys2
        performTest("src/testSys2Dup.cc", 7, 15, "sys_include2/sys2dup.h", 4, 5); // duplicationSys2
//        performTest("src/testSysDup2.cc", 4, 10, "sys_include/sys2dup.h", 2, 1); // Duplication
//        performTest("src/testSysDup2.cc", 6, 10, "sys_include/sys2dup.h", 9, 1); // ElementDup
    }
    
    public void testTypedefClassFwd() throws Exception {
        // IZ#146289: REGRESSTION: inaccuracy tests show significant regressions
        performTest("src/testTdClassFwdResolve.cc", 5, 25, "src/outer.h", 3, 5); // outerFunction
    }

    public void testLibraryClass() throws Exception {
        performTest("src/main.cc", 7, 6, "sys_include2/include2.h", 9, 1);
    }

    public void testLibraryClassConstructor() throws Exception {
        // IZ 137971 : library class name after "new" is not resolved
        performTest("src/main.cc", 7, 20, "sys_include2/include2.h", 9, 1);
    }

    public void testLibraryClassConstructor2() throws Exception {
        performTest("src/main.cc", 9, 20, "sys_include/include1.h", 12, 5);
    }

    public void testNsAliases() throws Exception {
        // IZ 131914: Code completion should work for namespace aliases
        performTest("src/main.cc", 18, 16, "sys_include/include1.h", 32, 5);
        performTest("src/main.cc", 19, 16, "src/include.h", 4, 5);
    }

    public void testNamespaceOverride() throws Exception {
        // Main project has namespace std overriding std from library.
        // This should not break hyperlinks for original std members.
        performTest("src/main.cc", 20, 13, "sys_include/include1.h", 37, 1);
    }

    public void testGlobalNamespaceInLibrary() throws Exception {
        // Library has declaration of size_t and namespace std with
        // "using ::size_t". For hyperlink to work in this declaration
        // the global namespace must be resolved in library project,
        // not in the main project.
        performTest("sys_include/include1.h", 40, 15, "sys_include/include1.h", 37, 1);
    }

    public void testEndl() throws Exception {
        performTest("src/main2.cc", 7, 8, "sys_include/iostream", 20, 5);
        performTest("src/main2.cc", 7, 26, "sys_include/iostream", 14, 5);
    }

    public void testNamespaceInDifferentFolders() throws Exception {
        performTest("src/main.cc", 26, 8, "sys_include/include1.h", 44, 5);
        performTest("src/main.cc", 27, 8, "sys_include2/include2.h", 28, 5);
    }

    public void testIZ140787_cout() throws Exception {
        // iz #140787 cout, endl unresolved in some Loki files
        performTest("src/iz140787_cout.cc", 9, 9, "sys_include/include1.h", 44, 5);
        performTest("src/iz140787_cout.cc", 10, 10, "sys_include2/include2.h", 28, 5);
    }

}
