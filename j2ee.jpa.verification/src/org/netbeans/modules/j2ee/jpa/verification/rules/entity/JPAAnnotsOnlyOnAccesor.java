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
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * Only accesor methods can have JPA Annotations
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAAnnotsOnlyOnAccesor extends JPAClassRule {
    
    public JPAAnnotsOnlyOnAccesor() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        
        if (((JPAProblemContext)ctx).getAccessType() != AccessType.PROPERTY){
            return null;
        }
        
        List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
        
        for (ExecutableElement method : ElementFilter.methodsIn(subject.getEnclosedElements())){
            if (!isAccessor(method)){
                for (String annotName : ModelUtils.extractAnnotationNames(method)){
                    if (JPAAnnotations.MEMBER_LEVEL.contains(annotName)){
                        ErrorDescription error = createProblem(method, ctx,
                                    NbBundle.getMessage(JPAAnnotsOnlyOnAccesor.class,
                                    "MSG_JPAAnnotsOnlyOnAccesor",
                                    ModelUtils.shortAnnotationName(annotName)));
                                    
                        problemsFound.add(error);
                        break;
                    }
                }
            }
        }
        
        return problemsFound.toArray(new ErrorDescription[problemsFound.size()]);
    }
    
    private boolean isAccessor(ExecutableElement method){

        if (!method.getParameters().isEmpty()){
            return false;
        }

        String methodName = method.getSimpleName().toString();
        if (methodName.startsWith("get")){ //NO18N
            return true;
        }
        if (isBoolean(method.getReturnType().toString()) && methodName.startsWith("is")){ //NO18N
            return true;
        }
        return false;
    }
    
    private boolean isBoolean(String type){
        return "boolean".equals(type) || "java.lang.Boolean".equals(type); //NO18N
    }
}
