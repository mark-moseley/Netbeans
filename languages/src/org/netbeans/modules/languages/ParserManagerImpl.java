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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.SyntaxError;
import org.netbeans.modules.languages.parser.TokenInputUtils;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class ParserManagerImpl extends ParserManager {

    private Document                        document;
    private TokenHierarchy                  tokenHierarchy;
    private ASTNode                         ast = ASTNode.create (null, "Root", 0);
    private State                           state = State.NOT_PARSED;
    private List<SyntaxError>               syntaxErrors = Collections.<SyntaxError>emptyList ();
    private boolean[]                       cancel = new boolean[] {false};
    private Set<ParserManagerListener>      listeners;
    private Map<String,Set<ASTEvaluator>>   evaluatorsMap;
    private static RequestProcessor         rp = new RequestProcessor ("Parser");
    
    
    public ParserManagerImpl (Document doc) {
        this.document = doc;
        tokenHierarchy = TokenHierarchy.get (doc);
        String mimeType = (String) doc.getProperty ("mimeType");        
        if (tokenHierarchy == null) {
            // for tests only....
            if (mimeType != null) {
                try {
                    Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
                    if (language.getParser () != null) {
                        doc.putProperty (
                            org.netbeans.api.lexer.Language.class, 
                            new SLanguageHierarchy (language).language ()
                        );
                        tokenHierarchy = TokenHierarchy.get (doc);
                    }
                } catch (LanguageDefinitionNotFoundException ex) {
                }
            }
        }
        if (tokenHierarchy != null) {
            new DocListener (this, tokenHierarchy);
            if (mimeType != null && state == State.NOT_PARSED) {
                try {
                    LanguagesManager.getDefault().getLanguage(mimeType);
                    startParsing();
                } catch (LanguageDefinitionNotFoundException e) {
                    //not supported language
                }
            }
        }
        
        managers.put (doc, new WeakReference<ParserManager> (this));
    }
    
    public static ParserManagerImpl getImpl (Document document) {
        return (ParserManagerImpl) get (document);
    }
    
    public State getState () {
        return state;
    }
    
    public List<SyntaxError> getSyntaxErrors () {
        return syntaxErrors;
    }
    
    public ASTNode getAST () {
        return ast;
    }
    
    public void addListener (ParserManagerListener l) {
        if (listeners == null) listeners = new HashSet<ParserManagerListener> ();
        listeners.add (l);
    }
    
    public void removeListener (ParserManagerListener l) {
        if (listeners == null) return;
        listeners.remove (l);
    }
    
    public void addASTEvaluator (ASTEvaluator e) {
        if (evaluatorsMap == null)
            evaluatorsMap = new HashMap<String,Set<ASTEvaluator>> ();
        Set<ASTEvaluator> evaluatorsSet = evaluatorsMap.get (e.getFeatureName ());
        if (evaluatorsSet == null) {
            evaluatorsSet = new HashSet<ASTEvaluator> ();
            evaluatorsMap.put (e.getFeatureName (), evaluatorsSet);
        }
        evaluatorsSet.add (e);
    }
    
    public void removeASTEvaluator (ASTEvaluator e) {
        if (evaluatorsMap != null) {
            Set<ASTEvaluator> evaluatorsSet = evaluatorsMap.get (e.getFeatureName ());
            if (evaluatorsSet != null) 
                evaluatorsSet.remove (e);
        }
    }
    
    public void fire (
        final State                           state, 
        final List<ParserManagerListener>     listeners,
        final Map<String,Set<ASTEvaluator>>   evaluators,
        final ASTNode                         root
    ) {
        if (root == null) throw new NullPointerException ();
        parsingTask = rp.post (new Runnable () {
            public void run () {
                cancel [0] = false;
                fire2 (
                    state,
                    listeners,
                    evaluators,
                    root
                );
            }
        });
    }

    
    // private methods .........................................................
    
    private RequestProcessor.Task parsingTask;
    
    private synchronized void startParsing () {
        setChange (State.PARSING, ast);
        cancel [0] = true;
        if (parsingTask != null) {
            parsingTask.cancel ();
        }
        parsingTask = rp.post (new Runnable () {
            public void run () {
                cancel [0] = false;
                parse ();
            }
        }, 1000);
    }
    
    private void setChange (State state, ASTNode root) {
        if (state == this.state) return;
        this.state = state;
        this.ast = root;
        List<ParserManagerListener> listeners = this.listeners == null ?
            null : new ArrayList<ParserManagerListener> (this.listeners);
        Map<String,Set<ASTEvaluator>> evaluatorsMap = this.evaluatorsMap == null ?
            null : new HashMap<String,Set<ASTEvaluator>> (this.evaluatorsMap);
        fire2 (state, listeners, evaluatorsMap, root);
    }
    
    private void fire2 (
        State                           state, 
        List<ParserManagerListener>     listeners,
        Map<String,Set<ASTEvaluator>>   evaluators,
        ASTNode                         root
    ) {

        if (state == State.PARSING) return;
        if (evaluators != null) {
            if (!evaluators.isEmpty ()) {
                Iterator<Set<ASTEvaluator>> it = evaluators.values ().iterator ();
                while (it.hasNext ()) {
                    Iterator<ASTEvaluator> it2 = it.next ().iterator ();
                    while (it2.hasNext ()) {
                        ASTEvaluator e = it2.next ();
                        e.beforeEvaluation (state, root);
                        if (cancel [0]) return;
                    }
                }
                                                                                //times = new HashMap<Object,Long> ();
                evaluate (
                    state, 
                    root, 
                    new ArrayList<ASTItem> (), 
                    evaluators                                                  //, times
                );                                                              //iit = times.keySet ().iterator ();while (iit.hasNext()) {Object object = iit.next();S ystem.out.println("  Evaluator " + object + " : " + times.get (object));}
                if (cancel [0]) return;
                it = evaluators.values ().iterator ();
                while (it.hasNext ()) {
                    Iterator<ASTEvaluator> it2 = it.next ().iterator ();
                    while (it2.hasNext ()) {
                        ASTEvaluator e = it2.next ();
                        e.afterEvaluation (state, root);
                        if (cancel [0]) return;
                    }
                }
            }
        }
        
        if (listeners != null) {
            Iterator<ParserManagerListener> it = listeners.iterator ();
            while (it.hasNext ()) {
                ParserManagerListener l = it.next ();                           //long start = System.currentTimeMillis ();
                l.parsed (state, ast);
                                                                                //Long t = times.get (l);if (t == null) t = new Long (0);times.put (l, t.longValue () + S ystem.currentTimeMillis () - start);
                if (cancel [0]) return;
            }
        }                                                                       //Iterator iit = times.keySet ().iterator ();while (iit.hasNext()) {Object object = iit.next();S ystem.out.println("  Listener " + object + " : " + times.get (object));}
    }
    
    private void evaluate (
        State state, 
        ASTItem item, 
        List<ASTItem> path,
        Map<String,Set<ASTEvaluator>> evaluatorsMap2                            //, Map<Object,Long> times                                         
    ) {
        path.add (item);
        Language language = (Language) item.getLanguage ();
        if (language != null)
            language.getFeatureList ().evaluate (
                 state, 
                 path, 
                 evaluatorsMap2                                                 //, times
            );
        Iterator<ASTItem> it2 = item.getChildren ().iterator ();
        while (it2.hasNext ()) {
            if (cancel [0]) return;
            evaluate (
                state, 
                it2.next (), 
                path, 
                evaluatorsMap2                                                  //, times
            );
        }
        path.remove (path.size () - 1);
    }
    
    private void parse () {
        setChange (State.PARSING, ast);
        String mimeType = (String) document.getProperty ("mimeType");
        Language language = getLanguage (mimeType);
        LLSyntaxAnalyser analyser = language.getAnalyser ();                           //long start = System.currentTimeMillis ();
        TokenInput input = createTokenInput ();
        if (cancel [0]) return;                                                 //S ystem.out.println ("lex " + (System.currentTimeMillis () - start));start = System.currentTimeMillis ();
        List<SyntaxError> newSyntaxErrors = new ArrayList<SyntaxError> ();
        try {
            ast = analyser.read (
                input, 
                true, 
                newSyntaxErrors,
                cancel
            );                                                                  //S ystem.out.println ("syntax " + (System.currentTimeMillis () - start));
            syntaxErrors = newSyntaxErrors;
        } catch (ParseException ex) {
            // should not be called - read (skipErrors == true)
            Utils.notify (ex);
            ast = ASTNode.create (language, "Root", 0);
            setChange (State.OK, ast);                                          //S ystem.out.println ("fire " + (System.currentTimeMillis () - start));
            return;
        }
        if (cancel [0]) return;                                                 //long start = System.currentTimeMillis ();
        try {
            Feature astProperties = language.getFeatureList ().getFeature ("AST");
            if (astProperties != null && astProperties.getType () != Feature.Type.NOT_SET)
                ast = (ASTNode) astProperties.getValue (
                    "process", 
                    SyntaxContext.create (document, ASTPath.create (ast))
                );
        } catch (Exception ex) {
            Utils.notify (ex);
            ast = ASTNode.create (language, "Root", 0);
        }                                                                       //start = System.currentTimeMillis () - start;if (start > 100)S ystem.out.println ("postprocess " + start);
        if (ast == null) {
            Utils.notify (new NullPointerException ());
            ast = ASTNode.create (language, "Root", 0);
        }                                                                   //start = System.currentTimeMillis ();
        setChange (State.OK, ast);                                          //S ystem.out.println ("fire " + (System.currentTimeMillis () - start));
    }
    
    private TokenInput createTokenInput () {
        final TokenInput[] ret = new TokenInput[1];
        document.render(new Runnable() {
            public void run() {
                if (tokenHierarchy == null) {
                    ret[0] = TokenInputUtils.create(Collections.<ASTToken>emptyList());
                    return;
                }
                TokenSequence ts = tokenHierarchy.tokenSequence();
                if (ts == null) {
                    ret [0] = TokenInputUtils.create(Collections.<ASTToken>emptyList());
                    return;
                }
                List<ASTToken> tokens = getTokens(ts);
                if (cancel[0]) {
                    // Leave null in ret[0]
                    return;
                }
                ret[0] = TokenInputUtils.create(tokens);
            }
        });
        return ret[0];
    }
    
    private List<ASTToken> getTokens (TokenSequence ts) {
        Language language = null;
        try {
            language = LanguagesManager.getDefault ().getLanguage (ts.language ().mimeType ());
        } catch (LanguageDefinitionNotFoundException ex) {
        }
        List<ASTToken> tokens = new ArrayList<ASTToken> ();
        while (ts.moveNext ()) {
            if (cancel [0]) return null;
            Token t = ts.token ();
            int type = t.id ().ordinal ();
            int offset = ts.offset ();
            String ttype = (String) t.getProperty ("type");
            if (ttype == null || ttype.equals ("E")) {
                List<ASTToken> children = null;
                TokenSequence ts2 = ts.embedded ();
                if (ts2 != null)
                    children = getTokens (ts2);
                tokens.add (ASTToken.create (
                    language,
                    type, 
                    t.text ().toString (), 
                    offset,
                    t.length (),
                    children
                ));
            } else
            if (ttype.equals ("S")) {
                StringBuilder sb = new StringBuilder (t.text ());
                List<ASTToken> children = new ArrayList<ASTToken> ();
                TokenSequence ts2 = ts.embedded ();
//                if (ts2 != null)
//                    children.addAll (
//                        getTokens (ts2)
//                    );
                while (ts.moveNext ()) {
                    if (cancel [0]) return null;
                    t = ts.token ();
                    ttype = (String) t.getProperty ("type");
                    if (ttype == null) {
                        ts.movePrevious ();
                        break;
                    }
                    if (ttype.equals ("E")) {
                        ts2 = ts.embedded ();
//                        children.add (ASTToken.create (
//                            ts2.language ().mimeType (),
//                            t.id ().name (), 
//                            t.text ().toString (), 
//                            ts.offset (),
//                            t.length (),
//                            getTokens (ts2)
//                        ));
                        List<ASTToken> tokens2 = getTokens (ts2);
                        if (cancel [0]) return null;
                        children.addAll (tokens2);
                        continue;
                    }
                    if (ttype.equals ("S")) {
                        ts.movePrevious ();
                        break;
                    }
                    if (!ttype.equals ("C"))
                        throw new IllegalArgumentException ();
//                    ts2 = ts.embedded ();
//                    if (ts2 != null)
//                        children.addAll (
//                            getTokens (ts2)
//                        );
                    if (type != t.id ().ordinal ())
                        throw new IllegalArgumentException ();
                    sb.append (t.text ());
                }
                int no = ts.offset () + ts.token ().length ();
                tokens.add (ASTToken.create (
                    language,
                    type, 
                    sb.toString (), 
                    offset,
                    no - offset,
                    children
                ));
            } else
                throw new IllegalArgumentException ();
        }
        return tokens;
    }
    
    private Language getLanguage (String mimeType) {
        try {
            return LanguagesManager.getDefault ().getLanguage (mimeType);
        } catch (LanguageDefinitionNotFoundException ex) {
            return Language.create (LanguagesManager.normalizeMimeType(mimeType));
        }
    }
    
    private static Map<Document,WeakReference<ParserManager>> managers = 
        new WeakHashMap<Document,WeakReference<ParserManager>> ();

    // HACK
    static void refreshHack () {
        Iterator<Document> it = managers.keySet ().iterator ();
        while (it.hasNext ()) {
            AbstractDocument document = (AbstractDocument) it.next ();
            document.readLock ();
            try {
                MutableTextInput mti = (MutableTextInput) document.getProperty (MutableTextInput.class);
                mti.tokenHierarchyControl ().rebuild ();
            } finally {
                document.readUnlock ();
            }
//            final StyledDocument document = (StyledDocument) it.next ();
//            NbDocument.runAtomic (document, new Runnable () {
//                public void run() {
//                    MutableTextInput mti = (MutableTextInput) document.getProperty (MutableTextInput.class);
//                    mti.tokenHierarchyControl ().rebuild ();
//                }
//            });
        }
    }
    
    // innerclasses ............................................................
    
    private static class DocListener implements TokenHierarchyListener {
        
        private WeakReference<ParserManagerImpl> pmwr;
        
        DocListener (ParserManagerImpl pm, TokenHierarchy hierarchy) {
            pmwr = new WeakReference<ParserManagerImpl> (pm);
            hierarchy.addTokenHierarchyListener (this);
        }
        
        private ParserManagerImpl getPM () {
            ParserManagerImpl pm = pmwr.get ();
            if (pm != null) return pm;
            return null;
        }
    
        public void tokenHierarchyChanged (TokenHierarchyEvent evt) {
            ParserManagerImpl pm = getPM ();
            if (pm == null) return;
            pm.startParsing ();
        }
    }
}
