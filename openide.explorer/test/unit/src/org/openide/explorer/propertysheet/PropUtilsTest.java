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

package org.openide.explorer.propertysheet;

import junit.framework.TestCase;
import org.openide.nodes.Node;

/**
 * @author mkrauskopf
 */
public class PropUtilsTest extends TestCase {
    
    public PropUtilsTest(String testName) {
        super(testName);
    }
    
    public void testCreateHtmlTooltip() {
        System.out.println("testCreateHtmlTooltip");
        // slash-separated
        String expectedResult = "<html><b><u>TitleTest</u></b><br>/usr/share/" +
                "java/netbeans-cvs-current/openide/test/unit/src/org/<br>" +
                "openide/explorer/propertysheet/SomeTest.java</html>";
        String result = PropUtils.createHtmlTooltip("TitleTest",
                "/usr/share/java/netbeans-cvs-current/openide/test/unit/src" +
                "/org/openide/explorer/propertysheet/SomeTest.java");
        assertEquals("Unexpected result. "
                + "\n  Expected: " + expectedResult
                + "\n  Actual  : " + result,
                expectedResult, result);
        
        // comma-separated
        expectedResult = "<html><b><u>TitleTest</u></b><br>Overridden to " +
                "supply different tooltips depending on mouse position<br> " +
                "(name, value, custom editor button).  Will HTML-ize long " +
                "tooltips<br></html>";
        result = PropUtils.createHtmlTooltip("TitleTest", "Overridden to supply " +
                "different tooltips depending on mouse position (name, value, " +
                "custom editor button).  Will HTML-ize long tooltips");
        assertEquals("Unexpected result. " +
                "\n  Expected: " + expectedResult +
                "\n  Actual  : " + result,
                expectedResult, result);
    }
    
    /* Tests whether "Restore Default Value" enabling/disabling works well. */
    public void testRestoreDefaultValueBehaviour() {
        System.out.println("testRestoreDefaultValueBehaviour");
        
        Node.Property trueProp = new OldModulePropertyWithSDVReturningTrue();
        assertTrue("OldModuleProperty doesn't know about Node.Property.isDefaultValue()" +
                " therefore it should be enabled in every case.",
                PropUtils.shallBeRDVEnabled(trueProp));
        
        Node.Property falseProp = new PropertyWithSDVReturningFalse();
        assertFalse("Property doesn't support default value. It should be " +
                "disabled", PropUtils.shallBeRDVEnabled(falseProp));
        
        Node.Property newIDVFalseProp = new BothMethodsOverridedPropertyWithIDSReturningFalse();
        assertTrue("Correctly implemented property with isDefaultValue() " +
                "returning false should be enable.",
                PropUtils.shallBeRDVEnabled(newIDVFalseProp));
        
        Node.Property newIDVTrueProp = new BothMethodsOverridedPropertyWithIDSReturningTrue();
        assertFalse("Correctly implemented property with isDefaultValue() " +
                "returning true should be disabled.",
                PropUtils.shallBeRDVEnabled(newIDVTrueProp));
        
        Node.Property noneOverrided = new DefaultTestProperty();
        assertFalse("Correctly implemented property which doesn't override any " +
                "of the two method should be disabled",
                PropUtils.shallBeRDVEnabled(noneOverrided));
    }
    
    /**
     * Simulates property for old modules which didn't know about
     * isDefaultValue() method but could overrode restoreDefaultValue().
     */
    private static final class OldModulePropertyWithSDVReturningTrue extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
    }
    
    private static final class PropertyWithSDVReturningFalse extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return false;
        }
    }
    
    /**
     * Simulates correctly implemented property which override both methods.
     */
    private static final class BothMethodsOverridedPropertyWithIDSReturningFalse
            extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
        public boolean isDefaultValue() {
            return false;
        }
    }
    
    private static final class BothMethodsOverridedPropertyWithIDSReturningTrue
            extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
        public boolean isDefaultValue() {
            return true;
        }
    }
    
    /**
     * Simulates correctly implemented property which doesn't override any of
     * the methods (supportsDefaultValue(), isDefaultValue()).
     */
    private static class DefaultTestProperty extends Node.Property {
        /** We don't need any of these method (or constructor) for our testing. */
        public DefaultTestProperty() { super(Object.class); }
        public void setValue(Object val) {}
        public Object getValue() { return null; }
        public boolean canWrite() { return false; }
        public boolean canRead() { return false; }
    }
}
