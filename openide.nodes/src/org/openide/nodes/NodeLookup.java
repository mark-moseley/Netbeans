/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.AbstractLookup.Pair;

/** A lookup that represents content of a Node.getCookie and the node itself.
 *
 *
 * @author  Jaroslav Tulach
 */
final class NodeLookup extends AbstractLookup {
    /** See #40734 and NodeLookupTest and CookieActionIsTooSlowTest.
     * When finding action state for FilterNode, the action might been
     * triggered way to many times, due to initialization in beforeLookup
     * that triggered LookupListener and PROP_COOKIE change.
     */
    static final ThreadLocal<Node> NO_COOKIE_CHANGE = new ThreadLocal<Node>();

    /** Set of Classes that we have already queried <type>Class</type> */
    private java.util.Collection<Class> queriedCookieClasses = new ArrayList<Class>();

    /** node we are associated with
     */
    private Node node;

    /** New flat lookup.
     */
    public NodeLookup(Node n) {
        super();

        this.node = n;
        addPair(new LookupItem(n));
    }

    /** Calls into Node to find out if it has a cookie of given class.
     * It does special tricks to make CookieSet.Entry work.
     *
     * @param node node to ask
     * @param c class to query
     * @param colleciton to put Pair into if found
     */
    private static void addCookie(Node node, Class<?> c, 
            Collection<AbstractLookup.Pair> collection, 
            java.util.Map<AbstractLookup.Pair, Class> fromPairToClass) {
        Object res;
        Collection<AbstractLookup.Pair> pairs;
        Object prev = CookieSet.entryQueryMode(c);

        try {
            @SuppressWarnings("unchecked")
            Class<? extends Node.Cookie> fake = (Class<? extends Node.Cookie>)c;
            res = node.getCookie(fake);
        } finally {
            pairs = CookieSet.exitQueryMode(prev);
        }

        if (pairs == null) {
            if (res == null) {
                return;
            }

            pairs = Collections.singleton((AbstractLookup.Pair)new LookupItem(res));
        }

        collection.addAll(pairs);
        for (AbstractLookup.Pair p : pairs) {
            Class<?> oldClazz = fromPairToClass.get(p);
            if (oldClazz == null || c.isAssignableFrom(oldClazz)) {
                fromPairToClass.put(p, c);
            }
        }
    }

    /** Notifies subclasses that a query is about to be processed.
     * @param template the template
     */
    protected final void beforeLookup(Template template) {
        Class type = template.getType();

        if (type == Object.class) {
            // ok, this is likely query for everything
            java.util.Set all;
            Object prev = null;

            try {
                prev = CookieSet.entryAllClassesMode();

                Object ignoreResult = node.getCookie(Node.Cookie.class);
            } finally {
                all = CookieSet.exitAllClassesMode(prev);
            }

            Iterator it = all.iterator();

            while (it.hasNext()) {
                Class c = (Class) it.next();
                updateLookupAsCookiesAreChanged(c);
            }

            // update Node.Cookie if not yet
            if (!queriedCookieClasses.contains(Node.Cookie.class)) {
                updateLookupAsCookiesAreChanged(Node.Cookie.class);
            }
        }

        if (!queriedCookieClasses.contains(type)) {
            updateLookupAsCookiesAreChanged(type);
        }
    }

    public void updateLookupAsCookiesAreChanged(Class toAdd) {
        java.util.Collection<AbstractLookup.Pair> instances;
        java.util.Map<AbstractLookup.Pair, Class> fromPairToQueryClass;

        // if it is cookie change, do the rescan, try to keep order
        synchronized (this) {
            if (toAdd != null) {
                if (queriedCookieClasses.contains(toAdd)) {
                    // if this class has already been added, go away
                    return;
                }

                queriedCookieClasses.add(toAdd);
            }

            instances = new java.util.LinkedHashSet<AbstractLookup.Pair>(queriedCookieClasses.size());
            fromPairToQueryClass = new java.util.LinkedHashMap<AbstractLookup.Pair, Class>();

            java.util.Iterator<Class> it = /* #74334 */new ArrayList<Class>(queriedCookieClasses).iterator();
            LookupItem nodePair = new LookupItem(node);
            instances.add(nodePair);
            fromPairToQueryClass.put(nodePair, Node.class);

            while (it.hasNext()) {
                Class c = it.next();
                addCookie(node, c, instances, fromPairToQueryClass);
            }
        }

        final java.util.Map<AbstractLookup.Pair, Class> m = fromPairToQueryClass;

        class Cmp implements java.util.Comparator<AbstractLookup.Pair> {
            public int compare(AbstractLookup.Pair p1, AbstractLookup.Pair p2) {
                Class<?> c1 = m.get(p1);
                Class<?> c2 = m.get(p2);
                
                if (c1 == c2) {
                    return 0;
                }

                if (c1.isAssignableFrom(c2)) {
                    return -1;
                }

                if (c2.isAssignableFrom(c1)) {
                    return 1;
                }

                if (c1.isAssignableFrom(p2.getType())) {
                    return -1;
                }

                if (c2.isAssignableFrom(p1.getType())) {
                    return 1;
                }

                return 0;
            }
        }

        java.util.ArrayList<AbstractLookup.Pair> list = new java.util.ArrayList<AbstractLookup.Pair>(instances);
        java.util.Collections.sort(list, new Cmp());

        if (toAdd == null) {
            setPairs(list);
        } else {
            Node prev = NO_COOKIE_CHANGE.get();

            try {
                NO_COOKIE_CHANGE.set(node);

                // doing the setPairs under entryQueryMode guarantees that 
                // FilterNode will ignore the change
                setPairs(list);
            } finally {
                NO_COOKIE_CHANGE.set(prev);
            }
        }
    }

    /** Simple Pair to hold cookies and nodes */
    private static class LookupItem extends AbstractLookup.Pair {
        private Object instance;

        public LookupItem(Object instance) {
            this.instance = instance;
        }

        public String getDisplayName() {
            return getId();
        }

        public String getId() {
            return instance.toString();
        }

        public Object getInstance() {
            return instance;
        }

        public Class getType() {
            return instance.getClass();
        }

        public boolean equals(Object object) {
            if (object instanceof LookupItem) {
                return instance == ((LookupItem) object).getInstance();
            }

            return false;
        }

        public int hashCode() {
            return instance.hashCode();
        }

        protected boolean creatorOf(Object obj) {
            return instance == obj;
        }

        protected boolean instanceOf(Class c) {
            return c.isInstance(instance);
        }
    }
     // End of LookupItem class
}
