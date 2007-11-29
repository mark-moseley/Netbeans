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
public class OrigSurroundWithTryCatchFixTest extends ErrorHintsTestBase {
    
    public OrigSurroundWithTryCatchFixTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MagicSurroundWithTryCatchFix.DISABLE_JAVA_UTIL_LOGGER = true;
    }
    
    public void testTryWrapper1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\"));\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        try {\n" +
                       "            FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTryWrapper2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\"));\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper3() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        {\n" +
                       "            FileInputStream fis = |new FileInputStream(new File(\"\"));\n" +
                       "        }\n" +
                       "        {\n" +
                       "            FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                       "            fis.read();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                        "        {\n" +
                        "            try {\n" +
                        "                FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                        "            } catch (FileNotFoundException ex) {\n" +
                        "                ex.printStackTrace();\n" +
                        "            }\n" +
                        "        }\n" +
                        "        {\n" +
                        "            FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                        "            fis.read();\n" +
                        "        }\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper4() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream a,b,c,fis = |new FileInputStream(new File(\"\"));\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream a,b,c,fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper5() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream a,b,c,fis = |new FileInputStream(new File(\"\")),d,e,f;\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream a,b,c,fis,d,e,f;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper6() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\")),a,b,c;\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis,a,b,c;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void XtestTryWrapper7() throws Exception {} //TODO: see the original test
    
    public void testTryWrapper8() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis;\n" +
                       "        fis = |new FileInputStream(new File(\"\"));\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper9() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.IOException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        |throw new IOException();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.IOException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        try {\n" +
                       "            throw new IOException();\n" +
                       "        } catch (IOException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new UncaughtException().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof OrigSurroundWithTryCatchFix) {
            return "FixImpl";
        }
        
        return super.toDebugString(info, f);
    }

}
