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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.SlidingView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.AbstractModeContainer;
import org.netbeans.core.windows.view.ui.ModeComponent;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.ui.Tabbed;
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
        panel.setBorder(computeBorder(getSlidingView().getSide()));
        panel.add(this.tabbedHandler.getComponent(), BorderLayout.CENTER);
    }
    
    
    private SlidingView getSlidingView() {
        return (SlidingView)super.getModeView();
    }
    public void requestAttention (TopComponent tc) {
        tabbedHandler.requestAttention(tc);
    }

    public void cancelRequestAttention (TopComponent tc) {
        tabbedHandler.cancelRequestAttention (tc);
    }    
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        super.setTopComponents(tcs, selected);
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        return tabbedHandler.getTabBounds(tabIndex);
    }

    protected Component getModeComponent() {
        return panel;
    }
    
    protected Tabbed createTabbed() {
        return new TabbedSlideAdapter(((SlidingView)modeView).getSide());
    }
    
    protected boolean isAttachingPossible() {
        return false;
    }

    protected TopComponentDroppable getModeDroppable() {
        return panel;
    }    
    
    protected void updateActive(boolean active) {
        // #48588 - when in SDI, slidein needs to front the editor frame.
        if(active) {
            Window window = SwingUtilities.getWindowAncestor(panel);
            if(window != null && !window.isActive() && WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                window.toFront();
            }
        }
    }
    
    public boolean isActive() {
        Window window = SwingUtilities.getWindowAncestor(panel);
        // #54791 - just a doublecheck, IMHO should not happen anymore
        // after the winsys reenetrancy fix.
        return window == null ? false : window.isActive();
    }    
    
    protected void updateTitle(String title) {
        // XXX - we have no title?
    }
    
    /** Builds empty border around slide bar. Computes its correct size
     * based on given orientation
     */
    private static Border computeBorder(String orientation) {
        int bottom = 0, left = 0, right = 0, top = 0;
        if (Constants.LEFT.equals(orientation)) {
            top = 1; left = 1; bottom = 1; right = 2; 
        }
        if (Constants.BOTTOM.equals(orientation)) {
            top = 2; left = 1; bottom = 1; right = 1; 
        }
        if (Constants.RIGHT.equals(orientation)) {
            top = 1; left = 2; bottom = 1; right = 1; 
        }
        return new EmptyBorder(top, left, bottom, right);
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
            boolean isNonEditor = kind == Constants.MODE_KIND_VIEW || kind == Constants.MODE_KIND_SLIDING;
            boolean thisIsNonEditor = getKind() == Constants.MODE_KIND_VIEW || getKind() == Constants.MODE_KIND_SLIDING;

            return (isNonEditor == thisIsNonEditor);
        }
        // TopComponentDroppable<<

        public Dimension getMinimumSize() {
            if (modeContainer.getTopComponents().length == 0) {
                // have minimum size, to avoid gridbag layout to place the empty component at [0,0] location.
                // clashes with the dnd
                Border b = getBorder();
                if( null != b ) {
                    Insets insets = b.getBorderInsets( this );
                    return new Dimension( Math.max(1, insets.left+insets.right), Math.max(1, insets.top+insets.bottom) );
                }
                return new Dimension(1,1);
            }
            Dimension retValue;
            retValue = super.getMinimumSize();
            return retValue;
        }
        
    } // End of VisualPanel
    
}
