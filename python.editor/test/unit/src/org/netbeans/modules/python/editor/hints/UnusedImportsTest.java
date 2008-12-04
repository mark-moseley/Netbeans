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

package org.netbeans.modules.python.editor.hints;

import org.netbeans.modules.python.editor.PythonTestBase;

/**
 *
 * @author Tor Norbye
 */
public class UnusedImportsTest extends PythonTestBase {

    public UnusedImportsTest(String name) {
        super(name);
    }

    private PythonAstRule createRule() {
        return new UnusedImports();
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testHints() throws Exception {
        findHints(this, createRule(), "testfiles/unusedimports1.py", null, null);
    }

    public void testNoHints2() throws Exception {
        // The operator import isn't unused - it's responsible for importing abs() even though
        // abs() would otherwise have been imported by the builtin functions module.
        findHints(this, createRule(), "testfiles/unusedimports3.py", null, null);
    }

    public void testFix1() throws Exception {
        applyHint(this, createRule(), "testfiles/unusedimports1.py", "import ur^import", "Remove Unused");
    }

    public void testFix2() throws Exception {
        applyHint(this, createRule(), "testfiles/unusedimports1.py", "import ur^import", "Remove All");
    }

    public void testFix3() throws Exception {
        applyHint(this, createRule(), "testfiles/unusedimports1.py", "import ur^import", "Organize");
    }

    public void testFix4() throws Exception {
        applyHint(this, createRule(), "testfiles/unusedimports1.py", "from foo import Sy^m1, Sym2", "Sym1");
    }

    public void testFix5() throws Exception {
        // BUGGY -- there should be no comma at the end of sys! TODO FIXME
        applyHint(this, createRule(), "testfiles/unusedimports2.py", "import sys, os, f^our", "four");
    }
}