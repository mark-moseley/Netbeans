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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class OffsetableDeclarationBase<T> extends OffsetableIdentifiableBase<T> implements CsmOffsetableDeclaration<T> {
    
    public static final char UNIQUE_NAME_SEPARATOR = ':';
    
    public OffsetableDeclarationBase(AST ast, CsmFile file) {
        super(ast, file);
    }
    
    public OffsetableDeclarationBase(CsmFile file) {
        super(file);
    }
    
    public String getUniqueName() {
        return getKind().toString() + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
    }
    
    public String getUniqueNameWithoutPrefix() {
        return getQualifiedName();
    }
    
    protected final CsmProject getProject() {
        CsmFile file = this.getContainingFile();
        assert file != null;
        return file != null ? file.getProject() : null;
    }    
    
    protected String getQualifiedNamePostfix() {
        if (TraceFlags.SET_UNNAMED_QUALIFIED_NAME && (getName().length() == 0)) {
            return getOffsetBasedName();
        } else {
            return getName();
        }
    }
    
    private String getOffsetBasedName() {
        return "[" + this.getContainingFile().getName() + ":" + this.getStartOffset() + "-" + this.getEndOffset() + "]"; // NOI18N
    }   
    
    protected CsmUID createUID() {
        return UIDUtilities.createDeclarationUID(this);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }  
    
    protected OffsetableDeclarationBase(DataInput input) throws IOException {
        super(input);
    }    

    public String toString() {
        return "" + getKind() + ' ' + getName()  + getOffsetString(); // NOI18N
    }    
}
