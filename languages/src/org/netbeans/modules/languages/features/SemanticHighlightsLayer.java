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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.features;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;


/**
 *
 * @author Jan Jancura
 */
class SemanticHighlightsLayer extends AbstractHighlightsContainer {

    private static Map<Document,List<WeakReference<SemanticHighlightsLayer>>> cache = new WeakHashMap<Document,List<WeakReference<SemanticHighlightsLayer>>> ();

    static void addHighlight (
        Document document, 
        int startOffset,
        int endOffset,
        AttributeSet attributeSet
    ) {
        List<WeakReference<SemanticHighlightsLayer>> layers = cache.get (document);
        List<WeakReference<SemanticHighlightsLayer>> newLayers = new ArrayList<WeakReference<SemanticHighlightsLayer>> ();
        boolean remove = true;
        if (layers != null) {
            Iterator<WeakReference<SemanticHighlightsLayer>> it = layers.iterator ();
            while (it.hasNext()) {
                WeakReference<SemanticHighlightsLayer> weakReference = it.next ();
                SemanticHighlightsLayer layer = weakReference.get ();
                if (layer == null) continue;
                remove = false;
                if (layer.offsetsBag1 == null)
                    layer.offsetsBag1 = new OffsetsBag (document);
                layer.offsetsBag1.addHighlight (startOffset, endOffset, attributeSet);
                newLayers.add (weakReference);
            }
        }
        if (remove) {
            cache.remove (document);
            ColorsASTEvaluator.unregister (document);
            DeclarationASTEvaluator.unregister (document);
            ContextASTEvaluator.unregister (document);
            UsagesASTEvaluator.unregister (document);
        } else
            cache.put (document, newLayers);
    }
    
    static void update (Document document) {
        List<WeakReference<SemanticHighlightsLayer>> layers = cache.get (document);
        boolean remove = true;
        if (layers != null) {
            Iterator<WeakReference<SemanticHighlightsLayer>> it = layers.iterator ();
            while (it.hasNext()) {
                WeakReference<SemanticHighlightsLayer> weakReference = it.next ();
                SemanticHighlightsLayer layer = weakReference.get ();
                if (layer == null) continue;
                remove = false;
                layer.offsetsBag = layer.offsetsBag1;
                layer.offsetsBag1 = null;
                if (layer.offsetsBag == null)
                    layer.offsetsBag = new OffsetsBag (document);
                layer.fireHighlightsChange (0, document.getLength ());
            }
        }
        if (remove) {
            cache.remove (document);
            ColorsASTEvaluator.unregister (document);
            DeclarationASTEvaluator.unregister (document);
            ContextASTEvaluator.unregister (document);
            UsagesASTEvaluator.unregister (document);
        }
    }

    
    private Document            document;
    private OffsetsBag          offsetsBag;
    private OffsetsBag          offsetsBag1;
    
    SemanticHighlightsLayer (Document document) {
        this.document = document;
        ColorsASTEvaluator.register (document);
        DeclarationASTEvaluator.register (document);
        ContextASTEvaluator.register (document);
        UsagesASTEvaluator.register (document);
        
        List<WeakReference<SemanticHighlightsLayer>> layers = cache.get (document);
        if (layers == null) {
            layers = new ArrayList<WeakReference<SemanticHighlightsLayer>> ();
            cache.put (document, layers);
        }
        layers.add (new WeakReference<SemanticHighlightsLayer> (this));
    }
    
    public HighlightsSequence getHighlights (int startOffset, int endOffset) {
                                                                                //S ystem.out.println("SemanticHighlightsLayer.getHighlights " + startOffset + " : " + endOffset);
        if (offsetsBag == null) {
            offsetsBag = new OffsetsBag (document);
            refresh ();
        }
        return offsetsBag.getHighlights (startOffset, endOffset);
    }
    
    private void refresh () {
        ParserManagerImpl parserManager = (ParserManagerImpl) ParserManagerImpl.get (document);
        try {
            parserManager.fire (
                parserManager.getState (), 
                null, 
                getEvaluators (), 
                parserManager.getAST ()
            );
        } catch (ParseException ex) {
        }
    }
    
    private Map<String,Set<ASTEvaluator>> evaluators;
    
    private Map<String,Set<ASTEvaluator>> getEvaluators () {
        if (evaluators == null) {
            evaluators = new HashMap<String,Set<ASTEvaluator>> ();
            ColorsASTEvaluator colorsASTEvaluator = ColorsASTEvaluator.get (document);
            evaluators.put (colorsASTEvaluator.getFeatureName (), Collections.<ASTEvaluator>singleton (colorsASTEvaluator));
            UsagesASTEvaluator usagesASTEvaluator = UsagesASTEvaluator.get (document);
            evaluators.put (usagesASTEvaluator.getFeatureName (), Collections.<ASTEvaluator>singleton (usagesASTEvaluator));
            DeclarationASTEvaluator declarationASTEvaluator = DeclarationASTEvaluator.get (document);
            evaluators.put (declarationASTEvaluator.getFeatureName (), Collections.<ASTEvaluator>singleton (declarationASTEvaluator));
            ContextASTEvaluator contextASTEvaluator = ContextASTEvaluator.get (document);
            evaluators.put (contextASTEvaluator.getFeatureName (), Collections.<ASTEvaluator>singleton (contextASTEvaluator));
        }
        return evaluators;
    }
}
