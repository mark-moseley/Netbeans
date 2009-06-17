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

package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;

import org.netbeans.modules.web.jsf.api.metamodel.Component;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "faces-config" element is the root of the configuration
 * information hierarchy, and contains nested elements for all
 * of the other configuration settings.
 * @author Petr Pisl
 */

public interface FacesConfig extends JSFConfigComponent, IdentifiableElement {
    
    /**
     * Property for &lt;managed-bean&gt; element
     */
    String MANAGED_BEAN = JSFConfigQNames.MANAGED_BEAN.getLocalName();
    /**
     * Property of &lt;navigation-rule&gt; element
     */
    String NAVIGATION_RULE = JSFConfigQNames.NAVIGATION_RULE.getLocalName();
    /**
     * Property of &lt;converter&gt; element
     */
    String CONVERTER = JSFConfigQNames.CONVERTER.getLocalName();
    
    /**
     * Property of &lt;application&gt; element
     */
    String APPLICATION = JSFConfigQNames.APPLICATION.getLocalName();
    
    /**
     * Property of &lt;ordering&gt; element
     */
    String ORDERING = JSFConfigQNames.ORDERING.getLocalName();
    
    /**
     * Property of &lt;absolute-ordering&gt; element
     */
    String ABSOLUTE_ORDERING =JSFConfigQNames.ABSOLUTE_ORDERING.getLocalName();
    
    /**
     * Property of &lt;factory&gt; element
     */
    String FACTORY =JSFConfigQNames.FACTORY.getLocalName();
    
    /**
     * Property of &lt;component&gt; element
     */
    String COMPONENT =JSFConfigQNames.FACTORY.getLocalName();
    
    /**
     * Property of &lt;name&gt; element.
     */
    String NAME = JSFConfigQNames.NAME.getLocalName();
    
    /**
     * Property of &lt;referenced-bean&gt; element.
     */
    String REFERENCED_BEAN = JSFConfigQNames.REFERENCED_BEAN.getLocalName();
    
    /**
     * Property of &lt;referenced-bean&gt; element.
     */
    String RENDER_KIT = JSFConfigQNames.RENDER_KIT.getLocalName();
    
    /**
     * Property of &lt;lifecycle&gt; element.
     */
    String LIFECYCLE= JSFConfigQNames.LIFECYCLE.getLocalName();
    
    /**
     * Property of &lt;validator&gt; element.
     */
    String VALIDATOR= JSFConfigQNames.VALIDATOR.getLocalName();
    
    /**
     * Property of &lt;faces-config-extension&gt; element.
     */
    String FACES_CONFIG_EXTENSION= JSFConfigQNames.FACES_CONFIG_EXTENSION.getLocalName();
    
    /**
     * Property of &lt;behavior&gt; element.
     */
    String BEHAVIOR= JSFConfigQNames.BEHAVIOR.getLocalName();
    
    
    /**
     * Attribute &lt;metadata-complete&gt; element.
     */
    String METADATA_COMPLETE = "metadata-complete";     // NOI18N
    
    /**
     * Attribute &lt;version&gt; element.
     */
    String VERSION = "version";                         // NOI18N
    
    List<Ordering> getOrderings();
    void addOrdering(Ordering ordering);
    void removeOrdering(Ordering ordering);
    
    List<AbsoluteOrdering> getAbsoluteOrderings();
    void addAbsoluteOrdering(AbsoluteOrdering ordering);
    void removeAbsoluteOrdering(AbsoluteOrdering ordering);
    
    List<Factory> getFactories();
    void addFactories( Factory factory );
    void removeFactory( Factory factory );
    
    List<Component> getComponents();
    void addComponent( FacesComponent component );
    void removeComponent( FacesComponent component );
    
    List<Name> getNames();
    void addName( Name name );
    void removeName(Name name );
    
    List<ReferencedBean> getReferencedBeans();
    void addReferencedBean( ReferencedBean bean );
    void removeReferencedBean( ReferencedBean bean);
    
    List<RenderKit> getRenderKits();
    void addRenderKit( RenderKit kit );
    void removeRenderKit( RenderKit kit );
    
    List<Lifecycle> getLifecycles();
    void addLifecycle( Lifecycle lifecycle );
    void removeLifecycle( Lifecycle lifecycle );
    
    List<FacesValidator> getValidators();
    void addValidator( FacesValidator validator );
    void removeValidator( FacesValidator validator );
    
    List<FacesConfigExtension> getFacesConfigExtensions();
    void addFacesConfigExtension( FacesConfigExtension extension );
    void removeFacesConfigExtension( FacesConfigExtension extension );
    
    List<Converter> getConverters();
    void addConverter(Converter converter);
    void removeConverter(Converter converter);
    
    List <ManagedBean> getManagedBeans();
    void addManagedBean(ManagedBean bean);
    void removeManagedBean(ManagedBean bean);
    
    List<NavigationRule> getNavigationRules();
    void addNavigationRule(NavigationRule rule);
    void removeNavigationRule(NavigationRule rule);
    
    List<Application> getApplications();
    void addApplication(Application application);
    void removeApplication(Application application);
    
    List<FacesBehavior> getBehaviors();
    void addBehavior( FacesBehavior behavior );
    void removeBehavior( FacesBehavior behavior );
    
    void addFacesConfigElement( int index, FacesConfigElement element );
    List<FacesConfigElement> getFacesConfigElements();
    
    Boolean isMetaDataComplete();
    void setMetaDataComplete( Boolean isMetadataComplete);
    
    String getVersion();
    void setVersion(String version);
}
