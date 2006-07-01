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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.logging.Logger;
import org.openide.util.WeakSet;

/** An action which may be called programmatically.
* Typically a presenter will call its {@link #performAction} method,
* which must be implemented.
* <p>Provides default presenters using the <a href="@org-openide-awt@/org/openide/awt/Actions.html">Actions</a> utility class.
*
* @author   Ian Formanek, Jaroslav Tulach, Jan Jancura, Petr Hamernik
*/
public abstract class CallableSystemAction extends SystemAction implements Presenter.Menu, Presenter.Popup,
    Presenter.Toolbar {
    /** serialVersionUID */
    static final long serialVersionUID = 2339794599168944156L;

    // ASYNCHRONICITY
    // Adapted from org.netbeans.core.ModuleActions by jglick

    /**
     * Set of action classes for which we have already issued a warning that
     * {@link #asynchronous} was not overridden to return false.
     */
    private static final Set<Class> warnedAsynchronousActions = new WeakSet<Class>(); 
    private static final boolean DEFAULT_ASYNCH = !Boolean.getBoolean(
            "org.openide.util.actions.CallableSystemAction.synchronousByDefault"
        );

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return org.netbeans.modules.openide.util.AWTBridge.getDefault().createMenuPresenter(this);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a Popup Menu.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getPopupPresenter() {
        return org.netbeans.modules.openide.util.AWTBridge.getDefault().createPopupPresenter(this);
    }

    /* Returns a Component that presents the Action, that implements this
    * interface, in a ToolBar.
    * @return the Component representation for the Action
    */
    public java.awt.Component getToolbarPresenter() {
        return org.netbeans.modules.openide.util.AWTBridge.getDefault().createToolbarPresenter(this);
    }

    /** Actually perform the action.
    * This is the method which should be called programmatically.
    * Presenters in <a href="@org-openide-awt@/org/openide/awt/Actions.html">Actions</a> use this.
    * <p>See {@link SystemAction#actionPerformed} for a note on
    * threading usage: in particular, do not access GUI components
    * without explicitly asking for the AWT event thread!
    */
    public abstract void performAction();

    /* Implementation of method of javax.swing.Action interface.
    * Delegates the execution to performAction method.
    *
    * @param ev the action event
    */
    public void actionPerformed(ActionEvent ev) {
        if (isEnabled()) {
            org.netbeans.modules.openide.util.ActionsBridge.doPerformAction(
                this,
                new org.netbeans.modules.openide.util.ActionsBridge.ActionRunnable(ev, this, asynchronous()) {
                    public void run() {
                        performAction();
                    }
                }
            );
        } else {
            // Should not normally happen.
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * If true, this action should be performed asynchronously in a private thread.
     * If false, it will be performed synchronously as called in the event thread.
     * <p>The default value is true for compatibility reasons; subclasses are strongly
     * encouraged to override it to be false, and to either do their work promptly
     * in the event thread and return, or to somehow do work asynchronously (for example
     * using {@link RequestProcessor#getDefault}).
     * <p class="nonnormative">You may currently set the global default to false
     * by setting the system property
     * <code>org.openide.util.actions.CallableSystemAction.synchronousByDefault</code>
     * to <code>true</code>.</p>
     * <p class="nonnormative">When true, the current implementation also provides for a wait cursor during
     * the execution of the action. Subclasses which override to return false should
     * consider directly providing a wait or busy cursor if the nature of the action
     * merits it.</p>
     * @return true if this action should automatically be performed asynchronously
     * @since 4.11
     */
    protected boolean asynchronous() {
        if (warnedAsynchronousActions.add(getClass())) {
            Logger.getAnonymousLogger().warning(
                "Warning - " + getClass().getName() +
                " should override CallableSystemAction.asynchronous() to return false"
            );
        }

        return DEFAULT_ASYNCH;
    }
}
