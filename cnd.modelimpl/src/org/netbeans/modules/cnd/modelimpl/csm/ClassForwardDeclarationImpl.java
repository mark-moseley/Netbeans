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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 *
 * @author Vladimir Kvasihn
 */
public class ClassForwardDeclarationImpl extends OffsetableDeclarationBase<CsmClassForwardDeclaration> implements CsmClassForwardDeclaration {
    private final CharSequence name;
    private final CharSequence[] nameParts;
    
    public ClassForwardDeclarationImpl(AST ast, FileImpl file) {
        super(ast, file);
        AST qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        name = (qid == null) ? CharSequenceKey.empty() : QualifiedNameCache.getManager().getString(AstRenderer.getQualifiedName(qid));
        nameParts = initNameParts(qid);
    }

    public CsmScope getScope() {
        return getContainingFile();
    }

    public CharSequence getName() {
        return getQualifiedName();
    }

    public CharSequence getQualifiedName() {
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

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name.toString());
        PersistentUtils.writeStrings(this.nameParts, output);
    }
    
    public ClassForwardDeclarationImpl(DataInput input) throws IOException {
        super(input);
        this.name = QualifiedNameCache.getManager().getString(input.readUTF());
        assert this.name != null;
        this.nameParts = PersistentUtils.readStrings(input, NameCache.getManager());
    }
}
