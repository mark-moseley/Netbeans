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

package org.netbeans.lib.editor.codetemplates;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.TextAction;
import org.netbeans.editor.BaseKit;

/**
 * Code template allows the client to paste itself into the given
 * text component.
 *
 * @author Miloslav Metelka
 */
final class CodeTemplateOverrideAction extends TextAction {

    /**
     * Action property that gets filled by the original action in the action map
     * before the custom actions get installed.
     */
    public static final String ORIGINAL_ACTION_PROPERTY = "original-action"; // NOI18N

    private static final int DEFAULT_KEY_TYPED = 0;
    private static final int TAB = 1;
    private static final int SHIFT_TAB = 2;
    private static final int ENTER = 3;
    private static final int UNDO = 4;
    private static final int REDO = 5;

    public static ActionMap installOverrideActionMap(JComponent component,
    CodeTemplateInsertHandler handler) {

        ActionMap origActionMap = component.getActionMap();
        ActionMap actionMap = new ActionMap() {
            public Action get(Object key) {

                Action retValue;
                
                retValue = super.get(key);
                return retValue;
            }
            
        };
        CodeTemplateOverrideAction[] actions = new CodeTemplateOverrideAction[] {
            new CodeTemplateOverrideAction(handler, DEFAULT_KEY_TYPED),
            new CodeTemplateOverrideAction(handler, TAB),
            new CodeTemplateOverrideAction(handler, SHIFT_TAB),
            new CodeTemplateOverrideAction(handler, ENTER),
//            new CodeTemplateOverrideAction(handler, UNDO),
//            new CodeTemplateOverrideAction(handler, REDO),
        };
        
        // Install the actions into new action map
        for (int i = actions.length - 1; i >= 0; i--) {
            CodeTemplateOverrideAction action = actions[i];
            Object actionKey = (String)action.getValue(Action.NAME);
            assert (actionKey != null);
            // Translate to the real key in the action map
            actionKey = action.findActionKey(component);
            if (actionKey != null) { // == null may happen during unit tests
                Action origAction = origActionMap.get(actionKey);
                action.putValue(ORIGINAL_ACTION_PROPERTY, origAction);
                actionMap.put(actionKey, action);
            }
        }
        
        actionMap.setParent(origActionMap);
        
        // Install the new action map and return the original action map
        component.setActionMap(actionMap);
        return origActionMap;
    }
    
    private static String actionType2Name(int actionType) {
        switch (actionType) {
            case DEFAULT_KEY_TYPED:
                return DefaultEditorKit.defaultKeyTypedAction;
            case TAB:
                return BaseKit.insertTabAction;
            case SHIFT_TAB:
                return BaseKit.removeTabAction;
            case ENTER:
                return DefaultEditorKit.insertBreakAction;
            case UNDO:
                return BaseKit.undoAction;
            case REDO:
                return BaseKit.redoAction;
            default:
                throw new IllegalArgumentException();
        }
    }
    
    
    private final CodeTemplateInsertHandler handler;
    
    private final int actionType;
    
    private CodeTemplateOverrideAction(CodeTemplateInsertHandler handler, int actionType) {
        super(actionType2Name(actionType));
        this.handler = handler;
        this.actionType = actionType;
    }
    
    private Action getOrigAction() {
        return (Action)getValue(ORIGINAL_ACTION_PROPERTY);
    }
    
    public void actionPerformed(ActionEvent evt) {
        switch (actionType) {
            case DEFAULT_KEY_TYPED:
                handler.defaultKeyTypedAction(evt, getOrigAction());
                break;
            case TAB:
                handler.tabAction(evt, getOrigAction());
                break;
            case SHIFT_TAB:
                handler.shiftTabAction(evt);
                break;
            case ENTER:
                handler.enterAction(evt);
                break;
            case UNDO:
                handler.undoAction(evt);
                break;
            case REDO:
                handler.redoAction(evt);
                break;
        }
    }

    Object findActionKey(JComponent component) {
        KeyStroke keyStroke;
        switch (actionType) {
            case DEFAULT_KEY_TYPED:
                keyStroke = KeyStroke.getKeyStroke('a');
                break;
            case TAB:
                keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
                break;
            case SHIFT_TAB:
                keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK);
                break;
            case ENTER:
                keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                break;
            case UNDO:
                keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK);
                break;
            case REDO:
                keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK);
                break;
            default:
                throw new IllegalArgumentException();
        }
        // Assume the 'a' character will trigger defaultKeyTypedAction
        Object key = component.getInputMap().get(keyStroke);
        return key;
    }

}
