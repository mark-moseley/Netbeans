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

import java.awt.Component;
import javax.swing.JButton;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class AddProfilingPointWizardTest_1_2_1  extends PerformanceTestCase {

    private static final String menuPrefix = "Window|Profiling|"; //NOI18N
    private String commandName;
    private String windowName;
    private TopComponentOperator ppointsPane;
    private JButtonOperator addPointButton;
    private NbDialogOperator wizard;
    
    /**
     * @param testName 
     */
    public AddProfilingPointWizardTest_1_2_1(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * @param testName 
     * @param performanceDataName
     */
    public AddProfilingPointWizardTest_1_2_1(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(AddProfilingPointWizardTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testAddProfilingPointWizard() {
        doMeasurement();
    }
        
    @Override
    public void initialize() {
        commandName = "Profiling Points"; //NOI18N
        windowName = "Profiling Points"; ////NOI18N
        new Action(menuPrefix+commandName,null).performMenu(); // NOI18N  
        ppointsPane = new TopComponentOperator(windowName);
        addPointButton = new JButtonOperator(ppointsPane,new ComponentChooser() {

            public boolean checkComponent(Component component) {
                try{
                    if ( (((JButton)component).getToolTipText()).equals("Add Profiling Point") ) //NOI18N
                          return true;
                    else  return false;
                } catch (java.lang.NullPointerException npe) {}
                 return false;
            }

            public String getDescription() {
                return "Selecting button by tooltip";
            }
            });
    }

    public void prepare() {
    }

    public ComponentOperator open() {
        addPointButton.pushNoBlock();
        wizard =new NbDialogOperator("New Profiling Point"); //NOI18N
        return null;
    }

    @Override
    public void close() {
        wizard.close();
    }
    
    @Override
    public void shutdown() {
        ppointsPane.closeWindow();
    }

}