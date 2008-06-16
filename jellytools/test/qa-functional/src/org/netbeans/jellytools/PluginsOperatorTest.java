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
package org.netbeans.jellytools;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/** Test PluginsOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class PluginsOperatorTest extends JellyTestCase {

    /** Creates test case with given name.
     * @param testName name of test case
     */
    public PluginsOperatorTest(String testName) {
        super(testName);
    }

    /** Used for internal run in IDE.
     * @param args not used here
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static final String[] tests = new String[] {
"testInvoke",
        "testInstall",
        "testUninstall",
        "testDeactivate",
        "testSettings",
        "testDowloaded",
        "testClose"};
    /** Define test suite.
     * @return suite.
     */
    public static NbTest suite() {
        /*
        NbTestSuite suite = new NbTestSuite();
        // test cases have to be in particular order
        suite.addTest(new PluginsOperatorTest("testInvoke"));
        suite.addTest(new PluginsOperatorTest("testInstall"));
        suite.addTest(new PluginsOperatorTest("testUninstall"));
        suite.addTest(new PluginsOperatorTest("testDeactivate"));
        suite.addTest(new PluginsOperatorTest("testSettings"));
        suite.addTest(new PluginsOperatorTest("testDowloaded"));
        suite.addTest(new PluginsOperatorTest("testClose"));
        return suite;
         */
        return (NbTest) createModuleTest(PluginsOperatorTest.class, 
        tests);
    }

    /** Print out test name. */
    @Override
    public void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
    }
    private static PluginsOperator pluginsOper;
    private static final String SOURCE_BROWSER_LABEL = "netbeans.org Source Browser"; //NOI18N

    /** Test of invoke method. */
    public void testInvoke() {
        pluginsOper = PluginsOperator.invoke();
    }

    /** Test install() method. 
     * - select Available Plugins tab
     * - type "netbeans.org Source Browser" into Search text field
     * - finish installation
     */
    public void testInstall() {
        pluginsOper.selectAvailablePlugins();
        pluginsOper.search(SOURCE_BROWSER_LABEL);
        pluginsOper.install(SOURCE_BROWSER_LABEL);
    }

    /** Test uninstallation
     * - select Installed tab
     * - select Java and "netbeans.org Source Browser" plugins
     * - click Uninstall button
     * - wait for "NetBeans IDE Installer" dialog
     * - click Cancel
     */
    public void testUninstall() {
        pluginsOper.selectInstalled();
        pluginsOper.selectPlugins(new String[]{
            "Java",
            SOURCE_BROWSER_LABEL
        });
        pluginsOper.uninstall();
        pluginsOper.installer().cancel();
    }

    /** Test deactivation
     * - select Installed tab
     * - select "netbeans.org Source Browser" plugin
     * - click Deactivate button
     * - wait for "NetBeans IDE Installer" dialog
     * - click Cancel
     */
    public void testDeactivate() {
        pluginsOper.selectInstalled();
        pluginsOper.selectPlugin(SOURCE_BROWSER_LABEL);
        pluginsOper.deactivate();
        pluginsOper.installer().cancel();
    }

    /** Test settings
     * - select Settings tab
     */
    public void testSettings() {
        pluginsOper.selectSettings();
    }

    /** Test Downloaded tab
     * - select Downloaded tab
     * - wait for file chooser
     * - close file chooser
     */
    public void testDowloaded() {
        pluginsOper.addPlugins();
        new JFileChooserOperator().cancel();
    }
    
    /** Close Plugins dialog. */
    public void testClose() {
        pluginsOper.close();
    }
}
