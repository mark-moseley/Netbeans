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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.util.Map;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.ext.ExtSettingsNames;

/** Extended settings for CC */ 
public class CCSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "cc-settings-initializer";  //NOI18N

    private Class ccKitClass;

    /**
     * Construct new cc-settings-initializer.
     * @param ccKitClass the real kit class for which the settings are created.
     *   It's unknown here so it must be passed to this constructor.
     */
    public CCSettingsInitializer(Class ccKitClass) {
        super(NAME);
        this.ccKitClass = ccKitClass;
    }

    /**
     *  Update map filled with the settings.
     *
     *  @param kitClass kit class for which the settings are being updated. It is always non-null value.
     *  @param settingsMap map holding [setting-name, setting-value] pairs.
     *   The map can be empty if this is the first initializer
     *   that updates it or if no previous initializers updated it.
     */
    @SuppressWarnings("unchecked")
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        
        if (kitClass == ccKitClass) {
            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] { CCTokenContext.context });

            settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE,
                            CCSettingsDefaults.defaultCaretSimpleMatchBrace);

            settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE,
                            CCSettingsDefaults.defaultHighlightMatchBrace);

            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                            CCSettingsDefaults.defaultCCIdentifierAcceptor);

            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR,
                            CCSettingsDefaults.defaultAbbrevResetAcceptor);

            settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE,
                            CCSettingsDefaults.defaultWordMatchMatchCase);

            settingsMap.put(SettingsNames.WORD_MATCH_STATIC_WORDS,
                            CCSettingsDefaults.defaultWordMatchStaticWords);

            // Formatting settings
            settingsMap.put(CCSettingsNames.CC_FORMAT_SPACE_BEFORE_PARENTHESIS,
                            CCSettingsDefaults.defaultCCFormatSpaceBeforeParenthesis);

            settingsMap.put(CCSettingsNames.CC_FORMAT_SPACE_AFTER_COMMA,
                            CCSettingsDefaults.defaultCCFormatSpaceAfterComma);

            settingsMap.put(CCSettingsNames.CC_FORMAT_NEWLINE_BEFORE_BRACE,
                            CCSettingsDefaults.defaultCCFormatNewlineBeforeBrace);

            settingsMap.put(CCSettingsNames.CC_FORMAT_NEWLINE_BEFORE_BRACE_DECLARATION,
                            CCSettingsDefaults.defaultCCFormatNewlineBeforeBraceDeclaration);

            settingsMap.put(CCSettingsNames.CC_FORMAT_LEADING_SPACE_IN_COMMENT,
                            CCSettingsDefaults.defaultCCFormatLeadingSpaceInComment);

            settingsMap.put(CCSettingsNames.CC_FORMAT_LEADING_STAR_IN_COMMENT,
                            CCSettingsDefaults.defaultCCFormatLeadingStarInComment);

            settingsMap.put(CCSettingsNames.INDENT_HOT_CHARS_ACCEPTOR,
                            CCSettingsDefaults.defaultIndentHotCharsAcceptor);

	    // Code folding settings
	    settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, CCSettingsDefaults.defaultCCCodeFoldingEnable);
            
	    settingsMap.put(SettingsNames.PAIR_CHARACTERS_COMPLETION,
			    CCSettingsDefaults.defaultPairCharactersCompletion);
            
            settingsMap.put(ExtSettingsNames.JAVADOC_AUTO_POPUP,
                            CCSettingsDefaults.defaultCCDocAutoPopup);
        }
    }
}
 
