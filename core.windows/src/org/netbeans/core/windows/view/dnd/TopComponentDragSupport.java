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


package org.netbeans.core.windows.view.dnd;



import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.lang.ref.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.ui.*;
import org.openide.util.*;
import org.openide.windows.TopComponent;



/**
 * Window system drag support for <code>TopComponet</code>'s.
 * It imitates role of drag gesture recognizer, possible
 * on any kind of <code>Component</code>, currently on <code>Tabbed</code>.
 * Starts also programatically the DnD for TopComponent in container
 * when the starting gestures are Shift+Mouse Drag or Ctrl+Mouse Drag
 * respectivelly.
 * It serves as <code>DragSourceListener</code> during the DnD in progress
 * and sets dragging cursor appopriatelly.
 *
 * <em>Note:</em> There is used only one singleton instance in window system
 * DnD available via {@link #getDefault}.
 *
 *
 * @author  Peter Zavadsky
 *
 * @see java awt.dnd.DragSourceListener
 */
final class TopComponentDragSupport 
implements AWTEventListener, DragSourceListener {
    
    /** Mime type for <code>TopComponent</code> <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT = 
        DataFlavor.javaJVMLocalObjectMimeType
        // Note: important is the space after semicolon, thus to match
        // when comparing.
        + "; class=org.openide.windows.TopComponent"; // NOI18N

    /** Mime type for <code>TopComponent.Cloneable</code> <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT_CLONEABLE = 
        DataFlavor.javaJVMLocalObjectMimeType
        + "; class=org.openide.windows.TopComponent$Cloneable"; // NOI18N
    

    /** Mime type for <code>TopComponent</code>'s array <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT_ARRAY =
        DataFlavor.javaJVMLocalObjectMimeType
        + "; class=org.netbeans.core.windows.view.dnd.TopComponentDragSupport$TopComponentArray"; // NOI18N

    
    /** 'Copy window' cursor type. */
    private static final int CURSOR_COPY    = 0;
    /** 'Copy_No window' cursor type. */
    private static final int CURSOR_COPY_NO = 1;
    /** 'Move window' cursor type. */
    private static final int CURSOR_MOVE    = 2;
    /** 'Move_No window' cursor type. */
    private static final int CURSOR_MOVE_NO = 3;
    /** Cursor type indicating there cannont be copy operation 
     * done, but could be done move operation. In fact is
     * the same like {@link #CURSOR_COPY_NO} with the diff name
     * to be recognized correctly when switching action over drop target */
    private static final int CURSOR_COPY_NO_MOVE = 4;
    /** Move to free area cursor type */
    private static final int CURSOR_MOVE_FREE = 5;

    /** Name for 'Copy window' cursor. */
    private static final String NAME_CURSOR_COPY         = "CursorTopComponentCopy"; // NOI18N
    /** Name for 'Copy_No window' cursor. */
    private static final String NAME_CURSOR_COPY_NO      = "CursorTopComponentCopyNo"; // NOI18N
    /** Name for 'Move window' cursor. */
    private static final String NAME_CURSOR_MOVE         = "CursorTopComponentMove"; // NOI18N
    /** Name for 'Move_No window' cursor. */
    private static final String NAME_CURSOR_MOVE_NO      = "CursorTopComponentMoveNo"; // NOI18N
    /** */
    private static final String NAME_CURSOR_COPY_NO_MOVE = "CursorTopComponentCopyNoMove"; // NOI18N
    /** Name for cursor to drop to free area. */
    private static final String NAME_CURSOR_MOVE_FREE    = "CursorTopComponentMoveFree"; // NOI18N

    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(TopComponentDragSupport.class);
    
    private final WindowDnDManager windowDnDManager;

    /** Weak reference to <code>DragSourceContext</code> used in processed
     * drag operation. Used for by fixing bugs while not passed correct
     * order of events to <code>DragSourceListener</code>. */
    private Reference<DragSourceContext> dragContextWRef = new WeakReference<DragSourceContext>(null);
    
    /** Flag indicating the current window drag operation transferable
     * can be 'copied', i.e. the dragged <code>TopComponent</code> is
     * <code>TopComponent.Cloneable</code> instance. */
    private boolean canCopy;
    
    // #21918. There is not possible to indicate drop action in "free" desktop
    // area. This field helps to workaround the problem.
    /** Flag indicating user drop action. */
    private int hackUserDropAction;

    // #21918. Determine the ESC pressed.
    /** Flag indicating the user has cancelled drag operation by pressing ESC key. */
    private boolean hackESC;

    /** Weak set of componens on which we listen for ESC key. */
    private final Set keyObservers = new WeakSet(4);

    private Point startingPoint;
    private Component startingComponent;
    private long startingTime;
    
    
    /** Creates a new instance of TopComponentDragSupport. */
    TopComponentDragSupport(WindowDnDManager windowDnDManager) {
        this.windowDnDManager = windowDnDManager;
    }

    
    /** Informs whether the 'copy' operation is possible. Gets valid result
     * during processed drag operation only.
     * @return <code>true</code> if the drop copy operation is possible from
     * drag source point of view
     * @see #canCopy */
    public boolean isCopyOperationPossible() {
        return canCopy;
    }

    /** Simulates drag gesture recongition valid for winsys.
     * Implements <code>AWTEventListener</code>. */
    public void eventDispatched(AWTEvent evt) {
        MouseEvent me = (MouseEvent) evt;
        //#118828
        if (! (evt.getSource() instanceof Component)) {
            return;
        }

        // #40736: only left mouse button drag should start DnD
        if((me.getID() == MouseEvent.MOUSE_PRESSED) && SwingUtilities.isLeftMouseButton(me)) {
                startingPoint = me.getPoint();
            startingComponent = me.getComponent();
            startingTime = me.getWhen();
        } else if(me.getID() == MouseEvent.MOUSE_RELEASED) {
            startingPoint = null;
            startingComponent = null;
        }
        
        if(evt.getID() != MouseEvent.MOUSE_DRAGGED) {
            return;
        }
        if(windowDnDManager.isDragging()) {
            return;
        }
        if(startingPoint == null) {
            return;
        }
        if( evt.getSource() instanceof JButton ) {
            //do not initiate topcomponent drag when the mouse is dragged out of a tabcontrol button
            return;
        }
        if(!windowDnDManager.isDnDEnabled()) {
            return;
        }

        Component srcComp = startingComponent;
        if(srcComp == null) {
            return;
        }
        
        final Point point = new Point(startingPoint);
        Point currentPoint = me.getPoint();
        Component currentComponent = me.getComponent();
        if(currentComponent == null) {
            return;
        }
        currentPoint = SwingUtilities.convertPoint(currentComponent, currentPoint, srcComp);
        if(Math.abs(currentPoint.x - point.x) <= Constants.DRAG_GESTURE_START_DISTANCE
        && Math.abs(currentPoint.y - point.y) <= Constants.DRAG_GESTURE_START_DISTANCE) {
            return;
        }
        // time check, to prevent wild mouse clicks to be considered DnD start
        if (me.getWhen() - startingTime <= Constants.DRAG_GESTURE_START_TIME) {
            return;
        }
        startingPoint = null;
        startingComponent = null;
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("eventDispatched (MOUSE_DRAGGED)"); // NOI18N
        }
        
        // XXX Do not clash with JTree (e.g. in explorer) drag.
        if((srcComp instanceof JTree)
        && ((JTree)srcComp).getPathForLocation(me.getX(), me.getY()) != null) {
            return;  
        }
        
        // #22622: AWT listener just passivelly listnens what is happenning around,
        // and we need always the deepest component to start from.
        srcComp = SwingUtilities.getDeepestComponentAt(srcComp, point.x, point.y);

        boolean ctrlDown  = me.isControlDown();
        
        TopComponent tc = null;
        Tabbed tabbed;

        if(srcComp instanceof Tabbed) {
            tabbed = (Tabbed)srcComp;
        } else {
            tabbed = (Tabbed)SwingUtilities.getAncestorOfClass(Tabbed.class, srcComp);
        }
        if (tabbed == null) {
            if(srcComp instanceof Tabbed.Accessor) {
                tabbed = ((Tabbed.Accessor)srcComp).getTabbed();
            } else {
                Tabbed.Accessor acc = (Tabbed.Accessor)SwingUtilities.getAncestorOfClass(Tabbed.Accessor.class, srcComp);
                tabbed = acc != null ? acc.getTabbed() : null;
            }
        }
        if(tabbed == null) {
            return;
        }
        
        Point ppp = new Point(point);
        Point p = SwingUtilities.convertPoint(srcComp, ppp, tabbed.getComponent());
        
        // #106761: tabForCoordinate may return -1, so check is needed
        int tabIndex = tabbed.tabForCoordinate(p);
        tc = tabIndex != -1 ? tabbed.getTopComponentAt(tabIndex) : null;
        if (tc == null) {
            return;
        }

        // #21918. See above.
        if (ctrlDown) {
            hackUserDropAction = DnDConstants.ACTION_COPY;
        }
        else {
            hackUserDropAction = DnDConstants.ACTION_MOVE;
        }
                 

        List<MouseEvent> list = new ArrayList<MouseEvent>();
        list.add(me);

        // Get start droppable (if there) and its starting point.
        TopComponentDroppable startDroppable = (TopComponentDroppable)SwingUtilities
                            .getAncestorOfClass(TopComponentDroppable.class, tc);
        Point startPoint;
        if (startDroppable == null) {
            startDroppable = (TopComponentDroppable)SwingUtilities
                                .getAncestorOfClass(TopComponentDroppable.class, tabbed.getComponent());
        }
        if(startDroppable != null) {
            startPoint = point;
            Point pp = new Point(point);
            startPoint = SwingUtilities.convertPoint(srcComp, pp, (Component)startDroppable);
        } else {
            startPoint = null;
        }
        //dragSource.startDrag(event, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) ,image , new Point(-offX, -offY),text, this);

        doStartDrag(
            srcComp,
            tc, 
            new DragGestureEvent(
                new FakeDragGestureRecognizer(windowDnDManager, me),
                hackUserDropAction,
                point,
                list
            ),
            startDroppable,
            startPoint
        );
    }
    
    /** Actually starts the drag operation. */
    private void doStartDrag(Component startingComp, Object transfer, DragGestureEvent evt,
    TopComponentDroppable startingDroppable, Point startingPoint) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("doStartDrag"); // NOI18N
        }
        
        TopComponent firstTC = transfer instanceof TopComponent
                ? (TopComponent)transfer
                : (((TopComponent[])transfer)[0]);
        
        // #22132. If in modal dialog no drag allowed.
        Dialog dlg = (Dialog)SwingUtilities.getAncestorOfClass(
                Dialog.class, firstTC);
        if(dlg != null && dlg.isModal()) {
            return; 
        }
        
        if(firstTC instanceof TopComponent.Cloneable) {
            canCopy = true;
        } else {
            canCopy = false;
        }
        
        // Inform window sys there is DnD about to start.
        // XXX Using the firstTC in DnD manager is a hack.
        windowDnDManager.dragStarting(startingDroppable, startingPoint, firstTC);

        Cursor cursor = hackUserDropAction == DnDConstants.ACTION_MOVE
            ? getDragCursor(startingComp, CURSOR_MOVE)
            : (canCopy 
                ? getDragCursor(startingComp, CURSOR_COPY)
                : getDragCursor(startingComp, CURSOR_COPY_NO_MOVE));

        // Sets listnening for ESC key.
        addListening();
        hackESC = false;
        
        Tabbed tabbed = (Tabbed) SwingUtilities.getAncestorOfClass (Tabbed.class,
            startingComp);
        if (tabbed == null) {
            Tabbed.Accessor acc = (Tabbed.Accessor) SwingUtilities.getAncestorOfClass (Tabbed.Accessor.class,
                                                                                       startingComp);
            tabbed = acc != null ? acc.getTabbed() : null;
        }
        
        Image img = null;
        if (tabbed != null && Constants.SWITCH_USE_DRAG_IMAGES) {
            int idx = tabbed.indexOf(firstTC);
            img = tabbed.createImageOfTab(idx);
        }
        try {
            evt.startDrag(
                cursor,
                img,
                new Point (0,0), 
                (transfer instanceof TopComponent
                        ? (Transferable)new TopComponentTransferable(
                                (TopComponent)transfer)
                        : (Transferable)new TopComponentArrayTransferable(
                                (TopComponent[])transfer)),
                this
            );
        } catch(InvalidDnDOperationException idoe) {
            Logger.getLogger(TopComponentDragSupport.class.getName()).log(Level.WARNING, null, idoe);
            
            removeListening();
            windowDnDManager.resetDragSource();
        }
    }

    private AWTEventListener keyListener = new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                KeyEvent keyevent = (KeyEvent)event;
                
                if (keyevent.getID() == KeyEvent.KEY_RELEASED && 
                    keyevent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hackESC = true;
                }                
            }
            
        };
    /** Adds <code>KeyListener</code> to container and its component
     * hierarchy to listen for ESC key. */
    private void addListening() {
        Toolkit.getDefaultToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);
    }
    
    /** Removes ESC listening. Helper method. */
    private void removeListening() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(keyListener);
    }
    
    // >> DragSourceListener implementation >>
    /** Implements <code>DragSourceListener</code> method.
     * It just refreshes the weak reference of <code>DragSourceContext</code>
     * for the sake of setSuccessCursor method.
     * The excpected code, changing of cursor, is done in setSuccessCursor method
     * due to an undeterministic calls of this method especially in MDI mode.
     * @see #setSuccessCursor */
    public void dragEnter(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragEnter");// NOI18N
        }
            
        // Just refresh the weak ref to the context if necessary.
        // The expected code here is done by ragExitHack method called from DropTarget's.
        if(dragContextWRef.get() == null) {
            dragContextWRef = new java.lang.ref.WeakReference<DragSourceContext>(evt.getDragSourceContext());
        }
    }

    /** Dummy implementation of <code>DragSourceListener</code> method. */
    public void dragOver(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragOver"); // NOI18N
        }
    }

    /** Implements <code>DragSourceListener</code> method.
     * It just refreshes the weak reference of <code>DragSourceContext</code>
     * for the sake of setUnsuccessCursor method.
     * The excpected code, changing of cursor, is done in setUnsuccessCursor method
     * due to an undeterministic calls of this method especially in MDI mode.
     * @see #setUnsuccessCursor */
    public void dragExit(DragSourceEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragExit"); // NOI18N
        }
        
        // Just refresh the weak ref to the context if necessary.
        // The expected code here is done by ragExitHack method called from DropTarget's.
        if(dragContextWRef.get() == null) {
              dragContextWRef = new WeakReference<DragSourceContext>(evt.getDragSourceContext());
        }
    }
    
    /** Implements <code>DragSourceListener</code> method.
     * It changes the cursor type from copy to move and bakc accordting the
     * user action. */
    public void dropActionChanged(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dropActionChanged"); // NOI18N
        }
        String name = evt.getDragSourceContext().getCursor().getName();
        
        if(name == null) {
            // Not our cursor??
            return;
        }

        // For us is the user action important.
        int userAction = evt.getUserAction();

        // Consider NONE action as MOVE one.
        if(userAction == DnDConstants.ACTION_NONE) {
            userAction = DnDConstants.ACTION_MOVE;
        }
        // #21918. See above.
        hackUserDropAction = userAction;
        
        int type;
        if((NAME_CURSOR_COPY.equals(name)
        || NAME_CURSOR_COPY_NO_MOVE.equals(name))
        && userAction == DnDConstants.ACTION_MOVE) {
            type = CURSOR_MOVE;
        } else if(NAME_CURSOR_COPY_NO.equals(name)
        && userAction == DnDConstants.ACTION_MOVE) {
            type = CURSOR_MOVE_NO;
        } else if(NAME_CURSOR_MOVE.equals(name)
        && userAction == DnDConstants.ACTION_COPY) {
            type = CURSOR_COPY;
        } else if(NAME_CURSOR_MOVE_NO.equals(name)
        && userAction == DnDConstants.ACTION_COPY) {
            type = CURSOR_COPY_NO;
        } else {
            return;
        }

        // There can't be copy operation performed,
        // transferreed TopComponent in not of TopComponent.Cloneable instance.
        if(type == CURSOR_COPY && !canCopy) {
            type = CURSOR_COPY_NO_MOVE;
        }

        // Check if there is already our cursor.
        if(getDragCursorName(type).equals(
        evt.getDragSourceContext().getCursor().getName())) {
            return;
        }
        
        evt.getDragSourceContext().setCursor(getDragCursor(evt.getDragSourceContext().getComponent(),type));
    }

    /** Implements <code>DragSourceListener</code> method.
     * Informs window dnd manager the drag operation finished.
     * @see WindowDnDManager#dragFinished */
    public void dragDropEnd(final DragSourceDropEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragDropEnd"); // NOI18N
        }
        
        windowDnDManager.dragFinished();
        
        try {
            if(checkDropSuccess(evt)) {
                removeListening();
                return;
            }
            
            // Now simulate drop into "free" desktop area.
            
            // Finally schedule the "drop" task later to be able to
            // detect if ESC was pressed.
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(createDropIntoFreeAreaTask(
                            evt, evt.getLocation()));
                }},
                250 // XXX #21918, Neccessary to skip after possible ESC key event.
            );
        } finally {
            windowDnDManager.dragFinishedEx();
        }
    }
    // << DragSourceListener implementation <<

    /** Checks whether there was a successfull drop. */
    private boolean checkDropSuccess(DragSourceDropEvent evt) {
        // XXX #21917.
        if(windowDnDManager.isDropSuccess()) {
            return true;
        }
        
        // Gets location.
        Point location = evt.getLocation();
        if(location == null) {
            return true;
        }
        
        if(WindowDnDManager.isInMainWindow(location)
        || windowDnDManager.isInFloatingFrame(location)
        || WindowDnDManager.isAroundCenterPanel(location)) {
            return false;
        }
//        else if(evt.getDropSuccess()) {
//            return true;
//        } // PENDING it seem it is not working correctly (at least on linux).
        return false;
    }
    
    /** Creates task which performs actual drop into "free area", i.e. it
     * creates new separated (floating) window. */
    private Runnable createDropIntoFreeAreaTask(final DragSourceDropEvent evt,
    final Point location) {
        return new Runnable() {
            public void run() {
                // XXX #21918. Don't move the check sooner
                // (before the enclosing blocks), it would be invalid.
                if(hackESC) {
                    removeListening();
                    return;
                }

                TopComponent[] tcArray = WindowDnDManager.extractTopComponent(
                    false,
                    evt.getDragSourceContext().getTransferable()
                );
                
                // Provide actual drop into "free" desktop area.
                if(tcArray != null) {
                    // XXX there is a problem if jdk dnd framework sets as drop action
                    // ACTION_NONE, there is not called drop event on DropTargetListener,
                    // even it is there.
                    // Performs hacked drop action, simulates ACTION_MOVE when
                    // system set ACTION_NONE (which we do not use).
                    windowDnDManager.tryPerformDrop(
                        windowDnDManager.getController(),
                        windowDnDManager.getFloatingFrames(),
                        location,
                        DnDConstants.ACTION_MOVE, // MOVE only
                        evt.getDragSourceContext().getTransferable());
                }
            }
        };
    }

    /** Gets bounds for the new mode created in the "free area". */
    private static Rectangle getBoundsForNewMode(TopComponent tc, Point location) {
        int width = tc.getWidth();
        int height = tc.getHeight();
        
        // Take also the native title and borders into account.
        java.awt.Window window = SwingUtilities.getWindowAncestor(tc);
        if(window != null) {
            java.awt.Insets ins = window.getInsets();
            width += ins.left + ins.right;
            height += ins.top + ins.bottom;
        }
        // PENDING else { how to get the insets of newly created window? }

        Rectangle tcBounds = tc.getBounds();
        Rectangle initBounds = new Rectangle(
            location.x,
            location.y,
            width,
            height
        );

        return initBounds;
    }
    
    /** Hacks problems with <code>dragEnter</code> wrong method calls.
     * It plays its role. Sets the cursor from 'no-drop' state
     * to its 'drop' state sibling.
     * @param freeArea true when mouse pointer in free screen area
     * @see #dragEnter */
    void setSuccessCursor (boolean freeArea) {
        int dropAction = hackUserDropAction;
        DragSourceContext ctx = dragContextWRef.get();
        
        if(ctx == null) {
            return;
        }

        int type;
        if(dropAction == DnDConstants.ACTION_MOVE) {
            type = freeArea ? CURSOR_MOVE_FREE : CURSOR_MOVE;
        } else if(dropAction == DnDConstants.ACTION_COPY) {
            if(canCopy) {
                type = CURSOR_COPY;
            } else {
                type = CURSOR_COPY_NO_MOVE;
            }
        } else {
            // PENDING throw exception?
            Logger.getLogger(TopComponentDragSupport.class.getName()).log(Level.WARNING, null,
                              new java.lang.IllegalStateException("Invalid action type->" +
                                                                  dropAction)); // NOI18N
            return;
        }

        // Check if there is already our cursor.
        if(getDragCursorName(type).equals(ctx.getCursor().getName())) {
            return;
        }

        ctx.setCursor(getDragCursor(ctx.getComponent(),type));
    }
    
    /** Hacks problems with <code>dragExit</code> wrong method calls.
     * It plays its role. Sets the cursor from 'drop' state
     * to its 'no-drop' state sibling.
     * @see #dragExit */
    void setUnsuccessCursor() {
        DragSourceContext ctx = dragContextWRef.get();
        
        if(ctx == null) {
            return;
        }
        
        String name = ctx.getCursor().getName();
        
        int type;
        if(NAME_CURSOR_COPY.equals(name)
        || NAME_CURSOR_COPY_NO_MOVE.equals(name)) {
            type = CURSOR_COPY_NO;
        } else if(NAME_CURSOR_MOVE.equals(name) || NAME_CURSOR_MOVE_NO.equals(name)) {
            type = CURSOR_MOVE_NO;
        } else {
            return;
        }

        ctx.setCursor(getDragCursor(ctx.getComponent(),type));
    }

    /** Provides cleanup when finished drag operation. Ideally the code
     * should reside in {@ling #dragDropEnd} method only. But that one
     * is not called in case of error in DnD framework. */
    void dragFinished() {
        dragContextWRef = new WeakReference<DragSourceContext>(null);
    }
   
    private static void debugLog(String message) {
        Debug.log(TopComponentDragSupport.class, message);
    }
    
    // Helpers>>
    /** Gets window drag <code>Cursor</code> of specified type. Utility method.
     * @param type valid one of {@link #CURSOR_COPY}, {@link #CURSOR_COPY_NO}, 
     *             {@link #CURSOR_MOVE}, {@link #CURSOR_MOVE_NO}, {@link #CURSOR_MOVE_FREE} */
    private static String getDragCursorName(int type) {
        if(type == CURSOR_COPY) {
            return NAME_CURSOR_COPY;
        } else if(type == CURSOR_COPY_NO) {
            return NAME_CURSOR_COPY_NO;
        } else if(type == CURSOR_MOVE) {
            return NAME_CURSOR_MOVE;
        } else if(type == CURSOR_MOVE_NO) {
            return NAME_CURSOR_MOVE_NO;
        } else if(type == CURSOR_COPY_NO_MOVE) {
            return NAME_CURSOR_COPY_NO_MOVE;
        } else if(type == CURSOR_MOVE_FREE) {
            return NAME_CURSOR_MOVE_FREE;
        } else {
            return null;
        }
    }
    
    /** Gets window drag <code>Cursor</code> of specified type. Utility method.
     * @param type valid one of {@link #CURSOR_COPY}, {@link #CURSOR_COPY_NO}, 
     *             {@link #CURSOR_MOVE}, {@link #CURSOR_MOVE_NO}, {@link #CURSOR_MOVE_FREE}
     * @exception IllegalArgumentException if invalid type parameter passed in */
    private static Cursor getDragCursor( Component comp, int type ) {
        Image image = null;
        String name = null;
        
        if(type == CURSOR_COPY) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopy.gif"); // NOI18N
            name = NAME_CURSOR_COPY;
        } else if(type == CURSOR_COPY_NO) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopyNo.gif"); // NOI18N
            name = NAME_CURSOR_COPY_NO;
        } else if(type == CURSOR_MOVE) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragMove.gif"); // NOI18N
            name = NAME_CURSOR_MOVE;
        } else if(type == CURSOR_MOVE_NO) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragMoveNo.gif"); // NOI18N
            name = NAME_CURSOR_MOVE_NO;
        } else if(type == CURSOR_COPY_NO_MOVE) {
            image = Utilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopyNo.gif"); // NOI18N
            name = NAME_CURSOR_COPY_NO_MOVE;
        } else if(type == CURSOR_MOVE_FREE) {
            image = Utilities.loadImage(
                "org/netbeans/core/windows/resources/topComponentDragMoveFreeArea.gif"); // NOI18N
            name = NAME_CURSOR_MOVE_FREE;
        } else {
            throw new IllegalArgumentException("Unknown cursor type=" + type); // NOI18N
        }
        
        return Utilities.createCustomCursor( comp, image, name );
    }

    // Helpers<<
    
    /** <code>Transferable</code> used for <code>TopComponent</code> instances
     * to be used in window system DnD. */
    private static class TopComponentTransferable extends Object
    implements Transferable {

        // #86564: Hold TopComponent weakly to workaround AWT bug #6555816
        /** <code>TopComponent</code> to be transferred. */
        private WeakReference<TopComponent> weakTC;

        
        /** Crates <code>Transferable</code> for specified <code>TopComponent</code> */
        public TopComponentTransferable(TopComponent tc) {
            this.weakTC = new WeakReference<TopComponent>(tc);
        }

        
        // >> Transferable implementation >>
        /** Implements <code>Transferable</code> method.
         * @return <code>TopComponent</code> instance for <code>DataFlavor</code>
         * with mimetype equal to {@link #MIME_TOP_COMPONENT} or if mimetype
         * equals to {@link #MIME_CLONEABLE_TOP_COMPONENT} and the top component
         * is instance of <code>TopComponent.Cloneable</code> returns the instance */
        public Object getTransferData(DataFlavor df) {
            TopComponent tc = weakTC.get();
            if(MIME_TOP_COMPONENT.equals(df.getMimeType())) {
                return tc;
            } else if(MIME_TOP_COMPONENT_CLONEABLE.equals(
                df.getMimeType())
            && tc instanceof TopComponent.Cloneable) {
                return tc;
            }

            return null;
        }

        /** Implements <code>Transferable</code> method.
         * @return Array of <code>DataFlavor</code> with mimetype
         * {@link #MIME_TOP_COMPONENT} and also with mimetype
         * {@link #MIME_CLONEABLE_TOP_COMPONENT}
         * if the <code>tc</code> is instance
         * of <code>TopComponent.Cloneable</code> */
        public DataFlavor[] getTransferDataFlavors() {
            try {
                TopComponent tc = weakTC.get();
                if(tc instanceof TopComponent.Cloneable) {
                    return new DataFlavor[]{
                        new DataFlavor(MIME_TOP_COMPONENT, null, TopComponent.class.getClassLoader()),
                        new DataFlavor(MIME_TOP_COMPONENT_CLONEABLE, null, TopComponent.Cloneable.class.getClassLoader())};
                } else {
                    return new DataFlavor[] {
                        new DataFlavor(MIME_TOP_COMPONENT, null, TopComponent.class.getClassLoader())
                    };
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TopComponentDragSupport.class.getName()).log(
                        Level.WARNING, ex.getMessage(), ex);
            }
            return new DataFlavor[0];
        }

        /** Implements <code>Transferable</code> method.
         * @return <code>true</code> for <code>DataFlavor</code> with mimetype
         * equal to {@link #MIME_TOP_COMPONENT}
         * and if <code>tc</code> is instance
         * of <code>TopComponent.Cloneable</code> also for the one
         * with mimetype {@link #MIME_TOP_COMPONENT_CLONEABLE},
         * <code>false</code> otherwise */
        public boolean isDataFlavorSupported(DataFlavor df) {
            TopComponent tc = weakTC.get();
            if(MIME_TOP_COMPONENT.equals(df.getMimeType())) {
                return true;
            } else if(MIME_TOP_COMPONENT_CLONEABLE.equals(
                df.getMimeType())
            && tc instanceof TopComponent.Cloneable) {
                return true;
            }

            return false;
        }
        // << Transferable implementation <<
    } // End of class TopComponentTransferable.

    /** <code>Transferable</code> used for <code>TopComponent</code> instances
     * to be used in window system DnD. */
    private static class TopComponentArrayTransferable extends Object
    implements Transferable {

        // #86564: Hold TopComponents weakly to workaround AWT bug #6555816
        /** <code>TopComponent</code> to be transferred. */
        private List<WeakReference<TopComponent>> weakTCList;

        
        /** Crates <code>Transferable</code> for specified <code>TopComponent</code> */
        public TopComponentArrayTransferable(TopComponent[] tcArray) {
            this.weakTCList = new ArrayList<WeakReference<TopComponent>>();
            for (TopComponent topComponent : tcArray) {
                weakTCList.add(new WeakReference<TopComponent>(topComponent));
            }
        }
        
        // >> Transferable implementation >>
        /** Implements <code>Transferable</code> method.
         * @return <code>TopComponent</code> instance for <code>DataFlavor</code>
         * with mimetype equal to {@link #MIME_TOP_COMPONENT}
         * or if mimetype equals to
         * {@link #MIME_CLONEABLE_TOP_COMPONENT} and
         * the top component is instance
         * of <code>TopComponent.Cloneable</code> returns the instance. */
        public Object getTransferData(DataFlavor df) {
            if(MIME_TOP_COMPONENT_ARRAY.equals(df.getMimeType())) {
                List<TopComponent> tcList = new ArrayList<TopComponent>(weakTCList.size());
                TopComponent curTC = null;
                for (WeakReference<TopComponent> weakTC : weakTCList) {
                    curTC = weakTC.get();
                    if (curTC != null) {
                        tcList.add(curTC);
                    }
                }
            }
            return null;
        }

        /** Implements <code>Transferable</code> method.
         * @return Array of <code>DataFlavor</code> with mimetype
         * {@link #MIME_TOP_COMPONENT} and also with mimetype
         * {@link #MIME_CLONEABLE_TOP_COMPONENT}
         * if the <code>tc</code> is 
         * instance of <code>TopComponent.Cloneable</code> */
        public DataFlavor[] getTransferDataFlavors() {
            try {
                return new DataFlavor[]{new DataFlavor(MIME_TOP_COMPONENT_ARRAY,
                                                       null,
                                                       TopComponent.class.getClassLoader())};
            }
            catch (ClassNotFoundException ex) {
                Logger.getLogger(TopComponentDragSupport.class.getName()).log(
                        Level.WARNING, ex.getMessage(), ex);
            }
            return new DataFlavor[0];
        }

        /** Implements <code>Transferable</code> method.
         * @return <code>true</code> for <code>DataFlavor</code> with mimetype
         * equal to {@link #MIME_TOP_COMPONENT}
         * and if <code>tc</code> is instance
         * of <code>TopComponent.Cloneable</code> also for the one
         * with mimetype {@link #MIME_TOP_COMPONENT_CLONEABLE},
         * <code>false</code> otherwise. */
        public boolean isDataFlavorSupported(DataFlavor df) {
            if(MIME_TOP_COMPONENT_ARRAY.equals(
            df.getMimeType())) {
                return true;
            }

            return false;
        }
        // << Transferable implementation <<
    } // End of class TopComponentArrayTransferable.

    
    /** Fake <code>DragGestureRecognizer</code> used when starting
     * DnD programatically. */
    private static class FakeDragGestureRecognizer extends DragGestureRecognizer {

        /** Constructs <code>FakeDragGestureRecpgnizer</code>.
         * @param evt trigger event */
        public FakeDragGestureRecognizer(WindowDnDManager windowDnDManager, MouseEvent evt) {
            super(windowDnDManager.getWindowDragSource(),
                (Component)evt.getSource(), DnDConstants.ACTION_COPY_OR_MOVE, null);

            appendEvent(evt);
        }

        /** Dummy implementation of superclass abstract method. */
        public void registerListeners() {}
        /** Dummy implementation of superclass abstract method. */
        public void unregisterListeners() {}
        
    } // End of class FakeDragGestureRecognizer

    
    /**
     * Ugly fake class to pass by the issue #4752224. There is not possible
     * to create DataFlavor of mime type application/x-java-jvm-local-objectref 
     * for array class type. */
    static class TopComponentArray {
    } // End of TopComponentArray.
    
}
