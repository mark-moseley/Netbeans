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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.rm.FlowControl;
import org.netbeans.modules.websvc.wsitmodelext.rm.InactivityTimeout;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMAssertion;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author Martin Grebac
 */
public class RMModelHelper {
    
    public static final String DEFAULT_TIMEOUT = "600000";         //NOI18N
    public static final String DEFAULT_MAXRCVBUFFERSIZE = "32";    //NOI18N

    /**
     * Creates a new instance of RMModelHelper
     */
    public RMModelHelper() {
    }
    
    public static RMAssertion getRMAssertion(Policy p) {
        return (RMAssertion) PolicyModelHelper.getTopLevelElement(p, RMAssertion.class);
    }
    
    // checks if RM is enabled in the config wsdl on specified binding
    public static boolean isRMEnabled(WSDLComponent c) {
        if (c instanceof Operation) {
            Operation o = (Operation)c;
            Binding b = (Binding)o.getParent();    
            return isRMEnabledB(b);
        }
        if (c instanceof Binding) {
            return isRMEnabledB((Binding)c);
        }
        return false;
    }
    
    // checks if RM is enabled in the config wsdl on specified binding
    private static boolean isRMEnabledB(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            RMAssertion rm = getRMAssertion(p);
            return (rm != null);
        }
        return false;
    }
    
    // enables RM in the config wsdl on specified binding
    public static void enableRM(Binding b) {
        All a = PolicyModelHelper.createPolicy(b, true);
        PolicyModelHelper.createElement(a, RMQName.RMASSERTION.getQName(), RMAssertion.class, false);
    }

    // disables RM in the config wsdl on specified binding
    public static void disableRM(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        RMAssertion rm = getRMAssertion(p);
        if (rm != null) {
            PolicyModelHelper.removeElement(rm.getParent(), RMAssertion.class, false);
            PolicyModelHelper.removeElement(rm.getParent(), FlowControl.class, false);
        }
        PolicyModelHelper.cleanPolicies(b);
    }
    
    public static String getInactivityTimeout(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        RMAssertion rm = getRMAssertion(p);
        return getInactivityTimeout(rm);
    }    
    
    public static String getInactivityTimeout(RMAssertion rm) {
        String timeout = null;
        if (rm != null) {
            InactivityTimeout time = rm.getInactivityTimeout();
            if (time != null) {
                timeout = time.getMilliseconds();
            }
        }
        return timeout;
    }

    public static void setInactivityTimeout(Binding b, String value) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        RMAssertion rm = getRMAssertion(p);
        setInactivityTimeout(rm, value);
    }
    
    public static void setInactivityTimeout(RMAssertion rm, String value) {
        if (rm != null) {
            Model model = rm.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                InactivityTimeout inTimeout = rm.getInactivityTimeout();
                if (inTimeout == null) {
                    if (value != null) {    // if is null, then there's no element and we want to remove it -> do nothing
                        WSDLComponentFactory wcf = rm.getModel().getFactory();
                        InactivityTimeout inT = (InactivityTimeout)wcf.create(rm, 
                                RMQName.INACTIVITYTIMEOUT.getQName()
                               );
                        inT.setMilliseconds(value);
                        rm.addExtensibilityElement(inT);
                    }
                } else {
                    if (value == null) {
                        rm.removeInactivityTimeout(inTimeout);
                    } else {
                        inTimeout.setMilliseconds(value);
                    }
                }
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
    }

}
