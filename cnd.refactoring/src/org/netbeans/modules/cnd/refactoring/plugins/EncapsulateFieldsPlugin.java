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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldsRefactoring;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldsRefactoring.EncapsulateFieldInfo;
import org.netbeans.modules.cnd.refactoring.hints.infrastructure.Utilities;
import org.netbeans.modules.cnd.refactoring.support.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.openide.util.NbBundle;

/** Encapsulate fields refactoring. This is a composed refactoring (uses instances of {@link org.netbeans.modules.refactoring.api.EncapsulateFieldRefactoring}
 * to encapsulate several fields at once.
 *
 * @author Pavel Flaska
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldsPlugin extends CsmModificationRefactoringPlugin {

    private static final int FAST_CHECK_PARAMETERS = 1;
    private static final int CHECK_PARAMETERS = 2;
    private List<EncapsulateFieldRefactoringPlugin> refactorings;
    private final EncapsulateFieldsRefactoring refactoring;
    // objects affected by refactoring
    private Collection<CsmField> referencedFields = new ArrayList<CsmField>();
    private CsmClass enclosingClass;
    private ProgressListener listener = new ProgressListener() {

        public void start(ProgressEvent event) {
            fireProgressListenerStart(event.getOperationType(), event.getCount());
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
        super(refactoring);
        this.refactoring = refactoring;
    }

    @Override
    public Problem checkParameters() {
        return validation(CHECK_PARAMETERS);
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
                refactoring.isAlwaysUseAccessors(),
                refactoring.isMethodInline());
        return validation(FAST_CHECK_PARAMETERS);
    }

    private CsmObject getRefactoredCsmElement() {
        CsmObject out = getStartReferenceObject();
        if (out == null) {
            CsmContext editorContext = getEditorContext();
            if (editorContext != null) {
                out = editorContext.getObjectUnderOffset();
                if (!CsmKindUtilities.isField(out)) {
                    out = Utilities.extractEnclosingClass(getEditorContext());
                }
            }
        }
        return out;
    }

    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 4);
        // check if resolved element
        Collection<EncapsulateFieldInfo> fieldsInfo = refactoring.getRefactorFields();
        CsmObject refactoredElement = getRefactoredCsmElement();
        preCheckProblem = fieldsInfo.isEmpty() ? isResovledElement(refactoredElement) : null;
        fireProgressListenerStep();
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        // check if valid element
        CsmObject directReferencedObject = CsmRefactoringUtils.getReferencedElement(refactoredElement);
        initReferencedObjects(directReferencedObject, fieldsInfo);
        fireProgressListenerStep();
        // support only fields and enclosing classes
        if (this.enclosingClass == null) {
            preCheckProblem = createProblem(preCheckProblem, true, getString("ERR_EncapsulateWrongType")); // NOI18N
            return preCheckProblem;
        }
        // check read-only elements
        preCheckProblem = checkIfModificationPossible(preCheckProblem, this.enclosingClass, "", ""); // NOI18N
        fireProgressListenerStop();
        if (fieldsInfo.isEmpty()) {
            // check that class has at least one field
            for (CsmMember csmMember : this.enclosingClass.getMembers()) {
                if (CsmKindUtilities.isField(csmMember)) {
                    return null;
                }
            }
            return new Problem(true, getString("ERR_EncapsulateNoFields", enclosingClass.getQualifiedName().toString())); // NOI18N
        } else {
            return null;
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(EncapsulateFieldsPlugin.class, key);
    }

    private static String getString(String key, String param) {
        return NbBundle.getMessage(EncapsulateFieldsPlugin.class, key, param);
    }
//    public Problem prepare(RefactoringElementsBag elements) {
//        Problem problem = null;
//        Set<FileObject> references = new HashSet<FileObject>();
//        List<EncapsulateDesc> descs = new ArrayList<EncapsulateDesc>(refactorings.size());
//        fireProgressListenerStart(ProgressEvent.START, refactorings.size() + 1);
//        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
//            if (cancelRequest) {
//                return null;
//            }
//
//            EncapsulateDesc desc = ref.prepareEncapsulator(problem);
//            problem = desc.p;
//            desc.p = null;
//            if (problem != null && problem.isFatal()) {
//                return problem;
//            }
//            descs.add(desc);
//            references.addAll(desc.refs);
//            fireProgressListenerStep();
//        }
//
//        Encapsulator encapsulator = new Encapsulator(descs, problem,
//                refactoring.getContext().lookup(InsertPoint.class),
//                refactoring.getContext().lookup(SortBy.class),
//                refactoring.getContext().lookup(Javadoc.class)
//                );
//        Problem prob = createAndAddElements(references, new TransformTask(encapsulator, descs.get(0).fieldHandle), elements, refactoring);
//        fireProgressListenerStop();
//        problem = encapsulator.getProblem();
//        return prob != null ? prob : problem;
//    }

    private void initRefactorings(Collection<EncapsulateFieldInfo> refactorFields, Set<CsmVisibility> methodModifier, Set<CsmVisibility> fieldModifier, 
            boolean alwaysUseAccessors, boolean methodInline) {
        refactorings = new ArrayList<EncapsulateFieldRefactoringPlugin>(refactorFields.size());
        for (EncapsulateFieldInfo info : refactorFields) {
            EncapsulateFieldRefactoring ref = new EncapsulateFieldRefactoring(info.getField());
            ref.setGetterName(info.getGetterName());
            ref.setSetterName(info.getSetterName());
            ref.setMethodModifiers(methodModifier);
            ref.setFieldModifiers(fieldModifier);
            ref.setAlwaysUseAccessors(alwaysUseAccessors);
            ref.setMethodInline(methodInline);
            refactorings.add(new EncapsulateFieldRefactoringPlugin(ref));
        }
    }

    private Problem validation(int phase) {
        Problem result = null;
        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
            Problem lastresult = null;
            switch (phase) {
                case FAST_CHECK_PARAMETERS:
                    lastresult = ref.fastCheckParameters();
                    break;
                case CHECK_PARAMETERS:
                    lastresult = ref.preCheck();
                    result = chainProblems(result, lastresult);
                    if (result != null && result.isFatal()) {
                        return result;
                    }
                    lastresult = ref.checkParameters();
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
            while (p.getNext() != null) {
                p = p.getNext();
            }
            p.setNext(newp);
            return oldp;
        }
    }

    @Override
    protected Collection<? extends CsmObject> getRefactoredObjects() {
        return referencedFields;
    }

    private void initReferencedObjects(CsmObject referencedObject, Collection<EncapsulateFieldInfo> fieldsInfo) {
        referencedFields = new ArrayList<CsmField>(fieldsInfo.size());
        if (!fieldsInfo.isEmpty()) {
            for (EncapsulateFieldInfo info : fieldsInfo) {
                final CsmField field = info.getField();
                referencedFields.add(field);
                if (enclosingClass == null) {
                    enclosingClass = field.getContainingClass();
                }
            }
        } else if (referencedObject != null) {
            if (CsmKindUtilities.isClass(referencedObject)) {
                this.enclosingClass = (CsmClass) referencedObject;
            } else if (CsmKindUtilities.isField(referencedObject)) {
                this.enclosingClass = ((CsmField) referencedObject).getContainingClass();
            }
        }
    }

    @Override
    protected void processFile(CsmFile csmFile, ModificationResult mr) {
        
    }
}    
