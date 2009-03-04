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
package org.netbeans.modules.uml.diagrams.actions.state;

import javax.swing.Action;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.state.StateWidget;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class ShowTransitionsAction extends SceneNodeAction
{

    private IState state;
    private DesignerScene scene;
    private IPresentationElement pe;

    public Action createContextAwareInstance(Lookup actionContext)
    {
        scene = actionContext.lookup(DesignerScene.class);
        pe = actionContext.lookup(IPresentationElement.class);
        if(pe!=null)
        {
            IElement e = pe.getFirstSubject();
            if (e instanceof IState)
            {
                state = (IState) e;
            }
        }
        return this;
    }

    @Override
    protected void performAction(Node[] activatedNodes)
    {

        DesignerScene ds = activatedNodes[0].getLookup().lookup(DesignerScene.class);
        IPresentationElement pe = activatedNodes[0].getLookup().lookup(IPresentationElement.class);
        Widget stateWidget = ds.findWidget(pe);

        if (stateWidget instanceof StateWidget)
        {
            StateWidget widget = (StateWidget) stateWidget;
            widget.setDetailVisible(!widget.isDetailVisible());
        }
    }

    @Override
    public String getName()
    {
        if (scene == null)
            return "";
        StateWidget widget = getStateWidget(scene, pe);
        if (widget == null)
        {
            return "";
        }
        return widget.isDetailVisible() ? loc("CTL_HideTransitions") : loc("CTL_ShowTransitions");
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    private String loc(String key)
    {
        return NbBundle.getMessage(ShowTransitionsAction.class, key);
    }

    private StateWidget getStateWidget(ObjectScene scene, IPresentationElement pe)
    {
        Widget widget = scene.findWidget(pe);
        if (widget instanceof StateWidget)
        {
            return (StateWidget) widget;
        }
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

//    @Override
//    protected boolean enable(Node[] activatedNodes)
//    {
//        return activatedNodes.length == 1;
//    }
}
