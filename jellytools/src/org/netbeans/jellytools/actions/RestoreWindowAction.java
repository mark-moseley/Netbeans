/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.ComponentOperator;

/** Used to call "Restore Window" popup menu item, "Window|Restore Window" main menu item,
 * shortcut or restore window by IDE API.
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri.Skrivanek@sun.com
 */
public class RestoreWindowAction extends Action {
    
    /** "Window" main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
                                                                     "Menu/Window");
    
    /** "Window|Restore Window" */
    private static final String windowRestorePath = windowItem+"|"+
                Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                        "CTL_UnmaximizeWindowAction");

    /** "Restore Window" */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                                    "CTL_UnmaximizeWindowAction");
    

    /** Creates new instance of RestoreWindowAction. */
    public RestoreWindowAction() {
        // action MaximizeWindowAction is no called but it is used to get shortcut
        super(windowRestorePath, popupPath, "org.netbeans.core.windows.actions.MaximizeWindowAction");
    }

    
    /** Performs popup action Restore Window on given component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and restored
     */
    public void performPopup(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "RestoreWindowAction can only be called on TopComponentOperator.");
        }
    }
    
    /** Performs popup action Restore Window on given top component operator 
     * which is activated before the action.
     * @param tco top component operator which should be activated and maximized
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPath);
    }
    
    /** Restore active top component by IDE API.*/
    public void performAPI() {
        // run in dispatch thread
        new QueueTool().invokeSmoothly(new Runnable() {
            public void run() {
                AttachWindowAction.callWindowManager("switchMaximizedMode", new Object[] {null});
            }
        });
    }

    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
            "RestoreWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
            "RestoreWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
            "RestoreWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
            "RestoreWindowAction doesn't have popup representation on nodes.");
    }
}
