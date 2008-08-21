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

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * Common ancestor for ClassImpl and EnumImpl
 * @author Vladimir Kvashin
 */
public abstract class ClassEnumBase<T> extends OffsetableDeclarationBase<T> implements Disposable, CsmCompoundClassifier<T>, CsmMember<T> {
    
    private final CharSequence name;
    
    private /*final*/ CharSequence qualifiedName;
    
    // only one of scopeRef/scopeAccessor must be used (based on USE_REPOSITORY)
    private CsmScope scopeRef;// can be set in onDispose or contstructor only
    private CsmUID<CsmScope> scopeUID;
    
    private boolean isValid = true;

    private boolean _static = false;
    private CsmVisibility visibility = CsmVisibility.PRIVATE;
    private final List<CsmUID<CsmTypedef>> enclosingTypdefs = Collections.synchronizedList(new ArrayList<CsmUID<CsmTypedef>>());
    
    protected ClassEnumBase(String name, CsmFile file, AST ast) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        this.name = (name == null) ? CharSequenceKey.empty() : NameCache.getManager().getString(name);
    }

    protected static int getEndOffset(AST node) {
        if (node != null) {
            AST rcurly = AstUtil.findChildOfType(node, CPPTokenTypes.RCURLY);
            if (rcurly instanceof CsmAST) {
                return ((CsmAST)rcurly).getEndOffset();
            } else {
                return OffsetableBase.getEndOffset(node);
            }
        }
        return 0;
    }

    public CharSequence getName() {
        return name;
    }

    protected ClassImpl.ClassMemberForwardDeclaration isClassDefinition(){
        CsmScope scope = getScope();
        if (name != null && name.toString().indexOf("::") > 0) { // NOI18N
            String n = name.toString();
            String prefix = n.substring(0,n.indexOf("::")); // NOI18N
            String suffix = n.substring(n.indexOf("::")+2); // NOI18N
            if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace) scope;
                String qn;
                if (ns.isGlobal()) {
                    qn = prefix;
                } else {
                    qn = ns.getQualifiedName().toString()+"::"+prefix; // NOI18N
                }
                CsmClassifier cls = ns.getProject().findClassifier(qn);
                if (cls != null) {
                    scope = (CsmScope) cls;
                    if (CsmKindUtilities.isClass(cls)){
                        CsmClass container = (CsmClass) cls;
                        Iterator<CsmMember> it = CsmSelect.getDefault().getClassMembers(container,
                                CsmSelect.getDefault().getFilterBuilder().createNameFilter(suffix, true, true, false));
                        if (it.hasNext()){
                            CsmMember m = it.next();
                            if (m instanceof ClassImpl.ClassMemberForwardDeclaration) {
                                return (ClassImpl.ClassMemberForwardDeclaration) m;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /** Initializes scope */
    protected final void initScope(CsmScope scope, AST ast) {
	if (scope instanceof CsmIdentifiable) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (this.scopeUID != null || scope == null) : "null UID for class scope " + scope;
            this.scopeRef = null;
	} else {
	    // in the case of classes/enums inside bodies
	    this.scopeRef = scope;
	}
    }

    /** Initializes qualified name */
    protected final void initQualifiedName(CsmScope scope, AST ast) {
	CharSequence qualifiedNamePostfix = getQualifiedNamePostfix();
        if(  CsmKindUtilities.isNamespace(scope) ) {
            qualifiedName = Utils.getQualifiedName(qualifiedNamePostfix.toString(), (CsmNamespace) scope);
        }
	else if( CsmKindUtilities.isClass(scope) ) {
            qualifiedName = ((CsmClass) scope).getQualifiedName() + "::" + qualifiedNamePostfix; // NOI18N
	}
        else  {
	    qualifiedName = qualifiedNamePostfix;
        }
        qualifiedName = QualifiedNameCache.getManager().getString(qualifiedName);
        // can't register here, because descendant class' constructor hasn't yet finished!
        // so registering is a descendant class' responsibility
    }
    
    abstract public Kind getKind();
    
    protected void register(CsmScope scope, boolean registerUnnamedInNamespace) {
        
        RepositoryUtils.put(this);
        boolean registerInNamespace = registerUnnamedInNamespace;
        if( ProjectBase.canRegisterDeclaration(this) ) {
            registerInProject();
	    registerInNamespace = true;
        }
        if (registerInNamespace) {
            if (getContainingClass() == null) {
                if (CsmKindUtilities.isNamespace(scope)) {
                    ((NamespaceImpl) scope).addDeclaration(this);
                }
            }
        }
    }
    
    private void registerInProject() {
        ClassImpl.ClassMemberForwardDeclaration fd = isClassDefinition();
        if (fd != null && CsmKindUtilities.isClass(this))  {
            fd.setCsmClass((CsmClass)this);
            return;
        }
       ((ProjectBase) getContainingFile().getProject()).registerDeclaration(this);
    }
    
    private void unregisterInProject() {
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
        this.cleanUID();
    }
    
    public NamespaceImpl getContainingNamespaceImpl() {
	CsmScope scope = getScope();
	return (scope instanceof NamespaceImpl) ? (NamespaceImpl) scope : null;
    }
    
    public CharSequence getQualifiedName() {
        return qualifiedName;
    }

  
    public CsmScope getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        if (getContainingNamespaceImpl() != null) {
            getContainingNamespaceImpl().removeDeclaration(this);
        }
	unregisterInProject();
        isValid = false;
    }
        
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }
    
    public boolean isValid() {
        return isValid && getContainingFile().isValid();
    }

    public CsmClass getContainingClass() {
	CsmScope scope = getScope();
	return CsmKindUtilities.isClass(scope) ? (CsmClass) scope : null;
    }
    
//    private void setContainingClass(CsmClass cls) {
//        containingClassRef = cls;
//        qualifiedName = cls.getQualifiedName() + "::" + getName();
//    }
    
    public CsmVisibility getVisibility() {
        return visibility;
    }
    
    public void setVisibility(CsmVisibility visibility) {
        this.visibility = visibility;
    }
    
    public boolean isStatic() {
        return _static;
    }
    
    public void setStatic(boolean _static) {
        this._static = _static;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output); 
        output.writeBoolean(this.isValid);
	
        assert this.name != null;
        output.writeUTF(this.name.toString());
	
        assert this.qualifiedName != null;
        output.writeUTF(this.qualifiedName.toString());
        
	UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
        
        output.writeBoolean(this._static);
	
        assert this.visibility != null;
        PersistentUtils.writeVisibility(this.visibility, output);
        
        // write UID for unnamed classifier
        if (getName().length() == 0) {
            super.writeUID(output);
        }
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(enclosingTypdefs, output, true);
    }  
    
    protected ClassEnumBase(DataInput input) throws IOException {
        super(input);
        this.isValid = input.readBoolean();
	
        this.name = NameCache.getManager().getString(input.readUTF());
        assert this.name != null;
	
        this.qualifiedName = QualifiedNameCache.getManager().getString(input.readUTF());
        assert this.qualifiedName != null;
	
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.scopeRef = null;
	
        this._static = input.readBoolean();
	
        this.visibility = PersistentUtils.readVisibility(input);
        assert this.visibility != null;
                
        // restore UID for unnamed classifier
        if (getName().length() == 0) {
            super.readUID(input);
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(enclosingTypdefs, input);
    }

    public Collection<CsmTypedef> getEnclosingTypedefs() {
        return UIDCsmConverter.UIDsToDeclarations(enclosingTypdefs);
    }

    public void addEnclosingTypedef(CsmTypedef typedef) {
        enclosingTypdefs.add(typedef.getUID());
    }
}
