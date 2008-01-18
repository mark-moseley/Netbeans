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
 * SchemaGenerator.java
 *
 * Created on Oct 8, 2007, 3:39:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.masterindex.plugin;

import org.netbeans.modules.masterindex.plugin.datamodel.ObjectDefinitionBuilder;
import org.netbeans.modules.masterindex.plugin.datamodel.Field;
import org.netbeans.modules.masterindex.plugin.datamodel.Lookup;
import org.netbeans.modules.masterindex.plugin.datamodel.ObjectDefinition;
import org.netbeans.modules.masterindex.plugin.util.PluginDTConstants;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import net.java.hulp.i18n.Logger;
import org.axiondb.AxionException;
import org.axiondb.io.AxionFileSystem;
import org.axiondb.io.BufferedDataInputStream;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;

/**
 *
 * @author Manish
 */
public class TargetDBSchemaGenerator {

    private static TargetDBSchemaGenerator targetdbSchemaGenerator = null;
    private Lookup lookup = null;
    private static ObjectDefinition objDef = null;
    private Connection conn = null;
    //eview data model type vs. db data type
    private String[][] datatypes = {
        {"string", "VARCHAR"},
        {"int", "INTEGER"},
        {"char", "CHAR"},
        {"boolean", "BOOLEAN"},
        {"date", "DATE"},
        {"blob", "BLOB"},
        {"float", "FLOAT"}};
    /**
     * logger
     */
    /**
     * logger
     */
    private static transient final Logger mLogger = LogUtil.getLogger(TargetDBSchemaGenerator.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    private TargetDBSchemaGenerator() {
    }

    public static TargetDBSchemaGenerator getTargetDBSchemaGenerator() {
        if (targetdbSchemaGenerator == null) {
            targetdbSchemaGenerator = new TargetDBSchemaGenerator();
        }
        return targetdbSchemaGenerator;
    }

    public void createTargetDB(String dbname) {
        //
    }

    public void createTargetDB(String dbdir, String dbname) {
        //Check if dbdir is valid
        if (validateDir(dbdir)) {
            // Init DB
            initDB(dbdir, dbname);
            // Create List of Tables to be created 
            //[Note - As FKs reference keys in Primary Table, Parent needs to be created first, build a ordered list]
            ArrayList tablelist = buildTableList();
            for (int i = 0; i < tablelist.size(); i++) {
                createTable(normalizeTableName(tablelist.get(i).toString()), tablelist.get(i).toString());
            }
        }
    }

    private void createTable(String NormtableName, String ModelTableName) {
        try {
            mLogger.infoNoloc(mLoc.t("PRSR001: Creating Table [ {0}", NormtableName));
            Statement stmt = conn.createStatement();
            stmt.execute(createSQL(NormtableName, ModelTableName));
        } catch (SQLException ex) {
            mLogger.errorNoloc(mLoc.t("PRSR002: global"), ex);
        }
    }

    private String createSQL(String normtablename, String modeltablename) {
        String query = "CREATE EXTERNAL TABLE IF NOT EXISTS " + normtablename + " ( " + createSQLTableColumns(modeltablename) + " ) " + createOrganizationString(normtablename);
        mLogger.infoNoloc(mLoc.t("PRSR002: SQL Executed :{0}", query));
        return query;
    }

    private String createSQLTableColumns(String modeltablename) {
        StringBuilder columns = new StringBuilder();
        HashMap colmap = (HashMap) lookup.getLookupMap().get(modeltablename);
        Iterator iterator = colmap.keySet().iterator();
        String nonQualifiedTableName = modeltablename.substring(modeltablename.lastIndexOf(".") + 1);
        ArrayList<Field> fieldsForTable = lookup.getFields(nonQualifiedTableName);

        while (iterator.hasNext()) {
            String columnname = iterator.next().toString();
            String fdatatype = getFieldDataType(fieldsForTable, columnname);
            if (fdatatype.equals("VARCHAR")) {
                columns.append(columnname + " " + getFieldDataType(fieldsForTable, columnname) + "(" + getFieldSize(fieldsForTable, columnname) + "), ");
            } else {
                columns.append(columnname + " " + getFieldDataType(fieldsForTable, columnname) + ", ");
            }
        }

        // Add QueryManager Default Columns into the database if already not modelled (<TableName + Id>)
        String defaultCol = modeltablename.substring(modeltablename.lastIndexOf(".") + 1) + "Id";
        if (!colmap.containsKey(defaultCol)) {
            columns.append(defaultCol + " " + getDataTypeMapping(PluginDTConstants.datatype) + "(" + PluginDTConstants.datasize + "), ");
        }

        columns.append(createPrimaryKeyConstraint(defaultCol));

        //columns.append(createForeignKeyConstraint(modeltablename));

        return columns.toString();
    }

    private String createPrimaryKeyConstraint(String defaultcol) {
        String PK_Constraint = "CONSTRAINT pk_" + defaultcol.toLowerCase() + " PRIMARY KEY (" + defaultcol + ")";
        return PK_Constraint;
    }

    private String createForeignKeyConstraint(String modeltablename) {
        String tablename = modeltablename.substring(modeltablename.lastIndexOf(".") + 1);
        if (!tablename.equals(lookup.getRootName())) {
            return ", CONSTRAINT fk_" + lookup.getRootName().toLowerCase() + "id" + tablename.toLowerCase() + " FOREIGN KEY (" + lookup.getRootName() + "Id" + ") REFERENCES " + normalizeTableName(lookup.getRootName()) + "(" + lookup.getRootName() + "Id" + ")";
        }
        return "";
    }

    private String createOrganizationString(String normtablename) {
        String orgstr = "organization(LOADTYPE='delimited' filename='" + normtablename + ".csv" + "' FIELDDELIMITER='|')";
        return orgstr;
    }

    /**
     * Sets eViewConfig File Object (objectmap.xml conventionally)
     * @param configpath
     * @param configfilename
     * @return boolean
     */
    public boolean setEViewConfigFilePath(String configpath, String configfilename) {
        if (validateDir(configpath)) {
            if (validateFile(configpath, configfilename)) {
                return true;
            } else {
                mLogger.infoNoloc(mLoc.t("PRSR019: Invalid Config File : {0}", configfilename));
            }
        } else {
            mLogger.infoNoloc(mLoc.t("PRSR004: Invalid Directory :{0}", configpath));
        }
        return false;
    }

    private boolean validateDir(String dirpath) {
        File dbdir = new File(dirpath);
        if (dbdir.exists()) {
            if (dbdir.isDirectory()) {
                return true;
            } else {
                mLogger.infoNoloc(mLoc.t("PRSR005: Directory is not a valid dir :{0}", dirpath));
            }
        } else {
            mLogger.infoNoloc(mLoc.t("PRSR025: Directory is not a valid dir :{0}", dirpath));
        }
        return false;
    }

    private boolean validateFile(String filepath, String filename) {
        boolean ret = false;
        File dbfile = new File(filepath + PluginDTConstants.fs + filename);
        if (dbfile.exists()) {
            if (dbfile.isFile()) {
                setEviewConfigFile(new File(filepath + PluginDTConstants.fs + filename));
                createObjectDefModel();
                ret = true;
            } else {
                mLogger.infoNoloc(mLoc.t("PRSR007: File does not exist :{0}", filename));
            }
        } else {
            mLogger.infoNoloc(mLoc.t("PRSR008: File [{0} ] does not exist in dir :{1}", filename, filepath));
        }
        return ret;
    }

    private void initDB(String dbdir, String dbname) {
        try {
            Class.forName(PluginDTConstants.DB_DRIVER);
            String uri = PluginDTConstants.URI_PRIFIX + PluginDTConstants.PS + dbname + PluginDTConstants.PS + dbdir;
            conn = DriverManager.getConnection(uri);
        } catch (SQLException ex) {
            mLogger.errorNoloc(mLoc.t("PRSR009: Exception"), ex);
        } catch (ClassNotFoundException ex) {
            mLogger.infoNoloc(mLoc.t("PRSR010: Exception"), ex);
        }
    }

    private static void setEviewConfigFile(File filename) {
        PluginDTConstants.EVIEW_CONFIG_FILE = filename;
    }

    public File getEviewConfigFile() {
        return PluginDTConstants.EVIEW_CONFIG_FILE;
    }

    /*
    public Lookup getLookup(){
    return this.lookup;
    }
     */
    private void createObjectDefModel() {
        AxionFileSystem afs = new AxionFileSystem();
        File configfile = PluginDTConstants.EVIEW_CONFIG_FILE;
        if (configfile != null) {
            if (configfile.exists()) {
                BufferedDataInputStream bdis = null;
                try {
                    bdis = afs.openBufferedDIS(configfile);
                    objDef = new ObjectDefinitionBuilder().parse(bdis);
                    addExtraFieldsToParent(objDef);
                    lookup = Lookup.createLookup(objDef);
                } catch (AxionException ex) {
                    mLogger.infoNoloc(mLoc.t("PRSR018: Error Reading eview config file :{0}", ex.getMessage()));
                } finally {
                    try {
                        bdis.close();
                    } catch (IOException ex) {
                        mLogger.infoNoloc(mLoc.t("PRSR011: Error Closing Axion BufferedDataInputStream :{0}", ex.getMessage()));
                    }
                }
            } else {
                mLogger.infoNoloc(mLoc.t("PRSR012: Unable to find file : {0}", PluginDTConstants.EVIEW_CONFIG_FILE.getAbsolutePath()));
            }
        } else {
            mLogger.infoNoloc(mLoc.t("PRSR013: EView Config File is not available. Set the file using DataSourceReaderFactory.setEViewConfigFilePath()"));

        }
    }

    private static void addExtraFieldsToParent(ObjectDefinition objDef) {
        String[] sysfields = DefaultSystemFields.getDefaultSystemFields();
        for (int i = 0; i < sysfields.length; i++) {
            objDef.addField(i, createExtraFieldObj(sysfields[i]));
        }
    }

    private static Field createExtraFieldObj(String name) {
        Field newfield = new Field();
        newfield.setName(name);
        newfield.setType("string"); //Change Later
        newfield.setSize(32); //Change Later
        return newfield;
    }

    private ArrayList buildTableList() {
        ArrayList tlist = new ArrayList();
        tlist.add(lookup.getRootName());
        Iterator iterator = lookup.getLookupMap().keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if ((key).indexOf(".") != -1) {
                tlist.add(key);
            }
        }
        return tlist;
    }

    private String normalizeTableName(String tableNameModel) {
        String tablename = tableNameModel.substring(tableNameModel.lastIndexOf(".") + 1);
        return PluginDTConstants.QueryManagerTablePrefix + tablename.toUpperCase();
    }

    private String getFieldDataType(ArrayList fields, String colname) {
        for (int i = 0; i < fields.size(); i++) {
            if (((Field) fields.get(i)).getName().equals(colname)) {
                return getDataTypeMapping(((Field) fields.get(i)).getType());
            }
        }
        return null;
    }

    private int getFieldSize(ArrayList fields, String colname) {
        for (int i = 0; i < fields.size(); i++) {
            if (((Field) fields.get(i)).getName().equals(colname)) {
                return ((Field) fields.get(i)).getSize();
            }
        }
        return -1;
    }

    /**
     * This method Maps datatypes defined in eview data model with Relational DB datatypes
     */
    private String getDataTypeMapping(String datatypeModel) {
        for (int i = 0; i < datatypes.length; i++) {
            if (datatypes[i][0].equals(datatypeModel)) {
                return datatypes[i][1];
            }
        }
        return "NULL";
    }
}
