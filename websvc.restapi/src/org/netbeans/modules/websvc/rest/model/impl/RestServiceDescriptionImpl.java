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

package org.netbeans.modules.websvc.rest.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;

/**
 *
 * @author Peter Liu
 */
public class RestServiceDescriptionImpl extends PersistentObject implements RestServiceDescription {
    
    private String name;
    private String uriTemplate;
    private String className;
    private Map<String, RestMethodDescriptionImpl> methods;
    private boolean isRest;
  
    public RestServiceDescriptionImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        
        this.name = typeElement.getSimpleName().toString();
        this.uriTemplate = Utils.getUriTemplate(typeElement);
        this.className = typeElement.getQualifiedName().toString();
        this.isRest = true;
  
        initMethods(typeElement);
    }
    
    
    private void initMethods(TypeElement typeElement) {
        methods = new HashMap<String, RestMethodDescriptionImpl>();
        AnnotationModelHelper helper = getHelper();
        
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                addMethod(element);
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getUriTemplate() {
        return uriTemplate;
    }
    
    public List<RestMethodDescription> getMethods() {
        List<RestMethodDescription> list = new ArrayList<RestMethodDescription>();
        
        for (RestMethodDescriptionImpl method : methods.values()) {
            list.add((RestMethodDescription) method);
        }
        
        return list;
    }
 
    public String getClassName() {
        return className;
    }
 
    public boolean isRest() {
        return isRest;
    }
    
    public Status refresh(TypeElement typeElement) {
        if (typeElement.getKind() == ElementKind.INTERFACE) {
            return Status.REMOVED;
        }
        
        boolean isRest = false;
        boolean isModified = false;
        
        if (Utils.hasUriTemplate(typeElement)) {
            isRest = true;
        }
        
        String newValue = typeElement.getSimpleName().toString();
        
        
        // Refresh the resource name.
        if (this.name != newValue) {
            this.name = newValue;
            isModified = true;
        }
        
        // Refresh the class name.
        newValue = typeElement.getQualifiedName().toString();
        if (this.className != newValue) {
            this.className = newValue;
            isModified = true;
        }
        
        // Refresh the uriTemplate.
        newValue = Utils.getUriTemplate(typeElement);
        if (!this.uriTemplate.equals(newValue)) {
            this.uriTemplate = newValue;
            isModified = true;
        }
        
        Map<String, RestMethodDescriptionImpl> prevMethods = methods;
        methods = new HashMap<String, RestMethodDescriptionImpl>();
        
        // Refresh all the methods.
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                String methodName = element.getSimpleName().toString();
                
                RestMethodDescriptionImpl method = prevMethods.get(methodName);
                
                if (method != null) {
                    Status status = method.refresh(element);
                    
                    switch (status) {
                    case REMOVED:
                        if (addMethod(element)) {
                            isRest = true;
                        }
                        isModified = true;
                        break;
                    case MODIFIED:
                        isRest = true;
                        isModified = true;
                        methods.put(methodName, method);
                        break;
                    case UNMODIFIED:
                        isRest = true;
                        methods.put(methodName, method);
                        break;
                    }
                } else {
                    if (addMethod(element)) {
                        isRest = true;
                        isModified = true;
                    }
                }
            }
        }
        
        if (methods.size() != prevMethods.size()) {
            isModified = true;
        }
        
        if (!isRest) {
            this.isRest = false;
            return Status.REMOVED;
        }
        
        if (isModified) {
            return Status.MODIFIED;
        }
        
        return Status.UNMODIFIED;
    }
    
    
    private boolean addMethod(Element element) {
        RestMethodDescriptionImpl method = RestMethodDescriptionFactory.create(element);
        
        if (method != null) {
            methods.put(element.getSimpleName().toString(), method);
            
            return true;
        }
        return false;
    }
    
    public String toString() {
        return name + "[" + uriTemplate + "]"; //NOI18N
    }
}
