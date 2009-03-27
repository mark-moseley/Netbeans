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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import junit.framework.AssertionFailedError;

/**
 *
 * @author eu155513
 */
public class InstantiationHyperlinkTestCase extends HyperlinkBaseTestCase {
    public InstantiationHyperlinkTestCase(String testName) {
        super(testName);
    }

    public void test159679() throws Exception {
        // IZ159679: regression on Boost: resolver prefers global variable to local typedef
        performTest("iz159679.cpp", 12, 25, "iz159679.cpp", 11, 5);
        performTest("iz159679.cpp", 12, 35, "iz159679.cpp", 3, 5);
        performTest("iz159679.cpp", 12, 40, "iz159679.cpp", 6, 5);
        performTest("iz159679.cpp", 12, 45, "iz159679.cpp", 12, 5);
    }

    public void test154777() throws Exception {
        // IZ154777: Unresolved inner type of specialization
        performTest("iz154777.cpp", 16, 19, "iz154777.cpp", 10, 9); // DD in CC<int>::DD::dType j;
        performTest("iz154777.cpp", 16, 24, "iz154777.cpp", 11, 13); // dType in CC<int>::DD::dType j;
        performTest("iz154777.cpp", 17, 15, "iz154777.cpp", 3, 9); // method in j.method();
    }

    public void testClassForward() throws Exception {
        // IZ144869 : fixed instantiation of class forward declaration
        performTest("classForward.h", 21, 12, "classForward.h", 16, 5);
    }

    public void testCyclicTypedef() throws Exception {
        // IZ148453 : Highlighting thread hangs on boost
        try {
            performTest("cyclic_typedef.cc", 25, 66, "cyclic_typedef.cc", 25, 66);
        } catch (AssertionFailedError e) {
            // it's ok: it won't find: it just shouldn't hang
        }
    }
    
    public void testGccVector() throws Exception {
        // IZ144869 : fixed instantiation of class forward declaration
        performTest("iz146697.cc", 41, 20, "iz146697.cc", 34, 5);
    }

    public void test153986() throws Exception {
        // MYSTL case of IZ#153986: code completion of iterators and of the [] operator
        performTest("iz153986.cc", 18, 15, "iz153986.cc", 9, 9);
        performTest("iz153986.cc", 18, 30, "iz153986.cc", 4, 9);
    }

    public void test159068() throws Exception {
        // IZ#159068 : Unresolved ids in instantiations after &
        performTest("iz159068.cc", 4, 27, "iz159068.cc", 2, 5);
    }

    public void test159054() throws Exception {
        // IZ#159054 : Unresolved id in case of reference to template as return type
        performTest("iz159054.cc", 9, 17, "iz159054.cc", 3, 5);
        performTest("iz159054.cc", 15, 17, "iz159054.cc", 4, 5);
    }

    public void test151194() throws Exception {
        // IZ#151194 : Unresolved template instantiation with template as parameter
        performTest("iz151194.cpp", 32, 12, "iz151194.cpp", 3, 5);
    }

    public void test154792() throws Exception {
        // IZ#154792 : Completion fails on question mark
        performTest("iz154792.cpp", 6, 34, "iz154792.cpp", 2, 5);
    }

    public void test151619() throws Exception {
        // IZ#151619 : completion parser fails on complex template instantiation
        performTest("iz151619.cpp", 6, 14, "iz151619.cpp", 3, 5);
        performTest("iz151619.cpp", 7, 14, "iz151619.cpp", 3, 5);
    }

    public void test147518() throws Exception {
        // IZ#147518 : Code completion issue with template specialization
        performTest("iz147518.cpp", 61, 21, "iz147518.cpp", 41, 5);
        performTest("iz147518.cpp", 49, 17, "iz147518.cpp", 7, 5);
        performTest("iz147518.cpp", 50, 17, "iz147518.cpp", 7, 5);
        performTest("iz147518.cpp", 53, 17, "iz147518.cpp", 25, 5);
        performTest("iz147518.cpp", 54, 17, "iz147518.cpp", 25, 5);
        performTest("iz147518.cpp", 57, 17, "iz147518.cpp", 16, 5);
        performTest("iz147518.cpp", 58, 17, "iz147518.cpp", 16, 5);
    }

    public void test144869() throws Exception {
        // IZ#144869 : pair members are not resolved when accessed via iterator
        performTest("iz144869.cpp", 282, 10, "iz144869.cpp", 81, 7);
        performTest("iz144869.cpp", 283, 10, "iz144869.cpp", 82, 7);
        performTest("iz144869.cpp", 288, 10, "iz144869.cpp", 81, 7);
        performTest("iz144869.cpp", 289, 10, "iz144869.cpp", 82, 7);
        performTest("iz144869.cpp", 294, 10, "iz144869.cpp", 81, 7);
        performTest("iz144869.cpp", 295, 10, "iz144869.cpp", 82, 7);
    }

    public void test144869_2() throws Exception {
        // IZ#144869 : pair members are not resolved when accessed via iterator
        performTest("iz144869_2.cpp", 32, 7, "iz144869_2.cpp", 6, 5);
        performTest("iz144869_2.cpp", 35, 8, "iz144869_2.cpp", 11, 5);
        performTest("iz144869_2.cpp", 38, 8, "iz144869_2.cpp", 16, 5);
        performTest("iz144869_2.cpp", 41, 8, "iz144869_2.cpp", 26, 5);
    }
}
