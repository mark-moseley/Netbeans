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

import java.util.Map;

/**
* Initializer for the editor settings. This initializer contains
* the default values for the majority of the base-level settings.
* The constants defined here are used also internally
* as the default values for the editor in cases
* the user enters an invalid value into some setting that
* would break the editor functionality.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "base-settings-initializer"; // NOI18N

    public BaseSettingsInitializer() {
        super(NAME);
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
// XXX: remove
//
//        // ------------------------ BaseKit Settings --------------------------------------
//        if (kitClass == BaseKit.class) {
//            settingsMap.put(SettingsNames.TAB_SIZE, SettingsDefaults.defaultTabSize);
//            settingsMap.put(SettingsNames.EXPAND_TABS, SettingsDefaults.defaultExpandTabs);
//            settingsMap.put(SettingsNames.SPACES_PER_TAB, SettingsDefaults.defaultSpacesPerTab);
//
//            settingsMap.put(SettingsNames.CARET_TYPE_INSERT_MODE, SettingsDefaults.defaultCaretTypeInsertMode);
//            settingsMap.put(SettingsNames.CARET_TYPE_OVERWRITE_MODE, SettingsDefaults.defaultCaretTypeOverwriteMode);
//            settingsMap.put(SettingsNames.CARET_ITALIC_INSERT_MODE, SettingsDefaults.defaultCaretItalicInsertMode);
//            settingsMap.put(SettingsNames.CARET_ITALIC_OVERWRITE_MODE, SettingsDefaults.defaultCaretItalicOverwriteMode);
//            settingsMap.put(SettingsNames.CARET_COLOR_INSERT_MODE, SettingsDefaults.defaultCaretColorInsertMode);
//            settingsMap.put(SettingsNames.CARET_COLOR_OVERWRITE_MODE, SettingsDefaults.defaultCaretColorOvwerwriteMode);
//            settingsMap.put(SettingsNames.CARET_BLINK_RATE, SettingsDefaults.defaultCaretBlinkRate);
//
//            settingsMap.put(SettingsNames.MACRO_MAP, SettingsDefaults.defaultMacroMap );
//            settingsMap.put(SettingsNames.ABBREV_MAP, SettingsDefaults.defaultAbbrevMap );
//            settingsMap.put(SettingsNames.ABBREV_EXPAND_ACCEPTOR, SettingsDefaults.defaultAbbrevExpandAcceptor);
//            settingsMap.put(SettingsNames.ABBREV_ADD_TYPED_CHAR_ACCEPTOR, SettingsDefaults.defaultAbbrevAddTypedCharAcceptor);
//            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR, SettingsDefaults.defaultAbbrevResetAcceptor);
//
//            settingsMap.put(SettingsNames.LINE_NUMBER_VISIBLE, SettingsDefaults.defaultLineNumberVisible);
//
//            settingsMap.put(SettingsNames.LINE_HEIGHT_CORRECTION, SettingsDefaults.defaultLineHeightCorrection);
//            settingsMap.put(SettingsNames.TEXT_LEFT_MARGIN_WIDTH, SettingsDefaults.defaultTextLeftMarginWidth);
//            settingsMap.put(SettingsNames.MARGIN, SettingsDefaults.defaultMargin);
//            settingsMap.put(SettingsNames.SCROLL_JUMP_INSETS, SettingsDefaults.defaultScrollJumpInsets);
//            settingsMap.put(SettingsNames.SCROLL_FIND_INSETS, SettingsDefaults.defaultScrollFindInsets);
//            settingsMap.put(SettingsNames.COMPONENT_SIZE_INCREMENT, SettingsDefaults.defaultComponentSizeIncrement);
//
//            settingsMap.put(SettingsNames.STATUS_BAR_VISIBLE, SettingsDefaults.defaultStatusBarVisible);
//            settingsMap.put(SettingsNames.STATUS_BAR_CARET_DELAY, SettingsDefaults.defaultStatusBarCaretDelay);
//
//            settingsMap.put(SettingsNames.TEXT_LIMIT_LINE_VISIBLE, SettingsDefaults.defaultTextLimitLineVisible);
//            settingsMap.put(SettingsNames.TEXT_LIMIT_LINE_COLOR, SettingsDefaults.defaultTextLimitLineColor);
//            settingsMap.put(SettingsNames.TEXT_LIMIT_WIDTH, SettingsDefaults.defaultTextLimitWidth);
//
//            settingsMap.put(SettingsNames.HOME_KEY_COLUMN_ONE, SettingsDefaults.defaultHomeKeyColumnOne);
//            settingsMap.put(SettingsNames.WORD_MOVE_NEWLINE_STOP, SettingsDefaults.defaultWordMoveNewlineStop);
//            settingsMap.put(SettingsNames.INPUT_METHODS_ENABLED, SettingsDefaults.defaultInputMethodsEnabled);
//            settingsMap.put(SettingsNames.FIND_HIGHLIGHT_SEARCH, SettingsDefaults.defaultFindHighlightSearch);
//            settingsMap.put(SettingsNames.FIND_INC_SEARCH, SettingsDefaults.defaultFindIncSearch);
//            settingsMap.put(SettingsNames.FIND_BACKWARD_SEARCH, SettingsDefaults.defaultFindBackwardSearch);
//            settingsMap.put(SettingsNames.FIND_WRAP_SEARCH, SettingsDefaults.defaultFindWrapSearch);
//            settingsMap.put(SettingsNames.FIND_MATCH_CASE, SettingsDefaults.defaultFindMatchCase);
//            settingsMap.put(SettingsNames.FIND_WHOLE_WORDS, SettingsDefaults.defaultFindWholeWords);
//            settingsMap.put(SettingsNames.FIND_REG_EXP, SettingsDefaults.defaultFindRegExp);
//            settingsMap.put(SettingsNames.FIND_HISTORY_SIZE, SettingsDefaults.defaultFindHistorySize);
//            settingsMap.put(SettingsNames.WORD_MATCH_SEARCH_LEN, SettingsDefaults.defaultWordMatchSearchLen);
//            settingsMap.put(SettingsNames.WORD_MATCH_WRAP_SEARCH, SettingsDefaults.defaultWordMatchWrapSearch);
//            settingsMap.put(SettingsNames.WORD_MATCH_MATCH_ONE_CHAR, SettingsDefaults.defaultWordMatchMatchOneChar);
//            settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE, SettingsDefaults.defaultWordMatchMatchCase);
//            settingsMap.put(SettingsNames.WORD_MATCH_SMART_CASE, SettingsDefaults.defaultWordMatchSmartCase);
//            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR, SettingsDefaults.defaultIdentifierAcceptor);
//            settingsMap.put(SettingsNames.WHITESPACE_ACCEPTOR, SettingsDefaults.defaultWhitespaceAcceptor);
//            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, SettingsDefaults.defaultCodeFoldingEnable);
//            
//            // Base key-bindings
////            SettingsUtil.updateListSetting(settingsMap,
////                                           SettingsNames.KEY_BINDING_LIST, SettingsDefaults.defaultKeyBindings);
//
//            // Base colorings
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.DEFAULT_COLORING, SettingsDefaults.defaultColoring);
//
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.LINE_NUMBER_COLORING, SettingsDefaults.defaultLineNumberColoring);
//            
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.GUARDED_COLORING, SettingsDefaults.defaultGuardedColoring);
//            
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.CODE_FOLDING_COLORING, SettingsDefaults.defaultCodeFoldingColoring);
//
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.CODE_FOLDING_BAR_COLORING, SettingsDefaults.defaultCodeFoldingBarColoring);
//            
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.SELECTION_COLORING, SettingsDefaults.defaultSelectionColoring);
//
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.HIGHLIGHT_SEARCH_COLORING, SettingsDefaults.defaultHighlightSearchColoring);
//
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.INC_SEARCH_COLORING, SettingsDefaults.defaultIncSearchColoring);
//
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.BLOCK_SEARCH_COLORING, SettingsDefaults.defaultblockSearchColoring);
//
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.STATUS_BAR_COLORING, SettingsDefaults.defaultStatusBarColoring);
//
//            SettingsUtil.setColoring(settingsMap,
//                                     SettingsNames.STATUS_BAR_BOLD_COLORING, SettingsDefaults.defaultStatusBarBoldColoring);
//
//            // List of the colorings for all the kits
//            SettingsUtil.updateListSetting(settingsMap,
//                                           SettingsNames.COLORING_NAME_LIST, SettingsDefaults.defaultColoringNames);
//
///*          This was removed because document now uses this algorithm by default.
// *          Providing an explicit value for the setting means that the default
// *          algorithm should be overriden. Although the substituter below seems OK
// *          it doesn't reflect possible custom indent engines that can be provided by the IDE.
//
//
//            settingsMap.put(SettingsNames.INDENT_SHIFT_WIDTH, // substitute
//                            new Settings.Evaluator() {
//                                public Object getValue(Class kitClass2, String settingName) {
//                                    Object ret;
//                                    if (SettingsUtil.getBoolean(kitClass2, SettingsNames.EXPAND_TABS, false)) {
//                                        ret = Settings.getValue(kitClass2, SettingsNames.SPACES_PER_TAB);
//                                    } else { // don't expand tabs
//                                        ret = Settings.getValue(kitClass2, SettingsNames.TAB_SIZE);
//                                    }
//                                    return ret;
//                                }
//                            }
//                           );
// */
//
//            /* WARNING!
//            * The following settings should be changed with care as there are some
//            * dependencies among the values of these settings. If the values are wrong
//            * the editor may work in a wrong way in some circumstances.
//            * The EditorDebug.checkSettings(kitClass) should be run
//            * in some DOC_INSTALL_ACTION_LIST action to ensure that
//            * the changed values are correct.
//            */
//            settingsMap.put(SettingsNames.READ_BUFFER_SIZE, SettingsDefaults.defaultReadBufferSize);
//            settingsMap.put(SettingsNames.WRITE_BUFFER_SIZE, SettingsDefaults.defaultWriteBufferSize);
//            settingsMap.put(SettingsNames.READ_MARK_DISTANCE, SettingsDefaults.defaultReadMarkDistance);
//            settingsMap.put(SettingsNames.MARK_DISTANCE, SettingsDefaults.defaultMarkDistance);
//            settingsMap.put(SettingsNames.MAX_MARK_DISTANCE, SettingsDefaults.defaultMaxMarkDistance);
//            settingsMap.put(SettingsNames.MIN_MARK_DISTANCE, SettingsDefaults.defaultMinMarkDistance);
//            settingsMap.put(SettingsNames.SYNTAX_UPDATE_BATCH_SIZE, SettingsDefaults.defaultSyntaxUpdateBatchSize);
//            settingsMap.put(SettingsNames.LINE_BATCH_SIZE, SettingsDefaults.defaultLineBatchSize);
//
//        }
    }

}
