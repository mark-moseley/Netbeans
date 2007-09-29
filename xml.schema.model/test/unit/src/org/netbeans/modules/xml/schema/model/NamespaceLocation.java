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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.schema.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public enum NamespaceLocation {
    OTA("http://www.opentravel.org/OTA/2003/05", "resources/J1_TravelItinerary.xsd"),
    ORGCHART("http://www.xmlspy.com/schemas/orgchart", "resources/OrgChart.xsd"),
    IPO("http://www.altova.com/IPO", "resources/ipo.xsd"),
    CUTPASTE("resources/CutPasteTest_before.xsd"),
    EXPREPORT("resources/ExpReport.xsd"),
    KEYREF("namespace1", "resources/KeyRef.xsd"),
    TEST_INCLUDE("http://www.example.com/testInclude", "resources/testInclude.xsd"),
    SOMEFILE("http://www.example.com/testInclude", "resources/somefile.xsd"),
    TEST_LENGTH("resources/testLength.xsd"),
    TEST_LIST("resources/testList.xsd"),
    TEST_BAD("resources/testBad.xsd"),
    LOANAPP("resources/loanApplication.xsd"),
    ADDRESS("resources/address.xsd"),
    REORDER_TEST("resources/ReorderTest.xsd"),
    SYNCTEST_PO("resources/PurchaseOrderSyncTest.xsd"),
    SYNCTEST_GLOBAL("resources/SyncTestGlobal_before.xsd"),
    SYNCTEST_NONGLOBAL("resources/SyncTestNonGlobal_before.xsd"),
    PO("http://www.example.com/PO1", "resources/PurchaseOrder.xsd");

    private String namespace;
    private String resourcePath;
    private String location;
    
    /** Creates a new instance of NamespaceLocation */
    NamespaceLocation(String location) {
        this("http://www.example.com/" +nameFromLocation(location), location);
    }
    
    NamespaceLocation(String namespace, String resourcePath) {
        this.namespace = namespace;
        this.resourcePath = resourcePath;
        this.location = resourcePath.substring(resourcePath.lastIndexOf("resources/")+10);
    }
    
    private static String nameFromLocation(String loc) {
         File f = new File(loc);
         String name = f.getName();
         return name.substring(0, name.length()-4);
    }
    
    public String getNamespace() { return namespace; }
    public String getResourcePath() { return resourcePath; }
    public String getLocationString() { return location; }
    public URI getNamespaceURI() throws URISyntaxException { return new URI(getNamespace()); }
    public static File schemaTestDir = null;
    public static File getSchemaTestTempDir() throws Exception {
        if (schemaTestDir == null) {
            schemaTestDir = Util.getTempDir("schematest");
        }
        return schemaTestDir;
    }
    public File getResourceFile() throws Exception {
        return new File(getSchemaTestTempDir(), Util.getFileName(getResourcePath()));
    }
    public void refreshResourceFile() throws Exception {
        Util.copyResource(getResourcePath(), FileUtil.toFileObject(getSchemaTestTempDir().getCanonicalFile()));
    }
    public URI getResourceURI() throws Exception { 
        return getResourceFile().toURI(); 
    }
    public URI getLocationURI() throws Exception { return new URI(location); }
    
    public static NamespaceLocation valueFromResourcePath(String resourcePath) {
        for (NamespaceLocation nl : values()) {
            if (nl.getResourcePath().equals(resourcePath)) {
                return nl;
            }
        }
        return null;
    }
}
