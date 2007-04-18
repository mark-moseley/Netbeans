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
// Generated on: 2006.12.09 at 06:26:06 PM PST 
//


package org.netbeans.modules.compapp.javaee.sunresources.generated.jaxrpcmapping11;

import java.util.ArrayList;
import java.util.List;
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
 * 	The java-xml-type-mapping element contains a java-type that is the
 * 	fully qualified name of the Java class, primitive type, or array
 * 	type, QName of the XML root type or anonymous type, the WSDL type
 * 	scope the QName applies to and the set of variable mappings for
 * 	each public variable within the Java class.
 * 
 * 	Used in: java-wsdl-mapping
 * 
 *       
 * 
 * <p>Java class for java-xml-type-mappingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="java-xml-type-mappingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="java-type" type="{http://java.sun.com/xml/ns/j2ee}java-typeType"/>
 *         &lt;choice>
 *           &lt;element name="root-type-qname" type="{http://java.sun.com/xml/ns/j2ee}xsdQNameType"/>
 *           &lt;element name="anonymous-type-qname" type="{http://java.sun.com/xml/ns/j2ee}string"/>
 *         &lt;/choice>
 *         &lt;element name="qname-scope" type="{http://java.sun.com/xml/ns/j2ee}qname-scopeType"/>
 *         &lt;element name="variable-mapping" type="{http://java.sun.com/xml/ns/j2ee}variable-mappingType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "java-xml-type-mappingType", propOrder = {
    "javaType",
    "rootTypeQname",
    "anonymousTypeQname",
    "qnameScope",
    "variableMapping"
})
public class JavaXmlTypeMappingType {

    @XmlElement(name = "java-type", required = true)
    protected JavaTypeType javaType;
    @XmlElement(name = "root-type-qname")
    protected XsdQNameType rootTypeQname;
    @XmlElement(name = "anonymous-type-qname")
    protected org.netbeans.modules.compapp.javaee.sunresources.generated.jaxrpcmapping11.String anonymousTypeQname;
    @XmlElement(name = "qname-scope", required = true)
    protected QnameScopeType qnameScope;
    @XmlElement(name = "variable-mapping")
    protected List<VariableMappingType> variableMapping;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected java.lang.String id;

    /**
     * Gets the value of the javaType property.
     * 
     * @return
     *     possible object is
     *     {@link JavaTypeType }
     *     
     */
    public JavaTypeType getJavaType() {
        return javaType;
    }

    /**
     * Sets the value of the javaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaTypeType }
     *     
     */
    public void setJavaType(JavaTypeType value) {
        this.javaType = value;
    }

    /**
     * Gets the value of the rootTypeQname property.
     * 
     * @return
     *     possible object is
     *     {@link XsdQNameType }
     *     
     */
    public XsdQNameType getRootTypeQname() {
        return rootTypeQname;
    }

    /**
     * Sets the value of the rootTypeQname property.
     * 
     * @param value
     *     allowed object is
     *     {@link XsdQNameType }
     *     
     */
    public void setRootTypeQname(XsdQNameType value) {
        this.rootTypeQname = value;
    }

    /**
     * Gets the value of the anonymousTypeQname property.
     * 
     * @return
     *     possible object is
     *     {@link org.netbeans.modules.compapp.javaee.sunresources.generated.jaxrpcmapping11.String }
     *     
     */
    public org.netbeans.modules.compapp.javaee.sunresources.generated.jaxrpcmapping11.String getAnonymousTypeQname() {
        return anonymousTypeQname;
    }

    /**
     * Sets the value of the anonymousTypeQname property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.netbeans.modules.compapp.javaee.sunresources.generated.jaxrpcmapping11.String }
     *     
     */
    public void setAnonymousTypeQname(org.netbeans.modules.compapp.javaee.sunresources.generated.jaxrpcmapping11.String value) {
        this.anonymousTypeQname = value;
    }

    /**
     * Gets the value of the qnameScope property.
     * 
     * @return
     *     possible object is
     *     {@link QnameScopeType }
     *     
     */
    public QnameScopeType getQnameScope() {
        return qnameScope;
    }

    /**
     * Sets the value of the qnameScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link QnameScopeType }
     *     
     */
    public void setQnameScope(QnameScopeType value) {
        this.qnameScope = value;
    }

    /**
     * Gets the value of the variableMapping property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variableMapping property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariableMapping().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VariableMappingType }
     * 
     * 
     */
    public List<VariableMappingType> getVariableMapping() {
        if (variableMapping == null) {
            variableMapping = new ArrayList<VariableMappingType>();
        }
        return this.variableMapping;
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
