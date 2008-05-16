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
package org.netbeans.modules.visual.router;

import java.util.EnumSet;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.CollisionsCollector;
import org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector;
import org.netbeans.api.visual.widget.ConnectionWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.visual.anchor.Anchor.Direction;
import org.netbeans.api.visual.anchor.Anchor.Result;

/**
 * @author David Kaspar
 */
public final class OrthogonalSearchRouter implements Router {

    static final int SPACING_EDGE = 8;
    static final int SPACING_NODE = 16;

    private CollisionsCollector collector;
    private ConnectionWidgetCollisionsCollector connectionWidgetCollector;

    public OrthogonalSearchRouter (CollisionsCollector collector) {
        this.collector = collector;
    }

    public OrthogonalSearchRouter (ConnectionWidgetCollisionsCollector collector) {
        this.connectionWidgetCollector = collector;
    }

    public java.util.List<Point> routeConnection(ConnectionWidget widget) {
        /* How this works:
         * 1) get the source and target anchors
         * 2) determine collisions - needed for the actual routing
         * 3) get the centers of the source and target anchor
         * 4) check to see if the anchor will allow the route to connect to
         *    any side. This really means that the router can test every 
         *    orthogonal direction out of the center of the anchor. If not
         *    allowed then get the attach point from the anchor and use it to
         *    determine the route.
         * 5) Iterate through the different allowed orthogonal directions from 
         *    the target and source to find the actual "best" route. This route
         *    or Solution with contain a list of points for the route.
         * 6) Send the list of best points to the source anchor and allow it to 
         *    alter the points. The assumption is that the anchor will only move
         *    the end point to the appropriate intersection point.
         * 7) Repeat (6) for the target.
         * 8) Now that the "best" points have been moved they may not represent
         *    an orthogonal path any longer. Calculate the directions coming from/to 
         *    the anchors in preparation for re-routing.
         * 9) Using the new endpoints(6&7) and the directions(8), re-route
         *    and get the updated solution.
         *   
         */
        Anchor sourceAnchor = widget.getSourceAnchor();
        Anchor targetAnchor = widget.getTargetAnchor();

        if (sourceAnchor == null || targetAnchor == null) {
            return Collections.emptyList();
        }

        ArrayList<Rectangle> verticalCollisions = new ArrayList<Rectangle>();
        ArrayList<Rectangle> horizontalCollisions = new ArrayList<Rectangle>();

        if (collector != null) {
            collector.collectCollisions(verticalCollisions, horizontalCollisions);
        } else {
            connectionWidgetCollector.collectCollisions(widget, verticalCollisions, horizontalCollisions);
        }

        //the default is to point to the center of any anchor. If an extended anchor
        //overwrites this class to return a different point than the center, then
        //drawing problem will exist. Meaning that the connection may draw over the
        //widget or run along its boundary.
        final Point originalSourceCenterPoint = sourceAnchor.getRelatedSceneLocation();
        final Point originalTargetCenterPoint = targetAnchor.getRelatedSceneLocation();
        Point sourcePoint = originalSourceCenterPoint;
        Point targetPoint = originalTargetCenterPoint;

        //set the default test direction to be ANY in order to test all directions
        //to find the best path.
        EnumSet<Direction> sourceDirections = Anchor.DIRECTION_ANY;
        EnumSet<Direction> targetDirections = Anchor.DIRECTION_ANY;

        //if the anchor does not allow arbitrary connection points, then ask the 
        //anchor for the attach point.
        if (!sourceAnchor.allowsArbitraryConnectionPlacement()) {
            Result sourceResult = sourceAnchor.compute(widget.getSourceAnchorEntry());
            sourceDirections = sourceResult.getDirections();
            sourcePoint = sourceResult.getAnchorSceneLocation();
        }

        if (!targetAnchor.allowsArbitraryConnectionPlacement()) {

            Result targetResult = targetAnchor.compute(widget.getTargetAnchorEntry());
            targetDirections = targetResult.getDirections();
            targetPoint = targetResult.getAnchorSceneLocation();
        }

        //set best solution to be replaced by first real solution
        Solution bestSolution = new Solution(Integer.MAX_VALUE >> 2, null);

        for (Anchor.Direction sourceDirection : sourceDirections) { //up to four choices (TOP, BOTTOM, RIGHT, LEFT)

            for (Anchor.Direction targetDirection : targetDirections) {//up to four choices (TOP, BOTTOM, RIGHT, LEFT)

                Solution solution =
                        new OrthogonalSearchRouterCore(widget.getScene(),
                        verticalCollisions, horizontalCollisions,
                        sourcePoint, sourceDirection,
                        targetPoint, targetDirection).route();

                if (solution != null && solution.compareTo(bestSolution) > 0) {
                    bestSolution = solution;
                }
            }
        }

        List<Point> bestListOfPoints = bestSolution.getPoints();

        //quick return if possible. If there is no adjusting that will be done,
        //then simply return the best points.

        if (!sourceAnchor.allowsArbitraryConnectionPlacement() && !targetAnchor.allowsArbitraryConnectionPlacement()) {
            return bestListOfPoints;
        }

        List<Point> bestPoints = bestListOfPoints;

        Direction sourceDirection = null;
        Direction targetDirection = null;
        Point firstPointOutsideAnchor = null;

        if (sourceAnchor.allowsArbitraryConnectionPlacement()) {
            //the initial route went to the center of the anchor. Now that we have the
            //"best" route, ask the anchor to where to place its end point. These calls
            //only adjust the end points within the anchor. Example would be with the 
            //rectangular anchor. The connection is meant to terminate on the boundary
            //and not at the center. Since the "best" route ends at the center, it must
            //be moved to the boundary.
            bestPoints = sourceAnchor.compute(bestListOfPoints);

            //best points now contain points that may no longer be orthogonal due to the
            //adjusting of the endpoints. In order to re-align the points, first determine
            //the direction that the points attach to the anchor and then re-route from the 
            //best attach points.
            //Note: this methodology will fail miserably if the anchor moves the points
            //around around they have been initially set. Meaning that this method assumes
            //that the only sections of the connection not orthogonal are possibly the
            //the first and last.

            firstPointOutsideAnchor = bestPoints.get(1);
            int deltaX = originalSourceCenterPoint.x - firstPointOutsideAnchor.x;

            if (deltaX == 0) { // if deltaX is zero then we have a vertical connection

                sourceDirection = originalSourceCenterPoint.y < firstPointOutsideAnchor.y ? Direction.BOTTOM : Direction.TOP;
            } else {
                sourceDirection = originalSourceCenterPoint.x < firstPointOutsideAnchor.x ? Direction.RIGHT : Direction.LEFT;
            }
        }

        //repeat the above steps for the target anchor.
        if (targetAnchor.allowsArbitraryConnectionPlacement()) {
            bestPoints = targetAnchor.compute(bestPoints);

            firstPointOutsideAnchor = bestPoints.get(bestPoints.size() - 2);
            int deltaX = originalTargetCenterPoint.x - firstPointOutsideAnchor.x;

            if (deltaX == 0) {
                targetDirection = originalTargetCenterPoint.y < firstPointOutsideAnchor.y ? Direction.BOTTOM : Direction.TOP;
            } else {
                targetDirection = originalTargetCenterPoint.x < firstPointOutsideAnchor.x ? Direction.RIGHT : Direction.LEFT;
            }

        }

        //re-route
        if (sourceAnchor.allowsArbitraryConnectionPlacement() || targetAnchor.allowsArbitraryConnectionPlacement()) {
            bestSolution = new OrthogonalSearchRouterCore(widget.getScene(),
                    verticalCollisions, horizontalCollisions,
                    bestPoints.get(0), sourceDirection,
                    bestPoints.get(bestPoints.size() - 1), targetDirection).route();
        }

        return bestSolution.getPoints();
        
    }

    static final class Solution implements Comparable<Solution> {

        private int price;
        private List<Point> points;

        public Solution (int price, List<Point> points) {
            this.price = price;
            this.points = points;
        }

        public int getPrice () {
            return price;
        }

        public List<Point> getPoints () {
            return points;
        }

        public int compareTo (Solution other) {
            return other.price - price;
        }

    }

}
