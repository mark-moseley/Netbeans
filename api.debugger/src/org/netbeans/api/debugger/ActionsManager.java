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

package org.netbeans.api.debugger;

import java.beans.*;
import java.io.*;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;


/** 
 * Manages some set of actions. Loads some set of ActionProviders registerred 
 * for some context, and allows to call isEnabled and doAction methods on them.
 *
 * @author   Jan Jancura
 */
public final class ActionsManager {

    
    /** Action constant for Step Over Action. */
    public static final Object              ACTION_STEP_OVER = "stepOver";
    
    /** Action constant for breakpoint hit action. */
    public static final Object              ACTION_RUN_INTO_METHOD = "runIntoMethod";
    
    /** Action constant for Step Into Action. */
    public static final Object              ACTION_STEP_INTO = "stepInto";
    
    /** Action constant for Step Out Action. */
    public static final Object              ACTION_STEP_OUT = "stepOut";
    
    /** Action constant for Continue Action. */
    public static final Object              ACTION_CONTINUE = "continue";
    
    /** Action constant for Start Action. */
    public static final Object              ACTION_START = "start";
    
    /** Action constant for Kill Action. */
    public static final Object              ACTION_KILL= "kill";
    
    /** Action constant for Make Caller Current Action. */
    public static final Object              ACTION_MAKE_CALLER_CURRENT = "makeCallerCurrent";
    
    /** Action constant for Make Callee Current Action. */
    public static final Object              ACTION_MAKE_CALLEE_CURRENT = "makeCalleeCurrent";
    
    /** Action constant for Pause Action. */
    public static final Object              ACTION_PAUSE = "pause";
    
    /** Action constant for Run to Cursor Action. */
    public static final Object              ACTION_RUN_TO_CURSOR = "runToCursor";
    
    /** Action constant for Pop Topmost Call Action. */
    public static final Object              ACTION_POP_TOPMOST_CALL = "popTopmostCall";
    
    /** Action constant for Fix Action. */
    public static final Object              ACTION_FIX = "fix";
    
    /** Action constant for Restart Action. */
    public static final Object              ACTION_RESTART = "restart";

    /** Action constant for Restart Action. */
    public static final Object              ACTION_TOGGLE_BREAKPOINT = "toggleBreakpoint";
    
    
    // variables ...............................................................
    
    private Vector                  listener = new Vector ();
    private HashMap                 listeners = new HashMap ();
    private HashMap                 actionProviders;
    private MyActionListener        actionListener = new MyActionListener ();
    private Lookup                  lookup;
    private boolean                 doiingDo = false;
    private boolean                 destroy = false;

    
    ActionsManager (Lookup lookup) {
        this.lookup = lookup;
    }
    
    
    // main public methods .....................................................

    /**
     * Performs action on this DebbuggerEngine.
     *
     * @param action action constant (default set of constanct are defined
     *    in this class with ACTION_ prefix)
     * @return true if action has been performed
     */
    public final void doAction (Object action) {
        doiingDo = true;
        if (actionProviders == null) initActionImpls ();
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l != null) {
            l = (ArrayList) l.clone ();
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (((ActionsProvider) l.get (i)).isEnabled (action))
                    ((ActionsProvider) l.get (i)).doAction (action);
        }
        fireActionDone (action);
        doiingDo = false;
        if (destroy) destroyIn ();
    }
    
    /**
     * Returns true if given action can be performed on this DebuggerEngine.
     * 
     * @param action action constant (default set of constanct are defined
     *    in this class with ACTION_ prefix)
     * @return true if given action can be performed on this DebuggerEngine
     */
    public final boolean isEnabled (final Object action) {
        if (actionProviders == null) initActionImpls ();
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l != null) {
            l = (ArrayList) l.clone ();
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (((ActionsProvider) l.get (i)).isEnabled (action))
                    return true;
        }
        return false;
    }
    
    /**
     * Stops listening on all actions, stops firing events.
     */
    public void destroy () {
        if (!doiingDo) destroyIn ();
        destroy = true;
    }

    
    // ActionsManagerListener support ..........................................

    /**
     * Add ActionsManagerListener.
     *
     * @param l listener instance
     */
    public void addActionsManagerListener (ActionsManagerListener l) {
        listener.addElement (l);
    }

    /**
     * Removes ActionsManagerListener.
     *
     * @param l listener instance
     */
    public void removeActionsManagerListener (ActionsManagerListener l) {
        listener.removeElement (l);
    }

    /** 
     * Add ActionsManagerListener.
     *
     * @param propertyName a name of property to listen on
     * @param l the ActionsManagerListener to add
     */
    public void addActionsManagerListener (
        String propertyName, 
        ActionsManagerListener l
    ) {
        Vector listener = (Vector) listeners.get (propertyName);
        if (listener == null) {
            listener = new Vector ();
            listeners.put (propertyName, listener);
        }
        listener.addElement (l);
    }

    /** 
     * Remove ActionsManagerListener.
     *
     * @param propertyName a name of property to listen on
     * @param l the ActionsManagerListener to remove
     */
    public void removeActionsManagerListener (
        String propertyName, 
        ActionsManagerListener l
    ) {
        Vector listener = (Vector) listeners.get (propertyName);
        if (listener == null) return;
        listener.removeElement (l);
        if (listener.size () == 0)
            listeners.remove (propertyName);
    }

    
    // firing support ..........................................................

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was removed
     */
    private void fireActionDone (
        final Object action
    ) {
        initListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (
            ActionsManagerListener.PROP_ACTION_PERFORMED
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((ActionsManagerListener) l.elementAt (i)).actionPerformed ( 
                action
            );
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++)
                ((ActionsManagerListener) l1.elementAt (i)).actionPerformed 
                    (action);
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was removed
     */
    private void fireActionStateChanged (
        final Object action, 
        boolean enabled
    ) {
        initListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (
            ActionsManagerListener.PROP_ACTION_STATE_CHANGED
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((ActionsManagerListener) l.elementAt (i)).actionStateChanged ( 
                action, enabled
            );
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++)
                ((ActionsManagerListener) l1.elementAt (i)).actionStateChanged 
                    (action, enabled);
        }
    }
    
    
    // private support .........................................................
    
    private void registerActionsProvider (Object action, ActionsProvider p) {
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l == null) {
            l = new ArrayList ();
            actionProviders.put (action, l);
        }
        l.add (p);
        fireActionStateChanged (action, p.isEnabled (action));
        p.addActionsProviderListener (actionListener);
    }
    
    private void unregisterActionsProvider (Object action, ActionsProvider p) {
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l == null) return; 
        l.remove (p);
        if (l.size () == 0)
            actionProviders.remove (action);
        p.removeActionsProviderListener (actionListener);
    }
    
    private void initActionImpls () {
        actionProviders = new HashMap ();
        Iterator i = lookup.lookup (null, ActionsProvider.class).iterator ();
        List l = new ArrayList ();
        while (i.hasNext ()) {
            ActionsProvider ap = (ActionsProvider) i.next ();
            Iterator ii = ap.getActions ().iterator ();
            while (ii.hasNext ())
                registerActionsProvider (ii.next (), ap);
        }
    }
    
    private boolean listerersLoaded = false;
    private List lazyListeners;
    
    private void initListeners () {
        if (listerersLoaded) return;
        listerersLoaded = true;
        lazyListeners = lookup.lookup (null, LazyActionsManagerListener.class);
        int i, k = lazyListeners.size ();
        for (i = 0; i < k; i++) {
            LazyActionsManagerListener l = (LazyActionsManagerListener)
                lazyListeners.get (i);
            String[] props = l.getProperties ();
            if (props == null) {
                addActionsManagerListener (l);
                continue;
            }
            int j, jj = props.length;
            for (j = 0; j < jj; j++) {
                addActionsManagerListener (props [j], l);
            }
        }
    }
    
    private synchronized void destroyIn () {
        int i, k = lazyListeners.size ();
        for (i = 0; i < k; i++) {
            LazyActionsManagerListener l = (LazyActionsManagerListener)
                lazyListeners.get (i);
            String[] props = l.getProperties ();
            if (props == null) {
                removeActionsManagerListener (l);
                continue;
            }
            int j, jj = props.length;
            for (j = 0; j < jj; j++) {
                removeActionsManagerListener (props [j], l);
                l.destroy ();
            }
        }
        lazyListeners = new ArrayList ();
    }

    
    // innerclasses ............................................................
    
    class MyActionListener implements ActionsProviderListener {
        public void actionStateChange (Object action, boolean enabled) {
            fireActionStateChanged (action, enabled);
        }
    }
}

