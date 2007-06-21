/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.impl.client.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.EjbRefHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.openide.filesystems.FileObject;
import org.xml.sax.SAXParseException;

/**
 * Default implementation of {@link AppClient}.
 * @author Tomas Mysik
 */
public class AppClientImpl implements AppClient {
    
    private final AnnotationModelHelper helper;
    private ResourceRef[] resourceRefs = null;
    private ResourceEnvRef[] resourceEnvRefs = null;
    private EnvEntry[] envEntries = null;
    private MessageDestinationRef[] messageDestinationRefs = null;
    private ServiceRef[] serviceRefs = null;
    private EjbRef[] ejbRefs;

    
    /**
     * Constructor with all properties.
     * @param helper annotation model helper.
     */
    public AppClientImpl(AnnotationModelHelper helper) {
        this.helper = helper;
    }
    
    private void initResourceRefs() {
        if (resourceRefs != null) {
            return;
        }
        resourceRefs = CommonAnnotationHelper.getResourceRefs(helper);
    }
    
    private void initResourceEnvRefs() {
        if (resourceEnvRefs != null) {
            return;
        }
        resourceEnvRefs = CommonAnnotationHelper.getResourceEnvRefs(helper);
    }
    
    private void initEnvEntries() {
        if (envEntries != null) {
            return;
        }
        envEntries = CommonAnnotationHelper.getEnvEntries(helper);
    }
    
    private void initMessageDestinationRefs() {
        if (messageDestinationRefs != null) {
            return;
        }
        messageDestinationRefs = CommonAnnotationHelper.getMessageDestinationRefs(helper);
    }
    
    private void initServiceRefs() {
        if (serviceRefs != null) {
            return;
        }
        serviceRefs = CommonAnnotationHelper.getServiceRefs(helper);
    }
    
    private void initEjbRefs() {
        
        if (ejbRefs != null) {
            return;
        }
        
        final List<EjbRef> resultEjbRefs = new ArrayList<EjbRef>();
        
        EjbRefHelper.setEjbRefs(helper, resultEjbRefs, null);
        
        ejbRefs = resultEjbRefs.toArray(new EjbRef[resultEjbRefs.size()]);
                
    }
    
    // <editor-fold desc="Model implementation">
    public ResourceRef[] getResourceRef() {
        initResourceRefs();
        return resourceRefs;
    }

    public ResourceRef getResourceRef(int index) {
        initResourceRefs();
        return resourceRefs[index];
    }

    public int sizeResourceRef() {
        initResourceRefs();
        return resourceRefs.length;
    }
    
    public ResourceEnvRef[] getResourceEnvRef() {
        initResourceEnvRefs();
        return resourceEnvRefs;
    }

    public ResourceEnvRef getResourceEnvRef(int index) {
        initResourceEnvRefs();
        return resourceEnvRefs[index];
    }

    public int sizeResourceEnvRef() {
        initResourceEnvRefs();
        return resourceEnvRefs.length;
    }

    public EnvEntry[] getEnvEntry() {
        initEnvEntries();
        return envEntries;
    }

    public EnvEntry getEnvEntry(int index) {
        initEnvEntries();
        return envEntries[index];
    }

    public int sizeEnvEntry() {
        initEnvEntries();
        return envEntries.length;
    }
    
    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs;
    }

    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs[index];
    }

    public int sizeMessageDestinationRef() throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs.length;
    }
    
    public ServiceRef[] getServiceRef() throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs;
    }

    public ServiceRef getServiceRef(int index) throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs[index];
    }

    public int sizeServiceRef() throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs.length;
    }
    
    public EjbRef[] getEjbRef() {
        initEjbRefs();
        return ejbRefs;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addEjbRef(EjbRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEnvEntry(EnvEntry valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addIcon(Icon valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMessageDestination(MessageDestination valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMessageDestinationRef(MessageDestinationRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addResourceEnvRef(ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addResourceRef(ResourceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addServiceRef(ServiceRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCallbackHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef getEjbRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SAXParseException getError() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Icon getIcon(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestination[] getMessageDestination() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestination getMessageDestination(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEjbRef(EjbRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEnvEntry(EnvEntry valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeIcon(Icon valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMessageDestination(MessageDestination valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMessageDestinationRef(MessageDestinationRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeResourceEnvRef(ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeResourceRef(ResourceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeServiceRef(ServiceRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCallbackHandler(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRef(int index, EjbRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRef(EjbRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnvEntry(int index, EnvEntry valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnvEntry(EnvEntry[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setIcon(int index, Icon valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setIcon(Icon[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestination(int index,
                                      MessageDestination valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestination(MessageDestination[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRef(int index,
                                         MessageDestinationRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceEnvRef(int index, ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceEnvRef(ResourceEnvRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceRef(int index, ResourceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceRef(ResourceRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRef(int index, ServiceRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeIcon() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMessageDestination() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef newEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvEntry newEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Icon newIcon() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestination newMessageDestination() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceEnvRef newResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceRef newResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRef newServiceRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BigDecimal getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setVersion(BigDecimal version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(FileObject fo) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(RootInterface root, int mode) {
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

    public CommonDDBean addBean(String beanName, String[] propertyNames,
                                Object[] propertyValues, String keyProperty) throws ClassNotFoundException,
                                                                                    NameAlreadyUsedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean findBeanByName(String beanName, String propertyName,
                                       String value) {
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

    public void setAllIcons(String[] locales, String[] smallIcons,
                            String[] largeIcons) throws VersionNotSupportedException {
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
