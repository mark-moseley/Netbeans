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

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 *
 * @author Peter Williams
 */
public class DbUtil {

    private static final String __DatabaseVendor = "database-vendor";
    private static final String __DatabaseName = "databaseName";
    private static final String __Url = "URL";
    private static final String __User = "User";
    private static final String __Password = "Password";
    private static final String __NotApplicable = "NA";
    private static final String __IsXA = "isXA";  
    private static final String __IsCPExisting = "is-cp-existing";
    
    private static final String __DerbyDatabaseName = "DatabaseName";
    private static final String __DerbyPortNumber = "PortNumber";
    private static final String __ServerName = "serverName";
    private static final String __InformixHostName = "IfxIFXHOST";
    private static final String __InformixServer = "InformixServer";
    private static final String __DerbyConnAttr = "connectionAttributes";
    
    private static final String __PortNumber = "portNumber";
    private static final String __SID = "SID";
    private static final String __DriverClass = "driverClass";    
    
    
    static final String[] VendorsDBNameProp = {
        "sun_db2", "sun_oracle", "sun_msftsql", "db2", "microsoft_sql", 
        "post-gre-sql", "mysql", "datadirect_sql", "datadirect_db2",
        "datadirect_informix", "datadirect_sybase", "datadirect_oracle",
        "jtds_sql", "jtds_sybase", "informix"
    };
    
    static final String[] Reqd_DBName = {
        "sun_db2", "sun_msftsql", "datadirect_sql", "microsoft_sql", 
        "datadirect_db2", "datadirect_informix", "datadirect_sybase"
    };
    
    static Map<String, String> normalizePoolMap(Map<String, String> poolValues) {
        String driverClassName = poolValues.get("dsClassName"); //NOI18N
        String resType = poolValues.get("resType"); //NOI18N
        String url = ""; //NOI18N
        String serverName = poolValues.get(__ServerName);
        String portNo = poolValues.get(__DerbyPortNumber);
        String dbName = poolValues.get(__DerbyDatabaseName);
        String dbVal = poolValues.get(__DatabaseName);
        String portVal = poolValues.get(__PortNumber);
        String sid = poolValues.get(__SID);
        String urlValue = poolValues.get(__Url);
        String driverClass = poolValues.get(__DriverClass);
        String derbyConnAttr = poolValues.get(__DerbyConnAttr);
        
        if(driverClassName.indexOf("pointbase") != -1){
            url = poolValues.get(__DatabaseName);
        }
        if (urlValue == null || urlValue.equals("")) { //NOI18N
            if (driverClassName.indexOf("derby") != -1) {
                if (serverName != null) {
                    url = "jdbc:derby://" + serverName;
                    if (portNo != null && portNo.length() > 0) {
                        url = url + ":" + portNo; //NOI18N
                    }   
                    url = url + "/" + dbName; //NOI18N
                    if(derbyConnAttr != null && (! derbyConnAttr.equals(""))) { //NOI18N
                        url = url + derbyConnAttr;
                    }
                }
            } else {
                String in_url = poolValues.get(__Url);
                if (in_url != null) {
                    url = in_url;
                }
                if (url.equals("")) {  //NOI18N
                    String urlPrefix = DriverMaps.getUrlPrefix(driverClassName, resType);
                    // !PW FIXME no access to vendor name yet.
//                    String vName = ResourceConfigurator.getDatabaseVendorName(urlPrefix, null);
                    String vName = "Unknown";
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, 
                            "Unable to compute database vendor name for datasource url.");
                    if (serverName != null) {
                        if (vName.equals("sun_oracle")) {    //NOI18N
                            url = urlPrefix + serverName;
                        } else {
                            url = urlPrefix + "//" + serverName; //NOI18N
                        }
                        if (portVal != null && portVal.length() > 0) {
                            url = url + ":" + portVal; //NOI18N
                        }
                    }
                    if (vName.equals("sun_oracle") || vName.equals("datadirect_oracle")) {  //NOI18N
                        url = url + ";SID=" + sid; //NOI18N
                    } else if (Arrays.asList(Reqd_DBName).contains(vName)) {
                        url = url + ";databaseName=" + dbVal; //NOI18N
                    } else if (Arrays.asList(VendorsDBNameProp).contains(vName)) {
                        url = url + "/" + dbVal; //NOI18N
                    }
                }
            }
        } else {
            url = urlValue;
        }
        
        if(driverClass == null || driverClass.equals("")) { //NOI18N
            DatabaseConnection databaseConnection = getDatabaseConnection(url);
            if (databaseConnection != null) {
                driverClass = databaseConnection.getDriverClass();
            } else {
                //Fix Issue 78212 - NB required driver classname
                String drivername = DriverMaps.getDriverName(url);
                if (drivername != null) {
                    driverClass = drivername;
                }
            }
        }
        
        poolValues.put(__Url, url);
        poolValues.put(__DriverClass, driverClass);
        
        return poolValues;
    }
    
    private static DatabaseConnection getDatabaseConnection(String url) {
        DatabaseConnection [] dbConns = ConnectionManager.getDefault().getConnections();
        for(int i = 0; i < dbConns.length; i++) {
            String dbConnUrl = dbConns[i].getDatabaseURL();
            if(dbConnUrl.startsWith(url)) {
                return dbConns[i];
            }
        }
        return null;
    }
    
    public static final boolean notEmpty(String testedString) {
        return (testedString != null) && (testedString.length() > 0);
    }
    
    public static final boolean strEmpty(String testedString) {
        return testedString == null || testedString.length() == 0;
    }
    
    public static final boolean strEquals(String one, String two) {
        boolean result = false;
        
        if(one == null) {
            result = (two == null);
        } else {
            if(two == null) {
                result = false;
            } else {
                result = one.equals(two);
            }
        }
        return result;
    }

    public static final boolean strEquivalent(String one, String two) {
        boolean result = false;
        
        if(strEmpty(one) && strEmpty(two)) {
            result = true;
        } else if(one != null && two != null) {
            result = one.equals(two);
        }
        
        return result;
    }
    
    public static final int strCompareTo(String one, String two) {
        int result;
        
        if(one == null) {
            if(two == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if(two == null) {
                result = 1;
            } else {
                result = one.compareTo(two);
            }
        }
        
        return result;
    }
    
}
