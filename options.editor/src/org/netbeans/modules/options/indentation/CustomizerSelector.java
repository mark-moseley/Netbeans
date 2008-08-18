/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.options.indentation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author vita
 */
public final class CustomizerSelector {

    public static final String PROP_MIMETYPE = "CustomizerSelector.PROP_MIMETYPE"; //NOI18N
    public static final String PROP_CUSTOMIZER = "CustomizerSelector.PROP_CUSTOMIZER"; //NOI18N

    public CustomizerSelector(PreferencesFactory pf, boolean acceptOldControllers) {
        this.pf = pf;
        this.acceptOldControllers = acceptOldControllers;
    }
    
    public synchronized String getSelectedMimeType() {
        return selectedMimeType;
    }

    public synchronized void setSelectedMimeType(String mimeType) {
        assert getMimeTypes().contains(mimeType);

        if (selectedMimeType == null || !selectedMimeType.equals(mimeType)) {
            String old = selectedMimeType;
            selectedMimeType = mimeType;
            pcs.firePropertyChange(PROP_MIMETYPE, old, mimeType);

            selectedCustomizerId = null;
        }
    }

    public synchronized PreferencesCustomizer getSelectedCustomizer() {
        if (selectedCustomizerId != null) {
            for(PreferencesCustomizer c : getCustomizersFor(selectedMimeType)) {
                if (selectedCustomizerId.equals(c.getId())) {
                    return c;
                }
            }
        }
        return null;
    }

    public synchronized void setSelectedCustomizer(String id) {
        if (selectedCustomizerId == null || !selectedCustomizerId.equals(id)) {
            String old = selectedCustomizerId;
            selectedCustomizerId = id;
            pcs.firePropertyChange(PROP_CUSTOMIZER, old, id);
        }
    }

    public synchronized Preferences getCustomizerPreferences(PreferencesCustomizer c) {
        Preferences prefs = c2p.get(c);
        assert prefs != null;
        return prefs;
    }

    public synchronized Collection<? extends String> getMimeTypes() {
        if (mimeTypes == null) {
            mimeTypes = new HashSet<String>();
            mimeTypes.add(""); //NOI18N

            // filter out mime types that don't supply customizers
            for(String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
                Lookup l = Lookups.forPath(FOLDER + mimeType);
                Collection<? extends PreferencesCustomizer.Factory> factories = l.lookupAll(PreferencesCustomizer.Factory.class);
                if (!factories.isEmpty()) {
                    mimeTypes.add(mimeType);
                } else if (acceptOldControllers) {
                    Collection<? extends OptionsPanelController> controllers = l.lookupAll(OptionsPanelController.class);
                    if (!controllers.isEmpty()) {
                        mimeTypes.add(mimeType);
                    }
                }
            }
        }
        return mimeTypes;
    }

    public synchronized List<? extends PreferencesCustomizer> getCustomizers(String mimeType) {
        return getCustomizersFor(mimeType);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public interface PreferencesFactory {
        Preferences getPreferences(String mimeType);
        boolean isKeyOverridenForMimeType(String key, String mimeType);
    }

    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------

    private static final String FOLDER = "OptionsDialog/Editor/Formatting/"; //NOI18N
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final PreferencesFactory pf;
    private final boolean acceptOldControllers;

    private String selectedMimeType;
    private String selectedCustomizerId;

    private Set<String> mimeTypes = null;
    private final Map<String, List<? extends PreferencesCustomizer>> allCustomizers = new HashMap<String, List<? extends PreferencesCustomizer>>();
    private final Map<PreferencesCustomizer, Preferences> c2p = new HashMap<PreferencesCustomizer, Preferences>();
    
    private List<? extends PreferencesCustomizer> getCustomizersFor(String mimeType) {
        List<? extends PreferencesCustomizer> list = allCustomizers.get(mimeType);
        if (list == null) {
            list = loadCustomizers(mimeType);
            allCustomizers.put(mimeType, list);
        }
        return list;
    }
    
    private List<? extends PreferencesCustomizer> loadCustomizers(String mimeType) {
        ArrayList<PreferencesCustomizer> list = new ArrayList<PreferencesCustomizer>();
        
        if (mimeType.length() > 0) {
            Lookup l = Lookups.forPath(FOLDER + mimeType);
            Preferences prefs = pf.getPreferences(mimeType);
            
            // collect factories
            Collection<? extends PreferencesCustomizer.Factory> factories = l.lookupAll(PreferencesCustomizer.Factory.class);
            for(PreferencesCustomizer.Factory f : factories) {
                PreferencesCustomizer c = f.create(prefs);
                
                if (c.getId().equals("tabs-and-indents")) { //NOI18N
                    Preferences allLangPrefs = pf.getPreferences(""); //NOI18N
                    c = new IndentationPanelController(MimePath.parse(mimeType), pf, prefs, allLangPrefs, c);
                }
                
                list.add(c);
                c2p.put(c, prefs);
            }
            
            // if permitted, collect old controllers
            if (acceptOldControllers) {
                Collection<? extends OptionsPanelController> controllers = l.lookupAll(OptionsPanelController.class);
                for(OptionsPanelController controller : controllers) {
                    PreferencesCustomizer c = controller instanceof PreviewProvider ?
                        new WrapperCustomizerWithPreview(controller) :
                        new WrapperCustomizer(controller);

                    list.add(c);
                    c2p.put(c, prefs);
                }
            }
        } else {
            Preferences prefs = pf.getPreferences(mimeType);
            PreferencesCustomizer c = new IndentationPanelController(prefs);
            list.add(c);
            c2p.put(c, prefs);
        }

        return list;
    }

    // this is here only to support C/C++ panels, because they don't use PreferencesCustomizer.Factory
    // Instead they save formatting settings to their own module storage (NbPreferences). They
    // also use subnodes for formatting profiles, which is not supported by the Preferences
    // implementation in MimeLookup.

    /* package */ static class WrapperCustomizer implements PreferencesCustomizer {

        private final OptionsPanelController controller;
        private JComponent component;

        public WrapperCustomizer(OptionsPanelController controller) {
            this.controller = controller;
        }

        public String getId() {
            return controller.getClass() + "@" + Integer.toHexString(System.identityHashCode(controller)); //NOI18N
        }

        public String getDisplayName() {
            return getComponent().getName();
        }

        public HelpCtx getHelpCtx() {
            return controller.getHelpCtx();
        }

        public JComponent getComponent() {
            if (component == null) {
                component = controller.getComponent(Lookup.EMPTY);
                controller.update();
            }
            return component;
        }

        public void applyChanges() {
            controller.applyChanges();
        }

        public void cancel() {
            controller.cancel();
        }
    } // End of WrapperCustomizer class

    private static final class WrapperCustomizerWithPreview extends WrapperCustomizer implements PreviewProvider {

        private final PreviewProvider provider;
        
        public WrapperCustomizerWithPreview(OptionsPanelController controller) {
            super(controller);
            this.provider = (PreviewProvider) controller;
        }

        public JComponent getPreviewComponent() {
            return provider.getPreviewComponent();
        }

        public void refreshPreview() {
            provider.refreshPreview();
        }

    } // End of WrapperCustomizerWithPreview class
}
