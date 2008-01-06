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

package org.netbeans.modules.languages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

import javax.swing.event.EventListenerList;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.lexer.SLexer;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.Parser;


/**
 *
 * @author Jan Jancura
 */
public class LanguageImpl extends Language {

    
    private NBSLanguageReader   reader;
    private String              mimeType;
    private Parser              parser;
    private LLSyntaxAnalyser    analyser;
    private FeatureList         featureList = new FeatureList ();

    
    /** Creates a new instance of Language */
    public LanguageImpl (
        String                  mimeType,
        NBSLanguageReader       reader
    ) {
        this.mimeType = mimeType;
        this.reader = reader;
    }
    
    
    // public methods ..........................................................
    
    public String getMimeType () {
        return mimeType;
    }

    public Parser getParser () {
        return parser;
    }
    
    public LLSyntaxAnalyser getAnalyser () {
        if (analyser == null)
            analyser = LLSyntaxAnalyser.createEmpty (this);
        return analyser;
    }
    
    public FeatureList getFeatureList () {
        return featureList;
    }
    
    private EventListenerList listenerList = new EventListenerList ();
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        listenerList.add (PropertyChangeListener.class, l);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        listenerList.remove (PropertyChangeListener.class, l);
    }
    
    // ids ...
    
    private Map<String,Integer> tokenTypeToID;
    private Map<Integer,String> idToTokenType;
    private int                 tokenTypeCount = 0;

    public int getTokenID (String tokenType) {
        if (!tokenTypeToID.containsKey (tokenType))
            return -1;
        return tokenTypeToID.get (tokenType);
    }
    
    public int getTokenTypeCount () {
        return tokenTypeCount;
    }
    
    public String getTokenType (int tokenTypeID) {
        if (idToTokenType == null) return null;
        return idToTokenType.get (tokenTypeID);
    }
    
    private Map<String,Integer> ntToNTID;
    private Map<Integer,String> ntidToNt;
    
    public int getNTID (String nt) {
        if (ntidToNt == null) ntidToNt = new HashMap<Integer,String> ();
        if (ntToNTID == null) ntToNTID = new HashMap<String,Integer> ();
        if (!ntToNTID.containsKey (nt)) {
            int id = ntToNTID.size ();
            ntToNTID.put (nt, id);
            ntidToNt.put (id, nt);
        }
        return ntToNTID.get (nt);
    }
    
    public int getNTCount () {
        if (ntToNTID == null) return 0;
        return ntToNTID.size ();
    }
    
    public String getNT (int ntid) {
        return ntidToNt.get (ntid);
    }
    
    
    // imports ...
    
    private Feature             preprocessorImport;
    private Map<String,Feature> tokenImports = new HashMap<String,Feature> ();
    private List<Language>      importedLangauges = new ArrayList<Language> ();
    
    public Feature getPreprocessorImport () {
        return preprocessorImport;
    }
    
    public Map<String,Feature> getTokenImports () {
        return tokenImports;
    }
    
    public List<Language> getImportedLanguages () {
        return importedLangauges;
    }
    
    void importLanguage (
        Feature feature
    ) {
        String mimeType = (String) feature.getValue ("mimeType");
        if (feature.getPattern ("start") != null) {
            //feature.put ("token", Language.EMBEDDING_TOKEN_TYPE_NAME);
            assert (preprocessorImport == null);
            preprocessorImport = feature;
            try {
                importedLangauges.add (LanguagesManager.getDefault ().getLanguage (mimeType));
            } catch (LanguageDefinitionNotFoundException ex) {
                importedLangauges.add (Language.create (mimeType));
            }
            return;
        }
        if (feature.getValue ("state") == null) {
            String tokenName = feature.getSelector ().getAsString ();
            assert (!tokenImports.containsKey (tokenName));
            tokenImports.put (tokenName, feature);
            try {
                importedLangauges.add (LanguagesManager.getDefault ().getLanguage (mimeType));
            } catch (LanguageDefinitionNotFoundException ex) {
                importedLangauges.add (Language.create (mimeType));
            }
            return;
        }
        try {
            Language language = LanguagesManager.getDefault ().getLanguage (mimeType);

            String state = (String) feature.getValue ("state"); 
            String tokenName = feature.getSelector ().getAsString ();

            // import tokenTypes
    //!!            Iterator<TokenType> it = language.getTokenTypes ().iterator ();
    //            while (it.hasNext ()) {
    //                TokenType tt = it.next ();
    //                String startState = tt.getStartState ();
    //                Pattern pattern = tt.getPattern ().clonePattern ();
    //                String endState = tt.getEndState ();
    //                if (startState == null || Parser.DEFAULT_STATE.equals (startState)) 
    //                    startState = state;
    //                else
    //                    startState = tokenName + '-' + startState;
    //                if (endState == null || Parser.DEFAULT_STATE.equals (endState)) 
    //                    endState = state;
    //                else
    //                    endState = tokenName + '-' + endState;
    //                //!!addToken (startState, tt.getType (), pattern, endState, tt.getProperties ());
    //            }

            // import grammar rues
            if (language.getAnalyser () != null)
                try {
                    analyser = LLSyntaxAnalyser.create (
                        this, 
                        language.getAnalyser ().getRules (), 
                        language.getAnalyser ().getSkipTokenTypes ()
                    );
                } catch (ParseException ex) {
                    ex.printStackTrace ();
                }
            // import features
            featureList.importFeatures (language.getFeatureList ());
            importedLangauges.addAll (language.getImportedLanguages ());
            tokenImports.putAll (language.getTokenImports ());
        } catch (LanguageDefinitionNotFoundException ex) {
            Utils.notify ("Editors/" + mimeType + "/language.nbs:", ex);
        }
    }
    
    
    // other methods ...........................................................
    
    public void read (NBSLanguageReader reader) throws ParseException, IOException {
        this.reader = reader;
        read ();
    }
    
    public void read () throws ParseException, IOException {
        try {
            tokenTypeToID = new HashMap<String, Integer> ();
            idToTokenType = new HashMap<Integer, String> ();
            featureList = new FeatureList ();
            if (!reader.containsTokens ()) {
                org.netbeans.api.lexer.Language lexerLanguage = org.netbeans.api.lexer.Language.find (getMimeType ());
                if (lexerLanguage != null) {
                    Iterator it = lexerLanguage.tokenIds ().iterator ();
                    while (it.hasNext()) {
                        TokenId tokenId = (TokenId) it.next();
                        int id = tokenId.ordinal ();
                        String name = tokenId.name ();
                        idToTokenType.put (id, name);
                        tokenTypeToID.put (name, id);
                        tokenTypeCount = Math.max (tokenTypeCount, id + 1);
                    }
                } else
                    initLexicalStuff (reader.getTokenTypes ());
            } else
                initLexicalStuff (reader.getTokenTypes ());

            List<Feature> features = reader.getFeatures ();
            Iterator<Feature> it2 = features.iterator ();
            while (it2.hasNext ()) {
                Feature feature = it2.next ();
                featureList.add (feature);
            }
            Set<Integer> skipTokenIDs = new HashSet<Integer> ();
            Iterator<Feature> it = featureList.getFeatures ("SKIP").iterator ();
            while (it.hasNext()) {
                Feature feature = it.next();
                if (feature.getFeatureName ().equals ("SKIP")) {
                    skipTokenIDs.add (tokenTypeToID.get (feature.getSelector ().toString ()));
                }
            }
            List<Rule> rules = reader.getRules (this);
            analyser = LLSyntaxAnalyser.create (
                this, rules, skipTokenIDs
            );
            fire ();
        } finally {
            reader = null;
        }
    }
    
    private void initLexicalStuff (List<TokenType> tokenTypes) {
        Iterator<TokenType> it = tokenTypes.iterator ();
        while (it.hasNext()) {
            TokenType tokenType = it.next ();
            int id = tokenType.getTypeID ();
            String name = tokenType.getType ();
            idToTokenType.put (id, name);
            tokenTypeToID.put (name, id);
            tokenTypeCount = Math.max (tokenTypeCount, id + 1);
        }
        parser = Parser.create (tokenTypes);
    }
    
    protected void fire () {
        if (listenerList == null) return;
        Object[] l = listenerList.getListenerList ();
        PropertyChangeEvent event = null;
        for (int i = l.length - 2; i >= 0; i -= 2) {
            if (event == null)
                event = new PropertyChangeEvent (this, null, null, null);
            ((PropertyChangeListener) l [i+1]).propertyChange (event);
        }
    }
}


