/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * NbExecutor.java
 *
 * Created on March 28, 2001, 6:57 PM
 */

package org.netbeans.xtest.harness;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;

import java.util.*;
import java.io.File;

/**
 *
 * @author  mk97936
 * @version
 */
public class MTestCompiler extends Task {
    
    String targetName = null;
    String targetParamClasspathProp = null;
    String targetParamTestConfigProp = "tbag.testtype";
    String targetParamNameProp = null;
    String targetParamExecutorProp = "tbag.executor";
    
    
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
    
    
    public void executeDefinedCompiler(Testbag testbag, Ant antTask) throws BuildException{
        
        MTestConfig.AntExecType compiler = testbag.getCompiler();
        
        antTask.setAntfile( compiler.getAntFile() );
        antTask.setTarget( compiler.getTarget() );
        
        if (compiler.getDir() != null) {
            antTask.setDir( antTask.getProject().resolveFile( compiler.getDir()));
        }
        
        //ant_new.init();
        
        /// ??????? add xtest.userdata| prefix ?????
        // add all test properties for given testbag
                /*
                Testbag.TestProperty properties[] = testbag.getTestProperties();
                if (properties != null)
                    for (int j=0; j<properties.length; j++) {
                        Property ant_prop = ant_new.createProperty();
                        ant_prop.setName( properties[j].getName() );
                        ant_prop.setValue( properties[j].getValue() );
                    }
                 
                 */
        // Set classpath property for given testbag
                /*
                Property clspth_prop = ant_new.createProperty();
                clspth_prop.setName( targetParamClasspathProp );
                 */
        
                /*
                StringBuffer stb = new StringBuffer();
                for (int j=0; j<testbag.getTestsets().length; j++) {
                    // name of this property should be passed by atribute
                    // and also it's not clear where it's resolved !!!!!!
                    stb.append( ant_new.getProject().getProperty( "tbag.classpath.root" ) );
                    // don't need it :-)
                 
                 
                    //stb.append( "/" );
                    //stb.append( MTestConfigurator.getCurrentTestType() );
                 
                    stb.append( "/" );
                    stb.append( ant_new.getProject().getProperty( "tbag.classpath.work" ) );
                    stb.append( "/" );
                    stb.append( testbag.getTestsets()[j].getDir());
                    stb.append( ";" );
                }
                if ( stb.length() > 1 ) {
                    stb.deleteCharAt( stb.length() - 1 );
                }
                clspth_prop.setValue( stb.toString() );
                 */
        // set name of executed test config
        Property cttprop = antTask.createProperty();
        cttprop.setName( targetParamTestConfigProp );
        cttprop.setValue( MTestConfigTask.getMTestConfig().getTesttype() );
        
        // set name of executed testbag
        Property nameprop = antTask.createProperty();
        nameprop.setName( targetParamNameProp );
        nameprop.setValue( testbag.getName() );
        
        // set name of executor for executed testbags
                /*
                Property execprop = ant_new.createProperty();
                execprop.setName( targetParamExecutorProp );
                execprop.setValue( testbag.getExecutor().getName() );
                 */
        
        // execute tests
        antTask.execute();
    }
    
    /*
    public void executePluginCompiler(String pluginName, Ant antTask) throws BuildException {
    }
     */
    
    public void execute() throws BuildException {
        
        if (null == targetParamClasspathProp || 0 == targetParamClasspathProp.length())
            throw new BuildException("Attribute 'targetParamClasspathProp' has to be set.");
        
        Testbag testbags[] = MTestConfigTask.getTestbags();
        if (null == testbags)
            throw new BuildException("TestBag configuration wasn't chosen.", getLocation());

        for (int i=0; i<testbags.length; i++) {
                // get TestBag 
                Testbag testbag = testbags[i];

                if (testbag.getCompiler() == null) {
                    throw new BuildException("Testbag "+testbag.getName()+" has not a defined compiler.");
                    // check whether there is plugin
                    /*
                    if (testbag.getPluginName() == null) {
                        throw new BuildException("Testbag "+testbag.getName()+" has not a compiler, nor plugin defined.");
                    }
                     */
                } 

                //Ant ant_new = (Ant) getProject().createTask( "ant" );
                Ant ant = new Ant();
                ant.setOwningTarget( this.getOwningTarget() ); 
                ant.setProject(getProject());
                executeDefinedCompiler(testbag, ant);
            }
        
    }
    
}
