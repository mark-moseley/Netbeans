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
package org.netbeans.jellytools.properties;

/*
 * StringArrayProperty.java
 *
 * Created on June 18, 2002, 11:53 AM
 */

import org.netbeans.jellytools.properties.editors.StringArrayCustomEditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** Operator serving property of type String[]
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class StringArrayProperty extends TextFieldProperty {
    
    /** Creates a new instance of StringArrayProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name */
    public StringArrayProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return StringArrayCustomEditorOperator */    
    public StringArrayCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new StringArrayCustomEditorOperator(getName());
    }
    
    /** setter for StringArray value through Custom Editor
     * @param value String[] array of strings */    
    public void setStringArrayValue(String[] value) {
        StringArrayCustomEditorOperator customizer=invokeCustomizer();
        customizer.setStringArrayValue(value);
        customizer.ok();
    }        
    
    /** getter for StringArray value through Custom Editor
     * @return String[] array of strings */    
    public String[] getStringArrayValue() {
        String[] value;
        StringArrayCustomEditorOperator customizer=invokeCustomizer();
        value=customizer.getStringArrayValue();
        customizer.close();
        return value;
    }
    
}
