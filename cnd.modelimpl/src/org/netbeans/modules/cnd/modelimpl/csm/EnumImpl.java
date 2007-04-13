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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.* ;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Implements CsmEnum
 * @author Vladimir Kvashin
 */
public class EnumImpl extends ClassEnumBase<CsmEnum>  implements CsmEnum, CsmMember<CsmEnum> {
    
    private List/*CsmEnumerator*/ enumeratorsOLD = Collections.EMPTY_LIST;
    private final List<CsmUID<CsmEnumerator>> enumerators = new ArrayList<CsmUID<CsmEnumerator>>();
    
    private EnumImpl(AST ast, CsmFile file) {
        super(findName(ast), file, ast);
    }
    
    protected void init(NamespaceImpl namespace, CsmClass containingClass, AST ast) {
	super.init(namespace, containingClass, ast);
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
        }
        initEnumeratorList(ast);
        register();
    }
    
    public static EnumImpl create(AST ast, NamespaceImpl namespace, CsmFile file) {
	return create(ast, namespace, file, null);
    }
    
    public static EnumImpl create(AST ast, NamespaceImpl namespace, CsmFile file, CsmClass containingClass) {
	EnumImpl impl = new EnumImpl(ast, file);
	impl.init(namespace, containingClass, ast);
	return impl;
    }
    
    private static String findName(AST ast){
        String name = AstUtil.findId(ast, CPPTokenTypes.RCURLY);
        if (name == null || name.length()==0){
            AST token = ast.getNextSibling();
            if( token != null) {
                if (token.getType() == CPPTokenTypes.ID) {
                    //typedef enum C { a2, b2, c2 } D;
                    name = token.getText();
                }
            }
        }
        return name;
    }
    
    private void initEnumeratorList(AST ast){
        //enum A { a, b, c };
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST ) {
                addList(token);
                return;
            }
        }
        AST token = ast.getNextSibling();
        if( token != null) {
            //typedef enum { a1, b1, c1 } B;
            if (token.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST ) {
                addList(token);
                return;
            } else if (token.getType() == CPPTokenTypes.ID) {
                token = token.getNextSibling();
                //typedef enum C { a2, b2, c2 } D;
                if( token != null && token.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST ) {
                    addList(token);
                    return;
                }
            }
        }
    }
    
    private void addList(AST token){
        for( AST t = token.getFirstChild(); t != null; t = t.getNextSibling() ) {
            if( t.getType() == CPPTokenTypes.ID ) {
                EnumeratorImpl ei = new EnumeratorImpl(t, this);
            }
        }
    }
    
    public List getEnumerators() {
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmEnumerator> out = UIDCsmConverter.UIDsToDeclarations(enumerators);
            return out;
        } else {
            return enumeratorsOLD;
        }
    }
    
    public void addEnumerator(CsmEnumerator enumerator) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmEnumerator> uid = RepositoryUtils.put(enumerator);
            enumerators.add(uid);
        } else {
            if(enumeratorsOLD == Collections.EMPTY_LIST) {
                enumeratorsOLD = new ArrayList();
            }
            enumeratorsOLD.add(enumerator);
        }
    }
    
    public List getScopeElements() {
        return getEnumerators();
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUM;
    }
    
    public void dispose() {
        _clearEnumerators();
        super.dispose();
    }
    
    private void _clearEnumerators() {
        List<CsmEnumerator> enumers = getEnumerators();
        Utils.disposeAll(enumers);
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.remove(enumerators);
        } else {
            enumeratorsOLD.clear();
        }
    }
    
////////////////////////////////////////////////////////////////////////////
// impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(this.enumerators, output, false);
    }
    
    public EnumImpl(DataInput input) throws IOException {
        super(input);
        UIDObjectFactory.getDefaultFactory().readUIDCollection(this.enumerators, input);
    }
}
