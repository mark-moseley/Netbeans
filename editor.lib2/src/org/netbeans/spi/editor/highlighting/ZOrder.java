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

package org.netbeans.spi.editor.highlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.TopologicalSortException;

/**
 * This class determines a position of a <code>HighlightsLayer</code> in relation
 * to other layers. Instances of this class are immutable.
 *
 * <p>Developers are encouraged to use the predefined z-order constants in this
 * class. Each constant refers to a rack in vertical ordering with a specific
 * purpose. The racks are ordered in the following way:
 *
 * <ul>
 * <li>{@link #TOP_RACK} - the highest rack
 * <li>{@link #SHOW_OFF_RACK}
 * <li>{@link #DEFAULT_RACK}
 * <li>{@link #CARET_RACK}
 * <li>{@link #SYNTAX_RACK}
 * <li>{@link #BOTTOM_RACK} - the lowest rack
 * </ul>
 *
 * <p>It is possible to further specify a position of a layer within a rack by
 * using the {@link #forPosition(int)} method. The <code>ZOrder</code> constants
 * for each rack use zero as the default position. Therefore calling <code>forPosition</code>
 * with a positive number will create a <code>ZOrder</code> above the rack marker
 * and using a negative number will produce <code>ZOrder</code> below the rack marker.
 * 
 * <div class="nonnormative">
 * <p>When positioning your layer you should choose the rack that suites the purpose
 * of your layer. It is possible to further define more precise ordering
 * between layers within a rack. For example, if you have two layers - one providing
 * syntactical highlighting and the other one providing semantical highlighting -
 * they both belong to the <code>SYNTAX_RACK</code>, but the semantical layer should be placed
 * above the syntactical one, because it provides 'more accurate' highlights. You could
 * define <code>ZOrder</code> of your layers like this:
 *
 * <pre>
 * ZOrder syntaxLayerZOrder = ZOrder.SYNTAX;
 * ZOrder semanticLayerZOrder = ZOrder.SYNTAX.forPosition(10);
 * </pre>
 * </div>
 *
 * @author Vita Stejskal
 */
public final class ZOrder {

    private static final Logger LOG = Logger.getLogger(ZOrder.class.getName());

    /**
     * The highest rack of z-orders. Layers in this rack will be placed at
     * the top of the hierarchy.
     */
    public static final ZOrder TOP_RACK = new ZOrder(50, 0);

    /**
     * The show off rack of z-orders. This rack is meant to be used by
     * layers with short-lived highlights that can temporarily override highlights
     * provided by other layers (eg. syntax coloring).
     */
    public static final ZOrder SHOW_OFF_RACK = new ZOrder(40, 0);
    
    /**
     * The default rack of z-orders. This rack should be used by most of the layers.
     */
    public static final ZOrder DEFAULT_RACK = new ZOrder(30, 0);
    
    /**
     * The rack for highlights showing the position of a caret.
     */
    public static final ZOrder CARET_RACK = new ZOrder(20, 0);
    
    /**
     * The syntax highlighting rack of z-order. This rack is meant to be used by
     * layers that provide highlighting of a text according to its syntactical or
     * semantical rules.
     */
    public static final ZOrder SYNTAX_RACK = new ZOrder(10, 0);
    
    /**
     * The lowest rack of z-orders. Layers in this rack will be placed at the
     * bottom of the hierarchy.
     */
    public static final ZOrder BOTTOM_RACK = new ZOrder(0, 0);

    /**
     * Sorts an array of <code>HighlightLayer</code>s by their z-order. This is
     * a convenience method that delegates to the <code>sort(Collection)</code>
     * method.
     *
     * @param layers    The array to sort.
     * 
     * @return The sorted array where layers are sorted by their z-order starting
     * with the lowest z-order and going to the highest one.
     * @throws TopologicalSortException If the array contains cycles.
     */
    /* package */ static HighlightsLayer[] sort(HighlightsLayer[] layers) throws TopologicalSortException {
        List<? extends HighlightsLayer> list = sort(Arrays.asList(layers));
        return list.toArray(new HighlightsLayer [list.size()]);
    }

    /* package */ int getRack() {
        return rack;
    }
    
    private static final Comparator<HighlightsLayer> COMPARATOR = new Comparator<HighlightsLayer>() {
        public int compare(HighlightsLayer layerA, HighlightsLayer layerB) {
            ZOrder zOrderA = layerA.getZOrder();
            ZOrder zOrderB = layerB.getZOrder();
            if (zOrderA.rack == zOrderB.rack) {
                return zOrderA.position - zOrderB.position;
            } else {
                return zOrderA.rack - zOrderB.rack;
            }
        }
    };
    
    /**
     * Sorts a collection of <code>HighlightLayer</code>s by their z-order. The layers
     * with <code>ZOrder.BOTTOM</code> will preceed all other layers in the resulting
     * list. Similarily the layers with <code>ZOrder.TOP</code> will be placed at the
     * end of the list. All the other layers will appear in between sorted by their
     * z-order.
     *
     * @param layers    The array to sort.
     *
     * @return The sorted array where layers are sorted by their z-order starting
     * with the lowest z-order and going to the highest one.
     * @throws TopologicalSortException If the collection contains cycles.
     */
    /* package */ static List<? extends HighlightsLayer> sort(
        Collection<? extends HighlightsLayer> layers
    ) throws TopologicalSortException {
        List<HighlightsLayer> sortedLayers = new ArrayList<HighlightsLayer>(layers);
        
        Collections.sort(sortedLayers, COMPARATOR);
        
        // Print the sorted layers
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Sorted layer Ids: ");
            for (HighlightsLayer layer : sortedLayers) {
                LOG.finest("    " + layer.getLayerTypeId());
            }
            LOG.finest("End of Sorted layer Ids: -----------------------");
        }
        
        return sortedLayers;
    }
    
    private final int rack;
    private final int position;
    
    /** 
     * Creates a new instance of ZOrder.
     * @param rack The z-order position of the rack.
     * @param position The z-order position within the rack.
     */
    private ZOrder(int rack, int position) {
        this.rack = rack;
        this.position = position;
    }

    /**
     * Creates a new ZOrder for given position. The new <code>ZOrder</code>
     * will belong to the same rack as the original one, but will have the
     * the new position passed in as a parameter.
     * 
     * @param position The new position.
     * 
     * @return A new <code>ZOrder</code> instance.
     */
    public ZOrder forPosition(int position) {
        return new ZOrder(this.rack, position);
    }
    
    public @Override String toString() {
        String s = "Unknown_rack"; //NOI18N
        switch(rack) {
            case  0: s = "BOTTOM_RACK"; break; //NOI18N
            case 10: s = "SYNTAX_RACK"; break; //NOI18N
            case 20: s = "CARET_RACK"; break; //NOI18N
            case 30: s = "DEFAULT_RACK"; break; //NOI18N
            case 40: s = "SHOW_OFF_RACK"; break; //NOI18N
            case 50: s = "TOP_RACK"; break; //NOI18N
        }
        return s + "(" + position + ")"; //NOI18N
    }
}
