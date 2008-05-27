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

package org.netbeans.performance.j2se.dialogs;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.WizardOperator;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

/**
 * Test of Proxy Configuration.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ProxyConfiguration extends PluginManager {

    private JButtonOperator openProxyButton;
    protected String BUTTON, TAB;
    private WizardOperator wizard;
    
    
    /** Creates a new instance of ProxyConfiguration */
    public ProxyConfiguration(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ProxyConfiguration */
    public ProxyConfiguration(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    
    public void testProxyConfiguration() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        String BUNDLE2 =  "org.netbeans.modules.autoupdate.ui.Bundle";
        
        TAB = Bundle.getStringTrimmed(BUNDLE2,"SettingsTab_displayName");
        BUTTON = Bundle.getStringTrimmed(BUNDLE2,"SettingsTab.bProxy.text");
    }
    
    public void prepare(){
        wizard = (WizardOperator) super.open();
// waiting plugin initialization
        waitNoEvent(5000);
        new JTabbedPaneOperator(wizard, 0).selectPage(TAB);
        
        openProxyButton = new JButtonOperator(wizard, BUTTON);
    }
    
    public ComponentOperator open(){
        // invoke an action
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
    
    public void shutdown() {
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new ProxyConfiguration("measureTime"));
    }
}
