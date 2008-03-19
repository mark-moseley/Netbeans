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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.plugins.EncapsulateFieldRefactoringPlugin.EncapsulateDesc;
import org.netbeans.modules.refactoring.java.plugins.EncapsulateFieldRefactoringPlugin.Encapsulator;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring.EncapsulateFieldInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** Encapsulate fields refactoring. This is a composed refactoring (uses instances of {@link org.netbeans.modules.refactoring.api.EncapsulateFieldRefactoring}
 * to encapsulate several fields at once.
 *
 * @author Pavel Flaska
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldsPlugin extends JavaRefactoringPlugin {
    
    private List<EncapsulateFieldRefactoringPlugin> refactorings;
    private final EncapsulateFieldsRefactoring refactoring;
    
    private ProgressListener listener = new ProgressListener() {
        public void start(ProgressEvent event) {
            fireProgressListenerStart(event.getOperationType(),event.getCount());
        }

        public void step(ProgressEvent event) {
            fireProgressListenerStep();
        }

       public void stop(ProgressEvent event) {
            fireProgressListenerStop();
        }
    };

    /** Creates a new instance of EcapsulateFields.
     * @param selectedObjects Array of objects (fields) that should be encapsulated.
     */
    public EncapsulateFieldsPlugin(EncapsulateFieldsRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        return validation(2, javac);
    }
    
    @Override
    public Problem fastCheckParameters() {
        Collection<EncapsulateFieldInfo> fields = refactoring.getRefactorFields();
        if (fields.isEmpty()) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateNothingSelected"));
        }
        initRefactorings(fields,
                refactoring.getMethodModifiers(),
                refactoring.getFieldModifiers(),
                refactoring.isAlwaysUseAccessors());
        try {
            return validation(1, null);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        TreePath selectedField = refactoring.getSelectedObject().resolve(javac);
        if (selectedField == null) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "DSC_ElNotAvail"));
        }

        Element elm = javac.getTrees().getElement(selectedField);
        if (elm != null && ElementKind.FIELD == elm.getKind()) {
            TreePath source = javac.getTrees().getPath(elm);
            if (source == null) {
                // missing sources with field declaration
                return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "DSC_ElNotAvail"));
            }
            
            TypeElement encloser = (TypeElement) elm.getEnclosingElement();
            if (ElementKind.INTERFACE == encloser.getKind() || NestingKind.ANONYMOUS == encloser.getNestingKind()) {
                // interface constants, local variables and annonymous declarations are unsupported
                return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateInIntf"));
            }
            return null;
        }

        TreePath clazz = RetoucheUtils.findEnclosingClass(javac, selectedField, true, false, true, false, false);
        TypeElement clazzElm = (TypeElement) javac.getTrees().getElement(clazz);
        if (elm != clazzElm || clazzElm == null) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateWrongType"));
        }
        if (ElementKind.INTERFACE == clazzElm.getKind()
                || ElementKind.ANNOTATION_TYPE == clazzElm.getKind()
                || NestingKind.ANONYMOUS == clazzElm.getNestingKind()) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateInIntf"));
        }

        for (Element member : clazzElm.getEnclosedElements()) {
            if (ElementKind.FIELD == member.getKind()) { // no enum constant
                return null;
            }
        }
        return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateNoFields", clazzElm.getQualifiedName()));
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        Problem problem = null;
        Set<FileObject> references = new HashSet<FileObject>();
        List<EncapsulateDesc> descs = new ArrayList<EncapsulateDesc>(refactorings.size());
        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
            if (cancelRequest) {
                return null;
            }
            
            EncapsulateDesc desc = ref.prepareEncapsulator(problem);
            problem = desc.p;
            desc.p = null;
            if (problem != null && problem.isFatal()) {
                return problem;
            }
            descs.add(desc);
            references.addAll(desc.refs);
        }

        Encapsulator encapsulator = new Encapsulator(descs, problem);
        Problem prob = createAndAddElements(references, new TransformTask(encapsulator, descs.get(0).fieldHandle), elements, refactoring);
        problem = encapsulator.getProblem();
        return prob != null ? prob : problem;
    }
    
    private void initRefactorings(Collection<EncapsulateFieldInfo> refactorFields, Set<Modifier> methodModifier, Set<Modifier> fieldModifier, boolean alwaysUseAccessors) {
        refactorings = new ArrayList<EncapsulateFieldRefactoringPlugin>(refactorFields.size());
        for (EncapsulateFieldInfo info: refactorFields) {
            EncapsulateFieldRefactoring ref = new EncapsulateFieldRefactoring(info.getField());
            ref.setGetterName(info.getGetterName());
            ref.setSetterName(info.getSetterName());
            ref.setMethodModifiers(methodModifier);
            ref.setFieldModifiers(fieldModifier);
            ref.setAlwaysUseAccessors(alwaysUseAccessors);
            refactorings.add(new EncapsulateFieldRefactoringPlugin(ref));
        }
    }
    
    private Problem validation(int phase, CompilationController javac) throws IOException {
        Problem result = null;
        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
            Problem lastresult = null;
            switch (phase) {
            case 1: lastresult = ref.fastCheckParameters(); break;
            case 2:
                lastresult = ref.preCheck(javac);
                result = chainProblems(result, lastresult);
                if (result != null && result.isFatal()) {
                    return result;
                }
                lastresult = ref.checkParameters(javac);
                ref.addProgressListener(listener);
                break;
            }
            
            result = chainProblems(result, lastresult);
            if (result != null && result.isFatal()) {
                return result;
            }
            
        }

        return result;
    }
    
    private static Problem chainProblems(Problem oldp, Problem newp) {
        if (oldp == null) {
            return newp;
        } else if (newp == null) {
            return oldp;
        } else if (newp.isFatal()) {
            newp.setNext(oldp);
            return newp;
        } else {
            // [TODO] performance
            Problem p = oldp;
            while (p.getNext() != null)
                p = p.getNext();
            p.setNext(newp);
            return oldp;
        }
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        TreePathHandle selectedField = refactoring.getSelectedObject();
        FileObject fo = selectedField.getFileObject();
        return JavaSource.forFileObject(fo);
    }

}    
