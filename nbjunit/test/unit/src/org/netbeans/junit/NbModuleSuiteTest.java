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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.junit;

import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteTest extends TestCase {
    
    public NbModuleSuiteTest(String testName) {
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

    /**
     * Test of run method, of class NbModuleSuite.
     */
    public void testRun() {
        Test instance = NbModuleSuite.create(T.class, null, null);
        junit.textui.TestRunner.run(instance);
        
        assertEquals("OK", System.getProperty("t.one"));
    }
    
    public void testModulesForCL() throws Exception {
        Set<String> s = NbModuleSuite.S.findEnabledModules(ClassLoader.getSystemClassLoader());
        assertEquals("Three modules: " + s, 3, s.size());
        
        assertTrue("Util: " + s, s.contains("org.openide.util"));
        assertTrue("nbjunit: " + s, s.contains("org.netbeans.modules.nbjunit"));
        assertTrue("insane: " + s, s.contains("org.netbeans.insane"));
    }
    
    public void testIfOneCanLoadFromToolsJarOneShallDoThatInTheFrameworkAsWell() throws Exception {
        
        Class<?> vmm;
        try {
            vmm = ClassLoader.getSystemClassLoader().loadClass("com.sun.jdi.VirtualMachineManager");
        } catch (ClassNotFoundException ex) {
            vmm = null;
            //throw ex;
        }
        Class<?> own;
        try {
            own = Thread.currentThread().getContextClassLoader().loadClass("com.sun.jdi.VirtualMachineManager");
        } catch (ClassNotFoundException ex) {
            //own = null;
            throw ex;
        }
        
        //assertEquals(vmm, own);
        
    }

    public void testModulesForMe() throws Exception {
        Set<String> s = NbModuleSuite.S.findEnabledModules(getClass().getClassLoader());
        assertEquals("Three modules: " + s, 3, s.size());
        
        assertTrue("Util: " + s, s.contains("org.openide.util"));
        assertTrue("nbjunit: " + s, s.contains("org.netbeans.modules.nbjunit"));
        assertTrue("insanse: " + s, s.contains("org.netbeans.insane"));
    }
    
    public static class T extends TestCase {
        public T(String t) {
            super(t);
        }

        public void testOne() {
            System.setProperty("t.one", "OK");
        }
    }
    
}
