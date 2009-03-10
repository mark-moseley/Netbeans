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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wsdlextui.template.soap12;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Address;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Fault;

/**
 * @author Sujit Biswas
 *
 */
public class Soap12BindingPostProcessor {
    
    private String mWsdlTargetNamespace;
    
    private static final String SOAP_LOCATION_PPREFIX = "http://localhost:${HttpDefaultPort}/";
    
    /** Creates a new instance of SoapBindingPostProcessor */
    public Soap12BindingPostProcessor() {
    }
    
    public void postProcess(String wsdlTargetNamespace, Port port) {
        this.mWsdlTargetNamespace = wsdlTargetNamespace;
        
        List<ExtensibilityElement> ees =  port.getExtensibilityElements();
        Iterator<ExtensibilityElement> it = ees.iterator();
        
        while(it.hasNext()) {
            ExtensibilityElement ee = it.next();
            if(ee instanceof SOAP12Address) {
                SOAP12Address soapAddress = (SOAP12Address) ee;
                WSDLComponent parent = port.getParent();
                if(parent != null && parent instanceof Service) {
                    Service service = (Service) parent;
                    soapAddress.setLocation(SOAP_LOCATION_PPREFIX + service.getName() + "/" + port.getName() );
                }
            }
        }
    }
    
    public void postProcess(String wsdlTargetNamespace, Binding binding) {
        this.mWsdlTargetNamespace = wsdlTargetNamespace;
        
        SOAP12Binding.Style style = null;
        
        List<ExtensibilityElement> ee = binding.getExtensibilityElements();
        Iterator<ExtensibilityElement> it = ee.iterator();
        while(it.hasNext()) {
            ExtensibilityElement e = it.next();
            if(e instanceof SOAP12Binding) {
                SOAP12Binding sBinding = (SOAP12Binding) e;
                style = sBinding.getStyle();
                break;
            }
        }
        
        if(style != null) {
            Collection<BindingOperation> bOps = binding.getBindingOperations();
            Iterator<BindingOperation> itBops = bOps.iterator();
            while(itBops.hasNext()) {
                BindingOperation op = itBops.next();
                processBindingOperation(style, op);
            }
        }
    }
    
    private void processBindingOperation(SOAP12Binding.Style style, BindingOperation bindingOperation) {
        BindingInput bIn = bindingOperation.getBindingInput();
        if (bIn != null) {
            processBindingOperationInput(style, bIn);
        }
        
        BindingOutput bOut = bindingOperation.getBindingOutput();
        if (bOut != null) {
            processBindingOperationOutput(style, bOut);
        }
        
        Collection<BindingFault> bFaults = bindingOperation.getBindingFaults();
        if (bFaults != null && !bFaults.isEmpty()) {
            Iterator<BindingFault> it = bFaults.iterator();
            while(it.hasNext()) {
                BindingFault bFault = it.next();
                processBindingOperationFault(style, bFault);
            }
        }
    }
    
    
    private void processBindingOperationInput(SOAP12Binding.Style style, BindingInput bIn) {
        if(style.equals(SOAP12Binding.Style.RPC)) {
            List<ExtensibilityElement> eeList = bIn.getExtensibilityElements();
            Iterator<ExtensibilityElement> it =  eeList.iterator();
            while(it.hasNext()) {
                ExtensibilityElement ee = it.next();
                if(ee instanceof SOAP12Body) {
                    SOAP12Body sBody = (SOAP12Body) ee;
                    sBody.setNamespace(mWsdlTargetNamespace);
                }
            }
        }
    }
    
    private void processBindingOperationOutput(SOAP12Binding.Style style, BindingOutput bOut ) {
        if(style.equals(SOAP12Binding.Style.RPC)) {
            List<ExtensibilityElement> eeList = bOut.getExtensibilityElements();
            Iterator<ExtensibilityElement> it =  eeList.iterator();
            while(it.hasNext()) {
                ExtensibilityElement ee = it.next();
                if(ee instanceof SOAP12Body) {
                    SOAP12Body sBody = (SOAP12Body) ee;
                    sBody.setNamespace(mWsdlTargetNamespace);
                }
            }
        }
    }
    
    private void processBindingOperationFault(SOAP12Binding.Style style, BindingFault bFault) {
        
            List<ExtensibilityElement> eeList = bFault.getExtensibilityElements();
            Iterator<ExtensibilityElement> it =  eeList.iterator();
            while(it.hasNext()) {
                ExtensibilityElement ee = it.next();
                if(ee instanceof SOAP12Fault) {
                    SOAP12Fault sFault = (SOAP12Fault) ee;
                    sFault.setName(bFault.getName());
                    if(style.equals(SOAP12Binding.Style.RPC)) {
                        sFault.setNamespace(mWsdlTargetNamespace);
                    }
                }
            }
        
    }
    
}
