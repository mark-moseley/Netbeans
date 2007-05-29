/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationInfo;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;


/**
 *
 * @author Jan Lahoda
 */
public class CreateMethodTest extends ErrorHintsTestBase {
    
    /** Creates a new instance of CreateElementTest */
    public CreateMethodTest(String name) {
        super(name);
    }
    
    public void testMoreMethods() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {test(1);}}", 103 - 48, "CreateMethodFix:test(int i)void:test.Test");
    }
    
    public void testConstructor() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new Test(1);}}", 114 - 48, "CreateConstructorFix:(int i):test.Test");
    }
    
    public void testNoCreateConstructorForNonExistingClass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new NonExisting(1);}}", 114 - 48);
    }
    
    public void testFieldLike() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {Collections.emptyList();}}", 107 - 48);
    }

    public void testMemberSelect1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {emptyList().doSomething();}}", 107 - 48, "CreateMethodFix:emptyList()java.lang.Object:test.Test");
    }
    
    public void testMemberSelect2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public Test test() {test().doSomething();}}", 112 - 48, "CreateMethodFix:doSomething()void:test.Test");
    }
    
    public void testAssignment() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {int i = fff();}}", 110 - 48, "CreateMethodFix:fff()int:test.Test");
    }
    
    public void testNewInAnnonymousInnerclass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public Test(){} public void test() {new Runnable() {public void run() {new Test(1);}}}}", 158 - 48, "CreateConstructorFix:(int i):test.Test");
    }
    
    public void testCreateMethodInInterface() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {Int i = null; i.test(1);} public static interface Int{}}", 96 - 24,
                       "CreateMethodFix:test(int i)void:test.Test.Int",
                       "package test; public class Test {public void test() {Int i = null; i.test(1);} public static interface Int{ public void test(int i); }}");
    }
    
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) {
        List<Fix> fixes = new CreateElement().analyze(info, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateMethodFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((CreateMethodFix) f).toDebugString(info);
    }
    
}
