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

package org.netbeans.modules.gsf.testrunner.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single test suite.
 *
 * @author Erno Mononen
 */
public final class TestSuite {

    public static final String ANONYMOUS_SUITE = new String();

    /**
     * The name of this suite.
     */
    private final String name;
    /**
     * The test cases that this suite contains.
     */
    private final List<Testcase> testcases = new ArrayList<Testcase>();

    /**
     * Constructs a new TestSuite.
     * 
     * @param name the name for the suite, e.g. WhatEverTest. May be null.
     */
    public TestSuite(String name) {
        this.name = name;
    }

    void addTestcase(Testcase testcase) {
        testcases.add(testcase);
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }

    /**
     * @return the name of this suite, may return <code>null</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the last test case of this suite or <code>null</code> if 
     * the suite contains no test cases.
     */
    public Testcase getLastTestCase() {
        return testcases.isEmpty() ? null : testcases.get(testcases.size() -1);
    }

}
