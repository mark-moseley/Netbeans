/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Java Platform Manager
 *
 * @author  mmirilovic@netbeans.org
 */
public class JavaPlatformManager extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private String MENU, TITLE;

    /** Creates a new instance of JavaPlatformManager */
    public JavaPlatformManager(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of JavaPlatformManager */
    public JavaPlatformManager(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Tools") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle","CTL_PlatformManager");
        TITLE = Bundle.getStringTrimmed("org.netbeans.api.java.platform.Bundle","TXT_PlatformsManager");
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
   }
    
    public ComponentOperator open() {
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new NbDialogOperator(TITLE);
    }
    
}
