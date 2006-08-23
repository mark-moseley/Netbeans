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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.DeclarationBase;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 *
 * @author vk155633
 */
public abstract class OffsetableDeclarationBase extends OffsetableBase implements CsmDeclaration {
    
    public static final char UNIQUE_NAME_SEPARATOR = ':';
    
    public OffsetableDeclarationBase(AST ast, CsmFile file) {
        super(ast, file);
    }
    
    public OffsetableDeclarationBase(CsmFile file, int start, int end) {
        super(file, start, end);
    }
    
    public String getUniqueName() {
        return getKind().toString() + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
    }
    
    public String getUniqueNameWithoutPrefix() {
        return getQualifiedName();
    }
    
}
