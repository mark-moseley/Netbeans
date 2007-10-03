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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.PortComponentRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChains;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;

/**
 *
 * @author Milan Kuchtiak
 */
public class ServiceRefImpl implements ServiceRef {

    private final TypeElement typeElement;
    private final TypeElement parentElement;
    private final Element element;
    private final AnnotationModelHelper helper;
    private String serviceRefName, wsdlFile;
    private String value, type;
    private PortComponentRef[] portComponentRefs;

    public ServiceRefImpl(Element element, TypeElement typeElement, TypeElement parentElement, AnnotationModelHelper helper) {
        this.typeElement = typeElement;
        this.helper = helper;
        this.element = element;
        this.parentElement=parentElement;
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(element.getAnnotationMirrors());
        AnnotationParser parser = AnnotationParser.create(helper);     
        parser.expectString("name",null);
        parser.expectString("wsdlLocation",null);
        parser.expectClass("value",null);
        parser.expectClass("type",null);
        ParseResult parseResult = parser.parse(annByType.get("javax.xml.ws.WebServiceRef")); //NOI18N
        serviceRefName = parseResult.get("name", String.class); // NOI18N
        wsdlFile = parseResult.get("wsdlLocation", String.class); // NOI18N
        value = parseResult.get("value", String.class); // NOI18N
        type = parseResult.get("type", String.class); // NOI18N
    }
    
    /**
     * Create new ServiceRefImpl instance based on given {@link javax.annotation.Resource @Resource} annotation.
     * @param resource {@link ResourceImpl @Resource} implementation.
     * @see ResourceImpl
     */
    public ServiceRefImpl(ResourceImpl resource) {
        serviceRefName = resource.getName();
        // not type, but value
        value = resource.getType();
        
        typeElement = null;
        element = null;
        helper = null;
        parentElement = null;
    }
    
    public URI getWsdlFile() {
        URI wsdlURI = null;
        if (wsdlFile != null) {
            try {
                wsdlURI = new URI(wsdlFile);
            } catch(URISyntaxException ex) {                
            }
        }
        return wsdlURI;
    }    

    public String getServiceRefName() {
        if (serviceRefName!=null) {
            return serviceRefName;
        } else if (parentElement!=null && element != null) {
            return parentElement.getQualifiedName().toString()+"/"+element.getSimpleName().toString(); //NOI18N
        } else if (value!=null) {
            return "service/"+getClassNameFromClass(value); //NOI18N
        } else if (type!=null) {
            return "service/"+getClassNameFromClass(type); //NOI18N
        } else {
            return "service/"+typeElement.getSimpleName().toString(); //NOI18N
        }
    }
 
    public String getServiceInterface() {
        if (value != null) {
            return value;
        } else if (type != null) {
            return type;
        } else {
            return typeElement.getQualifiedName().toString();
        }
    }

    public PortComponentRef[] getPortComponentRef() {
        initPortComponentRefs();
        return portComponentRefs;
    }

    public int sizePortComponentRef() {
        initPortComponentRefs();
        return portComponentRefs.length;
    }
    
    public PortComponentRef getPortComponentRef(int index) {
        initPortComponentRefs();
        return portComponentRefs[index];
    }
    
    private void initPortComponentRefs() {
        if (portComponentRefs == null) {
            List<PortComponentRef> portComponents = new ArrayList<PortComponentRef>();
            String sei = null;
            if (value!=null) {
                if (type!=null) {
                    sei = type;
                } else {
                    sei = typeElement.getQualifiedName().toString();
                }
            }
            if (sei != null) portComponents.add(new PortComponentRefImpl(sei));
            portComponentRefs = portComponents.toArray(new PortComponentRef[portComponents.size()]);
        }
    }

    public String getJaxrpcMappingFile() {
        return null;
    }
    
    public int addPortComponentRef(PortComponentRef portComponentRef) {
        if (!(portComponentRef instanceof PortComponentRefImpl)) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            if (portComponentRefs != null) {
                PortComponentRef[] newRefs = new PortComponentRef[portComponentRefs.length+1];
                for (int i=0;i<portComponentRefs.length;i++) {
                    newRefs[i] = portComponentRefs[i];
                }
                newRefs[portComponentRefs.length] = portComponentRef;
                portComponentRefs = newRefs;
            } else {
                portComponentRefs = new PortComponentRef[] {portComponentRef};
            }
            return portComponentRefs.length-1;
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRefName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceInterface(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWsdlFile(URI value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setJaxrpcMappingFile(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceQname(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getServiceQname() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPortComponentRef(int index, PortComponentRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPortComponentRef(PortComponentRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePortComponentRef(PortComponentRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHandler(int index, ServiceRefHandler valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRefHandler getHandler(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHandler(ServiceRefHandler[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRefHandler[] getHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addHandler(ServiceRefHandler valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeHandler(ServiceRefHandler valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMappedName(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMappedName() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHandlerChains(ServiceRefHandlerChains valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRefHandlerChains getHandlerChains() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PortComponentRef newPortComponentRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRefHandler newServiceRefHandler() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRefHandlerChains newServiceRefHandlerChains() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
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

    private String getClassNameFromClass(String className) {
        int indexDot = className.lastIndexOf('.');
        return className.substring(indexDot+1);
    }
}
