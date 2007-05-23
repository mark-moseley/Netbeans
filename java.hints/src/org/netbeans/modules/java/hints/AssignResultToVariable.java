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
package org.netbeans.modules.java.hints;

//import org.netbeans.modules.javahints.*;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class AssignResultToVariable extends AbstractHint {
    
    public AssignResultToVariable() {
        super(true, false, AbstractHint.HintSeverity.CURRENT_LINE_WARNING);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD_INVOCATION);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        try {
            if (treePath.getParentPath().getLeaf().getKind() != Kind.EXPRESSION_STATEMENT)
                return null;
            
            MethodInvocationTree mit    = (MethodInvocationTree) treePath.getLeaf();
            ExpressionTree       method = mit.getMethodSelect();
            
            long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), method);
            long end   = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), method);
            int   offset = CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject());
            
            if (start == (-1) || end == (-1) || offset < start || offset > end) {
                return null;
            }
            
            Element e = info.getTrees().getElement(treePath);
            
            if (e == null || e.getKind() != ElementKind.METHOD) {
                return null;
            }
            
            ExecutableElement ee = (ExecutableElement) e;
            
            if (ee.getReturnType() == null || ee.getReturnType().getKind() == TypeKind.VOID) {
                return null;
            }
            
            List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(info.getFileObject(), info.getDocument(), TreePathHandle.create(treePath, info)));
            
            return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), "Assign Return Value To New Variable", fixes, info.getFileObject(), (int) start, (int) end));
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
        return "AssignResultToVariable";
    }

    public String getDescription() {
        return "AssignResultToVariable";
    }

    private static final class FixImpl implements Fix {
        
        private FileObject file;
        private Document doc;
        private TreePathHandle tph;
        
        public FixImpl(FileObject file, Document doc, TreePathHandle tph) {
            this.file = file;
            this.doc = doc;
            this.tph = tph;
        }

        public String getText() {
            return "Assign Return Value To New Variable";
        }

        public ChangeInfo implement() {
            try {
                final String[] name = new String[1];
                ModificationResult result = JavaSource.forFileObject(file).runModificationTask(new CancellableTask<WorkingCopy>() {
                    public void cancel() {}
                    
                    public void run(WorkingCopy copy) throws Exception {
                        copy.toPhase(Phase.RESOLVED);
                        
                        TreePath tp = tph.resolve(copy);
                        
                        if (tp == null) {
                            Logger.getLogger(AssignResultToVariable.class.getName()).info("tp=null");
                            return ;
                        }
                        
                        Element  el = copy.getTrees().getElement(tp);
                        
                        if (el == null || el.getKind() != ElementKind.METHOD) {
                            return ;
                        }
                        
                        ExecutableElement ee = (ExecutableElement) el;
                        TreeMaker make = copy.getTreeMaker();
                        
                        name[0] = guessName(copy, tp);
                        
                        VariableTree var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name[0], make.Type(ee.getReturnType()), (ExpressionTree) tp.getLeaf());
                        
                        copy.rewrite(tp.getParentPath().getLeaf(), var);
                    }
                });
                
                List<? extends Difference> differences = result.getDifferences(file);
                
                if (differences == null) {
                    Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.INFO, "No differences.");
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
                    if (d.getNewText().contains(name[0])) {
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
                    Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.INFO, "Cannot find the difference: {0}", differences);
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
                            Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.FINE, "text after commit: {0}", text);
                            int    relPos = text.lastIndexOf(name[0]);
                            
                            Logger.getLogger(AssignResultToVariable.class.getName()).log(Level.FINE, "relPos: {0}", relPos);
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

    static String guessName(CompilationInfo info, TreePath tp) {
        String name = adjustName(SuspiciousNamesCombination.getName((ExpressionTree) tp.getLeaf()));
        
        if (name == null) {
            return "name";
        }
        
        Scope s = info.getTrees().getScope(tp);
        int counter = 0;
        boolean cont = true;
        String proposedName = name;
        
        while (cont) {
            proposedName = name + (counter != 0 ? String.valueOf(counter) : "");
            
            cont = false;
            
            for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
                if (proposedName.equals(e.getSimpleName().toString())) {
                    counter++;
                    cont = true;
                    break;
                }
            }
        }
        
        return proposedName;
    }
    
    private static String adjustName(String name) {
        if (name == null)
            return null;
        
        String shortName = null;
        
        if (name.startsWith("get") && name.length() > 3) {
            shortName = name.substring(3);
        }
        
        if (name.startsWith("is") && name.length() > 2) {
            shortName = name.substring(2);
        }
        
        if (shortName != null) {
            return Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1);
        }
        
        return name;
    }
    
    private static final class VariablesFilter implements ElementAcceptor {
        
        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
        
        public boolean accept(Element e, TypeMirror type) {
            return ACCEPTABLE_KINDS.contains(e.getKind());
        }
        
    }
}
