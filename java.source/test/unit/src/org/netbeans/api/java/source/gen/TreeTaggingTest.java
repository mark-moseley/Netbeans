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

package org.netbeans.api.java.source.gen;

import java.io.File;
import java.util.Collections;
import com.sun.source.tree.*;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileUtil;

/**
 * Unit tests for Tree tagging / span suport
 *
 * @author Max Sauer
 */
public class TreeTaggingTest extends GeneratorTestMDRCompat {

    public TreeTaggingTest(String name) {
        super(name);
    }

    /**
     * Adds 'System.err.println(true);' statement to the method body,
     * tags the tree and checks the marks are valid
     */
    public void testTaggingOfGeneratedMethodBody() throws Exception {

        // the tag
        final String methodBodyTag = "mbody"; //NOI18N

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    }\n" +
            "}\n"
            );

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree statement = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "err"
                            ),
                            "println"
                        ),
                        Collections.singletonList(
                            make.Literal(Boolean.TRUE)
                        )
                    )
                );
                //tag
                workingCopy.tag(statement, methodBodyTag);

                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }

        };
        ModificationResult diff = testSource.runModificationTask(task);
        diff.commit();
        int[] span = diff.getSpan(methodBodyTag);
        int delta = span[1] - span[0];
        //lenghth of added statement has to be the same as the length of span
        assertEquals(delta, new String("System.err.println(true);").length());
        //absolute position of span beginning
        assertEquals(span[0], 119);
    }

    @Override
    String getGoldenPckg() {
        return "";
    }

    @Override
    String getSourcePckg() {
        return "";
    }
}
