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

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.openide.windows.TopComponent;

/** Used to call "Maximize Window" popup menu item, "Window|Maximize Window" main menu item,
 * shortcut or maximize window by IDE API.
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri.Skrivanek@sun.com
 */
public class MaximizeWindowAction extends Action {
    
    /** "Window" main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                                                                     "Menu/Window");
    
    /** "Window|Maximize Window" */
    private static final String windowMaximizePath = windowItem+"|"+
                Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                        "CTL_MaximizeWindowAction");

    /** "Maximize Window" */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                                    "CTL_MaximizeWindowAction");
    

    /** Creates new instance of MaximizeWindowAction. */
    public MaximizeWindowAction() {
        super(windowMaximizePath, popupPath, "org.netbeans.core.windows.actions.MaximizeWindowAction");
    }
    
    /** Performs popup action Maximize Window on given component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and maximized
     */
    public void performPopup(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "MaximizeWindowAction can only be called on TopComponentOperator.");
        }
    }
    
    /** Performs popup action Maximize Window on given top component operator 
     * which is activated before the action.
     * @param tco top component operator which should be activated and maximized
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPath);
    }
    
    /** Maximize active top component by IDE API.*/
    public void performAPI() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        ModeImpl activeMode = wm.getActiveMode();
        if(activeMode != null) {
            wm.setMaximizedMode(activeMode);
        }
    }

    /** Performs Maximize Window by IDE API on given top component operator 
     * which is activated before the action.
     * @param tco top component operator which should be activated and maximized
     */
    public void performAPI(final TopComponentOperator tco) {
        tco.makeComponentVisible();
        // run in dispatch thread
        tco.getQueueTool().invokeSmoothly(new Runnable() {
            public void run() {
                WindowManagerImpl wm = WindowManagerImpl.getInstance();
                ModeImpl mode = (ModeImpl)wm.findMode((TopComponent)tco.getSource());
                wm.setMaximizedMode(mode);
            }
        });
    }
    
    /** Throws UnsupportedOperationException because MaximizeWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
            "MaximizeWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because MaximizeWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
            "MaximizeWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because MaximizeWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
            "MaximizeWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because MaximizeWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
            "MaximizeWindowAction doesn't have popup representation on nodes.");
    }
}
