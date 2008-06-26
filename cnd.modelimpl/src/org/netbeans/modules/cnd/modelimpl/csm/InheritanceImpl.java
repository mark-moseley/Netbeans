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

import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * CsmInheritance implementation
 * @author Vladimir Kvashin
 */
public class InheritanceImpl extends OffsetableBase implements CsmInheritance {

    private CsmVisibility visibility;
    private boolean virtual;
    
    private CsmUID<CsmClass> resolvedAncestorClassCacheUID;
    
    private CsmUID<CsmClassifier> classifierCacheUID;
    
    private CharSequence ancestorName;

    private boolean lastResolveFalure;
    
    public InheritanceImpl(AST ast, CsmFile file) {
        super(ast, file);
        render(ast);
    }

    public boolean isVirtual() {
        return virtual;
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }

    public CsmClass getCsmClass() {
        return getCsmClass(null);
    }
    
    public CsmClass getCsmClass(Resolver parent) {
        CsmClass ancestorCache = _getAncestorCache();
        if (ancestorCache == null || !ancestorCache.isValid())
        {
            ancestorCache = null;
            CsmClassifier classifier = getCsmClassifier(parent);
            classifier = CsmBaseUtilities.getOriginalClassifier(classifier);
            if (CsmKindUtilities.isClass(classifier)) {
                ancestorCache = (CsmClass)classifier;
            }
            _setAncestorCache(ancestorCache);
        }
        return ancestorCache;
    }
    
    public CsmClassifier getCsmClassifier() {
        return getCsmClassifier(null);
    }
    
    public CsmClassifier getCsmClassifier(Resolver parent) {
        CsmClassifier classifierCache = _getClassifierCache();
        if (!lastResolveFalure) {
            if (classifierCache == null || 
                    ((classifierCache instanceof CsmValidable) && !((CsmValidable)classifierCache).isValid())) {
                classifierCache = renderClassifier(ancestorName, parent);
                _setClassifierCache(classifierCache);
            }
            lastResolveFalure = classifierCache == null || 
                    ((classifierCache instanceof CsmValidable) && !((CsmValidable)classifierCache).isValid());
        }
        return classifierCache;        
    }
    
    private void render(AST node) {
        visibility = CsmVisibility.PRIVATE;
        for( AST token = node.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_private:
                    visibility = CsmVisibility.PRIVATE;
                    break;
                case CPPTokenTypes.LITERAL_public:
                    visibility = CsmVisibility.PUBLIC;
                    break;
                case CPPTokenTypes.LITERAL_protected:
                    visibility = CsmVisibility.PROTECTED;
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    virtual = true;
                    break;
                case CPPTokenTypes.ID:
                    StringBuilder ancNameBuffer = new StringBuilder();
                    int counter = 0;
                    for( ; token != null; token = token.getNextSibling() ) {
                        switch( token.getType() ) {
                            case CPPTokenTypes.ID:
                                ancNameBuffer.append(token.getText());
                                break;
                            case CPPTokenTypes.SCOPE:
                                ancNameBuffer.append("::"); // NOI18N
                                counter++;
                                break;
                            default:
                                // here can be "<", ">" and other template stuff
                        }
                    }
                    //CsmObject o = ResolverFactory.createResolver(this).resolve(new String[] { token.getText() } );
                    this.ancestorName = ancNameBuffer.toString();
                    this.ancestorName = counter == 0 ? NameCache.getManager().getString(this.ancestorName) : QualifiedNameCache.getManager().getString(this.ancestorName);
                    return; // it's definitely the last!; besides otherwise we get NPE in for 
                    //break;
            }
        }
    }

    private CsmClassifier renderClassifier(CharSequence ancName, Resolver parent) {
        CsmClassifier result = null;
        CsmObject o = ResolverFactory.createResolver(this, parent).resolve(ancName, Resolver.CLASSIFIER);
        if( CsmKindUtilities.isClassifier(o) ) {
            result = (CsmClassifier) o;
        }
        return result;
    }
    
    public CsmClass _getAncestorCache() {
        // can be null if cached one was removed 
        return UIDCsmConverter.UIDtoDeclaration(resolvedAncestorClassCacheUID);
    }

    public void _setAncestorCache(CsmClass ancestorCache) {
        resolvedAncestorClassCacheUID = UIDCsmConverter.declarationToUID(ancestorCache);
        assert (resolvedAncestorClassCacheUID != null || ancestorCache == null);
    }
    

    private CsmClassifier _getClassifierCache() {
        CsmClassifier classifierCache = UIDCsmConverter.UIDtoDeclaration(classifierCacheUID);
        // can be null if cached one was removed 
        return classifierCache;            
    }

    private void _setClassifierCache(CsmClassifier classifierCache) {
        classifierCacheUID = UIDCsmConverter.declarationToUID(classifierCache);
        assert (classifierCacheUID != null || classifierCacheUID == null);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeVisibility(this.visibility, output);
        output.writeBoolean(this.virtual);
        assert this.ancestorName != null;
        output.writeUTF(ancestorName.toString());        

        // save cache
        UIDObjectFactory.getDefaultFactory().writeUID(classifierCacheUID, output);     
        boolean theSame = ((CsmUID)resolvedAncestorClassCacheUID == (CsmUID)classifierCacheUID);
        output.writeBoolean(theSame);
        if (!theSame) {
            UIDObjectFactory.getDefaultFactory().writeUID(resolvedAncestorClassCacheUID, output);        
        }
    }

    @SuppressWarnings("unchecked")
    public InheritanceImpl(DataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
        this.virtual = input.readBoolean();
        this.ancestorName = input.readUTF();
        this.ancestorName = ancestorName.toString().indexOf("::") == -1 ? NameCache.getManager().getString(ancestorName) : QualifiedNameCache.getManager().getString(ancestorName); // NOI18N
        assert this.ancestorName != null;

        // restore cached value
        this.classifierCacheUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        boolean theSame = input.readBoolean();
        if (!theSame) {
            this.resolvedAncestorClassCacheUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        } else {
            this.resolvedAncestorClassCacheUID = (CsmUID)this.classifierCacheUID;
        }
    }    

    @Override
    public String toString() {
        return "INHERITANCE " + visibility + " " + (isVirtual() ? "virtual " : "") + ancestorName + getOffsetString(); // NOI18N
    }  
}
