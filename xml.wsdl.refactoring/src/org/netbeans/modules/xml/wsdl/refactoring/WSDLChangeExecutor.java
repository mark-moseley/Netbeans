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
package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import org.netbeans.modules.xml.refactoring.DeleteRequest;
import org.netbeans.modules.xml.refactoring.RefactorRequest;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.spi.ChangeExecutor;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Component;
import org.openide.util.NbBundle;

/**
 *
 * @author Nam Nguyen
 */
public class WSDLChangeExecutor extends ChangeExecutor {
    
    /** Creates a new instance of WSDLChangeExecutor */
    public WSDLChangeExecutor() {
    }

    public <T extends RefactorRequest> boolean canChange(Class<T> changeType, Component target) {
        if (target instanceof WSDLComponent && 
            (changeType.isAssignableFrom(RenameRequest.class) ||
            changeType.isAssignableFrom(DeleteRequest.class))) 
        {
            return true;
        }
        return false;
    }

    public void precheck(RefactorRequest request) {
        super.precheck(request);
        if (request instanceof RenameRequest) {
            _precheck((RenameRequest) request);
        }
    }
    
    public void doChange(RefactorRequest request) throws IOException {
        if (request instanceof RenameRequest) {
            _doChange((RenameRequest) request);
        } else if (request instanceof DeleteRequest) {
            _doChange((DeleteRequest) request);
        } else {
            //just do nothing
        }
    }
    
    public UIHelper getUIHelper() {
        //TODO
        return super.getUIHelper();
    }
    
    private void _doChange(RenameRequest request) throws IOException {
        if (! (request.getTarget() instanceof ReferenceableWSDLComponent)) {
            return;
        }
        ReferenceableWSDLComponent target = (ReferenceableWSDLComponent)request.getTarget();

        try {
            target.getModel().startTransaction();
            target.setName(request.getNewName());
        } finally {
            if (target.getModel().isIntransaction())
                target.getModel().endTransaction();
        }
        
        request.setRenamedTarget(target);
    }
    
    private void _doChange(DeleteRequest request) throws IOException {
        if (! (request.getTarget() instanceof ReferenceableWSDLComponent)) {
            return;
        }
        ReferenceableWSDLComponent target = (ReferenceableWSDLComponent)request.getTarget();
        
        try {
            target.getModel().startTransaction();
            target.getModel().removeChildComponent(target);
        } finally {
            if (target.getModel().isIntransaction())
                target.getModel().endTransaction();
        }
        request.setDone(true);
    }

    private void _precheck(RenameRequest request) {
        if (request.getNewName() == null || request.getNewName().length() == 0) {
            request.addError(NbBundle.getMessage(WSDLChangeExecutor.class, "MSG_NewNameNullEmpty"));
        }
    }
}
