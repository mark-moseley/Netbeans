/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.web;
/**
 * Generated interface for LocaleEncodingMapping element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface LocaleEncodingMapping extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for locale property.
         * @param value property value
         */
	public void setLocale(java.lang.String value);
        /** Getter for locale property.
         * @return property value 
         */
	public java.lang.String getLocale();
        /** Setter for encoding property.
         * @param value property value
         */
	public void setEncoding(java.lang.String value);
        /** Getter for encoding property.
         * @return property value 
         */
	public java.lang.String getEncoding();

}
