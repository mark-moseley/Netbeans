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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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
package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.File;
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class RMTest extends TestCase {
    
    public RMTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception { }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testRM() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");

        ConfigVersion cfgVersion = ConfigVersion.CONFIG_1_0;
        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        assertFalse("RM enabled indicated on empty WSDL", RMModelHelper.getInstance(cfgVersion).isRMEnabled(b));

        RMModelHelper.getInstance(cfgVersion).enableRM(b, true);
        assertTrue("RM not enabled correctly", RMModelHelper.getInstance(cfgVersion).isRMEnabled(b));

        assertNull("Inactivity timeout set even when not specified", RMModelHelper.getInstance(cfgVersion).getInactivityTimeout(b));
        RMModelHelper.getInstance(cfgVersion).setInactivityTimeout(b, "112233");
        assertEquals("Inactivity Timeout Value Not Saved/Read Correctly", "112233", RMModelHelper.getInstance(cfgVersion).getInactivityTimeout(b));

        assertFalse("Flow Control enabled indicated", RMModelHelper.isFlowControl(b));
        RMModelHelper.getInstance(cfgVersion).enableFlowControl(b, true);
        RMModelHelper.getInstance(cfgVersion).enableFlowControl(b, false);
        RMModelHelper.getInstance(cfgVersion).enableFlowControl(b, true);
        assertTrue("Flow Control disabled indicated", RMModelHelper.isFlowControl(b));

        assertNull("Max Receive Buffer Size set even when not specified", RMModelHelper.getMaxReceiveBufferSize(b));
        RMModelHelper.setMaxReceiveBufferSize(b, "2233");
        assertEquals("Max Receive Buffer Size Value Not Saved/Read Correctly", "2233", RMModelHelper.getMaxReceiveBufferSize(b));

        assertFalse("Ordered enabled indicated", RMModelHelper.getInstance(cfgVersion).isOrderedEnabled(b));
        RMModelHelper.getInstance(cfgVersion).enableOrdered(b, true);
        assertTrue("Ordered disabled indicated", RMModelHelper.getInstance(cfgVersion).isOrderedEnabled(b));
        RMModelHelper.getInstance(cfgVersion).enableOrdered(b, false);
        assertFalse("Ordered enabled indicated", RMModelHelper.getInstance(cfgVersion).isOrderedEnabled(b));
        
        RMModelHelper.getInstance(cfgVersion).enableRM(b, false);
        assertFalse("RM not disabled correctly", RMModelHelper.getInstance(cfgVersion).isRMEnabled(b));
        assertNull("RM not disabled correctly", RMModelHelper.getInstance(cfgVersion).getInactivityTimeout(b));
        
        model.endTransaction();

        TestUtil.dumpToFile(model.getBaseDocument(), new File("C:\\RMService.wsdl"));
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
