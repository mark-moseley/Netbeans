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

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.text.Keymap;
import javax.swing.text.JTextComponent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.KeyStroke;
import javax.swing.Action;
import javax.swing.AbstractAction;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;

/**
* Keymap that is capable to work with MultiKeyBindings
*
* @author Miloslav Metelka
* @version 0.10
*/

public class MultiKeymap implements Keymap {
    
    private static final boolean compatibleIgnoreNextTyped
        = Boolean.getBoolean("netbeans.editor.keymap.compatible");

    /** Action that does nothing */
    public static final Action EMPTY_ACTION = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
        }
    };

    /** Action that beeps. Used for wrong shortcut by default */
    public static final Action BEEP_ACTION = new DefaultEditorKit.BeepAction();

    /** JTextComponent.DefaultKeymap to be used for processing by this keymap */
    private Keymap delegate;

    /** Context keymap or null for base context */
    private Keymap context;

    /** Ignore possible keyTyped events after context reset */
    private boolean ignoreNextTyped = false;

    /** Action to return when there's no action for incoming key
    * in some context. This action doesn't occur when no action
    * is found in base context.
    */
    private Action contextKeyNotFoundAction = BEEP_ACTION;
    
    /**
     * List of key strokes that form the present context.
     * If this list differs from the global context maintained in the status displayer
     * then the keymap must be reset and attempted to be put
     * into the global context before attempting to process the given keystroke.
     * If the keymap cannot be put into such a context then
     * it returns null action for the given keystroke.
     */
    private List contextKeys;

    /** Construct new keymap.
    * @param name name of new keymap
    */
    public MultiKeymap(String name) {
        delegate = JTextComponent.addKeymap(name, null);
        contextKeys = new ArrayList();
    }

    /** Set the context keymap */
    void setContext(Keymap contextKeymap) {
        context = contextKeymap;
    }

    private static String getKeyText (KeyStroke keyStroke) {
        if (keyStroke == null) return "";                       // NOI18N
        String modifText = KeyEvent.getKeyModifiersText 
            (keyStroke.getModifiers ());
        if ("".equals (modifText))                              // NOI18N   
            return KeyEvent.getKeyText (keyStroke.getKeyCode ());
        return modifText + "+" +                                // NOI18N
            KeyEvent.getKeyText (keyStroke.getKeyCode ()); 
    }
            
    /** Reset keymap to base context */
    public void resetContext() {
        context = null;
        contextKeys.clear();
    }
    
    /**
     * Add a context key to the global context maintained by the NbKeymap. 
     *
     * @param key a key to be added to the global context.
     */
    private void shiftGlobalContext(KeyStroke key) {
        List globalContextList = getGlobalContextList();
        if (globalContextList != null) {
            globalContextList.add(key);
            
            StringBuffer text = new StringBuffer();
            for (Iterator it = globalContextList.iterator(); it.hasNext();) {
                text.append(getKeyText((KeyStroke)it.next())).append(' ');
            }
            StatusDisplayer.getDefault().setStatusText(text.toString());        
        }
        // Shift the locally maintained mirror context as well
        contextKeys.add(key);
    }

    /**
     * Reset the global context in case there is a reason for it.
     */
    private void resetGlobalContext() {
        List globalContextList = getGlobalContextList();
        if (globalContextList != null) {
            globalContextList.clear();
            StatusDisplayer.getDefault().setStatusText("");
        }
    }
    
    private List getGlobalContextList() {
        // Retrieve the list from NbKeymap by reflection
        // Get system classloader
        List globalContextList;
        try {
            ClassLoader sysCL = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            Class nbKeymapClass = Class.forName("org.netbeans.core.NbKeymap", true, sysCL); // NOI18N
            java.lang.reflect.Field contextField = nbKeymapClass.getDeclaredField("context"); // NOI18N
            contextField.setAccessible(true);
            globalContextList = (List)contextField.get(null);
        } catch (Exception e) {
            // Ignore the exception
            globalContextList = null;
        }
        return globalContextList;
    }

    /** What to do when key is not resolved for context */
    public void setContextKeyNotFoundAction(Action a) {
        contextKeyNotFoundAction = a;
    }

    /** Loads the key to action mappings into this keymap in similar way
    * as JTextComponent.loadKeymap() does. This method is able to handle
    * MultiKeyBindings but for compatibility it expects
    * JTextComponent.KeyBinding array.
    */
    public void load(JTextComponent.KeyBinding[] bindings, Action[] actions) {
        Map h = new HashMap(bindings.length);
        // add actions to map to resolve by names quickly
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            String value = (String)a.getValue(Action.NAME);
            h.put((value != null ? value : ""), a); // NOI18N
        }
        load(bindings, h);
    }

    /** Loads key to action mappings into this keymap
    * @param bindings array of bindings
    * @param actions map of [action_name, action] pairs
    */
    public void load(JTextComponent.KeyBinding[] bindings, Map actions) {
        // now create bindings in keymap(s)
        for (int i = 0; i < bindings.length; i++) {
            Action a = (Action)actions.get(bindings[i].actionName);
            if (a != null) {
                boolean added = false;
                if (bindings[i] instanceof MultiKeyBinding) {
                    MultiKeyBinding mb = (MultiKeyBinding)bindings[i];
                    if (mb.keys != null) {
                        Keymap cur = delegate;
                        for (int j = 0; j < mb.keys.length; j++) {
                            if (j == mb.keys.length - 1) { // last keystroke in sequence
                                cur.addActionForKeyStroke(mb.keys[j], a);
                            } else { // not the last keystroke
                                Action sca = cur.getAction(mb.keys[j]);
                                if (!(sca instanceof KeymapSetContextAction)) {
                                    sca = new KeymapSetContextAction(JTextComponent.addKeymap(null, null));
                                    cur.addActionForKeyStroke(mb.keys[j], sca);
                                }
                                cur = ((KeymapSetContextAction)sca).contextKeymap;
                            }
                        }
                        added = true;
                    }
                }
                if (!added) {
                    if (bindings[i].key != null) {
                        delegate.addActionForKeyStroke(bindings[i].key, a);
                    } else { // key is null -> set default action
                        setDefaultAction(a);
                    }
                }
            }
        }
    }

    public String getName() {
        return (context != null) ? context.getName()
               : delegate.getName();
    }

    /** Get default action of this keymap or parent keymap if this
    * one doesn't have one. Context keymap can have default action
    * but it will be not used.
    */
    public Action getDefaultAction() {
        return delegate.getDefaultAction();
    }

    public void setDefaultAction(Action a) {
        if (context != null) {
            context.setDefaultAction(a);
        } else {
            delegate.setDefaultAction(a);
        }
    }

    private Action getActionImpl(KeyStroke key) {
        Action a = null;
        if (context != null) {
            a = context.getAction(key);
            // Commented out the next part to allow the other
            // keystroke processors to work when the editor does not have an action
            // for the particular keystroke.
/*            if (a == null) { // possibly ignore modifier keystrokes
                switch (key.getKeyCode()) {
                case KeyEvent.VK_SHIFT:
                case KeyEvent.VK_CONTROL:
                case KeyEvent.VK_ALT:
                case KeyEvent.VK_META:
                    return EMPTY_ACTION;
                }
                if (key.isOnKeyRelease()
                    || (key.getKeyChar() != 0 && key.getKeyChar() != KeyEvent.CHAR_UNDEFINED)
                ) {
                    return EMPTY_ACTION; // ignore releasing and typed events
                }
            }
 */
        } else {
            a = delegate.getAction(key);
        }

        return a;
    }
    
    private boolean contextKeysEqual(List keys) {
        if (keys.size() != contextKeys.size()) {
            return false;
        }
        for (int i = keys.size() - 1; i >= 0; i--) {
            if (!contextKeys.get(i).equals(keys.get(i))) {
                return false;
            }
        }
        return true;
    }

    public Action getAction(KeyStroke key) {
        Action ret = null;

        // Check whether the context in status displayer corresponds to the keymap's context
        // If there would be a non-empty SD context that differs from the editor's one
        // then do not return any action for this keystroke.
        List globalContextList = getGlobalContextList();
        if (globalContextList != null && globalContextList.size() > 0 && !contextKeysEqual(globalContextList)) {
            resetContext();
            int i;
            for (i = 0; i < globalContextList.size(); i++) {
                Action a = getActionImpl((KeyStroke)globalContextList.get(i));
                if (a instanceof KeymapSetContextAction) {
                    a.actionPerformed(null);
                } else {
                    // no multi-keystrokes for such context in editor
                    resetContext();
                    break;
                }
            }
            if (i != globalContextList.size()) { // unsuccessful context switch
                return null;
            }
        }
        
        // Explicit patches of the keyboard problems
        if (ignoreNextTyped) {
            if (key.isOnKeyRelease()) { // ignore releasing here
                ret = EMPTY_ACTION;
            } else { // either pressed or typed
                ignoreNextTyped = false;
            }
            if (key.getKeyChar() != 0 && key.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                ret = EMPTY_ACTION; // prevent using defaultAction
            }
        }

        if (ret == null) {
            ret = getActionImpl(key);
            if (ret instanceof KeymapSetContextAction) { // 
                // Mark the context shifting
                shiftGlobalContext(key);

            } else { // not a context shift action
                if (context != null) { // Already in a non-empty context
                    ignoreNextTyped = true;

                } else if (compatibleIgnoreNextTyped) {
                    // #44307 = disabled extra ignoreNextTyped patches for past JDKs
                    if ( // Explicit patch for the keyTyped sent after Alt+key
                        (key.getModifiers() & InputEvent.ALT_MASK) != 0 // Alt pressed
                        && (key.getModifiers() & InputEvent.CTRL_MASK) == 0 // Ctrl not pressed
                    ) {
                        boolean patch = true;
                        if (key.getKeyChar() == 0 || key.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
                            switch (key.getKeyCode()) {
                            case KeyEvent.VK_ALT: // don't patch single Alt
                            case KeyEvent.VK_KANJI:
                            case KeyEvent.VK_KATAKANA:
                            case KeyEvent.VK_HIRAGANA:
                            case KeyEvent.VK_JAPANESE_KATAKANA:
                            case KeyEvent.VK_JAPANESE_HIRAGANA:
                            case 0x0107: // KeyEvent.VK_INPUT_METHOD_ON_OFF: - in 1.3 only
                            case KeyEvent.VK_NUMPAD0: // Alt+NumPad keys
                            case KeyEvent.VK_NUMPAD1:
                            case KeyEvent.VK_NUMPAD2:
                            case KeyEvent.VK_NUMPAD3:
                            case KeyEvent.VK_NUMPAD4:
                            case KeyEvent.VK_NUMPAD5:
                            case KeyEvent.VK_NUMPAD6:
                            case KeyEvent.VK_NUMPAD7:
                            case KeyEvent.VK_NUMPAD8:
                            case KeyEvent.VK_NUMPAD9:
                                patch = false;
                                break;
                            }
                        }

                        if (patch) {
                           ignoreNextTyped = true;
                        }
                    } else if ((key.getModifiers() & InputEvent.META_MASK) != 0) { // Explicit patch for the keyTyped sent after Meta+key for Mac OS X
                        ignoreNextTyped = true;
                    } else if ((key.getModifiers() & InputEvent.CTRL_MASK) != 0 &&
                               (key.getModifiers() & InputEvent.SHIFT_MASK) != 0 &&
                               (key.getKeyCode() == KeyEvent.VK_ADD || key.getKeyCode() == KeyEvent.VK_SUBTRACT)) {
                        // Explicit patch for keyTyped sent without the Ctrl+Shift modifiers on Mac OS X - see issue #39835
                        ignoreNextTyped = true;
                    }
                }

                resetContext(); // reset context when resolved
                // The global context cannot be reset because
                // this is just a situation when the editor keymap
                // does not know but the system or other 
            }

            if (context != null && ret == null) { // no action found when in context
                // Letting to return null in order to give chance to other keymaps
                // ret = contextKeyNotFoundAction;
            }
        }
        
        // Reset global context if a valid action is found
        if (ret != null && !(ret instanceof KeymapSetContextAction) && (ret != EMPTY_ACTION)) {
            resetGlobalContext();
        }

        if (compatibleIgnoreNextTyped) {
            // #44307 = disabled extra ignoreNextTyped patches for past JDKs
            // Explicit patch for Ctrl+Space - eliminating the additional KEY_TYPED sent
            if (key == KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK)) {
                ignoreNextTyped = true;
            }
        }

/*            System.out.println("key=" + key + ", keyChar=" + (int)key.getKeyChar() + ", keyCode=" + key.getKeyCode() + ", keyModifiers=" + key.getModifiers() // NOI18N
                + ", ignoreNextTyped=" + ignoreNextTyped + ", context=" + context // NOI18N
                + ", returning action=" + ((ret == EMPTY_ACTION) ? "EMPTY_ACTION" : ((ret == null) ? "null" : ((ret instanceof javax.swing.text.TextAction) // NOI18N
                    ? ret.getValue(javax.swing.Action.NAME) : ret.getClass()))));
*/
                    
        return ret;
    }

    public KeyStroke[] getBoundKeyStrokes() {
        return (context != null) ? context.getBoundKeyStrokes()
               : delegate.getBoundKeyStrokes();
    }

    public Action[] getBoundActions() {
        return (context != null) ? context.getBoundActions()
               : delegate.getBoundActions();
    }

    public KeyStroke[] getKeyStrokesForAction(Action a) {
        return (context != null) ? context.getKeyStrokesForAction(a)
               : delegate.getKeyStrokesForAction(a);
    }

    public boolean isLocallyDefined(KeyStroke key) {
        return (context != null) ? context.isLocallyDefined(key)
               : delegate.isLocallyDefined(key);
    }

    public void addActionForKeyStroke(KeyStroke key, Action a) {
        if (context != null) {
            context.addActionForKeyStroke(key, a);
        } else {
            delegate.addActionForKeyStroke(key, a);
        }
    }

    public void removeKeyStrokeBinding(KeyStroke key) {
        if (context != null) {
            context.removeKeyStrokeBinding(key);
        } else {
            delegate.removeKeyStrokeBinding(key);
        }
    }

    public void removeBindings() {
        if (context != null) {
            context.removeBindings();
        } else {
            delegate.removeBindings();
        }
    }

    public Keymap getResolveParent() {
        return (context != null) ? context.getResolveParent()
               : delegate.getResolveParent();
    }

    public void setResolveParent(Keymap parent) {
        if (context != null) {
            context.setResolveParent(parent);
        } else {
            delegate.setResolveParent(parent);
        }
    }

    public String toString() {
        return "MK: name=" + getName(); // NOI18N
    }

    /** Internal class used to set the context */
    class KeymapSetContextAction extends AbstractAction {

        Keymap contextKeymap;

        static final long serialVersionUID =1034848289049566148L;

        KeymapSetContextAction(Keymap contextKeymap) {
            this.contextKeymap = contextKeymap;
        }

        public void actionPerformed(ActionEvent evt) {
            setContext(contextKeymap);
        }

    }

}
