/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.wizard;

import javax.swing.event.*;
import java.lang.reflect.Method;
import org.netbeans.modules.form.*;

/**
 * The third panel of connection wizard - for entering parameters for method
 * which will be called on the target component (specified in step 2).
 *
 * @author Tomas Pavek
 */

class ConnectionWizardPanel3 implements org.openide.WizardDescriptor.Panel {

    private FormModel formModel;
    private Method method;

    private EventListenerList listenerList = null;

    private ConnectionPanel3 uiPanel;

    // --------

    ConnectionWizardPanel3(FormModel model) {
        formModel = model;
    }

    FormModel getFormModel() {
        return formModel;
    }

    void setMethod(Method m) {
        method = m;
        if (uiPanel != null)
            uiPanel.setMethod(m);
    }

    String getParametersText() {
        return uiPanel != null ? uiPanel.getParametersText() : null;
    }

    Object[] getParameters() {
        return uiPanel != null ? uiPanel.getParameters() : null;
    }

    // ---------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (uiPanel == null) {
            uiPanel = new ConnectionPanel3(this);
            if (method != null)
                uiPanel.setMethod(method);
        }
        return uiPanel;
    }

    public org.openide.util.HelpCtx getHelp() {
        return new org.openide.util.HelpCtx("gui.connecting.code"); // NOI18N
    }

    public boolean isValid() {
        return uiPanel != null ? uiPanel.isFilled() : false;
    }

    public void readSettings(java.lang.Object settings) {
    }

    public void storeSettings(java.lang.Object settings) {
    }

    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    // ------

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }
}
