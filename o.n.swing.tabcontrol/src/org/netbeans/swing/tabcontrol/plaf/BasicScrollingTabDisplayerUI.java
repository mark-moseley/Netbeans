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
/*
 * BasicScrollingTabDisplayerUI.java
 *
 * Created on March 19, 2004, 1:08 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import org.netbeans.swing.tabcontrol.TabDataModel;

/**
 * Base class for tab displayers that have scrollable tabs.
 *
 * @author Tim Boudreau
 */
public abstract class BasicScrollingTabDisplayerUI extends BasicTabDisplayerUI {
    private Rectangle scratch = new Rectangle();
    
    private JPanel controlButtons;
    
    private TabControlButton btnScrollLeft;
    private TabControlButton btnScrollRight;
    private TabControlButton btnDropDown;
    private TabControlButton btnMaximizeRestore;

    /**
     * Creates a new instance of BasicScrollingTabDisplayerUI
     */
    public BasicScrollingTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    protected final TabLayoutModel createLayoutModel() {
        DefaultTabLayoutModel dtlm = new DefaultTabLayoutModel(
                displayer.getModel(),
                displayer);
        return new ScrollingTabLayoutModel(dtlm, selectionModel,
                displayer.getModel());
    }

    protected TabState createTabState() {
        return new ScrollingTabState();
    }
    
    protected HierarchyListener createHierarchyListener() {
        return new ScrollingHierarchyListener();
    }

    public void makeTabVisible (int tab) {
        if (scroll().makeVisible(tab, getTabsAreaWidth())) {
            getTabsVisibleArea(scratch);
            displayer.repaint(scratch.x, scratch.y, scratch.width, scratch.height);
        }
    }

    /**
     * Returns the width of the tabs area
     */
    protected final int getTabsAreaWidth() {
        int result = displayer.getWidth();
        Insets ins = getTabAreaInsets();
        return result - (ins.left + ins.right);
    }

    public Insets getTabAreaInsets() {
        return new Insets(0, 0, 0, getControlButtons().getPreferredSize().width + 5);
    }

    protected final int getLastVisibleTab() {
        if (displayer.getModel().size() == 0) {
            return -1;
        }
        return scroll().getLastVisibleTab(getTabsAreaWidth());
    }

    protected final int getFirstVisibleTab() {
        if (displayer.getModel().size() == 0) {
            return -1;
        }
        return scroll().getFirstVisibleTab(getTabsAreaWidth());
    }

    protected void install() {
        super.install();
        installControlButtons();
        ((ScrollingTabLayoutModel) layoutModel).setPixelsToAddToSelection (
                defaultRenderer.getPixelsToAddToSelection());
    }

    protected void uninstall() {
        super.uninstall();
        displayer.setLayout(null);
        displayer.removeAll();
    }

    protected LayoutManager createLayout() {
        return new WCLayout();
    }
    
    /**
     * @return A component that holds control buttons (scroll left/right, drop down menu)
     * that are displayed to right of the tab area.
     */
    protected Component getControlButtons() {
        if( null == controlButtons ) {
            JPanel buttonsPanel = new JPanel( null );
            buttonsPanel.setOpaque( false );

            int width = 0;
            int height = 0;
            
            final boolean isGTK = "GTK".equals(UIManager.getLookAndFeel().getID());

            //create scroll-left button
            Action a = scroll().getBackwardAction();
            a.putValue( "control", displayer ); //NO18N
            btnScrollLeft = TabControlButtonFactory.createScrollLeftButton( displayer, a, isGTK );
            buttonsPanel.add( btnScrollLeft );
            Dimension prefDim = btnScrollLeft.getPreferredSize();
            btnScrollLeft.setBounds( width, 0, prefDim.width, prefDim.height );
            width += prefDim.width;
            height = prefDim.height;

            //create scroll-right button
            a = scroll().getForwardAction();
            a.putValue( "control", displayer ); //NO18N
            btnScrollRight = TabControlButtonFactory.createScrollRightButton( displayer, a, isGTK );
            buttonsPanel.add( btnScrollRight );
            prefDim = btnScrollRight.getPreferredSize();
            btnScrollRight.setBounds( width, 0, prefDim.width, prefDim.height );
            width += prefDim.width;
            height = Math.max ( height, prefDim.height );

            //create drop down button
            btnDropDown = TabControlButtonFactory.createDropDownButton( displayer, isGTK );
            buttonsPanel.add( btnDropDown );

            width += 3;
            prefDim = btnDropDown.getPreferredSize();
            btnDropDown.setBounds( width, 0, prefDim.width, prefDim.height );
            width += prefDim.width;
            height = Math.max ( height, prefDim.height );
            
            //maximize / restore button
            if( null != displayer.getContainerWinsysInfo() ) {
                width += 3;
                btnMaximizeRestore = TabControlButtonFactory.createMaximizeRestoreButton( displayer, isGTK );
                buttonsPanel.add( btnMaximizeRestore );
                prefDim = btnMaximizeRestore.getPreferredSize();
                btnMaximizeRestore.setBounds( width, 0, prefDim.width, prefDim.height );
                width += prefDim.width;
                height = Math.max ( height, prefDim.height );
            }
            
            Dimension size = new Dimension( width, height );
            buttonsPanel.setMinimumSize( size );
            buttonsPanel.setSize( size );
            buttonsPanel.setPreferredSize( size );
            buttonsPanel.setMaximumSize( size );
            
            controlButtons = buttonsPanel;
        }
        return controlButtons;
    }
    
    protected ComponentListener createComponentListener() {
        return new ScrollingDisplayerComponentListener();
    }

    private int lastKnownModelSize = Integer.MAX_VALUE;
    /** Overrides <code>modelChanged()</code> to clear the transient information in the
     * state model, which may now contain tab indices that don't exist, and also
     * to clear cached width/last-visible-tab data in the layout model, and ensure that
     * the selected tab is visible.
     */
    protected void modelChanged() {
        scroll().clearCachedData();
        int index = selectionModel.getSelectedIndex();
        
        //If the user has intentionally scrolled the selected tab offscreen, do ensure space is
        //optimally used, but don't volunteer to radically change the scroll point
        if (index >= scroll().getCachedFirstVisibleTab() && index < scroll().getCachedLastVisibleTab()) {
            makeTabVisible(selectionModel.getSelectedIndex());
        }
        
        int modelSize = displayer.getModel().size();
        if (modelSize < lastKnownModelSize) {
            //When closing tabs, make sure we resync the state, so the
            //user doesn't end up with a huge gap due to closed tabs
            scroll().ensureAvailableSpaceUsed(true);
        }
        lastKnownModelSize = modelSize;
        super.modelChanged();
    }

    protected void installControlButtons() {
        displayer.setLayout(createLayout());
        displayer.add(getControlButtons());
    }

    /**
     * Convenience getter for the layout model as an instance of
     * ScrollingTabLayoutModel
     */
    protected final ScrollingTabLayoutModel scroll() {
        return (ScrollingTabLayoutModel) layoutModel;
    }

    /**
     * Overridden to update the offset of the ScrollingTabLayoutModel on mouse
     * wheel events
     */
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        int i = e.getWheelRotation();
        //clear the mouse-in-tab index so we don't occasionally have
        //tabs the mouse is not in scrolling away looking as if the mouse
        //is in them
        tabState.clearTransientStates();
        int offset = scroll().getOffset();
        if (i > 0 && (offset < displayer.getModel().size() - 1)) {
            if (scroll().isLastTabClipped()) {
                scroll().setOffset(offset + 1);
            }
        } else if (i < 0) {
            if (offset >= 0) {
                scroll().setOffset(offset - 1);
            }
        } else {
            return;
        }
        

        //tabState.repaintAllTabs();
        //XXX should optimize this - need to make sure the space below the tabs
        //is painted on metal and win classic
        displayer.repaint();
    }


    protected class ScrollingTabState extends BasicTabState {
        public int getState(int tabIndex) {
            int result = super.getState(tabIndex);
            int first = getFirstVisibleTab();
            int last = getLastVisibleTab();

            if (tabIndex < first || tabIndex > last) {
                return TabState.NOT_ONSCREEN;
            }
            if (first == last && first == tabIndex
                    && displayer.getModel().size() > 1) {
                //We have a very small area to fit tabs - smaller than even the
                //minimum clip width, probably < 40 pixels.  Definitely don't
                //want to display a close button or much of anything else
                result |= TabState.CLIP_LEFT | TabState.CLIP_RIGHT;

            } else if (getTabsAreaWidth() < scroll()
                    .getMinimumLeftClippedWidth()
                    + scroll().getMinimumRightClippedWidth()
                    && tabIndex == first && last == first - 1 && displayer.getModel()
                    .size()
                    > 1 && scroll().isLastTabClipped()) {
                //when we're displaying two tabs in less than enough room,
                //make sure a truncated tab is never displayed with a close button
                result |= TabState.CLIP_LEFT;
            } else {
                if (tabIndex == first && scroll().getOffset() == first) {
                    result |= TabState.CLIP_LEFT;
                }
                if (tabIndex == last && scroll().isLastTabClipped()) {
                    result |= TabState.CLIP_RIGHT;
                }
            }
            return result;
        }
    }

    protected class ScrollingDisplayerComponentListener extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
            //Notify the layout model that its cached sizes are invalid
            makeTabVisible(selectionModel.getSelectedIndex());
        }
    }
    
    protected class ScrollingHierarchyListener extends DisplayerHierarchyListener {
        public void hierarchyChanged(HierarchyEvent e) {
            super.hierarchyChanged (e);
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (displayer.isShowing()) {
                    //#47850 - for some reason, uninstall can be called on the Ui class, before this gets processed.
                    // check for null values just to be sure.
                    if (tabState != null && selectionModel != null) {
                        tabState.setActive (displayer.isActive());
                        makeTabVisible (selectionModel.getSelectedIndex());
                    }
                }
            }
        }
    }

    static SoftReference<BufferedImage> ctx = null;

    /**
     * Provides an offscreen graphics context so that widths based on character
     * size can be calculated correctly before the component is shown
     */
    public static Graphics2D getOffscreenGraphics() {
        BufferedImage result = null;
        //XXX multi-monitors w/ different resolution may have problems;
        //Better to call Toolkit to create a screen graphics
        if (ctx != null) {
            result = ctx.get();
        }
        if (result == null) {
            result = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            ctx = new SoftReference<BufferedImage>(result);
        }
        return (Graphics2D) result.getGraphics();
    }

    /**
     * @return Bounds for the control buttons in the tab displayer container.
     */
    protected Rectangle getControlButtonsRectangle( Container parent ) {
        Component c = getControlButtons();
        return new Rectangle( parent.getWidth()-c.getWidth(), 0, c.getWidth(), c.getHeight() );
    }
    
    @Override
    public Dimension getMinimumSize(JComponent c) {
        int index = displayer.getSelectionModel().getSelectedIndex();
        TabDataModel model = displayer.getModel();
        if( index < 0 || index >= model.size() )
            index = 0;
        Dimension minSize = null;
        if( index >= model.size() )
            minSize = new Dimension( 100, 10 );
        else
            minSize = model.getTab(index).getComponent().getMinimumSize();
        minSize.width = Math.max(minSize.width, 100);
        minSize.height = Math.max(minSize.height, 10);
        return minSize;
    }
    
    /**
     * Layout manager for the tab displayer to make sure that control buttons
     * are always displayed at the end of the tab list.
     */
    private class WCLayout implements LayoutManager {

        public void addLayoutComponent(String name, Component comp) {
        }

        public void layoutContainer(java.awt.Container parent) {
            
            Rectangle r = getControlButtonsRectangle( parent );
            Component c = getControlButtons();
            c.setBounds( r );
        }

        public Dimension minimumLayoutSize(Container parent) {
            return getMinimumSize((JComponent) parent);
        }

        public Dimension preferredLayoutSize(Container parent) {
            return getPreferredSize((JComponent) parent);
        }

        public void removeLayoutComponent(java.awt.Component comp) {
        }
    }
}
