/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * Factory for EJB Module projects
 * @author Chris Webster
 */
public class SQLproProjectType implements AntBasedProjectType {
    public static final String TYPE = "org.netbeans.modules.sql.project";
    public static final String PROJECT_CONFIGURATION_NAME = "data";
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2ee-sqlpro/1";
    public static final String PRIVATE_CONFIGURATION_NAME = "data";
    public static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2ee-sqlpro-private/1";
    
    
    public SQLproProjectType() {
        int i = 0;
    }
    
    public String getType() {
        return TYPE;
    }
    
    public Project createProject(AntProjectHelper helper) throws IOException {
        if (null == helper)
            throw new IllegalArgumentException("helper");
        return new SQLproProject(helper);
    }

    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }
    
}
