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

