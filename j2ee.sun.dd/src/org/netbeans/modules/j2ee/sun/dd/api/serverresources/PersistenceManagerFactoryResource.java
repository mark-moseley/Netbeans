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
 * PersistenceManagerFactoryResource.java
 *
 * Created on November 21, 2004, 3:55 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface PersistenceManagerFactoryResource {

        public static final String JNDINAME = "JndiName";	// NOI18N
	public static final String FACTORYCLASS = "FactoryClass";	// NOI18N
	public static final String JDBCRESOURCEJNDINAME = "JdbcResourceJndiName";	// NOI18N
	public static final String ENABLED = "Enabled";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        public static final String OBJECTTYPE = "ObjectType";	// NOI18N
        
	/** Setter for jndi-name property
        * @param value property value
        */
	public void setJndiName(java.lang.String value);
        /** Getter for jndi-name property
        * @return property value
        */
	public java.lang.String getJndiName();
        /** Setter for factory-class property
        * @param value property value
        */
	public void setFactoryClass(java.lang.String value);
        /** Getter for factory-class property
        * @param value property value
        */
	public java.lang.String getFactoryClass();
        /** Setter for jdbc-resource-jndi-name property
        * @param value property value
        */
	public void setJdbcResourceJndiName(java.lang.String value);
        /** Getter for jdbc-resource-jndi-name property
        * @param value property value
        */
	public java.lang.String getJdbcResourceJndiName();
        /** Setter for enabled property
        * @param value property value
        */
	public void setEnabled(java.lang.String value);
        /** Getter for enabled property
        * @param value property value
        */
	public java.lang.String getEnabled();
        /** Setter for description attribute
        * @param value attribute value
        */
	public void setDescription(String value);
        /** Getter for description attribute
        * @return attribute value
        */
	public String getDescription();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();
        
        //Resource 1.2
        /** Setter for object-type property
        * @param value property value
        */
        public void setObjectType(java.lang.String value);
        /** Getter for object-type attribute.
        * @return attribute value
        */
	public java.lang.String getObjectType();

}
