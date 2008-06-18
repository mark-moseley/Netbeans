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

package org.netbeans.modules.websvc.wsitmodelext.rm;

import org.netbeans.modules.websvc.wsitmodelext.rm.impl.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

public class RMFactories {

    public static class RMAssertionFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(RMQName.RMASSERTION.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RMAssertionImpl(context.getModel(), element);
        }
    }

    public static class AcknowledgementIntervalFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.ACKNOWLEDGEMENTINTERVAL.getQName(ConfigVersion.CONFIG_1_0));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AcknowledgementIntervalImpl(context.getModel(), element);
        }
    }

    public static class DeliveryAssuranceFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.DELIVERYASSURANCE.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new DeliveryAssuranceImpl(context.getModel(), element);
        }
    }

    public static class ExactlyOnceFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.EXACTLYONCE.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ExactlyOnceImpl(context.getModel(), element);
        }
    }

    public static class AtMostOnceFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.ATMOSTONCE.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AtMostOnceImpl(context.getModel(), element);
        }
    }

    public static class AtLeastOnceFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.ATLEASTONCE.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AtLeastOnceImpl(context.getModel(), element);
        }
    }

    public static class InOrderFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.INORDER.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new InOrderImpl(context.getModel(), element);
        }
    }

    public static class SequenceSTRFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.SEQUENCESTR.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SequenceSTRImpl(context.getModel(), element);
        }
    }

    public static class SequenceTransportSecurityFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.SEQUENCETRANSPORTSECURITY.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SequenceTransportSecurityImpl(context.getModel(), element);
        }
    }
    
}
