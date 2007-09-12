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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import com.nwoods.jgo.JGoView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasObjectFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;

/**
 * <p>
 *
 * Title: </p> BasicCanvasObjectFactory <p>
 *
 * Description: </p> BasicCanvasObjectFactory provides a basic implemenation of
 * ICanvasObjectFactory<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 * @version   1.0
 */
public class BasicCanvasObjectFactory
     implements ICanvasObjectFactory {

    /**
     * The default x location of a canvas node. (from the border of the view port)
     */
    private static final int DEFAULT_X = 20;

    /**
     * The default y location of a canvas node. (from the border of the view port)
     */
    private static final int DEFAULT_Y = 20;

    /**
     * the canvas contains this factory
     */
    private ICanvasView mCanvas;

    /**
     * Creates a new BasicCanvasObjectFactory object.
     */
    public BasicCanvasObjectFactory() { }

    /**
     * Return the canvas contains this factory
     *
     * @return   the canvas contains this factory
     */
    public ICanvasView getMapperCanvas() {
        return mCanvas;
    }

    /**
     * Set the canvas contains this factory
     *
     * @param canvas  the canvas contains this factory
     */
    public void setMapperCanvas(ICanvasView canvas) {
        mCanvas = canvas;
    }

    /**
     * Return a new canvas field node repersenting the specified mapper field
     * node.
     *
     * @param fieldNode  Description of the Parameter
     * @return           a new canvas field node repersenting the specified
     *      mapper field node.
     */
    public ICanvasFieldNode createFieldNode(IFieldNode fieldNode) {
        ICanvasFieldNode fieldCanvasNode = new BasicCanvasFieldNode(fieldNode);
        fieldCanvasNode.setMapperCanvas(mCanvas);

        return fieldCanvasNode;
    }

    /**
     * Return a new canvas methoid node repersenting the specified mapper
     * methoid node.
     *
     * @param methoidNode  Description of the Parameter
     * @return             a new canvas methoid node repersenting the specified
     *      mapper methoid node.
     */
    public ICanvasMethoidNode createMethoidNode(IMethoidNode methoidNode) {
        BasicCanvasMethoidNode canvasMethoidNode = new BasicCanvasMethoidNode(methoidNode);
        IFieldNode fieldNode = (IFieldNode) methoidNode.getFirstNode();
        IMethoid methoid = (IMethoid) methoidNode.getMethoidObject();
        
        while (fieldNode != null) {
            ICanvasFieldNode canvasFieldNode = createFieldNode(fieldNode);
            canvasMethoidNode.addNode(canvasFieldNode);
            if (methoid != null) {
                if (methoid.isLiteral()) {
                    fieldNode.setLiteralName(fieldNode.getName());
                }
            }
            fieldNode = (IFieldNode) methoidNode.getNextNode(fieldNode);
        }

        canvasMethoidNode.setMapperCanvas(mCanvas);

        int x = methoidNode.getX();
        int y = methoidNode.getY();

        if (mCanvas instanceof JGoView) {
            if (x < 0) {
                x = ((JGoView) mCanvas).getViewPosition().x + DEFAULT_X;
            }
            if (y < 0) {
                y = ((JGoView) mCanvas).getViewPosition().y + DEFAULT_Y;
            }
        }
        canvasMethoidNode.setLocation(x,y);

        return canvasMethoidNode;
    }

    /**
     * Return a new canvas link repersenting the specified mapper link.
     *
     * @param link  the mapper link
     * @return      a new canvas link repersenting the specified mapper link.
     */
    public ICanvasMapperLink createLink(IMapperLink link) {
        ICanvasMapperLink canvasLink = null;

        if (link.getStartNode() instanceof IMapperTreeNode
            && link.getEndNode() instanceof IMapperTreeNode) {
            canvasLink = new BasicCanvasTreeToTreeLink(link);
        } else if (link.getStartNode() instanceof IMapperTreeNode
            && link.getEndNode() instanceof IFieldNode) {
            canvasLink = new BasicCanvasTreeToNodeLink(link);
        } else if (link.getStartNode() instanceof IFieldNode
            && link.getEndNode() instanceof IMapperTreeNode) {
            canvasLink = new BasicCanvasNodeToTreeLink(link);
        } else {
            canvasLink = new BasicCanvasNodeToNodeLink(link);
        }

        canvasLink.setMapperCanvas(mCanvas);

        return canvasLink;
    }
}
