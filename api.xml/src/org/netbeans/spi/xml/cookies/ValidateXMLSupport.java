/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.xml.cookies;

import org.openide.loaders.DataObject;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.parsers.ProcessorNotifier;

/**
 * <code>ValidateXMLCookie</code> and <code>CheckXMLCookie</code>implementation
 * support simplifing cookie providers based on <code>DataObject</code>s
 * representing XML documents and entities.
 * <p>
 * <b>Primary use case</b> in a DataObject subclass (which primary file is XML):
 * <pre>
 *   CookieSet cookies = getCookieSet();
 *   ValidateXMLSupport cookieImpl = new ValidateXMLSupport(this);
 *   cookies.add(cookieImpl);
 * </pre>
 * <p>
 * <b>Secondary use case:</b> Subclasses can customize the class by customization
 * protected methods. The customized subclass can be used according to
 * primary use case.
 *
 * @author Petr Kuzel
 * @deprecated XML tools SPI candidate
 */
public class ValidateXMLSupport extends CheckXMLSupport implements ValidateXMLCookie {
            
    /** 
     * Create new ValidateXMLSupport for given data object 
     * using DOCUMENT_MODE check strategy for CheckXMLCookie.
     * @param dataObject supported data object
     */    
    public ValidateXMLSupport(DataObject dataObject) {
        super(dataObject);
    }
    
    /** 
     * Create new CheckXMLSupport for given data object
     * @param dataObject supported data object
     * @param checkStrategy one of <code>CheckXMLSupport.*_MODE</code> constants
     */    
    private ValidateXMLSupport(DataObject dataObject, int checkStrategy) {
        super(dataObject, checkStrategy);
    }

    // inherit JavaDoc
    public boolean validateXML(ProcessorNotifier l) {
        return super.validateXML(l);
    }
}

