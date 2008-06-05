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
package org.netbeans.test.java.editor.codegeneration;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.editor.jelly.GenerateCodeOperator;
import org.netbeans.test.java.editor.jelly.GenerateEqualsAndHashCodeOperator;

/**
 *
 * @author Jiri Prox
 */
public class CreateEqualsHashcodeTest extends GenerateCodeTestCase {

    public CreateEqualsHashcodeTest(String testMethodName) {
        super(testMethodName);
    }

    public void testEqualsOnly() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testEqualsHascode");
        editor = new EditorOperator("testEqualsHascode");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(14, 5);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_EQUALS_HASHCODE, editor);
            GenerateEqualsAndHashCodeOperator geahco = new GenerateEqualsAndHashCodeOperator();
            JTreeOperator jto = geahco.equalsTreeOperator();
            jto.selectRow(1);
            jto.selectRow(2);
            geahco.generate();
            String expected = "" +
                    "    @Override\n" +
                    "    public boolean equals(Object obj) {\n" +
                    "        if (obj == null) {\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        if (getClass() != obj.getClass()) {\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        final testEqualsHashcode other = (testEqualsHashcode) obj;\n" +
                    "        if (this.a != other.a && (this.a == null || !this.a.equals(other.a))) {\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        if (this.c != other.c && (this.c == null || !this.c.equals(other.c))) {\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        return true;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public int hashCode() {\n" +
                    "        int hash = 5;\n" +
                    "        return hash;\n" +
                    "    }";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }

    public static void main(String[] args) {
        TestRunner.run(CreateEqualsHashcodeTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreateEqualsHashcodeTest.class).enableModules(".*").clusters(".*"));
    }
}
