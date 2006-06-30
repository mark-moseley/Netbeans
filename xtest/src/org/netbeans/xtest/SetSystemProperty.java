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

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author lm97939
 */
public class SetSystemProperty extends Task {

    private String system_prop = null;
    private String value = null;

    public void setSystemProperty(String s) {
        system_prop = s;
    }

    public void setValue(String v) {
        value = v;
    }

    public void execute() throws BuildException {
        if (system_prop == null) throw new BuildException("Attribute 'systemProperty' isn't set.");
        if (value == null) throw new BuildException("Attribute 'value' isn't set.");
        
        System.setProperty(system_prop,value);
    }

}
