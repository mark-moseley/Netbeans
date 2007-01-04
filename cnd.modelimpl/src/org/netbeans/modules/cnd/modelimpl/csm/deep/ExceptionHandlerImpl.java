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

package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Common ancestor for all ... statements
 * @author Vladimir Kvashin
 */
public class ExceptionHandlerImpl extends CompoundStatementImpl implements CsmExceptionHandler {
    
    private ParameterImpl parameter;
    
    public ExceptionHandlerImpl(AST ast,  CsmFile file) {
        super(ast, file);
    }
    
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.CATCH;
    }
    
    public boolean isCatchAll() {
        return parameter == null;
    }
    
    public CsmParameter getParameter() {
        if( parameter == null ) {
            AST ast = AstUtil.findChildOfType(getAst(), CPPTokenTypes.CSM_PARAMETER_DECLARATION);
            if( ast != null ) {
                parameter = AstRenderer.renderParameter(ast, getContainingFile());
            }
        }
        return parameter;
    }
    
    /** overrides parent method */
    protected boolean renderStatements() {
        AST ast = AstUtil.findChildOfType(getAst(), CPPTokenTypes.CSM_COMPOUND_STATEMENT);
        if( ast != null ) {
            super.renderStatements(ast);
        }
	return true;
    }
   
    public List getScopeElements() {
        return DeepUtil.merge(getParameter(), getStatements());
    }
    
}
