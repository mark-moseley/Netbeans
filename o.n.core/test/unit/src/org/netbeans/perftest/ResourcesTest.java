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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.perftest;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import junit.framework.*;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/** Tests for resources contained in modules.
 *
 * @author radim
 */
public class ResourcesTest extends NbTestCase {

    // TODO the idea is to check for duplicates
    //      for patterns that are not effective in NB environment
    private Logger LOG;

    public ResourcesTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(ResourcesTest.class);
        
        return suite;
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        
        super.setUp();
    }

    /** If package contains only one resource it is not too frendly to our classloaders.
     * Generally not a big problem OTOH we want to avoid this if this is simple.
     */
    public void testOneInPackage() throws Exception {
        SortedSet<Violation> violations = new TreeSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            // check JAR files only
            if (!f.getName().endsWith(".jar"))
                continue;
            
            // ignore branding
            if (f.getName().endsWith("_nb.jar"))
                continue;
            
            SortedMap<String, Integer> resourcesPerPackage = new TreeMap<String, Integer>();
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            int entryCount = 0;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;
                
                String name = entry.getName();
                String prefix = (name.lastIndexOf('/') >= 0)?
                    name.substring(0, name.lastIndexOf('/')): "";
                if (prefix.startsWith("META-INF")
                || prefix.startsWith("1.0/")
                || prefix.startsWith("com/")
                || prefix.startsWith("javax/")
                || prefix.startsWith("freemarker/")
                || prefix.startsWith("org/apache/tomcat")
                || prefix.startsWith("org/apache/lucene")
                || prefix.startsWith("org/w3c/")
//                || prefix.startsWith("")
                || prefix.startsWith("org/netbeans/modules/openide/actions")
                || prefix.startsWith("org/netbeans/modules/openide/awt")
                || prefix.startsWith("org/netbeans/modules/openide/windows")
                || prefix.startsWith("org/openide/explorer/propertysheet") // in deprecated core/settings
                || prefix.startsWith("org/openide/io")
                || prefix.startsWith("org/netbeans/api")
                || prefix.startsWith("org/netbeans/spi")
                || prefix.startsWith("org/netbeans/core/execution/beaninfo")
                || prefix.startsWith("org/netbeans/modules/web/monitor")
                || prefix.matches("org/netbeans/.*/[as]pi.*")
                        ) {
                    continue;
                }
                
                entryCount++;
                Integer count = resourcesPerPackage.get(prefix);
                if (count != null) {
                    resourcesPerPackage.put(prefix, count+1);
                }
                else {
                    resourcesPerPackage.put(prefix, 1);
                }
            }
            if (entryCount > 1) { // filter library wrappes (they have only Bundle.properties)
                for (Map.Entry<String, Integer> pkgInfo: resourcesPerPackage.entrySet()) {
                    if (pkgInfo.getValue().equals(1)) {
                        violations.add(new Violation(pkgInfo.getKey(), jar.getName(), " has package with just one resource"));
                    }
                }
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Some JARs in IDE contains sparsely populated packages ("+violations.size()+"):\n");
            for (Violation viol: violations) {
                msg.append(viol.entry).append(" in ").append(viol.jarFile).append('\n');
            }
            fail(msg.toString());
        }
        //                    assertTrue (entry.toString()+" should have line number table", v.foundLineNumberTable());
    }
    
    private static class Violation implements Comparable<Violation> {
        String entry;
        String jarFile;
        String comment;
        Violation(String entry, String jarFile, String comment) {
            this.entry = entry;
            this.jarFile = jarFile;
            this.comment = comment;
        }
    
        public int compareTo(Violation v2) {
            String second = v2.entry + v2.jarFile;
            return (entry +jarFile).compareTo(second);
        }
    }

}
