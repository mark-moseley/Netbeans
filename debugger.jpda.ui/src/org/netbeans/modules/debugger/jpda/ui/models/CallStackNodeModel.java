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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Jan Jancura
 */
public class CallStackNodeModel implements NodeModel {

    public static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame";
    public static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame";

    private JPDADebugger debugger;
    private Session session;
    private Vector listeners = new Vector ();
    
    
    public CallStackNodeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        session = (Session) lookupProvider.
            lookupFirst (null, Session.class);
        new Listener (this, debugger);
    }
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return "Name";
        } else
        if (o instanceof CallStackFrame) {
            CallStackFrame sf = (CallStackFrame) o;
            return getCSFName (session, sf, false);
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return "Name";
        } else
        if (o instanceof CallStackFrame) {
            CallStackFrame sf = (CallStackFrame) o;
            return getCSFName (session, sf, true);
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object node) throws UnknownTypeException {
        if (node instanceof String) return null;
        if (node instanceof CallStackFrame) {
            CallStackFrame ccsf = debugger.getCurrentCallStackFrame ();
            if ( (ccsf != null) && 
                 (ccsf.equals (node)) 
            ) return CURRENT_CALL_STACK;
            return CALL_STACK;
        }
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeChanged ();
    }
    
    private void fireTreeNodeChanged (Object parent) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeNodeChanged (parent);
    }

    public static String getLanguage (Session s, CallStackFrame sf) {
        String language = s.getCurrentLanguage ();
        List l = sf.getAvailableStrata ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            if (language.equals (l.get (i)))
                return language;
        return sf.getDefaultStratum ();
    }

    public static String getCSFName (
        Session s, 
        CallStackFrame sf,
        boolean l
    ) {
        String language = getLanguage (s, sf);
        int ln = sf.getLineNumber (language);
        String fileName = l ? 
            sf.getClassName () :
            BreakpointsNodeModel.getShort (sf.getClassName ());
        if (language.equals ("Java"))
            fileName += "." + sf.getMethodName ();
        else
            try {
                fileName = sf.getSourcePath (language);
            } catch (NoInformationException e) {
                fileName += "." + sf.getMethodName ();
            }
        if (ln < 0)
            return fileName;
        return fileName + ":" + ln;
    }
            
    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE, and on 
     * currentTreeModel.
     */
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference ref;
        private JPDADebugger debugger;
        
        private Listener (
            CallStackNodeModel rm,
            JPDADebugger debugger
        ) {
            ref = new WeakReference (rm);
            this.debugger = debugger;
            debugger.addPropertyChangeListener (
                debugger.PROP_CURRENT_CALL_STACK_FRAME,
                this
            );
        }
        
        private CallStackNodeModel getModel () {
            CallStackNodeModel rm = (CallStackNodeModel) ref.get ();
            if (rm == null) {
                debugger.removePropertyChangeListener (
                    debugger.PROP_CURRENT_CALL_STACK_FRAME,
                    this
                );
            }
            return rm;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            CallStackNodeModel rm = getModel ();
            if (rm == null) return;
            rm.fireTreeChanged ();
        }
    }
}
