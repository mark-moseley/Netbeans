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

package org.netbeans.modules.visualweb.designer.jsf;


import org.netbeans.modules.visualweb.spi.designer.Decoration;
import org.netbeans.modules.visualweb.spi.designer.DecorationProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;


/**
 * Manages the <code>DecorationProvider</code>s, and provides
 * decoration services for <code>CssBox</code>.
 *
 * @author Peter Zavadsky
 */
public class DecorationManager {

    /** Name of <code>decorationProviders</code> property. */
    public static final String PROP_DECORATION_PROVIDERS = "decorationProviders"; // NOI18N

    private static final DecorationManager instance = new DecorationManager();


    private final Lookup.Result<DecorationProvider> lookupResult;

    private final LookupListener decorationProvidersListener = new DecorationProvidersListener(this);

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);


    /** Creates a new instance of DecorationManager */
    private DecorationManager() {
        lookupResult = Lookup.getDefault().lookup(new Lookup.Template<DecorationProvider>(DecorationProvider.class));
        lookupResult.addLookupListener(
                (LookupListener)WeakListeners.create(LookupListener.class, decorationProvidersListener, lookupResult));
    }

    public static DecorationManager getDefault() {
        return instance;
    }


    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    public Decoration getDecoration(Element element) {
        DecorationProvider[] decorationProviders = getDecorationProviders();
//        if (decorationProviders.length == 0) {
//            return null;
//        } else {
//            return decorationProviders[0].getDecoration(element);
//        }
        for (DecorationProvider decorationProvider : decorationProviders) {
            Decoration decoration = decorationProvider.getDecoration(element);
            if (decoration != null) {
                return decoration;
            }
        }
        return null;
    }

    private void fireDecorationProvidersPropertyChange() {
        support.firePropertyChange(PROP_DECORATION_PROVIDERS, null, getDecorationProviders());
    }

    public DecorationProvider[] getDecorationProviders() {
        Collection<? extends DecorationProvider> decorationProviders = lookupResult.allInstances();
        return decorationProviders.toArray(new DecorationProvider[decorationProviders.size()]);
    }


    private static class DecorationProvidersListener implements LookupListener {
        private final DecorationManager decorationManager;

        public DecorationProvidersListener(DecorationManager decorationManager) {
            this.decorationManager = decorationManager;
        }

        public void resultChanged(LookupEvent lookupEvent) {
            decorationManager.fireDecorationProvidersPropertyChange();
        }
    } // End of DecorationProvidersListener.
}
