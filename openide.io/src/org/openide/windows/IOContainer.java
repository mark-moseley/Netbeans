/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General License Version 2 only ("GPL") or the Common
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

package org.openide.windows;

import java.awt.Component;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * IOContainer is accessor class to parent container of IO tabs for IOProvider implementations.
 * @since 1.15
 * @author Tomas Holy
 */
public final class IOContainer {

    /**
     * Factory method for IOContainer instances
     * @param provider Provider implemantation
     * @return IOContainer instance
     */
    public static IOContainer create(Provider provider) {
        Parameters.notNull("provider", provider);
        return new IOContainer(provider);
    }

    private static IOContainer defaultIOContainer;
    /**
     * Gets the default container according to a generic {@link Provider}.
     * <p>
     * Normally this is taken from {@link Lookup#getDefault} but if there is no
     * instance in lookup, a fallback instance is created which can be useful for
     * unit tests and perhaps for standalone usage of various libraries.
     * @return a generic container
     */
    public static IOContainer getDefault() {
        if (defaultIOContainer == null) {
            Provider provider = Lookup.getDefault().lookup(Provider.class);
            if (provider == null) {
                provider = new Trivial();
            }
            defaultIOContainer = create(provider);
        }
        return defaultIOContainer;
    }

    /** private constructor */
    private IOContainer(Provider provider) {
        this.provider = provider;
    }

    private Provider provider;

    /**
     * Opens parent container
     */
    public void open() {
        log("open()");
        provider.open();
    }

    /**
     * Activates parent container
     */
    public void requestActive() {
        log("requestActive()");
        provider.requestActive();
    }

    /**
     * Selects parent container (if it is opened), but does not activate it
     */
    public void requestVisible() {
        log("requestVisible()");
        provider.requestVisible();
    }

    /**
     * Checks if parent container is activated
     * @return true if parent container is activated
     */
    public boolean isActivated() {
        log("isActivated()");
        return provider.isActivated();
    }

    /**
     * Adds component to parent container
     * @param comp component to be added
     * @param cb callbacks for added component or null if not interested in notifications
     * @see CallBacks
     */
    public void add(JComponent comp, CallBacks cb) {
        log("requestVisible()", comp, cb);
        provider.add(comp, cb);
    }

    /**
     * Removes component from parent container
     * @param comp component that should be removed
     */
    public void remove(JComponent comp) {
        log("remove()", comp);
        provider.remove(comp);
    }

    /**
     * Selects component in parent container
     * @param comp component that should be selected
     */
    public void select(JComponent comp) {
        log("select()", comp);
        provider.select(comp);
    }

    /**
     * Gets currently selected component in parent container
     * @return selected tab
     */
    public JComponent getSelected() {
        log("getSelected()");
        return provider.getSelected();
    }

    /**
     * Sets title for provided component
     * @param comp component for which title should be set
     * @param name component title
     */
    public void setTitle(JComponent comp, String name) {
        log("setTitle()", comp, name);
        provider.setTitle(comp, name);
    }

    /**
     * Sets tool tip text for provided component
     * @param comp component for which title should be set
     * @param text component title
     */
    public void setToolTipText(JComponent comp, String text) {
        log("setToolTipText()", comp, text);
        provider.setToolTipText(comp, text);
    }

    /**
     * Sets icon for provided component
     * @param comp component for which icon should be set
     * @param icon component icon
     */
    public void setIcon(JComponent comp, Icon icon) {
        log("setIcon()", comp, icon);
        provider.setIcon(comp, icon);
    }

    /**
     * Sets toolbar actions for provided component
     * @param comp component for which actions should be set
     * @param toolbarActions toolbar actions for component
     */
    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
        log("setToolbarActions()", comp, toolbarActions);
        provider.setToolbarActions(comp, toolbarActions);
    }

    /**
     * Checks whether comp can be closed (e.g. if Close action should be
     * present in component popup menu)
     * @param comp component which should be closeable
     * @return true if component can be closed
     */
    public boolean isCloseable(JComponent comp) {
        log("isCloseable()", comp);
        return provider.isCloseable(comp);
    }

    /**
     * SPI for providers of parent container for IO components (tabs)
     */
    public interface Provider {

        /**
         * Parent container for should be opened
         */
        void open();

        /**
         * Parent container for should be activated
         */
        void requestActive();

        /**
         * Parent container for should be selected (if opened)
         */
        void requestVisible();

        /**
         * Checks whether parent container is activated
         * @return true if activated
         */
        boolean isActivated();

        /**
         * Provided component should be added to parent container
         * @param comp component to add
         * @param cb callbacks for component notifications or null if component does not need notifications
         */
        void add(JComponent comp, CallBacks cb);

        /**
         * Provided component should be removed from parent container
         * @param comp component to remove
         */
        void remove(JComponent comp);

        /**
         * Provided component should be selected
         * @param comp component to select
         */
        void select(JComponent comp);

        /**
         * Currently selected io component should be returned
         * @return currently selected io component or null
         */
        JComponent getSelected();

        /**
         * Should set title for provided component (e.g. tab title)
         * @param comp component for which title should be set
         * @param name component title
         */
        void setTitle(JComponent comp, String name);

        /**
         * Should set title for provided component (e.g. tab title)
         * @param comp component for which title should be set
         * @param text component tool tip text
         */
        void setToolTipText(JComponent comp, String text);

        /**
         * Should set icon for provided component
         * @param comp component for which icon should set
         * @param icon component icon
         */
        void setIcon(JComponent comp, Icon icon);

        /**
         * Should set toolbar actions for provided component
         * @param comp
         * @param toolbarActions toolbar actions for component
         */
        void setToolbarActions(JComponent comp, Action[] toolbarActions);

        /**
         * Checks whether comp can be closed (e.g. if Close action should be
         * present in component popup menu)
         * @param comp component which should be closeable
         * @return true if component can be closed
         */
        boolean isCloseable(JComponent comp);
    }

    /** Callbacks from IOContainer to child component corresponding to IO */
    public interface CallBacks {

        /** tab closed */
        void closed();

        /** tab selected */
        void selected();

        /** parent container activated and tab is selected */
        void activated();

        /** parent container deactivated and tab is selected */
        void deactivated();
    }

    private static final Logger LOGGER = Logger.getLogger(IOContainer.class.getName());

    private synchronized void log(String msg, Object... items) {
        LOGGER.log(Level.FINER, "{0}: {1} {2}", new Object[] {provider.getClass(), msg, Arrays.asList(items)});
        assert SwingUtilities.isEventDispatchThread() : "Should be called from AWT thread.";
    }

    private static class Trivial extends JTabbedPane implements Provider {

        private JFrame frame;

        public void open() {
            if (frame == null) {
                frame = new JFrame();
                frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                frame.add(this);
                if (getTabCount() > 0) {
                    frame.setTitle(getTitleAt(0));
                }
                frame.pack();
            }
            frame.setVisible(true);
        }

        public void requestActive() {
            frame.requestFocus();
        }

        public void requestVisible() {
            frame.setVisible(true);
        }

        public boolean isActivated() {
            return frame.isActive();
        }

        public void add(JComponent comp, CallBacks cb) {
            // XXX ignores callbacks
            add(comp);
        }

        public void remove(JComponent comp) {
            remove((Component) comp);
        }

        public void select(JComponent comp) {
            setSelectedComponent(comp);
        }

        public JComponent getSelected() {
            return (JComponent) getSelectedComponent();
        }

        public void setTitle(JComponent comp, String name) {
            setTitleAt(indexOfComponent(comp), name);
        }

        public void setToolTipText(JComponent comp, String text) {
            setToolTipTextAt(indexOfComponent(comp), text);
        }

        public void setIcon(JComponent comp, Icon icon) {
            setIconAt(indexOfComponent(comp), icon);
        }

        public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
            // XXX unsupported for now; setTabComponentAt could be useful in JDK 6
        }

        public boolean isCloseable(JComponent comp) {
            return true;
        }

    }

}
