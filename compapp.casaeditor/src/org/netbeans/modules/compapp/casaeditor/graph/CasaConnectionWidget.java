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

package org.netbeans.modules.compapp.casaeditor.graph;

import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;
import org.netbeans.api.visual.widget.ConnectionWidget;


public class CasaConnectionWidget extends ConnectionWidget {

    private static final Stroke STROKE_DEFAULT  = new BasicStroke (1.0f);
    private static final Stroke STROKE_HOVERED  = new BasicStroke (1.5f);
    private static final Stroke STROKE_SELECTED = new BasicStroke (2.0f);
    
    
    public CasaConnectionWidget (Scene scene) {
        super (scene);
        setSourceAnchorShape (AnchorShape.NONE);
        setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        setPaintControlPoints (true);
        setState (ObjectState.createNormal ());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (state.isSelected () || state.isFocused()) {
            bringToFront();
            setStroke(STROKE_SELECTED);
            setForeground (CasaFactory.getCasaCustomizer().getCOLOR_SELECTION());
        } else if (state.isHovered () || state.isHighlighted()) {
            bringToFront();
            setStroke(STROKE_HOVERED);
            setForeground (CasaFactory.getCasaCustomizer().getCOLOR_HOVERED_EDGE());
        } else {
            setStroke(STROKE_DEFAULT);
            setForeground (CasaFactory.getCasaCustomizer().getCOLOR_CONNECTION_NORMAL());
        }
    }
    
    public void setForegroundColor(Color color) {
        setForeground(color);
    }
}
