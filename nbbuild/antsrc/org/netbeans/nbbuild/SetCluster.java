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

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;

/** Settigns the given property to cluster value
 *
 * @author Michal Zlamal
 */
public class SetCluster extends org.apache.tools.ant.Task {
    private String name = null;
    private String propertiesList = null;
    private String cluster;
    private String thisModuleName = null;
    
    /** Sets the name of property which should contain the value */
    public void setName(String name) {
        this.name = name;
    }
    
    /** Comma separated list of properties. One of those properties should contain the name of module from what it is ran. */
    public void setList( String propertiesList ) {
        this.propertiesList = propertiesList;
    }
    
    /** Name of a cluster */
    public void setCluster (String cluster) {
        this.cluster = cluster;
    }
    
    /** Name of this module */
    public void setModule(String module) {
        thisModuleName = module;
    }
    
    public void execute() throws BuildException {
        if (name == null)
            throw new BuildException("Name of property to set have to be specified",this.getLocation());
        if (propertiesList != null) {
            if (cluster != null)
                throw new BuildException("Either list or cluster property can be specified not both",this.getLocation());
            if (thisModuleName == null)
                throw new BuildException("The name of current module have to be set", this.getLocation());
        } else {
            if (cluster == null) {
                throw new BuildException("Either list or cluster property have to be specified",this.getLocation());
            }
            if (thisModuleName != null) {
                throw new BuildException("When cluster property is used thisModuleName should not be set",this.getLocation());
            }
        }
        
        if (cluster != null) {
            String clusterDir = this.getProject().getProperty(cluster + ".dir");
            if (clusterDir == null) throw new BuildException( "Property: " + cluster + ".dir have to be defined", this.getLocation());
            this.getProject().setProperty( name, clusterDir );
            return;
        }
        
        HashSet modules = new HashSet();
        
        StringTokenizer tokens = new StringTokenizer( propertiesList, " \t\n\f\r," );
        while (tokens.hasMoreTokens()) {
            String property = tokens.nextToken().trim();
            String list = this.getProject().getProperty( property );
            if (list == null) throw new BuildException("Property: " + property + " is not defined anywhere",this.getLocation());
            StringTokenizer modTokens = new StringTokenizer(list," \t\n\f\r,");
            while (modTokens.hasMoreTokens()) {
                String module = modTokens.nextToken();
                log( property + " " + module, Project.MSG_VERBOSE );
                if (module.equals(thisModuleName)) {
                    // We found the list reffering to this module
                    String clusterDir = this.getProject().getProperty(property + ".dir");
                    if (clusterDir == null) throw new BuildException( "Property: " + property + ".dir have to be defined", this.getLocation());
                    System.out.println( "Property: "+name+" will be set to " + clusterDir);
                    this.getProject().setProperty( name, clusterDir );
                    return;
                }
            }
        }
        throw new BuildException("No cluster list with this module: " + thisModuleName + " was found", this.getLocation());
    }
}
