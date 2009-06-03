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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.openide.awt;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jaroslav Tulach
 */
final class GeneralAction {

    /** Creates a new instance of DelegatingAction */
    private GeneralAction() {
    }
    
    static final Logger LOG = Logger.getLogger(GeneralAction.class.getName());
    
    public static ContextAwareAction callback(
        String key, Action defaultDelegate, Lookup context, boolean surviveFocusChange
    ) {
        if (key == null) {
            throw new NullPointerException();
        }
        return new DelegateAction(null, key, context, defaultDelegate, surviveFocusChange);
    }
    
    public static Action alwaysEnabled(Map map) {
        return new AlwaysEnabledAction(map);
    }

    public static ContextAwareAction callback(Map map) {
        return new DelegateAction(map);
    }

    public static <T> ContextAwareAction context(
        ContextActionPerformer<? super T> handler, 
        ContextActionEnabler<? super T> enabler,
        ContextSelection selectionType, 
        Lookup context, 
        Class<T> dataType
    ) {
        return new ContextAction<T>(handler, enabler, selectionType, context, dataType, false);
    }
    
    public static ContextAwareAction context(Map map) {
        ContextSelection sel = readSelection(map.get("selectionType")); // NOI18N
        Class<?> dataType = readClass(map.get("type")); // NOI18N
        LazyPerformer perf = new LazyPerformer(map);
        
        ContextAwareAction cAction = context(perf, perf, sel, Utilities.actionsGlobalContext(), dataType);
        return new DelegateAction(map, cAction);
    }
    
    private static ContextSelection readSelection(Object obj) {
        if (obj instanceof ContextSelection) {
            return (ContextSelection)obj;
        }
        if (obj instanceof String) {
            return ContextSelection.valueOf((String)obj);
        }
        throw new IllegalStateException("Cannot parse selectionType value: " + obj); // NOI18N
    }
    private static Class<?> readClass(Object obj) {
        if (obj instanceof Class) {
            return (Class)obj;
        }
        if (obj instanceof String) {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = Thread.currentThread().getContextClassLoader();
            }
            if (l == null) {
                l = GeneralAction.class.getClassLoader();
            }
            try {
                return Class.forName((String)obj, false, l);
            } catch (Exception ex) {
                throw (IllegalStateException)new IllegalStateException(ex.getMessage()).initCause(ex);
            }
        }
        throw new IllegalStateException("Cannot parse selectionType value: " + obj); // NOI18N
    }
    static final Object extractCommonAttribute(Map fo, Action action, String name) {
        return AlwaysEnabledAction.extractCommonAttribute(fo, action, name);
    }

    private static String localizedName(Map fo) {
        return (String) fo.get("displayName");
    }

    public Logger getLOG() {
        return LOG;
    }
    
    private static final class LazyPerformer 
    implements ContextActionPerformer<Object>, ContextActionEnabler<Object> {
        private Map delegate;
        
        public LazyPerformer(Map delegate) {
            this.delegate = delegate;
        }
        
        public void actionPerformed(ActionEvent ev, List<? extends Object> data) {
            ContextActionPerformer<Object> perf = (ContextActionPerformer<Object>)delegate.get("delegate"); // NOI18N
            if (perf == null) {
                LOG.warning("No 'delegate' for " + delegate); // NOI18N
                return;
            }
            perf.actionPerformed(ev, data);
        }
        public boolean enabled(List<? extends Object> data) {
            Object o = delegate.get("enabler"); // NOI18N
            if (o == null) {
                return true;
            }
            
            if (o instanceof ContextActionEnabler) {
                ContextActionEnabler<Object> en = (ContextActionEnabler<Object>)o;
                return en.enabled(data);
            }
            
            LOG.warning("Wrong enabler for " + delegate + ":" + o);
            return false;
        }

        @Override
        public int hashCode() {
            return delegate.hashCode() + 117;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof LazyPerformer) {
                LazyPerformer l = (LazyPerformer)obj;
                return delegate.equals(l.delegate);
            }
            return false;
        }
    } // end of LazyPerformer
    
    /** A delegate action that is usually associated with a specific lookup and
     * extract the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction.
     */
    static final class DelegateAction extends Object 
    implements Action, ContextAwareAction, PropertyChangeListener {
        /** file object, if we are associated to any */
        private Map map;
        /** action to delegate too */
        private Action fallback;
        /** key to delegate to */
        private Object key;

        /** global lookup to work with */
        private GlobalManager global;

        /** support for listeners */
        private PropertyChangeSupport support;

        /** listener to check listen on state of action(s) we delegate to */
        private PropertyChangeListener weakL;
        
        /** Constructs new action that is bound to given context and
         * listens for changes of <code>ActionMap</code> in order to delegate
         * to right action.
         */
        public DelegateAction(Map map, Object key, Lookup actionContext, Action fallback, boolean surviveFocusChange) {
            this.map = map;
            this.key = key;
            this.fallback = fallback;
            this.global = GlobalManager.findManager(actionContext, surviveFocusChange);
            this.weakL = WeakListeners.propertyChange(this, fallback);
            if (fallback != null) {
                fallback.addPropertyChangeListener(weakL);
            }
        }
        
        public DelegateAction(Map map, Action fallback) {
            this(
                map,
                map.get("key"), // NOI18N
                Utilities.actionsGlobalContext(), // NOI18N
                fallback, // NOI18N
                Boolean.TRUE.equals(map.get("surviveFocusChange")) // NOI18N
            );
        }
        public DelegateAction(Map map) {
            this(map, (Action)map.get("fallback")); // NOI18N
        }

        /** Overrides superclass method, adds delegate description. */
        @Override
        public String toString() {
            return super.toString() + "[key=" + key + "]"; // NOI18N
        }

        /** Invoked when an action occurs.
         */
        public void actionPerformed(final java.awt.event.ActionEvent e) {
            assert EventQueue.isDispatchThread();
            final javax.swing.Action a = findAction();
            if (a != null) {
                a.actionPerformed(e);
            }
        }

        public boolean isEnabled() {
            assert EventQueue.isDispatchThread();
            javax.swing.Action a = findAction();
            return a == null ? false : a.isEnabled();
        }
        
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            boolean first = false;
            if (support== null) {
                support = new PropertyChangeSupport(this);
                first = true;
            }
            support.addPropertyChangeListener(listener);
            if (first) {
                global.registerListener(key, this);
            }
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
            if (!support.hasListeners(null)) {
                global.unregisterListener(key, this);
                support = null;
            }
        }

        public void putValue(String key, Object o) {
        }

        public Object getValue(String key) {
            Object ret = GeneralAction.extractCommonAttribute(map, this, key);
            if (ret != null) {
                return ret;
            }
            
            Action a = findAction();
            return a == null ? null : a.getValue(key);
        }

        public void setEnabled(boolean b) {
        }

        void updateState(ActionMap prev, ActionMap now) {
            if (key == null) {
                return;
            }

            if (prev != null) {
                Action prevAction = prev.get(key);
                if (prevAction != null) {
                    prevAction.removePropertyChangeListener(weakL);
                }
            }
            if (now != null) {
                Action nowAction = now.get(key);
                if (nowAction != null) {
                    nowAction.addPropertyChangeListener(weakL);
                }
            }
        }

        /*** Finds an action that we should delegate to
         * @return the action or null
         */
        private Action findAction() {
            Action a = global.findGlobalAction(key);
            return a == null ? fallback : a;
        }

        /** Clones itself with given context.
         */
        public Action createContextAwareInstance(Lookup actionContext) {
            Action f = fallback;
            if (f instanceof ContextAwareAction) {
                f = ((ContextAwareAction)f).createContextAwareInstance(actionContext);
            }
            return new DelegateAction(map, key, actionContext, f, global.isSurvive());
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                PropertyChangeSupport sup;
                synchronized (this) {
                    sup = support;
                }
                if (sup != null) {
                    sup.firePropertyChange("enabled", null, null); // NOI18N
                }
            }
        }

        @Override
        public int hashCode() {
            int k = key == null ? 37 : key.hashCode();
            int m = map == null ? 17 : map.hashCode();
            int f = fallback == null ? 7 : fallback.hashCode();
            
            return (k << 2) + (m << 1) + f;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof DelegateAction) {
                DelegateAction d = (DelegateAction)obj;
                
                if (key != null && !key.equals(d.key)) {
                    return false;
                }
                if (map != null && !map.equals(d.map)) {
                    return false;
                }
                if (fallback != null && !fallback.equals(d.fallback)) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }   // end of DelegateAction
}
