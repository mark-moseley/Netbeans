/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view;


import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.DefaultSeparateContainer;
import org.netbeans.core.windows.view.ui.DefaultSplitContainer;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.windows.TopComponent;


/**
 * Class which represents model of mode element for GUI hierarchy. 
 *
 * @author  Peter Zavadsky
 */
public class ModeView extends ViewElement {
    
    private final ModeContainer container;

    // PENDING it is valid only for separate mode, consider create of two classes
    // of view, one for split, and second one for separate mode type.
    private int frameState;

    // PENDING devide into two subclasses?
    // Split mode view constructor
    public ModeView(Controller controller, WindowDnDManager windowDnDManager, 
    double resizeWeight, int kind, TopComponent[] topComponents, TopComponent selectedTopComponent) {
        super(controller, resizeWeight);
        
        container = new DefaultSplitContainer(this, windowDnDManager, kind);
        
        setTopComponents(topComponents, selectedTopComponent);
    }
    
    // Separate mode view constructor
    public ModeView(Controller controller, WindowDnDManager windowDnDManager, Rectangle bounds, int frameState,
    TopComponent[] topComponents, TopComponent selectedTopComponent) {
        super(controller, 0D);
        
        this.frameState = frameState;
        
        container = new DefaultSeparateContainer(this, windowDnDManager, bounds);
        
        setTopComponents(topComponents, selectedTopComponent);
    }
    
    
    public void setFrameState(int frameState) {
        this.frameState = frameState;
    }

    
//    public void addTopComponent(TopComponent tc) {
//        if(getTopComponents().contains(tc)) {
//            return;
//        }
//        container.addTopComponent(tc);
//    }
    
    public void removeTopComponent(TopComponent tc) {
        if(!getTopComponents().contains(tc)) {
            return;
        }
        container.removeTopComponent(tc);
    }
    
    public void setTopComponents(TopComponent[] tcs, TopComponent select) {
        container.setTopComponents(tcs, select);
    }
    
//    public void setSelectedTopComponent(TopComponent tc) {
//        container.setSelectedTopComponent(tc);
//    }
    
    public TopComponent getSelectedTopComponent() {
        return container.getSelectedTopComponent();
    }
    
    public void setActive(boolean active) {
        container.setActive(active);
    }
    
    public List getTopComponents() {
        return new ArrayList(Arrays.asList(container.getTopComponents()));
    }
    
    public void focusSelectedTopComponent() {
        container.focusSelectedTopComponent();
    }
    
    public Component getComponent() {
        return container.getComponent();
    }
    
    public void updateName(TopComponent tc) {
        container.updateName(tc);
    }
    
    public void updateToolTip(TopComponent tc) {
        container.updateToolTip(tc);
    }
    
    public void updateIcon(TopComponent tc) {
        container.updateIcon(tc);
    }

    // XXX
    public void updateFrameState() {
        ((Frame)container.getComponent()).setExtendedState(frameState);
    }
    
    
    public String toString() {
        TopComponent selected = container.getSelectedTopComponent();
        return super.toString() + " [selected=" // NOI18N
            + (selected == null ? null : WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)) + "]"; // NOI18N
    }

}

