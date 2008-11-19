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
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.EPUtilities;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class SchemaNavigatorDesignViewTest  extends PerformanceTestCase {
    
    private Node processNode, schemaNode, tempNode;
    
    /** Creates a new instance of SchemaNavigatorDesignView */
    public SchemaNavigatorDesignViewTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        //   WAIT_AFTER_OPEN=4000;
    }
    
    /** Creates a new instance of SchemaNavigatorDesignView */
    public SchemaNavigatorDesignViewTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        //   WAIT_AFTER_OPEN=4000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(SchemaNavigatorDesignViewTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    protected void initialize() {
        log(":: initialize");
        System.gc();
        new EventTool().waitNoEvent(3000);
        processNode = new EPUtilities().getProcessFilesNode("SOATestProject");
        
        schemaNode = new Node(processNode, "fields.xsd");
        tempNode = new Node(processNode, "batch.xsd");        
        
        tempNode.select();
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log(":: open");
        schemaNode.select();
        return new TopComponentOperator("Navigator"); // NOI18N
    }
    
    @Override
    public void close() {
        tempNode.select();
        System.gc();
        new EventTool().waitNoEvent(1000);
    }

}