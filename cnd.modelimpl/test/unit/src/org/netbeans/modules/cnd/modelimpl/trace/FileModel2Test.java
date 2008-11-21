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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.util.Collection;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 * Just a continuation of the FileModelTest
 * (which became too large)
 * @author Vladimir Kvashin
 */
public class FileModel2Test extends TraceModelTestBase {

    public FileModel2Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    public void testIZ136887() throws Exception {
        // IZ136887: Model do not support bit fields
        performTest("iz136887.cc");
    }

    public void testIZ149505() throws Exception {
        // IZ#149505: special handling of __VA_ARGS__ with preceding comma
        performTest("iz149505.cc");
    }

    public void testIZ145280() throws Exception {
        // IZ#145280: IDE highlights code with '__attribute__((unused))' as wrong
        performTest("iz145280.cc");
    }

    public void testIZ143977_0() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_0.cc");
    }

    public void testIZ143977_1() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_1.cc");
    }

    public void testIZ143977_2() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_2.cc");
    }

    public void testIZ143977_3() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_3.cc");
    }

    public void testIZ103462_1() throws Exception {
        // IZ#103462: Errors in template typedef processing:   'first' and 'second' are missed in Code Completion listbox
        performTest("iz103462_first_and_second_1.cc");
    }

    public void testHeaderWithCKeywords() throws Exception {
        // IZ#144403: restrict keywords are flagged as ERRORs in C header files
        performTest("testHeaderWithCKeywords.c");
    }

    public void testNamesakes() throws Exception {
        // IZ#145553 Class in the same namespace should have priority over a global one
        performTest("iz_145553_namesakes.cc");
    }

    public void testIZ146560() throws Exception {
        // IZ#146560 Internal C++ compiler does not accept 'struct' after 'new'
        performTest("iz146560.cc");
    }

    public void testIZ147284isDefined() throws Exception {
        // IZ#147284 APTMacroCallback.isDefined(CharSequence) ignores #undef
        String base = "iz147284_is_defined";
        performTest(base + ".cc");
        FileImpl fileImpl = findFile(base + ".h");
        assertNotNull(fileImpl);
        Collection<APTPreprocHandler> handlers = fileImpl.getPreprocHandlers();
        assertEquals(handlers.size(), 1);
        String macro = "MAC";
        assertFalse(macro + " should be undefined!", handlers.iterator().next().getMacroMap().isDefined(macro));
    }

    public void testIZ147574() throws Exception {
        // IZ#147574 Parser cann't recognize code in yy.tab.c file correctly
        performTest("iz147574.c");
    }

    public void testIZ148014() throws Exception {
        // IZ#148014 Unable to resolve pure virtual method that throws
        performTest("iz148014.cc");
    }

    public void testIZ149225() throws Exception {
        // IZ#149225 incorrect concatenation with token that starts with digit
        performTest("iz149225.c");
    }

    public void testIZ151621() throws Exception {
        // IZ#151621 no support for __thread keyword
        performTest("iz151621.c");
    }

}
