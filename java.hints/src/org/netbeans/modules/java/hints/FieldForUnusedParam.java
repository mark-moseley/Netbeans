/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class FieldForUnusedParam implements TreeRule {

    private AtomicBoolean cancel = new AtomicBoolean();
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.VARIABLE);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        return run(compilationInfo, treePath, CaretAwareJavaSourceTaskFactory.getLastPosition(compilationInfo.getFileObject()));
    }
    
    List<ErrorDescription> run(final CompilationInfo info, TreePath treePath, int offset) {
        cancel.set(false);
        
        if (!getTreeKinds().contains(treePath.getLeaf().getKind())) {
            return null;
        }
        
        if (treePath.getParentPath() == null || treePath.getParentPath().getLeaf().getKind() != Kind.METHOD) {
            return null;
        }
        
        final Element el = info.getTrees().getElement(treePath);
        
        if (el == null || el.getKind() != ElementKind.PARAMETER) {
            return null;
        }
        
        MethodTree parent = (MethodTree) treePath.getParentPath().getLeaf();
        Element parentEl = info.getTrees().getElement(treePath.getParentPath());
        
        if (parentEl == null || parentEl.getKind() != ElementKind.CONSTRUCTOR || parent.getBody() == null) {
            return null;
        }
        
        boolean existing = false;
        
        for (VariableElement field : ElementFilter.fieldsIn(parentEl.getEnclosingElement().getEnclosedElements())) {
            if (cancel.get()) {
                return null;
            }
            
            if (field.getSimpleName().equals(el.getSimpleName())) {
                if (!info.getTypes().isAssignable(field.asType(), el.asType())) {
                    return null;
                }
                
                existing = true;
                break;
            }
        }
        
        @SuppressWarnings("serial")
        class Result extends RuntimeException {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return this;
            }
        }
        
        boolean found = false;

        try {
            new CancellableTreePathScanner<Void, Void>(cancel) {
                @Override
                public Void visitIdentifier(IdentifierTree node, Void p) {
                    Element e = info.getTrees().getElement(getCurrentPath());

                    if (el.equals(e)) {
                        throw new Result();
                    }
                    return super.visitIdentifier(node, p);
                }
            }.scan(new TreePath(treePath.getParentPath(), parent.getBody()), null);
        } catch (Result r) {
            found = true;
        }
        
        if (cancel.get() || found) {
            return null;
        }
        
        List<Fix> fix = Collections.<Fix>singletonList(new FixImpl(info.getJavaSource(), TreePathHandle.create(treePath, info), existing));
        String displayName = NbBundle.getMessage(FieldForUnusedParam.class, "ERR_UnusedParameter");
        ErrorDescription err = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, displayName,fix, info.getFileObject(), offset, offset);
        
        return Collections.singletonList(err);
    }

    public String getId() {
        return FieldForUnusedParam.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(FieldForUnusedParam.class, "DN_FieldUnusedParam");
    }

    public void cancel() {
        cancel.set(true);
    }

    static final class FixImpl implements Fix {

        private final JavaSource js;
        private final TreePathHandle tph;
                final boolean existing;

        public FixImpl(JavaSource js, TreePathHandle tph, boolean existing) {
            this.js = js;
            this.tph = tph;
            this.existing = existing;
        }
        
        public String getText() {
            return existing ? NbBundle.getMessage(FieldForUnusedParam.class, "FIX_AssignToExisting") : NbBundle.getMessage(FieldForUnusedParam.class, "FIX_CreateField");
        }

        public ChangeInfo implement() throws Exception {
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.PARSED);
                    
                    TreePath variable = tph.resolve(wc);
                    
                    if (variable == null) {
                        return ;
                    }
                    
                    VariableTree vt = (VariableTree) variable.getLeaf();
                    MethodTree   mt = (MethodTree) variable.getParentPath().getLeaf();
                    ClassTree    ct = (ClassTree) variable.getParentPath().getParentPath().getLeaf();
                    TreeMaker    make = wc.getTreeMaker();
                    Name         before = null;
                    int          index = 0;
                    
                    for (VariableTree p : mt.getParameters()) {
                        if (p == vt) {
                            if (mt.getParameters().size() > index + 1) {
                                before = mt.getParameters().get(index + 1).getName();
                            }
                            
                            break;
                        }
                        
                        index++;
                    }
                    
                    if (!existing) {
                        VariableTree field = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), vt.getName(), vt.getType(), null);
                        int insertPlace = -1;
                        
                        index = 0;
                        
                        for (Tree member : ct.getMembers()) {
                            if (member.getKind() == Kind.VARIABLE && ((VariableTree) member).getName().equals(before)) {
                                insertPlace = index;
                                break;
                            }
                            
                            index++;
                        }

                        wc.rewrite(ct, insertPlace != (-1) ? make.insertClassMember(ct, insertPlace, field) : GeneratorUtilities.get(wc).insertClassMember(ct, field));
                    }
                    
                    StatementTree assignment = make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), vt.getName()), make.Identifier(vt.getName()))); // NOI18N
                    
                    int insertPlace = -1;

                    index = 0;

                    for (StatementTree st : mt.getBody().getStatements()) {
                        if (st.getKind() == Kind.EXPRESSION_STATEMENT) {
                            ExpressionStatementTree est = (ExpressionStatementTree) st;
                            
                            if (est.getExpression().getKind() == Kind.ASSIGNMENT) {
                                AssignmentTree at = (AssignmentTree) est.getExpression();
                                
                                if (at.getVariable().getKind() == Kind.MEMBER_SELECT) {
                                    MemberSelectTree mst = (MemberSelectTree) at.getVariable();
                                    
                                    if (mst.getIdentifier().equals(before) && mst.getExpression().getKind() == Kind.IDENTIFIER && ((IdentifierTree) mst.getExpression()).getName().contentEquals("this")) { // NOI18N
                                        insertPlace = index;
                                        break;
                                    }
                                }
                            }
                        }

                        index++;
                    }
                    
                    wc.rewrite(mt.getBody(), insertPlace != (-1) ? make.insertBlockStatement(mt.getBody(), insertPlace, assignment) : make.addBlockStatement(mt.getBody(), assignment));
                }
            }).commit();
            
            return null;
        }
        
    }
    
}
