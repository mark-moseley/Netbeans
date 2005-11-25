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

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author lm97939
 */
public class GetSystemProperty extends Task {

    private String system_prop = null;
    private String ant_prop = null;
    
    public void setSystemProperty(String s) {
        system_prop = s;
    }

    public void setAntProperty(String s) {
        ant_prop = s;
    }
    
    public void execute() throws BuildException {
        if (system_prop == null) throw new BuildException("Attribute 'systemProperty' isn't set.");
        if (ant_prop == null) ant_prop = system_prop;
        
        String value = System.getProperty(system_prop);
        if (value != null) getProject().setProperty(ant_prop,value);
    }

}
