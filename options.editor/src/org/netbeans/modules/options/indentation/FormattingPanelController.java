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
package org.netbeans.modules.options.indentation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * This is used in Tools-Options, but not in project properties customizer.
 * 
 * @author Dusan Balek
 */
public final class FormattingPanelController extends OptionsPanelController implements CustomizerSelector.PreferencesFactory, PreferenceChangeListener, NodeChangeListener {

    // ------------------------------------------------------------------------
    // OptionsPanelController implementation
    // ------------------------------------------------------------------------

    public FormattingPanelController() {
        this.selector = new CustomizerSelector(this);
    }

    public void update() {
        cancel();
    }
    
    public synchronized void applyChanges() {
        for(String mimeType : mimeTypePreferences.keySet()) {
            ProxyPreferences pp = mimeTypePreferences.get(mimeType);
            try {
                pp.flush();
            } catch (BackingStoreException ex) {
                LOG.log(Level.WARNING, "Can't flush preferences for '" + mimeType + "'", ex); //NOI18N
            }
        }
        
        notifyChanged(false);
    }
    
    public synchronized void cancel() {
        // destroy all proxy preferences
        for(String mimeType : mimeTypePreferences.keySet()) {
            ProxyPreferences pp = mimeTypePreferences.get(mimeType);
            pp.removeNodeChangeListener(weakNodeL);
            pp.removePreferenceChangeListener(weakPrefL);
            pp.destroy();
        }

        // reset the cache
        mimeTypePreferences.clear();
        
        notifyChanged(false);
    }
    
    public boolean isValid() {
        return true;
    }
    
    public synchronized boolean isChanged() {
        return changed;
    }
    
    public HelpCtx getHelpCtx() {
        PreferencesCustomizer c = selector.getSelectedCustomizer();
        HelpCtx ctx = c == null ? null : c.getHelpCtx();
	return ctx != null ? ctx : new HelpCtx("netbeans.optionsDialog.editor.formatting"); //NOI18N
    }
    
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            panel = new FormattingPanel(selector);
        }
        return panel;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }

    // ------------------------------------------------------------------------
    // CustomizerSelector.PreferencesFactory implementation
    // ------------------------------------------------------------------------

    public synchronized Preferences getPreferences(String mimeType) {
        ProxyPreferences pp = mimeTypePreferences.get(mimeType);
        if (pp == null) {
            Preferences p = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            pp = ProxyPreferences.get(p);
            pp.addPreferenceChangeListener(weakPrefL);
            pp.addNodeChangeListener(weakNodeL);
            mimeTypePreferences.put(mimeType, pp);
        }
        return pp;
    }

    // ------------------------------------------------------------------------
    // PreferenceChangeListener implementation
    // ------------------------------------------------------------------------

    public void preferenceChange(PreferenceChangeEvent evt) {
        notifyChanged(true);
    }

    // ------------------------------------------------------------------------
    // NodeChangeListener implementation
    // ------------------------------------------------------------------------

    public void childAdded(NodeChangeEvent evt) {
        notifyChanged(true);
    }

    public void childRemoved(NodeChangeEvent evt) {
        notifyChanged(true);
    }

    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FormattingPanelController.class.getName());
    
    private final CustomizerSelector selector;
    private final Map<String, ProxyPreferences> mimeTypePreferences = new HashMap<String, ProxyPreferences>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final PreferenceChangeListener weakPrefL = WeakListeners.create(PreferenceChangeListener.class, this, null);
    private final NodeChangeListener weakNodeL = WeakListeners.create(NodeChangeListener.class, this, null);

    private FormattingPanel panel;
    private boolean changed = false;

    private void notifyChanged(boolean changed) {
        if (this.changed != changed) {
            this.changed = changed;
            pcs.firePropertyChange(PROP_CHANGED, !changed, changed);
        }
    }
}
