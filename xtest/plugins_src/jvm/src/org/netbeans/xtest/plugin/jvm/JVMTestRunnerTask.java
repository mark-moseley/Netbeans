/*
 *
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
 * JUnitTestRunnerTask.java
 *
 * Created on November 26, 2002, 2:32 PM
 */

package org.netbeans.xtest.plugin.jvm;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.*;
import java.util.*;
import org.apache.tools.ant.taskdefs.*;
//import org.apache.tools.ant.AntClassLoader;
import org.netbeans.xtest.testrunner.*;

/**
 *
 * @author  mb115822
 */
public class JVMTestRunnerTask extends Task implements TestBoardLauncher {

    /** Creates a new instance of JUnitTestRunnerTask */
    public JVMTestRunnerTask() {
    }
    
    
    // sys property
    public void addSysProperty(Environment.Variable sysp) {
        log("Adding system property "+sysp.getKey()+"="+sysp.getValue(),Project.MSG_DEBUG);        
        sysProperties.add(sysp);

    }
    
    // sys property file
    /**
     * Add a nested syspropertyfile element. This might be useful to tranfer
     * Ant properties from file to the testcases.
     */
    public void addConfiguredSysPropertyFile(FileVariable  fileVariable) throws IOException {
        log("Adding sys property file "+fileVariable.file,Project.MSG_DEBUG);
        Properties props = new Properties();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileVariable.file));
        props.load(bis);
        bis.close();
        Iterator iter = props.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            Environment.Variable var = new Environment.Variable();
            var.setKey((String)entry.getKey());
            var.setValue((String)entry.getValue());
            sysProperties.add(var);    
        }
    }
    
    
    public Path createClasspath() {
        log("Creating classpath",Project.MSG_DEBUG);
        return commandLine.createClasspath(getProject()).createPath();
    }
    
    // work directory
    public void setWorkDir(File workDir) {
        log("Setting workDir to "+workDir, Project.MSG_DEBUG);
        this.workDir = workDir;
    }
    
    private String getJavaExecutableName() {
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
            // hey, we're running on windows
            return "java.exe";
        } else {
            // normal OS :-)
            return "java";
        }
    }
    
    public void setJdkHome(File jdkHome) {
        if ((jdkHome != null) & (jdkHome.isDirectory())) {
            log("jdkHome is "+jdkHome,Project.MSG_DEBUG);
            // try to find java executable
            File binDir = new File(jdkHome,"bin");
            log("Bin is "+binDir, Project.MSG_DEBUG);
            if (binDir.isDirectory()) {
                File javaExecutable = new File(binDir,getJavaExecutableName());
                log("Executable is "+javaExecutable, Project.MSG_DEBUG);
                if (javaExecutable.exists()&(!javaExecutable.isDirectory())) {
                    log("Setting jdkHome to "+jdkHome, Project.MSG_DEBUG);
                    commandLine.setVm(javaExecutable.getAbsolutePath());
                    return;
                }
            }            
        }        
        // java executable not found !!!
        log("jdkHome is not set to a correct JAVA_HOME directory: "+jdkHome);
        log("  - using the same Java as the script is running in.");        
    }
    // jvm args
    public void setJvmArgs(String jvmArgs) {
        log("Setting jvm args to "+jvmArgs, Project.MSG_DEBUG);
        this.jvmArgs = jvmArgs;
    }
    
    /**
     * when set to true, -ea switch is used to run the JVM
     */
    public void setEnableAssertions(boolean enableAssertions) {
        this.enableAssertions = enableAssertions;
    }
    
    /** Debug address to connect to 
     */
    public void setDebugAddress(String address) {
       if (address.equals("")) return;

       this.debugAddress = address;
    }
    
    /**
     * when port is set > 0, JVM is started in debugging mode
     */
    public void setDebugPort(int port) {
       if (port == 0) return;
       
       
        this.debugPort=port;
    }
    
    /**
     * suspend switch of debug
     */
    public void setDebugSuspend(boolean suspend) {
        this.debugSuspend = suspend;
    }    
    
    // test mode
    public void setTestMode(String testMode) {
        if ((testMode != null) & (!testMode.equals(""))) {
            log("Setting test modes to "+testMode, Project.MSG_DEBUG);
            this.testMode = testMode;
        } else {
            log("Leaving default setting.", Project.MSG_DEBUG);
        }
    }
    
    // timeout
    public void setTimeout(Long value) {
        this.timeout = value;
    }
    
    public void execute() throws BuildException {
        try {
            log("Running JUnit in plain VM");
            // check validity of input values
            checkInputValuesValidity();
            // log input Values
            logInputValues(Project.MSG_VERBOSE);
            // execute test run (this depends on testmode)
            TestRunnerHarness testRunnerHarness = new TestRunnerHarness(this, workDir, testMode);
            testRunnerHarness.runTests();
        } catch (IOException ioe) {
            throw new BuildException(ioe.getMessage()==null?"IOException caught":ioe.getMessage(), ioe);
        }
    }
    
    // private stuff

    // system properties arraylist 
    private ArrayList sysProperties = new ArrayList();

    // command line helper - from ant
    private CommandlineJava commandLine = new CommandlineJava();
    
    // test runner class name
    private static final String TESTRUNNER_CLASS_NAME = "org.netbeans.xtest.plugin.jvm.JUnitTestRunnerLauncher";
    // testlist filename
    private static final String TESTLIST_FILENAME = "testrunner.testlist";
    
    
    // test runner property file - is set via prepareTestRunnerProperties and used by prepareCommandLine
    private File testRunnerPropertyFile = null;
    
    // work dir where test runner property file is stored
    private File workDir;
    // jvm args
    private String jvmArgs;
    // test mode (e.g. tastbag, testsuite ....)
    private String testMode = TestRunnerHarness.TESTRUN_MODE_TESTSUITE;
    // timeout
    private Long timeout = null;
    
    // jvm should be started with -ea switch
    protected boolean enableAssertions = true;
    
    // debug should suspend code execution
    protected boolean debugSuspend = false;
    
    // debug port to which debugger is connected. When 0 - debugging is not started
    protected int debugPort = 0;
    
    /** debug address to connect to */
    private String debugAddress;
    
 
    //
    private void checkInputValuesValidity() throws BuildException {
        // workDir
        if (workDir == null) {
            throw new BuildException("WorkDir is not set");
        }
    }
    
    // verbose output of execute (overview of task arguments)
    private void logInputValues(int logLevel) {
        // verbose logs
        log("Using Java from: "+commandLine.getVmCommand().getExecutable(),logLevel);
        log("Using classpath: "+commandLine.getClasspath(),logLevel);
        log("Using work dir: "+workDir.getPath(),logLevel);        
        // sys properties
        Iterator i  = sysProperties.iterator();
        while (i.hasNext()) {
            Environment.Variable var = (Environment.Variable)i.next();
            log("Using system property (key=value): "+var.getKey()+"="+var.getValue(),logLevel);
        } 
        // jvm args
        if ((jvmArgs != null)&(!jvmArgs.equals(""))) {
            log("Using JVM args: "+jvmArgs,logLevel);
        }
        // test mode
        if (testMode != null) {
            log("Using test mode: "+testMode,logLevel);
        }
    }
    
    // VM starting method
    public void launchTestBoard(JUnitTestRunnerProperties testsToBeExecuted) throws TestBoardLauncherException {        
        File runnerPropertiesFile = new File(workDir,TESTLIST_FILENAME);
        try {
            // save runner properties to a file
            testsToBeExecuted.save(runnerPropertiesFile);
            CommandlineJava preparedCommandLine = prepareCommandLine(commandLine, runnerPropertiesFile);
            log("Running VM",Project.MSG_VERBOSE);
            executeCommandLine(preparedCommandLine);
        } catch (IOException ioe) {
            throw new TestBoardLauncherException("During launching tests caught IOException"+ioe.getMessage(),ioe);
        } finally {
            // delete runner properties
            runnerPropertiesFile.delete();
        }
    }       
    
    // save runner properties to a file
    
    
    // propare command line
    private CommandlineJava prepareCommandLine(CommandlineJava existingCommandLine, File runnerPropertiesFile) throws BuildException {
        log("Preparing command line",Project.MSG_DEBUG);
        CommandlineJava commandLine;
        try {
            commandLine = (CommandlineJava) existingCommandLine.clone();
        } catch (Exception ex) {
            // since ant1.6.3 java.lang.CloneNotSupportedException can be thrown
            throw new BuildException(ex);
        }
        log("Preparing command line",Project.MSG_DEBUG);
        commandLine.setClassname(TESTRUNNER_CLASS_NAME);
        // JVM args
        if ((jvmArgs != null ) & (!jvmArgs.equals(""))) {
            commandLine.createVmArgument().setLine(jvmArgs);
        }
        // enable assertions
        if (enableAssertions) {
            commandLine.createVmArgument().setValue("-ea");
        }
        // debugger
        if (debugPort > 0) {
            if (debugAddress != null) {
                throw new BuildException("Cannot set debug port and address at once: " + debugPort + " address: " + debugAddress);
            }
            String suspendArg = debugSuspend ? "y" : "n";
            String debugArgument = "-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend="+suspendArg+",address="+debugPort;
            commandLine.createVmArgument().setLine(debugArgument);
        } else {
            if (debugAddress != null) {
                String suspendArg = debugSuspend ? "y" : "n";
                String debugArgument = "-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,suspend="+suspendArg+",address="+debugAddress;
                commandLine.createVmArgument().setLine(debugArgument);
            }
        }
        
        
        // add runnerproperties file sys property
        Environment.Variable runnerProperties = new Environment.Variable();
        runnerProperties.setKey(JUnitTestRunner.TESTRUNNER_PROPERTIES_FILENAME_KEY);
        runnerProperties.setValue(runnerPropertiesFile.getAbsolutePath());        
        commandLine.addSysproperty(runnerProperties);
        
        // sys properties
        Iterator i  = sysProperties.iterator();
        while (i.hasNext()) {
            Environment.Variable var = (Environment.Variable)i.next();
            commandLine.addSysproperty(var);
        }
        return commandLine;
    }
    
    // execute the command line
    private void executeCommandLine(CommandlineJava execteCommandLine) throws BuildException {        
        //CommandlineJava cmd = (CommandlineJava) execteCommandLine.clone();
        CommandlineJava cmd;
        try {
            // since ant1.6.3 java.lang.CloneNotSupportedException can be thrown
            cmd = (CommandlineJava) execteCommandLine.clone();
        } catch (Exception ex) {
            throw new BuildException (ex);
        }
        ExecuteWatchdog watchdog = createWatchdog();
        Execute execute = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN), watchdog);
        execute.setCommandline(cmd.getCommandline());
        if (workDir != null) {
            execute.setWorkingDirectory(workDir);
            // ????? what does this do ??????
            execute.setAntRun(getProject());
        }
        // execute
        log("Executing: "+cmd.toString(), Project.MSG_VERBOSE);
        //log("Executing: "+cmd.toString(), Project.MSG_INFO);
        try {
            int result = execute.execute();
        } catch (IOException e) {
            throw new BuildException("Process fork failed.", e, getLocation());
        }
    }
    
    /**
     * @return <tt>null</tt> if there is a timeout value, otherwise the
     * watchdog instance.
     */
    private ExecuteWatchdog createWatchdog() throws BuildException {
        if (timeout == null){
            return null;
        }
        return new JVMExecuteWatchdog(timeout.longValue(), getProject());
    }
        
 
    /////////////////////////
    ///////// inner classes
    /////////////////////////
    
    /**
     * Nested 'syspropertyfile' element. It has only one attribute, file.
     */
    public static class FileVariable  {
        private File file;
          
        public void setFile(java.io.File file) {
            this.file = file;
        }
    }
    
}