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

package org.netbeans.modules.project.ui.actions;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.AssertionFailedError;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Lahoda
 */
public class ActionsUtilTest extends NbTestCase {
    
    public ActionsUtilTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    //due to use of weak references in ActionsUtil.LookupResultsCache this test fails
    //(there are cache misses after GC, and there may be even cache miss during the
    //first "assertTrue(result == ActionsUtil.getProjectsFromLookup(projects, null))"
    //statement.
//    public void testCacheWorks() throws Exception {
//        Project prj1 = new DummyProject();
//        Project prj2 = new DummyProject();
//        Lookup projects = Lookups.fixed(new Object[] {
//            prj1, prj2,
//        });
//        
//        Project[] result = ActionsUtil.getProjectsFromLookup(projects, null);
//        
//        assertTrue(result == ActionsUtil.getProjectsFromLookup(projects, null));
//        
//        //make sure the cache lives as long as the projects lookup:
//        //try hard to force gc:
//        for (int i = 0; i < 5; i++) {
//            System.gc();
//            System.runFinalization();
//        }
//        
//        assertTrue(result == ActionsUtil.getProjectsFromLookup(projects, null));
//    }
    
    private static final Object o = new Object();
    
    public void testCacheUpdatesCorrectly() throws Exception {
        Project prj1 = new DummyProject();
        Project prj2 = new DummyProject();
        TestProxyLookup projects = new TestProxyLookup(new Lookup[] {
            prj1.getLookup(),
            prj2.getLookup(),
        });
        
        Set bothProjects = new HashSet(Arrays.asList(new Object[] {prj1, prj2}));
        Set result = new HashSet(Arrays.asList(ActionsUtil.getProjectsFromLookup(projects, null)));
        
        assertTrue(bothProjects.equals(result));
        
        //make sure cache is somehow updated even after hard GC:
        //and try really hard to reclaim even (potential) SoftReferences:
        boolean wasThrown = false;
        
        try {
            assertGC("", new WeakReference(o));
        } catch (AssertionFailedError e) {
            //ignore
            wasThrown = true;
        }
        
        assertTrue(wasThrown);
        
        projects.setLookupsOverride(new Lookup[] {prj1.getLookup()});
        
        Set firstProject = new HashSet(Arrays.asList(new Object[] {prj1}));
        
        result = new HashSet(Arrays.asList(ActionsUtil.getProjectsFromLookup(projects, null)));
        
        assertTrue(firstProject.equals(result));
        
        projects.setLookupsOverride(new Lookup[] {});
        
        result = new HashSet(Arrays.asList(ActionsUtil.getProjectsFromLookup(projects, null)));
        
        assertTrue(Collections.EMPTY_SET.equals(result));
        
        projects.setLookupsOverride(new Lookup[] {prj1.getLookup(), prj2.getLookup()});

        result = new HashSet(Arrays.asList(ActionsUtil.getProjectsFromLookup(projects, null)));
        
        assertTrue(bothProjects.equals(result));
    }
    
    public void testCanBeReclaimed() throws Exception {
        Project prj1 = new DummyProject();
        Project prj2 = new DummyProject();
        Lookup projects = new TestProxyLookup(new Lookup[] {
            prj1.getLookup(),
            prj2.getLookup(),
        });
        
        ActionsUtil.getProjectsFromLookup(projects, null);
        
        WeakReference ref1 = new WeakReference(prj1);
        WeakReference ref2 = new WeakReference(prj2);
        WeakReference lookup = new WeakReference(projects);
        
        prj1 = null;
        prj2 = null;
        projects = null;
        
        assertGC("the projects can be reclaimed", ref1);
        assertGC("the projects can be reclaimed", ref2);
        assertGC("the lookup can be reclaimed", lookup);
    }
    
    public void testCanBeReclaimedWithSimpleLookup() throws Exception {
        Project prj1 = new DummyProject();
        Project prj2 = new DummyProject();
        Lookup projects = Lookups.fixed(new Object[] {
            prj1,
            prj2,
        });
        
        ActionsUtil.getProjectsFromLookup(projects, null);
        
        WeakReference ref1 = new WeakReference(prj1);
        WeakReference ref2 = new WeakReference(prj2);
        WeakReference lookup = new WeakReference(projects);
        
        prj1 = null;
        prj2 = null;
        projects = null;
        
        assertGC("the projects can be reclaimed", ref1);
        assertGC("the projects can be reclaimed", ref2);
        assertGC("the lookup can be reclaimed", lookup);
    }
    
    private static final class TestProxyLookup extends ProxyLookup {
        
        public TestProxyLookup(Lookup[] lookups) {
            super(lookups);
        }
        
        public void setLookupsOverride(Lookup[] lookups) {
            setLookups(lookups);
        }
        
    }
    
    private static final class DummyProject implements Project {
        
        private final Lookup lookup = Lookups.singleton(this);
        
        public FileObject getProjectDirectory() {
            return null;
        }
        
        public Lookup getLookup() {
            return lookup;
        }
        
    }
    
}
