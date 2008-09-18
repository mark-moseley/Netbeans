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

package org.netbeans.editor;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.text.BadLocationException;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;

/**
* This is the parent of majority of the actions. It implements
* the necessary resetting depending of what is required
* by constructor of target action.
* The other thing implemented here is macro recording.
*
* @author Miloslav Metelka
* @version 1.00
*/

public abstract class BaseAction extends TextAction {

    /** Text of the menu item in popup menu for this action */
    public static final String POPUP_MENU_TEXT = "PopupMenuText"; // NOI18N

    /** Prefix for the name of the key for description in locale support */
    public static final String LOCALE_DESC_PREFIX = "desc-"; // NOI18N

    /** Prefix for the name of the key for popup description in locale support */
    public static final String LOCALE_POPUP_PREFIX = "popup-"; // NOI18N

    /** Resource for the icon */
    public static final String ICON_RESOURCE_PROPERTY = "IconResource"; // NOI18N

    /** Remove the selected text at the action begining */
    public static final int SELECTION_REMOVE = 1;

    /** Reset magic caret position */
    public static final int MAGIC_POSITION_RESET = 2;

    /** 
     * Reset abbreviation accounting to empty string.
     * @deprecated Not used anymore.
     */
    public static final int ABBREV_RESET = 4;

    /** Prevents adding the new undoable edit to the old one when the next
    * document change occurs.
    */
    public static final int UNDO_MERGE_RESET = 8;

    /** Reset word-match table */
    public static final int WORD_MATCH_RESET = 16;

    /** Clear status bar text */
    public static final int CLEAR_STATUS_TEXT = 32;

    /** The action will not be recorded if in macro recording */
    public static final int NO_RECORDING = 64;

    /** Save current position in the jump list */
    public static final int SAVE_POSITION = 128;

    /** The name of Action property. If the action has property NO_KEYBINDING set to true, it won't
     *  be listed in editor keybindings customizer list.
     */
    public static final String NO_KEYBINDING = "no-keybinding"; //NOI18N

    /** logger for reporting invoked actions */
    private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions.editor"); // NOI18N
    
    /** Bit mask of what should be updated when the action is performed before
    * the action's real task is invoked.
    */
    protected int updateMask;
    
    private static boolean recording;
    private static StringBuffer macroBuffer = new StringBuffer();
    private static StringBuffer textBuffer = new StringBuffer();

    static final long serialVersionUID =-4255521122272110786L;

    public BaseAction(String name) {
        this(name, 0);
    }

    public BaseAction(String name, int updateMask) {
        super(name);
        this.updateMask = updateMask;
    }
    
    /** Find a value in resource bundles.
     * @deprecated this method is deprecated like the LocaleSupport which it uses by default.
     *   It should be replaced by implementing {@link #getShortDescriptionBundleClass()}
     */
    protected Object findValue(String key){
        return LocaleSupport.getString(key);
    }
    
    public @Override Object getValue(String key){
        Object obj = super.getValue(key);

        if (obj == null){
            obj = createDefaultValue(key);
            if (obj != null) {
                putValue(key, obj);
            }
        }

        return obj;
    }
    
    /**
     * This method is called when there is no value for the particular key.
     * <br/>
     * If the returned value is non-null it is remembered
     * by {@link #putValue(String, Object)} so in that case this method
     * is only called once.
     *
     * <p>
     * <b>Note:</b> When overriding this method <code>super</code> implementation
     * should always be called.
     *
     * @param key key for which the default value should be found.
     * @return default value or null if the default value does not exist
     *  for the given key.
     */
    protected Object createDefaultValue(String key) {
        Object ret = null;
        if (SHORT_DESCRIPTION.equals(key)) {
            Class bundleClass = getShortDescriptionBundleClass();
            if (bundleClass != null) {
                // The bundle key is just the action's name
                String bundleKey = (String)getValue(Action.NAME);
                ret = NbBundle.getBundle(bundleClass).getString(bundleKey);
            } else { // default to slow deprecated findValue()
                // getDefaultShortDescription() is only called once for non-null ret value
                ret = getDefaultShortDescription();
            }

        } else if (POPUP_MENU_TEXT.equals(key)){
            String bundleKey = LOCALE_POPUP_PREFIX + getValue(Action.NAME);
            ret = findValue(bundleKey);
            if (ret == null){
                ret = getValue(SHORT_DESCRIPTION);
            }
        }
        return ret;
    }
    
    /**
     * Get the class in a package where resource bundle for localization
     * of the short description of this action resides.
     * <br/>
     * By default this method returns null.
     */
    protected Class getShortDescriptionBundleClass() {
        return null;
    }
    
    /**
     * Get the default value for {@link Action#SHORT_DESCRIPTION} property.
     * <br>
     * If this method returns non-empty value it will only be called once
     * (its result will be remembered).
     *
     * @return value that will be use as result for
     *  <code>Action.getValue(Action.SHORT_DESCRIPTION)</code>.
     */
    protected Object getDefaultShortDescription() {
        String actionName = (String)getValue(Action.NAME);
        String localizerKey = LOCALE_DESC_PREFIX + actionName;
        Object obj = findValue(localizerKey);
        if (obj==null){
            obj = findValue(actionName);
            if (obj==null) obj = actionName;
        }
        return obj;
    }

// XXX: remove
//    /** This method is called once after the action is constructed
//    * and then each time the settings are changed.
//    * @param evt event describing the changed setting name. It's null
//    *   if it's called after the action construction.
//    * @param kitClass class of the kit that created the actions
//    */
//    protected void settingsChange(SettingsChangeEvent evt, Class kitClass) {
//    }

    /** This method is made final here as there's an important
    * processing that must be done before the real action
    * functionality is performed. It can include the following:
    * 1. Updating of the target component depending on the update
    *    mask given in action constructor.
    * 2. Possible macro recoding when the macro recording
    *    is turned on.
    * The real action functionality should be done in
    * the method actionPerformed(ActionEvent evt, JTextComponent target)
    * which must be redefined by the target action.
    */
    public final void actionPerformed(final ActionEvent evt) {
        final JTextComponent target = getTextComponent(evt);

        // #146657 - Only perform the action if the document is BaseDocument's instance
        if (!(target.getDocument() instanceof BaseDocument)) {
            return;
        }
                              
        if( recording && 0 == (updateMask & NO_RECORDING) ) {
            recordAction( target, evt );
        }
        

        updateComponent(target);

        if (UILOG.isLoggable(Level.FINE)) {
            String actionName = getValue(NAME) != null ? getValue(NAME).toString().toLowerCase() : null;
            if (actionName != null &&
                !"default-typed".equals(actionName) && //NOI18N
                -1 == actionName.indexOf("caret") && //NOI18N
                -1 == actionName.indexOf("delete") && //NOI18N
                -1 == actionName.indexOf("selection") && //NOI18N
                -1 == actionName.indexOf("build-tool-tip") &&//NOI18N
                -1 == actionName.indexOf("build-popup-menu") &&//NOI18N
                -1 == actionName.indexOf("page-up") &&//NOI18N
                -1 == actionName.indexOf("page-down") &&//NOI18N
                -1 == actionName.indexOf("-kit-install") //NOI18N
            ) {
                LogRecord r = new LogRecord(Level.FINE, "UI_ACTION_EDITOR"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(BaseAction.class));
                if (evt != null) {
                    r.setParameters(new Object[] { evt, evt.toString(), this, toString(), getValue(NAME) });
                } else {
                    r.setParameters(new Object[] { "no-ActionEvent", "no-ActionEvent", this, toString(), getValue(NAME) }); //NOI18N
                }
                r.setLoggerName(UILOG.getName());
                UILOG.log(r);
            }
        }
        
        if (asynchonous()) {
            RequestProcessor.getDefault().post(new Runnable () {
                public void run() {
                    actionPerformed(evt, target);
                }
            });
        } else {
            actionPerformed(evt, target);
        }
    }
    
    private void recordAction( JTextComponent target, ActionEvent evt ) {
        if( this == target.getKeymap().getDefaultAction() ) { // defaultKeyTyped
            textBuffer.append( getFilteredActionCommand(evt.getActionCommand()) );
        } else { // regular action
            if( textBuffer.length() > 0 ) {
                if( macroBuffer.length() > 0 ) macroBuffer.append( ' ' ); 
                macroBuffer.append( encodeText( textBuffer.toString() ) );
                textBuffer.setLength( 0 );
            }
            if( macroBuffer.length() > 0 ) macroBuffer.append( ' ' ); 
            String name = (String)getValue( Action.NAME );
            macroBuffer.append( encodeActionName( name ) );
        }
    }
    
    private String getFilteredActionCommand(String cmd)
    {
        if (cmd == null || cmd.length() == 0)
            return "";
        char ch = cmd.charAt(0);
        if ((ch >= 0x20) && (ch != 0x7F))
            return cmd;
        else
            return "";
    }
    
    boolean startRecording( JTextComponent target ) {
        if( recording ) return false;
        recording = true;
        macroBuffer.setLength(0);
        textBuffer.setLength(0);
        Utilities.setStatusText( target,
                NbBundle.getBundle(BaseAction.class).getString( "macro-recording" ) );
        return true;
    }
    
    String stopRecording( JTextComponent target ) {
        if( !recording ) return null;

        if( textBuffer.length() > 0 ) {
            if( macroBuffer.length() > 0 ) macroBuffer.append( ' ' ); 
            macroBuffer.append( encodeText( textBuffer.toString() ) );
        }        
        String retVal = macroBuffer.toString();
        recording = false;
        Utilities.setStatusText( target, "" ); // NOI18N
        return retVal;
    }
    
    private String encodeText( String s ) {
        char[] text = s.toCharArray();
        StringBuffer encoded = new StringBuffer( "\""); // NOI18N
        for( int i=0; i < text.length; i++ ) {
            char c = text[i];
            if( c == '"' || c == '\\' ) encoded.append( '\\' );
            encoded.append( c );
        }
        return encoded.append( '"' ).toString();
    }

    private String encodeActionName( String s ) {
        char[] actionName = s.toCharArray();
        StringBuffer encoded = new StringBuffer();
        for( int i=0; i < actionName.length; i++ ) {
            char c = actionName[i];
            if( Character.isWhitespace( c ) || c == '\\' ) encoded.append( '\\' );
            encoded.append( c );
        }
        return encoded.toString();
    }
    
    /** The target method that performs the real action functionality.
    * @param evt action event describing the action that occured
    * @param target target component where the action occured. It's retrieved
    *   by the TextAction.getTextComponent(evt).
    */
    public abstract void actionPerformed(ActionEvent evt, JTextComponent target);

    protected boolean asynchonous() {
        return false;
    }

    public JMenuItem getPopupMenuItem(JTextComponent target) {
        return null;
    }

    public String getPopupMenuText(JTextComponent target) {
        String txt = (String)getValue(POPUP_MENU_TEXT);
        if (txt == null) {
            txt = (String)getValue(NAME);
        }
        return txt;
    }

    /** Update the component according to the update mask specified
    * in the constructor of the action.
    * @param target target component to be updated.
    */
    public void updateComponent(JTextComponent target) {
        updateComponent(target, this.updateMask);
    }

    /** Update the component according to the given update mask
    * @param target target component to be updated.
    * @param updateMask mask that specifies what will be updated
    */
    public void updateComponent(JTextComponent target, int updateMask) {
        if (target != null && target.getDocument() instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            boolean writeLocked = false;

            try {
                // remove selected text
                if ((updateMask & SELECTION_REMOVE) != 0) {
                    writeLocked = true;
                    doc.extWriteLock();
                    Caret caret = target.getCaret();
                    if (caret != null && Utilities.isSelectionShowing(caret)) {
                        int dot = caret.getDot();
                        int markPos = caret.getMark();
                        if (dot < markPos) { // swap positions
                            int tmpPos = dot;
                            dot = markPos;
                            markPos = tmpPos;
                        }
                        try {
                            target.getDocument().remove(markPos, dot - markPos);
                        } catch (BadLocationException e) {
                            Utilities.annotateLoggable(e);
                        }
                    }
                }

                // reset magic caret position
                if ((updateMask & MAGIC_POSITION_RESET) != 0) {
                    if (target.getCaret() != null)
                        target.getCaret().setMagicCaretPosition(null);
                }

                // reset merging of undoable edits
                if ((updateMask & UNDO_MERGE_RESET) != 0) {
                    doc.resetUndoMerge();
                }

                // reset word matching
                if ((updateMask & WORD_MATCH_RESET) != 0) {
                    ((BaseTextUI)target.getUI()).getEditorUI().getWordMatch().clear();
                }

                // Clear status bar text
                if (!recording && (updateMask & CLEAR_STATUS_TEXT) != 0) {
                    Utilities.clearStatusText(target);
                }

                // Save current caret position in the jump-list
                if ((updateMask & SAVE_POSITION) != 0) {
                    JumpList.checkAddEntry(target);
                }

            } finally {
                if (writeLocked) {
                    doc.extWriteUnlock();
                }
            }
        }
    }

}
