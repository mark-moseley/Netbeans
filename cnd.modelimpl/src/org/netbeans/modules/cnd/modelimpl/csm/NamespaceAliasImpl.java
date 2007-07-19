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

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Implements CsmNamespaceAlias
 * @author Vladimir Kvasihn
 */
public class NamespaceAliasImpl extends OffsetableDeclarationBase<CsmNamespaceAlias> implements CsmNamespaceAlias, RawNamable {

    private final String alias;
    private final String namespace;
    private final String[] rawName;
    
    private CsmNamespace referencedNamespaceOLD = null;
    private CsmUID<CsmNamespace> referencedNamespaceUID = null;
    
    public NamespaceAliasImpl(AST ast, CsmFile file) {
        super(ast, file);
        rawName = createRawName(ast);
        alias = ast.getText();
        AST token = ast.getFirstChild();
        while( token != null && token.getType() != CPPTokenTypes.ASSIGNEQUAL ) {
            token = token.getNextSibling();
        }
        StringBuilder sb = new StringBuilder();
        if( token == null ) {
            if( FileImpl.reportErrors ) {
                int ln = ast.getLine();
                int col = ast.getColumn();
                AST child = ast.getFirstChild();
                if( child != null ) {
                    ln = child.getLine();
                    col = child.getColumn();
                }
                System.err.println("Corrupted AST for namespace alias in " + 
                file.getAbsolutePath() + ' ' + ln + ":" + col); // NOI18N
            }
            namespace = "";
        }
        else {
            for( token = token.getNextSibling() ; token != null; token = token.getNextSibling() ) {
                sb.append(token.getText());
            }
            namespace = sb.toString();
        }
    }

    public CsmNamespace getReferencedNamespace() {
//        if (!Boolean.getBoolean("cnd.modelimpl.resolver2"))
        assert ResolverFactory.resolver != 2;
        return getContainingFile().getProject().findNamespace(namespace);
    }

    private CsmNamespace _getReferencedNamespace() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmNamespace referencedNamespace = UIDCsmConverter.UIDtoNamespace(referencedNamespaceUID);
            // can be null if namespace was removed 
            return referencedNamespace;
        } else {
            return this.referencedNamespaceOLD;
        }
    }    

    private void _setReferencedNamespace(CsmNamespace referencedNamespace) {
        if (TraceFlags.USE_REPOSITORY) {
            this.referencedNamespaceUID = UIDCsmConverter.namespaceToUID(referencedNamespace);
            assert this.referencedNamespaceUID != null || referencedNamespace == null;
        } else {
            this.referencedNamespaceOLD = referencedNamespace;
        }
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.NAMESPACE_ALIAS;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return getAlias();
    }
    
    public String getQualifiedName() {
        return getName();
    }
    
    private static String[] createRawName(AST node) {
        AST token = node.getFirstChild();
        while( token != null && token.getType() != CPPTokenTypes.ASSIGNEQUAL ) {
            token = token.getNextSibling();
        }
        if( token != null ) {
            token = token.getNextSibling();
            if( token != null && token.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                return AstUtil.getRawName(token.getFirstChild());
            }
        }
        return new String[0];
    }

    public String[] getRawName() {
        return rawName;
    }
    
    public String toString() {
        return "" + getKind() + ' ' + alias + '=' + namespace /*+ " rawName=" + Utils.toString(getRawName())*/; // NOI18N
    }
    
    public CsmScope getScope() {
        //TODO: implement!
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.alias != null;
        output.writeUTF(this.alias);
        assert this.namespace != null;
        output.writeUTF(this.namespace);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // save cached namespace
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedNamespaceUID, output);
    }
    
    public NamespaceAliasImpl(DataInput input) throws IOException {
        super(input);
        this.alias = TextCache.getString(input.readUTF());
        assert this.alias != null;
        this.namespace = TextCache.getString(input.readUTF());
        assert this.namespace != null;
        this.rawName = PersistentUtils.readStrings(input, TextCache.getManager());
        
        // read cached namespace
        this.referencedNamespaceUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
    }    
}
