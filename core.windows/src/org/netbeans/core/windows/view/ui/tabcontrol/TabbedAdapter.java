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

package org.netbeans.core.windows.view.ui.tabcontrol;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.Tabbed;
import org.netbeans.swing.tabcontrol.ComponentConverter;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.plaf.EqualPolygon;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.openide.ErrorManager;
import java.awt.Image;
import org.netbeans.core.windows.view.ui.slides.SlideController;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import org.netbeans.core.windows.actions.ActionUtils;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.LocationInformer;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;

/** Adapter class that implements a pseudo JTabbedPane API on top
 * of the new tab control.  This class should eventually be eliminated
 * and the TabbedContainer's model-driven API should be used directly.
 *
 * @author  Tim Boudreau
 */
public class TabbedAdapter extends TabbedContainer implements Tabbed, Tabbed.Accessor, SlideController {
    
    public static final int DOCUMENT = 1;
    
    /** Utility field holding list of ChangeListeners. */
    private transient java.util.ArrayList changeListenerList;
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(TabbedAdapter.class);
    private ChangeEvent changeEvent = new ChangeEvent(this);

    /** Creates a new instance of TabbedAdapter */
    public TabbedAdapter (int type) {
        super (null, type, new LocInfo());
        getSelectionModel().addChangeListener(new ChangeListener() {
            public void stateChanged (ChangeEvent ce) {
                int idx = getSelectionModel().getSelectedIndex();
                if (idx != -1) {
                    fireStateChanged();
                }
            }
        });
    }
    
    public void addTopComponent(String name, javax.swing.Icon icon, TopComponent tc, String toolTip) {
        insertComponent (name, icon, tc, toolTip, getTabCount());
    }
    
    public TopComponent getTopComponentAt(int index) {
        if (index == -1) {
            return null;
        }
        return (TopComponent)getModel().getTab(index).getComponent();
    }
    
    public TopComponent getSelectedTopComponent() {
        int i = getSelectionModel().getSelectedIndex();
        return i == -1 ? null : getTopComponentAt(i);
    }

    public void insertComponent(String name, javax.swing.Icon icon, Component comp, String toolTip, int position) {
        TabData td = new TabData (comp, icon, name, toolTip);
        
        if(DEBUG) {
            debugLog("InsertTab: " + name + " hash:" + System.identityHashCode(comp)); // NOI18N
        }
        
        getModel().addTab(position, td);
    }

    public void setSelectedComponent(Component comp) {
        int i = indexOf (comp);
        if (i == -1) {
            throw new IllegalArgumentException (
                "Component not a child of this control: " + comp); //NOI18N
        } else {
            getSelectionModel().setSelectedIndex(i);
        }
    }
    
    public TopComponent[] getTopComponents() {
        ComponentConverter cc = getComponentConverter();
        TabData[] td = (TabData[]) getModel().getTabs().toArray(new TabData[0]);
        TopComponent[] result = new TopComponent[getModel().size()];
        for (int i=0; i < td.length; i++) {
            result[i] = (TopComponent) cc.getComponent(td[i]);
        }
        return result;
    }
    
    public void removeComponent(Component comp) {
        int i=indexOf(comp);
        getModel().removeTab(i);
        if (getModel().size() == 0) {
            revalidate();
            repaint();
        }
    }

    public void addComponents(Component[] comps, String[] names, javax.swing.Icon[] icons, String[] tips) {
        ArrayList al = new ArrayList (comps.length);
        TabData[] data = new TabData[comps.length];
        for (int i=0; i < comps.length; i++) {
            TabData td = new TabData (comps[i], icons[i], names[i], tips[i]);
            data[i] = td;
        }
        getModel().addTabs (0, data);
    }

    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        assert selected != null : "Null passed as component to select";
        int sizeBefore = getModel().size();
        
        TabData[] data = new TabData[tcs.length];
        int toSelect=-1;
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            Image icon = tc.getIcon();
            String displayName = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
            data[i] = new TabData(
                tc,
                icon == null ? null : new ImageIcon(icon),
                displayName == null ? "" : displayName, // NOI18N
                tc.getToolTipText());
            if (selected == tcs[i]) {
                toSelect = i;
            }
        }

        //DO NOT DELETE THIS ASSERTION AGAIN!
        //If it triggered, it means there is a problem in the state of the
        //window system's model.  If it is just diagnostic logging, there
        //*will* be an exception later, it just won't contain any useful
        //information. See issue 39914 for what happens if it is deleted.
        assert toSelect != -1 : "Tried to set a selected component that was " +
            " not in the array of open components. ToSelect: " + selected + 
            " open components: " + Arrays.asList(tcs);
        
        getModel().setTabs(data);
        
        if (toSelect != -1) {
            getSelectionModel().setSelectedIndex(toSelect);
        } else {
            //Assertions are off
            ErrorManager.getDefault().log (ErrorManager.WARNING, "Tried to" +
            "set a selected component that was not in the array of open " +
            "components.  ToSelect: " + selected + " components: " + 
            Arrays.asList(tcs));
        }
        int sizeNow = getModel().size();
        if (sizeBefore != 0 && sizeNow == 0) {
            //issue 40076, ensure repaint if the control has been emptied.
            revalidate();
            repaint();
        }
    }
    
    // DnD>>
    /** Finds tab which contains x coordinate of given location point.
     * @param location The point for which a constraint is required
     * @return Integer object representing found tab index. Returns null if
     * no such tab can be found.
     */
    public Object getConstraintForLocation(Point location, boolean attachingPossible) {
        if(attachingPossible) {
            String s = getSideForLocation(location);
            if(s != null) {
                return s;
            }
        }
        int index = dropIndexOfPoint(location);
        return index < 0 ? null : new Integer(index);
    }

    /** Computes and returns feedback indication shape for given location
     * point.
     * TBD - extend for various feedback types
     * @return Shape representing feedback indication
     */
    public Shape getIndicationForLocation(Point location,
        TopComponent startingTransfer, Point startingPoint, boolean attachingPossible) {

        Rectangle rect = getBounds();
        rect.setLocation(0, 0);
        
        String side;
        if(attachingPossible) {
            side = getSideForLocation(location);
        } else {
            side = null;
        }
        
        double ratio = Constants.DROP_TO_SIDE_RATIO;
        if(side == Constants.TOP) {
            return new Rectangle(0, 0, rect.width, (int)(rect.height * ratio));
        } else if(side == Constants.LEFT) {
            return new Rectangle(0, 0, (int)(rect.width * ratio), rect.height);
        } else if(side == Constants.RIGHT) {
            return new Rectangle(rect.width - (int)(rect.width * ratio), 0, (int)(rect.width * ratio), rect.height);
        } else if(side == Constants.BOTTOM) {
            return new Rectangle(0, rect.height - (int)(rect.height * ratio), rect.width, (int)(rect.height * ratio));
        }

        Shape s = getDropIndication(startingTransfer, location);
        if(s != null) {
            return s;
        }
        
        if(startingPoint != null
            && indexOf(startingTransfer) != -1) {
            return getStartingIndication(startingPoint, location);
        }
        
        return rect;
    }

    private String getSideForLocation(Point location) {
        Rectangle bounds = getBounds();
        bounds.setLocation(0, 0);
        
        final int TOP_HEIGHT = 10;
        final int BOTTOM_HEIGHT = (int)(0.25 * bounds.height);
        
        final int LEFT_WIDTH = Math.max (getWidth() / 8, 40);
        final int RIGHT_WIDTH = LEFT_WIDTH;
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("TOP_HEIGHT    =" + TOP_HEIGHT); // NOI18N
            debugLog("BOTTOM_HEIGHT =" + BOTTOM_HEIGHT); // NOI18N
            debugLog("LEFT_WIDTH    =" + LEFT_WIDTH); // NOI18N
            debugLog("RIGHT_WIDTH   =" + RIGHT_WIDTH); // NOI18N
        }
        
        // Size of area which indicates creation of new split.
//        int delta = Constants.DROP_AREA_SIZE;
        
        Rectangle top = new Rectangle(0, 0, bounds.width, TOP_HEIGHT);
        if(top.contains(location)) {
            return Constants.TOP;
        }
        
        Polygon left = new EqualPolygon(
            new int[] {0, LEFT_WIDTH, LEFT_WIDTH, 0},
            new int[] {TOP_HEIGHT, TOP_HEIGHT, bounds.height - BOTTOM_HEIGHT, bounds.height},
            4
        );
        if(left.contains(location)) {
            return Constants.LEFT;
        }
        
        Polygon right = new EqualPolygon(
            new int[] {bounds.width - RIGHT_WIDTH, bounds.width, bounds.width, bounds.width - RIGHT_WIDTH},
            new int[] {TOP_HEIGHT, TOP_HEIGHT, bounds.height, bounds.height - BOTTOM_HEIGHT},
            4
        );
        if(right.contains(location)) {
            return Constants.RIGHT;
        }

        Polygon bottom = new EqualPolygon(
            new int[] {LEFT_WIDTH, bounds.width - RIGHT_WIDTH, bounds.width, 0},
            new int[] {bounds.height - BOTTOM_HEIGHT, bounds.height - BOTTOM_HEIGHT, bounds.height, bounds.height},
            4
        );
        if(bottom.contains(location)) {
            return Constants.BOTTOM;
        }
            
        return null;
    }

    private Shape getStartingIndication(Point startingPoint, Point location) {
        Rectangle rect = getBounds();
        rect.setLocation(location.x - startingPoint.x, location.y - startingPoint.y);
        return rect;
    }
    // DnD<<

    
    /** Registers ChangeListener to receive events.
     * @param listener The listener to register.
     *
     */
    public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
        if (changeListenerList == null ) {
            changeListenerList = new java.util.ArrayList();
        }
        changeListenerList.add(listener);
    }    
    
    /** Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removeChangeListener(javax.swing.event.ChangeListener listener) {
        if (changeListenerList != null ) {
            changeListenerList.remove(listener);
        }
    }
    
    /** Notifies all registered listeners about the event. */
    private void fireStateChanged() {
        java.util.ArrayList list;
        synchronized (this) {
            if (changeListenerList == null) return;
            list = (java.util.ArrayList)changeListenerList.clone();
        }
        //Note: Firing the events while holding the tree lock avoids many
        //gratuitous repaints that slow down switching tabs.  To demonstrate this,
        //comment this code out and run the IDE with -J-Dawt.nativeDoubleBuffering=true
        //so you'll really see every repaint.  When switching between a form
        //tab and an editor tab, you will see the property sheet get repainted
        //8 times due to changes in the component hierarchy, before the 
        //selected node is even changed to the appropriate one for the new tab.
        //Synchronizing here ensures that never happens.
        
        if (!SwingUtilities.isEventDispatchThread()) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                "All state changes to the tab component must happen on the event thread!"); //NOI18N
            Exception e = new Exception();
            e.fillInStackTrace();
            System.err.println(e.getStackTrace()[1]);
        }
        
        synchronized (getTreeLock()) {
            for (int i = 0; i < list.size(); i++) {
                ((javax.swing.event.ChangeListener)list.get(i)).stateChanged(changeEvent);
            }
        }
    }
    
    private static void debugLog(String message) {
        Debug.log(TabbedAdapter.class, message);
    }
    
    public Image createImageOfTab(int tabIndex) {
        // XXX -TBD
        return null;
    }
    
    public Component getComponent() {
        return this;
    }
    
    /** Add action for enabling slide */
    public Action[] getPopupActions(Action[] defaultActions, int tabIndex) {
        // no auto hide for editor types
        if (TabbedContainer.TYPE_EDITOR == getType()) {
            return defaultActions;
        }
        Action[] result = new Action[defaultActions.length + 1];
        System.arraycopy(defaultActions, 0, result, 0, defaultActions.length);
        result[defaultActions.length] = 
            new ActionUtils.AutoHideWindowAction(this, tabIndex, false);
        return result;
    }
    
    /********** implementation of SlideController *****************/
    
    public void userToggledAutoHide(int tabIndex, boolean enabled) {
        postActionEvent(new TabActionEvent(this, TabbedContainer.COMMAND_ENABLE_AUTO_HIDE, tabIndex));
    }    
    
    /********* implementation of Tabbed.Accessor **************/
    
    public Tabbed getTabbed() {
        return this;
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        return getTabRect(tabIndex, new Rectangle());
    }
    
    /********* implementation of LocationInformer ********/
    
    static class LocInfo implements LocationInformer {
    
        public Object getOrientation(Component comp) {
            String side = ((WindowManagerImpl)WindowManagerImpl.getDefault()).guessSlideSide((TopComponent)comp);
            Object result = null;
            if (side.equals(Constants.LEFT)) {
                result = TabDisplayer.ORIENTATION_WEST;
            } else if (side.equals(Constants.RIGHT)) {
                result = TabDisplayer.ORIENTATION_EAST;
            } else if (side.equals(Constants.BOTTOM)) {
                result = TabDisplayer.ORIENTATION_SOUTH;
            } else {
                result = TabDisplayer.ORIENTATION_CENTER;
            }
            return result;   
        }    
    } // end of LocInfo

    
}
