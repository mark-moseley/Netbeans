/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
*/

package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.awt.FlowLayout;
import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;

import org.openide.*;
import org.openide.explorer.propertysheet.*;

import junit.framework.*;
import junit.textui.TestRunner;

import org.netbeans.junit.*;
import java.beans.PropertyDescriptor;
import java.awt.IllegalComponentStateException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;

/** A test of a property panel.
 */
public final class PropertyPanelTest extends NbTestCase {
    static {
        //Added with property panel rewrite - the property model given the
        //variable name "replace" is a String property, and the default
        //property editor for the JDK does not support a custom editor.
        //PropertyPanel will no longer allow itself to be put in custom editor
        //mode if the property does not support a custom editor.
        org.netbeans.core.NonGui.registerPropertyEditors();
    }
    
    
    
    public PropertyPanelTest(String name) {
        super(name);
    }
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new NbTestSuite (PropertyPanelTest.class));
    }
    
    //
    // Sample property impl
    //
    
    private String prop;
    
    public void setProp (String x) {
        prop = x;
    }
    
    public String getProp () {
        return prop;
    }
   
    JFrame jf;
    protected void setUp() throws Exception {
        PropUtils.forceRadioButtons=false;
        jf = new JFrame();
        jf.getContentPane().setLayout(new FlowLayout());
        jf.setSize (400,400);
        jf.setLocation (30,30);
        jf.show();
    }


    public void testStateUpdates () throws Exception {
        PropertyDescriptor feature = new PropertyDescriptor ("prop", this.getClass ());
        feature.setPropertyEditorClass (Ed.class);
        DefaultPropertyModel model = new DefaultPropertyModel (
            this, feature
        );
        
        final PropertyPanel pp = new PropertyPanel (model, PropertyPanel.PREF_CUSTOM_EDITOR);
        
        //The property panel must be displayed - it will not attempt to communicate
        //with the property editor until it is on screen
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jf.getContentPane().add(pp);
                System.err.println("  Aded to panel");
                jf.validate();
                jf.repaint();
                System.err.println(" bounds: " + pp.getBounds());
            }
        });
        Thread.currentThread().sleep(1000);
        
        assertTrue ("Ed editor created", pp.getPropertyEditor() instanceof Ed);
        
        Ed ed = (Ed)pp.getPropertyEditor ();
        
        assertNotNull("PropertyPanel returns the right property editor", ed);
        
        assertNotNull("Environment has not been attached", ed.env);
        
        Listener envListener = new Listener ();
        Listener panelListener = new Listener ();
        
        pp.addPropertyChangeListener(panelListener);
        ed.env.addPropertyChangeListener (envListener);
        ed.env.addVetoableChangeListener (envListener);
        
        ed.env.setState (PropertyEnv.STATE_INVALID);
        
        assertEquals ("State of panel is invalid", PropertyEnv.STATE_INVALID, pp.getState ());
        envListener.assertChanges ("Notified in environment", 1, 1);
        panelListener.assertChanges ("Notified in panel", 1, 0);
        
        ed.env.setState (PropertyEnv.STATE_INVALID);
        assertEquals ("Remains invalid", PropertyEnv.STATE_INVALID, pp.getState ());
        envListener.assertChanges ("No changes notified", 0, 0);
        panelListener.assertChanges ("No changes notified in panel", 0, 0);
        
        pp.updateValue();
        
        assertEquals ("Update valud does not change the state if invalid", PropertyEnv.STATE_INVALID, pp.getState ());
        envListener.assertChanges ("Changes notified in env", 0, 0);
        panelListener.assertChanges ("Notified in panel", 0, 0);
        
        ed.env.setState (PropertyEnv.STATE_NEEDS_VALIDATION);
        assertEquals ("Now we need validation", PropertyEnv.STATE_NEEDS_VALIDATION, pp.getState ());
        envListener.assertChanges ("Notified in environment", 1, 1);
        panelListener.assertChanges ("Notified in panel", 1, 0);

        pp.updateValue ();
        assertEquals ("Update from needs validation shall switch to valid state if not vetoed", PropertyEnv.STATE_VALID, pp.getState ());
        envListener.assertChanges ("Notified in environment", 1, 1);
        panelListener.assertChanges ("Notified in panel", 1, 0);
        
        ed.env.setState (PropertyEnv.STATE_NEEDS_VALIDATION);
        assertEquals ("Now we need validation", PropertyEnv.STATE_NEEDS_VALIDATION, pp.getState ());
        envListener.assertChanges ("Notified in environment", 1, 1);
        panelListener.assertChanges ("Notified in panel", 1, 0);
        
        
        envListener.shallVeto = true;
        pp.updateValue ();
        assertTrue ("Was vetoed", !envListener.shallVeto);
        
        assertEquals ("The state remains", PropertyEnv.STATE_NEEDS_VALIDATION, pp.getState ());
        envListener.assertChanges ("No approved property changes", 0, -1);
        panelListener.assertChanges ("No approved property changes", 0, -1);
        
        
        //
        // Now try to do the cleanup
        //
        
        DefaultPropertyModel replace = new DefaultPropertyModel (this, "prop");
        pp.setModel (replace);
        
        assertEquals ("Model changed", replace, pp.getModel());
        
        
        WeakReference wEd = new WeakReference (ed);
        WeakReference wEnv = new WeakReference (ed.env);
        
        ed = null;
        
        assertGC ("Property editor should disappear", wEd);
        assertGC ("Environment should disapper", wEnv);
    }
    
    private void addToPanel(final PropertyPanel pp) throws Exception {
        //The property panel must be displayed - it will not attempt to communicate
        //with the property editor until it is on screen
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jf.getContentPane().add(pp);
                jf.validate();
                jf.repaint();
            }
        });
        Thread.currentThread().sleep(500);
    }
    
    private void removeFromPanel(final PropertyPanel pp) throws Exception {
        //The property panel must be displayed - it will not attempt to communicate
        //with the property editor until it is on screen
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jf.getContentPane().remove(pp);
                jf.validate();
                jf.repaint();
            }
        });
        Thread.currentThread().sleep(500);
    }    

    public void testPropertyPanelShallGCEvenIfEditorExists () throws Exception {
        PropertyDescriptor feature = new PropertyDescriptor ("prop", this.getClass ());
        feature.setPropertyEditorClass (Ed.class);
        DefaultPropertyModel model = new DefaultPropertyModel (
            this, feature
        );
        
        PropertyPanel pp = new PropertyPanel (model, PropertyPanel.PREF_CUSTOM_EDITOR);
        addToPanel(pp);
        
        assertTrue ("Ed editor created", pp.getPropertyEditor() instanceof Ed);
        
        Ed ed = (Ed)pp.getPropertyEditor ();
        assertNotNull ("Environment has been attached", ed.env);
        
        //
        // Make sure that the panel listens on changes in env
        //
        Listener panelListener = new Listener ();
        
        pp.addPropertyChangeListener (panelListener);
        ed.env.setState (PropertyEnv.STATE_INVALID);
        panelListener.assertChanges ("Change notified in panel", 1, 0);
        
        removeFromPanel(pp);
        pp.removePropertyChangeListener(panelListener);
        
        WeakReference weak = new WeakReference (pp);
        pp = null;
        model = null;
        feature = null;
        
        assertGC ("Panel should disappear even if we have reference to property editor", weak);
    }
    
    public void testCompatibilityWhenUsingNodePropertyAndAskingForPropertyModel () throws Exception {
        final Ed editor = new Ed ();
        
        class NP extends org.openide.nodes.Node.Property {
            private Object value;
            
            public NP () {
                super (Runnable.class);
            }
            
            public Object getValue () {
                return value;
            }
            
            public void setValue (Object o) {
                this.value = o;
            }
            
            public boolean canWrite () {
                return true;
            }
            
            public boolean canRead () {
                return true;
            }
            
            public java.beans.PropertyEditor getPropertyEditor () {
                return editor;
            }
        }
        
        NP property = new NP ();
        PropertyPanel panel = new PropertyPanel (property);
        
        assertEquals ("The property is mine", property, panel.getProperty ());
        assertEquals ("Editor is delegated", editor, panel.getPropertyEditor());
        assertNotNull ("There is a model", panel.getModel ());
        assertEquals ("Type is delegated", Runnable.class, panel.getModel ().getPropertyType());
        
        Listener listener = new Listener();
        PropertyModel model = panel.getModel ();
        model.addPropertyChangeListener(listener);
        panel.getProperty ().setValue (this);
        
        assertEquals("Value changed in model", this, model.getValue());
        assertEquals("Value changed in prop", this, panel.getProperty().getValue());
    }

    public void testCompatibilityWhenUsingPropertyModelAndAskingForNodeProperty () throws Exception {
        class PM implements PropertyModel {
            private Object value;
            private PropertyChangeListener listener;
            
            public PM() {
            }
            
            public void addPropertyChangeListener (PropertyChangeListener l) {
                assertNull ("Support for only one listener is here now", listener);
                listener = l;
            }
            
            public void removePropertyChangeListener (PropertyChangeListener l) {
                assertEquals ("Removing the one added", listener, l);
                listener = null;
            }
            
            public Class getPropertyType () {
                return Runnable.class;
            }
            
            public Object getValue() {
                return value;
            }
            
            public void setValue(Object o) {
                Object old = value;
                
                this.value = o;
                if (listener != null) {
                    listener.propertyChange (new PropertyChangeEvent (this, "value", old, o));
                }
            }
            /*
            public boolean canWrite() {
                return true;
            }
            
            public boolean canRead() {
                return true;
            }
             */
            
            public Class getPropertyEditorClass () {
                return Ed.class;
            }
        }
        
        PM model = new PM ();
        PropertyPanel panel = new PropertyPanel(model, 0);
        
        assertEquals("The model is mine", model, panel.getModel());
        assertEquals("Editor is delegated", Ed.class, panel.getPropertyEditor().getClass ());
        assertNotNull("There is a property", panel.getProperty());
        assertEquals("Type is delegated", Runnable.class, panel.getProperty ().getValueType());
        
        panel.getProperty ().setValue (this);
        assertEquals ("Value changed in model", this, model.getValue ());
        assertEquals ("Value changed in prop", this, panel.getProperty ().getValue ());

        
        model.setValue (model);
        assertEquals("Value change propagated into prop", model, panel.getProperty().getValue());
    }
    
    public void testPropertyPanelPropagatesChangesEvenWhenItDoesntExist() throws Exception {
        class PM implements PropertyModel {
            private Object value;
            private PropertyChangeListener listener=null;
            private PropertyChangeListener listener2=null;
            
            public PM() {
            }
            
            public void addPropertyChangeListener(PropertyChangeListener l) {
                if (listener != null) {
                    listener2 = l;
                } else {
                    listener = l;
                }
            }
            
            public void removePropertyChangeListener(PropertyChangeListener l) {
                if (l == listener) {
                    listener = null;
                    return;
                }
                if (l == listener2) {
                    listener2 = null;
                    return;
                }
                fail("Tried to remove a listener that was never attached: " + l);
            }
            
            public Class getPropertyType() {
                return Runnable.class;
            }
            
            public Object getValue() {
                return value;
            }
            
            public void setValue(Object o) {
                Object old = value;
                this.value = o;
                if (listener != null) {
                    listener.propertyChange(new PropertyChangeEvent(this, "value", old, o));
                }
                if (listener2 != null) {
                    listener2.propertyChange(new PropertyChangeEvent(this, "value", old, o));
                }
                assertTrue ("Some listener should still be listenening", listener != null || listener2 != null);
            }
            
            public void assertValueChangedTo(Object o) throws Exception {
                assertSame("Value should have been updated even though property panel doesn't exist", value, o);
            }
            
            public Class getPropertyEditorClass() {
                return Ed.class;
            }
        }
        
        PM model = new PM();
        PropertyPanel pp = new PropertyPanel(model, PropertyPanel.PREF_CUSTOM_EDITOR);
        
        addToPanel(pp);
        
        assertTrue("Ed editor created", pp.getPropertyEditor() instanceof Ed);
        
        Ed ed = (Ed)pp.getPropertyEditor();
        
        removeFromPanel(pp);
        
        WeakReference weak = new WeakReference(pp);
        pp = null;
        
        Runnable toTest = new Runnable() {
            public void run() {
            }
        };
        
        ed.setValue(toTest);
        
        model.assertValueChangedTo(toTest);
        
    }
    
    
    /** Listener that counts changes.
     */
    private static final class Listener 
    implements PropertyChangeListener, VetoableChangeListener {
        public boolean shallVeto;
        
        private int veto;
        private int change;
        
        public void assertChanges (String t, int c, int v) {
            if (c != -1) {
                assertEquals (t + " [propertychange]", c, change);
            }
            
            if (v != -1) {
                assertEquals (t + " [vetochange]", v, veto);
            }
            
            change = 0;
            veto = 0;
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
            change++;
        }
        
        public void vetoableChange(java.beans.PropertyChangeEvent propertyChangeEvent) throws java.beans.PropertyVetoException {
            if (shallVeto) {
                shallVeto = false;
                PropertyVetoException e = new PropertyVetoException ("Veto", propertyChangeEvent);
                
                // marks this exception as one that we do not want to notify
                PropertyDialogManager.doNotNotify (e);
                throw e;
            }
            
            veto++;
        }
        
    }

    /** Sample property editor.
     */
    private static final class Ed extends java.beans.PropertyEditorSupport 
    implements ExPropertyEditor {
        public PropertyEnv env;
        
        public Ed () {
        }
        
        public void addPropertyChangeListener (PropertyChangeListener pcl) {
            super.addPropertyChangeListener(pcl);
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        //The two methods below are added because, in the property panel
        //rewrite, the property panel uses polling with a ReusablePropertyEnv
        //to determine valid state for editors that do not support a custom
        //editor - and the PropertyPanel cannot be initialized into custom
        //editor mode for a property editor that doesn't actually support
        //custom editors
        public boolean supportsCustomEditor() {
            return true;
        }
        
        //To avoid NPE when propertypanel tries to add the custom editor
        public Component getCustomEditor() {
            JPanel result = new JPanel();
            result.setBackground(java.awt.Color.ORANGE);
            return result;
        }
    }
    
}



