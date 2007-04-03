/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.keymap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.Action;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.ErrorManager;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.NbBundle;

/**
 * Bridge to old layers based system.
 *
 * @author Jan Jancura
 */
public class LayersBridge extends KeymapManager {
    
    static final String         KEYMAPS_FOLDER = "Keymaps";
    private static final String SHORTCUTS_FOLDER = "Shortcuts";
    private static final String TOOLBARS_FOLDER = "Toolbars";
    
    private static final String LAYERS_BRIDGE = "LayersBridge";
    
    /** Map (GlobalAction > DataObject). */
    private Map actionToDataObject = new HashMap ();
    /** Map (String (folderName) > Set (GlobalAction)). */
    private Map categoryToActions;
    /** Set (GlobalAction). */
    private Set actions = new HashSet ();
    
    public LayersBridge() {
        super(LAYERS_BRIDGE);
    }
    
    /**
     * Returns Map (String (folderName) > Set (GlobalAction)).
     */
    public Map getActions () {
        if (categoryToActions == null) {
            categoryToActions = new HashMap ();
            initActions ("OptionsDialog/Actions", null);               // NOI18N
            initActions (
                "Actions", 
                NbBundle.getMessage (LayersBridge.class, "CTL_Other")
            );
            categoryToActions.remove ("Hidden");                       // NOI18N
            categoryToActions = Collections.unmodifiableMap (categoryToActions);
        }
        return categoryToActions;
    }

    private void initActions (String folder, String category) {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject fo = fs.findResource (folder);
        if (fo == null) return;
        DataFolder root = DataFolder.findFolder (fo);
        Enumeration en = root.children ();
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            if (dataObject instanceof DataFolder)
                initActions ((DataFolder) dataObject, null, category);
        }
    }
    
    private void initActions (
        DataFolder folder, 
        String folderName, 
        String category
    ) {
        
        // 1) reslove name
        String name = folder.getName ();
        if (category != null)
            name = category;
        else {
            String bundleName = (String) folder.getPrimaryFile ().getAttribute 
                ("SystemFileSystem.localizingBundle");
            if (bundleName != null)
                try {
                    name = NbBundle.getBundle (bundleName).getString (
                        folder.getPrimaryFile ().getPath ()
                    );
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            if (folderName != null) 
                name = folderName + '/' + name;
        }
        
        Enumeration en = folder.children ();
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            if (dataObject instanceof DataFolder) {
                initActions ((DataFolder) dataObject, name, category);
                continue;
            }
            GlobalAction action = createAction (dataObject);
            if (actions.contains (action)) continue;
            if (action == null) continue;
            actions.add (action);
            
            // add to actions (Map (String (folderName) > Set (GlobalAction))).
            Set a = (Set) categoryToActions.get (name);
            if (a == null) {
                a = new HashSet ();
                categoryToActions.put (name, a);
            }
            a.add (action);
            
            while (dataObject instanceof DataShadow)
                dataObject = ((DataShadow) dataObject).getOriginal ();
            
            actionToDataObject.put (action, dataObject);
        }
    }
    
    private List keymapNames;
    
   public List getProfiles () {
        if (keymapNames == null) {
            DataFolder root = getRootFolder (KEYMAPS_FOLDER, null);
            Enumeration en = root.children (false);
            keymapNames = new ArrayList ();
            while (en.hasMoreElements ()) {
                DataObject dataObject = (DataObject) en.nextElement ();
                if (!(dataObject instanceof DataFolder)) continue;
                keymapNames.add (dataObject.getName ());
            }
            if (!keymapNames.contains ("NetBeans"))
                keymapNames.add ("NetBeans");
        }
        return Collections.unmodifiableList (keymapNames);
    }
    
    /** Map (String (profile) > Map (GlobalAction > Set (String (shortcut)))). */
    private Map keymaps = new HashMap ();
    
    /**
     * Returns Map (GlobalAction > Set (String (shortcut))).
     */
    public Map getKeymap (String profile) {
        if (!keymaps.containsKey (profile)) {
            DataFolder root = getRootFolder (SHORTCUTS_FOLDER, null);
            Map m = readKeymap (root);
            root = getRootFolder (KEYMAPS_FOLDER, profile);
            m.putAll (readKeymap (root));
            keymaps.put (profile, m);
        }
        return Collections.unmodifiableMap ((Map) keymaps.get (profile));
    }
    
    /** Map (String (profile) > Map (GlobalAction > Set (String (shortcut)))). */
    private Map keymapDefaults = new HashMap ();
    
    /**
     * Returns Map (GlobalAction > Set (String (shortcut))).
     */
    Map getKeymapDefaults (String profile) {
        if (!keymapDefaults.containsKey (profile)) {
            DataFolder root = getRootFolder (SHORTCUTS_FOLDER, null);
            Map m = readKeymap (root);
            root = getRootFolder (KEYMAPS_FOLDER, profile);
            m.putAll (readKeymap (root));
            keymapDefaults.put (profile, m);
        }
        return Collections.unmodifiableMap ((Map) keymapDefaults.get (profile));
    }
    
    DataObject getDataObject (Object action) {
        return (DataObject) actionToDataObject.get (action);
    }
    
    /**
     * Read keymap from one folder Map (GlobalAction > Set (String (shortcut))).
     */
    private Map readKeymap (DataFolder root) {
        Map keymap = new HashMap ();
        if (root == null) return keymap;
        Enumeration en = root.children (false);
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            if (dataObject instanceof DataFolder) continue;
            GlobalAction action = createAction (dataObject);
            if (action == null) continue;
            String shortcut = dataObject.getName ();
            Set s = (Set) keymap.get (action);
            if (s == null) {
                s = new HashSet ();
                keymap.put (action, s);
            }
            s.add (shortcut);
        }
        return keymap;
    }

    public void deleteProfile (String profile) {
        FileObject root = Repository.getDefault ().
            getDefaultFileSystem ().getRoot ();
        root = root.getFileObject (KEYMAPS_FOLDER);
        if (root == null) return;
        root = root.getFileObject (profile);
        if (root == null) return;
        try {
            root.delete ();
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    // actionToShortcuts Map (GlobalAction > Set (String (shortcut))
    public void saveKeymap (String profile, Map actionToShortcuts) {
        // discard our cached copy first
        keymaps.remove(profile);
        
        // 1) get / create Keymaps/Profile folder
        DataFolder folder = getRootFolder (KEYMAPS_FOLDER, profile);
        if (folder == null) {
            folder = getRootFolder (KEYMAPS_FOLDER, null);
            try {
                folder = DataFolder.create (folder, profile);
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
                return;
            }
        }
        saveKeymap (folder, actionToShortcuts, true);
        
        folder = getRootFolder (SHORTCUTS_FOLDER, null);
        saveKeymap (folder, actionToShortcuts, false);
    }
    
    private void saveKeymap (DataFolder folder, Map actionToShortcuts, boolean add) {
        // hack: initialize the actions map first
  	getActions();
        // 2) convert to: Map (String (shortcut AC-C X) > GlobalAction)
        Map shortcutToAction = shortcutToAction (actionToShortcuts);
        
        // 3) delete obsolete DataObjects
        Enumeration en = folder.children ();
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            GlobalAction a1 = (GlobalAction) shortcutToAction.get (dataObject.getName ());
            if (a1 != null) {
                GlobalAction action = createAction (dataObject);
                if (action == null) continue;
                if (action.equals (a1)) {
                    // shortcut already saved
                    shortcutToAction.remove (dataObject.getName ());
                    continue;
                }
            }
            // obsolete shortcut
            try {
                dataObject.delete ();
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        
        // 4) add new shortcuts
        if (!add) return;
        Iterator it = shortcutToAction.keySet ().iterator ();
        while (it.hasNext ()) {
            String shortcut = (String) it.next ();
            GlobalAction action = (GlobalAction) shortcutToAction.get (shortcut);
            DataObject dataObject = (DataObject) actionToDataObject.get (action);
            if (dataObject == null) {
                 if (System.getProperty ("org.netbeans.optionsDialog") != null)
                     System.out.println ("No original DataObject specified! Not possible to create shadow1. " + action);
                 continue;
            }
            try {
                DataShadow.create (folder, shortcut, dataObject);
            } catch (IOException ex) {
                ex.printStackTrace ();
                continue;
            }
        }
    }    

    private static DataFolder getRootFolder (String name1, String name2) {
        FileObject root = Repository.getDefault ().
            getDefaultFileSystem ().getRoot ();
        FileObject fo1 = root.getFileObject (name1);
        try {
            if (fo1 == null) root.createFolder (name1);
            if (fo1 == null) return null;
            if (name2 == null) return DataFolder.findFolder (fo1);
            FileObject fo2 = fo1.getFileObject (name2);
            if (fo2 == null) fo1.createFolder (name2);
            if (fo2 == null) return null;
            return DataFolder.findFolder (fo2);
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
            return null;
        }
    }

    /**
     * Returns instance of GlobalAction encapsulating action, or null.
     */
    private GlobalAction createAction (DataObject dataObject) {
        InstanceCookie ic = (InstanceCookie) dataObject.getCookie 
            (InstanceCookie.class);
        if (ic == null) return null;
        try {
            Object action = ic.instanceCreate ();
            if (action == null) return null;
            if (!(action instanceof Action)) return null;
            return new GlobalAction ((Action) action);
        } catch (Exception ex) {
            ex.printStackTrace ();
            return null;
        }
    }
    
    /**
     * converts: actionToShortcuts: Map (ShortcutAction > Set (String (shortcut AC-C X)))
     * to: Map (String (shortcut AC-C X) > GlobalAction).
     * removes all non GlobalAction actions.
     */
    static Map shortcutToAction (Map actionToShortcuts) {
        Map shortcutToAction = new HashMap ();
        Iterator it = actionToShortcuts.keySet ().iterator ();
        while (it.hasNext ()) {
            ShortcutAction action = (ShortcutAction) it.next ();
            Set shortcuts = (Set) actionToShortcuts.get (action);
            action = action.getKeymapManagerInstance(LAYERS_BRIDGE);
            if (!(action instanceof GlobalAction)) continue;
            Iterator it2 = shortcuts.iterator ();
            while (it2.hasNext ()) {
                String multiShortcut = (String) it2.next ();
                shortcutToAction.put (multiShortcut, action);
            }
        }
        return shortcutToAction;
    }
    
    private static String[] toArray (String multiShortcut) {
        StringTokenizer st = new StringTokenizer (multiShortcut, " ");
        List result = new ArrayList ();
        while (st.hasMoreTokens ()) {
            String shortcut = st.nextToken ();
            if (shortcut == null) {
                if (System.getProperty ("org.netbeans.optionsDialog") != null)
                    System.out.println ("can not parse shortcut: " + multiShortcut);
                continue;
            }
            result.add (shortcut);
        }
        return (String[]) result.toArray (new String [result.size ()]);
    }

    public void refreshActions() {
    }

    public String getCurrentProfile() {
        return null;
    }

    public void setCurrentProfile(String profileName) {
    }

    public boolean isCustomProfile(String profileName) {
        // TODO:
        return false;
    }
    
    
    private static class GlobalAction implements ShortcutAction {
        private Action action;
        private String name;
        private String id;
        
        private GlobalAction (Action a) {
            action = a;
        }
        
        public String getDisplayName () {
            if (name == null) {
                name = (String) action.getValue (Action.NAME);
                if (name == null) name = action.toString ();
                name = name.replaceAll ("&", "").trim ();
            }
            return name;
        }
        
        public String getId () {
            if (id == null)
                id = action.getClass ().getName ();
            return id;
        }
        
        public String getDelegatingActionId () {
            return null;
        }
        
        public boolean equals (Object o) {
            if (!(o instanceof GlobalAction)) return false;
            return ((GlobalAction) o).action.equals (action);
        }
        
        public int hashCode () {
            return action.hashCode ();
        }
        
        public String toString () {
            return "GlobalAction[" + getDisplayName()+ ":" + id + "]";
        }
    
        public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
            if (LAYERS_BRIDGE.equals(keymapManagerName)) {
                return this;
            }
            return null;
        }
}
}
