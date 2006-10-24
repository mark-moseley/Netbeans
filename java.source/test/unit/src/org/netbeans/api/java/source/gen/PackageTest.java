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

import com.sun.source.tree.CompilationUnitTree;
import java.io.File;
import org.netbeans.jackpot.test.TestUtilities;
import org.netbeans.jackpot.transform.Transformer;

/**
 * Test packages.
 * 
 * @author Pavel Flaska
 */
public class PackageTest extends GeneratorTest {
    
    /** Creates a new instance of PackageTest */
    public PackageTest(String testName) {
        super(testName);
    }

    /**
     * Change package declaration 'package org.nothing;' to
     * 'package com.unit;'.
     */
    public void testChangePackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package org.nothing;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package com.unit;\n\n" +
            "class Test {\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.CompilationUnit(
                        make.Identifier("com.unit"),
                        node.getImports(),
                        node.getTypeDecls(),
                        node.getSourceFile()
                    );
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );  
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeToDefPackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package org.nothing;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "\n\n" +
            "class Test {\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.CompilationUnit(
                        null,
                        node.getImports(),
                        node.getTypeDecls(),
                        node.getSourceFile()
                    );
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );  
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeDefToNamedPackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package gro.snaebten.seludom.avaj;\n\n" +
            "class Test {\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.CompilationUnit(
                        make.Identifier("gro.snaebten.seludom.avaj"),
                        node.getImports(),
                        node.getTypeDecls(),
                        node.getSourceFile()
                    );
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );  
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeDefToNamedPackageWithImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "import gro;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package gro.snaebten.seludom.avaj;\n\n" +
            "import gro;\n\n" +
            "class Test {\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    super.visitCompilationUnit(node, p);
                    CompilationUnitTree copy = make.CompilationUnit(
                        make.Identifier("gro.snaebten.seludom.avaj"),
                        node.getImports(),
                        node.getTypeDecls(),
                        node.getSourceFile()
                    );
                    changes.rewrite(node, copy);
                    return null;
                }
            }
        );  
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "Test.java");
    }

    // not important for this test
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    
}
