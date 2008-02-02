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
package org.netbeans.modules.hibernate.wizards;

import java.util.HashMap;
import java.util.Map;

/**
 * This class lists all the database dialects, drivers and URLs * 
 * 
 * @author gowri
 */
public class Util {

    private static String[] dialectCodes = new String[]{
        "DB2",
        "DB2",
        "DB2/400",
        "Derby",
        "Firebird",
        "FrontBase",
        "HSQL",
        "Informix",
        "Ingres",
        "Interbase",
        "Mckoi SQL",
        "MySQL(Connector/J driver)",
        "MySQL (InnoDB)",
        "MySQL (MyISAM)",
        "Oracle (Any version)",
        "Oracle 9i/10g",
        "Pointbase",
        "PostgreSQL",
        "Progress",
        "SAP DB",
        "SQL Server",
        "Sybase",
        "Sybase Anywhere"
    };
    private static String[] dialects = new String[]{
        "org.hibernate.dialect.DB2Dialect",
        "org.hibernate.dialect.DB2390Dialect",
        "org.hibernate.dialect.DB2400Dialect",
        "org.hibernate.dialect.derbyDialect",
        "org.hibernate.dialect.FirebirdDialect",
        "org.hibernate.dialect.FrontbaseDialect",
        "org.hibernate.dialect.HSQLDialect",
        "org.hibernate.dialect.InformixDialect",
        "org.hibernate.dialect.IngresDialect",
        "org.hibernate.dialect.InterbaseDialect",
        "org.hibernate.dialect.MckoiDialect",
        "org.hibernate.dialect.MySQLDialect",
        "org.hibernate.dialect.MySQLInnoDBDialect",
        "org.hibernate.dialect.MySQLMyISAMDialect",
        "org.hibernate.dialect.OracleDialect",
        "org.hibernate.dialect.Oracle9Dialect",
        "org.hibernate.dialect.PointbaseDialect",
        "org.hibernate.dialect.PostgreSQLDialect",
        "org.hibernate.dialect.ProgressDialect",
        "org.hibernate.dialect.SAPDBDialect",
        "org.hibernate.dialect.SQLServerDialect",
        "org.hibernate.dialect.SybaseDialect",
        "org.hibernate.dialect.SybaseAnywhereDialect"
    };
    private static String[] drivers = new String[]{
        "org.apache.derby.jdbc.ClientDriver",
        "com.mysql.jdbc.Driver",
        "org.postgresql:Driver"
    };
    private static String[] urlConnections = new String[]{
        "jdbc:derby://localhost:1527/travel",
        "jdbc:mysql:///test",
        "jdbc:postgresql:template1"
    };
    private static Map dialectMap = new HashMap();

    static {
        for (int i = 0; i < dialects.length; i++) {
            dialectMap.put(dialectCodes[i], dialects[i]);
        }
    }
    private static Map driverMap = new HashMap();

    static {
        driverMap.put(dialectCodes[3], drivers[0]);
        driverMap.put(dialectCodes[11], drivers[1]);
        driverMap.put(dialectCodes[17], drivers[2]);
    }
    private static Map urlConnectionMap = new HashMap();

    static {
        urlConnectionMap.put(dialectCodes[3], urlConnections[0]);
        urlConnectionMap.put(dialectCodes[11], urlConnections[1]);
        urlConnectionMap.put(dialectCodes[17], urlConnections[2]);
    }

    public static String[] getDialectCodes() {
        return dialectCodes;
    }

    public static String[] getDrivers() {
        return drivers;

    }

    public static String[] getURLConnections() {
        return urlConnections;
    }

    public static String getSelectedDialect(String code) {
        return (String) dialectMap.get(code);
    }

    public static String getSelectedDriver(String code) {
        return (String) driverMap.get(code);
    }

    public static String getSelectedURLConnection(String code) {
        return (String) urlConnectionMap.get(code);
    }
  
}
