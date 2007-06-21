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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import java.io.File;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Erno Mononen
 */
public class EntityWizardTest extends SourceTestSupport{
    
    public EntityWizardTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception{
        super.setUp();
    }
    
    private FileObject getPkgFolder() throws Exception{
        File pkg = new File(getWorkDir(), "foobar"); 
        pkg.mkdirs();
        return FileUtil.toFileObject(pkg);
    }
    
    public void testGenerateEntityFieldAccess() throws Exception {
        FileObject result = EntityWizard.generateEntity(getPkgFolder(), "MyEntity", "Long", true);
        assertFile(result);
    }
    
    public void testGenerateEntityPropertyAccess() throws Exception {
        FileObject result = EntityWizard.generateEntity(getPkgFolder(), "MyEntity", "java.lang.Long", false);
        assertFile(result);
    }
}
