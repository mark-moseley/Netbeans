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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.diagrams.edges.BasicUMLLabelManager;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager.LabelType;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public final class StateTransitionLabelManager extends BasicUMLLabelManager
{

    public static final String PRECONDITION = "precondition";
    public static final String POSTCONDITION = "postcondition";
    private Action[] contextActions = null;

    public StateTransitionLabelManager(ConnectionWidget widget)
    {
        super(widget);
    }

    @Override
    public Action[] getContextActions(LabelType type)
    {
        if (contextActions == null)
        {
            ArrayList<Action> actions = new ArrayList<Action>();
//            actions.addAll(Arrays.asList(super.getContextActions(type)));
            
            actions.add(new ToggleConditionLabelAction(this, NAME, LabelType.EDGE,
                    NbBundle.getMessage(BasicUMLLabelManager.class, "LBL_NAME_LABEL")));
            actions.add(new ToggleConditionLabelAction(this, PRECONDITION, LabelType.SOURCE,
                    NbBundle.getMessage(StateTransitionLabelManager.class, "CTL_PreCondition")));
            actions.add(new ToggleConditionLabelAction(this, POSTCONDITION, LabelType.TARGET,
                    NbBundle.getMessage(StateTransitionLabelManager.class, "CTL_PostCondition")));

            contextActions = new Action[actions.size()];
            actions.toArray(contextActions);
        }
        return contextActions;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);
        String propName = evt.getPropertyName();

        if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
        {
            INamedElement element = (INamedElement) getModelElement();
            if (element instanceof ITransition)
            {
                ITransition transition = (ITransition) element;

                if (this.isVisible(PRECONDITION, LabelType.SOURCE))
                {
                    updateLabel(LabelType.SOURCE, PRECONDITION, getCondition(transition.getPreCondition()));
                }
                if (this.isVisible(POSTCONDITION, LabelType.TARGET))
                {
                    updateLabel(LabelType.TARGET, POSTCONDITION, getCondition(transition.getPostCondition()));
                }
            }
        }
    }

    @Override
    protected Widget createLabel(String name, LabelType type)
    {
        if (name.equals(PRECONDITION) || name.equals(POSTCONDITION))
        {
            IElement e = getModelElement();

            ITransition transition = (ITransition) e;
            IConstraint constraint = type == LabelType.SOURCE ? transition.getPreCondition() : transition.getPostCondition();
            getModelElement().addOwnedConstraint(constraint);
            String text = getCondition(constraint);

            EditableCompartmentWidget widget = new EditableCompartmentWidget(getScene(), constraint, "dummy", text);
            return widget;

        } else
        {
            return super.createLabel(name, type);
        }


    }

    private String getCondition(IConstraint constraint)
    {
        String text = constraint.getExpression();
        if (text == null || text.equals(""))
        {
            return retrieveDefaultName();
        }
        return text;
    }
}
