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
package org.netbeans.modules.languages.features;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.Feature;


/**
 *
 * @author hanz
 */
class UsagesASTEvaluator extends ASTEvaluator {

    private static Map<Document,WeakReference<UsagesASTEvaluator>> cache = new WeakHashMap<Document,WeakReference<UsagesASTEvaluator>> ();
    
    static void register (Document document) {
        if (get (document) != null) return;
        cache.put (document, new WeakReference<UsagesASTEvaluator> (new UsagesASTEvaluator (document)));
    }
    
    static void unregister (Document document) {
        UsagesASTEvaluator evaluator = get (document);
        if (evaluator != null)
            ParserManager.get (document).removeASTEvaluator (evaluator);
        cache.remove (document);
    }
    
    static UsagesASTEvaluator get (Document document) {
        WeakReference<UsagesASTEvaluator> weakReference = cache.get (document);
        if (weakReference == null) return null;
        return weakReference.get ();
    }
    
    static void addDatabaseDefinition (Document document, DatabaseDefinition definition) {
        UsagesASTEvaluator evaluator = get (document);
        evaluator.definitions.add (definition);
    }

    
    private Document                    document;
    private ParserManager               parserManager;
    private Set<DatabaseDefinition>     definitions;
    
    
    UsagesASTEvaluator (Document document) {
        this.document = document;
        parserManager = ParserManager.get (document);        
        parserManager.addASTEvaluator (this);
    }
    
    private List<DatabaseItem> unresolvedUsages;
    private List<Boolean> unresolvedUsages_declaration_precedes_ussage;

    public void beforeEvaluation (State state, ASTNode root) {
        unresolvedUsages = null;
        definitions = new HashSet<DatabaseDefinition> ();
    }

    public void afterEvaluation (State state, ASTNode root) {
        if (unresolvedUsages != null) {
            Iterator<DatabaseItem> it = unresolvedUsages.iterator ();
            Iterator<Boolean> it2 = unresolvedUsages_declaration_precedes_ussage.iterator ();
            while (it.hasNext ()) {
                if (parserManager != null && parserManager.getState () == State.PARSING)
                    return;
                DatabaseUsage usage = (DatabaseUsage) it.next ();
                DatabaseContext context = (DatabaseContext) it.next ();
                boolean declaration_precedes_ussage = it2.next ();
                DatabaseDefinition definition = context.getDefinition (
                    usage.getName (), 
                    usage.getOffset ()
                );
                //S ystem.out.println("add2 " + usage + " (" + definition + ") to " + context);
                if (definition != null) {
                    if (declaration_precedes_ussage && definition.getOffset () > usage.getOffset ()) continue;
                    definition.addUsage (usage);
                    context.addUsage (usage);
                    usage.setDatabaseDefinition (definition);
                    highlightUssage (usage, definition);
                    if (definitions.contains (definition)) {
                        highlightDefinition (definition);
                        definitions.remove (definition);
                    }
                }
            }
            unresolvedUsages = null;
        }
        Iterator<DatabaseDefinition> it2 = definitions.iterator ();
        while (it2.hasNext ())
            highlightUnusedDefinition (it2.next ());
         SemanticHighlightsLayer.update (document);
    }

    public void evaluate (State state, List<ASTItem> path, Feature feature) {
        SyntaxContext sc = SyntaxContext.create (document, ASTPath.create (path));
        if (!feature.getBoolean ("condition", sc, true)) return;
        ASTItem leaf = path.get (path.size () - 1);
        DatabaseContext context = ContextASTEvaluator.getCurrentContext (document, leaf.getOffset ());
        String name = ((String) feature.getValue ("name", sc)).trim ();
        boolean declaration_precedes_ussage = feature.getBoolean ("declaration_precedes_usage", true);
        DatabaseDefinition definition = context.getDefinition (name, leaf.getOffset ());
        if (definition != null && definition.getOffset () == leaf.getOffset ()) return;
        if (definition != null && declaration_precedes_ussage && definition.getOffset () > leaf.getOffset ()) return;
        DatabaseUsage usage = new DatabaseUsage (name, leaf.getOffset (), leaf.getEndOffset ());
//        System.out.println("add " + usage + " (" + definition + ") to " + context);
        if (definition != null) {
            definition.addUsage (usage);
            usage.setDatabaseDefinition (definition);
            context.addUsage (usage);
            highlightUssage (usage, definition);
            if (definitions.contains (definition)) {
                highlightDefinition (definition);
                definitions.remove (definition);
            }
        } else {
            if (unresolvedUsages == null) {
                unresolvedUsages = new ArrayList<DatabaseItem> ();
                unresolvedUsages_declaration_precedes_ussage = new ArrayList<Boolean> ();
            }
            unresolvedUsages.add (usage);
            unresolvedUsages.add (context);
            unresolvedUsages_declaration_precedes_ussage.add (declaration_precedes_ussage);
        }
    }

    public String getFeatureName () {
        return "SEMANTIC_USAGE";
    }
    
    private void highlightUssage (DatabaseUsage usage, DatabaseDefinition definition) {
        if ("parameter".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                usage.getOffset (), 
                usage.getEndOffset (), 
                getParameterAttributes ()
            );
        else
        if ("local".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                usage.getOffset (), 
                usage.getEndOffset (), 
                getLocalVariableAttributes ()
            );
        else
        if ("field".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                usage.getOffset (), 
                usage.getEndOffset (), 
                getFieldAttributes ()
            );
    }

    private void highlightDefinition (DatabaseDefinition definition) {
        if ("parameter".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getParameterAttributes ()
            );
        else
        if ("variable".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getLocalVariableAttributes ()
            );
        else
        if ("field".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getFieldAttributes ()
            );
    }

    private void highlightUnusedDefinition (DatabaseDefinition definition) {
        if ("parameter".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getUnusedParameterAttributes ()
            );
        else
        if ("variable".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getUnusedLocalVariableAttributes ()
            );
        else
        if ("field".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getUnusedFieldAttributes ()
            );
    }
    
    private static AttributeSet unusedParameterAttributeSet;
    
    private static AttributeSet getUnusedParameterAttributes () {
        if (unusedParameterAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (EditorStyleConstants.WaveUnderlineColor, new Color (153, 153, 153));
            unusedParameterAttributeSet = sas;
        }
        return unusedParameterAttributeSet;
    }
    
    private static AttributeSet parameterAttributeSet;
    
    private static AttributeSet getParameterAttributes () {
        if (parameterAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            parameterAttributeSet = sas;
        }
        return parameterAttributeSet;
    }
    
    private static AttributeSet unusedLocalVariableAttributeSet;
    
    private static AttributeSet getUnusedLocalVariableAttributes () {
        if (unusedLocalVariableAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (EditorStyleConstants.WaveUnderlineColor, new Color (153, 153, 153));
            unusedLocalVariableAttributeSet = sas;
        }
        return unusedLocalVariableAttributeSet;
    }
    
    private static AttributeSet localVariableAttributeSet;
    
    private static AttributeSet getLocalVariableAttributes () {
        if (localVariableAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            localVariableAttributeSet = sas;
        }
        return localVariableAttributeSet;
    }
    
    private static AttributeSet unusedFieldAttributeSet;
    
    private static AttributeSet getUnusedFieldAttributes () {
        if (unusedFieldAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (EditorStyleConstants.WaveUnderlineColor, new Color (153, 153, 153));
            StyleConstants.setForeground (sas, new Color (0, 153, 0));
            unusedFieldAttributeSet = sas;
        }
        return unusedFieldAttributeSet;
    }
    
    private static AttributeSet fieldAttributeSet;
    
    private static AttributeSet getFieldAttributes () {
        if (fieldAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (0, 153, 0));
            fieldAttributeSet = sas;
        }
        return fieldAttributeSet;
    }
}
