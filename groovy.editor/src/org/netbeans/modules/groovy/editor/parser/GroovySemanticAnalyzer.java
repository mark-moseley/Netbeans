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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.editor.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.api.gsf.ColoringAttributes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.SemanticAnalyzer;
import org.netbeans.modules.groovy.editor.AstPath;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;

/**
 *
 * @author MArtin Adamek
 */
public class GroovySemanticAnalyzer implements SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, ColoringAttributes> semanticHighlights;

    public GroovySemanticAnalyzer() {
    }

    public Map<OffsetRange, ColoringAttributes> getHighlights() {
        return semanticHighlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public final synchronized void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo info) {
        
        System.out.println("### GroovySemanticAnalyzer.run: " + info);
        
        resume();

        if (isCancelled()) {
            return;
        }

        ASTNode root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }

        Map<OffsetRange, ColoringAttributes> highlights =
            new HashMap<OffsetRange, ColoringAttributes>(100);

        AstPath path = new AstPath();
        path.descend(root);
        annotate(root, highlights, path, null, false, info.getText());
        path.ascend();

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            if (info.getPositionManager().isTranslatingSource()) {
                Map<OffsetRange, ColoringAttributes> translated = new HashMap<OffsetRange,ColoringAttributes>(2*highlights.size());
                for (Map.Entry<OffsetRange,ColoringAttributes> entry : highlights.entrySet()) {
                    OffsetRange range = LexUtilities.getLexerOffsets(info, entry.getKey());
                    if (range != OffsetRange.NONE) {
                        translated.put(range, entry.getValue());
                    }
                }
                
                highlights = translated;
            }
            
            this.semanticHighlights = highlights;
        } else {
            this.semanticHighlights = null;
        }
    }

    /** Find unused local and dynamic variables */
    @SuppressWarnings("unchecked")
    private void annotate(ASTNode node, Map<OffsetRange, ColoringAttributes> highlights, AstPath path,
        List<String> parameters, boolean isParameter, String text) {
        
        if (node instanceof MethodNode) {
            OffsetRange range = AstUtilities.getRange(node, text);
            highlights.put(range, ColoringAttributes.METHOD);
        } else if (node instanceof FieldNode) {
            OffsetRange range = AstUtilities.getRange(node, text);
            highlights.put(range, ColoringAttributes.FIELD);
        }

        List<ASTNode> list = AstUtilities.children(node);
        for (ASTNode child : list) {
            path.descend(child);
            annotate(child, highlights, path, parameters, isParameter, text);
            path.ascend();
        }
    }

    
}
