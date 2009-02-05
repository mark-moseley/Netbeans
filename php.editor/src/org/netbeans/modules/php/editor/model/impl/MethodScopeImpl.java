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

import java.util.List;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 * @author Radek Matous
 */
final class MethodScopeImpl extends FunctionScopeImpl implements MethodScope, VariableContainerImpl {
    private String classNormName;
    //new contructors
    MethodScopeImpl(Scope inScope, String returnType, MethodDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, returnType);
        assert inScope instanceof TypeScope;
        classNormName = inScope.getNormalizedName();
    }

    MethodScopeImpl(Scope inScope, IndexedFunction element, PhpKind kind) {
        super(inScope, element, kind);
        assert inScope instanceof TypeScope;
        classNormName = inScope.getNormalizedName();
    }

    @Override
    public VariableNameImpl createElement(Program program, Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, program, node, false);
        addElement(retval);
        return retval;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" ");//NOI18N
        sb.append(super.toString());
        return sb.toString();
    }

    public boolean isMagic() {
        return PredefinedSymbols.MAGIC_METHODS.containsKey(getName().toLowerCase());
    }

    public boolean isConstructor() {
        return isMagic() ? getName().contains("__construct") : false;
    }

    public ClassScope getClassScope() {
        return (ClassScope) getInScope();
    }

    @Override
    public String getNormalizedName() {
        return classNormName+super.getNormalizedName();
    }

    public String getClassSkeleton() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" ");//NOI18N
        sb.append("function").append(" ").append(getName());//NOI18N
        sb.append("(");//NOI18N
        List<? extends String> parameterNames = getParameterNames();
        for (int i = 0; i < parameterNames.size(); i++) {
            String param = parameterNames.get(i);
            if (i > 0) {
                sb.append(",");//NOI18N
            }
            sb.append(param);
        }
        sb.append(")");
        sb.append("{\n}\n");//NOI18N
        return sb.toString();
    }

    public String getInterfaceSkeleton() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" ");//NOI18N
        sb.append("function").append(" ").append(getName());//NOI18N
        sb.append("(");//NOI18N
        List<? extends String> parameterNames = getParameterNames();
        for (int i = 0; i < parameterNames.size(); i++) {
            String param = parameterNames.get(i);
            if (i > 0) {
                sb.append(",");//NOI18N
            }
            sb.append(param);
        }
        sb.append(")");
        sb.append(";\n");//NOI18N
        return sb.toString();
    }

}
