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
 * Software is Nokia. Portions Copyright 2004 Nokia. All Rights Reserved.
 */

package org.openide.awt;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Tests for the Actions class.
 * @author David Strupl
 */
public class ActionsTest extends NbTestCase {
    
    // colors of the testing images in this order:
    // (test recognizes the icon by the white/black colors in specified positions :-)))
    // testIcon.gif
    // testIcon_rollover.gif
    // testIcon_pressed.gif
    // testIcon_disabled.gif
    private static int[][] RESULT_COLORS_00 = {
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
    };
    private static int[][] RESULT_COLORS_01 = {
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
    };
    private static int[][] RESULT_COLORS_11 = {
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
    };
    
    
    public ActionsTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        MockServices.setServices(new Class[] {TestKeymap.class});
        assertNotNull("Keymap has to be in lookup", Lookup.getDefault().lookup(Keymap.class));
    }
    
    /**
     * Test whether pressed, rollover and disabled icons
     * work for javax.swing.Action.
     */
    public void testIconsAction() throws Exception {
        JButton jb = new JButton();
        Actions.connect(jb, new TestAction());
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 0, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 1, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 2, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 3, "Disabled icon");
    }
    
    /**
     * Test whether pressed, rollover and disabled icons
     * work for SystemAction.
     */
    public void testIconsSystemAction() throws Exception {
        SystemAction saInstance = SystemAction.get(TestSystemAction.class);
        
        JButton jb = new JButton();
        Actions.connect(jb, saInstance);
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 0, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 1, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 2, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 3, "Disabled icon");
    }
    
    /**
     * Test whether pressed, rollover and disabled 24x24 icons
     * work for javax.swing.Action.
     */
    public void testIconsAction24() throws Exception {
        JButton jb = new JButton();
        jb.putClientProperty("PreferredIconSize",new Integer(24));
        Actions.connect(jb, new TestAction());
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 4, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 5, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 6, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 7, "Disabled icon");
    }
    
    /**
     * #47527
     * Tests if "noIconInMenu" really will NOT push the icon from the action
     * to the menu item.
     */
    public void testNoIconInMenu() throws Exception {
        JMenuItem item = new JMenuItem();
        item.setIcon(null);
        Actions.connect(item, new TestNoMenuIconAction(), false);
        assertNull(item.getIcon());
    }
    
    /**
     * Test whether pressed, rollover and disabled 24x24 icons
     * work for SystemAction.
     */
    public void testIconsSystemAction24() throws Exception {
        SystemAction saInstance = SystemAction.get(TestSystemAction.class);
        
        JButton jb = new JButton();
        jb.putClientProperty("PreferredIconSize",new Integer(24));
        Actions.connect(jb, saInstance);
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 4, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 5, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 6, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 7, "Disabled icon");
    }
    
    /**
     * tests if the accelerator for JMenuItem is reset when the global KeyMap changes.
     * Has to work even when the menu is not visible (when visible is handled by Actions.Bridge listeners)
     * when not visible handled by the tested Actions.setMenuActionConnection() - only for menu items.
     * #39508
     */
    public void testActionRemoval_Issue39508() throws Exception {
        // prepare
        Keymap map = (Keymap)Lookup.getDefault().lookup(Keymap.class);
        map.removeBindings();
        Action action = new ActionsTest.TestAction();
        KeyStroke stroke = KeyStroke.getKeyStroke("ctrl alt 7");
        assertNotNull(stroke);
        //test start
        JMenuItem menu = new JMenuItem();
        assertNull(menu.getAccelerator());
        Actions.connect(menu, action, false);
        assertEquals(1, ((Observable)map).countObservers());
        assertNull(menu.getAccelerator());
        map.addActionForKeyStroke(stroke, action);
        assertNotNull(action.getValue(Action.ACCELERATOR_KEY));
        assertNotNull(menu.getAccelerator());
        map.removeKeyStrokeBinding(stroke);
        assertNull(action.getValue(Action.ACCELERATOR_KEY));
        assertNull(menu.getAccelerator());
        Reference ref = new WeakReference(action);
        menu = null;
        action = null;
        assertGC("action can dissappear", ref);
    }
    
    /**
     * Tests if changes in accelerator key or name of the action does not change the tooltip
     * of the button if the action has a custom tooltip. See first part of #57974.
     */
    public void testTooltipsArePersistent() throws Exception {
        Action action = new ActionsTest.TestActionWithTooltip();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('a'));
        
        assertTrue(button.getToolTipText().indexOf(TestActionWithTooltip.TOOLTIP) != (-1));
        
        f.setVisible(false);
    }
    
    /**
     * Tests if the tooltip is made out of the NAME if there is not tooltip set for an action.
     * See also #57974.
     */
    public void testTooltipsIsBuiltFromNameIfNoTooltip() throws Exception {
        Action action = new ActionsTest.TestAction();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals("test"));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals("new-name"));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('a'));
        
        assertTrue(button.getToolTipText().indexOf("new-name") != (-1));
        
        f.setVisible(false);
    }
    
    /**
     * Tests if the accelerator key is shown in the button's tooltip for actions with
     * custom tooltips.
     */
    public void testTooltipsContainAccelerator() throws Exception {
        Action action = new ActionsTest.TestActionWithTooltip();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        
        assertTrue(button.getToolTipText().indexOf("Ctrl+C") != (-1));
        
        action.putValue(Action.SHORT_DESCRIPTION, null);
        
        assertTrue(button.getToolTipText().indexOf("Ctrl+C") != (-1));
        
        f.setVisible(false);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    private void checkIfLoadedCorrectIcon(Icon icon, Component c, int rowToCheck, String nameOfIcon) {
        checkIfIconOk(icon, c, 0, 0, RESULT_COLORS_00[rowToCheck], nameOfIcon);
        checkIfIconOk(icon, c, 0, 1, RESULT_COLORS_01[rowToCheck], nameOfIcon);
        checkIfIconOk(icon, c, 1, 1, RESULT_COLORS_11[rowToCheck], nameOfIcon);
    }
    
    /**
     * Checks colors on coordinates X,Y of the icon and compares them
     * to expectedResult.
     */
    private void checkIfIconOk(Icon icon, Component c, int pixelX, int pixelY, int[] expectedResult, String nameOfIcon) {
        BufferedImage bufImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        icon.paintIcon(c, bufImg.getGraphics(), 0, 0);
        int[] res = bufImg.getData().getPixel(pixelX, pixelY, (int[])null);
        log("Icon height is " + icon.getIconHeight());
        log("Icon width is " + icon.getIconWidth());
        for (int i = 0; i < res.length; i++) {
            // Huh, Ugly hack. the sparc returns a fuzzy values +/- 1 unit e.g. 254 for Black instead of 255 as other OSs do
            // this hack doesn't broken the functionality which should testing
            assertTrue(nameOfIcon + ": Color of the ["+pixelX+","+pixelY+"] pixel is " + res[i] + ", expected was " + expectedResult[i], Math.abs(res[i] - expectedResult[i]) < 10);
        }
    }
    
    private static final class TestSystemAction extends SystemAction {
        
        public void actionPerformed(ActionEvent e) {
        }
        
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        public String getName() {
            return "TestSystemAction";
        }
        
        protected String iconResource() {
            return "org/openide/awt/data/testIcon.gif";
        }
        
    }
    
    private static final class TestAction extends AbstractAction {
        
        public TestAction() {
            putValue("iconBase", "org/openide/awt/data/testIcon.gif");
            putValue(NAME, "test");
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    private static final class TestNoMenuIconAction extends AbstractAction {
        
        public TestNoMenuIconAction() {
            putValue("iconBase", "org/openide/awt/data/testIcon.gif");
            putValue("noIconInMenu", Boolean.TRUE);
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    private static final class TestActionWithTooltip extends AbstractAction {
        
        private static String TOOLTIP = "tooltip";
        
        public TestActionWithTooltip() {
            putValue(NAME, "name");
            putValue(SHORT_DESCRIPTION, TOOLTIP);
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    public static final class TestKeymap extends Observable implements Keymap {
        
        private Map map = new HashMap();
        private Action defAct;
        
        public void addActionForKeyStroke(KeyStroke key, Action act) {
            map.put(key, act);
            act.putValue(Action.ACCELERATOR_KEY, key);
            setChanged();
            notifyObservers();
        }
        
        public Action getAction(KeyStroke key) {
            return (Action)map.get(key);
        }
        
        public Action[] getBoundActions() {
            return new Action[0];
        }
        
        public KeyStroke[] getBoundKeyStrokes() {
            return new KeyStroke[0];
        }
        
        public Action getDefaultAction() {
            return defAct;
        }
        
        public KeyStroke[] getKeyStrokesForAction(Action a) {
            return new KeyStroke[0];
        }
        
        public String getName() {
            return "testKeymap";
        }
        
        public Keymap getResolveParent() {
            return null;
        }
        
        public boolean isLocallyDefined(KeyStroke key) {
            return true;
        }
        
        public void removeBindings() {
            map.clear();
        }
        
        public void removeKeyStrokeBinding(KeyStroke keys) {
            Action act = (Action)map.remove(keys);
            if (act != null) {
                act.putValue(Action.ACCELERATOR_KEY, null);
            }
            setChanged();
            notifyObservers();
        }
        
        public void setDefaultAction(Action a) {
            defAct = a;
        }
        
        public void setResolveParent(Keymap parent) {
            // ignore
        }
        
    }
    
}
