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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.SQDDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author Sheryl Su
 */
public class InteractiveZoomAction extends WidgetAction.Adapter
{

    private Scene scene;
    private Point lastLocation;
    private static final double zoomMultiplier = 1.01;

    @Override
    public State mousePressed(Widget widget, WidgetMouseEvent event)
    {
        if (event.getButton() == MouseEvent.BUTTON1)
        {
            scene = widget.getScene();
            lastLocation = event.getPoint();
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    @Override
    public State mouseReleased(Widget widget, WidgetMouseEvent event)
    {
        return State.REJECTED;
    }

    @Override
    public State mouseDragged(Widget widget, WidgetMouseEvent event)
    {
        if (scene != widget.getScene())
        {
            return State.REJECTED;
        }

        Point newLocation = event.getPoint();

        int amount = lastLocation.y - newLocation.y;
        lastLocation = newLocation;

        double zoom = scene.getZoomFactor();
        if (amount > 0)
        {
            zoom *= zoomMultiplier;
        } else
        {
            zoom /= zoomMultiplier;
        }
        scene.setZoomFactor(zoom);
        if(scene instanceof DesignerScene)
        {
            DesignerScene ds=(DesignerScene) scene;
            if(ds.getTopComponent() instanceof SQDDiagramTopComponent)
            {
                SQDDiagramTopComponent tc=(SQDDiagramTopComponent) ds.getTopComponent();
                tc.getTrackBar().onPostScrollZoom();
            }
        }
        return State.CONSUMED;
    }
}