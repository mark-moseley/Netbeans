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

package threaddemo.locking;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test Locks.event.
 * @author Jesse Glick
 */
public class EventLockTest extends TestCase {
    
    public EventLockTest(String name) {
        super(name);
    }
    
    public void testEventAccess() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        // test canRead, canWrite, correct thread used by synch methods
        assertTrue(!Locks.event().canRead());
        assertTrue(!Locks.event().canWrite());
        assertTrue(Locks.event().read(new LockAction<Boolean>() {
            public Boolean run() {
                return EventQueue.isDispatchThread() &&
                        Locks.event().canRead() &&
                        Locks.event().canWrite();
            }
        }));
        assertTrue(Locks.event().read(new LockExceptionAction<Boolean,Exception>() {
            public Boolean run() throws Exception {
                return EventQueue.isDispatchThread() &&
                        Locks.event().canRead() &&
                        Locks.event().canWrite();
            }
        }));
        assertTrue(Locks.event().write(new LockAction<Boolean>() {
            public Boolean run() {
                return EventQueue.isDispatchThread() &&
                        Locks.event().canRead() &&
                        Locks.event().canWrite();
            }
        }));
        assertTrue(Locks.event().write(new LockExceptionAction<Boolean,Exception>() {
            public Boolean run() throws Exception {
                return EventQueue.isDispatchThread() &&
                        Locks.event().canRead() &&
                        Locks.event().canWrite();
            }
        }));
        // test that r/wA(Runnable) runs in AWT eventually
        final boolean[] b = new boolean[1];
        // first, that r/wA will run (even asynch)
        Locks.event().readLater(new Runnable() {
            public void run() {
                synchronized (b) {
                    b[0] = EventQueue.isDispatchThread();
                    b.notify();
                }
            }
        });
        synchronized (b) {
            if (!b[0]) b.wait(9999);
        }
        assertTrue(b[0]);
        Locks.event().writeLater(new Runnable() {
            public void run() {
                synchronized (b) {
                    b[0] = !EventQueue.isDispatchThread();
                    b.notify();
                }
            }
        });
        synchronized (b) {
            if (b[0]) b.wait(9999);
        }
        assertTrue(!b[0]);
        /*
        // now that r/wA runs synch in event thread
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Locks.event().readLater(new Runnable() {
                    public void run() {
                        b[0] = EventQueue.isDispatchThread();
                    }
                });
            }
        });
        assertTrue(b[0]);
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Locks.event().writeLater(new Runnable() {
                    public void run() {
                        b[0] = !EventQueue.isDispatchThread();
                    }
                });
            }
        });
        assertTrue(!b[0]);
         */
    }
    
    public void testEventExceptions() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        // test that checked excs from M.EA throw correct ME
        try {
            Locks.event().read(new LockExceptionAction<Void,IOException>() {
                public Void run() throws IOException {
                    throw new IOException();
                }
            });
            fail();
        } catch (IOException e) {
            // OK
        }
        // but that unchecked excs are passed thru
        try {
            Locks.event().read(new LockExceptionAction<Void,IOException>() {
                public Void run() throws IOException {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        } catch (IOException e) {
            fail(e.toString());
        }
        // similarly for unchecked excs from M.A
        try {
            Locks.event().read(new LockAction<Void>() {
                public Void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        } catch (RuntimeException e) {
            fail(e.toString());
        }
        // and blocking runnables
        try {
            Locks.event().read(new Runnable() {
                public void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK.
        }
        try {
            Locks.event().write(new Runnable() {
                public void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK.
        }
    }
    
}
