/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * XTestTask.java
 * for now using only static configuration hardcoded in this class
 * This is clearly far from the best solution, but it works for now
 * configuration should be based on a xml file ...
 *
 * Created on 22.9. 2003, 17:04
 */

package org.netbeans.xtest.usertasks;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.Ant;
//import org.apache.tools.ant.taskdefs.Property;
import java.io.File;

/**
 * @author mb115822
 */
public class XTestActionTask extends Task {

    // action names - should be loaded from the config file
    private static final String ACTION_BUILD_TESTS = "buildTests";
    private static final String ACTION_RUN_TESTS = "runTests";
    private static final String ACTION_CLEAN_TESTS = "cleanTests";
    private static final String ACTION_CLEAN_RESULTS = "cleanResults";
    private static final String ACTION_CLEAN_ALL = "cleanAll";
    private static final String ACTION_VERSION = "version";
    private static final String ACTION_GENERATE_FAILED_CONFIG = "generateFailedConfig";
    private static final String ACTION_GENERATE_EXCLUDED_CONFIG = "generateExcludedConfig";
    
    // this is not public action
    private static final String ACTION_PREPARE_TESTS ="prepare-tests";
    
    // targets to be executed -> for now all will be executed from 
    // ${xtest.home}/lib/module_harness.xml, but it might change in the future
    private static final String ACTIONS_SCRIPT_FILENAME = "lib/module_harness.xml";
    
    // target names
    private static final String TARGET_BUILD_TESTS = "compiler-launcher"; // weird naming - should be named buildtests, but
                                                                   // this is used by the deprecated compilers
    private static final String TARGET_RUN_TESTS = "runtests";
    private static final String TARGET_CLEAN_TESTS = "cleantests";
    private static final String TARGET_CLEAN_RESULTS = "cleanresults";
    private static final String TARGET_CLEAN_ALL    = "realclean";
    private static final String TARGET_VERSION      = "version";
    private static final String TARGET_GENERATE_FAILED_CONFIG = "generateFailedConfig";
    private static final String TARGET_GENERATE_EXCLUDED_CONFIG = "generateExcludedConfig";
    
    // non-public target
    private static final String TARGET_PREPARE_TESTS      = "prepare-tests";
    
    private static final String[][] ACTION_MATRIX = {
        {ACTION_BUILD_TESTS, TARGET_BUILD_TESTS},
        {ACTION_RUN_TESTS, TARGET_RUN_TESTS},
        {ACTION_CLEAN_TESTS, TARGET_CLEAN_TESTS},
        {ACTION_CLEAN_RESULTS, TARGET_CLEAN_RESULTS},
        {ACTION_CLEAN_ALL, TARGET_CLEAN_ALL},
        {ACTION_VERSION, TARGET_VERSION},
        {ACTION_PREPARE_TESTS, TARGET_PREPARE_TESTS},
        {ACTION_GENERATE_FAILED_CONFIG, TARGET_GENERATE_FAILED_CONFIG},
        {ACTION_GENERATE_EXCLUDED_CONFIG, TARGET_GENERATE_EXCLUDED_CONFIG}
    };
    
    
    private static String getTargetForAction(String action) {
        for (int i=0; i < ACTION_MATRIX.length; i++) {
            if (ACTION_MATRIX[i][0].equalsIgnoreCase(action)) {
                return ACTION_MATRIX[i][1];
            }
        }
        return null;
    }
    
    private String xtestHome;
    private static final String XTEST_HOME_PROPERTY_NAME = "xtest.home";
    
    private String executeAction;
    
    public void setXTestHome(String xtestHome) {
        this.xtestHome = xtestHome;
    }
    
    public void setExecuteAction(String executeAction) {
        this.executeAction = executeAction;
    }
    
    private void setXTestHome() throws BuildException {
        if (this.getProject().getProperty(XTEST_HOME_PROPERTY_NAME) == null) {
            if (this.xtestHome != null) {
                this.getProject().setProperty(XTEST_HOME_PROPERTY_NAME, xtestHome);
            } else {
                throw new BuildException("Cannot continue - "+XTEST_HOME_PROPERTY_NAME+" is not set");
            }
        } else {
            xtestHome = this.getProject().getProperty(XTEST_HOME_PROPERTY_NAME);
        }
    }
    
    private void executeAction() throws BuildException {
        String target = getTargetForAction(executeAction);
        if (target != null) {
            File antFile = (new File(new File(xtestHome),ACTIONS_SCRIPT_FILENAME));
            Ant newAnt = new Ant();
            newAnt.setProject(this.getProject());
            newAnt.setOwningTarget(this.getOwningTarget());
            newAnt.setAntfile(antFile.getAbsolutePath());
            newAnt.setTarget(target);
            log("XTest will execute action "+executeAction);
            newAnt.execute();
        } else {
            throw new BuildException("Cannot execute unknown action '"+executeAction+"'");
        }
    }
    
    public void execute() throws BuildException {
        setXTestHome();
        executeAction();       
    }
    
}
