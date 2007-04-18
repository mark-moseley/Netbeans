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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * 	The message-destination-linkType is used to link a message
 * 	destination reference or message-driven bean to a message
 * 	destination.
 * 
 * 	The Assembler sets the value to reflect the flow of messages
 * 	between producers and consumers in the application.
 * 
 * 	The value must be the message-destination-name of a message
 * 	destination in the same Deployment File or in another
 * 	Deployment File in the same J2EE application unit.
 * 
 * 	Alternatively, the value may be composed of a path name
 * 	specifying a Deployment File containing the referenced
 * 	message destination with the message-destination-name of the
 * 	destination appended and separated from the path name by
 * 	"#". The path name is relative to the Deployment File
 * 	containing Deployment Component that is referencing the
 * 	message destination.  This allows multiple message
 * 	destinations with the same name to be uniquely identified.
 * 
 *       
 * 
 * <p>Java class for message-destination-linkType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="message-destination-linkType">
 *   &lt;simpleContent>
 *     &lt;restriction base="&lt;http://java.sun.com/xml/ns/j2ee>string">
 *     &lt;/restriction>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-destination-linkType")
public class MessageDestinationLinkType
    extends String
{


}
