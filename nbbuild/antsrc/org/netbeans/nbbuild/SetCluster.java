/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/** Settigns the given property to cluster value
 *
 * @author Michal Zlamal
 */
public class SetCluster extends Task {
    private String name = null;
    private String cluster;
    private String thisModuleName = null;
    private String defaultLocation = null;
    
    /** Sets the name of property which should contain the value */
    public void setName(String name) {
        this.name = name;
    }
    
    /** Name of a cluster */
    public void setCluster (String cluster) {
        this.cluster = cluster;
    }
    
    /** Name of this module */
    public void setModule(String module) {
        thisModuleName = module;
    }

    /** Location of default cluster */
    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
    }
    
    public void execute() throws BuildException {
        if (name == null) {
            throw new BuildException("Name of property to set have to be specified",this.getLocation());
        }
        if (cluster != null) {
            String clusterDir = this.getProject().getProperty(cluster + ".dir");
            if (clusterDir == null) throw new BuildException( "Property: " + cluster + ".dir have to be defined", this.getLocation());
            this.getProject().setProperty( name, clusterDir );
            return;
        }
        if (thisModuleName == null) {
            throw new BuildException("The name of current module have to be set", getLocation());
        }

        for (Object key : getProject().getProperties().keySet()) {
            String property = (String) key;
            String clusterDir = getProject().getProperty(property + ".dir");
            if (clusterDir == null) {
                continue;
            }
            String list = this.getProject().getProperty( property );
            assert list != null : property;
            StringTokenizer modTokens = new StringTokenizer(list," \t\n\f\r,");
            while (modTokens.hasMoreTokens()) {
                String module = modTokens.nextToken();
                if (module.equals(thisModuleName)) {
                    // We found the list reffering to this module
                    log( "Property: " + name + " will be set to " + clusterDir, Project.MSG_VERBOSE);
                    this.getProject().setProperty( name, clusterDir );
                    return;
                }
            }
        }
       log("No cluster list with this module: " + thisModuleName + " was found. Using default cluster location: " + defaultLocation, Project.MSG_WARN);
       if (defaultLocation == null)
           throw new BuildException("No default cluster location defined", this.getLocation());

       log( "Property: " + name + " will be set to " + defaultLocation, Project.MSG_VERBOSE);
       this.getProject().setProperty( name, defaultLocation );
    }
}
