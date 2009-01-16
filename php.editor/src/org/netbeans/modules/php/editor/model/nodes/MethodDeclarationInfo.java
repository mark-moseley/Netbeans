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

package org.netbeans.modules.php.editor.model.nodes;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.Parameter;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;

/**
 * @author Radek Matous
 */
public class MethodDeclarationInfo extends ASTNodeInfo<MethodDeclaration> {
    MethodDeclarationInfo(MethodDeclaration methodDeclaration) {
        super(methodDeclaration);
    }

    public static MethodDeclarationInfo create(MethodDeclaration classDeclaration) {
        return new MethodDeclarationInfo(classDeclaration);
    }

    @Override
    public Kind getKind() {
        PhpModifiers modifiers = new PhpModifiers(getOriginalNode().getModifier());
        return modifiers.isStatic() ? Kind.STATIC_METHOD: Kind.METHOD;
    }

    @Override
    public String getName() {
        MethodDeclaration methodDeclaration = getOriginalNode();
        return methodDeclaration.getFunction().getFunctionName().getName();
    }

    @Override
    public OffsetRange getRange() {
        MethodDeclaration methodDeclaration = getOriginalNode();
        Identifier name = methodDeclaration.getFunction().getFunctionName();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public List<? extends Parameter> getParameters() {
        List<FormalParameter> formalParameters = getOriginalNode().getFunction().getFormalParameters();
        List<Parameter> retval = new ArrayList<Parameter>();
        for (FormalParameter formalParameter : formalParameters) {
            String name = CodeUtils.getParamDisplayName(formalParameter);
            String defVal = CodeUtils.getParamDefaultValue(formalParameter);
            if (name != null) {
                retval.add(new ParameterImpl(name, defVal));
            }
        }
        return retval;
    }
    
    public PhpModifiers getAccessModifiers() {
        return new PhpModifiers(getOriginalNode().getModifier());
    }


    public FunctionDeclarationInfo getFunctionDeclarationInfo() {
        return FunctionDeclarationInfo.create(getOriginalNode().getFunction());
    }
}
