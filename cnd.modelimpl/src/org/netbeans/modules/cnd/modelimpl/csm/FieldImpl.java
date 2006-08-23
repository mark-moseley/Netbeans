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

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * CsmVariable + CsmMember implementation
 * @author Vladimir Kvashin
 */
public class FieldImpl extends VariableImpl implements CsmField {

    private ClassImpl containingClass;
    private CsmVisibility visibility;

    public FieldImpl(ClassImpl cls, CsmVisibility visibility, String name, int start, int end) {
        super(name, cls.getContainingFile(), start, end);
        setScope(cls);
        this.visibility = visibility;
        this.containingClass = cls;
    }
    
//    public FieldImpl(AST ast, ClassImpl cls, CsmVisibility visibility) {
//        super(""/*AstUtil.findId(ast)*/, cls.getContainingFile(), 0, 0);
//        AST var = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_VARIABLE_DECLARATION);
//        setName(AstUtil.findId(var == null ? ast : var));
//        init(cls, visibility);
//        setAst(ast);
//        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
//            switch( token.getType() ) {
//                case CPPTokenTypes.LITERAL_static:
//                    setStatic(true);
//                    break;
//            }
//        }
//    }
    
    public FieldImpl(AST ast, CsmFile file, CsmType type, String name, ClassImpl cls, CsmVisibility visibility) {
        super(ast, file, type, name);
        setScope(cls);
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                    setStatic(true);
                    break;
            }
        }
        this.visibility = visibility;
        this.containingClass = cls;
    }
    
//    private void init(ClassImpl cls, CsmVisibility visibility) {
//        this.visibility = visibility;
//        this.containingClass = cls;
//    }
    
    public CsmClass getContainingClass() {
        return containingClass;
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }
     
//    public void setVisibility(CsmVisibility visibility) {
//        this.visibility = visibility;
//    }
    
    
}
