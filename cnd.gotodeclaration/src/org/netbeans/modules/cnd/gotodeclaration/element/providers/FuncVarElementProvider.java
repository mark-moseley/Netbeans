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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcher;
import org.openide.util.NbBundle;

/**
 * ElementProvider for functions and variables
 * @author Vladimir Kvashin
 */
public class FuncVarElementProvider extends BaseProvider implements ElementProvider {

    
    public String name() {
	return "C/C++ Functions and Variables"; // NOI18N
    }

    public String getDisplayName() {
	return NbBundle.getMessage(FuncVarElementProvider.class, "FUNCVAR_PROVIDER_DISPLAY_NAME"); // NOI18N
    }

    protected void processProject(CsmProject project, ResultSet result, NameMatcher comparator) {
	if( TRACE ) System.err.printf("FuncVarElementProvider.processProject %s\n", project.getName());
        processNamespace(project.getGlobalNamespace(), result, comparator);
    }
    
    private void processNamespace(CsmNamespace nsp, ResultSet result, NameMatcher comparator) {
        if( TRACE ) System.err.printf("processNamespace %s\n", nsp.getQualifiedName());
	for( CsmDeclaration declaration : nsp.getDeclarations() ) {
            if( isCancelled() ) {
		return;
	    }
	    processDeclaration(declaration, result, comparator);
	}
	for( CsmNamespace child : nsp.getNestedNamespaces() ) {
            if( isCancelled() ) {
		return;
	    }
	    processNamespace(child, result, comparator);
	}
    }

    private void processDeclaration(CsmDeclaration decl, ResultSet result, NameMatcher comparator) {
        switch (decl.getKind()) {
            case FUNCTION_DEFINITION:
		if( comparator.matches(decl.getName().toString()) ) {
		    CsmFunctionDefinition fdef = (CsmFunctionDefinition) decl;
//		    CsmFunction fdecl = fdef.getDeclaration();
//		    if( fdecl == null || fdecl == fdef) {
                        result.add(new FunctionElementDescriptor(fdef));
//		    }
		}
		break;
            case FUNCTION:
		if( comparator.matches(decl.getName().toString()) ) {
                    CsmFunction fdecl = (CsmFunction) decl;
                    CsmFunctionDefinition fdef = fdecl.getDefinition();
                    if( fdef == null || fdef.equals(fdecl) ) {
                        result.add(new FunctionElementDescriptor((CsmFunction) decl));
                    }
		}
		break;
            case VARIABLE:
		if( comparator.matches(decl.getName().toString()) ) {
                    result.add(new VariableElementDescriptor((CsmVariable) decl));
		}
		break;
            case CLASS:
            case UNION:
            case STRUCT:
            case ENUM:
            case TYPEDEF:
            case BUILT_IN:
            case ENUMERATOR:
            case MACRO:
            case VARIABLE_DEFINITION:
            case TEMPLATE_SPECIALIZATION:
            case ASM:
            case TEMPLATE_DECLARATION:
            case NAMESPACE_DEFINITION:
            case NAMESPACE_ALIAS:
            case USING_DIRECTIVE:
            case USING_DECLARATION:
            case CLASS_FORWARD_DECLARATION:
            case CLASS_FRIEND_DECLARATION:
                break;
        }
    }


}
