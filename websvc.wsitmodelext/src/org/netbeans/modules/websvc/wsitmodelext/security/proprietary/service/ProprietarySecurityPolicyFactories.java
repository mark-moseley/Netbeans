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

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.TimestampImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.CallbackHandlerConfigurationImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.CallbackHandlerImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.DisableStreamingSecurityImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.KeyStoreImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.TrustStoreImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ValidatorConfigurationImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ValidatorImpl;

public class ProprietarySecurityPolicyFactories {

    public static class KeyStoreServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.KEYSTORE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new KeyStoreImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KeyStoreImpl(context.getModel(), element);
        }
    }

    public static class ValidatorConfigurationServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.VALIDATORCONFIGURATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ValidatorConfigurationImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ValidatorConfigurationImpl(context.getModel(), element);
        }
    }

    public static class ValidatorServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.VALIDATOR.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ValidatorImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ValidatorImpl(context.getModel(), element);
        }
    }

    public static class TimestampServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.TIMESTAMP.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TimestampImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TimestampImpl(context.getModel(), element);
        }
    }
    
    public static class TrustStoreServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.TRUSTSTORE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TrustStoreImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TrustStoreImpl(context.getModel(), element);
        }
    }

    public static class CallbackHandlerServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.CALLBACKHANDLER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new CallbackHandlerImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CallbackHandlerImpl(context.getModel(), element);
        }
    }

    public static class CallbackHandlerConfigurationServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.CALLBACKHANDLERCONFIGURATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new CallbackHandlerConfigurationImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CallbackHandlerConfigurationImpl(context.getModel(), element);
        }
    }

    public static class DisableStreamingSecurityServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.DISABLESTREAMINGSECURITY.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new DisableStreamingSecurityImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new DisableStreamingSecurityImpl(context.getModel(), element);
        }
    }
    
}
