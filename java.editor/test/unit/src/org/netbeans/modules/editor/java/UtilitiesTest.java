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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.TreePathScanner;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class UtilitiesTest extends NbTestCase {
    
    public UtilitiesTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        super.setUp();
    }

    public void testFuzzyResolveMethod1() throws Exception {
        performTest("package test;" +
                "public class Test {" +
                "   private void test() {" +
                "       Object o = null;" +
                "       t(o);" +
                "   }" +
                "   private void t(String s) {}" +
                "}", "t(java.lang.String)");
    }
    
    public void testFuzzyResolveMethod2() throws Exception {
        performTest("package test;" +
                "public class Test {" +
                "   private void test() {" +
                "       Object o = null;" +
                "       t(o);" +
                "   }" +
                "   private static void t(String s) {}" +
                "}", "t(java.lang.String)");
    }
    
    public void testFuzzyResolveMethod3() throws Exception {
        performTest("package test;" +
                "public class Test {" +
                "   private void test() {" +
                "       test(\"\", x);\n" +
                "   }" +
                "   private static void test(String s, int i) {}\n" +
                "   private static void test(Object o, int i) {}\n" +
                "}", "test(java.lang.String,int)");
    }
    
    public void testFuzzyResolveMethod124901() throws Exception {
        performTest("package test;" +
                "public class Test<K> {" +
                "   private K read() {return null;}\n" +
                "   private void test() {" +
                "       read().read();\n" +
                "   }" +
                "}", "<not resolved>", "<not resolved>");
    }
    
    public void testFuzzyResolveConstructor1() throws Exception {
        performTest("package test;" +
                "public class Test {" +
                "   private void test() {" +
                "       Object o = null;" +
                "       new Test(o);" +
                "   }" +
                "   private Test(String s) {}" +
                "}", "Test(java.lang.String)");
    }
    
    public void testFuzzyResolveConstructor2() throws Exception {
        performTest("package test;" +
                "import java.awt.Font;\n" +
                "public class Test {" +
                "   private void test() {" +
                "       new Font(getSetting(\"fontName\"), Font.BOLD, 12);" +
                "   }\n" +
                "   public Object getSetting(String s) {return s;}\n" +
                "}", "Font(java.lang.String,int,int)", "<not resolved>");
    }
    
    private FileObject source;
    
    private void performTest(String sourceCode, String... golden) throws Exception {
        FileObject root = GoToSupportTest.makeScratchDir(this);
        
        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");
        FileObject testDir  = sourceDir.createFolder("test");
        
        source = testDir.createData("Test.java");
        
        TestUtilities.copyStringToFile(source, sourceCode);
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        SourceUtilsTestUtil.compileRecursively(sourceDir);
        
        final List<String> result = new LinkedList<String>();
        
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {
            public void run(final CompilationController cc) throws Exception {
                cc.toPhase(Phase.RESOLVED);
                
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                        if (!cc.getTreeUtilities().isSynthetic(getCurrentPath())) {
                            ExecutableElement ee = Utilities.fuzzyResolveMethodInvocation(cc, getCurrentPath(), new TypeMirror[1], new int[1]);

                            if (ee != null) {
                                result.add(ee.toString()); //XXX
                            } else {
                                result.add("<not resolved>");
                            }
                        }
                        
                        return super.visitMethodInvocation(node, p);
                    }

                    @Override
                    public Void visitNewClass(NewClassTree node, Void p) {
                        if (!cc.getTreeUtilities().isSynthetic(getCurrentPath())) {
                            ExecutableElement ee = Utilities.fuzzyResolveMethodInvocation(cc, getCurrentPath(), new TypeMirror[1], new int[1]);

                            if (ee != null) {
                                result.add(ee.toString()); //XXX
                            } else {
                                result.add("<not resolved>");
                            }
                        }
                        
                        return super.visitNewClass(node, p);
                    }
                    
                }.scan(cc.getCompilationUnit(), null);
            }
        }, true);
        
        assertEquals(Arrays.asList(golden), result);
    }
}
