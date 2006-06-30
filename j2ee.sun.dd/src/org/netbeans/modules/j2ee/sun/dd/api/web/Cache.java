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
 * Cache.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface Cache extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String MAXENTRIES = "MaxEntries";	// NOI18N
	public static final String TIMEOUTINSECONDS = "TimeoutInSeconds";	// NOI18N
	public static final String ENABLED = "Enabled";	// NOI18N
	public static final String CACHE_HELPER = "CacheHelper";	// NOI18N
	public static final String DEFAULT_HELPER = "DefaultHelper";	// NOI18N
	public static final String PROPERTY = "WebProperty";	// NOI18N
	public static final String CACHE_MAPPING = "CacheMapping";	// NOI18N
        
        /** Setter for max-entries attribute
         * @param value attribute value
         */
	public void setMaxEntries(java.lang.String value);
        /** Getter for max-entries attribute.
         * @return attribute value
         */
	public java.lang.String getMaxEntries();
        /** Setter for timeout-in-seconds attribute
         * @param value attribute value
         */
	public void setTimeoutInSeconds(java.lang.String value);
        /** Getter for timeout-in-seconds attribute.
         * @return attribute value
         */
	public java.lang.String getTimeoutInSeconds();
        /** Setter for enabled attribute
         * @param value attribute value
         */
	public void setEnabled(java.lang.String value);
        /** Getter for enabled property.
         * @return property value
         */
	public java.lang.String getEnabled();

	public void setCacheHelper(int index, CacheHelper value); 
	public CacheHelper getCacheHelper(int index);
	public int sizeCacheHelper();
	public void setCacheHelper(CacheHelper[] value);
	public CacheHelper[] getCacheHelper();
	public int addCacheHelper(CacheHelper value);
	public int removeCacheHelper(CacheHelper value);
	public CacheHelper newCacheHelper();

        /** Setter for default-helper property
         * @param value property value
         */
	public void setDefaultHelper(DefaultHelper value); 
        /** Getter for default-helper property.
         * @return property value
         */
	public DefaultHelper getDefaultHelper();

	public DefaultHelper newDefaultHelper(); 

	public void setWebProperty(int index, WebProperty value);
	public WebProperty getWebProperty(int index);
	public int sizeWebProperty();
	public void setWebProperty(WebProperty[] value);
	public WebProperty[] getWebProperty();
	public int addWebProperty(WebProperty value);
	public int removeWebProperty(WebProperty value);
	public WebProperty newWebProperty();

	public void setCacheMapping(int index, CacheMapping value);
	public CacheMapping getCacheMapping(int index);
	public int sizeCacheMapping();
	public void setCacheMapping(CacheMapping[] value);
	public CacheMapping[] getCacheMapping();
	public int addCacheMapping(CacheMapping value);
	public int removeCacheMapping(CacheMapping value);
	public CacheMapping newCacheMapping();

}
