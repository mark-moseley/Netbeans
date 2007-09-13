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

/*
 * OperationSceneLayer.java
 *
 * Created on November 6, 2006, 6:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.OneSideJustifiedLayout;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.OperationWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.PartnerLinkTypeContentWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.PartnerScene;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.RectangleWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.RoleWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.WidgetConstants;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.WidgetFactory;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.WeakListeners;

/**
 *
 * @author radval
 */
public class OperationSceneLayer extends Widget implements ComponentListener, PropertyChangeListener {
    
    private static final int OPERATION_GAP = 25;
    private PartnerLinkTypeContentWidget mOuterWidget;
    private Widget dummyOperationWidget;
    private PortType leftPortType;
    private PortType rightPortType;
    private Widget dummyEndWidget;
    private PropertyChangeListener weakModelListener;
    private Widget rightsideWidgetsHolder;
    private Widget leftsideWidgetsHolder;
    private Widget dummyRightEndWidget;
    
    public OperationSceneLayer(Scene scene, PartnerLinkTypeContentWidget outerWidget) {
        super(scene);
        mOuterWidget = outerWidget;
        WSDLModel model = mOuterWidget.getWSDLComponent().getModel();
        model.addComponentListener(this);
        
        weakModelListener = WeakListeners.propertyChange(this, model);
        model.addPropertyChangeListener(weakModelListener);
        
        // Get the port types involved so we can detect when they are deleted.
        rightPortType = getPortType(mOuterWidget.getRightRoleWidget());
        leftPortType = getPortType(mOuterWidget.getLeftRoleWidget());
        init();
    }
    
    private void init() {
        Layout layout = LayoutFactory.createVerticalFlowLayout(SerialAlignment.JUSTIFY, OPERATION_GAP);
        setLayout(LayoutFactory.createVerticalFlowLayout());
        rightsideWidgetsHolder = new Widget(getScene());
        rightsideWidgetsHolder.setLayout(layout);
        addChild(rightsideWidgetsHolder);
        leftsideWidgetsHolder = new Widget(getScene());
        leftsideWidgetsHolder.setLayout(layout);
        addChild(leftsideWidgetsHolder);
        
        dummyOperationWidget = new Widget(getScene());
        
        RectangleWidget rectangleWidget = new RectangleWidget(getScene(), 12, 70);
        rectangleWidget.setThickness(4);
        rectangleWidget.setColor(WidgetConstants.HIT_POINT_BORDER);
        dummyOperationWidget.addChild(new Widget(getScene()));
        dummyOperationWidget.addChild(rectangleWidget);
        dummyEndWidget = new Widget(getScene());
        dummyEndWidget.setPreferredBounds(new Rectangle(0, OPERATION_GAP));//67 = height of a operation widget
        dummyRightEndWidget = new Widget(getScene());
        dummyRightEndWidget.setPreferredBounds(new Rectangle(0, OPERATION_GAP));//67 = height of a operation widget
        refreshOperations();
        setMinimumSize(new Dimension(400, 0));
    }

    public void childrenAdded(ComponentEvent evt) {
    }

    
    public void childrenDeleted(ComponentEvent evt) {

        // Assume the source is the Definitions instance.
        PortType rpt = getPortType(mOuterWidget.getRightRoleWidget());
        PortType lpt = getPortType(mOuterWidget.getLeftRoleWidget());
        if (lpt != leftPortType || rpt != rightPortType) {
            // Looks like one or more of our port types changed.
            leftPortType = lpt;
            rightPortType = rpt;
            refreshOperations();
        }
    }
    
    
    public void valueChanged(ComponentEvent evt) {
    }
    
    /**
     * Remove all of the children and reconstruct based on the current
     * state of the port types.
     */
    private void refreshOperations() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                rightsideWidgetsHolder.removeChildren();
                leftsideWidgetsHolder.removeChildren();
                dummyEndWidget.removeFromParent();
                clearHotSpot();
                mOuterWidget.revalidate();
                renderOperations(true);
                renderOperations(false);
                addChild(dummyEndWidget);
                getScene().validate();
            }
        });

    }
    
    public void showHotSpot(boolean right) {
        showBlankWidget(right);
    }
    
    
    public void clearHotSpot() {
        removeBlankWidget();
    }
   

    /**
     * Renders the operations..
     *
     * @param  right  true if right-sided, false if left.
     */
    private void renderOperations(boolean right) {
        PortType pt = right ? rightPortType : leftPortType;
        if (pt != null) {
            Collection<Operation> operations = pt.getOperations();
            WidgetFactory factory = WidgetFactory.getInstance();
            List<Widget> widgets = new ArrayList<Widget>();
            for (Operation operation : operations) {
                OperationWidget operationWidget =
                    (OperationWidget) factory.createWidget(getScene(), operation);
                operationWidget.setRightSided(right);
                widgets.add(operationWidget);
            }
            addChildren(widgets, right);
        }
    }

    private void addChildren(Collection<? extends Widget> operationChildren, boolean right) {
        Widget parent = null;
        if (right) {
            parent = rightsideWidgetsHolder;
        } else {
            parent = leftsideWidgetsHolder;
        }
        if (parent == null) return;
        for (Widget w : operationChildren) {
            parent.addChild(w);
        }
    }


    /**
     * Retrieve the PortType from the given RoleWidget.
     *
     * @param  rw  RoleWidget from which to get PortType.
     * @return  PortType, or null if not available.
     */
    private PortType getPortType(RoleWidget rw) {
        try {
            if (rw != null) {
                Role role = rw.getWSDLComponent();
                if (role != null) {
                    NamedComponentReference<PortType> ptref = role.getPortType();
                    if (ptref != null) {
                        return ptref.get();
                    }
                }
            }
        } catch (IllegalStateException ise) {
            // Indicates the referencing component is no longer in the model.
            // Fall through and return null.
        }
        return null;
    }



    public void showBlankWidget(boolean right) {
        dummyOperationWidget.removeFromParent();
        dummyRightEndWidget.removeFromParent();
        dummyEndWidget.removeFromParent();
        dummyOperationWidget.setLayout(new OneSideJustifiedLayout(right));
        if (right) {
            rightsideWidgetsHolder.addChild(dummyOperationWidget);
            if (leftsideWidgetsHolder.getChildren().size() > 0) {
                rightsideWidgetsHolder.addChild(dummyRightEndWidget);
            }
        } else {
            leftsideWidgetsHolder.addChild(dummyOperationWidget);
        }
        addChild(dummyEndWidget);
    }

    public void removeBlankWidget() {
        dummyOperationWidget.removeFromParent();
        dummyRightEndWidget.removeFromParent();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PortType.OPERATION_PROPERTY)) {
            boolean isRight = evt.getSource() == rightPortType;
            boolean isLeft = evt.getSource() == leftPortType;
            if (isRight || isLeft) {
                clearHotSpot();
                mOuterWidget.revalidate();
                mOuterWidget.getRightRoleWidget().revalidate();
                mOuterWidget.getLeftRoleWidget().revalidate();
                Object value = null;
                if ((value = evt.getNewValue()) != null) {
                    if (value instanceof Operation) {
                        WidgetFactory factory = WidgetFactory.getInstance();
                        OperationWidget operationWidget =
                            (OperationWidget) factory.createWidget(getScene(), (Operation) value);
                        operationWidget.setRightSided(isRight);
                        if (isRight) {
                            rightsideWidgetsHolder.addChild(operationWidget);
                        }
                        
                        if (isLeft) {
                            leftsideWidgetsHolder.addChild(operationWidget);
                        }
                        getScene().validate();
                        ActionHelper.selectNode((Operation) value);
                    }
                } else if (evt.getOldValue() != null) {
                    List<Widget> widgets = ((PartnerScene)getScene()).findWidgets(evt.getOldValue());
                    for (Widget w : widgets) {
                        if (w.getParentWidget() != null) {
                            w.getParentWidget().removeChild(w);
                        }
                    }
                    getScene().validate();
                }
            }
        } else if (evt.getPropertyName().equals(Role.PORT_TYPE_PROPERTY)) {
            if (evt.getSource() instanceof Role) {
                Role role = (Role) evt.getSource();
                //Check whether the role that changed was what we are interested in.
                if (role.equals(mOuterWidget.getRightRoleWidget().getWSDLComponent())) {
                    rightPortType = getPortType(mOuterWidget.getRightRoleWidget());
                    refreshOperations();
                } else if (role.equals(mOuterWidget.getLeftRoleWidget().getWSDLComponent())) {
                    leftPortType = getPortType(mOuterWidget.getLeftRoleWidget());
                    refreshOperations();
                }
            }
        }
    }
}
