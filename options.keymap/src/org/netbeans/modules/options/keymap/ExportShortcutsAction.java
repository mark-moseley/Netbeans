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


package org.netbeans.modules.options.keymap;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.modules.options.keymap.XMLStorage.Attribs;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class ExportShortcutsAction {
    
    private ExportShortcutsAction() {}
    
    private static Action exportIDEActionsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_IDE_Actions_Action"));}
        
        public void actionPerformed (ActionEvent e) {

            // 1) load all keymaps to allKeyMaps
            LayersBridge layersBridge = new LayersBridge ();
            Map<String, Set<ShortcutAction>> categoryToActions = layersBridge.getActions ();
            Map<String, Map<String, ShortcutAction>> m = resolveNames (categoryToActions);

            generateLayersXML (layersBridge, m);
        }
    };
    
    public static Action getExportIDEActionsAction () {
        return exportIDEActionsAction;
    }
    
    private static Action exportIDEShortcutsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_IDE_Shortcuts_Action"));}
        
        public void actionPerformed (ActionEvent e) {

            // 1) load all keymaps to allKeyMaps
            Map<String, Map<String, ShortcutAction>> allKeyMaps = 
                    new HashMap<String, Map<String, ShortcutAction>> ();
            LayersBridge layersBridge = new LayersBridge ();
            layersBridge.getActions ();
            List keyMaps = layersBridge.getProfiles ();
            Iterator it3 = keyMaps.iterator ();
            while (it3.hasNext ()) {
                String keyMapName = (String) it3.next ();
                Map<ShortcutAction, Set<String>> actionToShortcuts = layersBridge.getKeymap (keyMapName);
                Map<String, ShortcutAction> shortcutToAction = LayersBridge.shortcutToAction (actionToShortcuts);
                allKeyMaps.put (keyMapName, shortcutToAction);
            }

            generateLayersXML (layersBridge, allKeyMaps);
        }
    };
    
    public static Action getExportIDEShortcutsAction () {
        return exportIDEShortcutsAction;
    }
    
    private static Action exportEditorShortcutsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_Editor_Shortcuts_Action"));}
        
        public void actionPerformed (ActionEvent e) {
            KeymapManager editorBridge = null;
            for (KeymapManager km : KeymapModel.getKeymapManagerInstances()) {
                if ("EditorBridge".equals(km.getName())) {
                    editorBridge = km;
                    break;
                }
            }
            if (editorBridge != null) {
                Map<ShortcutAction, Set<String>> actionToShortcuts = 
                        editorBridge.getKeymap(editorBridge.getCurrentProfile ());
                generateEditorXML (actionToShortcuts);
            }
        }
    };
    
    public static Action getExportEditorShortcutsAction () {
        return exportEditorShortcutsAction;
    }
    
    private static Action exportShortcutsToHTMLAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_Shortcuts_to_HTML_Action"));}
        
        public void actionPerformed (ActionEvent e) {
            exportShortcutsToHTML ();
        }
    };
    
    public static Action getExportShortcutsToHTMLAction () {
        return exportShortcutsToHTMLAction;
    }

    
    // helper methods ..........................................................
    
    private static void exportShortcutsToHTML () {
        // read all shortcuts to keymaps
        KeymapModel keymapModel = new KeymapModel ();
        Map<String, Map<ShortcutAction, Set<String>>> keymaps = 
                new TreeMap<String, Map<ShortcutAction, Set<String>>> ();
        for (String profile: keymapModel.getProfiles ()) {
            keymaps.put (
                profile,
                keymapModel.getKeymap (profile)
            );
        }
        
        try {
            StringBuffer sb = new StringBuffer ();

            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "html", attribs, "");
            XMLStorage.generateFolderStart (sb, "body", attribs, "  ");
            attribs.add ("border", "1");
            attribs.add ("cellpadding", "1");
            attribs.add ("cellspacing", "0");
            XMLStorage.generateFolderStart (sb, "table", attribs, "    ");
            attribs = new Attribs (true);

            // print header of table
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            XMLStorage.generateFolderStart (sb, "h2", attribs, "        ");
            sb.append ("Action Name");
            XMLStorage.generateFolderEnd (sb, "h2", "        ");
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            for (String profile: keymaps.keySet ()) {
                XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
                XMLStorage.generateFolderStart (sb, "h2", attribs, "        ");
                sb.append (profile);
                XMLStorage.generateFolderEnd (sb, "h2", "        ");
                XMLStorage.generateFolderEnd (sb, "td", "        ");
            }
            
            // print body of table
            exportShortcutsToHTML2 (keymapModel, sb, keymaps);
            
            XMLStorage.generateFolderEnd (sb, "table", "    ");
            XMLStorage.generateFolderEnd (sb, "body", "  ");
            XMLStorage.generateFolderEnd (sb, "html", "");
            
            FileObject fo = FileUtil.createData (
                Repository.getDefault ().getDefaultFileSystem ().getRoot (),
                "shortcuts.html"
            );
            FileLock fileLock = fo.lock ();
            try {
                OutputStream outputStream = fo.getOutputStream (fileLock);
                OutputStreamWriter writer = new OutputStreamWriter (outputStream);
                writer.write (sb.toString ());
                writer.close ();
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            } finally {
                fileLock.releaseLock ();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }

    /**
     * Writes body of shortcuts table to given StringBuffer.
     */
    private static void exportShortcutsToHTML2 (
        KeymapModel keymapModel, 
        StringBuffer sb,
        Map<String, Map<ShortcutAction, Set<String>>> keymaps
    ) {
        List<String> categories = new ArrayList<String> (keymapModel.getActionCategories ());
        Collections.<String>sort (categories);
        Attribs attribs = new Attribs (true);
        for (String category: categories) {
            
            // print category title
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            attribs.add ("colspan", Integer.toString (keymaps.size () + 1));
            attribs.add ("rowspan", "1");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "h3", attribs, "        ");
            sb.append (category);
            XMLStorage.generateFolderEnd (sb, "h3", "        ");
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            XMLStorage.generateFolderEnd (sb, "tr", "      ");
            
            // print body of one category
            exportShortcutsToHTML3 (sb, keymapModel, category, keymaps);
        }
    }

    /**
     * Writes body of given category.
     */
    private static void exportShortcutsToHTML3 (
        StringBuffer sb, 
        KeymapModel keymapModel, 
        String category,
        Map<String, Map<ShortcutAction, Set<String>>> keymaps
    ) {
        Set<ShortcutAction> actions = keymapModel.getActions (category);

        // sort actions
        Map<String, ShortcutAction> sortedActions = new TreeMap<String, ShortcutAction> ();
        for (ShortcutAction action: actions) {
            sortedActions.put (
                action.getDisplayName (), 
                action
            );
        }

        // print actions
        Attribs attribs = new Attribs (true);
        for (Map.Entry<String, ShortcutAction> entry: sortedActions.entrySet()) {
            String actionName = entry.getKey();
            ShortcutAction action = entry.getValue();

            // print action name to the first column
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            sb.append (actionName);
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            
            for (String profile: keymaps.keySet ()) {
                Map<ShortcutAction, Set<String>> keymap = keymaps.get (profile);
                Set<String> shortcuts = keymap.get (action);

                XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
                printShortcuts (shortcuts, sb);
                XMLStorage.generateFolderEnd (sb, "td", "        ");
            }
            
            XMLStorage.generateFolderEnd (sb, "tr", "      ");
        }
    }
    
    private static void printShortcuts (Set<String> shortcuts, StringBuffer sb) {
        if (shortcuts == null) {
            sb.append ('-');
            return;
        }
        Iterator<String> it = shortcuts.iterator ();
        while (it.hasNext ()) {
            String shortcut = it.next ();
            sb.append (shortcut);
            if (it.hasNext ()) sb.append (", ");
        }
    }
    
    private static void generateLayersXML (
        LayersBridge layersBridge, 
        Map<String, Map<String, ShortcutAction>> categoryToActions
    ) {
        Writer fw = null;
        try {
            fw = openWriter ();
            if (fw == null) return;

            StringBuffer sb = XMLStorage.generateHeader ();
            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "filesystem", attribs, "");
            attribs.add ("name", "Keymaps");
                XMLStorage.generateFolderStart (sb, "folder", attribs, "    ");
                    generateShadowsToXML (layersBridge, sb, categoryToActions, "        ");
                XMLStorage.generateFolderEnd (sb, "folder", "    ");
            XMLStorage.generateFolderEnd (sb, "filesystem", "");
            System.out.println(sb.toString ());
            fw.write (sb.toString ());
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        } finally {
            try {
                if (fw != null) {
                    fw.flush ();
                    fw.close ();
                }
            } catch (IOException e) {}
        }
    }
    
    private static void generateEditorXML (
        Map<ShortcutAction, Set<String>> actionToShortcuts
    ) {
        Writer fw = null;
        try {
            fw = openWriter ();
            if (fw == null) return;

            StringBuffer sb = XMLStorage.generateHeader ();
            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "bindings", attribs, "");
            
            Map<String, Set<String>> sortedMap = new TreeMap<String, Set<String>> ();
            for (ShortcutAction action: actionToShortcuts.keySet ()) {
                sortedMap.put (
                    action.getDisplayName (), 
                    actionToShortcuts.get (action)
                );
            }
            for (String actionName: sortedMap.keySet ()) {
                Set<String> shortcuts = sortedMap.get (actionName);
                for (String shortcut: shortcuts) {
                    attribs = new Attribs (true);
                    attribs.add ("actionName", actionName);
                    attribs.add ("key", shortcut);
                    XMLStorage.generateLeaf (sb, "bind", attribs, "  ");
                }
            }
            
            XMLStorage.generateFolderEnd (sb, "bindings", "");
            System.out.println(sb.toString ());
            fw.write (sb.toString ());
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        } finally {
            try {
                if (fw != null) {
                    fw.flush ();
                    fw.close ();
                }
            } catch (IOException e) {}
        }
    }
    
    private static Map<String, Map<String, ShortcutAction>> resolveNames (Map<String, Set<ShortcutAction>> categoryToActions) {
        Map<String, Map<String, ShortcutAction>> result = new HashMap<String, Map<String, ShortcutAction>> ();
        for (Map.Entry<String, Set<ShortcutAction>> entry: categoryToActions.entrySet ()) {
            String category = entry.getKey();
            Set<ShortcutAction> actions = entry.getValue();
            Map<String, ShortcutAction> actionsMap = new HashMap<String, ShortcutAction> ();
            for (ShortcutAction action: actions) {
                actionsMap.put (action.getDisplayName (), action);
            }
            result.put (category, actionsMap);
        }
        return result;
    }
    
    /**
     * Converts:
     * Map (String (profile | category) > Map (String (category)) |
     *                                    ShortcutAction)
     * to xml. 
     *   (String > Map) is represented by folder and
     *   (String > DataObject) by ShadowDO
     */
    private static void generateShadowsToXML (
        LayersBridge        layersBridge,
        StringBuffer        sb,
        Map<String, Map<String, ShortcutAction>> shortcutToAction,
        String              indentation
    ) {
        Iterator<String> it = shortcutToAction.keySet ().iterator ();
        while (it.hasNext ()) {
            String key = it.next ();
            Map<String, ShortcutAction> value = shortcutToAction.get (key);
            Attribs attribs = new Attribs (true);
            attribs.add ("name", key);
            XMLStorage.generateFolderStart (sb, "folder", attribs, indentation);
            generateShadowsToXML2 (
                layersBridge,
                sb, 
                value, 
                "    " + indentation
            );
            XMLStorage.generateFolderEnd (sb, "folder", indentation);
        }
    }
    
    private static void generateShadowsToXML2 (
        LayersBridge        layersBridge,
        StringBuffer        sb,
        Map<String, ShortcutAction> shortcutToAction,
        String              indentation
    ) {
        Iterator<String> it = shortcutToAction.keySet ().iterator ();
        while (it.hasNext ()) {
            String key = it.next ();
            ShortcutAction value = shortcutToAction.get (key);

            DataObject dob = layersBridge.getDataObject (value);
            if (dob == null) {
                System.out.println("no Dataobject " + value);
                continue;
            }
            FileObject fo = dob.getPrimaryFile ();
            Attribs attribs = new Attribs (true);
            attribs.add ("name", key + ".shadow");
            XMLStorage.generateFolderStart (sb, "file", attribs, indentation);
                Attribs attribs2 = new Attribs (true);
                attribs2.add ("name", "originalFile");
                attribs2.add ("stringvalue", fo.getPath ());
                XMLStorage.generateLeaf (sb, "attr", attribs2, indentation + "    ");
            XMLStorage.generateFolderEnd (sb, "file", indentation);
        }
    }
    
    private static Writer openWriter () throws IOException {
        JFileChooser fileChooser = new JFileChooser ();
        int result = fileChooser.showSaveDialog 
            (WindowManager.getDefault ().getMainWindow ());
        if (result != JFileChooser.APPROVE_OPTION) return null;
        File f = fileChooser.getSelectedFile ();
        return new FileWriter (f);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ExportShortcutsAction.class, key);
    }
}

