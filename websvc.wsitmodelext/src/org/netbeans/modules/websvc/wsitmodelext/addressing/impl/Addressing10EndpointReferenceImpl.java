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

package org.netbeans.modules.websvc.wsitmodelext.addressing.impl;

import org.netbeans.modules.websvc.wsitmodelext.addressing.Address10;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10Metadata;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10ReferenceProperties;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10EndpointReference;

/**
 *
 * @author Martin Grebac
 */
public class Addressing10EndpointReferenceImpl extends Addressing10ComponentImpl implements Addressing10EndpointReference {
    
    /**
     * Creates a new instance of Addressing10EndpointReferenceImpl
     */
    public Addressing10EndpointReferenceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public Addressing10EndpointReferenceImpl(WSDLModel model){
        this(model, createPrefixedElement(Addressing10QName.ENDPOINTREFERENCE.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setAddress(Address10 address) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Address10.class, ADDRESS_PROPERTY, address, classes);
    }

    public Address10 getAddress() {
        return getChild(Address10.class);
    }

    public void removeAddress(Address10 address) {
        removeChild(ADDRESS_PROPERTY, address);
    }

    public void setReferenceProperties(Addressing10ReferenceProperties referenceProperties) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Addressing10ReferenceProperties.class, REFERENCE_PROPERTIES_PROPERTY, referenceProperties, classes);
    }

    public Addressing10ReferenceProperties getReferenceProperties() {
        return getChild(Addressing10ReferenceProperties.class);
    }

    public void removeReferenceProperties(Addressing10ReferenceProperties referenceProperties) {
        removeChild(REFERENCE_PROPERTIES_PROPERTY, referenceProperties);
    }

    public void setAddressing10Metadata(Addressing10Metadata addressingMetadata) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Addressing10Metadata.class, METADATA_PROPERTY, addressingMetadata, classes);
    }

    public Addressing10Metadata getAddressing10Metadata() {
        return getChild(Addressing10Metadata.class);
    }

    public void removeAddressing10Metadata(Addressing10Metadata addressing10Metadata) {
        removeChild(METADATA_PROPERTY, addressing10Metadata);
    }
    
}
