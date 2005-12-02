/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.lookup;

import java.io.Serializable;
import org.openide.util.*;

import java.lang.ref.WeakReference;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;

/** Runs all NbLookupTest tests on ProxyLookup and adds few additional.
 */
public class LookupsProxyTest extends AbstractLookupBaseHid
implements AbstractLookupBaseHid.Impl {
    public LookupsProxyTest(java.lang.String testName) {
        super(testName, null);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite (LookupsProxyTest.class));
    }
    
    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (final Lookup lookup) {
        return org.openide.util.lookup.Lookups.proxy (
            new Lookup.Provider () {
                public Lookup getLookup () {
                    return lookup;
                }
            }
        );
    }
    
    public Lookup createInstancesLookup (InstanceContent ic) {
        return new AbstractLookup (ic);
    }

    public void clearCaches () {
    }    
    
    
   
    /** Check whether setLookups method does not fire when there is no
     * change in the lookups.
     */
    public void testProxyListener () {
        Changer ch = new Changer (Lookup.EMPTY);
        
        Lookup lookup = Lookups.proxy(ch);
        Lookup.Result res = lookup.lookup (new Lookup.Template (Object.class));
        
        LL ll = new LL ();
        res.addLookupListener (ll);
        Collection allRes = res.allInstances ();

        ch.setLookup (new AbstractLookup (new InstanceContent ())); // another empty lookup
        lookup.lookup (Object.class); // does the refresh
        
        assertEquals("Replacing an empty by empty does not generate an event", 0, ll.getCount());
        
        InstanceContent content = new InstanceContent ();
        AbstractLookup del = new AbstractLookup (content);
        content.add (this);
        ch.setLookup (del);
        lookup.lookup (Object.class);
        
        if (ll.getCount () != 1) {
            fail ("Changing lookups with different content generates an event");
        }
        
        ch.setLookup (del);
        lookup.lookup (Object.class);
        
        if (ll.getCount () != 0) {
           fail ("Not changing the lookups does not generate any event");
        }
    }

    
    public void testListeningAndQueryingByTwoListenersInstancesSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 1, false);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 1, false);        
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 1, false);
    }
    
    public void testListeningAndQueryingByTwoListenersInstancesSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 2, false);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 2, false);        
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 2, false);
    }

    public void testListeningAndQueryingByTwoListenersInstancesSetLookupsWithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 1, true);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookupsWithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 1, true);        
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookupsWithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 1, true);
    }
    
    public void testListeningAndQueryingByTwoListenersInstancesSetLookups2WithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 2, true);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups2WithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 2, true);        
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups2WithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 2, true);
    }
    
    /* XXX: these are pretty slow, seems there is a performance problem 2^22
    public void testListeningAndQueryingByTwoListenersInstancesSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 22);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 22);        
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 22);
    }
     */
    
    private void doListeningAndQueryingByTwoListenersSetLookups(final int type, int depth, boolean cacheOnTop) {
        Changer orig = new Changer(Lookup.EMPTY);
        Lookup on = Lookups.proxy(orig);
        Lookup first = on;
        
        while (--depth > 0) {
            Changer next = new Changer(on);
            on = Lookups.proxy(next);
        }
        
        
        final Lookup lookup = cacheOnTop ? new ProxyLookup(new Lookup[] { on }) : on;
        
        class L implements LookupListener {
            Lookup.Result integer = lookup.lookup(new Lookup.Template(Integer.class));
            Lookup.Result number = lookup.lookup(new Lookup.Template(Number.class));
            Lookup.Result serial = lookup.lookup(new Lookup.Template(Serializable.class));
            
            {
                integer.addLookupListener(this);
                number.addLookupListener(this);
                serial.addLookupListener(this);
            }
            
            int round;
            
            public void resultChanged(LookupEvent ev) {
                Collection c1 = get(type, integer);
                Collection c2 = get(type, number);
                Collection c3 = get(type, serial);
                
                assertEquals("round " + round + " c1 vs. c2", c1, c2);
                assertEquals("round " + round + " c1 vs. c3", c1, c3);
                assertEquals("round " + round + " c2 vs. c3", c2, c3);
                
                round++;
            }            

            private Collection get(int type, Lookup.Result res) {
                Collection c;
                switch(type) {
                    case 0: c = res.allInstances(); break;
                    case 1: c = res.allClasses(); break;
                    case 2: c = res.allItems(); break;
                    default: c = null; fail("Type: " + type); break;
                }
                
                assertNotNull(c);
                return new ArrayList(c);
            }
        }
        
        L listener = new L();
        listener.resultChanged(null);
        ArrayList arr = new ArrayList();
        for(int i = 0; i < 100; i++) {
            arr.add(new Integer(i));
            
            orig.lookup = Lookups.fixed(arr.toArray());
            // do the refresh
            first.lookup((Class)null);
        }
        
        assertEquals("3x100+1 checks", 301, listener.round);
    }
    
    private static final class Changer implements Lookup.Provider {
        private Lookup lookup;
        
        public Changer (Lookup lookup) {
            setLookup (lookup);
        }
        
        public void setLookup (Lookup lookup) {
            this.lookup = lookup;
        }
        
        public Lookup getLookup() {
            return lookup;
        }
    }
}
