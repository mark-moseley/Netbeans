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
package org.netbeans.modules.web.beans.impl.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;


/**
 * @author ads
 *
 */
class MemberBindingFilter extends TypeFilter {
    
    private static final String NON_BINDING_MEMBER_ANNOTATION =
                "javax.enterprise.inject.NonBinding";    // NOI18N
    
    static MemberBindingFilter get() {
        // could be changed to cached ThreadLocal variable 
        return new MemberBindingFilter();
    }

    void init( List<AnnotationMirror> bindingAnnotations,
            WebBeansModelImplementation impl )
    {
        myImpl = impl;
        myBindingAnnotations = bindingAnnotations;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.TypeFilter#filter(java.util.Set)
     */
    void filter( Set<TypeElement> set ) {
        super.filter(set);
        filterElements(set);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.TypeFilter#filterElements(java.util.Set)
     */
    void filterElements( Set<? extends Element> set ) {
        if ( set.size() == 0 ){
            return;
        }
        /*
         * Binding annotation could have members. See example :
         * @BindingType
         * @Retention(RUNTIME)
         * @Target({METHOD, FIELD, PARAMETER, TYPE})
         * public @interface PayBy {
         * PaymentMethod value();
         * @NonBinding String comment();
         * }    
         * One need to check presence of member in binding annotation at 
         * injected point and compare this member with member in annotation
         * for discovered type.
         * Members with  @NonBinding annotation should be ignored. 
         */
         for (AnnotationMirror annotation : getBindingAnnotations()) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> 
                elementValues = annotation.getElementValues();
            Set<ExecutableElement> bindingMembers = collectBindingMembers(
                    annotation );
            checkMembers(elementValues, bindingMembers, set );
        }
    }
    
    private void checkMembers(
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues,
            Set<ExecutableElement> members, Set<? extends Element> set )
    {
        MemberCheckerFilter filter = MemberCheckerFilter.get();
        filter.init( elementValues , members, getImplementation());
        filter.filterElements(set);
    }
    
    
    private Set<ExecutableElement> collectBindingMembers( AnnotationMirror annotation ) 
    {
        DeclaredType annotationType  = annotation.getAnnotationType();
        TypeElement annotationElement = (TypeElement)annotationType.asElement();
        List<? extends Element> members = annotationElement.getEnclosedElements();
        Set<ExecutableElement> bindingMembers = new HashSet<ExecutableElement>();
        for (Element member : members) {
            if ( member instanceof ExecutableElement ){
                ExecutableElement exec = (ExecutableElement)member;
                if ( isBindingMember( exec )){
                    bindingMembers.add( exec );
                }
            }
        }
        return bindingMembers;
    }
    
    private boolean isBindingMember( ExecutableElement element )
    {
        List<? extends AnnotationMirror> annotationMirrors = 
            getImplementation().getHelper().getCompilationController().getElements().
                    getAllAnnotationMirrors( element);
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            Name name = ((TypeElement)annotationMirror.getAnnotationType().asElement()).
                getQualifiedName();
            if ( NON_BINDING_MEMBER_ANNOTATION.contentEquals(name)){
                return false;
            }
        }
        return true;
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private List<AnnotationMirror> getBindingAnnotations(){
        return myBindingAnnotations;
    }

    private WebBeansModelImplementation myImpl;
    private List<AnnotationMirror> myBindingAnnotations;

}
