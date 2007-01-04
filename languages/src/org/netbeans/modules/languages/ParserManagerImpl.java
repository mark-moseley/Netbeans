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

package org.netbeans.modules.languages;

import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.SToken;
import org.netbeans.api.languages.SyntaxCookie;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.LanguagesManagerImpl.LanguagesManagerListener;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.SyntaxCookie;
import org.netbeans.api.languages.ASTNode;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 *
 * @author Jan Jancura
 */
public class ParserManagerImpl extends ParserManager {

    private NbEditorDocument        doc;
    private ASTNode                 ast = null;
    private ParseException          exception = null;
    private int                     state = PARSING;
    private Vector                  listeners = new Vector ();
    private static RequestProcessor rp = new RequestProcessor ("Parser");
    
    
    public ParserManagerImpl (NbEditorDocument doc) {
        this.doc = doc;
        DocListener l = new DocListener ();
        doc.addDocumentListener (l);
        String mimeType = (String) doc.getProperty ("mimeType");
        ((LanguagesManagerImpl) LanguagesManager.getDefault ()).addLanguagesManagerListener (l);
    }
    
    public int getState () {
        return state;
    }
    
    public ASTNode getAST () throws ParseException {
        if (exception != null) throw exception;
        return ast;
    }
    
    public void addListener (ParserManagerListener l) {
        listeners.add (l);
    }
    
    public void removeListener (ParserManagerListener l) {
        listeners.remove (l);
    }

    
    // private methods .........................................................
    
    private RequestProcessor.Task parsingTask;
    
    private synchronized void startParsing () {
        setChange (PARSING, ast);
        if (parsingTask != null) {
            String mimeType = (String) doc.getProperty ("mimeType");
            try {
                Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                    getLanguage (mimeType);
                LLSyntaxAnalyser a = l.getAnalyser ();
                a.cancel ();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            parsingTask.cancel ();
        }
        parsingTask = rp.post (new Runnable () {
            public void run () {
                parseAST ();
            }
        }, 1000);
    }
    
    private void setChange (int state, ASTNode ast) {
        if (state == this.state) return;
        
        //switch (state) {case PARSING:System.out.println("parsing started");break;case OK:System.out.println("parsed OK");break;case ERROR:System.out.println("parser ERROR");};
        
        this.state = state;
        this.ast = ast;
        exception = null;
        Iterator it = new ArrayList (listeners).iterator ();
        while (it.hasNext ()) {
            ParserManagerListener l = (ParserManagerListener) it.next ();
            l.parsed (state, ast);
        }
    }
    
    private void setChange (ParseException ex) {
        state = ERROR;
        ast = null;
        exception = ex;
        Iterator it = new ArrayList (listeners).iterator ();
        while (it.hasNext ()) {
            ParserManagerListener l = (ParserManagerListener) it.next ();
            l.parsed (state, ast);
        }
    }
    
    private void parseAST () {
        try {
            setChange (PARSING, ast);
            ast = parse (doc);
            if (ast == null) {
                setChange (new ParseException ("ast is null?!"));
                return;
            }
            ast = process (ast);
            if (ast == null) {
                setChange (new ParseException ("ast is null?!"));
                return;
            }
            setChange (OK, ast);
        } catch (ParseException ex) {
            if (ex.getASTNode () != null) {
                ASTNode ast = process (ex.getASTNode ());
                ex = new ParseException (ex, ast);
            }
            ex.printStackTrace ();
            setChange (ex);
        }
    }
    
    private ASTNode process (ASTNode n) {
        try {
            String mimeType = (String) doc.getProperty ("mimeType");
            Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                getLanguage (mimeType);
            Evaluator e = (Evaluator) l.getProperty (mimeType, "processAST");
            if (e != null && ast != null) 
                return (ASTNode) e.evaluate (SyntaxCookie.create (doc, ast.getPath ()));
            return n;
        } catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
            return n;
        }
    }
    
    private static ASTNode parse (NbEditorDocument doc) throws ParseException {
        String mimeType = (String) doc.getProperty ("mimeType");
        Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
            getLanguage (mimeType);
        LLSyntaxAnalyser a = l.getAnalyser ();
        long start = System.currentTimeMillis ();

        TokenInput input = createTokenInput (doc, mimeType);
        long to = System.currentTimeMillis () - start;
        ASTNode n = a.read (input, true);
        System.out.println("parse " + doc.getProperty ("title") + " " + (System.currentTimeMillis () - start) + " " + to);
        return n;
    }

    private static TokenInput createTokenInput (NbEditorDocument doc, String mimeType) {
        try {
            List tokens = new ArrayList ();
            doc.readLock ();
            TokenHierarchy th = TokenHierarchy.get (doc);
            TokenSequence ts = th.tokenSequence ();

            while (ts.moveNext ()) {
                Token t = ts.token ();
                String type = t.id ().name ();
                String mt = Hack.getMimeType (mimeType, type);
                tokens.add (SToken.create (
                    mt, 
                    type, 
                    t.text ().toString (), 
                    t.offset (th)
                ));
            }
            return TokenInput.create (tokens);
        } finally {
            doc.readUnlock ();
        }
    }
    
    
    // innerclasses ............................................................
    
    private class DocListener implements DocumentListener, 
    LanguagesManagerListener {
        
        public void insertUpdate (DocumentEvent e) {
            startParsing ();
        }

        public void removeUpdate (DocumentEvent e) {
            startParsing ();
        }

        public void changedUpdate (DocumentEvent e) {
        }

        public void languageChanged (String mimeType) {
            startParsing ();
        }

        public void languageAdded(String mimeType) {
        }

        public void languageRemoved(String mimeType) {
        }
    }
}



