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
package org.netbeans.jellytools.actions;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import org.netbeans.core.NbKeymap;

import org.netbeans.jellytools.JellyVersion;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator.NoSuchPathException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.ComponentVisualizer;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.jemmy.util.EmptyVisualizer;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;


/** Ancestor class for all blocking actions.<p>
 * It handles performing action through main menu (MENU_MODE), popup menu
 * (POPUP_MODE), IDE SystemAction API call (API_MODE) or through keyboard shortcut
 * (SHORTCUT_MODE).<p>
 * Action can be performed in exact mode by calling performMenu(...),
 * performPopup(...), performAPI(...) or performShortcut(...).<p>
 * If exact mode is not supported by the action it throws
 * UnsupportedOperationException.<p>
 * Current implementation supports MENU_MODE when menuPath is defined, POPUP_MODE
 * when popupPath is defined, API_MODE when systemActionClass is defined and
 * SHORTCUT_MODE when shorcut is defined (see Action constructors).<p>
 * Action also can be performed using runtime default mode by calling perform(...).<p>
 * When default mode is not support by the action other modes are tried till
 * supported mode found and action is performed.
 *
 * <BR>Timeouts used: <BR>
 * Action.WaitAfterShortcutTimeout - time to sleep between shortcuts in sequence (default 0) <BR>
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class Action {
    
    /** through menu action performing mode */    
    public static final int MENU_MODE = 0;
    /** through popup menu action performing mode */    
    public static final int POPUP_MODE = 1;
    /** through API action performing mode */    
    public static final int API_MODE = 2;
    /** through shortcut action performing mode */    
    public static final int SHORTCUT_MODE = 3;
    
    /** sleep time between nodes selection and action execution */    
    protected static final long SELECTION_WAIT_TIME = 300;
    /** sleep time after action execution */    
    protected static final long AFTER_ACTION_WAIT_TIME = 0;
    /** sleep time between sequence of shortcuts */
    protected static final long WAIT_AFTER_SHORTCUT_TIMEOUT = 0;
    
    private static final int sequence[][] = {{MENU_MODE, POPUP_MODE, SHORTCUT_MODE, API_MODE}, 
                                             {POPUP_MODE, MENU_MODE, SHORTCUT_MODE, API_MODE}, 
                                             {API_MODE, POPUP_MODE, MENU_MODE, SHORTCUT_MODE},
                                             {SHORTCUT_MODE, POPUP_MODE, MENU_MODE, API_MODE}};

    /** menu path of current action or null when MENU_MODE is not supported */                                             
    protected String menuPath;
    /** popup menu path of current action or null when POPUP_MODE is not supported */    
    protected String popupPath;
    /** SystemAction class of current action or null when API_MODE is not supported */    
    protected Class systemActionClass;
    /** array of shortcuts of current action or null when SHORTCUT_MODE is not supported.
     * @deprecated Use {@link #keystrokes} instead
    */
    protected Shortcut[] shortcuts;
    /** Array of key strokes or null when SHORTCUT_MODE is not supported. */
    protected KeyStroke[] keystrokes;

    /** Comparator used as default for all Action instances. It is set in static clause. */
    private static StringComparator defaultComparator;
    /** Comparator used for this action instance. */
    private StringComparator comparator;

    /** creates new Action instance without API_MODE and SHORTCUT_MODE support
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */    
    public Action(String menuPath, String popupPath) {
        this(menuPath, popupPath, null, (KeyStroke[])null);
    }
    
    /** creates new Action instance without SHORTCUT_MODE support
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public Action(String menuPath, String popupPath, String systemActionClass) {
        this(menuPath, popupPath, systemActionClass, (KeyStroke[])null);
    }
    
    /** creates new Action instance without API_MODE support
     * @param shortcuts array of Shortcut instances (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported)
     * @deprecated Use {@link Action#Action(String menuPath, String popupPath, KeyStroke[] keystrokes)} instead.
    */    
    public Action(String menuPath, String popupPath, Shortcut[] shortcuts) {
        this(menuPath, popupPath, null, shortcuts);
    }
    
    /** creates new Action instance without API_MODE support
     * @param shortcut Shortcut (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported)
     * @deprecated Use {@link Action#Action(String menuPath, String popupPath, KeyStroke keystroke)} instead.
     */
    public Action(String menuPath, String popupPath, Shortcut shortcut) {
        this(menuPath, popupPath, null, new Shortcut[] {shortcut});
    }

    /** creates new Action instance
     * @param shortcuts array of Shortcut instances (use null value if shortcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) 
     * @deprecated Use {@link Action#Action(String menuPath, String popupPath, String systemActionClass, KeyStroke[] keystrokes)} instead.
    */
    public Action(String menuPath, String popupPath, String systemActionClass, Shortcut[] shortcuts) {
        this.menuPath = menuPath;
        this.popupPath = popupPath;
        if (systemActionClass==null) {
            this.systemActionClass = null;
        } else try {
            this.systemActionClass = Class.forName(systemActionClass);
        } catch (ClassNotFoundException e) {
            this.systemActionClass = null;
        }
        if(shortcuts != null) {
            this.keystrokes = new KeyStroke[shortcuts.length];
            for(int i=0;i<shortcuts.length;i++) {
                this.keystrokes[i] = KeyStroke.getKeyStroke(shortcuts[i].getKeyCode(), shortcuts[i].getKeyModifiers());
            }
        }
    }
     
    /** creates new Action instance
     * @param shortcut Shortcut String (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported)
     * @deprecated Use {@link Action#Action(String menuPath, String popupPath, String systemActionClass, KeyStroke keystroke)} instead.
    */
    public Action(String menuPath, String popupPath, String systemActionClass, Shortcut shortcut) {
        this(menuPath, popupPath, systemActionClass, new Shortcut[] {shortcut});
    }
    
    /** Creates new Action instance without API_MODE support.
     * @param keystroke KeyStroke instance (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported)
    */
    public Action(String menuPath, String popupPath, KeyStroke keystroke) {
        this(menuPath, popupPath, null, keystroke);
    }

    /** Creates new Action instance without API_MODE support.
     * @param keystrokes array of KeyStroke instances (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported)
    */
    public Action(String menuPath, String popupPath, KeyStroke[] keystrokes) {
        this(menuPath, popupPath, null, keystrokes);
    }
    
    /** Creates new Action instance.
     * @param keystroke KeyStroke instance (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) 
    */
    public Action(String menuPath, String popupPath, String systemActionClass, KeyStroke keystroke) {
        this(menuPath, popupPath, systemActionClass, new KeyStroke[] {keystroke});
    }
    
    /** Creates new Action instance.
     * @param keystrokes array of KeyStroke instances (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) 
    */
    public Action(String menuPath, String popupPath, String systemActionClass, KeyStroke[] keystrokes) {
        this.menuPath = menuPath;
        this.popupPath = popupPath;
        if (systemActionClass==null) {
            this.systemActionClass = null;
        } else try {
            this.systemActionClass = Class.forName(systemActionClass);
        } catch (ClassNotFoundException e) {
            this.systemActionClass = null;
        }            
        this.keystrokes = keystrokes;
    }

    
    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();

        if (JemmyProperties.getCurrentProperty("Action.DefaultMode")==null)
            JemmyProperties.setCurrentProperty("Action.DefaultMode", new Integer(POPUP_MODE));
        Timeouts.initDefault("Action.WaitAfterShortcutTimeout", WAIT_AFTER_SHORTCUT_TIMEOUT);
        // Set case sensitive comparator as default because of 
        // very often clash between Cut and Execute menu items.
        // Substring criterion is set according to default string comparator
        boolean compareExactly = !Operator.getDefaultStringComparator().equals("abc", "a"); // NOI18N
        defaultComparator = new DefaultStringComparator(compareExactly, true);
    }
    
    private void perform(int mode) {
        switch (mode) {
            case POPUP_MODE: performPopup(); break;
            case MENU_MODE: performMenu(); break;
            case API_MODE: performAPI(); break;
            case SHORTCUT_MODE: performShortcut(); break;
            default: throw new IllegalArgumentException("Wrong Action.MODE");
        }
    }
            
    private void perform(Node node, int mode) {
        switch (mode) {
            case POPUP_MODE: performPopup(node); break;
            case MENU_MODE: performMenu(node); break;
            case API_MODE: performAPI(node); break;
            case SHORTCUT_MODE: performShortcut(node); break;
            default: throw new IllegalArgumentException("Wrong Action.MODE");
        }
    }
            
    private void perform(Node[] nodes, int mode) {
        switch (mode) {
            case POPUP_MODE: performPopup(nodes); break;
            case MENU_MODE: performMenu(nodes); break;
            case API_MODE: performAPI(nodes); break;
            case SHORTCUT_MODE: performShortcut(nodes); break;
            default: throw new IllegalArgumentException("Wrong Action.MODE");
        }
    }
            
    private void perform(ComponentOperator component, int mode) {
        switch (mode) {
            case POPUP_MODE: performPopup(component); break;
            case MENU_MODE: performMenu(component); break;
            case API_MODE: performAPI(component); break;
            case SHORTCUT_MODE: performShortcut(component); break;
            default: throw new IllegalArgumentException("Wrong Action.MODE");
        }
    }
            
    
    /** performs action depending on default mode,<br>
     * calls performPopup(), performMenu() or performAPI(),<br>
     * when default mode is not supported, others are tried */    
    public void perform() {
        int modes[] = sequence[getDefaultMode()];
        for (int i=0; i<modes.length; i++) try {
            perform(modes[i]);
            return;
        } catch (UnsupportedOperationException e) {}
    }
    
    /** performs action depending on default mode,<br>
     * calls performPopup(), performMenu() or performAPI(),<br>
     * when default mode is not supported, others are tried
     * @param node node to be action performed on */    
    public void perform(Node node) {
        int modes[] = sequence[getDefaultMode()];
        for (int i=0; i<modes.length; i++) try {
            perform(node, modes[i]);
            return;
        } catch (UnsupportedOperationException e) {}
    }
    
    /** performs action depending on default mode,<br>
     * calls performPopup(), performMenu() or performAPI(),<br>
     * when default mode is not supported, others are tried
     * @param nodes nodes to be action performed on */    
    public void perform(Node[] nodes) {
        int modes[] = sequence[getDefaultMode()];
        for (int i=0; i<modes.length; i++) try {
            perform(nodes, modes[i]);
            return;
        } catch (UnsupportedOperationException e) {}
    }

    /** performs action depending on default mode,<br>
     * calls performPopup(), performMenu() or performAPI(),<br>
     * when default mode is not supported, others are tried
     * @param component component to be action performed on */    
    public void perform(ComponentOperator component) {
        int modes[] = sequence[getDefaultMode()];
        for (int i=0; i<modes.length; i++) try {
            perform(component, modes[i]);
            return;
        } catch (UnsupportedOperationException e) {}
    }
    
    /** performs action through main menu
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu() {
        if (menuPath==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define menu path");
        }
        // Need to wait here to be more reliable.
        // TBD - It can be removed after issue 23663 is solved.
        new EventTool().waitNoEvent(500);
        MainWindowOperator.getDefault().menuBar().pushMenu(menuPath, "|", getComparator());
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }    

    /** performs action through main menu
     * @param node node to be action performed on 
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu(Node node) {
        performMenu(new Node[]{node});
    }
    
    /** performs action through main menu
     * @param nodes nodes to be action performed on
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu(Node[] nodes) {
        if (menuPath==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define menu path");
        testNodes(nodes);
        nodes[0].select();
        for (int i=1; i<nodes.length; i++)
            nodes[i].addSelectionPath();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performMenu();
    }
    
    /** performs action through main menu
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu(ComponentOperator component) {
        component.getFocus();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performMenu();
    }
    
    /** performs action through popup menu
     * @throws UnsupportedOperationException */    
    public void performPopup() {
        throw new UnsupportedOperationException(getClass().toString()+" does not implement performPopup()");
    }

    /** performs action through popup menu
     * @param node node to be action performed on 
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(Node node) {
        performPopup(new Node[]{node});
    }
    
    /** performs action through popup menu
     * @param nodes nodes to be action performed on  
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(Node[] nodes) {
        callPopup(nodes).pushMenu(popupPath, "|", getComparator());
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** Calls popup on given nodes and returns JPopupMenuOperator instance.
     * @param nodes nodes to be action performed on  
     * @return JPopupMenuOperator instance
     */
    JPopupMenuOperator callPopup(Node[] nodes) {
        if (popupPath==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        }
        testNodes(nodes);
        ComponentVisualizer treeVisualizer = nodes[0].tree().getVisualizer();
        ComponentVisualizer oldVisualizer = null;
        // If visualizer of JTreeOperator is EmptyVisualizer, we need
        // to avoid making tree component visible in callPopup method.
        // So far only known case is tree from TreeTableOperator.
        if(treeVisualizer instanceof EmptyVisualizer) {
            oldVisualizer = Operator.getDefaultComponentVisualizer();
            Operator.setDefaultComponentVisualizer(treeVisualizer);
        }
        // Need to wait here to be more reliable.
        // TBD - It can be removed after issue 23663 is solved.
        new EventTool().waitNoEvent(500);
        TreePath paths[] = new TreePath[nodes.length];
        for (int i=0; i<nodes.length; i++) {
            paths[i]=nodes[i].getTreePath();
        }
        JPopupMenuOperator popup;
        try {
            popup = new JPopupMenuOperator(nodes[0].tree().callPopupOnPaths(paths));
        } catch (NoSuchPathException e) {
            // possibly node was recreated, so we can find it again (see issue #71591)
            for (int i=0; i<nodes.length; i++) {
                paths[i] = nodes[i].getTreePath();
            }
            popup = new JPopupMenuOperator(nodes[0].tree().callPopupOnPaths(paths));
        }
        // restore previously used default visualizer
        if(oldVisualizer != null) {
            Operator.setDefaultComponentVisualizer(oldVisualizer);
        }
        popup.setComparator(getComparator());
        return popup;
    }
    
    /** performs action through popup menu
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(ComponentOperator component) {
        if (popupPath==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        }
        // Need to wait here to be more reliable.
        // TBD - It can be removed after issue 23663 is solved.
        new EventTool().waitNoEvent(500);
        component.clickForPopup();
        new JPopupMenuOperator(component).pushMenu(popupPath, "|", getComparator());
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** performs action through API  
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI() {
        if (systemActionClass==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not support API call.");
        }
        try {
            // actions has to be invoked in dispatch thread (see http://www.netbeans.org/issues/show_bug.cgi?id=35755)
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    if(SystemAction.class.isAssignableFrom(systemActionClass)) {
                        // SystemAction used in IDE
                        SystemAction.get(systemActionClass).actionPerformed(
                                                new ActionEvent(new Container(), 0, null));
                    } else {
                        // action implements javax.swing.Action
                        try {
                            ((javax.swing.Action)systemActionClass.newInstance()).actionPerformed(null);
                        } catch (Exception e) {
                            throw new JemmyException("Exception when trying to create instance of action \""+systemActionClass.getName()+"\".", e);
                        }
                    }
                }
            });
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Interrupted", e);
        }
    }
    
    /** performs action through API
     * @param node node to be action performed on 
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI(Node node) {
        performAPI(new Node[]{node});
    }
    
    /** performs action through API
     * @param nodes nodes to be action performed on 
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI(Node[] nodes) {
        if (systemActionClass==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define SystemAction");
        testNodes(nodes);
        nodes[0].select();
        for (int i=1; i<nodes.length; i++)
            nodes[i].addSelectionPath();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performAPI();
    }
    
    /** performs action through API
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI(ComponentOperator component) {
        component.getFocus();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performAPI();
    }
    
    /** performs action through shortcut  
     * @throws UnsupportedOperationException if no shortcut is defined */
    public void performShortcut() {
        KeyStroke[] strokes = getKeyStrokes();
        if (strokes == null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        }
        for(int i=0; i<strokes.length; i++) {
            new KeyRobotDriver(null).pushKey(null, strokes[i].getKeyCode(), strokes[i].getModifiers(), JemmyProperties.getCurrentTimeouts().create("ComponentOperator.PushKeyTimeout"));
            JemmyProperties.getProperties().getTimeouts().sleep("Action.WaitAfterShortcutTimeout");
        }
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** performs action through shortcut
     * @param node node to be action performed on 
     * @throws UnsupportedOperationException when action does not support shortcut mode */    
    public void performShortcut(Node node) {
        performShortcut(new Node[]{node});
    }
    
    /** performs action through shortcut
     * @param nodes nodes to be action performed on 
     * @throws UnsupportedOperationException when action does not support shortcut mode */    
    public void performShortcut(Node[] nodes) {
        final KeyStroke[] strokes = getKeyStrokes();
        if (strokes == null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        }
        testNodes(nodes);
        nodes[0].select();
        for (int i=1; i<nodes.length; i++)
            nodes[i].addSelectionPath();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performShortcut();
    }
    
    /** performs action through shortcut
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support shortcut mode */    
    public void performShortcut(ComponentOperator component) {
        component.getFocus();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performShortcut();
    }
    
    /** tests if nodes are all from the same tree
     * @param nodes nodes
     * @throws IllegalArgumentException when given nodes does not pass */    
    protected void testNodes(Node[] nodes) {
        if ((nodes==null)||(nodes.length==0))
            throw new IllegalArgumentException("argument nodes is null or empty");
        Class myClass = getClass();
        Component nodesTree=nodes[0].tree().getSource();
        for (int i=0; i<nodes.length; i++) {
//            if (!nodes[i].hasAction(myClass))
//                throw new IllegalArgumentException(this.toString()+" could not be performed on "+nodes[i].toString());
            if (nodes[i]==null)
                throw new IllegalArgumentException("argument nodes contains null value");
            if (!nodesTree.equals(nodes[i].tree().getSource()))
                throw new IllegalArgumentException(nodes[i].toString()+" is from different tree");
        }
    }
    
    /** Returns default mode in which actions are performed.
     * @return default mode in which actions are performed
     * @see #POPUP_MODE
     * @see #MENU_MODE
     * @see #API_MODE
     * @see #SHORTCUT_MODE
     */
    public int getDefaultMode() {
        int mode=(((Integer)JemmyProperties.getCurrentProperty("Action.DefaultMode")).intValue());
        if (mode<0 || mode>3)
            return POPUP_MODE;
        return mode;
    }
    
    /** Sets default mode in which actions are performed. If given mode value
     * is not valid, it sets {@link #POPUP_MODE} as default.
     * @param mode mode to be set
     * @return previous value
     * @see #POPUP_MODE
     * @see #MENU_MODE
     * @see #API_MODE
     * @see #SHORTCUT_MODE
     */
    public int setDefaultMode(int mode) {
        int oldMode = (((Integer)JemmyProperties.getCurrentProperty("Action.DefaultMode")).intValue());
        if (mode<0 || mode>3) {
            mode = POPUP_MODE;
        }
        JemmyProperties.setCurrentProperty("Action.DefaultMode", new Integer(mode));
        return oldMode;
    }
    
    
    /** Sets comparator fot this action. Comparator is used for all actions
     * after this method is called.
     * @param comparator new comparator to be set (e.g. 
     *                   new Operator.DefaultStringComparator(true, true);
     *                   to search string item exactly and case sensitive)
     */
    public void setComparator(StringComparator comparator) {
        this.comparator = comparator;
    }

    /** Gets comparator set for this action instance.
     * @return comparator set for this action instance.
     */
    public StringComparator getComparator() {
        if(comparator == null) {
            comparator = defaultComparator;
        }
        return comparator;
    }
    
    /** getter for popup menu path
     * @return String popup menu path (or null if not suported)
     */    
    public String getPopupPath() {
        return popupPath;
    }
    
    /** getter for main menu path
     * @return String main menu path (or null if not suported)
     */    
    public String getMenuPath() {
        return menuPath;
    }
    
    /** getter for system action class
     * @return Class of system action (or null if not suported)
     */    
    public Class getSystemActionClass() {
        return systemActionClass;
    }
    
    /** getter for array of shortcuts
     * @return Shortcut[] (or null if not suported)
     * @deprecated Use {@link #getKeyStrokes} instead
     */    
    public Shortcut[] getShortcuts() {
        return shortcuts;
    }

    /** Returns an array of KeyStroke objects.
     * If systemActionClass is defined and no keystrokes defined in constructor,
     * it tries to find key strokes in NbKeymap.
     * Otherwise it returns keystrokes defined in constructor, which can be null.
     * It helps to overcome differencies on platforms (e.g. on mac).
     * @return an array of KeyStroke objects or null if not found.
     */
    public KeyStroke[] getKeyStrokes() {
        if (this.keystrokes == null && systemActionClass != null) {
            NbKeymap keymap = (NbKeymap) Lookup.getDefault().lookup(NbKeymap.class);
            javax.swing.Action myAction = null;
            javax.swing.Action[] actions = keymap.getBoundActions();
            for (int i = 0; i < actions.length; i++) {
                if(actions[i].getClass().equals(systemActionClass)) {
                    // Returns just first keystroke. For actions like copy can 
                    // can exists more than one (Ctrl+C, Ctrl+Insert, Copy key).
                    return new KeyStroke[] {keymap.getKeyStrokesForAction(actions[i])[0]};
                }
            }
        }
        return this.keystrokes;
    }
    
    /** Checks whether this action is enabled. If IDE system action class
     * is defined, it calls its isEnabled() method else if main menu path is 
     * defined, it checks menu item is enabled. Otherwise it throws 
     * UnsupportedOperationException.
     * @return true if this action is enabled; false otherwise
     */
    public boolean isEnabled() {
        if(systemActionClass != null) {
            return SystemAction.get(systemActionClass).isEnabled();
        } else if(menuPath != null) {
            return MainWindowOperator.getDefault().menuBar().showMenuItem(
                                    menuPath, "|", getComparator()).isEnabled();
        } else {
            throw new UnsupportedOperationException("Cannot detect if "+getClass().getName()+" is enabled.");
        }
    }

    /** Checks whether this action on given nodes is enabled. If IDE system action class
     * is defined, it calls its isEnabled() method. Nodes are selected first. 
     * Else if popup menu path is defined, it checks menu item is enabled.
     * Otherwise it throws UnsupportedOperationException.
     * @param nodes array of nodes to be selected before a check
     * @return true if this action is enabled; false otherwise
     */
    public boolean isEnabled(Node[] nodes) {
        testNodes(nodes);
        if(systemActionClass != null) {
            nodes[0].select();
            for (int i=1; i<nodes.length; i++)
                nodes[i].addSelectionPath();
            try {
                Thread.sleep(SELECTION_WAIT_TIME);
            } catch (Exception e) {
                throw new JemmyException("Sleeping interrupted", e);
            }
            return SystemAction.get(systemActionClass).isEnabled();
        } else if(popupPath != null) {
            TreePath paths[]=new TreePath[nodes.length];
            for (int i=0; i<nodes.length; i++) {
                paths[i]=nodes[i].getTreePath();
            }
            ComponentVisualizer treeVisualizer = nodes[0].tree().getVisualizer();
            ComponentVisualizer oldVisualizer = null;
            // If visualizer of JTreeOperator is EmptyVisualizer, we need
            // to avoid making tree component visible in callPopup method.
            // So far only known case is tree from TreeTableOperator.
            if(treeVisualizer instanceof EmptyVisualizer) {
                oldVisualizer = Operator.getDefaultComponentVisualizer();
                Operator.setDefaultComponentVisualizer(treeVisualizer);
            }
            // Need to wait here to be more reliable.
            // TBD - It can be removed after issue 23663 is solved.
            new EventTool().waitNoEvent(500);
            JPopupMenuOperator popup=new JPopupMenuOperator(nodes[0].tree().callPopupOnPaths(paths));
            // restore previously used default visualizer
            if(oldVisualizer != null) {
                Operator.setDefaultComponentVisualizer(oldVisualizer);
            }
            return popup.showMenuItem(popupPath, "|", getComparator()).isEnabled();
        } else {
            throw new UnsupportedOperationException("Cannot detect if "+getClass().getName()+" is enabled.");
        }
    }
    
    /** Checks whether this action on given node is enabled. If IDE system action class
     * is defined, it calls its isEnabled() method. Node is selected first.
     * Else if popup menu path is defined, it checks menu item is enabled.
     * Otherwise it throws UnsupportedOperationException.
     * @param node node to be selected before a check
     * @return true if this action is enabled; false otherwise
     */
    public boolean isEnabled(Node node) {
        return isEnabled(new Node[] {node});
    }

    /** Checks whether this action is enabled for given ComponentOperator. 
     * First it makes component visible and focused.
     * If IDE system action class is defined, it calls its isEnabled() method.
     * Else if main menu path is defined, it checks menu item is enabled.
     * Otherwise it throws UnsupportedOperationException.
     * @param componentOperator instance of ComponentOperator
     * @return true if this action is enabled; false otherwise
     */
    public boolean isEnabled(ComponentOperator componentOperator) {
        componentOperator.makeComponentVisible();
        componentOperator.getFocus();
        if(systemActionClass != null) {
            return SystemAction.get(systemActionClass).isEnabled();
        } else if(popupPath != null) {
            // Need to wait here to be more reliable.
            // TBD - It can be removed after issue 23663 is solved.
            new EventTool().waitNoEvent(500);
            componentOperator.clickForPopup();
            return new JPopupMenuOperator(componentOperator).showMenuItem(
                                    popupPath, "|", getComparator()).isEnabled();
        } else if(menuPath != null) {
            return MainWindowOperator.getDefault().menuBar().showMenuItem(
                                    menuPath, "|", getComparator()).isEnabled();
        } else {
            throw new UnsupportedOperationException("Cannot detect if "+getClass().getName()+" is enabled.");
        }
    }

    /** This class defines keyboard shortcut for action execution.
     * @deprecated Use {@link javax.swing.KeyStroke} instead.
    */    
    public static class Shortcut extends Object {
        /** key code of shortcut (see KeyEvent) */        
        protected int keyCode;
        /** key modifiers of shortcut (see KeyEvent) */        
        protected int keyModifiers;
        
        /** creates new shortcut
         * @param keyCode int key code (see KeyEvent) */        
        public Shortcut(int keyCode) {
            this(keyCode, 0);
        }

        /** creates new shortcut
         * @param keyCode int key code (see KeyEvent)
         * @param keyModifiers int key modifiers (see KeyEvent) */        
        public Shortcut(int keyCode, int keyModifiers) {
            this.keyCode=keyCode;
            this.keyModifiers=keyModifiers;
        }
        
        /** getter for key code
         * @return int key code */        
        public int getKeyCode() {
            return keyCode;
        }
        
        /** getter for key modifiers
         * @return int key modifiers */        
        public int getKeyModifiers() {
            return keyModifiers;
        }
            
        /** returns String representation of shortcut
         * @return String representation of shortcut */        
        public String toString() {
            String s=KeyEvent.getKeyModifiersText(getKeyModifiers());
            return s+(s.length()>0?"+":"")+KeyEvent.getKeyText(getKeyCode());
        }
    }
}
