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
 *
 * SimpleExtBrowserTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */

package org.netbeans.modules.extbrowser;

import junit.framework.*;
import org.netbeans.junit.*;
import java.beans.*;
import org.openide.execution.NbProcessDescriptor;
         
/**
 *
 * @author rk109395
 */
public class SimpleExtBrowserTest extends NbTestCase {

    public SimpleExtBrowserTest (java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test of getName method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testGetName () {
        if (testObject.getName () == null)
            fail ("SimpleExtBrowser.getName () returns <null>.");
    }
    
    /** Test of setNamemethod, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testSetName () {
        testObject.setName ("Dummy");
    }
    
    /** Test of createHtmlBrowserImpl method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testCreateHtmlBrowserImpl () {
        if (testObject.createHtmlBrowserImpl () == null)
            fail ("SimpleExtBrowser.createHtmlBrowserImpl () returns <null>.");
    }
    
    /** Test of getBrowserExecutable method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testGetBrowserExecutable () {
        if (testObject.getBrowserExecutable () == null)
            fail ("SimpleExtBrowser.getBrowserExecutable () returns <null>.");
    }
    
    /** Test of setBrowserExecutable method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testSetBrowserExecutable () {
        testObject.setBrowserExecutable (new NbProcessDescriptor ("netscape", ""));
    }
    
    /** Test of addPropertyChangeListener method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testAddPropertyChangeListener () {
        testObject.addPropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {}
        });
    }
    
    /** Test of removePropertyChangeListener method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testRemovePropertyChangeListener () {
        testObject.removePropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {}
        });
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (SimpleExtBrowserTest.class);
        
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}
    protected SimpleExtBrowser testObject;
    
    protected void setUp () {
        testObject = new SimpleExtBrowser ();
    }

}
