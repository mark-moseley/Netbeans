/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.html;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;
import org.netbeans.editor.ext.html.*;


/**A testing Completion Provider that provides abbreviations as result.
 *
 * @author Jan Lahoda
 */
public class HTMLCompletionProvider implements CompletionProvider {
    
    private static ErrorManager ERR = ErrorManager.getDefault();
    
    /**Whether only full match of the abbreviation code should be considered for the completion.
     * E.g. if NON_EXACT_MATCH == true, ser| would provide System.err.println("|"); abbreviation,
     * if NON_EXACT_MATCH == false, ser| would not provide the abbreviation, but serr| would.
     */
    //private static final boolean NON_EXACT_MATCH = Boolean.getBoolean("nebeans.editor.completion.abbreviations.nonexactmatch");
    private static final boolean NON_EXACT_MATCH = true;
    
    /**
     * Enable the AbbreviationsCompletionProvider
     */
    //private static final boolean ENABLED = Boolean.getBoolean("nebeans.editor.completion.abbreviations.enable");
    private static final boolean ENABLED = true;
    
    /** Creates a new instance of JavaDocCompletionProvider */
    public HTMLCompletionProvider() {
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE)
            return new AsyncCompletionTask(new Query(), component);
        return null;
    }
    
    static class Query extends AsyncCompletionQuery {
        
        private JTextComponent component;
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            List/*<CompletionItem>*/ results = queryImpl(component, caretOffset);
            assert (results != null);
            resultSet.addAllItems(results);
            resultSet.finish();
        }
    }
    
    private static List/*<CompletionItem>*/ queryImpl(JTextComponent component, int offset) {
        if (!ENABLED) return Collections.EMPTY_LIST;
        
        Class kitClass = Utilities.getKitClass(component);
        if (kitClass != null) {
            ExtEditorUI eeui = (ExtEditorUI)Utilities.getEditorUI(component);
            Completion compl = ((HTMLKit)Utilities.getKit(component)).createCompletionForProvider(eeui);
            HTMLSyntaxSupport support = (HTMLSyntaxSupport)Utilities.getSyntaxSupport(component);
            
            CompletionQuery.Result res = compl.getQuery().query(component, offset, support);
            if(res  == null) return Collections.EMPTY_LIST;
            else return res.getData();
        }
        
        return Collections.EMPTY_LIST;
    }
    
}
