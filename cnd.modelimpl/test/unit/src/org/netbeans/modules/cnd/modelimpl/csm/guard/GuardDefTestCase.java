/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.csm.guard;

import java.io.File;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 * base class for guard block tests
 *
 * @author Alexander Simon
 */
public class GuardDefTestCase extends TraceModelTestBase {
    
    public GuardDefTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGuard() throws Exception {
        performTest("argc.cc"); // NOI18N
    }
    
    @Override
    protected void performTest(String source) throws Exception {
        File testFile = getDataFile(source);
        assertTrue("File not found "+testFile.getAbsolutePath(),testFile.exists()); // NOI18N
        performModelTest(testFile, System.out, System.err);
        boolean checked = false;
        for(FileImpl file : getProject().getAllFileImpls()){
            if ("cstdlib.h".equals(file.getName().toString())){ // NOI18N
                assertTrue("Guard guard block not defined", file.getMacros().size()==0); // NOI18N
                String guard = file.testGetGuardState().testGetGuardName();
                assertTrue("Guard guard block name not _STDLIB_H", "_STDLIB_H".equals(guard)); // NOI18N
                checked = true;
            } else if ("iostream.h".equals(file.getName())){ // NOI18N
                String guard = file.testGetGuardState().testGetGuardName();
                assertTrue("Guard guard block name not _IOSTREAM_H", "_IOSTREAM_H".equals(guard)); // NOI18N
            } else if ("argc.cc".equals(file.getName())){ // NOI18N
                String guard = file.testGetGuardState().testGetGuardName();
                assertTrue("Guard guard block found", guard == null); // NOI18N
            }
        }
        assertTrue("Not found FileImpl for cstdlib.h", checked); // NOI18N
    }
    
    private String getClassName(Class cls){
        String s = cls.getName();
        return s.substring(s.lastIndexOf('.')+1);
    }
}
