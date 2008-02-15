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
package org.netbeans.api.java.source;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TreeUtilitiesTest extends NbTestCase {
    
    public TreeUtilitiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }
    
    private CompilationInfo info;
    
    private void prepareTest(String filename, String code) throws Exception {
        File work = TestUtil.createWorkFolder();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = sourceRoot.createFolder("test");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        FileObject testSource = packageRoot.createData(filename + ".java");
        
        assertNotNull(testSource);
        
        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), code);
        
        JavaSource js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);
        
        assertNotNull(info);
    }

    public void testIsSynthetic1() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(47);
        BlockTree bt = (BlockTree) tp.getLeaf();
        
        tp = new TreePath(tp, bt.getStatements().get(0));
        
        assertTrue(info.getTreeUtilities().isSynthetic(tp));
    }
    
    public void testIsSynthetic2() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){super();}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(47);
        BlockTree bt = (BlockTree) tp.getLeaf();
        
        tp = new TreePath(tp, bt.getStatements().get(0));
        
        assertFalse(info.getTreeUtilities().isSynthetic(tp));
    }

    public void testFindNameSpan1() throws Exception {
        prepareTest("Test", "package test; public class Test {}");
        
        TreePath tp = info.getTreeUtilities().pathFor(29);
        ClassTree ct = (ClassTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {27, 31}));
    }

    public void testFindNameSpanEnum() throws Exception {
        prepareTest("Test", "package test; public enum Test {}");
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {56 - 30, 60 - 30}));
    }

    public void testFindNameSpanInterface() throws Exception {
        prepareTest("Test", "package test; public interface Test {}");
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {61 - 30, 65 - 30}));
    }

    public void testFindNameSpanAnnotationType() throws Exception {
        prepareTest("Test", "package test; public @interface Test {}");
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {62 - 30, 66 - 30}));
    }
    
    public void testFindNameSpan2() throws Exception {
        prepareTest("Test", "package test; public /*dsfasfd*/   class   /*laksdjflk*/   /**asdff*/ //    \n Test {}");
        
        TreePath tp = info.getTreeUtilities().pathFor(109 - 30);
        ClassTree ct = (ClassTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {108 - 30, 112 - 30}));
    }
    
    public void testFindNameSpan3() throws Exception {
        prepareTest("Test", "package test; public class   {}");
        
        TreePath tp = info.getTreeUtilities().pathFor(54 - 30);
        ClassTree ct = (ClassTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertEquals(null, span);
    }
    
    public void testFindNameSpan4() throws Exception {
        prepareTest("Test", "package test; public class Test {public void test() {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {75 - 30, 79 - 30}));
    }
    
    public void testFindNameSpan5() throws Exception {
        prepareTest("Test", "package test; public class Test {private int test;}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        VariableTree ct = (VariableTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {75 - 30, 79 - 30}));
    }
    
    public void testFindNameSpan6() throws Exception {
        prepareTest("Test", "package test; public class Test {public void test()[] {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {75 - 30, 79 - 30}));
    }
    
    public void testFindNameSpan7() throws Exception {
        prepareTest("Test", "package test; public class Test {private int test[];}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        VariableTree ct = (VariableTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {75 - 30, 79 - 30}));
    }
    
    public void testFindNameSpan8() throws Exception {
        prepareTest("Test", "package test; public class Test {private test.Test t;}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MemberSelectTree ct = (MemberSelectTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {76 - 30, 80 - 30}));
    }
    
    public void testFindNameSpan9() throws Exception {
        prepareTest("Test", "package test; public class Test {private test. /*adsTestsldf*/ //\n /**aTestklajdf*/ Test t;}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MemberSelectTree ct = (MemberSelectTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {114 - 30, 118 - 30}));
    }
    
    public void testFindNameSpan10() throws Exception {
        prepareTest("Test", "package test; public class Test {public void /*test*/test()[] {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {83 - 30, 87 - 30}));
    }
    
    public void testFindNameSpan11() throws Exception {
        prepareTest("Test", "package test; public class Test {private int /*test*/test[];}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        VariableTree ct = (VariableTree) tp.getParentPath().getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {83 - 30, 87 - 30}));
    }
    
    public void testFindNameSpanConstructor() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(70 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {70 - 30, 74 - 30}));
    }
    
    public void testFindNameSpanConstructor2() throws Exception {
        prepareTest("Test", "package test; public class Test {}");
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        MethodTree mt = (MethodTree) ct.getMembers().get(0); // synthetic constructor
        
        int[] span = info.getTreeUtilities().findNameSpan(mt);
        
        assertNull(span);
    }
    
    public void testTreePath124760a() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(int iii[]){}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(50);
        
        assertEquals(Kind.VARIABLE, tp.getLeaf().getKind());
        //#125856:
        assertFalse(Kind.VARIABLE == tp.getParentPath().getLeaf().getKind());
    }
    
    public void testTreePath124760b() throws Exception {
        prepareTest("Test", "package test; public class Test {public int test()[]{}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(47);
        
        assertEquals(Kind.METHOD, tp.getLeaf().getKind());
        //#125856:
        assertFalse(Kind.METHOD == tp.getParentPath().getLeaf().getKind());
    }
    
    public void testAutoMapComments1() throws Exception {
        prepareTest("Test", "package test;\n" +
                            "import java.io.File;\n" +
                            "\n" +
                            "/*test1*/\n" +
                            "public class Test {\n" +
                            "\n" +
                            "    //test2\n" +
                            "    void method() {\n" +
                            "        // Test\n" +
                            "        int a = 0;\n" +
                            "    }\n" +
                            "\n" +
                            "}\n");
        
        ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        List<Comment> clazzComments = info.getTreeUtilities().getComments(clazz, true);
        
        assertEquals(3, clazzComments.size());
        
        assertEquals(Style.WHITESPACE, clazzComments.get(0).style());
        assertEquals(Style.BLOCK, clazzComments.get(1).style());
        assertEquals(Style.WHITESPACE, clazzComments.get(0).style());
        
        assertEquals("/*test1*/", clazzComments.get(1).getText());
        
        List<Comment> clazzComments2 = info.getTreeUtilities().getComments(clazz, true);
        
        assertEquals(3, clazzComments2.size());
        
        assertTrue(clazzComments.get(0) == clazzComments2.get(0));
        assertTrue(clazzComments.get(1) == clazzComments2.get(1));
        assertTrue(clazzComments.get(2) == clazzComments2.get(2));
    }
    
    public void testAutoMapComments2() throws Exception {
        prepareTest("Test", "package test;\n" +
                            "import java.io.File;\n" +
                            "\n" +
                            "/*test1*/\n" +
                            "public class Test {\n" +
                            "\n" +
                            "    //test2\n" +
                            "    void method() {\n" +
                            "        // Test\n" +
                            "        int a = 0;\n" +
                            "    }\n" +
                            "\n" +
                            "}\n");
        
        ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        MethodTree method = (MethodTree) clazz.getMembers().get(1);
        List<Comment> methodComments = info.getTreeUtilities().getComments(method, true);
        
        assertEquals(3, methodComments.size());
        
        assertEquals(Style.WHITESPACE, methodComments.get(0).style());
        assertEquals(Style.LINE, methodComments.get(1).style());
        assertEquals(Style.WHITESPACE, methodComments.get(0).style());
        
        assertEquals("//test2\n", methodComments.get(1).getText());
        
        List<Comment> methodComments2 = info.getTreeUtilities().getComments(method, true);
        
        assertEquals(3, methodComments2.size());
        
        assertTrue(methodComments.get(0) == methodComments2.get(0));
        assertTrue(methodComments.get(1) == methodComments2.get(1));
        assertTrue(methodComments.get(2) == methodComments2.get(2));
    }
}
