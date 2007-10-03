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


package org.netbeans.modules.j2ee.jpa.model;

import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;

/**
 * Utility methods for discovering various facts
 * about JPA model
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAHelper {
    
    /**
     * Utility method to find out if any member is annotated as Id or
     * EmbeddedId in this class? It does not check any of the inheritted
     * members.
     *
     * @param javaClass JavaClass whose members will be inspected.
     * @return returns true if atleast one member is annotated as Id or EmbeddedId
     */
    public static boolean isAnyMemberAnnotatedAsIdOrEmbeddedId(Object modelObject) {
        Attributes attrs = null;
        
        if (modelObject instanceof Entity){
            attrs = ((Entity)modelObject).getAttributes();
        } else if (modelObject instanceof MappedSuperclass){
            attrs = ((MappedSuperclass)modelObject).getAttributes();
        }
        
        if (attrs != null){
            if (attrs.getEmbeddedId() != null){
                return true;
            }
            
            if (attrs.getId().length > 0){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * @return name of the primary table that will be mapped to given entity class
     */
    public static String getPrimaryTableName(Entity entity){
        return entity.getTable().getName();
    }
    
    public static AnnotationMirror getFirstAnnotationFromGivenSet(Element element,
            Collection<String> searchedAnnotations){
        
        for (AnnotationMirror ann : element.getAnnotationMirrors()){
            String annType = ann.getAnnotationType().toString();
            
            if (searchedAnnotations.contains(annType)){
                return ann;
            }
        }
        
        return null;
    }
    
    /**
     *
     */
    public static AccessType findAccessType(TypeElement entityClass, Object modelElement){
        AccessType accessType = AccessType.INDETERMINED;
        
        if (modelElement instanceof Entity){
            String accessDef = ((Entity)modelElement).getAccess();
            
            if (Entity.FIELD_ACCESS.equals(accessDef)){
                accessType = AccessType.FIELD;
            }
            else if (Entity.PROPERTY_ACCESS.equals(accessDef)){
                accessType = AccessType.PROPERTY;
            }
        }
        
        // look for the first element annotated with a JPA field annotation
        for (Element element : entityClass.getEnclosedElements()){
            if (element.getKind() == ElementKind.FIELD || element.getKind() == ElementKind.METHOD){
                AnnotationMirror ann = getFirstAnnotationFromGivenSet(element, JPAAnnotations.MEMBER_LEVEL);
                
                if (ann != null){
                    accessType = element.getKind() == ElementKind.FIELD ?
                        AccessType.FIELD : AccessType.PROPERTY;
                    
                    break;
                }
            }
        }
        
        if (accessType.isDetermined()){
            // check if access type is consistent
            Collection<? extends Element> otherElems = null;
            
            if (accessType == AccessType.FIELD){
                otherElems = ElementFilter.methodsIn(entityClass.getEnclosedElements());
            } else{
                otherElems = ElementFilter.fieldsIn(entityClass.getEnclosedElements());
            }
            
            for (Element element : otherElems){
                AnnotationMirror ann = getFirstAnnotationFromGivenSet(element, JPAAnnotations.MEMBER_LEVEL);
                
                if (ann != null){
                    accessType = AccessType.INCONSISTENT;
                    break;
                }
            }
        }
        
        return accessType;
    }
}
