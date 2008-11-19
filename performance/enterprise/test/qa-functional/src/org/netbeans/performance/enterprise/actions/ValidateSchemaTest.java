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

package org.netbeans.performance.enterprise.actions;



import junit.framework.Test;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.EPUtilities;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class ValidateSchemaTest extends PerformanceTestCase {
    
    private Node schemaNode;
    
    /** Creates a new instance of ValidateSchema */
    public ValidateSchemaTest(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public ValidateSchemaTest(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(ValidateSchemaTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize(){
        log(":: initialize");
        schemaNode = new Node(new EPUtilities().getProcessFilesNode("SOATestProject"),"fields.xsd");
        schemaNode.select();
        new OpenAction().perform(schemaNode);
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log("::open");
        
        schemaNode.performPopupAction("Validate XML"); // NOI18N
        
        OutputOperator oot = new OutputOperator();
        OutputTabOperator asot = oot.getOutputTab("XML check"); // NOI18N
        asot.waitText("XML validation finished"); // NOI18N
        return oot;
    }
    
    @Override
    protected void shutdown() {
        log("::shutdown");        
        new TopComponentOperator("fields.xsd").getFocus(); // workaround issue 120762
        new CloseAllDocumentsAction().perform();
    }


    
}