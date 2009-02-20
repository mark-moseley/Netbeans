/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.io.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Utilities;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.IOContainer;
import org.openide.windows.IOContainer.CallBacks;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author Tomas Holy
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.windows.IOContainer.Provider.class, position=100)
public final class IOWindow implements IOContainer.Provider {
    private static IOWindowImpl impl;
    
    IOWindowImpl impl() {
        if (impl == null) {
            impl = IOWindowImpl.findDefault();
        }
        return impl;
    }

    public void add(JComponent comp, CallBacks cb) {
        impl().addTab(comp, cb);
    }

    public JComponent getSelected() {
        return impl().getSelectedTab();
    }

    public boolean isActivated() {
        return impl().isActivated();
    }

    public void open() {
        impl().open();
    }

    public void remove(JComponent comp) {
        impl().removeTab(comp);
    }

    public void requestActive() {
        impl().requestActive();
    }

    public void requestVisible() {
        impl().requestVisible();
    }

    public void select(JComponent comp) {
        impl().selectTab(comp);
    }

    public void setIcon(JComponent comp, Icon icon) {
        impl().setIcon(comp, icon);
    }

    public void setTitle(JComponent comp, String name) {
        impl().setTitle(comp, name);
    }

    public void setToolTipText(JComponent comp, String text) {
        impl().setToolTipText(comp, text);
    }

    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
        impl().setToolbarActions(comp, toolbarActions);
    }

    public boolean isCloseable(JComponent comp) {
        return true;
    }

    static final class IOWindowImpl extends TopComponent implements ChangeListener, PropertyChangeListener {

        static IOWindowImpl DEFAULT;

        static synchronized IOWindowImpl findDefault() {
            if (DEFAULT == null) {
                TopComponent tc = WindowManager.getDefault().findTopComponent("output"); // NOI18N
                if (tc != null) {
                    if (tc instanceof IOWindowImpl) {
                        DEFAULT = (IOWindowImpl) tc;
                    } else {
                        //This should not happen. Possible only if some other module
                        //defines different settings file with the same name but different class.
                        //Incorrect settings file?
                        IllegalStateException exc = new IllegalStateException("Incorrect settings file. Unexpected class returned." // NOI18N
                                + " Expected: " + IOWindowImpl.class.getName() // NOI18N
                                + " Returned: " + tc.getClass().getName()); // NOI18N
                        Logger.getLogger(IOWindowImpl.class.getName()).log(Level.WARNING, null, exc);
                        //Fallback to accessor reserved for window system.
                        IOWindowImpl.getDefault();
                    }
                } else {
                    IOWindowImpl.getDefault();
                }
            }
            return DEFAULT;
        }

        /* Singleton accessor reserved for window system ONLY. Used by window system to create
         * IOWindowImpl instance from settings file when method is given. Use <code>findDefault</code>
         * to get correctly deserialized instance of IOWindowImpl. */
        public static synchronized IOWindowImpl getDefault() {
            if (DEFAULT == null) {
                DEFAULT = new IOWindowImpl();
            }
            return DEFAULT;
        }

        public Object readResolve() throws java.io.ObjectStreamException {
            return getDefault();
        }

        private static final String ICON_PROP = "tabIcon"; //NOI18N
        private static final String TOOLBAR_ACTIONS_PROP = "toolbarActions"; //NOI18N
        private static final String TOOLBAR_BUTTONS_PROP = "toolbarButtons"; //NOI18N
        private static final String ICON_RESOURCE = "org/netbeans/core/resources/frames/output.png"; // NOI18N
        private static final boolean AQUA = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N
        private JTabbedPane pane;
        private JComponent singleTab;
        private JToolBar toolbar;
        private JPopupMenu popupMenu;
        private Map<JComponent, CallBacks> tabToCb = new HashMap<JComponent, CallBacks>();

        public IOWindowImpl() {
            pane = TabbedPaneFactory.createCloseButtonTabbedPane();
            pane.addChangeListener(this);
            pane.addPropertyChangeListener(TabbedPaneFactory.PROP_CLOSE, this);
            setFocusable(true);

            toolbar = new JToolBar();
            toolbar.setOrientation(JToolBar.VERTICAL);
            toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
            toolbar.setFloatable(false);
            Insets ins = toolbar.getMargin();
            JButton sample = new JButton();
            sample.setBorderPainted(false);
            sample.setOpaque(false);
            sample.setText(null);
            sample.setIcon(new Icon() {

                public int getIconHeight() {
                    return 16;
                }

                public int getIconWidth() {
                    return 16;
                }

                public void paintIcon(Component c, Graphics g, int x, int y) {
                }
            });
            toolbar.add(sample);
            Dimension buttonPref = sample.getPreferredSize();
            Dimension minDim = new Dimension(buttonPref.width + ins.left + ins.right, buttonPref.height + ins.top + ins.bottom);
            toolbar.setMinimumSize(minDim);
            toolbar.setPreferredSize(minDim);
            toolbar.remove(sample);
            setLayout(new BorderLayout());
            add(toolbar, BorderLayout.WEST);
            toolbar.setBorder(new VariableRightBorder(pane));
            toolbar.setBorderPainted(true);

            popupMenu = new JPopupMenu();
            popupMenu.add(new Close());
            popupMenu.add(new CloseAll());
            popupMenu.add(new CloseOthers());
            pane.addMouseListener(new MouseUtils.PopupMouseAdapter() {

                @Override
                protected void showPopup(MouseEvent evt) {
                    popupMenu.show(IOWindowImpl.this, evt.getX(), evt.getY());
                }
            });

            String name = NbBundle.getMessage(IOWindow.class, "LBL_IO_WINDOW");
            setDisplayName(name); //NOI18N
            // setting name to satisfy the accesible name requirement for window.
            setName(name); //NOI18N

            setIcon(ImageUtilities.loadImage(ICON_RESOURCE)); // NOI18N
            // special title for sliding mode
            // XXX - please rewrite to regular API when available - see issue #55955
            putClientProperty("SlidingName", getDisplayName()); //NOI18N

            if (AQUA) {
                setBackground(UIManager.getColor("NbExplorerView.background"));
                setOpaque(true);
                toolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
                pane.setBackground(UIManager.getColor("NbExplorerView.background"));
                pane.setOpaque(true);
            }
        }

        @Override
        public void open() {
            super.open();
        }

        @Override
        public void requestActive() {
            super.requestActive();
        }

        @Override
        public void requestVisible() {
            super.requestVisible();
        }

        boolean activated;
        public boolean isActivated() {
            return activated;
        }


        public void addTab(JComponent comp, CallBacks cb) {
            if (cb != null) {
                tabToCb.put(comp, cb);
            }
            setFocusable(false);
            if (singleTab != null) {
                // only single tab, remove it from TopComp. and add it to tabbed pane
                assert pane.getParent() == null;
                assert pane.getTabCount() == 0;
                remove(singleTab);
                pane.add(singleTab);
                pane.setIconAt(0, (Icon) singleTab.getClientProperty(ICON_PROP));
                pane.setToolTipTextAt(0, singleTab.getToolTipText());
                singleTab = null;
                pane.add(comp);
                add(pane);
                updateWindowName(null);
                updateWindowToolTip(null);
            } else if (pane.getTabCount() > 0) {
                // already several tabs
                assert pane.getParent() != null;
                assert singleTab == null;
                pane.add(comp);
            } else {
                // nothing yet
                assert pane.getParent() == null;
                assert singleTab == null;
                singleTab = comp;
                add(comp);
                updateWindowName(singleTab.getName());
                updateWindowToolTip(singleTab.getToolTipText());
                checkTabSelChange();
            }
            revalidate();
        }

        public void removeTab(JComponent comp) {
            if (singleTab != null) {
                assert singleTab == comp;
                remove(singleTab);
                singleTab = null;
                updateWindowName(null);
                updateWindowToolTip(null);
                checkTabSelChange();
                setFocusable(true);
                revalidate();
            } else if (pane.getParent() == this) {
                assert pane.getTabCount() > 1;
                pane.remove(comp);
                if (pane.getTabCount() == 1) {
                    singleTab = (JComponent) pane.getComponentAt(0);
                    pane.remove(singleTab);
                    remove(pane);
                    add(singleTab);
                    updateWindowName(singleTab.getName());
                    updateWindowToolTip(singleTab.getToolTipText());
                }
                revalidate();
            }
            CallBacks cb = tabToCb.get(comp);
            if (cb != null) {
                cb.closed();
            }
        }

        public void selectTab(JComponent comp) {
            if (!isOpened()) {
                open();
            }
            if (!isShowing()) {
                requestVisible();
            }
            if (singleTab == null) {
                pane.setSelectedComponent(comp);
            }
        }

        public JComponent getSelectedTab() {
            return singleTab != null ? singleTab : (JComponent) pane.getSelectedComponent();
        }

        public void setTitle(JComponent comp, String name) {
            comp.setName(name);
            if (singleTab != null) {
                assert singleTab == comp;
                updateWindowName(name);
            } else {
                assert pane.getParent() == this;
                int idx = pane.indexOfComponent(comp);
                assert idx >= 0;
                pane.setTitleAt(idx, name);
            }
        }

        public void setToolTipText(JComponent comp, String text) {
            comp.setToolTipText(text);
            if (singleTab != null) {
                assert singleTab == comp;
                updateWindowToolTip(text);
            } else {
                assert pane.getParent() == this;
                int idx = pane.indexOfComponent(comp);
                assert idx >= 0;
                pane.setToolTipTextAt(idx, text);
            }
        }

        public void setIcon(JComponent comp, Icon icon) {
            if (comp == singleTab) {
                comp.putClientProperty(ICON_PROP, icon);
                return;
            }
            int idx = pane.indexOfComponent(comp);
            if (idx < 0) {
                return;
            }
            comp.putClientProperty(ICON_PROP, icon);
            pane.setIconAt(idx, icon);
            pane.setDisabledIconAt(idx, icon);
        }

        void setToolbarActions(JComponent comp, Action[] toolbarActions) {
            if (toolbarActions != null && toolbarActions.length > 0) {
                if (toolbarActions.length > 5) {
                    throw new IllegalArgumentException("No more than 5 actions allowed in the output window toolbar"); //NOI18N
                }
                comp.putClientProperty(TOOLBAR_ACTIONS_PROP, toolbarActions);
            }
            if (getSelectedTab() == comp) {
                updateToolbar(comp);
            }
        }

        @Override
        public int getPersistenceType() {
            return PERSISTENCE_ALWAYS;
        }

        @Override
        public String preferredID() {
            return "output"; //NOI18N
        }

        @Override
        public String getToolTipText() {
            return getDisplayName();
        }

        @Override
        public void processFocusEvent(FocusEvent fe) {
            super.processFocusEvent(fe);
            if (Boolean.TRUE.equals(getClientProperty("isSliding"))) { //NOI18N
                repaint(200);
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            if (AQUA) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
            if (hasFocus()) {
                Insets ins = getInsets();
                Color col = UIManager.getColor("controlShadow"); //NOI18N
                //Draw *some* focus indication
                if (col == null) {
                    col = java.awt.Color.GRAY;
                }
                g.setColor(col);
                g.drawRect(
                        ins.left + 2,
                        ins.top + 2,
                        getWidth() - (ins.left + ins.right + 4),
                        getHeight() - (ins.top + ins.bottom + 4));
            }
        }

        void updateWindowName(String name) {
            String winName = NbBundle.getMessage(IOWindowImpl.class, "LBL_IO_WINDOW"); //NOI18N
            if (name != null) {
                String newName = NbBundle.getMessage(IOWindowImpl.class, "FMT_IO_WINDOW", new Object[]{winName, name}); //NOI18N
                if (newName.indexOf("<html>") != -1) {
                    newName = Utilities.replaceString(newName, "<html>", ""); //NOI18N
                    setHtmlDisplayName("<html>" + newName); //NOI18N
                } else {
                    setDisplayName(newName);
                    setHtmlDisplayName(null);
                }
            } else {
                setDisplayName(winName);
                setHtmlDisplayName(null);
            }

        }

        void updateWindowToolTip(String toolTipText) {
            setToolTipText(toolTipText == null ? NbBundle.getMessage(IOWindowImpl.class, "LBL_IO_WINDOW") : toolTipText);
        }

        private void updateToolbar(JComponent comp) {
            toolbar.removeAll();
            if (comp != null) {
                JButton[] buttons = getTabButtons(comp);
                for (int i = 0; i < buttons.length; i++) {
                    toolbar.add(buttons[i]);
                }
            }
            toolbar.revalidate();
            toolbar.repaint();
        }

        JButton[] getTabButtons(JComponent comp) {
            JButton[] buttons = (JButton[]) comp.getClientProperty(TOOLBAR_BUTTONS_PROP);
            if (buttons != null) {
                return buttons;
            }
            Action[] actions = (Action[]) comp.getClientProperty(TOOLBAR_ACTIONS_PROP);
            if (actions == null) {
                return new JButton[0];
            }

            buttons = new JButton[actions.length];
            for (int i=0; i < buttons.length; i++) {
                buttons[i] = new JButton(actions[i]);
                buttons[i].setBorderPainted(false);
                buttons[i].setOpaque(false);
                buttons[i].setText(null);
                buttons[i].putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
                if (actions[i].getValue (Action.SMALL_ICON) == null) {
                    throw new IllegalStateException ("No icon provided for " + actions[i]); //NOI18N
                }
            }
            return buttons;
        }

        

        @Override
        protected void componentActivated() {
            super.componentActivated();
            activated = true;
            JComponent comp = getSelectedTab();
            CallBacks cb = tabToCb.get(comp);
            if (cb != null) {
                cb.activated();
            }
        }

        @Override
        protected void componentDeactivated() {
            super.componentDeactivated();
            activated = false;
            JComponent comp = getSelectedTab();
            CallBacks cb = tabToCb.get(comp);
            if (cb != null) {
                cb.deactivated();
            }
        }

        public void stateChanged(ChangeEvent e) {
            checkTabSelChange();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                JComponent comp = (JComponent) evt.getNewValue();
                removeTab(comp);
            }
        }

        JComponent lastSelTab;
        void checkTabSelChange() {
            JComponent sel = getSelectedTab();
            if (sel != lastSelTab) {
                lastSelTab = sel;
                updateToolbar(sel);
            }
        }

        private void closeOtherTabs() {
            assert pane.getParent() == this;
            JComponent sel = getSelectedTab();
            for (int i = 0; i < pane.getTabCount(); i++) {
                JComponent comp = (JComponent) pane.getComponentAt(0);
                if (comp != sel) {
                    removeTab(comp);
                }
            }
        }

        private void closeAllTabs() {
            for (int i = 0; i < pane.getTabCount(); i++) {
                JComponent comp = (JComponent) pane.getComponentAt(0);
                removeTab(comp);
            }
        }

        private class Close extends AbstractAction {

            public Close() {
                super(NbBundle.getMessage(IOWindowImpl.class, "LBL_Close"));
            }

            public void actionPerformed(ActionEvent e) {
                removeTab(getSelectedTab());
            }
        }

        private class CloseAll extends AbstractAction {

            public CloseAll() {
                super(NbBundle.getMessage(IOWindowImpl.class, "LBL_CloseAll"));
            }

            public void actionPerformed(ActionEvent e) {
                closeAllTabs();
            }
        }

        private class CloseOthers extends AbstractAction {

            public CloseOthers() {
                super(NbBundle.getMessage(IOWindowImpl.class, "LBL_CloseOthers"));
            }

            public void actionPerformed(ActionEvent e) {
                closeOtherTabs();
            }
        }

        private class VariableRightBorder implements Border {

            private JTabbedPane pane;

            public VariableRightBorder(JTabbedPane pane) {
                this.pane = pane;
            }

            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                if (pane.getParent() != IOWindowImpl.this) {
                    Color old = g.getColor();
                    g.setColor(getColor());
                    g.drawLine(x + width - 1, y, x + width - 1, y + height);
                    g.setColor(old);
                }
            }

            public Color getColor() {
                if (Utilities.isMac()) {
                    Color c1 = UIManager.getColor("controlShadow");
                    Color c2 = UIManager.getColor("control");
                    return new Color((c1.getRed() + c2.getRed()) / 2,
                            (c1.getGreen() + c2.getGreen()) / 2,
                            (c1.getBlue() + c2.getBlue()) / 2);
                } else {
                    return UIManager.getColor("controlShadow");
                }
            }

            public Insets getBorderInsets(Component c) {
                if (pane.getParent() == IOWindowImpl.this) {
                    return new Insets(0, 0, 0, 0);
                }
                return new Insets(0, 0, 0, 2);
            }

            public boolean isBorderOpaque() {
                return true;
            }
        }
    }
}
