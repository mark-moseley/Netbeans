/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.classpath;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

// XXX need test for findResource

/**
 * Test functionality of GlobalPathRegistry.
 * @author Jesse Glick
 */
public class GlobalPathRegistryTest extends NbTestCase {
    
    public GlobalPathRegistryTest(String name) {
        super(name);
        TestUtil.setLookup(
            new ProxyLookup (new Lookup[] {
                Lookups.fixed (new Object[] {new SFBQImpl()}),
                Lookups.metaInfServices(Thread.currentThread().getContextClassLoader()),
            }));
    }
    
    private GlobalPathRegistry r;
    private ClassPath cp1, cp2, cp3, cp4, cp5;
    protected void setUp() throws Exception {
        super.setUp();
        r = GlobalPathRegistry.getDefault();
        r.clear();
        clearWorkDir();
        FileObject root = FileUtil.toFileObject(getWorkDir());
        cp1 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("1")});
        cp2 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("2")});
        cp3 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("3")});
        cp4 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("4")});
        cp5 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("5")});
    }
    
    public void testBasicOperation() throws Exception {
        assertEquals("initially no paths of type a", Collections.EMPTY_SET, r.getPaths("a"));
        r.register("a", new ClassPath[] {cp1, cp2});
        assertEquals("added some paths of type a", new HashSet(Arrays.asList(new ClassPath[] {cp1, cp2})), r.getPaths("a"));
        r.register("a", new ClassPath[0]);
        assertEquals("did not add any new paths to a", new HashSet(Arrays.asList(new ClassPath[] {cp1, cp2})), r.getPaths("a"));
        assertEquals("initially no paths of type b", Collections.EMPTY_SET, r.getPaths("b"));
        r.register("b", new ClassPath[] {cp3, cp4, cp5});
        assertEquals("added some paths of type b", new HashSet(Arrays.asList(new ClassPath[] {cp3, cp4, cp5})), r.getPaths("b"));
        r.unregister("a", new ClassPath[] {cp1});
        assertEquals("only one path left of type a", Collections.singleton(cp2), r.getPaths("a"));
        r.register("a", new ClassPath[] {cp2, cp3});
        assertEquals("only one new path added of type a", new HashSet(Arrays.asList(new ClassPath[] {cp2, cp3})), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp2});
        assertEquals("still have extra cp2 in a", new HashSet(Arrays.asList(new ClassPath[] {cp2, cp3})), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp2});
        assertEquals("last cp2 removed from a", Collections.singleton(cp3), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp3});
        assertEquals("a now empty", Collections.EMPTY_SET, r.getPaths("a"));
        r.unregister("a", new ClassPath[0]);
        assertEquals("a still empty", Collections.EMPTY_SET, r.getPaths("a"));
        try {
            r.unregister("a", new ClassPath[] {cp3});
            fail("should not have been permitted to unregister a nonexistent entry");
        } catch (IllegalArgumentException x) {
            // Good.
        }
    }
    
    public void testListening() throws Exception {
        assertEquals("initially no paths of type b", Collections.EMPTY_SET, r.getPaths("b"));
        L l = new L();
        r.addGlobalPathRegistryListener(l);
        r.register("b", new ClassPath[] {cp1, cp2});
        GlobalPathRegistryEvent e = l.event();
        assertNotNull("got an event", e);
        assertTrue("was an addition", l.added());
        assertEquals("right registry", r, e.getRegistry());
        assertEquals("right ID", "b", e.getId());
        assertEquals("right changed paths", new HashSet(Arrays.asList(new ClassPath[] {cp1, cp2})), e.getChangedPaths());
        r.register("b", new ClassPath[] {cp2, cp3});
        e = l.event();
        assertNotNull("got an event", e);
        assertTrue("was an addition", l.added());
        assertEquals("right changed paths", Collections.singleton(cp3), e.getChangedPaths());
        r.register("b", new ClassPath[] {cp3});
        e = l.event();
        assertNull("no event for adding a dupe", e);
        r.unregister("b", new ClassPath[] {cp1, cp3, cp3});
        e = l.event();
        assertNotNull("got an event", e);
        assertFalse("was a removal", l.added());
        assertEquals("right changed paths", new HashSet(Arrays.asList(new ClassPath[] {cp1, cp3})), e.getChangedPaths());
        r.unregister("b", new ClassPath[] {cp2});
        e = l.event();
        assertNull("no event for removing an extra", e);
        r.unregister("b", new ClassPath[] {cp2});
        e = l.event();
        assertNotNull("now an event for removing the last copy", e);
        assertFalse("was a removal", l.added());
        assertEquals("right changed paths", Collections.singleton(cp2), e.getChangedPaths());
    }
    
    
    public void testGetSourceRoots () throws FileStateInvalidException {
        SFBQImpl query = (SFBQImpl) Lookup.getDefault().lookup(SFBQImpl.class);
        assertNotNull ("SourceForBinaryQueryImplementation not found in lookup",query);                
        query.addPair(cp3.getRoots()[0].getURL(),new FileObject[0]);
        r.register(ClassPath.SOURCE, new ClassPath[] {cp1, cp2});
        r.register (ClassPath.COMPILE, new ClassPath[] {cp3});
        Set result = r.getSourceRoots();
        assertEquals ("Wrong number of source roots",result.size(),cp1.getRoots().length + cp2.getRoots().length);
        assertTrue ("Missing roots from cp1",result.containsAll (Arrays.asList(cp1.getRoots())));
        assertTrue ("Missing roots from cp2",result.containsAll (Arrays.asList(cp2.getRoots())));                
        query.addPair(cp3.getRoots()[0].getURL(),cp4.getRoots());       
        result = r.getSourceRoots();
        assertEquals ("Wrong number of source roots",result.size(),cp1.getRoots().length + cp2.getRoots().length+cp4.getRoots().length);
        assertTrue ("Missing roots from cp1",result.containsAll (Arrays.asList(cp1.getRoots())));
        assertTrue ("Missing roots from cp2",result.containsAll (Arrays.asList(cp2.getRoots())));
        assertTrue ("Missing roots from cp4",result.containsAll (Arrays.asList(cp4.getRoots())));
    }
    
    private static final class L implements GlobalPathRegistryListener {
        
        private GlobalPathRegistryEvent e;
        private boolean added;
        
        public L() {}
        
        public synchronized GlobalPathRegistryEvent event() {
            GlobalPathRegistryEvent _e = e;
            e = null;
            return _e;
        }
        
        public boolean added() {
            return added;
        }
        
        public synchronized void pathsAdded(GlobalPathRegistryEvent e) {
            assertNull("checked for last event", this.e);
            this.e = e;
            added = true;
        }
        
        public synchronized void pathsRemoved(GlobalPathRegistryEvent e) {
            assertNull("checked for last event", this.e);
            this.e = e;
            added = false;
        }
        
    }
    
    
    private static class SFBQImpl implements SourceForBinaryQueryImplementation {
        
        private Map pairs = new HashMap ();
        
        void addPair (URL binaryRoot, FileObject[] sourceRoots) {
            assert binaryRoot != null && sourceRoots != null;
            Result r = (Result) this.pairs.get (binaryRoot);
            if (r == null) {
                r = new Result (sourceRoots);
                this.pairs.put (binaryRoot, r);
            }
            else {
                r.setSources(sourceRoots);
            }
        }
                        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            Result result = (Result) this.pairs.get (binaryRoot);
            return result;
        }
        
        
        private static class Result implements SourceForBinaryQuery.Result {
            
            private FileObject[] sources;                        
            private ArrayList listeners = new ArrayList ();
            
            public Result (FileObject[] sources) {
                this.sources = sources;
            }
            
            
            void setSources (FileObject[] sources) {
                this.sources = sources;
                this.fireChange ();
            }
                        
            public synchronized void addChangeListener(javax.swing.event.ChangeListener l) {
                this.listeners.add (l);
            }            
            
            public FileObject[] getRoots() {
                return this.sources;
            }
            
            public synchronized void removeChangeListener(javax.swing.event.ChangeListener l) {
                this.listeners.remove (l);
            }
            
            private void fireChange () {
                Iterator it;
                synchronized (this) {
                    it = ((ArrayList)this.listeners.clone()).iterator();
                }
                ChangeEvent e = new ChangeEvent (this);
                while (it.hasNext()) {
                    ((ChangeListener)it.next()).stateChanged(e);
                }
            }
            
        }
        
    }
    
}
