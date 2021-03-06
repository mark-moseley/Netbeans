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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.rest;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.modules.ws.qaf.WebServicesTestBase;
import org.netbeans.modules.ws.qaf.utilities.ContentComparator;
import org.netbeans.modules.ws.qaf.utilities.FilteringLineDiff;
import org.netbeans.modules.ws.qaf.utilities.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.xml.sax.SAXException;

/**
 * Base class for REST tests
 * @author lukas
 */
public abstract class RestTestBase extends WebServicesTestBase {

    private static final String HOSTNAME = "localhost"; //NOI18N
    private static final int PORT = resolveServerPort();
    private static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver"; //NOI18N
    private static final String JDBC_URL = "jdbc:derby://localhost:1527/sample"; //NOI18N
    private static final String DB_USERNAME = "app"; //NOI18N
    private static final String DB_PASSWORD = "app"; //NOI18N
    private WebConversation wc;
    private Connection connection;

    private static boolean CREATE_GOLDEN_FILES;
    private static final Logger LOGGER = Logger.getLogger(RestTestBase.class.getName());

    /**
     * Enum type to hold supported Mime Types
     */
    protected enum MimeType {

        APPLICATION_XML,
        APPLICATION_JSON,
        TEXT_PLAIN,
        TEXT_HTML;

        @Override
        public String toString() {
            switch (this) {
                case APPLICATION_XML:
                    return "application/xml"; //NOI18N
                case APPLICATION_JSON:
                    return "application/json"; //NOI18N
                case TEXT_PLAIN:
                    return "text/plain"; //NOI18N
                case TEXT_HTML:
                    return "text/html"; //NOI18N
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    public RestTestBase(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        wc = new WebConversation();
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException ex) {
        } catch (ClassNotFoundException cnfe) {
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        wc = null;
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
        }
    }

    /**
     * Deploy a project returned by <code>getProjectName()</code>
     */
    public void testDeploy() throws IOException {
        deployProject(getProjectName());
    }

    /**
     * Undeploy a project returned by <code>getProjectName()</code>
     */
    public void testUndeploy() throws IOException {
        undeployProject(getProjectName());
    }

    /**
     * Close project
     */
    public void testCloseProject() {
        // Close
        String close = Bundle.getStringTrimmed("org.netbeans.core.ui.Bundle", "LBL_Close");
        getProjectRootNode().performPopupAction(close);
    }

    /**
     * Helper method to get folder containing data for Rest tests
     * (&lt;dataDir&gt/resources)
     */
    protected File getRestDataDir() {
        return new File(getDataDir(), "resources"); //NOI18N
    }

    /**
     * Helper method to get RESTful Web Services node
     * 
     * @return RESTful Web Services node
     */
    protected Node getRestNode() {
        String restNodeLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.nodes.Bundle", "LBL_RestServices");
        Node restNode = new Node(getProjectRootNode(), restNodeLabel);
        if (restNode.isCollapsed()) {
            restNode.expand();
        }
        return restNode;
    }

    /**
     * Helper method to get URL on which current project will become available
     */
    protected String getRestAppURL() {
        return "http://" + HOSTNAME + ":" + PORT + "/" + getProjectName(); //NOI18N
    }

    /**
     * Helper method to get URL on which REST services from current project will
     * become available
     */
    protected String getResourcesURL() {
        return getRestAppURL() + "/resources"; //NOI18N
    }

    /**
     * Get instance of used <code>WebConversation</code>
     *
     * @return instance of <code>WebConversation</code>
     */
    protected WebConversation getWebConversation() {
        return wc;
    }

    /**
     * Run HTTP GET request on given <code>url</code> with given <code>mimeType</code>
     *
     * @param url where to send a request
     * @param mimeType mime type to be used
     * @return response of the request
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    protected WebResponse doGet(String url, MimeType mimeType) throws MalformedURLException, IOException, SAXException {
        WebRequest request = new GetMethodWebRequest(url);
        request.setHeaderField("Accept", mimeType.toString()); //NOI18N
        return getWebConversation().getResponse(request);
    }

    /**
     * Run HTTP POST request on given <code>url</code> with given <code>mimeType</code>
     *
     * @param url where to send a request
     * @param is InputStream containing data to be send within the request
     * @param mimeType mime type to be used
     * @return response of the request
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    protected WebResponse doPost(String url, InputStream is, MimeType mimeType) throws MalformedURLException, IOException, SAXException {
        PostMethodWebRequest request = new PostMethodWebRequest(url, is, mimeType.toString());
        request.setHeaderField("Accept", mimeType.toString()); //NOI18N
        return getWebConversation().getResponse(request);
    }

    /**
     * Run HTTP PUT request on given <code>url</code> with given <code>mimeType</code>
     *
     * @param url where to send a request
     * @param is InputStream containing data to be send within the request
     * @param mimeType mime type to be used
     * @return response of the request
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    protected WebResponse doPut(String url, InputStream is, MimeType mimeType) throws MalformedURLException, IOException, SAXException {
        WebRequest request = new PutMethodWebRequest(url, is, mimeType.toString());
        request.setHeaderField("Accept", mimeType.toString()); //NOI18N
        return getWebConversation().getResponse(request);
    }

    /**
     * Run HTTP DELETE request on given <code>url</code> with given <code>mimeType</code>
     *
     * @param url where to send a request
     * @param mimeType mime type to be used
     * @return response of the request
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    protected WebResponse doDelete(String url, MimeType mimeType) throws MalformedURLException, IOException, SAXException {
        WebRequest request = new DeleteMethodWebRequest(url);
        request.setHeaderField("Accept", mimeType.toString()); //NOI18N
        return getWebConversation().getResponse(request);
    }

    /**
     * Run given <code>query</code> against sample database
     * @param query query to run
     * @return result of given <code>query</code>
     * @throws java.sql.SQLException
     */
    protected ResultSet doQuery(String query) throws SQLException {
        return connection.createStatement().executeQuery(query);
    }

    /**
     * Create Rest test client under Web Pages in tested project so we're able
     * to test it
     * <strong>Note</strong>: This method uses reflection to call appropriate
     * method from RestSuport class because RESTful plugin is not part of
     * standart NetBeans distribution yet
     *
     * @throws java.io.IOException
     */
    protected void prepareRestClient() throws IOException {
        //generate Rest test client into the <projectDir>/web directory
        Class c = null;
        try {
            c = Class.forName("org.netbeans.modules.websvc.rest.spi.RestSupport");
        } catch (ClassNotFoundException cnfe) {
            fail("Cannot find RestSupport class. Please make sure RESTful Web Services plugin is installed.");
        }
        assertNotNull(c);
        Object o = getProject().getLookup().lookup(c);
        File restClientFolder = new File(FileUtil.toFile(getProject().getProjectDirectory()), "web"); //NOI18N
        try {
            Method m = c.getMethod("generateTestClient", File.class);
            m.invoke(o, restClientFolder);
        } catch (IllegalAccessException iae) {
            LOGGER.info(iae.getMessage());
        } catch (InvocationTargetException ite) {
            LOGGER.info(ite.getMessage());
        } catch (NoSuchMethodException nsme) {
            LOGGER.info(nsme.getMessage());
        }
        Field f = null;
        File restClient= null;
        try {
            f = c.getDeclaredField("TEST_RESBEANS_HTML"); //NOI18N
            restClient = new File(restClientFolder, (String) f.get(o));
        } catch (IllegalAccessException iae) {
            LOGGER.info(iae.getMessage());
        } catch (NoSuchFieldException nsfe) {
            LOGGER.info(nsfe.getMessage());
        }
        //make sure the test client has been generated
        assertNotNull(f);
        assertNotNull(restClient);
        assertTrue(restClient.exists());
        assertTrue(restClient.isFile());
        //replace "___BASE_URL___" with proper URL of generated Rest resources
        //(this is usually done by "Test RESTful Web Services" action; this action
        // also opens browser, but this is something what we don't want to do
        // in tests)
        FileObject restClientFO = FileUtil.toFileObject(FileUtil.normalizeFile(restClient));
        DataObject dobj = DataObject.find(restClientFO);
        assertNotNull(dobj);
        EditorCookie editorCookie = dobj.getCookie(EditorCookie.class);
        assertNotNull(editorCookie);
        editorCookie.open();
        try {
            EditorOperator eo = new EditorOperator((String) f.get(o));
            assertNotNull(eo);
            f = c.getDeclaredField("BASE_URL_TOKEN"); //NOI18N
            assertNotNull(f);
            eo.replace((String) f.get(o), getRestAppURL() + "/"); //NOI18N
            eo.close(true);
        } catch (IllegalAccessException iae) {
            LOGGER.info(iae.getMessage());
        } catch (NoSuchFieldException nsfe) {
            LOGGER.info(nsfe.getMessage());
        }
    }

    /**
     * Check files against golden files.
     *
     *@param newFiles files to check
     */
    protected void checkFiles(Set<File> newFiles) {
        // save all instead of timeout
        new SaveAllAction().performAPI();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        if (!CREATE_GOLDEN_FILES) {
            Set<String> set = new HashSet<String>(newFiles.size() / 2);
            for (Iterator<File> i = newFiles.iterator(); i.hasNext();) {
                File newFile = i.next();
                File goldenFile = null;
                try {
                    goldenFile = getGoldenFile(getName() + "/" + newFile.getName() + ".pass"); //NOI18N
                    if (newFile.getName().endsWith(".xml") //NOI18N
                            && !newFile.getName().startsWith("sun-") //NOI18N
                            && !newFile.getName().startsWith("webservices.xml")) { //NOI18N
                        assertTrue(ContentComparator.equalsXML(goldenFile, newFile));
                    } else {
                        assertFile(goldenFile, newFile,
                                new File(getWorkDirPath(), newFile.getName() + ".diff"), //NOI18N
                                new FilteringLineDiff());
                    }
                } catch (Throwable t) {
                    goldenFile = getGoldenFile(getName() + "/" + newFile.getName() + ".pass"); //NOI18N
                    Utils.copyFile(newFile, new File(getWorkDirPath(), newFile.getName() + ".bad.txt")); //NOI18N
                    Utils.copyFile(goldenFile,
                            new File(getWorkDirPath(), newFile.getName() + ".gf.txt")); //NOI18N
                    set.add(newFile.getName());
                }
            }
            assertTrue("File(s) " + set.toString() + " differ(s) from golden files.", set.isEmpty()); //NOI18N
        } else {
            createGoldenFiles(newFiles);
        }
    }

    private void createGoldenFiles(Set<File> from) {
        File f = getDataDir();
        List<String> names = new ArrayList<String>();
        names.add("goldenfiles"); //NOI18N
        while (!f.getName().equals("test")) { //NOI18N
            if (!f.getName().equals("sys") //NOI18N
                    && !f.getName().equals("work") //NOI18N
                    && !f.getName().equals("tests")) { //NOI18N
                names.add(f.getName());
            }
            f = f.getParentFile();
        }
        f = new File(f, "qa-functional/data/goldenfiles"); //NOI18N
        f = new File(f, getClass().getName().replace('.', File.separatorChar));
        File destDir = new File(f, getName());
        destDir.mkdirs();
        for (Iterator<File> i = from.iterator(); i.hasNext();) {
            File src = i.next();
            Utils.copyFile(src, new File(destDir, src.getName() + ".pass")); //NOI18N
        }
        assertTrue("Golden files generated.", false); //NOI18N
    }

    private static int resolveServerPort() {
        Integer i = Integer.getInteger("glassfish.server.port"); //NOI18N
        return i != null ? i.intValue() : 8080;
    }
}
