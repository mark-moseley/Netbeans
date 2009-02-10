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
package org.netbeans.modules.cnd.modelimpl.csm;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmKnRName;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

/**
 * parameter list of non K&R function
 * @author Vladimir Voskresensky
 */
public class FunctionParameterListImpl extends ParameterListImpl<CsmFunctionParameterList, CsmParameter> implements CsmFunctionParameterList {

    private FunctionParameterListImpl(CsmFile file, int start, int end, Collection/*<CsmParameter> or <CsmUID<CsmParameter>>*/ parameters) {
        super(file, start, end, parameters);
    }

    public CsmParameterList<CsmParameterList, CsmKnRName> getKernighanAndRitchieParameterList() {
        return null;
    }

    @Override
    public String toString() {
        return "Fun " + super.toString(); // NOI18N
    }

    /*package*/ static FunctionParameterListImpl create(CsmFile file, AST funAST, CsmScope scope, boolean isLocal) {
        AST lParen = null;
        AST rParen = null;
        AST paramList = null;
        AST krList = null;
        AST prev = null;
        AST token;
        Stack<AST> lParens = new Stack<AST>();
        for (token = funAST.getFirstChild(); token != null; token = token.getNextSibling()) {
            if (token.getType() == CPPTokenTypes.CSM_PARMLIST) {
                paramList = token;
                // previous is "("
                lParen = prev;
                // next is ")"
                rParen = token.getNextSibling();
                break;
            } else if (token.getType() == CPPTokenTypes.RPAREN) {
                // could be function without parameters
                if (!lParens.isEmpty()) {
                    lParen = lParens.pop();
                }
                rParen = token;
            } else if (token.getType() == CPPTokenTypes.LITERAL_throw) {
                if (rParen != null) {
                    // after empty fun params
                    assert lParen != null;
                    break;
                }
            } else if (token.getType() == CPPTokenTypes.LPAREN) {
                lParens.push(token);
            }
            prev = token;
        }
        if (rParen != null) {
            krList = AstUtil.findSiblingOfType(rParen, CPPTokenTypes.CSM_KR_PARMLIST);
        } else {
            return null;
        }
        return create(file, lParen, rParen, paramList, krList, scope, isLocal);
    }

    private static FunctionParameterListImpl create(CsmFile file, AST lParen, AST rParen, AST firstList, AST krList, CsmScope scope, boolean isLocal) {
        if (lParen == null || lParen.getType() != CPPTokenTypes.LPAREN || rParen == null || rParen.getType() != CPPTokenTypes.RPAREN) {
            return null;
        }
        List<CsmParameter> parameters = AstRenderer.renderParameters(krList == null ? firstList : krList, file, scope, isLocal);
        Collection<CsmUID<CsmParameter>> paramUIDs = UIDCsmConverter.objectsToUIDs(parameters);
        return new FunctionParameterListImpl(file, getStartOffset(lParen), getEndOffset(rParen), paramUIDs);
    }

    /*package*/ static FunctionParameterListImpl create(CsmFunctionParameterList originalParamList, Collection<CsmParameter> parameters) {
        return new FunctionParameterListImpl(originalParamList.getContainingFile(), originalParamList.getStartOffset(),
                originalParamList.getEndOffset(), parameters);
    }

    ////////////////////////////////////////////////////////////////////////////
    // persistent
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }

    @SuppressWarnings("unchecked")
    public FunctionParameterListImpl(DataInput input) throws IOException {
        super(input);
    }

    /**
     * parameter list of K&R function
     */
    public static final class FunctionKnRParameterListImpl extends FunctionParameterListImpl {
        private final ParameterListImpl<CsmParameterList, CsmKnRName> krList;

        private FunctionKnRParameterListImpl(CsmFile file, int start, int end,
                Collection<CsmUID<CsmParameter>> parameters, ParameterListImpl<CsmParameterList, CsmKnRName> krList) {
            super(file, start, end, parameters);
            this.krList = krList;
        }

        @Override
        public CsmParameterList<CsmParameterList, CsmKnRName> getKernighanAndRitchieParameterList() {
            return krList;
        }

        @Override
        public String toString() {
            return "K&R " + super.toString(); // NOI18N
        }

        ////////////////////////////////////////////////////////////////////////////
        // persistent
        @Override
        public void write(DataOutput output) throws IOException {
            super.write(output);
            PersistentUtils.writeParameterList(this.krList, output);
        }

        @SuppressWarnings("unchecked")
        public FunctionKnRParameterListImpl(DataInput input) throws IOException {
            super(input);
            this.krList = (ParameterListImpl<CsmParameterList, CsmKnRName>) PersistentUtils.readParameterList(input);
        }
    }
}
