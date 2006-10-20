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

package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.io.IOException;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.openide.ErrorManager;

public class ExtensibilityElementPropertyAdapter {
    private ExtensibilityElement element;
    private String attributeName;
    private String defaultValue; 
    private boolean supportsDefaultValue;
    
    public ExtensibilityElementPropertyAdapter(ExtensibilityElement element, String name) {
        this.element = element;
        this.attributeName = name;
    }
    
    public ExtensibilityElementPropertyAdapter(ExtensibilityElement element, String name, String defaultValue) {
        this(element, name);
        this.defaultValue = defaultValue;
        this.supportsDefaultValue = true;
    }
    
    /*
     * this is the default implementation, subclasses can override this.
     * @return value
     */
    public String getValue() {
        String value = element.getAttribute(attributeName);
        if (value == null) {
            value = "";
        }
        return value;
    }
    
    /*
     * this is the default implementation, subclasses can override this.
     * @param value the value to be set
     */
    public void setValue(String value) {
        boolean inTransaction = Utility.startTransaction(element.getModel());
        if (value != null && value.length() == 0) {
            value = null;
        }
        element.setAttribute(attributeName, value);
        try {
            Utility.endTransaction(element.getModel(), inTransaction);
        } catch (IOException e) {
           ErrorManager.getDefault().notify(e);
        }
    }
    
    /*
     * generic setValue. if overridden, also override getValue
     * 
     * @param val
     */
    public void setValue(Object val) {
        
    }
    
    public ExtensibilityElement getExtensibilityElement() {
        return element;
    }
    
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public boolean supportsDefaultValue() {
        return supportsDefaultValue;
    }
    
}
