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
 * BindingSchemaBundleTest.java
 * JUnit based test
 *
 * Created on February 5, 2007, 1:14 PM
 */

package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import java.io.File;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.ui.TestLookup;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class BindingSchemaBundleTest extends TestCase {
    
    static {
        try {
           System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
           Lookup l = Lookup.getDefault();
           if(l instanceof TestLookup) {
               XMLFileSystem x = new XMLFileSystem(BindingSchemaBundleTest.class.getResource("/org/netbeans/modules/wsdlextensions/jms/resources/layer.xml"));
              
               ((TestLookup) l).setup(x);
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public BindingSchemaBundleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    public void testBundleGenerator() throws Exception {
//        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
//        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();
//        
//        //binding
//        WSDLExtensibilityElement bindingElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING);
//        assertEquals(WSDLExtensibilityElements.ELEMENT_BINDING, bindingElement.getName());
//        
//        List<WSDLExtensibilityElementInfoContainer> containers =  bindingElement.getAllWSDLExtensibilityElementInfoContainers();
//        assertEquals(0, containers.size());
//        
//        List<WSDLExtensibilityElementInfo> exInfos = bindingElement.getAllWSDLExtensibilityElementInfos();
//        assertEquals(1, exInfos.size());
//        
//        WSDLExtensibilityElementInfo exInfo = exInfos.get(0);
//        assertNotNull(exInfo.getElement());
//        assertNotNull(exInfo.getPrefix());
//        assertNotNull(exInfo.getSchema());
//        
//        SchemaBundleGenerator sbg = new SchemaBundleGenerator(new File("C:\\Sun\\jse\\test.properties"), exInfo.getSchema());
//        sbg.generate();
    }
}
