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
// Generated on: 2006.12.09 at 06:25:52 PM PST 
//


package org.netbeans.modules.compapp.javaee.sunresources.generated.ejb21;

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
 * 
 * 	  The methodType is used to denote a method of an enterprise
 * 	  bean's home, component, and/or web service endpoint
 * 	  interface, or, in the case of a message-driven bean, the
 * 	  bean's message listener method, or a set of such
 * 	  methods. The ejb-name element must be the name of one of the
 * 	  enterprise beans declared in the deployment descriptor; the
 * 	  optional method-intf element allows to distinguish between a
 * 	  method with the same signature that is multiply defined
 * 	  across the home, component, and/or web service endpoint
 * 	  interfaces; the method-name element specifies the method
 * 	  name; and the optional method-params elements identify a
 * 	  single method among multiple methods with an overloaded
 * 	  method name.
 * 
 * 	  There are three possible styles of using methodType element
 * 	  within a method element:
 * 
 * 	  1.
 * 	  <method>
 * 	      <ejb-name>EJBNAME</ejb-name>
 * 	      <method-name>*</method-name>
 * 	  </method>
 * 
 * 	     This style is used to refer to all the methods of the
 * 	     specified enterprise bean's home, component, and/or web
 * 	     service endpoint interfaces.
 * 
 * 	  2.
 * 	  <method>
 * 	      <ejb-name>EJBNAME</ejb-name>
 * 	      <method-name>METHOD</method-name>
 * 	  </method>
 * 
 * 	     This style is used to refer to the specified method of
 * 	     the specified enterprise bean. If there are multiple
 * 	     methods with the same overloaded name, the element of
 * 	     this style refers to all the methods with the overloaded
 * 	     name.
 * 
 * 	  3.
 * 	  <method>
 * 	      <ejb-name>EJBNAME</ejb-name>
 * 	      <method-name>METHOD</method-name>
 * 	      <method-params>
 * 		  <method-param>PARAM-1</method-param>
 * 		  <method-param>PARAM-2</method-param>
 * 		  ...
 * 		  <method-param>PARAM-n</method-param>
 * 	      </method-params>
 * 	  </method>
 * 
 * 	     This style is used to refer to a single method within a
 * 	     set of methods with an overloaded name. PARAM-1 through
 * 	     PARAM-n are the fully-qualified Java types of the
 * 	     method's input parameters (if the method has no input
 * 	     arguments, the method-params element contains no
 * 	     method-param elements). Arrays are specified by the
 * 	     array element's type, followed by one or more pair of
 * 	     square brackets (e.g. int[][]). If there are multiple
 * 	     methods with the same overloaded name, this style refers
 * 	     to all of the overloaded methods.
 * 
 * 	  Examples:
 * 
 * 	  Style 1: The following method element refers to all the
 * 	  methods of the EmployeeService bean's home, component,
 * 	  and/or web service endpoint interfaces:
 * 
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-name>*</method-name>
 * 	  </method>
 * 
 * 	  Style 2: The following method element refers to all the
 * 	  create methods of the EmployeeService bean's home
 * 	  interface(s).
 * 
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-name>create</method-name>
 * 	  </method>
 * 
 * 	  Style 3: The following method element refers to the
 * 	  create(String firstName, String LastName) method of the
 * 	  EmployeeService bean's home interface(s).
 * 
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-name>create</method-name>
 * 	      <method-params>
 * 		  <method-param>java.lang.String</method-param>
 * 		  <method-param>java.lang.String</method-param>
 * 	      </method-params>
 * 	  </method>
 * 
 * 	  The following example illustrates a Style 3 element with
 * 	  more complex parameter types. The method
 * 	  foobar(char s, int i, int[] iar, mypackage.MyClass mycl,
 * 	  mypackage.MyClass[][] myclaar) would be specified as:
 * 
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-name>foobar</method-name>
 * 	      <method-params>
 * 		  <method-param>char</method-param>
 * 		  <method-param>int</method-param>
 * 		  <method-param>int[]</method-param>
 * 		  <method-param>mypackage.MyClass</method-param>
 * 		  <method-param>mypackage.MyClass[][]</method-param>
 * 	      </method-params>
 * 	  </method>
 * 
 * 	  The optional method-intf element can be used when it becomes
 * 	  necessary to differentiate between a method that is multiply
 * 	  defined across the enterprise bean's home, component, and/or
 * 	  web service endpoint interfaces with the same name and
 * 	  signature.
 * 
 * 	  For example, the method element
 * 
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-intf>Remote</method-intf>
 * 	      <method-name>create</method-name>
 * 	      <method-params>
 * 		  <method-param>java.lang.String</method-param>
 * 		  <method-param>java.lang.String</method-param>
 * 	      </method-params>
 * 	  </method>
 * 
 * 	  can be used to differentiate the create(String, String)
 * 	  method defined in the remote interface from the
 * 	  create(String, String) method defined in the remote home
 * 	  interface, which would be defined as
 * 
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-intf>Home</method-intf>
 * 	      <method-name>create</method-name>
 * 	      <method-params>
 * 		  <method-param>java.lang.String</method-param>
 * 		  <method-param>java.lang.String</method-param>
 * 	      </method-params>
 * 	  </method>
 * 
 * 	  and the create method that is defined in the local home
 * 	  interface which would be defined as
 * 
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-intf>LocalHome</method-intf>
 * 	      <method-name>create</method-name>
 * 	      <method-params>
 * 		  <method-param>java.lang.String</method-param>
 * 		  <method-param>java.lang.String</method-param>
 * 	      </method-params>
 * 	  </method>
 * 
 * 	  The method-intf element can be used with all th ree Styles
 * 	  of the method element usage. For example, the following
 * 	  method element example could be used to refer to all the
 * 	  methods of the EmployeeService bean's remote home interface.
 * 
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-intf>Home</method-intf>
 * 	      <method-name>*</method-name>
 * 	  </method>
 * 
 * 	  
 *       
 * 
 * <p>Java class for methodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="methodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/j2ee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/j2ee}ejb-nameType"/>
 *         &lt;element name="method-intf" type="{http://java.sun.com/xml/ns/j2ee}method-intfType" minOccurs="0"/>
 *         &lt;element name="method-name" type="{http://java.sun.com/xml/ns/j2ee}method-nameType"/>
 *         &lt;element name="method-params" type="{http://java.sun.com/xml/ns/j2ee}method-paramsType" minOccurs="0"/>
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
@XmlType(name = "methodType", propOrder = {
    "description",
    "ejbName",
    "methodIntf",
    "methodName",
    "methodParams"
})
public class MethodType {

    protected List<DescriptionType> description;
    @XmlElement(name = "ejb-name", required = true)
    protected EjbNameType ejbName;
    @XmlElement(name = "method-intf")
    protected MethodIntfType methodIntf;
    @XmlElement(name = "method-name", required = true)
    protected MethodNameType methodName;
    @XmlElement(name = "method-params")
    protected MethodParamsType methodParams;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected java.lang.String id;

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DescriptionType }
     * 
     * 
     */
    public List<DescriptionType> getDescription() {
        if (description == null) {
            description = new ArrayList<DescriptionType>();
        }
        return this.description;
    }

    /**
     * Gets the value of the ejbName property.
     * 
     * @return
     *     possible object is
     *     {@link EjbNameType }
     *     
     */
    public EjbNameType getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     * 
     * @param value
     *     allowed object is
     *     {@link EjbNameType }
     *     
     */
    public void setEjbName(EjbNameType value) {
        this.ejbName = value;
    }

    /**
     * Gets the value of the methodIntf property.
     * 
     * @return
     *     possible object is
     *     {@link MethodIntfType }
     *     
     */
    public MethodIntfType getMethodIntf() {
        return methodIntf;
    }

    /**
     * Sets the value of the methodIntf property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodIntfType }
     *     
     */
    public void setMethodIntf(MethodIntfType value) {
        this.methodIntf = value;
    }

    /**
     * Gets the value of the methodName property.
     * 
     * @return
     *     possible object is
     *     {@link MethodNameType }
     *     
     */
    public MethodNameType getMethodName() {
        return methodName;
    }

    /**
     * Sets the value of the methodName property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodNameType }
     *     
     */
    public void setMethodName(MethodNameType value) {
        this.methodName = value;
    }

    /**
     * Gets the value of the methodParams property.
     * 
     * @return
     *     possible object is
     *     {@link MethodParamsType }
     *     
     */
    public MethodParamsType getMethodParams() {
        return methodParams;
    }

    /**
     * Sets the value of the methodParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodParamsType }
     *     
     */
    public void setMethodParams(MethodParamsType value) {
        this.methodParams = value;
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
