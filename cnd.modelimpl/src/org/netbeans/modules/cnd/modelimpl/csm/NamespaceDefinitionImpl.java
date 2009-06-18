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

import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import java.util.*;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;

/**
 * Implements CsmNamespaceDefinition
 * @author Vladimir Kvasihn
 */
public final class NamespaceDefinitionImpl extends OffsetableDeclarationBase<CsmNamespaceDefinition>
    implements CsmNamespaceDefinition, MutableDeclarationsContainer, Disposable {

    private final List<CsmUID<CsmOffsetableDeclaration>> declarations;
    
    private final CharSequence name;
    
    // only one of namespaceRef/namespaceUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private /*final*/ NamespaceImpl namespaceRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmNamespace> namespaceUID;
    
    public NamespaceDefinitionImpl(AST ast, CsmFile file, NamespaceImpl parent) {
        super(ast, file);
        declarations = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        assert ast.getType() == CPPTokenTypes.CSM_NAMESPACE_DECLARATION;
        name = NameCache.getManager().getString(ast.getText());
        NamespaceImpl nsImpl = ((ProjectBase) file.getProject()).findNamespaceCreateIfNeeded(parent, name);
        
        // set parent ns, do it in constructor to have final fields
        namespaceUID = UIDCsmConverter.namespaceToUID(nsImpl);
        assert namespaceUID != null;
        this.namespaceRef = null;
        
        nsImpl.addNamespaceDefinition(this);
    }

    public static NamespaceDefinitionImpl findOrCreateNamespaceDefionition(MutableDeclarationsContainer container, AST ast, NamespaceImpl parentNamespace, FileImpl containerfile) {
        int start = getStartOffset(ast);
        int end = getEndOffset(ast);
        CharSequence name = NameCache.getManager().getString(ast.getText()); // otherwise equals returns false
        // #147376 Strange navigator behavior in header
        CsmOffsetableDeclaration candidate = container.findExistingDeclaration(start, end, name);
        if (CsmKindUtilities.isNamespaceDefinition(candidate)) {
            return (NamespaceDefinitionImpl) candidate;
        } else {
            NamespaceDefinitionImpl ns = new NamespaceDefinitionImpl(ast, containerfile, parentNamespace);
            container.addDeclaration(ns);
            return ns;
        }
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.NAMESPACE_DEFINITION;
    }
            
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        Collection<CsmOffsetableDeclaration> decls;
        synchronized (declarations) {
            decls = UIDCsmConverter.UIDsToDeclarations(declarations);
        }
        return decls;
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        Iterator<CsmOffsetableDeclaration> out;
        synchronized (declarations) {
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(declarations, filter);
         }
         return out;
    }

    public CsmOffsetableDeclaration findExistingDeclaration(int start, int end, CharSequence name) {
        CsmUID<CsmOffsetableDeclaration> out = null;
        // look for the object with the same start position and the same name
        // TODO: for now we are in O(n), but better to be O(ln n) speed
        synchronized (declarations) {
            out = UIDUtilities.findExistingUIDInList(declarations, start, end, name);
//            if (FileImpl.traceFile(getContainingFile().getAbsolutePath())) {
//                System.err.printf("%s found %s [%d-%d] in \n\t%s\n", (out == null) ? "NOT " : "", name, start, end, declarations);
//            }
        }
        return UIDCsmConverter.UIDtoDeclaration(out);
    }

    public void addDeclaration(CsmOffsetableDeclaration decl) {
        CsmUID<CsmOffsetableDeclaration> uid = RepositoryUtils.put(decl);
        assert uid != null;
        synchronized (declarations) {
            UIDUtilities.insertIntoSortedUIDList(uid, declarations);
        }
        if (decl instanceof VariableImpl) {
            VariableImpl v = (VariableImpl) decl;
            if (!NamespaceImpl.isNamespaceScope(v, false)) {
                v.setScope(this, true);
            }
        }
        if (decl instanceof FunctionImpl) {
            FunctionImpl f = (FunctionImpl) decl;
            if (!NamespaceImpl.isNamespaceScope(f)) {
                f.setScope(this);
            }
        }
        // update repository
        RepositoryUtils.put(this);
    }
    
    private void insertIntoSortedDeclArray(CsmUID<CsmOffsetableDeclaration> uid) {
        int start = UIDUtilities.getStartOffset(uid);
        // start from the last, because most of the time we are in append, not insert mode
        for (int pos = declarations.size() - 1; pos >= 0; pos--) {
            CsmUID<CsmOffsetableDeclaration> currUID = declarations.get(pos);
            int i = UIDUtilities.compareWithinFile(currUID, uid);
            if (i <= 0) {
                if (i == 0){
                    declarations.set(pos, uid);
                } else {
                    declarations.add(pos+1, uid);
                }
                return;
            } else if (UIDUtilities.getStartOffset(currUID) < start){
                break;
            }
        }
        declarations.add(uid);
    }
    
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> uid = UIDCsmConverter.declarationToUID(declaration);
        assert uid != null;
        synchronized (declarations) {
            declarations.remove(uid);
        }
        RepositoryUtils.remove(uid, declaration);
        // update repository
        RepositoryUtils.put(this);
    }

    public CharSequence getQualifiedName() {
        return getNamespace().getQualifiedName();
    }
    
//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }

    public CsmNamespace getNamespace() {
        return _getNamespaceImpl();
    }

    public CharSequence getName() {
        return name;
    }

    public CsmScope getScope() {
        return getContainingFile();
    }
    
    public Collection<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        for (Iterator iter = getDeclarations().iterator(); iter.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) iter.next();
            if (isOfMyScope(decl)) {
                l.add(decl);
            }
        }
        return l;
    }

    private boolean isOfMyScope(CsmDeclaration decl) {
        if (decl instanceof VariableImpl) {
            return ! NamespaceImpl.isNamespaceScope((VariableImpl) decl, false);
        } else if (decl instanceof FunctionImpl) {
            return ! NamespaceImpl.isNamespaceScope((FunctionImpl) decl);

        } else {
            return false;
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        Collection<CsmOffsetableDeclaration> decls;
        List<CsmUID<CsmOffsetableDeclaration>> uids;
        synchronized (declarations) {
            decls = getDeclarations();
            uids = declarations;
            declarations.clear();
            //declarations  = Collections.synchronizedList(new ArrayList<CsmUID<CsmOffsetableDeclaration>>());
        }
        Utils.disposeAll(decls);            
        RepositoryUtils.remove(uids);                      
        NamespaceImpl ns = _getNamespaceImpl();
        assert ns != null;
        ns.removeNamespaceDefinition(this);
    }

    private void onDispose() {
        if (this.namespaceRef == null) {
            // restore container from it's UID
            this.namespaceRef = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(this.namespaceUID);
            assert this.namespaceRef != null || this.namespaceUID == null : "no object for UID " + this.namespaceUID;
        }
    }
    
    private NamespaceImpl _getNamespaceImpl() {
        NamespaceImpl impl = this.namespaceRef;
        if (impl == null) {
            impl = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(this.namespaceUID);
            assert impl != null || this.namespaceUID == null : "null object for UID " + this.namespaceUID;
        }
        return impl;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);  
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.declarations, output, true);
        
        // not null
        assert this.namespaceUID != null;
        factory.writeUID(this.namespaceUID, output);
        
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);

        if (getName().length() == 0) {
            writeUID(output);
        }
    }  

    public NamespaceDefinitionImpl(DataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        int collSize = input.readInt();
        if (collSize < 0) {
            declarations = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        } else {
            declarations = new ArrayList<CsmUID<CsmOffsetableDeclaration>>(collSize);
        }
        factory.readUIDCollection(declarations, input, collSize);
        
        this.namespaceUID = factory.readUID(input);
        // not null UID
        assert this.namespaceUID != null;
        this.namespaceRef = null;    
        
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;

        if (getName().length() == 0) {
            readUID(input);
        }
    }      
}
