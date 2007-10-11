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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.tools.javac.code.Flags;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;
import org.netbeans.api.editor.indent.Reformat;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Test different formating options
 * 
 * @author Dusan Balek
 */
public class FormatingTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of FormatingTest */
    public FormatingTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FormatingTest.class);
//        suite.addTest(new FormatingTest("testLabelled"));
        return suite;
    }

    public void testClass() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, " ");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final int[] counter = new int[] {0};
        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putInt("rightMargin", 30);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                MethodTree method = maker.Method(maker.Modifiers(EnumSet.of(Modifier.PUBLIC)), "run", maker.Identifier("void"), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                List<Tree> impl = new ArrayList<Tree>();
                impl.add(maker.Identifier("Runnable"));
                impl.add(maker.Identifier("Serializable"));
                ClassTree clazz = maker.Class(maker.Modifiers(Collections.<Modifier>emptySet()), "Test" + counter[0]++, Collections.<TypeParameterTree>emptyList(), maker.Identifier("Integer"), impl, Collections.singletonList(method));
                if (counter[0] == 1)
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.CompilationUnit(maker.Identifier("hierbas.del.litoral"), Collections.<ImportTree>emptyList(), Collections.singletonList(clazz), workingCopy.getCompilationUnit().getSourceFile()));
                else
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.addCompUnitTypeDecl(workingCopy.getCompilationUnit(), clazz));
            }            
        };
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentTopLevelClassMembers", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentTopLevelClassMembers", true);

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineImplements", true);
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineImplements", false);
        testSource.runModificationTask(task).commit();
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_NEVER.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "class Test0 extends Integer implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test1 extends Integer implements Runnable, Serializable{\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test2 extends Integer implements Runnable, Serializable\n" +
            "{\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test3 extends Integer implements Runnable, Serializable\n" +
            "  {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "  }\n\n" +
            "class Test4 extends Integer implements Runnable, Serializable\n" +
            "    {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "    }\n\n" +
            "class Test5 extends Integer implements Runnable, Serializable {\n\n" +
            "public void run() {\n" +
            "}\n" +
            "}\n\n" +
            "class Test6 extends Integer\n" +
            "        implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test7\n" +
            "        extends Integer\n" +
            "        implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test8\n" +
            "        extends Integer\n" +
            "        implements Runnable,\n" +
            "        Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test9\n" +
            "        extends Integer\n" +
            "        implements Runnable,\n" +
            "                   Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test10 extends Integer implements Runnable,\n" +
            "                                        Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test11 extends Integer implements Runnable,\n" +
            "        Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
        
        String content = 
            "package hierbas.del.litoral;" +
            "class Test extends Integer implements Runnable, Serializable{" +
            "public void run(){" +
            "}" +
            "}\n";
        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);
        
        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer implements Runnable, Serializable{\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer implements Runnable, Serializable\n" +
            "{\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer implements Runnable, Serializable\n" +
            "  {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "  }\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer implements Runnable, Serializable\n" +
            "    {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "    }\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer implements Runnable, Serializable {\n\n" +
            "public void run() {\n" +
            "}\n" +
            "}\n";
        preferences.putBoolean("indentTopLevelClassMembers", false);
        reformat(doc, content, golden);
        preferences.putBoolean("indentTopLevelClassMembers", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer\n" +
            "        implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test\n" +
            "        extends Integer\n" +
            "        implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test\n" +
            "        extends Integer\n" +
            "        implements Runnable,\n" +
            "        Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test\n" +
            "        extends Integer\n" +
            "        implements Runnable,\n" +
            "                   Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("alignMultilineImplements", true);
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer implements Runnable,\n" +
            "                                      Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test extends Integer implements Runnable,\n" +
            "        Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";

        preferences.putBoolean("alignMultilineImplements", false);
        reformat(doc, content, golden);
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putInt("rightMargin", 120);
    }
    
    public void testEnum() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, " ");
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final int[] counter = new int[] {0};
        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putInt("rightMargin", 20);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                String name = "Test" + counter[0]++;
                List<Tree> members = new ArrayList<Tree>();
                ModifiersTree mods = maker.Modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL | Flags.ENUM, Collections.<AnnotationTree>emptyList());
                IdentifierTree type = maker.Identifier(name);
                List<ExpressionTree> empty = Collections.<ExpressionTree>emptyList();
                members.add(maker.Variable(mods, "NORTH", type, maker.NewClass(null, empty, type, empty, null)));
                members.add(maker.Variable(mods, "EAST", type, maker.NewClass(null, empty, type, empty, null)));
                members.add(maker.Variable(mods, "SOUTH", type, maker.NewClass(null, empty, type, empty, null)));
                members.add(maker.Variable(mods, "WEST", type, maker.NewClass(null, empty, type, empty, null)));
                ClassTree clazz = maker.Enum(maker.Modifiers(Collections.<Modifier>emptySet()), name, Collections.<Tree>emptyList(), members);
                if (counter[0] == 1)
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.CompilationUnit(maker.Identifier("hierbas.del.litoral"), Collections.<ImportTree>emptyList(), Collections.singletonList(clazz), workingCopy.getCompilationUnit().getSourceFile()));
                else
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.addCompUnitTypeDecl(workingCopy.getCompilationUnit(), clazz));
            }            
        };
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentTopLevelClassMembers", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentTopLevelClassMembers", true);

        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();
        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_NEVER.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "enum Test0 {\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "}\n\n" +
            "enum Test1{\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "}\n\n" +
            "enum Test2\n" +
            "{\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "}\n\n" +
            "enum Test3\n" +
            "  {\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "  }\n\n" +
            "enum Test4\n" +
            "    {\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "    }\n\n" +
            "enum Test5 {\n\n" +
            "NORTH, EAST, SOUTH, WEST\n" +
            "}\n\n" +
            "enum Test6 {\n\n" +
            "    NORTH, EAST,\n" +
            "    SOUTH, WEST\n" +
            "}\n\n" +
            "enum Test7 {\n\n" +
            "    NORTH,\n" +
            "    EAST,\n" +
            "    SOUTH,\n" +
            "    WEST\n" +
            "}\n";
        assertEquals(golden, res);

        String content =
            "package hierbas.del.litoral;" +
            "enum Test{" +
            "NORTH,EAST,SOUTH,WEST" +
            "}\n";
        golden =
            "package hierbas.del.litoral;\n\n" +
            "enum Test {\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "}\n";
        reformat(doc, content, golden);
        
        golden =
            "package hierbas.del.litoral;\n\n" +
            "enum Test{\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);
        
        golden =
            "package hierbas.del.litoral;\n\n" +
            "enum Test\n" +
            "{\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "}\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "enum Test\n" +
            "  {\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "  }\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "enum Test\n" +
            "    {\n\n" +
            "    NORTH, EAST, SOUTH, WEST\n" +
            "    }\n";
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
            "package hierbas.del.litoral;\n\n" +
            "enum Test {\n\n" +
            "NORTH, EAST, SOUTH, WEST\n" +
            "}\n";
        preferences.putBoolean("indentTopLevelClassMembers", false);
        reformat(doc, content, golden);
        preferences.putBoolean("indentTopLevelClassMembers", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "enum Test {\n\n" +
            "    NORTH, EAST,\n" +
            "    SOUTH, WEST\n" +
            "}\n";
        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "enum Test {\n\n" +
            "    NORTH,\n" +
            "    EAST,\n" +
            "    SOUTH,\n" +
            "    WEST\n" +
            "}\n";
        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        reformat(doc, content, golden);
        preferences.put("wrapEnumConstants", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putInt("rightMargin", 120);
     }
    
    public void testMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final int[] counter = new int[] {0};
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = maker.Modifiers(Collections.<Modifier>emptySet());
                MethodTree method = maker.Method(mods, "test" + counter[0]++, maker.Identifier("int"), Collections.<TypeParameterTree>emptyList(), Collections.singletonList(maker.Variable(mods, "i", maker.Identifier("int"), null)), Collections.<ExpressionTree>emptyList(), "{return i;}", null);
                workingCopy.rewrite(clazz, maker.addClassMember(clazz, method));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeMethodDeclParen", true);
        preferences.putBoolean("spaceWithinMethodDeclParens", true);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeMethodDeclParen", false);
        preferences.putBoolean("spaceWithinMethodDeclParens", false);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", true);

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    int test0(int i) {\n" +
            "        return i;\n" +
            "    }\n\n" +
            "    int test1 ( int i ){\n" +
            "        return i;\n" +
            "    }\n\n" +
            "    int test2(int i)\n" +
            "    {\n" +
            "        return i;\n" +
            "    }\n\n" +
            "    int test3(int i)\n" +
            "      {\n" +
            "        return i;\n" +
            "      }\n\n" +
            "    int test4(int i)\n" +
            "        {\n" +
            "        return i;\n" +
            "        }\n" +
            "}\n";
        assertEquals(golden, res);
        
        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "int test(int i){" +
            "return i;" +
            "}" +
            "}\n";

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    int test(int i) {\n" +
            "        return i;\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    int test ( int i ){\n" +
            "        return i;\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeMethodDeclParen", true);
        preferences.putBoolean("spaceWithinMethodDeclParens", true);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeMethodDeclParen", false);
        preferences.putBoolean("spaceWithinMethodDeclParens", false);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", true);

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    int test(int i)\n" +
            "    {\n" +
            "        return i;\n" +
            "    }\n" +
            "}\n";
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    int test(int i)\n" +
            "      {\n" +
            "        return i;\n" +
            "      }\n" +
            "}\n";
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    int test(int i)\n" +
            "        {\n" +
            "        return i;\n" +
            "        }\n" +
            "}\n";
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
    }
    
    public void testStaticBlock() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final int[] counter = new int[] {0};
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                BlockTree block = maker.Block(Collections.<StatementTree>emptyList(), true);
                workingCopy.rewrite(clazz, maker.addClassMember(clazz, block));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeStaticInitLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeStaticInitLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    static {\n" +
            "    }\n" +
            "    static{\n" +
            "    }\n" +
            "    static\n" +
            "    {\n" +
            "    }\n" +
            "    static\n" +
            "      {\n" +
            "      }\n" +
            "    static\n" +
            "        {\n" +
            "        }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "static{" +
            "}" +
            "}\n";
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    static {\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    static{\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeStaticInitLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeStaticInitLeftBrace", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    static\n" +
            "    {\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    static\n" +
            "      {\n" +
            "      }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    static\n" +
            "        {\n" +
            "        }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
    }
    
    public void testFor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "for (int i = 0; i < 10; i++) System.out.println(\"TRUE\");";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapForStatement", CodeStyle.WrapStyle.WRAP_NEVER.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
        preferences.put("wrapForStatement", CodeStyle.WrapStyle.WRAP_ALWAYS.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        for( int i = 0; i < 10; i++ ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        for (int i = 0; i < 10; i++) System.out.println(\"TRUE\");\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(){" +
            "for(int i=0;i<10;i++)" +
            "System.out.println(\"TRUE\");" +
            "}" +
            "}\n";
        
        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        for( int i = 0; i < 10; i++ ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            System.out.println(\"TRUE\");\n" +
            "    }\n" +
            "}\n";
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++) System.out.println(\"TRUE\");\n" +
            "    }\n" +
            "}\n";
        preferences.put("wrapForStatement", CodeStyle.WrapStyle.WRAP_NEVER.name());
        reformat(doc, content, golden);
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
        preferences.put("wrapForStatement", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
    }
    
    public void testForEach() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(String[] args) {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "for (String s : args) System.out.println(s);";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(String[] args) {\n" +
            "        for (String s : args) {\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "        for( String s : args ){\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "        for (String s : args)\n" +
            "        {\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "        for (String s : args)\n" +
            "          {\n" +
            "            System.out.println(s);\n" +
            "          }\n" +
            "        for (String s : args)\n" +
            "            {\n" +
            "            System.out.println(s);\n" +
            "            }\n" +
            "        for (String s : args)\n" +
            "            System.out.println(s);\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content =
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(String[] args){" +
            "for(String s:args)" +
            "System.out.println(s);" +
            "}" +
            "}\n";
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(String[] args) {\n" +
            "        for (String s : args) {\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(String[] args) {\n" +
            "        for( String s : args ){\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(String[] args) {\n" +
            "        for (String s : args)\n" +
            "        {\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(String[] args) {\n" +
            "        for (String s : args)\n" +
            "          {\n" +
            "            System.out.println(s);\n" +
            "          }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(String[] args) {\n" +
            "        for (String s : args)\n" +
            "            {\n" +
            "            System.out.println(s);\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(String[] args) {\n" +
            "        for (String s : args)\n" +
            "            System.out.println(s);\n" +
            "    }\n" +
            "}\n";
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
    }
    
    public void testIf() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "if (a) System.out.println(\"A\") else if (b) System.out.println(\"B\") else System.out.println(\"NONE\");";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeIfParen", false);
        preferences.putBoolean("spaceWithinIfParens", true);
        preferences.putBoolean("spaceBeforeIfLeftBrace", false);
        preferences.putBoolean("spaceBeforeElse", false);
        preferences.putBoolean("spaceBeforeElseLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeIfParen", true);
        preferences.putBoolean("spaceWithinIfParens", false);
        preferences.putBoolean("spaceBeforeIfLeftBrace", true);
        preferences.putBoolean("spaceBeforeElse", true);
        preferences.putBoolean("spaceBeforeElseLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        preferences.putBoolean("placeElseOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeElseOnNewLine", false);
        
        preferences.putBoolean("specialElseIf", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("specialElseIf", true);
        
        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else if (b) {\n" +
            "            System.out.println(\"B\");\n" +
            "        } else {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "        if( a ){\n" +
            "            System.out.println(\"A\");\n" +
            "        }else if( b ){\n" +
            "            System.out.println(\"B\");\n" +
            "        }else{\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "        if (a)\n" +
            "        {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else if (b)\n" +
            "        {\n" +
            "            System.out.println(\"B\");\n" +
            "        } else\n" +
            "        {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "        if (a)\n" +
            "          {\n" +
            "            System.out.println(\"A\");\n" +
            "          } else if (b)\n" +
            "          {\n" +
            "            System.out.println(\"B\");\n" +
            "          } else\n" +
            "          {\n" +
            "            System.out.println(\"NONE\");\n" +
            "          }\n" +
            "        if (a)\n" +
            "            {\n" +
            "            System.out.println(\"A\");\n" +
            "            } else if (b)\n" +
            "            {\n" +
            "            System.out.println(\"B\");\n" +
            "            } else\n" +
            "            {\n" +
            "            System.out.println(\"NONE\");\n" +
            "            }\n" +
            "        if (a)\n" +
            "            System.out.println(\"A\");\n" +
            "        else if (b)\n" +
            "            System.out.println(\"B\");\n" +
            "        else\n" +
            "            System.out.println(\"NONE\");\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        }\n" +
            "        else if (b) {\n" +
            "            System.out.println(\"B\");\n" +
            "        }\n" +
            "        else {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else {\n" +
            "            if (b) {\n" +
            "                System.out.println(\"B\");\n" +
            "            } else {\n" +
            "                System.out.println(\"NONE\");\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content =
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(boolean a,boolean b){" +
            "if(a)" +
            "System.out.println(\"A\");" +
            "else if(b)" +
            "System.out.println(\"B\");" +
            "else " +
            "System.out.println(\"NONE\");" +
            "}" +
            "}\n";

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else if (b) {\n" +
            "            System.out.println(\"B\");\n" +
            "        } else {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if( a ){\n" +
            "            System.out.println(\"A\");\n" +
            "        }else if( b ){\n" +
            "            System.out.println(\"B\");\n" +
            "        }else{\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeIfParen", false);
        preferences.putBoolean("spaceWithinIfParens", true);
        preferences.putBoolean("spaceBeforeIfLeftBrace", false);
        preferences.putBoolean("spaceBeforeElse", false);
        preferences.putBoolean("spaceBeforeElseLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeIfParen", true);
        preferences.putBoolean("spaceWithinIfParens", false);
        preferences.putBoolean("spaceBeforeIfLeftBrace", true);
        preferences.putBoolean("spaceBeforeElse", true);
        preferences.putBoolean("spaceBeforeElseLeftBrace", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a)\n" +
            "        {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else if (b)\n" +
            "        {\n" +
            "            System.out.println(\"B\");\n" +
            "        } else\n" +
            "        {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a)\n" +
            "          {\n" +
            "            System.out.println(\"A\");\n" +
            "          } else if (b)\n" +
            "          {\n" +
            "            System.out.println(\"B\");\n" +
            "          } else\n" +
            "          {\n" +
            "            System.out.println(\"NONE\");\n" +
            "          }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a)\n" +
            "            {\n" +
            "            System.out.println(\"A\");\n" +
            "            } else if (b)\n" +
            "            {\n" +
            "            System.out.println(\"B\");\n" +
            "            } else\n" +
            "            {\n" +
            "            System.out.println(\"NONE\");\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a)\n" +
            "            System.out.println(\"A\");\n" +
            "        else if (b)\n" +
            "            System.out.println(\"B\");\n" +
            "        else\n" +
            "            System.out.println(\"NONE\");\n" +
            "    }\n" +
            "}\n";
        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);
        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        }\n" +
            "        else if (b) {\n" +
            "            System.out.println(\"B\");\n" +
            "        }\n" +
            "        else {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("placeElseOnNewLine", true);
        reformat(doc, content, golden);
        preferences.putBoolean("placeElseOnNewLine", false);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else {\n" +
            "            if (b) {\n" +
            "                System.out.println(\"B\");\n" +
            "            } else {\n" +
            "                System.out.println(\"NONE\");\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("specialElseIf", false);
        reformat(doc, content, golden);
        preferences.putBoolean("specialElseIf", true);
    }
    
    public void testWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean b) {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "while (b) System.out.println(\"TRUE\");";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean b) {\n" +
            "        while (b) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while( b ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while (b)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while (b)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "        while (b)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "        while (b)\n" +
            "            System.out.println(\"TRUE\");\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(boolean b){" +
            "while(b)" +
            "System.out.println(\"TRUE\");" +
            "}" +
            "}\n";
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        while (b) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        while( b ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        while (b)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        while (b)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        while (b)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        while (b)\n" +
            "            System.out.println(\"TRUE\");\n" +
            "    }\n" +
            "}\n";
        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);
        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
    }
    
    public void testSwitch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int i) {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "switch (i) {case 0: System.out.println(i); break; default: System.out.println(\"DEFAULT\");}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentCasesFromSwitch", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentCasesFromSwitch", true);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int i) {\n" +
            "        switch (i) {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "        switch( i ){\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "        switch (i)\n" +
            "        {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "        switch (i)\n" +
            "          {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "          }\n" +
            "        switch (i)\n" +
            "            {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "            }\n" +
            "        switch (i) {\n" +
            "        case 0:\n" +
            "            System.out.println(i);\n" +
            "            break;\n" +
            "        default:\n" +
            "            System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(int i){" +
            "switch(i){" +
            "case 0:" +
            "System.out.println(i);" +
            "break;" +
            "default:" +
            "System.out.println(\"DEFAULT\");" +
            "}" +
            "}" +
            "}\n";
            
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int i) {\n" +
            "        switch (i) {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);
            
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int i) {\n" +
            "        switch( i ){\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);
            
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int i) {\n" +
            "        switch (i)\n" +
            "        {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);
            
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int i) {\n" +
            "        switch (i)\n" +
            "          {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "          }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);
            
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int i) {\n" +
            "        switch (i)\n" +
            "            {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
            
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int i) {\n" +
            "        switch (i) {\n" +
            "        case 0:\n" +
            "            System.out.println(i);\n" +
            "            break;\n" +
            "        default:\n" +
            "            System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("indentCasesFromSwitch", false);
        reformat(doc, content, golden);
        preferences.putBoolean("indentCasesFromSwitch", true);
    }
    
    public void testDoWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean b) {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "do System.out.println(\"TRUE\"); while (b);\n";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeDoLeftBrace", false);
        preferences.putBoolean("spaceBeforeWhile", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeDoLeftBrace", true);
        preferences.putBoolean("spaceBeforeWhile", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        preferences.putBoolean("placeWhileOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeWhileOnNewLine", false);
        
        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean b) {\n" +
            "        do {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        } while (b);\n" +
            "        do{\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }while( b );\n" +
            "        do\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        } while (b);\n" +
            "        do\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          } while (b);\n" +
            "        do\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            } while (b);\n" +
            "        do\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        while (b);\n" +
            "        do {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while (b);\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(boolean b){" +
            "do " +
            "System.out.println(\"TRUE\");" +
            "while(b);" +
            "}" +
            "}\n";
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        do {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        } while (b);\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        do{\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }while( b );\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeDoLeftBrace", false);
        preferences.putBoolean("spaceBeforeWhile", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeDoLeftBrace", true);
        preferences.putBoolean("spaceBeforeWhile", true);
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        do\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        } while (b);\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        do\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          } while (b);\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        do\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            } while (b);\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        do\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        while (b);\n" +
            "    }\n" +
            "}\n";
        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        reformat(doc, content, golden);
        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());
        
        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(boolean b) {\n" +
            "        do {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while (b);\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("placeWhileOnNewLine", true);
        reformat(doc, content, golden);
        preferences.putBoolean("placeWhileOnNewLine", false);
    }
    
    public void testSynchronized() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "synchronized (this) {System.out.println(\"TRUE\");}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeSynchronizedParen", false);
        preferences.putBoolean("spaceWithinSynchronizedParens", true);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeSynchronizedParen", true);
        preferences.putBoolean("spaceWithinSynchronizedParens", false);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        synchronized (this) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        synchronized( this ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        synchronized (this)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        synchronized (this)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "        synchronized (this)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content =
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(){" +
            "synchronized(this){" +
            "System.out.println(\"TRUE\");" +
            "}" +
            "}" +
            "}\n";

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        synchronized (this) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        synchronized( this ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeSynchronizedParen", false);
        preferences.putBoolean("spaceWithinSynchronizedParens", true);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeSynchronizedParen", true);
        preferences.putBoolean("spaceWithinSynchronizedParens", false);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        synchronized (this)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        synchronized (this)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        synchronized (this)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());
    }
    
    public void testTry() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "try {System.out.println(\"TEST\");} catch(Exception e) {System.out.println(\"CATCH\");} finally {System.out.println(\"FINALLY\");}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeCatchParen", false);
        preferences.putBoolean("spaceWithinCatchParens", true);
        preferences.putBoolean("spaceBeforeTryLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", false);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatch", false);
        preferences.putBoolean("spaceBeforeFinally", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeCatchParen", true);
        preferences.putBoolean("spaceWithinCatchParens", false);
        preferences.putBoolean("spaceBeforeTryLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", true);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatch", true);
        preferences.putBoolean("spaceBeforeFinally", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("placeCatchOnNewLine", true);
        preferences.putBoolean("placeFinallyOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeCatchOnNewLine", false);
        preferences.putBoolean("placeFinallyOnNewLine", false);
        
        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        } catch (Exception e) {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        } finally {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "        try{\n" +
            "            System.out.println(\"TEST\");\n" +
            "        }catch( Exception e ){\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        }finally{\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "        try\n" +
            "        {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        } catch (Exception e)\n" +
            "        {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        } finally\n" +
            "        {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "        try\n" +
            "          {\n" +
            "            System.out.println(\"TEST\");\n" +
            "          } catch (Exception e)\n" +
            "          {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "          } finally\n" +
            "          {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "          }\n" +
            "        try\n" +
            "            {\n" +
            "            System.out.println(\"TEST\");\n" +
            "            } catch (Exception e)\n" +
            "            {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "            } finally\n" +
            "            {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "            }\n" +
            "        try {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        }\n" +
            "        catch (Exception e) {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        }\n" +
            "        finally {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(){" +
            "try{" +
            "System.out.println(\"TEST\");" +
            "}catch(Exception e){" +
            "System.out.println(\"CATCH\");" +
            "}finally{" +
            "System.out.println(\"FINALLY\");" +
            "}" +
            "}" +
            "}\n";

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        } catch (Exception e) {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        } finally {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        try{\n" +
            "            System.out.println(\"TEST\");\n" +
            "        }catch( Exception e ){\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        }finally{\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceBeforeCatchParen", false);
        preferences.putBoolean("spaceWithinCatchParens", true);
        preferences.putBoolean("spaceBeforeTryLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", false);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatch", false);
        preferences.putBoolean("spaceBeforeFinally", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceBeforeCatchParen", true);
        preferences.putBoolean("spaceWithinCatchParens", false);
        preferences.putBoolean("spaceBeforeTryLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", true);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatch", true);
        preferences.putBoolean("spaceBeforeFinally", true);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        try\n" +
            "        {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        } catch (Exception e)\n" +
            "        {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        } finally\n" +
            "        {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        try\n" +
            "          {\n" +
            "            System.out.println(\"TEST\");\n" +
            "          } catch (Exception e)\n" +
            "          {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "          } finally\n" +
            "          {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "          }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        try\n" +
            "            {\n" +
            "            System.out.println(\"TEST\");\n" +
            "            } catch (Exception e)\n" +
            "            {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "            } finally\n" +
            "            {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        reformat(doc, content, golden);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        }\n" +
            "        catch (Exception e) {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        }\n" +
            "        finally {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("placeCatchOnNewLine", true);
        preferences.putBoolean("placeFinallyOnNewLine", true);
        reformat(doc, content, golden);
        preferences.putBoolean("placeCatchOnNewLine", false);
        preferences.putBoolean("placeFinallyOnNewLine", false);
    }
    
    public void testOperators() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int x, int y) {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "for (int i = 0; i < x; i++) y += (y ^ 123) << 2;";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceWithinParens", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceWithinParens", false);

        preferences.putBoolean("spaceAroundUnaryOps", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundUnaryOps", false);

        preferences.putBoolean("spaceAroundBinaryOps", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundBinaryOps", true);

        preferences.putBoolean("spaceAroundAssignOps", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundAssignOps", true);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int x, int y) {\n" +
            "        for (int i = 0; i < x; i++) {\n" +
            "            y += (y ^ 123) << 2;\n" +
            "        }\n" +
            "        for (int i = 0; i < x; i++) {\n" +
            "            y += ( y ^ 123 ) << 2;\n" +
            "        }\n" +
            "        for (int i = 0; i < x; i ++ ) {\n" +
            "            y += (y ^ 123) << 2;\n" +
            "        }\n" +
            "        for (int i = 0; i<x; i++) {\n" +
            "            y += (y^123)<<2;\n" +
            "        }\n" +
            "        for (int i=0; i < x; i++) {\n" +
            "            y+=(y ^ 123) << 2;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(int x, int y){" +
            "for(int i=0;i<x;i++)" +
            "y+=(y^123)<<2;" +
            "}" +
            "}\n";

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int x, int y) {\n" +
            "        for (int i = 0; i < x; i++) {\n" +
            "            y += (y ^ 123) << 2;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int x, int y) {\n" +
            "        for (int i = 0; i < x; i++) {\n" +
            "            y += ( y ^ 123 ) << 2;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceWithinParens", true);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceWithinParens", false);

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int x, int y) {\n" +
            "        for (int i = 0; i < x; i ++) {\n" +
            "            y += (y ^ 123) << 2;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceAroundUnaryOps", true);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceAroundUnaryOps", false);

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int x, int y) {\n" +
            "        for (int i = 0; i<x; i++) {\n" +
            "            y += (y^123)<<2;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceAroundBinaryOps", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceAroundBinaryOps", true);

        golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(int x, int y) {\n" +
            "        for (int i=0; i < x; i++) {\n" +
            "            y+=(y ^ 123) << 2;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceAroundAssignOps", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceAroundAssignOps", true);

    }
    
    public void testTypeCast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "if (cs instanceof String) {String s = (String)cs;}";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceWithinTypeCastParens", true);
        preferences.putBoolean("spaceAfterTypeCast", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceWithinTypeCastParens", false);
        preferences.putBoolean("spaceAfterTypeCast", true);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "        if (cs instanceof String) {\n" +
            "            String s = (String) cs;\n" +
            "        }\n" +
            "        if (cs instanceof String) {\n" +
            "            String s = ( String )cs;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content =
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(CharSequence cs){" +
            "if(cs instanceof String){" +
            "String s=(String)cs;" +
            "}" +
            "}" +
            "}\n";

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "        if (cs instanceof String) {\n" +
            "            String s = (String) cs;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "        if (cs instanceof String) {\n" +
            "            String s = ( String )cs;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("spaceWithinTypeCastParens", true);
        preferences.putBoolean("spaceAfterTypeCast", false);
        reformat(doc, content, golden);
        preferences.putBoolean("spaceWithinTypeCastParens", false);
        preferences.putBoolean("spaceAfterTypeCast", true);
    }
    
    public void testLabelled() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "    }\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        final String stmt = 
            "label: System.out.println();";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putInt("labelIndent", 4);
        testSource.runModificationTask(task).commit();
        preferences.putInt("labelIndent", 0);

        preferences.putBoolean("absoluteLabelIndent", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("absoluteLabelIndent", false);

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "        label:\n" +
            "        System.out.println();\n" +
            "        label:\n" +
            "            System.out.println();\n" +
            "label:  System.out.println();\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "public void taragui(CharSequence cs){" +
            "label:" +
            "System.out.println();" +
            "}" +
            "}\n";

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "        label:\n" +
            "        System.out.println();\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "        label:\n" +
            "            System.out.println();\n" +
            "    }\n" +
            "}\n";
        preferences.putInt("labelIndent", 4);
        reformat(doc, content, golden);
        preferences.putInt("labelIndent", 0);

        golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "label:  System.out.println();\n" +
            "    }\n" +
            "}\n";
        preferences.putBoolean("absoluteLabelIndent", true);
        reformat(doc, content, golden);
        preferences.putBoolean("absoluteLabelIndent", false);
    }
    
    /**
     * Do not put spaces to parenthesis when method declaration has no
     * parameters. The same rule should be applied to method invocation.
     * Regression test.
     * 
     * http://www.netbeans.org/issues/show_bug.cgi?id=116225
     */
    public void test116225() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        final Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource testSource = JavaSource.forDocument(doc);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = maker.Modifiers(Collections.<Modifier>emptySet());
                MethodTree method = maker.Method(
                        mods,
                        "test",
                        maker.Identifier("int"),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(), 
                        Collections.<ExpressionTree>emptyList(),
                        "{ System.err.println(i); System.err.println(); " +
                        " new ArrayList(); new ArrayList(i); return i; }",
                        null
                );
                workingCopy.rewrite(clazz, maker.addClassMember(clazz, method));
            }            
        };
        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceWithinMethodDeclParens", true);
        preferences.putBoolean("spaceWithinMethodCallParens", true);
        testSource.runModificationTask(task).commit();

        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    int test() {\n" +
            "        System.err.println( i );\n" +
            "        System.err.println();\n" +
            "        new ArrayList();\n" +
            "        new ArrayList( i );\n" +
            "        return i;\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);

        String content = 
            "package hierbas.del.litoral;" +
            "public class Test{" +
            "int test(){" +
            "System.err.println(i);" +
            "System.err.println();" +
            "new ArrayList();" +
            "new ArrayList(i);" +
            "return i;" +
            "}" +
            "}\n";

        golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    int test() {\n" +
            "        System.err.println( i );\n" +
            "        System.err.println();\n" +
            "        new ArrayList();\n" +
            "        new ArrayList( i );\n" +
            "        return i;\n" +
            "    }\n" +
            "}\n";
        reformat(doc, content, golden);
        preferences.putBoolean("spaceWithinMethodDeclParens", false);
        preferences.putBoolean("spaceWithinMethodCallParens", false);
    }
    
    private void reformat(Document doc, String content, String golden) throws Exception {
        doc.remove(0, doc.getLength());
        doc.insertString(0, content, null);
        
        Reformat reformat = Reformat.get(doc);
        reformat.lock();
        try {
            reformat.reformat(0, doc.getLength());
        } finally {
            reformat.unlock();
        }
        String res = doc.getText(0, doc.getLength());
        System.err.println(res);
        assertEquals(golden, res);        
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
