/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.grammar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.api.model.HintContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test functionality of AntGrammar.
 * @author Jesse Glick
 */
public class AntGrammarTest extends NbTestCase {
    
    static {
        AntGrammarTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public AntGrammarTest(String name) {
        super(name);
    }
    
    private AntGrammar g;
    
    protected void setUp() throws Exception {
        super.setUp();
        g = new AntGrammar();
    }
    protected void tearDown() throws Exception {
        g = null;
        super.tearDown();
    }
    
    public void testTypeOf() throws Exception {
        String simpleProject = "<project default='all'><target name='all'/></project>";
        Element e = TestUtil.createElementInDocument(simpleProject, "project", null);
        String[] type = AntGrammar.typeOf(e);
        assertEquals("is special", AntGrammar.KIND_SPECIAL, type[0]);
        assertEquals("is project", AntGrammar.SPECIAL_PROJECT, type[1]);
        // XXX other specials...
        String projectWithTasks = "<project default='all'><target name='all'><echo>hello</echo></target></project>";
        e = TestUtil.createElementInDocument(projectWithTasks, "echo", null);
        type = AntGrammar.typeOf(e);
        assertEquals("is task", AntGrammar.KIND_TASK, type[0]);
        assertEquals("is <echo>", "org.apache.tools.ant.taskdefs.Echo", type[1]);
        String projectWithTypes = "<project default='all'><path id='foo'/><target name='all'/></project>";
        e = TestUtil.createElementInDocument(projectWithTypes, "path", null);
        type = AntGrammar.typeOf(e);
        assertEquals("is type", AntGrammar.KIND_TYPE, type[0]);
        assertEquals("is <path>", "org.apache.tools.ant.types.Path", type[1]);
        // XXX more...
    }
    
    public void testTaskCompletion() throws Exception {
        String p = "<project default='x'><target name='x'><ecHERE/></target></project>";
        HintContext c = TestUtil.createCompletion(p);
        List l = TestUtil.elementNames(g.queryElements(c));
        assertTrue("matched <echo>", l.contains("echo"));
        // XXX more...
    }
    
}
