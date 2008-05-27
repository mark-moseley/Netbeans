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
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IComplexActivityGroup;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IIterationActivityGroup;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.diagrams.nodes.ContainerNode;
import org.netbeans.modules.uml.diagrams.nodes.RoundedRectWidget;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author thuy
 */
public class ActivityGroupWidget extends UMLNodeWidget//ContainerNode
{
    private UMLLabelWidget groupKindWidget = null;
    private UMLNameWidget actGrpNameWidget = null;
    private ContainerWidget containerWidget;
    public static final int MIN_NODE_WIDTH = 130;
    public static final int MIN_NODE_HEIGHT = 80;
    private static ResourceBundle bundle = NbBundle.getBundle(ContainerNode.class);

    public ActivityGroupWidget(Scene scene)
    {
        super(scene);
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if ( presentation != null)
        {
            IActivityGroup element = (IActivityGroup) presentation.getFirstSubject();
            setCurrentView(createActivityGroupView(element));
            addToLookup(initializeContextPalette());
        }
    }

    public  DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Activity");
        return paletteModel;
    }
    
    private Widget createActivityGroupView(IActivityGroup element)
    {
        Scene scene =  getScene();
        
        //create main view 
        RoundedRectWidget mainView = new RoundedRectWidget(scene, 
                getWidgetID(), bundle.getString("LBL_body"),
                RoundedRectWidget.DEFAULT_DASH);
         
        mainView.setLayout(LayoutFactory.createOverlayLayout());
        
        mainView.setMinimumSize(new Dimension(MIN_NODE_WIDTH, MIN_NODE_HEIGHT));
        mainView.setUseGradient(useGradient());
        mainView.setCustomizableResourceTypes(
                    new ResourceType [] {ResourceType.BACKGROUND} );
        mainView.setOpaque(true);
        mainView.setCheckClipping(true);
        
        Widget nameLayer = new Widget(scene);
        nameLayer.setLayout(
                LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 0));
        
        // groupKind widget
        groupKindWidget = new UMLLabelWidget(scene,
                this.getWidgetID()+".groupKind",  //NO I18N
                bundle.getString("LBL_groupKind"));
 
        groupKindWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
        setGroupKind(element);
        nameLayer.addChild(groupKindWidget);

        // element name widget
        actGrpNameWidget = new UMLNameWidget(scene, false, getWidgetID());
        actGrpNameWidget.initialize(element);
        nameLayer.addChild(actGrpNameWidget);
        
        // expression widget
        ExpressionWidget expresionWidget = 
                new ExpressionWidget(scene, 
                getWidgetID() + ".expression",
                bundle.getString("LBL_expression"));
        expresionWidget.setCustomizableResourceTypes(
                    new ResourceType [] {ResourceType.FONT, ResourceType.FOREGROUND} );
        expresionWidget.initialize(getTestExpression(element));
        expresionWidget.setVisible(true);
        nameLayer.addChild(expresionWidget);
        
        mainView.addChild(nameLayer);
        
        Widget containerLayer = new Widget(scene);
        containerLayer.setBorder(BorderFactory.createEmptyBorder(6));
        containerLayer.setLayout( LayoutFactory.createOverlayLayout());
        
        containerWidget = new ContainerWidget(scene);
        containerWidget.setCheckClipping(true);
   
        //containerWidget.setBorder(BorderFactory.createLineBorder(1, Color.BLUE));
        containerLayer.addChild(containerWidget);
        
        mainView.addChild(containerLayer);
        
        return mainView;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    { 
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();
        actGrpNameWidget.propertyChange(event);
        if (element instanceof IActivityGroup)
        {
            IActivityGroup actGroupElem = (IActivityGroup) element;
            if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
            {
                this.setGroupKind(actGroupElem);
            }
        }
    }

    public String getWidgetID()
    {
        return UMLWidgetIDString.ACTIVITYGROUPWIDGET.toString();
    }

    private void setGroupKind(IActivityGroup element)
    {
        String groupKindStr = "";
        if (element != null && element instanceof IComplexActivityGroup)
        {
            IComplexActivityGroup pComplex = (IComplexActivityGroup) element;
            String metaType = pComplex.getElementType();

            // If it's a complex group, change the metatype
            // to one of the simpler one
            if (metaType.equals("ComplexActivityGroup"))
            {
                int kind = 0;
                int groupKind = pComplex.getGroupKind();
                switch (groupKind)
                {
                    case BaseElement.AGK_STRUCTURED:
                        metaType = "StructuredActivityGroup";
                        groupKindStr = "<<structured>>";
                        break;
                    case BaseElement.AGK_INTERRUPTIBLE:
                        metaType = "InterruptibleActivityRegion";
                        groupKindStr = "<<structured>>";
                        break;
                    default:
                        metaType = "IterationActivityGroup";
                        if (element instanceof IIterationActivityGroup)
                        {
                            IIterationActivityGroup pIteration = (IIterationActivityGroup) element;
                            // get Kind
                            kind = pIteration.getKind();
                            groupKindStr = (kind == BaseElement.IAG_TEST_AT_BEGIN ? "<<testAtBegin>>" : "<<testAtEnd>>");
                        }
                        break;
                }
            }
        }

        if (groupKindStr != null && groupKindStr.trim().length() > 0)
        {
            groupKindWidget.setLabel(groupKindStr);
            groupKindWidget.setVisible(true);
        } else
        {
            groupKindWidget.setLabel("");
            groupKindWidget.setVisible(false);
        }
    }

    private IExpression getTestExpression(IElement element)
    {
        IExpression pNewExpression = null;
        if (element != null && element instanceof IIterationActivityGroup)
        {
            IIterationActivityGroup pIteration =  (IIterationActivityGroup) element;
            IValueSpecification pExpression = pIteration.getTest();
            
            if (pExpression == null)
            {
                // Create one if the activity doesn't already have one.
                TypedFactoryRetriever<IExpression> factory = new TypedFactoryRetriever<IExpression>();
                pNewExpression = factory.createType("Expression");
                
                if (pNewExpression != null)
                {
                    pIteration.addElement(pNewExpression);
                    pIteration.setTest(pNewExpression);
                }
            } else 
            {
                pNewExpression = (IExpression) pExpression;
            }
        }
        return pNewExpression;
    }

//    @Override
//    public ContainerWidget getContainer()
//    {
//        return containerWidget;
//    }
}
