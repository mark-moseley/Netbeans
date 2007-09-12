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

/*
 *
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder;

/**
 * Class to hold parameter metadata.
 * 
 * @author
 */
public class Parameter {
    private String name = ""; // name of parameter

    private String javaType; // Java type - ex. java.lang.String

    private String sqlType; // SQL type - ex. BIGINT, NUMERIC

    private String paramType; // parameter type description: IN, INOUT, OUT, RETURN, RESULT

    private int ordinalPosition; // ordinal position

    private int numericPrecision; // numeric precision

    private int numericScale; // numeric scale

    private boolean isNullable; // specifies if the parameter is nullable

    /**
     * Creates an instance of Paramter.
     */
    public Parameter() {
        this.name = "";
        this.javaType = "";
        this.sqlType = "";
        this.paramType = "";
        this.ordinalPosition = 0;
        this.numericPrecision = 0;
        this.numericScale = 0;
        this.isNullable = false;
    }

    /**
     * Creates an instance of Parameter with the given name.
     * 
     * @param newName Parameter name
     */
    public Parameter(final String newName) {
        this.name = newName;
    }

    /**
     * Creates an instance of Parameter with the given name and java type.
     * 
     * @param newName Parameter name
     * @param newJavaType Java type
     */
    public Parameter(final String newName, final String newJavaType) {
        this.name = newName;
        this.javaType = newJavaType;
    }

    /**
     * Creates an instance of Parameter with the given attributes.
     * 
     * @param newName Parameter name
     * @param newJavaType Java type
     * @param newParamType Parameter type
     * @param newOrdinalPosition Ordinal position
     * @param newNumericPrecision Numeric precision
     * @param newNumericScale Numeric scale
     * @param newIsNullable Nullable flag
     */
    public Parameter(final String newName, final String newJavaType, final String newParamType, final int newOrdinalPosition,
            final int newNumericPrecision, final int newNumericScale, final boolean newIsNullable) {
        this.name = newName;
        this.javaType = newJavaType;
        this.paramType = newParamType;
        this.ordinalPosition = newOrdinalPosition;
        this.numericPrecision = newNumericPrecision;
        this.numericScale = newNumericScale;
        this.isNullable = newIsNullable;
    }

    public Parameter(final Parameter p) {
        this.name = p.getName();
        this.javaType = p.getJavaType();
        this.sqlType = p.getSqlType();
        this.paramType = p.getParamType();
        this.ordinalPosition = p.getOrdinalPosition();
        this.numericPrecision = p.getNumericPrecision();
        this.numericScale = p.getNumericScale();
        this.isNullable = p.getIsNullable();
    }

    /**
     * Get the parameter name.
     * 
     * @return parameter name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the Java type.
     * 
     * @return Java type
     */
    public String getJavaType() {
        return this.javaType;
    }

    /**
     * Get the SQL type.
     * 
     * @return SQL type
     */
    public String getSqlType() {
        return this.sqlType;
    }

    /**
     * Get the parameter type.
     * 
     * @return Parameter type
     */
    public String getParamType() {
        return this.paramType;
    }

    /**
     * Get the parameter ordinal position.
     * 
     * @return Parameter ordinal position
     */
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    /**
     * Get the parameter numeric precision.
     * 
     * @return Parameter numeric precision.
     */
    public int getNumericPrecision() {
        return this.numericPrecision;
    }

    /**
     * Get the parameter numeric scale.
     * 
     * @return Parameter numeric scale.
     */
    public int getNumericScale() {
        return this.numericScale;
    }

    /**
     * Get the parameter nullable flag.
     * 
     * @return Parameter nullable flag.
     */
    public boolean getIsNullable() {
        return this.isNullable;
    }

    /**
     * Set the parameter name.
     * 
     * @param newName Parameter name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Set the parameter Java type.
     * 
     * @param newJavaType Parameter Java type.
     */
    public void setJavaType(final String newJavaType) {
        this.javaType = newJavaType;
    }

    /**
     * Set the parameter SQL type.
     * 
     * @param newSqlType Parameter SQL type.
     */
    public void setSqlType(final String newSqlType) {
        this.sqlType = newSqlType;
    }

    /**
     * Set the parameter type.
     * 
     * @param newParamType Parameter type.
     */
    public void setParamType(final String newParamType) {
        this.paramType = newParamType;
    }

    /**
     * Set the parameter ordinal position.
     * 
     * @param newOrdinalPosition Parameter ordinal Position.
     */
    public void setOrdinalPosition(final int newOrdinalPosition) {
        this.ordinalPosition = newOrdinalPosition;
    }

    /**
     * Set the parameter numeric position.
     * 
     * @param newNumericPrecision Parameter numeric precision
     */
    public void setNumericPrecision(final int newNumericPrecision) {
        this.numericPrecision = newNumericPrecision;
    }

    /**
     * Set the parameter numeric scale.
     * 
     * @param newNumericScale Parameter numeric scale
     */
    public void setNumericScale(final int newNumericScale) {
        this.numericScale = newNumericScale;
    }

    /**
     * Set the parameter nullable flag.
     * 
     * @param newIsNullable Parameter nullable flag
     */
    public void setIsNullable(final boolean newIsNullable) {
        this.isNullable = newIsNullable;
    }

    public int getAccessType() {
        throw new UnsupportedOperationException("Parameter:getAccessType()");
        // if (getParamType().equals("IN")) {
        // return OtdLeaf.Access.WRITE;
        // }
        // if (getParamType().equals("INOUT")) {
        // return OtdLeaf.Access.MODIFY;
        // }
        // return OtdLeaf.Access.READ;
    }
}
