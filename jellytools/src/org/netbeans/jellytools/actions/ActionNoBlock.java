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
package org.netbeans.jellytools.actions;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.EmptyVisualizer;
import org.openide.util.actions.SystemAction;

/** Ancestor class for all non-blocking actions.<p>
 * This class re-implements all blocking calls from parent Action class to
 * non-blocking call.<p>
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @see Action */
public class ActionNoBlock extends Action {
    
    /** creates new non-blocking Action instance without API_MODE support
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath) {
        super(menuPath, popupPath);
    }
    
    /** creates new non-blocking Action instance
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass) {
        super(menuPath, popupPath, systemActionClass);
    }
    
    /** creates new Action instance without API_MODE support
     * @param shortcut Shortcut (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath, Shortcut shortcut) {
        super(menuPath, popupPath, shortcut);
    }
    
    /** creates new Action instance
     * @param shortcut Shortcut String (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass, Shortcut shortcut) {
        super(menuPath, popupPath, systemActionClass, shortcut);
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
        MainWindowOperator.getDefault().menuBar().pushMenuNoBlock(menuPath, "|");
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }    
    
    /** performs action through popup menu
     * @param nodes nodes to be action performed on  
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(Node[] nodes) {
        if (popupPath==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        testNodes(nodes);
        TreePath paths[]=new TreePath[nodes.length];
        for (int i=0; i<nodes.length; i++) {
            paths[i]=nodes[i].getTreePath();
        }
        Operator.ComponentVisualizer treeVisualizer = nodes[0].tree().getVisualizer();
        Operator.ComponentVisualizer oldVisualizer = null;
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
        JPopupMenuOperator popup = new JPopupMenuOperator(nodes[0].tree().callPopupOnPaths(paths));
        // restore previously used default visualizer
        if(oldVisualizer != null) {
            Operator.setDefaultComponentVisualizer(oldVisualizer);
        }
        popup.setComparator(new Operator.DefaultStringComparator(false, true));
        popup.pushMenuNoBlock(popupPath, "|");
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** performs action through popup menu
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(ComponentOperator component) {
        if (popupPath==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        // Need to wait here to be more reliable.
        // TBD - It can be removed after issue 23663 is solved.
        new EventTool().waitNoEvent(500);
        component.clickForPopup();
        JPopupMenuOperator popup=new JPopupMenuOperator(component);
        popup.setComparator(new Operator.DefaultStringComparator(false, true));
        popup.pushMenuNoBlock(popupPath, "|");
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }

    /** performs action through API  
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI() {
        if (systemActionClass==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define SystemAction");
        new Thread(new Runnable() {
            public void run() {
                SystemAction.get(systemActionClass).actionPerformed(null);    
            }
        }, "thread performing action through API").start();
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }

    /** performs action through shortcut
     * @throws UnsupportedOperationException when action does not support shortcut mode */    
    public void performShortcut() {
        if (shortcut==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        new Thread(new Runnable() {
            public void run() {
                new KeyRobotDriver(null).pushKey(null, shortcut.getKeyCode(), shortcut.getKeyModifiers(), JemmyProperties.getCurrentTimeouts().create("Timeouts.DeltaTimeout"));
            }
        }, "thread performing action through shortcut").start();
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
}
