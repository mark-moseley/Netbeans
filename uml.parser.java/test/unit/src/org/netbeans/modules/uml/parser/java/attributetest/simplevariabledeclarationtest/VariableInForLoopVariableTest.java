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


package org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableInForLoopVariableTest extends AbstractParserTestCase {

	final String fileName = "VariableInForLoopVariableTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Loop","Loop Initializer","Variable Definition","Modifiers","Type","Test Condition","LT Relational Expression","Identifier","Loop PostProcess","Expression List","Increment Post Unary Expression","Identifier","Body","Assignment Expression","Identifier"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"VariableInForLoopVariableTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"j",  "Name"} ,{"0",  "Integer Constant"} ,{"for",  "Keyword"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{";",  "Conditional Separator"} ,{"<",  "Operator"} ,{"j",  "Identifier"} ,{"10",  "Integer Constant"} ,{";",  "PostProcessor Separator"} ,{"++",  "Operator"} ,{"j",  "Identifier"} ,{"{",  "Body Start"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"10",  "Integer Constant"} ,{"}",  "Body End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(VariableInForLoopVariableTest.class);
	}

	public void testVariableInForLoopVariable() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
