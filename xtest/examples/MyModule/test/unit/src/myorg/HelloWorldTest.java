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

package myorg;

import java.io.*;
import java.net.URL;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;

public class HelloWorldTest extends NbTestCase {
    
    public HelloWorldTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(HelloWorldTest.class);
        
        return suite;
    }
    
    /** Test of greeting method, of class HelloWorld. */
    public void testGreeting1() {
        String greeting;
        
        System.out.println("testGreeting1");
        greeting = testObject.greeting();
        assertTrue(null != greeting);
    }

    

    public void testGreeting2() throws IOException {
        File test;
        File pass;
        FileWriter wr;
        String greeting;
        System.out.println("testGreeting2");
        greeting = testObject.greeting();
        test = new File(dataDir, "greeting.test");
        pass = new File(dataDir, "greeting.pass");
        wr = new FileWriter(test);
        wr.write(greeting);
        wr.close();
        
        assertFile("This failure is for demonstration purpose only.", test, pass, dataDir);
    }
    
    protected HelloWorld testObject;
    protected File dataDir;
    
    protected void setUp() {
        String packageName = "";
        if (null != getClass().getPackage()) {
            packageName = getClass().getPackage().getName();
        }
        dataDir = getDataDir();
        
        testObject = new HelloWorld();
    }
}
