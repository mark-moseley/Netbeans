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

package org;

import java.lang.ref.*;
import java.util.*;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

aspect ListenerCount
{
    //
    // pointcuts and advice
    //
    
    pointcut addAnyListener(Object t, Object l) : target(t) && args(l) && execution(* *..add*Listener(*Listener+));
    pointcut addNamedPropertyChangeListener(Object t, Object l) : target(t) && execution(* *..addPropertyChangeListener(String, *Listener+)) && args(*, l);
    pointcut removeAnyListener(Object t, Object l) : target(t) && args(l) && execution(* *..remove*Listener(*Listener+));

    before(Object t, Object l): addAnyListener(t, l) || addNamedPropertyChangeListener(t, l) {
        listenerAdded(t, thisJoinPointStaticPart.getSignature().getName().substring("add".length()), l);
    }

    before(Object t, Object l): removeAnyListener(t, l) {
        listenerRemoved(t, thisJoinPointStaticPart.getSignature().getName().substring("remove".length()), l);
    }

    before() : execution(* org.netbeans.core.NbTopManager.exit()) {
        dumpListenerCounts();
    }

    //
    // implementation details
    //


    ListenerCount() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MagicKeyHandler());
    }

    private class MagicKeyHandler implements KeyEventDispatcher
    {
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED
                && e.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)
                && e.getKeyCode() == KeyEvent.VK_L) {
                ListenerCount.this.dumpListenerCounts();
            }
            return false;
        }
    }
        
    private ThreadLocal lastFailedRemovedListener = new ThreadLocal();
    private ThreadLocal lastFailedRemoveStackTrace = new ThreadLocal();
    
    private Map sourceListeners = new HashMap(); // SourceListener -> Map(listener->count)
    private Map sourceListenerPeaks = new HashMap();
    
    private synchronized void listenerAdded(Object source, String listenerType, Object listener) {
        //System.err.println("added: source = " + source + " listenerType = " + listenerType);
        checkLastFailedRemove(listener);
        
        SourceListener sl = new SourceListener(source, listenerType);
        Map listeners = (Map) sourceListeners.get(sl);

        int c = 1;
        
        if (listeners == null) {
            listeners = new IdentityHashMap();
            sourceListeners.put(sl, listeners);
        }
        else {
            Integer count = (Integer) listeners.get(listener);
            if (count != null) {
                c = count.intValue() + 1;
                
                System.err.println("listenerCount> WARNING: registering the same listener for the "
                                   + (c == 2 ? "2nd" : (c == 3 ? "3rd" : ("" + c + "th")))
                                   + " time, not good");
                System.err.println("listenerCount>     source = " + source);
                System.err.println("listenerCount>     listener = " + listener);
                Thread.dumpStack();
            }
        }
            
        listeners.put(listener, new Integer(c));

        Integer peak = (Integer) sourceListenerPeaks.get(sl);
        if (peak != null) {
            int count = listeners.size();
            if (peak.intValue() < count)
                sourceListenerPeaks.put(sl, new Integer(count));
        }
        else {
            sourceListenerPeaks.put(sl, new Integer(1));
        }
    }

    private synchronized void listenerRemoved(Object source, String listenerType, Object listener) {
        //System.err.println("rm'ed: source = " + source + " listenerType = " + listenerType);
        checkLastFailedRemove(null);
        
        SourceListener sl = new SourceListener(source, listenerType);
        Map listeners = (Map) sourceListeners.get(sl);
        
        if (listeners == null || null != listeners.remove(listener)) {
            
            lastFailedRemovedListener.set(listener);
            lastFailedRemoveStackTrace.set(new Throwable());
        }
    }

    private void checkLastFailedRemove(Object addedListener) {
        Object listener = lastFailedRemovedListener.get();
        if (listener != null && listener != addedListener) {
            System.err.println("listenerCount> WARNING: attempt to remove unregistered listener, may or may not be okay");
            ((Throwable)lastFailedRemoveStackTrace.get()).printStackTrace(System.err);
        }
        lastFailedRemovedListener.set(null);
        lastFailedRemoveStackTrace.set(null);
    }
    
    private void dumpListenerCounts() {
        System.gc();
        System.gc();
        System.gc();
        
        HashMap sourceListenerPeaks;
        
        synchronized (this) {
            sourceListenerPeaks = new HashMap(this.sourceListenerPeaks);
        }
        
        Map sclcounts = new HashMap(); // SourceClassListener -> List of Integers
        
        Iterator iter = sourceListenerPeaks.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            SourceListener sl = (SourceListener) entry.getKey();
            Integer count = (Integer) entry.getValue();
            
            SourceClassListener scl = new SourceClassListener(sl.getSourceClass(), sl.getListenerType());
            List counts = (List) sclcounts.get(scl);
            if (counts == null) {
                counts = new ArrayList();
                sclcounts.put(scl, counts);
            }
            counts.add(count);
        }

        iter = sclcounts.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            SourceClassListener scl = (SourceClassListener) entry.getKey();
            
            List counts = (List) entry.getValue();
            Collections.sort(counts);
            
            int num = -1;
            int occur = 0;
            
            Iterator iter2 = counts.iterator();
            while (iter2.hasNext()) {
                int c = ((Integer)iter2.next()).intValue();

                if (num < 0) {
                    num = c;
                }
                else {
                    if (c != num) {
                        System.out.println("listenerCount: " + num + "\t" + occur + "\t" + scl.getSourceClass().getName() + "\t" + scl.getListenerType());
                        num = c;
                        occur = 1;
                        continue;
                    }
                }

                occur++;
            }
            if (num > 0 && occur > 0)
                System.out.println("listenerCount: " + num + "\t" + occur + "\t" + scl.getSourceClass().getName() + "\t" + scl.getListenerType());
        }
    }

    private static class SourceClassListener {
        private Class sourceClass;
        private String listenerType;

        SourceClassListener(Class sourceClass, String listenerType) {
            this.sourceClass = sourceClass;
            this.listenerType = listenerType.intern();
        }

        Class getSourceClass() {
            return sourceClass;
        }

        String getListenerType() {
            return listenerType;
        }

        public boolean equals(Object o) {
            if (!(o instanceof SourceClassListener))
                return false;
            SourceClassListener scl = (SourceClassListener) o;
            return sourceClass == scl.sourceClass && listenerType == scl.listenerType;
        }

        public int hashCode() {
            return 37 * sourceClass.hashCode() + listenerType.hashCode();
        }
    }
    
    private static class SourceListener {
        private WeakReference sourceRef;
        private int sourceHashCode;
        private Class sourceClass;
        private String listenerType;
        
        SourceListener(Object source, String listenerType) {
            this.sourceRef = new WeakReference(source);
            this.sourceHashCode = source.hashCode();
            this.sourceClass = source.getClass();
            this.listenerType = listenerType.intern();
        }

        Object getSource() {
            return sourceRef.get();
        }
        
        Class getSourceClass() {
            return sourceClass;
        }
        
        String getListenerType() {
            return listenerType;
        }

        public boolean equals(Object o) {
            if (o instanceof SourceListener) {
                SourceListener sl = (SourceListener) o;
                return sourceRef.get() == sl.sourceRef.get() && listenerType == sl.listenerType;
            }
            else {
                return false;
            }
        }

        public int hashCode() {
            return 37 * listenerType.hashCode() + sourceHashCode;
        }
    }
}
