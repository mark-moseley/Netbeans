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

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * Common ancestor for ClassImpl and EnumImpl
 * @author Vladimir Kvashin
 */
public abstract class ClassEnumBase extends DeclarationBase implements CsmCompoundClassifier, CsmMember {

    private String qualifiedName;
    private NamespaceImpl namespace;
    private boolean isValid = true;

    private boolean _static = false;
    private CsmVisibility visibility = CsmVisibility.PRIVATE;
    private CsmClass containingClass = null;
     
    public ClassEnumBase(CsmDeclaration.Kind kind, String name, NamespaceImpl namespace, CsmFile file, int start, int end, CsmClass containingClass) {
        super(kind, name, file, start, end);
        this.namespace = namespace;
        this.containingClass = containingClass;
        if( containingClass == null ) {
            qualifiedName = Utils.getQualifiedName(getName(), namespace);
        }
        else {
            qualifiedName = containingClass.getQualifiedName() + "::" + getName();
        }
// can't register here, because descendant class' constructor hasn't yet finished!
// so registering is a descendant class' responsibility
//        registerInProject();
//        namespace.addDeclaration(this);
    }
    
    protected void register() {
        if( getName().length() > 0 ) {
            registerInProject();
            namespace.addDeclaration(this);
        }
    }
    
    private void registerInProject() {
        ((ProjectBase) getContainingFile().getProject()).registerDeclaration(this);
    }
    
    public CsmNamespace getContainingNamespace() {
        return namespace;
    }
    
    public NamespaceImpl getContainingNamespaceImpl() {
        return namespace;
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }

  
    public CsmScope getScope() {
        // TODO: think over: containing class?
        // TODO: what if declared in a statement?
        return getContainingNamespace(); 
    }

    public void dispose() {
        getContainingNamespaceImpl().removeDeclaration(this);
        isValid = false;
    }
        
    public boolean isValid() {
        return isValid;
    }
    
    public CsmClass getContainingClass() {
        return containingClass;
    }
    
//    private void setContainingClass(CsmClass cls) {
//        containingClass = cls;
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
    
}
