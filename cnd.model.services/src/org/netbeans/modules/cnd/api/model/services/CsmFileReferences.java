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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Set;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.openide.util.Lookup;

/**
 * Provides a list of CsmReferences of the identifiers in the CsmFile
 * 
 * @author Sergey Grinev
 */
public abstract class CsmFileReferences {
    /**
     * Provides visiting of the identifiers of the CsmFile
     */
    public abstract void accept(CsmScope csmScope, Visitor visitor);

    /**
     * Provides visiting of the identifiers of the CsmFile and point prefered 
     * kinds of references
     */
    public abstract void accept(CsmScope csmScope, Visitor visitor, Set<CsmReferenceKind> preferedKinds);
    
    /**
     * A dummy resolver that do nothing.
     */
    private static final CsmFileReferences EMPTY = new Empty();
    
    /** default instance */
    private static CsmFileReferences DEFAULT;
    
    protected CsmFileReferences() {
    }
    
    /** Static method to obtain the CsmFileReferences implementation.
     * @return the resolver
     */
    public static synchronized CsmFileReferences getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }
        DEFAULT = Lookup.getDefault().lookup(CsmFileReferences.class);
        return DEFAULT == null ? EMPTY : DEFAULT;
    }
    
    //
    // Implementation of the default query
    //
    private static final class Empty extends CsmFileReferences {
        Empty() {
        }

        @Override
        public void accept(CsmScope csmScope, Visitor visitor) {
            // do nothing
        }
        
        @Override
        public void accept(CsmScope csmScope, Visitor visitor, Set<CsmReferenceKind> kinds) {
            // do nothing
        }        
    }
    
    /**
     * visitor inteface
     */
    public interface Visitor {
        /**
         * This method is invoked for every matching reference in the file.
         * 
         * @param context  reference with its lexical context
         */
        void visit(CsmReferenceContext context);
    }

    /**
     * Determines whether reference is dereferenced template parameter
     */
    public static boolean isTemplateBased(CsmReferenceContext context) {
        if (2 <= context.size() && isDereference(context.getToken())) {
            CsmReference ref = context.getReference(context.size() - 2);
            if (ref != null) {
                CsmObject obj = ref.getReferencedObject();
                if (obj == null || isTemplateParameterInvolved(obj)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether reference is dereferenced macro
     */
    public static boolean isMacroBased(CsmReferenceContext context) {
        if (2 <= context.size() && isDereference(context.getToken())) {
            CsmReference ref = context.getReference(context.size() - 2);
            if (ref != null) {
                CsmObject obj = ref.getReferencedObject();
                if (obj == null || CsmKindUtilities.isMacro(obj)) {
                    return true;
                }
            }
        }
        for (int i = context.size() - 1; 0 < i; --i) {
            if (context.getToken(i) == CppTokenId.LPAREN) {
                CsmReference ref = context.getReference(i - 1);
                if (ref != null && CsmKindUtilities.isMacro(ref.getReferencedObject())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTemplateParameterInvolved(CsmObject obj) {
        if (CsmKindUtilities.isTemplateParameter(obj)) {
            return true;
        }
        CsmType type = null;
        if (CsmKindUtilities.isFunction(obj)) {
            type = ((CsmFunction)obj).getReturnType();
        } else if (CsmKindUtilities.isVariable(obj)) {
            type = ((CsmVariable)obj).getType();
        } else if(CsmKindUtilities.isTypedef(obj)) {
            type = ((CsmTypedef) obj).getType();
        }
        return (type == null) ? false : type.isTemplateBased();
    }

    public static boolean isDereference(CppTokenId token) {
        if (token == null) {
            return false;
        }
        switch (token) {
            case DOT:
            case DOTMBR:
            case ARROW:
            case ARROWMBR:
            case SCOPE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBracket(CppTokenId token) {
        if (token == null) { 
            return false; 
        }
        switch (token) {
            case LBRACE:
            case LBRACKET:
            case LPAREN:
            case LT:
                return true;
            default:
                return false;
        }
    }

}
