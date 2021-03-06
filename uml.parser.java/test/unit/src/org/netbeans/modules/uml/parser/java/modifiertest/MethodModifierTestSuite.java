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


package org.netbeans.modules.uml.parser.java.modifiertest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.AbstractInnerClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.AbstractClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.NestedFinalClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.FinalClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.PrivateNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.ProtectedNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.PublicNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.PublicClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.StaticNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.StrictfpNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.StrictfpClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.AbstractMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.FinalMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.NativeMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.PrivateMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.ProtectedMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.PublicMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.StaticMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.StrictfpMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.SynchronizedMethodTest;

public class MethodModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Method Modifier Tests");
		suite.addTest(new TestSuite(AbstractMethodTest.class));
		suite.addTest(new TestSuite(FinalMethodTest.class));
		suite.addTest(new TestSuite(NativeMethodTest.class));
		suite.addTest(new TestSuite(PrivateMethodTest.class));
		suite.addTest(new TestSuite(ProtectedMethodTest.class));
		suite.addTest(new TestSuite(PublicMethodTest.class));
		suite.addTest(new TestSuite(StaticMethodTest.class));
		suite.addTest(new TestSuite(StrictfpMethodTest.class));
		suite.addTest(new TestSuite(SynchronizedMethodTest.class));
		return suite;
	}
}
