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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import org.openide.explorer.propertysheet.ExtTestCase.WaitWindow;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

// This test class tests the main functionality of the property sheet
public class TagsAndEditorsTest extends ExtTestCase {
    private PropertySheet ps = null;
    JFrame jf = null;
    
    public TagsAndEditorsTest(String name) {
        super(name);
    }
    
    static {
        installCorePropertyEditors();
    }
    
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        PropUtils.forceRadioButtons = false;
        jf = new JFrame();
        // Create new TestProperty
        try {
            PropUtils.forceRadioButtons=false;
            ps = new PropertySheet();
            //ensure no stored value in preferences:
            sleep();
            
            setSortingMode(ps, PropertySheet.UNSORTED);
            
            jf.getContentPane().add(ps);
            jf.setLocation(20,20);
            jf.setSize(300, 400);
            
            new WaitWindow(jf);
        } catch (Exception e) {
            e.printStackTrace();
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
    }
    
    static {
        System.err.println("CLASSPATH: " + System.getProperty("java.class.path"));
        System.setProperty("org.openide.explorer.propertysheet.ComboInplaceEditor", "1");
    }
    public void testEditableEmptyTagEditor() throws Exception {
        if (!canSafelyRunFocusTests()) {
            return;
        }
        Node n = new TNode(new EditableEmptyTagsEditor());
        setCurrentNode(n,ps);
        requestFocus(ps);
        clickCell(ps.table, 1, 1);
        Component c = focusComp();
        assertTrue("Clicking on an editable property that returns a 1 element " +
                "array from getTags() should send focus to a combo box's child editor component not " + focusComp(),
                c.getParent() instanceof JComboBox);
    }
    
    public void testSingleTagEditor() throws Exception {
        if (!canSafelyRunFocusTests()) {
            return;
        }
        Node n = new TNode(new SingleTagEditor());
        setCurrentNode(n, ps);
        clickCell(ps.table, 1, 1);
        assertTrue("Clicking on an editable property that returns a 1 element " +
                "array from getTags() should send focus to a combo boxnot a " + focusComp(),
                focusComp() instanceof JComboBox);
    }
    
    public void testEditableSingleTagEditor() throws Exception {
        if (!canSafelyRunFocusTests()) {
            return;
        }
        Node n = new TNode(new EditableSingleTagEditor());
        setCurrentNode(n, ps);
        clickCell(ps.table, 1, 1);
        Component c = focusComp();
        assertTrue("Clicking on an editable property that returns a 1 element " +
                "array from getTags() should send focus to a combo box's child editor component",
                c.getParent() instanceof JComboBox);
    }
    
    public void testEmptyTagEditor() throws Exception {
        if (!canSafelyRunFocusTests()) {
            return;
        }
        requestFocus(ps);
        Node n = new TNode(new EmptyTagsEditor());
        setCurrentNode(n, ps);
        clickCell(ps.table, 1, 1);
        assertTrue("Clicking on an editable property that returns a 0 " +
                "length array from getTags() should send focus to a combo box",
                focusComp() instanceof JComboBox);
    }
    
    public void testPropertyMarkingAlignment() throws Exception {
        if (!canSafelyRunFocusTests()) {
            return;
        }
        Node n = new TNode(new PropertyEditor[]{
            new BadEditorWithTags(),
            new BadEditorWithoutTags()
        });
        setCurrentNode(n, ps);
    }
    
    public void testPropertySheetRepaintsCellOnPropertyChange() throws Exception {
        if (!canSafelyRunFocusTests()) {
            return;
        }
        Node n = new TNode(new SingleTagEditor());
        setCurrentNode(n, ps);
        Rectangle test = ps.table.getCellRect(1, 1, true);
        RM rm = new RM(test, ps.table);
        RepaintManager.setCurrentManager(rm);
        sleep();
        sleep();
        Node.Property prop = n.getPropertySets()[0].getProperties()[0];
        prop.setValue("new value");
        Thread.currentThread().sleep(1000);
        sleep();
        rm.assertRectRepainted();
    }
    
    private class RM extends RepaintManager {
        private Rectangle rect;
        private Rectangle repaintedRect = null;
        private JComponent repaintedComponent = null;
        private JComponent target = null;
        public RM(Rectangle rect, JComponent target) {
            this.rect = rect;
            this.target = target;
        }
        
        public void assertRectRepainted() {
            assertNotNull("No component repainted", repaintedComponent);
            assertSame("Wrong component repainted:" + repaintedComponent, repaintedComponent, target);
            assertEquals("Wrong rectangle repainted:" + repaintedRect + " should be " + rect, rect, repaintedRect);
        }
        
        public synchronized void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
            super.addDirtyRegion(c, x, y, w, h);
            if (repaintedComponent == null) {
                repaintedComponent = c;
                repaintedRect = new Rectangle(x, y, w, h);
            }
        }
    }
    
    private void clickCell(final SheetTable tb, final int row, final int col) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Rectangle r = tb.getCellRect(row, col, false);
                Point toClick = r.getLocation();
                toClick.x += 15;
                toClick.y +=3;
                MouseEvent me = new MouseEvent(tb, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                tb.dispatchEvent(me);
            }
        });
        sleep();
    }
    
    
    //Node definition
    public class TNode extends AbstractNode {
        private PropertyEditor[] ed;
        //create Node
        public TNode(PropertyEditor[] ed) {
            super(Children.LEAF);
            setName("TNode"); // or, super.setName if needed
            setDisplayName("TNode");
            this.ed = ed;
        }
        
        public TNode(PropertyEditor ed) {
            this(new PropertyEditor[] {ed});
        }
        
        //clone existing Node
        public Node cloneNode() {
            return new TNode(ed);
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
            for (int i=0; i < ed.length; i++) {
                props.put(new TProperty(this, ed[i], true));
            }
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    private String stripClassName(Object o) {
        String s = o.getClass().getName();
        int idx = s.indexOf('$');
        return s.substring(idx+1);
    }
    
    private static int ct = 0;
    // Property definition
    public class TProperty extends PropertySupport {
        private Object myValue = "Value";
        private PropertyEditor ed;
        private Node n;
        
        public TProperty(Node n, PropertyEditor ed, boolean writable) {
            super(stripClassName(ed) + "-" + (ct++), String.class, stripClassName(ed) + ct, "", true, writable);
            this.ed = ed;
            this.n = n;
        }
        
        public Object getValue() {
            return myValue;
        }
        
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value;
            ((TNode)n).fireMethod(getName(), oldVal, myValue);
        }
        
        public PropertyEditor getPropertyEditor() {
            return ed;
        }
    }
    
    
    private static Exception throwMe=null;
    private synchronized void setSortingMode(final PropertySheet ps, final int mode) throws Exception {
        throwMe = null;
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    ps.setSortingMode(mode);
                } catch (Exception e) {
                    throwMe = e;
                }
            }
        });
        if (throwMe != null) {
            Exception ex = throwMe;
            throwMe = null;
            throw (throwMe);
        }
    }
    
    public static class SingleTagEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public SingleTagEditor() {
        }
        
        public String[] getTags() {
            return new String[]{"lonely tag"};
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
    }
    
    public static class EditableSingleTagEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public EditableSingleTagEditor() {
        }
        
        public String[] getTags() {
            return new String[]{"lonely tag"};
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE);
        }
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
    }
    
    public static class EmptyTagsEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public EmptyTagsEditor() {
        }
        
        public String[] getTags() {
            return new String[0];
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
    }
    
    public static class EditableEmptyTagsEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public EditableEmptyTagsEditor() {
        }
        
        public String[] getTags() {
            return new String[0];
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE);
        }
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
    }
    
    
    public static class BadEditorWithoutTags extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public BadEditorWithoutTags() {
        }
        
        public String[] getTags() {
            return null;
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            env.setState(env.STATE_INVALID);
        }
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
        
        public Object getValue() {
            return Boolean.FALSE;
        }
    }
    
    public static class BadEditorWithTags extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public BadEditorWithTags() {
        }
        
        public String[] getTags() {
            return new String[] {"a","b","c","d","Value"};
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            env.setState(env.STATE_INVALID);
        }
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
        
        public Object getValue() {
            return Boolean.FALSE;
        }
    }
}
