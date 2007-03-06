/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.languages.lexer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.LanguagesManagerImpl;
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
        if (LanguagesManager.getDefault ().getSupportedMimeTypes ().contains (mimePath))
            return new SLanguageHierarchy (mimePath).language ();
        return null;
    }

    public LanguageEmbedding<? extends TokenId> findLanguageEmbedding (
        Token token, 
        LanguagePath languagePath, 
        InputAttributes inputAttributes
    ) {
        String mimeType = languagePath.innerLanguage ().mimeType ();
        Language language = getTokenImport (mimeType, token);
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
    
    private static Map<String,Language> preprocessorImport = new HashMap<String,Language> ();
    
    private static Language getPreprocessorImport (LanguagePath languagePath, Token token) {
        String tokenType = token.id ().name ();
        if (!tokenType.equals ("PE")) return null;
        String mimeType = languagePath.topLanguage ().mimeType ();
        if (!preprocessorImport.containsKey (mimeType)) {
            try {
                org.netbeans.modules.languages.Language language = 
                    ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                    getLanguage (mimeType);
                Feature properties = language.getPreprocessorImport ();
                if (properties != null) {
                    String innerMT = (String) properties.getValue ("mimeType");
                    preprocessorImport.put (
                        mimeType,
                        Language.find (innerMT)
                    );
                }
            } catch (ParseException ex) {
            }
        }
        return preprocessorImport.get (mimeType);
    }
    
    private static Map<String,Map<String,Language>> tokenImports = new HashMap ();
    
    private static Language getTokenImport (String mimeType, Token token) {
        String tokenType = token.id ().name ();
        Map<String,Language> tokenTypeToLanguage = tokenImports.get (mimeType);
        if (tokenTypeToLanguage == null) {
            tokenTypeToLanguage = new HashMap<String,Language> ();
            tokenImports.put (mimeType, tokenTypeToLanguage);
            try {
                org.netbeans.modules.languages.Language language = 
                    ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                    getLanguage (mimeType);
                Map<String,Feature> tokenImports = language.getTokenImports ();
                if (tokenImports != null) {
                    Iterator<String> it = tokenImports.keySet ().iterator ();
                    while (it.hasNext ()) {
                        String tokenType2 = it.next ();
                        Feature properties = tokenImports.get (tokenType2);
                        String innerMT = (String) properties.getValue ("mimeType");
                        tokenTypeToLanguage.put (
                            tokenType2,
                            Language.find (innerMT)
                        );
                    }
                }
            } catch (ParseException ex) {
            }
        }
        return tokenTypeToLanguage.get (tokenType);
    }
}
