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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;

/**
 * @author ads
 *         <p>
 *         Java class for tToPart complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tToPart&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *         &lt;attribute name=&quot;fromVariable&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *         &lt;attribute name=&quot;part&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *       &lt;/restriction&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface ToPart extends BpelEntity, PartReference, ReferenceCollection {

    /**
     * name of "fromVariable" attribute.
     */
    String FROM_VARIABLE = "fromVariable";      // NOI18N

    /**
     * @return reference to variable ( "fromVariable" attribute ).
     */
    BpelReference<VariableDeclaration> getFromVariable();

    /**
     * Set new reference to variable ( "fromVariable" attribute ).
     * 
     * @param variable
     *            New reference to variable .
     */
    void setFromVariable( BpelReference<VariableDeclaration> variable );

}
