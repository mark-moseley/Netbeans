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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ServiceImpl extends TMapComponentContainerImpl 
    implements Service
{

    public ServiceImpl(TMapModelImpl model) {
        this(model, createNewElement(TMapComponents.SERVICE, model));
    }

    public ServiceImpl(TMapModelImpl model, Element element) {
        super(model, element);
    }

    public void accept(TMapVisitor visitor) {
        visitor.visit(this);
    }

    public List<Operation> getOperations() {
        return getChildren(Operation.class);
    }

    public void addOperation(Operation operation) {
        addAfter(TYPE.getTagName(), operation, TYPE.getChildTypes());
    }

    public int getSizeOfOperations() {
        List<Operation> operations = getChildren(Operation.class);
        return operations == null ? 0 : operations.size();
    }

    public void removeOperation(Operation operation) {
        removeChild(TYPE.getTagName(), operation);
    }

    public WSDLReference<PartnerLinkType> getPartnerLinkType() {
        return getWSDLReference(TMapAttributes.PARTNER_LINK_TYPE, PartnerLinkType.class);
    }

    public void setPartnerLinkType(WSDLReference<PartnerLinkType> pltRef) {
        setWSDLReference( TMapAttributes.PARTNER_LINK_TYPE, pltRef);
    }

    public WSDLReference<Role> getRole() {
        return getWSDLReference(TMapAttributes.ROLE_NAME, Role.class);
    }

    public void setRole(WSDLReference<Role> roleRef) {
        setWSDLReference( TMapAttributes.ROLE_NAME, roleRef);
    }

    public Reference[] getReferences() {
        return new Reference[] {getPartnerLinkType(), getRole()};
    }

    public Class<Service> getComponentType() {
        return Service.class;
    }
}
