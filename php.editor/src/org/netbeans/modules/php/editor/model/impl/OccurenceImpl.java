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
package org.netbeans.modules.php.editor.model.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Occurence;

/**
 *
 * @author Radek Matous
 */
class OccurenceImpl<TDeclaration extends ModelElement> implements Occurence<TDeclaration> {
    private OffsetRange occurenceRange;
    private TDeclaration declaration;
    private List<? extends TDeclaration> allDeclarations;
    private FileScope fileScope;

    public OccurenceImpl(List<? extends TDeclaration> allDeclarations, OffsetRange occurenceRange,FileScope fileScope) {
        this.allDeclarations = allDeclarations;
        this.declaration = ModelUtils.getFirst(allDeclarations);
        //TODO: wrong bugfix when sometimes is offered just one declaration
        if (this.allDeclarations.size() == 1) {
            this.allDeclarations = null;
        }
        this.occurenceRange = occurenceRange;
        this.fileScope = fileScope;
    }

    public OccurenceImpl(TDeclaration declaration, OffsetRange occurenceRange, FileScope fileScope) {
        this.occurenceRange = occurenceRange;
        this.declaration = declaration;
        this.fileScope = fileScope;
    }

    public TDeclaration getDeclaration() {
        return declaration;
    }

    public OffsetRange getOffsetRange() {
        return occurenceRange;
    }

    public int getOffset() {
        return getOffsetRange().getStart();
    }

    @SuppressWarnings("unchecked")
    public List<? extends TDeclaration> getAllDeclarations() {
        if (allDeclarations == null) {
            allDeclarations = Collections.<TDeclaration>emptyList();
            ModelScopeImpl modelScope = (ModelScopeImpl) ModelUtils.getModelScope(getDeclaration());
            IndexScopeImpl indexScope = modelScope.getIndexScope();
            switch (getDeclaration().getPhpKind()) {
                case CONSTANT:
                    allDeclarations = (List<TDeclaration>) indexScope.getConstants(getDeclaration().getName());
                    break;
                case FUNCTION:
                    allDeclarations = (List<TDeclaration>) indexScope.getFunctions(getDeclaration().getName());
                    break;
                case CLASS:
                    allDeclarations = (List<TDeclaration>) indexScope.getClasses(getDeclaration().getName());
                    break;
                case IFACE:
                    allDeclarations = (List<TDeclaration>) indexScope.getInterfaces(getDeclaration().getName());
                    break;
                case METHOD:
                    allDeclarations = (List<TDeclaration>) indexScope.getMethods((ClassScopeImpl) getDeclaration().getInScope(),
                            getDeclaration().getName());
                    break;
                case FIELD:
                    allDeclarations = (List<TDeclaration>) indexScope.getFields((ClassScopeImpl) getDeclaration().getInScope(),
                            getDeclaration().getName());
                    break;
                case CLASS_CONSTANT:
                    //TODO: not implemented yet
                case VARIABLE:
                case INCLUDE:
                    allDeclarations = Collections.<TDeclaration>singletonList(declaration);
                    break;
                default:
                    throw new UnsupportedOperationException(getDeclaration().getPhpKind().toString());
            }
        }
        return this.allDeclarations;
    }

    public List<Occurence<? extends ModelElement>> getAllOccurences() {
        return ModelVisitor.getAllOccurences(fileScope,this);
    }
}
