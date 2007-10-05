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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.KeyStore;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class KeyStoreImpl extends ProprietarySecurityPolicyComponentImpl implements KeyStore {
    
    /**
     * Creates a new instance of KeyStoreImpl
     */
    public KeyStoreImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public KeyStoreImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietarySecurityPolicyQName.KEYSTORE.getQName(), model));
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

    public void setAlias(String alias) {
        setAttribute(ALIAS, ProprietarySecurityPolicyAttribute.ALIAS, alias);        
    }

    public String getAlias() {
        return getAttribute(ProprietarySecurityPolicyAttribute.ALIAS);
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

    public void setKeyPassword(String keypass) {
        setAttribute(KEYPASSWORD, ProprietarySecurityPolicyAttribute.KEYPASS, keypass);
    }

    public String getKeyPassword() {
        return getAttribute(ProprietarySecurityPolicyAttribute.KEYPASS);
    }
}
