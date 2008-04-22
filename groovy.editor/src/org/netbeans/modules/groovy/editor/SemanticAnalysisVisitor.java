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

package org.netbeans.modules.groovy.editor;

import java.util.HashMap;
import java.util.Map;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.OffsetRange;

/**
 *
 * @author Martin Adamek
 */
public class SemanticAnalysisVisitor extends ClassCodeVisitorSupport {

    private final ModuleNode root;
    private final BaseDocument doc;
    private final Map<OffsetRange, ColoringAttributes> highlights;

    public SemanticAnalysisVisitor(ModuleNode root, BaseDocument document) {
        this.root = root;
        this.doc = document;
        this.highlights = new HashMap<OffsetRange, ColoringAttributes>();
    }

    public Map<OffsetRange, ColoringAttributes> annotate() {
        highlights.clear();

        for (Object object : root.getClasses()) {
            visitClass((ClassNode)object);
        }

        for (Object object : root.getMethods()) {
            visitMethod((MethodNode)object);
        }

        visitBlockStatement(root.getStatementBlock());

        return highlights;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return root.getContext();
    }

    @Override
    public void visitField(FieldNode node) {
        if (node.getLineNumber() > 0) {
            OffsetRange range = AstUtilities.getRange(node, doc);
            highlights.put(range, ColoringAttributes.FIELD);

            if (node.isStatic()) {
                highlights.put(range, ColoringAttributes.STATIC);
            }
        }
        super.visitField(node);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        if (node.getLineNumber() > 0) {
            // Beware, a ConstructorNode is a MethodNode as well, (see below)
            // but we have to catch the Constructors first.
            OffsetRange range = AstUtilities.getRange(node, doc);
            highlights.put(range, ColoringAttributes.CONSTRUCTOR);
        }
        super.visitConstructor(node);
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (node.getLineNumber() > 0) {
            OffsetRange range = AstUtilities.getRange(node, doc);
            highlights.put(range, ColoringAttributes.METHOD);

            if (node.isStatic()) {
                highlights.put(range, ColoringAttributes.STATIC);
            }
        }
        super.visitMethod(node);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        // FIXME: Houston, we have a problem:
        // the PropertyExpression comes with no line/column information
        // this is supposed to be fixed in Groovy. See:
        // http://jira.codehaus.org/browse/GROOVY-2575

        super.visitPropertyExpression(node);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (node.getLineNumber() > 0) {
            OffsetRange range = AstUtilities.getRange(node, doc);
            highlights.put(range, ColoringAttributes.CLASS);
        }
        super.visitClass(node);
    }

    @Override
    public void visitVariableExpression(VariableExpression node) {
        Variable var = node.getAccessedVariable();

        if (var instanceof FieldNode) {
            if (node.getLineNumber() > 0) {
                OffsetRange range = AstUtilities.getRange(node, doc);
                highlights.put(range, ColoringAttributes.FIELD);
            }
        }
        super.visitVariableExpression(node);
    }
}
