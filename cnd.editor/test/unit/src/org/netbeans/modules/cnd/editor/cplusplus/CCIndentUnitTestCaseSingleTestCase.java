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

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;

/**
 * Class was taken from java
 * Links point to java IZ.
 * C/C++ specific tests begin from testReformatSimpleClass
 *
 * @author Alexander Simon
 */
public class CCIndentUnitTestCaseSingleTestCase extends CCFormatterBaseUnitTestCase {

    public CCIndentUnitTestCaseSingleTestCase(String testMethodName) {
        super(testMethodName);
    }
    /**
     * test parameter aligning
     */
    public void testIdentMethodParameters2() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                putBoolean(EditorOptions.alignMultilineMethodParams, true);
        setLoadDocumentText(
            "int longmain(int a,|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "int longmain(int a,\n" +
            "             \n");
    }

}
