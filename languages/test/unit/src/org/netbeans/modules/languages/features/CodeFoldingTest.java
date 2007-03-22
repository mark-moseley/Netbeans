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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.languages.features;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.modules.languages.*;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Selector;
import org.netbeans.modules.languages.dataobject.LanguagesEditorKit;
import org.netbeans.modules.languages.features.LanguagesFoldManager.FoldItem;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.Rule;
import org.netbeans.modules.languages.parser.Pattern;

/**
 *
 * @author Daniel Prusa
 */
public class CodeFoldingTest extends TestCase {
    
    public static final String TEST_MIME_TYPE = "text/mt";
    
    public CodeFoldingTest(String testName) {
        super(testName);
    }
    
    public void testAST1 () throws ParseException {
        Language l = new Language (TEST_MIME_TYPE);
        l.addToken (null, "keyword", Pattern.create ("'if' | 'while'"), null, null);
        l.addToken (null, "identifier", Pattern.create ("['a'-'z' '_']+"), null, null);
        l.addToken (null, "operator", Pattern.create ("'(' | ')' | '{' | '}'"), null, null);
        l.addToken (null, "whitespace", Pattern.create ("[' ' '\n' '\t' '\r']+"), null, null);
        
        l.addFeature (Feature.create ("SKIP", Selector.create ("whitespace")));
        
        ASTToken IDENTIFIER = ASTToken.create (null, "identifier", null, 0);
        ASTToken IF = ASTToken.create (null, "keyword", "if", 0);
        ASTToken WHILE = ASTToken.create (null, "keyword", "while", 0);
        ASTToken PARENTHESIS = ASTToken.create (null, "operator", "(", 0);
        ASTToken PARENTHESIS2 = ASTToken.create (null, "operator", ")", 0);
        ASTToken BRACE = ASTToken.create (null, "operator", "{", 0);
        ASTToken BRACE2 = ASTToken.create (null, "operator", "}", 0);
    
        l.addRule (Rule.create ("S", Arrays.asList (new Object[] {"Statement", "S"})));
        l.addRule (Rule.create ("S", Arrays.asList (new Object[] {})));
        l.addRule (Rule.create ("Statement", Arrays.asList (new Object[] {"IfStatement"})));
        l.addRule (Rule.create ("Statement", Arrays.asList (new Object[] {"WhileStatement"})));
        l.addRule (Rule.create ("Statement", Arrays.asList (new Object[] {"Block"})));
        l.addRule (Rule.create ("IfStatement", Arrays.asList (new Object[] {IF, PARENTHESIS, "ConditionalExpression", PARENTHESIS2, "Block"})));
        l.addRule (Rule.create ("WhileStatement", Arrays.asList (new Object[] {WHILE, PARENTHESIS, "ConditionalExpression", PARENTHESIS2, "Block"})));
        l.addRule (Rule.create ("ConditionalExpression", Arrays.asList (new Object[] {IDENTIFIER})));
        l.addRule (Rule.create ("Block", Arrays.asList (new Object[] {BRACE, "Block1", BRACE2})));
        l.addRule (Rule.create ("Block1", Arrays.asList (new Object[] {IDENTIFIER, "Block1"})));
        l.addRule (Rule.create ("Block1", Arrays.asList (new Object[] {"Statement", "Block1"})));
        l.addRule (Rule.create ("Block1", Arrays.asList (new Object[] {})));
        
        Map<String,String> calls = new HashMap<String,String> ();
        l.addFeature (Feature.create (
            "FOLD",
            Selector.create ("Block")
        ));
        
        LanguagesManager.getDefault ().addLanguage (l);
        
        String text = 
            "if (true) {\n" +
            "    id_one\n" +
            "    while (false) {\n" +
            "        id_two\n" +
            "        id_three\n" +
            "    }\n" +
            "    if (true) {id_four}\n" +
            "}\n";
        
        final JEditorPane pane = new JEditorPane ();

        //FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(pane);
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    pane.setEditorKit(new LanguagesEditorKit(TEST_MIME_TYPE));
                }
            });
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        
        //System.out.println("foldManager: " + env.getFoldManager());
        
        Document doc = pane.getDocument();
        doc.putProperty ("mimeType", l.getMimeType ());
        doc.putProperty (org.netbeans.api.lexer.Language.class, new SLanguageHierarchy (l.getMimeType ()).language ());

        LanguagesFoldManager.Factory factory = new LanguagesFoldManager.Factory();
        LanguagesFoldManager foldManager = (LanguagesFoldManager)factory.createFoldManager();
        foldManager.init(doc);
        
        ParserManager parserManager = ParserManager.get(doc);
        pane.setText (text);
        
        int counter = 0;
        try {
            while (((parserManager.getState() == ParserManager.State.NOT_PARSED) ||
                    (parserManager.getState() == ParserManager.State.PARSING))
                    && counter < 200) {
                Thread.sleep(100);
                counter++;
            }
            while (foldManager.isEvaluating() && counter < 200) {
                Thread.sleep(100);
                counter++;
            }
        } catch (InterruptedException e) {
        }
        
        ASTNode root = parserManager.getAST();
        
        //System.out.println("counter: " + counter);
        //System.out.println("state: " + parserManager.getState());
        List<FoldItem> items = foldManager.getFolds();
        
        assertEquals (2, items.size ());
    }
    
}




