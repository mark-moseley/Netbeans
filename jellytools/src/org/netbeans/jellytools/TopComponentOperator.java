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
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Point;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.core.windows.view.ui.tabcontrol.TabLayoutModel;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jellytools.actions.CloneViewAction;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/** Represents org.openide.windows.TopComponent. It is IDE wrapper for a lot of
 * panels in IDE. TopComponent is for example Filesystems panel, every editor 
 * panel or execution panel. TopComponent can be located by TopComponentOperator 
 * anywhere inside IDE, if it is opened. It is by default activated which means
 * it is put to foreground if there exist more top components in a split area.
 * TopComponent can also be located explicitly inside some Container.
 *
 * <p>
 * Usage:<br>
 * <pre>
 *      TopComponentOperator tco = new TopComponentOperator("Execution");
 *      tco.pushMenuOnTab("Maximize");
 *      tco.restore();
 *      tco.attachTo("Filesystems", AttachWindowAction.AS_LAST_TAB);
 *      tco.attachTo("Output", AttachWindowAction.RIGHT);
 *      tco.close();
 * </pre>
 * @author Adam.Sotona@sun.com
 * @author Jiri.Skrivanek@sun.com
 *
 * @see org.netbeans.jellytools.ations.AttachWindowAction
 * @see org.netbeans.jellytools.actions.CloneViewAction
 * @see org.netbeans.jellytools.actions.CloseAllDocumentsAction
 * @see org.netbeans.jellytools.actions.CloseViewAction
 * @see org.netbeans.jellytools.actions.MaximizeWindowAction
 * @see org.netbeans.jellytools.actions.RestoreWindowAction
 */
public class TopComponentOperator extends JComponentOperator {
    
    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
        // need to set timeout for the case it was not set previously
        JemmyProperties.getCurrentTimeouts().initDefault("EventDispatcher.RobotAutoDelay", 0);
        DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, 
        new MouseRobotDriver(JemmyProperties.getCurrentTimeouts().create("EventDispatcher.RobotAutoDelay"), 
                             new String[] {TopComponentOperator.class.getName()}));
    }
    
    /** Waits for index-th TopComponent with given name in specified container.
     * It is activated by default.
     * @param contOper container where to search
     * @param topComponentName name of TopComponent (it used to be label of tab)
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(ContainerOperator contOper, String topComponentName, int index) {
        super(waitTopComponent(contOper, topComponentName, index, null));
        copyEnvironment(contOper);
        makeComponentVisible();
    }
    
    /** Waits for TopComponent with given name in specified container.
     * It is activated by default.
     * @param contOper container where to search
     * @param topComponentName name of TopComponent (it used to be label of tab)
     */
    public TopComponentOperator(ContainerOperator contOper, String topComponentName) {
        this(contOper, topComponentName, 0);
    }
    
    /** Waits for index-th TopComponent in specified container.
     * It is activated by default.
     * @param contOper container where to search
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(ContainerOperator contOper, int index) {
        this(contOper, null, index);
    }
    
    /** Waits for first TopComponent in specified container.
     * It is activated by default.
     * @param contOper container where to search
     */
    public TopComponentOperator(ContainerOperator contOper) {
        this(contOper, null, 0);
    }
    
    /** Waits for index-th TopComponent with given name in whole IDE.
     * It is activated by default.
     * @param topComponentName name of TopComponent (it used to be label of tab)
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(String topComponentName, int index) {
        this(waitTopComponent(topComponentName, index));

    }
    
    /** Waits for first TopComponent with given name in whole IDE.
     * It is activated by default.
     * @param topComponentName name of TopComponent (it used to be label of tab)
     */
    public TopComponentOperator(String topComponentName) {
        this(topComponentName, 0);
    }
  
    /** Creates new instance from given TopComponent.
     * It is activated by default.
     * This constructor is used in properties.PropertySheetOperator.
     * @param jComponent instance of JComponent
     */
    public TopComponentOperator(JComponent jComponent) {
        super(jComponent);
        makeComponentVisible();
    }

    /** Makes active window in which this top component resides (main window
     * in joined mode) and then activates this top component to be in the
     * foreground.
     */    
    public void makeComponentVisible() {
        // Make active window in which this TopComponent resides. 
        // It is necessary e.g. for keyboard focus
        super.makeComponentVisible();
        //  Check if it is really TopComponent. It doesn't have to be 
        // for example for PropertySheetOperator in Options window.
        // In that case do nothing.
        if(getSource() instanceof TopComponent) {
            // activate TopComponent, i.e. switch tab control to be active.
            // run in dispatch thread
            runMapping(new MapVoidAction("close") {
                public void map() {
                    ((TopComponent)getSource()).requestActive();
                }
            });
        }
    }

    /** Attaches this top component to a new position defined by target top
     * component and side.
     * @param targetTopComponentName name of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link AttachWindowAction#LEFT}, 
     * {@link AttachWindowAction#RIGHT}, {@link AttachWindowAction#TOP}, 
     * {@link AttachWindowAction#BOTTOM}, {@link AttachWindowAction#AS_LAST_TAB})
     */
    public void attachTo(String targetTopComponentName, String side) {
        new AttachWindowAction(targetTopComponentName, side).perform(this);
    }

    /** Attaches this top component to a new position defined by target top
     * component and side.
     * @param targetTopComponentOperator operator of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link AttachWindowAction#LEFT}, 
     * {@link AttachWindowAction#RIGHT}, {@link AttachWindowAction#TOP}, 
     * {@link AttachWindowAction#BOTTOM}, {@link AttachWindowAction#AS_LAST_TAB})
     */
    public void attachTo(TopComponentOperator targetTopComponentOperator, String side) {
        new AttachWindowAction(targetTopComponentOperator, side).perform(this);
    }
    
    /** Maximizes this top component. */
    public void maximize() {
        new MaximizeWindowAction().perform(this);
    }

    /** Restores maximized window. */
    public void restore() {
        new RestoreWindowAction().perform(this);
    }
    
    /** Clones this TopComponent. TopComponent is activated before
     * action is performed. */
    public void cloneDocument() {
        new CloneViewAction().perform(this);
    }
    
    /** Closes this TopComponent and wait until it is closed. 
     * TopComponent is activated before action is performed. */
    public void closeWindow() {
        new CloseViewAction().perform(this);
        waitComponentShowing(false);
    }

    /** Closes this TopComponent instance by IDE API call and wait until
     * it is not closed. If this TopComponent is modified (e.g. editor top
     * component), it discards possible changes.
     * @see #close
     */
    public void closeDiscard() {
        setUnmodified();
        close();
    }
    
    /** Finds DataObject for the content of this TopComponent and set it
     * unmodified. Used in closeDiscard method.
     */
    private void setUnmodified() {
        // should be just one node
        org.openide.nodes.Node[] nodes = ((TopComponent)getSource()).getActivatedNodes();
        // TopComponent like Execution doesn't have any nodes associated
        if(nodes != null) {
            for(int i=0;i<nodes.length;i++) {
                DataObject dob = (DataObject)nodes[i].getCookie(DataObject.class);
                dob.setModified(false);
            }
        }
    }

    /** Saves content of this TopComponent. If it is not applicable or content 
     * of TopComponent is not modified, it does nothing.
     */
    public void save() {
        // should be just one node
        org.openide.nodes.Node[] nodes = ((TopComponent)getSource()).getActivatedNodes();
        // TopComponent like Execution doesn't have any nodes associated
        if(nodes != null) {
            for(int i=0;i<nodes.length;i++) {
                SaveCookie sc = (SaveCookie)nodes[i].getCookie(SaveCookie.class);
                if(sc != null) {
                    try {
                        sc.save();
                    } catch (IOException e) {
                        throw new JemmyException("Exception while saving this TopComponent.", e);
                    }
                }
            }
        }
    }
    
    /** Closes this TopComponent instance by IDE API call and wait until 
     * it is not closed. If this TopComponent is modified (e.g. editor top
     * component), question dialog is shown and you have to close it. To close 
     * this TopComponent and discard possible changes use {@link #closeDiscard}
     * method.
     */
    public void close() {
        // run in a new thread because question may block further execution
        new Thread(new Runnable() {
            public void run() {
                // run in dispatch thread
                runMapping(new MapVoidAction("close") {
                    public void map() {
                        ((TopComponent)getSource()).close();
                    }
                });
            }
        }, "thread to close TopComponent").start();
        // wait to be away
        waitComponentShowing(false);
    }

    /** Closes all opened documents and waits until this top component is closed. */
    public void closeAllDocuments() {
        new CloseAllDocumentsAction().perform(this);
        waitComponentShowing(false);
    }

    /** Saves this document by popup menu on tab. */
    public void saveDocument() {
        // Save Document
        String saveItem = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                      "LBL_SaveDocumentAction");
        pushMenuOnTab(saveItem);
    }

    /** Finds index-th TopComponent with given name in whole IDE.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @return TopComponent instance or null if noone matching criteria was found
     */
    public static JComponent findTopComponent(String name, int index) {
        return findTopComponent(null, name,  index, null);
    }

    /** Finds index-th TopComponent with given name in IDE registry.
     * @param cont container where to search
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @param subchooser ComponentChooser to determine exact TopComponent
     * @return TopComponent instance or null if noone matching criteria was found
     */
    protected static JComponent findTopComponent(ContainerOperator cont, String name, int index, ComponentChooser subchooser) {
        Object tc[]=TopComponent.getRegistry().getOpened().toArray();
        StringComparator comparator=cont==null?Operator.getDefaultStringComparator():cont.getComparator();
        TopComponent c;
        for (int i=0; i<tc.length; i++) {
            c=(TopComponent)tc[i];
            if (c.isShowing() && comparator.equals(c.getName(), name) && isUnder(cont, c) && (subchooser==null || subchooser.checkComponent(c))) {
                index--;
                if (index<0)
                    return c;
            }
        }
        for (int i=0; i<tc.length; i++) {
            c=(TopComponent)tc[i];
            if ((!c.isShowing()) && isParentShowing(c) && comparator.equals(c.getName(), name) && isUnder(cont, c) && (subchooser==null || subchooser.checkComponent(c))) {
                index--;
                if (index<0)
                    return c;
            }
        }
        return null;
    }

    private static boolean isParentShowing(Component c) {
        while (c!=null) {
            if (c.isShowing()) return true;
            c=c.getParent();
        }
        return false;
    }
    
    private static boolean isUnder(ContainerOperator cont, Component c) {
        if (cont==null) return true;
        Component comp=cont.getSource();
        while (comp!=c && c!=null) c=c.getParent();
        return (comp==c);
    }
    
    /** Waits for index-th TopComponent with given name in IDE registry.
     * It throws JemmyException when TopComponent is not find until timeout
     * expires.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @return TopComponent instance or throws JemmyException if not found
     * @see #findTopComponent
     */
    protected static JComponent waitTopComponent(final String name, final int index) {
        return waitTopComponent(null, name, index, null);
    }
    
    /** Waits for index-th TopComponent with given name in IDE registry.
     * It throws JemmyException when TopComponent is not find until timeout
     * expires.
     * @param cont container where to search
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @param subchooser ComponentChooser to determine exact TopComponent
     * @return TopComponent instance or throws JemmyException if not found
     * @see #findTopComponent
     */
    protected static JComponent waitTopComponent(final ContainerOperator cont, final String name, final int index, final ComponentChooser subchooser) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    return findTopComponent(cont, name, index, subchooser);
                }
                public String getDescription() {
                    return("Wait TopComponent with name="+name+
                           " index="+String.valueOf(index)+
                           (subchooser == null ? "" : " subchooser="+subchooser.getDescription())+
                           " loaded");
                }
            });
            Timeouts times = JemmyProperties.getCurrentTimeouts().cloneThis();
            times.setTimeout("Waiter.WaitingTime", times.getTimeout("ComponentOperator.WaitComponentTimeout"));
            waiter.setTimeouts(times);
            waiter.setOutput(JemmyProperties.getCurrentOutput());
            return((JComponent)waiter.waitAction(null));
        } catch(InterruptedException e) {
            return(null);
        }
    }
    
    /** Makes top component active and pushes given menu on its tab.
     * @param popupPath menu path separated by '|' (e.g. "CVS|Refresh")
     */
    public void pushMenuOnTab(String popupPath) {
        this.makeComponentVisible();
        TabbedAdapter ta = findTabbedAdapter();
        int index = ta.indexOfTopComponent((TopComponent)getSource());
        JComponent tabsComp = ta.getTabsDisplayer().getComponent();
        TabLayoutModel mdl = ta.getTabsDisplayer().getTabsUI().getLayoutModel();
        
        Point p = tabsComp.getLocation();
        int x = mdl.getX(index) + p.x;
        int y = mdl.getY(index) + p.y;
        // TODO - remove 4 when 36190 fixed
        // need a constant to satisfy that we are in tab
        new JPopupMenuOperator(JPopupMenuOperator.callPopup(tabsComp, x+4, y)).pushMenu(popupPath);
    }
    
    /** Returns TabbedAdapter component from parents hierarchy. 
     * Used also in EditorWindowOperator.
     */
    TabbedAdapter findTabbedAdapter() {
        Component parent = getSource().getParent();
        while(parent != null) {
            if(parent instanceof TabbedAdapter) {
                return (TabbedAdapter)parent;
            } else {
                parent = parent.getParent();
            }
        }
        return null;
    }
}
