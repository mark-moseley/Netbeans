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

package guitest;

import org.netbeans.junit.NbTestSuite;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;


public class GoldenGuiTest extends JellyTestCase {
    
    public GoldenGuiTest(String testName) {
        super(testName);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(GoldenGuiTest.class));
    }
    
    public void setUp() {
        /*
         * No special setup needed. JellyTestCase do following initialization:
         *  - Jemmy/jelly output is redirected to jemmy.log file in JUnit working directory
         *  - if an exception is thrown during test execution, screen shot is taken
         *  - all modal dialogs are closed
         *  - wait at least 1000 ms between test cases
         *  - dump xml hierarchy of all components (disabled by default)
         */
    }
    
    /* Simple gui test using golden files. It checks properties of HTTP Server node
     * in the runtime window.
     * If everything works well, contents of golden file is compared at the end
     * with output collected by ref PrintStream into a file. If files differ, test
     * is failed, otherwise it is passed.
     * If something goes wrong, exception is thrown from Jemmy, caught and reported.
     * You can also use assert*() and fail() methods to indicate a failure within the test.
     */
    public void testPart1() throws Exception {
        Node rootNode = RuntimeTabOperator.invoke().getRootNode();
        Node httpNode = new Node(rootNode, "HTTP Server");
        new PropertiesAction().perform(httpNode);
        PropertySheetOperator pso = new PropertySheetOperator("HTTP Server");
        Property p = new Property(pso, "Hosts With Granted Access");
        String value = p.getValue();
        pso.close();
        // write to ref PrintStream to compare with golden file
        ref(value);
        // compare outpup written to ref PrintStream with contents of golden file
        // (data/goldenfiles/guitest/GoldenGuiTest/testPart1.pass)
        compareReferenceFiles();
    }
    
}
