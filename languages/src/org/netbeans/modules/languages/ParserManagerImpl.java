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
import org.netbeans.modules.languages.LanguagesManager.LanguagesManagerListener;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.parser.TokenInputUtils;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;


/**
 *
 * @author Jan Jancura
 */
public class ParserManagerImpl extends ParserManager {

    private Document                        doc;
    private TokenHierarchy                  tokenHierarchy;
    private ASTNode                         ast = null;
    private ParseException                  exception = null;
    private State                           state = State.NOT_PARSED;
    private boolean[]                       cancel = new boolean[] {false};
    private Set<ParserManagerListener>      listeners;
    private Set<ASTEvaluator>               evaluators;
    private Map<String,Set<ASTEvaluator>>   evaluatorsMap;
    private static RequestProcessor         rp = new RequestProcessor ("Parser");
    
    
    public ParserManagerImpl (Document doc) {
        this.doc = doc;
        tokenHierarchy = TokenHierarchy.get (doc);
        String mimeType = (String) doc.getProperty ("mimeType");        
        if (tokenHierarchy == null) {
            // for tests only....
            if (mimeType != null) {
                doc.putProperty (
                    org.netbeans.api.lexer.Language.class, 
                    new SLanguageHierarchy (mimeType).language ()
                );
                tokenHierarchy = TokenHierarchy.get (doc);
            }
        }
        new DocListener (this, tokenHierarchy);
        if (state == State.NOT_PARSED) {
            try {
                LanguagesManager.getDefault().getLanguage(mimeType);
                startParsing();
            } catch (LanguageDefinitionNotFoundException e) {
                //not supported language
            }
        }
    }
    
    public State getState () {
        return state;
    }
    
    public ASTNode getAST () throws ParseException {
        if (exception != null) throw exception;
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
        if (evaluators == null) evaluators = new HashSet<ASTEvaluator> ();
        evaluators.add (e);
        
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
        if (evaluators != null) evaluators.remove (e);
        if (evaluatorsMap != null) {
            Set<ASTEvaluator> evaluatorsSet = evaluatorsMap.get (e.getFeatureName ());
            if (evaluatorsSet != null) 
                evaluatorsSet.remove (e);
        }
    }
    
//    public synchronized void forceEvaluation (ASTEvaluator e) {
//        if (state != State.ERROR && state != State.OK) {
//            return;
//        }
//        
//        e.beforeEvaluation (state, ast);
//        evaluate (state, ast, new ArrayList<ASTItem> (), new ArrayList<ASTEvaluator> (evaluators));
//        e.afterEvaluation (state, ast);
//    }
    
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
                parseAST ();
            }
        }, 1000);
    }
    
    private void setChange (State state, ASTNode root) {
        if (state == this.state) return;
        this.state = state;
        this.ast = root;
        exception = null;   
                                                                                Map<Object,Long> times = new HashMap<Object,Long> ();
        if (listeners != null) {
            Iterator<ParserManagerListener> it = new ArrayList<ParserManagerListener> (listeners).iterator ();
            while (it.hasNext ()) {
                ParserManagerListener l = it.next ();
                                                                                long start = System.currentTimeMillis ();
                l.parsed (state, root);
                                                                                Long t = times.get (l);
                                                                                if (t == null) t = new Long (0);
                                                                                times.put (l, t.longValue () + System.currentTimeMillis () - start);
                if (cancel [0]) return;
            }
        }
                                                                                Iterator iit = times.keySet ().iterator ();
                                                                                while (iit.hasNext()) {
                                                                                    Object object = iit.next();
                                                                                    System.out.println("  Listener " + object + " : " + times.get (object));
                                                                                }

        if (state == State.PARSING) return;
        if (evaluators != null) {
            List<ASTEvaluator> evaluators2 = new ArrayList<ASTEvaluator> (evaluators);
            Map<String,Set<ASTEvaluator>> evaluatorsMap2 = new HashMap<String,Set<ASTEvaluator>> (evaluatorsMap);
            if (!evaluators2.isEmpty ()) {
                Iterator<ASTEvaluator> it2 = evaluators2.iterator ();
                while (it2.hasNext ()) {
                    ASTEvaluator e = it2.next ();
                    e.beforeEvaluation (state, root);
                    if (cancel [0]) return;
                }
                                                                                times = new HashMap<Object,Long> ();
                evaluate (
                    state, 
                    root, 
                    new ArrayList<ASTItem> (), 
                    evaluatorsMap2                                              , times
                );
                                                                                iit = times.keySet ().iterator ();
                                                                                while (iit.hasNext()) {
                                                                                    Object object = iit.next();
                                                                                    System.out.println("  Evaluator " + object + " : " + times.get (object));
                                                                                }
                if (cancel [0]) return;
                it2 = evaluators2.iterator ();
                while (it2.hasNext ()) {
                    ASTEvaluator e = it2.next ();
                    e.afterEvaluation (state, root);
                    if (cancel [0]) return;
                }
            }
        }
    }
    
    private void evaluate (
        State state, 
        ASTItem item, 
        List<ASTItem> path,
        Map<String,Set<ASTEvaluator>> evaluatorsMap2                            , Map<Object,Long> times                                         
    ) {
        path.add (item);
        try {
            Language l = LanguagesManager.getDefault ().getLanguage (item.getMimeType ());
            l.evaluate (
                 state, 
                 path, 
                 evaluatorsMap2                                                 , times
            );
        } catch (LanguageDefinitionNotFoundException ex) {
        }
        Iterator<ASTItem> it2 = item.getChildren ().iterator ();
        while (it2.hasNext ()) {
            if (cancel [0]) return;
            evaluate (
                state, 
                it2.next (), 
                path, 
                evaluatorsMap2                                                  , times
            );
        }
        path.remove (path.size () - 1);
    }
    
    private void setChange (ParseException ex) {
        state = State.ERROR;
        ast = null;
        exception = ex;
        Iterator<ParserManagerListener> it = new ArrayList<ParserManagerListener> (listeners).iterator ();
        while (it.hasNext ()) {
            ParserManagerListener l = it.next ();
            l.parsed (state, ast);
            if (cancel [0]) return;
        }
        if (state == State.PARSING) return;
    }
    
    private void parseAST () {
        try {
            setChange (State.PARSING, ast);
            ast = parse ();
            if (cancel [0]) {
                return;
            }
            if (ast == null) {
                setChange (new ParseException ("ast is null?!"));
                return;
            }
                                                                                long start = System.currentTimeMillis ();
            ast = process (ast);
                                                                                start = System.currentTimeMillis () - start;
                                                                                if (start > 100)
                                                                                    System.out.println ("postprocess " + start);
            if (ast == null) {
                setChange (new ParseException ("ast is null?!"));
                return;
            }
                                                                                start = System.currentTimeMillis ();
            setChange (State.OK, ast);
                                                                                System.out.println ("fire " + (System.currentTimeMillis () - start));
        } catch (ParseException ex) {
            if (ex.getASTNode () != null) {
                ASTNode ast = process (ex.getASTNode ());
                ex = new ParseException (ex, ast);
            }
                                                                                long start = System.currentTimeMillis ();
            setChange (ex);
                                                                                System.out.println ("fire " + (System.currentTimeMillis () - start));
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    private ASTNode process (ASTNode root) {
        try {
            String mimeType = (String) doc.getProperty ("mimeType");
            Language l = getLanguage (mimeType);
            Feature astProperties = l.getFeature ("AST");
            if (astProperties != null && ast != null) {
                ASTNode nn = (ASTNode) astProperties.getValue (
                    "process", 
                    SyntaxContext.create (doc, ASTPath.create (root))
                );
                if (nn != null)
                    root = nn;
            }
            return root;
        } catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
            return root;
        }
    }
    
    private ASTNode parse () throws ParseException {
        String mimeType = (String) doc.getProperty ("mimeType");
        Language l = getLanguage (mimeType);
        LLSyntaxAnalyser a = l.getAnalyser ();
                                                                                long start = System.currentTimeMillis ();
        TokenInput input = createTokenInput ();
        if (cancel [0]) return null;
                                                                                System.out.println ("lex " + (System.currentTimeMillis () - start));
                                                                                start = System.currentTimeMillis ();
        ASTNode n = a.read (input, true, cancel);
                                                                                System.out.println ("syntax " + (System.currentTimeMillis () - start));
        return n;
    }

    public TokenInput createTokenInput () {
        if (doc instanceof NbEditorDocument)
            ((NbEditorDocument) doc).readLock ();
        try {
            if (tokenHierarchy == null) 
                return TokenInputUtils.create (Collections.<ASTToken>emptyList ());
            TokenSequence ts = tokenHierarchy.tokenSequence ();
            List<ASTToken> tokens = getTokens (ts);
            if (cancel [0]) return null;
            return TokenInputUtils.create (tokens);
        } finally {
            if (doc instanceof NbEditorDocument)
                ((NbEditorDocument) doc).readUnlock ();
        }
    }
    
    private List<ASTToken> getTokens (TokenSequence ts) {
        List<ASTToken> tokens = new ArrayList<ASTToken> ();
        while (ts.moveNext ()) {
            if (cancel [0]) return null;
            Token t = ts.token ();
            String type = t.id ().name ();
            int offset = ts.offset ();
            String ttype = (String) t.getProperty ("type");
            if (ttype == null || ttype.equals ("E")) {
                List<ASTToken> children = null;
                TokenSequence ts2 = ts.embedded ();
                if (ts2 != null)
                    children = getTokens (ts2);
                tokens.add (ASTToken.create (
                    ts.language ().mimeType (),
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
                    if (!type.equals (t.id ().name ()))
                        throw new IllegalArgumentException ();
                    sb.append (t.text ());
                }
                int no = ts.offset () + ts.token ().length ();
                tokens.add (ASTToken.create (
                    ts.language ().mimeType (),
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
        // [PENDING] workaround for internal mime type set by options for coloring preview
        if (mimeType.startsWith("test")) { // NOI18N
            int length = mimeType.length();
            for (int x = 4; x < length; x++) {
                char c = mimeType.charAt(x);
                if (!(c >= '0' && c <= '9')) {
                    if (x > 4 && c == '_' && x < length -1) {
                        mimeType = mimeType.substring(x + 1);
                    }
                    break;
                } // if
            } // for
        } // if
        // end of workaround
        try {
            return LanguagesManager.getDefault().getLanguage(mimeType);
        } catch (LanguageDefinitionNotFoundException ex) {
            return new Language (mimeType);
        }
    }
    
    // innerclasses ............................................................
    
    private static class DocListener implements TokenHierarchyListener, LanguagesManagerListener {
        
        private WeakReference<ParserManagerImpl> pmwr;
        
        DocListener (ParserManagerImpl pm, TokenHierarchy hierarchy) {
            pmwr = new WeakReference<ParserManagerImpl> (pm);
            hierarchy.addTokenHierarchyListener (this);
            LanguagesManager.getDefault ().addLanguagesManagerListener (this);
        }
        
        private ParserManagerImpl getPM () {
            ParserManagerImpl pm = pmwr.get ();
            if (pm != null) return pm;
            LanguagesManager.getDefault ().removeLanguagesManagerListener (this);
            return null;
        }

        public void languageChanged (String mimeType) {
            ParserManagerImpl pm = getPM ();
            if (pm == null) return;
            pm.startParsing ();
        }
    
        public void tokenHierarchyChanged (TokenHierarchyEvent evt) {
            ParserManagerImpl pm = getPM ();
            if (pm == null) return;
            pm.startParsing ();
        }
    }
}



