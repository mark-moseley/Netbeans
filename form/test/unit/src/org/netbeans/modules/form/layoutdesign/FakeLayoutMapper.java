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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import org.netbeans.modules.form.FormModel;

/**
 * VisualMapper implementation for layout tests. Works based on explicitly set
 * bounds and baseline positions.
 */

public class FakeLayoutMapper implements VisualMapper {

    private FormModel fm = null;
    private HashMap contInterior = null;
    private HashMap baselinePosition = null;
    private HashMap prefPaddingInParent = null;
    private HashMap prefPadding = null;
    private HashMap compBounds = null;
    private HashMap compMinSize = null;
    private HashMap compPrefSize = null;
    private HashMap hasExplicitPrefSize = null;
    
    public FakeLayoutMapper(FormModel fm, 
                            HashMap contInterior, 
                            HashMap baselinePosition, 
                            HashMap prefPaddingInParent,
                            HashMap compBounds,
                            HashMap compMinSize,
                            HashMap compPrefSize,
                            HashMap hasExplicitPrefSize,
                            HashMap prefPadding) {
        this.fm = fm;
        this.contInterior = contInterior;
        this.baselinePosition = baselinePosition;
        this.prefPaddingInParent = prefPaddingInParent;
        this.compBounds = compBounds;
        this.compMinSize = compMinSize;
        this.compPrefSize = compPrefSize;
        this.hasExplicitPrefSize = hasExplicitPrefSize;
        this.prefPadding = prefPadding;
    }
    
    // -------

    public Rectangle getComponentBounds(String componentId) {
        return (Rectangle) compBounds.get(componentId);
    }

    public Rectangle getContainerInterior(String componentId) {
        return (Rectangle) contInterior.get(componentId);
    }

    public Dimension getComponentMinimumSize(String componentId) {
        return (Dimension) compMinSize.get(componentId);
    }

    public Dimension getComponentPreferredSize(String componentId) {
        return (Dimension) compPrefSize.get(componentId);
    }

    public boolean hasExplicitPreferredSize(String componentId) {
        return ((Boolean) hasExplicitPrefSize.get(componentId)).booleanValue();
    }

    public int getBaselinePosition(String componentId, int width, int height) {
        String id = componentId + "-" + width + "-" + height; //NOI18N
        return ((Integer) baselinePosition.get(id)).intValue();
    }

    public int getPreferredPadding(String comp1Id,
                                   String comp2Id,
                                   int dimension,
                                   int comp2Alignment,
                                   PaddingType paddingType)
    {
        String id = comp1Id + "-" + comp2Id  + "-" + dimension + "-" + comp2Alignment + "-" // NOI18N
                    + (paddingType != null ? paddingType.ordinal() : 0);
        Integer pad = (Integer) prefPadding.get(id);
        return pad != null ? pad.intValue() : 6;
    }

    public int getPreferredPaddingInParent(String parentId,
                                           String compId,
                                           int dimension,
                                           int compAlignment)
    {
        String id = parentId + "-" + compId + "-" + dimension + "-" + compAlignment; //NOI18N
        Integer pad = (Integer) prefPaddingInParent.get(id);
        return pad != null ? pad.intValue() : 10;
    }

    public boolean[] getComponentResizability(String compId, boolean[] resizability) {
        resizability[0] = resizability[1] = true;
        return resizability;
    }

    public void rebuildLayout(String contId) {
    }

    public void setComponentVisibility(String componentId, boolean visible) {
    }
}
