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
package com.strikeiron.search;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.2-hudson-182-RC1
 * Generated source version: 2.1
 * 
 */
@WebService(name = "SISearchServiceSoap", targetNamespace = "http://www.strikeiron.com")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface SISearchServiceSoap {


    /**
     * Provides an array of MarketPlaceService objects
     * 
     * @param useCustomWSDL
     * @param sortBy
     * @param searchTerm
     * @param authenticationStyle
     * @return
     *     returns com.strikeiron.search.SearchOutPut
     */
    @WebMethod(operationName = "Search", action = "http://www.strikeiron.com/Search")
    @WebResult(name = "SearchResult", targetNamespace = "http://www.strikeiron.com")
    @RequestWrapper(localName = "Search", targetNamespace = "http://www.strikeiron.com", className = "com.strikeiron.search.Search")
    @ResponseWrapper(localName = "SearchResponse", targetNamespace = "http://www.strikeiron.com", className = "com.strikeiron.search.SearchResponse")
    public SearchOutPut search(
        @WebParam(name = "SearchTerm", targetNamespace = "http://www.strikeiron.com")
        String searchTerm,
        @WebParam(name = "SortBy", targetNamespace = "http://www.strikeiron.com")
        SORTBY sortBy,
        @WebParam(name = "UseCustomWSDL", targetNamespace = "http://www.strikeiron.com")
        boolean useCustomWSDL,
        @WebParam(name = "AuthenticationStyle", targetNamespace = "http://www.strikeiron.com")
        AUTHENTICATIONSTYLE authenticationStyle);

    /**
     * Return  all pricing for the specified StrikeIron web service
     * 
     * @param webServiceID
     * @return
     *     returns com.strikeiron.search.GetPricingOutPut
     */
    @WebMethod(operationName = "GetPricing", action = "http://www.strikeiron.com/GetPricing")
    @WebResult(name = "GetPricingResult", targetNamespace = "http://www.strikeiron.com")
    @RequestWrapper(localName = "GetPricing", targetNamespace = "http://www.strikeiron.com", className = "com.strikeiron.search.GetPricing")
    @ResponseWrapper(localName = "GetPricingResponse", targetNamespace = "http://www.strikeiron.com", className = "com.strikeiron.search.GetPricingResponse")
    public GetPricingOutPut getPricing(
        @WebParam(name = "WebServiceID", targetNamespace = "http://www.strikeiron.com")
        int webServiceID);

    /**
     * Get all statuses this service might return
     * 
     * @return
     *     returns com.strikeiron.search.StatusCodeOutput
     */
    @WebMethod(operationName = "GetAllStatuses", action = "http://www.strikeiron.com/GetAllStatuses")
    @WebResult(name = "GetAllStatusesResult", targetNamespace = "http://www.strikeiron.com")
    @RequestWrapper(localName = "GetAllStatuses", targetNamespace = "http://www.strikeiron.com", className = "com.strikeiron.search.GetAllStatuses")
    @ResponseWrapper(localName = "GetAllStatusesResponse", targetNamespace = "http://www.strikeiron.com", className = "com.strikeiron.search.GetAllStatusesResponse")
    public StatusCodeOutput getAllStatuses();

    /**
     * Get information about the web service
     * 
     * @return
     *     returns com.strikeiron.search.ServiceInfoOutput
     */
    @WebMethod(operationName = "GetServiceInfo", action = "http://www.strikeiron.com/GetServiceInfo")
    @WebResult(name = "GetServiceInfoResult", targetNamespace = "http://www.strikeiron.com")
    @RequestWrapper(localName = "GetServiceInfo", targetNamespace = "http://www.strikeiron.com", className = "com.strikeiron.search.GetServiceInfo")
    @ResponseWrapper(localName = "GetServiceInfoResponse", targetNamespace = "http://www.strikeiron.com", className = "com.strikeiron.search.GetServiceInfoResponse")
    public ServiceInfoOutput getServiceInfo();

    /**
     * 
     */
    @WebMethod(operationName = "GetRemainingHits", action = "http://ws.strikeiron.com/StrikeIron/MarketplaceSearch/SISearchService/GetRemainingHits")
    @RequestWrapper(localName = "GetRemainingHits", targetNamespace = "http://ws.strikeiron.com", className = "com.strikeiron.search.GetRemainingHits")
    @ResponseWrapper(localName = "GetRemainingHitsResponse", targetNamespace = "http://ws.strikeiron.com", className = "com.strikeiron.search.GetRemainingHitsResponse")
    public void getRemainingHits();

}
