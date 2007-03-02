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
package org.netbeans.modules.visualweb.propertyeditors.css.model;

/**
 *
 * @author  Winston Prakash
 */
public class PropertyWithUnitData extends PropertyData{
    
    /**
     * Holds value of property unit.
     */
    protected String unit="px"; //NOI18N
    
    public String toString(){
        String valueString = super.toString();
        if(Utils.isInteger(valueString)){
            valueString += unit;
        }
        return valueString;
    }
    
    
    /**
     * Setter for property unit.
     * @param unit New value of property unit.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Getter for property unit.
     * @return Value of property unit.
     */
    public java.lang.String getUnit() {
        return unit;
    }
    
}
