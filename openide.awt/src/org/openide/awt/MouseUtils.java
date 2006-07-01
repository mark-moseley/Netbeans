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
package org.openide.awt;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;


/** A class that contains a set of utility classes and methods
* around mouse events and processing.
*
* @author   Ian Formanek
*/
public class MouseUtils extends Object {
    private static int DOUBLE_CLICK_DELTA = 300;

    /** variable for double click */
    private static int tempx = 0;
    private static int tempy = 0;
    private static long temph = 0;
    private static int tempm = 0;
    private static MouseEvent tempe;

    /** Determines if the event is originated from the right mouse button
    * @param e the MouseEvent
    * @return true if the event is originated from the right mouse button, false otherwise
    * @deprecated Offers no advantages over the standard {@link SwingUtilities#isRightMouseButton}.
    */
    public static boolean isRightMouseButton(MouseEvent e) {
        return SwingUtilities.isRightMouseButton(e);
    }

    /** Determines if the event is originated from a left mouse button
    * @param e the MouseEvent
    * @return true if the event is originated from the left mouse button, false otherwise
    * @deprecated Offers no advantages over the standard {@link SwingUtilities#isLeftMouseButton}.
    */
    public static boolean isLeftMouseButton(MouseEvent e) {
        return javax.swing.SwingUtilities.isLeftMouseButton(e);
    }

    /** Returns true if parametr is a 'doubleclick event'
    * @param e MouseEvent
    * @return true if the event is a doubleclick
    */
    public static boolean isDoubleClick(MouseEvent e) {
        // even number of clicks is considered like doubleclick
        // it works as well as 'normal testing against 2'
        // but on solaris finaly works and on Win32 works better
        //System.out.println ("Click COunt: "+e.getClickCount ()); // NOI18N
        // If you don't do this, then if anyone calls isDoubleClick from
        // say a mouseReleased method, then the immediately following mouseClicked
        // method from a single mouse click will give isDoubleClick=true
        if ((e.getID() != MouseEvent.MOUSE_CLICKED) || (e.getClickCount() == 0)) {
            return false;
        }

        return ((e.getClickCount() % 2) == 0) || isDoubleClickImpl(e);
    }

    /** Tests the positions.
    */
    private static boolean isDoubleClickImpl(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        long h = e.getWhen();
        int m = e.getModifiers();

        //System.out.println ("When:: "+h); // NOI18N
        // same position at short time
        if ((tempx == x) && (tempy == y) && ((h - temph) < DOUBLE_CLICK_DELTA) && (
            // Without this, calling isDoubleClick() twice on the same
            // mouse event will return true the second time!
            e != tempe) && (m == tempm)) {
            // OK forget all
            tempx = 0;
            tempy = 0;
            temph = 0;
            tempm = 0;
            tempe = null;

            return true;
        } else {
            // remember
            tempx = x;
            tempy = y;
            temph = h;
            tempm = m;
            tempe = e;

            return false;
        }
    }

    // ---------------------------------------------------------------------------
    // Inner classes

    /** The PopupMouseAdapter provides safe way to implement popup menu invocation
     * mechanism. It should be used instead of invoking the popup in
     * mouseClicked because the mouseClicked does not work as "often" as
     * it should (i.e. sometimes it is not called).
     * PopupMouseAdapter delegates to isPopupTrigger to get correct popup
     * menu invocation gesture. Clients are supposed to extend this class and
     * implement showPopup method by adding code that shows popup menu properly.<br>
     *
     * Please note that older implementation which used treshold is now
     * deprecated, please use default constructor.
    */
    public static abstract class PopupMouseAdapter extends MouseAdapter {
        /** @deprecated Obsoleted as of 3.4, PopupMouseAdapter now uses isPopupTrigger properly.
            Threshold does nothing, please use default constructor without treshold.
         */
        public static final int DEFAULT_THRESHOLD = 5;

        /** Creates a new PopupMouseAdapter with specified threshold
         * @param threshold The threshold to be used
         * @deprecated Obsoleted as of 3.4, by class rewrite to use isPopupTrigger.
         * This constructor now just delegates to super constructor, please use
         * default constructor instead.
         */
        public PopupMouseAdapter(int threshold) {
            this();
        }

        /** Constructs PopupMouseAdapter. Just delegates to super constructor */
        public PopupMouseAdapter() {
            super();
        }

        public void mousePressed(MouseEvent e) {
            maybePopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybePopup(e);
        }

        private void maybePopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        /** Called when the sequnce of mouse events should lead to actual
        * showing of the popup menu.
        * Should be redefined to show the menu.
        * param evt The mouse release event - should be used to obtain the
        *           position of the popup menu
        */
        abstract protected void showPopup(MouseEvent evt);
    }
}
