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

package org.openide.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import org.netbeans.junit.NbTestCase;

public class UtilitiesActiveQueueTest extends NbTestCase {

    public UtilitiesActiveQueueTest(String testName) {
        super(testName);
    }

    public void testRunnableReferenceIsExecuted () throws Exception {
        Object obj = new Object ();
        RunnableRef ref = new RunnableRef (obj);
        synchronized (ref) {
            obj = null;
            assertGC ("Should be GCed quickly", ref);
            ref.wait ();
            assertTrue ("Run method has been executed", ref.executed);
        }
    }
    
    public void testRunnablesAreProcessedOneByOne () throws Exception {
        Object obj = new Object ();
        RunnableRef ref = new RunnableRef (obj);
        ref.wait = true;
        
        
        synchronized (ref) {
            obj = null;
            assertGC ("Is garbage collected", ref);
            ref.wait ();
            assertTrue ("Still not executed, it is blocked", !ref.executed);
        }    

        RunnableRef after = new RunnableRef (new Object ());
        synchronized (after) {
            assertGC ("Is garbage collected", after);
            after.wait (100); // will fail
            assertTrue ("Even if GCed, still not processed", !after.executed);
        }

        synchronized (after) {
            synchronized (ref) {
                ref.notify ();
                ref.wait ();
                assertTrue ("Processed", ref.executed);
            }
            after.wait ();
            assertTrue ("Processed too", after.executed);
        }
    }
    
    public void testCallingPublicMethodsThrowsExceptions () {
        try {
            Utilities.activeReferenceQueue().poll();
            fail ("One should not call public method from outside");
        } catch (RuntimeException ex) {
        }
        try {
            Utilities.activeReferenceQueue ().remove ();
            fail ("One should not call public method from outside");
        } catch (InterruptedException ex) {
        }
        try {
            Utilities.activeReferenceQueue ().remove (10);
            fail ("One should not call public method from outside");
        } catch (InterruptedException ex) {
        }
    }
    
    public void testMemoryLeak() throws Exception {
        final Class<?> u1 = Utilities.class;
        class L extends URLClassLoader {
            public L() {
                super(new URL[] {u1.getProtectionDomain().getCodeSource().getLocation()}, u1.getClassLoader().getParent());
            }
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(u1.getName()) || name.startsWith(u1.getName() + "$")) {
                    Class c = findLoadedClass(name);
                    if (c == null) {
                        c = findClass(name);
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                } else {
                    return super.loadClass(name, resolve);
                }
            }
        }
        ClassLoader l = new L();
        Class<?> u2 = l.loadClass(u1.getName());
        assertEquals(l, u2.getClassLoader());
        Object obj = new Object();
        @SuppressWarnings("unchecked")
        ReferenceQueue<Object> q = (ReferenceQueue<Object>) u2.getMethod("activeReferenceQueue").invoke(null);
        RunnableRef ref = new RunnableRef(obj, q);
        synchronized (ref) {
            obj = null;
            assertGC("Ref should be GC'ed as usual", ref);
            ref.wait();
            assertTrue("Run method has been executed", ref.executed);
        }
        Reference<?> r = new WeakReference<Object>(u2);
        q = null;
        u2 = null;
        l = null;
        assertGC("#86625: Utilities.class can also be collected now", r);
    }

    
    private static class RunnableRef extends WeakReference<Object>
    implements Runnable {
        public boolean wait;
        public boolean entered;
        public boolean executed;
        
        public RunnableRef (Object o) {
            this(o, Utilities.activeReferenceQueue());
        }
        
        public RunnableRef(Object o, ReferenceQueue<Object> q) {
            super(o, q);
        }
        
        public synchronized void run () {
            entered = true;
            if (wait) {
                // notify we are here
                notify ();
                try {
                    wait ();
                } catch (InterruptedException ex) {
                }
            }
            executed = true;
            
            notifyAll ();
        }
    }
}
