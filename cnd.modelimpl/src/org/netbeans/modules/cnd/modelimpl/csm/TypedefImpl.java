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

import antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;


/**
 * Implements CsmTypedef
 * @author vk155633
 */
public class TypedefImpl extends OffsetableDeclarationBase  implements CsmTypedef, CsmScopeElement {
    
    private String name;
    private CsmType type;
    private CsmObject container;
            
    public TypedefImpl(AST ast, CsmFile file, CsmObject container) {
        super(ast, file);
	this.container = container;
    }

    public TypedefImpl(AST ast, CsmFile file, CsmObject container, CsmType type, String name) {
        this(ast, file, container);
        this.type = type;
        this.name = name;
    }
    
//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }
    
    public CsmScope getScope() {
        // TODO: ???
        //return getContainingFile();
        if( container instanceof CsmNamespace ) {
            return (CsmNamespace) container;
        }
        else if( container instanceof CsmClass ) {
            return (CsmClass) container;
        }
        else {
            return getContainingFile();
        }
    }

    public String getQualifiedName() {
        if( CsmKindUtilities.isClass(container) ) {
	    return ((CsmClass) container).getQualifiedName() + "::" + getName();
	}
	else if( CsmKindUtilities.isNamespace(container) ) {
	    String nsName = ((CsmNamespace) container).getQualifiedName();
	    if( nsName != null && nsName.length() > 0 ) {
		return nsName + "::" + getName();
	    }
	}
        return getName();
    }

    public String getName() {
        if( name == null ) {
            AST tokId = null;
            for( AST token = getAst().getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                    AST child = token.getFirstChild();
                    if( child != null && child.getType() == CPPTokenTypes.ID ) {
                        name = child.getText();
                    }
                }
            }
        }
        if( name == null ) {
            name = "";
        }
        return name;
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.TYPEDEF;
    }
    
    public CsmType getType() {
        //
        // TODO: replace this horrible code with correct one
        //
        if( type == null ) {
            AST ptrOperator = null;
            int arrayDepth = 0;
            AST classifier = null;
            for( AST token = getAst().getFirstChild(); token != null; token = token.getNextSibling() ) {
//                if( token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND || 
//                        token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
//                    classifier = token;
//                    break;
//                }
                switch( token.getType() ) {
                    case CPPTokenTypes.CSM_TYPE_COMPOUND:
                    case CPPTokenTypes.CSM_TYPE_BUILTIN:
                        classifier = token;
                        break;
                    case CPPTokenTypes.LITERAL_struct:
                        AST next = token.getNextSibling();
                        if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                            classifier = next;
                            break;
                        }
                        break;
                }
                if( classifier != null ) {
                    break;
                }
            }
            if( classifier != null ) {
                type = TypeImpl.createType(classifier, getContainingFile(), ptrOperator, arrayDepth);
            }
        }
        return type;
    }
    
}
