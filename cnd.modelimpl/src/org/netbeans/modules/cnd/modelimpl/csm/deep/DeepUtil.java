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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.cnd.modelimpl.csm.deep;

import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmCondition;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import java.util.*;

/**
 * Misc static methods used by deep impls
 * @author Vladimir Kvashin
 */
public class DeepUtil {

    public static List<CsmScopeElement> merge(CsmVariable var, List<CsmStatement> statements) {
        if( var == null ) {
            return (List)statements;
        }
        else {
            List<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
            l.add(var);
            l.addAll(statements);
            return l;
        }
    }
    
    public static List<CsmScopeElement> merge(CsmCondition condition, CsmStatement statement) {
        return merge(condition == null ? null : condition.getDeclaration(),  statement);
    }

    public static List<CsmScopeElement> merge(CsmCondition condition, CsmStatement statement1, CsmStatement statement2) {
        CsmVariable var = (condition == null) ? (CsmVariable) null : condition.getDeclaration();
        List<CsmScopeElement> l = merge(var,  statement1);
        if( statement2 != null ) {
            l.add(statement2);
        }
        return l;
    }
    
    public static List<CsmScopeElement> merge(CsmVariable var, CsmStatement statement) {
        List<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        if( var != null ) {
            l.add(var);
        }
        if( statement != null ) {
            l.add(statement);
        }
        return l;
    }    
}

