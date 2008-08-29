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
package org.netbeans.modules.j2ee.dd.impl.webservices.annotation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.openide.filesystems.FileObject;
import org.xml.sax.SAXParseException;

/**
 *
 * @author mkuchtiak
 */
public class WebservicesImpl implements Webservices {

    private final AnnotationModelHelper helper;
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    private volatile PersistentObjectManager<WebserviceDescriptionImpl> webserviceManager;
    
    public static WebservicesImpl create(AnnotationModelHelper helper) {
        WebservicesImpl instance = new WebservicesImpl(helper);
        instance.initialize();
        return instance;
    }
    
    private WebservicesImpl(AnnotationModelHelper helper) {
        this.helper = helper;
    }
    
    /**
     * Initializing outside the constructor to avoid escaping "this" from
     * the constructor.
     */
    private void initialize() {
        webserviceManager = helper.createPersistentObjectManager(new WebserviceProvider());
        webserviceManager.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                propChangeSupport.firePropertyChange("/webservices", null, null); // NOI18N
            }
        });
    }
    
    public BigDecimal getVersion() {
        return BigDecimal.valueOf(1.2);
    }
    
    public WebserviceDescription[] getWebserviceDescription() {
        Collection<WebserviceDescriptionImpl> webservices = webserviceManager.getObjects();
        return webservices.toArray(new WebserviceDescriptionImpl[webservices.size()]);
    }
    
    public WebserviceDescription getWebserviceDescription(int index) {
        return getWebserviceDescription()[index];
    }

    public int sizeWebserviceDescription() {
        return webserviceManager.getObjects().size();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propChangeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propChangeSupport.removePropertyChangeListener(pcl);
    }

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SAXParseException getError() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWebserviceDescription(int index, WebserviceDescription value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWebserviceDescription(WebserviceDescription[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addWebserviceDescription(WebserviceDescription value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeWebserviceDescription(WebserviceDescription value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public WebserviceDescription newWebserviceDescription() {
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
    
    private final class WebserviceProvider implements ObjectProvider<WebserviceDescriptionImpl> {
        
        public List<WebserviceDescriptionImpl> createInitialObjects() throws InterruptedException {
            final List<WebserviceDescriptionImpl> result = new ArrayList<WebserviceDescriptionImpl>();
            helper.getAnnotationScanner().findAnnotations("javax.jws.WebService", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    // XXX should be called with another type kinds
                    // don't consider interfaces (SEI classes)
                    if (type.getKind() != ElementKind.INTERFACE)
                        result.add(new WebserviceDescriptionImpl(helper, type));
                }
            });
            helper.getAnnotationScanner().findAnnotations("javax.xml.ws.WebServiceProvider", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new WebserviceDescriptionImpl(helper, type));
                }
            });
            return result;
        }
        
        public List<WebserviceDescriptionImpl> createObjects(TypeElement type) {            
            if (type.getKind() != ElementKind.INTERFACE) { // don't consider interfaces
                if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.jws.WebService")) { // NOI18N
                    return Collections.singletonList(new WebserviceDescriptionImpl(helper, type));
                }
                if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.xml.ws.WebServiceProvider")) { // NOI18N
                    return Collections.singletonList(new WebserviceDescriptionImpl(helper, type));
                }
            }
            return Collections.emptyList();
        }
        
        public boolean modifyObjects(TypeElement type, List<WebserviceDescriptionImpl> objects) {
            assert objects.size() == 1;
            WebserviceDescriptionImpl webservice = objects.get(0);
            if (!webservice.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }
    }
}
