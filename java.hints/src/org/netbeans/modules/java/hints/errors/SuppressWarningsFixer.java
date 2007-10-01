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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class SuppressWarningsFixer implements ErrorRule<Void> {
    
    /** Creates a new instance of SuppressWarningsFixer */
    public SuppressWarningsFixer() {
    }
    
    private static final Map<String, String> KEY2SUPRESS_KEY;
    
    static {
        Map<String, String> map = new HashMap<String, String>();
        
        String uncheckedKey = "unchecked";
        
        map.put("compiler.warn.prob.found.req", uncheckedKey); // NOI18N
        map.put("compiler.warn.unchecked.cast.to.type", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.assign", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.assign.to.var", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.call.mbr.of.raw.type", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.meth.invocation.applied", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.generic.array.creation", uncheckedKey);  // NOI18N
        
        String fallThroughKey = "fallthrough"; // NOI18N
        
        map.put("compiler.warn.possible.fall-through.into.case", fallThroughKey);  // NOI18N
        
        String deprecationKey = "deprecation";  // NOI18N
        
        map.put("compiler.warn.has.been.deprecated", deprecationKey);  // NOI18N
        
        KEY2SUPRESS_KEY = Collections.unmodifiableMap(map); 
    }
    
    public Set<String> getCodes() {
        return KEY2SUPRESS_KEY.keySet();
    }

    private boolean isSuppressWarningsSupported(CompilationInfo info) {
        //cannot suppress if there is no SuppressWarnings annotation in the platform:
        if (info.getElements().getTypeElement("java.lang.SuppressWarnings") == null)
            return false;
        
        String sourceVersion = SourceLevelQuery.getSourceLevel(info.getFileObject());
        
        if (sourceVersion == null) {
            return true;
        }
        
        try {
            SpecificationVersion version = new SpecificationVersion(sourceVersion);
            SpecificationVersion supp = new SpecificationVersion("1.5");

            return version.compareTo(supp) >= 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }
    
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey,
                         int offset, TreePath treePath,
                         Data<Void> data) {
        if (!isSuppressWarningsSupported(compilationInfo)) {
            return null;
        }

        String suppressKey = KEY2SUPRESS_KEY.get(diagnosticKey);
	
	final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
	
	while (treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(treePath.getLeaf().getKind())) {
	    treePath = treePath.getParentPath();
	}
	
        if (suppressKey != null && treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            return Collections.singletonList((Fix) new FixImpl(TreePathHandle.create(treePath, compilationInfo), compilationInfo.getFileObject(), suppressKey));
        }
        
        return Collections.<Fix>emptyList();
    }

    public void cancel() {
    }

    public String getId() {
        return "SuppressWarningsFixer";  // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_Suppress_Waning");  // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_Suppress_Waning");  // NOI18N
    }

    public static final class FixImpl implements Fix {
        
        private String keys[];
        private TreePathHandle handle;
        private FileObject file;
        
//        public FixImpl(String key, TreePathHandle handle, FileObject file) {
//            this.key = key;
//            this.handle = handle;
//            this.file = file;
//        }
        
        public FixImpl(TreePathHandle handle, FileObject file, String... keys) {
            this.keys = keys;
            this.handle = handle;
            this.file = file;
        }
    
        public String getText() {
            StringBuilder keyNames = new StringBuilder();
            for (int i = 0; i < keys.length; i++) {
                String string = keys[i];
                keyNames.append(string);
                if ( i < keys.length - 1) {
                    keyNames.append(", "); // NOI18N
                }
            }

            return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_FIX_Suppress_Waning",  keyNames.toString() );  // NOI18N
        }
        
        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
        
        public ChangeInfo implement() {
            try {
                JavaSource js = JavaSource.forFileObject(file);
                
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(Phase.RESOLVED); //XXX: performance
                        TreePath path = handle.resolve(copy);
                        
                        while (path.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(path.getLeaf().getKind())) {
                            path = path.getParentPath();
                        }
                        
                        if (path.getLeaf().getKind() == Kind.COMPILATION_UNIT) {
                            return ;
                        }
                        
                        Tree top = path.getLeaf();
                        ModifiersTree modifiers = null;
                        
                        switch (top.getKind()) {
                            case CLASS:
                                modifiers = ((ClassTree) top).getModifiers();
                                break;
                            case METHOD:
                                modifiers = ((MethodTree) top).getModifiers();
                                break;
                            case VARIABLE:
                                modifiers = ((VariableTree) top).getModifiers();
                                break;
                            default: assert false : "Unhandled Tree.Kind";  // NOI18N
                        }
                        
                        if (modifiers == null) {
                            return ;
                        }
                        
                        TypeElement el = copy.getElements().getTypeElement("java.lang.SuppressWarnings");  // NOI18N
                        
                        if (el == null) {
                            return ;
                        }
                        
                        //check for already existing SuppressWarnings annotation:
                        for (AnnotationTree at : modifiers.getAnnotations()) {
                            TreePath tp = new TreePath(new TreePath(path, at), at.getAnnotationType());
                            Element  e  = copy.getTrees().getElement(tp);
                            
                            if (el.equals(e)) {
                                //found SuppressWarnings:
                                List<? extends ExpressionTree> arguments = at.getArguments();
                                
                                if (arguments.isEmpty() || arguments.size() > 1) {
                                    Logger.getLogger(SuppressWarningsFixer.class.getName()).log(Level.INFO, "SupressWarnings annotation has incorrect number of arguments - {0}.", arguments.size());  // NOI18N
                                    return ;
                                }
                                
                                ExpressionTree et = at.getArguments().get(0);
                                
                                if (et.getKind() != Kind.ASSIGNMENT) {
                                    Logger.getLogger(SuppressWarningsFixer.class.getName()).log(Level.INFO, "SupressWarnings annotation's argument is not an assignment - {0}.", et.getKind());  // NOI18N
                                    return ;
                                }
                                
                                AssignmentTree assignment = (AssignmentTree) et;
                                List<? extends ExpressionTree> currentValues = null;
                                
                                if (assignment.getExpression().getKind() == Kind.NEW_ARRAY) {
                                    currentValues = ((NewArrayTree) assignment.getExpression()).getInitializers();
                                } else {
                                    currentValues = Collections.singletonList(assignment.getExpression());
                                }
                                
                                assert currentValues != null;
                                
                                List<ExpressionTree> values = new ArrayList<ExpressionTree>(currentValues);
                                
                                for (String key : keys) {
                                    values.add(copy.getTreeMaker().Literal(key));
                                }

                                
                                copy.rewrite(assignment.getExpression(), copy.getTreeMaker().NewArray(null, Collections.<ExpressionTree>emptyList(), values));
                                return ;
                            }
                        }
                        
                        List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(modifiers.getAnnotations());
                        
                        
                        if ( keys.length > 1 ) {
                            List<LiteralTree> keyLiterals = new ArrayList<LiteralTree>(keys.length);
                            for (String key : keys) {
                                keyLiterals.add(copy.getTreeMaker().Literal(key));
                            }
                            annotations.add(copy.getTreeMaker().Annotation(copy.getTreeMaker().QualIdent(el), 
                                    Collections.singletonList( 
                                        copy.getTreeMaker().NewArray(null, Collections.<ExpressionTree>emptyList(), keyLiterals))));
                        }
                        else {
                            annotations.add(copy.getTreeMaker().Annotation(copy.getTreeMaker().QualIdent(el), Collections.singletonList(copy.getTreeMaker().Literal(keys[0]))));
                        }
                        ModifiersTree nueMods = copy.getTreeMaker().Modifiers(modifiers, annotations);
                        
                        copy.rewrite(modifiers, nueMods);
                    }
                }).commit();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            return null;
        }
    }
}
