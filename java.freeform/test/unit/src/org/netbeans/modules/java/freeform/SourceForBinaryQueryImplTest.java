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

package org.netbeans.modules.java.freeform;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Check that the correct sources are reported.
 * @author Jesse Glick
 */
public class SourceForBinaryQueryImplTest extends TestBase {
    
    public SourceForBinaryQueryImplTest(String name) {
        super(name);
    }
    
    public void testFindSourcesForBinaries() throws Exception {
        FileObject srcroot = simple.getProjectDirectory().getFileObject("src");
        URL binroot = new URL(simple.getProjectDirectory().getURL(), "build/classes/");
        assertEquals("correct source root for " + binroot, Collections.singletonList(srcroot), Arrays.asList(SourceForBinaryQuery.findSourceRoots(binroot).getRoots()));
        binroot = new URL("jar:" + simple.getProjectDirectory().getURL().toString() + "build/simple-app.jar!/");
        assertEquals("correct source root for " + binroot, Collections.singletonList(srcroot), Arrays.asList(SourceForBinaryQuery.findSourceRoots(binroot).getRoots()));
        srcroot = simple.getProjectDirectory().getFileObject("antsrc");
        binroot = new URL(simple.getProjectDirectory().getURL(), "build/antclasses/");
        assertEquals("correct source root for " + binroot, Collections.singletonList(srcroot), Arrays.asList(SourceForBinaryQuery.findSourceRoots(binroot).getRoots()));
        binroot = new URL(simple.getProjectDirectory().getURL(), "build/nonsense/");
        assertEquals("no source root for " + binroot, Collections.EMPTY_LIST, Arrays.asList(SourceForBinaryQuery.findSourceRoots(binroot).getRoots()));
    }
    
}
