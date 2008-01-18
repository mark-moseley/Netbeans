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
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, v2.0-06/22/2005 01:29 PM(ryans)-EA2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2005.09.05 at 07:05:33 PM MSD
//
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.wsdl.model.Message;

/**
 * <p>
 * Java class for tCatch complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 *   &lt;complexType name=&quot;tCatch&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tActivityOrCompensateContainer&quot;&gt;
 *         &lt;attribute name=&quot;faultElement&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}QName&quot; /&gt;
 *         &lt;attribute name=&quot;faultMessageType&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}QName&quot; /&gt;
 *         &lt;attribute name=&quot;faultName&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}QName&quot; /&gt;
 *         &lt;attribute name=&quot;faultVariable&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface Catch extends FaultHandler, CompensatableActivityHolder, 
    FaultNameReference, VariableDeclaration, VariableDeclarationScope, 
    ReferenceCollection
{

    String FAULT_MESSAGE_TYPE = "faultMessageType";     // NOI18N

    String FAULT_ELEMENT = "faultElement";              // NOI18N

    /**
     * faultVariable attribute name.
     */
    String FAULT_VARIABLE = "faultVariable";            // NOI18N

    /**
     * Removes fault name atribute.
     */
    void removeFaultName();

    /**
     * Getter for "faultMessageType" attribute value.
     * 
     * @return reference to Message object in WSDL model.
     */
    WSDLReference<Message> getFaultMessageType();

    /**
     * Setter for "faultMessageType" attribute value .
     * 
     * @param message
     *            reference to Message object in WSDL model.
     */
    void setFaultMessageType( WSDLReference<Message> message );

    /**
     * Removes "faultMessageType" attribute.
     */
    void removeFaultMessageType();

    /**
     * Getter for "faultElement" attribute value. // *
     * 
     * @return reference to GlobalElement object in schema model.
     */
    SchemaReference<GlobalElement> getFaultElement();

    /**
     * Setter for "faultElement" attribute value.
     * 
     * @param element
     *            reference to GlobalElement object in schema model.
     */
    void setFaultElement( SchemaReference<GlobalElement> element );

    /**
     * Removes "faultElement" attribute.
     */
    void removeFaultElement();

    /**
     * Gets the value of the faultVariable property. 
     * The faultVariable attribute defines local variable for catch.
     * 
     * @return possible object is String.
     */
    String getFaultVariable();

    /**
     * Sets the value of the faultVariable property.
     * 
     * @param value
     *            allowed object is String.
     * @throws VetoException {@link VetoException } will be thrown 
     * if <code>value</code> is not accaptable here.
     */
    void setFaultVariable( String value ) throws VetoException;

    /**
     * Removes faultVariable attribure.
     */
    void removeFaultVariable();
}
