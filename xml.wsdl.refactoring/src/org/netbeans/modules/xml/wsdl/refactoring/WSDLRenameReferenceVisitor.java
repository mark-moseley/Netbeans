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
package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.visitor.DefaultVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 */
public class WSDLRenameReferenceVisitor extends DefaultVisitor implements WSDLVisitor {
    RenameRefactoring request;
    String oldName;
    Referenceable target;
    
    /** Creates a new instance of WSDLRenameRefactorVisitor */
    public WSDLRenameReferenceVisitor() {
    }
    
    public void refactor(Model mod, Set<RefactoringElementImplementation> elements, RenameRefactoring request) throws IOException {
        if (request == null || elements == null || mod == null) return;
        if (! (mod instanceof WSDLModel)) return;
        
        this.request = request;
        this.target = request.getRefactoringSource().lookup(Referenceable.class);
        this.oldName = request.getContext().lookup(String.class);
        WSDLModel model = (WSDLModel) mod;
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            for (RefactoringElementImplementation element: elements) {
                assert element.getLookup().lookup(WSDLComponent.class)!=null : "Wrong component type in WSDL usage group"; //NOI18N
                element.getLookup().lookup(WSDLComponent.class).accept(this);
            }
        } finally {
            if (startTransaction && model.isIntransaction())
                model.endTransaction();
        }
    }
    
    private <T extends ReferenceableWSDLComponent> NamedComponentReference<T>
            createReference(Class<T> type, WSDLComponent referencing) {
        T referenced = type.cast(target);
        return referencing.createReferenceTo(referenced, type);
    }
    
    public void visit(BindingOperation referencing) {
        assert target instanceof Operation : "Invalid type, expect Operation"; //NOI18N
        referencing.setName(request.getNewName());
    }
    
    public void visit(Input referencing) {
        referencing.setMessage(createReference(Message.class, referencing));
    }
    
    public void visit(Output referencing) {
        referencing.setMessage(createReference(Message.class, referencing));
    }
    
    public void visit(Fault referencing) {
        referencing.setMessage(createReference(Message.class, referencing));
    }
    
    public void visit(Port referencing) {
        referencing.setBinding(createReference(Binding.class, referencing));
    }
    
    private boolean isOverloaded(BindingOperation bindingOperation) {
        assert ! bindingOperation.getOperation().isBroken() :
            "Broken operation reference: "+bindingOperation.getName();
        Operation operation = bindingOperation.getOperation().get();
        PortType pt = (PortType) operation.getParent();
        assert pt != null : "Operation not in tree";
        for (Operation o : pt.getOperations()) {
            if (o != operation && operation.getName().equals(o.getName())) {
                return true;
            }
        }
        return false;
    }
    
    public void visit(BindingInput referencing) {
        if (referencing.getName() != null && referencing.getName().equals(oldName) ||
                isOverloaded((BindingOperation) referencing.getParent())) {
            referencing.setName(request.getNewName());
        }
    }
    
    public void visit(BindingOutput referencing) {
        if (referencing.getName() != null && referencing.getName().equals(oldName) ||
                isOverloaded((BindingOperation) referencing.getParent())) {
            referencing.setName(request.getNewName());
        }
    }
    
    public void visit(BindingFault referencing) {
        referencing.setName(request.getNewName());
    }
    
    public void visit(Binding referencing) {
        referencing.setType(createReference(PortType.class, referencing));
    }

    public void visit(ExtensibilityElement referencing) {
        if (referencing instanceof SOAPComponent) {
            ((SOAPComponent) referencing).accept(new SOAPReferencingVisitor());
        }
    }
    
    // SOAPComponent.Visitor

    public class SOAPReferencingVisitor implements SOAPComponent.Visitor {
        public void visit(SOAPHeader referencing) {
            if (target instanceof Message) {
                referencing.setMessage(createReference(Message.class, referencing));
            } else if (target instanceof Part) {
                referencing.setPartRef(createReference(Part.class, referencing));
            }
        }

        public void visit(SOAPFault referencing) {
            referencing.setFault(createReference(Fault.class, referencing));
        }

        public void visit(SOAPHeaderFault referencing) {
            if (target instanceof Message) {
                referencing.setMessage(createReference(Message.class, referencing));
            } else if (target instanceof Part) {
                referencing.setPartRef(createReference(Part.class, referencing));
            }
        }
        
        public void visit(SOAPOperation referencing) {
            // no explicit reference
        }

        public void visit(SOAPBinding referencing) {
            // no explicit reference
        }

        public void visit(SOAPBody referencing) {
            if (target instanceof Part) {
                List<String> parts = referencing.getParts();
                int i = parts.indexOf(oldName);
                parts.remove(i);
                parts.add(i, request.getNewName());
                referencing.setParts(parts);
            }
        }

        public void visit(SOAPAddress referencing) {
            // no explicit reference
        }
    }
}
