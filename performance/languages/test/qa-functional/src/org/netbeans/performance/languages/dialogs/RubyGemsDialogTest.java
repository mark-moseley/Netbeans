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

package org.netbeans.performance.languages.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager.RegionFilter;

import javax.swing.JComponent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class RubyGemsDialogTest extends PerformanceTestCase {

    protected String MENU, TITLE;
    
    public RubyGemsDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    public RubyGemsDialogTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ScriptingSetup.class)
             .addTest(RubyGemsDialogTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
    public void testRubyGemsDialog() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        MENU = "Tools"+"|"+"Ruby Gems"; 
        TITLE = "Ruby Gems"; 
    }

    @Override
    public void prepare() {
        repaintManager().addRegionFilter(GemsProgress);
    }

    private static final RegionFilter GemsProgress = new RegionFilter() {
        public boolean accept(JComponent c) {
           return  !(c instanceof javax.swing.JProgressBar);
        }
        public String getFilterName() {
            return "Gems Dialog progressbar filter";
        }
    };

    @Override
    public ComponentOperator open() {
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU);
        return new WizardOperator(TITLE);
    }

    @Override
    public void close() {
        super.close();
        repaintManager().resetRegionFilters();        
        
    }    

}
