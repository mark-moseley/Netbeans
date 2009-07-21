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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class ImplementAbstractMethods extends ModelRule {
    public String getId() {
        return "Implement.Abstract.Methods";//NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(ImplementAbstractMethods.class, "ImplementAbstractMethodsDesc");//NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ImplementAbstractMethods.class, "ImplementAbstractMethodsDispName");//NOI18N
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }


    public boolean showInTasklist() {
        return false;
    }

    @Override
    void check(FileScope fileScope, RuleContext context, List<Hint> hints) {
        Collection<? extends TypeScope> allTypes = ModelUtils.getDeclaredTypes(fileScope);
        for (FixInfo fixInfo : checkHints(allTypes)) {
            hints.add(new Hint(ImplementAbstractMethods.this, getDisplayName(),
                    context.parserResult.getSnapshot().getSource().getFileObject(), fixInfo.classNameRange,
                    Collections.<HintFix>singletonList(new Fix(context,
                    fixInfo)), 500));
        }
    }

    private Collection<FixInfo> checkHints(Collection<? extends TypeScope> allTypes) {
        List<FixInfo> retval = new ArrayList<FixInfo>();
        for (TypeScope typeScope : allTypes) {
            LinkedHashSet<MethodScope> abstrMethods = new LinkedHashSet<MethodScope>();
            ClassScope cls = (typeScope instanceof ClassScope) ? ModelUtils.getFirst(((ClassScope) typeScope).getSuperClasses()) : null;
            Collection<? extends InterfaceScope> interfaces = typeScope.getSuperInterfaces();
            if ((cls != null || interfaces.size() > 0) && !typeScope.getPhpModifiers().isAbstract() && typeScope instanceof ClassScope) {
                Set<String> methNames = new HashSet<String>();
                Collection<? extends MethodScope> allInheritedMethods = typeScope.getMethods();
                Collection<? extends MethodScope> allMethods = typeScope.getDeclaredMethods();
                Set<String> methodNames = new HashSet<String>();
                for (MethodScope methodScope : allMethods) {
                    methodNames.add(methodScope.getName());
                }
                for (MethodScope methodScope : allInheritedMethods) {
                    Scope inScope = methodScope.getInScope();
                    if (inScope instanceof InterfaceScope || methodScope.getPhpModifiers().isAbstract()) {
                        if (!methodNames.contains(methodScope.getName())) {
                            abstrMethods.add(methodScope);
                        }
                    } else {
                        methNames.add(methodScope.getName());
                    }
                }
                for (Iterator<? extends MethodScope> it = abstrMethods.iterator(); it.hasNext();) {
                    MethodScope methodScope = it.next();
                    if (methNames.contains(methodScope.getName())) {
                        it.remove();
                    }
                }
            }
            if (!abstrMethods.isEmpty()) {
                LinkedHashSet<String> methodSkeletons = new LinkedHashSet<String>();
                for (MethodScope methodScope : abstrMethods) {
                    String skeleton = methodScope.getClassSkeleton();
                    skeleton = skeleton.replace("abstract ", ""); //NOI18N
                    methodSkeletons.add(skeleton);
                }
                retval.add(new FixInfo(typeScope, methodSkeletons));
            }
        }
        return retval;
    }

    private class Fix implements HintFix {

        private RuleContext context;
        private final FixInfo fixInfo;

        Fix(RuleContext context, FixInfo fixInfo) {
            this.context = context;
            this.fixInfo = fixInfo;
        }

        public String getDescription() {
            return ImplementAbstractMethods.this.getDescription();
        }

        public void implement() throws Exception {
            getEditList().apply();
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }

        EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);
            for (String methodScope : fixInfo.methodSkeletons) {
                edits.replace(fixInfo.offset, 0, methodScope, true, 0);
            }
            return edits;
        }
    }

    private static class FixInfo {
        private LinkedHashSet<String> methodSkeletons;
        private int offset;
        private OffsetRange classNameRange;

        FixInfo(TypeScope typeScope, LinkedHashSet<String> methodSkeletons) {
            this.methodSkeletons = methodSkeletons;
            this.classNameRange = typeScope.getNameRange();
            this.offset = typeScope.getBlockRange().getEnd() - 1;
        }
    }
}
