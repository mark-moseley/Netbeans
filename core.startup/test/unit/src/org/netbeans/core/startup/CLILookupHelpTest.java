/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Permission;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.CLIHandler;
import org.netbeans.junit.NbTestCase;


/** Make sure the CLIHandler can be in modules and really work.
 * @author Jaroslav Tulach
 */
public class CLILookupHelpTest extends NbTestCase {
    File home, cluster2, user;
    
    public CLILookupHelpTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        
        home = new File(getWorkDir(), "cluster1");
        cluster2 = new File(getWorkDir(), "cluster2");
        user = new File(getWorkDir(), "testuserdir");
        
        home.mkdirs();
        cluster2.mkdirs();
        user.mkdirs();
        
        System.setProperty("netbeans.home", home.toString());
        System.setProperty("netbeans.dirs", cluster2.toString());
        
        System.setSecurityManager(new NoExit());
    }
    

    protected void tearDown() throws Exception {
        NoExit.disable = true;
    }
    
    public void testModuleInAClusterCanBeFound() throws Exception {
        createJAR(home, "test-module-one", One.class);
        createJAR(cluster2, "test-module-two", Two.class);
        createJAR(user, "test-module-user", User.class);

        try {
            org.netbeans.Main.main(new String[] { "--help", "--userdir", user.toString() });
            fail("At the end this shall throw security exception");
        } catch (SecurityException ex) {
            assertEquals("Exit code shall be two", "2", ex.getMessage());
        }
        
        assertEquals("Usage one", 1, One.usageCnt); assertEquals("CLI", 0, One.cliCnt);
        assertEquals("Usage two", 1, Two.usageCnt); assertEquals("CLI", 0, Two.cliCnt);
        assertEquals("Usage user", 1, User.usageCnt); assertEquals("CLI", 0, User.cliCnt);
    }

    static void createJAR(File cluster, String moduleName, Class metaInfHandler) 
    throws IOException {
        File xml = new File(new File(new File(cluster, "config"), "Modules"), moduleName + ".xml");
        File jar = new File(new File(cluster, "modules"), moduleName + ".jar");
        
        xml.getParentFile().mkdirs();
        jar.getParentFile().mkdirs();
        
        
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().putValue("OpenIDE-Module", moduleName.replace('-', '.'));
        mf.getMainAttributes().putValue("OpenIDE-Module-Public-Packages", "-");
        
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jar), mf);
        os.putNextEntry(new JarEntry("META-INF/services/org.netbeans.CLIHandler"));
        os.write(metaInfHandler.getName().getBytes());
        os.close();
        
        FileWriter w = new FileWriter(xml);
        w.write(            
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
"                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
"<module name=\"" + moduleName.replace('-', '.') + "\">\n" +
"    <param name=\"autoload\">false</param>\n" +
"    <param name=\"eager\">false</param>\n" +
"    <param name=\"enabled\">true</param>\n" +
"    <param name=\"jar\">modules/" + moduleName + ".jar</param>\n" +
"    <param name=\"release\">2</param>\n" +
"    <param name=\"reloadable\">false</param>\n" +
"    <param name=\"specversion\">3.4.0.1</param>\n" +
"</module>");
        w.close();
    }

    
    public static final class One extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public One() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
    public static final class Two extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public Two() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
    public static final class User extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public User() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
    
    
    private static final class NoExit extends SecurityManager {
        public static boolean disable;
        
        public void checkExit(int status) {
            if (!disable) {
                throw new SecurityException(String.valueOf(status));
            }
        }

        public void checkPermission(Permission perm) {
            
        }

        public void checkPermission(Permission perm, Object context) {
            
        }
        
    }
}
