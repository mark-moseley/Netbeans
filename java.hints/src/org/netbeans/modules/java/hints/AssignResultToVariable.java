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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class AssignResultToVariable extends AbstractHint {
    
    public AssignResultToVariable() {
        super(true, false, AbstractHint.HintSeverity.CURRENT_LINE_WARNING);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD_INVOCATION, Kind.NEW_CLASS);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        try {
            if (treePath.getParentPath().getLeaf().getKind() != Kind.EXPRESSION_STATEMENT)
                return null;
            
            Tree tree = treePath.getLeaf();
            Tree exprTree = null;
            Kind kind = tree.getKind();
            if (kind == Kind.METHOD_INVOCATION) {
                exprTree = ((MethodInvocationTree)tree).getMethodSelect();
            } else if (kind == Kind.NEW_CLASS) {
                exprTree = ((NewClassTree)tree).getIdentifier();
            }
            
            long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), exprTree);
            long end   = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), exprTree);
            int offset = CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject());
            
            if (start == (-1) || end == (-1) || offset < start || offset > end) {
                return null;
            }
            
            Element elem = info.getTrees().getElement(treePath);
            
            if (elem == null || (elem.getKind() != ElementKind.METHOD && elem.getKind() != ElementKind.CONSTRUCTOR)) {
                return null;
            }
            
            TypeMirror type = info.getTrees().getTypeMirror(treePath);
            
            if (type == null || NOT_ACCEPTABLE_TYPE_KINDS.contains(type.getKind())) {
                return null;
            }
            
            List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(info.getFileObject(), info.getDocument(), TreePathHandle.create(treePath, info)));
            String description = NbBundle.getMessage(AssignResultToVariable.class, "HINT_AssignResultToVariable");
            
            return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), description, fixes, info.getFileObject(), (int) start, (int) end));
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    public void cancel() {
        // XXX implement me
    }
    
    public String getId() {
        return AssignResultToVariable.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AssignResultToVariable.class, "DN_AssignResultToVariable");
    }

    public String getDescription() {
        return NbBundle.getMessage(AssignResultToVariable.class, "DESC_AssignResultToVariable");
    }

    static final class FixImpl implements Fix {
        
        private FileObject file;
        private Document doc;
        private TreePathHandle tph;
        
        public FixImpl(FileObject file, Document doc, TreePathHandle tph) {
            this.file = file;
            this.doc = doc;
            this.tph = tph;
        }

        public String getText() {
            return NbBundle.getMessage(AssignResultToVariable.class, "FIX_AssignResultToVariable");
        }

        public ChangeInfo implement() {
            try {
                final String[] name = new String[1];
                ModificationResult result = JavaSource.forFileObject(file).runModificationTask(new Task<WorkingCopy>() {
                    
                    public void run(WorkingCopy copy) throws Exception {
                        copy.toPhase(Phase.RESOLVED);
                        
                        TreePath tp = tph.resolve(copy);
                        
                        if (tp == null) {
                            Logger.getLogger(AssignResultToVariable.class.getName()).info("tp=null"); // NOI18N
                            return ;
                        }
                        
                        TypeMirror type = copy.getTrees().getTypeMirror(tp);
                        Element el = copy.getTrees().getElement(tp);
                        
                        if (type == null || NOT_ACCEPTABLE_TYPE_KINDS.contains(type.getKind())) {
                            return ;
                        }

                        Tree t = tp.getLeaf();
                        boolean isAnonymous = false; //handle anonymous classes #138223
                        ExpressionTree identifier = null;
                        if (t instanceof NewClassTree) {
                            NewClassTree nct = ((NewClassTree)t);
                            isAnonymous = nct.getClassBody() != null || el.getKind().isInterface() || el.getModifiers().contains(Modifier.ABSTRACT);
                            identifier = nct.getIdentifier();
                        }

                        type = Utilities.resolveCapturedType(copy, type);
                        
                        TreeMaker make = copy.getTreeMaker();
                        
                        name[0] = Utilities.guessName(copy, tp);

                        VariableTree var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name[0], isAnonymous ? identifier : make.Type(type), (ExpressionTree) tp.getLeaf());
                        
                        var = Utilities.copyComments(copy, tp.getLeaf(), var);
                        
                        copy.rewrite(tp.getParentPath().getLeaf(), var);
                    }
                });
                
                List<? extends Difference> differences = result.getDifferences(file);
                
                if (differences == null) {
                    Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.INFO, "No differences."); // NOI18N
                    return null;
                }
                
                //should look like this, bug the code generator actually creates more that one difference:
//                if (differences.size() != 1) {
//                    Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.INFO, "Cannot find the difference: {0}", differences);
//                    result.commit();
//                    return null;
//                }
                
                Difference found = null;
                
                for (Difference d : differences) {
                    if (d.getNewText() != null && d.getNewText().contains(name[0])) {
                        if (found == null) {
                            found = d;
                        } else {
                            //more than one difference is containing the name...
                            found = null;
                            break;
                        }
                    }
                }
                
                if (found == null) {
                    Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.INFO, "Cannot find the difference: {0}", differences); // NOI18N
                    result.commit();
                    return null;
                }
                
                final Position start = NbDocument.createPosition(doc, found.getStartPosition().getOffset(), Bias.Backward);
                final int length = found.getNewText().length();
                
                result.commit();
                
                final ChangeInfo[] info = new ChangeInfo[1];
                
                doc.render(new Runnable() {
                    public void run() {
                        try {
                            String text = doc.getText(start.getOffset(), length);
                            Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.FINE, "text after commit: {0}", text); // NOI18N
                            int    relPos = text.lastIndexOf(name[0]);
                            
                            Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.FINE, "relPos: {0}", relPos); // NOI18N
                            if (relPos != (-1)) {
                                int startPos = start.getOffset() + relPos;
                                
                                info[0] = new ChangeInfo(doc.createPosition(startPos), doc.createPosition(startPos + name[0].length()));
                            }
                        } catch (BadLocationException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
                
                return info[0];
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            return null;
        }
    }

    private static final Set<TypeKind> NOT_ACCEPTABLE_TYPE_KINDS = EnumSet.of(TypeKind.ERROR, TypeKind.EXECUTABLE, TypeKind.NONE, TypeKind.NULL, TypeKind.OTHER, TypeKind.PACKAGE, TypeKind.WILDCARD, TypeKind.VOID);
    
}
