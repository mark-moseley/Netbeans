/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Petr Pisl
 */
public class SemanticAnalysis implements SemanticAnalyzer {

    public static final EnumSet<ColoringAttributes> UNUSED_FIELD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.FIELD);
    public static final EnumSet<ColoringAttributes> UNUSED_STATIC_FIELD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.FIELD, ColoringAttributes.STATIC);
    public static final EnumSet<ColoringAttributes> UNUSED_METHOD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> STATIC_METHOD_SET = EnumSet.of(ColoringAttributes.STATIC, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> UNUSED_STATIC_METHOD_SET = EnumSet.of(ColoringAttributes.STATIC, ColoringAttributes.METHOD, ColoringAttributes.UNUSED);

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public SemanticAnalysis() {
        semanticHighlights = null;
    }

    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo compilationInfo) throws Exception {
        resume();

        if (isCancelled()) {
            return;
        }

        PHPParseResult result = getParseResult(compilationInfo);
        Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<OffsetRange, Set<ColoringAttributes>>(100);

        if (result.getProgram() != null) {
            result.getProgram().accept(new SemanticHighlightVisitor(highlights));

            if (highlights.size() > 0) {
                semanticHighlights = highlights;
            } else {
                semanticHighlights = null;
            }
        }
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    private PHPParseResult getParseResult(CompilationInfo info) {
        ParserResult result = info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);

        if (result == null) {
            return null;
        } else {
            return ((PHPParseResult) result);
        }
    }

    private class SemanticHighlightVisitor extends DefaultVisitor {

        private class IdentifierColoring {
            public Identifier identifier;
            public Set<ColoringAttributes> coloring;

            public IdentifierColoring(Identifier identifier, Set<ColoringAttributes> coloring) {
                this.identifier = identifier;
                this.coloring = coloring;
            }
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights;        
        // for unused private fields: name, varible
        // if isused, then it's deleted from the list and marked as the field
        private final Map<String, IdentifierColoring> privateFieldsUsed;
        // for unsed private method: name, identifier
        private final Map<String, IdentifierColoring> privateMethod;

        public SemanticHighlightVisitor(Map<OffsetRange, Set<ColoringAttributes>> highlights) {
            this.highlights = highlights;
            privateFieldsUsed = new HashMap<String, IdentifierColoring>();
            privateMethod = new HashMap<String, IdentifierColoring>();
        }

        private OffsetRange createOffsetRange(ASTNode node) {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }

        @Override
        public void visit(ClassDeclaration cldec) {
            if (isCancelled()) {
                return;
            }
            Identifier name = cldec.getName();
            OffsetRange or = new OffsetRange(name.getStartOffset(), name.getEndOffset());
            highlights.put(or, ColoringAttributes.CLASS_SET);
            cldec.getBody().accept(this);

            // are there unused private fields?
            for (IdentifierColoring item : privateFieldsUsed.values()) {
                or = new OffsetRange(item.identifier.getStartOffset(), item.identifier.getEndOffset());
                if (item.coloring.contains(ColoringAttributes.STATIC)) {
                    highlights.put(or, UNUSED_STATIC_FIELD_SET);
                }
                else {
                    highlights.put(or, UNUSED_FIELD_SET);
                }
                
            }

            // are there unused private methods?
            for(IdentifierColoring item : privateMethod.values()) {
                or = new OffsetRange(item.identifier.getStartOffset(), item.identifier.getEndOffset());
                if (item.coloring.contains(ColoringAttributes.STATIC)) {
                    highlights.put(or, UNUSED_STATIC_METHOD_SET);
                }
                else {
                    highlights.put(or, UNUSED_METHOD_SET);
                }
            }
        }

        @Override
        public void visit(MethodDeclaration md) {
            boolean isPrivate = Modifier.isPrivate(md.getModifier());
            EnumSet<ColoringAttributes> coloring = ColoringAttributes.METHOD_SET;

            if (Modifier.isStatic(md.getModifier())) {
                coloring = STATIC_METHOD_SET;
            }

            Identifier identifier = md.getFunction().getFunctionName();
            if (isPrivate) {
                privateMethod.put(identifier.getName(), new IdentifierColoring(identifier, coloring));
            }
            else {
                // color now only non private method
                highlights.put(createOffsetRange(identifier), coloring);
            }
            md.getFunction().getBody().accept(this);
        }

        @Override
        public void visit(MethodInvocation node) {
            Identifier identifier = null;
            if (node.getMethod().getFunctionName().getName() instanceof Variable) {
                Variable variable = (Variable)node.getMethod().getFunctionName().getName();
                if (variable.getName() instanceof Identifier) {
                    identifier = (Identifier)variable.getName();
                }
            }
            else if (node.getMethod().getFunctionName().getName() instanceof Identifier) {
                identifier = (Identifier)node.getMethod().getFunctionName().getName();
            }

            if (identifier != null) {
                IdentifierColoring item = privateMethod.remove(identifier.getName());
                if (item != null) {
                    OffsetRange or = new OffsetRange(item.identifier.getStartOffset(), item.identifier.getEndOffset());
                    highlights.put(or, item.coloring);
                }
            }
            super.visit(node);
        }


        @Override
        public void visit(InterfaceDeclaration node) {
            if (isCancelled()) {
                return;
            }
            Identifier name = node.getName();
            OffsetRange or = new OffsetRange(name.getStartOffset(), name.getEndOffset());
            highlights.put(or, ColoringAttributes.CLASS_SET);
            node.getBody().accept(this);
        }

        @Override
        public void visit(FieldsDeclaration node) {
            if (isCancelled()) {
                return;
            }

            boolean isPrivate = Modifier.isPrivate(node.getModifier());
            EnumSet<ColoringAttributes> coloring = ColoringAttributes.FIELD_SET;

            if (Modifier.isStatic(node.getModifier())) {
                coloring = ColoringAttributes.STATIC_FIELD_SET;
            }

            Variable[] variables = node.getVariableNames();
            for (int i = 0; i < variables.length; i++) {
                Variable variable = variables[i];
                if (!isPrivate) {
                    OffsetRange or = new OffsetRange(variable.getName().getStartOffset(), variable.getName().getEndOffset());
                    highlights.put(or, ColoringAttributes.FIELD_SET);
                } else {
                    if (variable.getName() instanceof Identifier) {
                        Identifier identifier =  (Identifier) variable.getName();
                        privateFieldsUsed.put(identifier.getName(), new IdentifierColoring(identifier, coloring));
                    }
                }
            }
        }

        @Override
        public void visit(FieldAccess node) {
            if (isCancelled()) {
                return;
            }
            if (!node.getField().isDollared()) {
                Expression expr = node.getField().getName();
                processFieldAccess(expr);
                OffsetRange or = new OffsetRange(expr.getStartOffset(), expr.getEndOffset());
                highlights.put(or, ColoringAttributes.FIELD_SET);
            }
        }

        @Override
        public void visit(StaticMethodInvocation node) {
            if (isCancelled()) {
                return;
            }
            ASTNode name = node.getMethod().getFunctionName();
            OffsetRange or = new OffsetRange(name.getStartOffset(), name.getEndOffset());
            highlights.put(or, ColoringAttributes.STATIC_SET);
        }

        @Override
        public void visit(StaticFieldAccess node) {
            Expression expr = node.getField().getName();
            processFieldAccess(expr);
            OffsetRange or = new OffsetRange(expr.getStartOffset(), expr.getEndOffset());
            highlights.put(or, ColoringAttributes.STATIC_FIELD_SET);
        }

        private void processFieldAccess(Expression expr) {
            if (expr instanceof Identifier) {
                String name = ((Identifier) expr).getName();
                //remove the field, because is used
                IdentifierColoring removed = privateFieldsUsed.remove(name);
                if (removed != null) {
                    // if it was removed, marked as normal field
                    OffsetRange or = new OffsetRange(removed.identifier.getStartOffset(), removed.identifier.getEndOffset());
                    highlights.put(or, removed.coloring);
                }
            }
        }
    }
}
