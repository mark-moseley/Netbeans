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

package org.netbeans.modules.cnd.modelimpl.csm.deep;


import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;

/**
 * Implements condition of kind CsmCondition.Kind.DECLARATION
 * @author Vladimir Kvashin
 */
public class ConditionDeclarationImpl extends OffsetableBase implements CsmCondition {
    
    private VariableImpl declaration;
    
    public ConditionDeclarationImpl(AST ast, CsmFile file) {
        super(ast, file);
        initDeclaration(ast);
    }

    public CsmCondition.Kind getKind() {
        return CsmCondition.Kind.DECLARATION;
    }
    
    private void initDeclaration(AST node) {
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile()) {
            protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2) {
                ConditionDeclarationImpl.this.declaration = super.createVariable(offsetAst, file, type, name, _static, container1, container2);
                return declaration;
            }
        };
        renderer.renderVariable(node, null, null);
    }

    public CsmVariable getDeclaration() {
        return declaration;
    }

    public CsmExpression getExpression() {
        return null;
    }
    
}
