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

package org.netbeans.editor.ext;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.Caret;
import javax.swing.text.Keymap;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Formatter;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib.NavigationHistory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
* Extended kit offering advanced functionality
*
* @author Miloslav Metelka
* @version 1.00
*/
public class ExtKit extends BaseKit {

    /** This action is searched and executed when the popup menu should
    * be displayed to build the popup menu.
    */
    public static final String buildPopupMenuAction = "build-popup-menu"; // NOI18N

    /** Show popup menu.
    */
    public static final String showPopupMenuAction = "show-popup-menu"; // NOI18N

    /** This action is searched and executed when the tool-tip should
    * be displayed by tool-tip support to build the tool-tip.
    */
    public static final String buildToolTipAction = "build-tool-tip"; // NOI18N

    /** Open find dialog action - this action is defined in view package, but
     * its name is defined here for clarity
     * @deprecated Without any replacement.
     */
    public static final String findAction = "find"; // NOI18N

    /** Open replace dialog action - this action is defined in view package, but
    * its name is defined here for clarity
    */
    public static final String replaceAction = "replace"; // NOI18N

    /** Open goto dialog action - this action is defined in view package, but
    * its name is defined here for clarity
    */
    public static final String gotoAction = "goto"; // NOI18N

    /** Goto declaration depending on the context under the caret */
    public static final String gotoDeclarationAction = "goto-declaration"; // NOI18N

    /** Goto source depending on the context under the caret */
    public static final String gotoSourceAction = "goto-source"; // NOI18N

    public static final String gotoSuperImplementationAction = "goto-super-implementation"; // NOI18N

    /** Goto help depending on the context under the caret */
    public static final String gotoHelpAction = "goto-help"; // NOI18N

    /** Match brace */
    public static final String matchBraceAction = "match-brace"; // NOI18N

    /** Select the text to the matching bracket */
    public static final String selectionMatchBraceAction = "selection-match-brace"; // NOI18N

    /** Toggle the case for the first character of the word under caret */
    public static final String toggleCaseIdentifierBeginAction = "toggle-case-identifier-begin"; // NOI18N

    /** Advanced code selection technique
     * @deprecated this action name is not actively used by ExtKit and will be removed in future releases.
     */
    public static final String codeSelectAction = "code-select"; // NOI18N

    /** Action used when escape is pressed. By default it hides popup-menu
     * @deprecated this action name is not actively used by ExtKit and will be removed in future releases.
     */
    public static final String escapeAction = "escape"; // NOI18N

    /** Find the completion help and show it in the completion pane. */
    public static final String completionShowAction = "completion-show"; // NOI18N
    public static final String allCompletionShowAction = "all-completion-show"; // NOI18N

    /** Show documentation popup panel */
    public static final String documentationShowAction = "documentation-show"; // NOI18N
    
    /** Show completion tooltip */
    public static final String completionTooltipShowAction = "tooltip-show"; // NOI18N

    /** Comment out the selected block */
    public static final String commentAction = "comment"; // NOI18N

    /** Uncomment the selected block */
    public static final String uncommentAction = "uncomment"; // NOI18N
    
    /** Comment/Uncomment the selected block */
    public static final String toggleCommentAction = "toggle-comment"; // NOI18N
    
    /** Toggle the toolbar */
    public static final String toggleToolbarAction = "toggle-toolbar"; // NOI18N
   
    /** Trimmed text for go to submenu*/
    public static final String TRIMMED_TEXT = "trimmed-text";    //NOI18N
    
    /** Shared suport for find and replace dialogs */
    private static FindDialogSupport findDialogSupport;
    
    private static FindAction findActionDef = new FindAction();
    private static ReplaceAction replaceActionDef = new ReplaceAction();
    private static GotoAction gotoActionDef = new GotoAction();

    private static final String editorBundleHash = "org.netbeans.editor.Bundle#";

    /** Whether editor popup menu creation should be dumped to console */
    private static final boolean debugPopupMenu
            = Boolean.getBoolean("netbeans.debug.editor.popup.menu"); // NOI18N
    
    public ExtKit() {
    }

    /** Create caret to navigate through document */
    public @Override Caret createCaret() {
        return new ExtCaret();
    }

    public @Override SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new ExtSyntaxSupport(doc);
    }

// XXX: remove
//    public Completion createCompletion(ExtEditorUI extEditorUI) {
//        return null;
//    }
//    
//    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
//        return null;
//    }

    private boolean noExtEditorUIClass = false;
    protected @Override EditorUI createEditorUI() {
        if (!noExtEditorUIClass) {
            try {
                ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                Class extEditorUIClass = loader.loadClass("org.netbeans.editor.ext.ExtEditorUI"); //NOI18N
                return (EditorUI) extEditorUIClass.newInstance();
            } catch (Exception e) {
                noExtEditorUIClass = true;
            }
        }
        return new EditorUI();
    }

    protected @Override Action[] createActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        
        actions.add(replaceActionDef);
        actions.add(gotoActionDef);
// XXX: remove
//        if (!ExtCaret.NO_HIGHLIGHT_BRACE_LAYER) {
//            actions.add(new MatchBraceAction(matchBraceAction, false));
//            actions.add(new MatchBraceAction(selectionMatchBraceAction, true));
//        }
        actions.add(new CommentAction()); // to make ctrl-shift-T in Netbeans55 profile work
        actions.add(new UncommentAction()); // to make ctrl-shift-D in Netbeans55 profile work
                
        return TextAction.augmentList(super.createActions(), actions.toArray(new Action[actions.size()]));
    }
    
    /**
     * Action that is localized in org.netbeans.editor package.
     * <br/>
     * <code>BaseKit.class</code> is used as a bundle class.
     */
    private abstract static class BaseKitLocalizedAction extends BaseAction {

        public BaseKitLocalizedAction() {
            super();
        }

        public BaseKitLocalizedAction(int updateMask) {
            super(updateMask);
        }

        public BaseKitLocalizedAction(String name) {
            super(name);
        }
        
        public BaseKitLocalizedAction(String name, int updateMask) {
            super(name, updateMask);
        }
        
        protected @Override Class getShortDescriptionBundleClass() {
            return BaseKit.class;
        }
        
    }

    /** Called before the popup menu is shown to possibly rebuild
    * the popup menu.
    */
    @EditorActionRegistration(
            name = buildPopupMenuAction,
            shortDescription = editorBundleHash + buildPopupMenuAction
    )
    public static class BuildPopupMenuAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =4257043398248915291L;

        public BuildPopupMenuAction() {
            super(NO_RECORDING);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (debugPopupMenu) {
                    /*DEBUG*/System.err.println("POPUP CREATION START <<<<<"); // NOI18N
                }
                JPopupMenu pm = buildPopupMenu(target);
                if (debugPopupMenu) {
                    /*DEBUG*/System.err.println("POPUP CREATION END >>>>>"); // NOI18N
                }
                Utilities.getEditorUI(target).setPopupMenu(pm);
            }
        }
        
        protected JPopupMenu createPopupMenu(JTextComponent target) {
            return new JPopupMenu();
        }

        protected JPopupMenu buildPopupMenu(JTextComponent target) {
            JPopupMenu pm = createPopupMenu(target);

            EditorUI ui = Utilities.getEditorUI(target);
            String settingName = ui == null || ui.hasExtComponent()
                    ? "popup-menu-action-name-list" //NOI18N
                    : "dialog-popup-menu-action-name-list"; //NOI18N
            
            Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(target)).lookup(Preferences.class);
            String actionNames = prefs.get(settingName, null);

            if (actionNames != null) {
                for(StringTokenizer t = new StringTokenizer(actionNames, ","); t.hasMoreTokens(); ) {
                    String action = t.nextToken().trim();
                    addAction(target, pm, action);
                }
            }
            
            return pm;
        }

        /** Add the action to the popup menu. This method is called
         * for each action-name found in the action-name-list. It should
         * add the appopriate menu item to the popup menu.
         * @param target target component for which the menu is being
         *  constructed.
         * @param popupMenu popup menu to which this method should add
         *  the item corresponding to the action-name.
         * @param actionName name of the action to add. The real action
         *  can be retrieved from the kit by calling <tt>getActionByName()</tt>.
         */
        protected void addAction(JTextComponent target, JPopupMenu popupMenu,
        String actionName) {
            Action a = Utilities.getKit(target).getActionByName(actionName);
            if (a != null) {
                JMenuItem item = null;
                if (a instanceof BaseAction) {
                    item = ((BaseAction)a).getPopupMenuItem(target);
                }
                if (item == null) {
                    String itemText = getItemText(target, actionName, a);
                    if (itemText != null) {
                        item = new JMenuItem(itemText);
                        item.addActionListener(a);
                        // Try to get the accelerator
                        Keymap km = target.getKeymap();
                        if (km != null) {
                            KeyStroke[] keys = km.getKeyStrokesForAction(a);
                            if (keys != null && keys.length > 0) {
                                item.setAccelerator(keys[0]);
                            }
                        }
                        item.setEnabled(a.isEnabled());
                        Object helpID = a.getValue ("helpID"); // NOI18N
                        if (helpID != null && (helpID instanceof String))
                            item.putClientProperty ("HelpID", helpID); // NOI18N
                    }
                }

                if (item != null) {
                    debugPopupMenuItem(item, a);
                    popupMenu.add(item);
                }

            } else if (actionName == null){ // action-name is null, add the separator
                if (popupMenu.getComponentCount()>0){
                    debugPopupMenuItem(null, null);
                    popupMenu.addSeparator();
                }
            }
        }
        
        protected final void debugPopupMenuItem(JMenuItem item, Action action) {
            if (debugPopupMenu) {
                StringBuffer sb = new StringBuffer("POPUP: "); // NOI18N
                if (item != null) {
                    sb.append('"'); //NOI18N
                    sb.append(item.getText());
                    sb.append('"'); //NOI18N
                    if (!item.isVisible()) {
                        sb.append(", INVISIBLE"); // NOI18N
                    }
                    if (action != null) {
                        sb.append(", action="); // NOI18N
                        sb.append(action.getClass().getName());
                    }
                    
                } else { // null item means separator
                    sb.append("--Separator--"); // NOI18N
                }

                /*DEBUG*/System.err.println(sb.toString());
            }

        }

        protected String getItemText(JTextComponent target, String actionName, Action a) {
            String itemText;
            if (a instanceof BaseAction) {
                itemText = ((BaseAction)a).getPopupMenuText(target);
            } else {
                itemText = actionName;
            }
            return itemText;
        }

    }

    /** Show the popup menu.
    */
    @EditorActionRegistration(
            name = showPopupMenuAction,
            shortDescription = editorBundleHash + showPopupMenuAction
    )
    public static class ShowPopupMenuAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =4257043398248915291L;

        public ShowPopupMenuAction() {
            super(NO_RECORDING);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    int dotPos = target.getCaret().getDot();
                    Rectangle r = target.getUI().modelToView(target, dotPos);
                    if (r != null) {
                        EditorUI eui = Utilities.getEditorUI(target);
                        if (eui != null) {
                            eui.showPopupMenu(r.x, r.y + r.height);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }

    }

    @EditorActionRegistration(
            name = buildToolTipAction,
            shortDescription = editorBundleHash + buildToolTipAction
    )
    public static class BuildToolTipAction extends BaseAction {

        static final long serialVersionUID =-2701131863705941250L;

        public BuildToolTipAction() {
            super(NO_RECORDING);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        protected String buildText(JTextComponent target) {
            ToolTipSupport tts = Utilities.getEditorUI(target).getToolTipSupport();
            return  (tts != null)
                ? target.getToolTipText(tts.getLastMouseEvent())
                : target.getToolTipText();
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                ToolTipSupport tts = Utilities.getEditorUI(target).getToolTipSupport();
                if (tts != null) {
                    tts.setToolTipText(buildText(target));
                }
            }
        }

    }

    /**
     * @deprecated Without any replacement.
     */
    public static class FindAction extends BaseKitLocalizedAction {
    // Not registered by annotation since it's not actively used

        static final long serialVersionUID =719554648887497427L;

        public FindAction() {
            super(findAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | NO_RECORDING);
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                    "org/netbeans/modules/editor/resources/find"); //NOI18N
        }

        public FindDialogSupport getSupport() {
            return FindDialogSupport.getFindDialogSupport();
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                getSupport().showFindDialog(new KeyEventBlocker(target, false));
            }
        }

    }

    public static class ReplaceAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =1828017436079834384L;

        public ReplaceAction() {
            super(replaceAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | NO_RECORDING);
        }

        public FindDialogSupport getSupport() {
            return FindDialogSupport.getFindDialogSupport();
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                // make KeyEventBlocker to discard the first key typed event (Ctrl-H)
                // because it is mapped to backspace in the replace dialog
                getSupport().showReplaceDialog(new KeyEventBlocker(target, true));
            }
        }

    }

    public static class GotoAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =8425585413146373256L;

        public GotoAction() {
            super(gotoAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            String name = NbBundle.getBundle(BaseKit.class).getString("goto_trimmed");
            putValue(TRIMMED_TEXT, name);
            putValue(POPUP_MENU_TEXT, name);
        }


        /** This method is called by the dialog support
        * to translate the line offset to the document position. This
        * can be changed for example for the diff operations.
        * @param doc document to operate over
        * @param lineOffset the line offset to convert to position
        * @return document offset that corresponds to the row-start
        *  of the line with the line-number equal to (lineOffset + 1).
        */
        protected int getOffsetFromLine(BaseDocument doc, int lineOffset) {
            return Utilities.getRowStartFromLineOffset(doc, lineOffset);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                new GotoDialogSupport().showGotoDialog(new KeyEventBlocker(target, false));
            }
        }

    }

    /** Action to go to the declaration of the variable under the caret.
    */
    public static class GotoDeclarationAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =-6440495023918097760L;

        public GotoDeclarationAction() {
            super(gotoDeclarationAction,
                  ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET
                  | SAVE_POSITION
                 );
            String name = NbBundle.getBundle(BaseKit.class).getString("goto-declaration-trimmed");
            putValue(TRIMMED_TEXT, name);  //NOI18N            
            putValue(POPUP_MENU_TEXT, name);  //NOI18N            
        }

        public boolean gotoDeclaration(JTextComponent target) {
            BaseDocument doc = Utilities.getDocument(target);
            if (doc == null)
                return false;
            try {
                Caret caret = target.getCaret();
                int dotPos = caret.getDot();
                int[] idBlk = Utilities.getIdentifierBlock(doc, dotPos);
                ExtSyntaxSupport extSup = (ExtSyntaxSupport)doc.getSyntaxSupport();
                if (idBlk != null) {
                    int decPos = extSup.findDeclarationPosition(doc.getText(idBlk), idBlk[1]);
                    if (decPos >= 0) {
                        caret.setDot(decPos);
                        return true;
                    }
                }
            } catch (BadLocationException e) {
            }
            return false;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                gotoDeclaration(target); // try to go to the declaration position
            }
        }
    }

    @EditorActionRegistration(name = toggleCaseIdentifierBeginAction, shortDescription = "")
    public static class ToggleCaseIdentifierBeginAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =584392193824931979L;

        public ToggleCaseIdentifierBeginAction() {
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int[] idBlk = Utilities.getIdentifierBlock(doc, caret.getDot());
                    if (idBlk != null) {
                        Utilities.changeCase(doc, idBlk[0], 1, Utilities.CASE_SWITCH);
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /**
     * This action does nothing.
     * 
     * @deprecated Please use Braces Matching SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-bracesmatching@/overview-summary.html">Editor Braces Matching</a>.
     */
    public static class MatchBraceAction extends BaseKitLocalizedAction {
// XXX: remove
//        boolean select;

        static final long serialVersionUID =-184887499045886231L;

        public MatchBraceAction(String name, boolean select) {
            super(name, 0);
// XXX: remove
//            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
// XXX: remove
//            if (target != null) {
//                try {
//                    Caret caret = target.getCaret();
//                    BaseDocument doc = Utilities.getDocument(target);
//                    int dotPos = caret.getDot();
//                    ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
//                    int[] matchBlk = null;
//                    int setDotOffset = 0;
//                    if(caret instanceof ExtCaret) {
//                        int how = ((ExtCaret)caret).getMatchBraceOffset();
//                        if(dotPos > 0 && (how == ExtCaret.MATCH_BRACE_BEFORE
//                                        || how == ExtCaret.MATCH_BRACE_EITHER)) {
//                            matchBlk = sup.findMatchingBlock(dotPos - 1, false);
//                        }
//                        if(matchBlk == null && (how == ExtCaret.MATCH_BRACE_AFTER
//                                    || how == ExtCaret.MATCH_BRACE_EITHER)) {
//                            matchBlk = sup.findMatchingBlock(dotPos, false);
//                            if(how == ExtCaret.MATCH_BRACE_AFTER) {
//                                // back it up so caret is on the match
//                                setDotOffset = -1;
//                            }
//                        }
//                    } else if(dotPos > 0) {
//                        matchBlk = sup.findMatchingBlock(dotPos - 1, false);
//                    }
//                    if (matchBlk != null) {
//                        if (select) {
//                            caret.moveDot(matchBlk[1]);
//                        } else {
//                            caret.setDot(matchBlk[1] + setDotOffset);
//                        }
//                    }
//                } catch (BadLocationException e) {
//                    target.getToolkit().beep();
//                }
//            }
        }
    }

    /**
     * @deprecated this action is deprecated and will be removed in future releases.
     */
    public static class CodeSelectAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =4033474080778585860L;

        public CodeSelectAction() {
            super(codeSelectAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
/*            if (target != null) {
                BaseDocument doc = (BaseDocument)target.getDocument();
                SyntaxSupport sup = doc.getSyntaxSupport();
                Caret caret = target.getCaret();
                try {
                    int bracketPos = sup.findUnmatchedBracket(caret.getDot(), sup.getRightBrackets());
                    if (bracketPos >= 0) {
                        caret.setDot(bracketPos);
                        while (true) {
                          int bolPos = Utilities.getRowStart(doc, bracketPos);
                          boolean isWSC = sup.isCommentOrWhitespace(bolPos, bracketPos);
                          if (isWSC) { // get previous line end
                            
                          }
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
*/
        }
    }

    /** Prefix maker adds the prefix before the identifier under cursor.
    * The prefix is not added if it's already present. The prefix to be
    * added is specified in the constructor of the action together
    * with the prefix group. If there's already any prefix from the prefix
    * group at the begining of the identifier, that prefix is replaced
    * by the actual prefix.
    */
    public static class PrefixMakerAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =-2305157963664484920L;

        private String prefix;

        private String[] prefixGroup;

        public PrefixMakerAction(String name, String prefix, String[] prefixGroup) {
            super(name);
            this.prefix = prefix;
            this.prefixGroup = prefixGroup;
            
            // [PENDING] This should be done in a better way
            String iconRes = null;
            if ("get".equals(prefix)) { // NOI18N
                iconRes = "org/netbeans/modules/editor/resources/var_get.gif"; // NOI18N
            } else if ("set".equals(prefix)) { // NOI18N
                iconRes = "org/netbeans/modules/editor/resources/var_set.gif"; // NOI18N
            } else if ("is".equals(prefix)) { // NOI18N
                iconRes = "org/netbeans/modules/editor/resources/var_is.gif"; // NOI18N
            }
            if (iconRes != null) {
                putValue(BaseAction.ICON_RESOURCE_PROPERTY, iconRes);
            }
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                BaseDocument doc = (BaseDocument)target.getDocument();
                int dotPos = target.getCaret().getDot();
                try {
                    // look for identifier around caret
                    int[] block = org.netbeans.editor.Utilities.getIdentifierBlock(doc, dotPos);

                    // If there is no identifier around, warn user
                    if (block == null) {
                        target.getToolkit().beep();
                        return;
                    }

                    // Get the identifier to operate on
                    CharSequence identifier = DocumentUtilities.getText(doc, block[0], block[1] - block[0]);

                    // Handle the case we already have the work done - e.g. if we got called over 'getValue'
                    if (CharSequenceUtilities.startsWith(identifier, prefix) && 
                            Character.isUpperCase(identifier.charAt(prefix.length()))) return;

                    // Handle the case we have other type of known xEr: eg isRunning -> getRunning
                    for (int i=0; i<prefixGroup.length; i++) {
                        String actPref = prefixGroup[i];
                        if (CharSequenceUtilities.startsWith(identifier, actPref)
                                && identifier.length() > actPref.length()
                                && Character.isUpperCase(identifier.charAt(actPref.length()))
                           ) {
                            doc.remove(block[0], actPref.length());
                            doc.insertString(block[0], prefix, null);
                            return;
                        }
                    }

                    // Upcase the first letter
                    Utilities.changeCase(doc, block[0], 1, Utilities.CASE_UPPER);
                    // Prepend the prefix before it
                    doc.insertString(block[0], prefix, null);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class CommentAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =-1422954906554289179L;

        private ToggleCommentAction delegateAction;

        private CommentAction() {
            this(null);
        }
        
        public CommentAction(String lineCommentString) {
            super(commentAction);
            this.delegateAction = lineCommentString != null ? new ToggleCommentAction(lineCommentString) : null;
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/comment.png"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            ToggleCommentAction action = null;
            
            if (delegateAction != null) {
                action = delegateAction;
            } else {
                BaseKit kit = Utilities.getKit(target);
                Action a = kit == null ? null : kit.getActionByName(toggleCommentAction);
                if (a instanceof ToggleCommentAction) {
                    action = (ToggleCommentAction) a;
                }
            }

            if (action != null) {
                ((ToggleCommentAction) action).commentUncomment(evt, target, Boolean.TRUE);
            } else {
                target.getToolkit().beep();
            }
        }
    } // End of CommentAction class

    public static class UncommentAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =-7005758666529862034L;

        private ToggleCommentAction delegateAction;

        private UncommentAction() {
            this(null);
        }
        
        public UncommentAction(String lineCommentString) {
            super(uncommentAction);
            this.delegateAction = lineCommentString != null ? new ToggleCommentAction(lineCommentString) : null;
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/uncomment.png"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            ToggleCommentAction action = null;
            
            if (delegateAction != null) {
                action = delegateAction;
            } else {
                BaseKit kit = Utilities.getKit(target);
                Action a = kit == null ? null : kit.getActionByName(toggleCommentAction);
                if (a instanceof ToggleCommentAction) {
                    action = (ToggleCommentAction) a;
                }
            }

            if (action != null) {
                ((ToggleCommentAction) action).commentUncomment(evt, target, Boolean.FALSE);
            } else {
                target.getToolkit().beep();
            }
        }
    } // End of UncommentAction class

    /**
     * @since 1.16
     */
    public static class ToggleCommentAction extends BaseAction {

        static final long serialVersionUID = -1L;

        private final String lineCommentString;
        private final int lineCommentStringLen;
        
        public ToggleCommentAction(String lineCommentString) {
            super(toggleCommentAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(ToggleCommentAction.class, "ToggleCommentAction_shortDescription")); //NOI18N
            
            assert lineCommentString != null : "The lineCommentString parameter must not be null."; //NOI18N
            this.lineCommentString = lineCommentString;
            this.lineCommentStringLen = lineCommentString.length();
            
            putValue(BaseAction.ICON_RESOURCE_PROPERTY, "org/netbeans/modules/editor/resources/comment.png"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            commentUncomment(evt, target, null);
        }
        
        private void commentUncomment(ActionEvent evt, final JTextComponent target, final Boolean forceComment) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        try {
                            int startPos;
                            int endPos;

                            if (Utilities.isSelectionShowing(caret)) {
                                startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                                endPos = target.getSelectionEnd();
                                if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                    endPos--;
                                }
                                endPos = Utilities.getRowEnd(doc, endPos);
                            } else { // selection not visible
                                startPos = Utilities.getRowStart(doc, caret.getDot());
                                endPos = Utilities.getRowEnd(doc, caret.getDot());
                            }

                            int lineCount = Utilities.getRowCount(doc, startPos, endPos);
                            boolean comment = forceComment != null ? forceComment : !allComments(doc, startPos, lineCount);

                            if (comment) {
                                comment(doc, startPos, lineCount);
                            } else {
                                uncomment(doc, startPos, lineCount);
                            }
                            NavigationHistory.getEdits().markWaypoint(target, startPos, false, true);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        }
                    }
                });
            }
        }
        
        private boolean allComments(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--) {
                int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);
                if (firstNonWhitePos == -1) {
                    return false;
                }
                
                if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos < lineCommentStringLen) {
                    return false;
                }
                
                CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, lineCommentStringLen);
                if (!CharSequenceUtilities.textEquals(maybeLineComment, lineCommentString)) {
                    return false;
                }
                
                offset = Utilities.getRowStart(doc, offset, +1);
            }
            return true;
        }
        
        private void comment(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--) {
                doc.insertString(offset, lineCommentString, null); // NOI18N
                offset = Utilities.getRowStart(doc, offset, +1);
            }
        }
        
        private void uncomment(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--) {
                // Get the first non-whitespace char on the current line
                int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);

                // If there is any, check wheter it's the line-comment-chars and remove them
                if (firstNonWhitePos != -1) {
                    if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos >= lineCommentStringLen) {
                        CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, lineCommentStringLen);
                        if (CharSequenceUtilities.textEquals(maybeLineComment, lineCommentString)) {
                            doc.remove(firstNonWhitePos, lineCommentStringLen);
                        }
                    }
                }

                offset = Utilities.getRowStart(doc, offset, +1);
            }
        }
        
    } // End of CommentUncommentAction class

    /** Executed when the Escape key is pressed. By default it hides
    * the popup menu if visible.
    */
    public static class EscapeAction extends BaseKitLocalizedAction {

        public EscapeAction() {
            super(escapeAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Utilities.getEditorUI(target).hidePopupMenu();
            }
        }
    }


    // Completion customized actions
    @EditorActionRegistration(
            name = defaultKeyTypedAction,
            shortDescription = editorBundleHash + defaultKeyTypedAction
    )
    public static class ExtDefaultKeyTypedAction extends DefaultKeyTypedAction {

        static final long serialVersionUID =5273032708909044812L;

        public @Override void actionPerformed(ActionEvent evt, JTextComponent target) {
            String cmd = evt.getActionCommand();
            int mod = evt.getModifiers();

            // Dirty fix for Completion shortcut on Unix !!!
            if (cmd != null && cmd.equals(" ") && (mod == ActionEvent.CTRL_MASK)) { // NOI18N
                // Ctrl + SPACE
            } else {
                Caret caret = target.getCaret();
                if (caret instanceof ExtCaret) {
                    ((ExtCaret)caret).requestMatchBraceUpdateSync(); // synced bracket update
                }
                super.actionPerformed(evt, target);
            }

            if ((target != null) && (evt != null)) {
                if ((cmd != null) && (cmd.length() == 1) &&
                        ((mod & ActionEvent.ALT_MASK) == 0
                         && (mod & ActionEvent.CTRL_MASK) == 0)
                   ) {
                    // Check whether char that should reindent the line was inserted
                    checkIndentHotChars(target, cmd);

                    // Check the completion
                    checkCompletion(target, cmd);
                }
            }
        }

        /** Check the characters that should cause reindenting the line. */
        protected void checkIndentHotChars(JTextComponent target, String typedText) {
            BaseDocument doc = Utilities.getDocument(target);
            if (doc != null) {
                Caret caret = target.getCaret();
                Formatter f = doc.getFormatter();
                if (f instanceof ExtFormatter) {
                    ExtFormatter ef = (ExtFormatter)f;
                    int[] fmtBlk = ef.getReformatBlock(target, typedText);

                    if (fmtBlk != null) {
                        try {
                            fmtBlk[0] = Utilities.getRowStart(doc, fmtBlk[0]);
                            fmtBlk[1] = Utilities.getRowEnd(doc, fmtBlk[1]);

                            //this was the of #18922, that causes the bug #20198
                            //ef.reformat(doc, fmtBlk[0], fmtBlk[1]);

                            //bugfix of the bug #20198. Bug #18922 is fixed too as well as #6968
                            ef.reformat(doc, fmtBlk[0], fmtBlk[1], true);
                            
                        } catch (BadLocationException e) {
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }


        /** 
         * Check and possibly popup, hide or refresh the completion 
         * @deprecated Please use Editor Code Completion API instead, for details see
         *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
         */
        protected void checkCompletion(JTextComponent target, String typedText) {
// XXX: remove
//            Completion completion = ExtUtilities.getCompletion(target);
//
//            BaseDocument doc = (BaseDocument)target.getDocument();
//            ExtSyntaxSupport extSup = (ExtSyntaxSupport)doc.getSyntaxSupport();
//            
//            if (completion != null && typedText.length() > 0) {
//                if( !completion.isPaneVisible() ) {
//                    if (completion.isAutoPopupEnabled()) {
//                        int result = extSup.checkCompletion( target, typedText, false );
//                        if ( result == ExtSyntaxSupport.COMPLETION_POPUP ) {
//                            completion.popup(true);
//                        } else if ( result == ExtSyntaxSupport.COMPLETION_CANCEL ) {
//                            completion.cancelRequest();
//                        }
//                    }
//                } else {
//                    int result = extSup.checkCompletion( target, typedText, true );
//                    switch( result ) {
//                        case ExtSyntaxSupport.COMPLETION_HIDE:
//                            completion.setPaneVisible(false);
//                            break;
//                        case ExtSyntaxSupport.COMPLETION_REFRESH:
//                            completion.refresh(false);
//                            break;
//                        case ExtSyntaxSupport.COMPLETION_POST_REFRESH:
//                            completion.refresh(true);
//                            break;
//                    }
//                }
//            }
        }
    }

    /** 
     * @deprecated Please use Editor Code Completion API instead, for details see
     *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
     */
    @EditorActionRegistration(
            name = completionShowAction,
            shortDescription = editorBundleHash + completionShowAction
    )
    public static class CompletionShowAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =1050644925893851146L;

        public CompletionShowAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    /** 
     * @deprecated Please use Editor Code Completion API instead, for details see
     *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
     */
    @EditorActionRegistration(
            name = allCompletionShowAction,
            shortDescription = editorBundleHash + allCompletionShowAction
    )
    public static class AllCompletionShowAction extends BaseKitLocalizedAction {

        public AllCompletionShowAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    /** 
     * @deprecated Please use Editor Code Completion API instead, for details see
     *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
     */
    @EditorActionRegistration(
            name = documentationShowAction,
            shortDescription = editorBundleHash + documentationShowAction
    )
    public static class DocumentationShowAction extends BaseKitLocalizedAction {

        public DocumentationShowAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    /** 
     * @deprecated Please use Editor Code Completion API instead, for details see
     *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
     */
    @EditorActionRegistration(
            name = completionTooltipShowAction,
            shortDescription = editorBundleHash + completionTooltipShowAction
    )
    public static class CompletionTooltipShowAction extends BaseKitLocalizedAction {

        public CompletionTooltipShowAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

  public static class ExtDeleteCharAction extends DeleteCharAction {

    public ExtDeleteCharAction(String nm, boolean nextChar) {
      super(nm, nextChar);
    }
    
  }


}
