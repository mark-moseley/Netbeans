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
 * JdbcConnectionPool.java
 *
 * Created on November 21, 2004, 4:47 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface JdbcConnectionPool {

        public static final String NAME = "Name";	// NOI18N
	public static final String DATASOURCECLASSNAME = "DatasourceClassname";	// NOI18N
	public static final String RESTYPE = "ResType";	// NOI18N
	public static final String STEADYPOOLSIZE = "SteadyPoolSize";	// NOI18N
	public static final String MAXPOOLSIZE = "MaxPoolSize";	// NOI18N
	public static final String MAXWAITTIMEINMILLIS = "MaxWaitTimeInMillis";	// NOI18N
	public static final String POOLRESIZEQUANTITY = "PoolResizeQuantity";	// NOI18N
	public static final String IDLETIMEOUTINSECONDS = "IdleTimeoutInSeconds";	// NOI18N
	public static final String TRANSACTIONISOLATIONLEVEL = "TransactionIsolationLevel";	// NOI18N
	public static final String ISISOLATIONLEVELGUARANTEED = "IsIsolationLevelGuaranteed";	// NOI18N
	public static final String ISCONNECTIONVALIDATIONREQUIRED = "IsConnectionValidationRequired";	// NOI18N
	public static final String CONNECTIONVALIDATIONMETHOD = "ConnectionValidationMethod";	// NOI18N
	public static final String VALIDATIONTABLENAME = "ValidationTableName";	// NOI18N
	public static final String FAILALLCONNECTIONS = "FailAllConnections";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setDatasourceClassname(java.lang.String value);

	public java.lang.String getDatasourceClassname();

	public void setResType(java.lang.String value);

	public java.lang.String getResType();

	public void setSteadyPoolSize(java.lang.String value);

	public java.lang.String getSteadyPoolSize();

	public void setMaxPoolSize(java.lang.String value);

	public java.lang.String getMaxPoolSize();

	public void setMaxWaitTimeInMillis(java.lang.String value);

	public java.lang.String getMaxWaitTimeInMillis();

	public void setPoolResizeQuantity(java.lang.String value);

	public java.lang.String getPoolResizeQuantity();

	public void setIdleTimeoutInSeconds(java.lang.String value);

	public java.lang.String getIdleTimeoutInSeconds();

	public void setTransactionIsolationLevel(java.lang.String value);

	public java.lang.String getTransactionIsolationLevel();

	public void setIsIsolationLevelGuaranteed(java.lang.String value);

	public java.lang.String getIsIsolationLevelGuaranteed();

	public void setIsConnectionValidationRequired(java.lang.String value);

	public java.lang.String getIsConnectionValidationRequired();

	public void setConnectionValidationMethod(java.lang.String value);

	public java.lang.String getConnectionValidationMethod();

	public void setValidationTableName(java.lang.String value);

	public java.lang.String getValidationTableName();

	public void setFailAllConnections(java.lang.String value);

	public java.lang.String getFailAllConnections();

	public void setDescription(String value);

	public String getDescription();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();

}
