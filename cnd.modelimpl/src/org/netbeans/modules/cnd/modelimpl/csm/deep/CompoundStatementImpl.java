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
package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Common ancestor for all ... statements
 * @author Vladimir Kvashin
 */
public class CompoundStatementImpl extends StatementBase implements CsmCompoundStatement {

    private List<CsmStatement> statements;

    public CompoundStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.COMPOUND;
    }

    public final List<CsmStatement> getStatements() {
        if (statements == null) {
            renderStatements(getStartRenderingAst());
        }
        return statements;
    }

    protected AST getStartRenderingAst() {
        return getAst();
    }

    private void renderStatements(AST ast) {
        List<CsmStatement> out = new ArrayList<CsmStatement>();
        if (ast != null) {
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                CsmStatement stmt = AstRenderer.renderStatement(token, getContainingFile(), this);
                if (stmt != null) {
                    out.add(stmt);
                }
            }
        }
        statements = out;
    }

    public Collection<CsmScopeElement> getScopeElements() {
        @SuppressWarnings("unchecked")
        Collection<CsmScopeElement> out = (List) getStatements();
        return out;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }

    public CompoundStatementImpl(DataInput input) throws IOException {
        super(input);
        this.statements = null;
    }
}
