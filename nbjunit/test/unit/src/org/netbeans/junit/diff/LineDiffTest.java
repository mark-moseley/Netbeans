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

package org.netbeans.junit.diff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Lahoda
 */
public class LineDiffTest extends NbTestCase {
    
    public LineDiffTest(String testName) {
	super(testName);
    }

    private void doOutputToFile(File f, String content) throws IOException {
	content = content.replaceAll("\n", System.getProperty("line.separator"));
	
	Writer w = new FileWriter(f);
	
	try {
	    w.write(content);
	} finally {
	    w.close();
	}
    }
    
    public void testEmptyFile() throws Exception {
	File workDir = getWorkDir();
	
	workDir.mkdirs();
	
	File test1 = new File(workDir, "test1");
	File test2 = new File(workDir, "test2");
	File test3 = new File(workDir, "test3");
	File test4 = new File(workDir, "test4");
	File test5 = new File(workDir, "test5");
	
	doOutputToFile(test1, "");
	doOutputToFile(test2, "a\nb");
	doOutputToFile(test3, "a\nc");
	doOutputToFile(test4, "a\nb");
	doOutputToFile(test5, "");
	
	LineDiff diff = new LineDiff();
	
	assertTrue(diff.diff(test1, test2, null));
	assertTrue(diff.diff(test2, test3, null));
	assertFalse(diff.diff(test2, test4, null));
	assertFalse(diff.diff(test1, test5, null));
    }
    
}
