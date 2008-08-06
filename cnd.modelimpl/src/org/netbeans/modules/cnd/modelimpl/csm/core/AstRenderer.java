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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;

import antlr.collections.AST;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.AstRendererException;
import org.netbeans.modules.cnd.modelimpl.csm.deep.*;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * @author Vladimir Kvasihn
 */
public class AstRenderer {

    private FileImpl file;
    
    public AstRenderer(FileImpl fileImpl) {
        this.file = fileImpl;
    }
    
    public void render(AST root) {
        render(root, (NamespaceImpl) file.getProject().getGlobalNamespace(), file);
    }

    public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if( tree  == null ) return; // paranoia
        for( AST token = tree.getFirstChild(); token != null; token = token.getNextSibling() ) {
            int type = token.getType();
            switch( type ) {
                case CPPTokenTypes.CSM_LINKAGE_SPECIFICATION:
                    render(token, currentNamespace, container);
                    break;
                case CPPTokenTypes.CSM_NAMESPACE_DECLARATION:
                    NamespaceDefinitionImpl ns = new NamespaceDefinitionImpl(token, file, currentNamespace);
                    container.addDeclaration(ns);
                    render(token, (NamespaceImpl) ns.getNamespace(), ns);
                    break;
                case CPPTokenTypes.CSM_CLASS_DECLARATION:
                case CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION: 
                {
                    ClassImpl cls = TemplateUtils.isPartialClassSpecialization(token) ? 
			ClassImplSpecialization.create(token, currentNamespace, file) :
			ClassImpl.create(token, currentNamespace, file);
                    container.addDeclaration(cls);
                    addTypedefs(renderTypedef(token, cls, currentNamespace), currentNamespace, container);
                    renderVariableInClassifier(token, cls, currentNamespace, container);
                    break;
                }
                case CPPTokenTypes.CSM_ENUM_DECLARATION:
                {
                    CsmEnum csmEnum = EnumImpl.create(token, currentNamespace, file);
                    container.addDeclaration(csmEnum);
                    renderVariableInClassifier(token, csmEnum, currentNamespace, container);
                    break;
                }
                case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
		    if (isFuncLikeVariable(token)) {
			if( renderFuncLikeVariable(token, currentNamespace, container)) {
                            break;
			}
		    }
		    //nobreak!
                case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DECLARATION:
                case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                case CPPTokenTypes.CSM_USER_TYPE_CAST:
                    try {
                        FunctionImpl fi = new FunctionImpl(token, file, currentNamespace);
                        container.addDeclaration(fi);
                        if (NamespaceImpl.isNamespaceScope(fi)) {
                            currentNamespace.addDeclaration(fi);
                        }
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_CTOR_DEFINITION:
                case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:
                    try {
                        container.addDeclaration(new ConstructorDefinitionImpl(token, file, null));
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_DTOR_DEFINITION:
                case CPPTokenTypes.CSM_DTOR_TEMPLATE_DEFINITION:
                    try {
                        container.addDeclaration(new DestructorDefinitionImpl(token, file));
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DEFINITION:
                case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION:
		case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
                    try {
                        if( isMemberDefinition(token) ) {
                            container.addDeclaration(new FunctionDefinitionImpl(token, file, null));
                        } else {
                            FunctionDDImpl fddi = new FunctionDDImpl(token, file, currentNamespace);
                            //fddi.setScope(currentNamespace);
                            container.addDeclaration(fddi);
                            if (NamespaceImpl.isNamespaceScope(fddi)) {
                                currentNamespace.addDeclaration(fddi);
                            }
                        }
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                    if( isClassSpecialization(token) ) {
                        ClassImpl spec = ClassImplSpecialization.create(token, currentNamespace, file);
                        container.addDeclaration(spec);
                        addTypedefs(renderTypedef(token, spec, currentNamespace), currentNamespace, container);
                    }
                    else {
                        try {
                            if( isMemberDefinition(token) ) {
                                // this is a template method specialization declaration (without a definition)
                                container.addDeclaration(new FunctionImplEx(token, file, null));
                            }
                            else {
                                FunctionImpl funct = new FunctionImpl(token, file, currentNamespace);
                                container.addDeclaration(funct);
                                if (NamespaceImpl.isNamespaceScope(funct)) {
                                    currentNamespace.addDeclaration(funct);
                                }
                            }
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                    }
                    break; 
                case CPPTokenTypes.CSM_TEMPLATE_FUNCTION_DEFINITION_EXPLICIT_SPECIALIZATION:
                    try {
                        if( isMemberDefinition(token) ) {
                            container.addDeclaration(new FunctionDefinitionImpl(token, file, null));
                        } else {
                            FunctionDDImpl fddit = new FunctionDDImpl(token, file, currentNamespace);
                            container.addDeclaration(fddit);
                            if (NamespaceImpl.isNamespaceScope(fddit)) {
                                currentNamespace.addDeclaration(fddit);
                            }
                        }
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                    container.addDeclaration(new NamespaceAliasImpl(token, file));
                    break;
                case CPPTokenTypes.CSM_USING_DIRECTIVE:
                {
                    UsingDirectiveImpl using = new UsingDirectiveImpl(token, file);
                    container.addDeclaration(using);
                    currentNamespace.addDeclaration(using);
                    break;
                }
                case CPPTokenTypes.CSM_USING_DECLARATION:
                {
                    UsingDeclarationImpl using = new UsingDeclarationImpl(token, file, currentNamespace);
                    container.addDeclaration(using);
                    currentNamespace.addDeclaration(using);
                    break;
                }
                case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
                    if (renderForwardClassDeclaration(token, currentNamespace, container, file)){
                        break;
                    } else {
                        renderForwardMemberDeclaration(token, currentNamespace, container, file);
                    }
                    break;
                case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                    if( renderNSP(token, currentNamespace, container, file) ) {
                        break;
                    }
                    if( renderVariable(token, currentNamespace, container) ) {
                        break;
                    }
                    if( renderForwardClassDeclaration(token, currentNamespace, container, file) ) {
                        break;
                    }
                    if( renderLinkageSpec(token, file, currentNamespace, container) ) {
                        break;
                    }
                    CsmTypedef[] typedefs = renderTypedef(token, file, currentNamespace);
                    if( typedefs != null && typedefs.length > 0 ) {
                        addTypedefs(typedefs, currentNamespace, container);
                        break;
                    }
                default:
                    renderNSP(token, currentNamespace, container, file);
            }
        }
    }
    
    protected void addTypedefs(CsmTypedef[] typedefs, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if( typedefs != null ) {
            for (int i = 0; i < typedefs.length; i++) {
                // It could be important to register in project before add as member...
                file.getProjectImpl().registerDeclaration(typedefs[i]);
                if (container != null) {
                    container.addDeclaration(typedefs[i]);
                }
                if (currentNamespace != null) {
                    // Note: DeclarationStatementImpl.DSRenderer can call with null namespace
                    currentNamespace.addDeclaration(typedefs[i]);
                }
            }
        }
    }

    /**
     * Parser don't use a symbol table, so constructs like
     * int a(b) 
     * are parsed as if they were functions.
     * At the moment of rendering, we check whether this is a variable of a function
     * @return true if it's a variable, otherwise false (it's a function)
     */
    private boolean isFuncLikeVariable(AST ast) {
	AST astParmList = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_PARMLIST);
	if( astParmList != null ) {
            for( AST node = astParmList.getFirstChild(); node != null; node = node.getNextSibling() ) {
                if( ! isRefToVariable(node) ) {
                    return false;
                }
            }
	    return true;
	}
	return false;
    }

    /**
     * Determines whether the given parameter can actually be a reference to a variable,
     * not a parameter
     * @param node an AST node that corresponds to parameter
     * @return true if might be just a reference to a variable, otherwise false
     */
    private boolean isRefToVariable(AST node) {

	if( node.getType() != CPPTokenTypes.CSM_PARAMETER_DECLARATION ) { // paranoja
	    return false;
	}
	
	AST child = node.getFirstChild(); 

	AST name = null;
	
	// AST structure is different for int f1(A) and int f2(*A)
	if( child != null && child.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
	    while( child != null && child.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
		child = child.getNextSibling();
	    }
	    // now it's CSM_VARIABLE_DECLARATION
	    if( child != null ) {
		name = child.getFirstChild();
	    }
	}
	else if( child.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ) {
	    if( child.getNextSibling() != null ) {
		return false;
	    }
	    name = child.getFirstChild();
	}

	if( name == null ) {
	    return false;
	}
	if( name.getType() != CPPTokenTypes.CSM_QUALIFIED_ID && 
	    name.getType() != CPPTokenTypes.ID ) {
	    return false;
	}
	
	CsmAST csmAST = AstUtil.getFirstCsmAST(name);
	if(name == null) {
	    return false;
	}

	return findVariable( name.getText(), csmAST.getOffset());
    }
    
    /**
     * Finds variable in globals and in the current file
     */
    private boolean findVariable(CharSequence name, int offset) {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.VARIABLE) + 
                OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + "::" + name; // NOI18N
        if( findGlobal(file.getProject(), uname, new ArrayList<CsmProject>()) ) {
            return true;
        }
        return findVariable(name, file.getDeclarations(), offset);
    }
    
    private boolean findGlobal(CsmProject project, String uname, Collection<CsmProject> processedProjects) {
        if( processedProjects.contains(project) ) {
            return false;
        }
        processedProjects.add(project);
        if( project.findDeclaration(uname) != null ) {
            return true;
        }
        for( CsmProject lib : project.getLibraries() ) {
            if( findGlobal(lib, uname, processedProjects) ) {
                return true;
            }
        }
        return false;
    }

    private boolean findVariable(CharSequence name, Collection<CsmOffsetableDeclaration> declarations, int offset) {
        for( CsmOffsetableDeclaration decl : declarations ) {
            if( decl.getStartOffset() >= offset ) {
                break;
            }
            switch( decl.getKind() ) {
                case VARIABLE:
                    if( CharSequenceKey.Comparator.compare(name, ((CsmVariable) decl).getName()) == 0 ) {
                        return true;
                    }
                    break;
                case NAMESPACE_DEFINITION:
                    CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                    if( nd.getStartOffset() <= offset && nd.getEndOffset() >= offset ) {
                        if( findVariable(name, nd.getDeclarations(), offset) ) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return false;
    }
    
    /**
     * In the case of the "function-like variable" - construct like
     * int a(b) 
     * renders the AST to create the variable
     */
    private boolean renderFuncLikeVariable(AST token, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
	if( token != null ) {
	    token = token.getFirstChild();
	    switch( token.getType() ) {
	    	case CPPTokenTypes.CSM_TYPE_BUILTIN:
	    	case CPPTokenTypes.CSM_TYPE_COMPOUND:
		    AST typeToken = token;
		    AST next = token.getNextSibling();
		    while( next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
			next = next.getNextSibling();
		    }
		    if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                        TypeImpl type;
                        if (typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                            type = TypeFactory.createBuiltinType(typeToken.getText(), null, 0, typeToken, file);
                        } else {
                            type = TypeFactory.createType(typeToken, file, null, 0);
                        }
                        String name = next.getText();
                        VariableImpl var = createVariable(next, file, type, name, false, currentNamespace, container, null);
			if( currentNamespace != null ) {
			    currentNamespace.addDeclaration(var);
			}
			if( container != null ) {
			    container.addDeclaration(var);
			}
			return true;
		    }
		    break;
		
	    }
	}
	return false;
    }
    
    
    private boolean renderLinkageSpec(AST ast, FileImpl file, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if( ast != null ) {
            AST token = ast.getFirstChild();
            if (token != null) {
                if (token.getType() == CPPTokenTypes.CSM_LINKAGE_SPECIFICATION) {
                    render(token, currentNamespace, container);
                    return true;
                }
            }
        }
        return false;
    }

    protected void renderVariableInClassifier(AST ast, CsmClassifier classifier,
            MutableDeclarationsContainer container1, MutableDeclarationsContainer container2){
        AST token = ast.getFirstChild();
        boolean unnamedStaticUnion = false;
        boolean _static = false;
        int typeStartOffset = 0;
        if (token != null) {
            typeStartOffset = AstUtil.getFirstCsmAST(token).getOffset();
            if (token.getType() == CPPTokenTypes.LITERAL_static) {
                _static = true;
                token = token.getNextSibling();
                if (token != null) {
                    if (token.getType() == CPPTokenTypes.LITERAL_union) {
                        token = token.getNextSibling();
                        if (token != null) {
                            if (token.getType() == CPPTokenTypes.LCURLY) {
                                unnamedStaticUnion = true;
                            }
                        }
                    }
                }
            }
        }
        for (; token != null; token = token.getNextSibling()){
            if (token.getType() == CPPTokenTypes.RCURLY){
                break;
            }
        }
        if (token != null){
            int rcurlyOffset = AstUtil.getFirstCsmAST(token).getEndOffset();
            CsmOffsetable typeOffset = new OffsetableBase(file, typeStartOffset, rcurlyOffset);
            token = token.getNextSibling();
            boolean nothingBeforSemicolon = true;
            AST ptrOperator = null;
            for (; token != null; token = token.getNextSibling()){
                switch( token.getType() ) {
                    case CPPTokenTypes.CSM_PTR_OPERATOR:
                        nothingBeforSemicolon = false;
                        if( ptrOperator == null ) {
                            ptrOperator = token;
                        }
                        break;
                    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                    case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                    {
                        nothingBeforSemicolon = false;
                        int arrayDepth = 0;
                        String name = null;
                        for( AST varNode = token.getFirstChild(); varNode != null; varNode = varNode.getNextSibling() ) {
                            switch( varNode.getType() ) {
                                case CPPTokenTypes.LSQUARE:
                                    arrayDepth++;
                                    break;
                                case CPPTokenTypes.CSM_QUALIFIED_ID:
                                case CPPTokenTypes.ID:
                                    name = varNode.getText();
                                    break;
                            }
                        }
                        if (name != null) {
                            CsmType type = TypeFactory.createType(classifier, ptrOperator, arrayDepth, token, file, typeOffset);
                            VariableImpl var = createVariable(token, file, type, name, _static, container1, container2, null);
                            if( container2 != null ) {
                                container2.addDeclaration(var);
                            }
                            // TODO! don't add to namespace if....
                            if( container1 != null ) {
                                container1.addDeclaration(var);
                            }
                            ptrOperator = null;
                        }
                    }
                    case CPPTokenTypes.SEMICOLON:
                    {
                        if (unnamedStaticUnion && nothingBeforSemicolon) {
                            nothingBeforSemicolon = false;
                            CsmType type = TypeFactory.createType(classifier, null, 0, null, file, typeOffset);
                            VariableImpl var = new VariableImpl(new OffsetableBase(file, rcurlyOffset, rcurlyOffset),
                                    file, type, "", null, true, false, (container1 != null) || (container2 != null)); // NOI18N
                            if (container2 != null) {
                                container2.addDeclaration(var);
                            }
                            // TODO! don't add to namespace if....
                            if (container1 != null) {
                                container1.addDeclaration(var);
                            }
                        }                        
                    }
                    default:
                        nothingBeforSemicolon = false;
                }
            }
        }
    }

    protected CsmTypedef[] renderTypedef(AST ast, CsmClass cls, CsmObject container) {
        
        List<CsmTypedef> results = new ArrayList<CsmTypedef>();
        
        AST typedefNode = ast.getFirstChild();
        
        if( typedefNode != null && typedefNode.getType() == CPPTokenTypes.LITERAL_typedef ) {
            
            AST classNode = typedefNode.getNextSibling();
            
            switch ( classNode.getType() ) {

                case CPPTokenTypes.LITERAL_class:
                case CPPTokenTypes.LITERAL_union:
                case CPPTokenTypes.LITERAL_struct:

                    AST curr = AstUtil.findSiblingOfType(classNode, CPPTokenTypes.RCURLY);
                    if( curr == null ) {
                        return new CsmTypedef[0];
                    }

                    int arrayDepth = 0;
                    AST nameToken = null;
                    AST ptrOperator = null;
                    String name = "";
                    for( curr = curr.getNextSibling(); curr != null; curr = curr.getNextSibling() ) {
                        switch( curr.getType() ) {
                            case CPPTokenTypes.CSM_PTR_OPERATOR:
                                // store only 1-st one - the others (if any) follows,
                                // so it's TypeImpl.createType() responsibility to process them all
                                if( ptrOperator == null ) {
                                    ptrOperator = ast;
                                }
                                break;
                            case CPPTokenTypes.CSM_QUALIFIED_ID:
                                nameToken = curr;
                                //token t = nameToken.
                                name = AstUtil.findId(nameToken);
                                //name = token.getText();
                                break;
                            case CPPTokenTypes.LSQUARE:
                                arrayDepth++;
                            case CPPTokenTypes.COMMA:
                            case CPPTokenTypes.SEMICOLON:
                                TypeImpl typeImpl = TypeFactory.createType(cls, ptrOperator, arrayDepth, ast, file);
                                CsmTypedef typedef = createTypedef((nameToken == null) ? ast : nameToken, file, container, typeImpl, name);
                                if (cls != null && cls.getName().length()==0){
                                    ((TypedefImpl)typedef).setTypeUnnamed();
                                }
                                if( typedef != null ) {
                                    if (cls instanceof ClassEnumBase) {
                                        ((ClassEnumBase)cls).addEnclosingTypedef(typedef);
                                    }
                                    results.add(typedef);
                                }
                                ptrOperator = null;
                                name = "";
                                nameToken = null;
                                arrayDepth = 0;
                                break;
                        }

                    }
                    break;
                default:
                    // error message??
            }
        }
        return results.toArray(new CsmTypedef[results.size()]);
    }
    
    protected CsmTypedef[] renderTypedef(AST ast, FileImpl file, CsmScope container) {
        List<CsmTypedef> results = new ArrayList<CsmTypedef>();
        if( ast != null ) {
            AST firstChild = ast.getFirstChild();
            if (firstChild != null) {
                if (firstChild.getType() == CPPTokenTypes.LITERAL_typedef) {
                    //return createTypedef(ast, file, container);

                    AST classifier = null;
                    int arrayDepth = 0;
                    AST nameToken = null;
                    AST ptrOperator = null;
                    String name = "";

                    EnumImpl ei = null;

                    for (AST curr = ast.getFirstChild(); curr != null; curr = curr.getNextSibling()) {
                        switch (curr.getType()) {
                            case CPPTokenTypes.CSM_TYPE_COMPOUND:
                            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                                classifier = curr;
                                break;
                            case CPPTokenTypes.LITERAL_enum:
                                if (AstUtil.findSiblingOfType(curr, CPPTokenTypes.RCURLY) != null) {
                                    if (container instanceof CsmScope) {
                                        ei = EnumImpl.create(curr, (CsmScope) container, file);
                                        if (container instanceof MutableDeclarationsContainer) {
                                            ((MutableDeclarationsContainer) container).addDeclaration(ei);
                                        }
                                    }
                                    break;
                                }
                            // else fall through!
                            case CPPTokenTypes.LITERAL_struct:
                            case CPPTokenTypes.LITERAL_union:
                            case CPPTokenTypes.LITERAL_class:
                                AST next = curr.getNextSibling();
                                if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                                    classifier = next;
                                }
                                break;
                            case CPPTokenTypes.CSM_PTR_OPERATOR:
                                // store only 1-st one - the others (if any) follows,
                                // so it's TypeImpl.createType() responsibility to process them all
                                if (ptrOperator == null) {
                                    ptrOperator = curr;
                                }
                                break;
                            case CPPTokenTypes.CSM_QUALIFIED_ID:
                                // now token corresponds the name, since the case "struct S" is processed before
                                nameToken = curr;
                                name = AstUtil.findId(nameToken);
                                break;
                            case CPPTokenTypes.LSQUARE:
                                arrayDepth++;
                                break;
                            case CPPTokenTypes.COMMA:
                            case CPPTokenTypes.SEMICOLON:
                                TypeImpl typeImpl = null;
                                if (classifier != null) {
                                    typeImpl = TypeFactory.createType(classifier, file, ptrOperator, arrayDepth, container);
                                } else if (ei != null) {
                                    typeImpl = TypeFactory.createType(ei, ptrOperator, arrayDepth, ast, file);
                                }
                                if (typeImpl != null) {
                                    CsmTypedef typedef = createTypedef(ast/*nameToken*/, file, container, typeImpl, name);
                                    if (typedef != null) {
                                        if (ei != null && ei.getName().length() == 0) {
                                            ((TypedefImpl) typedef).setTypeUnnamed();
                                        }
                                        results.add(typedef);
                                        if (classifier instanceof ClassEnumBase) {
                                            ((ClassEnumBase) classifier).addEnclosingTypedef(typedef);
                                        } else if (ei instanceof ClassEnumBase) {
                                            ((ClassEnumBase) ei).addEnclosingTypedef(typedef);
                                        }
                                    }
                                }
                                ptrOperator = null;
                                name = "";
                                nameToken = null;
                                arrayDepth = 0;
                                break;
                        }
                    }
                }
            }
        }
        return results.toArray(new CsmTypedef[results.size()]);
    }
    
    protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, String name) {
        return new TypedefImpl(ast, file, container, type, name);
    }
    
    
    public static boolean renderForwardClassDeclaration(
            AST ast, 
            NamespaceImpl currentNamespace, MutableDeclarationsContainer container, 
            FileImpl file) {
        
        AST child = ast.getFirstChild();
        if( child == null ) {
            return false;
        }
        if (child.getType() == CPPTokenTypes.LITERAL_template) {
            child = child.getNextSibling();
            if( child == null ) {
                return false;
            }
        }
        
        switch( child.getType() ) {
            case CPPTokenTypes.LITERAL_class:
            case CPPTokenTypes.LITERAL_struct:
            case CPPTokenTypes.LITERAL_union:
                ClassForwardDeclarationImpl cfdi = new ClassForwardDeclarationImpl(ast, file);
                if( container != null ) {
                    container.addDeclaration(cfdi);
                }
                return true;
        }
                
        return false;
    }

    public static boolean renderForwardMemberDeclaration(
            AST ast, 
            NamespaceImpl currentNamespace, MutableDeclarationsContainer container, 
            FileImpl file) {
        
        AST child = ast.getFirstChild();
        while(child != null){
            switch(child.getType()){
                case CPPTokenTypes.LITERAL_template:
                case CPPTokenTypes.LITERAL_inline:
                case CPPTokenTypes.LITERAL__inline:
                case CPPTokenTypes.LITERAL___inline:
                case CPPTokenTypes.LITERAL___inline__:
                    child = child.getNextSibling();
                    continue;
            }
            break;
        }
        if( child == null ) {
            return false;
        }
        if (child.getType() == CPPTokenTypes.LITERAL_template) {
            child = child.getNextSibling();
            if( child == null ) {
                return false;
            }
        }
        
        switch( child.getType() ) {
            case CPPTokenTypes.CSM_TYPE_COMPOUND:
            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                child = child.getNextSibling();
                if (child != null){
                    if (child.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION){
                        //static variable definition
                    } else {
                        //method forward declaratin
                        try {
                            FunctionImpl ftdecl = new FunctionImpl(ast, file, currentNamespace);
                            if( container != null ) {
                                container.addDeclaration(ftdecl);
                            }
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                        return true;
                    }
                }
                break;
        }
                
        return false;
    }
    
    
    public static CharSequence getQualifiedName(AST qid) {
        if( qid != null && qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            if ( qid.getFirstChild() != null ) {
                StringBuilder sb = new StringBuilder();
                for( AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
                    // TODO: update this assert it should accept names like: allocator<char, typename A>
//                    if( ! ( namePart.getType() == CPPTokenTypes.ID || namePart.getType() == CPPTokenTypes.SCOPE ||
//                            namePart.getType() == CPPTokenTypes.LESSTHAN || namePart.getType() == CPPTokenTypes.GREATERTHAN ||
//                            namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
//                            namePart.getType() == CPPTokenTypes.COMMA) ) {
//			new Exception("Unexpected token type " + namePart).printStackTrace(System.err);
//		    }
                    if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                        namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
                        AST builtInType = namePart.getFirstChild();
                        sb.append(builtInType != null ? builtInType.getText() : "");
                    } else {
                        sb.append(namePart.getText());
                    }
                }
                return TextCache.getString(sb.toString());
            }
        }
        return "";
    }

    public static String[] getNameTokens(AST qid) {
        if( qid != null && (qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID || qid.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
            int templateDepth = 0;
            if ( qid.getNextSibling() != null ) {
                List<String> l = new ArrayList<String>();
                for( AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
                    if( templateDepth == 0 && namePart.getType() == CPPTokenTypes.ID ) {
                        l.add(namePart.getText());
                    }
                    else if( namePart.getType() == CPPTokenTypes.LESSTHAN ) {
                        // the beginning of template parameters
                        templateDepth++;
                    }
                    else if( namePart.getType() == CPPTokenTypes.GREATERTHAN ) {
                        // the beginning of template parameters
                        templateDepth--;
                    }
                    else {
                        //assert namePart.getType() == CPPTokenTypes.SCOPE;
                        if( templateDepth == 0 && namePart.getType() != CPPTokenTypes.SCOPE ) {
                            StringBuilder tokenText = new StringBuilder();
                            tokenText.append('[').append(namePart.getText());
                            if (namePart.getNumberOfChildren() == 0) {
                                tokenText.append(", line=").append(namePart.getLine()); // NOI18N
                                tokenText.append(", column=").append(namePart.getColumn()); // NOI18N
                            }
                            tokenText.append(']');
                            System.err.println("Incorect token: expected '::', found " + tokenText.toString());
                        }
                    }
                }
                return l.toArray(new String[l.size()]);
            }
        }
        return new String[0];
    }
    
  
    public static TypeImpl renderType(AST tokType, CsmFile file) {
        
        AST typeAST = tokType;
        tokType = getFirstSiblingSkipQualifiers(tokType);
                
        if( tokType == null ||  
            (tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN && 
            tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) ) {
            return null;
        }
         
        /**
        CsmClassifier classifier = null;
        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
            classifier = BuiltinTypes.getBuiltIn(tokType);
        }
        else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
            try {
                Resolver resolver = new Resolver(file, ((CsmAST) tokType.getFirstChild()).getOffset());
                // gather name components into string array 
                // for example, for std::vector new String[] { "std", "vector" }
                List l = new ArrayList();
                for( AST namePart = tokType.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
                    if( namePart.getType() == CPPTokenTypes.ID ) {
                        l.add(namePart.getText());
                    }
                    else {
                        assert namePart.getType() == CPPTokenTypes.SCOPE;
                    }
                }
                CsmObject o = resolver.resolve((String[]) l.toArray(new String[l.size()]));
                if( o instanceof CsmClassifier ) {
                    classifier = (CsmClassifier) o;
                }
            }
            catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }

        if( classifier != null ) {
            AST next = tokType.getNextSibling();
            AST ptrOperator =  (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
            return TypeImpl.createType(classifier, ptrOperator, 0);
        }
        
        return null;
         */
        AST next = tokType.getNextSibling();
        AST ptrOperator =  (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
        return TypeFactory.createType(typeAST/*tokType*/, file, ptrOperator, 0); 
    }

    /**
     * Returns first sibling (or just passed ast), skipps cv-qualifiers and storage class specifiers
     */
    public static AST getFirstSiblingSkipQualifiers(AST ast) {
        while( ast != null && isQualifier(ast.getType()) ) {
            ast = ast.getNextSibling();
        }
        return ast;
    }
    
    /**
     * Returns first child, skipps cv-qualifiers and storage class specifiers
     */
    public static AST getFirstChildSkipQualifiers(AST ast) {
        return getFirstSiblingSkipQualifiers(ast.getFirstChild());
    }
    
    public static boolean isQualifier(int tokenType) {
        return isCVQualifier(tokenType) || isStorageClassSpecifier(tokenType) || (tokenType == CPPTokenTypes.LITERAL_typename);
    }
    
    public static boolean isCVQualifier(int tokenType) {
        return isConstQualifier(tokenType) || isVolatileQualifier(tokenType);
    }

    public static boolean isConstQualifier(int tokenType) {
        switch( tokenType ) {
            case CPPTokenTypes.LITERAL_const:       return true;
            case CPPTokenTypes.LITERAL___const:     return true;
            case CPPTokenTypes.LITERAL___const__:   return true;
            default:                                return false;
        }
    }   

    public static boolean isVolatileQualifier(int tokenType) {
        switch( tokenType ) {
            case CPPTokenTypes.LITERAL_volatile:    return true;
            case CPPTokenTypes.LITERAL___volatile__:return true;
            case CPPTokenTypes.LITERAL___volatile:  return true;
            default:                                return false;
        }
    }
    
    public static boolean isStorageClassSpecifier(int tokenType) {
        switch( tokenType ) {
            case CPPTokenTypes.LITERAL_auto:        return true;
            case CPPTokenTypes.LITERAL_register:    return true;
            case CPPTokenTypes.LITERAL_static:      return true;
            case CPPTokenTypes.LITERAL_extern:      return true;
            case CPPTokenTypes.LITERAL_mutable:     return true;
            default:                                return false;
        }
    }

    /**
     * Checks whether the given AST is a variable declaration(s), 
     * if yes, creates variable(s), adds to conteiner(s), returns true,
     * otherwise returns false;
     *
     * There might be two containers, in which the given variable should be added.
     * For example, global variables should beadded both to file and to global namespace;
     * variables, declared in some namespace definition, should be added to both this definition and correspondent namespace as well.
     *
     * On the other hand, local variables are added only to it's containing scope, so either container1 or container2 might be null.
     *
     * @param ast AST to process
     * @param container1 container to add created variable into (may be null)
     * @param container2 container to add created variable into (may be null)
     */
    public boolean renderVariable(AST ast, MutableDeclarationsContainer namespaceContainer, MutableDeclarationsContainer container2) {
        boolean _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        AST typeAST = ast.getFirstChild();
        AST tokType = getFirstChildSkipQualifiers(ast);
        if( tokType == null ) {
            return false;
        }
        boolean isThisReference = false;
        if (tokType != null &&
            tokType.getType() == CPPTokenTypes.LITERAL_struct ||
            tokType.getType() == CPPTokenTypes.LITERAL_union ||
            tokType.getType() == CPPTokenTypes.LITERAL_enum ||
            tokType.getType() == CPPTokenTypes.LITERAL_class){
            // This is struct/class word for reference on containing struct/class
            tokType = tokType.getNextSibling();
            typeAST = tokType;
            if( tokType == null ) {
                return false;
            }
            isThisReference = true;
        }
        if( tokType != null && isConstQualifier(tokType.getType())) {
            assert (false): "must be skipped above";
            tokType = tokType.getNextSibling();
        }
        
        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
            tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
            tokType.getType() == CPPTokenTypes.CSM_QUALIFIED_ID && isThisReference) {
            
            AST nextToken = tokType.getNextSibling();
            while( nextToken != null && 
                    (nextToken.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ||
                     isQualifier(nextToken.getType()) ||
                     nextToken.getType() == CPPTokenTypes.LPAREN)) {
                nextToken = nextToken.getNextSibling(); 
            }
            
            if( nextToken == null || 
                nextToken.getType() == CPPTokenTypes.LSQUARE ||
                nextToken.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION || 
                nextToken.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
		nextToken.getType() == CPPTokenTypes.ASSIGNEQUAL ) {
                
                AST ptrOperator = null;
                boolean theOnly = true;
                boolean hasVariables = false;
		int inParamsLevel = 0;

                for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                    switch( token.getType() ) {
			case CPPTokenTypes.LPAREN:
			    inParamsLevel++;
			    break;
			case CPPTokenTypes.RPAREN:
			    inParamsLevel--;
			    break;		    
                        case CPPTokenTypes.CSM_PTR_OPERATOR:
                            // store only 1-st one - the others (if any) follows,
                            // so it's TypeImpl.createType() responsibility to process them all
                            if( ptrOperator == null && inParamsLevel == 0) {
                                ptrOperator = token;
                            }
                            break;
                        case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                        case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                            hasVariables = true;
                            if( theOnly ) {
                                for( AST next = token.getNextSibling(); next != null; next = next.getNextSibling() ) {
                                    int type = next.getType();
                                    if( type == CPPTokenTypes.CSM_VARIABLE_DECLARATION || type == CPPTokenTypes.CSM_ARRAY_DECLARATION ) {
                                        theOnly = false;
                                    }
                                }
                            }
                            processVariable(token, ptrOperator, (theOnly ? ast : token), typeAST/*tokType*/, namespaceContainer, container2, file, _static);
                            ptrOperator = null;
                            break;
                    }
                }
                if( ! hasVariables ) {
                    // unnamed parameter
                    processVariable(ast, ptrOperator, ast, typeAST/*tokType*/, namespaceContainer, container2, file, _static);
                }
                return true;
            }
        }
        return false;
    }
    
    protected void processVariable(AST varAst, AST ptrOperator, AST offsetAst,  AST classifier, 
                                MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, 
                                FileImpl file, boolean _static) {
        int arrayDepth = 0;
        String name = "";
            AST qn = null;
	int inParamsLevel = 0;
        for( AST token = varAst.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
		case CPPTokenTypes.LPAREN:
		    inParamsLevel++;
		    break;
		case CPPTokenTypes.RPAREN:
		    inParamsLevel--;
		    break;
                case CPPTokenTypes.LSQUARE:
		    if( inParamsLevel == 0 ) {
			arrayDepth++;
		    }
                    break;
                case CPPTokenTypes.LITERAL_struct:
                case CPPTokenTypes.LITERAL_union:
                case CPPTokenTypes.LITERAL_enum:
                case CPPTokenTypes.LITERAL_class:
		    // skip both this and next
                    token = token.getNextSibling();
                    continue;
                case CPPTokenTypes.CSM_QUALIFIED_ID:
		    if( inParamsLevel == 0 ) {
			qn = token;
		    }
                    // no break;
                case CPPTokenTypes.ID:
		    if( inParamsLevel == 0 ) {
			name = token.getText();
		    }
                    break;
            }
        }
        CsmType type = TypeFactory.createType(classifier, file, ptrOperator, arrayDepth);
        if (isScopedId(qn)){
            // This is definition of global namespace variable or definition of static class variable
            // TODO What about global variable definitions:
            // extern int i; - declaration
            // int i; - definition
            VariableDefinitionImpl var = new VariableDefinitionImpl(offsetAst, file, type, name);
            var.setStatic(_static);
            if( container2 != null ) {
                container2.addDeclaration(var);
            }
        } else {
            VariableImpl var = createVariable(offsetAst, file, type, name, _static, container1, container2, null);
            if( container2 != null ) {
                container2.addDeclaration(var);
            }
            // TODO! don't add to namespace if....
            if( container1 != null ) {
                container1.addDeclaration(var);
            }
        }
    }
    
    protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, 
	    MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
	type = TemplateUtils.checkTemplateType(type, scope);
        VariableImpl var = new VariableImpl(offsetAst, file, type, name, scope, (container1 != null) || (container2 != null));
        var.setStatic(_static);
        return var;
    }
    
    public static List<CsmParameter>  renderParameters(AST ast, final CsmFile file, CsmScope scope) {
        List<CsmParameter> parameters = new ArrayList<CsmParameter>();
        if( ast != null && (ast.getType() ==  CPPTokenTypes.CSM_PARMLIST ||
                            ast.getType() == CPPTokenTypes.CSM_KR_PARMLIST)) {
            for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION ) {
                    List<ParameterImpl> params = AstRenderer.renderParameter(token, file, scope);
                    if( params != null ) {
                        parameters.addAll(params);
                    }
                }
            }
        }
        return parameters;
    }
    
    public static boolean isVoidParameter(AST ast) {
        if( ast != null && (ast.getType() ==  CPPTokenTypes.CSM_PARMLIST ||
                            ast.getType() == CPPTokenTypes.CSM_KR_PARMLIST)) {
            AST token = ast.getFirstChild();
            if( token != null && token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION ) {
                AST firstChild = token.getFirstChild();
                if( firstChild != null ) {
                    if( firstChild.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN && firstChild.getNextSibling() == null ) {
                        AST grandChild = firstChild.getFirstChild();
                        if( grandChild != null && grandChild.getType() == CPPTokenTypes.LITERAL_void ) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public static List<ParameterImpl> renderParameter(AST ast, final CsmFile file, final CsmScope scope1) {
	
	// The only reason there might be several declarations is the K&R C style
	// we can split this function into two (for K&R and "normal" parameters)
	// if we found this ineffective; but now I vote for more clear and readable - i.e. single for both cases - code
	
	final List<ParameterImpl> result = new ArrayList<ParameterImpl>();
        AST firstChild = ast.getFirstChild();
        if( firstChild != null ) {
	    if( firstChild.getType() == CPPTokenTypes.ELLIPSIS ) {
		ParameterImpl parameter = new ParameterImpl(ast.getFirstChild(), file, null, "...", scope1); // NOI18N
		result.add(parameter);
		return result;
	    }
	    if( firstChild.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN && firstChild.getNextSibling() == null ) {
		AST grandChild = firstChild.getFirstChild();
		if( grandChild != null && grandChild.getType() == CPPTokenTypes.LITERAL_void ) {
		    return Collections.emptyList();
		}
	    }
        }
	class AstRendererEx extends AstRenderer {
	    public AstRendererEx() {
		super((FileImpl) file);
	    }
	    @Override
	    protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope2) {
                type = TemplateUtils.checkTemplateType(type, scope1);
		ParameterImpl parameter = new ParameterImpl(offsetAst, file, type, name, scope1);
		result.add(parameter);
		return parameter;
	    }
	}
	AstRendererEx renderer = new AstRendererEx();
	renderer.renderVariable(ast, null, null);
	return result;
    }
    
    
    
//    public static boolean isCsmType(AST token) {
//        if( token != null ) {
//            int type = token.getType();
//            return type == CPPTokenTypes.CSM_TYPE_BUILTIN || type == CPPTokenTypes.CSM_TYPE_COMPOUND;
//        }
//        return false;
//    }
    
    public static int getType(AST token) {
        return (token == null) ? -1 : token.getType();
    }
    
    public static int getFirstChildType(AST token) {
        AST child = token.getFirstChild();
        return (child == null) ? -1 : child.getType();
    }

//    public static int getNextSiblingType(AST token) {
//        AST sibling = token.getNextSibling();
//        return (sibling == null) ? -1 : sibling.getType();
//    }
    
    public static boolean renderNSP(AST token, NamespaceImpl currentNamespace, MutableDeclarationsContainer container, FileImpl file) {
        token = token.getFirstChild();
        if( token == null ) return false;
        switch( token.getType() ) {
            case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                container.addDeclaration(new NamespaceAliasImpl(token, file));
                return true;
            case CPPTokenTypes.CSM_USING_DIRECTIVE:
            {
                UsingDirectiveImpl using = new UsingDirectiveImpl(token, file);
                container.addDeclaration(using);
                currentNamespace.addDeclaration(using);
                return true;
            }
            case CPPTokenTypes.CSM_USING_DECLARATION:
            {
                UsingDeclarationImpl using = new UsingDeclarationImpl(token, file, currentNamespace);
                container.addDeclaration(using);
                currentNamespace.addDeclaration(using);
                return true;
            }
        }
        return false;
    }

    private boolean isClassSpecialization(AST ast){
        AST type = ast.getFirstChild(); // type
        if (type != null){
            AST child = type;
            while((child=child.getNextSibling())!=null){
                if (child.getType() == CPPTokenTypes.GREATERTHAN){
                    child = child=child.getNextSibling();
                    if (child != null && (child.getType() == CPPTokenTypes.LITERAL_class ||
                            child.getType() == CPPTokenTypes.LITERAL_struct)){
                        return true;
                    }
                    return false;
                }
            }
        }
        return true;
    }
     
    protected boolean isMemberDefinition(AST ast) {
	if( CastUtils.isCast(ast) ) {
	    return CastUtils.isMemberDefinition(ast);
	}
        AST id = AstUtil.findMethodName(ast);
        return isScopedId(id);
    }

    private boolean isScopedId(AST id){
	if( id == null ) {
	    return false;
	}
        if( id.getType() == CPPTokenTypes.ID ) {
            AST scope = id.getNextSibling();
            if( scope != null && scope.getType() == CPPTokenTypes.SCOPE ) {
                return true;
            }
        } else if( id.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            int i = 0;
            AST q = id.getFirstChild();
            while(q!=null){
                if (q.getType() == CPPTokenTypes.SCOPE){
                    return true;
                }
                q = q.getNextSibling();
            }
        }
        return false;
    }
    
    public static CsmCompoundStatement findCompoundStatement(AST ast, CsmFile file, CsmFunction owner) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
	    switch( token.getType() ) {
		case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
		    return new CompoundStatementImpl(token, file, owner);
		case CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY:
		    return new LazyCompoundStatementImpl(token, file, owner);
	    }
        }
        // prevent null bodies
        return new EmptyCompoundStatementImpl(ast, file, owner);
    }
    
    public static StatementBase renderStatement(AST ast, CsmFile file, CsmScope scope) {
        switch( ast.getType() ) {
            case CPPTokenTypes.CSM_LABELED_STATEMENT:
                return new LabelImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CASE_STATEMENT:
                return new CaseStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_DEFAULT_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.DEFAULT, scope);
            case CPPTokenTypes.CSM_EXPRESSION_STATEMENT:
                return new ExpressionStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CLASS_DECLARATION:
            case CPPTokenTypes.CSM_ENUM_DECLARATION:
            case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
            case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                return new DeclarationStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                return new CompoundStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_IF_STATEMENT:
                return new IfStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_SWITCH_STATEMENT:
                return new SwitchStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_WHILE_STATEMENT:
                return new LoopStatementImpl(ast, file, false, scope);
            case CPPTokenTypes.CSM_DO_WHILE_STATEMENT:
                return new LoopStatementImpl(ast, file, true, scope);
            case CPPTokenTypes.CSM_FOR_STATEMENT:
                return new ForStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_GOTO_STATEMENT:
                return new GotoStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CONTINUE_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.CONTINUE, scope);
            case CPPTokenTypes.CSM_BREAK_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.BREAK, scope);
            case CPPTokenTypes.CSM_RETURN_STATEMENT:
                return new ReturnStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_TRY_STATEMENT:
                return new TryCatchStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CATCH_CLAUSE:
                // TODO: isn't it in TryCatch ??
                return new UniversalStatement(ast, file, CsmStatement.Kind.CATCH, scope);
            case CPPTokenTypes.CSM_THROW_STATEMENT:
                // TODO: throw
                return new UniversalStatement(ast, file, CsmStatement.Kind.THROW, scope);
            case CPPTokenTypes.CSM_ASM_BLOCK:
                // just ignore
                break;
//            case CPPTokenTypes.SEMICOLON:
//            case CPPTokenTypes.LCURLY:
//            case CPPTokenTypes.RCURLY:
//                break;
//            default:
//                System.out.println("unexpected statement kind="+ast.getType());
//                break;
        }
        return null;
    }
    
    public ExpressionBase renderExpression(AST ast, CsmScope scope) {
        return isExpression(ast) ? new ExpressionBase(ast, file, null, scope) : null;
    }
    
    public CsmCondition renderCondition(AST ast, CsmScope scope) {
        if( ast != null && ast.getType() == CPPTokenTypes.CSM_CONDITION ) {
            AST first = ast.getFirstChild();
            if( first != null ) {
                int type = first.getType();
                if( isExpression(type) ) {
                    return new ConditionExpressionImpl(first, file, scope);
                }
                else if( type == CPPTokenTypes.CSM_TYPE_BUILTIN || type == CPPTokenTypes.CSM_TYPE_COMPOUND ) {
                    return new ConditionDeclarationImpl(ast, file, scope);
                }
            }
        }
        return null;
    }
    
    public static List<CsmExpression> renderConstructorInitializersList(AST ast, CsmScope scope, CsmFile file) {
        List<CsmExpression> initializers = null;
        for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
            if (token.getType() == CPPTokenTypes.CSM_CTOR_INITIALIZER_LIST) {
                for (AST initializerToken = token.getFirstChild(); initializerToken != null; initializerToken = initializerToken.getNextSibling()) {
                    if (initializerToken.getType() == CPPTokenTypes.CSM_CTOR_INITIALIZER) {
                        ExpressionBase initializer = new ExpressionBase(initializerToken, file, null, scope);
                        if (initializers == null) {
                            initializers = new ArrayList<CsmExpression>();
                        }
                        initializers.add(initializer);
                    }
                }
            }
        }
        return initializers;
    }
    
    public static boolean isExpression(AST ast) {
        return ast != null && isExpression(ast.getType());
    }
    
    public static boolean isExpression(int tokenType) {
        return 
            CPPTokenTypes.CSM_EXPRESSIONS_START < tokenType &&
            tokenType < CPPTokenTypes.CSM_EXPRESSIONS_END;
    }
    
    public static boolean isStatement(AST ast) {
        return ast != null && isStatement(ast.getType());
    }
    
    public static boolean isStatement(int tokenType) {
        return 
            CPPTokenTypes.CSM_STATEMENTS_START < tokenType && 
            tokenType < CPPTokenTypes.CSM_STATEMENTS_END;
    }
    
//    public ExpressionBase renderExpression(ExpressionBase parent) {
//        
//    }
    
}
    
