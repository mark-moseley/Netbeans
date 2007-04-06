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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Rectangle;

public interface VisualMapper extends LayoutConstants  {

//    String getTopComponentId();

    /**
     * Provides actual bounds (position and size) of a component - as it appears
     * in the visual design area. The position should be in coordinates of the
     * whole design visualization.
     * @param componentId
     * @return actual bounds of given component, null if the component is not
     *         currently visualized in the design area
     */
    Rectangle getComponentBounds(String componentId);
    
    /**
     * Provides actual position and size of the interior of a component
     * container - as it appears in the visual design area. (The interior
     * differs from the outer bounds in that it should reflect the borders
     * or other insets). The position should be in coordinates of the whole
     * design visualization.
     * @param componentId
     * @return actual interior of given component, null if the component is not
     *         currently visualized in the design area
     */
    Rectangle getContainerInterior(String componentId);

    Dimension getComponentMinimumSize(String componentId);
    Dimension getComponentPreferredSize(String componentId);

    boolean hasExplicitPreferredSize(String componentId);

    /**
     * Provides preferred padding (optimal amount of space) between two components.
     * @param component1Id first component Id
     * @param component2Id second component Id
     * @param dimension the dimension (HORIZONTAL or VERTICAL) in which the
     *        components are positioned
     * @param comp2Alignment the edge (LEADING or TRAILING) at which the second
     *        component is placed next to the first component
     * @param paddingType padding type (RELATED, UNRELATED, SEPARATE or INDENT)
     * @return preferred padding (amount of space) between the given components
     */
    int getPreferredPadding(String component1Id,
                            String component2Id,
                            int dimension,
                            int comp2Alignment,
                            PaddingType paddingType);

    /**
     * Provides preferred padding (optimal amount of space) between a component
     * and its parent's border.
     * @param parentId Id of the parent container
     * @param componentId Id of the component
     * @param dimension the dimension (HORIZONTAL or VERTICAL) in which the
     *        component is positioned
     * @param compALignment the edge (LEADING or TRAILING) of the component
     *        which should be placed next to the parent's border
     * @return preferred padding (amount of space) between the component and its
     *         parent's border
     */
    int getPreferredPaddingInParent(String parentId,
                                    String componentId,
                                    int dimension,
                                    int compAlignment);

    int getBaselinePosition(String componentId, int width, int height);

    boolean[] getComponentResizability(String compId, boolean[] resizability);

    /**
     * Rebuilds the layout of given container. Called if LayoutDesigner needs
     * immediate update of the layout according to the model.
     */
    void rebuildLayout(String containerId);

    void setComponentVisibility(String componentId, boolean visible);
}
