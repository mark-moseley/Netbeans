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

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabDisplayerUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.swing.tabcontrol.LocationInformer;
import org.netbeans.swing.tabcontrol.TabbedContainer;

/**
 * Basic UI class for view tabs - non scrollable tabbed displayer, which shows all
 * tabs equally sized, proportionally. This class is independent on specific
 * L&F, acts as base class for specific L&F descendants.
 * <p>
 * XXX eventually this class should be deleted and a subclass of BasicTabDisplayer can be used;
 * currently this is simply a port of the original code to the new API. Do not introduce any new
 * subclasses of this.
 *
 * @author Dafe Simonek
 *
 */
public abstract class AbstractViewTabDisplayerUI extends TabDisplayerUI {

    private TabDataModel dataModel;

    private ViewTabLayoutModel layoutModel;

    private FontMetrics fm;

    private Font txtFont;

    protected Controller controller;
    
    protected static IconLoader iconCache = new IconLoader();
    
    private PinButton pinButton;

    /** Pin action */
    private final Action pinAction = new PinAction();
    private static final String PIN_ACTION = "pinAction";

    public AbstractViewTabDisplayerUI (TabDisplayer displayer) {
        super (displayer);
        displayer.setLayout(null);
    }

    public void installUI(JComponent c) {
        super.installUI(c);
        controller = createController();
        dataModel = displayer.getModel();
        layoutModel = new ViewTabLayoutModel(dataModel, displayer);
        dataModel.addChangeListener (controller);
        displayer.addPropertyChangeListener (controller);
        selectionModel.addChangeListener (controller);
        displayer.addMouseListener(controller);
        displayer.addMouseMotionListener(controller);
        LocationInformer locInfo = displayer.getLocationInformer();
        if (locInfo != null) {
            pinButton = createPinButton();
        }
        if (pinButton != null) {
            displayer.add(pinButton);
            pinButton.addActionListener(controller);
        }
    }

    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        displayer.removePropertyChangeListener (controller);
        dataModel.removeChangeListener(controller);
        selectionModel.removeChangeListener(controller);
        displayer.removeMouseListener(controller);
        displayer.removeMouseMotionListener(controller);
        if (pinButton != null) {
            displayer.remove(pinButton);
            pinButton.removeActionListener(controller);
            pinButton = null;
        }
        layoutModel = null;
        selectionModel = null;
        dataModel = null;
        controller = null;
    }

    protected abstract Controller createController();

    public void paint(Graphics g, JComponent c) {
        TabData tabData;
        int x, y, width, height;
        String text;

        for (int i = 0; i < dataModel.size(); i++) {
            // gather data
            tabData = dataModel.getTab(i);
            x = layoutModel.getX(i);
            y = layoutModel.getY(i);
            width = layoutModel.getW(i);
            height = layoutModel.getH(i);
            text = tabData.getText();
            // perform paint
            if (g.hitClip(x, y, width, height)) {
                paintTabBackground(g, i, x, y, width, height);
                paintTabContent(g, i, text, x, y, width, height);
                paintTabBorder(g, i, x, y, width, height);
            }
        }
    }

    protected final TabDataModel getDataModel() {
        return dataModel;
    }

    public final TabLayoutModel getLayoutModel() {
        return layoutModel;
    }

    protected final TabDisplayer getDisplayer() {
        return displayer;
    }

    protected final SingleSelectionModel getSelectionModel() {
        return selectionModel;
    }

    public Controller getController() {
        return controller;
    }

    protected final boolean isSelected(int index) {
        return selectionModel.getSelectedIndex() == index;
    }

    protected final boolean isActive() {
        return displayer.isActive();
    }

    protected final boolean isFocused(int index) {
        return isSelected(index) && isActive();
    }

    protected final SingleSelectionModel createSelectionModel() {
        return new DefaultTabSelectionModel (displayer.getModel());
    }

    public String getCommandAtPoint(Point p) {
        return controller.inCloseIconRect(p) != -1 ? TabDisplayer.COMMAND_CLOSE :
                TabDisplayer.COMMAND_SELECT;
    }

    public int dropIndexOfPoint(Point p) {
        int result = 0;
        for (int i=0; i < displayer.getModel().size(); i++) {
            int x = getLayoutModel().getX(i);
            int w = getLayoutModel().getW(i);
            if (p.x >= x && p.x <= x + w) {
                if (i == displayer.getModel().size() - 1) {
                    if (p.x > x + (w / 2)) {
                        result = displayer.getModel().size();
                        break;
                    } else {
                        result = i;
                        break;
                    }
                } else {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Specifies font to use for text and font metrics. Subclasses may override
     * to specify their own text font
     */
    protected Font getTxtFont() {
        if (txtFont == null) {
            txtFont = (Font) UIManager.get("windowTitleFont");
            if (txtFont == null) {
                txtFont = new Font("Dialog", Font.PLAIN, 11);
            } else if (txtFont.isBold()) {
                txtFont = txtFont.deriveFont(Font.PLAIN);
            }
        }
        return txtFont;
    }

    protected final FontMetrics getTxtFontMetrics() {
        if (fm == null) {
            JComponent control = getDisplayer();
            fm = control.getFontMetrics(getTxtFont());
        }
        return fm;
    }

    protected abstract void paintTabContent(Graphics g, int index, String text,
                                            int x, int y, int width,
                                            int height);

    protected abstract void paintTabBorder(Graphics g, int index, int x, int y,
                                           int width, int height);

    protected abstract void paintTabBackground(Graphics g, int index, int x,
                                               int y, int width, int height);

    /**
     * Strips text to fit given width in actual font metrics. Cuts ands adds
     * three or less dots if string doesn't fit.
     *
     * @return text that fits into given width
     */
    protected static final String stripTextToFit(String text, int width,
                                                 FontMetrics fm) {
        int dotWidth = fm.charWidth('.');
        int curWidth = dotWidth;
        // three or less dots if desired width is really small
        if (width < curWidth) {
            return "";
        }
        curWidth += dotWidth;
        if (width < curWidth) {
            return ".";
        }
        curWidth += dotWidth;
        if (width < curWidth) {
            return "..";
        }
        for (int i = 0; i < text.length(); i++) {
            curWidth += fm.charWidth(text.charAt(i));
            if (width < curWidth) {
                // text doesn't fit, include portion of the text and finishing dots
                if (i == 0) {
                    return "...";
                }
                StringBuffer buf = new StringBuffer(text.substring(0, i));
                buf.append("...");
                return buf.toString();
            }
        }
        // OK, text all fits, no need to modify it
        return text;
    }

    /**
     * Utility to return y-axis centered icon position in given tab
     */
    protected final int getCenteredIconY(Icon icon, int index) {
        TabLayoutModel tlm = getLayoutModel();
        int y = tlm.getY(index);
        int h = tlm.getH(index);
        int iconHeight = icon.getIconHeight();
        return y + (Math.max(0, h / 2 - iconHeight / 2));
    }
    
    
    /** Utility method to access pin button instance conveniently */
    protected final PinButton getPinButton (int index) {
        if (pinButton == null) {
            return null;
        }
        LocationInformer locInfo = getDisplayer().getLocationInformer();
        if (locInfo == null) {
            return null;
        }
        Object orientation = locInfo.getOrientation(getDisplayer().getModel().getTab(index).getComponent());
        pinButton.setOrientation(orientation);
        
        return pinButton;
    }
    
    /** Subclasses should create and return pin button instance, parametrized
     * to given orientation
     * @see PinButton
     */ 
    // XXX - change back to abstract after implementing in all LFs
    protected /*abstract*/ PinButton createPinButton () {
        Map normalIcons = new HashMap(6);
        normalIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-east.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-west.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-south.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-center.gif");
        Map pressedIcons = new HashMap(6);
        pressedIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-east.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-west.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-south.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-center.gif");
        Map rolloverIcons = new HashMap(6);
        rolloverIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-east.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-west.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-south.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-center.gif");
        return new PinButton(normalIcons, pressedIcons, rolloverIcons);
    }

    /** Reaction to pin button / pin shortcut toggle. Does nothing itself,but
     * produces event for outer window system.
     */
    private void performPinAction() {
        // pin button only active on selected index, so this is safe here
        int index = getSelectionModel().getSelectedIndex();
        PinButton pinB = getPinButton(index);
        if (pinB != null) {
            if (TabDisplayer.ORIENTATION_CENTER.equals(pinB.getOrientation())) {
                shouldPerformAction(TabDisplayer.COMMAND_DISABLE_AUTO_HIDE, index, null);
            } else {
                shouldPerformAction(TabDisplayer.COMMAND_ENABLE_AUTO_HIDE, index, null);
            }
            // XXX - what to do if action was not consumed? nothing?
        }
    }
    
    /** Registers shortcut for enable/ disable auto-hide functionality */
    public void unregisterShortcuts(JComponent comp) {
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
            remove(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                                InputEvent.CTRL_DOWN_MASK));
        comp.getActionMap().remove(PIN_ACTION);
    }

    /** Registers shortcut for enable/ disable auto-hide functionality */
    public void registerShortcuts(JComponent comp) {
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
            put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                                InputEvent.CTRL_DOWN_MASK), PIN_ACTION);
        comp.getActionMap().put(PIN_ACTION, pinAction);
    }
    
    public Polygon getExactTabIndication(int index) {
        // TBD - the same code is copied in ScrollableTabsUI, should be shared
        // if will not differ
//        GeneralPath indication = new GeneralPath();
        JComponent control = getDisplayer();
        int height = control.getHeight();

        TabLayoutModel tlm = getLayoutModel();

        int tabXStart = tlm.getX(index);

        int tabXEnd = tabXStart + tlm.getW(index);

        int[] xpoints = new int[4];
        int[] ypoints = new int[4];
        xpoints[0] = tabXStart;
        ypoints[0] = 0;
        xpoints[1] = tabXEnd;
        ypoints[1] = 0;
        xpoints[2] = tabXEnd;
        ypoints[2] = height - 1;
        xpoints[3] = tabXStart;
        ypoints[3] = height - 1;

        return new EqualPolygon(xpoints, ypoints);
    }

    public Polygon getInsertTabIndication(int index) {
        EqualPolygon indication = new EqualPolygon();
        JComponent control = getDisplayer();
        int height = control.getHeight();
        int width = control.getWidth();
        TabLayoutModel tlm = getLayoutModel();

        int tabXStart;
        int tabXEnd;
        if (index == 0) {
            tabXStart = 0;
            tabXEnd = tlm.getW(0) / 2;
        } else if (index >= getDataModel().size()) {
            tabXStart = tlm.getX(index - 1) + tlm.getW(index - 1) / 2;
            tabXEnd = tabXStart + tlm.getW(index - 1);
            if (tabXEnd > width) {
                tabXEnd = width;
            }
        } else {
            tabXStart = tlm.getX(index - 1) + tlm.getW(index - 1) / 2;
            tabXEnd = tlm.getX(index) + tlm.getW(index) / 2;
        }

        indication.moveTo(tabXStart, 0);
        indication.lineTo(tabXEnd, 0);
        indication.lineTo(tabXEnd, height - 1);
        indication.lineTo(tabXStart, height - 1);
        return indication;
    }

    /**
     * Loader for icons. Caches loaded icons using hash map.
     */
    final static class IconLoader {
        /* mapping <String, Icon> from resource paths to icon objects, used as cache */
        private Map paths2Icons;

        /**
         * Finds and returns icon instance from cache, if present. Otherwise
         * loads icon using given resource path and stores icon into cache for
         * next access.
         *
         * @return icon image
         */
        public Icon obtainIcon(String iconPath) {
            if (paths2Icons == null) {
                paths2Icons = new HashMap(6);
            }
            Icon icon = (Icon) paths2Icons.get(iconPath);
            if (icon == null) {
                // not yet in cache, load and store
                Image image = loadImage(iconPath);
                if (image == null) {
                    throw new IllegalArgumentException("Icon with resource path: "
                                                       + iconPath
                                                       + " can't be loaded, probably wrong path.");
                }
                icon = new ImageIcon(image);
                paths2Icons.put(iconPath, icon);
            }
            return icon;
        }

    } // end of IconLoader

    private static Image loadImage(String path) {
        try {
            URL url = AbstractViewTabDisplayerUI.class.getResource("/"+path);
            return ImageIO.read(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Paints the rectangle occupied by a tab into an image and returns the result */
    public Image createImageOfTab(int index) {
        Rectangle r = new Rectangle();
        getTabRect(index, r);

        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();


        BufferedImage image = config.createCompatibleImage(r.width, r.height);
        Graphics2D g = image.createGraphics();
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                       0.5f);

        g.setComposite(ac);
        g.translate(-r.x, -r.y);

        displayer.paint(g);

        return image;
    }

    public Rectangle getTabRect(int index, Rectangle destination) {
        if (index < 0 || index > displayer.getModel().size()) {
            throw new ArrayIndexOutOfBoundsException("Tab index out of " +
                "bounds: " + index);
        }
        destination.x = layoutModel.getX(index);
        destination.width = layoutModel.getW(index);
        destination.height = layoutModel.getH(index);
        destination.y = Math.min (0, displayer.getHeight() - destination.height);
        return destination;
    }
    
    public int tabForCoordinate(Point p) {
        int max = displayer.getModel().size();
        if (max == 0 || p.y > displayer.getHeight() || p.y < 0 || p.x < 0 || 
            p.x > displayer.getWidth()) {
                
            return -1;
        }
        
        for (int i=0; i < max; i++) {
            int left = layoutModel.getX(i);
            int right = left + layoutModel.getW(i);
            if (p.x > left && p.x < right) {
                return i;
            }
        }
        
        return -1;
    }

    /**
     * Listen to mouse events and handles selection behaviour and close icon
     * button behaviour.
     */
    abstract class Controller extends MouseAdapter
            implements MouseMotionListener, ChangeListener, PropertyChangeListener, ActionListener {

        /**
         * index of tab whose close icon currently pressed, -1 otherwise
         */
        // TBD - should be part of model, not controller
        private int closePressed = -1;
        /**
         * index of tab whose close icon active area contains current mouse
         * pointer, false otherwise
         */
        // TBD - should be part of model, not controller
        private int mouseInCloseButton = -1;
        /**
         * true when selection is changed as a result of mouse press
         */
        private boolean selectionChanged;

        /**
         * Subclasses should override this method by detecting if given point is
         * contained in close icon.
         *
         * @return index of tab which close icon area contains given point, -1
         *         if point is outside any close icon area.
         */
        protected abstract int inCloseIconRect(Point point);

        protected boolean shouldReact(MouseEvent e) {
            boolean isLeft = SwingUtilities.isLeftMouseButton(e);
            return isLeft;
        }

        public void stateChanged (ChangeEvent ce) {
            displayer.repaint();
        }

        public void propertyChange (PropertyChangeEvent pce) {
            if (TabDisplayer.PROP_ACTIVE.equals (pce.getPropertyName())) {
                displayer.repaint();
            }
        }

        /**
         * Performs button action, default impl removes the tab. Subclasses can
         * alter this by overriding.
         */
        protected void performAction(MouseEvent e) {
            if (shouldPerformAction (TabDisplayer.COMMAND_CLOSE, mouseInCloseButton, e)) {
                //In NetBeans winsys, this should never be called - TabbedHandler will
                //consume the event when it is re-propagated from the TabbedContainer
                getDataModel().removeTab(mouseInCloseButton);
            }
        }


        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            int i = getLayoutModel().indexOfPoint(p.x, p.y);
            SingleSelectionModel sel = getSelectionModel();
            selectionChanged = i != sel.getSelectedIndex();
            // invoke possible selection change
            if ((i != -1) || !selectionChanged) {
                getSelectionModel().setSelectedIndex(i);
            } 
            // update pressed state
            if (shouldReact(e) && !selectionChanged) {
                setClosePressed(inCloseIconRect(e.getPoint()));
            }
            if ((i != -1) && e.isPopupTrigger()) {
                //Post a popup menu show request
                shouldPerformAction(TabDisplayer.COMMAND_POPUP_REQUEST, i, e);
            }
        }

        public void mouseClicked (MouseEvent e) {
            if (e.getClickCount() >= 2 && !e.isPopupTrigger()) {
                Point p = e.getPoint();
                int i = getLayoutModel().indexOfPoint(p.x, p.y);
                SingleSelectionModel sel = getSelectionModel();
                selectionChanged = i != sel.getSelectedIndex();
                // invoke possible selection change
                if ((i != -1) || !selectionChanged) {
                    getSelectionModel().setSelectedIndex(i);
                }
                if (i != -1) {
                    //Post a maximize request
                    shouldPerformAction(TabDisplayer.COMMAND_MAXIMIZE, i, e);
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            // close button must not be active when selection change was
            // triggered by mouse press
            if (shouldReact(e) && !selectionChanged) {
                setClosePressed(-1);
                Point point = e.getPoint();
                if ((mouseInCloseButton = inCloseIconRect(point)) >= 0) {
                    performAction(e);
                    // reset rollover effect after action is complete
                    setMouseInCloseButton(point);
                }
            }
            Point p = e.getPoint();
            int i = getLayoutModel().indexOfPoint(p.x, p.y);
            if ((i != -1) && e.isPopupTrigger()) {
                //Post a popup menu show request
                shouldPerformAction(TabDisplayer.COMMAND_POPUP_REQUEST, i, e);
            }
        }

        public void mouseMoved(MouseEvent e) {
            setMouseInCloseButton(e.getPoint());
        }

        public void mouseDragged(MouseEvent e) {
            setClosePressed(inCloseIconRect(e.getPoint()));
            setMouseInCloseButton(e.getPoint());
        }

        public void mouseExited(MouseEvent e) {
            setMouseInCloseButton(e.getPoint());
        }

        /**
         * @return true if close icon is pressed at the time of calling this
         *         method, false otherwise
         */
        public int isClosePressed() {
            return closePressed;
        }

        /**
         * @return true if mouse pointer is in close icon active area at the
         *         time of calling this method, false otherwise
         */
        public int isMouseInCloseButton() {
            return mouseInCloseButton;
        }

        /**
         * Sets state of close button to pressed or released. Updates visual
         * state properly.
         */
        protected void setClosePressed(int pressed) {
            if (closePressed == pressed) {
                return;
            }
            int oldValue = closePressed;
            closePressed = pressed;
            if (closePressed == -1) {
                // press ended
                TabLayoutModel tlm = getLayoutModel();
                getDisplayer().repaint(tlm.getX(oldValue),
                                     tlm.getY(oldValue),
                                     tlm.getW(oldValue),
                                     tlm.getH(oldValue));

            } else if (oldValue == -1) {
                // press started
                TabLayoutModel tlm = getLayoutModel();
                getDisplayer().repaint(tlm.getX(closePressed),
                                     tlm.getY(closePressed),
                                     tlm.getW(closePressed),
                                     tlm.getH(closePressed));
            } else {
                // rare situation, two tabs need repaint, so repaint all
                getDisplayer().repaint();
            }
        }

        /**
         * Sets state of mouse in close button value. Requests repaint of visual
         * state properly.
         */
        protected void setMouseInCloseButton(Point location) {
            int isNow = inCloseIconRect(location);
            if (mouseInCloseButton == isNow || dataModel.size() == 0) {
                return;
            }
            // sync of indexes
            int oldValue = mouseInCloseButton;
            mouseInCloseButton = isNow;
            if (isNow == -1) {
                // exit from close area
                TabLayoutModel tlm = getLayoutModel();
                getDisplayer().repaint(tlm.getX(oldValue),
                                     tlm.getY(oldValue),
                                     tlm.getW(oldValue),
                                     tlm.getH(oldValue));

            } else if (oldValue == -1) {
                // enter into close area
                TabLayoutModel tlm = getLayoutModel();
                getDisplayer().repaint(tlm.getX(isNow), tlm.getY(isNow),
                                     tlm.getW(isNow), tlm.getH(isNow));
            } else {
                // rare situation, two tabs need repaint, so repaint all
                getDisplayer().repaint();
            }
        }
        
        /** Implementation of ActionListener. Reacts to pin button clicks
         */
        public void actionPerformed(ActionEvent e) {
            performPinAction();
        }
        
    } // end of Controller
    

    /** Implementation of Pin button, its look is dependent on orientation
     * and can be set using setOrientation method.
     */
    protected static class PinButton extends JButton {
        
        private Map pressedIcons, rolloverIcons, regularIcons;
        
        private Object orientation;
        
        public PinButton (Map regularIcons, Map pressedIcons, Map rolloverIcons) {
            super();
            this.regularIcons = regularIcons;
            this.pressedIcons = pressedIcons;
            this.rolloverIcons = rolloverIcons;
            setFocusable(false);
            setBorder(null);
            setContentAreaFilled(false);
            setRolloverEnabled(rolloverIcons != null);
            setOrientation(TabDisplayer.ORIENTATION_CENTER);
        }
        
        public Object getOrientation () {
            return orientation;
        }
        
        public void setOrientation (Object orientation) {
            this.orientation = orientation;
            Icon icon = iconCache.obtainIcon((String)regularIcons.get(orientation));
            setIcon(icon);
            setSize(icon.getIconWidth(), icon.getIconHeight());
            if (pressedIcons != null) {
                setPressedIcon(iconCache.obtainIcon((String)regularIcons.get(orientation)));
            }
            if (rolloverIcons != null) {
                setRolloverIcon(iconCache.obtainIcon((String)rolloverIcons.get(orientation)));
            }
        }
        
    } // end of PinButton

    /** Executes enable / disable auto-hide mode */
    private final class PinAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            performPinAction();
        }
    } // end of PinAction
    
    
}
