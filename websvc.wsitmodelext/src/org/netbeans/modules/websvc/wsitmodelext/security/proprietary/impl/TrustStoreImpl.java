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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.TrustStore;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class TrustStoreImpl extends ProprietarySecurityPolicyComponentImpl implements TrustStore {
    
    /**
     * Creates a new instance of TrustStoreImpl
     */
    public TrustStoreImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public TrustStoreImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietarySecurityPolicyQName.TRUSTSTORE.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setVisibility(String vis) {
        setAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName(), vis);
    }

    public String getVisibility() {
        return getAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName());
    }
    
    public void setLocation(String location) {
        setAttribute(LOCATION, ProprietarySecurityPolicyAttribute.LOCATION, location);        
    }

    public String getLocation() {
        return getAttribute(ProprietarySecurityPolicyAttribute.LOCATION);
    }
    
    public void setType(String type) {
        setAttribute(TYPE, ProprietarySecurityPolicyAttribute.TYPE, type);        
    }

    public String getType() {
        return getAttribute(ProprietarySecurityPolicyAttribute.TYPE);
    }

    public void setStorePassword(String storepass) {
        setAttribute(PASSWORD, ProprietarySecurityPolicyAttribute.STOREPASS, storepass);        
    }

    public String getStorePassword() {
        return getAttribute(ProprietarySecurityPolicyAttribute.STOREPASS);
    }

    public void setSTSAlias(String alias) {
        setAttribute(STSALIAS, ProprietarySecurityPolicyAttribute.STSALIAS, alias);        
    }

    public String getSTSAlias() {
        return getAttribute(ProprietarySecurityPolicyAttribute.STSALIAS);
    }

    public void setPeerAlias(String alias) {
        setAttribute(PEERALIAS, ProprietarySecurityPolicyAttribute.PEERALIAS, alias);        
    }

    public String getPeerAlias() {
        return getAttribute(ProprietarySecurityPolicyAttribute.PEERALIAS);
    }
    
}
