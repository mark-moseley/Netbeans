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
package org.netbeans.modules.j2ee.jpa.verification.rules.attribute;

import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateManyToManyRelationshipHint;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateOneToManyRelationshipHint;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateUnidirOneToManyRelationshipHint;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 * if there is attr of type Collection<EntityType>
 * a multi-valued relationship should be defined for it
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class MVRelationshipForEntityTypeAttrDefined extends JPAEntityAttributeCheck  {
    
    public ErrorDescription[] check(JPAProblemContext ctx, AttributeWrapper attrib) {
        
        // Not applicable for embeddable classes, which do not have relationships.
        if (ctx.isEmbeddable()) {
            return null;
        }
        
        TypeMirror type = attrib.getType();
        
        if (type.getKind() == TypeKind.DECLARED){
            List<? extends TypeMirror> typeArgs = ((DeclaredType)type).getTypeArguments();
            
            if (typeArgs.size() == 1){
                Element typeElement = ctx.getCompilationInfo().getTypes().asElement(typeArgs.get(0));
                
                if (typeElement != null && typeElement.getKind() == ElementKind.CLASS){
                    Entity entity = ModelUtils.getEntity(ctx.getMetaData(), ((TypeElement)typeElement));
                    String remoteClassName = ((TypeElement)typeElement).getQualifiedName().toString();
                    
                    if (entity != null){
                        ElementHandle<TypeElement> classHandle = ElementHandle.create(ctx.getJavaClass());
                        ElementHandle<Element> elemHandle = ElementHandle.create(attrib.getJavaElement());
                        
                        Fix fix1 = new CreateUnidirOneToManyRelationshipHint(ctx.getFileObject(),
                                classHandle, elemHandle);
                        
                        Fix fix2 = new CreateOneToManyRelationshipHint(ctx.getFileObject(),
                                classHandle, ctx.getAccessType(), attrib.getName(), remoteClassName);
                        
                        Fix fix3 = new CreateManyToManyRelationshipHint(ctx.getFileObject(),
                                classHandle,
                                ctx.getAccessType(),
                                attrib.getName(),
                                remoteClassName);
                        
                        return new ErrorDescription[]{Rule.createProblem(attrib.getJavaElement(),
                                ctx, NbBundle.getMessage(MVRelationshipForEntityTypeAttrDefined.class,
                                "MSG_MVEntityRelationNotDefined"),
                                Severity.WARNING,
                                Arrays.asList(fix1, fix2, fix3))};
                    }
                }
            }
        }
        
        return null;
    }
}
