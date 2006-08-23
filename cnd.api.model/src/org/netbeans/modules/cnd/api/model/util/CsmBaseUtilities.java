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

package org.netbeans.modules.cnd.api.model.util;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmBaseUtilities {

    /** Creates a new instance of CsmBaseUtilities */
    private CsmBaseUtilities() {
    }

    public static boolean isStaticContext(CsmFunctionDefinition funDef) {
        assert (funDef != null) : "must be not null";
        // static context is in global functions and static methods
        if (CsmKindUtilities.isGlobalFunction(funDef)) {
            return true;
        } else {
            CsmFunction funDecl = funDef.getDeclaration();
            if (CsmKindUtilities.isClassMember(funDecl)) {
                return ((CsmMember)funDecl).isStatic();
            }
        }
        return false;
    }
    
    public static CsmClass getFunctionClass(CsmFunction fun) {
        assert (fun != null) : "must be not null";
        CsmClass clazz = null;
        CsmFunction funDecl = getFunctionDeclaration(fun);
        if (CsmKindUtilities.isClassMember(funDecl)) {
            clazz = ((CsmMember)funDecl).getContainingClass();
        }
        return clazz;
    }   
        
    public static CsmFunction getFunctionDeclaration(CsmFunction fun) {
        assert (fun != null) : "must be not null";
        CsmFunction funDecl = fun;
        if (CsmKindUtilities.isFunctionDefinition(funDecl)) {
            funDecl = ((CsmFunctionDefinition)funDecl).getDeclaration();
        }
        return funDecl;
    }    
}
