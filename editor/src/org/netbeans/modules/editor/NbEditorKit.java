/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.ActionFactory;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.FindDialogSupport;
import org.netbeans.editor.ext.GotoDialogSupport;
import org.openide.TopManager;
import org.openide.windows.TopComponent;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;
import org.openide.windows.TopComponent;
import org.openide.text.Annotation;
import org.netbeans.editor.Bookmarks;
import org.openide.text.Line;
import org.netbeans.editor.ActionFactory.ToggleBookmarkAction;
import org.netbeans.editor.ActionFactory.GotoNextBookmarkAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.OptionUtilities;
import org.netbeans.modules.editor.options.AllOptionsFolder;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorKit extends ExtKit {

    /** Action property that stores the name of the corresponding nb-system-action */
    public static final String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName";

    public static final String BOOKMARK_ANNOTATION_TYPE = "editor-bookmark";
    
    static final long serialVersionUID =4482122073483644089L;
    
    private static final Map contentTypeTable;

    static {
        contentTypeTable = new HashMap();
        contentTypeTable.put("org.netbeans.modules.properties.syntax.PropertiesKit", "text/x-properties");
        contentTypeTable.put("org.netbeans.modules.web.core.syntax.JSPKit", "text/x-jsp");
        contentTypeTable.put("org.netbeans.modules.css.text.syntax.CSSEditorKit", "text/css"); // new  - open source package
        contentTypeTable.put("org.netbeans.modules.xml.css.editor.CSSEditorKit", "text/css"); // old  - close source package
        contentTypeTable.put("org.netbeans.modules.xml.text.syntax.DTDKit", "text/x-dtd");
        contentTypeTable.put("org.netbeans.modules.xml.text.syntax.XMLKit", "text/xml");
        contentTypeTable.put("org.netbeans.modules.corba.idl.editor.coloring.IDLKit", "text/x-idl");
    }

    public NbEditorKit(){
        super();
        // lazy init of MIME options
        NbEditorSettingsInitializer.init();
        AllOptionsFolder.getDefault().loadMIMEOption(this.getClass());
    }

    public Document createDefaultDocument() {
        return new NbEditorDocument(this.getClass());
    }

    protected EditorUI createEditorUI() {
        return new NbEditorUI();
    }

    protected Action[] createActions() {
        Action[] nbEditorActions = new Action[] {
                                       new NbBuildPopupMenuAction(),
                                       new NbStopMacroRecordingAction(),
                                       new NbUndoAction(),
                                       new NbRedoAction(),
                                       new NbToggleBookmarkAction(),
                                       new NbGotoNextBookmarkAction(BaseKit.gotoNextBookmarkAction, false),
                                       new NbBuildToolTipAction(),
                                       new NbToggleLineNumbersAction()
                                   };
        return TextAction.augmentList(super.createActions(), nbEditorActions);
    }


    protected void addSystemActionMapping(String editorActionName, Class systemActionClass) {
        Action a = getActionByName(editorActionName);
        if (a != null) {
            a.putValue(SYSTEM_ACTION_CLASS_NAME_PROPERTY, systemActionClass.getName());
        }
    }

    protected void updateActions() {
        addSystemActionMapping(cutAction, org.openide.actions.CutAction.class);
        addSystemActionMapping(copyAction, org.openide.actions.CopyAction.class);
        addSystemActionMapping(pasteAction, org.openide.actions.PasteAction.class);
        addSystemActionMapping(removeSelectionAction, org.openide.actions.DeleteAction.class);

        addSystemActionMapping(findAction, org.openide.actions.FindAction.class);
        addSystemActionMapping(replaceAction, org.openide.actions.ReplaceAction.class);
        addSystemActionMapping(gotoAction, org.openide.actions.GotoAction.class);
    }

    public Class getFocusableComponentClass(JTextComponent c) {
        return TopComponent.class;
    }
    
    public String getContentType() {
        if( Boolean.getBoolean( "netbeans.debug.exceptions" ) ){ //NOI18N
            System.out.println("Warning: KitClass "+this.getClass().getName()+" doesn't override the method getContentType.");
        }
        return (contentTypeTable.containsKey(this.getClass().getName())) ? 
            (String)contentTypeTable.get(this.getClass().getName()) : "text/"+this.getClass().getName().replace('.','_'); //NOI18N
    }

    public class NbBuildPopupMenuAction extends BuildPopupMenuAction {

        static final long serialVersionUID =-8623762627678464181L;

        protected JPopupMenu buildPopupMenu(JTextComponent target) {        
            // XXX 
            // Force running of pending delayed frame activation requests
            // before node menu is constructed.
            // This is hack because of the bug #7794
            // Please see org.netbeans.core.windows.frames.DefaultContainerImpl$2 for details
            // This hack has been also used for showing popup on explorer nodes, see
            // org.openide.nodes.NodeOp.findContextMenu()
            javax.swing.JFrame main = (javax.swing.JFrame)TopManager.getDefault().
                                      getWindowManager().getMainWindow();
            javax.swing.JComponent mainContentPane = (javax.swing.JComponent)main.getContentPane();
            Object pendingNodeActivator =
                mainContentPane.getClientProperty("hack.pendingNodeActivator"); // NOI18N
            mainContentPane.putClientProperty("hack.pendingNodeActivator", null); // NOI18N
            if ( pendingNodeActivator instanceof Runnable) {
                ((Runnable)pendingNodeActivator).run();
            }

            // to make keyboard navigation (Up/Down keys) inside popup work, we
            // must use JPopupMenuPlus instead of JPopupMenu
            JPopupMenu popup = super.buildPopupMenu(target);
            if (popup instanceof org.openide.awt.JPopupMenuPlus)
                return popup;

            java.awt.Component[] comps = popup.getComponents();
            popup.removeAll();

            org.openide.awt.JPopupMenuPlus popupPlus = new org.openide.awt.JPopupMenuPlus();
            for (int i = 0; i < comps.length; i++) {
                popupPlus.add(comps[i]);
            }
            return popupPlus;
        }
        
        protected void addAction(JTextComponent target, JPopupMenu popupMenu,
        String actionName) {
            if (actionName != null) { // try if it's an action class name
                // Check for the TopComponent actions
                if (TopComponent.class.getName().equals(actionName)) {
                    // Get the cloneable-editor instance
                    TopComponent tc = NbEditorUtilities.getTopComponent(target);
                    if (tc != null) {
                        // Add all the actions
                        SystemAction[] actions = tc.getSystemActions();
                        TopManager tm = NbEditorUtilities.getTopManager();
                        if (tm != null) { // IDE initialized
                            for (int i = 0; i < actions.length; i++) {
/*                                System.out.println("NbEditorKit.java:126 top-component actions["
                                        + i + "]=" + ((actions[i] != null)
                                            ? actions[i].getClass().getName() : "NULL"));
*/
                                if (actions[i] instanceof Presenter.Popup) {
                                    JMenuItem item = ((Presenter.Popup)actions[i]).getPopupPresenter();
                                    if (item != null && !(item instanceof JMenu)) {
                                        KeyStroke[] keys
                                            = tm.getGlobalKeymap().getKeyStrokesForAction(actions[i]);
                                        if (keys != null && keys.length > 0) {
                                            item.setAccelerator(keys[0]);
                                        }

                                    }

                                    if (item != null) {
                                        popupMenu.add(item);
                                    }

                                } else if (actions[i] == null) {
                                    popupMenu.addSeparator();
                                }
                            }
                        }
                    }

                    return;

                } else { // not cloneable-editor actions
                    Class saClass = null;
                    TopManager tm = NbEditorUtilities.getTopManager();
                    try {
                        if (tm != null) {
                            saClass = Class.forName(actionName, false, tm.systemClassLoader());
                        }
                    } catch (Throwable t) {
                    }

                    if (saClass != null && SystemAction.class.isAssignableFrom(saClass)) {
                        if (tm != null) { // IDE initialized
                            SystemAction sa = SystemAction.get(saClass);
                            if (sa instanceof Presenter.Popup) {
                                JMenuItem item = ((Presenter.Popup)sa).getPopupPresenter();
                                if (item != null && !(item instanceof JMenu)) {
                                    KeyStroke[] keys = tm.getGlobalKeymap().getKeyStrokesForAction(sa);
                                    if (keys != null && keys.length > 0) {
                                        item.setAccelerator(keys[0]);
                                    }
                                }

                                if (item != null) {
                                    popupMenu.add(item);
                                }
                            }
                        }

                        return;
                    }
                }

            }

            super.addAction(target, popupMenu, actionName);

        }


    }

    public class NbStopMacroRecordingAction extends ActionFactory.StopMacroRecordingAction {
        
        private BaseOptions bo;
        
        private Map getKBMap(){
            Map ret;
            List list = bo.getKeyBindingList();
            if( list.size() > 0 &&
            ( list.get( 0 ) instanceof Class || list.get( 0 ) instanceof String )
            ) {
                list.remove( 0 ); //remove kit class name
            }
            ret = OptionUtilities.makeKeyBindingsMap(list);
            return ret;
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            bo = BaseOptions.getOptions(NbEditorKit.this.getClass());
            Map oldMacroMap = null;
            Map oldKBMap = null;
            if (bo != null){
                oldMacroMap = bo.getMacroMap();
                oldKBMap = getKBMap();
            }
            
            super.actionPerformed(evt, target);
            
            if (bo != null){
                bo.setMacroDiffMap(OptionUtilities.getMapDiff(oldMacroMap, bo.getMacroMap(), true));
                bo.setKeyBindingsDiffMap(OptionUtilities.getMapDiff(oldKBMap, getKBMap(), true));
            }
        }
        
    }
        
    public static class NbUndoAction extends ActionFactory.UndoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (NbEditorUtilities.getTopManager() != null) {
                // Delegate to system undo action
                UndoAction ua = (UndoAction)SystemAction.get(UndoAction.class);
                if (ua != null && ua.isEnabled()) {
                    ua.actionPerformed(evt);
                }

            } else {
                super.actionPerformed(evt, target);
            }
        }

    }

    public static class NbRedoAction extends ActionFactory.RedoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (NbEditorUtilities.getTopManager() != null) {
                // Delegate to system redo action
                RedoAction ra = (RedoAction)SystemAction.get(RedoAction.class);
                if (ra != null && ra.isEnabled()) {
                    ra.actionPerformed(evt);
                }

            } else {
                super.actionPerformed(evt, target);
            }
        }

    }


    public static class NbToggleBookmarkAction extends ToggleBookmarkAction {

        static final long serialVersionUID = 8870696224845563318L;

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null)
                return;

            BaseDocument doc = (BaseDocument)target.getDocument();
            Caret caret = target.getCaret();
            
            // check whether the glyph gutter is visible or not
            if (Utilities.getEditorUI(target) == null || !Utilities.getEditorUI(target).isGlyphGutterVisible()) {
                target.getToolkit().beep();
                return;
            }

            int line = 0;
            try {
                line = Utilities.getLineOffset(doc, caret.getDot());
            } catch (BadLocationException e) {
                target.getToolkit().beep();
                return;
            }

            Bookmarks bookmarks = doc.getBookmarks();
            
            Annotation anno = null;
            Bookmark bookmark = (Bookmark)bookmarks.getBookmark(line);
            if (bookmark != null)
                anno = bookmark.getAnno();
            
            if (anno == null) {
                anno = new BookmarkAnnotation();
                
                Line lineObj = NbEditorUtilities.getLine(doc, caret.getDot(), false);
                if (lineObj == null) {
                    target.getToolkit().beep();
                    return;
                }
                anno.attach(lineObj);

                bookmarks.putBookmark(new Bookmark(anno));
            } else {
                anno.detach();
                bookmarks.removeBookmark(bookmark);
            }
        }
    }

    public static class NbGotoNextBookmarkAction extends GotoNextBookmarkAction {

        static final long serialVersionUID =-6305740718286540539L;

        public NbGotoNextBookmarkAction() {
            super(null, false);
        }
        
        public NbGotoNextBookmarkAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null)
                return;
                
            BaseDocument doc = (BaseDocument)target.getDocument();
            Caret caret = target.getCaret();

            // check whether the glyph gutter is visible or not
            if (Utilities.getEditorUI(target) == null || !Utilities.getEditorUI(target).isGlyphGutterVisible()) {
                target.getToolkit().beep();
                return;
            }
            
            int line = 0;
            
            try {
                line = Utilities.getLineOffset(doc, caret.getDot());
            } catch (BadLocationException e) {
                target.getToolkit().beep();
                return;
            }

            Bookmarks bookmarks = doc.getBookmarks();
            
            Bookmark bookmark = (Bookmark)bookmarks.getNextLineBookmark(line+1);

            if (bookmark == null)
                bookmark = (Bookmark)bookmarks.getNextLineBookmark(0);
                
            if (bookmark == null)
                return;

            Annotation anno = bookmark.getAnno();
            anno.moveToFront();
            ((Line)anno.getAttachedAnnotatable()).show(Line.SHOW_GOTO);
        }
    }

    /** Switch visibility of line numbers in editor */
    public class NbToggleLineNumbersAction extends ActionFactory.ToggleLineNumbersAction {

        private BaseOptions bo;
        
        public NbToggleLineNumbersAction() {
            bo = BaseOptions.getOptions(NbEditorKit.this.getClass());
        }
        
        protected boolean isLineNumbersVisible() {
            return bo.getLineNumberVisible();
        }
        
        protected void toggleLineNumbers() {
            bo.setLineNumberVisible(!isLineNumbersVisible());
        }
        
    }

    
    /** Annotation implementation for bookmarks */
    private static class BookmarkAnnotation extends Annotation {
        
        public String getAnnotationType() {
            return BOOKMARK_ANNOTATION_TYPE;
        }
        
        public String getShortDescription() {
            return org.openide.util.NbBundle.getBundle (NbEditorKit.class).getString("Bookmark_Tooltip"); // NOI18N
        }
    }

    /** Description of bookmark */
    private static class Bookmark implements Bookmarks.Bookmark {
        
        private Annotation anno;
        
        public Bookmark(Annotation anno) {
            this.anno = anno;
        }
        
        public int getLine() {
            return ((Line)anno.getAttachedAnnotatable()).getLineNumber();
        }

        public Annotation getAnno() {
            return anno;
        }
        
        public void remove() {
            anno.detach();
            anno = null;
        }
        
    }
    

    public static class NbBuildToolTipAction extends BuildToolTipAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                NbToolTip.buildToolTip(target);
            }
        }

    }

}
