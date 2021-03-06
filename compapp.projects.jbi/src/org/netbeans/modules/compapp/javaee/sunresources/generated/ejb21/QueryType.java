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
// Generated on: 2006.12.09 at 06:25:52 PM PST 
//


package org.netbeans.modules.compapp.javaee.sunresources.generated.ejb21;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 
 * 	The queryType defines a finder or select
 * 	query. It contains
 * 	    - an optional description of the query
 * 	    - the specification of the finder or select
 * 	      method it is used by
 * 		- an optional specification of the result type
 * 		  mapping, if the query is for a select method
 * 		  and entity objects are returned.
 * 		- the EJB QL query string that defines the query.
 * 
 * 	Queries that are expressible in EJB QL must use the ejb-ql
 * 	element to specify the query. If a query is not expressible
 * 	in EJB QL, the description element should be used to
 * 	describe the semantics of the query and the ejb-ql element
 * 	should be empty.
 * 
 * 	The result-type-mapping is an optional element. It can only
 * 	be present if the query-method specifies a select method
 * 	that returns entity objects.  The default value for the
 * 	result-type-mapping element is "Local".
 * 
 *       
 * 
 * <p>Java class for queryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="queryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/j2ee}descriptionType" minOccurs="0"/>
 *         &lt;element name="query-method" type="{http://java.sun.com/xml/ns/j2ee}query-methodType"/>
 *         &lt;element name="result-type-mapping" type="{http://java.sun.com/xml/ns/j2ee}result-type-mappingType" minOccurs="0"/>
 *         &lt;element name="ejb-ql" type="{http://java.sun.com/xml/ns/j2ee}xsdStringType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queryType", propOrder = {
    "description",
    "queryMethod",
    "resultTypeMapping",
    "ejbQl"
})
public class QueryType {

    protected DescriptionType description;
    @XmlElement(name = "query-method", required = true)
    protected QueryMethodType queryMethod;
    @XmlElement(name = "result-type-mapping")
    protected ResultTypeMappingType resultTypeMapping;
    @XmlElement(name = "ejb-ql", required = true)
    protected XsdStringType ejbQl;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected java.lang.String id;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link DescriptionType }
     *     
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptionType }
     *     
     */
    public void setDescription(DescriptionType value) {
        this.description = value;
    }

    /**
     * Gets the value of the queryMethod property.
     * 
     * @return
     *     possible object is
     *     {@link QueryMethodType }
     *     
     */
    public QueryMethodType getQueryMethod() {
        return queryMethod;
    }

    /**
     * Sets the value of the queryMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryMethodType }
     *     
     */
    public void setQueryMethod(QueryMethodType value) {
        this.queryMethod = value;
    }

    /**
     * Gets the value of the resultTypeMapping property.
     * 
     * @return
     *     possible object is
     *     {@link ResultTypeMappingType }
     *     
     */
    public ResultTypeMappingType getResultTypeMapping() {
        return resultTypeMapping;
    }

    /**
     * Sets the value of the resultTypeMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultTypeMappingType }
     *     
     */
    public void setResultTypeMapping(ResultTypeMappingType value) {
        this.resultTypeMapping = value;
    }

    /**
     * Gets the value of the ejbQl property.
     * 
     * @return
     *     possible object is
     *     {@link XsdStringType }
     *     
     */
    public XsdStringType getEjbQl() {
        return ejbQl;
    }

    /**
     * Sets the value of the ejbQl property.
     * 
     * @param value
     *     allowed object is
     *     {@link XsdStringType }
     *     
     */
    public void setEjbQl(XsdStringType value) {
        this.ejbQl = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

}
