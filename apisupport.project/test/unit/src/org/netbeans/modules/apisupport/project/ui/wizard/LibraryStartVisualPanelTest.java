/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.io.File;
import org.netbeans.junit.NbTestCase;

/**
 * Test wizard logic.
 * @author mkleint
 */
public class LibraryStartVisualPanelTest extends NbTestCase {
    
    private File libraryPath;

    public LibraryStartVisualPanelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        libraryPath = new File(getDataDir(), "test-library-0.1_01.jar");
        assertTrue("test JAR " + libraryPath + " exists", libraryPath.isFile());
    }
    
    public void testPopulateProjectData() {
        NewModuleProjectData data = new NewModuleProjectData();
        LibraryStartVisualPanel.populateProjectData(data, libraryPath.getAbsolutePath());
        assertEquals("test-library", data.getProjectName());
        assertEquals("org.apache.commons.logging", data.getCodeNameBase());
    }
    
}
