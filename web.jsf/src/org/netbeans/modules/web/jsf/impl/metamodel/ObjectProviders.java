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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;


/**
 * @author ads
 *
 */
class ObjectProviders {
    
    /**
     * This method return collection of listeners for Application
     * which annotated with @ListenersFor annotation  with 
     * appropriate child @ListenerFor annotation.
     * This algorithm is very like {@link SystemEventListenerProvider}
     * except main annotation here @ListenersFor.
     */
    static List<SystemEventListener> findApplicationSystemEventListeners( 
            final AnnotationModelHelper helper )
    {
        final List<SystemEventListener> result = 
            new LinkedList<SystemEventListener>();
        try {
            helper.getAnnotationScanner().findAnnotations(
                    "javax.faces.event.ListenersFor",               // NOI18N
                    EnumSet.of(ElementKind.CLASS), 
                    new AnnotationHandler() {
                        public void handleAnnotation(TypeElement typeElement, 
                                Element element, AnnotationMirror annotation) 
                        {
                            if ( !SystemEventListenerProvider.
                                    isApplicationSystemEventListener(typeElement))
                            {
                                return;
                            }
                            AnnotationParser parser = AnnotationParser.create(
                                    helper );
                            parser.expectAnnotationArray("value" ,  // NOI18N
                                    helper.resolveType(
                                            "javax.faces.event.ListenerFor"),// NOI18N
                                    new ListenerForHandler( helper , typeElement, 
                                            result ), 
                                    null);
                            parser.parse( annotation );
                        }
                    });
        }
        catch (InterruptedException e) {
            // do nothing
        }
        return result;
    }

    private final static class ListenerForHandler implements ArrayValueHandler {
        
        ListenerForHandler(AnnotationModelHelper helper, 
                TypeElement type, List<SystemEventListener> listeners )
        {
            myHelper = helper;
            myType = type;
            myListeners = listeners;
        }

        public Object handleArray(List<AnnotationValue> arrayMembers )
        {
            for (AnnotationValue arrayMember : arrayMembers) {
                Object arrayMemberValue = arrayMember.getValue();
                if (arrayMemberValue instanceof AnnotationMirror) {
                    AnnotationParser parser = AnnotationParser.create(getHelper());
                    parser.expectClass( "systemEventClass", null);                  // NOI18N
                    parser.expectClass("sourceClass", AnnotationParser.defaultValue(// NOI18N
                            Void.class.getCanonicalName()));
                    ParseResult parseResult = parser.parse(
                            ( AnnotationMirror) arrayMemberValue);
                    String eventClass = parseResult.get( "systemEventClass" ,            // NOI18N 
                            String.class );
                    String sourceClass = parseResult.get( "sourceClass" ,                // NOI18N 
                            String.class );
                    String clazz = myType.getQualifiedName().toString();
                    myListeners.add( new SystemEventListenerAnnotation( clazz, 
                            sourceClass, eventClass ));
                }
             }
            return null;
        }
        
        private AnnotationModelHelper getHelper(){
            return myHelper;
        }
        
        private AnnotationModelHelper myHelper; 
        private TypeElement myType;
        private List<SystemEventListener> myListeners;
    }

    static final class ComponentProvider extends AbstractProvider<ComponentImpl> 
        implements ObjectProvider<ComponentImpl> 
    {

        ComponentProvider( AnnotationModelHelper helper )
        {
            super(helper, "javax.faces.component.FacesComponent");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ComponentImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ComponentImpl( helper , typeElement );
        }

    }
    
    static final class BehaviorProvider extends AbstractProvider<BehaviorImpl> {

        BehaviorProvider( AnnotationModelHelper helper ) {
            super( helper , "javax.faces.component.behavior.FacesBehavior");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        BehaviorImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new BehaviorImpl( helper , typeElement );
        }
        
    }
    
    static final class ConverterProvider extends AbstractProvider<ConverterImpl>
        implements ObjectProvider<ConverterImpl>
    {

        ConverterProvider( AnnotationModelHelper helper )
        {
            super(helper, "javax.faces.convert.FacesConverter");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ConverterImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ConverterImpl( helper , typeElement );
        }

        
    }
    
    static final class ManagedBeanProvider extends AbstractProvider<ManagedBeanImpl>
        implements ObjectProvider<ManagedBeanImpl>
    {

        ManagedBeanProvider( AnnotationModelHelper helper)
        {
            super(helper, "javax.faces.bean.ManagedBean");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ManagedBeanImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ManagedBeanImpl( helper , typeElement );
        }
        
    }
    
    static final class ValidatorProvider extends AbstractProvider<ValidatorImpl> 
        implements ObjectProvider<ValidatorImpl>
    {

        ValidatorProvider( AnnotationModelHelper helper )
        {
            super(helper, "javax.faces.validator.FacesValidator");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ValidatorImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ValidatorImpl( helper , typeElement );
        }

    }
    
    static final class RendererProvider extends AbstractProvider<RendererImpl>
            implements ObjectProvider<RendererImpl>
    {

        RendererProvider( AnnotationModelHelper helper ) {
            super(helper, "javax.faces.render.FacesRenderer");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        RendererImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new RendererImpl(helper, typeElement);
        }

    }
    
    static final class ClientBehaviorProvider extends
            AbstractProvider<ClientBehaviorRendererImpl> implements
            ObjectProvider<ClientBehaviorRendererImpl>
    {

        ClientBehaviorProvider( AnnotationModelHelper helper ) {
            super(helper, "javax.faces.render.FacesBehaviorRenderer");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ClientBehaviorRendererImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ClientBehaviorRendererImpl(helper, typeElement);
        }

    }
    
    static final class SystemEventListenerProvider extends 
        AbstractProvider<SystemEventListenerImpl> implements
            ObjectProvider<SystemEventListenerImpl>
    {

        SystemEventListenerProvider( AnnotationModelHelper helper )
        {
            super(helper, "javax.faces.event.ListenerFor");         // NOI18N
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        SystemEventListenerImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new SystemEventListenerImpl(helper , typeElement );
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#checkType(javax.lang.model.element.TypeElement)
         */
        @Override
        protected boolean checkType( TypeElement type ) {
            return isApplicationSystemEventListener(type);
        }
        
        static boolean isApplicationSystemEventListener( TypeElement type ){
            /**
             * This method checks if class annotated with @ListenerFor annotation
             * should be attached to Application as SystemEventListener.
             * The following algorithm identify type as such listener.
             */
            List<TypeElement> interfaces = getImplementedInterfaces(type);
            boolean isSystemEventListener = false;
            boolean isComponentSystemEventListener = false;
            for (TypeElement typeElement : interfaces) {
                if ( typeElement.getQualifiedName().contentEquals( 
                        "javax.faces.event.SystemEventListener") )      // NOI18N
                {
                    isSystemEventListener = true;
                }
                else if ( typeElement.getQualifiedName().contentEquals( 
                        "javax.faces.event.ComponentSystemEventListener"))
                {
                    isComponentSystemEventListener = true;
                }
            }
            return isSystemEventListener && ! isComponentSystemEventListener;
        }
        
        static List<TypeElement> getImplementedInterfaces( TypeElement type ) {
            List<? extends TypeMirror> interfaces = type.getInterfaces();
            List<TypeElement> result = new ArrayList<TypeElement>(interfaces
                    .size());
            for (TypeMirror typeMirror : interfaces) {
                if (TypeKind.DECLARED.equals(typeMirror.getKind())) {
                    Element element = ((DeclaredType) typeMirror).asElement();
                    if (ElementKind.INTERFACE.equals(element.getKind())) {
                        result.add( (TypeElement) element );
                    }
                }
            }
            return result;
        }

    }
    
    private static abstract class AbstractProvider<T extends Refreshable> 
        implements ObjectProvider<T>
    {
        AbstractProvider(AnnotationModelHelper helper, String annotationName) {
            myAnnotationName = annotationName;
            myHelper = helper;
        }

        public List<T> createInitialObjects() throws InterruptedException {
            final List<T> result = new LinkedList<T>();
            getHelper().getAnnotationScanner().findAnnotations(getAnnotationName(), 
                    EnumSet.of( ElementKind.CLASS ), 
                    new AnnotationHandler() {
                        public void handleAnnotation(TypeElement type, 
                                Element element, AnnotationMirror annotation) 
                        {
                            if ( checkType ( type )){
                                result.add(createObject(getHelper(), type));
                            }
                        }
            });
            return result;
        }

        public List<T> createObjects(TypeElement type) {
            final List<T> result = new ArrayList<T>();
            if (getHelper().hasAnnotation(type.getAnnotationMirrors(), 
                    getAnnotationName())) 
            {
                if ( checkType ( type )){
                    result.add(createObject(getHelper(), type));
                }
            }
            return result;
        }

        public boolean modifyObjects(TypeElement type, List<T> objects) {
            boolean isModified = false;
            for( Iterator<T> iterator = objects.iterator() ; iterator.hasNext();  ){
                T object = iterator.next();
                if (!object.refresh(type)) {
                    iterator.remove();
                    isModified = true;
                }
            }
            return isModified;
        }

        abstract T createObject(AnnotationModelHelper helper, TypeElement typeElement);
        
        protected boolean checkType( TypeElement type ){
            return true;
        }
        
        private AnnotationModelHelper getHelper(){
            return myHelper;
        }
        
        private String getAnnotationName(){
            return myAnnotationName;
        }
        
        private String myAnnotationName;
        private AnnotationModelHelper myHelper;
    }
}
