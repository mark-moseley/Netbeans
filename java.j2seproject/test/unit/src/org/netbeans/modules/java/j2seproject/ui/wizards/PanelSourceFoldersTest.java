/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.io.File;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author  tom
 */
public class PanelSourceFoldersTest extends NbTestCase {
    
    public PanelSourceFoldersTest (java.lang.String testName) {
        super(testName);
    }
    
    public void testCheckValidity () throws Exception {
        
        File root = getWorkDir();
        File projectDir = new File (root, "project");
        File test = new File (root,  "tests");
        test.mkdir();
        File src = new File (root, "src");
        src.mkdir();
        File badSrcDir = new File (root, "badSrc");
        File badSrcDir2 = new File (test, "src");
        badSrcDir2.mkdir();
        File badProjectDir = new File (root, "badPrjDir");
        badProjectDir.mkdir();
        badProjectDir.setReadOnly();
        
        assertNotNull("Empty name", PanelSourceFolders.checkValidity ("", projectDir.getAbsolutePath(), src.getAbsolutePath(), test.getAbsolutePath()));
        assertNotNull("Read Only WorkDir", PanelSourceFolders.checkValidity ("", badProjectDir.getAbsolutePath(), src.getAbsolutePath(), test.getAbsolutePath()));
        assertNotNull("Non Existent Sources", PanelSourceFolders.checkValidity ("test", projectDir.getAbsolutePath(), badSrcDir.getAbsolutePath(), test.getAbsolutePath()));
        assertNotNull("Sources == Tests", PanelSourceFolders.checkValidity ("test", projectDir.getAbsolutePath(), src.getAbsolutePath(), src.getAbsolutePath()));
        assertNotNull("Tests under Sources", PanelSourceFolders.checkValidity ("test", projectDir.getAbsolutePath(), src.getAbsolutePath(), new File (src, "Tests").getAbsolutePath()));
        assertNotNull("Sources under Tests",PanelSourceFolders.checkValidity ("test", projectDir.getAbsolutePath(), badSrcDir2.getAbsolutePath(), test.getAbsolutePath()));
        assertNull ("Valid data", PanelSourceFolders.checkValidity ("test", projectDir.getAbsolutePath(), src.getAbsolutePath(), test.getAbsolutePath()));
    }
}