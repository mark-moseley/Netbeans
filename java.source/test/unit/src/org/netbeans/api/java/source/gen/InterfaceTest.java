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
 * "Portions Copyrighted [2007] [Sun Microsystems, Inc]"
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jan Pokorsky
 */
public class InterfaceTest extends GeneratorTestMDRCompat {

    public InterfaceTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(InterfaceTest.class);
//        suite.addTest(new InterfaceTest("testAddField"));
        return suite;
    }
    
    // issue #100796
    public void testAddField() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public interface Test {\n" +
            "    public static final int CONSTANT = 0;\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        src.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                Tree vt = make.Variable(
                        make.Modifiers(EnumSet.<Modifier>of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)),
                        "CONSTANT",
                        make.PrimitiveType(TypeKind.INT),
                        make.Identifier("0")
                        );
                wc.rewrite(ct, make.addClassMember(ct, vt));
            }
        }).commit();
        
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

}
