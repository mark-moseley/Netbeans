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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.java.classpath;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.support.PathResourceBase;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 * Test functionality of GlobalPathRegistry.
 * @author Jesse Glick
 */
public class GlobalPathRegistryTest extends NbTestCase {
    
    public GlobalPathRegistryTest(String name) {
        super(name);
        MockServices.setServices(SFBQImpl.class, DeadLockSFBQImpl.class);
    }
    
    private GlobalPathRegistry r;
    private FileObject root;
    private ClassPath cp1, cp2, cp3, cp4, cp5;
    protected void setUp() throws Exception {
        super.setUp();
        r = GlobalPathRegistry.getDefault();
        r.clear();
        clearWorkDir();
        root = FileUtil.toFileObject(getWorkDir());
        cp1 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("1")});
        cp2 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("2")});
        cp3 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("3")});
        cp4 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("4")});
        cp5 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("5")});
    }
    
    public void testBasicOperation() throws Exception {
        assertEquals("initially no paths of type a", Collections.<ClassPath>emptySet(), r.getPaths("a"));
        r.register("a", new ClassPath[] {cp1, cp2});
        assertEquals("added some paths of type a", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp1, cp2})), r.getPaths("a"));
        r.register("a", new ClassPath[0]);
        assertEquals("did not add any new paths to a", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp1, cp2})), r.getPaths("a"));
        assertEquals("initially no paths of type b", Collections.<ClassPath>emptySet(), r.getPaths("b"));
        r.register("b", new ClassPath[] {cp3, cp4, cp5});
        assertEquals("added some paths of type b", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp3, cp4, cp5})), r.getPaths("b"));
        r.unregister("a", new ClassPath[] {cp1});
        assertEquals("only one path left of type a", Collections.<ClassPath>singleton(cp2), r.getPaths("a"));
        r.register("a", new ClassPath[] {cp2, cp3});
        assertEquals("only one new path added of type a", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp2, cp3})), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp2});
        assertEquals("still have extra cp2 in a", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp2, cp3})), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp2});
        assertEquals("last cp2 removed from a", Collections.<ClassPath>singleton(cp3), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp3});
        assertEquals("a now empty", Collections.<ClassPath>emptySet(), r.getPaths("a"));
        r.unregister("a", new ClassPath[0]);
        assertEquals("a still empty", Collections.<ClassPath>emptySet(), r.getPaths("a"));
        try {
            r.unregister("a", new ClassPath[] {cp3});
            fail("should not have been permitted to unregister a nonexistent entry");
        } catch (IllegalArgumentException x) {
            // Good.
        }
    }
    
    public void testListening() throws Exception {
        assertEquals("initially no paths of type b", Collections.<ClassPath>emptySet(), r.getPaths("b"));
        L l = new L();
        r.addGlobalPathRegistryListener(l);
        r.register("b", new ClassPath[] {cp1, cp2});
        GlobalPathRegistryEvent e = l.event();
        assertNotNull("got an event", e);
        assertTrue("was an addition", l.added());
        assertEquals("right registry", r, e.getRegistry());
        assertEquals("right ID", "b", e.getId());
        assertEquals("right changed paths", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp1, cp2})), e.getChangedPaths());
        r.register("b", new ClassPath[] {cp2, cp3});
        e = l.event();
        assertNotNull("got an event", e);
        assertTrue("was an addition", l.added());
        assertEquals("right changed paths", Collections.<ClassPath>singleton(cp3), e.getChangedPaths());
        r.register("b", new ClassPath[] {cp3});
        e = l.event();
        assertNull("no event for adding a dupe", e);
        r.unregister("b", new ClassPath[] {cp1, cp3, cp3});
        e = l.event();
        assertNotNull("got an event", e);
        assertFalse("was a removal", l.added());
        assertEquals("right changed paths", new HashSet<ClassPath>(Arrays.asList(new ClassPath[] {cp1, cp3})), e.getChangedPaths());
        r.unregister("b", new ClassPath[] {cp2});
        e = l.event();
        assertNull("no event for removing an extra", e);
        r.unregister("b", new ClassPath[] {cp2});
        e = l.event();
        assertNotNull("now an event for removing the last copy", e);
        assertFalse("was a removal", l.added());
        assertEquals("right changed paths", Collections.<ClassPath>singleton(cp2), e.getChangedPaths());
    }
    
    
    public void testGetSourceRoots () throws Exception {
        SFBQImpl query = Lookup.getDefault().lookup(SFBQImpl.class);
        assertNotNull ("SourceForBinaryQueryImplementation not found in lookup",query);                
        query.addPair(cp3.getRoots()[0].getURL(),new FileObject[0]);
        ClassPathTest.TestClassPathImplementation cpChangingImpl = new ClassPathTest.TestClassPathImplementation();
        ClassPath cpChanging = ClassPathFactory.createClassPath(cpChangingImpl);
        assertEquals("cpChangingImpl is empty", 0, cpChanging.getRoots().length);
        r.register(ClassPath.SOURCE, new ClassPath[] {cp1, cp2, cpChanging});
        r.register (ClassPath.COMPILE, new ClassPath[] {cp3});
        Set<FileObject> result = r.getSourceRoots();
        assertEquals ("Wrong number of source roots",result.size(),cp1.getRoots().length + cp2.getRoots().length);
        assertTrue ("Missing roots from cp1",result.containsAll (Arrays.asList(cp1.getRoots())));
        assertTrue ("Missing roots from cp2",result.containsAll (Arrays.asList(cp2.getRoots())));                
        // simulate classpath change:
        URL u = cp5.entries().get(0).getURL();
        cpChangingImpl.addResource(u);
        assertEquals("cpChangingImpl is not empty", 1, cpChanging.getRoots().length);
        result = r.getSourceRoots();
        assertEquals ("Wrong number of source roots",result.size(),cp1.getRoots().length + cp2.getRoots().length + cpChanging.getRoots().length);
        assertTrue ("Missing roots from cp1",result.containsAll (Arrays.asList(cp1.getRoots())));
        assertTrue ("Missing roots from cp2",result.containsAll (Arrays.asList(cp2.getRoots())));                
        cpChangingImpl.removeResource(u);
        
        query.addPair(cp3.getRoots()[0].getURL(),cp4.getRoots());       
        result = r.getSourceRoots();
        assertEquals ("Wrong number of source roots",result.size(),cp1.getRoots().length + cp2.getRoots().length+cp4.getRoots().length);
        assertTrue ("Missing roots from cp1",result.containsAll (Arrays.asList(cp1.getRoots())));
        assertTrue ("Missing roots from cp2",result.containsAll (Arrays.asList(cp2.getRoots())));
        assertTrue ("Missing roots from cp4",result.containsAll (Arrays.asList(cp4.getRoots())));
    }
    
    /**
     * Tests issue: #60976:Deadlock between JavaFastOpen$Evaluator and AntProjectHelper$something
     */
    public void testGetSourceRootsDeadLock () throws Exception {        
        DeadLockSFBQImpl query = Lookup.getDefault().lookup(DeadLockSFBQImpl.class);
        assertNotNull ("SourceForBinaryQueryImplementation not found in lookup",query);        
        r.register (ClassPath.COMPILE, new ClassPath[] {cp1});
        try {            
            query.setSynchronizedJob (
                new Runnable () {
                    public void run () {
                        r.register(ClassPath.COMPILE, new ClassPath[] {cp2});
                    }
                }
            );
            r.getSourceRoots();
        } finally {
            query.setSynchronizedJob (null);
        }
    }

    public void testFindResource() throws Exception {
        final FileObject src1 = root.createFolder("src1");
        FileObject src1included = FileUtil.createData(src1, "included/file");
        FileUtil.createData(src1, "excluded/file1");
        FileUtil.createData(src1, "excluded/file2");
        FileObject src2 = root.createFolder("src2");
        FileObject src2included = FileUtil.createData(src2, "included/file");
        FileObject src2excluded1 = FileUtil.createData(src2, "excluded/file1");
        class PRI extends PathResourceBase implements FilteringPathResourceImplementation {
            public URL[] getRoots() {
                try {
                    return new URL[] {src1.getURL()};
                } catch (FileStateInvalidException x) {
                    throw new AssertionError(x);
                }
            }
            public boolean includes(URL root, String resource) {
                return resource.startsWith("incl");
            }
            public ClassPathImplementation getContent() {
                return null;
            }
        }
        r.register(ClassPath.SOURCE, new ClassPath[] {
            ClassPathSupport.createClassPath(Collections.singletonList(new PRI())),
            ClassPathSupport.createClassPath(new FileObject[] {src2})
        });
        assertTrue(Arrays.asList(src1included, src2included).contains(r.findResource("included/file")));
        assertEquals(src2excluded1, r.findResource("excluded/file1"));
        assertEquals(null, r.findResource("excluded/file2"));
        assertEquals(null, r.findResource("nonexistent"));
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
    
    
    public static class SFBQImpl implements SourceForBinaryQueryImplementation {
        
        private Map<URL,SourceForBinaryQuery.Result> pairs = new HashMap<URL,SourceForBinaryQuery.Result> ();
        
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
            private final ChangeSupport changeSupport = new ChangeSupport(this);
            
            public Result (FileObject[] sources) {
                this.sources = sources;
            }
            
            
            void setSources (FileObject[] sources) {
                this.sources = sources;
                this.changeSupport.fireChange ();
            }
                        
            public void addChangeListener(javax.swing.event.ChangeListener l) {
                changeSupport.addChangeListener (l);
            }            
            
            public FileObject[] getRoots() {
                return this.sources;
            }
            
            public void removeChangeListener(javax.swing.event.ChangeListener l) {
                changeSupport.removeChangeListener (l);
            }
            
        }
        
    }
    
    public static class DeadLockSFBQImpl extends Thread implements SourceForBinaryQueryImplementation {
        
        private Runnable r;
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            if (this.r != null) {                
                synchronized (this) {
                    this.start();
                    try {
                        this.wait ();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
            return null;
        }
        
        public synchronized void run () {
            r.run();
            this.notify();
        }
        
        public void setSynchronizedJob (Runnable r) {
            this.r = r;
        }
        
    }
    
}
