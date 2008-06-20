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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.languages;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.lib.BasicOpenFileTest;

/**
 *
 * @author Jindrich Sedek
 */
public class OpenProjectFileTest extends BasicOpenFileTest {
    private static final String projectName = "OpenFileProj";
    public OpenProjectFileTest(String str) {
        super(str);
    }

    public static Test suite(){
        return NbModuleSuite.allModules(OpenProjectFileTest.class);
    }
    
    @Override
    public void setUp() throws IOException{
        openDataProjects(projectName);
    }
    
    @Override
    public void tearDown(){
        closeProject();
    }
    
    public void testBAT()throws Exception{
        openFile(projectName, "sample.bat");
        edit("FOR %%b in (A, B, C) DO IF %%b == B echo B is in the set!");
        closeFile();
    }

    public void testDIFF()throws Exception{
        openFile(projectName, "sample.diff");
        edit("0a1,6");
        closeFile();
    }
    
    public void testSH()throws Exception{
        openFile(projectName, "sample.sh");
        edit("ls -l | sed -e 's/[aeio]/u/g'");
        closeFile();
    }
    
    public void testMF()throws Exception{
        openFile(projectName, "sample.mf");
        edit("OpenIDE-Module: org.netbeans.modules.web.core.syntax/1");
        closeFile();
    }
    
}
