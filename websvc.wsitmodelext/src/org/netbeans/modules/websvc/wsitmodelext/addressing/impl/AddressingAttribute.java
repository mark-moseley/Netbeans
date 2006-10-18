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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.addressing.impl;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Martin Grebac
 */
public enum AddressingAttribute implements Attribute {
        PORTNAME("PortName");
    
    private String name;
    private Class type;
    private Class subtype;
    
    /**
     * Creates a new instance of AddressingAttribute
     */
    AddressingAttribute(String name) {
        this(name, String.class);
    }
    AddressingAttribute(String name, Class type) {
        this(name, type, null);
    }
    AddressingAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    @Override
    public String toString() { return name; }

    public Class getType() {
        return type;
    }

    public String getName() { return name; }

    public Class getMemberType() { return subtype; }
}
