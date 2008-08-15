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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.overridden.AnnotationType;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.modules.java.editor.overridden.IsOverriddenAnnotationHandler;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class UncaughtException implements ErrorRule<Void> {
    
    /**
     * Creates a new instance of UncaughtExceptionCreator
     */
    public UncaughtException() {
    }

    private List<? extends TypeMirror> findUncaughtExceptions(CompilationInfo info, TreePath path, List<? extends TypeMirror> exceptions) {
        List<TypeMirror> result = new ArrayList<TypeMirror>();
        
        result.addAll(exceptions);
        
        Tree lastTree = null;
        
        while (path != null) {
            TypeMirror tm = info.getTrees().getTypeMirror(path);
            
            if (tm != null && tm.getKind() == TypeKind.EXECUTABLE) {
                for (TypeMirror mirr : ((ExecutableType) tm).getThrownTypes()) {
                    for (Iterator<TypeMirror> it = result.iterator(); it.hasNext();)
                        if (info.getTypes().isSameType(it.next(), mirr))
                            it.remove();
                }
                break;
            }
            
            Tree currentTree = path.getLeaf();
            
            if (currentTree.getKind() == Kind.TRY) {
                TryTree tt = (TryTree) currentTree;
                
                if (tt.getBlock() == lastTree) {
                    for (CatchTree c : tt.getCatches()) {
                        TreePath catchPath = new TreePath(new TreePath(path, c), c.getParameter());
                        VariableElement variable = (VariableElement) info.getTrees().getElement(catchPath);

                        result.remove(variable.asType());
                    }
                }
            }
            
            lastTree = path.getLeaf();
            path = path.getParentPath();
        }
        
        return result;
    }
    
    public Set<String> getCodes() {
        return Collections.singleton("compiler.err.unreported.exception.need.to.catch.or.throw");
    }
    
    @SuppressWarnings("fallthrough")
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        List<? extends TypeMirror> uncaught = null;
        boolean disableSurroundWithTryCatch = false;
        Element el;
        
        OUTTER: while (path != null) {
            Tree leaf = path.getLeaf();
            
            switch (leaf.getKind()) {
                case METHOD_INVOCATION:
                    //check for super/this constructor call (and disable surround with try-catch):
                    MethodInvocationTree mit = (MethodInvocationTree) leaf;
                    
                    if (mit.getMethodSelect().getKind() == Kind.IDENTIFIER) {
                        String ident = ((IdentifierTree) mit.getMethodSelect()).getName().toString();
                        
                        if ("super".equals(ident) || "this".equals(ident)) {
                            Element element = info.getTrees().getElement(path);
                            
                            disableSurroundWithTryCatch = element != null && element.getKind() == ElementKind.CONSTRUCTOR;
                        }
                    }
                case NEW_CLASS:
                    el = info.getTrees().getElement(path);
		    
		    //IZ 95535 -- dont't offer surround with T-C for fields
		    if(!isInsideMethod(path))
			disableSurroundWithTryCatch = true;
		    
		    if(isThisParameter(path)) {
			disableSurroundWithTryCatch = el != null && (el.getKind() == ElementKind.CONSTRUCTOR || el.getKind() == ElementKind.METHOD);
		    }
		    
                    if (el != null && EXECUTABLE_ELEMENTS.contains(el.getKind())) {
			TypeMirror uncaughtException;
			if(leaf.getKind() == Kind.NEW_CLASS)
			    uncaughtException = info.getTrees().getTypeMirror(new TreePath(path, ((NewClassTree) leaf).getIdentifier()));
			else
			    uncaughtException = info.getTrees().getTypeMirror(new TreePath(path, ((MethodInvocationTree) leaf).getMethodSelect()));
			
			if(uncaughtException != null && uncaughtException.getKind() == TypeKind.EXECUTABLE)
			    uncaught = ((ExecutableType) uncaughtException).getThrownTypes();
			else
			    uncaught = ((ExecutableElement) el).getThrownTypes();
                    }
                    path = path.getParentPath();
                    break OUTTER;
                case THROW:
                    TypeMirror uncaughtException = info.getTrees().getTypeMirror(new TreePath(path, ((ThrowTree) leaf).getExpression()));
                    uncaught = Collections.singletonList(uncaughtException);
                    break OUTTER;
            }
            
            path = path.getParentPath();
        }
        
        if (uncaught != null) {
            uncaught = findUncaughtExceptions(info, path, uncaught);
            
            TreePath pathRec = path;
            
            while (pathRec != null && pathRec.getLeaf().getKind() != Kind.METHOD) {
                pathRec = pathRec.getParentPath();
            }
            
            ExecutableElement method = pathRec != null ? (ExecutableElement) info.getTrees().getElement(pathRec)  : null;
            
            if (method != null) {
                //if the method header is inside a guarded block, do nothing:
                if (!org.netbeans.modules.java.hints.errors.Utilities.isMethodHeaderInsideGuardedBlock(info, (MethodTree) pathRec.getLeaf())) {
                    List<ElementDescription> eds = new LinkedList<ElementDescription>();
                    TypeElement enclosingType = (TypeElement) method.getEnclosingElement();
                    AnnotationType at = IsOverriddenAnnotationHandler.detectOverrides(info, enclosingType, method,eds);
                    List<TypeMirror> declaredThrows = null;
                    
                    if (at != null) {
                        declaredThrows = new LinkedList<TypeMirror>();

                        for (ElementDescription ed : eds) {
                            ExecutableElement ee = (ExecutableElement) ed.getHandle().resolve(info);
                            ExecutableType et = (ExecutableType) info.getTypes().asMemberOf((DeclaredType) enclosingType.asType(), ee);
                            List<TypeMirror> thisDeclaredThrows = new LinkedList<TypeMirror>(et.getThrownTypes());
                            
                            if (!thisDeclaredThrows.isEmpty()) {
                                for (Iterator<TypeMirror> dt = declaredThrows.iterator(); dt.hasNext();) {
                                    for (Iterator<TypeMirror> tdt = thisDeclaredThrows.iterator(); tdt.hasNext();) {
                                        TypeMirror dtNext = dt.next();
                                        TypeMirror tdtNext = tdt.next();

                                        if (info.getTypes().isSubtype(tdtNext, dtNext)) {
                                            tdt.remove();
                                            continue;
                                        }

                                        if (info.getTypes().isSubtype(dtNext, tdtNext)) {
                                            dt.remove();
                                            continue;
                                        }

                                        tdt.remove();
                                        dt.remove();
                                    }
                                }
                            }
                            declaredThrows.addAll(thisDeclaredThrows);
                        }
                    }

                    for (TypeMirror tm : uncaught) {
                        if (declaredThrows != null) {
                            boolean found = false;

                            for (TypeMirror decl : declaredThrows) {
                                if (info.getTypes().isSubtype(tm, decl)) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                continue;
                            }
                        }
                        
                        if (tm.getKind() != TypeKind.ERROR) {
                            result.add(new AddThrowsClauseHintImpl(info.getJavaSource(), Utilities.getTypeName(tm, true).toString(), TypeMirrorHandle.create(tm), ElementHandle.create(method)));
                        }
                    }
                }
            }
            
            if (!uncaught.isEmpty() && !disableSurroundWithTryCatch) {
                List<TypeMirrorHandle> thandles = new ArrayList<TypeMirrorHandle>();
                List<String> fqns = new ArrayList<String>();
                
                for (TypeMirror tm : uncaught) {
                    if (tm.getKind() != TypeKind.ERROR) {
                        thandles.add(TypeMirrorHandle.create(tm));
                        fqns.add(Utilities.getTypeName(tm, true).toString());
                    }
                }
                
                if (ErrorFixesFakeHint.enabled(ErrorFixesFakeHint.FixKind.SURROUND_WITH_TRY_CATCH)) {
                    result.add(new OrigSurroundWithTryCatchFix(info.getJavaSource(), thandles, TreePathHandle.create(path, info), fqns));
                    //#134408: "Surround Block with try-catch" is redundant when the block contains just a single statement
                    TreePath tp = findBlock(path);
                    boolean magic = true;
                    if(tp != null && tp.getLeaf().getKind() == Kind.BLOCK) {
                        magic = ((BlockTree) tp.getLeaf()).getStatements().size() != 1;
                    }
                    if(magic)
                        result.add(new MagicSurroundWithTryCatchFix(info.getJavaSource(), thandles, offset, method != null ? ElementHandle.create(method) : null, fqns));
                }
            }
        }
        
        return result;
    }
    
    private TreePath findBlock(TreePath path) {
        while (path != null && path.getLeaf().getKind() != Kind.BLOCK) {
            path = path.getParentPath();
        }
        return path;
    }
    
    /**
     * Detects if we are parameter of this() or super() call
     * @return true if yes
     */ 
    private boolean isThisParameter(TreePath path) {
	//anonymous class must not be on the path to top
	while(path.getLeaf().getKind() != Kind.CLASS && path.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
	    if (path.getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
		MethodInvocationTree mi = (MethodInvocationTree) path.getParentPath().getLeaf();
		if(mi.getMethodSelect().getKind() == Kind.IDENTIFIER) {
		    String id = ((IdentifierTree) mi.getMethodSelect()).getName().toString();
		    if ("super".equals(id) || "this".equals(id))
			return true;
		}
	    }
	    path = path.getParentPath();
	}
	return false;
    }
    
    private static boolean isInsideMethod(TreePath tp) {
        while (tp != null) {
            if (tp.getLeaf().getKind() == Kind.METHOD)
                return true;
            
            tp = tp.getParentPath();
        }
        
        return false;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return UncaughtException.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(UncaughtException.class, "DN_AddThrowsClauseAndSurround");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(UncaughtException.class, "DESC_AddThrowsClauseAndSurround");
    }
    
    private static final Set<ElementKind> EXECUTABLE_ELEMENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind. METHOD);
    
    private static final class AddThrowsClauseHintImpl implements Fix {

        private JavaSource js;
        private String fqn;
        private TypeMirrorHandle thandle;
        private ElementHandle<ExecutableElement> method;
        
        public AddThrowsClauseHintImpl(JavaSource js, String fqn, TypeMirrorHandle thandle, ElementHandle<ExecutableElement> method) {
            this.js = js;
            this.fqn = fqn;
            this.thandle = thandle;
            this.method = method;
        }
        
        public String getText() {
            return NbBundle.getMessage(UncaughtException.class, "FIX_AddThrowsClause", new Object[]{String.valueOf(fqn)});
        }
        
        public ChangeInfo implement() throws IOException {
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.RESOLVED);
                    Tree tree = wc.getTrees().getTree(method.resolve(wc));

                    assert tree != null;
                    assert tree.getKind() == Kind.METHOD;

                    MethodTree nue = wc.getTreeMaker().addMethodThrows((MethodTree) tree, (ExpressionTree) wc.getTreeMaker().Type(thandle.resolve(wc)));

                    wc.rewrite(tree, nue);
                }
            }).commit();
            return null;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AddThrowsClauseHintImpl other = (AddThrowsClauseHintImpl) obj;
            if (this.js != other.js && (this.js == null || !this.js.equals(other.js))) {
                return false;
            }
            if (this.fqn == null || !this.fqn.equals(other.fqn)) {
                return false;
            }
            if (this.method != other.method && (this.method == null || !this.method.equals(other.method))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 13 * hash + (this.fqn != null ? this.fqn.hashCode() : 0);
            hash = 13 * hash + (this.method != null ? this.method.hashCode() : 0);
            return hash;
        }
        
    }
    
    static final Set<Kind> STATEMENT_KINDS;
    
    static {
        Set<Kind> kinds = new HashSet<Kind>();
        
        for (Kind k : Kind.values()) {
            Class c = k.asInterface();
            
            if (c != null && StatementTree.class.isAssignableFrom(c)) {
                kinds.add(k);
            }
        }
        
        STATEMENT_KINDS = Collections.unmodifiableSet(EnumSet.copyOf(kinds));
    }
    
}
