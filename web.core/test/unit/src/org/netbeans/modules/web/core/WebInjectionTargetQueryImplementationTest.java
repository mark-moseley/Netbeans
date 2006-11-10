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

package org.netbeans.modules.web.core;

import java.io.File;
import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.junit.NbTestCase;

import org.netbeans.modules.web.core.test.TestUtil;

/**
 *
 * @author Radko Najman
 */
public class WebInjectionTargetQueryImplementationTest extends NbTestCase {
    
    private String serverID;
    private FileObject ordinaryClass;
    private FileObject fileSubclass;
    private FileObject directServletSubclass;
    private FileObject secondLevelServletSubclass;

    public WebInjectionTargetQueryImplementationTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }

    protected void tearDown() throws Exception {
        serverID = null;
        ordinaryClass = null;
        fileSubclass = null;
        directServletSubclass = null;
        secondLevelServletSubclass = null;
        
        super.tearDown();
    }

    /**
     * Test of isInjectionTarget method, of class org.netbeans.modules.web.core.WebInjectionTargetQueryImplementation.
     */
    public void testIsInjectionTarget() {
        System.out.println("isInjectionTarget");
        
        boolean result;        
        WebInjectionTargetQueryImplementation instance = new WebInjectionTargetQueryImplementation();

        //J2EE 1.4 project
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication_j2ee14");
        FileObject projdir = FileUtil.toFileObject(f);

        ordinaryClass = projdir.getFileObject("src/java/org/test/NewClass.java");
        fileSubclass = projdir.getFileObject("src/java/org/test/FileSubclass.java");
        directServletSubclass = projdir.getFileObject("src/java/org/test/NewServlet.java");
        secondLevelServletSubclass = projdir.getFileObject("src/java/org/test/NewServletSubclass.java");
        
        result = instance.isInjectionTarget(ordinaryClass, "org.test.NewClass");
        assertEquals(false, result);
        result = instance.isInjectionTarget(fileSubclass, "org.test.FileSubclass");
        assertEquals(false, result);
        result = instance.isInjectionTarget(directServletSubclass, "org.test.NewServlet");
        assertEquals(false, result);
        result = instance.isInjectionTarget(secondLevelServletSubclass, "org.test.NewServletSubclass");
        assertEquals(false, result);
        
        //Java EE 5 project
        f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication_jee5");
        projdir = FileUtil.toFileObject(f);

        ordinaryClass = projdir.getFileObject("src/java/org/test/NewClass.java");
        fileSubclass = projdir.getFileObject("src/java/org/test/FileSubclass.java");
        directServletSubclass = projdir.getFileObject("src/java/org/test/NewServlet.java");
        secondLevelServletSubclass = projdir.getFileObject("src/java/org/test/NewServletSubclass.java");
        
        result = instance.isInjectionTarget(ordinaryClass, "org.test.NewClass");
        assertEquals(false, result);
        result = instance.isInjectionTarget(fileSubclass, "org.test.FileSubclass");
        assertEquals(false, result);
        result = instance.isInjectionTarget(directServletSubclass, "org.test.NewServlet");
        assertEquals(true, result);
        result = instance.isInjectionTarget(secondLevelServletSubclass, "org.test.NewServletSubclass");
        assertEquals(true, result);
    }
    
}
