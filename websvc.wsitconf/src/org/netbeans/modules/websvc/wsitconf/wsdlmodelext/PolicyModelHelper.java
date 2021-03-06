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

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.ExactlyOne;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlQName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlUsingAddressing;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.wsdl.model.*;

/**
 *
 * @author Martin Grebac
 */
public class PolicyModelHelper {

    private static final String POLICY = "Policy";
    private static final Logger logger = Logger.getLogger(PolicyModelHelper.class.getName());
    
    /**
     * Creates top level policy (Policy/ExactlyOne/All) elements if they don't exist. Used for creating nested policies.
     * 
     * @param p - policy element, under which ExactlyOne/All gets created
     * @return the bottom-most All element
     */
    public static All createTopExactlyOneAll(final Policy p) {
        ExactlyOne eo = createElement(p, PolicyQName.EXACTLYONE.getQName(), ExactlyOne.class, false);
        All all = createElement(eo, PolicyQName.ALL.getQName(), All.class, false);
        return all;
    }

    /** Returns existing, or creates a new policy (Policy/ExactlyOne/All) and a PolicyReference 
     * attached to a Binding/BindingOperation/BindingOutput/Input/Fault element
     * should be used in order to create a policy, or to access the All element
     * component c must not be null
     */
    public static All createPolicy(final WSDLComponent c, boolean addressing) {

        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        Definitions d = model.getDefinitions();
        String policyName = null;
        String msgName = null;
        Policy policy = null;

        policy = getPolicyForElement(c);
        if (policy == null) {
            policyName = getPolicyUriForElement(c);
            if (policyName == null) {
                if (c instanceof Binding) {
                    policyName = ((Binding) c).getName() + POLICY;                 //NOI18N
                }
                if (c instanceof BindingInput) {
                    msgName = ((BindingInput) c).getName();
                    if (msgName == null) {
                        msgName = ((BindingOperation) c.getParent()).getName() + "_Input";          //NOI18N
                    }
                    Binding b = (Binding) c.getParent().getParent();
                    policyName = b.getName() + "_".concat(msgName).concat("_").concat(POLICY);           //NOI18N
                }
                if (c instanceof BindingOutput) {
                    msgName = ((BindingOutput) c).getName();
                    if (msgName == null) {
                        msgName = ((BindingOperation) c.getParent()).getName() + "_Output";          //NOI18N
                    }
                    Binding b = (Binding) c.getParent().getParent();
                    policyName = b.getName() + "_".concat(msgName).concat("_").concat(POLICY);           //NOI18N
                }
                if (c instanceof BindingFault) {
                    msgName = ((BindingFault) c).getName();
                    if (msgName == null) {
                        msgName = ((BindingOperation) c.getParent()).getName() + "_Fault";          //NOI18N
                    }
                    Binding b = (Binding) c.getParent().getParent();
                    policyName = b.getName() + "_".concat(msgName).concat("_").concat(POLICY);           //NOI18N
                }
                if (c instanceof BindingOperation) {
                    msgName = ((BindingOperation) c).getName();
                    if (msgName == null) {
                        msgName = ((BindingOperation) c.getParent()).getName();          //NOI18N
                    }
                    Binding b = (Binding) c.getParent();
                    policyName = b.getName() + "_".concat(msgName).concat("_").concat(POLICY);           //NOI18N
                }
            }

            List<Policy> policies = d.getExtensibilityElements(Policy.class);

            boolean isTransaction = model.isIntransaction();
            try {
                if (!isTransaction) {
                    model.startTransaction();
                }
                for (Policy p : policies) {
                    if (policyName.equals(p.getID())) {
                        List<PolicyReference> policyRefs = c.getExtensibilityElements(PolicyReference.class);
                        PolicyReference policyRef;
                        if ((policyRefs == null) || (policyRefs.isEmpty())) {
                            policyRef = (PolicyReference)wcf.create(c, PolicyQName.POLICYREFERENCE.getQName());
                        } else {
                            policyRef = policyRefs.get(0);
                        }
                        policyRef.setPolicyURI("#".concat(policyName));                   //NOI18N
                        c.addExtensibilityElement(policyRef);
                        All all = createTopExactlyOneAll(p);
                        if ((c instanceof Binding) && (addressing)) {
                            PolicyModelHelper.createElement(all, Addressing10WsdlQName.USINGADDRESSING.getQName(), Addressing10WsdlUsingAddressing.class, false);
                        }
                        return all;
                    }
                }
                policy = (Policy)wcf.create(d, PolicyQName.POLICY.getQName());
                policy.setID(policyName);
                PolicyReference policyRef = (PolicyReference)wcf.create(c, PolicyQName.POLICYREFERENCE.getQName());
                policyRef.setPolicyURI("#".concat(policyName));                   //NOI18N
                c.addExtensibilityElement(policyRef);
                d.addExtensibilityElement(policy);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }

        All all = createTopExactlyOneAll(policy);
        if ((c instanceof Binding) && (addressing)) {
            PolicyModelHelper.createElement(all, Addressing10WsdlQName.USINGADDRESSING.getQName(), Addressing10WsdlUsingAddressing.class, false);
        }
        return all;
    }

    /* Used to get specific domain elements under top of the policy - under POLICY/ExactlyOne/All/*SPECIFICELEMENT*
     * Does not create any elements
     */
    public @SuppressWarnings("unchecked")
    static <T extends ExtensibilityElement> T getTopLevelElement(WSDLComponent c, Class elementClass) {
        ExtensibilityElement e = null;
        if (c == null) {
            return null;
        }
        if (c instanceof Policy) {
            ExactlyOne eo = ((Policy) c).getExactlyOne();
            if (eo != null) {
                All all = eo.getAll();
                e = getTopLevelElement(all, elementClass);
            } else {
                List<ExtensibilityElement> l = c.getExtensibilityElements(elementClass);
                if ((l != null) && !(l.isEmpty())) {
                    e = l.get(0);
                }
            }
        } else {
            List<ExtensibilityElement> l = c.getExtensibilityElements(elementClass);
            if ((l != null) && !(l.isEmpty())) {
                e = l.get(0);
            }
        }
        return (T) e;
    }

    /* Returns name of policy attached to a wsdl component */
    public static String getPolicyUriForElement(WSDLComponent c) {
        List<PolicyReference> extPRefElems = c.getExtensibilityElements(PolicyReference.class);
        if ((extPRefElems != null) && (!extPRefElems.isEmpty())) {
            PolicyReference pref = extPRefElems.get(0);
            String policyURI = pref.getPolicyURI();
            return policyURI;
        }
        return null;
    }

    /* Returns policy with specific uri */
    private static Policy getPolicyForPolicyUri(String policyURI, Definitions d) {
        if ((policyURI != null) && (policyURI.startsWith("#"))) {   //NOI18N
            policyURI = policyURI.substring(1);
        }
        List<Policy> extPElems = d.getExtensibilityElements(Policy.class);
        for (Policy p : extPElems) {
            String id = p.getID();
            if (policyURI.equals(id)) {
                return p;
            }
        }
        return null;
    }

    /* Returns policy with specific uri */
    static void attachPolicyToElement(String policyURI, WSDLComponent c) {
        if (c == null) return;

        WSDLModel model = c.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        WSDLComponentFactory wcf = model.getFactory();
        try {
            List<PolicyReference> policyRefs = c.getExtensibilityElements(PolicyReference.class);
            PolicyReference ref = null;
            if ((policyRefs != null) && (!policyRefs.isEmpty())) {
                ref = policyRefs.get(0);
                ref.getParent().removeExtensibilityElement(ref);
            }
            if (policyURI != null) {
                ref = (PolicyReference) wcf.create(c, PolicyQName.POLICYREFERENCE.getQName());
                ref.setPolicyURI(policyURI);
                c.addExtensibilityElement(ref);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    /* Returns policy attached to a wsdl component */
    static Policy getPolicyForElement(WSDLComponent c) {
        if (c == null) {
            return null;
        }
        WSDLModel model = c.getModel();
        if (model != null) {
            String policyUri = getPolicyUriForElement(c);
            Definitions d = model.getDefinitions();
            if ((d != null) && (policyUri != null)) {
                Policy p = getPolicyForPolicyUri(policyUri, d);
                return p;
            }
        }
        return null;
    }

    public static boolean isSharedPolicy(WSDLComponent c) {
        if ((c instanceof BindingOutput) || (c instanceof BindingInput) || (c instanceof BindingFault)) {
            Policy p = getPolicyForElement(c);
            String pUri = p.getID();
            return isSharedPolicy(pUri,(Binding) c.getParent().getParent());
        }
        throw new IllegalArgumentException("Not allowed type: " + c);
    }
    
    /* Returns policy attached to a wsdl component */
    private static boolean isSharedPolicy(String policyName, Binding b) {
        if (b == null) {
            return false;
        }
        WSDLModel model = b.getModel();
        if (model != null) {
            Definitions d = model.getDefinitions();
            if (d != null) {
                Policy p = getPolicyForPolicyUri(policyName, d);
                Collection<BindingOperation> bOps = b.getBindingOperations();
                int counter = 0;
                for (BindingOperation bO : bOps) {
                    BindingInput bInput = bO.getBindingInput();
                    BindingOutput bOutput = bO.getBindingOutput();
                    String poliUriInput = getPolicyUriForElement(bInput);
                    String poliUriOutput = getPolicyUriForElement(bOutput);
                    if (policyName.equals(poliUriInput) || policyName.equals(poliUriOutput)) {
                        counter += 1;
                    }
                    if (counter > 1) {  // there are 2 policy references referencing the same policy
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    static void removePolicyForElement(WSDLComponent c) {
        WSDLModel model = c.getModel();
        if (model != null) {
            String policyUri = getPolicyUriForElement(c);
            Definitions d = model.getDefinitions();

            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }

            try {
                if ((d != null) && (policyUri != null)) {
                    Policy p = getPolicyForPolicyUri(policyUri, d);
                    if (p != null) {
                        p.getParent().removeExtensibilityElement(p);
                    }
                    List<PolicyReference> extPRefElems = c.getExtensibilityElements(PolicyReference.class);
                    if ((extPRefElems != null) && (!extPRefElems.isEmpty())) {
                        PolicyReference pref = extPRefElems.get(0);
                        pref.getParent().removeExtensibilityElement(pref);
                    }
                }
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
    }

    public static Binding getBinding(WSDLModel model, String bindingName) {
        Binding b = model.findComponentByName(bindingName, Binding.class);
        if (b == null) {
            Collection<Import> imports = model.getDefinitions().getImports();
            for (Import i : imports) {
                WSDLModel importedModel;
                try {
                    importedModel = i.getImportedWSDLModel();
                    return getBinding(importedModel, bindingName);
                } catch (CatalogModelException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return b;
    }

    /**
     *  Creates element with QName qname, of type cl, under wsdlcomponent c and returns it; if such element already exists, 
     * returns the existing element
     */
    public static @SuppressWarnings("unchecked")
    <T extends WSDLComponent> T createElement(WSDLComponent c, QName qname, Class cl, boolean withPolicy) {
        if (c == null) {
            return null;
        }

        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (withPolicy) {
                c = createElement(c, PolicyQName.POLICY.getQName(), Policy.class, false);
            }
            List<T> ts = c.getExtensibilityElements(cl);
            T t = null;
            if ((ts == null) || (ts.isEmpty())) {
                t = (T) wcf.create(c, qname);
                c.addExtensibilityElement((ExtensibilityElement) t);
            } else {
                t = ts.get(0);
            }
            return t;
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    /** Removes first element of class cl from under component c
     */
    static void removeElement(WSDLComponent c, Class cl, boolean underPolicy) {
        if (c == null) {
            return;
        }

        WSDLModel model = c.getModel();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (underPolicy) {
                List<Policy> policies = c.getExtensibilityElements(Policy.class);
                if ((policies != null) && (!policies.isEmpty())) {
                    c = policies.get(0);
                }
            }
            @SuppressWarnings("unchecked")
            List<ExtensibilityElement> l = c.getExtensibilityElements(cl);
            if ((l != null) && (!l.isEmpty())) {
                ExtensibilityElement tok = l.get(0);
                tok.getParent().removeExtensibilityElement(tok);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    static void removeElement(WSDLComponent c) {
        if (c == null) {
            return;
        }
        WSDLModel model = c.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            c.getParent().removeExtensibilityElement((ExtensibilityElement) c);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    static void cleanPolicies(WSDLComponent c) {
        Policy p = getPolicyForElement(c);
        if ((p != null) && (isEmpty(p))) {
            removePolicyForElement(c);
        }
    }

    /**
     * policy is empty if it contains only policy/all/exactlyone elements 
     * comp must be non-null
     */
    private static boolean isEmpty(WSDLComponent comp) {
        List<WSDLComponent> children = comp.getChildren();
        for (WSDLComponent c : children) {
            if ((c instanceof Policy) ||
                    (c instanceof All) || (c instanceof ExactlyOne)) {
                return isEmpty(c);
            }
            if (!(c instanceof Addressing10WsdlUsingAddressing)) {
                return false;
            }
        }
        return true;
    }
}
