/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.db;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Collect all the vendor name manipulation in one place.  Refactor it ASAP.
 *
 * @author Peter Williams
 */
public class VendorNameMgr {

    private final static char BLANK = ' ';
    private final static char DOT   = '.';
    private final static char []	ILLEGAL_FILENAME_CHARS	= {'/', '\\', ':', '*', '?', '"', '<', '>', '|', ',', '=', ';' };

    private final static String ILLEGAL_CHARS_PATTERN = "[/\\:*\"<>|,=; \\.]"; // NOI18N
    private final static String REPLACEMENT_PATTERN = "_"; // NOI18N
    

    /**
     * Determine the proper vendor name for the database url passed in.  If
     * no vendor name can be determined, generate something valid from the url.
     * 
     * @param url database url for a datasource
     * @return vendor name (or filename legal string) for the database vendor
     *   associated with that url.
     */
    public static String vendorNameFromDbUrl(String url) {
        url = stripExtraDBInfo(url);
        String vendorName = getDatabaseVendorName(url);
        if(vendorName != null) {
            if(vendorName.length() > 0) { // NOI18N
                if(!isFriendlyFilename(vendorName)) {
                    vendorName = makeLegalFilename(vendorName);
                }
            } else {
                vendorName = makeShorterLegalFilename(url);
            }
        }
        return vendorName;
    }
    
    /**
     * Determine the standard datasource classname for this vendor
     * 
     * @param vendorName vendor name to lookup
     * @return standard datasource classname for this vendor, or null if unknown.
     */
    public static String dsClassNameFromVendorName(String vendorName) {
        return vendorName != null ? vendorNameToDscnMap.get(vendorName) : null;
    }
    
    
    private static boolean isLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++) {
            if(filename.indexOf(ILLEGAL_FILENAME_CHARS[i]) >= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean isFriendlyFilename(String filename) {
        if(filename.indexOf(BLANK) >= 0 || filename.indexOf(DOT) >= 0) {
            return false;
        }
        
        return isLegalFilename(filename);
    }
    
    private static String makeLegalFilename(String filename) {
        return filename.replaceAll(ILLEGAL_CHARS_PATTERN, REPLACEMENT_PATTERN);
    }
    
    private static String makeShorterLegalFilename(String filename) {
        //To clean up the default generation a little
        int separatorIndex = filename.indexOf("://"); // NOI18N
        if(separatorIndex != -1) { // NOI18N
            filename = filename.substring(0, separatorIndex) + 
                    "_" +  // NOI18N
                    filename.substring(separatorIndex + 3);
        }
        separatorIndex = filename.indexOf("//"); // NOI18N
        if(separatorIndex != -1) { // NOI18N
            filename = filename.substring(0, separatorIndex) + 
                    "_" +  // NOI18N
                    filename.substring(separatorIndex + 2);
        }
        filename = makeLegalFilename(filename);
        
        return filename;
    }    
    
    
    static String stripExtraDBInfo(String dbConnectionString) {
        int bracketIndex = dbConnectionString.indexOf("["); // NOI18N
        if(bracketIndex != -1) { 
            dbConnectionString = dbConnectionString.substring(0, bracketIndex).trim();
        }
        return dbConnectionString;
    }
    
    private static String getDatabaseVendorName(String url) {
        String vendorName = "";
        for(Entry<String, String> entry : vendorNameToUrlMap.entrySet()) {
            if(url.indexOf(entry.getValue()) != -1){
                vendorName = entry.getKey();
                break;
            }
        }
        return vendorName;
    }
    
    // vendor name -> datasource classname mapping.
    // XXX From CPWizard.xml until that can be refactored
    // XXX Using linked hashmap due to order dependency between derby urls & reverse lookup.
    // 
    private static Map<String, String> vendorNameToDscnMap = new LinkedHashMap<String, String>();
    
    static {
        vendorNameToDscnMap.put("oracle-thin", "oracle.jdbc.pool.OracleDataSource");
        vendorNameToDscnMap.put("derby_net", "org.apache.derby.jdbc.ClientDataSource");
        vendorNameToDscnMap.put("sun_db2", "com.sun.sql.jdbcx.db2.DB2DataSource");
        vendorNameToDscnMap.put("sun_msftsql", "com.sun.sql.jdbcx.sqlserver.SQLServerDataSource");
        vendorNameToDscnMap.put("sun_oracle", "com.sun.sql.jdbcx.oracle.OracleDataSource");
        vendorNameToDscnMap.put("sun_sybase", "com.sun.sql.jdbcx.sybase.SybaseDataSource");
        vendorNameToDscnMap.put("post-gre-sql", "org.postgresql.ds.PGSimpleDataSource");
        vendorNameToDscnMap.put("microsoft_sql", "com.microsoft.sqlserver.jdbc.SQLServerDataSource");
        vendorNameToDscnMap.put("jtds_sql", "net.sourceforge.jtds.jdbcx.JtdsDataSource");
        vendorNameToDscnMap.put("jtds_sybase", "net.sourceforge.jtds.jdbcx.JtdsDataSource");
        vendorNameToDscnMap.put("oracle", "oracle.jdbc.pool.OracleDataSource");
        vendorNameToDscnMap.put("db2", "com.ibm.db2.jcc.DB2DataSource");
        vendorNameToDscnMap.put("datadirect_sql", "com.ddtek.jdbcx.sqlserver.SQLServerDataSource");
        vendorNameToDscnMap.put("datadirect_oracle", "com.ddtek.jdbcx.oracle.OracleDataSource");
        vendorNameToDscnMap.put("datadirect_db2", "com.ddtek.jdbcx.db2.DB2DataSource");
        vendorNameToDscnMap.put("datadirect_informix", "com.ddtek.jdbcx.informix.InformixDataSource");
        vendorNameToDscnMap.put("datadirect_sybase", "com.ddtek.jdbcx.sybase.SybaseDataSource");
        vendorNameToDscnMap.put("sybase2", "com.sybase.jdbc2.jdbc.SybDataSource");
        vendorNameToDscnMap.put("pointbase", "com.pointbase.jdbc.jdbcDataSource");
        vendorNameToDscnMap.put("cloudscape", "com.cloudscape.core.BasicDataSource");
        vendorNameToDscnMap.put("informix", "com.informix.jdbcx.IfxDataSource");
        vendorNameToDscnMap.put("mysql", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        vendorNameToDscnMap.put("jdbc-odbc-bridge", "sun.jdbc.odbc.JdbcOdbcDriver");
        vendorNameToDscnMap.put("sql-server", "weblogic.jdbc.mssqlserver4.Driver");
    }
    
    // Url fragment -> vendor name mapping.
    // XXX From CPWizard.xml until that can be refactored
    // XXX Using linked hashmap due to order dependency between derby urls & reverse lookup.
    // 
    private static Map<String, String> vendorNameToUrlMap = new LinkedHashMap<String, String>();
    
    static {
        vendorNameToUrlMap.put("oracle-thin", "jdbc:oracle:thin");
        vendorNameToUrlMap.put("derby_net", "jdbc:derby://");
        vendorNameToUrlMap.put("derby_embedded", "jdbc:derby:");
        vendorNameToUrlMap.put("sun_db2", "jdbc:sun:db2:");
        vendorNameToUrlMap.put("sun_msftsql", "jdbc:sun:sqlserver:");
        vendorNameToUrlMap.put("sun_oracle", "jdbc:sun:oracle:");
        vendorNameToUrlMap.put("sun_sybase", "jdbc:sun:sybase:");
        vendorNameToUrlMap.put("post-gre-sql", "jdbc:postgresql:");
        vendorNameToUrlMap.put("microsoft_sql", "jdbc:sqlserver:");
        vendorNameToUrlMap.put("jtds_sql", "jdbc:jtds:sqlserver:");
        vendorNameToUrlMap.put("jtds_sybase", "jdbc:jtds:sybase:");
        vendorNameToUrlMap.put("oracle", "jdbc:oracle:oci8:");
        vendorNameToUrlMap.put("db2", "jdbc:db2:");
        vendorNameToUrlMap.put("jdbc-odbc-bridge", "jdbc:odbc:");
        vendorNameToUrlMap.put("sql-server", "jdbc:weblogic:mssqlserver4:");
        vendorNameToUrlMap.put("sybase2", "jdbc:sybase:Tds:");
        vendorNameToUrlMap.put("cloudscape", "jdbc:cloudscape:");
        vendorNameToUrlMap.put("informix", "jdbc:informix-sqli:");
        vendorNameToUrlMap.put("mysql", "jdbc:mysql:");
        vendorNameToUrlMap.put("pointbase", "jdbc:pointbase:");
        vendorNameToUrlMap.put("datadirect_sql", "jdbc:datadirect:sqlserver:");
        vendorNameToUrlMap.put("datadirect_oracle", "jdbc:datadirect:oracle:");
        vendorNameToUrlMap.put("datadirect_db2", "jdbc:datadirect:db2:");
        vendorNameToUrlMap.put("datadirect_informix", "jdbc:datadirect:informix:");
        vendorNameToUrlMap.put("datadirect_sybase", "jdbc:datadirect:sybase:");
        vendorNameToUrlMap.put("as400", "jdbc:as400:");
    }
}
