/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.beans.PropertyEditorSupport;
import java.util.Enumeration;
import java.util.Properties;

import org.openide.ErrorManager;
import org.openide.TopManager;


/** A property editor for Properties class.
* @author   Ian Formanek
*/
public class PropertiesEditor extends PropertyEditorSupport {

    /** Overrides superclass method. */
    public String getAsText() {
        Object value = getValue();
        
        if(value instanceof Properties) {
            Properties prop = (Properties)value;

            StringBuffer buff = new StringBuffer();
            
            for(Enumeration enum = prop.keys(); enum.hasMoreElements(); ) {
                if(buff.length() > 0) {
                    buff.append("; "); // NOI18N
                }
                
                Object key = enum.nextElement();
                
                buff.append(key + "=" + prop.get(key)); // NOI18N
            }
            
            return buff.toString();
        }
        
        return "" + value; // NOI18N
    }

    /** Overrides superclass method.
     * @exception IllegalArgumentException if <code>null</code> value
     * is passes in or some io problem by converting occured */
    public void setAsText(String text) throws IllegalArgumentException {
        if(text == null) {
            throw new IllegalArgumentException("Inserted value can't be null."); // NOI18N
        }
        
        try {
            Properties prop = new Properties();
            InputStream is = new ByteArrayInputStream(
                text.replace(';', '\n').getBytes("ISO8859_1") // NOI18N
            );
            prop.load(is);
            setValue(prop);
        } catch(IOException ioe) {
            throw (IllegalArgumentException)ErrorManager.getDefault()
                .annotate(new IllegalArgumentException(), ioe);
        }
    }

    public String getJavaInitializationString () {
        return null; // does not generate any code
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new PropertiesCustomEditor (this);
    }

}
