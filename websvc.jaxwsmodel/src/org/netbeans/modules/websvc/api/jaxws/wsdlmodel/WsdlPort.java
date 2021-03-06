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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mkuchtiak
 */
public class WsdlPort {
    public static final String STYLE_DOCUMENT="document"; //NOI18N
    public static final String STYLE_RPC="rpc"; //NOI18N
    public static final String SOAP_VERSION_11="http://schemas.xmlsoap.org/wsdl/soap/http"; //NOI18N
    public static final String SOAP_VERSION_12="http://www.w3.org/2003/05/soap/bindings/HTTP/"; //NOI18N
    
    private Port port;
    private String soapVersion = SOAP_VERSION_11;
    
    /** Creates a new instance of WsdlPort */
    WsdlPort(Port port) {
        this.port=port;
    }
    
    public Object /*com.sun.tools.ws.processor.model.Port*/ getInternalJAXWSPort() {
        return port;
    }
    
    public List<WsdlOperation> getOperations() {
        List<WsdlOperation> wsdlOperations = new ArrayList<WsdlOperation> ();
        if (port==null) return wsdlOperations;
        List<Operation> operations = port.getOperations();
        for (Operation op:operations)
            wsdlOperations.add(new WsdlOperation(op));
        return wsdlOperations;
    }
    
    public String getName() {
        if (port==null) return null;
        return port.getName().getLocalPart();
    }
    
    public String getNamespaceURI() {
        return port.getName().getNamespaceURI();
    }
    
    public String getJavaName() {
        if (port==null) return null;
        return port.getJavaInterface().getName();
    }
    
    public String getPortGetter() {
        if (port==null) return null;
        return port.getPortGetter();
    }
    
    public String getSOAPVersion() {
        return soapVersion;
    }
    
    public void setSOAPVersion(String soapVersion) {
        this.soapVersion=soapVersion;
    }
    
    public String getStyle() {
        SOAPStyle style = port.getStyle();
        if (SOAPStyle.DOCUMENT.equals(style)) return STYLE_DOCUMENT;
        else if (SOAPStyle.RPC.equals(style)) return STYLE_RPC;
        return null;
    }
    
    public boolean isProvider(){
        return port.isProvider();
    }
    
    public String getAddress(){
        return port.getAddress();
    }
}
