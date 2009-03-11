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
package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.*;

import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/** Support class for storing cookies and
* retriving them by representation class.
* Provides simple notifications about changes
* in cookies.
*
* @author Jaroslav Tulach
*/
public final class CookieSet extends Object implements Lookup.Provider {
    /** variable to allow effecient communication with NodeLookup, Node.Cookie or Class or Set */
    private static ThreadLocal<Object> QUERY_MODE = new ThreadLocal<Object>();

    /** list of cookies (Class, Node.Cookie) */
    private HashMap<Class, R> map = new HashMap<Class,R>(31);

    private final ChangeSupport cs = new ChangeSupport(this);
    
    /** potential instance content */
    private final CookieSetLkp ic;
    /** lookup to use return from the cookie set, if initialized */
    private Lookup lookup;

    /** Default constructor. */
    public CookieSet() {
        this(null, null);
    }
    
    private CookieSet(CookieSetLkp ic, Lookup lookup) {
        this.ic = ic;
        this.lookup = lookup;
    }
    
    /** Factory method to create new, general purpose cookie set. 
     * The <q>general purpose</q> means that it is possible to store
     * any object, into the cookie set and then obtain it using {@link #getLookup}
     * and queries on the returned {@link Lookup}. The before object can
     * be passed in if one wants to do a lazy initialization of the {@link CookieSet}
     * content.
     * 
     * @param before the interface to support lazy initialization
     * @return new cookie set that can contain not only {@link Node.Cookie} but also 
     *    any plain old java object
     * @see #assign
     * @since 7.0
     */
    public static CookieSet createGeneric(Before before) {
        CookieSetLkp al = new CookieSetLkp(before);
        return new CookieSet(al, al);
    }

    /** The lookup associated with this cookie set. Keeps track of
     * the same things that are in the cookie set, but presents them
     * as being inside the lookup.
     * 
     * @return the lookup representing this cookie set
     * @since 7.0
     */
    public Lookup getLookup() {
        synchronized (QUERY_MODE) {
            if (lookup == null) {
                AbstractNode an = new AbstractNode(this);
                lookup = an.getLookup();
            }
        }
        return lookup;
    }

    
    /** Add a new cookie to the set. If a cookie of the same
    * <em>actual</em> (not representation!) class is already there,
    * it is replaced.
    * <p>Cookies inserted earlier are given preference during lookup,
    * in case a supplied representation class matches more than one cookie
    * in the set.
    *
    * @param cookie cookie to add
    */
    public void add(Node.Cookie cookie) {
        addImpl((Object)cookie);
        fireChangeEvent();
    }
    
    private void addImpl(Object cookie) {
        synchronized (this) {
            registerCookie(cookie.getClass(), cookie);
        }
        if (ic != null) {
            ic.add(cookie);
        }
    }

    /** Remove a cookie from the set.
    * @param cookie the cookie to remove
    */
    public void remove(Node.Cookie cookie) {
        removeImpl((Object)cookie);
        fireChangeEvent();
    }
    
    void removeImpl(Object cookie) {
        synchronized (this) {
            unregisterCookie(cookie.getClass(), cookie);
        }
        if (ic != null) {
            ic.remove(cookie);
        }
    }
    
    /** Get a cookie.
    *
    * @param clazz the representation class
    * @return a cookie assignable to the representation class, or <code>null</code> if there is none
    */
    public <T extends Node.Cookie> T getCookie(Class<T> clazz) {
        if (ic != null) {
            ic.beforeLookupImpl(clazz);
        }
        return lookupCookie(clazz);
    }
    
    
    private <T extends Node.Cookie> T lookupCookie(Class<T> clazz) {
        Node.Cookie ret = null;
        Object queryMode = QUERY_MODE.get();

        synchronized (this) {
            R r = findR(clazz);

            if (r == null) {
                if (queryMode == null || ic == null) {
                    return null;
                }
            } else {
                ret = r.cookie();

                if (queryMode instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<Class> keys = (Set<Class>)queryMode;
                    keys.addAll(map.keySet());
                }
            }
        }

        if (ret instanceof CookieEntry) {
            if (clazz == queryMode) {
                // we expected to be asked for this class
                // set cookie entry as a result
                QUERY_MODE.set(ret);
                ret = null;
            } else {
                // unwrap the cookie
                ret = ((CookieEntry) ret).getCookie(true);
            }
        } else if (ret == null) {
            if (ic != null && 
                (!Node.Cookie.class.isAssignableFrom(clazz) || clazz == Node.Cookie.class)
            ) {
                enhancedQueryMode(lookup, clazz);
                ret = null;
            }
        }

        return clazz.cast(ret);
    }
    
    static void enhancedQueryMode(Lookup lookup, Class<?> clazz) {
        Object type = QUERY_MODE.get();
        if (type != clazz) {
            return;
        }
        Collection<? extends Lookup.Item<?>> items = lookup.lookupResult(clazz).allItems();
        if (items.size() == 0) {
            return;
        }
        AbstractLookup.Pair[] arr = new AbstractLookup.Pair[items.size()];
        Iterator<? extends Lookup.Item> it = items.iterator();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new PairWrap(it.next());
        }
        QUERY_MODE.set(arr);
    }

    /** Add a listener to changes in the cookie set.
    * @param l the listener to add
    */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /** Remove a listener to changes in the cookie set.
    * @param l the listener to remove
    */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    
    /** Node lookup starts its non-important query.
     */
    static Object entryQueryMode(Class c) {
        Object prev = QUERY_MODE.get();
        QUERY_MODE.set(c);

        return prev;
    }

    /** Allows query for all know classes registered in this cookie.
     */
    static Object entryAllClassesMode() {
        Object prev = QUERY_MODE.get();
        QUERY_MODE.set(new HashSet());

        return prev;
    }

    /** Exits query mode.
     */
    static Collection<AbstractLookup.Pair> exitQueryMode(Object prev) {
        Object cookie = QUERY_MODE.get();
        QUERY_MODE.set(prev);

        if (cookie instanceof CookieSet.CookieEntry) {
            return Collections.singleton((AbstractLookup.Pair)new CookieEntryPair((CookieSet.CookieEntry) cookie));
        } else if (cookie instanceof AbstractLookup.Pair[]) {
            return Arrays.asList((AbstractLookup.Pair[])cookie);
        } else {
            return null;
        }
    }

    /** Returns list of all classes. */
    static Set exitAllClassesMode(Object prev) {
        Object cookie = QUERY_MODE.get();
        QUERY_MODE.set(prev);

        if (cookie instanceof HashSet) {
            return (Set) cookie;
        }

        return null;
    }

    /** Fires change event
    */
    final void fireChangeEvent() {
        cs.fireChange();
    }

    /** Attaches cookie to given class and all its superclasses and
    * superinterfaces.
    *
    * @param c class or null
    * @param cookie cookie to attach
    */
    private void registerCookie(Class<?> c, Object cookie) {
        if ((c == null) || !Node.Cookie.class.isAssignableFrom(c)) {
            return;
        }
        Class<? extends Node.Cookie> nc = c.asSubclass(Node.Cookie.class);

        R r = findR(nc);

        if (r == null) {
            r = new R();
            map.put(c, r);
        }

        r.add((Node.Cookie)cookie);

        registerCookie(c.getSuperclass(), cookie);

        Class[] inter = c.getInterfaces();

        for (int i = 0; i < inter.length; i++) {
            registerCookie(inter[i], cookie);
        }
    }

    /** Removes cookie from the class and all its superclasses and
    * superinterfaces.
    *
    * @param c class or null
    * @param cookie cookie to attach
    */
    private void unregisterCookie(Class<?> c, Object cookie) {
        if ((c == null) || !Node.Cookie.class.isAssignableFrom(c)) {
            return;
        }
        Class<? extends Node.Cookie> nc = c.asSubclass(Node.Cookie.class);


        // if different cookie is attached to class c stop removing
        R r = findR(nc);

        if (r != null) {
            // remove the cookie
            r.remove((Node.Cookie)cookie);
        }

        unregisterCookie(c.getSuperclass(), cookie);

        Class[] inter = c.getInterfaces();

        for (int i = 0; i < inter.length; i++) {
            unregisterCookie(inter[i], cookie);
        }
    }

    /** Registers a Factory for given cookie class */
    public void add(Class<? extends Node.Cookie> cookieClass, Factory factory) {
        if (factory == null) {
            throw new IllegalArgumentException();
        }

        synchronized (this) {
            registerCookie(cookieClass, new CookieEntry(factory, cookieClass));
        }
        if (ic != null) {
            ic.add(new FactAndClass(cookieClass, factory), C.INSTANCE);
        }
        fireChangeEvent();
    }

    /** Registers a Factory for given cookie classes */
    public void add(Class<? extends Node.Cookie>[] cookieClass, Factory factory) {
        if (factory == null) {
            throw new IllegalArgumentException();
        }

        synchronized (this) {
            for (int i = 0; i < cookieClass.length; i++) {
                registerCookie(cookieClass[i], new CookieEntry(factory, cookieClass[i]));
            }
        }

        if (ic != null) {
            for (Class<? extends Node.Cookie> c : cookieClass) {
                ic.add(new FactAndClass(c, factory), C.INSTANCE);
            }
        }
        fireChangeEvent();
    }

    /**
     * Unregisters a Factory for given cookie class
     * @since 2.6
     */
    public void remove(Class<? extends Node.Cookie> cookieClass, Factory factory) {
        if (factory == null) {
            throw new IllegalArgumentException();
        }

        synchronized (this) {
            R r = findR(cookieClass);

            if (r != null) {
                Node.Cookie c = r.cookie();

                if (c instanceof CookieEntry) {
                    CookieEntry ce = (CookieEntry) c;

                    if (ce.factory == factory) {
                        unregisterCookie(cookieClass, c);
                    }
                }
            }
        }
        if (ic != null) {
            ic.remove(new FactAndClass(cookieClass, factory), C.INSTANCE);
        }

        fireChangeEvent();
    }

    /**
     * Unregisters a Factory for given cookie classes
     * @since 2.6
     */
    public void remove(Class<? extends Node.Cookie>[] cookieClass, Factory factory) {
        if (factory == null) {
            throw new IllegalArgumentException();
        }

        synchronized (this) {
            for (int i = 0; i < cookieClass.length; i++) {
                R r = findR(cookieClass[i]);

                if (r != null) {
                    Node.Cookie c = r.cookie();

                    if (c instanceof CookieEntry) {
                        CookieEntry ce = (CookieEntry) c;

                        if (ce.factory == factory) {
                            unregisterCookie(cookieClass[i], c);
                        }
                    }
                }
            }
        }
        
        if (ic != null) {
            for (Class<? extends Node.Cookie> c : cookieClass) {
                ic.remove(new FactAndClass(c, factory), C.INSTANCE);
            }
        }

        fireChangeEvent();
    }
    
    /** Removes all instances of clazz from the set and replaces them
     * with newly provided instance(s).
     * 
     * @param clazz the root clazz for cookies to remove
     * @param instances the one or more instances to put into the lookup
     * 
     * @since 7.0
     */
    public <T> void assign(Class<? extends T> clazz, T... instances) {
        if (Node.Cookie.class.isAssignableFrom(clazz)) {
            Class<? extends Node.Cookie> cookieClazz = clazz.asSubclass(Node.Cookie.class);
            for(;;) {
                Node.Cookie cookie = lookupCookie(cookieClazz);
                if (cookie != null) {
                    removeImpl(cookie);
                } else {
                    break;
                }
            }
            for (T t : instances) {
                addImpl(t);
            }
        
            fireChangeEvent();
        } else if (ic != null) {
            synchronized (this) {
                for (T t : instances) {
                    registerCookie(t.getClass(), t);
                }
            }
            ic.replaceInstances(clazz, instances, this);
        }
    }
    
    /** Assignes a trigger that gets called everytime given class is about
     * to be queried. Can be used only for cookie set created with
     * {@link CookieSet#create(true)} and for classes that are not 
     * subclasses of Node.Cookie.
     *
     * @param clazz the trigger class (not subclass of Node.Cookie)
     * @param run runnable to run when the task is queried
     *
    public void beforeLookup(Class<?> clazz, Runnable run) {
        if (Node.Cookie.class.isAssignableFrom(clazz) || clazz == Object.class) {
            throw new IllegalArgumentException("Too generic class: " + clazz); // NOI18N
        }
        if (ic == null) {
            throw new IllegalStateException("Can be used only on CookieSet.create(true)"); // NOI18N
        }
        ic.registerBeforeLookup(clazz, run);
    }
    */
        
    /** Finds a result in a map.
     */
    private R findR(Class<? extends Node.Cookie> c) {
        return map.get(c);
    }

    /** Finds base class for a cookie.
     * @param c cookie
     * @return base class
     */
    private static Class<? extends Node.Cookie> baseForCookie(Node.Cookie c) {
        if (c instanceof CookieEntry) {
            return ((CookieEntry) c).klass;
        }

        return c.getClass();
    }

    /** Factory for creating cookies of given Class */
    public interface Factory {
        /** Creates a Node.Cookie of given class. The method
         * may be called more than once.
         */
        <T extends Node.Cookie> T createCookie(Class<T> klass);
    }
    
    /** Allows to update content of the cookie set just before 
     * a query for a given class is made.
     */
    public interface Before {
        public void beforeLookup(Class<?> clazz);
    }

    /** Entry for one Cookie */
    private static class CookieEntry implements Node.Cookie {
        /** Factory for the cookie */
        final Factory factory;

        /** Class of the cookie */
        private final Class<? extends Node.Cookie> klass;

        private Reference<Node.Cookie> cookie;

        /** Constructs new FactoryEntry */
        public CookieEntry(Factory factory, Class<? extends Node.Cookie> klass) {
            this.factory = factory;
            this.klass = klass;
        }

        /** Getter for the cookie.
         * Synchronized because we don't want to run factory.createCookie
         * simultaneously from two threads.
         */
        public synchronized Node.Cookie getCookie(boolean create) {
            Node.Cookie ret;

            if (create) {
                if ((cookie == null) || ((ret = cookie.get()) == null)) {
                    ret = factory.createCookie(klass);

                    if (ret == null) {
                        return null;
                    }

                    cookie = new WeakReference<Node.Cookie>(ret);
                }
            } else {
                ret = (cookie == null) ? null : cookie.get();
            }

            return ret;
        }
    } // end of CookieEntry

    /** Implementation of the result.
     */
    private static final class R extends Object {
        /** list of registered cookies */
        public List<Node.Cookie> cookies;

        /** base class of the first cookie registered here */
        public Class base;

        R() {
        }

        /** Adds a cookie.
         * @return true if adding should continue on superclasses should continue
         */
        public void add(Node.Cookie cookie) {
            if (cookies == null) {
                cookies = new ArrayList<Node.Cookie>(1);
                cookies.add(cookie);
                base = baseForCookie(cookie);

                return;
            }

            Class<?> newBase = baseForCookie(cookie);

            if ((base == null) || newBase.isAssignableFrom(base)) {
                cookies.set(0, cookie);
                base = newBase;
            } else {
                cookies.add(cookie);
            }
        }

        /** Removes a cookie.
         * @return true if empty
         */
        public boolean remove(Node.Cookie cookie) {
            if (cookies == null) {
                return true;
            }

            if (cookies.remove(cookie) && (cookies.size() == 0)) {
                base = null;
                cookies = null;

                return true;
            }

            base = baseForCookie(cookies.get(0));

            return false;
        }

        /** @return the cookie for this result or null
         */
        public Node.Cookie cookie() {
            return ((cookies == null) || cookies.isEmpty()) ? null : cookies.get(0);
        }
    }
    
    /** Pair that wraps another Lookup.Item
     */
    static final class PairWrap extends AbstractLookup.Pair {
        private Lookup.Item<?> item;
        private boolean created;
        
        public PairWrap(Lookup.Item<?> item) {
            this.item = item;
        }

        protected boolean instanceOf(Class c) {
            Class<?> k = c;
            return k.isAssignableFrom(getType());
        }

        protected boolean creatorOf(Object obj) {
            return created && getInstance() == obj;
        }

        public Object getInstance() {
            created = true;
            return item.getInstance();
        }

        public Class<? extends Object> getType() {
            return item.getType();
        }

        public String getId() {
            return item.getId();
        }

        public String getDisplayName() {
            return item.getDisplayName();
        }

        public int hashCode() {
            return 777 + item.hashCode();
        }

        public boolean equals(Object object) {
            if (object instanceof PairWrap) {
                PairWrap p = (PairWrap)object;
                return item.equals(p.item);
            }
            return false;
        }
    } // end of PairWrap

    /** Pair that represents an entry.
     */
    private static final class CookieEntryPair extends AbstractLookup.Pair {
        private CookieEntry entry;

        public CookieEntryPair(CookieEntry e) {
            this.entry = e;
        }

        protected boolean creatorOf(Object obj) {
            return obj == entry.getCookie(false);
        }

        public String getDisplayName() {
            return getId();
        }

        public String getId() {
            return entry.klass.getName();
        }

        public Object getInstance() {
            return entry.getCookie(true);
        }

        public Class getType() {
            return entry.klass;
        }

        protected boolean instanceOf(Class c) {
            Class<?> k = c;
            return k.isAssignableFrom(entry.klass);
        }

        public int hashCode() {
            return entry.hashCode() + 5;
        }

        public boolean equals(Object obj) {
            if (obj instanceof CookieEntryPair) {
                return ((CookieEntryPair) obj).entry == entry;
            }

            return false;
        }
    } // end of CookieEntryPair
    
    private static final class FactAndClass {
        final Class<? extends Node.Cookie> clazz;
        final Factory factory;
        
        public FactAndClass(Class<? extends Node.Cookie> clazz, Factory factory) {
            this.clazz = clazz;
            this.factory = factory;
        }
        
        public int hashCode() {
            return clazz.hashCode() + factory.hashCode();
        }
        
        public boolean equals(Object o) {
            if (o instanceof FactAndClass) {
                FactAndClass f = (FactAndClass)o;
                return f.clazz.equals(clazz) && f.factory == factory;
            }
            return false;
        }
    }
    
    private static class C implements InstanceContent.Convertor<FactAndClass, Node.Cookie> {
        static final C INSTANCE = new C();
        

        public Node.Cookie convert(CookieSet.FactAndClass obj) {
            return obj.factory.createCookie(obj.clazz);
        }

        public Class<? extends Node.Cookie> type(CookieSet.FactAndClass obj) {
            return obj.clazz;
        }

        public String id(CookieSet.FactAndClass obj) {
            return obj.clazz.getName();
        }

        public String displayName(CookieSet.FactAndClass obj) {
            return obj.clazz.getName();
        }
    }
}
