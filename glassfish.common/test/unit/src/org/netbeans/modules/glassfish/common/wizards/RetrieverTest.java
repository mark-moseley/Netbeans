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

package org.netbeans.modules.glassfish.common.wizards;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vkraemer
 */
public class RetrieverTest {

    public RetrieverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getDurationString method, of class Retriever.
     */
    @Test
    public void testGetDurationString() {
        System.out.println("getDurationString");
        int time = 0;
        String expResult = "no time at all";
        String result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = -50;
        expResult = "an eternity";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 7;
        expResult = "7 ms";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 77;
        expResult = "77 ms";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 777;
        expResult = "777 ms";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 7777;
        expResult = "8 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 77777;
        expResult = "1 minute, 18 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 60000;
        expResult = "1 minute";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 61001;
        expResult = "1 minute, 1 second";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 777777;
        expResult = "12 minutes, 58 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 7777777;
        expResult = "2 hours, 9 minutes, 38 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 77777777;
        expResult = "21 hours, 36 minutes, 18 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 777777777;
        expResult = "216 hours, 2 minutes, 58 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
    }

}