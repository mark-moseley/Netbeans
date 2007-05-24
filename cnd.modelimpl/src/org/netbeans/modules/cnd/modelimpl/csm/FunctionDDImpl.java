/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.ArrayList;
import java.util.List;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * Implements both CsmFunction and CsmFunctionDefinition -
 * for those cases, when they coinside (i.e. implivit inlines)
 * @author Vladimir Kvasihn
 */
public final class FunctionDDImpl extends FunctionImpl<CsmFunctionDefinition> implements CsmFunctionDefinition {
    
    private final CsmCompoundStatement body;

    public FunctionDDImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope, false);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile());
        assert body != null : "null body in function definition, line " + getStartPosition().getLine() + ":" + file.getAbsolutePath();
        registerInProject();
    }

    public CsmCompoundStatement getBody() {
        return body;
    }

    public CsmFunction getDeclaration() {
        String uname = CsmDeclaration.Kind.FUNCTION.toString() + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmDeclaration decl = getContainingFile().getProject().findDeclaration(uname);
        if( decl != null && decl.getKind() == CsmDeclaration.Kind.FUNCTION ) {
            return (CsmFunction) decl;
        }
        else {
            return this;
        }
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }
    
    public List<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        l.addAll(getParameters());
        l.add(getBody());
        return l;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.body != null: "null body in " + this.getQualifiedName();
        PersistentUtils.writeCompoundStatement(body, output);
    }
    
    public FunctionDDImpl(DataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
        assert this.body != null: "read null body for " + this.getName();
    }       
}

