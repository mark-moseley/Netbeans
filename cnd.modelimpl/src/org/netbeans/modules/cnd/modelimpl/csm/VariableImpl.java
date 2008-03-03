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

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 *
 * @param T 
 * @author Dmitriy Ivanov
 */
public class VariableImpl<T> extends OffsetableDeclarationBase<T> implements CsmVariable<T>, Disposable {
    
    private final CharSequence name;
    private final CsmType type;
    private boolean _static = false;
    
    // only one of scopeRef/scopeAccessor must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private CsmScope scopeRef;
    private CsmUID<CsmScope> scopeUID;
    
    private final boolean _extern;
    private ExpressionBase initExpr;

    /** Creates a new instance of VariableImpl 
     * @param ast 
     * @param file 
     * @param type 
     * @param name 
     * @param registerInProject 
     */
    public VariableImpl(AST ast, CsmFile file, CsmType type, String name, boolean registerInProject) {
	this(ast, file, type, name, null, registerInProject);
    }
    
    /** Creates a new instance of VariableImpl 
     * @param ast 
     * @param file 
     * @param type 
     * @param name 
     * @param scope variable scope
     * @param registerInProject 
     */
    public VariableImpl(AST ast, CsmFile file, CsmType type, String name, CsmScope scope,  boolean registerInProject) {
        super(ast, file);
        initInitialValue(ast);
        _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        _extern = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern);
        this.name = QualifiedNameCache.getManager().getString(name);
        this.type = type;
	_setScope(scope);
        if (registerInProject) {
            registerInProject();
        }
    }
    
    public VariableImpl(CsmOffsetable pos, CsmFile file, CsmType type, String name, CsmScope scope, boolean _static, boolean _extern, boolean registerInProject) {
        super(file, pos);
        this._static = _static;
        this._extern = _extern;
        this.name = QualifiedNameCache.getManager().getString(name);
        this.type = type;
	_setScope(scope);
        if (registerInProject) {
            registerInProject();
        }
    }
    
    protected final void registerInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            ((ProjectBase) project).registerDeclaration(this);
        }
    }
    
    private void unregisterInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            ((ProjectBase) project).unregisterDeclaration(this);
            this.cleanUID();
        }
    }
    
    
    /** Gets this element name 
     * @return 
     */
    public CharSequence getName() {
        return name;
    }
    
    public CharSequence getQualifiedName() {
        CsmScope scope = getScope();
        if( (scope instanceof CsmNamespace) || (scope instanceof CsmClass) ) {
            return CharSequenceKey.create(((CsmQualifiedNamedElement) scope).getQualifiedName() + "::" + getQualifiedNamePostfix()); // NOI18N
        }
        return getName();
    }
    
    @Override
    public CharSequence getUniqueNameWithoutPrefix() {
        if (isExtern()) {
            return getQualifiedName() + " (EXTERN)"; // NOI18N
        } else {
            return getQualifiedName();
        }
    }
    
    /** Gets this variable type 
     * @return 
     */
    // TODO: fix it
    public CsmType getType() {
        return type;
    }
    
    private final void initInitialValue(AST node) {
        if( node != null ) {
            AST tok = AstUtil.findChildOfType(node, CPPTokenTypes.ASSIGNEQUAL);
            if( tok != null ) {
                tok = tok.getNextSibling();
            }
            if( tok != null ) {
                initExpr = new ExpressionBase(tok, getContainingFile(), null, _getScope());
            }
        }
    }
    
    /** Gets this variable initial value 
     * @return 
     */
    public CsmExpression getInitialValue() {
        return initExpr;
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.VARIABLE;
    }
    
    //TODO: create an interface to place getDeclarationText() in
    public String getDeclarationText() {
        return "";
    }
    
    public boolean isAuto() {
        return true;
    }
    
    public boolean isRegister() {
        return false;
    }
    
    public boolean isStatic() {
        return _static;
    }
    
    public void setStatic(boolean _static) {
        this._static = _static;
    }
    
    public boolean isExtern() {
        return _extern;
    }
    
    public boolean isConst() {
        CsmType _type = getType();
        if( _type != null ) {
            return _type.isConst();
        }
        return false;
    }
    
//    // TODO: remove and replace calls with
//    // isConst() && ! isExtern
//    public boolean isConstAndNotExtern() {
//        if( isExtern() ) {
//            return false;
//        }
//        else {
//            // it isn't extern
//            CsmType type = getType();
//            if( type == null ) {
//                return false;
//            }
//            else {
//                return type.isConst();
//            }
//        }
//    }
    
    public boolean isMutable() {
        return false;
    }
    
    public void setScope(CsmScope scope) {
        unregisterInProject();
        this._setScope(scope);
        registerInProject();
    }
    
    public CsmScope getScope() {
        return _getScope();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        // dispose type
        if (this.type != null && this.type instanceof Disposable) {
            ((Disposable)this.type).dispose();
        }
        if( _getScope() instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) _getScope()).removeDeclaration(this);
        }
        unregisterInProject();
    }
    
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }    
    
    public CsmVariableDefinition getDefinition() {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.VARIABLE_DEFINITION) + UNIQUE_NAME_SEPARATOR + getQualifiedName();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
        return (def == null) ? null : (CsmVariableDefinition) def;
    }
    
    private CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            // scope could be null when enclosing context is invalidated
        }
        return scope;
    }
    
    private void _setScope(CsmScope scope) {
	// for variables declared in bodies scope is CsmCompoundStatement - it is not Identifiable
        if ((scope instanceof CsmIdentifiable)) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (scopeUID != null || scope == null);
        } else {
            this.scopeRef = scope;
        }
    }
    
    public CharSequence getDisplayText() {
	StringBuilder sb = new StringBuilder();
	CsmType _type = getType();
	if( _type instanceof TypeImpl ) {
	    return ((TypeImpl) _type).getText(false, this.getName()).toString();
	}
	else if( _type != null ) {
	    sb.append(_type.getText());
	    CharSequence _name = getName();
	    if (_name != null && _name.length() >0) {
		sb.append(' ');
		sb.append(_name);
	    }
	}
	return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output); 
        assert this.name != null;
        output.writeUTF(this.name.toString());
        output.writeBoolean(this._static);
        output.writeBoolean(this._extern);
        PersistentUtils.writeExpression(initExpr, output);
        PersistentUtils.writeType(type, output);
             
        // could be null UID (i.e. parameter)
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);        
    }  
    
    public VariableImpl(DataInput input) throws IOException {
        super(input);
        this.name = QualifiedNameCache.getManager().getString(input.readUTF());
        assert this.name != null;
        this._static = input.readBoolean();
        this._extern = input.readBoolean();
        this.initExpr = (ExpressionBase) PersistentUtils.readExpression(input);
        this.type = PersistentUtils.readType(input);

        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // could be null UID (i.e. parameter)
        this.scopeRef = null;
    }    
    
    @Override
    public String toString() {
        return (isExtern() ? "EXTERN " : "") + super.toString(); // NOI18N
    }     
}
