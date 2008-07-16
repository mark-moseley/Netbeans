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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes.activity;

import java.awt.Dimension;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.MultilineEditableCompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.RoundedRectWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;

/**
 *
 * @author thuy
 */
public class InvocationWidget extends ActivityNodeWidget
{
    private static final int MIN_NODE_WIDTH = 80;

    public InvocationWidget(Scene scene)
    {
        super(scene, true, false);  // context palette is on, Default part is off
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if (presentation != null)
        {
            IInvocationNode invocationElem = (IInvocationNode) presentation.getFirstSubject();
            Scene scene = getScene();

            //create main view 
            RoundedRectWidget mainView = new RoundedRectWidget(scene,
                                                               getWidgetID(),
                                                               bundle.getString("LBL_body"));

            mainView.setMinimumSize(new Dimension(
                                      MIN_NODE_WIDTH, MIN_NODE_WIDTH));

            mainView.setLayout(
                    LayoutFactory.createVerticalFlowLayout(
                    LayoutFactory.SerialAlignment.JUSTIFY, 0));

            mainView.setUseGradient(useGradient);
            mainView.setCustomizableResourceTypes(
                    new ResourceType[]{ResourceType.BACKGROUND});
            mainView.setOpaque(true);
            mainView.setCheckClipping(true);

            // stereotype widget
            mainView.addChild(createStereoTypeWidget(), 20);
            enableStereoTypeWidget(invocationElem);

            // create multiline editable widget
            Widget editorPanel = new Widget(scene);
            editorPanel.setLayout(
                    LayoutFactory.createHorizontalFlowLayout(
                    LayoutFactory.SerialAlignment.JUSTIFY, 0));
            
            nameWidget = new MultilineEditableCompartmentWidget(scene, "", null,
                                                                 mainView,
                                                                 getWidgetID()+".name",
                                                                 bundle.getString("LBL_text"));
            nameWidget.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
            nameWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
            String labelStr = invocationElem.getNameWithAlias();
            nameWidget.setLabel(labelStr != null && labelStr.trim().length() > 0 ? labelStr : "");
            editorPanel.addChild(nameWidget,80);
            mainView.addChild(editorPanel, 60);
            
            // tagged value widget
            mainView.addChild(createTaggedValueWidget(), 20); 
            enableTaggedValueWidget(invocationElem);

            setCurrentView(mainView);
        }
    }

//    @Override
//    public void propertyChange(PropertyChangeEvent event)
//    {
//        IElement element = (IElement) event.getSource();
//        String propName = event.getPropertyName();
//        if (element != null && element instanceof IInvocationNode)
//        {
//            IInvocationNode invocationElem = (IInvocationNode) element;
//            if (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
//            {
//                nameWidget.setLabel(invocationElem.getNameWithAlias());
//            }
//        }
//    }

    public String getWidgetID()
    {
        return UMLWidgetIDString.INVOCATIONWIDGET.toString();
    }

    @Override
    public double getNameWidgetPercentage()
    {
        return 0.06; //weight/constraint assigned to nameWidget;
        
    }
}
