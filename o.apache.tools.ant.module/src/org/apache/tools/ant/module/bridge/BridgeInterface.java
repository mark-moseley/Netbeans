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

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.openide.windows.OutputWriter;

/**
 * What is implemented by bridge.jar.
 * @author Jesse Glick
 */
public interface BridgeInterface {
    
    /**
     * Actually run a build script.
     * @param buildFile an Ant build script
     * @param targets a list of target names to run, or null to run the default target
     * @param in an input stream for console input
     * @param out an output stream with the ability to have hyperlinks
     * @param err an error stream with the ability to have hyperlinks
     * @param properties any Ant properties to define
     * @param verbosity the intended logging level
     * @param displayName a user-presentable name for the session
     * @return true if the build succeeded, false if it failed for any reason
     */
    boolean run(File buildFile, List targets, InputStream in, OutputWriter out, OutputWriter err, Properties properties, int verbosity, String displayName);
    
    /**
     * Get some informational value of the Ant version.
     * @return the version
     */
    String getAntVersion();
    
    /**
     * Check whether Ant 1.6 is loaded.
     * If so, additional abilities may be possible, such as namespace support.
     */
    boolean isAnt16();
    
    /**
     * Get a proxy for IntrospectionHelper, to introspect task + type structure.
     */
    IntrospectionHelperProxy getIntrospectionHelper(Class clazz);
    
    /**
     * See Project.toBoolean.
     */
    boolean toBoolean(String val);
    
    /**
     * Get values of an enumeration class.
     * If it is not actually an enumeration class, return null.
     */
    String[] getEnumeratedValues(Class c);
    
}
