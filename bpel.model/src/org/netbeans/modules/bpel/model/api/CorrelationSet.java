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

import java.util.List;

import org.netbeans.modules.bpel.model.api.references.BpelReferenceable;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;

/**
 * <p>
 * Java class for tCorrelationSet complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 *   &lt;complexType name=&quot;tCorrelationSet&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *         &lt;attribute name=&quot;name&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *         &lt;attribute name=&quot;properties&quot; use=&quot;required&quot;&gt;
 *           &lt;simpleType&gt;
 *             &lt;list itemType=&quot;{http://www.w3.org/2001/XMLSchema}QName&quot; /&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/attribute&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface CorrelationSet extends ExtensibleElements,
        NamedElement, BpelReferenceable, ReferenceCollection
{

    /**
     * properties attribute name.
     */
    String PROPERTIES = "properties";   // NOI18N

    /**
     * Returns list of properties. This list could be used for
     * adding/setting/removing property from correlationSet.
     * 
     * @return list of properties.
     */
    List<WSDLReference<CorrelationProperty>> getProperties();

    /**
     * Set list of properties.
     * 
     * @param list
     *            list for set.
     */
    void setProperties( List<WSDLReference<CorrelationProperty>> list );

}
