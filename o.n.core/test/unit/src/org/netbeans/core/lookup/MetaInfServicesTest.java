/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.lookup;

import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.netbeans.core.NbTopManager;
import org.netbeans.core.modules.*;
import org.openide.util.*;
import java.io.File;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

/** Test whether modules can really register things in their META-INF/services/class.Name
 * files, and whether this behaves correctly when the modules are disabled/enabled.
 * Note that Plain loads its classpath modules as soon as you ask for it, so these
 * tests do not check what happens on the NetBeans startup classpath.
 * @author Jesse Glick
 */
public class MetaInfServicesTest extends NbTestCase {

    static {
        // Currently this feature is turned off in the core by default. We want to test it on.
        System.setProperty("netbeans.lookup.usemetainfservices", "true");
    }
    
    public MetaInfServicesTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) throws Exception {
        File temp = File.createTempFile("MetaInfServicesTest", ".dummy");
        System.setProperty("nbjunit.workdir", new File(temp.getParentFile(), temp.getName().substring(0, temp.getName().length() - 6)).getAbsolutePath());
        TestRunner.run(new NbTestSuite(MetaInfServicesTest.class));
    }
    
    private ModuleManager mgr;
    private Module m1, m2;
    protected void setUp() throws Exception {
        //System.err.println("setUp");
        //Thread.dumpStack();
        clearWorkDir();
        // Load Plain.
        // Make a couple of modules.
        mgr = NbTopManager.get().getModuleSystem().getManager();
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    File jar1 = new File(MetaInfServicesTest.class.getResource("data/services-jar-1.jar").getPath());
                    File jar2 = new File(MetaInfServicesTest.class.getResource("data/services-jar-2.jar").getPath());
                    m1 = mgr.create(jar1, new ModuleHistory(), false, false, false);
                    m2 = mgr.create(jar2, new ModuleHistory(), false, false, false);
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
        assertEquals(Collections.EMPTY_SET, m1.getProblems());
    }
    protected void tearDown() throws Exception {
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    if (m2.isEnabled()) mgr.disable(m2);
                    mgr.delete(m2);
                    if (m1.isEnabled()) mgr.disable(m1);
                    mgr.delete(m1);
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
        m1 = null;
        m2 = null;
        mgr = null;
    }
    protected static final int TWIDDLE_ENABLE = 0;
    protected static final int TWIDDLE_DISABLE = 1;
    protected void twiddle(final Module m, final int action) throws Exception {
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    switch (action) {
                    case TWIDDLE_ENABLE:
                        mgr.enable(m);
                        break;
                    case TWIDDLE_DISABLE:
                        mgr.disable(m);
                        break;
                    default:
                        throw new IllegalArgumentException("bad action: " + action);
                    }
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
    }
    
    /** Fails to work if you have >1 method per class, because setUp gets run more
     * than once (XTest bug I suppose).
     */
    public void testEverything() throws Exception {
        twiddle(m1, TWIDDLE_ENABLE);
        ClassLoader systemClassLoader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        Class xface = systemClassLoader.loadClass("org.foo.Interface");
        Lookup.Result r = Lookup.getDefault().lookup(new Lookup.Template(xface));
        List instances = new ArrayList(r.allInstances());
        // Expect to get Impl1 from first JAR.
        assertEquals(1, instances.size());
        Object instance1 = instances.get(0);
        assertTrue(xface.isInstance(instance1));
        assertEquals("org.foo.impl.Implementation1", instance1.getClass().getName());
        // Expect to have (same) Impl1 + Impl2.
        LookupL l = new LookupL();
        r.addLookupListener(l);
        twiddle(m2, TWIDDLE_ENABLE);
        assertTrue("Turning on a second module with a manifest service fires a lookup change", l.gotSomething());
        instances = new ArrayList(r.allInstances());
        assertEquals(2, instances.size());
        assertEquals(instance1, instances.get(0));
        assertEquals("org.bar.Implementation2", instances.get(1).getClass().getName());
        // Expect to lose Impl2.
        l.count = 0;
        twiddle(m2, TWIDDLE_DISABLE);
        assertTrue(l.gotSomething());
        instances = new ArrayList(r.allInstances());
        assertEquals(1, instances.size());
        assertEquals(instance1, instances.get(0));
        // Expect to lose Impl1 too.
        l.count = 0;
        twiddle(m1, TWIDDLE_DISABLE);
        assertTrue(l.gotSomething());
        instances = new ArrayList(r.allInstances());
        assertEquals(0, instances.size());
        // Expect to not get anything: wrong xface version
        l.count = 0;
        twiddle(m1, TWIDDLE_ENABLE);
        // not really important: assertTrue(!l.gotSomething());
        instances = new ArrayList(r.allInstances());
        assertEquals(0, instances.size());
        systemClassLoader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        Class xface2 = systemClassLoader.loadClass("org.foo.Interface");
        assertTrue(xface != xface2);
        Lookup.Result r2 = Lookup.getDefault().lookup(new Lookup.Template(xface2));
        instances = new ArrayList(r2.allInstances());
        assertEquals(1, instances.size());
        // Let's also check up on XML providers, which ought to be in the classpath.
        DocumentBuilderFactory dbf = (DocumentBuilderFactory)Lookup.getDefault().lookup(DocumentBuilderFactory.class);
        assertNotNull(dbf);
        SAXParserFactory spf = (SAXParserFactory)Lookup.getDefault().lookup(SAXParserFactory.class);
        assertNotNull(spf);
        //System.err.println("dbf=" + dbf + " spf=" + spf);
        //System.err.println("all DBFs=" + Lookup.getDefault().lookup(new Lookup.Template(DocumentBuilderFactory.class)).allInstances());
    }
    
    protected static final class LookupL implements LookupListener {
        public int count = 0;
        public synchronized void resultChanged(LookupEvent ev) {
            count++;
            notifyAll();
        }
        public synchronized boolean gotSomething() throws InterruptedException {
            if (count > 0) return true;
            wait(9999);
            return count > 0;
        }
    }
    
}
