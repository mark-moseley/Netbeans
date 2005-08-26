/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger;

import java.beans.*;
import java.util.*;
import java.util.HashMap;
import org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider;
import org.netbeans.spi.debugger.DelegatingSessionProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.SessionProvider;


/**
 * The root class of Debugger APIs. DebuggerManager manages list of 
 * {@link org.netbeans.api.debugger.Session}s, 
 * {@link org.netbeans.api.debugger.Breakpoint}s and
 * {@link org.netbeans.api.debugger.Watch}es.
 *  
 *
 * <p><br><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tbody><tr bgcolor="#ccccff">
 * <td colspan="2"><font size="+2"><b>Description </b></font></td>
 * </tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Functionality</b></font></td><td> 
 *
 * <b>Start & finish debugging:</b>
 *    DebuggerManager manages a process of starting a new debugging (
 *    {@link #startDebugging}). It cooperates with all installed
 *    {@link org.netbeans.spi.debugger.DebuggerEngineProvider}s to create a new 
 *    {@link org.netbeans.api.debugger.Session} (or Sessions) and a new 
 *    {@link org.netbeans.api.debugger.DebuggerEngine} (or Engines).
 *    It supports kill all sessions too ({@link #finishAllSessions}).
 *
 * <br><br>
 * <b>Sessions management:</b>
 *    DebuggerManager keeps list of all 
 *    {@link org.netbeans.api.debugger.Session}s ({@link #getSessions}),
 *    and manages current session ({@link #getCurrentSession},
 *    {@link #setCurrentSession}).
 *
 * <br><br>
 * <b>Engine management:</b>
 *    DebuggerManager provides current engine ({@link #getCurrentEngine}).
 *    Current engine is derivated from current session. So,
 *    <i>
 *    debuggerManager.getCurrentEngine () == debuggerManager.
 *    getCurrentSession.getCurrentEngine ()
 *    </i>
 *    should be always true.
 *
 * <br><br>
 * <b>Breakpoints management:</b>
 *    DebuggerManager keeps list of all shared breakpoints 
 *    ({@link #getBreakpoints}).
 *    Breakpoint can be added ({@link #addBreakpoint}) and removed
 *    ({@link #removeBreakpoint}).
 *
 * <br><br>
 * <b>Watches management:</b>
 *    DebuggerManager keeps list of all shared watches ({@link #getWatches}).
 *    Watch can be created & added ({@link #createWatch}).
 *
 * <br><br>
 * <b>Support for listening:</b>
 *    DebuggerManager propagates all changes to two type of listeners - general
 *    {@link java.beans.PropertyChangeListener} and specific
 *    {@link org.netbeans.api.debugger.DebuggerManagerListener}.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Clinents / Providers</b></font></td><td> 
 *
 * DebuggerCore module should be the only one provider of this abstract class.
 * This class should be called from debugger plug-in modules and from debugger
 * UI modules. 
 * 
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Lifecycle</b></font></td><td> 
 *
 * The only one instance of DebuggerManager should exist, and it should be 
 * created in {@link #getDebuggerManager} method.
 * 
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Evolution</b></font></td><td>
 *
 * No method should be removed from this class, but some functionality can 
 * be added.
 *
 * </td></tr></tbody></table>
 *
 * @author Jan Jancura
 */
public final class DebuggerManager {
    
    // TODO: deprecate all these properties. They are useless, since there are
    //       dedicated methods in DebuggerManagerListener
    
    // OR: Remove DebuggerManagerListener and use just the properties.
    // - probably not possible because of initBreakpoints() method.
    
    /** Name of property for the set of breakpoints in the system. */
    public static final String                PROP_BREAKPOINTS_INIT = "breakpointsInit"; // NOI18N
    
    /** Name of property for the set of breakpoints in the system. */
    public static final String                PROP_BREAKPOINTS = "breakpoints"; // NOI18N

    /** Name of property for current debugger engine. */
    public static final String                PROP_CURRENT_ENGINE = "currentEngine";

    /** Name of property for current debugger session. */
    public static final String                PROP_CURRENT_SESSION = "currentSession";
    
    /** Name of property for set of running debugger sessions. */
    public static final String                PROP_SESSIONS = "sessions";
    
    /** Name of property for set of running debugger engines. */
    public static final String                PROP_DEBUGGER_ENGINES = "debuggerEngines";

    /** Name of property for the set of watches in the system. */
    public static final String                PROP_WATCHES = "watches"; // NOI18N

    /** Name of property for the set of watches in the system. */
    public static final String                PROP_WATCHES_INIT = "watchesInit"; // NOI18N
    
    
    private static DebuggerManager            debuggerManager;
    private Session                           currentSession;
    private DebuggerEngine                    currentEngine;
    private List                              sessions = new ArrayList();
    private Set                               engines = new HashSet ();
    private Vector                            breakpoints = new Vector ();
    private boolean                           breakpointsInitialized = false;
    private Vector                            watches = new Vector ();
    private boolean                           watchesInitialized = false;
    private SessionListener                   sessionListener = new SessionListener ();
    private Vector                            listeners = new Vector ();
    private HashMap                           listenersMap = new HashMap ();
    private ActionsManager                    actionsManager = null;
    
    private Lookup                            lookup = new Lookup.MetaInf (null);
    
    
    /**
     * Returns default instance of DebuggerManager.
     *
     * @return default instance of DebuggerManager
     */
    public static synchronized DebuggerManager getDebuggerManager () {
        if (debuggerManager == null) 
            debuggerManager = new DebuggerManager ();
        return debuggerManager;
    }

    /**
     * Creates a new instance of DebuggerManager.
     * It's called from a synchronized block, do not call any foreign code from here.
     */
    private DebuggerManager () {
    }


    public synchronized ActionsManager getActionsManager () {
        if (actionsManager == null)
            actionsManager = new ActionsManager (lookup);
        return actionsManager;
    }
    
    
    // lookup management .............................................
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public List lookup (String folder, Class service) {
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public Object lookupFirst (String folder, Class service) {
        return lookup.lookupFirst (folder, service);
    }
    
    
    // session / engine management .............................................
    
    /** 
     * Start a new debugging for given 
     * {@link org.netbeans.api.debugger.DebuggerInfo}. DebuggerInfo provides
     * information needed to start new debugging. DebuggerManager finds
     * all {@link org.netbeans.spi.debugger.SessionProvider}s and 
     *  {@link org.netbeans.spi.debugger.DelegatingSessionProvider}s
     * installed for given DebuggerInfo, and creates a new 
     * {@link Session}(s). 
     * After that it looks for all 
     * {@link org.netbeans.spi.debugger.DebuggerEngineProvider}s and 
     * {@link org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider}s
     * installed for Session, and crates a new 
     * {@link DebuggerEngine}(s).
     *
     * @param info debugger startup info
     * @return DebuggerEngines started for given info
     */
    public DebuggerEngine[] startDebugging (DebuggerInfo info) {
        //S ystem.out.println("@StartDebugging info: " + info);
        
        // init sessions
        ArrayList sessionProviders = new ArrayList ();
        ArrayList engines = new ArrayList ();
        Lookup l = info.getLookup ();
        Lookup l2 = info.getLookup ();
        sessionProviders.addAll (
            l.lookup (
                null,
                SessionProvider.class
            )
        );
        sessionProviders.addAll (
            l.lookup (
                null,
                DelegatingSessionProvider.class
            )
        );
        Session sessionToStart = null;
        int i, k = sessionProviders.size ();
        for (i = 0; i < k; i++) {
            Session s = null;
            if (sessionProviders.get (i) instanceof DelegatingSessionProvider) {
                s = ((DelegatingSessionProvider) sessionProviders.get (i)).
                    getSession (info);
                l = new Lookup.Compound (
                    l,
                    s.privateLookup
                );
                //S ystem.out.println("@  StartDebugging DelegaingSession: " + s);
            } else {
                SessionProvider sp = (SessionProvider) sessionProviders.get (i);
                s = new Session (
                    sp.getSessionName (),
                    sp.getLocationName (),
                    sp.getTypeID (),
                    sp.getServices (),
                    l
                );
                sessionToStart = s;
                l = s.getLookup ();
                l2 = s.getLookup ();
                addSession (s);
                //S ystem.out.println("@  StartDebugging new Session: " + s);
            }
            
            // init DebuggerEngines
            ArrayList engineProviders = new ArrayList ();
            engineProviders.addAll (
                l2.lookup (null, DebuggerEngineProvider.class)
            );
            engineProviders.addAll (
                l2.lookup (null, DelegatingDebuggerEngineProvider.class)
            );
            int j, jj = engineProviders.size ();
            for (j = 0; j < jj; j++) {
                DebuggerEngine engine = null;
                String[] languages = null; 
                if (engineProviders.get (j) instanceof DebuggerEngineProvider) {
                    DebuggerEngineProvider ep = (DebuggerEngineProvider) 
                        engineProviders.get (j);
                    Object[] services = ep.getServices ();
                    engine = new DebuggerEngine (
                        ((DebuggerEngineProvider) engineProviders.get (j)).
                            getEngineTypeID (),
                        s,
                        services,
                        l
                    );
                    languages = ep.getLanguages ();
                    ep.setDestructor (engine.new Destructor ());
                    engines.add (engine);
                    //S ystem.out.println("@    StartDebugging new Engine: " + engine);
                } else {
                    DelegatingDebuggerEngineProvider dep = 
                        (DelegatingDebuggerEngineProvider) 
                        engineProviders.get (j);
                    languages = dep.getLanguages ();
                    engine = dep.getEngine ();
                    dep.setDestructor (engine.new Destructor ());
                    //S ystem.out.println("@    StartDebugging DelegatingEngine: " + engine);
                }
                int w, ww = languages.length;
                for (w = 0; w < ww; w++)
                    s.addLanguage (languages [w], engine);
            }
        }
        
        k = engines.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerEngine) engines.get (i)).getActionsManager ().postAction 
                (ActionsManager.ACTION_START);
        }
        
        if (sessionToStart != null)
            setCurrentSession (sessionToStart);
        
        DebuggerEngine[] des = new DebuggerEngine [engines.size ()];
        return (DebuggerEngine[]) engines.toArray (des);
    }

    /**
     * Kills all {@link org.netbeans.api.debugger.Session}s and
     * {@link org.netbeans.api.debugger.DebuggerEngine}s.
     */
    public void finishAllSessions () {
        Session[] ds = getSessions ();
        
        if (ds.length == 0) return;

        // finish all non persistent sessions
        int i, k = ds.length;
        for (i = 0; i < k; i++)
            ds [i].getCurrentEngine ().getActionsManager ().
                doAction (ActionsManager.ACTION_KILL);
    }
    
    /**
     * Returns current debugger session or <code>null</code>.
     *
     * @return current debugger session or <code>null</code>
     */
    public Session getCurrentSession () {
        return currentSession;
    }

    /**
     * Sets current debugger session.
     *
     * @param session a session to be current
     */
    public void setCurrentSession (Session session) {
        Session oldSession;
        Session newSession;
        DebuggerEngine oldEngine;
        DebuggerEngine newEngine;
        synchronized (sessions) {
            // 1) check if the session is registerred
            if (session != null) {
                int i, k = sessions.size();
                for (i = 0; i < k; i++)
                    if (session == sessions.get(i)) break;
                if (i == k) 
                    return;
            }
            
            // fire all changes
            oldSession = getCurrentSession ();
            if (session == oldSession) return;
            currentSession = newSession = session;
            
            oldEngine = currentEngine;
            newEngine = null;
            if (getCurrentSession () != null)
                newEngine = getCurrentSession ().getCurrentEngine ();
            currentEngine = newEngine;
        }
        if (oldEngine != newEngine) {
            firePropertyChange (PROP_CURRENT_ENGINE, oldEngine, newEngine);
        }
        firePropertyChange (PROP_CURRENT_SESSION, oldSession, newSession);
    }

    /**
     * Returns set of running debugger sessions.
     *
     * @return set of running debugger sessions
     */
    public Session[] getSessions () {
        synchronized (sessions) {
            return (Session[]) sessions.toArray(new Session[0]);
        }
    }

    /**
     * Returns set of running debugger engines.
     *
     * @return set of running debugger engines
     */
    public DebuggerEngine[] getDebuggerEngines () {
        synchronized (engines) {
            return (DebuggerEngine[]) engines.toArray (new DebuggerEngine [engines.size ()]);
        }
    }
    
    /**
     * Returns current debugger engine or <code>null</code>.
     *
     * @return current debugger engine or <code>null</code>
     */
    public DebuggerEngine getCurrentEngine () {
        return currentEngine;
    }

    
    // breakpoints management ..................................................
    
    /** 
     * Adds a new breakpoint.
     *
     * @param breakpoint a new breakpoint
     */
    public void addBreakpoint (
        Breakpoint breakpoint
    ) {
        initBreakpoints ();
        breakpoints.addElement (breakpoint);
        fireBreakpointCreated (breakpoint);
    }
    
    /** 
     * Removes breakpoint.
     *
     * @param breakpoint a breakpoint to be removed
     */
    public void removeBreakpoint (
        Breakpoint breakpoint
    ) {
        initBreakpoints ();
        breakpoints.removeElement (breakpoint);
        breakpoint.disposeOut ();
        fireBreakpointRemoved (breakpoint);
    }

    /** 
     * Gets all registered breakpoints.
     *
     * @return all breakpoints
     */
    public Breakpoint[] getBreakpoints () {
        initBreakpoints ();
        return (Breakpoint[]) breakpoints.toArray(new Breakpoint[0]);
    }

    
    // watches management ......................................................

    /** 
     * Creates a watch with its expression set to an initial value.
     * Also allows creation of a hidden watch (not presented to the user), 
     * for example for internal use in the editor to obtain values of variables
     * under the mouse pointer.
     *
     * @param expr expression to watch for (the format is the responsibility 
     *    of the debugger plug-in implementation, but it is typically 
     *    a variable name).
     * @return the new watch
     */
    public Watch createWatch (String expr) {
        initWatches ();
        Watch w = new Watch (expr);
        watches.addElement (w);
        fireWatchCreated (w);
        return w;
    }

    /**
    * Gets all shared watches in the system.
    *
    * @return all watches
    */
    public Watch[] getWatches () {
        initWatches ();
        return (Watch[]) watches.toArray(new Watch[0]);
    }

    /**
     * Removes all watches from the system.
     */
    public void removeAllWatches () {
        initWatches ();
        Vector v = (Vector) watches.clone ();
        int i, k = v.size ();
        for (i = k - 1; i >= 0; i--)
            ((Watch) v.elementAt (i)).remove ();
    }

    /**
    * Removes watch.
    *
    * @param w watch to be removed
    */
    void removeWatch (Watch w) {
        initWatches ();
        watches.removeElement (w);
        fireWatchRemoved (w);
    }

    
    // listenersMap ...............................................................

    
    /**
    * Fires property change.
    */
    private void firePropertyChange (String name, Object o, Object n) {
        initDebuggerManagerListeners ();
        Vector l = (Vector) listeners.clone ();
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (name);
            if (l1 != null)
                l1 = (Vector) l1.clone ();
        }
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, name, o, n
        );
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((DebuggerManagerListener)l.elementAt (i)).propertyChange (ev);
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++)
                ((DebuggerManagerListener)l1.elementAt (i)).propertyChange (ev);
        }
    }

    /**
    * This listener notificates about changes of breakpoints, watches and threads.
    *
    * @param l listener object.
    */
    public void addDebuggerListener (DebuggerManagerListener l) {
        listeners.addElement (l);
    }

    /**
    * Removes debugger listener.
    *
    * @param l listener object.
    */
    public void removeDebuggerListener (DebuggerManagerListener l) {
        listeners.removeElement (l);
    }

    /** 
     * Add a debuggerManager listener to changes of watches and breakpoints.
     *
     * @param propertyName a name of property to listen on
     * @param l the debuggerManager listener to add
     */
    public void addDebuggerListener (
        String propertyName, 
        DebuggerManagerListener l
    ) {
        synchronized (listenersMap) {
            Vector listeners = (Vector) listenersMap.get (propertyName);
            if (listeners == null) {
                listeners = new Vector ();
                listenersMap.put (propertyName, listeners);
            }
            listeners.addElement (l);
        }
    }

    /** 
     * Remove a debuggerManager listener to changes of watches and breakpoints.
     *
     * @param propertyName a name of property to listen on
     * @param l the debuggerManager listener to remove
     */
    public void removeDebuggerListener (
        String propertyName, 
        DebuggerManagerListener l
    ) {
        synchronized (listenersMap) {
            Vector listeners = (Vector) listenersMap.get (propertyName);
            if (listeners == null) return;
            listeners.removeElement (l);
            if (listeners.size () == 0)
                listenersMap.remove (propertyName);
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointAdded was added}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was created
     */
    private void fireBreakpointCreated (final Breakpoint breakpoint) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_BREAKPOINTS, null, null
        );
        
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).breakpointAdded 
                (breakpoint);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }
        
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_BREAKPOINTS);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).breakpointAdded 
                    (breakpoint);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listenersMap about a change.
     * Notifies {@link #listeners registered listenersMap} that a breakpoint
     * {@link DebuggerManagerListener#breakpointRemoved was removed}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was removed
     */
    private void fireBreakpointRemoved (final Breakpoint breakpoint) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_BREAKPOINTS, null, null
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).breakpointRemoved 
                (breakpoint);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }
        
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_BREAKPOINTS);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).breakpointRemoved 
                    (breakpoint);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    private void initBreakpoints () {
        List createdBreakpoints;
        // All is under the lock, including DebuggerManagerListener.initBreakpoints()
        // and DebuggerManagerListener.propertyChange(..PROP_BREAKPOINTS_INIT..) calls.
        // Clients should return the breakpoints via that listener, not add them
        // directly. Therefore this should not lead to deadlock...
        synchronized (breakpoints) {
            if (breakpointsInitialized) return ;
            breakpointsInitialized = true; 
            initDebuggerManagerListeners ();
            PropertyChangeEvent ev = new PropertyChangeEvent (
                this, PROP_BREAKPOINTS_INIT, null, null
            );
            
            createdBreakpoints = new ArrayList();
            
            Vector l = (Vector) listeners.clone ();
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                createdBreakpoints.addAll (Arrays.asList (
                    ((DebuggerManagerListener) l.elementAt (i)).initBreakpoints ()
                ));
                ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
            }
            
            Vector l1;
            synchronized (listenersMap) {
                l1 = (Vector) listenersMap.get (PROP_BREAKPOINTS_INIT);
            }
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
                k = l1.size ();
                for (i = 0; i < k; i++) {
                    createdBreakpoints.addAll (Arrays.asList (
                        ((DebuggerManagerListener) l1.elementAt (i)).initBreakpoints ()
                    ));
                    ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
                }
            }
            
            breakpoints.addAll(createdBreakpoints);
        }
        int k = createdBreakpoints.size ();
        for (int i = 0; i < k; i++) {
            fireBreakpointCreated ((Breakpoint) createdBreakpoints.get (i));
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a watch
     * {@link DebuggerManagerListener#watchAdded was added}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param watch  a watch that was created
     */
    private void fireWatchCreated (final Watch watch) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_WATCHES, null, null
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).watchAdded 
                (watch);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_WATCHES);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).watchAdded 
                    (watch);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a watch
     * {@link DebuggerManagerListener#watchRemoved was removed}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param watch  a watch that was removed
     */
    private void fireWatchRemoved (final Watch watch) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_WATCHES, null, null
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).watchRemoved 
                (watch);
            // TODO: fix nonsense double firing
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_WATCHES);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).watchRemoved 
                    (watch);
                // TODO: fix nonsense double firing
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    private void initWatches () {
        synchronized (watches) {
            if (watchesInitialized) return ;
            watchesInitialized = true;
        }
        // The rest must not be synchronized, since initWatches() does call createWatch()
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_WATCHES_INIT, null, null
        );
        
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).initWatches ();
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_WATCHES_INIT);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).initWatches ();
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a session
     * {@link DebuggerManagerListener#sessionAdded was added}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param session a session that was created
     */
    private void fireSessionAdded (
        final Session session,
        final Session[] old,
        final Session[] ne
    ) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_SESSIONS, old, ne
        );
        
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).sessionAdded 
                (session);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }
        
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_SESSIONS);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).sessionAdded
                    (session);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a session
     * {@link DebuggerManagerListener#sessionRemoved was removed}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param session a session that was removed
     */
    private void fireSessionRemoved (
        final Session session,
        final Session[] old,
        final Session[] ne
    ) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_SESSIONS, old, ne
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).sessionRemoved 
                (session);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_SESSIONS);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).sessionRemoved 
                    (session);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a engine
     * {@link DebuggerManagerListener#engineAdded was added}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param engine a engine that was created
     */
    private void fireEngineAdded (
        final DebuggerEngine engine,
        final DebuggerEngine[] old,
        final DebuggerEngine[] ne
    ) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_DEBUGGER_ENGINES, old, ne
        );
        
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).engineAdded 
                (engine);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }
        
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_DEBUGGER_ENGINES);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).engineAdded
                    (engine);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a engine
     * {@link DebuggerManagerListener#engineRemoved was removed}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param engine a engine that was removed
     */
    private void fireEngineRemoved (
        final DebuggerEngine engine,
        final DebuggerEngine[] old,
        final DebuggerEngine[] ne
    ) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_DEBUGGER_ENGINES, old, ne
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).engineRemoved 
                (engine);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_DEBUGGER_ENGINES);
        }
        if (l1 != null) {
            l1 = (Vector) l1.clone ();
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).engineRemoved 
                    (engine);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    
    // helper methods ....................................................
    
    private boolean listerersLoaded = false;
    
    private void initDebuggerManagerListeners () {
        synchronized (listenersMap) {
            if (listerersLoaded) return;
            listerersLoaded = true;
            List listenersMap = lookup.lookup (null, LazyDebuggerManagerListener.class);
            int i, k = listenersMap.size ();
            for (i = 0; i < k; i++) {
                LazyDebuggerManagerListener l = (LazyDebuggerManagerListener)
                    listenersMap.get (i);
                String[] props = l.getProperties ();
                if ((props == null) || (props.length == 0)) {
                    addDebuggerListener (l);
                    continue;
                }
                int j, jj = props.length;
                for (j = 0; j < jj; j++) {
                    addDebuggerListener (props [j], l);
                }
            }
        }
    }
    
    private void addSession (Session session) {
        Session[] oldSessions;
        Session[] newSessions;
        synchronized (sessions) {
            oldSessions = getSessions();
            int i, k = oldSessions.length;
            for (i = 0; i < k; i++)
                if (session == oldSessions[i]) return;

            newSessions = new Session [oldSessions.length + 1];
            System.arraycopy (oldSessions, 0, newSessions, 0, oldSessions.length);
            newSessions[oldSessions.length] = session;
            this.sessions.add(session);

            session.addPropertyChangeListener (sessionListener);
        }
        fireSessionAdded (session, oldSessions, newSessions);
    }
    
    private void removeSession (Session session) {
        Session[] oldSessions;
        Session[] newSessions;
        DebuggerEngine oldEngine;
        DebuggerEngine newEngine;
        synchronized (sessions) {
            oldSessions = getSessions();
            // find index of given debugger and new instance of currentDebugger
            Session nCurrentSesson = null;
            int i, k = oldSessions.length;
            for (i = 0; i < k; i++) {
                if (oldSessions[i] == session) {
                    break;
                } else if (nCurrentSesson == null) {
                    nCurrentSesson = oldSessions[i];
                }
            }
            if (i == k) return; // this debugger is not registered
            
            // set new current debugger session
            if (session == getCurrentSession ()) {
                if ((nCurrentSesson == null) && (k > 1))
                    nCurrentSesson = oldSessions[1];
                setCurrentSession (nCurrentSesson);
            }
            
            newSessions = new Session [oldSessions.length - 1];
            System.arraycopy (oldSessions, 0, newSessions, 0, i);
            if ((oldSessions.length - i) > 1)
                System.arraycopy (
                    oldSessions, i + 1, newSessions, i, oldSessions.length - i - 1
                );
            sessions.remove(i);
            
            session.removePropertyChangeListener (sessionListener);
            
            oldEngine = currentEngine;
            newEngine = null;
            if (getCurrentSession () != null)
                newEngine = getCurrentSession ().getCurrentEngine ();
            currentEngine = newEngine;
        }
        if (oldEngine != newEngine) {
            firePropertyChange (PROP_CURRENT_ENGINE, oldEngine, newEngine);
        }
        fireSessionRemoved (session, oldSessions, newSessions);
    }
    
    void addEngine (DebuggerEngine engine) {
        DebuggerEngine[] old;
        DebuggerEngine[] ne;
        synchronized (engines) {
            if (engines.contains (engine)) return;
            old = (DebuggerEngine[]) engines.toArray (new DebuggerEngine [engines.size ()]);
            engines.add (engine);
            ne = (DebuggerEngine[]) engines.toArray (new DebuggerEngine [engines.size ()]);
        }
        fireEngineAdded (engine, old, ne);
    }
    
    void removeEngine (DebuggerEngine engine) {
        DebuggerEngine[] old;
        DebuggerEngine[] ne;
        synchronized (engines) {
            if (!engines.contains (engine)) return;
            old = (DebuggerEngine[]) engines.toArray (new DebuggerEngine [engines.size ()]);
            engines.remove (engine);
            ne = (DebuggerEngine[]) engines.toArray (new DebuggerEngine [engines.size ()]);
        }
        fireEngineRemoved (engine, old, ne);
    }
    

    
    // innerclasses ............................................................

    /**
     * Listens on all engines and sessions for: 
     * current thread changes 
     * start / finish of engines
     * last action
     * current engine
     */  
    private class SessionListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getSource () instanceof Session) {
                if ( (!e.getPropertyName ().equals
                      (Session.PROP_CURRENT_LANGUAGE)) &&
                     (!e.getPropertyName ().equals
                      (Session.PROP_SUPPORTED_LANGUAGES))
                ) return;
                // update the current engine
                DebuggerEngine oldEngine;
                DebuggerEngine newEngine;
                synchronized (sessions) {
                    oldEngine = currentEngine;
                    newEngine = null;
                    if (getCurrentSession () != null)
                        newEngine = getCurrentSession ().getCurrentEngine ();
                    currentEngine = newEngine;
                }
                if (newEngine != oldEngine) {
                    firePropertyChange (PROP_CURRENT_ENGINE, oldEngine, newEngine);
                }
                Session s = (Session) e.getSource ();
                if (s.getSupportedLanguages ().length == 0)
                    removeSession (s);
            }
        }
    }
}

