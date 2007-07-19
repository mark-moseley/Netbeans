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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 *
 * @author Vladimir Kvasihn
 */
public class ClassForwardDeclarationImpl extends OffsetableDeclarationBase<CsmClassForwardDeclaration> implements CsmClassForwardDeclaration {
    private final String name;
    private final String[] nameParts;
    
    public ClassForwardDeclarationImpl(AST ast, FileImpl file) {
        super(ast, file);
        AST qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        name = (qid == null) ? "" : AstRenderer.getQualifiedName(qid);
        nameParts = initNameParts(qid);
    }

    public CsmScope getScope() {
        return getContainingFile();
    }

    public String getName() {
        return getQualifiedName();
    }

    public String getQualifiedName() {
        return name;
    }

//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION;
    }

    public CsmClass getCsmClass() {
        return  getCsmClass(null);
    }
    
    public CsmClass getCsmClass(Resolver resolver) {
        CsmObject o = resolve(resolver);
        return (o instanceof CsmClass) ? (CsmClass) o : (CsmClass) null;
    }
    
    private String[] initNameParts(AST qid) {
        if( qid != null ) {
            return AstRenderer.getNameTokens(qid);
        }
        return new String[0];
    }
    
    private CsmObject resolve(Resolver resolver) {
        return ResolverFactory.createResolver(this, resolver).resolve(nameParts, Resolver.CLASSIFIER);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name);
        PersistentUtils.writeStrings(this.nameParts, output);
    }
    
    public ClassForwardDeclarationImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this.nameParts = PersistentUtils.readStrings(input, TextCache.getManager());
    }
}
