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
/*
 * AsContext.java
 *
 * Created on November 18, 2004, 10:03 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface AsContext extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String AUTH_METHOD = "AuthMethod";	// NOI18N
    public static final String REALM = "Realm";	// NOI18N
    public static final String REQUIRED = "Required";	// NOI18N

    /** Setter for auth-method property
     * @param value property value
     */
    public void setAuthMethod(java.lang.String value);
    /** Getter for auth-method property.
     * @return property value
     */
    public java.lang.String getAuthMethod();
    /** Setter for realm property
     * @param value property value
     */
    public void setRealm(java.lang.String value);
    /** Getter for realm property.
     * @return property value
     */
    public java.lang.String getRealm();
    /** Setter for required property
     * @param value property value
     */
    public void setRequired(java.lang.String value);
    /** Getter for required property.
     * @return property value
     */
    public java.lang.String getRequired();
   
}
