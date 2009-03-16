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
package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Matula
 */
abstract class WSITRefactoringPlugin<T extends AbstractRefactoring> extends ProgressProviderAdapter implements RefactoringPlugin {
    protected static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.websvc.wsitconf.refactoring");

    protected static final String WS_ANNOTATION = "javax.xml.ws.WebService";
    protected static final String WSDL_LOCATION_ELEMENT = "wsdlLocation";
    
    protected final T refactoring;
    protected final TreePathHandle[] treePathHandle;

    public WSITRefactoringPlugin(T refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class).toArray(new TreePathHandle[0]);
        LOGGER.log(Level.FINE, "refactoring: " + refactoring.getClass().getName() + "; refactoring sources: " + Arrays.asList(treePathHandle));
    }

    public void cancelRequest() {
        // do nothing - WSIT refactoring operations are fast - no need to cancel
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem preCheck() {
        return null;
    }
    
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        LOGGER.log(Level.FINE, "prepare()");

        Problem result = null;
        ClasspathInfo cpInfo = getClasspathInfo();
        JavaSource source = JavaSource.create(cpInfo, treePathHandle[0].getFileObject());
                fireProgressListenerStart(AbstractRefactoring.PREPARE, 5);
                try {
                    source.runUserActionTask(new CancellableTask<CompilationController>() {

                        public void cancel() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        public void run(CompilationController info) throws Exception {
                            info.toPhase(JavaSource.Phase.RESOLVED);
                            for (TreePathHandle tph : treePathHandle) {
                                Element el = tph.resolveElement(info);
                                if (el == null) return;
                                
                                switch (el.getKind()) {
                                case METHOD: {
                                    ElementHandle elh = ElementHandle.create(el);
                                    FileObject file = SourceUtils.getFile(elh, info.getClasspathInfo());

                                    if (file == null) {
                                        ErrorManager.getDefault().log(
                                                ErrorManager.INFORMATIONAL, "WSIT: Null instance returned from SourceUtils.getFile; element not found " + el);
                                        return;
                                    }
                                    fireProgressListenerStep();
                                    Element javaClass = el.getEnclosingElement();
                                    if (isWebSvcFromWsdl(javaClass)) return;
                                    fireProgressListenerStep();
                                    JAXWSSupport supp = JAXWSSupport.getJAXWSSupport(file);
                                    if (supp == null) return;
                                    fireProgressListenerStep();
                                    Project p = FileOwnerQuery.getOwner(file);
                                    if (p == null) return;
                                    WSDLModel model = null;
                                    try {
                                        model = WSITModelSupport.getModelForServiceFromJava(file, p, false, null);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    fireProgressListenerStep();
                                    if (model == null) return;
                                    refactoringElements.add(refactoring, createMethodRE(el.getSimpleName().toString(), model));
                                    fireProgressListenerStep();
                                    break;
                                } case CLASS:
                                case INTERFACE:
                                case ANNOTATION_TYPE:
                                case ENUM: {
                                    ElementHandle elh = ElementHandle.create(el);
                                    FileObject file = SourceUtils.getFile(elh, info.getClasspathInfo());

                                    if (file == null) {
                                        ErrorManager.getDefault().log(
                                                ErrorManager.INFORMATIONAL, "WSIT: Null instance returned from SourceUtils.getFile; element not found " + el);
                                        return;
                                    }
                                    fireProgressListenerStep();
                                    if (isWebSvcFromWsdl(el)) return;
                                    fireProgressListenerStep();
                                    JAXWSSupport supp = JAXWSSupport.getJAXWSSupport(file);
                                    if (supp == null) return;
                                    WSDLModel model = null;
                                    fireProgressListenerStep();
                                    Project p = FileOwnerQuery.getOwner(file);
                                    if (p == null) return;
                                    try {
                                        model = WSITModelSupport.getModelForServiceFromJava(file, p, false, null);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    fireProgressListenerStep();
                                    if (model == null){
                                        return;
                                    }
                                    refactoringElements.addFileChange(refactoring, createClassRE(model));
                                    fireProgressListenerStep();
                                    break;
                                }
                            }
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                throw (RuntimeException) new RuntimeException().initCause(ioe);
            } finally {
                fireProgressListenerStop();
            }
        
        return result;
    }
            
    protected abstract RefactoringElementImplementation createMethodRE(String methodName, WSDLModel model);
    protected abstract RefactoringElementImplementation createClassRE(WSDLModel model);

    protected final ClasspathInfo getClasspathInfo() {
        return refactoring.getContext().lookup(ClasspathInfo.class);
    }
    
    protected static boolean isWebSvcFromWsdl(Element element){
        for (AnnotationMirror ann : element.getAnnotationMirrors()) {
            if (WS_ANNOTATION.equals(((TypeElement) ann.getAnnotationType().asElement()).getQualifiedName())) {
                for (ExecutableElement annVal : ann.getElementValues().keySet()) {
                    if (WSDL_LOCATION_ELEMENT.equals(annVal.getSimpleName().toString())){
                        return true;
                    }
                }
            }
        }
        return false;
    }    
    
    protected static Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(result);
            return problem;
        } else {
            Problem p = result;
            while (p.getNext() != null) {
                p = p.getNext();
            }
            p.setNext(problem);
            return result;
        }
    }

    protected static abstract class AbstractRefactoringElement extends SimpleRefactoringElementImplementation {
        protected final WSDLModel model;
        protected final FileObject file;

        public AbstractRefactoringElement(WSDLModel model) {
            this.model = model;
            this.file = Util.getFOForModel(model);
        }

        /** Returns text describing the refactoring element.
         * @return Text.
         */
        public String getText() {
            return getDisplayText();
        }

        /** Returns file that the element affects (relates to)
         * @return File
         */
        public FileObject getParentFile() {
            return file;
        }

        /** Returns position bounds of the text to be affected by this refactoring element.
         */
        public PositionBounds getPosition() {
            return null;
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }
}
