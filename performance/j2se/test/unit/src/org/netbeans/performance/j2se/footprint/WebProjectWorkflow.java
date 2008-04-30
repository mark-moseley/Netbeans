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

package org.netbeans.performance.j2se.footprint;


import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;

/**
 * Measure Web Project Workflow Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class WebProjectWorkflow extends MemoryFootprintTestCase {

    private String webproject;

    /**
     * Creates a new instance of WebProjectWorkflow
     * @param testName the name of the test
     */
    public WebProjectWorkflow(String testName) {
        super(testName);
        prefix = "Web Project Workflow |";
    }

    /**
     * Creates a new instance of WebProjectWorkflow
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public WebProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Web Project Workflow |";
    }
    
    public void initialize() {
        super.initialize();
        FootprintUtilities.closeAllDocuments();
        FootprintUtilities.closeMemoryToolbar();
    }
    
    @Override
    public void setUp() {
        //do nothing
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open(){
        // Web project
        webproject = FootprintUtilities.createproject("Samples|Web", "Tomcat Servlet Example", false);
        
        FootprintUtilities.openFile(webproject, "<default package>", "SessionExample.java", true);
        FootprintUtilities.buildproject(webproject);
        FootprintUtilities.deployProject(webproject);
        //FootprintUtilities.collapseProject(webproject);
        
        return null;
    }
    
    public void close(){
        FootprintUtilities.deleteProject(webproject);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new WebProjectWorkflow("measureMemoryFooprint"));
    }
    
}
