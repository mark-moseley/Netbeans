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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.XMLFileSystem;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author jqian
 */
public class JbiExtensionAttributeTest {
    
    private static final String REDELIVERY_EXTENSION = "RedeliveryExtension";
    
    private JbiExtensionAttribute endpointNameAttr;

    public JbiExtensionAttributeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws SAXException {
        URL fsURLDef = this.getClass().getResource("resources/fs.xml");
        assertTrue("Cannot create XML FS for testing purposes", fsURLDef != null);
        
        FileSystem fs = new XMLFileSystem(fsURLDef);   
        JbiInstalledExtensionInfo installedExtensionInfo = 
                JbiInstalledExtensionInfo.getInstalledExtensionInfo(fs);    
        JbiExtensionInfo redeliveryExtensionInfo = 
                installedExtensionInfo.getExtensionInfo(REDELIVERY_EXTENSION);
        JbiExtensionElement redeliveryExtensionElement = 
                redeliveryExtensionInfo.getElements().get(0);
        JbiExtensionElement redeliveryOnFailureChoiceExtensionElement = 
                redeliveryExtensionElement.getElements().get(0);
        JbiExtensionElement errorEndpointElement = 
                redeliveryOnFailureChoiceExtensionElement.getElements().get(1);
        endpointNameAttr = 
                errorEndpointElement.getAttributes().get(0);        
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getName method, of class JbiExtensionAttribute.
     */
    @Test
    public void getName() {
        System.out.println("getName");
        String expResult = "endpoint-name";
        String result = endpointNameAttr.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getType method, of class JbiExtensionAttribute.
     */
    @Test
    public void getType() {
        System.out.println("getType");
        String expResult = "String";
        String result = endpointNameAttr.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDescription method, of class JbiExtensionAttribute.
     */
    @Test
    public void getDescription() {
        System.out.println("getDescription");
        String expResult = "Error endpoint name";
        String result = endpointNameAttr.getDescription();
        assertEquals(expResult, result);
    }

}