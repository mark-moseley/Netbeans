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

package org.netbeans.modules.projectimport.j2seimport.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf, Radek Matous
 */
public abstract class BasicWizardPanel implements WizardDescriptor.Panel, PropertyChangeListener {
    
    private boolean valid = true;
    private WizardDescriptor settings;
    
    private EventListenerList listeners = new EventListenerList();
    
    protected BasicWizardPanel(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    public void setSettings(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    protected WizardDescriptor getSettings() {
        return settings;
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(ChangeListener.class, l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(ChangeListener.class, l);
    }
    
    protected void fireChange() {
        ChangeListener[] chListeners = (ChangeListener[]) listeners.
                getListeners(ChangeListener.class);
        ChangeEvent e = new ChangeEvent(this);
        for (int i = 0; i < chListeners.length; i++) {
            chListeners[i].stateChanged(e);
        }
    }
        
    public HelpCtx getHelp() {
        return null;
    }
    
    public void storeSettings(Object settings) {}
    
    public void readSettings(Object settings) {}
    
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Mainly for receiving events from wrapped component about its validity.
     * Firing events further to Wizard descriptor so it will reread this panel's
     * state and reenable/redisable its next/prev/finish/... buttons.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("valid".equals(evt.getPropertyName())) { // NOI18N
            boolean nueValid = ((Boolean) evt.getNewValue()).booleanValue();
            if (nueValid != valid) {
                valid = nueValid;
                fireChange();
            }
        }
    }    
}