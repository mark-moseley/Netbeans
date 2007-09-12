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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.soa.mapper.common.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.io.IOException;


import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * Description of the Class
 *
 * @author    htung
 * @created   October 2, 2002
 */
public class DNDList extends JList
     implements DNDComponentInterface,
    DropTargetListener,
    DragSourceListener,
    DragGestureListener {

    /**
     * enables this component to be a mDropTarget
     */
    private DropTarget mDropTarget = null;

    /**
     * enables this component to be a Drag Source
     */
    private DragSource mDragSource = null;

    /**
     * Description of the Field
     */
    protected DataFlavor[] mDataFlavorArray = new DataFlavor[1];

    /**
     * constructor - initializes the DropTarget and DragSource.
     */

    public DNDList() {
        super();
        init();
    }

    /**
     * Constructor for the DNDList object
     *
     * @param model  Description of the Parameter
     */
    public DNDList(ListModel model) {
        super(model);
        init();
    }

    /**
     * Constructor for the DNDList object
     *
     * @param vector  Description of the Parameter
     */
    public DNDList(Vector vector) {
        super(vector);
        init();
    }

    /**
     * Constructor for the DNDList object
     *
     * @param objs  Description of the Parameter
     */
    public DNDList(Object[] objs) {
        super(objs);
        init();
    }

    /**
     * initialize the drag and drop
     */
    protected void init() {
        mDropTarget = new DropTarget(this, this);
        mDragSource = new DragSource();
        mDragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        try {
            mDataFlavorArray[0] =
                new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * a drag gesture has been initiated
     *
     * @param event  Description of the Parameter
     */
    public void dragGestureRecognized(DragGestureEvent event) {

        Object selected = getSelectedValue();
        if (selected != null) {

            StringSelection text = new StringSelection(selected.toString());
            // as the name suggests, starts the dragging
            mDragSource.startDrag(event, DragSource.DefaultMoveDrop,
                text, this);
        } else {
            System.out.println("nothing was selected");
        }
    }

    /**
     * is invoked when you are dragging over the DropSite
     *
     * @param event  Description of the Parameter
     */

    public void dragEnter(DropTargetDragEvent event) {

        // debug messages for diagnostics
        System.out.println("dragEnter");
        event.acceptDrag(DnDConstants.ACTION_COPY);
    }

    /**
     * is invoked when you are exit the DropSite without dropping
     *
     * @param event  Description of the Parameter
     */

    public void dragExit(DropTargetEvent event) {
        System.out.println("dragExit");

    }

    /**
     * is invoked when a drag operation is going on
     *
     * @param event  Description of the Parameter
     */

    public void dragOver(DropTargetDragEvent event) {
        System.out.println("dragOver");
    }

    /**
     * a drop has occurred
     *
     * @param event  Description of the Parameter
     */

    public void drop(DropTargetDropEvent event) {

        try {
            Transferable transferable = event.getTransferable();

            // currently, accept only Strings
            // will change to accept repostiory component
            if (transferable.isDataFlavorSupported(mDataFlavorArray[0])) {

                event.acceptDrop(DnDConstants.ACTION_COPY);
                String s = (String) transferable.getTransferData(mDataFlavorArray[0]);
                addElement(s);
                event.getDropTargetContext().dropComplete(true);
            } else {
                event.rejectDrop();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Exception" + exception.getMessage());
            event.rejectDrop();
        } catch (UnsupportedFlavorException ufException) {
            ufException.printStackTrace();
            System.err.println("Exception" + ufException.getMessage());
            event.rejectDrop();
        }
    }

    /**
     * is invoked if the use modifies the current drop gesture
     *
     * @param event  Description of the Parameter
     */
    public void dropActionChanged(DropTargetDragEvent event) { }

    /**
     * this message goes to DragSourceListener, informing it that the
     * dragging has ended
     *
     * @param event  Description of the Parameter
     */
    public void dragDropEnd(DragSourceDropEvent event) {
        if (event.getDropSuccess()) {
            System.out.println(" dragDropEnd -> DropSuccess");
            //removeElement();
        }
    }

    /**
     * this message goes to DragSourceListener, informing it that the
     * dragging has entered the DropSite
     *
     * @param event  Description of the Parameter
     */
    public void dragEnter(DragSourceDragEvent event) {
        System.out.println(" dragEnter");
    }

    /**
     * this message goes to DragSourceListener, informing it that the
     * dragging has exited the DropSite
     *
     * @param event  Description of the Parameter
     */
    public void dragExit(DragSourceEvent event) {
        System.out.println("dragExit");
    }

    /**
     * this message goes to DragSourceListener, informing it that the
     * dragging is currently ocurring over the DropSite
     *
     * @param event  Description of the Parameter
     */
    public void dragOver(DragSourceDragEvent event) {
        System.out.println("dragExit");
    }

    /**
     * is invoked when the user changes the dropAction
     *
     * @param event  Description of the Parameter
     */
    public void dropActionChanged(DragSourceDragEvent event) {
        System.out.println("dropActionChanged");
    }

    /**
     * adds elements to itself
     *
     * @param s  The feature to be added to the Element attribute
     */
    public void addElement(Object s) {
        ((DefaultListModel) getModel()).addElement(s.toString());
    }

    /**
     * removes an element from itself
     */
    public void removeElement() {
        ((DefaultListModel) getModel()).removeElement(getSelectedValue());
    }

}
