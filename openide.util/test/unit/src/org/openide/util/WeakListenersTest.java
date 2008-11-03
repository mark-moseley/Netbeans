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

import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

public class WeakListenersTest extends NbTestCase {

    private static final Logger LOG = Logger.getLogger(WeakListenersTest.class.getName());
    
    public WeakListenersTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 45000;
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }
    
    public void testOneCanCallHashCodeOrOnWeakListener () {
        Listener l = new Listener ();
        Object weak = WeakListeners.create (PropertyChangeListener.class, l, null);
        weak.hashCode ();
    }
    
    /** Useful for next test */
    interface X extends java.util.EventListener {
        public void invoke ();
    }
    /** Useful for next test */
    class XImpl implements X {
        public int cnt;
        public void invoke () {
            cnt++;
        }
    }
    public void testCallingMethodsWithNoArgumentWorks() {
        XImpl l = new XImpl ();
        LOG.fine("XImpl created: " + l);
        X weak = WeakListeners.create(X.class, l, null);
        LOG.fine("weak created: " + weak);
        weak.invoke ();
        LOG.fine("invoked");
        assertEquals ("One invocation", 1, l.cnt);
    }

    public void testReleaseOfListenerWithNullSource () throws Exception {
        doTestReleaseOfListener (false);
    }
    
    public void testReleaseOfListenerWithSource () throws Exception {
        doTestReleaseOfListener (true);
    }
    
    private void doTestReleaseOfListener (final boolean source) throws Exception {   
        Listener l = new Listener ();
        
        class MyButton extends javax.swing.JButton {
            private Thread removedBy;
            private int cnt;
            
            @Override
            public synchronized void removePropertyChangeListener (PropertyChangeListener l) {
                // notify prior
                LOG.fine("removePropertyChangeListener: " + source + " cnt: " + cnt);
                if (source && cnt == 0) {
                    notifyAll ();
                    try {
                        // wait for 1
                        LOG.fine("wait for 1");
                        wait ();
                        LOG.fine("wait for 1 over");
                    } catch (InterruptedException ex) {
                        fail ("Not happen");
                    }
                }
                LOG.fine("Super removePropertyChangeListener");
                super.removePropertyChangeListener (l);
                LOG.fine("Super over removePropertyChangeListener");
                removedBy = Thread.currentThread();
                cnt++;
                notifyAll ();
            }
            
            public synchronized void waitListener () throws Exception {
                int count = 0;
                while (removedBy == null) {
                    LOG.fine("waitListener, wait 500");
                    wait (500);
                    LOG.fine("waitListener 500 Over");
                    if (count++ == 5) {
                        fail ("Time out: removePropertyChangeListener was not called at all");
                    } else {
                        LOG.fine("Forced gc");
                        System.gc ();
                        System.runFinalization();
                        LOG.fine("after force runFinalization");
                    }
                }
            }
        }
        
        MyButton button = new MyButton ();
        LOG.fine("Button is here");
        java.beans.PropertyChangeListener weakL = WeakListeners.propertyChange (l, source ? button : null);
        LOG.fine("WeakListeners created: " + weakL);
        button.addPropertyChangeListener(weakL);
        LOG.fine("WeakListeners attached");
        assertTrue ("Weak listener is there", Arrays.asList (button.getPropertyChangeListeners()).indexOf (weakL) >= 0);
        
        button.setText("Ahoj");
        LOG.fine("setText changed to ahoj");
        assertEquals ("Listener called once", 1, l.cnt);
        
        Reference<?> ref = new WeakReference<Object>(l);
        LOG.fine("Clearing listener");
        l = null;
        

        synchronized (button) {
            LOG.fine("Before assertGC");
            assertGC ("Can disappear", ref);
            LOG.fine("assertGC ok");
            
            if (source) {
                LOG.fine("before wait");
                button.wait ();
                LOG.fine("after wait");
                // this should not remove the listener twice
                button.setText ("Hoj");
                LOG.fine("after setText - > hoj");
                // go on (wait 1)
                button.notify ();
                LOG.fine("before wait listener");
                
                button.waitListener ();
                LOG.fine("after waitListener");
            } else {
                // trigger the even firing so weak listener knows from
                // where to unregister
                LOG.fine("before setText -> Hoj");
                button.setText ("Hoj");
                LOG.fine("after setText -> Hoj");
            }
            
            LOG.fine("before 2 waitListener");
            button.waitListener ();
            LOG.fine("after 2 waitListener");
            Thread.sleep (500);
            LOG.fine("Thread.sleep over");
        }

        assertEquals ("Weak listener has been removed", -1, Arrays.asList (button.getPropertyChangeListeners()).indexOf (weakL));
        assertEquals ("Button released from a thread", "Active Reference Queue Daemon", button.removedBy.getName());
        assertEquals ("Unregister called just once", 1, button.cnt);
        
        // and because it is not here, it can be GCed
        Reference<?> weakRef = new WeakReference<Object>(weakL);
        weakL = null;
        LOG.fine("Doing assertGC at the end");
        assertGC ("Weak listener can go away as well", weakRef);
    }
    
    
    public void testSourceCanBeGarbageCollected () {
        javax.swing.JButton b = new javax.swing.JButton ();
        Listener l = new Listener ();
        
        b.addPropertyChangeListener (WeakListeners.propertyChange (l, b));
        
        Reference<?> ref = new WeakReference<Object>(b);
        b = null;
        
        assertGC ("Source can be GC", ref);
    }
    
    public void testNamingListenerBehaviour () throws Exception {
        Listener l = new Listener ();
        ImplEventContext c = new ImplEventContext ();
        javax.naming.event.NamingListener weakL = (javax.naming.event.NamingListener)WeakListeners.create (
            javax.naming.event.ObjectChangeListener.class,
            javax.naming.event.NamingListener.class,
            l,
            c
        );
        
        c.addNamingListener("", javax.naming.event.EventContext.OBJECT_SCOPE, weakL);
        assertEquals ("Weak listener is there", weakL, c.listener);
        
        Reference<?> ref = new WeakReference<Object>(l);
        l = null;

        synchronized (c) {
            assertGC ("Can disappear", ref);
            c.waitListener ();
        }
        assertNull ("Listener removed", c.listener);
    }
    
    public void testExceptionIllegalState () {
        Listener l = new Listener ();
        /* Will not even compile any more:
        try {
            WeakListeners.create(PropertyChangeListener.class, javax.naming.event.NamingListener.class, l, null);
            fail ("This shall not be allowed as NamingListener is not superclass of PropertyChangeListener");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        
        try {
            WeakListeners.create(Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        
        try {
            WeakListeners.create(Object.class, Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
         */
        
        try {
            WeakListeners.create (PropertyChangeListener.class, Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
    }
    
    public void testHowBigIsWeakListener () throws Exception {
        Listener l = new Listener ();
        javax.swing.JButton button = new javax.swing.JButton ();
        ImplEventContext c = new ImplEventContext ();
        
        Object[] ignore = {
            l, 
            button,
            c,
            Utilities.activeReferenceQueue()
        };
        
        
        PropertyChangeListener pcl = WeakListeners.propertyChange(l, button);
        assertSize ("Not too big (plus 32 from ReferenceQueue)", java.util.Collections.singleton (pcl), 120, ignore);
        
        Object ocl = WeakListeners.create (javax.naming.event.ObjectChangeListener.class, javax.naming.event.NamingListener.class, l, c);
        assertSize ("A bit bigger (plus 32 from ReferenceQueue)", java.util.Collections.singleton (ocl), 136, ignore);
        
        Object nl = WeakListeners.create (javax.naming.event.NamingListener.class, l, c);
        assertSize ("The same (plus 32 from ReferenceQueue)", java.util.Collections.singleton (nl), 136, ignore);
        
    }

    public void testPrivateRemoveMethod() throws Exception {
        PropChBean bean = new PropChBean();
        Listener listener = new Listener();
        PCL weakL = WeakListeners.create(PCL.class, listener, bean);
        Reference<?> ref = new WeakReference<Object>(listener);
        
        bean.addPCL(weakL);
        
        bean.listeners.firePropertyChange (null, null, null);
        assertEquals ("One call to the listener", 1, listener.cnt);
        listener.cnt = 0;
        
        listener = null;
        assertGC("Listener wasn't GCed", ref);
        
        ref = new WeakReference<Object>(weakL);
        weakL = null;
        assertGC("WeakListener wasn't GCed", ref);
        
        // this shall enforce the removal of the listener
        bean.listeners.firePropertyChange (null, null, null);
        
        assertEquals ("No listeners", 0, bean.listeners.getPropertyChangeListeners ().length);
    }

    @RandomlyFails // NB-Core-Build #1651
    public void testStaticRemoveMethod() throws Exception {
        ChangeListener l = new ChangeListener() {public void stateChanged(ChangeEvent e) {}};
        Singleton.addChangeListener(WeakListeners.change(l, Singleton.class));
        assertEquals(1, Singleton.listeners.size());
        Reference<?> r = new WeakReference<Object>(l);
        l = null;
        assertGC("could collect listener", r);
        assertEquals("called remove method", 0, Singleton.listeners.size());
    }
    public static class Singleton {
        public static List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        public static void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        public static void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
    }
    
    private static final class Listener
    implements PCL, java.beans.PropertyChangeListener, javax.naming.event.ObjectChangeListener {
        public int cnt;
        
        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            cnt++;
        }
        
        public void namingExceptionThrown(javax.naming.event.NamingExceptionEvent evt) {
            cnt++;
        }
        
        public void objectChanged(javax.naming.event.NamingEvent evt) {
            cnt++;
        }
    } // end of Listener
    
    private static final class ImplEventContext extends javax.naming.InitialContext 
    implements javax.naming.event.EventContext {
        public javax.naming.event.NamingListener listener;
        
        public ImplEventContext () throws Exception {
        }
        
        public void addNamingListener(javax.naming.Name target, int scope, javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertNull (listener);
            listener = l;
        }
        
        public void addNamingListener(String target, int scope, javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertNull (listener);
            listener = l;
        }
        
        public synchronized void removeNamingListener(javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertEquals ("Removing the same listener", listener, l);
            listener = null;
            notifyAll ();
        }
        
        public boolean targetMustExist() throws javax.naming.NamingException {
            return false;
        }
        
        public synchronized void waitListener () throws Exception {
            int cnt = 0;
            while (listener != null) {
                wait (500);
                if (cnt++ == 5) {
                    fail ("Time out: removeNamingListener was not called at all");
                } else {
                    System.gc ();
                    System.runFinalization();
                }
            }
        }
        
    }
    
    private static class PropChBean {
        private java.beans.PropertyChangeSupport listeners = new java.beans.PropertyChangeSupport (this);
        private void addPCL(PCL l) { listeners.addPropertyChangeListener (l); }
        private void removePCL(PCL l) { listeners.removePropertyChangeListener (l); }
    } // End of PropChBean class

    // just a marker, its name will be used to construct the name of add/remove methods, e.g. addPCL, removePCL
    private static interface PCL extends PropertyChangeListener {
    } // End of PrivatePropL class
    
}
