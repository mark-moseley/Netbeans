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

package gui.actions;

import gui.EPUtilities;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class NavigatorSchemaViewMode  extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node processNode; 
    
    // After fix of 128596 "Please wait" text is shown so it is okay to wait up to 10 sec
    public final long EXPECTED_TIME = 10000;
    
    /** Creates a new instance of SchemaDesignView */
    public NavigatorSchemaViewMode(String testName) {
        super(testName);
        expectedTime = EXPECTED_TIME; 
    }
    
    public NavigatorSchemaViewMode(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = EXPECTED_TIME; 
    }
    
    protected void initialize() {
        log(":: initialize");
//        new CloseAllDocumentsAction().performAPI();
        
        processNode = EPUtilities.getProcessFilesNode("SOATestProject");
        Node doc1 = new Node(processNode,"batch.xsd");
        doc1.select();
        
        JComboBoxOperator combo = new JComboBoxOperator(new TopComponentOperator("Navigator")); // NOI18N
        combo.selectItem("Schema View"); // NOI18N
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log(":: open");
        Node doc = new Node(processNode,"fields.xsd");
        doc.select();
        
        return new TopComponentOperator("fields.xsd");
    }
    
    public void close() {
        log("::close");
        Node doc1 = new Node(processNode,"batch.xsd");
        doc1.select();
    }
    
    protected void shutdown() {
        log(":: shutdown");
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new NavigatorSchemaViewMode("measureTime"));
    }
    
}