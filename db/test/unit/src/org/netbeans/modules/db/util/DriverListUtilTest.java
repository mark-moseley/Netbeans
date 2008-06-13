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

package org.netbeans.modules.db.util;

import com.sun.j3d.utils.behaviors.interpolators.KBCubicSplineCurve;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.openide.util.NbBundle;

/**
 *
 * @author David
 */
public class DriverListUtilTest extends TestCase {
    private static final String HOST = "myhost";
    private static final String PORT = "8888";
    private static final String DB = "mydb";
    private static final String SERVERNAME = "servername";
    private static final String ADDITIONAL = "foo;bar;baz";
    private static final String SERVICENAME = "servicename";
    private static final String SID = "mysid";
    private static final String DSN = "mydsn";
    private static final String TNSNAME = "mytns";
    
    private static final HashMap<String, String> ALLPROPS = 
            new HashMap<String, String>();
    
    private static final ArrayList<String> STD_SUPPORTED_PROPS =
            new ArrayList<String>();
    
    static {
        ALLPROPS.put(JdbcUrl.TOKEN_HOST, HOST);
        ALLPROPS.put(JdbcUrl.TOKEN_DB, DB);
        ALLPROPS.put(JdbcUrl.TOKEN_PORT, PORT);
        ALLPROPS.put(JdbcUrl.TOKEN_SERVERNAME, SERVERNAME);
        ALLPROPS.put(JdbcUrl.TOKEN_ADDITIONAL, ADDITIONAL);
        ALLPROPS.put(JdbcUrl.TOKEN_DSN, DSN);
        ALLPROPS.put(JdbcUrl.TOKEN_SERVICENAME, SERVICENAME);
        ALLPROPS.put(JdbcUrl.TOKEN_SID, SID);
        ALLPROPS.put(JdbcUrl.TOKEN_TNSNAME, TNSNAME);
        
        STD_SUPPORTED_PROPS.add(JdbcUrl.TOKEN_HOST);
        STD_SUPPORTED_PROPS.add(JdbcUrl.TOKEN_PORT);
        STD_SUPPORTED_PROPS.add(JdbcUrl.TOKEN_DB);
        STD_SUPPORTED_PROPS.add(JdbcUrl.TOKEN_ADDITIONAL);
    }
    
    public DriverListUtilTest(String testName) {
        super(testName);
    }
    
    public void testNonParsedJdbcUrls() throws Exception {
        List<JdbcUrl> urls = DriverListUtil.getJdbcUrls();
        for ( JdbcUrl url : urls ) {
            if (! url.urlIsParsed()) {
                testNonParsedUrl(url);
            }
        }
    }
    
    private JdbcUrl getJdbcUrl(String name, String type) throws Exception {
        List<JdbcUrl> urls = DriverListUtil.getJdbcUrls();
        for (JdbcUrl url : urls) {
            if (url.getName().equals(name) &&
                    isEqual(url.getType(), type)) {
                return url;
            }
        }
        
        throw new Exception("No JdbcUrl found for name " + name + " and type " + type);
    }
        
    private boolean isEqual(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        
        if (o1 == null || o2 == null) {
            return false;
        }
        
        return o1.equals(o2);
    }
    
    public void testJavaDbEmbedded() throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_DB);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.add(JdbcUrl.TOKEN_DB);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_JavaDbEmbedded"), null, "org.apache.derby.jdbc.EmbeddedDriver", 
                "jdbc:derby:<DB>[;<ADDITIONAL>]", supportedProps, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);        
        testUrlString(url, propValues, "jdbc:derby:" + DB + ";" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:derby:" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, "jdbc:derby:");
        testBadUrlString(url, "jdbc:daryb://db");
        testBadUrlString(url, "jdbc:derby/:db;create=true");
    }

    public void testJavaDbNetwork() throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        requiredProps.add(JdbcUrl.TOKEN_DB);
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_JavaDbNetwork"), null, "org.apache.derby.jdbc.ClientDriver", 
                "jdbc:derby://<HOST>[:<PORT>]/<DB>[;<ADDITIONAL>]", STD_SUPPORTED_PROPS, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(STD_SUPPORTED_PROPS);        
        testUrlString(url, propValues, "jdbc:derby://" + HOST + ":" + PORT + "/" + DB + ";" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:derby://" + HOST + ":" + PORT + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, propValues, "jdbc:derby://" + HOST + "/" + DB);  
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, propValues);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_DB, DB);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, "jdbc:derby:///db");
        testBadUrlString(url, "jdbc:derby://localhost");
        testBadUrlString(url, "jdbc:derby://localhost/;create=true");
        testBadUrlString(url, "jdbc:derby:/localhost:8889/db;create=true");
    }

    public void testMySQL() throws Exception {
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_MySQL"), null, "com.mysql.jdbc.Driver", 
                "jdbc:mysql://[<HOST>[:<PORT>]]/[<DB>][?<ADDITIONAL>]",
                STD_SUPPORTED_PROPS, new ArrayList<String>());
        
        HashMap<String, String> propValues = buildPropValues(STD_SUPPORTED_PROPS);
        
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        testUrlString(url, propValues, "jdbc:mysql:///" + DB); 
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        testUrlString(url, propValues, "jdbc:mysql:///");
        
        propValues.put(JdbcUrl.TOKEN_HOST, HOST);
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + "/");
        
        propValues.put(JdbcUrl.TOKEN_PORT, PORT);
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + ":" + PORT + "/");
        
        propValues.put(JdbcUrl.TOKEN_ADDITIONAL, ADDITIONAL);
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + ":" + PORT + "/?" + ADDITIONAL);
    }
    
    enum OracleTypes { THIN, OCI, OCI8 };
    
    public void testOracleThinSID() throws Exception {
        testOracleSID(OracleTypes.THIN);
    }
    
    public void testOracleOciSID() throws Exception {
        testOracleSID(OracleTypes.OCI);
    }
    
    public void testOracleOci8SID() throws Exception {
        testOracleSID(OracleTypes.OCI8);
    }
    
    public void testOracleThinServiceName() throws Exception {
        testOracleServiceName(OracleTypes.THIN);
    }
    public void testOracleOciServiceName() throws Exception {
        testOracleServiceName(OracleTypes.OCI);
    }
    public void testOracleOci8ServiceName() throws Exception {
        testOracleServiceName(OracleTypes.OCI8);
    }
    
    public void testOracleThinTnsName() throws Exception {
        testOracleTnsName(OracleTypes.THIN);
    }
    public void testOracleOciTnsName() throws Exception {
        testOracleTnsName(OracleTypes.OCI);
    }

    private JdbcUrl checkOracleUrl(OracleTypes otype, String urlSuffix, String type,
            List<String> supportedProps, List<String> requiredProps) throws Exception {
        String driverClass;
        String driverName;
        
        switch (otype) {
            case THIN:
                driverClass = "oracle.jdbc.OracleDriver";
                driverName = getDriverName("DRIVERNAME_OracleThin");
                break;
            case OCI:
                driverClass = "oracle.jdbc.driver.OracleDriver";
                driverName = getDriverName("DRIVERNAME_OracleOCI");
                break;
            case OCI8:
                driverClass = "oracle.jdbc.driver.OracleDriver";
                driverName = getDriverName("DRIVERNAME_OracleOCI");
                type = "OCI8 " + type;
                break;
            default:
                throw new Exception("Unknown Oracle Type " + otype);                
        }
        
        String prefix = getOracleUrlPrefix(otype);        
        
        return checkUrl(driverName, type, driverClass, 
                prefix + urlSuffix, supportedProps, requiredProps);
     
    }
    
    private String getOracleUrlPrefix(OracleTypes otype) {
        String prefix = "jdbc:oracle:";
        switch (otype) {
            case THIN:
                prefix = prefix + "thin";
                break;
            case OCI:
                prefix = prefix  + "oci";
                break;
            case OCI8:
                prefix = prefix + "oci8";
                break;
        }
        
        prefix = prefix + ":@";
        return prefix;
    }
    
    private void testOracleSID(OracleTypes otype) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        requiredProps.add(JdbcUrl.TOKEN_SID);
        requiredProps.add(JdbcUrl.TOKEN_PORT);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.addAll(requiredProps);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        
        JdbcUrl url = checkOracleUrl(otype, "<HOST>:<PORT>:<SID>[?<ADDITIONAL>]", getType("TYPE_SID"),
                supportedProps, requiredProps);

        String prefix = getOracleUrlPrefix(otype);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);
        
        testUrlString(url, propValues, prefix + HOST + ":" + PORT + ":" + SID + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, prefix + HOST + ":" + PORT + ":" + SID);
                
        propValues.remove(JdbcUrl.TOKEN_SID);
        testMissingParameter(url, propValues);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_SID, SID);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, prefix + ":db");
        testBadUrlString(url, prefix);
    }
    
    private void testOracleServiceName(OracleTypes otype) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        requiredProps.add(JdbcUrl.TOKEN_SERVICENAME);
        requiredProps.add(JdbcUrl.TOKEN_PORT);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.addAll(requiredProps);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        
        JdbcUrl url = checkOracleUrl(otype, "//<HOST>:<PORT>/<SERVICE>[?<ADDITIONAL>]", getType("TYPE_Service"),
                supportedProps, requiredProps);

        String prefix = getOracleUrlPrefix(otype);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);
        
        testUrlString(url, propValues, prefix + "//" + HOST + ":" + PORT + "/" + SERVICENAME + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, prefix + "//" + HOST + ":" + PORT + "/" + SERVICENAME);
                
        propValues.remove(JdbcUrl.TOKEN_SERVICENAME);
        testMissingParameter(url, propValues);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_SERVICENAME, SERVICENAME);
        testMissingParameter(url, propValues);
        

        propValues.put(JdbcUrl.TOKEN_HOST, HOST);
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testMissingParameter(url, propValues);

        testBadUrlString(url, prefix + ":db");
        testBadUrlString(url, prefix);
    }
    private void testOracleTnsName(OracleTypes otype) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_TNSNAME);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.addAll(requiredProps);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        
        JdbcUrl url = checkOracleUrl(otype, "<TNSNAME>[?<ADDITIONAL>]", getType("TYPE_TNSName"),
                supportedProps, requiredProps);

        String prefix = getOracleUrlPrefix(otype);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);
        
        testUrlString(url, propValues, prefix + TNSNAME + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, prefix + TNSNAME);
                
        propValues.remove(JdbcUrl.TOKEN_TNSNAME);
        testMissingParameter(url, propValues);

        testBadUrlString(url, prefix);
    }

    private HashMap<String,String> buildPropValues(List<String> supportedProps) {
        HashMap<String, String> propValues = new HashMap<String,String>();
        for (String prop : ALLPROPS.keySet()) {
            if (supportedProps.contains(prop)) {
                propValues.put(prop, ALLPROPS.get(prop));
            }
        }
        
        return propValues;        
    }
    private static String getDriverName(String key) {
        return NbBundle.getMessage(DriverListUtil.class, key);
    }

    private static String getType(String typeKey) {
        return NbBundle.getMessage(DriverListUtil.class, typeKey);
    }


    private void testNonParsedUrl(JdbcUrl url) throws Exception {
        String urlString = "foo:bar:my.url";
        url.setUrl(urlString);
        assertEquals(url.getUrl(), urlString);
    }
    
    private JdbcUrl checkUrl(String name, String type, String className,
            String template, List<String> supportedTokens, List<String> requiredTokens) throws Exception {
        JdbcUrl url = getJdbcUrl(name, type);
        assertEquals(name, url.getName());
        assertEquals(type, url.getType());
        
        if (type == null) {
            assertEquals(name, url.getDisplayName());
        } else {
            assertEquals(name + " (" + type + ")", url.getDisplayName());
        }
        
        assertEquals(className, url.getClassName());
        assertEquals(template, url.getUrlTemplate());
        
        JdbcUrl other = new JdbcUrl(url.getName(), url.getClassName(),
                url.getType(), url.getUrlTemplate(), url.urlIsParsed());
        
        assertEquals(url, other);

        checkSupportedTokens(url, supportedTokens);
        checkRequiredTokens(url, requiredTokens);
        
        return url;
    }

    public void testPostgreSQL() throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_DB);
        
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_PostgreSQL"), null, "org.postgresql.Driver", 
                "jdbc:postgresql:[//<HOST>[:<PORT>]/]<DB>[?<ADDITIONAL>]",
                STD_SUPPORTED_PROPS, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(STD_SUPPORTED_PROPS);        
        testUrlString(url, propValues, "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, propValues, "jdbc:postgresql://" + HOST + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        testUrlString(url, propValues, "jdbc:postgresql:" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, "jdbc:postgresql:");
        testBadUrlString(url, "jdbc:postgresql:///" + DB);
    }
    
    private void testUrlString(JdbcUrl url, Map<String, String> props, String urlString) throws Exception {
        url.clear();
        url.putAll(props);
        assertEquals(urlString, url.getUrl());
        
        url.clear();
        
        url.setUrl(urlString);
        for (String prop : props.keySet()) {
            assertEquals(props.get(prop), url.get(prop));
        }
    }
    
    private void testMissingParameter(JdbcUrl url, HashMap<String, String> props) {
        url.clear();
        url.putAll(props);        
        
        assertEquals("", url.getUrl());
    }

    private void testBadUrlString(JdbcUrl url, String urlString) {
        boolean shouldHaveFailed = false;
        try {
          url.setUrl(urlString);
          shouldHaveFailed = true;
        } catch (Throwable t) {
            if (! (t instanceof MalformedURLException)) {
                fail("Should have thrown a MalformedURLException");
            }
        }
        
        if (shouldHaveFailed) {
            fail("Should have thrown an exception");
        }
    }


    private void checkSupportedTokens(JdbcUrl url, List<String> expected) {       
        for (String token : ALLPROPS.keySet()) {
            if (expected.contains(token)) {
                assertTrue(url.supportsToken(token));
            } else {
                assertFalse(url.supportsToken(token));
                assertFalse(url.requiresToken(token));
            }
        }
    }

    private void checkRequiredTokens(JdbcUrl url, List<String> expected) { 
        for (String token : ALLPROPS.keySet()) {
            if (expected.contains(token)) {
                assertTrue(url.requiresToken(token));
                assertTrue(url.supportsToken(token));
            } else {
                assertFalse(url.requiresToken(token));
            }
        }
    }
    
}
