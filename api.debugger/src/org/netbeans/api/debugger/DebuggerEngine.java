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
 * Debugger Engine represents implementation of one debugger (Java Debugger, 
 * CPP Debugger). It can support debugging of one or more 
 * {@link Session}s, in one or more languages. 
 * It provides root of threads hierarchy (call stacks, locals)
 * and manages debugger actions.
 *
 * <p><br><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tbody><tr bgcolor="#ccccff">
 * <td colspan="2"><font size="+2"><b>Description </b></font></td>
 * </tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Functionality</b></font></td><td>
 *
 * <b>Support for actions:</b>
 *    DebuggerEngine manages list of actions ({@link #getActionsManager}). 
 *    Debugger action (implemented by 
 *    {@link org.netbeans.spi.debugger.ActionsProvider}) can be registerred to 
 *    DebuggerEngine during a start of debugger. See 
 *    {@link org.netbeans.spi.debugger.ActionsProvider}.
 *    DebuggerEngine can be used to call some debugger action 
 *    ({@link ActionsManager#doAction}) and to distinguish availability of action 
 *    ({@link ActionsManager#isEnabled}).
 *    Example how to call Kill Action on this engine:
 *    <pre>
 *    engine.getActionsManager ().doAction (DebuggerEngine.ACTION_KILL);</pre>
 *
 * <br>
 * <b>Support for aditional services:</b>
 *    DebuggerEngine is final class. That is why the standard method how to 
 *    extend its functionality is using lookup methods ({@link #lookup} and 
 *    {@link #lookupFirst}).
 *    There are two ways how to register some service provider for some
 *    type of DebuggerEngine:
 *    <ul>
 *      <li>Register 'live' instance of service provider during creation of 
 *        new instance of DebuggerEngine (see method
 *        {@link org.netbeans.spi.debugger.DebuggerEngineProvider#getServices}).
 *      </li>
 *      <li>Register service provider in Manifest-inf/debugger/{{@link 
 *        #getTypeID}} folder. See Debugger SPI for more information about
 *        registration.</li>
 *    </ul>
 *
 * <br>
 * <b>Support for listening:</b>
 *    DebuggerEngine propagates all changes to two type of listeners - general
 *    {@link java.beans.PropertyChangeListener} and specific
 *    {@link DebuggerEngineListener}.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Clinents / Providers</b></font></td><td>
 *
 * This class is final, so it does not have any external provider.
 * Debugger Plug-ins and UI modules are clients of this class.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Lifecycle</b></font></td><td>
 *
 * A new instance(s) of DebuggerEngine class are created in Debugger Core 
 * module only, during the process of starting of debugging (see
 * {@link DebuggerManager#startDebugging}.
 *
 * DebuggerEngine is removed automatically from {@link DebuggerManager} when the 
 * the last action is ({@link #ACTION_KILL}).
 *
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Evolution</b></font></td><td>
 *
 * No method should be removed from this class, but some functionality can
 * be added in future.
 *
 * </td></tr></tbody></table>
 *
 * @author   Jan Jancura
 */
public final class DebuggerEngine {
    
    
    // variables ...............................................................
    
    private Lookup                  lookup;
    private ActionsManager          actionsManager;

    
    DebuggerEngine (
        String typeID, 
        Session s, 
        Object[] services,
        Lookup sessionLookup
    ) {
        Object[] services1 = new Object [services.length + 1];
        System.arraycopy (services, 0, services1, 0, services.length);
        services1 [services1.length - 1] = this;
        Lookup privateLookup = (services == null) ? 
            (Lookup) new Lookup.MetaInf (typeID) :
            new Lookup.Compound (
                new Lookup.Instance (services1),
                new Lookup.MetaInf (typeID)
            );
        this.lookup = new Lookup.Compound (
            privateLookup,
            sessionLookup
        );
    }
    
//    /**
//     * Returns list of services of given type.
//     *
//     * @param service a type of service to look for
//     * @return list of services of given type
//     */
//    public List lookup (Class service) {
//        return lookup.lookup (null, service);
//    }
//    
//    /**
//     * Returns one service of given type.
//     *
//     * @param service a type of service to look for
//     * @return ne service of given type
//     */
//    public Object lookupFirst (Class service) {
//        return lookup.lookupFirst (null, service);
//    }
    
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
    
    
    // main public methods .....................................................


    public ActionsManager getActionsManager () {
        if (actionsManager == null)
            actionsManager = new ActionsManager (lookup);
        return actionsManager;
    }
    
    
    // innerclasses ............................................................

    /**
     * This class notifies about DebuggerEngine remove from the system, and
     * about changes in language support. Instance of Destructor can be 
     * obtained from: {@link org.netbeans.spi.debugger.DebuggerEngineProvider#setDestructor(DebuggerEngine.Destructor)}, or
     * {@link org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider#setDestructor(DebuggerEngine.Destructor)}.
     */
    public class Destructor {
        
        /**
         * Removes DebuggerEngine form all sessions.
         */
        public void killEngine () {
            Session[] ss = DebuggerManager.getDebuggerManager ().getSessions ();
            int i, k = ss.length;
            for (i = 0; i < k; i++)
                ss [i].removeEngine (DebuggerEngine.this);
            getActionsManager ().destroy ();
        }
        
        /**
         * Removes given language support from given session.
         *
         * @param s a session
         * @param language a language to be removed
         */
        public void killLanguage (Session s, String language) {
            s.removeLanguage (language, DebuggerEngine.this);
            getActionsManager ().destroy ();
        }
    }
}

