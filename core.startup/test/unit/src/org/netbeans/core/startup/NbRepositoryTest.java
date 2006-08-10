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

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class NbRepositoryTest extends NbTestCase {
    
    public NbRepositoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testUserDirIsWriteableEvenInstallDirDoesNotExists() throws IOException {
        System.getProperties().remove("netbeans.home");
        System.setProperty("netbeans.user", getWorkDirPath());
        
        FileObject fo = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        FileObject ahoj = FileUtil.createData(fo, "ahoj.jardo");
        
        OutputStream os = ahoj.getOutputStream();
        os.write("Ahoj".getBytes());
        os.close();
        
        File af = new File(new File(getWorkDir(), "config"), "ahoj.jardo");
        assertTrue("File created", af.exists());
        assertEquals("4 bytes", 4, af.length());
    }
}
