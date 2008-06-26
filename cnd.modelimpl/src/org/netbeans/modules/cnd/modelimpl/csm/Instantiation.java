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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author eu155513
 */
public abstract class Instantiation<T> implements CsmOffsetableDeclaration<T>, CsmInstantiation {
    protected final CsmOffsetableDeclaration declaration;
    protected final Map<CsmTemplateParameter, CsmType> mapping;

    public Instantiation(CsmOffsetableDeclaration declaration, CsmType instType) {
        this.declaration = declaration;
        this.mapping = new HashMap<CsmTemplateParameter, CsmType>();
        // create mapping map
        if (CsmKindUtilities.isTemplate(declaration)) {
            Iterator<CsmType> typeIter = instType.getInstantiationParams().iterator();
            for (CsmTemplateParameter param : ((CsmTemplate)declaration).getTemplateParameters()) {
                if (!typeIter.hasNext()) {
                    break;
                }
                mapping.put(param, typeIter.next());
            }
        }
    }
    
    public Instantiation(CsmOffsetableDeclaration declaration, Map<CsmTemplateParameter, CsmType> mapping) {
        this.declaration = declaration;
        this.mapping = mapping;
    }

    public CsmOffsetableDeclaration getTemplateDeclaration() {
        return declaration;
    }

    public Map<CsmTemplateParameter, CsmType> getMapping() {
        return mapping;
    }

    /*
     * The only public method to create a new instantiation
     */
    public static CsmObject create(CsmTemplate template, CsmType type) {
        if (template instanceof CsmClass) {
            return new Class((CsmClass)template, type);
        } else if (template instanceof CsmFunction) {
            return new Function((CsmFunction)template, type);
        }
        assert false : "Unknown class for template instantiation:" + template; // NOI18N
        return template;
    }
    
    public static CsmObject create(CsmTemplate template, Map<CsmTemplateParameter, CsmType> mapping) {
        if (template instanceof CsmClass) {
            return new Class((CsmClass)template, mapping);
        } else if (template instanceof CsmFunction) {
            return new Function((CsmFunction)template, mapping);
        }
        assert false : "Unknown class for template instantiation:" + template; // NOI18N
        return template;
    }
    
    public CsmFile getContainingFile() {
        return getTemplateDeclaration().getContainingFile();
    }

    public int getEndOffset() {
        return getTemplateDeclaration().getEndOffset();
    }

    public Position getEndPosition() {
        return getTemplateDeclaration().getEndPosition();
    }

    public int getStartOffset() {
        return getTemplateDeclaration().getStartOffset();
    }

    public Position getStartPosition() {
        return getTemplateDeclaration().getStartPosition();
    }

    public CharSequence getText() {
        return getTemplateDeclaration().getText();
    }

    public Kind getKind() {
        return getTemplateDeclaration().getKind();
    }

    public CharSequence getUniqueName() {
        return getTemplateDeclaration().getUniqueName();
    }

    public CharSequence getQualifiedName() {
        return getTemplateDeclaration().getQualifiedName();
    }

    public CharSequence getName() {
        return getTemplateDeclaration().getName();
    }

    public CsmScope getScope() {
        return getTemplateDeclaration().getScope();
    }

    public CsmUID<T> getUID() {
        return new InstantiationUID(this);
    }
    
    //////////////////////////////
    ////////////// STATIC MEMBERS
    private static class Class extends Instantiation<CsmClass> implements CsmClass, CsmMember<CsmClass>, CsmTemplate {
        public Class(CsmClass clazz, CsmType type) {
            super(clazz, type);
            assert type.isInstantiation() : "Instantiation without parameters"; // NOI18N
        }
        
        public Class(CsmClass clazz, Map<CsmTemplateParameter, CsmType> mapping) {
            super(clazz, mapping);
        }
        
        public boolean isValid() {
            return ((CsmClass)declaration).isValid();
        }

        public Collection<CsmScopeElement> getScopeElements() {
            return ((CsmClass)declaration).getScopeElements();
        }

        public Collection<CsmTypedef> getEnclosingTypedefs() {
            return ((CsmClass)declaration).getEnclosingTypedefs();
        }

        public boolean isTemplate() {
            return ((CsmClass)declaration).isTemplate();
        }

        private CsmMember createMember(CsmMember member) {
            if (member instanceof CsmField) {
                return new Field((CsmField)member, this);
            } else if (member instanceof CsmMethod) {
                return new Method((CsmMethod)member, this);
            } else if (member instanceof CsmTypedef) {
                return new Typedef((CsmTypedef)member, this);
            } else if (member instanceof CsmClass) {
                return new Class((CsmClass)member, getMapping());
            } else if (member instanceof CsmClassForwardDeclaration) {
                return new ClassForward((CsmClassForwardDeclaration)member, this);
            }
            assert false : "Unknown class for member instantiation:" + member + " of class:" + member.getClass(); // NOI18N
            return member;
        }

        public Collection<CsmMember> getMembers() {
            Collection<CsmMember> res = new ArrayList<CsmMember>();
            for (CsmMember member : ((CsmClass)declaration).getMembers()) {
                res.add(createMember(member));
            }
            return res;
        }

        public int getLeftBracketOffset() {
            return ((CsmClass)declaration).getLeftBracketOffset();
        }

        public Collection<CsmFriend> getFriends() {
            return ((CsmClass)declaration).getFriends();
        }

        public Collection<CsmInheritance> getBaseClasses() {
            Collection<CsmInheritance> res = new ArrayList<CsmInheritance>();
            for (CsmInheritance inh : ((CsmClass)declaration).getBaseClasses()) {
                res.add(new Inheritance(inh, this));
            }
            return res;
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF CLASS: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }

        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }

        public CharSequence getDisplayName() {
            return ((CsmTemplate)declaration).getDisplayName();
        }

        public List<CsmTemplateParameter> getTemplateParameters() {
            return ((CsmTemplate)declaration).getTemplateParameters();
        }
    }

    private static class Inheritance implements CsmInheritance {
        private final CsmInheritance inheritance;
        private final CsmType type;

        public Inheritance(CsmInheritance inheritance, Instantiation instantiation) {
            this.inheritance = inheritance;
            this.type = createType(inheritance.getAncestorType(), instantiation);
        }

        public CsmType getAncestorType() {
            return type;
        }

        public CharSequence getText() {
            return inheritance.getText();
        }

        public Position getStartPosition() {
            return inheritance.getStartPosition();
        }

        public int getStartOffset() {
            return inheritance.getStartOffset();
        }

        public Position getEndPosition() {
            return inheritance.getEndPosition();
        }

        public int getEndOffset() {
            return inheritance.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return inheritance.getContainingFile();
        }

        public boolean isVirtual() {
            return inheritance.isVirtual();
        }

        public CsmVisibility getVisibility() {
            return inheritance.getVisibility();
        }

        public CsmClassifier getCsmClassifier() {
            return getAncestorType().getClassifier();
        }
    }
    
    private static class Function extends Instantiation implements CsmFunction {
        private final CsmType retType;
        
        public Function(CsmFunction function, CsmType instantiation) {
            super(function, instantiation);
            assert instantiation.isInstantiation() : "Instantiation without parameters"; // NOI18N
            this.retType = createType(function.getReturnType(), this);
        }
        
        public Function(CsmFunction function, Map<CsmTemplateParameter, CsmType> mapping) {
            super(function, mapping);
            this.retType = createType(function.getReturnType(), this);
        }

        public Collection<CsmScopeElement> getScopeElements() {
            return ((CsmFunction)declaration).getScopeElements();
        }

        public boolean isTemplate() {
            return ((CsmFunction)declaration).isTemplate();
        }

        public boolean isInline() {
            return ((CsmFunction)declaration).isInline();
        }

        public boolean isOperator() {
            return ((CsmFunction)declaration).isOperator();
        }
        
        public OperatorKind getOperatorKind() {
            return ((CsmFunction)declaration).getOperatorKind();
        }
        
        public CharSequence getSignature() {
            return ((CsmFunction)declaration).getSignature();
        }

        public CsmType getReturnType() {
            return retType;
        }

        public Collection getParameters() {
            Collection<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = ((CsmFunction)declaration).getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            return res;
        }

        public CsmFunctionDefinition getDefinition() {
            return ((CsmFunction)declaration).getDefinition();
        }

        public CsmFunction getDeclaration() {
            return ((CsmFunction)declaration).getDeclaration();
        }
        
        public CharSequence getDeclarationText() {
            return ((CsmFunction)declaration).getDeclarationText();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF FUNCTION: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }

    private static class Field extends Instantiation<CsmField> implements CsmField {
        private final CsmType type;

        public Field(CsmField field, CsmInstantiation instantiation) {
            super(field, instantiation.getMapping());
            this.type = createType(field.getType(), instantiation);
        }

        public boolean isExtern() {
            return ((CsmField)declaration).isExtern();
        }

        public CsmType getType() {
            return type;
        }

        public CsmExpression getInitialValue() {
            return ((CsmField)declaration).getInitialValue();
        }

        public CharSequence getDisplayText() {
            return ((CsmField)declaration).getDisplayText();
        }

        public CsmVariableDefinition getDefinition() {
            return ((CsmField)declaration).getDefinition();
        }

        public CharSequence getDeclarationText() {
            return ((CsmField)declaration).getDeclarationText();
        }

        public boolean isStatic() {
            return ((CsmField)declaration).isStatic();
        }

        public CsmVisibility getVisibility() {
            return ((CsmField)declaration).getVisibility();
        }

        public CsmClass getContainingClass() {
            return ((CsmField)declaration).getContainingClass();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF FIELD: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }
    
    private static class Typedef extends Instantiation<CsmTypedef> implements CsmTypedef, CsmMember<CsmTypedef> {
        private final CsmType type;

        public Typedef(CsmTypedef typedef, CsmInstantiation instantiation) {
            super(typedef, instantiation.getMapping());
            this.type = createType(typedef.getType(), instantiation);
        }

        public boolean isTypeUnnamed() {
            return ((CsmTypedef)declaration).isTypeUnnamed();
        }

        public CsmType getType() {
            return type;
        }

        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF TYPEDEF: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }
    
    private static class ClassForward extends Instantiation<CsmClassForwardDeclaration> implements CsmClassForwardDeclaration, CsmMember<CsmClassForwardDeclaration> {
        private CsmClassForwardDeclaration forward;

        public ClassForward(CsmClassForwardDeclaration forward, CsmInstantiation instantiation) {
            super(forward, instantiation.getMapping());
            this.forward = forward;
        }

        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF TYPEDEF: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }

        public CsmClass getCsmClass() {
            return forward.getCsmClass();
        }
    }

    private static class Method extends Instantiation implements CsmMethod, CsmFunctionDefinition {
        private final CsmInstantiation instantiation;
        private final CsmType retType;

        public Method(CsmMethod method, CsmInstantiation instantiation) {
            super(method, instantiation.getMapping());
            this.instantiation = instantiation;
            this.retType = createType(method.getReturnType(), instantiation);
        }

        public Collection<CsmScopeElement> getScopeElements() {
            return ((CsmMethod)declaration).getScopeElements();
        }

        public boolean isStatic() {
            return ((CsmMethod)declaration).isStatic();
        }

        public CsmVisibility getVisibility() {
            return ((CsmMethod)declaration).getVisibility();
        }

        public CsmClass getContainingClass() {
            return ((CsmMethod)declaration).getContainingClass();
        }

        public boolean isTemplate() {
            return ((CsmMethod)declaration).isTemplate();
        }

        public boolean isInline() {
            return ((CsmMethod)declaration).isInline();
        }

        public CharSequence getSignature() {
            return ((CsmMethod)declaration).getSignature();
        }

        public CsmType getReturnType() {
            return retType;
        }

        public Collection<CsmParameter> getParameters() {
            Collection<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = ((CsmMethod)declaration).getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, instantiation));
            }
            return res;
        }

        public CsmFunctionDefinition getDefinition() {
            return ((CsmMethod)declaration).getDefinition();
        }

        public CharSequence getDeclarationText() {
            return ((CsmMethod)declaration).getDeclarationText();
        }

        public boolean isVirtual() {
            return ((CsmMethod)declaration).isVirtual();
        }

        public boolean isExplicit() {
            return ((CsmMethod)declaration).isExplicit();
        }

        public boolean isConst() {
            return ((CsmMethod)declaration).isConst();
        }

        public boolean isAbstract() {
            return ((CsmMethod)declaration).isAbstract();
        }

        public boolean isOperator() {
            return ((CsmMethod)declaration).isOperator();
        }

        public OperatorKind getOperatorKind() {
            return ((CsmMethod)declaration).getOperatorKind();
        }
        
        public CsmCompoundStatement getBody() {
            return ((CsmFunctionDefinition)declaration).getBody();
        }

        public CsmFunction getDeclaration() {
            return ((CsmFunctionDefinition)declaration).getDeclaration();
        }
        
        @Override
        public String toString() {
            return "INSTANTIATION OF METHOD: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }
    
    private static class Parameter extends Instantiation<CsmParameter> implements CsmParameter {
        private final CsmType type;

        public Parameter(CsmParameter parameter, CsmInstantiation instantiation) {
            super(parameter, instantiation.getMapping());
            this.type = createType(parameter.getType(), instantiation);
        }

        public boolean isExtern() {
            return ((CsmParameter)declaration).isExtern();
        }

        public CsmType getType() {
            return type;
        }

        public CsmExpression getInitialValue() {
            return ((CsmParameter)declaration).getInitialValue();
        }

        public CharSequence getDisplayText() {
            return ((CsmParameter)declaration).getDisplayText();
        }

        public CsmVariableDefinition getDefinition() {
            return ((CsmParameter)declaration).getDefinition();
        }

        public CharSequence getDeclarationText() {
            return ((CsmParameter)declaration).getDeclarationText();
        }

        public boolean isVarArgs() {
            return ((CsmParameter)declaration).isVarArgs();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF FUN PARAM: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }
    
    private static Type createType(CsmType type, CsmInstantiation instantiation) {
         if (CsmKindUtilities.isTemplateParameterType(type)) {
                CsmTemplateParameterType paramType = (CsmTemplateParameterType)type;
                if (CsmKindUtilities.isTemplateParameterType(instantiation.getMapping().get(paramType.getParameter()))) {
                    return new TemplateParameterType(type, instantiation);
                }
         }
         return new Type(type, instantiation);
    }
    
    private static class TemplateParameterType extends Type implements CsmTemplateParameterType {
        public TemplateParameterType(CsmType type, CsmInstantiation instantiation) {
            super(type, instantiation);
        }
        
        public CsmTemplateParameter getParameter() {
            return ((CsmTemplateParameterType)instantiatedType).getParameter();
        }

        public CsmType getTemplateType() {
            return ((CsmTemplateParameterType)instantiatedType).getTemplateType();
        }
    }
    
    private static class Type implements CsmType, Resolver.SafeClassifierProvider {
        private final CsmType originalType;
        private final CsmInstantiation instantiation;
        protected final CsmType instantiatedType;
        private final boolean inst;
        
        private Type(CsmType type, CsmInstantiation instantiation) {
            this.instantiation = instantiation;
            inst = type.isInstantiation();
            CsmType origType = type;
            CsmType newType = type;
            
            if (CsmKindUtilities.isTemplateParameterType(type)) {
                CsmTemplateParameterType paramType = (CsmTemplateParameterType)type;
                newType = instantiation.getMapping().get(paramType.getParameter());
                if (newType != null) {
                    origType = paramType.getTemplateType();
                } else {
                    newType = type;
                }
            }
            this.originalType = origType;
            this.instantiatedType = newType;
        }
        
        private boolean instantiationHappened() {
            return originalType != instantiatedType;
        }

        public CharSequence getClassifierText() {
            return instantiatedType.getClassifierText().toString() + TypeImpl.getInstantiationText(this);
        }

        public CharSequence getText() {
            if (originalType instanceof TypeImpl) {
                return ((TypeImpl)originalType).decorateText(getClassifierText(), this, false, null);
            }
            return originalType.getText();
        }

        public Position getStartPosition() {
            return instantiatedType.getStartPosition();
        }

        public int getStartOffset() {
            return instantiatedType.getStartOffset();
        }

        public Position getEndPosition() {
            return instantiatedType.getEndPosition();
        }

        public int getEndOffset() {
            return instantiatedType.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return instantiatedType.getContainingFile();
        }

        public boolean isInstantiation() {
            return instantiatedType.isInstantiation();
        }

        public boolean isReference() {
            return originalType.isReference() || instantiatedType.isReference();
        }

        public boolean isPointer() {
            return originalType.isPointer() || instantiatedType.isPointer();
        }

        public boolean isConst() {
            return originalType.isConst() || instantiatedType.isConst();
        }

        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return instantiatedType.isBuiltInBased(resolveTypeChain);
        }

        public List<CsmType> getInstantiationParams() {
            if (!originalType.isInstantiation()) {
                return Collections.emptyList();
            }
            List<CsmType> res = new ArrayList<CsmType>();
            for (CsmType instParam : originalType.getInstantiationParams()) {
                if (CsmKindUtilities.isTemplateParameterType(instParam)) {
                    CsmTemplateParameterType paramType = (CsmTemplateParameterType)instParam;
                    CsmType newTp = instantiation.getMapping().get(paramType.getParameter());
                    if (newTp != null && newTp != instParam) {
                        res.add(newTp);
                    } else {
                        res.add(instParam);
                    }
                } else {
                    res.add(instParam);
                }
            }
            return res;
        }

        public int getPointerDepth() {
            if (instantiationHappened()) {
                return originalType.getPointerDepth() + instantiatedType.getPointerDepth();
            } else {
                return originalType.getPointerDepth();
            }
        }

        public CsmClassifier getClassifier(Resolver resolver) {
            CsmClassifier res;
            
            if (inst) {
                CsmClassifier classifier = originalType.getClassifier();
                if (CsmKindUtilities.isTemplate(classifier)) {
                    res = (CsmClassifier)Class.create(
                            (CsmTemplate)classifier, instantiation.getMapping());
                    return res;
                }
            }
            
            if (instantiatedType instanceof Resolver.SafeClassifierProvider) {
                res = ((Resolver.SafeClassifierProvider)instantiatedType).getClassifier(resolver);
            } else {
                res = instantiatedType.getClassifier();
            }
            
            if (CsmKindUtilities.isTypedef(res) && CsmKindUtilities.isClassMember(res)) {
                CsmMember tdMember = (CsmMember)res;
                if (CsmKindUtilities.isTemplate(tdMember.getContainingClass())) {
                    return new Typedef((CsmTypedef)res, instantiation);
                }
            }
            return res;
        }
        
        public CsmClassifier getClassifier() {
            return getClassifier(null);
        }

        public CharSequence getCanonicalText() {
            return originalType.getCanonicalText();
        }

        public int getArrayDepth() {
            if (instantiationHappened()) {
                return originalType.getArrayDepth() + instantiatedType.getArrayDepth();
            } else {
                return originalType.getArrayDepth();
            }
        }
        
        @Override
        public String toString() {
            String res = "INSTANTIATION OF TYPE: " + originalType + " with types (" + instantiation.getMapping() + ")";
            if (instantiationHappened()) {
                res += " becomes " + instantiatedType;
            }
            return res;
        }
    }
    
    public final static class InstantiationUID<T extends CsmIdentifiable> implements CsmUID<T>, SelfPersistent {
        private final T ref;
        private InstantiationUID(T ref) {
            this.ref = ref;
        }

        public T getObject() {
            return this.ref;
        }
        ////////////////////////////////////////////////////////////////////////////
        // impl for Persistent 

        public void write(DataOutput output) throws IOException {
            // write nothing
        }

        public InstantiationUID(DataInput input) throws IOException {
            this.ref = null;
        }
    }
}
