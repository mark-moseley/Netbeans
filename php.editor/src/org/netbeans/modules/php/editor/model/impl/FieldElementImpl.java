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
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class FieldElementImpl extends ScopeImpl implements FieldElement {
    private String returnType;
    private String className;

    FieldElementImpl(Scope inScope, String returnType, SingleFieldDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), null);
        this.returnType = returnType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
    }

    FieldElementImpl(Scope inScope, String returnType, PhpDocTypeTagInfo nodeInfo) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), null);
        this.returnType = returnType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
    }

    FieldElementImpl(Scope inScope, IndexedConstant indexedConstant) {
        super(inScope, indexedConstant, PhpKind.FIELD);
        String in = indexedConstant.getIn();
        if (in != null) {
            className = in;
        } else {
            className = inScope.getName();
        }
    }

    private FieldElementImpl(Scope inScope, String name,
            Union2<String/*url*/, FileObject> file, OffsetRange offsetRange,
            PhpModifiers modifiers, String returnType) {
        super(inScope, name, file, offsetRange, PhpKind.FIELD, modifiers);
        this.returnType = returnType;
    }

    static String toName(SingleFieldDeclaration node) {
        return VariableNameImpl.toName(node.getName());
    }

    static OffsetRange toOffsetRange(SingleFieldDeclaration node) {
        return VariableNameImpl.toOffsetRange(node.getName());
    }

    static PhpModifiers toAccessModifiers(FieldsDeclaration node) {
        return new PhpModifiers(node.getModifier());
    }

    public List<? extends TypeScope> getReturnTypes() {
        return (returnType != null && returnType.length() > 0) ?
            CachingSupport.getTypes(returnType.split("\\|")[0], this) :
            Collections.<TypeScopeImpl>emptyList();

    }
    @Override
    public String getNormalizedName() {
        return className+super.getNormalizedName();
    }

    public List<? extends TypeScope> getTypes(int offset) {
        AssignmentImpl assignment = findAssignment(offset);
        return (assignment != null) ? assignment.getTypes() : getReturnTypes();
    }

    public List<? extends FieldAssignmentImpl> getAssignments() {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElement element) {
                return true;
            }
        });
    }

    public AssignmentImpl findAssignment(int offset) {
        FieldAssignmentImpl retval = null;
        List<? extends FieldAssignmentImpl> assignments = getAssignments();
        if (assignments.size() == 1) {
            retval = assignments.get(0);
        } else {
            for (FieldAssignmentImpl assignmentImpl : assignments) {
                if (assignmentImpl.getBlockRange().containsInclusive(offset)) {
                    if (retval == null || retval.getOffset() <= assignmentImpl.getOffset()) {
                         if (assignmentImpl.getOffset() < offset) {
                            retval = assignmentImpl;
                         }
                    }
                }
            }
        }
        return retval;
    }

}
