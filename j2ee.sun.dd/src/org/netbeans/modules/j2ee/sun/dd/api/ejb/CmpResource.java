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
 * CmpResource.java
 *
 * Created on November 17, 2004, 4:49 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface CmpResource extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String JNDI_NAME = "JndiName";	// NOI18N
    public static final String DEFAULT_RESOURCE_PRINCIPAL = "DefaultResourcePrincipal";	// NOI18N
    public static final String PROPERTY = "PropertyElement";	// NOI18N
    public static final String CREATE_TABLES_AT_DEPLOY = "CreateTablesAtDeploy";	// NOI18N
    public static final String DROP_TABLES_AT_UNDEPLOY = "DropTablesAtUndeploy";	// NOI18N
    public static final String DATABASE_VENDOR_NAME = "DatabaseVendorName";	// NOI18N
    public static final String SCHEMA_GENERATOR_PROPERTIES = "SchemaGeneratorProperties";	// NOI18N
        
    /** Setter for jndi-name property
     * @param value property value
     */
    public void setJndiName(java.lang.String value);
    /** Getter for jndi-name property.
     * @return property value
     */
    public java.lang.String getJndiName();
    
    /** Setter for default-resource-principal property
     * @param value property value
     */
    public void setDefaultResourcePrincipal(DefaultResourcePrincipal value);
    /** Getter for default-resource-principal property.
     * @return property value
     */
    public DefaultResourcePrincipal getDefaultResourcePrincipal();
    
    public DefaultResourcePrincipal newDefaultResourcePrincipal();
    
    public PropertyElement[] getPropertyElement();
    public PropertyElement getPropertyElement(int index);
    public void setPropertyElement(PropertyElement[] value);
    public void setPropertyElement(int index, PropertyElement value);
    public int addPropertyElement(PropertyElement value);
    public int removePropertyElement(PropertyElement value); 
    public int sizePropertyElement();
    public PropertyElement newPropertyElement();
    
    /** Setter for create-tables-at-deploy property
     * @param value property value
     */
    public void setCreateTablesAtDeploy(java.lang.String value);
    /** Getter for create-tables-at-deploy property.
     * @return property value
     */
    public java.lang.String getCreateTablesAtDeploy();
    
    /** Setter for drop-tables-at-undeploy property
     * @param value property value
     */
    public void setDropTablesAtUndeploy(java.lang.String value);
    /** Getter for drop-tables-at-undeploy property.
     * @return property value
     */
    public java.lang.String getDropTablesAtUndeploy();
    
    /** Setter for database-vendor-name property
     * @param value property value
     */
    public void setDatabaseVendorName(java.lang.String value);
    /** Getter for database-vendor-name property.
     * @return property value
     */
    public java.lang.String getDatabaseVendorName();
    /** Setter for schema-generator-properties property
     * @param value property value
     */
    public void setSchemaGeneratorProperties(SchemaGeneratorProperties value);
    /** Getter for schema-generator-properties property.
     * @return property value
     */
    public SchemaGeneratorProperties getSchemaGeneratorProperties(); 
    
    public SchemaGeneratorProperties newSchemaGeneratorProperties();
    
}
