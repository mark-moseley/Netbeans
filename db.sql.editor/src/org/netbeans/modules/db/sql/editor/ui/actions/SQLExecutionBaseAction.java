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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Andrei Badea
 */
public abstract class SQLExecutionBaseAction extends AbstractAction implements ContextAwareAction, HelpCtx.Provider {

    public SQLExecutionBaseAction() {
        initialize();
        
        // allow subclasses to set the name for the "master" action
        if (getValue(Action.NAME) == null) {
            putValue(Action.NAME, getDisplayName(null));
        }
        String iconBase = getIconBase();
        if (iconBase != null) {
            putValue("iconBase", iconBase);
        }
    }
    
    protected void initialize() {
        // allows subclasses to e.g. set noIconInMenu
    }

    protected abstract String getDisplayName(SQLExecution sqlExecution);

    protected String getIconBase() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(SQLExecution sqlExecution) {
        return !sqlExecution.isExecuting();
    }

    protected abstract void actionPerformed(SQLExecution sqlExecution);

    public void actionPerformed(ActionEvent e) {
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAwareDelegate(this, actionContext);
    }

    public static void notifyNoDatabaseConnection() {
        String message = NbBundle.getMessage(SQLExecutionBaseAction.class, "LBL_NoDatabaseConnection");
        NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(desc);
    }

    static class ContextAwareDelegate extends AbstractAction implements Presenter.Toolbar, HelpCtx.Provider {

        private final SQLExecutionBaseAction parent;
        private final Lookup.Result result;

        private SQLExecution sqlExecution;
        private PropertyChangeListener listener;

        public ContextAwareDelegate(SQLExecutionBaseAction parent, Lookup actionContext) {
            this.parent = parent;

            result = actionContext.lookup(new Lookup.Template(SQLExecution.class));
            result.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    ContextAwareDelegate.this.resultChanged();
                }
            });
            resultChanged();
        }

        protected synchronized void setSQLExecution(SQLExecution sqlExecution) {
            this.sqlExecution = sqlExecution;
        }

        protected synchronized SQLExecution getSQLExecution() {
            return sqlExecution;
        }

        private synchronized void resultChanged() {
            if (sqlExecution != null) {
                sqlExecution.removePropertyChangeListener(listener);
            }

            Iterator iterator = result.allInstances().iterator();
            if (iterator.hasNext()) {
                setSQLExecution((SQLExecution)iterator.next());
                listener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        propertyChanged(evt.getPropertyName());
                    }
                };
                sqlExecution.addPropertyChangeListener(listener);
                propertyChanged(null);

                if (iterator.hasNext()) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Multiple SQLExecution instances in the action context. Will only use the first one."); // NOI18N
                }
            } else {
                setSQLExecution(null);
                listener = null;
                propertyChanged(null);
            }
        }

        private void propertyChanged(String propertyName) {
            if (propertyName == null || SQLExecution.PROP_EXECUTING.equals(propertyName)) {
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        boolean enabled = false;
                        SQLExecution sqlExecution = getSQLExecution();
                        if (sqlExecution != null) {
                            enabled = parent.enable(sqlExecution);
                        }
                        String name = parent.getDisplayName(sqlExecution);

                        setEnabled(enabled);
                        putValue(Action.NAME, name);
                    }
                });
            }
        }

        public void actionPerformed(ActionEvent e) {
            SQLExecution sqlExecution = getSQLExecution();
            if (sqlExecution != null) {
                parent.actionPerformed(sqlExecution);
            }
        }

        public Object getValue(String key) {
            Object value = super.getValue(key);
            if (value == null) {
                value = parent.getValue(key);
            }
            return value;
        }

        public HelpCtx getHelpCtx() {
            return parent.getHelpCtx();
        }

        public Component getToolbarPresenter() {
            return new Actions.ToolbarButton(this);
        }
    }
}
