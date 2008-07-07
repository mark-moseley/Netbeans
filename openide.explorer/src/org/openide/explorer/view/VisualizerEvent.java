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
package org.openide.explorer.view;

import org.openide.nodes.*;

import java.util.Comparator;
import java.util.EventObject;
import java.util.LinkedList;


/** Event describing change in a visualizer. Runnable to be added into
* the event queue.
*
* @author Jaroslav Tulach
*/
abstract class VisualizerEvent extends EventObject {
    /** indices */
    int[] array;

    public VisualizerEvent(VisualizerChildren ch, int[] array) {
        super(ch);
        this.array = array;
    }

    /** Getter for changed indexes */
    public final int[] getArray() {
        return array;
    }

    /** Getter for the children list.
    */
    public final VisualizerChildren getChildren() {
        return (VisualizerChildren) getSource();
    }

    /** Getter for the visualizer.
    */
    public final VisualizerNode getVisualizer() {
        return getChildren().parent;
    }

    /** Class for notification of adding of nodes that can be passed into
    * the event queue and in such case notifies all listeners in Swing Dispatch Thread
    */
    static final class Added extends VisualizerEvent implements Runnable {
        static final long serialVersionUID = 5906423476285962043L;

        /** Constructor for nodes adding notification.
        * @param ch children
        * @param idxs indicies of added nodes
        */
        public Added(VisualizerChildren ch, int[] idxs) {
            super(ch, idxs);
        }

        /** Process the event
        */
        public void run() {
            super.getChildren().added(this);
        }
    }

    /** Class for notification of removing of nodes that can be passed into
    * the event queue and in such case notifies all listeners in Swing Dispatch Thread
    */
    static final class Removed extends VisualizerEvent implements Runnable {
        static final long serialVersionUID = 5102881916407672392L;

        /** linked list of removed nodes, that is filled in getChildren ().removed () method
        */
        public LinkedList<VisualizerNode> removed = new LinkedList<VisualizerNode>();

        /** Constructor for nodes removal notification.
        * @param ch children
        * @param idxs indicies of added nodes
        */
        public Removed(VisualizerChildren ch, int[] idxs) {
            super(ch, idxs);
        }

        /** Process the event
        */
        public void run() {
            super.getChildren().removed(this);
        }
    }

    /** Class for notification of reordering of nodes that can be passed into
    * the event queue and in such case notifies all listeners in Swing Dispatch Thread
    */
    static final class Reordered extends VisualizerEvent implements Runnable {
        static final long serialVersionUID = -4572356079752325870L;
        private Comparator<VisualizerNode> comparator = null;

        /** Constructor for nodes reordering notification.
        * @param ch children
        * @param indx indicies of added nodes
        */
        public Reordered(VisualizerChildren ch, int[] idxs) {
            super(ch, idxs);
        }

        //#37802 - provide a way to just send a comparator along to do the 
        //sorting
        Reordered(VisualizerChildren ch, Comparator<VisualizerNode> comparator) {
            this(ch, new int[0]);
            this.comparator = comparator;
        }

        public Comparator<VisualizerNode> getComparator() {
            return comparator;
        }

        /** Process the event
        */
        public void run() {
            super.getChildren().reordered(this);
        }
    }
}
