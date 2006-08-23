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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

/**
 * Method, which contains it's body right at throws POD (point of declaration)
 * @author Vladimir Kvasihn
 */
public class MethodDDImpl extends MethodImpl implements CsmFunctionDefinition {

    private CsmCompoundStatement body;
    
    public MethodDDImpl(AST ast, ClassImpl cls, CsmVisibility visibility) {
        super(ast, cls, visibility);
    }

    public CsmFunction getDeclaration() {
        return this;
    }

    public CsmCompoundStatement getBody() {
        if( body == null ) {
            body = AstRenderer.findCompoundStatement(getAst(), getContainingFile());
        }
        return body;
    }
    
    public List getScopeElements() {
        java.util.List l = new ArrayList();
        l.addAll(getParameters());
        l.add(getBody());
        return l;
    }
}
