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

package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

// This test class tests the main functionality of the property sheet
public class PropertySheetTest extends NbTestCase {
    public PropertySheetTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    private static boolean setup = false;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        if (setup) return;
        setup = true;
        // Create new TestProperty
        tp = new TProperty("TProperty", true);
        // Create new TEditor
        te = new TEditor();
        // Create new TNode
        tn = new TNode();
        
        System.err.println("RUNNING ON THREAD " + Thread.currentThread());
        
        //Replacing NodeOp w/ JFrame to eliminate depending on full IDE init
        //and long delay while waiting for property sheet thus requested to
        //initialize
        final JFrame jf = new JFrame();
        final PropertySheet ps = new PropertySheet();
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(ps, BorderLayout.CENTER);
        jf.setLocation(30,30);
        jf.setSize(500,500);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ps.setNodes(new Node[] {tn});
                //ps.setCurrentNode(tn);
                jf.show();
            }
        });
        
        //      ps.setNodes(new Node[] {tn});
        
        jf.show();
        new ExtTestCase.WaitWindow(jf);
        
        
        try {
            //            Thread.currentThread().sleep(500);
            
        } catch (Exception e) {
            
        }
        
        System.err.println("Current node set ");
        try {
            
            // Wait for the initialization
            for (int i = 0; i < 10; i++) {
                if (te.getAsText().equals("null")) {
                    System.err.println("Checking editor getAsText - " + te.getAsText());
                    //System.out.println("null");
                    Thread.sleep(1000);
                } else break;
            }
            // Test if the initialization was sucessfull
            
            initEditorValue = te.getAsText();
            System.err.println("Got initial editor value " + initEditorValue);
            
            initPropertyValue = tp.getValue().toString();
            System.err.println("Got initial property value " + initPropertyValue);
            
            
            //Set new value to the Property
            tp.setValue("Test2");
            postChangePropertyValue = tp.getValue().toString();
            
            System.err.println("Post change property value is " + postChangePropertyValue);
            
            
            // Wait for the reinitialization
            for (int i = 0; i < 100; i++) {
                if (te.getAsText().equals(initEditorValue)) {
                    //System.err.println(i + " value not updated ");;
                    Thread.sleep(50);
                } else {
                    System.err.println("value was updated");
                    break;
                }
            }
            
            //issues 39205 & 39206 - ensure the property sheet really repaints
            //before we get the value, or the value in the editor will not
            //have changed
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Graphics g = ps.getGraphics();
                    ps.paintImmediately(0,0,ps.getWidth(), ps.getHeight());
                }
            });
            
            // Test if the reinitialization was sucessfull
            postChangeEditorValue = te.getAsText();
            System.err.println("postEditorChangeValue = " + postChangeEditorValue);
            
        } catch (Exception e) {
            fail("FAILED - Exception thrown "+e.getClass().toString());
        } finally {
            jf.hide();
            jf.dispose();
        }
    }
    
    public void testInitializeEditorValue() throws Exception {
        assertTrue("Editor wasn't initialized successfuly (null) - value was " + initEditorValue,!initEditorValue.equals("null"));
    }
    
    public void testPropertyEQEditorValueAfterInit() throws Exception {
        assertEquals("Editor was initialized to the same value as the Property, value was " + initPropertyValue, initPropertyValue, initEditorValue);
    }
    
    public void testSetPropertyValue() throws Exception {
        assertTrue("Property value wasn't successfuly changed. Initial property value, " + initPropertyValue + " should not match " + postChangePropertyValue,!initPropertyValue.equals(postChangePropertyValue));
    }
    
    public void testSetEditorValue() throws Exception {
        assertTrue("Editor value wasn't changed successfuly. Initial editor value, " + initEditorValue + " should not match " + postChangeEditorValue,!initEditorValue.equals(postChangeEditorValue));
    }
    
    public void testPropertyEQEditorValueAfterChange() throws Exception {
        assertEquals("Editor value doesn't reflect the Property value. Post change property value, " + postChangePropertyValue + " should equal " + postChangeEditorValue, postChangePropertyValue, postChangeEditorValue);
    }
    
    
    //Node definition
    public class TNode extends AbstractNode {
        //create Node
        public TNode() {
            super(Children.LEAF);
            setName("TNode"); // or, super.setName if needed
            setDisplayName("TNode");
        }
        //clone existing Node
        public Node cloneNode() {
            return new TNode();
        }
        
        // Create a property sheet:
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            // Make sure there is a "Properties" set:
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }
            props.put(tp);
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            System.err.println("TNode firing change " + s + " from " + o1 + " to " + o2);
            firePropertyChange(s,o1,o2);
        }
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public TProperty(String name, boolean isWriteable) {
            super(name, Object.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        
        
        
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            System.err.println("TProperty setValue: " + value);
            Object oldVal = myValue;
            myValue = value;
            System.err.println("TProperty triggering node property change");
            tn.fireMethod(getName(), oldVal, myValue);
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return te;
        }
    }
    
    // Editor definition
    public class TEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        // Create new TEditor
        public TEditor() {
        }
        
        /*
         * This method is called by the IDE to pass
         * the environment to the property editor.
         */
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        // Set that this Editor doesn't support custom Editor
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            System.err.println("Property change listener added to property editor " + System.identityHashCode(this) + " - " + l);
            super.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            System.err.println("Property change listener removed from property editor " + System.identityHashCode(this) + " - " + l);
            super.removePropertyChangeListener(l);
        }
        
        
        
        // Set the Property value threw the Editor
        public void setValue(Object newValue) {
            System.err.println("TEditor.setValue: " + newValue);
            super.setValue(newValue);
        }
        
        public void firePropertyChange() {
            System.err.println("TEditor.firePropertyChange");
            super.firePropertyChange();
        }
    }
    
    private static TNode tn;
    private static TProperty tp;
    private static TEditor te;
    private static String initEditorValue;
    private static String initPropertyValue;
    private static String postChangePropertyValue;
    private static String postChangeEditorValue;
}
