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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.elements.DiffElement;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.cnd.refactoring.support.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.api.*;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * base class for C/C++ refactoring plug-ins
 * 
 * @author Vladimir Voskresensky
 */
public abstract class CsmRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    protected enum Phase {PRECHECK, FASTCHECKPARAMETERS, CHECKPARAMETERS, PREPARE, DEFAULT};
    private Phase whatRun = Phase.DEFAULT;
    private Problem problem;
    protected volatile boolean cancelRequest = false;
    
    public void cancel() {
    }
    
    public Problem preCheck() {
        return run(Phase.PRECHECK);
    }

    public Problem checkParameters() {
        return run(Phase.CHECKPARAMETERS);
    }

    public Problem fastCheckParameters() {
        return run(Phase.FASTCHECKPARAMETERS);
    }

    private Problem run(Phase s) {
        this.whatRun = s;
        this.problem = null;
//        if (js==null) {
//            return null;
//        }
//        try {
//            js.runUserActionTask(this, true);
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
        return problem;
    }
    
    public void cancelRequest() {
        cancelRequest = true;
//        if (currentTask!=null) {
//            currentTask.cancel();
//        }
    }
    
    protected ModificationResult processFiles(Collection<CsmFile> files) {
        return null;
    }
    
    private Collection<ModificationResult> processFiles(Iterable<? extends List<CsmFile>> fileGroups) {
        Collection<ModificationResult> results = new LinkedList<ModificationResult>();
        for (List<CsmFile> list : fileGroups) {
            if (cancelRequest) {
                // may be return partial "results"?
                return Collections.<ModificationResult>emptyList();
            }
            ModificationResult modification = processFiles(list);
            if (modification != null) {
                results.add(modification);
            }
        }
        return results;
    }
    
    protected final void createAndAddElements(Collection<CsmFile> files, RefactoringElementsBag elements, AbstractRefactoring refactoring) {
        Iterable<? extends List<CsmFile>> fileGroups = groupByRoot(files);
        final Collection<ModificationResult> results = processFiles(fileGroups);
        elements.registerTransaction(new RefactoringCommit(results));
        for (ModificationResult result:results) {
            for (FileObject fo : result.getModifiedFileObjects()) {
                for (Difference dif: result.getDifferences(fo)) {
                        elements.add(refactoring,DiffElement.create(dif, fo, result));
                }
            }
        }
    }
    
    protected static final Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(result);
            return problem;
        } else {
            //problem.setNext(result.getNext());
            //result.setNext(problem);
            
            // [TODO] performance
            Problem p = result;
            while (p.getNext() != null)
                p = p.getNext();
            p.setNext(problem);
            return result;
        }
    }
    
    private Iterable<? extends List<CsmFile>> groupByRoot (Iterable<? extends CsmFile> files) {
        Map<CsmProject,List<CsmFile>> result = new HashMap<CsmProject,List<CsmFile>> ();
        for (CsmFile file : files) {
            CsmProject prj = file.getProject();
            if (prj != null) {
                List<CsmFile> group = result.get(prj);
                if (group == null) {
                    group = new LinkedList<CsmFile>();
                    result.put(prj, group);
                }
                group.add(file);
            }
        }
        return result.values();
    }          
    
    protected final CsmFile getCsmFile(CsmObject csmObject) {
        if (CsmKindUtilities.isFile(csmObject)) {
            return ((CsmFile)csmObject);
        } else if (CsmKindUtilities.isOffsetable(csmObject)) {
            return ((CsmOffsetable)csmObject).getContainingFile();
        }
        return null;
    }  
    
    protected Collection<CsmFile> getRelevantFiles(CsmFile startFile, CsmObject referencedObject) {
        CsmObject enclScope = referencedObject == null ? null : CsmRefactoringUtils.getEnclosingElement(referencedObject);
        CsmFile scopeFile = null;
        if (CsmKindUtilities.isFunction(enclScope)) {
            scopeFile = ((CsmOffsetable)enclScope).getContainingFile();
        }
        if (startFile.equals(scopeFile)) {
            return Collections.singleton(scopeFile);
        } else {
            CsmProject prj = startFile.getProject();
            return prj.getAllFiles();
        }
    }   
    
    protected Problem isResovledElement(CsmObject ref) {
        if (ref==null) {
            //reference is null or is not valid.
            return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElNotAvail")); // NOI18N
        } else {
            CsmObject referencedObject = CsmRefactoringUtils.getReferencedElement(ref);
            if (referencedObject == null) {
                return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElementNotResolved"));
            }
            if (CsmKindUtilities.isValidable(referencedObject) && !((CsmValidable)referencedObject).isValid()) {
                return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElementNotResolved"));
            }            
            // element is still available
            return null;
        }
    }    
}
