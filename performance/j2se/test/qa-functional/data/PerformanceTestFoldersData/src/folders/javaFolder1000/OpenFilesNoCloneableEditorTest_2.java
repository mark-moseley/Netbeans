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

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of opening files, where CloneableEditor isn't super class of their editor.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFilesNoCloneableEditorTest_2 extends PerformanceTestCase {

    /** Node to be opened/edited */
    public static Node openNode ;
    /** Folder with data */
    public static String fileProject;
    /** Folder with data  */
    public static String filePackage;
    /** Name of file to open */
    public static String fileName;
    /** Menu item name that opens the editor */
    public static String menuItem;
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    protected static String EDIT = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    
    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     * @param testName the name of the test
     */
    public OpenFilesNoCloneableEditorTest_2(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesNoCloneableEditorTest_2(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(OpenFilesNoCloneableEditorTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testOpening20kBPropertiesFile(){
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Bundle20kB.properties";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpening20kBPictureFile(){
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "splash.gif";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpening20kBFormFile(){
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "JFrame20kB.java";
        menuItem = OPEN;
        doMeasurement();
    }

    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }
    
    @Override
    protected void shutdown(){
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }
    
    @Override
    public ComponentOperator open(){
        JPopupMenuOperator popup =  openNode.callPopup();
        popup.pushMenu(menuItem);
        return new TopComponentOperator(fileName);
    }
    
    @Override
    public void close(){
        new TopComponentOperator(fileName).closeDiscard();
    }

    @Override
    public void prepare() {
        openNode = new Node(new SourcePackagesNode(fileProject), filePackage + '|' + fileName);
    }

}
