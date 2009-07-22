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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;

import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.Expression;
import org.netbeans.modules.debugger.jpda.expr.ParseException;

import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;

/**
 * @author   Jan Jancura
 */
public class WatchesModel implements TreeModel {

    
    private static boolean verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('w') >= 0);

    private JPDADebuggerImpl    debugger;
    private Listener            listener;
    private Vector<ModelListener> listeners = new Vector<ModelListener>();
    private ContextProvider     lookupProvider;
    // Watch to Expression or Exception
    private final Map<Watch, JPDAWatchEvaluating>  watchToValue = new WeakHashMap<Watch, JPDAWatchEvaluating>(); // <node (expression), JPDAWatch>
    private final JPDAWatch EMPTY_WATCH;

    
    public WatchesModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        this.lookupProvider = lookupProvider;
        EMPTY_WATCH = new EmptyWatch();
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }

    /**
     *
     * @return watches contained in this group of watches
     */
    public Object[] getChildren (Object parent, int from, int to) 
    throws UnknownTypeException {
        if (parent == ROOT) {
            
            // 1) get Watches
            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            to = Math.min(ws.length, to);
            from = Math.min(ws.length, from);
            Watch[] fws = new Watch [to - from];
            System.arraycopy (ws, from, fws, 0, to - from);
            
            // 2) create JPDAWatches for Watches
            int i, k = fws.length;
            JPDAWatch[] jws = new JPDAWatch [k + 1];
            for (i = 0; i < k; i++) {
                
                JPDAWatchEvaluating jw = watchToValue.get(fws[i]);
                if (jw == null) {
                    jw = new JPDAWatchEvaluating(this, fws[i], debugger);
                    watchToValue.put(fws[i], jw);
                }
                jws[i] = jw;
                
                // The actual expressions are computed on demand in JPDAWatchEvaluating
            }
            jws[k] = EMPTY_WATCH;
            
            if (listener == null)
                listener = new Listener (this, debugger);
            return jws;
        }
        if (parent instanceof JPDAWatchImpl) {
            return getLocalsTreeModel ().getChildren (parent, from, to);
        }
        return getLocalsTreeModel ().getChildren (parent, from, to);
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
        if (node == ROOT) {
            if (listener == null)
                listener = new Listener (this, debugger);
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            //return DebuggerManager.getDebuggerManager ().getWatches ().length;
        }
        if (node instanceof JPDAWatchImpl) {
            return getLocalsTreeModel ().getChildrenCount (node);
        }
        return getLocalsTreeModel ().getChildrenCount (node);
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof JPDAWatchEvaluating) {
            JPDAWatchEvaluating jwe = (JPDAWatchEvaluating) node;
            JPDAWatch jw = jwe.getEvaluatedWatch();
            if (jw instanceof JPDAWatchImpl) {
                return ((JPDAWatchImpl) jw).isPrimitive ();
            }
        }
        if (node == EMPTY_WATCH) return true;
        return getLocalsTreeModel ().isLeaf (node);
    }

    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTreeChanged () {
        synchronized (watchToValue) {
            for (Iterator<JPDAWatchEvaluating> it = watchToValue.values().iterator(); it.hasNext(); ) {
                it.next().setEvaluated(null);
            }
        }
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        ModelEvent event = new ModelEvent.TreeChanged(this);
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (event);
    }
    
    private void fireWatchesChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        ModelEvent event = new ModelEvent.NodeChanged(this, ROOT, ModelEvent.NodeChanged.CHILDREN_MASK);
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (event);
    }
    
    void fireTableValueChangedChanged (Object node, String propertyName) {
        ((JPDAWatchEvaluating) node).setEvaluated(null);
        fireTableValueChangedComputed(node, propertyName);
    }
        
    void fireTableValueChangedComputed (Object node, String propertyName) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TableValueChanged (this, node, propertyName)
            );
    }
    
    
    // other methods ...........................................................
    
    JPDADebuggerImpl getDebugger () {
        return debugger;
    }
    
    private LocalsTreeModel localsTreeModel;

    LocalsTreeModel getLocalsTreeModel () {
        if (localsTreeModel == null)
            localsTreeModel = (LocalsTreeModel) lookupProvider.
                lookupFirst ("LocalsView", TreeModel.class);
        return localsTreeModel;
    }


    // innerclasses ............................................................
    
    private static class JPDAWatchEvaluating extends AbstractObjectVariable
                                             implements JPDAWatch, Variable,
                                                        Refreshable, //.Lazy {
                                                        PropertyChangeListener {
        
        private WatchesModel model;
        private Watch w;
        private JPDADebuggerImpl debugger;
        private JPDAWatch evaluatedWatch;
        private Expression expression;
        private ParseException parseException;
        private final boolean[] evaluating = new boolean[] { false };
        
        public JPDAWatchEvaluating(WatchesModel model, Watch w, JPDADebuggerImpl debugger) {
            this(model, w, debugger, 0);
        }
        
        private JPDAWatchEvaluating(WatchesModel model, Watch w, JPDADebuggerImpl debugger, int cloneNumber) {
            super(debugger, null, (cloneNumber > 0) ? w + "_clone" + cloneNumber : "" + w);
            this.model = model;
            this.w = w;
            this.debugger = debugger;
            parseExpression(w.getExpression());
            if (cloneNumber == 0) {
                debugger.varChangeSupport.addPropertyChangeListener(WeakListeners.propertyChange(this, debugger.varChangeSupport));
            }
        }
        
        private void parseExpression(String exprStr) {
            try {
                expression = Expression.parse (
                    exprStr, 
                    Expression.LANGUAGE_JAVA_1_5
                );
                parseException = null;
            } catch (ParseException e) {
                setEvaluated(new JPDAWatchImpl(debugger, w, e, this));
                parseException = e;
            }
        }
        
        Expression getParsedExpression() throws ParseException {
            if (parseException != null) {
                throw parseException;
            }
            return expression;
        }

        
        public void setEvaluated(JPDAWatch evaluatedWatch) {
            synchronized (this) {
                this.evaluatedWatch = evaluatedWatch;
            }
            if (evaluatedWatch != null) {
                if (evaluatedWatch instanceof JPDAWatchImpl) {
                    setInnerValue(((JPDAWatchImpl) evaluatedWatch).getInnerValue());
                } else if (evaluatedWatch instanceof JPDAObjectWatchImpl) {
                    setInnerValue(((JPDAObjectWatchImpl) evaluatedWatch).getInnerValue());
                }
                //propSupp.firePropertyChange(PROP_INITIALIZED, null, Boolean.TRUE);
            } else {
                setInnerValue(null);
            }
            //model.fireTableValueChangedComputed(this, null);
        }
        
        synchronized JPDAWatch getEvaluatedWatch() {
            return evaluatedWatch;
        }
        
        public void expressionChanged() {
            setEvaluated(null);
            parseExpression(w.getExpression());
        }
        
        public synchronized String getExceptionDescription() {
            if (evaluatedWatch != null) {
                return evaluatedWatch.getExceptionDescription();
            } else {
                return null;
            }
        }

        public synchronized String getExpression() {
            if (evaluatedWatch != null) {
                return evaluatedWatch.getExpression();
            } else {
                return w.getExpression();
            }
        }

        @Override
        public String getToStringValue() throws InvalidExpressionException {
            JPDAWatch evaluatedWatch;
            synchronized (this) {
                evaluatedWatch = this.evaluatedWatch;
            }
            if (evaluatedWatch == null) {
                JPDAWatch[] watchRef = new JPDAWatch[] { null };
                getValue(watchRef); // To init the evaluatedWatch
                evaluatedWatch = watchRef[0];
            }
            String e = evaluatedWatch.getExceptionDescription();
            if (e != null) {
                return ">" + e + "<"; // NOI18N
            } else {
                return evaluatedWatch.getToStringValue();
            }
        }

        @Override
        public String getType() {
            JPDAWatch evaluatedWatch;
            synchronized (this) {
                evaluatedWatch = this.evaluatedWatch;
            }
            if (evaluatedWatch == null) {
                JPDAWatch[] watchRef = new JPDAWatch[] { null };
                getValue(watchRef); // To init the evaluatedWatch
                evaluatedWatch = watchRef[0];
            }
            return evaluatedWatch.getType();
        }

        @Override
        public String getValue() {
            return getValue((JPDAWatch[]) null);
        }
        
        private String getValue(JPDAWatch[] watchRef) {
            synchronized (evaluating) {
                if (evaluating[0]) {
                    try {
                        evaluating.wait();
                    } catch (InterruptedException iex) {
                        return null;
                    }
                }
                synchronized (this) {
                    if (evaluatedWatch != null) {
                        if (watchRef != null) watchRef[0] = evaluatedWatch;
                        return evaluatedWatch.getValue();
                    }
                }
                evaluating[0] = true;
            }
            
            JPDAWatch jw = null;
            try {
                Expression expr = getParsedExpression();
                Value v = debugger.evaluateIn (expr);
                //if (v instanceof ObjectReference)
                //    jw = new JPDAObjectWatchImpl (debugger, w, (ObjectReference) v);
                if (v instanceof PrimitiveValue) {
                    JPDAWatchImpl jwi = new JPDAWatchImpl (debugger, w, (PrimitiveValue) v, this);
                    jwi.addPropertyChangeListener(this);
                    jw = jwi;
                } else { // ObjectReference or VoidValue
                    JPDAObjectWatchImpl jwi = new JPDAObjectWatchImpl (debugger, w, v);
                    jwi.addPropertyChangeListener(this);
                    jw = jwi;
                    /* Uncomment if evaluator returns variables with disabled collection
                    if (v instanceof ObjectReference) {
                        // Returned variable with disabled collection. When not used any more,
                        // it's collection must be enabled again.
                        try {
                            ObjectReferenceWrapper.enableCollection((ObjectReference) v);
                        } catch (Exception ex) {}
                    }*/
                }
            } catch (InvalidExpressionException e) {
                JPDAWatchImpl jwi = new JPDAWatchImpl (debugger, w, e, this);
                jwi.addPropertyChangeListener(this);
                jw = jwi;
            } catch (ParseException e) {
                JPDAWatchImpl jwi = new JPDAWatchImpl (debugger, w, e, this);
                jwi.addPropertyChangeListener(this);
                jw = jwi;
            } finally {
                setEvaluated(jw);
                if (watchRef != null) watchRef[0] = jw;
                synchronized (evaluating) {
                    evaluating[0] = false;
                    evaluating.notifyAll();
                }
            }
            //System.out.println("    value = "+jw.getValue());
            return jw.getValue();
        }

        public synchronized void remove() {
            if (evaluatedWatch != null) {
                evaluatedWatch.remove();
            } else {
                w.remove ();
            }
        }

        public void setExpression(String expression) {
            w.setExpression (expression);
            expressionChanged();
        }

        @Override
        public synchronized void setValue(String value) throws InvalidExpressionException {
            if (evaluatedWatch != null) {
                evaluatedWatch.setValue(value);
            } else {
                throw new InvalidExpressionException("Can not set value while evaluating.");
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof JPDAWatchEvaluating) {
                // Do not re-fire my own changes
                return;
            }
            model.fireTableValueChangedChanged (this, null);
        }
        
        /** Does wait for the value to be evaluated. */
        public void refresh() throws RefreshFailedException {
            synchronized (evaluating) {
                if (evaluating[0]) {
                    try {
                        evaluating.wait();
                    } catch (InterruptedException iex) {
                        throw new RefreshFailedException(iex.getLocalizedMessage());
                    }
                }
            }
        }
        
        /** Tells whether the variable is fully initialized and getValue()
         *  returns the value immediately. */
        public synchronized boolean isCurrent() {
            return evaluatedWatch != null;
        }
        
        private int cloneNumber = 1;

        @Override
        public JPDAWatchEvaluating clone() {
            JPDAWatchEvaluating clon = new JPDAWatchEvaluating(model, w, debugger, cloneNumber++);
            clon.setEvaluated(evaluatedWatch);
            return clon;
        }
        
    }
    
    private static class Listener extends DebuggerManagerAdapter implements 
    PropertyChangeListener {
        
        private WeakReference<WatchesModel> model;
        private WeakReference<JPDADebuggerImpl> debugger;
        
        private Listener (
            WatchesModel tm,
            JPDADebuggerImpl debugger
        ) {
            model = new WeakReference<WatchesModel>(tm);
            this.debugger = new WeakReference<JPDADebuggerImpl>(debugger);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_WATCHES,
                this
            );
            debugger.addPropertyChangeListener (this);
            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            int i, k = ws.length;
            for (i = 0; i < k; i++)
                ws [i].addPropertyChangeListener (this);
        }
        
        private WatchesModel getModel () {
            WatchesModel m = model.get ();
            if (m == null) destroy ();
            return m;
        }
        
        @Override
        public void watchAdded (Watch watch) {
            WatchesModel m = getModel ();
            if (m == null) return;
            watch.addPropertyChangeListener (this);
            m.fireWatchesChanged ();
        }
        
        @Override
        public void watchRemoved (Watch watch) {
            WatchesModel m = getModel ();
            if (m == null) return;
            watch.removePropertyChangeListener (this);
            m.fireWatchesChanged ();
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private Task task;
        
        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            // We already have watchAdded & watchRemoved. Ignore PROP_WATCHES:
            // We care only about the current call stack frame change and watch expression change here...
            if (!(JPDADebugger.PROP_STATE.equals(propName) || Watch.PROP_EXPRESSION.equals(propName) ||
                    JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(propName))) return;
            final WatchesModel m = getModel ();
            if (m == null) return;
            if (JPDADebugger.PROP_STATE.equals(propName) &&
                m.debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
                
                    destroy ();
                    return;
            }
            if (m.debugger.getState () == JPDADebugger.STATE_RUNNING ||
                 JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(propName) &&
                 m.debugger.getCurrentCallStackFrameOrNull() == null) {

                    return;
            }
            
            if (evt.getSource () instanceof Watch) {
                Object node;
                synchronized (m.watchToValue) {
                    node = m.watchToValue.get(evt.getSource());
                }
                if (node != null) {
                    m.fireTableValueChangedChanged(node, null);
                    return ;
                }
            }
            
            if (task == null) {
                task = m.debugger.getRequestProcessor().create (new Runnable () {
                    public void run () {
                        if (verbose)
                            System.out.println("WM do task " + task);
                        m.fireTreeChanged ();
                    }
                });
                if (verbose)
                    System.out.println("WM  create task " + task);
            }
            task.schedule(100);
        }
        
        private void destroy () {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_WATCHES,
                this
            );
            JPDADebugger d = debugger.get ();
            if (d != null)
                d.removePropertyChangeListener (this);

            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            int i, k = ws.length;
            for (i = 0; i < k; i++)
                ws [i].removePropertyChangeListener (this);

            if (task != null) {
                // cancel old task
                task.cancel ();
                if (verbose)
                    System.out.println("WM cancel old task " + task);
                task = null;
            }
        }
    }
    
    /**
     * The last empty watch, that can be used to enter new watch expressions.
     */
    private final class EmptyWatch implements JPDAWatch {
        
    
        public String getExpression() {
            return ""; // NOI18N
        }

        public void setExpression(String expr) {
            String infoStr = NbBundle.getBundle (WatchesModel.class).getString("CTL_WatchesModel_Empty_Watch_Hint");
            infoStr = "<" + infoStr + ">";
            if (expr == null || expr.trim().length() == 0 || infoStr.equals(expr)) {
                return; // cancel action
            }
            DebuggerManager.getDebuggerManager().createWatch(expr);
            
            Vector v = (Vector) listeners.clone ();
            int i, k = v.size ();
            for (i = 0; i < k; i++)
                ((ModelListener) v.get (i)).modelChanged (
                    new ModelEvent.NodeChanged (WatchesModel.this, EmptyWatch.this)
                );
        }

        public void remove() {
            // Can not be removed
        }

        public String getType() {
            return ""; // NOI18N
        }

        public String getValue() {
            return ""; // NOI18N
        }

        public String getExceptionDescription() {
            return null;
        }

        public void setValue(String value) throws InvalidExpressionException {
            // Can not be set
        }

        public String getToStringValue() throws InvalidExpressionException {
            return ""; // NOI18N
        }
    }
}
