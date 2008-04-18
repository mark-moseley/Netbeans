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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class AddCastTest extends ErrorHintsTestBase {
    
    public AddCastTest(String testName) {
        super(testName);
    }

    public void test117868() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private void test() {int length = 0; byte b = |length & 0xFF00; } }",
                       "[AddCastFix:...length&0xFF00:byte]",
                       "package test; public class Test {private void test() {int length = 0; byte b = (byte) (length & 0xFF00); } }");
    }
    
    public void test118284() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private Object[][] o; private String test() {return |o[0][0];} }",
                       "[AddCastFix:...o[][]:String]",
                       "package test; public class Test {private Object[][] o; private String test() {return (String) o[0][0];} }");
    }

    public void testMethodInvocation1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private void test() {Object o = null; |x(o);} private void x(String s) {}}",
                       "[AddCastFix:...o:String]",
                       "package test; public class Test {private void test() {Object o = null; x((String) o);} private void x(String s) {}}");
    }
    
    public void testMethodInvocation2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private void test() {java.util.List<String> l = null; Object o = null; |l.add(o);} }",
                       "[AddCastFix:...o:String]",
                       "package test; public class Test {private void test() {java.util.List<String> l = null; Object o = null; l.add((String) o);} }");
    }
    
    public void testNewClass1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private static void test() {Object o = null; |new Test(o);} public Test(String s) {} }",
                       "[AddCastFix:...o:String]",
                       "package test; public class Test {private static void test() {Object o = null; new Test((String) o);} public Test(String s) {} }");
    }
    
    public void test132639() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import javax.swing.JComponent; public class Test {private static void test() {Class<? extends JComponent> c; c = |Class.forName(\"java.swingx.JLabel\");}}",
                       "[AddCastFix:...forName(...):Class<? extends JComponent>]",
                       "package test; import javax.swing.JComponent; public class Test {private static void test() {Class<? extends JComponent> c; c = (Class<? extends JComponent>) Class.forName(\"java.swingx.JLabel\");}}");
    }
    
    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new AddCast().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof AddCastFix) {
            return ((AddCastFix) f).toDebugString();
        }
        
        return super.toDebugString(info, f);
    }
    
}
