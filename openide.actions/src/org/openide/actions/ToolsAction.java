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
package org.openide.actions;

import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

import java.beans.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import org.openide.awt.DynamicMenuContent;


/** A "meta-action" that displays (in a submenu) a list of enabled actions provided by modules.
* Such registered actions are called "service actions":
* they are provided externally but seem to provide additional services on existing components.
* Often they will be {@link NodeAction}s or {@link CookieAction}s so that they will
* be enabled based on the node selection, i.e. the node containing this popup.
* It is desirable for most nodes to include this action somewhere in their popup menu.
*
* <p><em>Note:</em> you do not need to touch this class to add a service action!
* Just add the action to a module manifest in an <code>Action</code> section.
*
* <p>The list of registered service actions is provided to this action from the implementation
* by means of {@link ActionManager}.
*
* @author Jaroslav Tulach
*/
public class ToolsAction extends SystemAction implements ContextAwareAction, Presenter.Menu, Presenter.Popup {
    static final long serialVersionUID = 4906417339959070129L;

    // Global ActionManager listener monitoring all available actions
    // and their state
    private static G gl;

    /** Lazy initialization of global listener.
     */
    private static synchronized G gl() {
        if (gl == null) {
            gl = new G();
        }

        return gl;
    }

    /* @return name
    */
    public String getName() {
        return getActionName();
    }

    /* @return help for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ToolsAction.class);
    }

    /* @return menu presenter for the action
    */
    public JMenuItem getMenuPresenter() {
        return new Inline(this);
    }

    /* @return menu presenter for the action
    */
    public JMenuItem getPopupPresenter() {
        return new Popup(this);
    }

    /* Does nothing.
    */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        assert false;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }

    /* @return name
    */
    private static String getActionName() {
        return NbBundle.getMessage(ToolsAction.class, "CTL_Tools");
    }

    /** Implementation method that regenerates the items in the menu or
    * in the array.
    *
    * @param forMenu true if will be presented in menu or false if presented in popup
    * @param list (can be null)
    */
    private static List<JMenuItem> generate(Action toolsAction, boolean forMenu) {
        ActionManager am = ActionManager.getDefault();
        SystemAction[] actions = am.getContextActions();
        List<JMenuItem> list = new ArrayList<JMenuItem>(actions.length);

        boolean separator = false;
        boolean firstItemAdded = false; // flag to prevent adding separator before actual menu items

        // Get action context.
        Lookup lookup;

        if (toolsAction instanceof Lookup.Provider) {
            lookup = ((Lookup.Provider) toolsAction).getLookup();
        } else {
            lookup = null;
        }

        for (Action a : actions) {

            // Retrieve context sensitive action instance if possible.
            if (lookup != null && a instanceof ContextAwareAction) {
                a = ((ContextAwareAction) a).createContextAwareInstance(lookup);
            }

            if (a == null) {
                if (firstItemAdded) {
                    separator = true;
                }
            } else {
                boolean isPopup = (a instanceof Presenter.Popup);
                boolean isMenu = (a instanceof Presenter.Menu);

                if (!((forMenu && isMenu) || (!forMenu && isPopup)) && (isMenu || isPopup)) {
                    continue; // do not call isEnabled on action that is only popup presenter when building menu (i18nPopupAction)
                }

                if (a.isEnabled()) {
                    JMenuItem mi;

                    if (forMenu && isMenu) {
                        mi = ((Presenter.Menu) a).getMenuPresenter();
                    } else if (!forMenu && isPopup) {
                        mi = ((Presenter.Popup) a).getPopupPresenter();
                    } else if (!isMenu && !isPopup) {
                        // Generic Swing action.
                        mi = new JMenuItem();
                        Actions.connect(mi, a, !forMenu);
                    } else {
                        // Should not be here.
                        continue;
                    }

                    if (separator) {
                        list.add(null);
                        separator = false;
                    }

                    list.add(mi);
                    firstItemAdded = true;
                }
            }
        }

        return list;
    }

    //------------------------------------------

    /** @deprecated Useless, see {@link ActionManager}. */
    @Deprecated
    public static void setModel(Model m) {
        throw new SecurityException();
    }

    /** @deprecated Useless, see {@link ActionManager}. */
    @Deprecated
    public static interface Model {
        public SystemAction[] getActions();

        public void addChangeListener(javax.swing.event.ChangeListener l);

        public void removeChangeListener(javax.swing.event.ChangeListener l);
    }

    /** Inline menu that watches model changes only when really needed.
     */
    private static final class Inline extends JMenuItem implements DynamicMenuContent {
        static final long serialVersionUID = 2269006599727576059L;

        /** timestamp of the beginning of the last regeneration */
        private int timestamp = 0;

        /** Associated tools action. */
        private Action toolsAction;

        Inline(Action toolsAction) {
            this.toolsAction = toolsAction;
        }


        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            if (timestamp == gl().getTimestamp()) {
                return items;
            }
            // generate directly list of menu items
            List<JMenuItem> l = generate(toolsAction, true);
            timestamp = gl().getTimestamp();
            return l.toArray(new JMenuItem[l.size()]);
        }
        
        
        public JComponent[] getMenuPresenters() {
            return synchMenuPresenters(new JComponent[0]);
        }        
    }

    //--------------------------------------------------

    /** Inline menu that is either empty or contains one submenu.*/
    private static final class Popup extends JMenuItem implements DynamicMenuContent {
        static final long serialVersionUID = 2269006599727576059L;

        /** sub menu */
        private JMenu menu = new MyMenu();

        /** Associated tools action. */
        private Action toolsAction;

        public Popup(Action toolsAction) {
            super();
            this.toolsAction = toolsAction;
            HelpCtx.setHelpIDString(menu, ToolsAction.class.getName());

        }

        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return gl().isPopupEnabled(toolsAction) ? new JMenuItem[] { menu } : new JMenuItem[0];
        }
        
        
        public JComponent[] getMenuPresenters() {
            return synchMenuPresenters(new JComponent[0]);
        }                


        /** A special menu that will properly update its submenu before posting */
        private class MyMenu extends org.openide.awt.JMenuPlus implements PopupMenuListener {
            /* A popup menu we've attached our listener to.
             * If null, the content is not up-to-date */
            private JPopupMenu lastPopup = null;

            MyMenu() {
                super(getActionName());
            }

            public JPopupMenu getPopupMenu() {
                JPopupMenu popup = super.getPopupMenu();
                fillSubmenu(popup);

                return popup;
            }

            private void fillSubmenu(JPopupMenu pop) {
                if (lastPopup == null) {
                    pop.addPopupMenuListener(this);
                    lastPopup = pop;

                    removeAll();

                    Iterator it = generate(toolsAction, false).iterator();

                    while (it.hasNext()) {
                        java.awt.Component item = (java.awt.Component) it.next();

                        if (item == null) {
                            addSeparator();
                        } else {
                            add(item);
                        }
                    }

                    // also work with empty element
                    if (getMenuComponentCount() == 0) {
                        JMenuItem empty = new JMenuItem(NbBundle.getMessage(ToolsAction.class, "CTL_EmptySubMenu"));
                        empty.setEnabled(false);
                        add(empty);
                    }
                }
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                lastPopup.removePopupMenuListener(this);
                lastPopup = null; // clear the status and stop listening
            }
        }
    }

    //------------------------------------------------
    //----------------------------------------------------------
    private static class G implements PropertyChangeListener {
        public static final String PROP_STATE = "actionsState"; // NOI18N
        private int timestamp = 1;
        private SystemAction[] actions = null;
        private PropertyChangeSupport supp = new PropertyChangeSupport(this);

        public G() {
            ActionManager am = ActionManager.getDefault();
            am.addPropertyChangeListener(this);
            actionsListChanged();
        }

        public final void addPropertyChangeListener(PropertyChangeListener listener) {
            supp.addPropertyChangeListener(listener);
        }

        public final void removePropertyChangeListener(PropertyChangeListener listener) {
            supp.removePropertyChangeListener(listener);
        }

        protected final void firePropertyChange(String name, Object o, Object n) {
            supp.firePropertyChange(name, o, n);
        }

        private void actionsListChanged() {
            timestamp++;

            // deregister all actions listeners
            SystemAction[] copy = actions;

            if (copy != null) {
                for (int i = 0; i < copy.length; i++) {
                    SystemAction act = copy[i];

                    if (act != null) {
                        act.removePropertyChangeListener(this);
                    }
                }
            }

            ActionManager am = ActionManager.getDefault();
            copy = am.getContextActions();

            for (int i = 0; i < copy.length; i++) {
                SystemAction act = copy[i];

                if (act != null) {
                    act.addPropertyChangeListener(this);
                }
            }

            actions = copy;

            firePropertyChange(PROP_STATE, null, null); // tell the world
        }

        private void actionStateChanged() {
            timestamp++;
            firePropertyChange(PROP_STATE, null, null); // tell the world
        }

        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();

            if ((prop == null) || prop.equals(ActionManager.PROP_CONTEXT_ACTIONS)) {
                actionsListChanged();
            } else if (prop.equals(SystemAction.PROP_ENABLED)) {
                actionStateChanged();
            }
        }

        /** Tells if there is any action that is willing to provide
         * Presenter.Popup
         */
        private boolean isPopupEnabled(Action toolsAction) {
            boolean en = false;
            SystemAction[] copy = actions;

            // Get action conext.
            Lookup lookup;

            if (toolsAction instanceof Lookup.Provider) {
                lookup = ((Lookup.Provider) toolsAction).getLookup();
            } else {
                lookup = null;
            }

            for (int i = 0; i < copy.length; i++) {
                // Get context aware action instance if needed.
                Action act;

                // Retrieve context aware action instance if possible.
                if ((lookup != null) && copy[i] instanceof ContextAwareAction) {
                    act = ((ContextAwareAction) copy[i]).createContextAwareInstance(lookup);
                } else {
                    act = copy[i];
                }

                if (act instanceof Presenter.Popup && act.isEnabled()) {
                    en = true;

                    break;
                }
            }

            return en;
        }

        private int getTimestamp() {
            return timestamp;
        }
    }

    /** Delegate tools action. Which act accordingly to current context
     * (represented by lookup). */
    private static final class DelegateAction extends Object implements Action, Presenter.Menu, Presenter.Popup,
        Lookup.Provider {
        private ToolsAction delegate;
        private Lookup lookup;

        /** support for listeners */
        private PropertyChangeSupport support = new PropertyChangeSupport(this);

        public DelegateAction(ToolsAction delegate, Lookup actionContext) {
            this.delegate = delegate;
            this.lookup = actionContext;
        }

        /** Overrides superclass method, adds delegate description. */
        public String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Implements <code>Lookup.Provider</code>. */
        public Lookup getLookup() {
            return lookup;
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
        }

        public void putValue(String key, Object o) {
        }

        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        public boolean isEnabled() {
            // Irrelevant see G#isPopupEnabled(..).
            return delegate.isEnabled();
        }

        public void setEnabled(boolean b) {
            // Irrelevant see G#isPopupEnabled(..).
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        /** Implements <code>Presenter.Menu</code>. */
        public javax.swing.JMenuItem getMenuPresenter() {
            return new Inline(this);
        }

        /** Implements <code>Presenter.Popup</code>. */
        public javax.swing.JMenuItem getPopupPresenter() {
            return new ToolsAction.Popup(this);
        }
    }
     // End of DelegateAction.
}
