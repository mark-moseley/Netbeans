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

package org.netbeans.modules.xslt.model;

import org.netbeans.modules.xml.xam.locator.CatalogModelException;


/**
 * "href" attribute holder.
 * 
 * @author ads
 *
 */
public interface XslModelReference extends XslComponent {

    String HREF     = "href";                       // NOI18N
    
    /**
     * @return "href" attribute value.
     */
    String getHref();
    

    /**
     * Set new "href" attribute value.
     * @param href new attribute value
     */
    void setHref( String href );
    
    /**
     * obtain the model for the referenced xslt. 
     * 
     * @throws CatalogModelException if the referenced model cannot
     * be created.
     */
    XslModel resolveReferencedModel() throws CatalogModelException;
}
