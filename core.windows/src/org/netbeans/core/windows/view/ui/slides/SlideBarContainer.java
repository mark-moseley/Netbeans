/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.slides;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Shape;
import javax.swing.JPanel;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.AbstractModeContainer;
import org.netbeans.core.windows.view.ui.ModeComponent;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.openide.windows.TopComponent;


/*
 * SlideBarContainer.java
 *
 * @author Dafe Simonek
 */
public final class SlideBarContainer extends AbstractModeContainer {
    
    /** panel displaying content of this container */
    VisualPanel panel;
    
    /** Creates a new instance of SlideBarContainer */
    public SlideBarContainer(ModeView modeView, WindowDnDManager windowDnDManager) {
        super(modeView, windowDnDManager, Constants.MODE_KIND_SLIDING);
        
        panel = new VisualPanel(this);
        panel.add(this.tabbedHandler.getComponent(), BorderLayout.CENTER);
    }
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        super.setTopComponents(tcs, selected);
    }

    protected Component getModeComponent() {
        return panel;
    }
    
    protected boolean isAttachingPossible() {
        return false;
    }

    protected TopComponentDroppable getModeDroppable() {
        return panel;
    }    
    
    protected void updateActive(boolean active) {
        // XXX - what we should do?
    }
    
    protected void updateTitle(String title) {
        // XXX - we have no title?
    }
    
    
    /** Component enclosing slide boxes, implements needed interfaces to talk
     * to rest of winsys
     */
    private static class VisualPanel extends JPanel implements ModeComponent, TopComponentDroppable {
    
        private final SlideBarContainer modeContainer;
        
        public VisualPanel (SlideBarContainer modeContainer) {
            super(new BorderLayout());
            this.modeContainer = modeContainer;
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
        }
        
        public ModeView getModeView() {
            return modeContainer.getModeView();
        }
        
        public int getKind() {
            return modeContainer.getKind();
        }
        
        // TopComponentDroppable>>
        public Shape getIndicationForLocation(Point location) {
            return modeContainer.getIndicationForLocation(location);
        }
        
        public Object getConstraintForLocation(Point location) {
            return modeContainer.getConstraintForLocation(location);
        }
        
        public Component getDropComponent() {
            return modeContainer.getDropComponent();
        }
        
        public ViewElement getDropViewElement() {
            return modeContainer.getDropModeView();
        }
        
        public boolean canDrop(TopComponent transfer, Point location) {
            return modeContainer.canDrop(transfer);
        }
        
        public boolean supportsKind(int kind, TopComponent transfer) {
            if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }

            return kind == getKind();
        }
        // TopComponentDroppable<<
        
    } // End of VisualPanel
    
}
