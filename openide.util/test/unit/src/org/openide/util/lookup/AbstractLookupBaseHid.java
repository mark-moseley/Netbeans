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

import javax.swing.ActionMap;
import javax.swing.InputMap;
import org.openide.util.*;

import java.lang.ref.WeakReference;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;
import java.io.Serializable;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

public class AbstractLookupBaseHid extends NbTestCase {
    private static AbstractLookupBaseHid running;
    
    /** instance content to work with */
    InstanceContent ic;
    /** the lookup to work on */
    protected Lookup instanceLookup;
    /** the lookup created to work with */
    private Lookup lookup;
    /** implementation of methods that can influence the behaviour */
    Impl impl;
    
    protected AbstractLookupBaseHid(java.lang.String testName, Impl impl) {
        super(testName);
        if (impl == null && (this instanceof Impl)) {
            impl = (Impl)this;
        }
        this.impl = impl;
    }
    
    protected void setUp () {
        this.ic = new InstanceContent ();
        this.instanceLookup = createInstancesLookup (ic);
        this.lookup = createLookup (instanceLookup);
        running = this;
    }        
    
    protected void tearDown () {
        running = null;
    }
    
    /** The methods to influence test behaviour */
    public static interface Impl {
        /** Creates the initial abstract lookup.
         */
        public Lookup createInstancesLookup (InstanceContent ic);
        /** Creates an lookup for given lookup. This class just returns 
         * the object passed in, but subclasses can be different.
         * @param lookup in lookup
         * @return a lookup to use
         */
        public Lookup createLookup (Lookup lookup);
        
        /** If the impl has any caches that would prevent the system
         * to not garbage collect correctly, then clear them now.
         */
        public void clearCaches ();
    }
    
    private Lookup createInstancesLookup (InstanceContent ic) {
        return impl.createInstancesLookup (ic);
    }
    
    private Lookup createLookup (Lookup lookup) {
        return impl.createLookup (lookup);
    }
    
    /** instances that we register */
    private static Object[] INSTANCES = new Object[] {
        new Integer (10), 
        new Object ()
    };
    
    /** Test if first is really first.
     */
    public void testFirst () {
        Object i1 = new Integer (1);
        Object i2 = new Integer (2);
        
        ic.add (i1);
        ic.add (i2);
        
        Object found = lookup.lookup (Integer.class);
        if (found != i1) {
            fail ("First object is not first: " + found + " != " + i1);
        }
        
        ArrayList list = new ArrayList ();
        list.add (i2);
        list.add (i1);
        ic.set (list, null);
        
        found = lookup.lookup (Integer.class);
        if (found != i2) {
            fail ("Second object is not first after reorder: " + found + " != " + i2);
        }
        
    }
    


    /** Tests ordering of items in the lookup.
    */
    public void testOrder () {
        addInstances (INSTANCES);

        if (INSTANCES[0] != lookup.lookup (INSTANCES[0].getClass ())) {
            fail ("First object in intances not found");
        }

        Iterator all = lookup.lookup (new Lookup.Template (Object.class)).allInstances ().iterator ();
        checkIterator ("Difference between instances added and found", all, Arrays.asList (INSTANCES));
    }
    
    /** Checks the reorder of items in lookup reflects the result.
     * Testing both classes and interfaces, because they are often treated
     * especially.
     */
    public void testReorder () {
        String s1 = "s2";
        String s2 = "s1";
        Runnable r1 = new Runnable () {
            public void run () {}
        };
        Runnable r2 = new Runnable () {
            public void run () {}
        };
        ArrayList l = new ArrayList ();

        l.add (s1);
        l.add (s2);
        l.add (r1);
        l.add (r2);
        ic.set (l, null);
     
        assertEquals ("s1 is found", s1, lookup.lookup (String.class));
        assertEquals ("r1 is found", r1, lookup.lookup (Runnable.class));
        
        Collections.reverse (l);
        
        ic.set (l, null);
        
        assertEquals ("s2 is found", s2, lookup.lookup (String.class));
        assertEquals ("r2 is found", r2, lookup.lookup (Runnable.class));
    }
    
    /** Tries to set empty collection to the lookup.
     */
    public void testSetEmpty () {
        ic.add ("A serializable string");
        lookup.lookup (Serializable.class);
        
        ic.set (Collections.EMPTY_LIST, null);
    }
    
    /** Tests a more complex reorder on nodes.
     */
    public void testComplexReorder () {
        Integer i1 = new Integer (1);
        Long i2 = new Long (2);
        
        ArrayList l = new ArrayList ();
        l.add (i1);
        l.add (i2);
        ic.set (l, null);
        
        assertEquals ("Find integer", i1, lookup.lookup (Integer.class));
        assertEquals ("Find long", i2, lookup.lookup (Long.class));
        assertEquals ("Find number", i1, lookup.lookup (Number.class));
        
        Collections.reverse (l);
        
        ic.set (l, null);
        
        assertEquals ("Find integer", i1, lookup.lookup (Integer.class));
        assertEquals ("Find long", i2, lookup.lookup (Long.class));
        assertEquals ("Find number", i2, lookup.lookup (Number.class));
    }
    
    /** Checks whether setPairs keeps the order.
     */
    public void testSetPairs () {
        // test setPairs method
        ArrayList li = new ArrayList();
        li.addAll (Arrays.asList (INSTANCES));
        ic.set (li, null);
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (Object.class));
        Iterator all = res.allInstances ().iterator ();
        checkIterator ("Original order not kept", all, li);
        
        // reverse the order
        Collections.reverse (li);
        
        // change the pairs
        LL listener = new LL (res);
        res.addLookupListener (listener);
        ic.set (li, null);
        if (listener.getCount () != 1) {
            fail ("Result has not changed even we set reversed order");
        }
        
        all = res.allInstances ().iterator ();
        checkIterator ("Reversed order not kept", all, li);
    }

    /** Checks whether setPairs fires correct events.
     */
    public void testSetPairsFire () {
        // test setPairs method
        ArrayList li = new ArrayList();
        li.addAll (Arrays.asList (INSTANCES));
        ic.set (li, null);
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (Integer.class));
        Iterator all = res.allInstances ().iterator ();
        checkIterator ("Integer is not there", all, Collections.nCopies (1, INSTANCES[0]));
        
        // change the pairs
        LL listener = new LL (res);
        res.addLookupListener (listener);

        ArrayList l2 = new ArrayList (li);
        l2.remove (INSTANCES[0]);
        ic.set (l2, null);

        all = lookup.lookup (new Lookup.Template (Object.class)).allInstances ().iterator ();
        checkIterator ("The removed integer is not noticed", all, l2);

        if (listener.getCount () != 1) {
            fail ("Nothing has not been fired");
        }
    }

    /** Checks whether set pairs does not fire when they should not.
    */
    public void testSetPairsDoesNotFire () {
        Object tmp = new Object ();

        ArrayList li = new ArrayList();
        li.add (tmp);
        li.addAll (Arrays.asList (INSTANCES));
        ic.set (li, null);
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (Integer.class));
        Iterator all = res.allInstances ().iterator ();
        checkIterator ("Integer is not there", all, Collections.nCopies (1, INSTANCES[0]));
        
        // change the pairs
        LL listener = new LL (res);
        res.addLookupListener (listener);

        ArrayList l2 = new ArrayList (li);
        l2.remove (tmp);
        ic.set (l2, null);

        all = lookup.lookup (new Lookup.Template (Object.class)).allInstances ().iterator ();
        checkIterator ("The removed integer is not noticed", all, l2);

        if (listener.getCount () != 0) {
            fail ("Something has been fired");
        }
    }
    
    /** Test whether after registration it is possible to find registered objects
    * 
     */
    public void testLookupAndAdd () throws Exception {
        addInstances (INSTANCES);

        for (int i = 0; i < INSTANCES.length; i++) {
            Object obj = INSTANCES[i];
            findAll (lookup, obj.getClass (), true);
        }
    }

    /** Tries to find all classes and superclasses in the lookup.
    */
    private void findAll (Lookup lookup, Class clazz, boolean shouldBeThere) {
        if (clazz == null) return;

        Object found = lookup.lookup (clazz);
        if (found == null) {
            if (shouldBeThere) {
                // should find at either instance or something else, but must
                // find at least something
                fail ("Lookup (" + clazz.getName () + ") found nothing");
            }
        } else {
            if (!shouldBeThere) {
                // should find at either instance or something else, but must
                // find at least something
                fail ("Lookup (" + clazz.getName () + ") found " + found);
            }
        }

        Lookup.Result res = lookup.lookup (new Lookup.Template (clazz));
        Collection collection = res.allInstances ();

        for (int i = 0; i < INSTANCES.length; i++) {
            boolean isSubclass = clazz.isInstance (INSTANCES[i]);
            boolean isThere = collection.contains (INSTANCES[i]);

            if (isSubclass != isThere) {
                // a problem found
                // should find at either instance or something else, but must
                // find at least something
                fail ("Lookup.Result (" + clazz.getName () + ") for " + INSTANCES[i] + " is subclass: " + isSubclass + " isThere: " + isThere);
            }
        }

        // go on for superclasses

        findAll (lookup, clazz.getSuperclass (), shouldBeThere);

        Class[] ies = clazz.getInterfaces ();
        for (int i = 0; i < ies.length; i++) {
            findAll (lookup, ies[i], shouldBeThere);
        }
    }
    
    /** Test if it is possible to remove a registered object. */
    public void testRemoveRegisteredObject() {
        Integer inst = new Integer(10);
        
        ic.add(inst);
        if (lookup.lookup(inst.getClass()) == null) {
            // should find an instance
            fail("Lookup (" + inst.getClass().getName () + ") found nothing");
        }
        
        ic.remove(inst);
        if (lookup.lookup(inst.getClass()) != null) {
            // should NOT find an instance
            fail("Lookup (" + inst.getClass().getName () +
                ") found an instance after remove operation");
        }
    }
    
    public void testCanReturnReallyStrangeResults () throws Exception {
        class QueryingPair extends org.openide.util.lookup.AbstractLookup.Pair {
            private Integer i = new Integer (434);
            
            //
            // do the test
            //
            
            public void doTest () throws Exception {
                ic.add (i);
                ic.addPair (this);
                
                Object found = lookup.lookup (QueryingPair.class);
                assertEquals ("This object is found", this, found);
            }
            
            
            //
            // Implementation of pair
            // 
        
            public java.lang.String getId() {
                return getType ().toString();
            }

            public java.lang.String getDisplayName() {
                return getId ();
            }

            public java.lang.Class getType() {
                return getClass ();
            }

            protected boolean creatorOf(java.lang.Object obj) {
                return obj == this;
            }

            protected boolean instanceOf(java.lang.Class c) {
                assertEquals ("Integer found or exception is thrown", i, lookup.lookup (Integer.class));
                return c.isAssignableFrom(getType ());
            }

            public java.lang.Object getInstance() {
                return this;
            }
            
            
        }
        
        
        QueryingPair qp = new QueryingPair ();
        qp.doTest ();
    }
    
    /** Test of firing events. */
    public void testLookupListener() {
        Integer inst = new Integer(10);
        Lookup.Result res = lookup.lookup(new Lookup.Template(inst.getClass()));
        res.allInstances ();
        
        LL listener = new LL(res);
        res.addLookupListener(listener);
        
        ic.add(inst);
        if (listener.getCount() == 0) {
            fail("None event fired during NbLookup.addPair()");
        }
        
        ic.remove(inst);
        if (listener.getCount() == 0) {
            fail("None event fired during NbLookup.removePair()");
        }
        
        ic.add(inst);
        if (listener.getCount() == 0) {
            fail("None event fired during second NbLookup.addPair()");
        }
        
        ic.remove(inst);
        if (listener.getCount() == 0) {
            fail("None event fired during second NbLookup.removePair()");
        }
    }
    
    /** Testing identity of the lookup.
     */
    public void testId () {
        AbstractLookup.Template templ;
        int cnt;
        
        addInstances (INSTANCES);
        
        AbstractLookup.Result res = lookup.lookup (new AbstractLookup.Template ());
        Iterator it;
        it = res.allItems ().iterator ();
        while (it.hasNext ()) {
            AbstractLookup.Item item = (AbstractLookup.Item)it.next ();
            
            templ = new AbstractLookup.Template (null, item.getId (), null);
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 1) {
                fail ("Identity lookup failed. Instances = " + cnt);
            }

            templ = new AbstractLookup.Template (item.getType (), item.getId (), null);
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 1) {
                fail ("Identity lookup with type failed. Instances = " + cnt);
            }
            
            templ = new AbstractLookup.Template (this.getClass (), item.getId (), null);
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 0) {
                fail ("Identity lookup with wrong type failed. Instances = " + cnt);
            }
            
            templ = new AbstractLookup.Template (null, null, item.getInstance ());
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 1) {
                fail ("Instance lookup failed. Instances = " + cnt);
            }

            templ = new AbstractLookup.Template (null, item.getId (), item.getInstance ());
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 1) {
                fail ("Instance & identity lookup failed. Instances = " + cnt);
            }
            
        }
    }
    
    /** Tests adding and removing.
     */
    public void testAddAndRemove () throws Exception {
        Object map = new javax.swing.ActionMap ();
        LL ll = new LL ();
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (map.getClass ()));
        res.allItems();
        res.addLookupListener (ll);
        ll.source = res;
        
        ic.add (map);
        
        assertEquals ("First change when adding", ll.getCount (), 1);
        
        ic.remove (map);
        
        assertEquals ("Second when removing", ll.getCount (), 1);
        
        ic.add (map);
        
        assertEquals ("Third when readding", ll.getCount (), 1);
        
        ic.remove (map);
        
        assertEquals ("Forth when reremoving", ll.getCount (), 1);
        
    }
    
    /** Will a class garbage collect even it is registered in lookup.
     */
    public void testGarbageCollect () throws Exception {
        ClassLoader l = new CL ();
        Class c = l.loadClass (Garbage.class.getName ());
        WeakReference ref = new WeakReference (c);

        lookup.lookup (c);
        
        // now test garbage collection
        c = null;
        l = null;
        impl.clearCaches ();
        assertGC ("The classloader has not been garbage collected!", ref);
    }
                
    /** Items are the same as results.
     */
    public void testItemsAndIntances () {
        addInstances (INSTANCES);
        
        Lookup.Template t = new Lookup.Template (Object.class);
        Lookup.Result r = lookup.lookup (t);
        Collection items = r.allItems ();
        Collection insts = r.allInstances ();
        
        if (items.size () != insts.size ()) {
            fail ("Different size of sets");
        }
        
        Iterator it = items.iterator ();
        while (it.hasNext ()) {
            Lookup.Item item = (Lookup.Item)it.next ();
            if (!insts.contains (item.getInstance ())) {
                fail ("Intance " + item.getInstance () + " is missing in " + insts);
            }
        }
    }
    
    /** Checks search for interface.
     */
    public void testSearchForInterface () {
        Lookup.Template t = new Lookup.Template (Serializable.class, null, null);
        
        assertNull("Nothing to find", lookup.lookupItem (t));
        
        Serializable s = new Serializable () {};
        ic.add (s);
        
        Lookup.Item item = lookup.lookupItem (t);
        assertNotNull ("Something found", item);
    }

    /** Test to add broken item if it incorrectly answers instanceOf questions.
     */
    public void testIncorectInstanceOf40364 () {
        final Long sharedLong = new Long (0);
        
        class P extends AbstractLookup.Pair {
            public boolean isLong;
            
            P (boolean b) {
                isLong = b;
            }
            
            protected boolean creatorOf (Object obj) {
                return obj == sharedLong;
            }
            
            public String getDisplayName () {
                return "";
            }
            
            public String getId () {
                return "";
            }
            
            public Object getInstance () {
                return sharedLong;
            }
            
            public Class getType () {
                return isLong ? Long.class : Number.class;
            }
            
            protected boolean instanceOf (Class c) {
                return c.isAssignableFrom (getType ());
            }
    
            public int hashCode () {
                return getClass ().hashCode ();
            }    

            public boolean equals (Object obj) {
                return obj != null && getClass ().equals (obj.getClass ());
            }
        }
        
        // to create the right structure in the lookup
        lookup.lookup (Object.class);
        lookup.lookup (Long.class);
        lookup.lookup (Number.class);
        
        P lng1 = new P (true);
        ic.addPair (lng1);

        P lng2 = new P (false);
        ic.setPairs (Collections.singleton (lng2));
        
        Collection res = lookup.lookup (new Lookup.Template (Object.class)).allItems ();
        assertEquals ("Just one pair", 1, res.size ());
    }

    public void testAbsolutelyCrazyWayToSimulateIssue48590ByChangingTheBehaviourOfEqualOnTheFly () throws Exception {
        class X implements testInterfaceInheritanceA, testInterfaceInheritanceB {
        }
        final X shared = new X ();
        
        class P extends AbstractLookup.Pair {
            public int howLong;
            
            P (int b) {
                howLong = b;
            }
            
            protected boolean creatorOf (Object obj) {
                return obj == shared;
            }
            
            public String getDisplayName () {
                return "";
            }
            
            public String getId () {
                return "";
            }
            
            public Object getInstance () {
                return shared;
            }
            
            public Class getType () {
                return howLong == 0 ? testInterfaceInheritanceB.class : testInterfaceInheritanceA.class;
            }
            
            protected boolean instanceOf (Class c) {
                return c.isAssignableFrom (getType ());
            }
    
            public int hashCode () {
                return getClass ().hashCode ();
            }    

            public boolean equals (Object obj) {
                if (obj instanceof P) {
                    P p = (P)obj;
                    if (this.howLong > 0) {
                        this.howLong--;
                        return false;
                    }
                    if (p.howLong > 0) {
                        p.howLong--;
                        return false;
                    }
                    return getClass ().equals (p.getClass ());
                }
                return false;
            }
        }
        
        // to create the right structure in the lookup
        Lookup.Result a = lookup.lookup (new Lookup.Template (testInterfaceInheritanceA.class));
        Lookup.Result b = lookup.lookup (new Lookup.Template (testInterfaceInheritanceB.class));
        
        P lng1 = new P (0);
        ic.addPair (lng1);
        
        assertEquals ("One in a", 1, a.allItems ().size ());
        assertEquals ("One in b", 1, b.allItems ().size ());

        P lng2 = new P (1);
        

        /* Following call used to generate this exception:
    java.lang.IllegalStateException: Duplicate pair in treePair1:  pair2:  index1: 0 index2: 0 item1: org.openide.util.lookup.AbstractLookupBaseHid$1X@1a457b6 item2: org.openide.util.lookup.AbstractLookupBaseHid$1X@1a457b6 id1: 7a78d3 id2: 929206
	at org.openide.util.lookup.ALPairComparator.compare(ALPairComparator.java:52)
	at java.util.Arrays.mergeSort(Arrays.java:1284)
	at java.util.Arrays.sort(Arrays.java:1223)
	at java.util.Collections.sort(Collections.java:159)
	at org.openide.util.lookup.InheritanceTree.retainAllInterface(InheritanceTree.java:753)
	at org.openide.util.lookup.InheritanceTree.retainAll(InheritanceTree.java:183)
	at org.openide.util.lookup.DelegatingStorage.retainAll(DelegatingStorage.java:83)
	at org.openide.util.lookup.AbstractLookup.setPairsAndCollectListeners(AbstractLookup.java:238)
	at org.openide.util.lookup.AbstractLookup.setPairs(AbstractLookup.java:203)
	at org.openide.util.lookup.AbstractLookup$Content.setPairs(AbstractLookup.java:885)
	at org.openide.util.lookup.AbstractLookupBaseHid.testAbsolutelyCrazyWayToSimulateIssue48590ByChangingTheBehaviourOfEqualOnTheFly(AbstractLookupBaseHid.java:696)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at org.netbeans.junit.NbTestCase.run(NbTestCase.java:119)
    */  
        ic.setPairs (Collections.singleton (lng2));

        
    }
    
    public void testInstancesArePreservedFoundWhenFixing48590 () throws Exception {
        class X implements Runnable, Serializable {
            public void run () {
                
            }
            
            public void assertOnlyMe (String msg, Lookup.Result res) {
                Collection col = res.allInstances ();
                assertEquals (msg + " just one", 1, col.size ());
                assertSame (msg + " and it is me", this, col.iterator ().next ());
            }
        }
        
        Lookup.Result runnable = lookup.lookup (new Lookup.Template (Runnable.class));
        Lookup.Result serial = lookup.lookup (new Lookup.Template (Serializable.class));
        
        
        X x = new X ();
        ic.add (x);
        
        
        x.assertOnlyMe ("x implements it (1)", runnable);
        x.assertOnlyMe ("x implements it (2)", serial);
        
        ic.set (Collections.singleton (x), null);
        
        x.assertOnlyMe ("x implements it (3)", runnable);
        x.assertOnlyMe ("x implements it (4)", serial);
    }
    
    /** Testing lookup of inherited classes. */
    public void testInheritance() {
        class A {}
        class B extends A implements java.rmi.Remote {}
        class BB extends B {}
        class C extends A implements java.rmi.Remote {}
        class D extends A {}
        
        A[] types = {new B(), new BB(), new C(), new D()};
        
        for (int i = 0; i < types.length; i++) {
            ic.add(types[i]);
            if (lookup.lookup(types[i].getClass()) == null) {
                // should find an instance
                fail("Lookup (" + types[i].getClass().getName () + ") found nothing");
            }
        }
        
        int size1, size2;
        
        //interface query
        size1 = lookup.lookup(new Lookup.Template(java.rmi.Remote.class)).allInstances().size();
        size2 = countInstances(types, java.rmi.Remote.class);
        
        if (size1 != size2) fail("Lookup with interface failed: " + size1 + " != " + size2);
        
        // superclass query
        size1 = lookup.lookup(new Lookup.Template(A.class)).allInstances().size();
        size2 = countInstances(types, A.class);
        
        if (size1 != size2) fail("Lookup with superclass failed: " + size1 + " != " + size2);
    }
    
    /** Test interface inheritance.
     */
    public void testInterfaceInheritance() {
        testInterfaceInheritanceA[] types = {
            new testInterfaceInheritanceB() {}, 
            new testInterfaceInheritanceBB() {}, 
            new testInterfaceInheritanceC() {}, 
            new testInterfaceInheritanceD() {}
        };
        
        for (int i = 0; i < types.length; i++) {
            ic.add(types[i]);
            if (lookup.lookup(types[i].getClass()) == null) {
                // should find an instance
                fail("Lookup (" + types[i].getClass().getName () + ") found nothing");
            }
        }
        
        int size1, size2;
        
        //interface query
        LL l = new LL ();
        Lookup.Result res = lookup.lookup(new Lookup.Template(java.rmi.Remote.class));
        l.source = res;
        size1 = res.allInstances().size();
        size2 = countInstances(types, java.rmi.Remote.class);
        
        if (size1 != size2) fail("Lookup with interface failed: " + size1 + " != " + size2);
        
        // superclass query
        size1 = lookup.lookup(new Lookup.Template(testInterfaceInheritanceA.class)).allInstances().size();
        size2 = countInstances(types, testInterfaceInheritanceA.class);
        
        if (size1 != size2) fail("Lookup with superclass failed: " + size1 + " != " + size2);
        
        res.addLookupListener (l);
        ic.remove (types[0]);
        
        if (l.getCount () != 1) {
            fail ("No notification that a Remote is removed");
        }
    }
    
    /** Checks whether the AbstractLookup is guarded against modifications
     * while doing some kind of modification.
     */
    public void testModificationArePreventedWhenDoingModifications () throws Exception {
        BrokenPair broken = new BrokenPair (true, false);
        ic.addPair (broken);
        
        Lookup.Template templ = new Lookup.Template (BrokenPair.class);
        Object item = lookup.lookupItem (templ);
        assertEquals ("Broken is found", broken, item);
    }
    
    public void testModificationArePreventedWhenDoingModificationsResult () throws Exception {
        BrokenPair broken = new BrokenPair (false, true);
        ic.addPair (broken);
        
        Lookup.Template templ = new Lookup.Template (BrokenPair.class);
        
        Collection c = lookup.lookup (templ).allInstances();
        assertEquals ("One item", 1, c.size ());
        assertEquals ("Broken is found again", broken, c.iterator().next ());
    }
    
    public void testModificationArePreventedWhenDoingModificationsItemAndResult () throws Exception {
        BrokenPair broken = new BrokenPair (false, true);
        ic.addPair (broken);
        
        Lookup.Template templ = new Lookup.Template (BrokenPair.class);
        Object item = lookup.lookupItem (templ);
        assertEquals ("Broken is found", broken, item);
        
        Collection c = lookup.lookup (templ).allInstances();
        assertEquals ("One item", 1, c.size ());
        assertEquals ("Broken is found again", broken, c.iterator().next ());
    }

    public void testModificationArePreventedWhenDoingModificationsResultAndItem () throws Exception {
        BrokenPair broken = new BrokenPair (false, true);
        ic.addPair (broken);
        
        Lookup.Template templ = new Lookup.Template (BrokenPair.class);
        Collection c = lookup.lookup (templ).allInstances();
        assertEquals ("One item", 1, c.size ());
        assertEquals ("Broken is found again", broken, c.iterator().next ());
        
        Object item = lookup.lookupItem (templ);
        assertEquals ("Broken is found", broken, item);
    }
    
    public void testAddALotOfPairsIntoTheLookupOneByOne () throws Exception {
        Lookup.Result res = lookup.lookup (new Lookup.Template (Integer.class));
        for (int i = 0; i < 1000; i++) {
            ic.add (new Integer (i));
        }
        assertEquals (
            "there is the right count", 
            1000, 
            res.allItems().size ()
        );
    }
    
    public void testAddALotOfPairsIntoTheLookup () throws Exception {
        ArrayList arr = new ArrayList ();
        for (int i = 0; i < 1000; i++) {
            arr.add (new Integer (i));
        }
        ic.set (arr, null);
        
        assertEquals (
            "there is the right count", 
            1000, 
            lookup.lookup (new Lookup.Template (Integer.class)).allItems().size ()
        );
    }

    
    public void testDoubleAddIssue35274 () throws Exception {
        class P extends AbstractLookup.Pair {
            protected boolean creatorOf(Object obj) { return false; }
            public String getDisplayName() { return ""; }
            public String getId() { return ""; }
            public Object getInstance() { return null; }
            public Class getType() { return Object.class; }
            protected boolean instanceOf(Class c) { return c.isAssignableFrom(getType ()); }
            
            public int hashCode () { return getClass ().hashCode(); };
            public boolean equals (Object obj) { return getClass () == obj.getClass (); };
        }
        
        P p = new P ();
        
        ic.addPair (p);
        ic.addPair (p);
        
        Lookup.Result result = lookup.lookup (new Lookup.Template (Object.class));
        Collection res = result.allItems ();
        assertEquals ("One item there", 1, res.size ());
        assertTrue ("It is the p", p == res.iterator ().next ());
        
        P p2 = new P ();
        ic.addPair (p2);
        
        WeakReference ref = new WeakReference (result);
        result = null;
        assertGC ("The result can disappear", ref);
        
        impl.clearCaches ();
        
        result = lookup.lookup (new Lookup.Template (Object.class));
        res = result.allItems ();
        assertEquals ("One item is still there", 1, res.size ());
        assertTrue ("But the p2 replaced p", p2 == res.iterator ().next ());
        
    }
    
    /** Test for proper serialization.
     */
    public void testSerializationSupport () throws Exception {
        doSerializationSupport (1);
    }
    public void testDoubleSerializationSupport () throws Exception {
        doSerializationSupport (2);
    }

    private void doSerializationSupport (int count) throws Exception {
        if (lookup instanceof Serializable) {
            ic.addPair (new SerialPair ("1"));
            ic.addPair (new SerialPair ("2"));
            ic.addPair (new SerialPair ("3"));

            Lookup l = (Lookup)new org.openide.util.io.NbMarshalledObject (lookup).get ();

            assertEquals ("Able to answer simple query", "1", l.lookup (String.class));

            assertEquals ("Three objects there", 3, l.lookup (new Lookup.Template (String.class)).allInstances().size ());

            while (count-- > 0) {
                l = (Lookup)new org.openide.util.io.NbMarshalledObject (l).get ();
            }

            assertEquals ("Able to answer simple query", "1", l.lookup (String.class));

            assertEquals ("Three objects there", 3, l.lookup (new Lookup.Template (String.class)).allInstances().size ());
        }
    }

    /** When a lookup with two different versions of the same class 
     * get's serialized, the results may be very bad. 
     */
    public void testSerializationOfTwoClassesWithTheSameName () throws Exception {
        if (lookup instanceof Serializable) {
            doTwoSerializedClasses (false, false);
        }
    }
    public void testSerializationOfTwoClassesWithTheSameNameButQueryBeforeSave () throws Exception {
        if (lookup instanceof Serializable) {
            doTwoSerializedClasses (true, false);
        }
    }
    public void testSerializationOfTwoClassesWithTheSameNameWithBroken () throws Exception {
        if (lookup instanceof Serializable) {
            doTwoSerializedClasses (false, true);
        }
    }
    public void testSerializationOfTwoClassesWithTheSameNameButQueryBeforeSaveWithBroken () throws Exception {
        if (lookup instanceof Serializable) {
            doTwoSerializedClasses (true, true);
        }
    }
   
    private void doTwoSerializedClasses (boolean queryBeforeSerialization, boolean useBroken) throws Exception {
        ClassLoader loader = new CL ();
        Class c = loader.loadClass (Garbage.class.getName ());

        // in case of InheritanceTree it creates a slot for class Garbage
        lookup.lookup(c);

        // but creates new instance and adds it into the lookup
        // without querying for it
        loader = new CL ();
        c = loader.loadClass (Garbage.class.getName ());

        Object theInstance = c.newInstance ();

        ic.addPair (new SerialPair (theInstance));

        Broken2Pair broken = null;
        if (useBroken) {
            broken = new Broken2Pair ();
            ic.addPair (broken);
            
            assertNull (
                "We need to create the slot for the List as " +
                "the Broken2Pair will ask for it after deserialization", 
                lookup.lookup (java.awt.List.class)
            );
        }

        if (queryBeforeSerialization) {
            assertEquals ("Instance is found", theInstance, lookup.lookup (c));
        }
        
        // replace the old lookup with new one
        lookup = (Lookup)new org.openide.util.io.NbMarshalledObject (lookup).get ();
        
        Lookup.Result result = lookup.lookup (new Lookup.Template (Garbage.class));
        assertEquals ("One item is the result", 1, result.allInstances ().size ());
        Object r = result.allInstances ().iterator ().next ();
        assertNotNull("A value is found", r);
        assertEquals ("It is of the right class", Garbage.class, r.getClass());
    }
   
    /** Test of reorder and item change which used to fail on interfaces.
     */
    public void testReoderingIssue13779 () throws Exception {
        LinkedList arr = new LinkedList ();
        
        class R extends Exception implements Cloneable {
        }
        Object o1 = new R ();
        Object o2 = new R ();
        Object o3 = new R ();
        
        arr.add (o1);
        arr.add (o2);
        
        ic.set (arr, null);
        
        Lookup.Result objectResult = lookup.lookup (new Lookup.Template (Exception.class));
        Lookup.Result interfaceResult = lookup.lookup (new Lookup.Template (Cloneable.class));
        objectResult.allItems ();
        interfaceResult.allItems ();
        
        LL l1 = new LL (objectResult);
        LL l2 = new LL (interfaceResult);
        
        objectResult.addLookupListener(l1);
        interfaceResult.addLookupListener(l2);
        
        arr.addFirst (o3);
        
        ic.set (arr, null);
        
        assertEquals ("One change on objects", 1, l1.getCount ());
        assertEquals ("One change on interfaces", 1, l2.getCount ());
        
        arr.addFirst (new Cloneable () { });
        ic.set (arr, null);
        
        assertEquals ("No change on objects", 0, l1.getCount ());
        assertEquals ("But one change on interfaces", 1, l2.getCount ());
        
    }
    
    public void testDeadlockBetweenProxyResultAndLookupIssue47772 () throws Exception {
        final String myModule = "My Module";
        ic.add (myModule);
        
        class MyProxy extends ProxyLookup {
            public MyProxy () {
                super (new Lookup[] { lookup });
            }
        }
        final MyProxy my = new MyProxy ();
        
        final Lookup.Result allModules = my.lookup (new Lookup.Template (String.class));
        
        class PairThatNeedsInfoAboutModules extends AbstractLookup.Pair {
            public String getDisplayName () {
                return "Need a module";
            }
            public String getId () {
                return getDisplayName ();
            }
            public Class getType () {
                return Integer.class;
            }
            protected boolean instanceOf (Class c) {
                if (c == Integer.class) {
                    synchronized (this) {
                        notifyAll ();
                        try {
                            wait (1000);
                        } catch (InterruptedException ex) {
                            fail (ex.getMessage ());
                        }
                    }
                    java.util.Collection coll = allModules.allInstances ();
                    assertEquals ("Size is 1", 1, coll.size ());
                    assertEquals ("My module is there", myModule, coll.iterator ().next ());
                }
                return c.isAssignableFrom (Integer.class);
            }
            
            public Object getInstance () {
                return new Integer (10);
            }
            
            protected boolean creatorOf (Object obj) {
                return new Integer (10).equals (obj);
            }
        }
        
        PairThatNeedsInfoAboutModules pair = new PairThatNeedsInfoAboutModules ();
        ic.addPair (pair);
        
        synchronized (pair) {
            class BlockInInstanceOf implements Runnable {
                public void run () {
                    Integer i = (Integer)my.lookup (Integer.class);
                    assertEquals (new Integer (10), i);
                }
            }
            BlockInInstanceOf blk = new BlockInInstanceOf ();
            RequestProcessor.getDefault ().post (blk);
            pair.wait ();
        }
        
        java.util.Collection coll = allModules.allInstances ();
        assertEquals ("Size is 1", 1, coll.size ());
        assertEquals ("My module is there", myModule, coll.iterator ().next ());
    }

    public void testAWayToGenerateProblem13779 () {
        ic.add (new Integer (1));
        ic.add (new Integer (2));
        ic.add (new Integer (1));
        ic.add (new Integer (2));
        
        Collection c = lookup.lookup (new Lookup.Template (Integer.class)).allInstances ();
        assertEquals ("There are two objects", 2, c.size ());
        
    }
    
    /** Replacing items with different objects.
     */
    public void testReplacingObjectsDoesNotGenerateException () throws Exception {
        LinkedList arr = new LinkedList ();
        
        class R extends Exception implements Cloneable {
        }
        arr.add (new R ());
        arr.add (new R ());
        
        ic.set (arr, null);
        
        arr.clear();
        
        arr.add (new R ());
        arr.add (new R ());
        
        ic.set (arr, null);
    }

    public void testAfterDeserializationNoQueryIsPeformedOnAlreadyQueriedObjects() throws Exception {
        if (! (lookup instanceof Serializable)) {
            // well this test works only for serializable lookups
            return;
        }
        
        SerialPair my = new SerialPair ("no");
        ic.addPair (my);
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (String.class));
        assertEquals ("One instance", 1, res.allInstances().size ());
        assertEquals ("my.instanceOf called once", 1, my.countInstanceOf);
        
        Lookup serial = (Lookup)new org.openide.util.io.NbMarshalledObject (lookup).get ();
        
        Lookup.Result r2 = serial.lookup(new Lookup.Template(String.class));
        
        assertEquals ("One item", 1, r2.allItems ().size ());
        Object one = r2.allItems().iterator().next ();
        assertEquals ("The right class", SerialPair.class, one.getClass());
        SerialPair p = (SerialPair)one;
        
        assertEquals ("p.instanceOf has not been queried", 0, p.countInstanceOf);
    }
    
    /** Checks the iterator */
    private void checkIterator (String msg, Iterator it1, List list) {
        int cnt = 0;
        Iterator it2 = list.iterator ();
        while (it1.hasNext () && it2.hasNext ()) {
            Object n1 = it1.next ();
            Object n2 = it2.next ();
            
            if (n1 != n2) {
                fail (msg + " iterator[" + cnt + "] = " + n1 + " but list[" + cnt + "] = " + n2);
            }
            
            cnt++;
        }
        
        if (it1.hasNext ()) {
            fail ("Iterator has more elements than list");
        }
        
        if (it2.hasNext ()) {
            fail ("List has more elements than iterator");
        }
    }
    
    
    public void testResultsAreUnmodifyableOrAtLeastTheyDoNotPropagateToCache() throws Exception {
        String s = "Ahoj";
        
        ic.add(s);
        
        Lookup.Result res = lookup.lookup(new Template(String.class));
        
        for (int i = 1; i < 5; i++) {
            Collection c1 = res.allInstances();
            Collection c2 = res.allClasses();
            Collection c3 = res.allItems();

            assertTrue(i + ": c1 has it", c1.contains(s));
            assertTrue(i + ": c2 has it", c2.contains(s.getClass()));
            assertEquals(i + ": c3 has one", 1, c3.size());
            Lookup.Item item = (Lookup.Item) c3.iterator().next();
            assertEquals(i + ": c3 has it", s, item.getInstance());

            try {
                c1.remove(s);
                assertEquals("No elements now", 0, c1.size());
            } catch (UnsupportedOperationException ex) {
                // ok, this need not be supported
            }
            try {
                c2.remove(s.getClass());
                assertEquals("No elements now", 0, c2.size());
            } catch (UnsupportedOperationException ex) {
                // ok, this need not be supported
            }
            try {
                c3.remove(item);
                assertEquals("No elements now", 0, c3.size());
            } catch (UnsupportedOperationException ex) {
                // ok, this need not be supported
            }
        }
    }
    
    
    public void testChangeOfNodeDoesNotFireChangeInActionMap() {
        ActionMap am = new ActionMap();
        Lookup s = Lookups.singleton(am);
        doChangeOfNodeDoesNotFireChangeInActionMap(am, s);
    }

    public void testChangeOfNodeDoesNotFireChangeInActionMapWithBeforeLookup() {
        final ActionMap am = new ActionMap();
        
        class Before extends AbstractLookup {
            public InstanceContent ic;
            public Before() {
                this(new InstanceContent());
            }
            
            private Before(InstanceContent ic) {
                super(ic);
                this.ic = ic;
            }

            protected void beforeLookup(Template template) {
                if (ic != null) {
                    ic.add(am);
                    ic = null;
                }
            }
        }
        
        Before s = new Before();
        doChangeOfNodeDoesNotFireChangeInActionMap(am, s);
        
        assertNull("beforeLookup called once", s.ic);
    }
    
    private void doChangeOfNodeDoesNotFireChangeInActionMap(final ActionMap am, Lookup actionMapLookup) {
        Lookup[] lookups = { lookup, actionMapLookup };
        ProxyLookup proxy = new ProxyLookup(lookups);
        Lookup.Result res = proxy.lookup(new Lookup.Template(ActionMap.class));
        LL ll = new LL();
        res.addLookupListener(ll);

        Collection c = res.allInstances();
        assertFalse("Has next", c.isEmpty());
        
        ActionMap am1 = (ActionMap)c.iterator().next();
        assertEquals("Am is there", am, am1);
        
        assertEquals("No change in first get", 0, ll.getCount());
        
        Object m1 = new InputMap();
        Object m2 = new InputMap();
        
        ic.add(m1);
        assertEquals("No change in ActionMap 1", 0, ll.getCount());
        ic.set(Collections.singletonList(m2), null);
        assertEquals("No change in ActionMap 2", 0, ll.getCount());
        ic.add(m2);
        assertEquals("No change in ActionMap 3", 0, ll.getCount());
        proxy.setLookups(new Lookup[]{ lookup, actionMapLookup, Lookup.EMPTY });
        assertEquals("No change in ActionMap 4", 0, ll.getCount());
        
        ActionMap am2 = (ActionMap)proxy.lookup(ActionMap.class);
        assertEquals("Still the same action map", am, am2);
        
        
        class Before extends AbstractLookup {
            public InstanceContent ic;
            public Before() {
                this(new InstanceContent());
            }
            
            private Before(InstanceContent ic) {
                super(ic);
                this.ic = ic;
            }

            protected void beforeLookup(Template template) {
                if (ic != null) {
                    ic.add(am);
                    ic = null;
                }
            }
        }
        
        Before s = new Before();
        
        // adding different Before, but returning the same instance
        // this happens with metaInfServices lookup often, moreover
        // it adds the instance in beforeLookup, which confuses a lot
        proxy.setLookups(new Lookup[]{ lookup, new Before() });
        assertEquals("No change in ActionMap 5", 0, ll.getCount());
        
        
    }
    
    /** Adds instances to the instance lookup.
     */
    private void addInstances (Object[] instances) {
        for (int i = 0; i < instances.length; i++) {
            ic.add(instances[i]);
        }
    }
    
    /** Count instances of clazz in an array. */
    private int countInstances (Object[] objs, Class clazz) {
        int count = 0;
        for (int i = 0; i < objs.length; i++) {
            if (clazz.isInstance(objs[i])) count++;
        }
        return count;
    }
    
    /** Counting listener */
    protected static class LL implements LookupListener {
        private int count = 0;
        public Object source;
        
        public LL () {
            this (null);
        }
        
        public LL (Object source) {
            this.source = source;
        }
        
        public void resultChanged(LookupEvent ev) {
            ++count;
            if (source != null) {
                assertSame ("Source is the same", source, ev.getSource ());
//                assertSame ("Result is the same", source, ev.getResult ());
            }
        }

        public int getCount() {
            int i = count;
            count = 0;
            return i;
        }
    };

    /** A set of interfaces for testInterfaceInheritance
     */
    interface testInterfaceInheritanceA {}
    interface testInterfaceInheritanceB extends testInterfaceInheritanceA, java.rmi.Remote {}
    interface testInterfaceInheritanceBB extends testInterfaceInheritanceB {}
    interface testInterfaceInheritanceC extends testInterfaceInheritanceA, java.rmi.Remote {}
    interface testInterfaceInheritanceD extends testInterfaceInheritanceA {}
    
    /** A special class for garbage test */
    public static final class Garbage extends Object implements Serializable {
        static final long serialVersionUID = 435340912534L;
    }
    

    /* A classloader that can load one class in a special way */
    private static class CL extends ClassLoader {
        public CL () {
            super (null);
        }

        public Class findClass (String name) throws ClassNotFoundException {
            if (name.equals (Garbage.class.getName ())) {
                String n = name.replace ('.', '/');
                java.io.InputStream is = getClass ().getResourceAsStream ("/" + n + ".class");
                byte[] arr = new byte[8096];
                try {
                    int cnt = is.read (arr);
                    if (cnt == arr.length) {
                        fail ("Buffer to load the class is not big enough");
                    }

                    return defineClass (name, arr, 0, cnt);
                } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                        fail ("IO Exception");
                        return null;
                }
            } else {
                return null;
            }
        }

        /** Convert obj to other object. There is no need to implement
         * cache mechanism. It is provided by AbstractLookup.Item.getInstance().
         * Method should be called more than once because Lookup holds
         * just weak reference.
         */
        public Object convert(Object obj) {
            return null;
        }

        /** Return type of converted object. */
        public Class type(Object obj) {
            try {
                return loadClass (Garbage.class.getName ());
            } catch (ClassNotFoundException ex) {
                fail ("Class not found");
                throw new InternalError ();
            }
        }
    }

    public static final class SerialPair extends AbstractLookup.Pair
    implements java.io.Serializable {
        static final long serialVersionUID = 54305834L;
        private Object value;
        public transient int countInstanceOf;
        
        public SerialPair (Object value) {
            this.value = value;
        }
        
        protected boolean creatorOf(Object obj) {
            return obj == value;
        }
        
        public String getDisplayName() {
            return getId ();
        }
        
        public String getId() {
            return value.toString();
        }
        
        public Object getInstance() {
            return value;
        }
        
        public Class getType() {
            return value.getClass ();
        }
        
        protected boolean instanceOf(Class c) {
            countInstanceOf++;
            return c.isInstance(value);
        }
    } // end of SerialPair
    
    private static class BrokenPair extends AbstractLookup.Pair {
        private transient ThreadLocal IN = new ThreadLocal ();
        private boolean checkModify;
        private boolean checkQuery;
        
        public BrokenPair (boolean checkModify, boolean checkQuery) {
            this.checkModify = checkModify;
            this.checkQuery = checkQuery;
        }
        
        protected boolean creatorOf(Object obj) { return this == obj; }
        public String getDisplayName() { return "Broken"; }
        public String getId() { return "broken"; }
        public Object getInstance() { return this; }
        public Class getType() { return getClass (); }
        protected boolean instanceOf(Class c) { 
            
            if (checkQuery) {
                if (IN.get () == null) {
                    try {
                        IN.set (this);
                        // broken behaviour, tries to modify the lookup
                        // queries have to survive

                        running.lookup.lookup (java.awt.List.class);

                        // 
                        // creation of new result has to survive as well
                        Lookup.Result myQuery = running.lookup.lookup (new Lookup.Template (java.awt.Button.class));
                        Collection all = myQuery.allItems ();
                    } finally {
                        IN.set (null);
                    }
                }
            }
                

            if (checkModify) {
                //
                // modifications should fail
                //

                try {
                    running.ic.addPair (new SerialPair (""));
                    fail ("Modification from a query should be prohibited");
                } catch (IllegalStateException ex) {
                }
                
                try {
                    running.ic.removePair (this);
                    fail ("This has to throw the exception");
                } catch (IllegalStateException ex) {
                }
                try {
                    running.ic.setPairs (Collections.EMPTY_SET);
                    fail ("This has to throw the exception as well");
                } catch (IllegalStateException ex) {
                }
            }
            
            return c.isAssignableFrom(getType ()); 
        }
    } // end of BrokenPair
    
    private static class Broken2Pair extends AbstractLookup.Pair {
        static final long serialVersionUID = 4532587018501L;
        public transient ThreadLocal IN;
        
        public Broken2Pair () {
        }
        
        private void writeObject (java.io.ObjectOutputStream oos) throws java.io.IOException {
        }
        
        private void readObject (java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
            IN = new ThreadLocal ();
        }
        
        protected boolean creatorOf(Object obj) { return this == obj; }
        public String getDisplayName() { return "Broken"; }
        public String getId() { return "broken"; }
        public Object getInstance() { return this; }
        public Class getType() { return getClass (); }
        protected boolean instanceOf(Class c) { 
            
            // behaviour gets broken only after deserialization
            if (IN != null && IN.get () == null) {
                try {
                    IN.set (this);

                    // creation of new result has to survive as well
                    Lookup.Result myQuery = running.lookup.lookup (new Lookup.Template (java.awt.List.class));
                    Collection all = myQuery.allItems ();
                } finally {
                    IN.set (null);
                }
            }
            
            return c.isAssignableFrom(getType ()); 
        }
    } // end of Broken2Pair    
}
