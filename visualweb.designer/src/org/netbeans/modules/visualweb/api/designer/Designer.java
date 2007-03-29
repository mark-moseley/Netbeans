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

package org.netbeans.modules.visualweb.api.designer;

import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.w3c.dom.Element;

/**
 * Interface representing the designer component.
 *
 * @author Peter Zavadsky
 */
public interface Designer {

    public JComponent getDesignerComponent();

    // XXX Temp after moved TopComponent impl out >>>
    public JComponent getVisualRepresentation();
    public JComponent getToolbarRepresentation();
    public Action[] getActions();
    public Lookup getLookup();
    public void componentOpened();
    public void componentClosed();
    public void componentShowing();
    public void componentHidden();
    public void componentActivated();
    public void componentDeactivated();
    public UndoRedo getUndoRedo();
    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback);
    public CloseOperationState canCloseElement();
    // XXX Temp after moved TopComponent impl out <<<

    public void startInlineEditing(Element componentRootElement, String propertyName);

    public void selectComponent(Element componentRootElement);
    public int getSelectedCount();

    public enum Alignment {
        SNAP_TO_GRID,
        TOP,
        MIDDLE,
        BOTTOM,
        LEFT,
        CENTER,
        RIGHT
    }
    public void align(Alignment alignment);
    public void snapToGrid();

    public boolean isInlineEditing();

    // >>> Boxes stuff
    /** Representing the individual box. Providing accessors (getters) only! */
    public interface Box {
        public Element getComponentRootElement();
        public Box getParent();
        public Box[] getChildren();
        
        // XXX Get rid of this.
        public HtmlTag getTag();
        // XXX Get rid of this.
        public Element getSourceElement();
        
        public int getWidth();
        public int getHeight();
        
        public int getX();
        public int getY();
        public int getZ();
        
        public int getAbsoluteX();
        public int getAbsoluteY();
        
        public int getRightMargin();
        public int getLeftMargin();
        
        public int getEffectiveTopMargin();
        
        // XXX Get rid of.
        public boolean isPositioned();
        // XXX Very suspicious.
        public Box getPositionedBy();
    } // End of Box.
    
    public Box findBox(int x, int y);
    // XXX Get rid of.
    public Box findBoxForSourceElement(Element sourceElement);
    
    public int snapX(int x, Box positionedBy);
    public int snapY(int y, Box positionedBy);
    // <<< Boxes stuff
}
