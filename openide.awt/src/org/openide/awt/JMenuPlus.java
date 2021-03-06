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
package org.openide.awt;

import java.awt.*;

import javax.swing.*;


/** A subclass of JMenu which provides workaround for pre-JDK 1.2.2 JMenu positioning problem.
 * It assures, that the popup menu gets placed inside visible screen area.
 * It also improves placement of popups in the case the subclass lazily changes
 * the content from getPopupMenu().
 * @deprecated doesn't do anything special anymore - since org.openide.awt 6.5
 */
public class JMenuPlus extends JMenu {
    static final long serialVersionUID = -7700146216422707913L;
//    private static final boolean NO_POPUP_PLACEMENT_HACK = Boolean.getBoolean("netbeans.popup.no_hack"); // NOI18N

    public JMenuPlus() {
        this(""); // NOI18N
    }

    public JMenuPlus(String label) {
        super(label);

        enableInputMethods(false);

        getAccessibleContext().setAccessibleDescription(label);
    }

//    /** Overriden to provide better strategy for placing the JMenu on the screen.
//    * @param b a boolean value -- true to make the menu visible, false to hide it
//    */
//    public void setPopupMenuVisible(boolean b) {
//        boolean isVisible = isPopupMenuVisible();
//
//        if (b != isVisible) {
//            if ((b == true) && isShowing()) {
////                // The order of calls is a provision for subclassers that 
////                // change the content of the menu during getPopupMenu()
////                // We compute the origin later with properly filled popup
//                JPopupMenu popup = getPopupMenu();
////
////                // HACK[pnejedly]: Notify all the items in the menu we're going to show
////                JInlineMenu.prepareItemsInContainer(popup);
////
////                // End of HACK
////                // HACK[mkleint]: Notify all the items in the menu we're going to show
////                // #40824 - when the text changes, it's too late to update in popup.show() (which triggers the updateState() in the MenuBridge.
////                Actions.prepareMenuBridgeItemsInContainer(popup);
//
//                // End of HACK
////                if (NO_POPUP_PLACEMENT_HACK) {
//                    Point p = super.getPopupMenuOrigin();
//                    popup.show(this, p.x, p.y);
////                } else {
////                    Point p = getPopupMenuOrigin(popup);
////                    popup.show(this, p.x, p.y);
////                }
//            } else {
//                getPopupMenu().setVisible(false);
//            }
//        }
//    }

//    /** Overriden to provide better strategy for placing the JMenu on the screen.
//    *
//    * @return a Point in the coordinate space of the menu instance
//    * which should be used as the origin of the JMenu's popup menu.
//    */
//    protected Point getPopupMenuOrigin(JPopupMenu pm) {
//        int x = 0;
//        int y = 0;
//        Rectangle screenRect = JPopupMenuUtils.getScreenRect();
//        Dimension s = getSize();
//        Dimension pmSize = pm.getSize();
//        int screenRight = screenRect.x + screenRect.width;
//        int screenBottom = screenRect.y + screenRect.height;
//
//        // For the first time the menu is popped up,
//        // the size has not yet been initiated
//        if (pmSize.width == 0) {
//            pmSize = pm.getPreferredSize();
//        }
//
//        Point position = getLocationOnScreen();
//
//        Container parent = getParent();
//
//        if (parent instanceof JPopupMenu) {
//            // We are a submenu (pull-right)
//            // First determine x:
//            if ((position.x + s.width + pmSize.width) < screenRight) {
//                x = s.width; // Prefer placement to the right
//            } else {
//                x = 0 - pmSize.width; // Otherwise place to the left
//            }
//
//            // Then the y:
//            if ((position.y + pmSize.height) < screenBottom) {
//                y = 0; // Prefer dropping down
//            } else {
//                y = s.height - pmSize.height; // Otherwise drop 'up'
//            }
//        } else {
//            // We are a toplevel menu (pull-down)
//            // First determine the x:
//            if ((position.x + pmSize.width) < screenRight) {
//                x = 0; // Prefer extending to right
//            } else {
//                x = s.width - pmSize.width; // Otherwise extend to left
//            }
//
//            // Then the y:
//            if ((position.y + s.height + pmSize.height) < screenBottom) {
//                y = s.height; // Prefer dropping down
//            } else {
//                y = 0 - pmSize.height; // Otherwise drop 'up'
//            }
//        }
//
//        if (y < -position.y) {
//            y = -position.y;
//        }
//
//        if (x < -position.x) {
//            x = -position.x;
//        }
//
//        return new Point(x, y);
//    }
}
