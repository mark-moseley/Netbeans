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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.view.ui.Tabbed;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.LocationInformer;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.windows.TopComponent;

/*
 * Swing component of slide bar. 
 * Holds and shows set of toggle slide buttons and synchronizes them with 
 * data model.
 *
 * All data manipulation are done indirectly through ascoiated models,
 * Swing AWT hierarchy is just synchronized.
 *
 * @author Dafe Simonek
 */
public final class SlideBar extends Box implements ComplexListDataListener,
    SlideBarController, Tabbed.Accessor, LocationInformer, ChangeListener {
    
    /** Command indicating request for slide in (appear) of sliding component */
    public static final String COMMAND_SLIDE_IN = "slideIn"; //NOI18N
    
    /** Command indicating request for slide out (hide) of sliding component */
    public static final String COMMAND_SLIDE_OUT = "slideOut"; //NOI18N

    /** Action command indicating that a popup menu should be shown */
    public static final String COMMAND_POPUP_REQUEST = "popup"; //NOI18N

    /** Action command indicating that component is going from auto-hide state to regular */
    public static final String COMMAND_DISABLE_AUTO_HIDE = "disableAutoHide"; //NOI18N
    
    /** Asociation with Tabbed implementation */
    private final TabbedSlideAdapter tabbed;
    /** Holds all data of slide bar */
    private final SlideBarDataModel dataModel;
    /** Selection info */
    private final SingleSelectionModel selModel;
    /** listener for mouse actions and moves, which trigger slide operations */
    private SlideGestureRecognizer gestureRecognizer;
    /** list of sliding buttons */
    private List buttons;
    /** operation handler */
    private CommandManager commandMgr;
    
    /** Creates a new instance of SlideBarContainer with specified orientation.
     * See SlideBarDataModel for possible orientation values.
     */
    public SlideBar(TabbedSlideAdapter tabbed, SlideBarDataModel dataModel, SingleSelectionModel selModel) {
        super(dataModel.getOrientation() == SlideBarDataModel.SOUTH
                ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS);
        this.tabbed = tabbed;                
        this.dataModel = dataModel;
        this.selModel = selModel;
        gestureRecognizer = new SlideGestureRecognizer(this);
        commandMgr = new CommandManager(this);
        buttons = new ArrayList(5);
        
        setBorder(computeBorder(dataModel.getOrientation()));
        syncWithModel();
        
        dataModel.addComplexListDataListener(this);
        selModel.addChangeListener(this);
    }
    
    
    public SlideBarDataModel getModel() {
        return dataModel;
    }
    
    public SingleSelectionModel getSelectionModel () {
        return selModel;
    }
    
    /***** reactions to changes in data model, synchronizes AWT hierarchy and display ***/
    
    public void intervalAdded(ListDataEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        int first = e.getIndex0();
        int last = e.getIndex1();
        SlideBarDataModel data = (SlideBarDataModel)e.getSource();
        SlidingButton curButton;
        for (int i = first; i <= last; i++) {
            curButton = new SlidingButton(this, data.getTab(i), data.getOrientation());
            attachListeners(curButton);
            buttons.add(i, curButton);
            add(curButton, i * 2);
            add(createStrut(), i * 2 + 1);
        }
    }
    
    public void intervalRemoved(ListDataEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        int first = e.getIndex0();
        int last = e.getIndex1();
        SlideBarDataModel data = (SlideBarDataModel)e.getSource();
        SlidingButton curButton = null;
        for (int i = last; i >= first; i--) {
            detachListeners((SlidingButton)buttons.get(i));
            buttons.remove(i);
            // have to remove also strut (space) component
            remove(i * 2 + 1);
            remove(i * 2);
        }
    }
    
    public void contentsChanged(ListDataEvent e) {
        syncWithModel();
    }
    
    public void indicesAdded(ComplexListDataEvent e) {
        // XXX - TBD - change impl to create only changed items
        syncWithModel();
    }
    
    public void indicesChanged(ComplexListDataEvent e) {
        // XXX - TBD - change impl to create only changed items
        syncWithModel();
    }
    
    public void indicesRemoved(ComplexListDataEvent e) {
        // XXX - TBD - change impl to create only changed items
        syncWithModel();
    }

    /** Finds button which contains given point and returns button's index
     * valid in asociated dataModel. Or returns -1 if no button contains
     * given point
     */  
    public int tabForCoordinate(int x, int y) {
        Rectangle curBounds = new Rectangle();
        int index = 0;
        for (Iterator iter = buttons.iterator(); iter.hasNext(); index++) {
            ((Component)iter.next()).getBounds(curBounds);
            if (curBounds.contains(x, y)) {
                return index;
            }
        }
        return -1;
    }
    
    private Component createStrut () {
        return dataModel.getOrientation() == SlideBarDataModel.SOUTH
            ? createHorizontalStrut(5) : createVerticalStrut(5);
    }
    
    private void syncWithModel () {
        assert SwingUtilities.isEventDispatchThread();
        
        for (Iterator iter = buttons.iterator(); iter.hasNext(); ) {
            detachListeners((SlidingButton)iter.next());
        }
        removeAll();
        buttons.clear();
        
        List dataList = dataModel.getTabs();
        SlidingButton curButton;
        for (Iterator iter = dataList.iterator(); iter.hasNext(); ) {
            curButton = new SlidingButton(this, (TabData)iter.next(), dataModel.getOrientation());
            attachListeners(curButton);
            buttons.add(curButton);
            add(curButton);
            add(createStrut());
        }

        commandMgr.syncWithModel();
    }
    
    /** Builds empty border around slide bar. Computes its correct size
     * based on given orientation
     */
    private static Border computeBorder(int orientation) {
        int bottom = 0, left = 0, right = 0, top = 0;
        switch (orientation) {
            case SlideBarDataModel.WEST:
                top = 10; left = 1; bottom = 5; right = 1; 
                break;
            case SlideBarDataModel.SOUTH:
                // XXX - left and right should be width of appropriate slidebar + 5
                top = 1; left = 25; bottom = 2; right = 25; 
                break;
            case SlideBarDataModel.EAST:
                top = 10; left = 1; bottom = 5; right = 1; 
                break;
        }
        return new EmptyBorder(top, left, bottom, right);
    }
    
    /** Implementation of ChangeListener, reacts to selection changes
     * and assures that currently selected component is slided in
     */
    public void stateChanged(ChangeEvent e) {
        int selIndex = selModel.getSelectedIndex();
        
        // notify winsys about selection change
        tabbed.postSelectionEvent();
        // a check to prevent NPE as described in #43605, dafe - is this correct or rather a hack? mkleint
        if (isDisplayable() && isVisible()) {
            // slide in or out
            if (selIndex != -1) {
                commandMgr.slideIn(selIndex);
            } else {
                commandMgr.slideOut(false);
            }
        }
    }
    
    
    /********** implementation of SlideBarController *****************/
    
    public void userToggledAutoHide(int tabIndex, boolean enabled) {
        commandMgr.slideIntoDesktop(tabIndex);
    }
    
    public void userTriggeredPopup(MouseEvent mouseEvent, Component clickedButton) {
        int index = getButtonIndex(clickedButton);
        commandMgr.showPopup(mouseEvent, index);
    }

    /** Triggers slide operation by changing selected index */
    public void userClickedSlidingButton(Component clickedButton) {
        int index = getButtonIndex(clickedButton);
        
        if (index != selModel.getSelectedIndex()) {
            TopComponent tc = (TopComponent)dataModel.getTab(index).getComponent();
            if (tc != null) {
                tc.requestActive();
            }
        } else {
            selModel.setSelectedIndex(-1);
        }
    }
    
    void setActive(boolean active) {
        commandMgr.setActive(active);
    }
    
    private void attachListeners (SlidingButton button) {
        button.addActionListener(gestureRecognizer);
        button.addMouseListener(gestureRecognizer);
    }
    
    private void detachListeners (SlidingButton button) {
        button.removeActionListener(gestureRecognizer);
        button.removeMouseListener(gestureRecognizer);
    }
    
    private int getButtonIndex(Component button) {
        return buttons.indexOf(button);
    }
    
    SlidingButton getButton(int index) {
        return (SlidingButton)buttons.get(index);
    }
    
    /** @return true if slide bar contains given component, false otherwise */
    boolean containsComp(Component comp) {
        List tabs = getModel().getTabs();
        TabData curTab = null;
        for (Iterator iter = tabs.iterator(); iter.hasNext(); ) {
            curTab = (TabData)iter.next();
            if (comp.equals(curTab.getComponent())) {
                return true;
            }
        }
        return false;
    }
    
    
    /********* implementation of Tabbed.Accessor **************/
    
    public Tabbed getTabbed () {
        return tabbed;
    }
    
    /********* implementation of LocationInformer **************/
    
    public Object getOrientation(Component comp) {
        return TabDisplayer.ORIENTATION_CENTER;
    }
    
}


    /********* Swing standard handling mechanism for asociated UI class - will
      see if we need our own UI class or not */
    
    /** String ID of UI class for slide bar used in UIManager */ 
    //private static final String uiClassID = "SlideBarUI";

    /**
     * Returns the tool bar's current UI.
     * @see #setUI
     */
    /*public SlideBarUI getUI() {
        return (SlideBarUI)ui;
    }*
    
    /**
     * Sets the L&F object that renders this component.
     */
    /*public void setUI(SlideBarUI ui) {
        super.setUI(ui);
    }*/
    
    /**
     * Notification from the <code>UIFactory</code> that the L&F has changed. 
     * Called to replace the UI with the latest version from the 
     * <code>UIFactory</code>.
     *
     * @see JComponent#updateUI
     */
    /*public void updateUI() {
        setUI((SlideBarUI)UIManager.getUI(this));
        invalidate();
    }*/

    /**
     * Returns the name of the L&F class that renders this component.
     */
    /*public String getUIClassID() {
        return uiClassID;
    }*/
