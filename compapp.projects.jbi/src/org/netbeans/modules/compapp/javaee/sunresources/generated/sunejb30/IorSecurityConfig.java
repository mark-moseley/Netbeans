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
    "transportConfig",
    "asContext",
    "sasContext"
})
@XmlRootElement(name = "ior-security-config")
public class IorSecurityConfig {

    @XmlElement(name = "transport-config")
    protected TransportConfig transportConfig;
    @XmlElement(name = "as-context")
    protected AsContext asContext;
    @XmlElement(name = "sas-context")
    protected SasContext sasContext;

    /**
     * Gets the value of the transportConfig property.
     * 
     * @return
     *     possible object is
     *     {@link TransportConfig }
     *     
     */
    public TransportConfig getTransportConfig() {
        return transportConfig;
    }

    /**
     * Sets the value of the transportConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransportConfig }
     *     
     */
    public void setTransportConfig(TransportConfig value) {
        this.transportConfig = value;
    }

    /**
     * Gets the value of the asContext property.
     * 
     * @return
     *     possible object is
     *     {@link AsContext }
     *     
     */
    public AsContext getAsContext() {
        return asContext;
    }

    /**
     * Sets the value of the asContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link AsContext }
     *     
     */
    public void setAsContext(AsContext value) {
        this.asContext = value;
    }

    /**
     * Gets the value of the sasContext property.
     * 
     * @return
     *     possible object is
     *     {@link SasContext }
     *     
     */
    public SasContext getSasContext() {
        return sasContext;
    }

    /**
     * Sets the value of the sasContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link SasContext }
     *     
     */
    public void setSasContext(SasContext value) {
        this.sasContext = value;
    }

}
