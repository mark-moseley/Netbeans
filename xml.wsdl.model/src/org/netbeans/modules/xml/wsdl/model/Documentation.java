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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model;

import org.w3c.dom.Element;

/**
 * @author rico
 * @author Nam Nguyen
 * Represents the documentation element in a WSDL document
 */
public interface Documentation extends WSDLComponent{
    public static final String CONTENT_PROPERTY = "content";

    /**
     * @return text representation of the documentation element content.
     */
    String getTextContent();

    /**
     * Set the documentation element content to a text node with the given
     * string value.
     */
    void setTextContent(String content);
    
    /**
     * @return a mutable clone of the documentation element itself.
     */
    Element getDocumentationElement();
    
    /**
     * Sets the documentation element to the given element.
     */
    void setDocumentationElement(Element documentationElement);
}
