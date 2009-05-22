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


import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import test.pkg.not.in.junit.NbModuleSuiteT;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.netbeans.junit.NbModuleSuite.Configuration;
import test.pkg.not.in.junit.NbModuleSuiteClusterPath;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteClusterPathFinalTest extends TestCase {
    
    public NbModuleSuiteClusterPathFinalTest(String testName) {
        super(testName);
    }            
    
    public static Test suite() {
        Test t = null;
        //t = new NbModuleSuiteTest("testRunEmptyConfig");
        if (t == null) {
            t = new NbTestSuite(NbModuleSuiteClusterPathFinalTest.class);
        }
        return t;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void testClusterPathFinal() throws Exception{
        LinkedList<File> clusters = new LinkedList<File>();
        NbModuleSuite.S.findClusters(clusters, Collections.singletonList("ide[0-9]*"));
        assertFalse("Something found", clusters.isEmpty());
        assertEquals("One element found", 1, clusters.size());
        final File ideCluster = clusters.get(0);
        System.setProperty("cluster.path.final", ideCluster.getPath() + ":" + new File(ideCluster.getParent(), "nonexistent"));
        Configuration conf = NbModuleSuite.createConfiguration(NbModuleSuiteClusterPath.class).gui(false).clusters(".*");
        Test test = NbModuleSuite.create(conf);
        test.run(new TestResult());
        String val = System.getProperty("my.clusters");
        assertNotNull("The test was running", clusters);
        assertTrue("ide cluster shall be included: " + val, val.contains(ideCluster.getPath()));
        assertFalse("no java cluster shall be included: " + val, val.matches(".*java[0-9]*[:;].*"));
        assertFalse("no apisupport cluster shall be included: " + val, val.matches(".*apisupport[0-9]*[:;].*"));
        assertFalse("no ergonomics cluster shall be included: " + val, val.matches(".*ergonomics[0-9]*[:;].*"));
    }
}
