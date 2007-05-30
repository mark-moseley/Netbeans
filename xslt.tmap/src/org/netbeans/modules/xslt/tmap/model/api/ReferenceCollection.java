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
package org.netbeans.modules.xslt.tmap.model.api;

import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.Reference;

/**
 * This interface should be implemented by entities that refers to some other
 * entities. They are contain reference to other OM element and when this
 * element is changed in some way they should be changed. e.g. service contains
 * reference to PartnerLInkType. 
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface ReferenceCollection {
    
    /**
     * 
     * @return array of the references
     */
    Reference[] getReferences();
    
    /**
     * Creates reference to specified WSDL OM referenceable element.
     * @param <T> Referencable OM class.
     * @param target Object for which needs to create reference.
     * @param type Type of referenceable object.
     * @return Reference to <code>target</code> object.
     */
    <T extends ReferenceableWSDLComponent> WSDLReference<T> createWSDLReference(
            T target, Class<T> type );

}
