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

package org.netbeans.modules.debugger.jpda.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.jpda.EngineContextProvider;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.jpda.SmartSteppingListener;


public class SmartSteppingImpl extends SmartSteppingListener implements 
PropertyChangeListener {
    
    
    private Set exclusionPatterns = new HashSet (); 
    private SmartSteppingFilter smartSteppingFilter;
    
    
    /**
     * Defines default set of smart stepping filters. Method is called when 
     * a new JPDA debugger session is created.
     *
     * @param f a filter to be initialized
     */
    public void initFilter (SmartSteppingFilter f) {
        smartSteppingFilter = f;
    }
    
    /**
     * This method is called during stepping through debugged application.
     * The execution is stopped when all registerred SmartSteppingListeners
     * returns true.
     *
     * @param thread contains all available information about current position
     *        in debugged application
     * @param f a filter
     * @return true if execution should be stopped on the current position
     */
    public boolean stopHere (
        LookupProvider lookupProvider, 
        JPDAThread thread, 
        SmartSteppingFilter f
    ) {
        String className = thread.getClassName ();
        if (className == null) return false;

        EngineContext ectx = getEngineContext (lookupProvider);
        boolean b = ectx.sourceAvailable (thread, null);
        if (b) return true;
        
        // find pattern
        String name, n1 = className.replace ('.', '/');
        do {
            name = n1;
            int i = name.lastIndexOf ('/');
            if (i < 0) break;
            n1 = name.substring (0, i);
        } while (!ectx.sourceAvailable (n1));
        HashSet s = new HashSet ();
        s.add (name.replace ('/', '.') + ".*");
        addExclusionPatterns (s);
        return false;
    }
    
    private void addExclusionPatterns (
        Set ep
    ) {
        smartSteppingFilter.addExclusionPatterns (ep);
        exclusionPatterns.addAll (ep);
    }
    
    private void removeExclusionPatterns () {
        smartSteppingFilter.removeExclusionPatterns (exclusionPatterns);
        exclusionPatterns = new HashSet ();
    }
    
    private EngineContext engineContext;
    
    private EngineContext getEngineContext (LookupProvider lookupProvider) {
        if (engineContext == null) {
            engineContext = (EngineContext) lookupProvider.lookupFirst 
                (EngineContext.class);
            engineContext.addPropertyChangeListener (this);
        }
        return engineContext;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName () == EngineContextProvider.PROP_SOURCE_ROOTS) {
            removeExclusionPatterns ();
        }
    }
}
