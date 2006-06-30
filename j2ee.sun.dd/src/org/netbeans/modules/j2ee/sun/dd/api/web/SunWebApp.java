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
 * SunWebApp.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;


import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;

public interface SunWebApp extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {
    
        public static final String VERSION_2_5_0 = "2.50"; //NOI18N
        public static final String VERSION_2_4_1 = "2.41"; //NOI18N
        public static final String VERSION_2_4_0 = "2.40"; //NOI18N
        public static final String VERSION_2_3_0 = "2.30"; //NOI18N
        
        public static final int STATE_VALID=0;
        public static final int STATE_INVALID_PARSABLE=1;
        public static final int STATE_INVALID_UNPARSABLE=2;
        public static final String PROPERTY_STATUS="dd_status"; //NOI18N
        public static final String PROPERTY_VERSION="dd_version"; //NOI18N
    
        public static final String ERRORURL = "ErrorUrl";	// NOI18N
	public static final String CONTEXT_ROOT = "ContextRoot";	// NOI18N
	public static final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
	public static final String SERVLET = "Servlet";	// NOI18N
	public static final String IDEMPOTENT_URL_PATTERN = "IdempotentUrlPattern";	// NOI18N
	public static final String IDEMPOTENTURLPATTERNURLPATTERN = "IdempotentUrlPatternUrlPattern";	// NOI18N
	public static final String IDEMPOTENTURLPATTERNNUMOFRETRIES = "IdempotentUrlPatternNumOfRetries";	// NOI18N
	public static final String SESSION_CONFIG = "SessionConfig";	// NOI18N
	public static final String EJB_REF = "EjbRef";	// NOI18N
	public static final String RESOURCE_REF = "ResourceRef";	// NOI18N
	public static final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
	public static final String SERVICE_REF = "ServiceRef";	// NOI18N
	public static final String CACHE = "Cache";	// NOI18N
	public static final String CLASS_LOADER = "MyClassLoader";	// NOI18N
	public static final String JSP_CONFIG = "JspConfig";	// NOI18N
	public static final String LOCALE_CHARSET_INFO = "LocaleCharsetInfo";	// NOI18N
	public static final String PARAMETER_ENCODING = "ParameterEncoding";	// NOI18N
	public static final String PARAMETERENCODINGFORMHINTFIELD = "ParameterEncodingFormHintField";	// NOI18N
	public static final String PARAMETERENCODINGDEFAULTCHARSET = "ParameterEncodingDefaultCharset";	// NOI18N
	public static final String PROPERTY = "WebProperty";	// NOI18N
	public static final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
	public static final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N
        public static final String MESSAGE_DESTINATION_REF = "MessageDestinationRef";	// NOI18N

        
        /** Setter for error-url property
         * @param value property value
         */
	public void setErrorUrl(java.lang.String value) throws VersionNotSupportedException;
        /** Getter for error-url property.
         * @return property value
         */
	public java.lang.String getErrorUrl() throws VersionNotSupportedException;
        /** Setter for context-root property
         * @param value property value
         */
	public void setContextRoot(String value);
        /** Getter for context-root property.
         * @return property value
         */
	public String getContextRoot();

	public void setSecurityRoleMapping(int index, SecurityRoleMapping value);
	public SecurityRoleMapping getSecurityRoleMapping(int index);
	public int sizeSecurityRoleMapping();
	public void setSecurityRoleMapping(SecurityRoleMapping[] value);
	public SecurityRoleMapping[] getSecurityRoleMapping();
	public int addSecurityRoleMapping(SecurityRoleMapping value);
	public int removeSecurityRoleMapping(SecurityRoleMapping value);
	public SecurityRoleMapping newSecurityRoleMapping();

	public void setServlet(int index, Servlet value); 
	public Servlet getServlet(int index);
	public int sizeServlet();
	public void setServlet(Servlet[] value);
	public Servlet[] getServlet();
	public int addServlet(Servlet value);
	public int removeServlet(Servlet value);
	public Servlet newServlet();

        public void setIdempotentUrlPattern(int index, boolean value) throws VersionNotSupportedException;
        public boolean isIdempotentUrlPattern(int index) throws VersionNotSupportedException;
        public int sizeIdempotentUrlPattern() throws VersionNotSupportedException;
	public void setIdempotentUrlPattern(boolean[] value) throws VersionNotSupportedException;
	public boolean[] getIdempotentUrlPattern() throws VersionNotSupportedException;
	public int addIdempotentUrlPattern(boolean value) throws VersionNotSupportedException;
	public int removeIdempotentUrlPattern(boolean value) throws VersionNotSupportedException;
	public void removeIdempotentUrlPattern(int index) throws VersionNotSupportedException;
        
        /** Setter for url-pattern attribute of idempotent-url-pattern
         * @param value attribute value
         */
	public void setIdempotentUrlPatternUrlPattern(int index, java.lang.String value) throws VersionNotSupportedException;
        /** Getter for url-pattern attribute of idempotent-url-pattern
         * @return attribute value
         */
	public java.lang.String getIdempotentUrlPatternUrlPattern(int index) throws VersionNotSupportedException;
	public int sizeIdempotentUrlPatternUrlPattern() throws VersionNotSupportedException;
	
        /** Setter for num-of-retries attribute of idempotent-url-pattern
         * @param value attribute value
         */
        public void setIdempotentUrlPatternNumOfRetries(int index, java.lang.String value) throws VersionNotSupportedException;
        /** Getter for num-of-retries attribute of idempotent-url-pattern
         * @return attribute value
         */
	public java.lang.String getIdempotentUrlPatternNumOfRetries(int index) throws VersionNotSupportedException;
	public int sizeIdempotentUrlPatternNumOfRetries() throws VersionNotSupportedException;

        /** Setter for session-config property
         * @param value property value
         */
	public void setSessionConfig(SessionConfig value); 
        /** Getter for session-config property.
         * @return property value
         */
	public SessionConfig getSessionConfig();

	public SessionConfig newSessionConfig(); 

	public void setEjbRef(int index, EjbRef value);
	public EjbRef getEjbRef(int index);
	public int sizeEjbRef();
	public void setEjbRef(EjbRef[] value);
	public EjbRef[] getEjbRef();
	public int addEjbRef(EjbRef value);
	public int removeEjbRef(EjbRef value);
	public EjbRef newEjbRef();

	public void setResourceRef(int index, ResourceRef value);
	public ResourceRef getResourceRef(int index);
	public int sizeResourceRef();
	public void setResourceRef(ResourceRef[] value);
	public ResourceRef[] getResourceRef();
	public int addResourceRef(ResourceRef value);
	public int removeResourceRef(ResourceRef value);
	public ResourceRef newResourceRef();

	public void setResourceEnvRef(int index, ResourceEnvRef value);
	public ResourceEnvRef getResourceEnvRef(int index);
	public int sizeResourceEnvRef();
	public void setResourceEnvRef(ResourceEnvRef[] value);
	public ResourceEnvRef[] getResourceEnvRef();
	public int addResourceEnvRef(ResourceEnvRef value);
	public int removeResourceEnvRef(ResourceEnvRef value);
	public ResourceEnvRef newResourceEnvRef();

	public void setServiceRef(int index, ServiceRef value);
	public ServiceRef getServiceRef(int index);
	public int sizeServiceRef();
	public void setServiceRef(ServiceRef[] value);
	public ServiceRef[] getServiceRef();
	public int addServiceRef(ServiceRef value);
	public int removeServiceRef(ServiceRef value);
	public ServiceRef newServiceRef();

        /** Setter for cache property
         * @param value property value
         */
	public void setCache(Cache value);
        /** Getter for cache property.
         * @return property value
         */
	public Cache getCache();
	public Cache newCache();

        /** Setter for class-loader property in web_2_4-1
         * @param value property value
         */
        public void setMyClassLoader(MyClassLoader value) throws VersionNotSupportedException;
        /** Getter for class-loader property in web_2_4-1
         * @return property value
         */
	public MyClassLoader getMyClassLoader() throws VersionNotSupportedException;

	public MyClassLoader newMyClassLoader() throws VersionNotSupportedException;

        /** Setter for class-loader property in web_2_4-0
         * @param value property value
         */
        public void setMyClassLoader(boolean value) throws VersionNotSupportedException;
	/** Getter for class-loader property. in web_2_4-0
         * @return property value
         */
	public boolean isMyClassLoader() throws VersionNotSupportedException;
        
        /** Setter for jsp-config property
         * @param value property value
         */
	public void setJspConfig(JspConfig value);
        /** Getter for jsp-config property.
         * @return property value
         */
	public JspConfig getJspConfig();

	public JspConfig newJspConfig();

        /** Setter for locale-charset-info property
         * @param value property value
         */
	public void setLocaleCharsetInfo(LocaleCharsetInfo value);
        /** Getter for locale-charset-info property.
         * @return property value
         */
	public LocaleCharsetInfo getLocaleCharsetInfo();

	public LocaleCharsetInfo newLocaleCharsetInfo();
        
        /** Setter for parameter-encoding property.
         * @param value property value
         */
	public void setParameterEncoding(boolean value) throws VersionNotSupportedException;
        /** Check for parameter-encoding property.
         * @return property value
         */
	public boolean isParameterEncoding() throws VersionNotSupportedException;
        /** Setter for form-hint-field attribute.
         * @param value attribute value
         */
	public void setParameterEncodingFormHintField(java.lang.String value) throws VersionNotSupportedException;
        /** Getter for form-hint-field attribute.
         * @return attribute value
         */
	public java.lang.String getParameterEncodingFormHintField() throws VersionNotSupportedException;
        /** Setter for default-charset attribute.
         * @param value attribute value
         */
	public void setParameterEncodingDefaultCharset(java.lang.String value) throws VersionNotSupportedException;
        /** Getter for default-charset attribute.
         * @return attribute value
         */
	public java.lang.String getParameterEncodingDefaultCharset() throws VersionNotSupportedException;

	public void setWebProperty(int index, WebProperty value);
        public WebProperty getWebProperty(int index);
	public int sizeWebProperty();
	public void setWebProperty(WebProperty[] value);
	public WebProperty[] getWebProperty();
	public int addWebProperty(WebProperty value);
	public int removeWebProperty(WebProperty value);
	public WebProperty newWebProperty();

	public void setMessageDestination(int index, MessageDestination value);
	public MessageDestination getMessageDestination(int index);
	public int sizeMessageDestination();
	public void setMessageDestination(MessageDestination[] value);
	public MessageDestination[] getMessageDestination();
	public int addMessageDestination(MessageDestination value);
	public int removeMessageDestination(MessageDestination value);
	public MessageDestination newMessageDestination();

	public void setWebserviceDescription(int index, WebserviceDescription value);
	public WebserviceDescription getWebserviceDescription(int index);
	public int sizeWebserviceDescription();
	public void setWebserviceDescription(WebserviceDescription[] value);
	public WebserviceDescription[] getWebserviceDescription();
	public int addWebserviceDescription(WebserviceDescription value);
	public int removeWebserviceDescription(WebserviceDescription value);
	public WebserviceDescription newWebserviceDescription();

        /** Setter for version property.
         * Warning : Only the upgrade from lower to higher version is supported.
         * @param version ejb-jar version value
         */
        public void setVersion(java.math.BigDecimal version);
        /** Getter for version property.
         * @return property value
         */
        public java.math.BigDecimal getVersion();
        
         //Required for web 2.5.0
        public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException;
        public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;
        public int sizeMessageDestinationRef() throws VersionNotSupportedException;
        public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException;
        public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;
        public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException;
        public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException;
        public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException;
}
