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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.2-b01-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.12.09 at 06:26:10 PM PST 
//


package org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "property"
})
@XmlRootElement(name = "jdbc-connection-pool")
public class JdbcConnectionPool {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String name;
    @XmlAttribute(name = "datasource-classname", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String datasourceClassname;
    @XmlAttribute(name = "res-type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String resType;
    @XmlAttribute(name = "steady-pool-size")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String steadyPoolSize;
    @XmlAttribute(name = "max-pool-size")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String maxPoolSize;
    @XmlAttribute(name = "max-wait-time-in-millis")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String maxWaitTimeInMillis;
    @XmlAttribute(name = "pool-resize-quantity")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String poolResizeQuantity;
    @XmlAttribute(name = "idle-timeout-in-seconds")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String idleTimeoutInSeconds;
    @XmlAttribute(name = "transaction-isolation-level")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String transactionIsolationLevel;
    @XmlAttribute(name = "is-isolation-level-guaranteed")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isIsolationLevelGuaranteed;
    @XmlAttribute(name = "is-connection-validation-required")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isConnectionValidationRequired;
    @XmlAttribute(name = "connection-validation-method")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String connectionValidationMethod;
    @XmlAttribute(name = "validation-table-name")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String validationTableName;
    @XmlAttribute(name = "fail-all-connections")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String failAllConnections;
    @XmlAttribute(name = "non-transactional-connections")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String nonTransactionalConnections;
    @XmlAttribute(name = "allow-non-component-callers")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String allowNonComponentCallers;
    protected String description;
    protected List<Property> property;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the datasourceClassname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatasourceClassname() {
        return datasourceClassname;
    }

    /**
     * Sets the value of the datasourceClassname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatasourceClassname(String value) {
        this.datasourceClassname = value;
    }

    /**
     * Gets the value of the resType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResType() {
        return resType;
    }

    /**
     * Sets the value of the resType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResType(String value) {
        this.resType = value;
    }

    /**
     * Gets the value of the steadyPoolSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSteadyPoolSize() {
        if (steadyPoolSize == null) {
            return "8";
        } else {
            return steadyPoolSize;
        }
    }

    /**
     * Sets the value of the steadyPoolSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSteadyPoolSize(String value) {
        this.steadyPoolSize = value;
    }

    /**
     * Gets the value of the maxPoolSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxPoolSize() {
        if (maxPoolSize == null) {
            return "32";
        } else {
            return maxPoolSize;
        }
    }

    /**
     * Sets the value of the maxPoolSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxPoolSize(String value) {
        this.maxPoolSize = value;
    }

    /**
     * Gets the value of the maxWaitTimeInMillis property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxWaitTimeInMillis() {
        if (maxWaitTimeInMillis == null) {
            return "60000";
        } else {
            return maxWaitTimeInMillis;
        }
    }

    /**
     * Sets the value of the maxWaitTimeInMillis property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxWaitTimeInMillis(String value) {
        this.maxWaitTimeInMillis = value;
    }

    /**
     * Gets the value of the poolResizeQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPoolResizeQuantity() {
        if (poolResizeQuantity == null) {
            return "2";
        } else {
            return poolResizeQuantity;
        }
    }

    /**
     * Sets the value of the poolResizeQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPoolResizeQuantity(String value) {
        this.poolResizeQuantity = value;
    }

    /**
     * Gets the value of the idleTimeoutInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdleTimeoutInSeconds() {
        if (idleTimeoutInSeconds == null) {
            return "300";
        } else {
            return idleTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the idleTimeoutInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdleTimeoutInSeconds(String value) {
        this.idleTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the transactionIsolationLevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionIsolationLevel() {
        return transactionIsolationLevel;
    }

    /**
     * Sets the value of the transactionIsolationLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionIsolationLevel(String value) {
        this.transactionIsolationLevel = value;
    }

    /**
     * Gets the value of the isIsolationLevelGuaranteed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsIsolationLevelGuaranteed() {
        if (isIsolationLevelGuaranteed == null) {
            return "true";
        } else {
            return isIsolationLevelGuaranteed;
        }
    }

    /**
     * Sets the value of the isIsolationLevelGuaranteed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsIsolationLevelGuaranteed(String value) {
        this.isIsolationLevelGuaranteed = value;
    }

    /**
     * Gets the value of the isConnectionValidationRequired property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsConnectionValidationRequired() {
        if (isConnectionValidationRequired == null) {
            return "false";
        } else {
            return isConnectionValidationRequired;
        }
    }

    /**
     * Sets the value of the isConnectionValidationRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsConnectionValidationRequired(String value) {
        this.isConnectionValidationRequired = value;
    }

    /**
     * Gets the value of the connectionValidationMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionValidationMethod() {
        if (connectionValidationMethod == null) {
            return "auto-commit";
        } else {
            return connectionValidationMethod;
        }
    }

    /**
     * Sets the value of the connectionValidationMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionValidationMethod(String value) {
        this.connectionValidationMethod = value;
    }

    /**
     * Gets the value of the validationTableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidationTableName() {
        return validationTableName;
    }

    /**
     * Sets the value of the validationTableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidationTableName(String value) {
        this.validationTableName = value;
    }

    /**
     * Gets the value of the failAllConnections property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFailAllConnections() {
        if (failAllConnections == null) {
            return "false";
        } else {
            return failAllConnections;
        }
    }

    /**
     * Sets the value of the failAllConnections property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFailAllConnections(String value) {
        this.failAllConnections = value;
    }

    /**
     * Gets the value of the nonTransactionalConnections property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNonTransactionalConnections() {
        if (nonTransactionalConnections == null) {
            return "false";
        } else {
            return nonTransactionalConnections;
        }
    }

    /**
     * Sets the value of the nonTransactionalConnections property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNonTransactionalConnections(String value) {
        this.nonTransactionalConnections = value;
    }

    /**
     * Gets the value of the allowNonComponentCallers property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllowNonComponentCallers() {
        if (allowNonComponentCallers == null) {
            return "false";
        } else {
            return allowNonComponentCallers;
        }
    }

    /**
     * Sets the value of the allowNonComponentCallers property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllowNonComponentCallers(String value) {
        this.allowNonComponentCallers = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * 
     * 
     */
    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

}
