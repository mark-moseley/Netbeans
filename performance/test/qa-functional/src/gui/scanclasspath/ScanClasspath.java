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

package gui.scanclasspath;

import java.util.ArrayList;

import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.junit.*;
import junit.framework.*;

import org.netbeans.performance.test.utilities.LoggingScanClasspath;
import org.netbeans.performance.test.utilities.LoggingScanClasspath.PerformanceScanData;


/**
 * Test provide measured time of scanning classpth for some classpath roots. 
 * We measure classpath scanning during openieng jEdit project.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ScanClasspath extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name;
    
    // list of classpath roots we are interesting to measure
    private static ArrayList reportCPR = new ArrayList();
    
    static {
        reportCPR.add("src.zip");
        reportCPR.add("rt.jar");
        reportCPR.add("src");
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
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite("Classpath scanning test suite");
        
        suite.addTest(new gui.setup.IDESetupTest("testCloseMemoryToolbar"));
        suite.addTest(new gui.setup.IDESetupTest("testCloseWelcome"));
        suite.addTest(new ScanClasspath("testOpenJEditProject"));

        return suite;
    }
    
    public void testOpenJEditProject() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/jEdit41");
        
        //wait until scan finished and at least some timeout, because we find that scandialog is still opened
        if(ProjectSupport.waitScanFinished()) 
            waitNoEvent(5000);
        
        LoggingScanClasspath.printMeasuredValues(getLog());
        java.util.ArrayList data = LoggingScanClasspath.getData();
        for (int i=0; i<data.size(); i++){
            PerformanceScanData psd = (PerformanceScanData) data.get(i);
            
            // report only if we want to report it
            if(reportCPR.contains((Object)psd.getName())){
                reportPerformance("Scanning " + psd.getName(), psd.getValue(), "ms", 1);
            }
        }
    }
    
    public ComponentOperator open(){
        return null;
    }
    
    public void close(){
    }
    
    public void prepare(){
    }
}
