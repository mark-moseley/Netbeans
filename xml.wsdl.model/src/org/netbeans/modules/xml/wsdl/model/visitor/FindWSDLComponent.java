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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * XMLModelMapperVisitor.java
 *
 * Created on October 28, 2005, 3:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.xam.xdm.ComponentFinder;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.visitor.XPathFinder;

/**
 *
 * @author ajit
 */
public class FindWSDLComponent extends ChildVisitor implements ComponentFinder<WSDLComponent> {
    
    /** Creates a new instance of XMLModelMapperVisitor */
    public FindWSDLComponent() {
    }
    
    public static <T extends WSDLComponent> T findComponent(Class<T> type, WSDLComponent root, String xpath) {
        return type.cast(new FindWSDLComponent().findComponent(root, xpath));
    }
    
    public WSDLComponent findComponent(WSDLComponent root, Element xmlNode) {
        assert root instanceof WSDLComponent;
        assert xmlNode != null;
        
        this.xmlNode = xmlNode;
        result = null;
        WSDLComponent wsdlRoot = WSDLComponent.class.cast(root);
        wsdlRoot.accept(this);
        return result;
    }
    
    public WSDLComponent findComponent(WSDLComponent root, String xpath) {
        Document doc = null;
        if (root instanceof WSDLComponentBase) {
            WSDLComponentBase rootImpl = (WSDLComponentBase) root;
            WSDLModel model = rootImpl.getModel();
            doc = Document.class.cast(model.getDocument());
        }
        if (doc == null) {
            return null;
        }
        
        Node result = new XPathFinder().findNode(doc, xpath);
        if (result instanceof Element) {
            return findComponent(root, (Element) result);
        } else {
            return null;
        }
    }

    protected void visitComponent(WSDLComponent c) {
        if (result != null) return;
        if (! (c instanceof WSDLComponentBase)) return;
        WSDLComponentBase component = (WSDLComponentBase) c;
        if (component.referencesSameNode(xmlNode)) {
            result = component;
            return;
        } else {
            super.visitComponent(component);
        }
    }
    
    private WSDLComponent result;
    private Element xmlNode;
}
