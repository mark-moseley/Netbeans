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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.spi.NamedExtensibilityElementBase;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 * 
 * changed by 
 * @author ads
 */
public class RoleImpl extends NamedExtensibilityElementBase implements Role {
    
    /** Creates a new instance of RoleImpl */
    public RoleImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public RoleImpl(WSDLModel model){
        this(model, createPrefixedElement(BPELQName.ROLE.getQName(), model));
    }
    
    protected String getNamespaceURI() {
        return BPELQName.PLNK_NS;
    }
    
    public NamedComponentReference<PortType> getPortType() {
        return resolveGlobalReference(PortType.class, BPELAttribute.PORT_TYPE);
    }

    public void setPortType(NamedComponentReference<PortType> portType) {
        setAttribute(PORT_TYPE_PROPERTY, BPELAttribute.PORT_TYPE, portType);
    }

    public void accept(BPELExtensibilityComponent.Visitor v) {
        v.visit(this);
    }

    @Override
    public void addExtensibilityElement(ExtensibilityElement ee) {
        if (ee instanceof Documentation) {
            addRoleDocumentation((Documentation) ee);
        } else {
            super.addExtensibilityElement(ee);
        }
    }

    public void addRoleDocumentation(Documentation doc) {
        if (doc == null) return;
        appendChild(ROLE_DOCUMENTATION_PROPERTY, doc);
    }

    public void removeRoleDocumentation(Documentation doc) {
        removeChild(ROLE_DOCUMENTATION_PROPERTY, doc);
        
    }

    public Collection<Documentation> getRoleDocumentations() {
        return getChildren(Documentation.class);
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof PartnerLinkType) {
            return true;
        }
        return false;
    }
}
