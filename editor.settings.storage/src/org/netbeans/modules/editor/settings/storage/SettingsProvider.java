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

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.codetemplates.CodeTemplateSettingsImpl;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 *  @author Jan Jancura
 */
public final class SettingsProvider implements MimeDataProvider {

    private static final Logger LOG = Logger.getLogger(SettingsProvider.class.getName());
    
    private final Map<MimePath, WeakReference<Lookup>> cache = new WeakHashMap<MimePath, WeakReference<Lookup>>();
    
    public SettingsProvider () {
    }
    
    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    public Lookup getLookup(MimePath mimePath) {
        if (mimePath.size() > 0 && mimePath.getMimeType(0).contains(EditorSettingsImpl.TEXT_BASE_MIME_TYPE)) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Won't provide any settings for " + EditorSettingsImpl.TEXT_BASE_MIME_TYPE + //NOI18N
                    " It's been deprecated, use MimePath.EMPTY instead."); //, new Throwable("Stacktrace") //NOI18N
            }
            return null;
        }
        
        synchronized (cache) {
            WeakReference<Lookup> ref = cache.get(mimePath);
            Lookup lookup = ref == null ? null : ref.get();
            
            if (lookup == null) {
                String path = mimePath.getPath();
                if (path.startsWith("test")) { //NOI18N
                    int idx = path.indexOf('_'); //NOI18N
                    if (idx == -1) {
                        throw new IllegalStateException("Invalid mimePath: " + path); //NOI18N
                    }
                    
                    // Get the special test profile name and the real mime path
                    String profile = path.substring(0, idx);
                    MimePath realMimePath = MimePath.parse(path.substring(idx + 1));
                    
                    lookup = new ProxyLookup(new Lookup [] {
                        new MyLookup(realMimePath, profile),
                        Lookups.exclude(
                            MimeLookup.getLookup(realMimePath),
                            new Class [] {
                                FontColorSettings.class,
                                KeyBindingSettings.class
                            })
                    });
                } else {
                    lookup = new MyLookup(mimePath, null);
                }
                
                cache.put(mimePath, new WeakReference<Lookup>(lookup));
            }
            
            return lookup;
        }
    }
    
    private static final class MyLookup extends AbstractLookup implements PropertyChangeListener {
        
        private final MimePath mimePath;
        private final boolean specialFcsProfile;
        private String fcsProfile;
        
        private final InstanceContent ic;
        private CompositeFCS fontColorSettings = null;
        private Object keyBindingSettings = null;
        private Object codeTemplateSettings = null;
        
        private KeyBindingSettingsImpl kbsi;
        private CodeTemplateSettingsImpl ctsi;
        
        public MyLookup(MimePath mimePath, String profile) {
            this(mimePath, profile, new InstanceContent());
        }
        
        private MyLookup(MimePath mimePath, String profile, InstanceContent ic) {
            super(ic);

            this.mimePath = mimePath;
            
            if (profile == null) {
                // Use the selected current profile
                String currentProfile = EditorSettings.getDefault().getCurrentFontColorProfile();
                this.fcsProfile = EditorSettingsImpl.getInstance().getInternalFontColorProfile(currentProfile);
                this.specialFcsProfile = false;
            } else {
                // This is the special test profile derived from the mime path.
                // It will never change.
                this.fcsProfile = profile;
                this.specialFcsProfile = true;
            }
            
            this.ic = ic;
            
            // Start listening
            EditorSettings es = EditorSettings.getDefault();
            es.addPropertyChangeListener(WeakListeners.propertyChange(this, es));
            
            this.kbsi = KeyBindingSettingsImpl.get(mimePath);
            this.kbsi.addPropertyChangeListener(WeakListeners.propertyChange(this, this.kbsi));

            this.ctsi = CodeTemplateSettingsImpl.get(mimePath);
            this.ctsi.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ctsi));
        }

        @Override
        protected void initialize() {
            synchronized (this) {
                fontColorSettings = new CompositeFCS(mimePath, fcsProfile);
                keyBindingSettings = this.kbsi.createInstanceForLookup();
                codeTemplateSettings = this.ctsi.createInstanceForLookup();
                
                ic.set(Arrays.asList(new Object [] {
                    fontColorSettings,
                    keyBindingSettings,
                    codeTemplateSettings
                }), null);
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (this) {
                boolean fcsChanged = false;
                boolean kbsChanged = false;
                boolean ctsChanged = false;

//                if (mimePath.getPath().contains("xml")) {
//                    System.out.println("@@@ propertyChange: mimePath = " + mimePath.getPath() + " profile = " + fcsProfile + " property = " + evt.getPropertyName() + " oldValue = " + (evt.getOldValue() instanceof MimePath ? ((MimePath) evt.getOldValue()).getPath() : evt.getOldValue()) + " newValue = " + evt.getNewValue());
//                }
                
                // Determine what has changed
                if (this.kbsi == evt.getSource()) {
                    kbsChanged = true;
                    
                } else if (this.ctsi == evt.getSource() || 
                           CodeTemplateSettingsImpl.PROP_EXPANSION_KEY.equals(evt.getPropertyName())
                ) {
                    ctsChanged = true;
                    
                } else if (evt.getPropertyName() == null) {
                    // reset all
                    if (!specialFcsProfile) {
                        String currentProfile = EditorSettings.getDefault().getCurrentFontColorProfile();
                        fcsProfile = EditorSettingsImpl.getInstance().getInternalFontColorProfile(currentProfile);
                    }
                    fcsChanged = true;
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_HIGHLIGHT_COLORINGS)) {
                    String changedProfile = (String) evt.getNewValue();
                    if (changedProfile.equals(fcsProfile)) {
                        fcsChanged = true;
                    }
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_TOKEN_COLORINGS)) {
                    String changedProfile = (String) evt.getNewValue();
                    if (changedProfile.equals(fcsProfile)) {
                        MimePath changedMimePath = (MimePath) evt.getOldValue();
                        if (fontColorSettings != null && fontColorSettings.isDerivedFromMimePath(changedMimePath)) {
                            fcsChanged = true;
                        }
                    }
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_CURRENT_FONT_COLOR_PROFILE)) {
                    if (!specialFcsProfile) {
                        String newProfile = (String) evt.getNewValue();
                        fcsProfile = EditorSettingsImpl.getInstance().getInternalFontColorProfile(newProfile);
                        fcsChanged = true;
                    }
                }
                
                // Update lookup contents
                boolean updateContents = false;
                
                if (fcsChanged && fontColorSettings != null) {
                    fontColorSettings = new CompositeFCS(mimePath, fcsProfile);
                    updateContents = true;
                }
                
                if (kbsChanged  && keyBindingSettings != null) {
                    keyBindingSettings = this.kbsi.createInstanceForLookup();
                    updateContents = true;
                }
                
                if (ctsChanged  && codeTemplateSettings != null) {
                    codeTemplateSettings = this.ctsi.createInstanceForLookup();
                    updateContents = true;
                }
                
                if (updateContents) {
                    ic.set(Arrays.asList(new Object [] {
                        fontColorSettings,
                        keyBindingSettings,
                        codeTemplateSettings
                    }), null);
                }
            }
        }

    } // End of MyLookup class
}
