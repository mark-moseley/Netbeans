/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * ExternalJndiResource.java
 *
 * Created on November 21, 2004, 2:47 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface ExternalJndiResource {

        public static final String JNDINAME = "JndiName";	// NOI18N
	public static final String JNDILOOKUPNAME = "JndiLookupName";	// NOI18N
	public static final String RESTYPE = "ResType";	// NOI18N
	public static final String FACTORYCLASS = "FactoryClass";	// NOI18N
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
        /** Setter for jndi-lookup-name property
        * @param value property value
        */
	public void setJndiLookupName(java.lang.String value);
        /** Getter for jndi-lookup-name property
        * @param value property value
        */
	public java.lang.String getJndiLookupName();
        /** Setter for res-type property
        * @param value property value
        */
	public void setResType(java.lang.String value);
        /** Getter for res-type property
        * @return property value
        */
	public java.lang.String getResType();
        /** Setter for factory-class property
        * @param value property value
        */
	public void setFactoryClass(java.lang.String value);
        /** Getter for factory-class property
        * @return property value
        */
	public java.lang.String getFactoryClass();
        /** Setter for enabled property
        * @param value property value
        */
	public void setEnabled(java.lang.String value);
        /** Getter for enabled property
        * @return property value
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
