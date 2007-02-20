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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import java.io.*;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Comment.Style;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.query.CommentHandler;
import org.netbeans.api.java.source.query.Query;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Pavel Flaska
 */
public class CommentsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of CommentsTest */
    public CommentsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CommentsTest.class);
  //      suite.addTest(new CommentsTest("testAddStatement"));
        return suite;
    }

    public void testAddStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n\n" +
            "    void method() {\n" +
            "    }\n\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n\n" +
            "    void method() {" +
            "        // test\n" +
            "        int a;\n\n" +
            "        /**\n" +
            "         * becko\n" +
            "         */\n" +
            "        int b;\n" +
            "        // cecko\n" +
            "        int c;\n\n" +
            "    }\n\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{ // test\nint a;\n/** becko */\nint b;\n/* cecko */\nint c;}";
                BlockTree block = make.createMethodBody(method, bodyText);
                CommentHandler comments = workingCopy.getCommentHandler();
                mapComments(block, bodyText, workingCopy, comments);
                workingCopy.rewrite(method.getBody(), block);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    private void mapComments(BlockTree block, String inputText, WorkingCopy copy, CommentHandler comments) {
        final EnumSet<JavaTokenId> nonRelevant = EnumSet.of(
                LINE_COMMENT,
                BLOCK_COMMENT,
                JAVADOC_COMMENT,
                WHITESPACE
            );
        TokenSequence<JavaTokenId> seq = TokenHierarchy.create(inputText, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        List<? extends StatementTree> trees = block.getStatements();
        SourcePositions pos = copy.getTrees().getSourcePositions();
        for (StatementTree statement : trees) {
            seq.move((int) pos.getStartPosition(null, statement));
            seq.moveNext();
            while (seq.movePrevious() && nonRelevant.contains(seq.token().id())) {
                switch (seq.token().id()) {
                    case LINE_COMMENT:
                        comments.addComment(statement, Comment.create(Style.LINE, Query.NOPOS, Query.NOPOS, Query.NOPOS, seq.token().toString()));
                        break;
                    case BLOCK_COMMENT:
                        comments.addComment(statement, Comment.create(Style.BLOCK, Query.NOPOS, Query.NOPOS, Query.NOPOS, seq.token().toString()));
                        break;
                    case JAVADOC_COMMENT:
                        comments.addComment(statement, Comment.create(Style.JAVADOC, Query.NOPOS, Query.NOPOS, Query.NOPOS, seq.token().toString()));
                        break;
                }
            }
        }
    }
    
}
