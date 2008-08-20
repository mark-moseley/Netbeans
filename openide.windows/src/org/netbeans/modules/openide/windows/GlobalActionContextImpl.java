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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.openide.windows;

import java.awt.EventQueue;
import javax.swing.ActionMap;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/** An interface that can be registered in a lookup by subsystems
 * wish to provide a global context actions should react to.
 *
 * @author Jaroslav Tulach
*/
public final class GlobalActionContextImpl extends Object
implements ContextGlobalProvider, Lookup.Provider, java.beans.PropertyChangeListener {
    /** registry to work with */
    private TopComponent.Registry registry;
    
    public GlobalActionContextImpl () {
        this (TopComponent.getRegistry());
    }
    
    public GlobalActionContextImpl (TopComponent.Registry r) {
        this.registry = r;
    }
    
    /** the lookup to temporarily use */
    private static volatile Lookup temporary;
    /** Temporarily provides different action map in the lookup.
     */
    public static void blickActionMap (final ActionMap map) {
        if (EventQueue.isDispatchThread()) {
            blickActionMapImpl(map);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    blickActionMapImpl(map);
                }
            });
        }
    }

    static void blickActionMapImpl(ActionMap map) {
        assert EventQueue.isDispatchThread();
        Object obj = Lookup.getDefault ().lookup (ContextGlobalProvider.class);
        if (obj instanceof GlobalActionContextImpl) {
            GlobalActionContextImpl g = (GlobalActionContextImpl)obj;
            
            Lookup[] arr = {
                Lookups.singleton (map),
                Lookups.exclude (g.getLookup (), new Class[] { javax.swing.ActionMap.class }),
            };

            ProxyLookup pl = new ProxyLookup (arr);
            try {
                synchronized (g) {
                    while (temporary != null) {
                        try {
                            g.wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    temporary = pl;
                }
                Object q = org.openide.util.Utilities.actionsGlobalContext ().lookup (javax.swing.ActionMap.class);
                assert q == map : "We really get map from the lookup. Map: " + map + " returned: " + q; // NOI18N
            } finally {
                synchronized (g) {
                    assert temporary == pl;
                    temporary = null;
                    g.notifyAll();
                }
                // fire the changes about return of the values back
                org.openide.util.Utilities.actionsGlobalContext ().lookup (javax.swing.ActionMap.class);
            }
        }
    }
    
    /** Let's create the proxy listener that delegates to currently 
     * selected top component.
     */
    public Lookup createGlobalContext() {
        registry.addPropertyChangeListener(this);
        return org.openide.util.lookup.Lookups.proxy(this);
    }
    
    /** The current component lookup */
    public Lookup getLookup() {
        Lookup l = temporary;
        if (l != null) {
            return l;
        }
        
        TopComponent tc = registry.getActivated();
        return tc == null ? Lookup.EMPTY : tc.getLookup();
    }
    
    /** Requests refresh of our lookup everytime component is chagned.
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals (evt.getPropertyName())) {
            org.openide.util.Utilities.actionsGlobalContext ().lookup (javax.swing.ActionMap.class);
        }
    }
    
}
