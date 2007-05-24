/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.loaders;

import java.io.File;
import javax.swing.JEditorPane;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.test.BaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CndDataObjectTestCase extends BaseTestCase {
    
    public CndDataObjectTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCDataObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.c");
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = FileUtil.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure", dob instanceof CDataObject);
    }
    
    public void testCCDataObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.cc");
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = FileUtil.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure", dob instanceof CCDataObject);   
    }

    public void testHDataObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.h");
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = FileUtil.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure", dob instanceof HDataObject);    
    }
    
}
