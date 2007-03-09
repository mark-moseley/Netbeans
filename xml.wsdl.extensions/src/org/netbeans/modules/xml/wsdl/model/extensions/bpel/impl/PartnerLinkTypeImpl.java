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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.spi.NamedExtensibilityElementBase;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 * changed by
 * @author ads
 */
public class PartnerLinkTypeImpl extends NamedExtensibilityElementBase
        implements PartnerLinkType
{

    PartnerLinkTypeImpl( WSDLModel model, Element e ) {
        super(model, e);
    }

    public PartnerLinkTypeImpl( WSDLModel model ) {
        this(model, createPrefixedElement(BPELQName.PARTNER_LINK_TYPE
                .getQName(), model));
    }

    protected String getNamespaceURI() {
        return BPELQName.PLNK_NS;
    }

    List<Role> getRoles() {
        return getChildren(Role.class);
    }

    void addRole( Role role ) {
        appendChild(ROLE_PROPERTY, role);
    }

    void removeRole( Role role ) {
        removeChild(ROLE_PROPERTY, role);
    }

    public ComponentUpdater getComponentUpdater() {
        return new BPELComponentUpdater();
    }

    public void accept( BPELExtensibilityComponent.Visitor v ) {
        v.visit(this);
    }

    public void setRole2( Role role ) {
        List<Role> roles = getRoles();
        if (roles.size() == 0) {
            throw new IllegalStateException("Needs to set role 1 first");
        }
        else {
            Role old = getRole2();
            if (old != null) {
                removeRole(old);
            }
            if (role != null) {
                insertAtIndex(ROLE_PROPERTY, role, 1, Role.class);
            }
        }
    }

    public void setRole1( Role role ) {
        List<Role> roles = getRoles();
        if (roles.size() == 0) {
            addRole(role);
        }
        else {
            Role old = getRole1();
            if (old != null) {
                removeRole(old);
            }
            if (role != null) {
                insertAtIndex(ROLE_PROPERTY, role, 0, Role.class);
            }
        }
    }

    public Role getRole2() {
        List<Role> roles = getRoles();
        if (roles.size() > 1) {
            return roles.get(1);
        }
        return null;
    }

    public Role getRole1() {
        List<Role> roles = getRoles();
        if (roles.size() > 0) {
            return roles.get(0);
        }
        return null;
    }

    @Override
    public void addExtensibilityElement(ExtensibilityElement ee) {
        if (ee instanceof Documentation) {
            addPartnerLinkTypeDocumentation((Documentation) ee);
        } else {
            super.addExtensibilityElement(ee);
        }
    }

    public void addPartnerLinkTypeDocumentation(Documentation doc) {
        if (doc == null) return;
        Collection<WSDLComponent> children = getChildren();
        int i = 0;
        if (children != null) {
            for (WSDLComponent child : children) {
                //add just before the role element.
                if (child instanceof Role) {
                    break;
                }
                i++;
            }
        }
        insertAtIndex(PARTNERLINKTYPE_DOCUMENTATION_PROPERTY, doc, i);
    }
    
    public void insertPartnerLinkTypeDocumentationAt(Documentation doc, int index) {
        if (doc == null) return;
        insertAtIndex(PARTNERLINKTYPE_DOCUMENTATION_PROPERTY, doc, index);
    }

    public void removePartnerLinkTypeDocumentation(Documentation doc) {
        removeChild(PARTNERLINKTYPE_DOCUMENTATION_PROPERTY, doc);
    }

    public Collection<Documentation> getPartnerLinkTypeDocumentations() {
        return getChildren(Documentation.class);
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof Definitions) {
            return true;
        }
        return false;
    }

}
