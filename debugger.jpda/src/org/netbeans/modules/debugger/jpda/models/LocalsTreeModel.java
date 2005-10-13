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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
public class LocalsTreeModel implements TreeModel {

    
    private static boolean      verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('l') >= 0);
    
    /** Nest array elements when array length is bigger then this. */
    private static final int ARRAY_CHILDREN_NESTED_LENGTH = 100;
    
    
    private JPDADebuggerImpl    debugger;
    private Listener            listener;
    private Vector              listeners = new Vector ();
    private Map                 cachedLocals = new WeakHashMap();
    private Map                 cachedArrayChildren = new WeakHashMap();
    
    
    public LocalsTreeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    public Object getRoot () {
        return ROOT;
    }
    
    public Object[] getChildren (Object o, int from, int to) 
    throws UnknownTypeException {
        try {
            if (o.equals (ROOT)) {
                Object[] os = getLocalVariables (from, to);
                return os;
            } else
            if (o instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable abstractVariable = (AbstractVariable) o;
                boolean isArray =
                        abstractVariable.getInnerValue () instanceof ArrayReference;
                if (isArray) {
                    to = abstractVariable.getFieldsCount ();
                    // We need to reset it for arrays, to get the full array
                }
                if (isArray && (to - from) > ARRAY_CHILDREN_NESTED_LENGTH) {
                    ArrayChildrenNode achn =
                            (ArrayChildrenNode) cachedArrayChildren.get(abstractVariable.getInnerValue ());
                    if (achn == null) {
                        achn = new ArrayChildrenNode(abstractVariable);
                        cachedArrayChildren.put(abstractVariable.getInnerValue (), achn);
                    } else {
                        achn.update(abstractVariable);
                    }
                    return achn.getChildren();
                } else {
                    return abstractVariable.getFields (from, to);
                }
            } else
            if (o instanceof ArrayChildrenNode) {
                return ((ArrayChildrenNode) o).getChildren();
            } else
            throw new UnknownTypeException (o);
        } catch (VMDisconnectedException ex) {
            return new Object [0];
        }
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object node) throws UnknownTypeException {
        try {
            if (node.equals (ROOT)) {
                CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame ();
                if (frame == null) 
                    return 1;
                StackFrame sf = frame.getStackFrame ();
                if (sf == null) 
                    return 1;
                try {
                    int i = 0;
                    try {
                        i = sf.visibleVariables ().size ();
                    } catch (AbsentInformationException ex) {
                        i++;
                    }
                    if (sf.thisObject () != null) i++;
                    return i;
                } catch (NativeMethodException ex) {
                    return 1;//throw new NoInformationException ("native method");
                } catch (InternalException ex) {
                    return 1;//throw new NoInformationException ("native method");
                } catch (InvalidStackFrameException ex) {
                    return 1;//throw new NoInformationException ("thread is running");
                } catch (VMDisconnectedException ex) {
                }
                return 0;
            } else
            if (node instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable abstractVariable = (AbstractVariable) node;
                if (abstractVariable.getInnerValue () instanceof 
                    ArrayReference
                ) 
                    return Math.min (abstractVariable.getFieldsCount (), ARRAY_CHILDREN_NESTED_LENGTH);
                return abstractVariable.getFieldsCount ();
            } else
            if (node instanceof ArrayChildrenNode) {
                return ((ArrayChildrenNode) node).getChildren().length;
            } else
            throw new UnknownTypeException (node);
        } catch (VMDisconnectedException ex) {
        }
        return 0;
    }
    
    public boolean isLeaf (Object o) throws UnknownTypeException {
        if (o.equals (ROOT))
            return false;
        if (o instanceof AbstractVariable)
            return !(((AbstractVariable) o).getInnerValue () instanceof ObjectReference);
        if (o.toString().startsWith("SubArray")) {
            return false;
        }
        if (o.equals ("NoInfo")) // NOI18N
            return true;
        throw new UnknownTypeException (o);
    }


    public void addModelListener (ModelListener l) {
        listeners.add (l);
        if (listener == null)
            listener = new Listener (this, debugger);
    }

    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
        if (listeners.size () == 0) {
            listener.destroy ();
            listener = null;
        }
    }
    
    void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TreeChanged (this)
            );
    }
    
    void fireTableValueChangedChanged (Object node, String propertyName) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TableValueChanged (this, node, propertyName)
            );
    }

    
    // private methods .........................................................
    
    private Object[] getLocalVariables (
        int from, 
        int to
    ) {
        synchronized (debugger.LOCK) {
            CallStackFrameImpl callStackFrame = (CallStackFrameImpl) debugger.
                getCurrentCallStackFrame ();
            if (callStackFrame == null) 
                return new String [] {"No current thread"};
            StackFrame stackFrame = callStackFrame.getStackFrame ();
            if (stackFrame == null) 
                return new String [] {"No current thread"};
            try {
                ObjectReference thisR = stackFrame.thisObject ();
                if (thisR == null) {
                    Object[] avs = null;
                    try {
                        return getLocalVariables (
                            callStackFrame,
                            stackFrame,
                            from,
                            to
                        );
                    } catch (AbsentInformationException ex) {
                        return new String [] {"NoInfo"};
                    }
                } else {
                    Object[] avs = null;
                    try {
                        avs = getLocalVariables (
                            callStackFrame,
                            stackFrame,
                            Math.max (from - 1, 0),
                            Math.max (to - 1, 0)
                        );
                    } catch (AbsentInformationException ex) {
                        avs = new Object[] {"NoInfo"};
                    }
                    Object[] result = new Object [avs.length + 1];
                    if (from < 1)
                        result [0] = getThis (thisR, "");
                    System.arraycopy (avs, 0, result, 1, avs.length);
                    return result;
                }            
            } catch (InternalException ex) {
                return new String [] {ex.getMessage ()};
            }
        } // synchronized
    }
    
    AbstractVariable[] getLocalVariables (
        final CallStackFrameImpl    callStackFrame, 
        final StackFrame            stackFrame,
        int                         from,
        int                         to
    ) throws AbsentInformationException {
        try {
            String className = stackFrame.location ().declaringType ().name ();
            List l = stackFrame.visibleVariables ();
            to = Math.min(l.size(), to);
            from = Math.min(l.size(), from);
            int i, k = to - from, j = from;
            AbstractVariable[] locals = new AbstractVariable [k];
            for (i = 0; i < k; i++) {
                LocalVariable lv = (LocalVariable) l.get (j++);
                locals [i] = getLocal (lv, callStackFrame, className);
            }
            return locals;
        } catch (NativeMethodException ex) {
            throw new AbsentInformationException ("native method");
        } catch (InvalidStackFrameException ex) {
            throw new AbsentInformationException ("thread is running");
        } catch (VMDisconnectedException ex) {
            return new AbstractVariable [0];
        }
    }
    
    ThisVariable getThis (ObjectReference thisR, String parentID) {
        return new ThisVariable (this, thisR, parentID);
    }
    
    private Local getLocal (LocalVariable lv, CallStackFrameImpl frame, String className) {
        Value v = frame.getStackFrame ().getValue (lv);
        Local local = (Local) cachedLocals.get(lv);
        if (local != null) {
            local.setInnerValue(v);
            local.setFrame(frame);
            local.setLocalVariable(lv);
        } else {
            if (v instanceof ObjectReference) {
                local = new ObjectLocalVariable (
                    this, 
                    v, 
                    className, 
                    lv, 
                    JPDADebuggerImpl.getGenericSignature (lv), 
                    frame
                );
            } else {
                local = new Local (this, v, className, lv, frame);
            }
            cachedLocals.put(lv, local);
        }
        return local;
    }
    
    public Variable getVariable (Value v) {
        if (v instanceof ObjectReference)
            return new AbstractVariable (
                this,
                (ObjectReference) v,
                null
            );
        return new AbstractVariable (this, v, null);
    }
    
    
    JPDADebuggerImpl getDebugger () {
        return debugger;
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebugger debugger;
        private WeakReference model;
        
        public Listener (
            LocalsTreeModel tm,
            JPDADebugger debugger
        ) {
            this.debugger = debugger;
            model = new WeakReference (tm);
            debugger.addPropertyChangeListener (this);
        }
        
        void destroy () {
            debugger.removePropertyChangeListener (this);
            if (task != null) {
                // cancel old task
                task.cancel ();
                if (verbose)
                    System.out.println("LTM cancel old task " + task);
                task = null;
            }
        }
        
        private LocalsTreeModel getModel () {
            LocalsTreeModel tm = (LocalsTreeModel) model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange (PropertyChangeEvent e) {
            if ( ( (e.getPropertyName () == 
                     JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                   //(e.getPropertyName () == debugger.PROP_CURRENT_THREAD) ||
                   (e.getPropertyName () == JPDADebugger.PROP_STATE)
                 ) && (debugger.getState () == JPDADebugger.STATE_STOPPED)
            ) {
                // IF state has been changed to STOPPED or
                // IF current call stack frame has been changed & state is stoped
                final LocalsTreeModel ltm = getModel ();
                if (ltm == null) return;
                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    if (verbose)
                        System.out.println("LTM cancel old task " + task);
                    task = null;
                }
                task = RequestProcessor.getDefault ().post (new Runnable () {
                    public void run () {
                        if (debugger.getState () != JPDADebugger.STATE_STOPPED) {
                            if (verbose)
                                System.out.println("LTM cancel started task " + task);
                            return;
                        }
                        if (verbose)
                            System.out.println("LTM do task " + task);
                        ltm.fireTreeChanged ();
                    }
                }, 500);
                if (verbose)
                    System.out.println("LTM  create task " + task);
            } else
            if ( (e.getPropertyName () == JPDADebugger.PROP_STATE) &&
                 (debugger.getState () != JPDADebugger.STATE_STOPPED) &&
                 (task != null)
            ) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel ();
                if (verbose)
                    System.out.println("LTM cancel task " + task);
                task = null;
            }
        }
    }
    
    /**
     * The hierarchical representation of nested array elements.
     * Used for arrays longer then {@link #ARRAY_CHILDREN_NESTED_LENGTH}.
     */
    private static final class ArrayChildrenNode {
        
        private AbstractVariable var;
        private int from = 0;
        private int length;
        private int maxIndexLog;
        
        public ArrayChildrenNode(AbstractVariable var) {
            this(var, 0, var.getFieldsCount(), -1);
        }
        
        private ArrayChildrenNode(AbstractVariable var, int from, int length,
                                  int maxIndex) {
            this.var = var;
            this.from = from;
            this.length = length;
            if (maxIndex < 0) {
                maxIndex = from + length - 1;
            }
            this.maxIndexLog = ArrayFieldVariable.log10(maxIndex);
        }
        
        private static int pow(int a, int b) {
            if (b == 0) return 1;
            int p = a;
            for (int i = 1; i < b; i++) {
                p *= a;
            }
            return p;
        }
        
        public Object[] getChildren() {
            if (length > ARRAY_CHILDREN_NESTED_LENGTH) {
                int depth = (int) Math.ceil(Math.log(length)/Math.log(ARRAY_CHILDREN_NESTED_LENGTH) - 1);
                int n = pow(ARRAY_CHILDREN_NESTED_LENGTH, depth);
                int numCh = (int) Math.ceil(length/((double) n));
                
                // We have 'numCh' children, each with 'n' sub-children (or possibly less for the last one)
                Object[] ch = new Object[numCh];
                for (int i = 0; i < numCh; i++) {
                    int chLength = n;
                    if (i == (numCh - 1)) {
                        chLength = length % n;
                        if (chLength == 0) chLength = n;
                    }
                    ch[i] = new ArrayChildrenNode(var, from + i*n, chLength, from + length - 1);
                }
                return ch;
            } else {
                return var.getFields(from, from + length);
            }
        }
        
        public void update(AbstractVariable var) {
            this.var = var;
        }
        
        /** Overriden equals so that the nodes are not re-created when not necessary. */
        public boolean equals(Object obj) {
            if (!(obj instanceof ArrayChildrenNode)) return false;
            ArrayChildrenNode achn = (ArrayChildrenNode) obj;
            return achn.var.equals(this.var) &&
                   achn.from == this.from &&
                   achn.length == this.length;
        }
        
        public int hashCode() {
            return var.hashCode() + from + length;
        }
        
        public String toString() {
            int num0 = maxIndexLog - ArrayFieldVariable.log10(from);
            String froms;
            if (num0 > 0) {
                froms = ArrayFieldVariable.zeros(2*num0) + from; // One space is roughly 1/2 of width of a number
            } else {
                froms = Integer.toString(from);
            }
            int last = from + length - 1;
            num0 = maxIndexLog - ArrayFieldVariable.log10(last);
            String lasts;
            if (num0 > 0) {
                lasts = ArrayFieldVariable.zeros(2*num0) + last; // One space is roughly 1/2 of width of a number
            } else {
                lasts = Integer.toString(last);
            }
            return "SubArray"+froms+"-"+lasts; // NOI18N
        }
        
    }
}
