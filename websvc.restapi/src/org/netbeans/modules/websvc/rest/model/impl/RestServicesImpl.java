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

import org.netbeans.modules.websvc.rest.model.impl.RestServiceDescriptionImpl;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.type.ErrorType;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Peter Liu
 */
public class RestServicesImpl implements RestServices {
    
    public enum Status {
        UNMODIFIED, MODIFIED, REMOVED
    }
    
    private Project project;
    private AnnotationModelHelper helper;
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    private volatile PersistentObjectManager<RestServiceDescriptionImpl> restServiceManager;
    private boolean disableChangeSupport;
    
    public static RestServicesImpl create(AnnotationModelHelper helper, Project project) {
        RestServicesImpl instance =  new RestServicesImpl(helper, project);
        instance.initialize();
        
        return instance;
        
    }
    
    private RestServicesImpl(AnnotationModelHelper helper, Project project) {
        this.helper = helper;
        this.project = project;
    }
    
    /**
     * Initializing outside the constructor to avoid escaping "this" from
     * the constructor.
     */
    private void initialize() {
        restServiceManager = helper.createPersistentObjectManager(new RestServiceProvider());
        restServiceManager.addChangeListener(new ChangeListener() {
            public synchronized void stateChanged(ChangeEvent e) {
                if (disableChangeSupport) {
                    return;
                }
                propChangeSupport.firePropertyChange("/restservices", null, null); // NOI18N
            }
        });
    }
    
    public RestServiceDescription[] getRestServiceDescription() {
        Collection<RestServiceDescriptionImpl> restServices = restServiceManager.getObjects();
        return restServices.toArray(new RestServiceDescriptionImpl[restServices.size()]);
    }
    
    public RestServiceDescription getRestServiceDescription(String name) {
        for (RestServiceDescription desc : getRestServiceDescription()) {
            if (desc.getName().equals(name)) {
                return desc;
            }
        }
        
        return null;
    }
    
    public int sizeRestServiceDescription() {
        return restServiceManager.getObjects().size();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propChangeSupport.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propChangeSupport.removePropertyChangeListener(pcl);
    }
    
    public void disablePropertyChangeListener() {
        this.disableChangeSupport = true;
    }
    
    public void enablePropertyChangeListener() {
        this.disableChangeSupport = false;
        
        //propChangeSupport.firePropertyChange("/restservices", null, null); // NOI18N
    }
    
    private final class RestServiceProvider implements ObjectProvider<RestServiceDescriptionImpl> {
        
        public List<RestServiceDescriptionImpl> createInitialObjects() throws InterruptedException {
            //System.out.println("createInitialObjects()");
            final Map<TypeElement, RestServiceDescriptionImpl> result =
                    new HashMap<TypeElement, RestServiceDescriptionImpl>();
            
            findAnnotation(RestConstants.PATH, EnumSet.of(ElementKind.CLASS), result);
            findAnnotation(RestConstants.GET, EnumSet.of(ElementKind.METHOD), result);
            findAnnotation(RestConstants.POST, EnumSet.of(ElementKind.METHOD), result);
            findAnnotation(RestConstants.PUT, EnumSet.of(ElementKind.METHOD), result);
            findAnnotation(RestConstants.DELETE, EnumSet.of(ElementKind.METHOD), result);
            
            return new ArrayList<RestServiceDescriptionImpl>(result.values());
        }
        
        public List<RestServiceDescriptionImpl> createObjects(TypeElement type) {
            //System.out.println("createObjects() type = " + type);
            //(new Exception()).printStackTrace();
            if (Utils.checkForJsr311Bootstrap(type, project)) {
                return Collections.emptyList();
            }
            
            if (type.getKind() != ElementKind.INTERFACE) { // don't consider interfaces
                boolean isRest = false;
                if (helper.hasAnnotation(type.getAnnotationMirrors(), RestConstants.PATH)) { // NOI18N
                    isRest = true;
                } else {
                    for (Element element : type.getEnclosedElements()) {
                        if (Utils.hasHttpMethod(element)) {
                            isRest = true;
                            break;
                        }
                    }
                }
                
                if (isRest) {
                    //System.out.println("creating RestServiceDescImpl for " + type.getQualifiedName().toString());
                    return Collections.singletonList(new RestServiceDescriptionImpl(helper, type));
                }
            }
            return Collections.emptyList();
        }
        
        public boolean modifyObjects(TypeElement type, List<RestServiceDescriptionImpl> objects) {
            if (Utils.checkForJsr311Bootstrap(type, project)) {
                return false;
            }
            //System.out.println("modifyObject type = " + type);
            assert objects.size() == 1;
            RestServiceDescriptionImpl restService = objects.get(0);
            Status status = restService.refresh(type);
            
            switch (status) {
            case REMOVED:
                //System.out.println("removing RestServiceDescImpl for " + type.getQualifiedName().toString());
                objects.remove(0);
                return true;
            case MODIFIED:
                return true;
            case UNMODIFIED:
                return false;
            }
            
            return false;
        }
        
        private void findAnnotation(
                String annotationType, EnumSet<ElementKind> kinds, 
                final Map<TypeElement, RestServiceDescriptionImpl> result) throws InterruptedException {
            
            helper.getAnnotationScanner().findAnnotations(annotationType, kinds,
                    new AnnotationHandler() {
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    if (Utils.checkForJsr311Bootstrap(type, project)) {
                        return;
                    }
                    if (!result.containsKey(type)) {
                        result.put(type, new RestServiceDescriptionImpl(helper, type));
                    }
                }
            });
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void write(FileObject fo) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void merge(RootInterface root, int mode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDefaultDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDisplayName(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDefaultDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Map getAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, NameAlreadyUsedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSmallIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setLargeIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setIcon(Icon icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Icon getDefaultIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Map getAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removeAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    // </editor-fold>
    
}
