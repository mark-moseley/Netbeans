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
 * The Original Software is NetBeans. The Initial Developer of the OriginalM
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.multiview;

import javax.swing.Action;
import org.openide.util.Lookup;
import java.beans.*;
import java.util.*;
import javax.swing.ActionMap;

import org.openide.nodes.*;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

class MultiViewTopComponentLookup extends Lookup {

    private MyProxyLookup proxy;
    private InitialProxyLookup initial;
    
    public MultiViewTopComponentLookup(ActionMap initialObject) {
        super();
        // need to delegate in order to get the correct Lookup.Templates that refresh..
        initial = new InitialProxyLookup(initialObject);
        proxy = new MyProxyLookup(initial);
    }
    
    
    public void setElementLookup(Lookup look) {
        proxy.setElementLookup(look);
        initial.refreshLookup();
    }
    
    @Override
    public Lookup.Item lookupItem(Lookup.Template template) {
        Lookup.Item retValue;
        if (template.getType() == ActionMap.class || (template.getId() != null && template.getId().equals("javax.swing.ActionMap"))) {
            return initial.lookupItem(template);
        }
        // do something here??
        retValue = super.lookupItem(template);
        return retValue;
    }    
    
     
    public Object lookup(Class clazz) {
        if (clazz == ActionMap.class) {
            return initial.lookup(clazz);
        }
        Object retValue;
        
        retValue = proxy.lookup(clazz);
        return retValue;
    }
    
    public Lookup.Result lookup(Lookup.Template template) {
        
        if (template.getType() == ActionMap.class || (template.getId() != null && template.getId().equals("javax.swing.ActionMap"))) {
            return initial.lookup(template);
        }
        Lookup.Result retValue;
        retValue = proxy.lookup(template);
        retValue = new ExclusionResult(retValue);
        return retValue;
    }
    
    /**
     * A lookup result excluding some instances.
     */
    private static final class ExclusionResult extends Lookup.Result implements LookupListener {
        
        private final Lookup.Result delegate;
        private final List listeners = new ArrayList(); // List<LookupListener>
        private Collection lastResults;
        
        public ExclusionResult(Lookup.Result delegate) {
            this.delegate = delegate;
        }
        
        public Collection allInstances() {
            // this shall remove duplicates??
            Set s = new HashSet(delegate.allInstances());
            return s;
        }
        
        public Set allClasses() {
            return delegate.allClasses(); // close enough
        }
        
        public Collection allItems() {
            // remove duplicates..
            Set s = new HashSet(delegate.allItems());
            Iterator it = s.iterator();
            Set instances = new HashSet();
            while (it.hasNext()) {
                Lookup.Item i = (Lookup.Item)it.next();
                if (instances.contains(i.getInstance())) {
                    it.remove();
                } else {
                    instances.add(i.getInstance());
                }
            }
            return s;
        }
        
        public void addLookupListener(LookupListener l) {
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    if (lastResults == null) {
                        lastResults = allInstances();
                    }
                    delegate.addLookupListener(this);
                }
                listeners.add(l);
            }
        }
        
        public void removeLookupListener(LookupListener l) {
            synchronized (listeners) {
                listeners.remove(l);
                if (listeners.isEmpty()) {
                    delegate.removeLookupListener(this);
                    lastResults = null;
                }
            }
        }
        
        public void resultChanged(LookupEvent ev) {
            synchronized (listeners) {
                Collection current = allInstances();
                boolean equal = lastResults != null && current != null && current.containsAll(lastResults) && lastResults.containsAll(current);
                if (equal) {
                    // the merged list is the same, ignore...
                    return ;
                }
                lastResults = current;
            }
                
            LookupEvent ev2 = new LookupEvent(this);
            LookupListener[] ls;
            synchronized (listeners) {
                ls = (LookupListener[])listeners.toArray(new LookupListener[listeners.size()]);
            }
            for (int i = 0; i < ls.length; i++) {
                ls[i].resultChanged(ev2);
            }
        }
        
    }
    
    private static class MyProxyLookup extends ProxyLookup {
        private Lookup initialLookup;
        public MyProxyLookup(Lookup initial) {
            super(new Lookup[] {initial});
            initialLookup = initial;
        }

        public void setElementLookup(Lookup look) {
            setLookups(new Lookup[] {initialLookup, look});
        }
    }
    
    static class InitialProxyLookup extends ProxyLookup {
        private ActionMap initObject;
        public InitialProxyLookup(ActionMap obj) {
            super(new Lookup[] {Lookups.fixed(new Object[] {new LookupProxyActionMap(obj)})});
            initObject = obj;
        }

        public void refreshLookup() {
            setLookups(new Lookup[] {Lookups.fixed(new Object[] {new LookupProxyActionMap(initObject)})});
        }
        
    }
    
    /**
     * A proxy ActionMap that delegates to the original one, used because of #47991
     * non private because of tests..
     */
    static class LookupProxyActionMap extends ActionMap  {
        private ActionMap map;
        public LookupProxyActionMap(ActionMap original) {
            map = original;
        }
        
        @Override
        public void setParent(ActionMap map) {
            this.map.setParent(map);
        }
        
        
        @Override
        public ActionMap getParent() {
            return map.getParent();
        }
        
        @Override
        public void put(Object key, Action action) {
            map.put(key, action);
        }
        
        @Override
        public Action get(Object key) {
            return map.get(key);
        }
        
        @Override
        public void remove(Object key) {
            map.remove(key);
        }
        
        @Override
        public void clear() {
            map.clear();
        }
        
        @Override
        public Object[] keys() {
            return map.keys();
        }
        
        @Override
        public int size() {
            return map.size();
        }
        
        @Override
        public Object[] allKeys() {
            return map.allKeys();
        }
        
    }
}
