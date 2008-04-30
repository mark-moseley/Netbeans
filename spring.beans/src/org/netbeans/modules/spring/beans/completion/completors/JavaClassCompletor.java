/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner6;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.LazyTypeCompletionItem;
import org.netbeans.modules.spring.beans.completion.QueryProgress;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.java.JavaUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class JavaClassCompletor extends Completor {

    public JavaClassCompletor() {
    }

    @Override
    protected void computeCompletionItems(CompletionContext context, QueryProgress progress) {
        try {
            final String typedChars = context.getTypedPrefix();

            JavaSource js = JavaUtils.getJavaSource(context.getFileObject());
            if (js == null) {
                return;
            }

            if (typedChars.contains(".") || typedChars.equals("")) { // Switch to normal completion
                doNormalJavaCompletion(js, progress, typedChars, context.getCurrentToken().getOffset() + 1);
            } else { // Switch to smart class path completion
                doSmartJavaCompletion(js, progress, typedChars, context.getCurrentToken().getOffset() + 1);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void doNormalJavaCompletion(JavaSource js, final QueryProgress progress,
            final String typedPrefix, final int substitutionOffset) throws IOException {
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws Exception {
                if(progress.isCancelled()) {
                    return;
                }
                
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                ClassIndex ci = cc.getJavaSource().getClasspathInfo().getClassIndex();
                int index = substitutionOffset;
                String packName = typedPrefix;
                int dotIndex = typedPrefix.lastIndexOf('.'); // NOI18N

                if (dotIndex != -1) {
                    index += (dotIndex + 1);  // NOI18N

                    packName = typedPrefix.substring(0, dotIndex);
                }
                addPackages(ci, progress, typedPrefix, index);

                if(progress.isCancelled()) {
                    return;
                }
                
                PackageElement pkgElem = cc.getElements().getPackageElement(packName);
                if (pkgElem == null) {
                    return;
                }

                // get this as well as non-static inner classes
                List<TypeElement> tes = new TypeScanner().scan(pkgElem);
                for (TypeElement te : tes) {
                    if(progress.isCancelled()) {
                        return;
                    }
                    
                    if (ElementUtilities.getBinaryName(te).startsWith(typedPrefix)) {
                        SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createTypeItem(substitutionOffset,
                                te, ElementHandle.create(te), cc.getElements().isDeprecated(te), false);
                        addItem(item);
                    }
                }

                setAnchorOffset(index);
            }
        }, true);
    }

    private void doSmartJavaCompletion(final JavaSource js, final QueryProgress progress,
            final String typedPrefix, final int substitutionOffset) throws IOException {
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws Exception {
                if(progress.isCancelled()) {
                    return;
                }
                
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                
                ClassIndex ci = cc.getJavaSource().getClasspathInfo().getClassIndex();
                // add packages
                addPackages(ci, progress, typedPrefix, substitutionOffset);
                if(progress.isCancelled()) {
                    return;
                }
                
                // add classes 
                Set<ElementHandle<TypeElement>> matchingTypes = ci.getDeclaredTypes(typedPrefix,
                        NameKind.CASE_INSENSITIVE_PREFIX, EnumSet.allOf(SearchScope.class));
                for (ElementHandle<TypeElement> eh : matchingTypes) {
                    if(progress.isCancelled()) {
                        return;
                    }
                    if (eh.getKind() == ElementKind.CLASS) {
                        LazyTypeCompletionItem item = LazyTypeCompletionItem.create(substitutionOffset, eh, js);
                        addItem(item);
                    }
                }
            }
        }, true);

        setAnchorOffset(substitutionOffset);
    }

    private static boolean isAccessibleClass(TypeElement te) {
        NestingKind nestingKind = te.getNestingKind();
        return (nestingKind == NestingKind.TOP_LEVEL) || (nestingKind == NestingKind.MEMBER && te.getModifiers().contains(Modifier.STATIC));
    }

    private void addPackages(ClassIndex ci, QueryProgress progress, String typedPrefix, int substitutionOffset) {
        Set<String> packages = ci.getPackageNames(typedPrefix, true, EnumSet.allOf(SearchScope.class));
        for (String pkg : packages) {
            if (pkg.length() > 0) {
                SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createPackageItem(substitutionOffset, pkg, false);
                addItem(item);
            }
        }
    }

    private static final class TypeScanner extends ElementScanner6<List<TypeElement>, Void> {

        public TypeScanner() {
            super(new ArrayList<TypeElement>());
        }

        @Override
        public List<TypeElement> visitType(TypeElement typeElement, Void arg) {
            if (typeElement.getKind() == ElementKind.CLASS && isAccessibleClass(typeElement)) {
                DEFAULT_VALUE.add(typeElement);
            }
            return super.visitType(typeElement, arg);
        }
    }


}
