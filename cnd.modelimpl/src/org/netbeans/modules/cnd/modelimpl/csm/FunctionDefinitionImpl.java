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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * @author Vladimir Kvasihn
 */
public class FunctionDefinitionImpl<T> extends FunctionImplEx<T> implements CsmFunctionDefinition<T> {

    private CsmUID<CsmFunction> declarationUID;
    
    private final CsmCompoundStatement body;
    
    public FunctionDefinitionImpl(AST ast, CsmFile file, CsmScope scope) {
        this(ast, file, scope, true);
    }
    
    protected  FunctionDefinitionImpl(AST ast, CsmFile file, CsmScope scope, boolean register) {
        super(ast, file, scope, false);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile(), this);
        assert body != null : "null body in function definition, line " + getStartPosition().getLine() + ":" + file.getAbsolutePath();
        if (register) {
            registerInProject();
        }
    }
    
    @Override
    public CsmCompoundStatement getBody() {
        return body;
    }

    public CsmFunction getDeclaration() {
        return getDeclaration(null);
    }
    
    public CsmFunction getDeclaration(Resolver parent) {
        CsmFunction declaration = _getDeclaration();
	if( declaration == null ) {
            _setDeclaration(null);
	    declaration = findDeclaration(parent);
            _setDeclaration(declaration);
	}
	return declaration;
    }
    
    private CsmFunction _getDeclaration() {
        CsmFunction decl = UIDCsmConverter.UIDtoDeclaration(this.declarationUID);
        // null object is OK here, because of changed cached reference
        return decl;
    }
    
    private void _setDeclaration(CsmFunction decl) {
        this.declarationUID = UIDCsmConverter.declarationToUID(decl);
        assert this.declarationUID != null || decl == null;
    }
    
    private CsmFunction findDeclaration(Resolver parent) {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
	if( def == null ) {
	    CsmObject owner = findOwner(parent);
	    if( owner instanceof CsmClass ) {
		def = findByName(((CsmClass) owner).getMembers(), getName());
	    }
	    else if( owner instanceof CsmNamespace ) {
		def = findByName(((CsmNamespace) owner).getDeclarations(), getName());
	    }
	}
        return (CsmFunction) def;
    }
    
    private static CsmFunction findByName(Collection/*CsmDeclaration*/ declarations, String name) {
	for (Iterator it = declarations.iterator(); it.hasNext();) {
	    CsmDeclaration decl = (CsmDeclaration) it.next();
	    if( decl.getName().equals(name) ) {
		if( decl instanceof  CsmFunction ) { // paranoja
		    return (CsmFunction) decl;
		}
	    }	
	}
	return null;
    }
    
    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    @Override
    protected String findQualifiedName() {
        CsmFunction declaration= _getDeclaration();
	if( declaration != null ) {
	    return declaration.getQualifiedName();
	}
	else {
	    return super.findQualifiedName();
	}
    }
    
    @Override
    public CsmScope getScope() {
        return getContainingFile();
    }

    @Override
    public List<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }

    @Override
    public CsmFunctionDefinition getDefinition() {
        return this;
    }
  
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeCompoundStatement(this.body, output);
        
        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.declarationUID, output);
    }
    
    public FunctionDefinitionImpl(DataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
        
        // read cached declaration
        this.declarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
    }      
}
