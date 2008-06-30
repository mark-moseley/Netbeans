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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes.activity;

import java.util.List;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.OvalWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;

/**
 *
 * @author thuy
 */
public class InitialNodeWidget extends ControlNodeWidget
{

    public InitialNodeWidget(Scene scene)
    {
        super(scene, true);   // context palette is on
        setResizable(false);
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if (presentation != null)
        {

            //IInitialNode element = (IInitialNode) presentation.getFirstSubject();
            // create a circle node
            OvalWidget circleWidget = new OvalWidget(getScene(),
                                                     DEFAULT_INNER_RADIUS,
                                                     getWidgetID(),
                                                     bundle.getString("LBL_body"));

            circleWidget.setUseGradient(useGradient);
            circleWidget.setCustomizableResourceTypes(
                    new ResourceType[]{ResourceType.BACKGROUND});
            circleWidget.setOpaque(true);
            setCurrentView(circleWidget);

            List<WidgetAction> actions = getActions().getActions();
            if (actions != null && actions.size() > 0)
            {
                getActions().removeAction(0);
            }
        }
    }

    public String getWidgetID()
    {
        return UMLWidgetIDString.INITIALNODEWIDGET.toString();
    }

    
//    private class CircleWidget extends OvalWidget
//    {   
//        public CircleWidget(Scene scene, int r, String propID, String propDisplayName)
//        {
//            super(scene, r, propID, propDisplayName);
//        }

//         @Override
//        protected Rectangle calculateClientArea()
//        {
                //make sure the circle always has a fixed bounds
//            Rectangle  bounds = getBounds();
//            if (bounds == null) 
//            {
//                int width = getWidth();
//                int height = getHeight();
//                return new Rectangle( -width/2, -height/2, width, height);
//            }
//            if (bounds.width != bounds.height)
//            {
//                int cx = GeomUtil.centerX(bounds);
//                int adjustedLen = Math.min(bounds.width, bounds.height);
//               return  new Rectangle( cx-(adjustedLen/2), bounds.y, adjustedLen, adjustedLen);
//            }
//            return bounds;
//        }
//    }   
}
