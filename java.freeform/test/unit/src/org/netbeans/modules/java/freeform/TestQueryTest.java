/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Check that the unit test query works.
 * @author Jesse Glick
 */
public class TestQueryTest extends TestBase {
    
    public TestQueryTest(String name) {
        super(name);
    }
    
    private FileObject src1, src1a, src2, test1, test2;
    
    protected void setUp() throws Exception {
        super.setUp();
        src1 = simple2.getProjectDirectory().getFileObject("src1");
        assertNotNull("have src1", src1);
        src1a = simple2.getProjectDirectory().getFileObject("src1a");
        assertNotNull("have src1a", src1a);
        src2 = simple2.getProjectDirectory().getFileObject("src2");
        assertNotNull("have src2", src2);
        test1 = simple2.getProjectDirectory().getFileObject("test1");
        assertNotNull("have test1", test1);
        test2 = simple2.getProjectDirectory().getFileObject("test2");
        assertNotNull("have test2", test2);
    }
    
    public void testFindUnitTests() throws Exception {
        URL[] tests = new URL[] {
            test1.getURL(),
            test2.getURL(),
        };
        assertEquals("correct tests for src1", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src1)));
        assertEquals("correct tests for src1a", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src1a)));
        assertEquals("correct tests for src2", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src2)));
        assertEquals("no tests for test1", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findUnitTests(test1)));
    }
    
    public void testFindSources() throws Exception {
        URL[] sources = new URL[] {
            src1.getURL(),
            src1a.getURL(),
            src2.getURL(),
        };
        assertEquals("correct sources for test1", Arrays.asList(sources), Arrays.asList(UnitTestForSourceQuery.findSources(test1)));
        assertEquals("correct sources for test2", Arrays.asList(sources), Arrays.asList(UnitTestForSourceQuery.findSources(test2)));
        assertEquals("no sources for src1", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findSources(src1)));
    }

}
