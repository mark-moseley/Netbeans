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

package org.netbeans.modules.xml.wsdlextui.template.http;

import org.netbeans.modules.xml.wsdlextui.template.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPAddress;

/**
 *
 * @author radval
 */
public class HttpBindingPostProcessor {
    
    private String mWsdlTargetNamespace;
    
    private static final String SOAP_LOCATION_PPREFIX = "http://localhost:${HttpDefaultPort}/";
    
    /** Creates a new instance of SoapBindingPostProcessor */
    public HttpBindingPostProcessor() {
    }
    
    public void postProcess(String wsdlTargetNamespace, Port port) {
        this.mWsdlTargetNamespace = wsdlTargetNamespace;
        
        List<ExtensibilityElement> ees =  port.getExtensibilityElements();
        for (ExtensibilityElement ee : ees) {
            if(ee instanceof HTTPAddress) {
                HTTPAddress address = (HTTPAddress) ee;
                WSDLComponent parent = port.getParent();
                if(parent != null && parent instanceof Service) {
                    Service service = (Service) parent;
                    address.setLocation(SOAP_LOCATION_PPREFIX + service.getName() + "/" + port.getName() );
                }
            }
        }
    }
}
