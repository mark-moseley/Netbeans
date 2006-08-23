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
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;

/**
 * Implementation for built-in types
 * @author Vladimir Kvasihn
 */
public class BuiltinTypes {

    private static class BuiltinImpl implements CsmBuiltIn {

        private String name;
        
        private BuiltinImpl(String name) {
            this.name = name;
        }
        
        public String getQualifiedName() {
            return getName();
        }

        public String getUniqueName() {
            return getKind().toString() + OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR +  getQualifiedName();
        }
        
        public String getName() {
            return name;
        }

        public CsmDeclaration.Kind getKind() {
            return CsmDeclaration.Kind.BUILT_IN;
        }

        public CsmScope getScope() {
            // TODO: builtins shouldn't be declarations! snd thus shouldn't be ScopeElements!
            return null;
        }
        
        
    }
    
    private static Map types = new HashMap();
    
    public static CsmBuiltIn getBuiltIn(AST ast) {
        assert ast.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN;
        StringBuffer sb = new StringBuffer();
        // TODO: take synonims into account!!!
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( sb.length() > 0 ) {
                sb.append(' ');
            }
            sb.append(token.getText());
        }
        return getBuiltIn(sb.toString());
    }
    
    public static CsmBuiltIn getBuiltIn(String text) {
        CsmBuiltIn builtIn = (CsmBuiltIn) types.get(text);
        if( builtIn == null ) {
            builtIn = new BuiltinImpl(text);
            types.put(text, builtIn);
        }
        return builtIn;
    }
}
