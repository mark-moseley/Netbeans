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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerEngineAdapter;
import org.netbeans.api.debugger.DebuggerEngineListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;


/**
 *
 * @author   Jan Jancura
 */
public abstract class DebuggerAction extends AbstractAction {

    public DebuggerAction () {
        new Listener (this);
        setEnabled (false);
    }
    
    public abstract Object getAction ();
    
    public void actionPerformed (ActionEvent evt) {
        DebuggerManager.getDebuggerManager ().getCurrentSession ().
            getCurrentEngine ().doAction (
                getAction ()
            );
    }

    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE and on current engine
     * on PROP_ACTION_STATE and updates state of this action instance.
     */
    static class Listener extends DebuggerManagerAdapter 
    implements DebuggerEngineListener {
        
        private DebuggerEngine currentEngine;
        private WeakReference ref;

        
        Listener (DebuggerAction da) {
            ref = new WeakReference (da);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            if (currentEngine != null)
                currentEngine.removeEngineListener 
                    (DebuggerEngineListener.PROP_ACTION_STATE_CHANGED, this);
            currentEngine = null;
            
            DebuggerEngine ne = DebuggerManager.getDebuggerManager ().
                getCurrentEngine ();
            if (ne == null) {
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        da.setEnabled (false);
                    }
                });
                return;
            }
            currentEngine = ne;
            currentEngine.addEngineListener
                (DebuggerEngineListener.PROP_ACTION_STATE_CHANGED, this);
            final boolean en = ne.isEnabled (da.getAction ());
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    da.setEnabled (en);
                }
            });
        }
        
        public void actionPerformed (DebuggerEngine engine, Object action, boolean success) {
        }
        public void actionStateChanged (
            final DebuggerEngine engine,
            final Object action, 
            final boolean enabled
        ) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            if (action != da.getAction ()) return;
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    da.setEnabled (enabled);
                }
            });
        }
        
        private DebuggerAction getDebuggerAction () {
            DebuggerAction da = (DebuggerAction) ref.get ();
            if (da == null) {
                DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                    DebuggerManager.PROP_CURRENT_ENGINE,
                    this
                );
                if (currentEngine != null)
                    currentEngine.removeEngineListener 
                        (DebuggerEngineListener.PROP_ACTION_STATE_CHANGED, this);
                currentEngine = null;
                return null;
            }
            return da;
        }
    }
}

