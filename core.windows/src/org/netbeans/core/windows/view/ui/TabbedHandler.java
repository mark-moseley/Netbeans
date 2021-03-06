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


package org.netbeans.core.windows.view.ui;


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.actions.ActionUtils;
import org.netbeans.core.windows.actions.MaximizeWindowAction;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.SlidingView;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.netbeans.core.windows.view.ui.slides.TabbedSlideAdapter;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.windows.TopComponent;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.AWTEventListener;
import org.netbeans.core.windows.view.ui.slides.SlideBar;
import org.netbeans.core.windows.view.ui.slides.SlideBarActionEvent;
import org.netbeans.core.windows.view.ui.slides.SlideOperationFactory;


/** Helper class which handles <code>Tabbed</code> component inside
 * <code>ModeComponent</code>.
 *
 * @author  Peter Zavadsky
 */
public final class TabbedHandler implements ChangeListener, ActionListener {

    /** Associated mode container. */
    private final ModeView modeView;
    
    /** Component which plays tabbed. */
    private final Tabbed tabbed;
    /** kind of the mode view we are handling tabs for */
    private final int kind;

    /** Ignore own changes. */
    private boolean ignoreChange = false;

    private static ActivationManager activationManager = null;

    /** Creates new SimpleContainerImpl */
    public TabbedHandler(ModeView modeView, int kind, Tabbed tbd) {
        this.modeView = modeView;
        this.kind = kind;

        synchronized (TabbedHandler.class) {
            if (activationManager == null) {
                activationManager = new ActivationManager();
                Toolkit.getDefaultToolkit().addAWTEventListener(
                    activationManager, AWTEvent.MOUSE_EVENT_MASK);
            }
        }
        tabbed = tbd;
        tabbed.addChangeListener(this);
        tabbed.addActionListener(this);
//        tabbed = createTabbedComponent(kind);

        // E.g. when switching tabs in mode.
        ((Container)tabbed.getComponent()).setFocusCycleRoot(true);
    }

    
//    /** Gets tabbed container on supplied position */
//    private Tabbed createTabbedComponent(int kind) {
//        Tabbed tabbed;
//
//        if(kind == Constants.MODE_KIND_EDITOR) {
//            tabbed = new TabbedAdapter(Constants.MODE_KIND_EDITOR);
//        } else if (kind == Constants.MODE_KIND_SLIDING) {
//            tabbed = new TabbedSlideAdapter(((SlidingView)modeView).getSide());
//        } else {
//            tabbed = new TabbedAdapter(Constants.MODE_KIND_VIEW);
//        }
//        
//        tabbed.addChangeListener(this);
//        tabbed.addActionListener(this);
//
//        return tabbed;
//    }
    
    public void requestAttention (TopComponent tc) {
        tabbed.requestAttention(tc);
    }

    public void cancelRequestAttention (TopComponent tc) {
        tabbed.cancelRequestAttention(tc);
    }
    
    public Component getComponent() {
        return tabbed.getComponent();
    }

    /** Adds given top component to this container. */
    public void addTopComponent(TopComponent tc, int kind) {
        addTCIntoTab(tc, kind);
    }
    

    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        ignoreChange = true;
        try {
            tabbed.setTopComponents(tcs, selected);
        } finally {
            ignoreChange = false;
        }
    }
    
    /** Adds TopComponent into specified tab. */
    private void addTCIntoTab(TopComponent tc, int kind) {
        
        if(containsTC(tabbed, tc)) {
            return;
        }

        Image icon = tc.getIcon();
        
        try {
            ignoreChange = true;
            String title = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
            if(title == null) {
                title = ""; // NOI18N
            }
            tabbed.addTopComponent(
                title,
                icon == null ? null : new ImageIcon(icon),
                tc, tc.getToolTipText());
        } finally {
            ignoreChange = false;
        }
    }

    /** Checks whether the tabbedPane already contains the component. */
    private static boolean containsTC(Tabbed tabbed, TopComponent tc) {
        return tabbed.indexOf(tc) != -1;
    }
    
    /** Removes TopComponent from this container. */
    public void removeTopComponent(TopComponent tc) {
        removeTCFromTab(tc); 
    }

    /** Removes TC from tab. */
    private void removeTCFromTab (TopComponent tc) {
        if(tabbed.indexOf(tc) != -1) {
            try {
                ignoreChange = true;
                tabbed.removeComponent(tc);
            } finally {
                ignoreChange = false;
            }

            //Bugfix #27644: Remove reference from TopComponent's accessible context
            //to our tabbed pane.
            tc.getAccessibleContext().setAccessibleParent(null);
        }
    }
    
    /** Called when icon of some component in this multi frame has changed  */
    public void topComponentIconChanged(TopComponent tc) {
        int index = tabbed.indexOf(tc);
        if (index < 0) {
            return;
        }
        
        tabbed.setIconAt(index, new ImageIcon(tc.getIcon()));
    }
    
    /** Called when the name of some component has changed  */
    public void topComponentNameChanged(TopComponent tc, int kind) {
        int index = tabbed.indexOf(tc);
        if (index < 0) {
            return;
        }
        
        String title = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
        if(title == null) {
            title = ""; // NOI18N
        }
        tabbed.setTitleAt (index, title);
    }
    
    public void topComponentToolTipChanged(TopComponent tc) {
        int index = tabbed.indexOf(tc);
        if (index < 0) {
            return;
        }
        
        tabbed.setToolTipTextAt(index, tc.getToolTipText());
    }
    
    /** Sets selected <code>TopComponent</code>.
     * Ensures GUI components to set accordingly. */
    public void setSelectedTopComponent(TopComponent tc) {
        if(tc == getSelectedTopComponent()) {
            return;
        }
        if (tc == null && !isNullSelectionAllowed()) {
            return;
        }
        
        if(tabbed.indexOf(tc) >= 0 || (isNullSelectionAllowed() && tc == null)) {
            try {
                ignoreChange = true;
                tabbed.setSelectedComponent(tc);
            } finally {
                ignoreChange = false;
            }
        }
    }
    
    private boolean isNullSelectionAllowed() {
        return kind == Constants.MODE_KIND_SLIDING;
    }
    
    public TopComponent getSelectedTopComponent() {
        return tabbed.getSelectedTopComponent();
    }

    public TopComponent[] getTopComponents() {
        return tabbed.getTopComponents();
    }

    public void setActive(boolean active) {
        tabbed.setActive(active);
    }
    
    ///////////////////
    // ChangeListener
    public void stateChanged(ChangeEvent evt) {
        if(ignoreChange || evt.getSource() != tabbed) {
            return;
        }
        TopComponent selected = tabbed.getSelectedTopComponent();
        modeView.getController().userSelectedTab(modeView, (TopComponent)selected);
    }
    
    // DnD>>
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer,
    Point startingPoint, boolean attachingPossible) {
        return tabbed.getIndicationForLocation(location, startingTransfer,
                                            startingPoint, attachingPossible);
    }
    
    public Object getConstraintForLocation(Point location, boolean attachingPossible) {
        return tabbed.getConstraintForLocation(location, attachingPossible);
    }
    
    // Sliding
    public Rectangle getTabBounds(int tabIndex) {
        return tabbed.getTabBounds(tabIndex);
    }

    public void actionPerformed(ActionEvent e) {
        if (e instanceof TabActionEvent) {
            TabActionEvent tae = (TabActionEvent) e;
            String cmd = tae.getActionCommand();
            if (TabbedContainer.COMMAND_SELECT.equals(cmd)) {
                return;
            }
            tae.consume();
            if (TabbedContainer.COMMAND_CLOSE == cmd) { //== test is safe here
                TopComponent tc = (TopComponent) tabbed.getTopComponentAt(tae.getTabIndex());
                if (tc == null) {
                    throw new IllegalStateException ("Component to be closed " +
                        "is null at index " + tae.getTabIndex());
                }
                modeView.getController().userClosedTopComponent(modeView, tc);
            } else if (TabbedContainer.COMMAND_POPUP_REQUEST == cmd) {
                handlePopupMenuShowing(tae.getMouseEvent(), tae.getTabIndex());
            } else if (TabbedContainer.COMMAND_MAXIMIZE == cmd) {
                handleMaximization(tae);
            } else if (TabbedContainer.COMMAND_CLOSE_ALL == cmd) {
                ActionUtils.closeAllDocuments();
            } else if (TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS == cmd) {
                TopComponent tc = (TopComponent) tabbed.getTopComponentAt(tae.getTabIndex());
                ActionUtils.closeAllExcept(tc);
            //Pin button handling here
            } else if (TabbedContainer.COMMAND_ENABLE_AUTO_HIDE.equals(cmd)) {
                TopComponent tc = (TopComponent) tabbed.getTopComponentAt(tae.getTabIndex());
                // prepare slide operation
                Component tabbedComp = tabbed.getComponent();
                
                String side = WindowManagerImpl.getInstance().guessSlideSide(tc);
                SlideOperation operation = SlideOperationFactory.createSlideIntoEdge(
                    tabbedComp, side, true);
                operation.setStartBounds(
                       new Rectangle(tabbedComp.getLocationOnScreen(), tabbedComp.getSize()));
                operation.prepareEffect();
                
                modeView.getController().userEnabledAutoHide(modeView, tc);
                modeView.getController().userTriggeredSlideIntoEdge(modeView, operation);
            }
        } else if (e instanceof SlideBarActionEvent) {
            // slide bar commands
            SlideBarActionEvent sbe = (SlideBarActionEvent)e;
            String cmd = sbe.getActionCommand();
            if (SlideBar.COMMAND_POPUP_REQUEST.equals(cmd)) {
                handlePopupMenuShowing(sbe.getMouseEvent(), sbe.getTabIndex());
            } else if (SlideBar.COMMAND_SLIDE_IN.equals(cmd)) {
                modeView.getController().userTriggeredSlideIn(modeView, sbe.getSlideOperation());
            } else if (SlideBar.COMMAND_SLIDE_RESIZE.equals(cmd)) {
                modeView.getController().userResizedSlidingWindow(modeView, sbe.getSlideOperation());
            } else if (SlideBar.COMMAND_SLIDE_OUT.equals(cmd)) {
                // when the call comes from the change of tehmodel rather than user clicking,
                // ignore activation requests.
                // #48539
                SlideOperation op = new ProxySlideOperation(sbe.getSlideOperation(), ignoreChange);
                modeView.getController().userTriggeredSlideOut(modeView, op);
            } else if (SlideBar.COMMAND_DISABLE_AUTO_HIDE.equals(cmd)) {
                TopComponent tc = (TopComponent) tabbed.getTopComponentAt(sbe.getTabIndex());
                modeView.getController().userDisabledAutoHide(modeView, tc);
            } else if( SlideBar.COMMAND_MAXIMIZE == cmd ) {
                TopComponent tc = (TopComponent) tabbed.getTopComponentAt(sbe.getTabIndex());
                MaximizeWindowAction mwa = new MaximizeWindowAction(tc);
                mwa.actionPerformed(e);
            }
        }
    }

    /** Possibly invokes popup menu. */
    public static void handlePopupMenuShowing(MouseEvent e, int idx) {
        Component c = (Component) e.getSource();
        while (c != null && !(c instanceof Tabbed.Accessor))
            c = c.getParent();
        if (c == null) {
            return;
        }
        final Tabbed tab = ((Tabbed.Accessor)c).getTabbed();

        final Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), c);

        final int clickTab = idx;
        if (clickTab < 0) {
            return;
        }

        TopComponent tc = tab.getTopComponentAt(clickTab);
        if(tc == null) {
            return;
        }
        
        // ask also tabbed to possibly alter actions
        Action[] actions = tab.getPopupActions(tc.getActions(), clickTab);
        if (actions == null) { 
            actions = tc.getActions();
        }

        showPopupMenu(
            Utilities.actionsToPopup(actions, tc.getLookup()), p, c);
    }

    /** Shows given popup on given coordinations and takes care about the
     * situation when menu can exceed screen limits */
    private static void showPopupMenu (JPopupMenu popup, Point p, Component comp) {
        popup.show(comp, p.x, p.y);
    }

    /** Possibly invokes the (un)maximization. */
    public static void handleMaximization(TabActionEvent tae) {
        Component c = (Component) tae.getSource();
        while (c != null && !(c instanceof Tabbed))
            c = c.getParent();
        if (c == null) {
            return;
        }
        
        final Tabbed tab = (Tabbed) c;
        TopComponent tc = tab.getTopComponentAt(tae.getTabIndex());
        // perform action
        MaximizeWindowAction mwa = new MaximizeWindowAction(tc);
        mwa.actionPerformed(tae);
    }

    /** Well, we can't totally get rid of AWT event listeners - this is what
     * keeps track of the activated mode. */
    private static class ActivationManager implements AWTEventListener {
        public void eventDispatched(AWTEvent e) {
            if(e.getID() == MouseEvent.MOUSE_PRESSED) {
                handleActivation((MouseEvent) e);
            }
        }

        //
        /* XXX(-ttran) when the split container contains two TopComponents say TC1
         * and TC2.  If TC2 itself does not accept focus or the user clicks on one
         * of TC2's child compoennts which does not accept focus, then the whole
         * split container is activated.  It in turn may choose to activate TC1 not
         * TC2.  This is a very annoying problem if TC1 is an Explorer and TC2 is
         * the global property sheet.  The user clicks on the property sheet but
         * the Explorer gets activated which has a different selected node than the
         * one attached to the property sheet at that moment.  The contents of the
         * property sheet is updated immediately after the mouse click.  For more
         * details see <http://www.netbeans.org/issues/show_bug.cgi?id=11149>
         *
         * What follows here is a special hack for mouse click on _any_
         * TopComponent.  The hack will cause the nearest upper TopComponent in the
         * AWT hieararchy to be activated on MOUSE_PRESSED on any of its child
         * components.  This behavior is compatible with all window managers I can
         * imagine.
         */
        private void handleActivation(MouseEvent evt) {
            Object obj = evt.getSource();
            if (!(obj instanceof Component)) {
                return;
            }
            Component comp = (Component) obj;
            
            while (comp != null && !(comp instanceof ModeComponent)) {
                if (comp instanceof JComponent) {
                    JComponent c = (JComponent)comp;
                    // don't activate if requested
                    if (Boolean.TRUE.equals(c.getClientProperty("dontActivate"))) { //NOI18N
                        return;
                    }
                }
                if (comp instanceof TopComponent) {
                    TopComponent tc = (TopComponent)comp;
                    // special way of activation for topcomponents in sliding mode
                    if (Boolean.TRUE.equals(tc.getClientProperty("isSliding"))) { //NOI18N
                        tc.requestActive();
                        return;
                    }
                }
                comp = comp.getParent();
            }

            if (comp instanceof ModeComponent) {
                ModeComponent modeComp = (ModeComponent)comp;
                // don't activate sliding views when user clicked edge bar
                if (modeComp.getKind() != Constants.MODE_KIND_SLIDING) {
                    ModeView modeView = modeComp.getModeView();
                    modeView.getController().userActivatedModeView(modeView);
                }
            }
        }
        
    } // end of ActivationManager
    
    /**
     * proxy slide operation that disables activation reqeuest when the operation comes from the model, not really user action.
     */
    private static class ProxySlideOperation implements SlideOperation {
        
        private SlideOperation original;
        private boolean disable;
        
        public ProxySlideOperation(SlideOperation orig, boolean disableActivation) {
            original = orig;
            disable = disableActivation;
        }
        
        public Component getComponent() {
            return original.getComponent();
        }

        public Rectangle getFinishBounds() {
            return original.getFinishBounds();
        }

        public String getSide() {
            return original.getSide();
        }

        public Rectangle getStartBounds() {
            return original.getStartBounds();
        }

        public int getType() {
            return original.getType();
        }

        public void prepareEffect() {
            original.prepareEffect();
        }

        public boolean requestsActivation() {
            if (disable) {
                return false;
            }
            return original.requestsActivation();
        }

        public void run(JLayeredPane pane, Integer layer) {
            original.run(pane, layer);
        }

        public void setFinishBounds(Rectangle bounds) {
            original.setFinishBounds(bounds);
        }

        public void setStartBounds(Rectangle bounds) {
            original.setStartBounds(bounds);
        }
    }

}

