/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.awt.Toolbar;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.WindowManager;
import org.netbeans.core.awt.TabControl;
import org.netbeans.core.windows.UIModeManager;
import org.netbeans.core.windows.WindowManagerImpl;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Handle NetBeans main window. It manipulates with toolbars and workspaces.
 * You can get text from status bar as well. To invoke menu items use
 * {@link org.netbeans.jellytools.actions actions}.
 * <br>
 * It is singleton, so to get instance use static method <code>getDefault()</code>.
 * <p>
 * Usage:<br>
 * <pre>
 *      MainWindowOperator mainWindow = MainWindowOperator.getDefault();
 *      mainWindow.waitStatusText("Finished");
 *      System.out.println("STATUS="+mainWindow.getStatusText());
 *      mainWindow.setSDI();
 *      mainWindow.switchToGUIEditingWorkspace();
 *      // push "Open" toolbar button in "System" toolbar
 *      mainWindow.getToolbarButton(mainWindow.getToolbar("System"), "Open").push();
 *      Thread.sleep(2000);
 *      new NbDialogOperator("Open").close();
 *      // invoke "Compile" menu item under "Build" menu
 *      new CompileAction().performMenu();
 *      // invoke About menu item under "Help" menu
 *      new ActionNoBlock("Help|About", null).perform();
 *      Thread.sleep(2000);
 *      new NbDialogOperator("About").close();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class MainWindowOperator extends JFrameOperator {
    
    /** Singleton instance of MainWindowOperator. */
    private static MainWindowOperator defaultMainWindowOperator;
    /** JMenuBarOperator instance. */
    private JMenuBarOperator _menuBar;
    /** Special tab component for workspace switching */
    private Component _tabControl;
    /** Instance of StatusTextTracer for this MainWindowOperator instance */
    private static StatusTextTracer statusTextTracer = null;
    
    /** Creates new instance of MainWindowOperator. Waits for JFrame with
     * title "NetBeans IDE..." or "Forte...".
     */
    private MainWindowOperator() {
        super(Bundle.getString("org.netbeans.core.windows.Bundle",
                               "CTL_MainWindow_Title",
                               new String[] {System.getProperty("netbeans.buildnumber")}));
    }
    
    /** Returns instance of MainWindowOperator. It is singleton, so this method
     * is only way how to obtain instance. If instance not exists, it creates one.
     * @return instance of MainWindowOperator
     */
    public static synchronized MainWindowOperator getDefault() {
        if(defaultMainWindowOperator == null) {
            defaultMainWindowOperator = new MainWindowOperator();
        }
        return defaultMainWindowOperator;
    }
    
    /** Returns operator of main menu bar.
     * @return  JMenuBarOperator instance of main menu
     */
    public JMenuBarOperator menuBar() {
        if(_menuBar == null) {
            _menuBar = new JMenuBarOperator(this);
        }
        return _menuBar;
    }
    
    /** Checks whether NetBeans are in MDI (full screen) or
     * SDI (multiple smaller windows) mode.
     * @return  true if IDE is in MDI mode; false otherwise (SDI mode)
     */
    public static boolean isMDI() {
        return UIModeManager.getDefault().getUIMode() == UIModeManager.MDI_MODE;
    }
    
    /** Makes IDE to switch to MDI (full screen) mode. */
    public static void setMDI() {
        UIModeManager.getDefault().setUIMode(UIModeManager.MDI_MODE);
        new EventTool().waitNoEvent(1000);
    }
    
    /** Makes IDE to switch to SDI (multiple smaller windows) mode. */
    public static void setSDI() {
        UIModeManager.getDefault().setUIMode(UIModeManager.SDI_MODE);
        new EventTool().waitNoEvent(1000);
    }
    
    /** Returns text from status bar.
     * @return  currently displayed text
     */
    public String getStatusText() {
        return org.openide.awt.StatusDisplayer.getDefault().getStatusText();
    }
    
    /** Returns singleton instance of StatusTextTracer.
     * @return singleton instance of StatusTextTracer
     */
    public synchronized StatusTextTracer getStatusTextTracer() {
        if(statusTextTracer == null) {
            statusTextTracer = new StatusTextTracer();
        }
        return statusTextTracer;
    }
    
    /** Waits until given text appears in the main window status bar.
     * If you want to trace status messages during an operation is proceed,
     * use {@link StatusTextTracer}.
     * @param text  a text to wait for
     */
    public void waitStatusText(final String text) {
        getStatusTextTracer().start();
        try {
            // not wait in case status text was already printed out
            if(!getComparator().equals(getStatusText(), text)) {
                getStatusTextTracer().waitText(text);
            }
        } finally {
            getStatusTextTracer().stop();
        }
    }
    
    /***************** methods for workspace manipulation *******************/
    
    /** Switches to specified workspace.
     * @param workspaceName name of workspace
     */
    public void switchToWorkspace(String workspaceName) {
        ((TabControl)getTabControl()).setSelectedIndex(getTabIndex(workspaceName));
        new EventTool().waitNoEvent(1000);
    }
    
    /** Switches to GUIEditing workspace. */
    public void switchToGUIEditingWorkspace() {
        String label = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                               "CTL_GuiEditingWorkspaceName");
        switchToWorkspace(label);
    }
    
    /** Switches to Debugging workspace. */
    public void switchToDebuggingWorkspace() {
        String label = Bundle.getString("org.netbeans.modules.debugger.resources.Bundle",
                                        "CTL_Debugging_workspace");
        switchToWorkspace(label);
    }
    
    /** Switches to Editing workspace. */
    public void switchToEditingWorkspace() {
        String label = Bundle.getStringTrimmed("org.netbeans.core.windows.Bundle",
                                               "CTL_Workspace_Editing");
        switchToWorkspace(label);
    }
    
    /** Returns instance of special tab component for workspace switching.
     * First time finds TabControl component in MainFrame, next time
     * returns already found instance.
     * @return instance of org.netbeans.core.awt.TabControl
     */
    public Component getTabControl() {
        if(_tabControl == null) {
            ComponentChooser chooser = new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return (comp instanceof org.netbeans.core.awt.TabControl);
                }
                public String getDescription() {
                    return "org.netbeans.core.awt.TabControl";
                }
            };
            _tabControl = findComponent((Container)this.getSource(), chooser);
            if(_tabControl == null) {
                throw new JemmyException("Component TabControl has not been found.");
            }
        }
        return _tabControl;
    }
    
    /** Returns number of workspaces present in IDE. Every workspace
     * is represented by tab in main window.
     * @return  number of workspaces
     */
    public int getTabCount() {
        return ((TabControl)getTabControl()).getTabCount();
    }
    
    /** Returns index of currently selected workspace.
     * @return  index of selected workspace (starts from 0)
     */
    public int getSelectedIndex() {
        return ((TabControl)getTabControl()).getSelectedIndex();
    }
    
    /** Returns index of workspace identified by its name
     * @param tabLabel name of workspace
     * @return  index of workspace (starts from 0)
     */
    public int getTabIndex(String tabLabel) {
        for(int i=0; i < getTabCount(); i++) {
            if (getComparator().equals(((TabControl)getTabControl()).getTabLabel(i), tabLabel)) {
                return i;
            }
        }
        throw new JemmyException("Tab with label \""+tabLabel+"\" cannot be found");
    }
    
    /** Clicks on tab in main window to switch workspace
     * @param index  index of workspace to switch to (starts from 0)
     */
    public void clickOnTab(int index) {
        new ComponentOperator(getTabControl()).clickMouse(getXForClick(index), 0, 1);
        new EventTool().waitNoEvent(1000);
    }
    
    /** Gets x-position of tab identified by its index.
     * @param index index of workspace (starts from 0)
     * @return x-position of desired tab
     */
    private int getXForClick(int index) {
        for(int x = 0; x< getTabControl().getWidth(); x++) {
            if (((TabControl)getTabControl()).pointToIndex(x) == index) return x;
        }
        throw new JemmyException("Point to click cannot be found for index="+index);
    }
    
    /** Pushes pop-up menu on a workspace tab specified by its index.
     * @param menuPath path of menu items (e.g. "Switch to Editing")
     * @param index index of tab
     */
    public void pushMenuOnTab(String menuPath, int index) {
        new ComponentOperator(getTabControl()).clickForPopup(getXForClick(index), 0);
        new JPopupMenuOperator().pushMenu(menuPath, "|");
    }
    
    /***************** methods for toolbars manipulation *******************/
    
    /** Returns ContainerOperator representing index-th floating toolbar in
     * IDE main window. Toolbars are NOT indexed from left to right.
     * @param index index of toolbar to find
     * @return ContainerOperator instance representing a toolbar
     */
    public ContainerOperator getToolbar(int index) {
        ComponentChooser chooser = new ToolbarChooser();
        return new ContainerOperator((Container)waitComponent((Container)getSource(),
                                     chooser, index));
    }
    
    /** Returns ContainerOperator representing floating toolbar with given name.
     * @param toolbarName toolbar's display name. It is shown in its tooltip.
     * @return  ContainerOperator instance representing a toolbar
     */
    public ContainerOperator getToolbar(String toolbarName) {
        ComponentChooser chooser = new ToolbarChooser(toolbarName, getComparator());
        return new ContainerOperator((Container)waitComponent((Container)getSource(), chooser));
    }
    
    /** Returns number of toolbars currently shown in IDE.
     * @return number of toolbars
     */
    public int getToolbarCount() {
        ToolbarChooser chooser = new ToolbarChooser("Non sense name - @#$%^&*", //NOI18N
                                                    getComparator());
        findComponent((Container)getSource(), chooser);
        return chooser.getCount();
    }
    
    /** Returns display name of toolbar with given index. Toolbars are NOT
     * indexed from left to right.
     * @param index index of toolbar
     * @return display name of toolbar
     */
    public String getToolbarName(int index) {
        return ((Toolbar)getToolbar(index).getSource()).getDisplayName();
    }
    
    /** Return JButtonOperator representing a toolbar button found by given
     * tooltip within given toolbar operator.
     * @param toolbarOper ContainerOperator of a toolbar.
     *          Use {@link #getToolbar(String)} or {@link #getToolbar(int)}
     *          to obtain an operator.
     * @param buttonTooltip tooltip of toolbar button
     * @return JButtonOperator instance of found toolbar button
     */
    public JButtonOperator getToolbarButton(ContainerOperator toolbarOper, String buttonTooltip) {
        ToolbarButtonChooser chooser = new ToolbarButtonChooser(buttonTooltip, getComparator());
        return new JButtonOperator(JButtonOperator.waitJButton(
        (Container)toolbarOper.getSource(), chooser));
    }
    
    /** Return JButtonOperator representing index-th toolbar button within given
     * toolbar operator.
     * @param toolbarOper ContainerOperator of a toolbar.
     *          Use {@link #getToolbar(String)} or {@link #getToolbar(int)}
     *          to obtain an operator.
     * @param index index of toolbar button to find
     * @return JButtonOperator instance of found toolbar button
     */
    public JButtonOperator getToolbarButton(ContainerOperator toolbarOper, int index) {
        return new JButtonOperator(toolbarOper, index);
    }
    
    /** Pushes popup menu on toolbars. It doesn't matter on which toolbar it is
     * invoked, everytime it is the same. That's why popup menu is invoked on
     * the right side of toolbar with index 0.
     * @param popupPath path to menu item (e.g. "Edit")
     */
    public void pushToolbarPopupMenu(String popupPath) {
        ContainerOperator contOper = getToolbar(0);
        contOper.clickForPopup(contOper.getWidth()-1, contOper.getHeight()/2);
        new JPopupMenuOperator().pushMenu(popupPath, "|");
    }
    
    /** Pushes popup menu on toolbars - no block further execution.
     * It doesn't matter on which toolbar it is
     * invoked, everytime it is the same. That's why popup menu is invoked on
     * the right side of toolbar with index 0.
     * @param popupPath path to menu item (e.g. "Save Configuration...")
     */
    public void pushToolbarPopupMenuNoBlock(String popupPath) {
        ContainerOperator contOper = getToolbar(0);
        contOper.clickForPopup(contOper.getWidth()-1, contOper.getHeight()/2);
        new JPopupMenuOperator().pushMenuNoBlock(popupPath, "|");
    }
    
    /** Drags a toolbar to a new position determined by [x, y] relatively.
     * @param toolbarOper ContainerOperator of a toolbar.
     *          Use {@link #getToolbar(String)} or {@link #getToolbar(int)}
     *          to obtain an operator.
     * @param x relative move along x direction
     * @param y relative move along y direction
     */
    public void dragNDropToolbar(ContainerOperator toolbarOper, int x, int y) {
        ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                if(comp instanceof JPanel) {
                    return comp.getClass().getName().equals("org.openide.awt.Toolbar$ToolbarBump");
                }
                return false;
            }
            public String getDescription() {
                return "org.openide.awt.Toolbar$ToolbarBump";
            }
        };
        Component comp = findComponent((Container)toolbarOper.getSource(), chooser);
        new ComponentOperator(comp).dragNDrop(comp.getWidth()/2, comp.getHeight()/2, x, y);
    }
    
    
    /** Chooser which can be used to find a org.openide.awt.Toolbar component or
     * count a number of such components in given container.
     */
    private static class ToolbarChooser implements ComponentChooser {
        private String toolbarName;
        private StringComparator comparator;
        private int count = 0;
        
        /** Use this to find org.openide.awt.Toolbar component with given name. */
        public ToolbarChooser(String toolbarName, StringComparator comparator) {
            this.toolbarName = toolbarName;
            this.comparator = comparator;
        }
        
        /** Use this to count org.openide.awt.Toolbar components in given container. */
        public ToolbarChooser() {
            this.comparator = null;
        }
        
        public boolean checkComponent(Component comp) {
            if(comp instanceof org.openide.awt.Toolbar) {
                count++;
                if(comparator != null) {
                    return comparator.equals(((Toolbar)comp).getDisplayName(), toolbarName);
                } else {
                    return true;
                }
            }
            return false;
        }
        
        public String getDescription() {
            return "org.openide.awt.Toolbar";
        }
        
        public int getCount() {
            return count;
        }
    }
    
    /** Chooser which can be used to find a component with given tooltip,
     * for example a toolbar button.
     */
    private static class ToolbarButtonChooser implements ComponentChooser {
        private String buttonTooltip;
        private StringComparator comparator;
        
        public ToolbarButtonChooser(String buttonTooltip, StringComparator comparator) {
            this.buttonTooltip = buttonTooltip;
            this.comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
        }
        
        public String getDescription() {
            return "Toolbar button with tooltip \""+buttonTooltip+"\".";
        }
    }
    
    /** Performs verification by accessing all sub-components */
    public void verify() {
        menuBar();
        getTabControl();
    }
    
    /** Class to trace messages printed to status bar of the Main Window.
     * <p>
     * Usage:<br>
     * <pre>
     *      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
     *      // start tracing
     *      stt.start();
     *      // compile action will produce at least two messages: "Compiling ...",
     *      // "Finished ..."
     *      new CompileAction().performAPI();

     *      // waits for "Compiling" status text
     *      stt.waitText("Compiling");
     *      // waits for "Finished" status text
     *      stt.waitText("Finished");
     *
     *      // order is not significant => following works as well
     *      stt.waitText("Finished");
     *      stt.waitText("Compiling");
     *
     *      // to be order significant, set removedCompared parameter to true
     *      stt.waitText("Compiling", true);
     *      stt.waitText("Finished", true);
     *
     *      // history was removed by above methods => need to produce a new messages
     *      new CompileAction().performAPI();
     *
     *      // order is significant if removedCompared parameter is true =>
     *      // => following fails because Finished is shown as second
     *      stt.waitText("Finished", true);
     *      stt.waitText("Compiling", true);
     *
     *      // stop tracing
     *      stt.stop();
     * </pre>
     */
    public class StatusTextTracer implements ChangeListener {
        /** List of all messages */
        private ArrayList statusTextHistory;
        
        /** Creates new instance. */
        public StatusTextTracer() {
            this.statusTextHistory = new ArrayList();
        }
        
        /** Starts to register all status messages into history array.
         * Exactly, it adds the listener to org.openide.awt.StatusDisplayer.
         * It clears possible previously filled history array before.
         */
        public void start() {
            stop();
            clear();
            StatusDisplayer.getDefault().addChangeListener(this);
        }
        
        /** Stops registering of status messages. Exactly, it removes the
         * listener from org.openide.awt.StatusDisplayer.
         */
        public void stop() {
            StatusDisplayer.getDefault().removeChangeListener(this);
        }
        
        /** Called when status text was changed. It adds status text to history
         * array.
         * @param evt change event - not used
         */
        public void stateChanged(ChangeEvent evt) {
            synchronized (this) {
                statusTextHistory.add(StatusDisplayer.getDefault().getStatusText());
            }
        }
        
        /** Clears status text history array. */
        public void clear() {
            synchronized (this) {
                statusTextHistory.clear();
            }
        }
        
        /** Checks whether given text equals to any of messages in the history
         * array. Comparator of this MainWindowOperator instance is used.
         * If <tt>removeCompared</tt> parameter is set to <tt>true</tt>,
         * messages already compared are removed from history array. Otherwise
         * messages are not removed until {@link #clear} or {@link #start} are
         * called.
         * @param text a text to be compared
         * @param removeCompared whether to remove already compared messages from
         * history array
         * @return true if text matches any of messages in the history array;
         * false otherwise
         */
        public boolean contains(String text, boolean removeCompared) {
            StringComparator comparator = getComparator();
            synchronized (this) {
                if(removeCompared) {
                    while(!statusTextHistory.isEmpty()) {
                        String status = (String)statusTextHistory.remove(0);
                        if(comparator.equals(status, text)) {
                            return true;
                        }
                    }
                } else {
                    for (int i = 0; i < statusTextHistory.size(); i ++) {
                        if(comparator.equals((String)statusTextHistory.get(i), text)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        
        /** Waits for text to be shown in the Main Window status bar not 
         * removing any message from history.
         * Comparator of this MainWindowOperator instance is used.
         * It throws TimeoutExpiredException if timeout expires.
         * @param text a text to wait for
         */
        public void waitText(final String text) {
            waitText(text, false);
        }
        
        /** Waits for text to be shown in the Main Window status bar.
         * Comparator of this MainWindowOperator instance is used.
         * If <tt>removeCompared</tt> parameter is set to <tt>true</tt>,
         * messages already compared are removed from history array. It satisfies
         * that order of messages is significant when this method is called
         * more than once.
         * If <tt>removeCompared</tt> parameter is set to <tt>false</tt>,
         * messages are not removed until {@link #clear} or {@link #start} are
         * called and its order is not taken into account.
         * @param text a text to wait for
         * @param removeCompared whether to remove already compared messages from
         * history array
         */
        public void waitText(final String text, final boolean removeCompared) {
            try {
                new Waiter(new Waitable() {
                    public Object actionProduced(Object anObject) {
                        return contains(text, removeCompared) ? Boolean.TRUE : null;
                    }
                    public String getDescription() {
                        return("Wait status text equals to "+text);
                    }
                }).waitAction(null);
            } catch (InterruptedException e) {
                throw new JemmyException("Interrupted.", e);
            }
        }
        
        /** Calls {@link #stop} at the end of life cycle of this class. */
        public void finalize() {
            stop();
        }
    }
}
