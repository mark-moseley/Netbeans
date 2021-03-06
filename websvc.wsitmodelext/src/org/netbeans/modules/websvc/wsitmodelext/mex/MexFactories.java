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

package org.netbeans.modules.websvc.wsitmodelext.mex;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.DialectImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.IdentifierImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.LocationImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.MetadataImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.MetadataReferenceImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.MetadataSectionImpl;

public class MexFactories {

    public static class Metadata extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.METADATA.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MetadataImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MetadataImpl(context.getModel(), element);
        }
    }   

    public static class MetadataReference extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.METADATAREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MetadataReferenceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MetadataReferenceImpl(context.getModel(), element);
        }
    }

    public static class MetadataSection extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.METADATASECTION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MetadataSectionImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MetadataSectionImpl(context.getModel(), element);
        }
    }

    public static class Dialect extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.DIALECT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new DialectImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new DialectImpl(context.getModel(), element);
        }
    }

    public static class Identifier extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.IDENTIFIER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new IdentifierImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IdentifierImpl(context.getModel(), element);
        }
    }

    public static class Location extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.LOCATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new LocationImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LocationImpl(context.getModel(), element);
        }
    }
}
