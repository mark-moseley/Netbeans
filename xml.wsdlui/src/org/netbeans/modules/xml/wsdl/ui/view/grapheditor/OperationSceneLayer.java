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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.OneSideJustifiedLayout;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.OperationWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.PartnerLinkTypeContentWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.RectangleWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.RoleWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.WidgetConstants;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.WidgetFactory;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.WidgetHelper;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class OperationSceneLayer extends Widget {
    
    private static final int OPERATION_GAP = 25;
    private final PartnerLinkTypeContentWidget mOuterWidget;
    private Widget dummyOperationWidget;
    private PortType leftPortType;
    private PortType rightPortType;
    private Widget dummyEndWidget;
    private final Widget rightsideWidgetsHolder;
    private final Widget leftsideWidgetsHolder;
    
    public OperationSceneLayer(Scene scene, PartnerLinkTypeContentWidget outerWidget) {
        super(scene);
        mOuterWidget = outerWidget;
        
        // Get the port types involved so we can detect when they are deleted.
        rightPortType = getPortType(mOuterWidget.getRightRoleWidget());
        leftPortType = getPortType(mOuterWidget.getLeftRoleWidget());
        
        Layout layout = LayoutFactory.createVerticalFlowLayout(SerialAlignment.JUSTIFY, OPERATION_GAP);
        setLayout(layout);
        rightsideWidgetsHolder = new Widget(getScene());
        rightsideWidgetsHolder.setLayout(layout);
        addChild(rightsideWidgetsHolder);
        leftsideWidgetsHolder = new Widget(getScene());
        leftsideWidgetsHolder.setLayout(layout);
        addChild(leftsideWidgetsHolder);
        init();
    }
    
    private void init() {
        
        dummyOperationWidget = new Widget(getScene());
        
        RectangleWidget rectangleWidget = new RectangleWidget(getScene(), 12, 70);
        rectangleWidget.setThickness(4);
        rectangleWidget.setColor(WidgetConstants.HIT_POINT_BORDER);
        dummyOperationWidget.addChild(new Widget(getScene()));
        dummyOperationWidget.setBorder(BorderFactory.createEmptyBorder(3));
        dummyOperationWidget.addChild(rectangleWidget);
        dummyEndWidget = new Widget(getScene());
        dummyEndWidget.setPreferredBounds(new Rectangle(0, OPERATION_GAP));//67 = height of a operation widget
        refreshOperations();
        setMinimumSize(new Dimension(400, 0));
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
        Widget parent = right ? rightsideWidgetsHolder : leftsideWidgetsHolder;
        if (pt != null) {
            Collection<Operation> operations = pt.getOperations();
            WidgetFactory factory = WidgetFactory.getInstance();
            List<Widget> widgets = new ArrayList<Widget>();

            for (Operation operation : operations) {
                OperationWidget operationWidget =
                    (OperationWidget) factory.getOrCreateWidget(getScene(), operation, parent);
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
        dummyEndWidget.removeFromParent();
        dummyOperationWidget.setLayout(new OneSideJustifiedLayout(right));
        if (right) {
            rightsideWidgetsHolder.addChild(dummyOperationWidget);
        } else {
            leftsideWidgetsHolder.addChild(dummyOperationWidget);
        }
        addChild(dummyEndWidget);
    }

    public void removeBlankWidget() {
        dummyOperationWidget.removeFromParent();
    }

    public void updateOperations(boolean rightSided) {
        if (rightSided) {
            PortType pt = getPortType(mOuterWidget.getRightRoleWidget());
            if (rightPortType != pt) {
                WidgetHelper.removeWidgetFromScene(getScene(), rightsideWidgetsHolder);
                rightPortType = pt;
                if (pt != null) {
                    renderOperations(rightSided);
                }
            } else {
                rightsideWidgetsHolder.removeChildren();
                renderOperations(rightSided);
            }
        } else {
            PortType pt = getPortType(mOuterWidget.getLeftRoleWidget());
            if (leftPortType != pt) {
                WidgetHelper.removeWidgetFromScene(getScene(), leftsideWidgetsHolder);
                leftsideWidgetsHolder.removeChildren();
                leftPortType = pt;
                if (pt != null) {
                    renderOperations(rightSided);
                }
            } else {
                leftsideWidgetsHolder.removeChildren();
                renderOperations(rightSided);
            }
        }
    }
    
    @Override
    protected void notifyRemoved() {
        super.notifyRemoved();
        leftPortType = null;
        rightPortType = null;
    }
}
