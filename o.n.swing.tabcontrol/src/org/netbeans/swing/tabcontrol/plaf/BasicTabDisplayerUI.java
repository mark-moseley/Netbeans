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
package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.event.ListDataEvent;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabbedContainer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;

/**
 * Base class for tab displayer UIs which use cell renderers to display tabs.
 * This class does not contain much logic itself, but rather acts to connect events
 * and data from various objects relating to the tab displayer, which it creates and
 * installs.  Basically, the things that are involved are:
 * <ul>
 * <li>A layout model (TabLayoutModel) - A data model providing the positions and sizes of tabs</li>
 * <li>A state model (TabState) - A data model which tracks state data (selected, pressed, etc.)
 *     for each tab, and can be queried when a tab is painted to determine how that should be done.</li>
 * <li>A selection model (SingleSelectionModel) - Which tracks which tab is selected</li>
 * <li>The TabDisplayer component itself</li>
 * <li>The TabDisplayer's data model, which contains the list of tab names, their icons and
 *     tooltips and the user object (or Component) they identify</li>
 * <li>Assorted listeners on the component and data models, specifically
 *       <ul><li>A mouse listener that tells the state model when a state-affecting event
 *               has happened, such as the mouse entering a tab</li>
 *           <li>A change listener that repaints appropriately when the selection changes</li>
 *           <li>A property change listener to trigger any repaints needed due to property
 *               changes on the displayer component</li>
 *           <li>A component listener to attach and detach listeners when the component is shown/
 *               hidden, and if neccessary, notify the layout model when the component is resized</li>
 *           <li>A default TabCellRenderer, which is what will actually paint the tabs, and which
 *               is also responsible for providing some miscellaneous data such as the number of
 *               pixels the layout model should add to tab widths to make room for decorations,
 *               etc.</li>
 *       </ul>
 * </ul>
 * The usage pattern of this class is similar to other ComponentUI subclasses - <code>installUI()</code>
 * is called via <code>JComponent.updateUI()</code>.  InstallUI initializes protected fields which
 * subclasses will need, in a well defined way; abstract methods are provided for subclasses to
 * create these objects (such as the things listed above), and convenience implementations of some
 * are provided. <code>Under no circumstances</code> should subclasses modify these protected fields -
 * due to the circuitousness of the way Swing installs UIs, they cannot be declared final, but should
 * be treated as read-only.
 * <p>
 * The goal of this class is to make it quite easy to implement new appearances
 * for tabs:  To create a new appearance, implement a TabCellRenderer that can 
 * paint individual tabs as desired.  This is made even easier via the 
 * TabPainter interface - simply create the painting logic needed there.  Then
 * subclass BasicTabDisplayerUI and include any painting logic for the background,
 * scroll buttons, etc. needed.  A good example is <a href="AquaEditorTabDisplayerUI">
 * AquaEditorTabDisplayerUI</a>
 *
 */
public abstract class BasicTabDisplayerUI extends AbstractTabDisplayerUI {
    protected TabState tabState = null;
    private static final boolean swingpainting = Boolean.getBoolean(
            "nb.tabs.swingpainting"); //NOI18N

    protected TabCellRenderer defaultRenderer = null;
    protected int repaintPolicy = 0;

    //A couple rectangles for calculation purposes
    private Rectangle scratch = new Rectangle();
    private Rectangle scratch2 = new Rectangle();
    private Rectangle scratch3 = new Rectangle();

    private Point lastKnownMouseLocation = new Point();

    int pixelsToAdd = 0;

    /** Command string renderers use to indicate they should be unhidden leftwards */
    static final String UNHIDE_LEFT = "unhideMeLeft"; //NOI18N
    /** Command string renderers use to indicate they should be unhidden rightwards */
    static final String UNHIDE_RIGHT = "unhideMeRight"; //NOI18N
    
    
    public BasicTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    /**
     * Overridden to initialize the <code>tabState</code> and <code>defaultRenderer</code>.
     */
    protected void install() {
        super.install();
        tabState = createTabState();
        defaultRenderer = createDefaultRenderer();
        layoutModel.setPadding (defaultRenderer.getPadding());
        pixelsToAdd = defaultRenderer.getPixelsToAddToSelection();
        repaintPolicy = createRepaintPolicy();
        if (displayer.getSelectionModel().getSelectedIndex() != -1) {
            tabState.setSelected(displayer.getSelectionModel().getSelectedIndex());
            tabState.setActive(displayer.isActive());
        }
    }

    protected void uninstall() {
        tabState = null;
        defaultRenderer = null;
        super.uninstall();
    }
    
    /** Used by unit tests */
    TabState getTabState() {
        return tabState;
    }

    /**
     * Create a TabState instance.  TabState manages the state of tabs - that is, which one
     * contains the mouse, which one is pressed, and so forth, providing methods such as
     * <code>setMouseInTab(int tab)</code>.  Its getState() method returns a bitmask of
     * states a tab may have which affect the way it is painted.
     * <p>
     * <b>Usage:</b> It is expected that UIs will subclass TabState, to implement the
     * repaint methods, and possibly override <code>getState(int tab)</code> to mix
     * additional state bits into the bitmask.  For example, scrollable tabs have the
     * possible states CLIP_LEFT and CLIP_RIGHT; BasicScrollingTabDisplayerUI's
     * implementation of this determines these states by consulting its layout model, and
     * adds them in when appropriate.
     *
     * @return An implementation of TabState
     * @see BasicTabDisplayerUI.BasicTabState
     * @see BasicScrollingTabDisplayerUI.ScrollingTabState
     */
    protected TabState createTabState() {
        return new BasicTabState();
    }

    /**
     * Create the default cell renderer for this control.  If it is desirable to
     * have more than one renderer, override getTabCellRenderer()
     */
    protected abstract TabCellRenderer createDefaultRenderer();

    /**
     * Return a set of insets defining the margins into which tabs should not be
     * painted.  Subclasses that want to paint some controls to the right of the
     * tabs should include space for those controls in these insets.  If a
     * bottom margin under the tabs is to be painted, include that as well.
     */
    public abstract Insets getTabAreaInsets();

    /**
     * Get the cell renderer for a given tab.  The default implementation simply
     * returns the renderer created by createDefaultRenderer().
     */
    protected TabCellRenderer getTabCellRenderer(int tab) {
        return defaultRenderer;
    }

    /**
     * Set the passed rectangle's bounds to the recangle in which tabs will be
     * painted; if your look and feel reserves some part of the tab area for its
     * own painting.  The rectangle is determined by what is returned by
     * getTabAreaInsets() - this is simply a convenience method for finding the
     * rectange into which tabs will be painted.
     */
    protected final void getTabsVisibleArea(Rectangle rect) {
        Insets ins = getTabAreaInsets();
        rect.x = ins.left;
        rect.y = ins.top;
        rect.width = displayer.getWidth() - ins.right;
        rect.height = displayer.getHeight() - ins.bottom;
    }

    protected MouseListener createMouseListener() {
        return new BasicDisplayerMouseListener();
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return new BasicDisplayerPropertyChangeListener();
    }

    public Polygon getExactTabIndication(int index) {
        Rectangle r = getTabRect(index, scratch);
        return getTabCellRenderer(index).getTabShape(tabState.getState(index), r);
    }

    public Polygon getInsertTabIndication(int index) {
        Polygon p;
        if (index == getLastVisibleTab() + 1) {
            p = (Polygon) getExactTabIndication (index-1);
            Rectangle r = getTabRect(index-1, scratch);
            p.translate(r.width/2, 0);
        } else {
            p = (Polygon) getExactTabIndication (index);
            Rectangle r = getTabRect(index, scratch);
            p.translate(-(r.width/2), 0);
        }
        return p;
    }

    public int tabForCoordinate(Point p) {
        if (displayer.getModel().size() == 0) {
            return -1;
        }
        getTabsVisibleArea(scratch);
        if (!scratch.contains(p)) {
            return -1;
        }
        return layoutModel.indexOfPoint(p.x, p.y);
    }

    public Rectangle getTabRect(int idx, Rectangle rect) {
        if (rect == null) {
            rect = new Rectangle();
        }
        if (idx < 0 || idx >= displayer.getModel().size()) {
            rect.x = rect.y = rect.width = rect.height = 0;
            return rect;
        }
        rect.x = layoutModel.getX(idx);
        rect.y = layoutModel.getY(idx);
        rect.width = layoutModel.getW(idx);
        getTabsVisibleArea(scratch3);
        //XXX for R->L component orientation cannot assume x = 0
        int maxPos = scratch.x + scratch3.width;
        if (rect.x > maxPos) {
            rect.width = 0;
        } else if (rect.x + rect.width > maxPos) {
            rect.width = (maxPos - rect.x);
        }
        rect.height = layoutModel.getH(idx);
        getTabsVisibleArea(scratch2);
        if (rect.y + rect.height > scratch2.y + scratch2.height) {
            rect.height = (scratch2.y + scratch2.height) - rect.y;
        }
        if (rect.x + rect.width > scratch2.x + scratch2.width) {
            rect.width = (scratch2.x + scratch2.width) - rect.x;
        }
        return rect;
    }

    public Image createImageOfTab(int index) {
        TabData td = displayer.getModel().getTab(index);
        
        JLabel lbl = new JLabel(td.getText());
        int width = lbl.getFontMetrics(lbl.getFont()).stringWidth(td.getText());
        int height = lbl.getFontMetrics(lbl.getFont()).getHeight();
        width = width + td.getIcon().getIconWidth() + 6;
        height = Math.max(height, td.getIcon().getIconHeight()) + 5;
        
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                        .getDefaultScreenDevice().getDefaultConfiguration();
        
        BufferedImage image = config.createCompatibleImage(width, height);
        Graphics2D g = image.createGraphics();
        g.setColor(lbl.getForeground());
        g.setFont(lbl.getFont());
        td.getIcon().paintIcon(lbl, g, 0, 0);
        g.drawString(td.getText(), 18, height / 2);
        
        
        return image;
    }

    public String getCommandAtPoint(Point p) {
        getTabsVisibleArea(scratch);
        if (scratch.contains(p)) {
            int idx = tabForCoordinate(p);
            if (idx != -1) {
                TabCellRenderer tcr = getTabCellRenderer (idx);
                getTabRect (idx, scratch);
                String action = tcr.getCommandAtPoint(p, tabState.getState(idx), scratch);
                return action;
            }
        }
        return super.getCommandAtPoint (p);
    }

    public int dropIndexOfPoint(Point p) {
        Point p2 = toDropPoint(p);
        int start = getFirstVisibleTab();
        int end = getLastVisibleTab();
        int target;
        for (target = start; target <= end; target ++) {
            getTabRect (target, scratch);
            if (scratch.contains(p2)) {
                if (target == end) {
                    Object orientation = displayer.getClientProperty (TabDisplayer.PROP_ORIENTATION);
                    boolean flip = displayer.getType() == TabDisplayer.TYPE_SLIDING && (
                            orientation == TabDisplayer.ORIENTATION_EAST ||
                            orientation == TabDisplayer.ORIENTATION_WEST);

                    if (flip) {
                        if (p2.y > scratch.y + (scratch.height / 2)) {
                            return target+1;
                        }
                    } else {
                        if (p2.x > scratch.x + (scratch.width / 2)) {
                            return target+1;
                        }
                    }
                }
                return target;
            }
        }
        return -1;
    }

    protected boolean isAntialiased() {
        return ColorUtil.shouldAntialias();
    }

    /**
     * Paints the tab control.  Calls paintBackground(), then paints the tabs using
     * their cell renderers,
     * then calls paintAfterTabs
     */
    public final void paint(Graphics g, JComponent c) {
        assert c == displayer;
        
        if (isAntialiased()) {
            ColorUtil.setupAntialiasing(g);
        }
        
        boolean showClose = displayer.isShowCloseButton();
        
        paintBackground(g);
        int start = getFirstVisibleTab();
        if (start == -1 || !displayer.isShowing()) {
            return;
        }
        //Possible to have a repaint called by a mouse-clicked event if close on mouse press
        int stop = Math.min(getLastVisibleTab(), displayer.getModel().size() - 1);
        getTabsVisibleArea(scratch);
        
//System.err.println("paint, clip bounds: " + g.getClipBounds() + " first visible: " + start + " last: " + stop);

        if (g.hitClip(scratch.x, scratch.y, scratch.width, scratch.height)) {
            Shape s = g.getClip();
            try {
                //Ensure that we will never paint an icon into the controls area
                //by setting the clipping bounds
                if (s != null) {
                    //Okay, some clip area is already set.  Get the intersection.
                    Area a = new Area(s);
                    a.intersect(new Area(scratch.getBounds2D()));
                    g.setClip(a);
                } else {
                    //Clip was not set (it's a normal call to repaint() or something
                    //like that).  Just set the bounds.
                    g.setClip(scratch.x, scratch.y, scratch.width,
                              scratch.height);
                }


                for (int i = start; i <= stop; i++) {
                    getTabRect(i, scratch);
                    if (g.hitClip(scratch.x, scratch.y, scratch.width + 1,
                                  scratch.height + 1)) {
                                      
                        int state = tabState.getState(i);
                        
                        if ((state & TabState.NOT_ONSCREEN) == 0) {
                            TabCellRenderer ren = getTabCellRenderer(i);
                            ren.setShowCloseButton(showClose);
                            
                            TabData data = displayer.getModel().getTab(i);
                            
                            
                            JComponent renderer = ren.getRendererComponent(
                                    data, scratch, state);
                            
                            renderer.setFont(displayer.getFont());
                            //prepareRenderer ( renderer, data, ren.getLastKnownState () );
                            if (swingpainting) {
                                //Conceivable that some L&F may need this, but it generates
                                //lots of useless events - better to do direct painting where
                                //possible
                                SwingUtilities.paintComponent(g, renderer,
                                                              displayer,
                                                              scratch);
                            } else {
                                try {
                                    g.translate(scratch.x, scratch.y);
                                    renderer.setBounds(scratch);
                                    renderer.paint(g);
                                } finally {
                                    g.translate(-scratch.x, -scratch.y);
                                }
                            }
                        }
                    }
                }
            } finally {
                g.setClip(s);
            }
        }
        paintAfterTabs(g);
    }

    /**
     * Fill in the background of the component prior to painting the tabs.  The default
     * implementation does nothing.  If it's just a matter of filling in a background color,
     * setOpaque (true) on the displayer, and ComponentUI.update() will take care of the rest.
     */
    protected void paintBackground(Graphics g) {

    }

    /**
     * Override this method to provide painting of areas outside the tabs
     * rectangle, such as margins and controls
     */
    protected void paintAfterTabs(Graphics g) {
        //do nothing
    }

    /**
     * Scrollable implementations will override this method to provide the first
     * visible (even if clipped) tab.  The default implementation returns 0 if
     * there is at least one tab in the data model, or -1 to indicate the model
     * is completely empty
     */
    protected int getFirstVisibleTab() {
        return displayer.getModel().size() > 0 ? 0 : -1;
    }

    /**
     * Scrollable implementations will override this method to provide the last
     * visible (even if clipped) tab.  The default implementation returns 0 if
     * there is at least one tab in the data model, or -1 to indicate the model
     * is completely empty
     */
    protected int getLastVisibleTab() {
        return displayer.getModel().size() - 1;
    }

    protected ChangeListener createSelectionListener() {
        return new BasicSelectionListener();
    }

    protected final Point getLastKnownMouseLocation() {
        return lastKnownMouseLocation;
    }

    /**
     * Convenience method to override for handling mouse wheel events. The
     * defualt implementation does nothing.
     */
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        //do nothing
    }
    
    protected final void requestAttention (int tab) {
        tabState.addAlarmTab(tab);
    }
    
    protected final void cancelRequestAttention (int tab) {
        tabState.removeAlarmTab(tab);
    }
    

    protected void modelChanged() {
        tabState.clearTransientStates();
        //DefaultTabSelectionModel automatically updates its selected index when things
        //are added/removed from the model, so just make sure our state machine stays in
        //sync
        int idx = selectionModel.getSelectedIndex();
        tabState.setSelected(idx);
        tabState.pruneAlarmTabs(displayer.getModel().size());
        super.modelChanged();
    }

    /**
     * Create the policy that will determine what types of events trigger a repaint of one or more tabs.
     * This is a bitmask composed of constants defined in TabState. The default value is
     * <pre>
     *  TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_TAB
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ON_MOUSE_PRESSED;
     *</pre>
     *
     *
     * @return  The repaint policy that should be used in conjunction with mouse events to determine when a
     *          repaint is needed.
     */
    protected int createRepaintPolicy () {
        return TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_TAB
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ON_MOUSE_PRESSED;
    }


    protected class BasicTabState extends TabState {

        public int getState(int tab) {
            if (displayer.getModel().size() == 0) {
                return TabState.NOT_ONSCREEN;
            }
            int result = super.getState(tab);
            if (tab == 0) {
                result |= TabState.LEFTMOST;
            }
            if (tab == displayer.getModel().size() - 1) {
                result |= TabState.RIGHTMOST;
            }
            return result;
        }

        protected void repaintAllTabs() {
            //XXX would be nicer to just repaint the tabs area,
            //but we also need to repaint below all the tabs in the
            //event of activated/deactivated.  No actual reason to
            //repaint the buttons here.
            displayer.repaint();
        }

        public int getRepaintPolicy(int tab) {
            //Defined in createRepaintPolicy()
            return repaintPolicy;
        }

        protected void repaintTab(int tab) {
            if (tab == -1 || tab > displayer.getModel().size()) {
                return;
            }
            getTabRect(tab, scratch);
            scratch.y = 0;
            scratch.height = displayer.getHeight();
            displayer.repaint(scratch.x, scratch.y, scratch.width,
                              scratch.height);
        }
    }
    
    protected ModelListener createModelListener() {
        return new BasicModelListener();
    }    

    private class BasicDisplayerPropertyChangeListener
            extends DisplayerPropertyChangeListener {

        protected void activationChanged() {
            tabState.setActive(displayer.isActive());
        }
    }

    protected class BasicDisplayerMouseListener implements MouseListener,
            MouseMotionListener, MouseWheelListener {
        private int updateMouseLocation(MouseEvent e) {
            lastKnownMouseLocation.x = e.getX();
            lastKnownMouseLocation.y = e.getY();
            return tabForCoordinate(lastKnownMouseLocation);
        }

        public void mouseClicked(MouseEvent e) {
            int idx = updateMouseLocation(e);
            if (idx == -1) {
                return;
            }

            TabCellRenderer tcr = getTabCellRenderer(idx);
            getTabRect(idx, scratch);
            int state = tabState.getState(idx);

            potentialCommand (idx, e, state, tcr, scratch);
        }

        public void mouseDragged(MouseEvent e) {
            mouseMoved (e);
        }

        public void mouseEntered(MouseEvent e) {
            int idx = updateMouseLocation(e);
            tabState.setMouseInTabsArea(true);
            tabState.setContainsMouse(idx);
        }

        public void mouseExited(MouseEvent e) {
            updateMouseLocation(e);
            tabState.setMouseInTabsArea(false);
            tabState.setContainsMouse(-1);
            tabState.setCloseButtonContainsMouse(-1);
        }

        public void mouseMoved(MouseEvent e) {
            int idx = updateMouseLocation(e);
            tabState.setMouseInTabsArea(true);
            tabState.setContainsMouse(idx);
            if (idx != -1) {
                TabCellRenderer tcr = getTabCellRenderer(idx);
                getTabRect(idx, scratch);
                int state = tabState.getState(idx);

                String s = tcr.getCommandAtPoint(e.getPoint(), state, scratch);
                if (TabDisplayer.COMMAND_CLOSE == s) {
                    tabState.setCloseButtonContainsMouse(idx);
                } else {
                    tabState.setCloseButtonContainsMouse(-1);
                }
            } else {
                tabState.setContainsMouse(-1);
            }
        }

        private int lastPressedTab = -1;
        private long pressTime = -1;
        public void mousePressed(MouseEvent e) {
            int idx = updateMouseLocation(e);
            tabState.setPressed(idx);

            //One a double click, preserve the tab that was initially clicked, in case
            //a re-layout happened.  We'll pass that to the action.
            long time = System.currentTimeMillis();
            if (time - pressTime > 400) {
                lastPressedTab = idx;
            }
            pressTime = time;
            if (idx != -1) {
                TabCellRenderer tcr = getTabCellRenderer(idx);
                getTabRect(idx, scratch);
                int state = tabState.getState(idx);

                //First find the command for the location with the default button -
                //TabState may trigger a repaint
                String command = tcr.getCommandAtPoint (e.getPoint(), state, scratch);
                if (TabDisplayer.COMMAND_CLOSE == command) {
                    tabState.setCloseButtonContainsMouse(idx);
                    tabState.setMousePressedInCloseButton(idx);

                    //We're closing, don't try to maximize this tab if it turns out to be
                    //a double click
                    pressTime = -1;
                    lastPressedTab = -1;
                }

                potentialCommand (idx, e, state, tcr, scratch);
            } else {
                tabState.setMousePressedInCloseButton(-1); //just in case
            }
        }

        private void potentialCommand (int idx, MouseEvent e, int state, TabCellRenderer tcr, Rectangle bounds) {
            String command = tcr.getCommandAtPoint (e.getPoint(), state, bounds,
                    e.getButton(), e.getID(), e.getModifiersEx());
            if (command == null || TabDisplayer.COMMAND_SELECT == command) {
                if (e.isPopupTrigger()) {
                    displayer.repaint();
                    performCommand (TabDisplayer.COMMAND_POPUP_REQUEST, idx, e);
                    return;
                } else if (e.getID() == MouseEvent.MOUSE_CLICKED && e.getClickCount() >= 2) {
                    performCommand (TabDisplayer.COMMAND_MAXIMIZE, idx, e);
                    return;
                }
            }

            if (command != null) {
                performCommand (command, lastPressedTab == -1 || lastPressedTab >
                    displayer.getModel().size() ? idx : lastPressedTab, e);
            }
        }

        private void performCommand (String command, int idx, MouseEvent evt) {
            evt.consume();
            if (TabDisplayer.COMMAND_SELECT == command) {
                if (idx != displayer.getSelectionModel().getSelectedIndex()) {
                    boolean go = shouldPerformAction (command, idx, evt);
                    if (go) {
                        selectionModel.setSelectedIndex (idx);
                    }
                }
            } else {
                boolean should = shouldPerformAction (command, idx, evt) && displayer.isShowCloseButton();
                if (should) {
                    if (TabDisplayer.COMMAND_CLOSE == command) {
                        displayer.getModel().removeTab(idx);
                    } else if (TabDisplayer.COMMAND_CLOSE_ALL == command) {
                        displayer.getModel().removeTabs (0, displayer.getModel().size());
                    } else if (TabDisplayer.COMMAND_CLOSE_ALL_BUT_THIS == command) {
                        int start;
                        int end;
                        if (idx != displayer.getModel().size()-1) {
                            start = idx+1;
                            end = displayer.getModel().size();
                            displayer.getModel().removeTabs(start, end);
                        }
                        if (idx != 0) {
                            start = 0;
                            end = idx;
                            displayer.getModel().removeTabs(start, end);
                        }
                    }
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            int idx = updateMouseLocation(e);
            if (idx != -1) {
                TabCellRenderer tcr = getTabCellRenderer(idx);
                getTabRect(idx, scratch);
                int state = tabState.getState(idx);
                if ((state & TabState.PRESSED) != 0 && ((state & TabState.CLIP_LEFT) != 0) || (state & TabState.CLIP_RIGHT) != 0) {
                    makeTabVisible(idx);
                }
                potentialCommand (idx, e, state, tcr, scratch);
            }
            tabState.setMouseInTabsArea(idx != -1);
            tabState.setPressed(-1);
            tabState.setMousePressedInCloseButton(-1);
        }

        public final void mouseWheelMoved(MouseWheelEvent e) {
            updateMouseLocation(e);
            processMouseWheelEvent(e);
        }
    }

    /** A simple selection listener implementation which updates the TabState model
     * with the new selected index from the selection model when it changes.
     */
    protected class BasicSelectionListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            assert e.getSource() == selectionModel : "Unknown event source: "
                    + e.getSource();
            int idx = selectionModel.getSelectedIndex();
            tabState.setSelected(idx >= 0 ? idx : -1);
            if (idx >= 0) {
                makeTabVisible (selectionModel.getSelectedIndex());
            }
        }
    }
    
    /**
     * Listener on data model which will pass modified indices to the
     * TabState object, so it can update which tab indices are flashing in
     * "attention" mode, if any.
     */
    protected class BasicModelListener extends ModelListener {
        public void contentsChanged(ListDataEvent e) {
            super.contentsChanged(e);
            tabState.contentsChanged(e);
        }

        public void indicesAdded(ComplexListDataEvent e) {
            super.indicesAdded(e);
            tabState.indicesAdded(e);
        }

        public void indicesChanged(ComplexListDataEvent e) {
            tabState.indicesChanged(e);
        }

        public void indicesRemoved(ComplexListDataEvent e) {
            tabState.indicesRemoved(e);
        }

        public void intervalAdded(ListDataEvent e) {
            tabState.intervalAdded(e);
        }

        public void intervalRemoved(ListDataEvent e) {
            tabState.intervalRemoved(e);
        }
    }    
}
