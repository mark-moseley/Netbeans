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

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySCClientQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.SCClientConfiguration;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class SCClientConfigurationImpl extends ProprietarySCComponentClientImpl implements SCClientConfiguration {
    
    /**
     * Creates a new instance of SCClientConfigurationImpl
     */
    public SCClientConfigurationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SCClientConfigurationImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietarySCClientQName.SCCLIENTCONFIGURATION.getQName(), model));
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
    
    public void setRequireCancelSCT(boolean requireCancel) {
        setAttribute(REQUIRECANCELSCT, ProprietarySecurityPolicyAttribute.REQUIRECANCELSCT, Boolean.toString(requireCancel));
    }

    public boolean isRequireCancelSCT() {
        return Boolean.parseBoolean(getAttribute(ProprietarySecurityPolicyAttribute.REQUIRECANCELSCT));
    }

    public void setRenewExpiredSCT(boolean renewExpired) {
        setAttribute(RENEWEXPIREDSCT, ProprietarySecurityPolicyAttribute.RENEWEXPIREDSCT, Boolean.toString(renewExpired));
    }

    public boolean isRenewExpiredSCT() {
        return Boolean.parseBoolean(getAttribute(ProprietarySecurityPolicyAttribute.RENEWEXPIREDSCT));
    }
    
}
