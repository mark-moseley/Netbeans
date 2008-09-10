/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Sheryl Su
 */
public class RemoveAllBendsAction extends NodeAction
{

    private DesignerScene scene;

    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        scene = actionContext.lookup(DesignerScene.class);
        return this;
    }

    @Override
    protected void performAction(Node[] activatedNodes)
    {
        for (Object object : scene.getSelectedObjects())
        {
            Widget widget = scene.findWidget(object);
            if (widget instanceof ConnectionWidget)
            {
                ConnectionWidget connectionWidget = (ConnectionWidget)widget;
                ArrayList<Point> list = new ArrayList<Point>();
                list.add(connectionWidget.getFirstControlPoint());
                list.add(connectionWidget.getLastControlPoint());
                connectionWidget.setControlPoints(list, true);
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes)
    {
        for (Object object : scene.getSelectedObjects())
        {
            Widget widget = scene.findWidget(object);
            if (!(widget instanceof ConnectionWidget))
            {
                return false;
            }

            if (((ConnectionWidget) widget).getControlPoints().size() < 3)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName()
    {
        return NbBundle.getMessage(RemoveAllBendsAction.class, "CTL_RemoveAllBends");
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }
}
