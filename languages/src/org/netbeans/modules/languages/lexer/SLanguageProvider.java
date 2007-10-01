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

package org.netbeans.modules.languages.lexer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;


/**
 *
 * @author Jan Jancura
 */
public class SLanguageProvider extends LanguageProvider {
    
    /** Creates a new instance of SLanguageProvider */
    public SLanguageProvider () {
    }

    public Language<STokenId> findLanguage (String mimePath) {
//        System.out.println("findLanguage " + mimePath);
        if (LanguagesManager.getDefault ().isSupported (mimePath)) {
            try {
                 org.netbeans.modules.languages.Language language = 
                        LanguagesManager.getDefault ().getLanguage (mimePath);
                 if (language.getTokenTypes ().isEmpty ()) return null;
                return new SLanguageHierarchy (mimePath).language ();
            } catch (ParseException ex) {
            }
        }
        return null;
    }

    public LanguageEmbedding<? extends TokenId> findLanguageEmbedding (
        Token token, 
        LanguagePath languagePath, 
        InputAttributes inputAttributes
    ) {
        String mimeType = languagePath.innerLanguage ().mimeType ();
        if (!LanguagesManager.getDefault ().isSupported (mimeType)) return null;
        Language<STokenId> language = getTokenImport (mimeType, token);
        if (language == null) 
            language = getPreprocessorImport (languagePath, token);
        if (language == null) return null;
        Integer i = (Integer) token.getProperty ("startSkipLength");
        int startSkipLength = i == null ? 0 : i.intValue ();
        i = (Integer) token.getProperty ("endSkipLength");
        int endSkipLength = i == null ? 0 : i.intValue ();
        return LanguageEmbedding.create (
            language, 
            startSkipLength, 
            endSkipLength
        );
    }

    
    // other methods ...........................................................
    
    private static Map<String,Language<STokenId>> preprocessorImport = new HashMap<String,Language<STokenId>> ();
    
    private static Language<STokenId> getPreprocessorImport (LanguagePath languagePath, Token token) {
        String tokenType = token.id ().name ();
        if (!tokenType.equals ("PE")) return null;
        String mimeType = languagePath.topLanguage ().mimeType ();
        if (!preprocessorImport.containsKey (mimeType)) {
            try {
                org.netbeans.modules.languages.Language language = 
                    LanguagesManager.getDefault ().getLanguage (mimeType);
                Feature properties = language.getPreprocessorImport ();
                if (properties != null) {
                    String innerMT = (String) properties.getValue ("mimeType");
                    preprocessorImport.put (
                        mimeType,
                        (Language<STokenId>)Language.find (innerMT)
                    );
                }
            } catch (ParseException ex) {
            }
        }
        return preprocessorImport.get (mimeType);
    }
    
    private static Map<String,Map<String,Language<STokenId>>> tokenImports = new HashMap<String,Map<String,Language<STokenId>>> ();
    
    private static Language<STokenId> getTokenImport (String mimeType, Token token) {
        String tokenType = token.id ().name ();
        Map<String,Language<STokenId>> tokenTypeToLanguage = tokenImports.get (mimeType);
        if (tokenTypeToLanguage == null) {
            tokenTypeToLanguage = new HashMap<String,Language<STokenId>> ();
            tokenImports.put (mimeType, tokenTypeToLanguage);
            try {
                org.netbeans.modules.languages.Language language = 
                    LanguagesManager.getDefault ().getLanguage (mimeType);
                Map<String,Feature> tokenImports = language.getTokenImports ();
                if (tokenImports != null) {
                    Iterator<String> it = tokenImports.keySet ().iterator ();
                    while (it.hasNext ()) {
                        String tokenType2 = it.next ();
                        Feature properties = tokenImports.get (tokenType2);
                        String innerMT = (String) properties.getValue ("mimeType");
                        tokenTypeToLanguage.put (
                            tokenType2,
                            (Language<STokenId>) Language.find (innerMT)
                        );
                    }
                }
            } catch (ParseException ex) {
            }
        }
        return tokenTypeToLanguage.get (tokenType);
    }
}
