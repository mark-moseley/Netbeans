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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.core.jaxws.bindings.model.impl;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponent;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Roderico Cruz
 */
public abstract class BindingsComponentImpl extends AbstractDocumentComponent<BindingsComponent>
        implements BindingsComponent{
    
    /** Creates a new instance of BindingsComponentImpl */
    public BindingsComponentImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }

    protected void populateChildren(List<BindingsComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    BindingsModel bindingsModel = getModel();
                    BindingsComponentImpl comp = (BindingsComponentImpl) bindingsModel.getFactory().create((Element)n,this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    protected abstract String getNamespaceURI(); 

    public BindingsModelImpl getModel() {
        return (BindingsModelImpl) super.getModel();
    }
    
    protected static org.w3c.dom.Element createNewElement(QName qName, BindingsModel model){
        return model.getDocument().createElementNS(
                qName.getNamespaceURI(),
                qName.getLocalPart());
    }
    
    protected static org.w3c.dom.Element createPrefixedElement(QName qName, BindingsModel model){
        org.w3c.dom.Element e = createNewElement(qName, model);
        e.setPrefix(qName.getPrefix());
        return e;
    }
    
}
