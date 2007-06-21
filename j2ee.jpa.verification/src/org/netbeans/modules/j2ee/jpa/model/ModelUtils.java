/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.j2ee.jpa.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@SUN.COM
 */
public class ModelUtils {
    
    public static Entity getEntity(EntityMappingsMetadata metadata, TypeElement clazz){
        for (Entity entity: metadata.getRoot().getEntity()){
            if (clazz.getQualifiedName().contentEquals(entity.getClass2())){
                return entity;
            }
        }
        return null;
    }
    
    public static Entity getEntity(EntityMappingsMetadata metadata, String qualifiedClassName){
        for (Entity entity: metadata.getRoot().getEntity()){
            if (qualifiedClassName.equals(entity.getClass2())){
                return entity;
            }
        }
        return null;
    }
    
    public static Embeddable getEmbeddable(EntityMappingsMetadata metadata, TypeElement clazz){
        for (Embeddable embeddable: metadata.getRoot().getEmbeddable()){
            if (clazz.getQualifiedName().contentEquals(embeddable.getClass2())){
                return embeddable;
            }
        }
        return null;
    }
    
    public static MappedSuperclass getMappedSuperclass(EntityMappingsMetadata metadata, TypeElement clazz){
        for (MappedSuperclass mappedSuperclass: metadata.getRoot().getMappedSuperclass()){
            if (clazz.getQualifiedName().contentEquals(mappedSuperclass.getClass2())){
                return mappedSuperclass;
            }
        }
        return null;
    }
    
    public static TypeElement getTypeElementFromModel(CompilationInfo info, Object modelElement){
        String className = null;
        
        if (modelElement instanceof Entity){
            className = ((Entity)modelElement).getClass2();
        }
        
        if (className != null){
            return info.getElements().getTypeElement(className);
        }
        
        return null;
    }
    
    public static void resolveJavaElementFromModel(JPAProblemContext problemCtx, AttributeWrapper attr){
        String attrName = attr.getName();
        
        attr.setInstanceVariable(getField(problemCtx.getJavaClass(), attrName));
        attr.setAccesor(getAccesor(problemCtx.getJavaClass(), attrName));
        
        if (attr.getInstanceVariable() != null){
            attr.setMutator(getMutator(
                    problemCtx.getCompilationInfo(),
                    problemCtx.getJavaClass(),
                    attr.getInstanceVariable()));
        }
        
        if (problemCtx.getAccessType() == AccessType.FIELD){
            attr.setJavaElement(attr.getInstanceVariable());
        }
        
        else if (problemCtx.getAccessType() == AccessType.PROPERTY){
            attr.setJavaElement(attr.getAccesor());
        }
    }
    
    public static ExecutableElement getAccesor(TypeElement clazz, String fieldName){
        for (ExecutableElement method : getMethod(clazz, getAccesorName(fieldName))){
            if (method.getParameters().size() == 0){
                return method;
            }
        }
        
        return null;
    }
    
    public static ExecutableElement getMutator(CompilationInfo info, TypeElement clazz, VariableElement field){
        ExecutableElement matchingMethods[] = ModelUtils.getMethod(
                clazz, ModelUtils.getMutatorName(field.getSimpleName().toString()));
        
        for (ExecutableElement potentialMutator : matchingMethods){
            if (potentialMutator.getParameters().size() == 1){
                TypeMirror argType = potentialMutator.getParameters().get(0).asType();
                
                if (info.getTypes().isSameType(argType,
                        field.asType())) {
                    return potentialMutator;
                }
            }
        }
        
        return null;
    }
    
    public static ExecutableElement[] getMethod(TypeElement clazz, String methodName){
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
        
        for (ExecutableElement method : ElementFilter.methodsIn(clazz.getEnclosedElements())){
            if (method.getSimpleName().contentEquals(methodName)){
                methods.add(method);
            }
        }
        
        return methods.toArray(new ExecutableElement[methods.size()]);
    }
    
    public static String getAccesorName(String fieldName){
        return "get" //NOI18N
                + Character.toString(fieldName.charAt(0)).toUpperCase() +
                fieldName.substring(1);
    }
    
    public static String getMutatorName(String fieldName){
        return "set" //NOI18N
                + Character.toString(fieldName.charAt(0)).toUpperCase() +
                fieldName.substring(1);
    }
    
    public static String getFieldNameFromAccessor(String accessorName){
        if (!accessorName.startsWith("get")){ //NOI18N
            throw new IllegalArgumentException("accessor name must start with 'get'");
        }
        
        return String.valueOf(accessorName.charAt(3)).toLowerCase() + accessorName.substring(4);
    }
    
    public static VariableElement getField(TypeElement clazz, String fieldName){
        for (VariableElement field : ElementFilter.fieldsIn(clazz.getEnclosedElements())){
            if (field.getSimpleName().contentEquals(fieldName)){
                return field;
            }
        }
        
        return null;
    }
    
    public static MetadataModel<EntityMappingsMetadata> getModel(FileObject sourceFile){
        EntityClassScope scope = EntityClassScope.getEntityClassScope(sourceFile);
        
        if (scope != null) {
            return scope.getEntityMappingsModel(false); // false since I guess you only want the entity classes defined in the project
        }
        return null;
    }
    
    public static Collection<String> extractAnnotationNames(Element elem) {
        Collection<String> annotationsOnElement = new LinkedList<String>();
        
        for (AnnotationMirror ann : elem.getAnnotationMirrors()){
            TypeMirror annType = ann. getAnnotationType();
            Element typeElem = ((DeclaredType)annType).asElement();
            String typeName = ((TypeElement)typeElem).getQualifiedName().toString();
            annotationsOnElement.add(typeName);
        }
        
        return annotationsOnElement;
    }
    
    public static String shortAnnotationName(String annClass){
        return "@" + annClass.substring(annClass.lastIndexOf(".") + 1); //NOI18N
    }
}
