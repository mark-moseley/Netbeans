/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * PropertySheetTest.java
 *
 * Created on August 24, 2001, 4:25 PM
 */

package org.openide.explorer.propertysheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.explorer.propertysheet.*;
import org.openide.explorer.propertysheet.editors.*;
import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.ImageIcon;
import junit.framework.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/** Tests property marking functionality and the ability of a Property to
 * provide a "postSetAction" action hint, which will be run if the user
 * successfully changes the property value.
 */
public class ComboTest extends NbTestCase {
    private static boolean setup=false;
    
    static {
        registerPropertyEditors();
    }
    
    public static void registerPropertyEditors () {
        //org.netbeans.core.startup.Main.registerPropertyEditors();
    }
    
    public ComboTest(String name) {
        super(name);
    }
 
    static SheetTable tb=null;
    static JFrame jf=null;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        if (setup) return;

        try {

            tp = new TProperty("oh", true);
            tp1 = new TProperty2("the", true);
            postSetAction = new PostSetAction();
            
            tn = new TNode();
//            PropUtils.forceRadioButtons=true;
            final PropertySheet ps = new PropertySheet();
            
            //ensure no stored value in preferences:
            ps.setCurrentNode(tn);
            sleep();
            ps.setSortingMode(PropertySheet.UNSORTED);
            
            jf = new JFrame();
            jf.getContentPane().add (ps);
            jf.setLocation (20,20);
            jf.setSize (300, 400);
            new WaitWindow(jf);
            tb = ps.table;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                    ps.setSortingMode(ps.SORTED_BY_NAMES);
                    }catch (Exception e){}
                }
            });
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("FAILED - Exception thrown "+e.getClass().toString());
        } finally {
            setup = true;
        }
    }
    
    public void tearDown() {
        jf.hide();
        jf.dispose();
    }
    
    
    
    static boolean checkGraphicsEnvironment() {
        if (GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadless()) {
            System.err.println("Cannot run test in a headless environment");
        }
        DisplayMode dm = 
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        int i = dm.getBitDepth();
        if (i == dm.BIT_DEPTH_MULTI || i >= 16) {
            return true;
        }
        return false;
    }
    
    public void testFoo () throws Exception {
    }
    
     
    
    private static class WaitWindow extends WindowAdapter {
        boolean shown=false;
        public WaitWindow (JFrame f) {
            f.addWindowListener(this);
            f.show();
            if (!shown) {
                synchronized(this) {
                    try {
                            wait(5000);
                    } catch (Exception e) {}
                }
            }
        }
        
        public void windowOpened(WindowEvent e) {
            shown = true;
            synchronized(this) {
                notifyAll();
                ((JFrame) e.getSource()).removeWindowListener(this);
            }
        }
    }
    
    private static final int SLEEP_LENGTH=1000;
    private void sleep() {
         //useful when running interactively
        
        try {
//            Thread.currentThread().sleep(800);
            //jf.getTreeLock().wait();
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });
            //jf.getTreeLock().wait();
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });            
        } catch (Exception e) {
        }
        
        
    }
    
    
    private static Color checkColor=null;
    private Exception throwMe=null;
    
    private static int count=0;
    /** Asserts that a pixel at a given position in an image matches a 
     * pixel in a given position in a component */
    private synchronized void assertPixelFromImage(final Image i, final Component c, final int imageX, final int imageY, final int compX, final int compY) throws Exception {
        final BufferedImage bi = i instanceof BufferedImage ? (BufferedImage) i : toBufferedImage(i);
        throwMe = null;
        sleep();
        
        int rgb = bi.getRGB(imageX, imageY);
        Color color = new Color (rgb);
       
        
        //uncomment the code below for diagnosing painting problems
        //and seeing which pixel you'return really checking
        JFrame jf = new JFrame("assertPixelFromImage " + count + " (look for the yellow line)") {
            public void paint (Graphics g) {
                new ImageIcon (bi).paintIcon(this, g, 25, 25);
                g.setColor (Color.YELLOW);
                g.drawLine(imageX+20, imageY+25, imageX+25, imageY+25);
            }
        };
        jf.setLocation (500,500);
        jf.setSize (100,100);
        jf.show();
        
        try {
            assertPixel(c, color, compX, compY);
        } catch (Exception e) {
            throwMe = e;
        }
        if (throwMe != null) {
            throw throwMe;
        }
    }
    
    private Exception throwMe2=null;
    private synchronized void assertPixel(final Component c, final Color toMatch, final int x, final int y) throws Exception {
        sleep();
        throwMe2 = null;
        if (true) {
            doAssertPixel(c, toMatch, x, y);
            return;
        }
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    doAssertPixel(c, toMatch, x, y);
                } catch (Exception e) {
                    throwMe2 = e;
                }
            }
        });
        if (throwMe2 != null) {
            throw throwMe2;
        }
    }
    
    private synchronized void doAssertPixel(final Component c, final Color toMatch, final int x, final int y) throws Exception {
        final BufferedImage bi = new BufferedImage (700, 700, BufferedImage.TYPE_INT_RGB);

        sleep();
        ((JComponent) c).paintAll(bi.getGraphics());
        sleep();
        int[] cArr = new int[3];
        bi.getData().getPixel(x, y, cArr);
        checkColor = new Color (cArr[0], cArr[1], cArr[2]);

        
        //uncomment the code below for diagnosing painting problems
        //and seeing which pixel you'return really checking
        JFrame jf = new JFrame("Assert pixel test " + count + " (look for the yellow line)") {
            public void paint (Graphics g) {
                new ImageIcon (bi).paintIcon(this, g, 25, 25);
                g.setColor (Color.YELLOW);
                g.drawLine(x+20, y+25, x+25, y+25);
            }
        };
        jf.setLocation (400,400);
        jf.setSize (500,500);
        jf.show();
        count++;
        
        assertEquals("Color at " + x + "," + y + " does not match", toMatch, checkColor);
                
    }

    
    private void clickOn (final SheetTable tb, final int row, final int col) throws Exception {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                Rectangle r = tb.getCellRect(row, col, false);
                Point toClick = r.getLocation();
                toClick.x += 15;
                toClick.y +=3;
                MouseEvent me = new MouseEvent (tb, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                tb.dispatchEvent(me);
            }
        });
        sleep();
    }

    private void releaseKey (final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent (target, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, key, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    private void pressKey (final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent (target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
        
    private void typeKey (final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent (target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    //Node definition
    public class TNode extends AbstractNode {
        //create Node
        public TNode() {
            super (Children.LEAF);
            setName("TNode"); // or, super.setName if needed
            setDisplayName("TNode");
        }
        //clone existing Node
        public Node cloneNode() {
            return new TNode();
        }
        
        public void addProp (Node.Property p) {
            props.put(p);
            this.firePropertyChange(PROP_PROPERTY_SETS, null, null);
            this.firePropertySetsChange(null, null);
        }
        
        Sheet sheet=null;
        Sheet.Set props=null;
        // Create a property sheet:
        protected Sheet createSheet() {
            sheet = super.createSheet();
            // Make sure there is a "Properties" set:
            props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }
            props.put(tp);
            props.put(tp1);
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    public static void main(String args[]) {
        LookAndFeel lf = UIManager.getLookAndFeel();
        TestRunner.run(suite ());
    }
    
    public static Test suite() {
        return new ComboTestSuite ();
    }
    
    private static final class ComboTestSuite extends NbTestSuite {
        public ComboTestSuite () {
            super (ComboTest.class);
        }
        
        public void run (final TestResult tr) {
            super.run (tr);
        }
    }

    // Property definition
    public class TProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public TProperty(String name, boolean isWriteable) {
            super(name, String.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return new TEditor();
        }
        
        public Object getValue(String key) {
            if ("nameIcon".equals(key)) {
                return new NameIcon();
            } else if ("valueIcon".equals(key)) {
                return new ValueIcon();
            } else if ("postSetAction".equals(key)) {
                return postSetAction;
            }
            return super.getValue(key);
        }
    }
    
    // Editor definition
    public class TEditor extends PropertyEditorSupport {
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
        
        // Set the Property value threw the Editor
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
        
        public String getAsText() {
            return getValue() == null ? "null" : getValue().toString();
        }
    }
    

    public class TagsEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public TagsEditor() {
        }
        
        public String[] getTags() {
            return new String[] {"a","b","c","d","Value"};
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
    
    // Property definition
    public class TProperty2 extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public TProperty2(String name, boolean isWriteable) {
            super(name, Object.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return new TagsEditor();
        }
        
        public Object getValue(String key) {
            if ("canEditAsText".equals(key)) {
                return Boolean.TRUE;
            } else {
                return super.getValue(key);
            }
        }
    }
    
    // Property definition
    public class TProperty3 extends PropertySupport {
        private Boolean myValue = Boolean.FALSE;
        // Create new Property
        public TProperty3(String name, boolean isWriteable) {
            super(name, Boolean.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = (Boolean) value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        
        public Object getValue(String key) {
            if ("nameIcon".equals(key)) {
                return new NameIcon();
            } else if ("valueIcon".equals(key)) {
                return new ValueIcon();
            } else if ("postSetAction".equals(key)) {
                return postSetAction;
            }
            return super.getValue(key);
        }
        
        public PropertyEditor getPropertyEditor() {
            return new WrapperEx(super.getPropertyEditor());
        }
    
        public class WrapperEx implements ExPropertyEditor {
            private PropertyEditor orig;
            public WrapperEx (PropertyEditor orig) {
                this.orig = orig;
            }


            public void attachEnv(PropertyEnv env) {
                env.setState(myValue == Boolean.FALSE ? env.STATE_INVALID : env.STATE_VALID);
            }        

            public void addPropertyChangeListener(PropertyChangeListener listener) {
                orig.addPropertyChangeListener(listener);
            }

            public String getAsText() {
                return orig.getAsText();
            }

            public java.awt.Component getCustomEditor() {
                return orig.getCustomEditor();
            }

            public String getJavaInitializationString() {
                return orig.getJavaInitializationString();
            }

            public String[] getTags() {
                return orig.getTags();
            }

            public Object getValue() {
                return orig.getValue();
            }

            public boolean isPaintable() {
                return orig.isPaintable();
            }

            public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
                orig.paintValue(gfx, box);
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
                orig.removePropertyChangeListener(listener);
            }

            public void setAsText(String text) throws java.lang.IllegalArgumentException {
                orig.setAsText(text);
            }

            public void setValue(Object value) {
                orig.setValue(value);
            }

            public boolean supportsCustomEditor() {
                return orig.supportsCustomEditor();
            }
        }
    }
    
    public class BadEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public BadEditor() {
        }
        
        public String[] getTags() {
            //return new String[] {"a","b","c","d","Value"};
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
    }
    
    private class NameIcon implements Icon {
        
        public int getIconHeight() {
            return 12;
        }
        
        public int getIconWidth() {
            return 12;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color col = g.getColor();
            try {
                g.setColor(Color.BLUE);
                g.drawRect(x, y, getIconWidth(), getIconHeight());
                g.fillRect(x+3, y+3, getIconWidth()-5, getIconHeight()-5);
            } finally {
                g.setColor(col);
            }
        }
        
    }
    
    private class ValueIcon implements Icon {
        
        public int getIconHeight() {
            return 12;
        }
        
        public int getIconWidth() {
            return 12;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color col = g.getColor();
            try {
                g.setColor(Color.GREEN);
                g.drawRect(x, y, getIconWidth(), getIconHeight());
                g.fillRect(x+3, y+3, getIconWidth()-5, getIconHeight()-5);
            } finally {
                g.setColor(col);
            }
        }
    }
    
    private class PostSetAction extends AbstractAction {
        boolean performed = false;
        
        public void assertPerformed() {
            assertTrue ("Action was not performed", performed);
            performed = false;
        }
        
        public void assertNotPerformed() {
            assertTrue ("Action should not be performed before an appropriate event is triggered", !performed);
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            performed = true;
        }
        
    }
    
    
    private static TNode tn;
    private static TProperty tp;
    private static TProperty2 tp1;
    private static TEditor te;
    private static PostSetAction postSetAction;
    private static String initEditorValue;
    private static String initPropertyValue;
    private static String postChangePropertyValue;
    private static String postChangeEditorValue;
    
    //Shamelessly stolen from util.IconManager
    private static final BufferedImage toBufferedImage(Image img) {
        // load the image
        new javax.swing.ImageIcon(img);
        java.awt.image.BufferedImage rep = createBufferedImage(img.getWidth(null), img.getHeight(null));
        java.awt.Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();
        return rep;
    }
    
    /** Creates BufferedImage 16x16 and Transparency.BITMASK */
    private static final java.awt.image.BufferedImage createBufferedImage(int width, int height) {
        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().
                                          getDefaultScreenDevice().getDefaultConfiguration().getColorModel(java.awt.Transparency.BITMASK);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage(model,
                model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null);
        return buffImage;
    }
    

    
}
