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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * MTestExecutor
 * Class running tests by executing chosen testbags with appropriate
 * executor (result processor).
 *
 * Created on March 28, 2001, 6:57 PM
 */

package org.netbeans.xtest.harness;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;

import java.util.*;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.Filter;
import org.netbeans.xtest.testrunner.*;
import org.netbeans.xtest.plugin.PluginExecuteTask;

/**
 *
 * @author  mk97936
 * @version
 */
public class MTestExecutor extends Task {
    
    String targetName = null;
    String targetParamClasspathProp = null;
    String targetParamTestConfigProp = "tbag.testtype";
    String targetParamNameProp = null;
    String targetParamExecutorProp = "tbag.executor";
    String targetParamTimeoutProp = "xtest.timeout";
    
    /*
    protected boolean enableAssertions = true;
    protected int debugPort = 0;
    protected boolean debugSuspend = false;
    */
    
    File resultDir;
    File workDir;
    
    public void setTargetName(String name) {
        this.targetName = name;
    }
    
    public void setParamClasspathProp(String param) {
        this.targetParamClasspathProp = param;
    }
    
    public void setParamTestConfigProp(String param) {
        this.targetParamTestConfigProp = param;
    }
    
    public void setParamNameProp(String param) {
        this.targetParamNameProp = param;
    }
    
    public void setParamExecutorProp(String param) {
        this.targetParamExecutorProp = param;
    }
    
    public void setParamTimeoutProp(String param) {
        this.targetParamTimeoutProp = param;
    }
    
    public void setResultDir(File s) {
        resultDir = s;
    }
    
    public void setWorkDir(File s) {
        workDir = s;
    }
    
    /*
    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }
    
    public void setDebugSuspend(boolean suspend) {
        this.debugSuspend = suspend;
    }
    
    public void setEnableAssertions(boolean enableAssertions) {
        this.enableAssertions = enableAssertions;
    }
     */

    public void execute() throws BuildException {

        if (null == targetParamClasspathProp || 0 == targetParamClasspathProp.length())
            throw new BuildException("Attribute 'targetParamClasspathProp' has to be set.");
        
        if (resultDir == null) {
            throw new BuildException("ResultDir is not set");
        }
        if (workDir == null) {
            throw new BuildException("WorkDir is not set");
        }
        
        Testbag testbags[] = MTestConfigTask.getTestbags();
        if (null == testbags)
            throw new BuildException("TestBag configuration wasn't chosen.", getLocation());

        for (int i=0; i<testbags.length; i++) {

                // get TestBag 
                Testbag testbag = testbags[i];
                
                MTestConfig.AntExecType exec = testbag.getExecutor();
                if (exec == null) throw new BuildException("Testbag "+testbag.getName()+" has not a executor.");

                Ant ant_new = (Ant) getProject().createTask( "ant" );
                ant_new.setOwningTarget(getOwningTarget()); 
                ant_new.setAntfile( exec.getAntFile() );
                ant_new.setTarget( exec.getTarget() );
                if (exec.getDir() != null) ant_new.setDir( getProject().resolveFile ( exec.getDir()));
                ant_new.init();
                
                /// ??????? add xtest.userdata| prefix ?????
                
                // add all test properties for given testbag
                Testbag.TestProperty properties[] = testbag.getTestProperties();
                if (properties != null)
                    for (int j=0; j<properties.length; j++) {
                        Property ant_prop = ant_new.createProperty();
                        ant_prop.setName(  properties[j].getName() );
                        ant_prop.setValue( properties[j].getValue() );
                    }
                
                // Set classpath property for given testbag
                Property clspth_prop = ant_new.createProperty();
                clspth_prop.setName( targetParamClasspathProp );
                
                StringBuffer stb = new StringBuffer();
                
                // if any of the testset contains setup dir, it does not
                // have to be added to classpath
                boolean testsetContainsSetupDir = false;
                if (testbag.getSetupDir() == null) {
                    testsetContainsSetupDir = true;
                }
                
                for (int j=0; j<testbag.getTestsets().length; j++) {
                    // add compiled tests to classpath (work/sys/test)
                    
                    stb.append( ant_new.getProject().getProperty("xtest.tests.dir") );
                    stb.append(File.pathSeparatorChar);

                    // check if this testset contains setup dir
                    if ( ! testsetContainsSetupDir) {
                        String testsetDir = testbag.getTestsets()[j].getDir();
                        testsetContainsSetupDir = testbag.getSetupDir().equals(testsetDir);
                    }
                }
                // add setup/teardown dir if available
                if ( ! testsetContainsSetupDir ) {
                    stb.append(testbag.getSetupDir());
                    stb.append(File.pathSeparatorChar);
                }
               
                if ( stb.length() > 1 ) {
                    stb.deleteCharAt( stb.length() - 1 );                
                }
                clspth_prop.setValue( stb.toString() );
                
                // set name of executed test config
                Property cttprop = ant_new.createProperty();
                cttprop.setName( targetParamTestConfigProp );
                cttprop.setValue( MTestConfigTask.getMTestConfig().getTesttype() );
                
                // set name of executed testbag
                Property nameprop = ant_new.createProperty();
                nameprop.setName( targetParamNameProp );
                nameprop.setValue( testbag.getName() );
                
                // set name of executor for executed testbags
                Property execprop = ant_new.createProperty();
                execprop.setName( targetParamExecutorProp );
                execprop.setValue( testbag.getExecutor().getName() );
                
                if (testbag.getTimeout() != null) {
                    Property timeoutprop = ant_new.createProperty();
                    timeoutprop.setName( targetParamTimeoutProp );
                    timeoutprop.setValue( testbag.getTimeout().toString() );
                }
                
                // Need to unpack test because we want to search for tests only
                // in compiled distribution.
                getProject().executeTarget("prepare-tests");
                
                createJUnitTestRunnerPropertyFile(testbag);
                
                // execute tests
                ant_new.execute();
                
                // run results processor
                PluginExecuteTask.executeCorrespondingResultProcessor(this);
            }
        
    }
    
    private JUnitTestRunnerProperties createJUnitTestRunnerPropertyFile(Testbag testbag) {
        
       ArrayList testsWithFilters = new ArrayList();
       try {
       
         for (int i=0; i<testbag.getTestsets().length; i++) {
            Testbag.Testset testset = testbag.getTestsets()[i];
            TestScanner ts = new TestScanner();

            // scan for tests in ${xtest.tests.dir} (i.e. work/sys/tests)
            String testsClassesDir = getProject().getProperty("xtest.tests.dir");
            ts.setBasedir(testsClassesDir);

            /*
            for (int i=0; i<additionalPatterns.size(); i++) {
                Object o = additionalPatterns.elementAt(i);
                defaultPatterns.append((PatternSet) o, p);
            } */
            
            /*System.out.println("Testbag "+testbag.getName());
            System.out.println("testset = "+testset);
            System.out.println("Testset includes "+testset.getIncludes());
            */
            ts.setIncludes(testset.getIncludes());
            ts.setExcludes(testset.getExcludes());
                
            // scan directories for test classes and associated test patterns
            ts.scan();
           
            TestFilter[] filter;
                filter = ts.getIncludedFiles();
           
            // go through all found test classes and prepare the JUnitTest instances
            for(int j = 0;  j < filter.length; j++) {
                String testName = fileToClassname(filter[j].getFile());
                log("Adding test class "+testName,Project.MSG_VERBOSE);
                TestWithFilter test = new TestWithFilter();
                test.testName = testName;
                test.includeFilter = filter[j].getFilter().getIncludes();
                test.excludeFilter = filter[j].getFilter().getExcludes();
                // add test to array
                testsWithFilters.add(test);
            }
         }
       } catch (IOException e) {
            throw new BuildException(e.getMessage(),e);
       }
       
        log("Preparing test runner property file",Project.MSG_DEBUG);
        JUnitTestRunnerProperties props = new JUnitTestRunnerProperties();
        // results dir
        props.setResultsDirName(resultDir.getAbsolutePath());
        // setup teardown
        if ((testbag.getSetUpClassName() != null) && (testbag.getSetUpMethodName() != null)) {
            props.setTestbagSetup(testbag.getSetUpClassName(), testbag.getSetUpMethodName());
        }
        if ((testbag.getTearDownClassName() != null) && (testbag.getTearDownMethodName() != null)) {
            props.setTestbagTeardown(testbag.getTearDownClassName(), testbag.getTearDownMethodName());
        }        
        // tests
        for (int i=0; i < testsWithFilters.size(); i++) {
            TestWithFilter test = (TestWithFilter)testsWithFilters.get(i);
            props.addTestNameWithFilter(test.testName,test.includeFilter,
                            test.excludeFilter);                
        }
        // save it to workdir
        try {
            File testRunnerPropertyFile = new File(workDir, TestRunnerHarness.TESTLIST_FILENAME);
            log("Saving test runner property to "+testRunnerPropertyFile,Project.MSG_DEBUG);
            props.save(testRunnerPropertyFile);
        } catch (IOException ioe) {
            throw new BuildException("Cannot save test runner property file",ioe);
        }
        return props;
        
    }
    
    // convert file to class name
    private String fileToClassname(String file) {
        if (file.endsWith(".java")) {
            file = file.substring(0, file.length() - 5); // ".java".length() equals 5
        }
        else if (file.endsWith(".class")) {
            file = file.substring(0, file.length() - 6); // ".class".length() equals 6
        }
        return javaToClass(file);
    }    
    
    /**
     * convenient method to convert a pathname without extension to a
     * fully qualified classname. For example <tt>org/apache/Whatever</tt> will
     * be converted to <tt>org.apache.Whatever</tt>
     * @param filename the filename to "convert" to a classname.
     * @return the classname matching the filename.
     */
    public final static String javaToClass(String filename){
        return filename.replace(File.separatorChar, '.');
    }
    
    // this is rather struct than class :-(
    // this represents test (testname) with include and exclude filters
    static class TestWithFilter {
        String   testName;
        Filter.IncludeExclude[] includeFilter;
        Filter.IncludeExclude[] excludeFilter;
    }            
}
