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

import java.util.List;

import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;


/**
 * @author ads
 *
 */
class ObjectProviders {

    static final class ComponentProvider implements ObjectProvider<ComponentImpl> {

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
         */
        public List<ComponentImpl> createInitialObjects()
                throws InterruptedException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
         */
        public List<ComponentImpl> createObjects( TypeElement type ) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
         */
        public boolean modifyObjects( TypeElement type,
                List<ComponentImpl> objects )
        {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    
    static final class BehaviorProvider implements ObjectProvider<BehaviorImpl> {

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
         */
        public List<BehaviorImpl> createInitialObjects()
                throws InterruptedException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
         */
        public List<BehaviorImpl> createObjects( TypeElement type ) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
         */
        public boolean modifyObjects( TypeElement type,
                List<BehaviorImpl> objects )
        {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    
    static final class ConverterProvider implements ObjectProvider<ConverterImpl>{

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
         */
        public List<ConverterImpl> createInitialObjects()
                throws InterruptedException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
         */
        public List<ConverterImpl> createObjects( TypeElement type ) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
         */
        public boolean modifyObjects( TypeElement type,
                List<ConverterImpl> objects )
        {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    
    static final class ManagedBeanProvider implements ObjectProvider<ManagedBeanImpl>{

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
         */
        public List<ManagedBeanImpl> createInitialObjects()
                throws InterruptedException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
         */
        public List<ManagedBeanImpl> createObjects( TypeElement type ) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
         */
        public boolean modifyObjects( TypeElement type,
                List<ManagedBeanImpl> objects )
        {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    
    static final class ValidatorProvider implements ObjectProvider<ValidatorImpl>{

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
         */
        public List<ValidatorImpl> createInitialObjects()
                throws InterruptedException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
         */
        public List<ValidatorImpl> createObjects( TypeElement type ) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
         */
        public boolean modifyObjects( TypeElement type,
                List<ValidatorImpl> objects )
        {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
}
