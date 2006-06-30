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
