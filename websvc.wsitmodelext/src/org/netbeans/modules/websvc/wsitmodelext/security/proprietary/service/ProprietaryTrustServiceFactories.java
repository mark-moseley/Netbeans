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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.IssuerImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.KeyTypeImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.CertAliasImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ContractImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.LifeTimeSTSImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.STSConfigurationServiceImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ServiceProviderImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ServiceProvidersImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.TokenTypeImpl;

public class ProprietaryTrustServiceFactories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class CertAliasFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.CERTALIAS.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CertAliasImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ContractFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.CONTRACT.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ContractImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class STSIssuerFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.ISSUER.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IssuerImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class STSConfigurationFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.STSCONFIGURATION.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new STSConfigurationServiceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ServiceProviderFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.SERVICEPROVIDER.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ServiceProviderImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ServiceProvidersFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.SERVICEPROVIDERS.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ServiceProvidersImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class TokenTypeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.TOKENTYPE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TokenTypeImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class KeyTypeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.KEYTYPE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KeyTypeImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class LifeTimeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.LIFETIME.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LifeTimeSTSImpl(context.getModel(), element);
        }
    }
}
