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

package folders.javaFolder1000;



import java.awt.AWTEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import org.netbeans.junit.Log;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.EPUtilities;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org, mmirilovic@netbeans.org, mrkam@netbeans.org
 *
 */
public class OpenComplexDiagram_1 extends PerformanceTestCase {
    
    /** Creates a new instance of OpenComplexDiagram */
    
    public OpenComplexDiagram_1(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    public OpenComplexDiagram_1(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    @Override
    public void initialize(){
        log(":: initialize");
        
        // The following disables EventTool to hold a reference to DesignView in 
        // its listeners
        EventTool.addListeners(EventTool.getCurrentEventMask() & ~AWTEvent.FOCUS_EVENT_MASK);        
        
        Log.enableInstances(Logger.getLogger("TIMER.bpel"), "BPEL DesignView", Level.FINEST);
    }
    
    public void prepare() {
        log(":: prepare");
    }

    /**
     * Check of memory leaks. measureTime testcase should be executed before 
     * this testcase
     */
    public void testGC() {
        Log.assertInstances("Can't GC BPEL DesignView");        
    }

    public ComponentOperator open() {
        log("::open");
        Node processFilesNode = new EPUtilities().getProcessFilesNode("TravelReservationService");
        Node doc = new Node(processFilesNode,"TravelReservationService.bpel");

        // Use double click instead of Open because Open opens Source view
        // while double click opens Schema view
        TreePath treePath = doc.getTreePath();
        new EventTool().waitNoEvent(1000);
        doc.tree().clickOnPath(treePath, 2);
        return new TopComponentOperator("TravelReservationService.bpel");
    }
    
    @Override
    protected void shutdown() {
        log("::shutdown");
    }
    
    @Override
    public void close(){
        log("::close");
        new CloseAllDocumentsAction().performAPI();        
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(OpenComplexDiagram.class)
            .addTest("measureTime")
            .enableModules(".*")
            .clusters(".*")
        );    
    }
    
}