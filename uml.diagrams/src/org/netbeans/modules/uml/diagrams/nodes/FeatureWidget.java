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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import javax.swing.UIManager;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.diagrams.engines.DefaultDiagramEngine;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;

/**
 *
 * @author treyspiva
 */
public abstract class FeatureWidget extends CustomizableWidget
        implements DiagramNodeWriter, DiagramEdgeWriter, UMLWidget
{
    private EditableCompartmentWidget label = null;
    public static final String ID = "feature";
    private Alignment alignment = Alignment.LEFT;
    
    public FeatureWidget(Scene scene)
    {
        this(scene, "", "");
    }
    
    public FeatureWidget(Scene scene, String propId, String propDisplayName)
    {
        super(scene, propId, propDisplayName);
        setForeground((Color)null); 
        setLayout(LayoutFactory.createHorizontalFlowLayout());
        
        if (scene instanceof ObjectScene) 
        {
            ObjectScene objScene = (ObjectScene) scene;
            
            WidgetAction.Chain chain = createActions(DesignerTools.SELECT);
            chain.addAction(objScene.createSelectAction());
            chain.addAction(DefaultDiagramEngine.POPUP_ACTION);
        }
    }
    
    public void setAlignment(Alignment alignment)
    {
        if(alignment == Alignment.CENTER)
        {
            setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        }
        
        this.alignment = alignment;
    }
    
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state)
    {
        if((previousState.isSelected() == false) && (state.isSelected() == true))
        {
            // Going from not selected to selected.
            // Need to remove the background and changed the font back to the 
            // standard color.
            setOpaque(true);
            
            setBackground(UIManager.getColor("List.selectionBackground"));
            if(label != null)
            {
                label.setForeground(UIManager.getColor("List.selectionForeground"));
            }
            
            setBorder(BorderFactory.createLineBorder(1, BORDER_HILIGHTED_COLOR));
            setParentSelectedState(true);
        }
        else if((previousState.isSelected() == true) && (state.isSelected() == false))
        {
            // Going from selected to not selected
            setOpaque(false);
            if((label != null) && (getParentWidget() != null))
            {
                label.setForeground(getParentWidget().getForeground());
                //label.closeEditorCommitChanges();//
            }
            
            setParentSelectedState(false);
            
            setBorder(BorderFactory.createEmptyBorder(1));
        }
    }
    
    /**
     * 
     * @param attr
     */
    public void initialize(IElement attr)
    {
        addPresentation(attr);
        updateUI();
        
        addActions();
    }
    
    protected void updateUI() 
    {
        removeChildren();

        label = new EditableCompartmentWidget(getScene(), this, ID);
        label.setAlignment(alignment);
        label.setLabel(formatElement());
        addChild(label);
        
        if(alignment == Alignment.CENTER)
        {
            setChildConstraint(label, 100);
        }
        
        setBorder(BorderFactory.createEmptyBorder(1));
        
    }
    
    protected void setText(String value)
    {
        if(label != null)
        {
            label.setLabel(value);
        }
    }
    
    public String getText()
    {
        return label.getLabel();
    }
    
    protected void addActions()
    {
    }
    
    private void addPresentation(IElement element)
    {
        Scene scene = getScene();
        if (scene instanceof ObjectScene)
        {
            IPresentationElement presentation = createPresentationElement();
            presentation.addSubject(element);
            
            ObjectScene objectScene = (ObjectScene)scene;
            objectScene.addObject(presentation, this);
        }

    }
    
    private IPresentationElement createPresentationElement()
    {
        IPresentationElement retVal = null;
        
        ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
        if(factory != null)
        {
           Object presentationObj = factory.retrieveMetaType("NodePresentation", null);
           if (presentationObj instanceof IPresentationElement)
           {
                  retVal = (IPresentationElement)presentationObj;    
           }
        }
        
        return retVal;
    }
    
    protected IElement getElement()
    {
        IElement retVal = null;
        
            Scene scene = getScene();
            if (scene instanceof ObjectScene)
            {
                ObjectScene objScene = (ObjectScene)scene;
                IPresentationElement element = (IPresentationElement)objScene.findObject(this);
                if(element != null)
                {
                    retVal = element.getFirstSubject();
                }
            }
        return retVal;
    }

    
    protected IPresentationElement getObject()
    {
        Scene scene = getScene();
        if (scene instanceof ObjectScene)
        {
            ObjectScene objScene = (ObjectScene) scene;
            IPresentationElement element = (IPresentationElement) objScene.findObject(this);
            return element;
        }
        return null;
    }
    
    private void setParentSelectedState(boolean value)
    {

        IElement element = getElement();
        if (element != null)
        {
            ObjectScene scene = (ObjectScene) getScene();
            Widget parentWidget = Util.getParentNodeWidget(this);

            if (parentWidget != null)
            {
                ObjectState newState = parentWidget.getState().deriveSelected(value);
                parentWidget.setState(newState);
            }
        }
    }
    
    public void remove()
    {
        super.removeFromParent();
        IPresentationElement pe = getObject();
        if (pe != null)
            pe.delete();
    }

    public void refresh() 
    {
    }

    public void save(EdgeWriter edgeWriter)
    {
        // this is mainly for saving sqd message labels
        edgeWriter.setPEID(PersistenceUtil.getPEID(this));
        edgeWriter.setVisible(this.isVisible());
        edgeWriter.setLocation(this.getLocation());
        edgeWriter.setSize(this.getBounds().getSize());
        edgeWriter.setPresentation("");
        edgeWriter.setHasPositionSize(true);
        edgeWriter.beginGraphNode();
        edgeWriter.endGraphNode();
    }

    public void save(NodeWriter nodeWriter) {
        setNodeWriterValues(nodeWriter, this);
        nodeWriter.beginGraphNodeWithModelBridge();
        nodeWriter.beginContained();
        //write contained
        saveChildren(this, nodeWriter);
        nodeWriter.endContained();     
        nodeWriter.endGraphNode();
    }

    public void saveChildren(Widget widget, NodeWriter nodeWriter) {
        if (widget == null || nodeWriter == null)
            return;
        
        List<Widget> widList = widget.getChildren();
        for (Widget child : widList) {
            if (child instanceof DiagramNodeWriter) {
                ((DiagramNodeWriter) child).save(nodeWriter);
            } else {
                saveChildren(child, nodeWriter);
            }
        }
    }
    
    protected void setNodeWriterValues(NodeWriter nodeWriter, Widget widget) {
        nodeWriter = PersistenceUtil.populateNodeWriter(nodeWriter, widget);
        nodeWriter.setHasPositionSize(true);
        PersistenceUtil.populateProperties(nodeWriter, widget);
    }
    
    protected String formatElement()
    {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatElement(getElement());
    }
    
    public void switchToEditMode()
    {
        if(label!=null)label.switchToEditMode();
    }
    
    /**
     * select feature widget for edition, usually after addiion of new feature widget
     * works for active editor only
     */
    public void select()
    {
        DesignerScene scene=(DesignerScene) getScene();
        UMLDiagramTopComponent tc=(UMLDiagramTopComponent) scene.getTopComponent();
        if(tc.isActivated())//diagram action
        {
            //Set selected=scene.getSelectedObjects();
            HashSet newSelection=new HashSet();
            newSelection.add(getObject());
            scene.setFocusedObject(getObject());
            scene.setSelectedObjects(newSelection);
            //ObjectState stFocFoc=attrW.getState().deriveWidgetFocused(true).deriveObjectFocused(true).deriveHighlighted(true).deriveWidgetAimed(true);
            //attrW.setState(stFocFoc);
            //scene.setSelectedObjects(newSelection);
//                        new AfterValidationExecutor(new ActionProvider() {
//                            public void perfomeAction() {
//                                attrW.switchToEditMode();
//                            }
//                        }, scene);
            scene.validate();
        }
    }
}
