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

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Implementation of CsmDeclarationStatement
 * @author Vladimir Kvashin
 */
public class DeclarationStatementImpl extends StatementBase implements CsmDeclarationStatement {

    private List<CsmDeclaration> declarators;
    
    public DeclarationStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.DECLARATION;
    }

    public List<CsmDeclaration> getDeclarators() {
        if( declarators == null ) {
            declarators = new ArrayList<CsmDeclaration>();
            render();
        }
        return declarators;
    }
    
    public String toString() {
        return "" + getKind() + ' ' + getOffsetString() + '[' + declarators + ']'; // NOI18N
    }
    
    private void render() {
        AstRenderer renderer = new DSRenderer();
        renderer.render(getAst(), null, null);
    }
    
       
    private class DSRenderer extends AstRenderer {
	
	public DSRenderer() {
	    super((FileImpl) getContainingFile());
	}
	
	protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2,CsmScope scope) {
	    VariableImpl var = super.createVariable(offsetAst, file, type, name, _static, container1, container2, getScope());
	    declarators.add(var);
	    return var;
	}

	public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
	    if( tree != null ) {
		AST token = tree;
		switch( token.getType() ) {
		    case CPPTokenTypes.CSM_FOR_INIT_STATEMENT:
		    case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
			if (!renderVariable(token, currentNamespace, container)){
			    render(token.getFirstChild(), currentNamespace, container);
			}
			break;
		    case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
			declarators.add(new NamespaceAliasImpl(token, getContainingFile()));
			break;
		    case CPPTokenTypes.CSM_USING_DIRECTIVE:
			declarators.add(new UsingDirectiveImpl(token, getContainingFile()));
			break;
		    case CPPTokenTypes.CSM_USING_DECLARATION:
			declarators.add(new UsingDeclarationImpl(token, getContainingFile()));
			break;

		    case CPPTokenTypes.CSM_CLASS_DECLARATION:
		    {
			ClassImpl cls = ClassImpl.create(token, null, getContainingFile());
			declarators.add(cls);
			addTypedefs(renderTypedef(token, cls, currentNamespace), currentNamespace, container);
			renderVariableInClassifier(token, cls, currentNamespace, container);
			break;
		    }
		    case CPPTokenTypes.CSM_ENUM_DECLARATION:
		    {
			CsmEnum csmEnum = EnumImpl.create(token, currentNamespace, getContainingFile());
			declarators.add(csmEnum);
			renderVariableInClassifier(token, csmEnum, currentNamespace, container);
			break;
		    }
		    case CPPTokenTypes.CSM_TYPE_BUILTIN:
		    case CPPTokenTypes.CSM_TYPE_COMPOUND:
			AST typeToken = token;
			AST next = token.getNextSibling();
			if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
			    do {
				TypeImpl type;
				if( typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
				    type = TypeFactory.createBuiltinType(typeToken.getText(), null, 0, typeToken, getContainingFile());
				}
				else {
				    type = TypeFactory.createType(typeToken, getContainingFile(), null, 0);
				}
				String name = next.getText();
				VariableImpl var = createVariable(next, getContainingFile(), type, name, false, currentNamespace, container, getScope());
				// we ignore both currentNamespace and container; <= WHY?
				// eat all tokens up to the comma that separates the next decl
				next = next.getNextSibling();
				if( next != null && next.getType() == CPPTokenTypes.CSM_PARMLIST ) {
				    next = next.getNextSibling();
				}
				if( next != null && next.getType() == CPPTokenTypes.COMMA ) {
				    next = next.getNextSibling();
				}
			    }
			    while( next != null && next.getType() ==  CPPTokenTypes.CSM_QUALIFIED_ID );
			}
			break;
                    case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                        CsmTypedef[] typedefs = renderTypedef(token, (FileImpl) getContainingFile(), getScope());
			if( typedefs != null && typedefs.length > 0 ) {
			    for (int i = 0; i < typedefs.length; i++) {
				declarators.add(typedefs[i]);
                            }
                        }
		}
	    }
	}

// Never used 
//	/**
//	 * Creates a variable for declaration like int x(y);
//	 * Returns a token that follows this declaration or null
//	 */
//	private AST createVarWithCtor(AST token) {
//	    assert(token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND);
//	    AST typeToken = token;
//	    AST next = token.getNextSibling();
//	    if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
//		TypeImpl type;
//		if( typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
//		    type = TypeFactory.createBuiltinType(typeToken.getText(), null, 0, typeToken, getContainingFile());
//		}
//		else {
//		    type = TypeFactory.createType(typeToken, getContainingFile(), null, 0);
//		}
//		String name = next.getText();
//		VariableImpl var = new VariableImpl(next, getContainingFile(), type, name, true);
//		// we ignore both currentNamespace and container
//		declarators.add(var);
//		// eat all tokens up to the comma that separates the next decl
//		next = next.getNextSibling();
//		if( next != null && next.getType() == CPPTokenTypes.CSM_PARMLIST ) {
//		    next = next.getNextSibling();
//		}
//		if( next != null && next.getType() == CPPTokenTypes.COMMA ) {
//		    next = next.getNextSibling();
//		}
//		return next;
//	    }
//	    return null;
//	}
    }
	    
}
