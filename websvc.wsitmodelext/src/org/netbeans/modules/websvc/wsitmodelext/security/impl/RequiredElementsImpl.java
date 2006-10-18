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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.security.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.RequiredElements;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class RequiredElementsImpl extends SecurityPolicyComponentImpl implements RequiredElements {
    
    /**
     * Creates a new instance of RequiredElementsImpl
     */
    public RequiredElementsImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public RequiredElementsImpl(WSDLModel model){
        this(model, createPrefixedElement(SecurityPolicyQName.REQUIREDELEMENTS.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

}
