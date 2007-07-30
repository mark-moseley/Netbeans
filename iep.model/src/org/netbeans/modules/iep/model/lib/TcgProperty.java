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


package org.netbeans.modules.iep.model.lib;

import java.io.Serializable;

/**
 * Interface TcgProperty specifies the methods needed to access properties of
 * TcgComponentType
 *
 * @author Bing Lu
 *
 * @see TcgPropertyImpl
 * @see TcgComponentType
 * @since May 1, 2002
 */
public interface TcgProperty extends Serializable {

    /**
     * Gets the name attribute of the TcgProperty object
     *
     * @return The name value
     */
    public String getName();

    /**
     * Gets the containing TcgComponent of this property
     *
     * @return The containing TcgComponent of this property
     */
    public TcgComponent getParentComponent();

    /**
     * Sets the value attribute of the TcgProperty object in string format
     *
     * @param val The new value in string format
     */
    public void setStringValue(String val);

    /**
     * Gets the value attribute of the TcgProperty object in string format
     *
     * @return The value in string format
     */
    public String getStringValue();

    /**
     * Gets the type attribute of the TcgProperty object
     *
     * @return The type value
     */
    public TcgPropertyType getType();

    /**
     * Sets the value attribute of the TcgProperty object
     *
     * @param val The new value value
     */
    public void setValue(Object val);

    /**
     * Gets the value attribute of the TcgProperty object
     *
     * @return The value value
     */
    public Object getValue();
    
    public boolean hasValue();
        
    public java.util.List getListValue();
    
    public int getIntValue();
    
    public double getDblValue();
    
    public boolean getBoolValue();
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
