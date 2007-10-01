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
package org.netbeans.modules.java.editor.semantic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.editor.semantic.ColoringAttributes.Coloring;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Lahoda
 */
public class LexerBasedHighlightLayer extends AbstractHighlightsContainer {
    
    private Map<Token, Coloring> colorings;
    private Map<Coloring, AttributeSet> CACHE = new HashMap<Coloring, AttributeSet>();
    private Document doc;
    private boolean topLevelIsJava;

    public static LexerBasedHighlightLayer getLayer(Class id, Document doc) {
        LexerBasedHighlightLayer l = (LexerBasedHighlightLayer) doc.getProperty(id);
        
        if (l == null) {
            doc.putProperty(id, l = new LexerBasedHighlightLayer(doc));
        }
        
        return l;
    }
    
    private LexerBasedHighlightLayer(Document doc) {
        this.doc = doc;
        this.colorings = Collections.emptyMap();
        TokenHierarchy th = TokenHierarchy.get(doc);
        
        topLevelIsJava = th.tokenSequence().language() == JavaTokenId.language();
    }
    
    public void setColorings(final Map<Token, Coloring> colorings, final Set<Token> addedTokens, final Set<Token> removedTokens) {
        NbDocument.runAtomic((StyledDocument) doc, 
//        SwingUtilities.invokeLater(
        /*doc.render(*/new Runnable() {
            public void run() {
                synchronized (LexerBasedHighlightLayer.this) {
                    LexerBasedHighlightLayer.this.colorings = colorings;
                    
                    if (addedTokens.isEmpty()) {
                        //need to fire anything here?
                    } else {
                        if (addedTokens.size() == 1) {
                            Token t = addedTokens.iterator().next();
                            
                            fireHighlightsChange(t.offset(null), t.offset(null) + t.length()); //XXX: locking
                        } else {
                            fireHighlightsChange(0, doc.getLength()); //XXX: locking
                        }
                    }
                }
            }
        });
    }
    
    public synchronized Map<Token, Coloring> getColorings() {
        return colorings;
    }
    
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        
        if (topLevelIsJava) {
            return new LexerBasedHighlightSequence(this, th.tokenSequence().subSequence(startOffset, endOffset), colorings);
        } else {
            return new EmbeddedLexerBasedHighlightSequence(this, th.tokenSequence().subSequence(startOffset, endOffset), colorings);
        }
    }

    synchronized AttributeSet getColoring(Coloring c) {
        AttributeSet a = CACHE.get(c);
        
        if (a == null) {
            CACHE.put(c, a = ColoringManager.getColoringImpl(c));
        }
        
        return a;
    }
}
