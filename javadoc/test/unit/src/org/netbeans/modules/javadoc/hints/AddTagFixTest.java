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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javadoc.hints;

import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jan Pokorsky
 */
public class AddTagFixTest extends JavadocTestSupport {
    
    public AddTagFixTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AddTagFixTest.class);
//        suite.addTest(new AddTagFixTest("testAddParamTagFixWithReturn_115974"));
        return suite;
    }

    public void testAddReturnTagFixInEmptyJavadoc() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @return \n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");        
    }

    public void testAddReturnTagFix() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * bla\n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * bla\n" +
                "     * @return \n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");        
    }

    public void testAddReturnTagFix2() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /** bla\n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /** bla\n" +
                "     * @return \n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");        
    }

    public void testAddReturnTagFixInEmpty1LineJavadoc() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /***/\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @return \n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");
    }

    public void testAddReturnTagFixIn1LineJavadoc() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /** bla */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /** bla\n" +
                "     * @return \n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");
    }

    public void testAddReturnTagFixIn1LineJavadoc2() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /** @since 1.1 */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @return \n" +
                "     * @since 1.1 */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");
    }

    public void testAddReturnTagFixIn1LineJavadoc3() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /** bla {@link nekam} */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /** bla {@link nekam}\n" +
                "     * @return \n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");
    }

    public void testAddParamTagFixInEmptyJavadoc() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden(int prvniho) {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param prvniho \n" +
                "     */\n" +
                "    void leden(int prvniho) {\n" +
                "    }\n" +
                "}\n");        
    }

    public void testAddParamTagFixWithReturn() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @return bla\n" +
                "     */\n" +
                "    int leden(int prvniho) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param prvniho \n" +
                "     * @return bla\n" +
                "     */\n" +
                "    int leden(int prvniho) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");        
    }

    public void testAddParamTagFixWithReturn_115974() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @return bla */\n" +
                "    int leden(int prvniho) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param prvniho \n" +
                "     * @return bla */\n" +
                "    int leden(int prvniho) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");        
    }

    public void testAddParamTagFixAndParamOrder() throws Exception {
        doFirstMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param prvniho \n" +
                "     * @param tretiho \n" +
                "     * @return bla\n" +
                "     */\n" +
                "    int leden(int prvniho, int druheho, int tretiho) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param prvniho \n" +
                "     * @param druheho \n" +
                "     * @param tretiho\n" +
                "     * @return bla\n" +
                "     */\n" +
                "    int leden(int prvniho, int druheho, int tretiho) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");        
    }
    
}
