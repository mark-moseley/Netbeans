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


package org.netbeans.modules.deployment.deviceanywhere.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import org.openide.util.Exceptions;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1-04/12/2007 02:26 PM(vivekp)-RC1
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "ApplicationAPIService", targetNamespace = "http://services.mc.com")
public class ApplicationAPIService
    extends Service
{
    private static URL url;
    
    static {
        try {
            String urlKey = "http://www.deviceanywhere.com/axis/services/ApplicationAPI";
            try {
                ResourceBundle bundle = ResourceBundle.getBundle("org/netbeans/modules/deployment/deviceanywhere/service/Bundle"); //NOI18N
                urlKey = bundle.getString("service_url");
            } catch (Exception exception) {
                //ignore
            }

            //lookup for replacement
            String newUrl = System.getProperty("deviceanywhere.service.url");
            if (newUrl != null) {
                urlKey = newUrl;
            }
            url = new URL(urlKey + "?wsdl");
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    //    private static String[] SERVICES = new String[]{
//        "http://www.deviceanywhere.com/axis/services/ApplicationAPI", //default
//        "http://www.deviceanywhere.com/vdl/sprint/axis/services/ApplicationAPI", //vdl
//        "http://mcdemo5.mobilecomplete.com/axis/services/ApplicationAPI" //test
//    };

    public ApplicationAPIService() throws MalformedURLException {
        super(url, new QName("http://services.mc.com", "ApplicationAPIService")); //NOI18N
    }

    /**
     * 
     * @return
     *     returns ApplicationAPI
     */
    @WebEndpoint(name = "ApplicationAPI")
    public ApplicationAPI getApplicationAPI() {
        return (ApplicationAPI)super.getPort(new QName("http://services.mc.com", "ApplicationAPI"), ApplicationAPI.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ApplicationAPI
     */
    @WebEndpoint(name = "ApplicationAPI")
    public ApplicationAPI getApplicationAPI(WebServiceFeature... features) {
        return (ApplicationAPI)super.getPort(new QName("http://services.mc.com", "ApplicationAPI"), ApplicationAPI.class, features);
    }

}
