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

package gui.scanclasspath;

import java.util.ArrayList;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.performance.test.utilities.LoggingScanClasspath;


/**
 * Test provide measured time of scanning classpth for some classpath roots.
 * We measure classpath scanning during openieng jEdit project.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ScanClasspath extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    // list of classpath roots we are interesting to measure
    protected static ArrayList<String> reportCPR = new ArrayList<String> ();
    
    // measure whole classpath scan time together
    protected static long wholeClasspathScan = 0;
        
    static {
        reportCPR.add("rt.jar");        // JDK/jre/lib/rt.jar
        reportCPR.add("jEdit41/src");   // jEdit41/src
    }
    
    /**
     * Creates a new instance of ScanClasspath
     * @param testName the name of the test
     */
    public ScanClasspath(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of ScanClasspath
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ScanClasspath(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new ScanClasspath("openJEditProject"));

        return suite;
    }
    
    public void openJEditProject() {
        gui.Utilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+"/jEdit41");
        measureClassPathScan();
        reportPerformance("Scanning Java Project Classpath", wholeClasspathScan, "ms", 1);
    }
    
    protected void measureClassPathScan() {
        
        LoggingScanClasspath.printMeasuredValues(getLog());
        
        for (LoggingScanClasspath.PerformanceScanData data : LoggingScanClasspath.getData()) {
            // report only if we want to report it
            if(reportCPR.contains((Object)data.getName())){
                if(data.getValue() > 0)
                    reportPerformance("Scanning " + data.getName(), data.getValue(), "ms", 1);
                else
                    fail("Measured value ["+data.getValue()+"] is not > 0 !");
            }
            
            // measure whole classpath scan
            wholeClasspathScan = wholeClasspathScan + data.getValue();
        }
    }
    
    public ComponentOperator open(){
        return null;
    }
    
    public void close(){
    }
    
    public void prepare(){
    }
    
    @Override
    public void setUp() {
        // do nothing
    }
    
}
