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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.12.09 at 06:25:55 PM PST 
//


package org.netbeans.modules.compapp.javaee.sunresources.generated.sunejb30;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "webserviceDescriptionName",
    "wsdlPublishLocation"
})
@XmlRootElement(name = "webservice-description")
public class WebserviceDescription {

    @XmlElement(name = "webservice-description-name", required = true)
    protected String webserviceDescriptionName;
    @XmlElement(name = "wsdl-publish-location")
    protected String wsdlPublishLocation;

    /**
     * Gets the value of the webserviceDescriptionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebserviceDescriptionName() {
        return webserviceDescriptionName;
    }

    /**
     * Sets the value of the webserviceDescriptionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebserviceDescriptionName(String value) {
        this.webserviceDescriptionName = value;
    }

    /**
     * Gets the value of the wsdlPublishLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWsdlPublishLocation() {
        return wsdlPublishLocation;
    }

    /**
     * Sets the value of the wsdlPublishLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWsdlPublishLocation(String value) {
        this.wsdlPublishLocation = value;
    }

}
