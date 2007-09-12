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

/**
 * This class is required since the OTD seed is generated as Procedure1, Procedure2
 * and inside WSDLGenerator there is no way of distinguishing between PreparedStatement and stored procedure DBObjects.
 */
package org.netbeans.modules.jdbcwizard.builder.dbmodel.impl;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBObject;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import java.util.ResourceBundle;
import org.openide.util.NbBundle;

public class DBPSObject extends DBObjectImpl {

    public static final String OTDSEED_PREPSTMTID_PATTERN = "PreparedStatement{0}";

    public static final String OTDSEED_PSPARAMID_PATTERN = "PreparedStatement{0}Param{1}";

    public static final String OTDSEED_PSCOLID_PATTERN = "PreparedStatement{0}$ResultsColumn{1}";

    private int numParameters = 0;

    private ArrayList parameters = null;

    private String sqlText = null;

    public PreparedStmtResultSetDescriptor getPsResultSetDesc() {
        return this.psResultSetDesc;
    }

    private PreparedStmtResultSetDescriptor psResultSetDesc = null;

    /** Map of column metadata. */
    protected Map columns;

    /* No-arg constructor; initializes Collections-related member variables. */
    public DBPSObject() {
        this.parameters = new ArrayList();
        this.columns = new HashMap();
    }

    public DBPSObject(final String Objectname, final String schema, final String catalog) {

        super(Objectname, schema, catalog);
        this.parameters = new ArrayList();
    }

    /**
     * Creates a new instance of DBTableImpl, cloning the contents of the given DBTable
     * implementation instance.
     * 
     * @param src DBTable instance to be cloned
     */
    public DBPSObject(final DBObject src) {
        this();
        if (src == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(DBPSObject.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_DBTABLE") + "ERROR_NULL_DBTABLE");// NO
            // i18n
        }

        super.copyFrom(src);
    }

    public String getJavaName() {
        return super.getJavaName();
    }

    /**
     * getter for numParameters; *
     * 
     * @return numParameters;
     */
    public int getNumParameters() {
        return this.numParameters;
    }

    /**
     * setter for numParameters; *
     * 
     * @param numParameters number of parameters;
     */
    public void setNumParameters(final int numParameters) {
        this.numParameters = numParameters;
    }

    /**
     * getter for parameters; *
     * 
     * @return parameters;
     */
    public ArrayList getParameters() {
        return this.parameters;
    }

    /**
     * setter for parameters; *
     * 
     * @param parameters list of <code>ParameterDescriptor</code> * objects; *
     */
    public void setParameters(final ArrayList parameters) {
        this.parameters = parameters;
    }

    /**
     * getter for sqlText; *
     * 
     * @return sqlText;
     */
    public String getSqlText() {
        return this.sqlText;
    }

    /**
     * setter for sqlText; *
     * 
     * @param sqlText string to hold the prepared statement;
     */
    public void setSqlText(final String sqlText) {
        this.sqlText = sqlText;
    }

    public void setName(final String name) {
        super.setName(name);
    }

    public void setJavaName(final String javaName) {
        super.setJavaName(javaName);
    }

    public void setCatalog(final String catalog) {
        super.setCatalog(catalog);
    }

    public void setSchema(final String schema) {
        super.setSchema(schema);
    }

    public void setPSResultSetDesc(final PreparedStmtResultSetDescriptor psrsd) {
        this.psResultSetDesc = psrsd;
    }

    public String getName() {
        return super.getName();
    }
}
