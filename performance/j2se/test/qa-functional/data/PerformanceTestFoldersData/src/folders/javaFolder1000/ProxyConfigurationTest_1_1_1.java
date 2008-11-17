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

import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of Proxy Configuration.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ProxyConfigurationTest_1_1_1 extends PerformanceTestCase {

    private JButtonOperator openProxyButton;
    protected String BUTTON, TAB;
    private WizardOperator wizard;
    protected String BUNDLE, MENU, TITLE;
    
 
    /** Creates a new instance of ProxyConfiguration */
    public ProxyConfigurationTest_1_1_1(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ProxyConfiguration */
    public ProxyConfigurationTest_1_1_1(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(ProxyConfigurationTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
    public void testProxyConfiguration() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        String BUNDLE2 =  "org.netbeans.modules.autoupdate.ui.Bundle";
        TAB = Bundle.getStringTrimmed(BUNDLE2,"SettingsTab_displayName");
        BUTTON = Bundle.getStringTrimmed(BUNDLE2,"SettingsTab.bProxy.text");
        BUNDLE = "org.netbeans.modules.autoupdate.ui.actions.Bundle";
        MENU = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle","Menu/Tools") + "|" + Bundle.getStringTrimmed(BUNDLE,"PluginManagerAction_Name");
        TITLE = Bundle.getStringTrimmed(BUNDLE,"PluginManager_Panel_Name");
    }
    
    public void prepare(){
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        wizard =new WizardOperator(TITLE);
        new JTabbedPaneOperator(wizard, 0).selectPage(TAB);
        openProxyButton = new JButtonOperator(wizard, BUTTON);
    }
    
    public ComponentOperator open(){
        openProxyButton.pushNoBlock();
        return new OptionsOperator();
    }

    @Override
    public void close() {
        if(testedComponentOperator!=null && testedComponentOperator.isShowing())
            ((OptionsOperator) testedComponentOperator).close();
        if(wizard!=null && wizard.isShowing())
            wizard.close();
    }
    
}
