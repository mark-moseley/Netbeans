/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;
import org.netbeans.modules.websvc.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.design.view.actions.AddOperationAction;
import org.netbeans.modules.websvc.design.view.layout.LeftRightLayout;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class OperationsWidget extends RoundedRectangleWidget 
        implements ExpandableWidget, PropertyChangeListener{
    
    private static final Color BORDER_COLOR = new Color(180,180,255);
    private static final Color TITLE_COLOR = new Color(204,204,255);
    private static final Color TITLE_COLOR2 = new Color(229,229,255);
    private static final int GAP = 16;
    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/operation.png"); // NOI18N   

    private transient WSDLModel wsdlModel;
    private transient Action addAction;

    private transient Widget contentWidget;
    private transient HeaderWidget headerWidget;
    private transient Widget buttons;
    private transient ExpanderWidget expander;
    private transient ImageLabelWidget headerLabelWidget;

    
    
    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param service 
     * @param implementationClass 
     * @param wsdlModel 
     */
    public OperationsWidget(Scene scene, Service service, FileObject implementationClass, WSDLModel wsdlModel) {
        super(scene);
        this.wsdlModel = wsdlModel;
        setRadius(GAP);
        setBorderColor(BORDER_COLOR);
        setTitleColor(TITLE_COLOR,TITLE_COLOR2);
        addAction = new AddOperationAction(service, implementationClass);
        addAction.addPropertyChangeListener(this);
        addAction.putValue(Action.SMALL_ICON, new ImageIcon(IMAGE));
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
            addAction,
        })));
        createContent();
    }
    
    private void createContent() {
        if (wsdlModel==null) return;
        
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        boolean expanded = ExpanderWidget.isExpanded(this, true);
        expander = new ExpanderWidget(getScene(), this, expanded);

        headerWidget = new HeaderWidget(getScene(), expander);
        headerWidget.setLayout(new LeftRightLayout(32));

        headerLabelWidget = new ImageLabelWidget(getScene(), IMAGE, 
                NbBundle.getMessage(OperationWidget.class, "LBL_Operations"));
        headerWidget.addChild(headerLabelWidget);
        updateHeaderLabel();

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));

        ButtonWidget addButton = new ButtonWidget(getScene(), addAction);

        buttons.addChild(addButton);
        buttons.addChild(expander);

        headerWidget.addChild(buttons);

        addChild(headerWidget);
        setTitleWidget(headerWidget);
        
        contentWidget = new Widget(getScene());
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        for(PortType portType:wsdlModel.getDefinitions().getPortTypes()) {
            for(Operation operation:portType.getOperations()) {
                contentWidget.addChild(new OperationWidget(getScene(),operation));
            }
        }
        
        if(expanded) {
            expandWidget();
        } else {
            collapseWidget();
        }
    }

    private void updateHeaderLabel() {
        int noOfOperations = 0;
        for(PortType portType:wsdlModel.getDefinitions().getPortTypes()) {
            noOfOperations += portType.getOperations().size();
        }
        headerLabelWidget.setComment("("+noOfOperations+")");
    }

    public void collapseWidget() {
        if(contentWidget.getParentWidget()!=null) {
            removeChild(contentWidget);
            repaint();
        }
    }

    public void expandWidget() {
        if(contentWidget.getParentWidget()==null) {
            addChild(contentWidget);
        }
    }

    public Object hashKey() {
        return wsdlModel==null?null:wsdlModel.getDefinitions().getName();
    }
    
    /**
     * Adds the widget actions to the given toolbar (no separators are
     * added to either the beginning or end).
     *
     * @param  toolbar  to which the actions are added.
     */
    public void addToolbarActions(JToolBar toolbar) {
        toolbar.add(addAction);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        Object source = evt.getSource();
        Object newValue = evt.getNewValue();
        if(property.equals(AddOperationAction.PROPERTY_OPERATION_ADDED) &&
                source==addAction && newValue instanceof Operation) {
            contentWidget.addChild(new OperationWidget(getScene(),(Operation)newValue));
            updateHeaderLabel();
        }
    }
}
