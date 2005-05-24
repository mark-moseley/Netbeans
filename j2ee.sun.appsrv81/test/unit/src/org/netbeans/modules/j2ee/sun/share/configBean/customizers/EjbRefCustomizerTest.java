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
/*
 * EjbRefCustomizerTest.java
 * JUnit based test
 *
 * Created on March 15, 2004, 6:05 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.ResourceBundle;
import java.beans.Customizer;
import java.beans.PropertyVetoException;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.EjbRef;
import org.netbeans.modules.j2ee.sun.share.configbean.ErrorMessageDB;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerErrorPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class EjbRefCustomizerTest extends TestCase {
    
    public void testCreate() {
        EjbRefCustomizer foo = new EjbRefCustomizer();
        
    }
    
    public EjbRefCustomizerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EjbRefCustomizerTest.class);
        return suite;
    }
        
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
