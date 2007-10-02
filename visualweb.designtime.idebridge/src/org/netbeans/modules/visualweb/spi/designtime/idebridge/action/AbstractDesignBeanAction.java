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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.visualweb.spi.designtime.idebridge.action;


import com.sun.rave.designtime.DesignBean;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;


/**
 * An action which operates on <code>DesignBean</code> context. The subclasses
 * need to implement the <code>isEnabled</code>, <code>perfomAction</code>
 * and also <code>getDisplayName</code> and <code>getIconBase</code> methods
 * taking the <code>DesignBean</code> array as an argument.
 * If they wish to implement different than standard menu, toolbar or popup
 * presenter, they can just override the corresponding method
 * <code>getMenuPresenter</code>, <code>getPopupPresenter</code>
 * or <code>getToolbarPresenter</code>.
 *
 * @author Peter Zavadsky
 */
public abstract class AbstractDesignBeanAction extends AbstractAction implements ContextAwareAction {


    /** Name of property designBeans. */
    protected static final String PROP_DESIGN_BEANS = "designBeans"; // NOI18N


    /** Creates a new instance of AbstractDesignBeanAction */
    public AbstractDesignBeanAction() {
        // #6485313 To show the display name in the options dialog.
        putValue(NAME, getDisplayName(new DesignBean[0]));
    }


    /** This method is not used, only the context aware instance are in the game,
     * this instance is only a placeholder in layer (or nodes). */
    public final void actionPerformed(ActionEvent evt) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalStateException("This can't be called directly, it is just a placeholder," // NOI18N
                + " the context aware instance has to be used.")); // NOI18N
    }

    /** Creates the context aware instance working in the specific context. */
    public final Action createContextAwareInstance(Lookup context) {
        return new DelegateAction(this, context);
    }

    /** Gets action display name based on provided beans. */
    protected abstract String getDisplayName(DesignBean[] designBeans);

    /** Gets icon base based on provided beans, in the form "com/mycompany/myIcon.gif".
     * @see org.openide.awt.Actions#connect(AbstractButton, Action) */
    protected abstract String getIconBase(DesignBean[] designBeans);

    /** Implement in order to enable/disable the action based on provided beans. */
    protected abstract boolean isEnabled(DesignBean[] designBeans);

    /** Implement to perform the action based on provided beans. */
    protected abstract void performAction(DesignBean[] designBeans);


    // Presenters
    /** Override if you wish to change the default presenter of the context aware action. */
    protected JMenuItem getMenuPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.MenuItem(contextAwareAction, true);
    }

    /** Override if you wish to change the default presenter of the context aware action. */
    protected JMenuItem getPopupPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.MenuItem(contextAwareAction, false);
    }

    /** Override if you wish to change the default presenter of the context aware action. */
    protected Component getToolbarPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.ToolbarButton(contextAwareAction);
    }
    
    
    protected static final DesignBean[] getDesignBeans(Lookup.Result result) {
        Node[] nodes = getNodes(result);
        List designBeans = new ArrayList();

        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            DesignBean bean = (DesignBean)node.getLookup().lookup(DesignBean.class);
            if (bean != null && !designBeans.contains(bean)) {
                designBeans.add(bean);
            }
        }
        return (DesignBean[])designBeans.toArray(new DesignBean[designBeans.size()]);
    }

    private static final Node[] getNodes(Lookup.Result result) {
        Collection col = result.allInstances();
        return (Node[])col.toArray(new Node[col.size()]);
    }
    

    /** Context aware implementation. */
    private static class DelegateAction implements Action, LookupListener, Presenter.Menu, Presenter.Popup, Presenter.Toolbar {
        
        /** <code>AbstractDesignBeanAction</code> to delegate to. */
        private final AbstractDesignBeanAction delegate;

        /** <code>Lookup.Result</code> we are associated with. */
        private final Lookup.Result result;

        /** State of enabled. */
        private boolean enabled = true;

        /** Support for listeners */
        private final PropertyChangeSupport support = new PropertyChangeSupport(this);

        
        public DelegateAction(AbstractDesignBeanAction delegate, Lookup actionContext) {
            this.delegate = delegate;

            this.result = actionContext.lookup(new Lookup.Template(Node.class));
            this.result.addLookupListener(
                (LookupListener)WeakListeners.create(LookupListener.class, this, this.result)
            );
            resultChanged(null);
        }

        /** Overrides superclass method, adds delegate description. */
        public String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Invoked when an action occurs. */
        public void actionPerformed(ActionEvent evt) {
            delegate.performAction(getDesignBeans(result));
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        public void putValue(String key, Object object) {
            delegate.putValue(key, object);
        }

        public Object getValue(String key) {
            // XXX Delegating display name and icon base to the
            // context sensitive methods.
            if (Action.NAME.equals(key)) {
                return delegate.getDisplayName(getDesignBeans(result));
            } else if ("iconBase".equals(key)) { // NOI18N
                return delegate.getIconBase(getDesignBeans(result));
            } else {
                return delegate.getValue(key);
            }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            if (this.enabled == enabled) {
                return;
            }
            
            this.enabled = enabled;
            support.firePropertyChange("enabled", !enabled, enabled); // NOI18N
        }

        public void resultChanged(LookupEvent evt) {
            DesignBean[] designBeans = getDesignBeans(result);
            
            setEnabled(delegate.isEnabled(designBeans));
            
            support.firePropertyChange(PROP_DESIGN_BEANS, null, designBeans);
        }

        public JMenuItem getMenuPresenter() {
            return delegate.getMenuPresenter(this, result);
        }

        public JMenuItem getPopupPresenter() {
            return delegate.getPopupPresenter(this, result);
        }

        public Component getToolbarPresenter() {
            return delegate.getToolbarPresenter(this, result);
        }
    } // End of DelegateAction.
            
}
