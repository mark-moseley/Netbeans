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

package org.netbeans.modules.soa.mapper.common.gtk;

import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.List;


/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public interface ICanvasController {

    /**
     * node name change
     */
    int NODE_NAME_CHANGE = 1;

    /**
     * Set the canvas.
     *
     * @param canvas  the canvas this controller is controlling.
     */
    void setCanvas(ICanvas canvas);

    /**
     * Sets the view manager
     *
     * @param viewManager - the view manager
     */
    void setViewManager(Object viewManager);

    /**
     * Retrieves the view manager
     *
     * @return Object
     */
    Object getViewManager();

    /**
     * Sets the data model
     *
     * @param model - the data model
     */
    void setDataModel(Object model);

    /**
     * Retrieves the data model
     *
     * @return Object
     */
    Object getDataModel();

    /**
     * Handles mouse pressed
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean handleMouseDown(ICanvasMouseData data);

    /**
     * Handles when mouse us up
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean handleMouseUp(ICanvasMouseData data);

    /**
     * Handles when mouse is double clicked
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean handleMouseDblClick(ICanvasMouseData data);

    /**
     * Handles when mouse is moved
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean handleMouseMove(ICanvasMouseData data);

    /**
     * Handles when mouse clicks
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean handleMouseClick(ICanvasMouseData data);

    /**
     * Handles delete selection
     *
     * @return boolean
     */
    boolean handleDeleteSelection();

    /**
     * Handles canvas updates
     *
     * @param id - the id
     * @param dataList - the data list
     * @return  boolean
     */
    boolean handleCanvasUpdates(int id, List dataList);

    /**
     * Handles add links
     *
     * @param fromNode - the from canvas node
     * @param toNode - the to canvas node
     * @return  boolean
     */
    boolean handleAddLink(ICanvasNode fromNode, ICanvasNode toNode);

    /**
     * Handles add links
     *
     * @param fromNode - the from canvas node
     * @param toNode - the to canvas node
     * @param isComponentNode - signifies if component is a node
     * @param isWithBinding  - signifies if component is with a binding
     * @return boolean
     */
    boolean handleAddLink(ICanvasNode fromNode, ICanvasNode toNode,
        boolean isComponentNode, boolean isWithBinding);

    /**
     * Handles add links
     *
     * @param node - the canvas node
     * @param documentLocation - model location of where to put the node
     * @param viewLocation - visible view location of where action occurred
     * @return boolean
     */
    boolean handleAddLink(ICanvasNode node, Point modelLocation, Point viewLocation, int action);
    
    /**
     * Handles update links
     *
     * @param fromNode - the from canvas node
     * @param toNode - the to canvas node
     * @param isComponentNode - signifies if component is a node
     * @param isWithBinding  - signifies if component is with a binding
     * @return boolean
     */
    boolean updateLink(ICanvasNode fromNode, ICanvasNode toNode,
        ICanvasNode mSourceNode, boolean isWithBinding);
    
    boolean handleDragDropEnd(DragSourceDropEvent event);

    void handleDragEnter(DropTargetDragEvent dtde);
    
    void handleDragExit(DropTargetEvent dte);
    
    void handleDragGestureRecognized(DragGestureEvent dge);
    
    boolean handleDragOver(DropTargetDragEvent event);

    boolean handleDrop(DropTargetDropEvent event);
}
