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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10UsingAddressing;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class AddressingModelHelper {
    
    /**
     * Creates a new instance of AddressingModelHelper
     */
    public AddressingModelHelper() {
    }
    
    public static Addressing10UsingAddressing getUsingAddressing(Policy p) {
        return (Addressing10UsingAddressing) PolicyModelHelper.getTopLevelElement(p, Addressing10UsingAddressing.class);        
    }
    
    // checks if Addressing is enabled in the config wsdl on specified binding
    public static boolean isAddressingEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            Addressing10UsingAddressing addrAssertion = getUsingAddressing(p);
            return (addrAssertion != null);
        }
        return false;
    }
    
    // enables Addressing in the config wsdl on specified binding
    public static void enableAddressing(Binding b) {
        All a = PolicyModelHelper.createPolicy(b);
        PolicyModelHelper.createElement(a, Addressing10QName.USINGADDRESSING.getQName(), Addressing10UsingAddressing.class, false);
    }

    // disables Addressing in the config wsdl on specified binding
    public static void disableAddressing(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        Addressing10UsingAddressing a = getUsingAddressing(p);
        if (a != null) {
            PolicyModelHelper.removeElement(a.getParent(), Addressing10UsingAddressing.class, false);
        }
    }
}
