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

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 *
 * @author Alexander Simon
 */
public final class VariableDefinitionImpl extends VariableImpl<CsmVariableDefinition> implements CsmVariableDefinition {
    
    private CsmUID<CsmVariable> declarationUID;
    private CharSequence qualifiedName;
    private final CharSequence[] classOrNspNames;

    /** Creates a new instance of VariableDefinitionImpl */
    public VariableDefinitionImpl(AST ast, CsmFile file, CsmType type, String name) {
        super(ast, file, type, getLastname(name), false);
        classOrNspNames = getClassOrNspNames(ast);
        registerInProject();
    }
    
    private static String getLastname(String name){
        int i = name.lastIndexOf("::"); // NOI18N
        if (i >=0){
            name = name.substring(i+2);
        }
        return name;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.VARIABLE_DEFINITION;
    }
    
    public CsmVariable getDeclaration() {
        CsmVariable declaration = _getDeclaration(); 
	if( declaration == null ) {
            _setDeclaration(null);
	    declaration = findDeclaration();
            _setDeclaration(declaration);
	}
	return declaration;
    }

    private CsmVariable _getDeclaration() {
        // null object is OK here, because of changed cached reference
        return UIDCsmConverter.UIDtoDeclaration(this.declarationUID);
    }
    
    private void _setDeclaration(CsmVariable decl) {
        this.declarationUID = UIDCsmConverter.declarationToUID(decl);
        assert declarationUID != null || decl == null;
    }
    
    @Override
    public CharSequence getQualifiedName() {
	if( qualifiedName == null ) {
	    qualifiedName = QualifiedNameCache.getManager().getString(findQualifiedName());
	}
	return qualifiedName;
    }
    
    private String findQualifiedName() {
        CsmVariable declaration = _getDeclaration();
	if( declaration != null ) {
	    return declaration.getQualifiedName().toString();
	}
	CsmObject owner = findOwner();
	if( owner instanceof CsmQualifiedNamedElement  ) {
	    return ((CsmQualifiedNamedElement) owner).getQualifiedName() + "::" + getQualifiedNamePostfix(); // NOI18N
	}
	else {
	    CharSequence[] cnn = classOrNspNames;
	    CsmNamespaceDefinition nsd = findNamespaceDefinition();
	    StringBuilder sb = new StringBuilder();
	    if( nsd != null ) {
		sb.append(nsd.getQualifiedName());
	    }
	    if( cnn != null ) {
		for (int i = 0; i < cnn.length; i++) {
		    if( sb.length() > 0 ) {
			sb.append("::"); // NOI18N
		    }
		    sb.append(cnn[i]);
		}
	    }
	    if( sb.length() == 0 ) {
		sb.append("unknown>"); // NOI18N
	    }
	    sb.append("::"); // NOI18N
	    sb.append(getQualifiedNamePostfix());
	    return sb.toString();
	}
    }

    private CsmNamespaceDefinition findNamespaceDefinition() {
	return findNamespaceDefinition(getContainingFile().getDeclarations());
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition(Collection/*<CsmOffsetableDeclaration>*/ declarations) {
	for (Iterator it = declarations.iterator(); it.hasNext();) {
	    CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) it.next();
	    if( decl.getStartOffset() > this.getStartOffset() ) {
		break;
	    }
	    if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
		if( this.getEndOffset() < decl.getEndOffset() ) {
		    CsmNamespaceDefinition nsdef = (CsmNamespaceDefinition) decl;
		    CsmNamespaceDefinition inner = findNamespaceDefinition(nsdef.getDeclarations());
		    return (inner == null) ? nsdef : inner;
		}
	    }
	}
	return null;
    }

    private CsmVariable findDeclaration() {
        String uname = CsmDeclaration.Kind.VARIABLE.toString() + UNIQUE_NAME_SEPARATOR + getQualifiedName();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
	if( def == null ) {
	    CsmObject owner = findOwner();
	    if( owner instanceof CsmClass ) {
		def = findByName(((CsmClass) owner).getMembers(), getName());
	    }
	    else if( owner instanceof CsmNamespace ) {
		def = findByName(((CsmNamespace) owner).getDeclarations(), getName());
	    }
	}
        return (CsmVariable) def;
    }

    private CsmVariable findByName(Collection/*CsmDeclaration*/ declarations, CharSequence name) {
	for (Iterator it = declarations.iterator(); it.hasNext();) {
	    CsmDeclaration decl = (CsmDeclaration) it.next();
	    if( decl.getName().equals(name) ) {
		if( decl instanceof  CsmVariable ) { // paranoja
		    return (CsmVariable) decl;
		}
	    }	
	}
	return null;
    }

    /** @return either class or namespace */
    private CsmObject findOwner() {
	CharSequence[] cnn = classOrNspNames;
	if( cnn != null ) {
	    CsmObject obj = ResolverFactory.createResolver(this).resolve(cnn, Resolver.CLASSIFIER | Resolver.NAMESPACE);
	    if( obj instanceof CsmClass ) {
		if( !( obj instanceof Unresolved.UnresolvedClass) ) {
		    return (CsmClass) obj;
		}
	    }
	    else if( obj instanceof CsmNamespace ) {
		return (CsmNamespace) obj;
	    }
	}
	return null;
    }    
    
    private static String[] getClassOrNspNames(AST ast) {
        AST qid = getQialifiedId(ast);
        if( qid == null ) {
            return null;
        }
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List/*<String>*/ l = new ArrayList/*<String>*/();
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.ID ) {
                    if( token.getNextSibling() != null ) {
                        l.add(token.getText());
                    }
                }
            }
            return (String[]) l.toArray(new String[l.size()]);
        }
        return null;
    }
    
    private static AST getQialifiedId(AST ast){
        AST varAst = ast;
        for( AST token = varAst.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                    return token = token.getFirstChild();
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                case CPPTokenTypes.ID:
                    return token;
            }
        }
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);    
        assert this.qualifiedName != null;
        output.writeUTF(this.qualifiedName.toString());
        PersistentUtils.writeStrings(this.classOrNspNames, output);
        
        UIDObjectFactory.getDefaultFactory().writeUID(this.declarationUID, output);
    }  
    
    public VariableDefinitionImpl(DataInput input) throws IOException {
        super(input);
        this.qualifiedName = QualifiedNameCache.getManager().getString(input.readUTF());
        assert this.qualifiedName != null;
        this.classOrNspNames = PersistentUtils.readStrings(input, NameCache.getManager());
        
        this.declarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }     
}
