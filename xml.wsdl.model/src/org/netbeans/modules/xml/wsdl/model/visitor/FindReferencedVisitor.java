/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author Nam Nguyen
 */
public class FindReferencedVisitor<T extends ReferenceableWSDLComponent> extends DefaultVisitor {
    private Class<T> type;
    private String localName;
    private T referenced = null;
    private Definitions root;
    
    /** Creates a new instance of FindReferencedVisitor */
    public FindReferencedVisitor(Definitions root) {
        this.root = root;
    }
    
    public T find(String localName, Class<T> type) {
        this.type = type;
        this.localName = localName;
        visitChildren(root);
        return referenced;
    }

    public void visit(Binding c) {
        checkReference(c, true); //extensible
    }

    public void visit(Message c) { //extensible
        checkReference(c, true);
    }

    public void visit(PortType c) { //extensible
        checkReference(c, true);
    }

    public void visit(ExtensibilityElement c) {
        if (c instanceof Referenceable) {
            checkReference(ReferenceableExtensibilityElement.class.cast(c), true);
        }
        visitChildren(c);
    }

    private void checkReference(ReferenceableWSDLComponent c, boolean checkChildren) {
         if (type.isAssignableFrom(c.getClass()) && c.getName().equals(localName)) {
             referenced = type.cast(c);
             return;
         } else if (checkChildren) {
             visitChildren(c);
         }
    }
    
    private void visitChildren(WSDLComponent c) {
        for (WSDLComponent child : c.getChildren()) {
           if (referenced != null) { // before start each visit
                return;
           }
            child.accept(this);
        }
    }
    
}
