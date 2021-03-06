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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.lib.editor.codetemplates.storage;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.api.editor.settings.CodeTemplateSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.openide.util.Lookup;

/**
 *
 * @author Vita Stejskal
 */
public final class CodeTemplateSettingsImpl {

    private static final Logger LOG = Logger.getLogger(CodeTemplateSettingsImpl.class.getName());
    
    public static final String PROP_CODE_TEMPLATES = "CodeTemplateSettingsImpl.PROP_CODE_TEMPLATES"; //NOI18N
    public static final String PROP_EXPANSION_KEY = "CodeTemplateSettingsImpl.PROP_EXPANSION_KEY"; //NOI18N
    
    public static synchronized CodeTemplateSettingsImpl get(MimePath mimePath) {
        WeakReference<CodeTemplateSettingsImpl> reference = INSTANCES.get(mimePath);
        CodeTemplateSettingsImpl result = reference == null ? null : reference.get();
        
        if (result == null) {
            result = new CodeTemplateSettingsImpl(mimePath);
            INSTANCES.put(mimePath, new WeakReference<CodeTemplateSettingsImpl>(result));
        }
        
        return result;
    }
    
    public Map<String, CodeTemplateDescription> getCodeTemplates() {
        EditorSettingsStorage<String, CodeTemplateDescription> ess = EditorSettingsStorage.<String, CodeTemplateDescription>get(CodeTemplatesStorage.ID);
        try {
            return ess.load(mimePath, null, false);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return Collections.<String, CodeTemplateDescription>emptyMap();
        }
    }

    public void setCodeTemplates(Map<String, CodeTemplateDescription> map) {
        EditorSettingsStorage<String, CodeTemplateDescription> ess = EditorSettingsStorage.<String, CodeTemplateDescription>get(CodeTemplatesStorage.ID);
        
        try {
            if (map == null) {
                ess.delete(mimePath, null, false);
            } else {
                ess.save(mimePath, null, false, map);
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
        
        pcs.firePropertyChange(PROP_CODE_TEMPLATES, null, null);
    }

    public KeyStroke getExpandKey() {
        // XXX: use SimpleValueSettings or whatever other appropriate way
        return BaseOptions_getCodeTemplateExpandKey();
    }

    public void setExpandKey(KeyStroke expansionKey) {
        // XXX: use SimpleValueSettings or whatever other appropriate way
        BaseOptions_setCodeTemplateExpandKey(expansionKey);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // ignore
        }
        
        // notify all lookups for all mime types that the expansion key has changed
        List<CodeTemplateSettingsImpl> all = new ArrayList<CodeTemplateSettingsImpl>();
        synchronized (CodeTemplateSettingsImpl.class) {
            for(Reference<CodeTemplateSettingsImpl> r : INSTANCES.values()) {
                CodeTemplateSettingsImpl ctsi = r.get();
                if (ctsi != null) {
                    all.add(ctsi);
                }
            }
        }
        
        for(CodeTemplateSettingsImpl ctsi : all) {
            ctsi.pcs.firePropertyChange(PROP_EXPANSION_KEY, null, null);
        }
    }

    public Object createInstanceForLookup() {
        Map<String, CodeTemplateDescription> map = getCodeTemplates();
        return new Immutable(
            Collections.unmodifiableList(new ArrayList<CodeTemplateDescription>(map.values())), 
            getExpandKey()
        );
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    // ---------------------------------------------
    // Private implementation
    // ---------------------------------------------

    private static final KeyStroke DEFAULT_EXPANSION_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);

    private static final Map<MimePath, WeakReference<CodeTemplateSettingsImpl>> INSTANCES =
        new WeakHashMap<MimePath, WeakReference<CodeTemplateSettingsImpl>>();
    
//    private static final CodeTemplateDescriptionComparator CTC = new CodeTemplateDescriptionComparator();
    
    private final MimePath mimePath;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private CodeTemplateSettingsImpl(MimePath mimePath) {
        this.mimePath = mimePath;
    }

    private static KeyStroke BaseOptions_getCodeTemplateExpandKey() {
        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            Class clazz = cl.loadClass("org.netbeans.modules.editor.options.BaseOptions"); //NOI18N
            Method m = clazz.getDeclaredMethod("getCodeTemplateExpandKey"); //NOI18N
            return (KeyStroke) m.invoke(null);
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
            return DEFAULT_EXPANSION_KEY;
        }
    }

    private static void BaseOptions_setCodeTemplateExpandKey(KeyStroke keyStroke) {
        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            Class clazz = cl.loadClass("org.netbeans.modules.editor.options.BaseOptions"); //NOI18N
            Method m = clazz.getDeclaredMethod("setCodeTemplateExpandKey", KeyStroke.class); //NOI18N
            m.invoke(null, keyStroke);
        } catch (Exception e) {
            // ignore
        }
    }
    
    private static final class Immutable extends CodeTemplateSettings {
        
        private final List<CodeTemplateDescription> codeTemplates;
        private final KeyStroke expansionKey;
        
        public Immutable(List<CodeTemplateDescription> codeTemplates, KeyStroke expansionKey) {
            this.codeTemplates = codeTemplates;
            this.expansionKey = expansionKey;
        }
        
        public List<CodeTemplateDescription> getCodeTemplateDescriptions() {
            return codeTemplates;
        }

        public KeyStroke getExpandKey() {
            return expansionKey;
        }
    } // End of Immutable class
    
//    private static final class CodeTemplateDescriptionComparator implements Comparator<CodeTemplateDescription> {
//        public int compare(CodeTemplateDescription t1, CodeTemplateDescription t2) {
//            if (t1.getAbbreviation().equals(t2.getAbbreviation()) &&
//                compareTexts(t1.getDescription(), t2.getDescription()) &&
//                compareTexts(t1.getParametrizedText(), t2.getParametrizedText()) &&
//                Utilities.compareObjects(t1.getContexts(), t2.getContexts())
//            ) {
//                return 0;
//            } else {
//                return -1;
//            }
//        }
//    } //NOI18N
//    
//    private static boolean compareTexts(String t1, String t2) {
//        if (t1 == null || t1.length() == 0) {
//            t1 = null;
//        }
//        if (t2 == null || t2.length() == 0) {
//            t2 = null;
//        }
//        if (t1 != null && t2 != null) {
//            return t1.equals(t2);
//        } else {
//            return t1 == null && t2 == null;
//        }
//    }
}
