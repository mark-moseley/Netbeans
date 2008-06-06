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

package org.netbeans.modules.iep.model;

import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;


/**
 *
 * @author iep
 * Base interface of all IEP entity
 */
public interface IEPComponent extends DocumentComponent<IEPComponent> {
 
    /**
     * @return WSDL model.
     */
    IEPModel getModel();
    
    void accept(IEPVisitor visitor);
    
//    /**
//     * Creates a global reference to the given target WSDL component.
//     * @param target the target WSDLComponent
//     * @param type actual type of the target
//     * @return the global reference.
//     */
//    <T extends ReferenceableIEPComponent> NamedComponentReference<T> createReferenceTo(T target, Class<T> type);
    
    /**
     * Returns map of attribute names and string values.
     */
    Map<QName,String> getAttributeMap();
    
    IEPComponent createChild (Element childEl);
    
    void removeChild(IEPComponent child);
    
//    <T extends ReferenceableWSDLComponent> NamedComponentReference<T> createReferenceTo(T target, Class<T> type);
}
