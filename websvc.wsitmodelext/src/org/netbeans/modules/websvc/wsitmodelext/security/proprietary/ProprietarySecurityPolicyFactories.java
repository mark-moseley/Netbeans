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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl.TimestampImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl.CallbackHandlerConfigurationImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl.CallbackHandlerImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl.KerberosConfigImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl.KeyStoreImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl.TrustStoreImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl.ValidatorConfigurationImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl.ValidatorImpl;

public class ProprietarySecurityPolicyFactories {

    public static class KeyStoreFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyQName.KEYSTORE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KeyStoreImpl(context.getModel(), element);
        }
    }

    public static class KerberosConfigFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyQName.KERBEROSCONFIG.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KerberosConfigImpl(context.getModel(), element);
        }
    }
    
    public static class ValidatorConfigurationFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyQName.VALIDATORCONFIGURATION.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ValidatorConfigurationImpl(context.getModel(), element);
        }
    }

    public static class ValidatorFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyQName.VALIDATOR.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ValidatorImpl(context.getModel(), element);
        }
    }
    
    public static class TimestampFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyQName.TIMESTAMP.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TimestampImpl(context.getModel(), element);
        }
    }

    public static class TrustStoreFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyQName.TRUSTSTORE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TrustStoreImpl(context.getModel(), element);
        }
    }

    public static class CallbackHandlerFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyQName.CALLBACKHANDLER.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CallbackHandlerImpl(context.getModel(), element);
        }
    }

    public static class CallbackHandlerConfigurationFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyQName.CALLBACKHANDLERCONFIGURATION.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CallbackHandlerConfigurationImpl(context.getModel(), element);
        }
    }

}
