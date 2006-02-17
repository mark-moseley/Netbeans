/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Basic wizard panel for APISupport projects.
 *
 * @author Martin Krauskopf
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
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    protected final String getMessage(String key) {
        return NbBundle.getMessage(getClass(), key);
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
    
    abstract static class NewTemplatePanel extends BasicWizardPanel {
        
        private final NewModuleProjectData data;
        
        NewTemplatePanel(final NewModuleProjectData data) {
            super(data.getSettings());
            this.data = data;
        }
        
        abstract void reloadData();
        abstract void storeData();
        
        public NewModuleProjectData getData() {
            return data;
        }
        
        public void readSettings(Object settings) {
            reloadData();
        }
        
        public void storeSettings(Object settings) {
            storeData();
        }
        
        protected String getWizardTypeString() {
            String helpId = null;
            switch (data.getWizardType()) {
                case NewNbModuleWizardIterator.TYPE_SUITE:
                    helpId = "suite"; // NOI18N
                    break;
                case NewNbModuleWizardIterator.TYPE_MODULE:
                case NewNbModuleWizardIterator.TYPE_SUITE_COMPONENT:
                    helpId = "module"; // NOI18N
                    break;
                case NewNbModuleWizardIterator.TYPE_LIBRARY_MODULE:
                    helpId = "library"; // NOI18N
                    break;
                default:
                    assert false : "Unknown wizard type = " + data.getWizardType();
            }
            return helpId;
        }
        
    }
    
}