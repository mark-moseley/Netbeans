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
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 *
 * @author Vladimir Kvasihn
 */
public class FriendClassImpl extends OffsetableDeclarationBase<CsmFriendClass> implements CsmFriendClass {
    private final String name;
    private final String[] nameParts;
    private final CsmUID<CsmClass> parentUID;
    
    public FriendClassImpl(AST ast, FileImpl file, CsmClass parent) {
        super(ast, file);
        this.parentUID = parent.getUID();
        AST qid = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        name = (qid == null) ? "" : AstRenderer.getQualifiedName(qid);
        nameParts = initNameParts(qid);
        registerInProject();
    }

    private void registerInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            ((ProjectBase) project).registerDeclaration(this);
        }
    }

    public CsmClass getContainingClass() {
        return parentUID.getObject();
    }

    public CsmScope getScope() {
        return getContainingClass();
    }

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        CsmClass cls = getContainingClass();
        String clsQName = cls.getQualifiedName();
	if( clsQName != null && clsQName.length() > 0 ) {
            return clsQName + "::" + getQualifiedNamePostfix(); // NOI18N
	}
        return getName();
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION;
    }

    public CsmClass getReferencedClass() {
        return getReferencedClass(null);
    }

    public CsmClass getReferencedClass(Resolver resolver) {
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

    public void dispose() {
        super.dispose();
	unregisterInProject();
    }

    private void unregisterInProject() {
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
        this.cleanUID();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name);
        PersistentUtils.writeStrings(this.nameParts, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.parentUID, output);    
    }


    public FriendClassImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this.nameParts = PersistentUtils.readStrings(input, TextCache.getManager());
        this.parentUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}
