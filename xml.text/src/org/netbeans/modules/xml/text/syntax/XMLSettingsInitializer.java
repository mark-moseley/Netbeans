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
package org.netbeans.modules.xml.text.syntax;

import java.util.*;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.TokenContext;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;

/**
 * Editor settings defaults.
 * It shoudl be replaced by layer based "Defaults" to simplify
 * {@link TextEditorModuleInstall}.
 */
public class XMLSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "xml-settings-initializer"; // NOI18N

    public XMLSettingsInitializer() {
        super(NAME);
    }

    public void updateSettingsMap (Class kitClass, Map settingsMap) {
        // editor breaks the contact, handle it somehow
        if (kitClass == null) return;

        /** Add editor actions to DTD Kit. */
        if (kitClass == DTDKit.class) {
            SettingsUtil.updateListSetting (settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { DTDTokenContext.context }
            );
        }


        /** Add editor actions to XML Kit. */
        if (kitClass == XMLKit.class) {
            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, Boolean.TRUE);
            
            SettingsUtil.updateListSetting (settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { XMLDefaultTokenContext.context }
            );
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR, getXMLIdentifierAcceptor());            
        }


        /* Allow '?' and '!' in abbrevirations. */
        if (kitClass == XMLKit.class || kitClass == DTDKit.class) {
            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR,
                            new Acceptor() {
                                public boolean accept(char ch) {
                                    return AcceptorFactory.NON_JAVA_IDENTIFIER.accept(ch) && ch != '!' && ch != '?';
                                }
                            }
                           );
        }

    }
    
    /*
     * Identifiers accept all NameChar [4].
     */
    Acceptor getXMLIdentifierAcceptor() {
        
        return  new Acceptor() {
            public boolean accept(char ch) {
                switch (ch) {
                    case ' ': case '\t': case '\n': case '\r':          // WS
                    case '>': case '<': case '&': case '\'': case '"': case '/':
                    case '\\': // markup
                        return false;
                }

                return true;
            }
        };
    }
}
