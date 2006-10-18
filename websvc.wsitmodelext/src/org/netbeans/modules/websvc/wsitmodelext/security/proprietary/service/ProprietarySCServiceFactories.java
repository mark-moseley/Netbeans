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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.LifeTimeServiceImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.SCConfigurationServiceImpl;

public class ProprietarySCServiceFactories {

    public static class SCConfigurationServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySCServiceQName.SCCONFIGURATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SCConfigurationServiceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SCConfigurationServiceImpl(context.getModel(), element);
        }
    }

    public static class LifeTimeServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySCServiceQName.LIFETIME.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new LifeTimeServiceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LifeTimeServiceImpl(context.getModel(), element);
        }
    }
}
